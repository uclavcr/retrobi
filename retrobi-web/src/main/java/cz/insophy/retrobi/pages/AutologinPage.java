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

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * A page that logs user automatically after start. The login information is
 * taken from the page request URL.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AutologinPage extends AbstractBasicPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * parameters
     */
    public AutologinPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
        
        // accept parameters
        
        final String login = AutologinPage.extractLogin(parameters);
        final String passwordHash = AutologinPage.extractPasswordHash(parameters);
        
        if ((login != null) && (passwordHash != null)) {
            // try to login with these parameters
            
            if (RetrobiWebSession.get().login(login, passwordHash)) {
                // success - redirect to the profile page
                
                this.setResponsePage(ProfilePage.class);
                return;
            }
        }
        
        // an exception during the auto-login
        
        this.error("Chybné přihlašovací parametry.");
    }
    
    @Override
    protected String getPageTitle() {
        return "Automatické přihlášení";
    }
    
    /**
     * Extracts a login from page parameters.
     * 
     * @param parameters
     * page parameters
     * @return login or <code>null</code> (if empty)
     */
    private static String extractLogin(final PageParameters parameters) {
        final String value = parameters.getString(RetrobiWebApplication.PARAM_AUTO_LOGIN, null);
        return SimpleStringUtils.decodeFromUrl(value);
    }
    
    /**
     * Extracts a password hash from page parameters.
     * 
     * @param parameters
     * page parameters
     * @return password hash or <code>null</code> (if empty)
     */
    private static String extractPasswordHash(final PageParameters parameters) {
        final String value = parameters.getString(RetrobiWebApplication.PARAM_AUTO_PASSWORD_HASH, null);
        return SimpleStringUtils.decodeFromUrl(value);
    }
    
    /**
     * Creates a parameters for this page with the given login parameters.
     * 
     * @param login
     * login
     * @param passwordHash
     * password hash
     * @return page parameters (for this page)
     */
    public static PageParameters createParameters(final String login, final String passwordHash) {
        final PageParameters p = new PageParameters();
        p.put(RetrobiWebApplication.PARAM_AUTO_LOGIN, SimpleStringUtils.encodeForUrl(login));
        p.put(RetrobiWebApplication.PARAM_AUTO_PASSWORD_HASH, SimpleStringUtils.encodeForUrl(passwordHash));
        return p;
    }
}
