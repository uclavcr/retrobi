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

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.database.entity.type.MessageStateOption;
import cz.insophy.retrobi.database.entity.type.MessageType;
import cz.insophy.retrobi.panel.MessageListPanel;

/**
 * Custom filter form for showing message list.
 * 
 * @author Vojtěch Hordějčuk
 */
public class MessageListFilterForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * dependent message list view
     */
    private final MessageListPanel list;
    /**
     * state choice model
     */
    private final IModel<MessageStateOption> state;
    /**
     * type choice model
     */
    private final IModel<MessageType> type;
    /**
     * user field model
     */
    private final IModel<String> user;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param list
     * dependent message list view
     */
    public MessageListFilterForm(final String id, final MessageListPanel list) {
        super(id);
        
        // prepare models
        
        this.list = list;
        this.state = new Model<MessageStateOption>(MessageStateOption.UNCONFIRMED_ALL);
        this.type = new Model<MessageType>();
        this.user = new Model<String>("");
        
        // prepare renderers
        
        final IChoiceRenderer<MessageType> typeRenderer = new IChoiceRenderer<MessageType>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Object getDisplayValue(final MessageType object) {
                final String prefix = object.isEvent() ? "událost" : "hlášení";
                return prefix + ": " + object.toString();
            }
            
            @Override
            public String getIdValue(final MessageType object, final int index) {
                return object.name();
            }
        };
        
        // create components
        
        final DropDownChoice<?> chooseState = new DropDownChoice<MessageStateOption>(
                "select.state",
                this.state,
                Arrays.asList(MessageStateOption.values()));
        
        final DropDownChoice<?> chooseType = new DropDownChoice<MessageType>(
                "select.type",
                this.type,
                MessageType.valuesSavingInDatabase(),
                typeRenderer);
        
        final TextField<?> userField = new TextField<String>(
                "input.user",
                this.user);
        
        // setup components
        
        chooseState.setNullValid(true);
        chooseType.setNullValid(true);
        
        // place components
        
        this.add(chooseState);
        this.add(chooseType);
        this.add(userField);
    }
    
    @Override
    protected void onSubmit() {
        // update the filter
        
        this.list.setState(this.state.getObject());
        this.list.setType(this.type.getObject());
        this.list.setUser(this.user.getObject());
        this.list.reset();
    }
}
