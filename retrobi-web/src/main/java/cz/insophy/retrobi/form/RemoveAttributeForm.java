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
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.longtask.CardModification;
import cz.insophy.retrobi.longtask.RemoveAttributeModification;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.model.task.BatchModificationTask;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * Attribute remove form.
 */
public class RemoveAttributeForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * attribute root cache
     */
    private final AttributePrototype root;
    /**
     * selected attribute model
     */
    private final IModel<Tuple<String, AttributePrototype>> attribute;
    /**
     * attribute value model
     */
    private final IModel<String> value;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public RemoveAttributeForm(final String id) {
        super(id);
        
        // prepare model
        
        this.root = RetrobiWebConfiguration.getInstance().getAttributeRoot();
        this.attribute = new Model<Tuple<String, AttributePrototype>>();
        this.value = new Model<String>();
        
        // create components
        
        final DropDownChoice<Tuple<String, AttributePrototype>> select = new DropDownChoice<Tuple<String, AttributePrototype>>(
                "select",
                this.attribute,
                SimpleAttributeUtils.gatherToList(this.root, false),
                new AttributePrototypeRenderer());
        
        final TextArea<String> input = new TextArea<String>("input", this.value);
        
        // setup components
        
        select.setNullValid(true);
        
        // place components
        
        this.add(select);
        this.add(input);
    }
    
    @Override
    protected void onSubmit() {
        if (this.attribute.getObject() == null) {
            this.error("Vyberte atribut.");
            return;
        }
        
        try {
            // modify cards
            
            final CardModification modification = new RemoveAttributeModification(
                    this.root,
                    this.attribute.getObject().getSecond(),
                    this.value.getObject());
            
            RetrobiWebSession.get().scheduleTask(new BatchModificationTask(
                    RetrobiWebSession.get().getCardContainer().getBasketCardIds(),
                    RetrobiWebSession.get().getCardContainer().getBatchModificationResult(),
                    modification));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
}
