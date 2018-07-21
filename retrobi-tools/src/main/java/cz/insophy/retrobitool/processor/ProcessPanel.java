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

package cz.insophy.retrobitool.processor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.utils.library.SimpleFrameUtils;
import cz.insophy.retrobitool.processor.model.ProcessorModel;
import cz.insophy.retrobitool.processor.model.ProcessorModelListener;
import cz.insophy.retrobitool.processor.table.FilesTableModel;

/**
 * Process panel. Contains a controls for loading the file list, changing the
 * output directories and running the process. It also shows a progress of the
 * process.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ProcessPanel extends JPanel implements ActionListener, ItemListener, ProcessorModelListener {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * main model
     */
    private final ProcessorModel model;
    /**
     * catalog combobox
     */
    private final JComboBox catalogCombo;
    /**
     * source directory location field
     */
    private final JTextField sourceDirLocation;
    /**
     * button for choosing the source directory
     */
    private final JButton sourceDirChoose;
    /**
     * backup directory location field
     */
    private final JTextField backupDirLocation;
    /**
     * button for choosing the backup directory
     */
    private final JButton backupDirChoose;
    /**
     * button for loading the files
     */
    private final JButton loadButton;
    /**
     * button for starting the process
     */
    private final JButton processButton;
    /**
     * button for pausing the process
     */
    private final JToggleButton pauseButton;
    /**
     * button for canceling the process
     */
    private final JButton cancelButton;
    /**
     * progress bar of the files
     */
    private final JProgressBar fileProgress;
    /**
     * blank page detector parameter slider (threshold)
     */
    private final JSpinner paramThreshold;
    /**
     * blank page detector parameter slider (tolerance)
     */
    private final JSpinner paramTolerance;
    /**
     * blank page detector parameter slider (shave)
     */
    private final JSpinner paramShave;
    /**
     * button for analyzing empty pages
     */
    private final JButton analyzeButton;
    /**
     * files table model
     */
    private final FilesTableModel tableModel;
    /**
     * running thread
     */
    private Thread thread;
    
    /**
     * Creates a new instance.
     * 
     * @param model
     * main applet model
     * @param sourceDirName
     * default backup directory
     * @param backupDirName
     * default source directory
     * @param tableModel
     * files table mode
     */
    protected ProcessPanel(final ProcessorModel model, final String sourceDirName, final String backupDirName, final FilesTableModel tableModel) {
        super();
        
        // prepare models
        
        this.model = model;
        this.thread = null;
        
        // create components
        
        this.catalogCombo = new JComboBox(Catalog.values());
        this.sourceDirLocation = new JTextField(sourceDirName, 35);
        this.sourceDirLocation.setEditable(false);
        this.sourceDirChoose = new JButton("Procházet...", new ImageIcon(ProcessPanel.class.getResource("open.png")));
        this.sourceDirChoose.setToolTipText("Procházet složky na disku");
        this.backupDirLocation = new JTextField(backupDirName, 35);
        this.backupDirLocation.setEditable(false);
        this.backupDirChoose = new JButton("Procházet...", new ImageIcon(ProcessPanel.class.getResource("open.png")));
        this.backupDirChoose.setToolTipText("Procházet složky na disku");
        this.loadButton = new JButton("Načíst soubory", new ImageIcon(ProcessPanel.class.getResource("load.png")));
        this.loadButton.setToolTipText("Načíst naskenované soubory ze zadané složky");
        this.processButton = new JButton("Spustit", new ImageIcon(ProcessPanel.class.getResource("start.png")));
        this.processButton.setToolTipText("Zpracovat naskenované soubory");
        this.pauseButton = new JToggleButton("Pauza", new ImageIcon(ProcessPanel.class.getResource("pause.png")));
        this.pauseButton.setToolTipText("Pozastavit zpracování souborů");
        this.cancelButton = new JButton("Zrušit", new ImageIcon(ProcessPanel.class.getResource("stop.png")));
        this.cancelButton.setToolTipText("Přerušit zpracování");
        this.fileProgress = new JProgressBar();
        this.fileProgress.setMinimumSize(new Dimension(100, 24));
        this.paramThreshold = new JSpinner(new SpinnerNumberModel(Settings.DEFAULT_NONBLANK_DARK_THRESHOLD, 0, 100, 1));
        this.paramTolerance = new JSpinner(new SpinnerNumberModel(Settings.DEFAULT_NONBLANK_DARK_TOLERANCE, 0, 1000, 1));
        this.paramShave = new JSpinner(new SpinnerNumberModel(Settings.DEFAULT_SHAVE, 0, 100, 1));
        this.analyzeButton = new JButton("Analyzovat prázdné", new ImageIcon(ProcessPanel.class.getResource("analyze.png")));
        this.analyzeButton.setToolTipText("Vyhledat prázdné lístky v načtených souborech");
        this.tableModel = tableModel;
        
        // setup components
        
        this.setLayout(new GridBagLayout());
        this.processButton.setEnabled(false);
        this.pauseButton.setEnabled(false);
        this.cancelButton.setEnabled(false);
        this.analyzeButton.setEnabled(false);
        this.fileProgress.setStringPainted(true);
        
        // place components
        
        GridBagConstraints c;
        final Insets i = new Insets(3, 5, 3, 5);
        
        c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(new JLabel("Část katalogu:"), c);
        c = new GridBagConstraints(1, 0, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.catalogCombo, c);
        c = new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(new JLabel("Složka naskenovaných obrázků:"), c);
        c = new GridBagConstraints(1, 1, 3, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.sourceDirLocation, c);
        c = new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.sourceDirChoose, c);
        c = new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(new JLabel("Složka se zálohou:"), c);
        c = new GridBagConstraints(1, 2, 3, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.backupDirLocation, c);
        c = new GridBagConstraints(4, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.backupDirChoose, c);
        c = new GridBagConstraints(1, 3, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.loadButton, c);
        c = new GridBagConstraints(3, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.processButton, c);
        c = new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.fileProgress, c);
        c = new GridBagConstraints(2, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.pauseButton, c);
        c = new GridBagConstraints(3, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.cancelButton, c);
        
        c = new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(new JLabel("Detekce prázdných lístků:"), c);
        c = new GridBagConstraints(1, 5, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(new JLabel("Práh citlivosti na text (%):"), c);
        c = new GridBagConstraints(3, 5, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.paramThreshold, c);
        c = new GridBagConstraints(1, 6, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(new JLabel("Minimální množství textu (promile):"), c);
        c = new GridBagConstraints(3, 6, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.paramTolerance, c);
        c = new GridBagConstraints(1, 7, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(new JLabel("Ořezávání okrajů stránky (%):"), c);
        c = new GridBagConstraints(3, 7, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.paramShave, c);
        c = new GridBagConstraints(3, 8, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.add(this.analyzeButton, c);
        
        // setup listeners
        
        this.catalogCombo.addItemListener(this);
        this.sourceDirChoose.addActionListener(this);
        this.backupDirChoose.addActionListener(this);
        this.loadButton.addActionListener(this);
        this.processButton.addActionListener(this);
        this.pauseButton.addActionListener(this);
        this.cancelButton.addActionListener(this);
        this.analyzeButton.addActionListener(this);
        model.addListener(this);
        
        // default values
        
        this.catalogCombo.setSelectedItem(null);
        this.catalogCombo.setSelectedItem(Catalog.A);
    }
    
    // ======
    // EVENTS
    // ======
    
    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (e.getSource().equals(this.catalogCombo)) {
            // ----------------
            // CATALOG SELECTED
            // ----------------
            
            if (e.getStateChange() == ItemEvent.SELECTED) {
                this.model.setCatalog((Catalog) this.catalogCombo.getSelectedItem());
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(this.sourceDirChoose)) {
            // -----------------------
            // CHOOSE SOURCE DIRECTORY
            // -----------------------
            
            this.chooserDirectory(this.sourceDirLocation);
        } else if (e.getSource().equals(this.backupDirChoose)) {
            // ------------------------
            // CHOOSER BACKUP DIRECTORY
            // ------------------------
            
            this.chooserDirectory(this.backupDirLocation);
        } else if (e.getSource().equals(this.loadButton)) {
            // -----------
            // FETCH FILES
            // -----------
            
            if (this.sourceDirLocation.getText().length() < 1) {
                return;
            }
            
            if (this.backupDirLocation.getText().length() < 1) {
                return;
            }
            
            try {
                // set the directories
                
                this.model.setSourceDirectory(new File(this.sourceDirLocation.getText().trim()));
                this.model.setTargetDirectory(new File(this.backupDirLocation.getText().trim()));
                
                // load the files into the model
                
                this.model.loadFiles();
                
                // check the file count
                
                if (this.model.getFiles().size() < 1) {
                    SimpleFrameUtils.showInformation("Nebyly načteny žádné soubory.");
                }
            } catch (final Exception x) {
                SimpleFrameUtils.showError(x);
            }
        } else if (e.getSource().equals(this.processButton) || e.getSource().equals(this.analyzeButton)) {
            // -----------------
            // START THE PROCESS
            // -----------------
            
            if (this.sourceDirLocation.getText().length() < 1) {
                return;
            }
            
            if (this.backupDirLocation.getText().length() < 1) {
                return;
            }
            
            this.model.setSourceDirectory(new File(this.sourceDirLocation.getText().trim()));
            this.model.setTargetDirectory(new File(this.backupDirLocation.getText().trim()));
            
            this.thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // process the files
                        
                        ProcessPanel.this.model.process(
                                ((Integer) ProcessPanel.this.paramThreshold.getValue()) * 0.01,
                                ((Integer) ProcessPanel.this.paramTolerance.getValue()) * 0.001,
                                ((Integer) ProcessPanel.this.paramShave.getValue()) * 0.01,
                                e.getSource().equals(ProcessPanel.this.analyzeButton));
                        
                        // done
                        
                        SimpleFrameUtils.showInformation("Zpracování obrázků bylo dokončeno.");
                    }
                    catch (final Exception x) {
                        SimpleFrameUtils.showError(x);
                    }
                }
            });
            
            this.pauseButton.setSelected(false);
            this.thread.start();
        } else if (e.getSource().equals(this.pauseButton)) {
            // -----------------
            // PAUSE THE PROCESS
            // -----------------
            
            this.model.togglePause();
        } else if (e.getSource().equals(this.cancelButton)) {
            // ------------------
            // CANCEL THE PROCESS
            // ------------------
            
            this.pauseButton.setSelected(false);
            this.cancelButton.setEnabled(false);
            this.model.cancel();
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public void filesUpdated() {
        // enable process button only if some files are loaded
        
        this.processButton.setEnabled(this.model.getFiles().size() > 0);
        this.analyzeButton.setEnabled(this.model.getFiles().size() > 0);
    }
    
    @Override
    public void processStarted() {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // reset the progress
                
                ProcessPanel.this.fileProgress.setValue(0);
                
                // disable all components except the cancel button
                
                for (final Component c : ProcessPanel.this.getComponents()) {
                    c.setEnabled(false);
                }
                
                ProcessPanel.this.cancelButton.setEnabled(true);
                ProcessPanel.this.pauseButton.setEnabled(true);
            }
        });
    }
    
    @Override
    public void processStatusUpdated(final int total, final int done) {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // update the progress
                
                ProcessPanel.this.fileProgress.setMaximum(total);
                ProcessPanel.this.fileProgress.setValue(done);
            }
        });
    }
    
    @Override
    public void processFinished() {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // reset the progress
                
                ProcessPanel.this.fileProgress.setValue(0);
                
                // enable all components except the cancel button
                
                for (final Component c : ProcessPanel.this.getComponents()) {
                    c.setEnabled(true);
                }
                
                ProcessPanel.this.cancelButton.setEnabled(false);
                ProcessPanel.this.pauseButton.setEnabled(false);
                
                // save the table to a CSV file
                
                try {
                    final File outputFile = ProcessPanel.getFileForToday(ProcessPanel.this.model.getLogDirectory());
                    ProcessPanel.this.tableModel.exportToCSV(outputFile);
                    SimpleFrameUtils.showInformation("Tabulka byla uložena do souboru:" + Settings.LINE_END + outputFile.getAbsolutePath());
                } catch (final IOException x) {
                    SimpleFrameUtils.showError("Chyba při zápisu tabulky do souboru: " + x.getMessage());
                }
            }
        });
    }
    
    @Override
    public void processFailed() {
        // the same as if the process finished
        
        this.processFinished();
    }
    
    // ===============
    // UTILITY METHODS
    // ===============
    
    /**
     * Returns the filename for today. The filename is time based (for example
     * <code>2005-12-24_13-42.csv</code>).
     * 
     * @param outputDir
     * output directory
     * @return filename for today
     */
    private static File getFileForToday(final File outputDir) {
        final Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH + 1);
        final int day = c.get(Calendar.DATE);
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);
        return new File(outputDir, String.format("process_%04d-%02d-%02d_%02d-%02d.csv", year, month, day, hour, minute));
    }
    
    /**
     * Displays a directory chooser and writes a path of the selected directory
     * into the target text field provided.
     * 
     * @param target
     * target text field, where the path goes
     */
    private void chooserDirectory(final JTextField target) {
        if (SimpleFrameUtils.chooseDirectory(target)) {
            // fill other empty fields if they are empty
            // value is based on the source directory
            
            if (target.equals(this.sourceDirLocation)) {
                final File parent = new File(target.getText()).getParentFile();
                
                if (this.backupDirLocation.getText().length() < 1) {
                    this.backupDirLocation.setText(new File(parent, "zaloha_tif").getAbsolutePath());
                }
            }
        }
    }
}
