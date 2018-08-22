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

package cz.insophy.retrobi.link;

import org.apache.wicket.markup.html.link.Link;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * A link that resets the whole attribute tree to default after click.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ResetTreeLink extends Link<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * card ID
     */
    private final String cardId;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param cardId
     * card ID
     */
    public ResetTreeLink(final String id, final String cardId) {
        super(id);
        
        this.cardId = cardId;
    }
    
    @Override
    public void onClick() {
        try {
            // load the card
            
            final Card card = RetrobiApplication.db().getCardRepository().getCard(this.cardId);
            
            // get the attribute prototype
            
            final AttributePrototype prototype = RetrobiWebConfiguration.getInstance().getAttributeRoot();
            
            // create the new tree
            
            final AttributeNode root = SimpleAttributeUtils.createFromPrototype(prototype);
            
            // replace the existing card tree
            
            SimpleAttributeUtils.toDocument(card, root);
            
            // save the card
            
            RetrobiApplication.db().getCardRepository().updateCard(card);
            
            this.info("Výchozí položkový rozpis byl vytvořen a uložen k lístku.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
