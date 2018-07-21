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

package cz.insophy.retrobi.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.form.AddAttributeForm;
import cz.insophy.retrobi.form.CleanAttributeForm;
import cz.insophy.retrobi.form.CreateCardForm;
import cz.insophy.retrobi.form.EditBatchForm;
import cz.insophy.retrobi.form.EditNoteForm;
import cz.insophy.retrobi.form.EditUrlForm;
import cz.insophy.retrobi.form.RemoveAttributeForm;
import cz.insophy.retrobi.model.setup.AdminViewMode;
import cz.insophy.retrobi.model.task.SaveBatchModificationTask;
import cz.insophy.retrobi.utils.component.OnClickConfirmer;

/**
 * Page for batch editing cards.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AdminCardsPage extends AbstractAdminPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public AdminCardsPage(final PageParameters parameters) { // NO_UCD
        super(parameters, AdminViewMode.CARDS);
        
        // add components
        
        this.addFormComponents();
        this.addLabelComponents();
        this.addLinkComponents();
    }
    
    /**
     * Adds label components to the page.
     */
    private void addLabelComponents() {
        // create components
        
        final Component okLabel = new Label("label.ok", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                final int size = RetrobiWebSession.get().getCardContainer().getBatchModificationResult().getChangedCardsCount();
                return String.valueOf(size);
            }
        });
        
        final Component failLabel = new Label("label.fail", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                final int size = RetrobiWebSession.get().getCardContainer().getBatchModificationResult().getUnchangedCardsCount();
                return String.valueOf(size);
            }
        });
        
        final Component skipLabel = new Label("label.skip", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                final int size = RetrobiWebSession.get().getCardContainer().getBatchModificationResult().getSkippedCardsCount();
                return String.valueOf(size);
            }
        });
        
        // place components
        
        this.add(okLabel);
        this.add(failLabel);
        this.add(skipLabel);
    }
    
    /**
     * Adds link components to the page.
     */
    private void addLinkComponents() {
        // create components
        
        // ------
        // SAVING
        // ------
        
        final Component okSaveLink = new Link<Object>("link.ok_save") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                try {
                    // save all changed cards
                    
                    RetrobiWebSession.get().scheduleTask(new SaveBatchModificationTask(
                            RetrobiWebSession.get().getCardContainer().getBatchModificationResult(),
                            RetrobiWebSession.get().getLoggedUser()));
                    
                    this.info("Změněné lístky budou uloženy.");
                } catch (final InterruptedException x) {
                    this.error(x.getMessage());
                }
            }
        };
        
        // -------------
        // BASKET ADDING
        // -------------
        
        final Component okToBasketLink = new Link<Object>("link.ok_to_basket") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                // add changed cards to the basket
                
                try {
                    RetrobiWebSession.get().getCardContainer().getBatchModificationResult().putChangedToBasket(
                            RetrobiWebSession.get().getCardContainer(),
                            RetrobiWebSession.get().getBasketLimit());
                    
                    this.info("Změněné lístky byly vloženy do schránky.");
                } catch (final OverLimitException x) {
                    this.error(x.getMessage());
                }
            }
        };
        
        final Component failToBasket = new Link<Object>("link.fail_to_basket") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                // add unchanged cards to the basket
                
                try {
                    RetrobiWebSession.get().getCardContainer().getBatchModificationResult().putUnchangedToBasket(
                            RetrobiWebSession.get().getCardContainer(),
                            RetrobiWebSession.get().getBasketLimit());
                    
                    this.info("Nezměněné lístky byly vloženy do schránky.");
                } catch (final OverLimitException x) {
                    this.error(x.getMessage());
                }
            }
        };
        
        // --------------
        // BASKET REMOVAL
        // --------------
        
        final Component okFromBasketLink = new Link<Object>("link.ok_from_basket") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                // remove changed cards from the basket
                
                RetrobiWebSession.get().getCardContainer().getBatchModificationResult().removeChangedFromBasket(RetrobiWebSession.get().getCardContainer());
                this.info("Změněné lístky byly vyjmuty ze schránky.");
            }
        };
        
        final Component failFromBasket = new Link<Object>("link.fail_from_basket") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                // remove unchanged cards from the basket
                
                RetrobiWebSession.get().getCardContainer().getBatchModificationResult().removeUnchangedFromBasket(RetrobiWebSession.get().getCardContainer());
                this.info("Nezměněné lístky byly vyjmuty ze schránky.");
            }
        };
        
        // -----------
        // CLEAR LINKS
        // -----------
        
        final Component clearAllLink = new Link<Object>("link.clear") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                // clear all unsaved cards
                
                RetrobiWebSession.get().getCardContainer().getBatchModificationResult().clear();
                this.info("Všechny neuložené změny byly zrušeny.");
            }
        };
        
        // setup components
        
        clearAllLink.add(new OnClickConfirmer("Opravdu chcete zrušit všechny neuložené změny?"));
        
        // place components
        
        this.add(okSaveLink);
        this.add(okToBasketLink);
        this.add(okFromBasketLink);
        this.add(failToBasket);
        this.add(failFromBasket);
        this.add(clearAllLink);
    }
    
    /**
     * Adds form components to the page.
     */
    private void addFormComponents() {
        // create components
        
        final Component addForm = new AddAttributeForm("form.add");
        final Component removeForm = new RemoveAttributeForm("form.remove");
        final Component cleanForm = new CleanAttributeForm("form.clean");
        final Component editUrlForm = new EditUrlForm("form.edit_url");
        final Component editNoteForm = new EditNoteForm("form.edit_note");
        
        final Component editBatchForm = new EditBatchForm("form.edit_batch") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
                    // hide if not administrator
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final Component createForm = new CreateCardForm("form.create") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
                    // hide if not administrator
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        // place components
        
        this.add(addForm);
        this.add(removeForm);
        this.add(cleanForm);
        this.add(editBatchForm);
        this.add(editUrlForm);
        this.add(editNoteForm);
        this.add(createForm);
    }
}
