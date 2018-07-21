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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.utils.component.PagedLazyListView;

/**
 * Form used to change page in paged list views.
 * 
 * @author Vojtěch Hordějčuk
 */
public class PageSkipForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param target
     * target paged list view
     */
    public PageSkipForm(final String id, final PagedLazyListView<?> target) {
        super(id);
        
        // create models
        
        final IModel<String> countModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return String.valueOf(target.getMaxPage() + 1);
            }
        };
        
        final IModel<String> pageModel = new IModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return String.valueOf(target.getCurrentPage() + 1);
            }
            
            @Override
            public void setObject(final String object) {
                if (object != null) {
                    final int newPage = Integer.valueOf(object);
                    target.setCurrentPage(newPage - 1);
                }
            }
            
            @Override
            public void detach() {
                // NOP
            }
        };
        
        // create components
        
        final Component pageField = new TextField<String>("input.page", pageModel);
        final Component maxPageLabel = new Label("label.pages", countModel);
        
        // place components
        
        this.add(pageField);
        this.add(maxPageLabel);
    }
}
