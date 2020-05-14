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
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cz.insophy.retrobi.Settings;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
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
import cz.insophy.retrobi.pages.SearchPage;
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
     * helper for initializing the search for from the parameters
     */
    private final FormInitializationHelper formInitializationHelper;
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
     * mutable query URL model
     */
    private final IModel<String> mutableQueryUrl;

    /**
     * Creates a new instance.
     *
     * @param id
     * component ID
     * @param mutableQuery
     * mutable query model
     * @param pageParameters
     * page parameters
     */
    public CardSearchForm(final String id, final IModel<SearchQuery> mutableQuery, final PageParameters pageParameters) {
        super(id);

        this.mutableQuery = mutableQuery;
        this.mutableQueryUrl = new Model<>("");

        // create models

        this.formInitializationHelper = new FormInitializationHelper();
        this.searchIndexModel = new Model<AbstractCardIndex>(this.formInitializationHelper.getSearchIndexModelValue(pageParameters));
        this.searchStateModel = new Model<CardStateAndAll>(this.formInitializationHelper.getSearchStateModelValue(pageParameters));
        this.searchFieldModel = new Model<String>(this.formInitializationHelper.getSearchFieldModelValue(pageParameters));
        this.sensitiveModel = new Model<Boolean>(this.formInitializationHelper.getSensitiveModelValue(pageParameters));
        this.inBasketModel = new Model<Boolean>(false);
        this.inCatalogModel = new HashMap<Catalog, IModel<Boolean>>();

        for (final Catalog catalog : Catalog.values()) {
            this.inCatalogModel.put(catalog, Model.of(this.formInitializationHelper.getInCatalogModelValue(pageParameters, catalog)));
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

        final Component searchUrl = new Label("link.external.search", this.mutableQueryUrl) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("href", this.getDefaultModelObjectAsString());
            }

            @Override
            protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
                replaceComponentTagBody(markupStream, openTag, "Odkaz na tyto výsledky");
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
        this.add(searchUrl);
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

        this.mutableQueryUrl.setObject(this.formInitializationHelper.toUrl(this, query));
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final CardStateAndAll that = (CardStateAndAll) o;
            return state == that.state;
        }

        @Override
        public int hashCode() {
            return Objects.hash(state);
        }
    }

    /**
     * Class that handles saving/loading search form state from the page parameters.
     */
    private static class FormInitializationHelper implements Serializable {
        /**
         * default serial version
         */
        private static final long serialVersionUID = 1L;

        // parameter constants

        private static final String PARAM_QUERY = "dotaz";
        private static final String PARAM_CATALOG = "katalog";
        private static final String PARAM_STATE = "stav";
        private static final String PARAM_INDEX = "index";
        private static final String PARAM_IS_SENSITIVE = "citlivost";

        // parameter value constants

        private static final String YES_FLAG = "ano";

        private AbstractCardIndex getSearchIndexModelValue(final PageParameters pageParameters) {
            final String pageParametersIndex = pageParameters.getString(PARAM_INDEX, "");
            final RetrobiWebConfiguration config = RetrobiWebConfiguration.getInstance();
            for (final AbstractCardIndex index : config.getIndexes()) {
                if (index.getName().equalsIgnoreCase(pageParametersIndex)) {
                    return index;
                }
            }
            return config.getDefaultIndex();
        }

        private boolean getInCatalogModelValue(final PageParameters pageParameters, final Catalog catalog) {
            final String pageParametersCatalog = pageParameters.getString(PARAM_CATALOG, "");
            final Set<String> pageParametersCatalogSet = splitOnComma(pageParametersCatalog, Catalog.values().length);
            return pageParametersCatalogSet.contains(catalog.name());
        }

        private CardSearchForm.CardStateAndAll getSearchStateModelValue(final PageParameters pageParameters) {
            final String pageParametersState = pageParameters.getString(PARAM_STATE, "");
            for (CardState state: CardState.values()) {
                if (state.name().equalsIgnoreCase(pageParametersState)) {
                    return new CardStateAndAll(state);
                }
            }
            return CardSearchForm.CardStateAndAll.ALL;
        }

        private String getSearchFieldModelValue(final PageParameters pageParameters) {
            return pageParameters.getString(PARAM_QUERY, "");
        }

        private boolean getSensitiveModelValue(final PageParameters pageParameters) {
            return pageParameters.getString(PARAM_IS_SENSITIVE, "").equalsIgnoreCase(YES_FLAG);
        }

        private static Set<String> splitOnComma(final String value, int limit) {
            return new HashSet<>(Arrays.asList(value.split(Pattern.quote(","), limit)));
        }

        private String toUrl(final Component component, final SearchQuery query) {
            final PageParameters newPageParameters = new PageParameters();

            newPageParameters.put(PARAM_QUERY, query.getQuery());

            if (query.getCatalogFilter() != null) {
                newPageParameters.put(PARAM_CATALOG, query.getCatalogFilter().stream().map(Enum::name).collect(Collectors.joining(",")));
            }

            if (query.getStateFilter() != null) {
                newPageParameters.put(PARAM_STATE, query.getStateFilter().name());
            }

            newPageParameters.put(PARAM_INDEX, query.getIndex().getName());

            if (query.isSensitive()) {
                newPageParameters.put(PARAM_IS_SENSITIVE, YES_FLAG);
            }

            return Settings.SERVER_URL_FOR_EMAIL + component.urlFor(SearchPage.class, newPageParameters).toString();
        }
    }
}
