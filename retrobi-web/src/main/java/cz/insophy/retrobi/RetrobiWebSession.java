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

package cz.insophy.retrobi;

import java.util.Locale;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.Time;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.model.LongTaskManager;
import cz.insophy.retrobi.model.SessionCardContainer;
import cz.insophy.retrobi.model.setup.SessionCardView;
import cz.insophy.retrobi.model.task.LongTask;

/**
 * Custom web session that stores information about the logged user, contains a
 * private user collection of picked cards (something like a basket) and a list
 * of cards returned by the last search.
 * 
 * @author Vojtěch Hordějčuk
 */
public class RetrobiWebSession extends WebSession {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(RetrobiWebSession.class);
    /**
     * long task manager
     */
    private final LongTaskManager longTaskManager;
    /**
     * personal card container
     */
    private final SessionCardContainer cardContainer;
    /**
     * the view settings
     */
    private final SessionCardView cardView;
    /**
     * time of last message sent
     */
    private long lastMessageSent;
    /**
     * currently logged user
     */
    private User loggedUser;
    
    /**
     * Creates a new instance.
     * 
     * @param request
     * request
     */
    protected RetrobiWebSession(final Request request) {
        super(request);
        
        RetrobiWebSession.LOG.debug("Session created.");
        
        // initialize
        
        this.setLocale(new Locale("cs"));
        this.longTaskManager = new LongTaskManager();
        this.cardContainer = new SessionCardContainer();
        this.cardView = new SessionCardView();
        this.lastMessageSent = 0;
        this.loggedUser = null;
    }
    
    // ================
    // INSTANCE GETTERS
    // ================
    
    /**
     * Utility method for type safe session extraction. If the runtime session
     * class is not correct, an exception will be thrown.
     * 
     * @return safely typed session
     */
    public static RetrobiWebSession get() {
        final Session session = Session.get();
        
        if (!(session instanceof RetrobiWebSession)) {
            throw new IllegalStateException();
        }
        
        return (RetrobiWebSession) session;
    }
    
    // ==================
    // TASK RELATED STUFF
    // ==================
    
    /**
     * Returns the active task or <code>null</code> if none.
     * 
     * @return the active task or <code>null</code>
     */
    public LongTask getActiveTask() {
        return this.longTaskManager.getActiveTask();
    }
    
    /**
     * Returns the current task queue version. This version number changes
     * (increments) each time the queue changes so it is possible to watch for
     * task queue changes and update pages accordingly.
     * 
     * @return the current task queue version
     */
    public long getTaskQueueVersion() {
        return this.longTaskManager.getVersion();
    }
    
    /**
     * Schedules a task. If the task is defined as <b>quick</b>, it is executed
     * immediately. All guests can do this. Other tasks (regular long tasks) are
     * added to the task queue and scheduled for execution later. Only users can
     * do this.
     * 
     * @param task
     * task to be scheduled
     * @throws InterruptedException
     * queue is full or other error during adding
     */
    public void scheduleTask(final LongTask task) throws InterruptedException {
        if (task.isQuick()) {
            // execute the quick task immediately
            
            task.start();
            
            if (task.getErrors().isEmpty()) {
                this.info(String.format("Úloha '%s' byla dokončena (okamžité spuštění).", task.getName()));
            } else {
                for (final Exception x : task.getErrors()) {
                    this.error(x.getMessage());
                }
            }
        } else {
            if (this.hasRoleAtLeast(UserRole.USER)) {
                // add the task to queue
                
                if (this.longTaskManager.scheduleTask(task)) {
                    this.info(String.format("Úloha '%s' byla spuštěna (dlouhý běh).", task.getName()));
                } else {
                    this.error(String.format("Najednou může běžet jen jedna úloha."));
                }
            } else {
                this.error(String.format("Úlohu '%s' může spustit jen přihlášený uživatel.", task.getName()));
            }
        }
    }
    
    /**
     * Stops the active task (if any).
     * 
     * @return <code>true</code> if the task was active and stopped,
     * <code>false</code> otherwise
     */
    public boolean stopActiveTask() {
        return this.longTaskManager.stopActiveTask();
    }
    
