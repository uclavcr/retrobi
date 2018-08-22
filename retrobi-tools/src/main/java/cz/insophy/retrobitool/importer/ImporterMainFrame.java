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

package cz.insophy.retrobitool.importer;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobitool.CommonLoginDialog;
import cz.insophy.retrobitool.importer.model.ImporterModel;
import cz.insophy.retrobitool.importer.model.ImporterModelListener;

/**
 * Importer main frame. The frame is composed of tabs, each of containing one
 * step of the import process:
 * <ol>
 * <li>specify input and output directory</li>
 * <li>specify batch properties</li>
 * <li>review cards</li>
 * <li>upload cards and images</li>
 * </ol>
 * 
 * @author Vojtěch Hordějčuk
 */
public class ImporterMainFrame extends JFrame implements ImporterModelListener, WindowListener { // NO_UCD
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Starts the application.<br>
     * Usage: <code>APPNAME [source target]</code>
     * 
     * @param args
     * arguments
     * @throws GeneralRepositoryException
     * exception
     */
    public static void main(final String[] args) throws GeneralRepositoryException {
        Settings.setupLookAndFeel();
        
        String sourceDir = "";
        String targetDir = "";
        
        if (args.length == 2) {
            sourceDir = args[0];
            targetDir = args[1];
        }
        
        ImporterMainFrame.start(sourceDir, targetDir);
    }
    
    /**
     * Starts the application.
     * 
     * @param sourceDir
     * default source directory (or "")
     * @param targetDir
     * default target directory (or "")
     * @throws GeneralRepositoryException
     * repository exception
     */
    private static void start(final String sourceDir, final String targetDir) throws GeneralRepositoryException {
        // show login dialog
        
        final CommonLoginDialog loginDialog = new CommonLoginDialog();
        loginDialog.showDialog();
        
        // show main dialog only after the administrator is logged
        
        if (loginDialog.hasRoleAtLeast(UserRole.EDITOR)) {
            // show the main frame
            
            new ImporterMainFrame(sourceDir, targetDir).setVisible(true);
        } else {
            ImporterMainFrame.terminate();
        }
    }
    
    /**
     * Terminates the application.
     */
    private static void terminate() {
        System.exit(0);
    }
    
    /**
     * main model
     */
    private final ImporterModel model;
    /**
     * tabbed pane
     */
    private final JTabbedPane tabs;
    
    /**
     * Creates a new instance.
     * 
     * @param sourceDir
     * default source directory (or "")
     * @param targetDir
     * default target directory (or "")
     * @throws GeneralRepositoryException
     * exception
     */
    private ImporterMainFrame(final String sourceDir, final String targetDir) throws GeneralRepositoryException {
        super("Nástroj pro import lístků");
        
        // prepare model
        
        this.model = new ImporterModel();
        
        // create components
        
        final FilesPanel filesPanel = new FilesPanel(this.model, sourceDir, targetDir);
        final CardPanel cardPanel = new CardPanel(this.model);
        final UploadPanel uploadPanel = new UploadPanel(this.model);
        this.tabs = new JTabbedPane();
        
        // setup components
        
        this.tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        this.setLayout(new BorderLayout());
        
        // place components
        
        this.tabs.addTab("1. Soubory", filesPanel);
        this.tabs.addTab("2. Lístky", cardPanel);
        this.tabs.addTab("3. Upload", uploadPanel);
        this.add(this.tabs, BorderLayout.CENTER);
        
        // setup frame
        
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.pack();
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        
        // setup listeners
        
        this.addWindowListener(this);
        this.model.addListener(this);
    }
    
    // ======
    // EVENTS
    // ======
    
    @Override
    public void filesChanged() {
        // nothing
    }
    
    @Override
    public void cardsChanged() {
        // nothing
    }
    
    @Override
    public void uploadStarted() {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                ImporterMainFrame.this.tabs.setEnabled(false);
            }
        });
    }
    
    @Override
    public void uploadFinished() {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                ImporterMainFrame.this.tabs.setEnabled(true);
            }
        });
    }
    
    @Override
    public void uploadStatusChanged(final String message, final Card card, final File file) {
        // nothing
    }
    
    @Override
    public void uploadStatusChanged(final String message, final Card card, final File file, final int done, final int total) {
        // nothing
    }
    
    @Override
    public void windowOpened(final WindowEvent e) {
        // nothing
    }
    
    @Override
    public void windowClosing(final WindowEvent e) {
        // safe terminate when closing
        
        ImporterMainFrame.terminate();
    }
    
    @Override
    public void windowClosed(final WindowEvent e) {
        // nothing
    }
    
    @Override
    public void windowIconified(final WindowEvent e) {
        // nothing
    }
    
    @Override
    public void windowDeiconified(final WindowEvent e) {
        // nothing
    }
    
    @Override
    public void windowActivated(final WindowEvent e) {
        // nothing
    }
    
    @Override
    public void windowDeactivated(final WindowEvent e) {
        // nothing
    }
}
