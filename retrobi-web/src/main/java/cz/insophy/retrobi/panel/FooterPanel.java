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

package cz.insophy.retrobi.panel;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.link.BookmarkableMessageLink;

/**
 * Footer panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class FooterPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public FooterPanel(final String id) {
        super(id);
        
        // prepare models
        
        final String email = RetrobiApplication.db().getTextRepository().getText(TextType.S_EMAIL);
        
        // create components
        
        final Component messageLink = new BookmarkableMessageLink("link.message", null, null);
        final Label emailLabel = new Label("label", email);
        final AbstractLink emailLink = new ExternalLink("link.email", "mailto:" + email);
        
        // place components
        
        emailLink.add(emailLabel);
        this.add(emailLink);
        this.add(messageLink);
    }
}