    // ==================
    // CARD RELATED STUFF
    // ==================
    
    /**
     * Returns the card container for this session.
     * 
     * @return card container
     */
    public SessionCardContainer getCardContainer() {
        return this.cardContainer;
    }
    
    /**
     * Returns the card view for this session.
     * 
     * @return card view
     */
    public SessionCardView getCardView() {
        return this.cardView;
    }
    
    // ==================
    // USER RELATED STUFF
    // ==================
    
    /**
     * Returns the currently logged user. Throws a runtime exception when no
     * user is currently logged in.
     * 
     * @return logged user
     */
    public User getLoggedUser() {
        if (this.loggedUser == null) {
            RetrobiWebSession.LOG.error("No user is logged.");
            throw new NullPointerException("Není přihlášen žádný uživatel.");
        }
        
        return this.loggedUser;
    }
    
    /**
     * Checks if the user has at least the minimal role specified.
     * 
     * @param minRole
     * minimal role
     * @return <code>true</code> if the user has at least the role specified,
     * <code>false</code> otherwise
     */
    public boolean hasRoleAtLeast(final UserRole minRole) {
        if (this.loggedUser == null) {
            if (minRole.equals(UserRole.GUEST)) {
                return true;
            }
            
            return false;
        }
        
        return this.loggedUser.hasRoleAtLeast(minRole);
    }
    
    /**
     * Reloads the logged user from database (if any).
     * 
     * @return <code>true</code> if the user was successfully reloaded or no
     * user is logged at all, <code>false</code> otherwise
     */
    public boolean refreshLoggedUser() {
        RetrobiWebSession.LOG.debug("Refreshing logged user: " + this.loggedUser);
        
        if (this.loggedUser == null) {
            return true;
        }
        
        // try to reload the currently logged user
        
        final String login = this.loggedUser.getLogin();
        final String passwordHash = this.loggedUser.getPassword();
        
        try {
            this.loggedUser = RetrobiApplication.db().getUserRepository().getUser(login, passwordHash);
            
            if (this.loggedUser != null) {
                // success, return the status
                
                RetrobiWebSession.LOG.debug("Logged user refreshed.");
                return true;
            }
            
            // failure
            
            RetrobiWebSession.LOG.debug("Invalid login.");
            this.error("Přihlašovací údaje již nejsou platné.");
        } catch (final GeneralRepositoryException x) {
            RetrobiWebSession.LOG.debug("General exception.");
            this.error("Chyba při načítání přihlášeného uživatele.");
        } catch (final NotFoundRepositoryException x) {
            RetrobiWebSession.LOG.debug("User not found.");
            this.error("Přihlášený uživatel nebyl nalezen.");
        }
        
        // reload was not successful, logout and return the status
        
        RetrobiWebSession.LOG.debug("Login invalid, will be logged out.");
        this.logout();
        return false;
    }
    
    /**
     * Logs the user in (login feature). If the combination of login and
     * password is invalid, no user is logged at all.
     * 
     * @param login
     * user login
     * @param passwordHash
     * user password hash
     * @return <code>true</code> if the login was successful, <code>false</code>
     * otherwise
     */
    public boolean login(final String login, final String passwordHash) {
        // clear the session (just for sure)
        
        this.clear();
        this.dirty();
        
        try {
            // load the user with the given login combination
            
            final User newUser = RetrobiApplication.db().getUserRepository().getUser(login, passwordHash);
            
            // check if that user was found
            
            if (newUser != null) {
                RetrobiWebSession.LOG.debug(String.format("Logging in user '%s'...", newUser.getEmail()));
                this.loggedUser = newUser;
                this.incrementLoginCount();
                this.info("Přihlášení bylo úspěšné.");
                return true;
            }
            
            // no such user was found
            
            this.error("Špatné přihlašovací jméno nebo heslo.");
            return false;
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        }
        
        return false;
    }
    
