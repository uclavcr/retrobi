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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobitool.importer.model.ImporterModel;
import cz.insophy.retrobitool.importer.model.ImporterModelListener;
import cz.insophy.retrobitool.importer.model.LoggingListModel;

/**
 * Upload panel. Contains a list for debug messages. This list automatically
 * scrolls down when a new debug message is added. It is also possible to clear
 * this log.
 * 
 * @author Vojtěch Hordějčuk
 */
public class UploadPanel extends JPanel implements ActionListener, ImporterModelListener {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * main model
     */
    private final ImporterModel model;
    /**
     * logging list model
     */
    private LoggingListModel listModel;
    /**
     * log list
     */
    private final JList list;
    /**
     * list scroll
     */
    private final JScrollPane listScroll;
    /**
     * button causing upload to start
     */
    private final JButton uploadButton;
    /**
     * progress bar
     */
    private final JProgressBar progress;
    /**
     * button for pausing the process
     */
    private final JToggleButton pauseButton;
    /**
     * button for canceling the process
     */
    private final JButton cancelButton;
    /**
     * panel with all command buttons
     */
    private final JPanel bottomPanel;
    
    /**
     * Creates a new instance.
     * 
     * @param model
     * main model
     */
    protected UploadPanel(final ImporterModel model) {
        super();
        
        // prepare models
        
        this.model = model;
        this.listModel = null;
        
        // create components
        
        this.list = new JList();
        this.listScroll = new JScrollPane(this.list);
        this.uploadButton = new JButton("Odeslat na server", new ImageIcon(FilesPanel.class.getResource("upload.png")));
        this.uploadButton.setToolTipText("Odeslat nové lístky na server");
        this.progress = new JProgressBar(0, 100);
        this.pauseButton = new JToggleButton("Pauza");
        this.pauseButton.setToolTipText("Pozastavit nahrávání lístků");
        this.cancelButton = new JButton("Zrušit");
        this.cancelButton.setToolTipText("Zrušit nahrávání lístků na server");
        this.bottomPanel = new JPanel();
        
        // setup components
        
        this.cancelButton.setEnabled(false);
        this.pauseButton.setEnabled(false);
        this.bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setLayout(new BorderLayout());
        
        // place components
        
        this.bottomPanel.add(this.uploadButton);
        this.bottomPanel.add(this.progress);
        this.bottomPanel.add(this.pauseButton);
        this.bottomPanel.add(this.cancelButton);
        this.add(new JLabel("<html><big>Odeslat</big><br>Tento panel zobrazuje poslední stav protokolu o odesílání lístků na server.</html>"), BorderLayout.NORTH);
        this.add(this.listScroll, BorderLayout.CENTER);
        this.add(this.bottomPanel, BorderLayout.SOUTH);
        
        // add listeners
        
        this.model.addListener(this);
        this.uploadButton.addActionListener(this);
        this.pauseButton.addActionListener(this);
        this.cancelButton.addActionListener(this);
    }
    
    // ======
    // EVENTS
    // ======
    
    @Override
    public void filesChanged() {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                UploadPanel.this.progress.setValue(0);
            }
        });
    }
    
    @Override
    public void cardsChanged() {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                UploadPanel.this.progress.setValue(0);
            }
        });
    }
    
    @Override
    public void uploadStarted() {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                // clear log
                
                try {
                    UploadPanel.this.startLog();
                } catch (final IOException x) {
                    throw new IllegalStateException("Chyba při vytváření nového logu.", x);
                }
                
                // update components
                
                UploadPanel.this.getParent().setEnabled(false);
                UploadPanel.this.progress.setMaximum(1);
                UploadPanel.this.progress.setValue(0);
                UploadPanel.this.listModel.clear();
                UploadPanel.this.uploadButton.setEnabled(false);
                UploadPanel.this.pauseButton.setSelected(false);
                UploadPanel.this.pauseButton.setEnabled(true);
                UploadPanel.this.cancelButton.setEnabled(true);
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
                // update components
                
                UploadPanel.this.getParent().setEnabled(true);
                UploadPanel.this.pauseButton.setSelected(false);
                UploadPanel.this.progress.setMaximum(1);
                UploadPanel.this.progress.setValue(1);
                UploadPanel.this.uploadButton.setEnabled(true);
                UploadPanel.this.pauseButton.setEnabled(false);
                UploadPanel.this.cancelButton.setEnabled(false);
                
                try {
                    UploadPanel.this.endLog();
                } catch (final IOException x) {
                    throw new IllegalStateException("Chyba při ukončování logu.", x);
                }
            }
        });
    }
    
    @Override
    public void uploadStatusChanged(final String message, final Card card, final File file) {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                try {
                    // add log message to the list
                    
                    UploadPanel.this.listModel.addElementWithWrite(message, card, file);
                    
                    // scroll to the last item in the list
                    
                    UploadPanel.this.scrollLog();
                } catch (final IOException x) {
                    // NOP
                }
            }
        });
    }
    
    @Override
    public void uploadStatusChanged(final String message, final Card card, final File file, final int done, final int total) {
        this.uploadStatusChanged(message, card, file);
        
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                // update progress
                
                UploadPanel.this.progress.setMaximum(total);
                UploadPanel.this.progress.setValue(done);
            }
        });
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(this.uploadButton)) {
            // ------------
            // UPLOAD CARDS
            // ------------
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // card upload will be done in a separate thread
                    
                    try {
                        UploadPanel.this.model.uploadCards();
                    } catch (final Exception x) {
                        UploadPanel.this.uploadStatusChanged("Chyba při odesílání karet: " + x.getMessage(), null, null);
                    }
                }
            }).start();
        } else if (e.getSource().equals(this.pauseButton)) {
            // ------------
            // PAUSE UPLOAD
            // ------------
            
            this.model.togglePause();
        } else if (e.getSource().equals(this.cancelButton)) {
            // -------------
            // CANCEL UPLOAD
            // -------------
            
            this.model.cancel();
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    // ===============
    // UTILITY METHODS
    // ===============
    
    /**
     * Starts the log file.
     * 
     * @throws IOException
     * IO exception
     */
    private void startLog() throws IOException {
        this.listModel = new LoggingListModel(this.model.getLogDirectory());
        this.list.setModel(this.listModel);
    }
    
    /**
     * Ends the log file.
     * 
     * @throws IOException
     * IO exception
     */
    private void endLog() throws IOException {
        this.listModel.close();
        this.listModel = null;
    }
    
    /**
     * Scrolls the log list view to the last item.
     */
    private void scrollLog() {
        final int index = UploadPanel.this.listModel.getSize() - 1;
        
        if (index >= 0) {
            UploadPanel.this.list.ensureIndexIsVisible(index);
            UploadPanel.this.list.setSelectedIndex(index);
            UploadPanel.this.list.repaint();
        }
    }
}
