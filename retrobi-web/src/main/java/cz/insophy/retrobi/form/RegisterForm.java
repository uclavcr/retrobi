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
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;
import cz.insophy.retrobi.pages.AutologinPage;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Registration form.
 */
public class RegisterForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * random captcha string length
     */
    private static final int CAPTCHA_LENGTH = 5;
    /**
     * list of predefined values for user "type" property
     */
    public static final List<String> PRESET_TYPE = Arrays.asList(
            "student SŠ",
            "student Bc.",
            "student Mgr.",
            "doktorand",
            "vysokoškolský pedagog",
            "učitel ZŠ/SŠ",
            "vědecký pracovník",
            "odborný pracovník (redaktor, publicista atp.)",
            "laický zájemce",
            "(jiné)");
    /**
     * list of predefined values for user "type" property
     */
    public static final List<String> PRESET_BRANCH = Arrays.asList(
            "bohemistika",
            "slavistika",
            "anglistika",
            "romanistika",
            "germanistika",
            "hispanistika",
            "jiná národní filologie",
            "historie",
            "divadelní věda",
            "dějiny umění",
            "žurnalistika",
            "knihovnictví a informační věda",
            "(jiné)");
    /**
     * list of predefined values for user "alma mater" property
     */
    public static final List<String> PRESET_ALMA = Arrays.asList(
            "Akademie věd ČR",
            "FF UK Praha",
            "PedF UK Praha",
            "FF MU Brno",
            "PedF MU Brno",
            "FF Ostravské univerzity",
            "PedF Ostravské univerzity",
            "FF UP Olomouc",
            "PedF UP Olomouc",
            "FF JČU v Českých Budějovicích",
            "PedF JČU v Českých Budějovicích",
            "PedF ZČU v Plzni",
            "PedF Univerzity Hradec Králové",
            "FF Univerzity Pardubice",
            "PedF UJEP v Ústí nad Labem",
            "FPF TU v Liberci",
            "FPF Slezské univerzity v Opavě",
            "Literární akademie − soukromá VŠ Josefa Škvoreckého",
            "Památník národního písemnictví",
            "(jiné)");
    /**
     * default preset value (other)
     */
    private static final String DEFAULT_PRESET = "(jiné)";
    /**
     * login
     */
    private final IModel<String> login;
    /**
     * e-mail
     */
    private final IModel<String> email;
    /**
     * type
     */
    private final IModel<String> type;
    /**
     * branch
     */
    private final IModel<String> branch;
    /**
     * alma mater
     */
    private final IModel<String> alma;
    /**
     * agree with the rules
     */
    private final IModel<Boolean> agree;
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
     * wicket id
     */
    public RegisterForm(final String id) {
        super(id);
        
        // initialize models
        
        this.login = Model.of("");
        this.email = Model.of("");
        this.type = Model.of(RegisterForm.DEFAULT_PRESET);
        this.branch = Model.of(RegisterForm.DEFAULT_PRESET);
        this.alma = Model.of(RegisterForm.DEFAULT_PRESET);
        this.agree = Model.of(false);
        this.captcha = Model.of("");
        this.captchaContent = Model.of("");
        this.captchaImageSource = new CaptchaImageResource(this.captchaContent);
        
        final String rules = RetrobiApplication.db().getTextRepository().getText(TextType.S_RULES);
        
        // create components
        
        final TextField<String> loginField = RegisterForm.createLoginField("login", this.login);
        final TextField<String> emailField = RegisterForm.createEmailField("email", this.email);
        final DropDownChoice<String> typeSelect = new DropDownChoice<String>("select.type", this.type, RegisterForm.PRESET_TYPE);
        final DropDownChoice<String> branchSelect = new DropDownChoice<String>("select.branch", this.branch, RegisterForm.PRESET_BRANCH);
        final DropDownChoice<String> almaSelect = new DropDownChoice<String>("select.alma", this.alma, RegisterForm.PRESET_ALMA);
        final TextArea<String> rulesField = new TextArea<String>("rules", Model.of(rules));
        final CheckBox agreeCheck = new CheckBox("agree", this.agree);
        final TextField<String> captchaField = new TextField<String>("input.captcha", this.captcha);
        final Image captchaImage = new Image("image.captcha", this.captchaImageSource);

        final Component resetLink = new Link<Object>("link.reset") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                resetCaptcha();
            }
        };
        
        // setup components
        
        loginField.setLabel(Model.of("Login"));
        emailField.setLabel(Model.of("E-mail"));
        typeSelect.setLabel(Model.of("Kdo jste"));
        branchSelect.setLabel(Model.of("Odborné zaměření"));
        almaSelect.setLabel(Model.of("Škola či pracoviště"));
        rulesField.setLabel(Model.of("Pravidla"));
        agreeCheck.setLabel(Model.of("Souhlas s pravidly"));
        this.captchaImageSource.setCacheable(false);
        this.add(resetLink);
        captchaField.setLabel(Model.of("Spam ochrana"));
        captchaField.setRequired(true);
        captchaField.add(StringValidator.exactLength(CAPTCHA_LENGTH));
        
        // place components
        
        this.add(loginField);
        this.add(emailField);
        this.add(typeSelect);
        this.add(branchSelect);
        this.add(almaSelect);
        this.add(rulesField);
        this.add(agreeCheck);
        this.add(captchaField);
        this.add(captchaImage);

        // reset captcha

        this.resetCaptcha();
        
        // add validators
        
        agreeCheck.add(new IValidator<Boolean>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void validate(final IValidatable<Boolean> validatable) {
                if ((validatable.getValue() == null) || !validatable.getValue()) {
                    validatable.error(new IValidationError() {
                        private static final long serialVersionUID = 1L;
                        
                        @Override
                        public String getErrorMessage(final IErrorMessageSource messageSource) {
                            return "Pro registraci je nezbytný souhlas s 'Pravidly užívání systému'.";
                        }
                    });
                }
            }
        });
        
        typeSelect.setRequired(true);
        typeSelect.setNullValid(true);
        branchSelect.setNullValid(true);
        almaSelect.setNullValid(true);
    }
    
    @Override
    protected void onSubmit() {
        if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.error("Přihlášený uživatel nemůže provést registraci.");
            return;
        }

        // validate captcha

        if (!this.captcha.getObject().equals(this.captchaContent.getObject())) {
            this.error("Přepiště prosím ochranu proti spamu.");
            this.resetCaptcha();
            return;
        }
        
        // initialize values
        
        final String newLogin = this.login.getObject();
        final String newEmail = this.email.getObject();
        final String newPassword = SimpleStringUtils.getRandomString(User.MIN_PASSWORD_LENGTH);
        
        try {
            // create a new user
            
            final User newUser = new User();
            
            newUser.setLogin(newLogin);
            newUser.setEmail(newEmail);
            newUser.setPassword(SimpleStringUtils.getHash(newPassword));
            newUser.setAlma(SimpleStringUtils.nullToEmpty(this.alma.getObject()));
            newUser.setBranch(SimpleStringUtils.nullToEmpty(this.branch.getObject()));
            newUser.setType(SimpleStringUtils.nullToEmpty(this.type.getObject()));
            
            // get auto-login URL
            
            final String autologinUrl = RegisterForm.createAutologinUrl(this, newUser.getLogin(), newUser.getPassword());
            
            // register the new user
            
            RetrobiOperations.registerUser(newUser, newPassword, autologinUrl, RetrobiWebApplication.getCSVLogger());
            
            // reset the form
            
            this.login.setObject("");
            this.email.setObject("");
            this.type.setObject("");
            this.branch.setObject("");
            this.alma.setObject("");
            this.agree.setObject(false);
            
            // inform the new user about the result
            
            this.info("Registrace proběhla úspěšně. Na e-mail Vám byly zaslány registrační informace. E-mail si přečtěte a přihlašte se.");
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        } finally {
            // reset captcha in all cases

            this.resetCaptcha();
        }
    }
    
    /**
     * Creates the login field.
     * 
     * @param id
     * component ID
     * @param model
     * value model
     * @return a new text field
     */
    public static TextField<String> createLoginField(final String id, final IModel<String> model) {
        final TextField<String> field = new RequiredTextField<String>(id, model);
        field.add(StringValidator.lengthBetween(User.MIN_EMAIL_LENGTH, User.MAX_EMAIL_LENGTH));
        return field;
    }
    
    /**
     * Creates the e-mail field.
     * 
     * @param id
     * component ID
     * @param model
     * value model
     * @return a new text field
     */
    public static TextField<String> createEmailField(final String id, final IModel<String> model) {
        final TextField<String> field = new RequiredTextField<String>(id, model);
        field.add(StringValidator.lengthBetween(User.MIN_EMAIL_LENGTH, User.MAX_EMAIL_LENGTH));
        field.add(EmailAddressValidator.getInstance());
        return field;
    }
    
    /**
     * Creates an autologin URL.
     * 
     * @param c
     * source component which generates the URL
     * @param login
     * user login
     * @param password
     * user password hash
     * @return autologin URL
     */
    public static String createAutologinUrl(final Component c, final String login, final String password) {
        final String autologinUrl = RequestUtils.toAbsolutePath(c.urlFor(
                AutologinPage.class,
                AutologinPage.createParameters(login, password)).toString());
        
        String autologinUrlFixed = autologinUrl;
        autologinUrlFixed = autologinUrlFixed.replace("http://127.0.0.1:8080/", Settings.SERVER_URL_FOR_EMAIL);
        autologinUrlFixed = autologinUrlFixed.replace("http://127.0.0.1/", Settings.SERVER_URL_FOR_EMAIL);
        autologinUrlFixed = autologinUrlFixed.replace("http://localhost:8080/", Settings.SERVER_URL_FOR_EMAIL);
        autologinUrlFixed = autologinUrlFixed.replace("http://localhost/", Settings.SERVER_URL_FOR_EMAIL);
        return autologinUrlFixed;
    }

    /**
     * Resets the new random value to CAPTCHA and redraws the image.
     */
    private void resetCaptcha() {
        this.captchaContent.setObject(SimpleStringUtils.getRandomString(CAPTCHA_LENGTH));
        this.captchaImageSource.invalidate();
    }
}
