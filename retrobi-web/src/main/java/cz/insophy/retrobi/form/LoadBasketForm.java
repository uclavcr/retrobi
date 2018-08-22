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

import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Cardset;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.Triple;

/**
 * Load basket form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LoadBasketForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * select name model
     */
    private final IModel<Triple<String, String, String>> model;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent navigator panel
     */
    public LoadBasketForm(final String id, final AbstractCardNavigatorPanel parent) {
        super(id);
        
        // create models
        
        this.model = new Model<Triple<String, String, String>>();
        
        final IChoiceRenderer<Triple<String, String, String>> renderer = new IChoiceRenderer<Triple<String, String, String>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Object getDisplayValue(final Triple<String, String, String> object) {
                return object.getThird();
            }
            
            @Override
            public String getIdValue(final Triple<String, String, String> object, final int index) {
                return String.valueOf(index);
            }
        };
        
        // create components
        
        final IModel<List<Triple<String, String, String>>> options = new AbstractReadOnlyModel<List<Triple<String, String, String>>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<Triple<String, String, String>> getObject() {
                if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    try {
                        final String userId = RetrobiWebSession.get().getLoggedUser().getId();
                        return RetrobiApplication.db().getCardsetRepository().getCardsetIds(userId);
                    } catch (final NotFoundRepositoryException x) {
                        LoadBasketForm.this.error(x.getMessage());
                    } catch (final GeneralRepositoryException x) {
                        LoadBasketForm.this.error(x.getMessage());
                    }
                }
                
                return Collections.emptyList();
            }
        };
        
        final DropDownChoice<Triple<String, String, String>> select = new DropDownChoice<Triple<String, String, String>>(
                "select",
                this.model,
                options,
                renderer);
        
        final Button submitLoad = new Button("submit.load") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onSubmit() {
                if (LoadBasketForm.this.model.getObject() == null) {
                    this.error("Vyberte schránku.");
                    return;
                }
                
                try {
                    final Cardset cardset = RetrobiApplication.db().getCardsetRepository().getCardset(LoadBasketForm.this.model.getObject().getFirst());
                    
                    RetrobiWebSession.get().getCardContainer().importBasketFromCardset(cardset, RetrobiWebSession.get().getBasketLimit());
                    
                    parent.requestCardViewerReset();
                } catch (final NotFoundRepositoryException x) {
                    this.error(x.getMessage());
                } catch (final GeneralRepositoryException x) {
                    this.error(x.getMessage());
                } catch (final OverLimitException x) {
                    this.error(x.getMessage());
                }
            }
        };
        
        final Button submitRemove = new Button("submit.remove") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onSubmit() {
                if (LoadBasketForm.this.model.getObject() == null) {
                    this.error("Vyberte schránku.");
                    return;
                }
                
                try {
                    final Cardset cardset = RetrobiApplication.db().getCardsetRepository().getCardset(LoadBasketForm.this.model.getObject().getFirst());
                    
                    RetrobiApplication.db().getCardsetRepository().deleteCardset(cardset);
                } catch (final NotFoundRepositoryException x) {
                    this.error(x.getMessage());
                } catch (final GeneralRepositoryException x) {
                    this.error(x.getMessage());
                }
            }
        };
        
        // setup components
        
        select.setNullValid(true);
        
        // place components
        
        this.add(select);
        this.add(submitLoad);
        this.add(submitRemove);
    }
}
