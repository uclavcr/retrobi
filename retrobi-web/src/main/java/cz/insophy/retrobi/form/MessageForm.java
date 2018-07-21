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

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Message;
import cz.insophy.retrobi.database.entity.type.MessageType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.link.BookmarkableCardLink;

/**
 * Message editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class MessageForm extends Form<Card> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * referenced card (or <code>null</code>)
     */
    private final Card refCard;
    /**
     * referenced image (or <code>null</code>)
     */
    private final String refImage;
    /**
     * text field model
     */
    private final IModel<String> text;
    /**
     * captcha field model
     */
    private final IModel<String> captcha;
    /**
     * message type field model
     */
    private final IModel<MessageType> type;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param refCard
     * referenced card or <code>null</code>
     * @param refImage
     * referenced image or <code>null</code>
     */
    public MessageForm(final String id, final Card refCard, final String refImage) {
        super(id);
        
        // prepare models
        
        this.refCard = refCard;
        this.refImage = refImage;
        this.text = Model.of("");
        this.captcha = Model.of("");
        this.type = Model.of(refCard == null ? MessageType.PROBLEM_GENERAL : MessageType.PROBLEM_CARD_GENERAL);
        
        // create components
        
        final Component linkCard = (refCard != null)
                ? new BookmarkableCardLink("link.card", refCard.getId())
                : new WebMarkupContainer("link.card") {
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public boolean isVisible() {
                        return false;
                    }
                };
        
        final Label refCardLabel = new Label("label.ref_card", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return refCard == null ? "(žádný)" : refCard.toString();
            }
        });
        
        final Label refPageLabel = new Label("label.ref_page", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return refImage == null ? "(žádné)" : refImage.toString();
            }
        });
        
        final DropDownChoice<MessageType> inputType = new DropDownChoice<MessageType>("input.type", this.type, MessageForm.getFormMessageTypes());
        
        final TextArea<String> inputText = new TextArea<String>("input.text", this.text);
        
        final TextField<String> inputCaptcha = new TextField<String>("input.captcha", this.captcha) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        // setup components
        
        inputType.setLabel(Model.of("Typ zprávy"));
        inputText.setLabel(Model.of("Dodatečné informace"));
        inputCaptcha.setLabel(Model.of("Kontrolní otázka"));
        
        // place components
        
        this.add(linkCard);
        this.add(refCardLabel);
        this.add(refPageLabel);
        this.add(inputType);
        this.add(inputText);
        this.add(inputCaptcha);
    }
    
    /**
     * Returns all the message types available in the form.
     * 
     * @return list of message types
     */
    private static List<MessageType> getFormMessageTypes() {
        final List<MessageType> types = new LinkedList<MessageType>();
        
        for (final MessageType type : MessageType.values()) {
            if (!type.isEvent() && type.isSavingToDatabase()) {
                types.add(type);
            }
        }
        
        return Collections.unmodifiableList(types);
    }
    
    @Override
    protected void onSubmit() {
        // check if the user can send a message now
        
        if (!RetrobiWebSession.get().canSendMessage()) {
            this.error("Před odesláním další zprávy musíte počkat (nepřihlášení 1 minutu, přihlášení 5 sekund).");
            return;
        }
        
        // check CAPTCHA (if not logged in)
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            final int year = Calendar.getInstance().get(Calendar.YEAR);
            final String correctCaptcha = String.valueOf(year);
            
            if (!correctCaptcha.equals(this.captcha.getObject())) {
                this.error("Nesprávná odpověď na kontrolní otázku.");
                return;
            }
        }
        
        // create a message
        
        final Message message = new Message(
                this.type.getObject(),
                this.refCard,
                this.refImage,
                RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)
                        ? RetrobiWebSession.get().getLoggedUser()
                        : null,
                this.text.getObject());
        
        try {
            // send the message
            
            RetrobiApplication.db().getMessageRepository().addCustomMessage(RetrobiWebApplication.getCSVLogger(), message);
            RetrobiWebSession.get().notifyMessageSent();
            
            this.info("Zpráva byla úspěšně odeslána. Další budete moci odeslat až za chvíli.");
            
            // reset the form
            
            this.captcha.setObject("");
            this.text.setObject("");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
