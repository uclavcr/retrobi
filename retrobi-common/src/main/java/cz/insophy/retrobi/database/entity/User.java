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

package cz.insophy.retrobi.database.entity;

import org.svenson.JSONProperty;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.document.StandaloneDocument;
import cz.insophy.retrobi.database.entity.type.UserRole;

/**
 * A guest or a registered system user.
 * 
 * @author Vojtěch Hordějčuk
 */
public class User extends StandaloneDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * minimal password length
     */
    public static final int MIN_PASSWORD_LENGTH = 5;
    /**
     * maximal password length
     */
    public static final int MAX_PASSWORD_LENGTH = 50;
    /**
     * minimal e-mail length
     */
    public static final int MIN_EMAIL_LENGTH = 5;
    /**
     * maximal e-mail length
     */
    public static final int MAX_EMAIL_LENGTH = 100;
    /**
     * user role (for authorization)
     */
    private UserRole role;
    /**
     * login
     */
    private String login;
    /**
     * password hash
     */
    private String password;
    /**
     * email
     */
    private String email;
    /**
     * user type (as a plain string)
     */
    private String type;
    /**
     * user branch (as a plain string)
     */
    private String branch;
    /**
     * alma mater (as a plain string)
     */
    private String alma;
    /**
     * basket limit (-1 means none)
     */
    private int basketLimit;
    /**
     * cardset limit (-1 means none)
     */
    private int cardsetLimit;
    /**
     * login counter
     */
    private int loginCount;
    /**
     * card OCR fix counter
     */
    private int editCount;
    /**
     * date of a last login
     */
    private Time lastLoginDate;
    /**
     * date of registration
     */
    private Time registrationDate;
    
    /**
     * Creates a new instance.
     */
    public User() {
        super();
        
        this.role = UserRole.USER;
        this.basketLimit = Settings.DEFAULT_USER_BASKET_LIMIT;
        this.cardsetLimit = Settings.DEFAULT_USER_CARDSET_LIMIT;
        this.registrationDate = Time.now();
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the document type flag.
     * 
     * @return document type flag
     */
    @JSONProperty(value = "TAG_user", readOnly = true)
    public boolean isUser() {
        return true;
    }
    
    /**
     * Returns a role.
     * 
     * @return role
     */
    public UserRole getRole() {
        return this.role;
    }
    
    /**
     * Returns a login.
     * 
     * @return a login
     */
    public String getLogin() {
        return this.login;
    }
    
    /**
     * Returns an encoded password.
     * 
     * @return encoded password
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Returns an e-mail.
     * 
     * @return e-mail
     */
    public String getEmail() {
        return this.email;
    }
    
    /**
     * Returns a type.
     * 
     * @return type
     */
    public String getType() {
        return this.type;
    }
    
    /**
     * Returns a branch.
     * 
     * @return branch
     */
    public String getBranch() {
        return this.branch;
    }
    
    /**
     * Returns an alma mater.
     * 
     * @return alma mater
     */
    public String getAlma() {
        return this.alma;
    }
    
    /**
     * Returns the user basket size limit. Returns -1 if no limit is specified
     * (e.g. for editors).
     * 
     * @return user basket size limit or -1
     */
    @JSONProperty(value = "limit_basket")
    public int getBasketLimit() {
        if (this.hasRoleAtLeast(UserRole.EDITOR)) {
            return -1;
        }
        
        return this.basketLimit;
    }
    
    /**
     * Returns the user cardset count limit. Returns -1 if no limit is specified
     * (e.g. for editors).
     * 
     * @return user cardset count limit or -1
     */
    @JSONProperty(value = "limit_cardset")
    public int getCardsetLimit() {
        if (this.hasRoleAtLeast(UserRole.EDITOR)) {
            return -1;
        }
        
        return this.cardsetLimit;
    }
    
    /**
     * Checks whether user has the same or higher privileges as the given role.
     * 
     * @param minRole
     * minimal role needed
     * @return <code>true</code> if the user has all necessary privileges,
     * <code>false</code> otherwise
     */
    @JSONProperty(ignore = true)
    public boolean hasRoleAtLeast(final UserRole minRole) {
        if ((this.role == null) || (minRole == null)) {
            throw new IllegalStateException();
        }
        
        return this.role.ordinal() >= minRole.ordinal();
    }
    
    /**
     * Returns the login count.
     * 
     * @return the login count
     */
    @JSONProperty(value = "count_login")
    public int getLoginCount() {
        return this.loginCount;
    }
    
    /**
     * Returns the card edit count.
     * 
     * @return the card edit count
     */
    @JSONProperty(value = "count_edit")
    public int getEditCount() {
        return this.editCount;
    }
    
    /**
     * Returns the last login date.
     * 
     * @return the last login date
     */
    @JSONProperty(value = "time_last_login")
    public Time getLastLoginDate() {
        return this.lastLoginDate;
    }
    
    /**
     * Returns the registration date.
     * 
     * @return the registration date
     */
    @JSONProperty(value = "time_registration")
    public Time getRegistrationDate() {
        return this.registrationDate;
    }
    
    @Override
    public String toString() {
        return this.email;
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets role.
     * 
     * @param value
     * role
     */
    public void setRole(final UserRole value) {
        this.role = value;
    }
    
    /**
     * Sets login.
     * 
     * @param value
     * login
     */
    public void setLogin(final String value) {
        this.login = value;
    }
    
    /**
     * Sets password.
     * 
     * @param value
     * encoded password
     */
    public void setPassword(final String value) {
        this.password = value;
    }
    
    /**
     * Sets e-mail.
     * 
     * @param value
     * e-mail
     */
    public void setEmail(final String value) {
        this.email = value;
    }
    
    /**
     * Sets type.
     * 
     * @param value
     * type
     */
    public void setType(final String value) {
        this.type = value;
    }
    
    /**
     * Sets branch.
     * 
     * @param value
     * branch
     */
    public void setBranch(final String value) {
        this.branch = value;
    }
    
    /**
     * Sets alma mater.
     * 
     * @param value
     * alma mater
     */
    public void setAlma(final String value) {
        this.alma = value;
    }
    
    /**
     * Sets the basket size limit. Value for unlimited is -1.
     * 
     * @param value
     * basket size limit
     */
    public void setBasketLimit(final int value) {
        this.basketLimit = value;
    }
    
    /**
     * Sets the cardset count limit. Value for unlimited is -1.
     * 
     * @param value
     * cardset count limit
     */
    public void setCardsetLimit(final int value) {
        this.cardsetLimit = value;
    }
    
    /**
     * Sets the login count.
     * 
     * @param value
     * the login count
     */
    public void setLoginCount(final int value) {
        this.loginCount = value;
    }
    
    /**
     * Sets the card edit count.
     * 
     * @param value
     * card edit count
     */
    public void setEditCount(final int value) {
        this.editCount = value;
    }
    
    /**
     * Increments the login count.
     */
    public void incrementLoginCount() {
        this.loginCount++;
    }
    
    /**
     * Changes the edit count. The amount floors at zero.
     * 
     * @param delta
     * change to be applied
     */
    public void changeEditCount(final int delta) {
        this.editCount += delta;
        
        if (this.editCount < 0) {
            this.editCount = 0;
        }
    }
    
    /**
     * Sets the last login date.
     * 
     * @param value
     * the last login date
     */
    public void setLastLoginDate(final Time value) {
        this.lastLoginDate = value;
    }
    
    /**
     * Sets the registration date.
     * 
     * @param value
     * the registration date
     */
    public void setRegistrationDate(final Time value) {
        this.registrationDate = value;
    }
}
