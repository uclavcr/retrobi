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

package cz.insophy.retrobi.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.View;
import org.jcouchdb.document.ViewResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * User repository for managing users. NOTE: User passwords are saved as hashes.
 * 
 * @author Vojtěch Hordějčuk
 */
final public class UserRepository extends AbstractRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);
    /**
     * top ranked users view name
     */
    private static final String V_TOP = "top";
    /**
     * user list by login view name
     */
    private static final String V_LIST_BY_LOGIN = "list_by_login";
    /**
     * user list by e-mail view name
     */
    private static final String V_LIST_BY_EMAIL = "list_by_email";
    /**
     * user count by e-mail
     */
    private static final String V_COUNT_BY_EMAIL = "count_by_email";
    /**
     * user list (for login) view name
     */
    private static final String V_LOGIN = "login";
    /**
     * user type/branch/alma counter
     */
    private static final String V_COUNT = "count";
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database object
     */
    protected UserRepository(final Database database) {
        super(database);
    }
    
    /**
     * Saves a new user. Update conflict or other error is converted to a
     * repository exception.
     * 
     * @param user
     * an user to insert
     * @throws GeneralRepositoryException
     * repository exception
     */
    public void addUser(final User user) throws GeneralRepositoryException {
        UserRepository.LOG.debug("Adding user...");
        this.createDocument(user);
        UserRepository.LOG.debug("User added.");
    }
    
    /**
     * Saves an existing user. Update conflict or other error is converted to a
     * repository exception.
     * 
     * @param user
     * an user to save
     * @throws GeneralRepositoryException
     * repository exception
     */
    public void updateUser(final User user) throws GeneralRepositoryException {
        UserRepository.LOG.debug("Updating user...");
        this.updateDocument(user);
        UserRepository.LOG.debug("User updated.");
    }
    
    /**
     * Removes an existing user from the database. Update conflict or other
     * error is converted to a repository exception.
     * 
     * @param user
     * an existing user to remove
     * @throws GeneralRepositoryException
     * repository exception
     */
    public void deleteUser(final User user) throws GeneralRepositoryException {
        UserRepository.LOG.debug("Deleting user...");
        this.deleteDocument(user);
        UserRepository.LOG.debug("User deleted.");
    }
    
    /**
     * Returns a list of all user IDs in the database. The users are ordered by
     * their email addresses.
     * 
     * @param page
     * current page (0 to N)
     * @param limit
     * user limit
     * @return list of users with paging information
     * @throws GeneralRepositoryException
     * repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public Tuple<Integer, List<String>> getUserIds(final int page, final int limit) throws GeneralRepositoryException, NotFoundRepositoryException {
        UserRepository.LOG.debug("Loading users (paged)...");
        return this.loadPagedDocumentIds(UserRepository.V_LIST_BY_EMAIL, UserRepository.V_COUNT_BY_EMAIL, null, null, false, page, limit);
    }
    
    /**
     * Loads a list of users by their IDs.
     * 
     * @param userIds
     * user IDs to be loaded
     * @return list of users
     * @throws GeneralRepositoryException
     * repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public List<User> getUsers(final List<String> userIds) throws NotFoundRepositoryException, GeneralRepositoryException {
        return this.loadDocuments(User.class, userIds);
    }
    
    /**
     * Returns a list of all users. Warning: use wisely!
     * 
     * @return a list of users
     * @throws GeneralRepositoryException
     * repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public List<User> getAllUsers() throws NotFoundRepositoryException, GeneralRepositoryException {
        UserRepository.LOG.debug("Loading all users...");
        return this.loadDocuments(User.class, this.loadDocumentIds(UserRepository.V_LIST_BY_LOGIN, new Options()));
    }
    
    /**
     * Loads a user from the database with the specified login and password
     * combination. If no such user exists or the password is invalid for the
     * given login, <code>null</code> will be returned.
     * 
     * @param login
     * user login
     * @param passwordHash
     * user password hash
     * @return user with these login information or <code>null</code>
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public User getUser(final String login, final String passwordHash) throws GeneralRepositoryException, NotFoundRepositoryException {
        UserRepository.LOG.debug("Loading user by a login information...");
        
        // check basic constraints
        
        if ((login == null) || (passwordHash == null) || (login.length() < 1) || (passwordHash.length() < 1)) {
            return null;
        }
        
        // prepare query
        
        final Object key = new Object[] { login, passwordHash };
        final Options options = new Options().key(key);
        
        // run the query
        
        final ViewResult<String> result = this.queryView(UserRepository.V_LOGIN, String.class, options);
        
        // process results
        
        if (result.getRows().isEmpty()) {
            UserRepository.LOG.warn("No user with that login information.");
            return null;
        }
        
        final ValueRow<String> row = result.getRows().get(0);
        
        // return results
        
        return this.loadDocument(User.class, row.getValue());
    }
    
    /**
     * Checks whether the provided login is free (not yet taken by another user
     * in the database).
     * 
     * @param login
     * login to be checked
     * @return <code>true</code> if the value is free, <code>false</code>
     * otherwise
     * @throws GeneralRepositoryException
     * general exception
     */
    public boolean isUserLoginFree(final String login) throws GeneralRepositoryException {
        UserRepository.LOG.debug("Checking if the given login is free...");
        final ViewResult<String> result = this.queryView(UserRepository.V_LIST_BY_LOGIN, String.class, new Options().key(login));
        return result.getRows().isEmpty();
    }
    
    /**
     * Checks whether the provided e-mail is free (not yet taken by another user
     * in the database).
     * 
     * @param email
     * e-mail to be checked
     * @return <code>true</code> if the value is free, <code>false</code>
     * otherwise
     * @throws GeneralRepositoryException
     * general exception
     */
    public boolean isUserEmailFree(final String email) throws GeneralRepositoryException {
        UserRepository.LOG.debug("Checking if the given e-mail is free...");
        final Options options = new Options().key(email);
        final ViewResult<String> result = this.queryView(UserRepository.V_LIST_BY_EMAIL, String.class, options);
        return result.getRows().isEmpty();
    }
    
    /**
     * Returns a password hash for the given user e-mail. If the e-mail is not
     * found at all or the e-mail is not unique (just a theoretical change,
     * should not happen ever during normal conditions), <code>null</code> is
     * returned.
     * 
     * @param email
     * user e-mail
     * @return a tuple of (user login, user password hash) or <code>null</code>
     * if no user with that e-mail is found
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public Tuple<String, String> getUserPasswordHashByEmail(final String email) throws GeneralRepositoryException, NotFoundRepositoryException {
        UserRepository.LOG.debug("Searching for a password by e-mail...");
        final Options options = new Options().key(email);
        final ViewResult<String> result = this.queryView(UserRepository.V_LIST_BY_EMAIL, String.class, options);
        
        if (result.getRows().size() == 1) {
            // just one record for the e-mail was found
            // this is correct, so we return the password hash
            
            UserRepository.LOG.debug("User found. Returning login and the password hash...");
            final String userId = result.getRows().get(0).getValue();
            final User user = this.loadDocument(User.class, userId);
            return Tuple.of(user.getLogin(), user.getPassword());
        }
        
        UserRepository.LOG.debug("Invalid row count (should be 1): " + result.getRows().size());
        return null;
    }
    
    /**
     * Returns the limited list of top editing users.
     * 
     * @param count
     * user count (list size limit)
     * @return list of top editing users
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public List<User> getTopUsers(final int count) throws GeneralRepositoryException, NotFoundRepositoryException {
        final Options options = new Options().descending(true).limit(count);
        return this.loadDocuments(User.class, this.loadDocumentIds(UserRepository.V_TOP, options));
    }
    
    /**
     * Returns the user count for the various types, institutions and branches.
     * 
     * @return list of tuples (title, user count)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<Tuple<String, Integer>> getUserCount() throws GeneralRepositoryException {
        // prepare the query
        
        final Options options = new Options().group(true);
        
        // run the query
        
        final ViewResult<Integer> result = this.queryView(UserRepository.V_COUNT, Integer.class, options);
        
        // process results
        
        final List<Tuple<String, Integer>> list = new LinkedList<Tuple<String, Integer>>();
        
        for (final ValueRow<Integer> row : result.getRows()) {
            // get the title
            final String title = (String) row.getKey();
            // get the count
            final int count = row.getValue();
            
            // add a new row to the result
            
            list.add(Tuple.of(title, count));
        }
        
        // return results
        
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Changes the user password to another one.
     * 
     * @param user
     * user that will be changed
     * @param newPassword
     * new user password (must be not empty)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void changeUserPassword(final User user, final String newPassword) throws GeneralRepositoryException {
        if ((newPassword == null) || (newPassword.length() < 1)) {
            throw new IllegalArgumentException("Neplatná hodnota hesla. Heslo nesmí být prázdné.");
        }
        
        // update user
        
        user.setPassword(SimpleStringUtils.getHash(newPassword));
        
        // save user
        
        RetrobiApplication.db().getUserRepository().updateUser(user);
    }
    
    /**
     * Changes the user edit count.
     * 
     * @param userId
     * user ID
     * @param delta
     * change to apply (both positive or negative changes can be applied)
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public void changeUserEditCount(final String userId, final int delta) throws NotFoundRepositoryException, GeneralRepositoryException {
        if (delta == 0) {
            return;
        }
        
        final User user = this.loadDocument(User.class, userId);
        user.changeEditCount(delta);
        this.updateUser(user);
    }
    
    @Override
    public Map<String, View> createViews() {
        final Map<String, View> views = new HashMap<String, View>();
        
        // -------------
        // LIST OF USERS
        // -------------
        
        // key = e-mail
        // value = user ID
        
        final View listByEmailView = new View("" +
                "function(doc) {\n" +
                "  if (doc.TAG_user) {\n" +
                "    emit (doc.email, doc._id);\n" +
                "  }\n" +
                "}\n");
        
        views.put(UserRepository.V_LIST_BY_EMAIL, listByEmailView);
        views.put(UserRepository.V_COUNT_BY_EMAIL, AbstractProtoRepository.createCountView(listByEmailView));
        
        // key = [e-mail, password hash]
        // value = user ID
        
        views.put(UserRepository.V_LOGIN, new View("" +
                "function(doc) {\n" +
                "  if (doc.TAG_user) {\n" +
                "    emit ([doc.login, doc.password], doc._id);\n" +
                "  }\n" +
                "}\n"));
        
        // key = login
        // value = user ID
        
        views.put(UserRepository.V_LIST_BY_LOGIN, new View("" +
                "function(doc) {\n" +
                "  if (doc.TAG_user) {\n" +
                "    emit (doc.login, doc._id);\n" +
                "  }\n" +
                "}\n"));
        
        // -------------------
        // LIST OF TOP-EDITORS
        // -------------------
        
        // key = edit count
        // value = 'user email - user edit count'
        
        views.put(UserRepository.V_TOP, new View("" +
                "function(doc) {\n" +
                "  if (doc.TAG_user && doc.count_edit > 0) {\n" +
                "    emit (doc.count_edit, doc._id);\n" +
                "  }\n" +
                "}\n"));
        
        // -----------------------
        // LIST OF USER TYPES ETC.
        // -----------------------
        
        // key = title
        // value = user count (grouped by title)
        
        views.put(UserRepository.V_COUNT, new View(
                "" +
                        "function (doc) {\n" +
                        "  if (doc.TAG_user) {\n" +
                        "    if (doc.type) emit('Typ: ' + doc.type, 1);\n" +
                        "    if (doc.branch) emit('Obor: ' + doc.branch, 1);\n" +
                        "    if (doc.alma) emit('Instituce: ' + doc.alma, 1);\n" +
                        "  }\n" +
                        "}\n",
                "" +
                        "function (key, values) {\n" +
                        "  return sum(values);\n" +
                        "}\n"));
        
        return Collections.unmodifiableMap(views);
    }
    
    @Override
    public void createStartupDocuments() throws GeneralRepositoryException {
        UserRepository.LOG.info("Initializing the first user in the database (admin / admin)...");
        
        final User adam = new User();
        adam.setLogin("admin");
        adam.setPassword(SimpleStringUtils.getHash("admin"));
        adam.setEmail("retrobi@ucl.cas.cz");
        adam.setRole(UserRole.ADMIN);
        this.createDocument(adam);
    }
}