    /**
     * Logs the logged user out (logout feature).
     */
    public void logout() {
        if (this.loggedUser == null) {
            RetrobiWebSession.LOG.error("No user is logged.");
            throw new NullPointerException("Není přihlášen žádný uživatel.");
        }
        
        // log out
        
        RetrobiWebSession.LOG.debug(String.format("Logging out user '%s'...", this.loggedUser.getEmail()));
        this.loggedUser = null;
        
        // clear the session
        
        try {
            this.longTaskManager.stopActiveTask();
            this.cardContainer.clearAll();
            this.cardView.reset();
        } finally {
            this.clear();
            this.dirty();
        }
    }
    
    /**
     * Increments the login count of the currently logged user (if any).
     */
    private void incrementLoginCount() {
        try {
            if (this.hasRoleAtLeast(UserRole.USER)) {
                final User user = this.getLoggedUser();
                RetrobiWebSession.LOG.debug("Incrementing login count of user '%s'...", user.getEmail());
                user.incrementLoginCount();
                user.setLastLoginDate(Time.now());
                RetrobiApplication.db().getUserRepository().updateUser(user);
                RetrobiWebSession.LOG.debug("Login count incremented.");
            }
        } catch (final GeneralRepositoryException x) {
            RetrobiWebSession.LOG.warn("Error while incrementing login count: " + x.getMessage());
        }
    }
    
    // ==============
    // LIMIT CHECKING
    // ==============
    
    /**
     * Returns the basket size limit. Returns -1 if there is no limit at all.
     * 
     * @return basket size limit (or -1 if none)
     */
    public int getBasketLimit() {
        if (this.hasRoleAtLeast(UserRole.USER)) {
            return this.getLoggedUser().getBasketLimit();
        }
        
        return Settings.DEFAULT_GUEST_BASKET_LIMIT;
    }
    
    /**
     * Returns the cardset count limit. Returns -1 if there is no limit at all.
     * 
     * @return cardset count limit (or -1 if none)
     */
    public int getCardsetLimit() {
        if (this.hasRoleAtLeast(UserRole.USER)) {
            return this.getLoggedUser().getCardsetLimit();
        }
        
        return Settings.DEFAULT_GUEST_CARDSET_LIMIT;
    }
    
    /**
     * Checks if a message can be sent right now. Logged user can send a message
     * each 2 seconds, unlogged user each 1 minute.
     * 
     * @return <code>true</code> if the message can be sent, <code>false</code>
     * otherwise
     */
    public boolean canSendMessage() {
        final long timeout = this.hasRoleAtLeast(UserRole.USER)
                ? Settings.USER_MESSAGE_TIMEOUT
                : Settings.GUEST_MESSAGE_TIMEOUT;
        
        final long past = System.currentTimeMillis() - timeout;
        
        return (this.lastMessageSent < past);
    }
    
    /**
     * Checks if the currently logged user (if any) can download the basket. If
     * no user is logged, he is treated as a guest and default guest limits are
     * used. If the basket is empty, returns <code>false</code>.
     * 
     * @return <code>true</code> if the basket can be downloaded by the current
     * user, <code>false</code> otherwise
     */
    public boolean canDownloadBasket() {
        if (this.getCardContainer().getBasketSize() < 1) {
            this.error("Schránka je prázdná.");
            return false;
        }
        
        int limit = Settings.DEFAULT_GUEST_BASKET_LIMIT;
        
        if (this.hasRoleAtLeast(UserRole.USER)) {
            limit = this.getLoggedUser().getBasketLimit();
        }
        
        if (limit < 0) {
            return true;
        }
        
        if (this.getCardContainer().getBasketSize() > limit) {
            this.error(String.format("Schránku nelze stáhnout - její velikost překračuje Váš limit (%d).", limit));
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns the user role. If no user is logged, returns the guest role.
     * 
     * @return the current user role
     */
    public UserRole getUserRole() {
        if (this.loggedUser != null) {
            return this.loggedUser.getRole();
        }
        
        return UserRole.GUEST;
    }
    
    // =============
    // NOTIFICATIONS
    // =============
    
    /**
     * Notifies session that a message was sent.
     */
    public void notifyMessageSent() {
        this.lastMessageSent = System.currentTimeMillis();
    }
}
