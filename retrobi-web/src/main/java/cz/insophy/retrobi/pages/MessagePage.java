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
import org.apache.wicket.markup.html.panel.Panel;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.panel.MessageEditorPanel;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Message page that allows user to report an error.
 * 
 * @author Vojtěch Hordějčuk
 */
public class MessagePage extends AbstractBasicPage {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * parameters
     */
    public MessagePage(final PageParameters parameters) { // NO_UCD
        super(parameters);
        
        // load parameters
        
        Card refCard = null;
        String refImage = null;
        
        final String pCard = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_CARD));
        final String pImage = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_IMAGE));
        
        // load models
        
        if (pCard != null) {
            try {
                refCard = RetrobiApplication.db().getCardRepository().getCard(pCard);
            } catch (final NotFoundRepositoryException x) {
                // NOP
            } catch (final GeneralRepositoryException x) {
                // NOP
            }
        }
        
        if (pImage != null) {
            refImage = pImage;
        }
        
        // create components
        
        final Panel messagePanel = new MessageEditorPanel("panel", refCard, refImage);
        
        // place components
        
        this.add(messagePanel);
    }
    
    @Override
    protected String getPageTitle() {
        return "Hlášení";
    }
}
