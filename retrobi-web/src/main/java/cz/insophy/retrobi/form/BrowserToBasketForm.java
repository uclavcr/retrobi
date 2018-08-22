/*
 * Copyright 2012 UCL AV CR v.v.i.
 *
 * This file is part of Retrobi.
 *
 * Retrobi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Retrobi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Retrobi. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.insophy.retrobi.form;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.model.task.AddLetterToBasketTask;
import cz.insophy.retrobi.model.task.RemoveLetterFromBasketTask;
import cz.insophy.retrobi.utils.CardCatalogRange;

/**
 * A form to add/remove a batch of cards to/from basket.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BrowserToBasketForm extends Form<Object> {
    /**
     * Basket form source.
     */
    private static enum BasketFormSource {
        /**
         * card range
         */
        CARDS,
        /**
         * whole batch
         */
        BATCH,
        /**
         * whole letter
         */
        LETTER;
        
        @Override
        public String toString() {
            switch (this.ordinal()) {
                case 0:
                    return "Lístky X až Y";
                case 1:
                    return "Celou skupinu";
                case 2:
                    return "Celé písmeno";
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }
    
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * current catalog
     */
    private final Catalog catalog;
    /**
     * current batch
     */
    private final String batch;
    /**
     * card count
     */
    private final int count;
    /**
     * mutable range model
     */
    private final IModel<CardCatalogRange> mutableRange;
    /**
     * basket form source
     */
    private final IModel<BasketFormSource> source;
    /**
     * left index boundary model
     */
    private final IModel<String> from;
    /**
     * right index boundary model
     */
    private final IModel<String> to;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param mutableRange
     * mutable range model
     */
    public BrowserToBasketForm(final String id, final IModel<CardCatalogRange> mutableRange) {
        super(id);
        
        // prepare models
        
        this.mutableRange = mutableRange;
        this.catalog = this.mutableRange.getObject().getCatalog();
        this.batch = this.mutableRange.getObject().getBatch();
        this.count = this.mutableRange.getObject().getRange().getCount();
        this.source = Model.of(BasketFormSource.CARDS);
        this.from = Model.of(String.valueOf(this.mutableRange.getObject().getRange().getFirstOffset() + 1));
        this.to = Model.of(String.valueOf(this.mutableRange.getObject().getRange().getLastOffset() + 1));
        
        // create components
        
        final TextField<String> fromField = new TextField<String>("input.from", this.from) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (!BasketFormSource.CARDS.equals(BrowserToBasketForm.this.source.getObject())) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final TextField<String> toField = new TextField<String>("input.to", this.to)
        {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return fromField.isVisible() && super.isVisible();
            }
        };
        
        final DropDownChoice<BrowserToBasketForm.BasketFormSource> sourceSelect = new DropDownChoice<BrowserToBasketForm.BasketFormSource>(
                "select.source",
                this.source,
                Arrays.asList(BasketFormSource.values())) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSelectionChanged(final BrowserToBasketForm.BasketFormSource newSelection) {
                // NOP
            }
            
            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }
        };
        
        final Button addSubmit = new Button("submit.add") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onSubmit() {
                switch (BrowserToBasketForm.this.source.getObject())
                {
                    case CARDS:
                        BrowserToBasketForm.this.addCards();
                        break;
                    case BATCH:
                       BrowserToBasketForm.this.addBatch();
                       break;
                   case LETTER:
                        BrowserToBasketForm.this.addLetter();
                        break;
                    default:
                        throw new IndexOutOfBoundsException();
                }
            }
        };
        
        final Button removeSubmit = new Button("submit.remove") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onSubmit() {
                switch (BrowserToBasketForm.this.source.getObject())
                {
                    case CARDS:
                        BrowserToBasketForm.this.removeCards();
                        break;
                    case BATCH:
                        BrowserToBasketForm.this.removeBatch();
                        break;
                    case LETTER:
                        BrowserToBasketForm.this.removeLetter();
                        break;
                    default:
                        throw new IndexOutOfBoundsException();
                }
            }
        };
        
        // place components
        
        this.add(sourceSelect);
        this.add(fromField);
        this.add(toField);
        this.add(addSubmit);
        this.add(removeSubmit);
    }
    
    /**
     * Sets the boundaries according to the current range.
     */
    public void updateByRange() {
        this.from.setObject(String.valueOf(this.mutableRange.getObject().getRange().getFirstOffset() + 1));
        this.to.setObject(String.valueOf(this.mutableRange.getObject().getRange().getLastOffset() + 1));
    }
    
    /**
     * Adds the card range specified to the basket.
     */
    private void addCards() {
        if (this.validateRange()) {
            try {
                final List<String> cardIds = this.getCardsInRange();
                RetrobiWebSession.get().getCardContainer().addToBasket(cardIds, RetrobiWebSession.get().getBasketLimit());
                this.info("Počet přidaných lístků: " + cardIds.size());
            } catch (final GeneralRepositoryException x) {
                this.error(x.getMessage());
            } catch (final OverLimitException x) {
                this.error(x.getMessage());
            }
        }
    }
    
    /**
     * Removes the card range specified from the basket.
     */
    private void removeCards() {
        if (this.validateRange()) {
            try {
                final List<String> cardIds = this.getCardsInRange();
                RetrobiWebSession.get().getCardContainer().removeFromBasket(cardIds);
                this.info("Počet vyjmutých lístků: " + cardIds.size());
            } catch (final GeneralRepositoryException x) {
                this.error(x.getMessage());
            }
        }
    }
    
    /**
     * Adds the whole batch to the basket.
     */
    private void addBatch() {
        try {
            final List<String> cardIds = RetrobiApplication.db().getCardRepository().getCardIdsInBatch(this.catalog, this.batch);
            RetrobiWebSession.get().getCardContainer().addToBasket(cardIds, RetrobiWebSession.get().getBasketLimit());
            this.info("Skupina byla vložena do schránky.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final OverLimitException x) {
            this.error(x.getMessage());
        }
    }
    
    /**
     * Removes the whole batch from the basket.
     */
    private void removeBatch() {
        try {
            final List<String> cardIds = RetrobiApplication.db().getCardRepository().getCardIdsInBatch(this.catalog, this.batch);
            RetrobiWebSession.get().getCardContainer().removeFromBasket(cardIds);
            this.info("Skupina byla vyjmuta ze schránky.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
    }
    
    /**
     * Adds the whole letter to the basket.
     */
    private void addLetter() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.error("Celé písmeno může do schránky přidat pouze přihlášený uživatel.");
            return;
        }
        
        try {
            RetrobiWebSession.get().scheduleTask(new AddLetterToBasketTask(
                    this.catalog,
                    CardCatalogModel.getInstance().getLetterOfBatch(this.batch),
                    RetrobiWebSession.get().getCardContainer(),
                    RetrobiWebSession.get().getBasketLimit()));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
    
    /**
     * Removes the whole letter from the basket.
     */
    private void removeLetter() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.error("Celé písmeno může ze schránky vyjmout pouze přihlášený uživatel.");
            return;
        }
        
        try {
            RetrobiWebSession.get().scheduleTask(new RemoveLetterFromBasketTask(
                    this.catalog,
                    CardCatalogModel.getInstance().getLetterOfBatch(this.batch),
                    RetrobiWebSession.get().getCardContainer()));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
    
    /**
     * Returns the cards in the specified range.
     * 
     * @return list of card IDs
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private List<String> getCardsInRange() throws GeneralRepositoryException {
        final int ifrom = Integer.valueOf(this.from.getObject());
        final int ito = Integer.valueOf(this.to.getObject());
        
        return RetrobiApplication.db().getCardRepository().getCardIdsInRange(
                this.catalog,
                this.batch,
                ifrom - 1,
                ito - 1);
    }
    
    /**
     * Validates the range specified.
     * 
     * @return <code>true</code> if the range is valid, <code>false</code>
     * otherwise
     */
    private boolean validateRange() {
        if ((this.from.getObject() == null) || (this.to.getObject() == null)) {
            this.error("Zadejte prosím rozsah lístků.");
            return false;
        }
        
        final int ifrom;
        final int ito;
        
        try {
            ifrom = Integer.valueOf(this.from.getObject());
            ito = Integer.valueOf(this.to.getObject());
        } catch (final NumberFormatException x) {
            this.error("Do polí s rozsahem zadejte čísla.");
            return false;
        }
        
        if (ifrom > ito) {
            this.error("Zadejte prosím platný rozsah lístků.");
            return false;
        }
        
        if ((ifrom < 1) || (ito < 1) || (ito > this.count)) {
            this.error("Rozsah je mimo platné hodnoty.");
            return false;
        }
        
        return true;
    }
}
