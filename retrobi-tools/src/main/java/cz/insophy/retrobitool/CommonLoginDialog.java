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

package cz.insophy.retrobitool;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleFrameUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Login dialog that provides functionality to validate inserted login /
 * password combination against the database and to load the correct user.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CommonLoginDialog implements ActionListener {
    /**
     * logged user
     */
    private User loggedUser;
    /**
     * main login dialog
     */
    private final JDialog dialog;
    /**
     * login text field
     */
    private final JTextField loginField;
    /**
     * password text field
     */
    private final JPasswordField passwordField;
    /**
     * OK button
     */
    private final JButton okButton;
    /**
     * clear button
     */
    private final JButton clearButton;
    /**
     * cancel button
     */
    private final JButton cancelButton;
    
    /**
     * Creates a new instance.
     */
    public CommonLoginDialog() {
        // initialize model
        
        this.loggedUser = null;
        
        // create dialog
        
        this.dialog = new JDialog();
        this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.dialog.setTitle("Přihlášení");
        this.dialog.setModal(true);
        this.dialog.setAlwaysOnTop(true);
        this.dialog.setResizable(false);
        this.dialog.setLayout(new GridBagLayout());
        
        // create components
        
        this.loginField = new JTextField(20);
        this.passwordField = new JPasswordField(20);
        this.okButton = new JButton("Přihlásit se", new ImageIcon(CommonLoginDialog.class.getResource("login.png")));
        this.okButton.setToolTipText("Odeslat zadané údaje a přihlásit se do systému");
        this.clearButton = new JButton("Reset", new ImageIcon(CommonLoginDialog.class.getResource("cancel.png")));
        this.clearButton.setToolTipText("Vymazat zadané údaje");
        this.cancelButton = new JButton("Zrušit", new ImageIcon(CommonLoginDialog.class.getResource("exit.png")));
        this.cancelButton.setToolTipText("Zavřít dialog a ukončit aplikaci");
        
        // place components
        
        GridBagConstraints c = null;
        final Insets i = new Insets(5, 5, 5, 5);
        
        c = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.dialog.add(new JLabel("Login:"), c);
        c = new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.dialog.add(this.loginField, c);
        c = new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.dialog.add(new JLabel("Heslo:"), c);
        c = new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.dialog.add(this.passwordField, c);
        c = new GridBagConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.dialog.add(this.okButton, c);
        c = new GridBagConstraints(0, 3, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.dialog.add(this.clearButton, c);
        c = new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.dialog.add(this.cancelButton, c);
        
        this.dialog.pack();
        this.dialog.setLocationRelativeTo(null);
        
        // setup listeners
        
        this.loginField.addActionListener(this);
        this.passwordField.addActionListener(this);
        this.okButton.addActionListener(this);
        this.clearButton.addActionListener(this);
        this.cancelButton.addActionListener(this);
    }
    
    /**
     * Resets and shows the model dialog.
     */
    public void showDialog() {
        this.loggedUser = null;
        this.loginField.setText("");
        this.passwordField.setText("");
        this.dialog.setVisible(false);
        this.dialog.setLocationRelativeTo(null);
        this.dialog.setVisible(true);
    }
    
    /**
     * Checks whether the user is logged in and has enough privileges. That is,
     * a role with equal or higher privileges than has the role specified.
     * 
     * @param minRole
     * minimal user role
     * @return <code>true</code> if the user is logged in and has the privileges
     */
    public boolean hasRoleAtLeast(final UserRole minRole) {
        if (this.loggedUser == null) {
            return false;
        }
        
        if (!this.loggedUser.hasRoleAtLeast(minRole)) {
            SimpleFrameUtils.showError(String.format("Nedostatečná oprávnění. Musíte být alespoň %s.", minRole.toString()));
            return false;
        }
        
        return true;
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(this.loginField)) {
            // ----------------------
            // ENTER PRESSED AT LOGIN
            // ----------------------
            
            e.setSource(this.okButton);
            this.actionPerformed(e);
        } else if (e.getSource().equals(this.passwordField)) {
            // -------------------------
            // ENTER PRESSED AT PASSWORD
            // -------------------------
            
            e.setSource(this.okButton);
            this.actionPerformed(e);
        } else if (e.getSource().equals(this.okButton)) {
            // -----
            // LOGIN
            // -----
            
            final String login = this.loginField.getText();
            final char[] rawPassword = this.passwordField.getPassword();
            final String password = SimpleStringUtils.getHash(new String(rawPassword));
            
            for (int i = 0; i < rawPassword.length; i++) {
                // zero chars for security purposes
                
                rawPassword[i] = 0;
            }
            
            try {
                this.loggedUser = RetrobiApplication.db().getUserRepository().getUser(login, password);
            } catch (final GeneralRepositoryException x) {
                SimpleFrameUtils.showError("Chyba při ověřování uživatele.");
            } catch (final NotFoundRepositoryException x) {
                SimpleFrameUtils.showError("Potřebná data o uživateli nebyla nalezena.");
            }
            
            if (this.loggedUser != null) {
                this.dialog.dispose();
            } else {
                this.clear();
                SimpleFrameUtils.showError("Nesprávné přihlašovací údaje.");
            }
        } else if (e.getSource().equals(this.clearButton)) {
            // ------------
            // CLEAR FIELDS
            // ------------
            
            this.clear();
        } else if (e.getSource().equals(this.cancelButton)) {
            // ------
            // CANCEL
            // ------
            
            this.loggedUser = null;
            this.dialog.setVisible(false);
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Clears the dialog and transfers the focus to the login field.
     */
    private void clear() {
        this.loggedUser = null;
        this.loginField.setText("");
        this.passwordField.setText("");
        this.loginField.requestFocus();
    }
}
