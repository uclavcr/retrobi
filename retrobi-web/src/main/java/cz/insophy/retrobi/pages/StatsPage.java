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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.form.SelectAttributeForm;
import cz.insophy.retrobi.model.CardCountContainer;
import cz.insophy.retrobi.utils.Triple;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.component.TextLabel;

/**
 * Page with various statistical information.
 * 
 * @author Vojtěch Hordějčuk
 */
public class StatsPage extends AbstractBasicPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public StatsPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
        
        // create and fill the common model
        
        final CardCountContainer counter = new CardCountContainer();
        
        try {
            for (final Triple<Catalog, Integer, Integer> row : RetrobiApplication.db().getAnalystRepository().getImageCount()) {
                counter.setCardCount(row.getFirst(), row.getSecond(), row.getThird());
            }
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
        
        try {
            for (final Tuple<CardState, Integer> row : RetrobiApplication.db().getAnalystRepository().getCardCount()) {
                counter.setCardCount(row.getFirst(), row.getSecond());
            }
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
        
        // ---------
        // PAGE TEXT
        // ---------
        
        this.add(new TextLabel("text.card_count", TextType.L_STATS_CARD_COUNT));
        this.add(new TextLabel("text.image_count", TextType.L_STATS_IMAGE_COUNT));
        this.add(new TextLabel("text.values", TextType.L_STATS_VALUES));
        
        // ----------
        // CARD COUNT
        // ----------
        
        // create components
        
        final ListView<CardState> cardCountList = new ListView<CardState>("card_count.list", counter.getStates()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<CardState> item) {
                item.add(new Label("label.state", item.getModelObject().toString()));
                item.add(new Label("label.count", String.valueOf(counter.getCardCount(item.getModelObject()))));
            }
        };
        
        final Label cardCountTotal = new Label("card_count.total", String.valueOf(counter.getTotalCardCount()));
        
        // place components
        
        this.add(cardCountList);
        this.add(cardCountTotal);
        
        // -----------
        // IMAGE COUNT
        // -----------
        
        // create components
        
        final ListView<Catalog> imageCountHeader = new ListView<Catalog>("image_count.header.list", counter.getCatalogs()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Catalog> item) {
                item.add(new Label("label", item.getModelObject().name()));
            }
        };
        
        final ListView<Catalog> imageCountFooter = new ListView<Catalog>("image_count.footer.list", counter.getCatalogs()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Catalog> item) {
                item.add(new Label("label", String.valueOf(counter.getCardCount(item.getModelObject()))));
            }
        };
        
        final Label imageCountFooterTotal = new Label("image_count.footer.total", String.valueOf(counter.getCardCountWithImage()));
        
        final ListView<Integer> imageCountBody = new ListView<Integer>("image_count.body.list", counter.getImageCounts()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Integer> item) {
                final Label imagesLabel = new Label("label.images", String.valueOf(item.getModelObject()));
                final Label totalLabel = new Label("label.total", String.valueOf(counter.getCardCount(item.getModelObject())));
                
                final ListView<Catalog> list = new ListView<Catalog>("list", counter.getCatalogs()) {
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    protected void populateItem(final ListItem<Catalog> innerItem) {
                        innerItem.add(new Label("label", String.valueOf(counter.getCardCount(
                                innerItem.getModelObject(),
                                item.getModelObject()))));
                    }
                };
                
                item.add(imagesLabel);
                item.add(totalLabel);
                item.add(list);
            }
        };
        
        // place components
        
        this.add(imageCountHeader);
        this.add(imageCountBody);
        this.add(imageCountFooter);
        this.add(imageCountFooterTotal);
        
        // ------
        // VALUES
        // ------
        
        // create models
        
        final IModel<Integer> valuesSumModel = Model.of(0);
        
        // create components
        
        final Label valuesSumLabel = new Label("values.sum", valuesSumModel);
        
        final ListView<Tuple<String, Integer>> valuesList = new ListView<Tuple<String, Integer>>("values.list") {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Tuple<String, Integer>> item) {
                item.add(new Label("label.value", item.getModelObject().getFirst()));
                item.add(new Label("label.count", String.valueOf(item.getModelObject().getSecond())));
            }
        };
        
        final SelectAttributeForm valuesForm = new SelectAttributeForm("values.form", valuesList, valuesSumModel);
        
        // place components
        
        this.add(valuesForm);
        this.add(valuesList);
        this.add(valuesSumLabel);
    }
    
    @Override
    protected String getPageTitle() {
        return "Statistiky";
    }
}
