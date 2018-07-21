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

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * Attribute selector form.
 */
public class SelectAttributeForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * target list viewer (will be updated from here)
     */
    private final ListView<Tuple<String, Integer>> target;
    /**
     * selected attribute model
     */
    private final IModel<Tuple<String, AttributePrototype>> attribute;
    /**
     * sum label model
     */
    private final IModel<Integer> sum;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param target
     * target value list model
     * @param sum
     * target sum model
     */
    public SelectAttributeForm(final String id, final ListView<Tuple<String, Integer>> target, final IModel<Integer> sum) {
        super(id);
        
        // initialize models
        
        this.target = target;
        this.attribute = new Model<Tuple<String, AttributePrototype>>();
        this.sum = sum;
        
        // create components
        
        final IChoiceRenderer<Tuple<String, AttributePrototype>> renderer = new IChoiceRenderer<Tuple<String, AttributePrototype>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getIdValue(final Tuple<String, AttributePrototype> object, final int index) {
                return String.valueOf(index);
            }
            
            @Override
            public Object getDisplayValue(final Tuple<String, AttributePrototype> object) {
                return object.getFirst();
            }
        };
        
        final DropDownChoice<Tuple<String, AttributePrototype>> selector = new DropDownChoice<Tuple<String, AttributePrototype>>(
                "select",
                this.attribute,
                SimpleAttributeUtils.gatherToList(RetrobiWebConfiguration.getInstance().getAttributeRoot(), true),
                renderer);
        
        selector.setNullValid(true);
        
        // place components
        
        this.add(selector);
    }
    
    @Override
    protected void onSubmit() {
        if (this.attribute.getObject() == null) {
            this.error("Vyberte atribut ze seznamu.");
            return;
        }
        
        try {
            // load distinct values
            
            this.target.setList(RetrobiApplication.db().getAnalystRepository().listDistinctValues(
                    this.attribute.getObject().getSecond(),
                    RetrobiWebConfiguration.getInstance().getAttributeRoot(),
                    true));
            
            // compute sum of all the cards
            
            int temp = 0;
            
            for (final Tuple<String, Integer> item : this.target.getList()) {
                temp += item.getSecond();
            }
            
            this.sum.setObject(temp);
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
