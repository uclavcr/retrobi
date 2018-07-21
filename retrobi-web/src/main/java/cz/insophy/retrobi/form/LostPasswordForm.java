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

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * A form for an user who lost or forget the password.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LostPasswordForm extends Form<Object> {
    /**
     * default version
     */
    private static final long serialVersionUID = 1L;
    /**
     * random captcha string length
     */
    private static final int CAPTCHA_LENGTH = 5;
    /**
     * e-mail model
     */
    private final IModel<String> email;
    /**
     * captcha model
     */
    private final IModel<String> captcha;
    /**
     * correct captcha value
     */
    private final IModel<String> captchaContent;
    /**
     * captcha image resource
     */
    private final CaptchaImageResource captchaImageSource;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public LostPasswordForm(final String id) {
        super(id);
        
        // create models
        
        this.email = Model.of("");
        this.captcha = Model.of("");
        this.captchaContent = Model.of("");
        this.captchaImageSource = new CaptchaImageResource(this.captchaContent);
        
        // create components
        
        final TextField<String> emailField = new TextField<String>("input.email", this.email);
        final TextField<String> captchaField = new TextField<String>("input.captcha", this.captcha);
        final Image captchaImage = new Image("image.captcha", this.captchaImageSource);
        
        final Component resetLink = new Link<Object>("link.reset") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                LostPasswordForm.this.resetCaptcha();
            }
        };
        
        // place components
        
        this.add(emailField);
        this.add(captchaField);
        this.add(captchaImage);
        this.add(resetLink);
        
        // setup components
        
        this.captchaImageSource.setCacheable(false);
        captchaField.setLabel(Model.of("Spam ochrana"));
        captchaField.setRequired(true);
        captchaField.add(StringValidator.exactLength(LostPasswordForm.CAPTCHA_LENGTH));
        emailField.setLabel(Model.of("E-mail"));
        emailField.setRequired(true);
        emailField.add(EmailAddressValidator.getInstance());
        
        // reset captcha
        
        this.resetCaptcha();
    }
    
    @Override
    protected void onSubmit() {
        // validate captcha
        
        if (!this.captcha.getObject().equals(this.captchaContent.getObject())) {
            this.error("Přepiště prosím ochranu proti spamu.");
            this.resetCaptcha();
            return;
        }
        
        try {
            // find password hash
            
            final Tuple<String, String> userInfo = RetrobiApplication.db().getUserRepository().getUserPasswordHashByEmail(
                    this.email.getObject());
            
            if (userInfo == null) {
                this.error("Zadaný e-mail nebyl nalezen v databázi.");
                this.resetCaptcha();
                return;
            }
            
            // everything OK here, send the e-mail
            
            final String autologinUrl = RegisterForm.createAutologinUrl(this, userInfo.getFirst(), userInfo.getSecond());
            
            RetrobiApplication.db().getMessageRepository().sendEmail(
                    this.email.getObject(),
                    "Zapomenuté heslo",
                    String.format("" +
                            "<p>Vy nebo někdo jiný jste požádali o připomenutí zapomenutého hesla.</p>" +
                            "<p>Tímto odkazem se <a href='%s'>můžete přihlásit</a> a změnit své heslo na nové.</p>",
                            autologinUrl));
            
            this.info("E-mail s odkazem pro přihlášení byl odeslán.");
            
            // reset the form
            
            this.email.setObject("");
            this.captcha.setObject("");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        } catch (final AddressException x) {
            this.error(x.getMessage());
        } catch (final MessagingException x) {
            this.error(x.getMessage());
        } finally {
            // reset captcha in all cases
            
            this.resetCaptcha();
        }
    }
    
    /**
     * Resets the new random value to CAPTCHA and redraws the image.
     */
    private void resetCaptcha() {
        this.captchaContent.setObject(SimpleStringUtils.getRandomString(LostPasswordForm.CAPTCHA_LENGTH));
        this.captchaImageSource.invalidate();
    }
}
