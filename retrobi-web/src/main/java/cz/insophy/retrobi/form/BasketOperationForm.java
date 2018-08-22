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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.Direction;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.CardRange;

/**
 * Basket operation form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasketOperationForm extends Form<Object> {
    /**
     * Batch operation.
     */
    private static enum BatchOperation {
        /**
         * remove from batch
         */
        REMOVE,
        /**
         * move before a card
         */
        MOVE_BEFORE,
        /**
         * move after a card
         */
        MOVE_AFTER;
        
        @Override
        public String toString() {
            switch (this.ordinal()) {
                case 0:
                    return "Vyjmout";
                case 1:
                    return "Přesun před lístek č.";
                case 2:
                    return "Přesun za lístek č.";
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
     * parent panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * card range
     */
    private final IModel<CardRange> range;
    /**
     * first card index model
     */
    private final IModel<String> from;
    /**
     * last card index model
     */
    private final IModel<String> to;
    /**
     * target index model
     */
    private final IModel<String> target;
    /**
     * operation model
     */
    private final IModel<BatchOperation> operation;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent panel
     * @param mutableRangeModel
     * mutable range model
     */
    public BasketOperationForm(final String id, final AbstractCardNavigatorPanel parent, final IModel<CardRange> mutableRangeModel) {
        super(id);
        
        // prepare models
        
        this.parent = parent;
        this.range = mutableRangeModel;
        this.from = Model.of(String.valueOf(1));
        this.to = Model.of(String.valueOf(1));
        this.target = Model.of((String) null);
        this.operation = Model.of((BasketOperationForm.BatchOperation) null);
        
        // create components
        
        final TextField<String> fromField = new TextField<String>("input.from", this.from);
        final TextField<String> toField = new TextField<String>("input.to", this.to);
        final TextField<String> targetField = new TextField<String>("input.target", this.target) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (BatchOperation.REMOVE.equals(BasketOperationForm.this.operation.getObject())) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final DropDownChoice<BasketOperationForm.BatchOperation> operationSelect = new DropDownChoice<BasketOperationForm.BatchOperation>(
                "select.operation",
                this.operation,
                Arrays.asList(BatchOperation.values()))
        {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSelectionChanged(final BasketOperationForm.BatchOperation newSelection) {
                // NOP
            }
            
            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }
        };
        
        // place components
        
        this.add(fromField);
        this.add(toField);
        this.add(targetField);
        this.add(operationSelect);
        
        // update range
        
        this.updateByRange();
    }
    
    /**
     * Sets the boundaries according to the current range.
     */
    public void updateByRange() {
        this.from.setObject(String.valueOf(this.range.getObject().getFirstOffset() + 1));
        this.to.setObject(String.valueOf(this.range.getObject().getLastOffset() + 1));
        this.operation.setObject(BatchOperation.REMOVE);
        this.target.setObject(null);
    }
    
    @Override
    protected void onSubmit() {
        if (!this.validateOperation()) {
            return;
        }
        
        if (!this.validateRange()) {
            return;
        }
        
        switch (this.operation.getObject()) {
            case MOVE_AFTER:
                // move cards after
                
                if (!this.validateTarget()) {
                    return;
                }
                
                RetrobiWebSession.get().getCardContainer().moveInBasket(
                        this.getCardsInRange(),
                        this.getTargetCard(),
                        Direction.DOWN);
                
                this.parent.requestCardViewerUpdate();
                break;
            case MOVE_BEFORE:
                // move cards before
                
                if (!this.validateTarget()) {
                    return;
                }
                
                RetrobiWebSession.get().getCardContainer().moveInBasket(
                        this.getCardsInRange(),
                        this.getTargetCard(),
                        Direction.UP);
                
                this.parent.requestCardViewerUpdate();
                break;
            case REMOVE:
                // remove cards
                
                RetrobiWebSession.get().getCardContainer().removeFromBasket(
                        this.getCardsInRange());
                
                this.parent.requestCardViewerReset();
                break;
            default:
                // NOP
                break;
        }
        
        // update by the current range
        
        this.updateByRange();
    }
    
    /**
     * Returns the cards in the specified range.
     * 
     * @return list of card IDs
     */
    private List<String> getCardsInRange() {
        try {
            // get boundaries
            
            final int ifrom = Integer.valueOf(this.from.getObject());
            final int ito = Integer.valueOf(this.to.getObject());
            
            // create a list
            
            final List<String> basketCardIds = RetrobiWebSession.get().getCardContainer().getBasketCardIds();
            return new ArrayList<String>(basketCardIds.subList(ifrom - 1, ito));
        } catch (final NumberFormatException x) {
            this.error("Chybný formát čísla.");
            return Collections.emptyList();
        }
    }
    
    /**
     * Returns the target card ID.
     * 
     * @return target card ID
     */
    private String getTargetCard() {
        final int itarget = Integer.valueOf(this.target.getObject());
        return RetrobiWebSession.get().getCardContainer().getBasketCardIds().get(itarget - 1);
    }
    
    /**
     * Validates the operation.
     * 
     * @return <code>true</code> if the operation selection is valid,
     * <code>false</code> otherwise
     */
    private boolean validateOperation() {
        if (this.operation.getObject() == null) {
            this.error("Vyberte prosím operaci.");
            return false;
        }
        
        return true;
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
        
        if ((ifrom < 1) || (ito < 1) || (ito > RetrobiWebSession.get().getCardContainer().getBasketSize())) {
            this.error("Rozsah je mimo platné hodnoty.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates the target.
     * 
     * @return <code>true</code> if the target is valid, <code>false</code>
     * otherwise
     */
    private boolean validateTarget() {
        if (this.target.getObject() == null) {
            this.error("Zadejte prosím cílový lístek.");
            return false;
        }
        
        final int itarget;
        
        try {
            itarget = Integer.valueOf(this.target.getObject());
        } catch (final NumberFormatException x) {
            this.error("Do pole cíl zadejte číslo.");
            return false;
        }
        
        if ((itarget < 1) || (itarget > RetrobiWebSession.get().getCardContainer().getBasketSize())) {
            this.error("Cíl je mimo rozsah.");
            return false;
        }
        
        return true;
    }
}
