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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.AbstractCardIndex;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.model.SearchQuery;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Card search form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardSearchForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * search index component model
     */
    private final IModel<AbstractCardIndex> searchIndexModel;
    /**
     * search state model
     */
    private final IModel<CardStateAndAll> searchStateModel;
    /**
     * search query component model
     */
    private final IModel<String> searchFieldModel;
    /**
     * sensitive component model
     */
    private final IModel<Boolean> sensitiveModel;
    /**
     * search in basket only
     */
    private final IModel<Boolean> inBasketModel;
    /**
     * search in selected catalogs only
     */
    private final Map<Catalog, IModel<Boolean>> inCatalogModel;
    /**
     * mutable query model
     */
    private final IModel<SearchQuery> mutableQuery;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param mutableQuery
     * mutable query model
     */
    public CardSearchForm(final String id, final IModel<SearchQuery> mutableQuery) {
        super(id);
        
        this.mutableQuery = mutableQuery;
        
        // create models
        
        this.searchIndexModel = new Model<AbstractCardIndex>(RetrobiWebConfiguration.getInstance().getDefaultIndex());
        this.searchStateModel = new Model<CardStateAndAll>(CardStateAndAll.ALL);
        this.searchFieldModel = new Model<String>("");
        this.sensitiveModel = new Model<Boolean>(false);
        this.inBasketModel = new Model<Boolean>(false);
        this.inCatalogModel = new HashMap<Catalog, IModel<Boolean>>();
        
        for (final Catalog catalog : Catalog.values()) {
            this.inCatalogModel.put(catalog, Model.of(false));
        }
        
        // prepare select box values
        
        final IModel<List<AbstractCardIndex>> indexListModel = new AbstractReadOnlyModel<List<AbstractCardIndex>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<AbstractCardIndex> getObject() {
                final List<AbstractCardIndex> list = new LinkedList<AbstractCardIndex>();
                
                for (final AbstractCardIndex index : RetrobiWebConfiguration.getInstance().getIndexes()) {
                    if (RetrobiWebSession.get().hasRoleAtLeast(index.getRole())) {
                        list.add(index);
                    }
                }
                
                return list;
            }
        };
        
        final List<CardStateAndAll> stateComboList = new LinkedList<CardStateAndAll>();
        
        stateComboList.add(CardStateAndAll.ALL);
        
        for (final CardState state : CardState.values()) {
            stateComboList.add(new CardStateAndAll(state));
        }
        
        // create select box renderers
        
        final IChoiceRenderer<AbstractCardIndex> sourceComboRenderer = new IChoiceRenderer<AbstractCardIndex>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Object getDisplayValue(final AbstractCardIndex object) {
                return object.getTitle();
            }
            
            @Override
            public String getIdValue(final AbstractCardIndex object, final int index) {
                return object.getName();
            }
        };
        
        // create form input components
        
        final DropDownChoice<AbstractCardIndex> sourceCombo = new DropDownChoice<AbstractCardIndex>(
                "select.index",
                this.searchIndexModel,
                indexListModel,
                sourceComboRenderer);
        
        final DropDownChoice<CardStateAndAll> stateCombo = new DropDownChoice<CardStateAndAll>(
                "select.state",
                this.searchStateModel,
                stateComboList);
        
        final TextField<String> searchField = new TextField<String>(
                "input.query",
                this.searchFieldModel);
        
        final CheckBox sensitiveCheck = new CheckBox(
                "check.sensitive",
                this.sensitiveModel);
        
        final CheckBox inBasketCheck = new CheckBox(
                "check.basket",
                this.inBasketModel);
        
        final ListView<Catalog> inCatalogList = new ListView<Catalog>("list.catalog", Arrays.asList(Catalog.values())) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Catalog> item) {
                item.add(new Label("label", item.getModelObject().getShortTitle()));
                item.add(new CheckBox("check", CardSearchForm.this.inCatalogModel.get(item.getModelObject())));
            }
        };
        
        // setup components
        
        sourceCombo.setNullValid(false);
        stateCombo.setNullValid(false);
        
        // place components
        
        this.add(sourceCombo);
        this.add(stateCombo);
        this.add(searchField);
        this.add(sensitiveCheck);
        this.add(inBasketCheck);
        this.add(inCatalogList);
    }
    
    @Override
    protected void onSubmit() {
        if (this.searchIndexModel.getObject() == null) {
            this.error("Vyberte zdroj, ve kterém chcete vyhledávat.");
            return;
        }
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(this.searchIndexModel.getObject().getRole())) {
            this.error("Nemáte oprávnění použít tento index.");
            return;
        }
        
        if (!SimpleStringUtils.isAcceptableQuery(this.searchFieldModel.getObject(), RetrobiWebSession.get().getUserRole())) {
            this.error("Byl zadán prázdný nebo zakázaný vyhledávací dotaz.");
            return;
        }
        
        // create a new query
        
        Set<Catalog> catalogFilter = null;
        
        for (final Entry<Catalog, IModel<Boolean>> entry : this.inCatalogModel.entrySet()) {
            if ((entry.getValue().getObject() != null) && entry.getValue().getObject()) {
                if (catalogFilter == null) {
                    catalogFilter = new HashSet<Catalog>();
                }
                
                catalogFilter.add(entry.getKey());
            }
        }
        
        final SearchQuery query = new SearchQuery(
                this.searchIndexModel.getObject(),
                this.searchFieldModel.getObject(),
                this.sensitiveModel.getObject(),
                this.inBasketModel.getObject(),
                this.searchStateModel.getObject().getState(),
                catalogFilter);
        
        this.mutableQuery.setObject(query);
    }
    
    /**
     * A class wrapping a card state with a special instance that represents
     * "all" the states. This class was created as a little hack for a
     * DropDownChoice to show a special label for the "all" value.
     * 
     * @author Vojtěch Hordějčuk
     */
    private static class CardStateAndAll implements Serializable {
        /**
         * default serial version
         */
        private static final long serialVersionUID = 1L;
        /**
         * special singleton instance representing "all" the values
         */
        public static final CardStateAndAll ALL = new CardStateAndAll(null);
        /**
         * wrapped card state
         */
        private final CardState state;
        
        /**
         * Creates a new instance.
         * 
         * @param state
         * wrapped state or <code>null</code> for all the states
         */
        public CardStateAndAll(final CardState state) {
            this.state = state;
        }
        
        /**
         * Returns the wrapped state or <code>null</code>.
         * 
         * @return the wrapped state or <code>null</code>
         */
        public CardState getState() {
            return this.state;
        }
        
        @Override
        public String toString() {
            if (this.state == null) {
                return "Všechny";
            }
            
            return this.state.toString();
        }
    }
}
