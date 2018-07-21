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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.insophy.retrobi.utils.library.SimpleFrameUtils;
import cz.insophy.retrobitool.importer.model.ImporterModel;
import cz.insophy.retrobitool.importer.table.FileListTableModel;

/**
 * Files panel. This panel contains a table with all loaded files.
 * 
 * @author Vojtěch Hordějčuk
 */
public class FilesPanel extends JPanel implements ActionListener, ChangeListener {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * main model
     */
    private final ImporterModel model;
    /**
     * table model
     */
    private final FileListTableModel tableModel;
    /**
     * file table
     */
    private final JTable table;
    /**
     * table scroll
     */
    private final JScrollPane tableScroll;
    /**
     * source directory location field
     */
    private final JTextField tifSourceDirLocation;
    /**
     * button for choosing the source directory
     */
    private final JButton tifSourceDirChoose;
    /**
     * backup directory location field
     */
    private final JTextField tifTargetDirLocation;
    /**
     * button for choosing the backup directory
     */
    private final JButton tifTargetDirChoose;
    /**
     * log directory location field
     */
    private final JTextField logDirLocation;
    /**
     * button for choosing the log directory
     */
    private final JButton logDirChoose;
    /**
     * button for loading the files
     */
    private final JButton loadTifButton;
    /**
     * checkbox for OCR sorting
     */
    private final JCheckBox sortOcrCheck;
    /**
     * panel with command buttons
     */
    private final JPanel bottomPanel;
    
    /**
     * Creates a new instance.
     * 
     * @param model
     * main model
     * @param sourceDir
     * default source directory (or "")
     * @param targetDir
     * default target directory (or "")
     */
    protected FilesPanel(final ImporterModel model, final String sourceDir, final String targetDir) {
        super();
        
        // prepare models
        
        this.model = model;
        this.tableModel = new FileListTableModel(this.model);
        
        // create components
        
        this.table = new JTable(this.tableModel);
        this.tableScroll = new JScrollPane(this.table);
        this.bottomPanel = new JPanel();
        this.tifSourceDirLocation = new JTextField(sourceDir, 35);
        this.tifSourceDirLocation.setEditable(false);
        this.tifSourceDirChoose = new JButton("Procházet...", new ImageIcon(FilesPanel.class.getResource("open.png")));
        this.tifSourceDirChoose.setToolTipText("Procházet složky na disku");
        this.tifTargetDirLocation = new JTextField(targetDir, 35);
        this.tifTargetDirLocation.setEditable(false);
        this.tifTargetDirChoose = new JButton("Procházet...", new ImageIcon(FilesPanel.class.getResource("open.png")));
        this.tifTargetDirChoose.setToolTipText("Procházet složky na disku");
        this.logDirLocation = new JTextField(targetDir, 35);
        this.logDirLocation.setEditable(false);
        this.logDirChoose = new JButton("Procházet...", new ImageIcon(FilesPanel.class.getResource("open.png")));
        this.logDirChoose.setToolTipText("Procházet složky na disku");
        this.loadTifButton = new JButton("Načíst soubory", new ImageIcon(FilesPanel.class.getResource("load.png")));
        this.loadTifButton.setToolTipText("Načíst soubory z vybrané složky");
        this.sortOcrCheck = new JCheckBox("Řadit soubory dle přítomnosti OCR", false);
        
        // setup components
        
        this.bottomPanel.setLayout(new GridBagLayout());
        this.setLayout(new BorderLayout());
        
        // place components
        
        GridBagConstraints c = null;
        final Insets i = new Insets(3, 5, 3, 5);
        
        c = new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(this.sortOcrCheck, c);
        c = new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(new JLabel("Vstupní složka (TIF nebo PNG):"), c);
        c = new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(this.tifSourceDirLocation, c);
        c = new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(this.tifSourceDirChoose, c);
        c = new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(new JLabel("Složka pro zálohu (PNG):"), c);
        c = new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(this.tifTargetDirLocation, c);
        c = new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(this.tifTargetDirChoose, c);
        c = new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(new JLabel("Složka protokolů:"), c);
        c = new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(this.logDirLocation, c);
        c = new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(this.logDirChoose, c);
        c = new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, i, 0, 0);
        this.bottomPanel.add(this.loadTifButton, c);
        
        this.add(new JLabel("<html><big>Soubory</big><br>Načtěte složku, ze které které chcete importovat zpracované lístky. Je možné importovat soubory TIF i PNG.<br>Spolu se soubory se načtou i jejich OCR přepisy.</html>"), BorderLayout.NORTH);
        this.add(this.tableScroll, BorderLayout.CENTER);
        this.add(this.bottomPanel, BorderLayout.SOUTH);
        
        // setup listeners
        
        this.tifSourceDirChoose.addActionListener(this);
        this.tifTargetDirChoose.addActionListener(this);
        this.logDirChoose.addActionListener(this);
        this.loadTifButton.addActionListener(this);
        this.sortOcrCheck.addChangeListener(this);
        this.tableModel.addTableModelListener(this.table);
        this.model.addListener(this.tableModel);
    }
    
    // ======
    // EVENTS
    // ======
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(this.tifSourceDirChoose)) {
            // -----------------------
            // CHOOSE SOURCE DIRECTORY
            // -----------------------
            
            this.chooserTifDirectory(this.tifSourceDirLocation);
        } else if (e.getSource().equals(this.tifTargetDirChoose)) {
            // ------------------------
            // CHOOSER TARGET DIRECTORY
            // ------------------------
            
            this.chooserTifDirectory(this.tifTargetDirLocation);
        } else if (e.getSource().equals(this.logDirChoose)) {
            // ---------------------
            // CHOOSER LOG DIRECTORY
            // ---------------------
            
            SimpleFrameUtils.chooseDirectory(this.logDirLocation);
        } else if (e.getSource().equals(this.loadTifButton)) {
            // -----------
            // FETCH FILES
            // -----------
            
            if (this.tifSourceDirLocation.getText().length() < 1) {
                SimpleFrameUtils.showInformation("Vyberte prosím vstupní složku.");
                return;
            }
            
            if (this.tifTargetDirLocation.getText().length() < 1) {
                SimpleFrameUtils.showInformation("Vyberte prosím výstupní složku.");
                return;
            }
            
            if (this.tifTargetDirLocation.getText().length() < 1) {
                SimpleFrameUtils.showInformation("Vyberte prosím složku pro logy.");
                return;
            }
            
            try {
                // set directories
                
                this.model.setSourceDirectory(new File(this.tifSourceDirLocation.getText().trim()));
                this.model.setTargetDirectory(new File(this.tifTargetDirLocation.getText().trim()));
                this.model.setLogDirectory(new File(this.logDirLocation.getText().trim()));
                
                // load the files
                
                this.model.loadFiles();
            } catch (final Exception x) {
                SimpleFrameUtils.showError(x);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public void stateChanged(final ChangeEvent e) {
        if (e.getSource().equals(this.sortOcrCheck)) {
            // -----------
            // SWITCH SORT
            // -----------
            
            this.tableModel.switchSortMode(this.sortOcrCheck.isSelected());
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    // ===============
    // UTILITY METHODS
    // ===============
    
    /**
     * Displays a directory chooser and writes a path of the selected directory
     * into the target text field provided.
     * 
     * @param target
     * target text field, where the path goes
     */
    private void chooserTifDirectory(final JTextField target) {
        if (SimpleFrameUtils.chooseDirectory(target)) {
            // fill other empty fields if they are empty
            // value is based on the source directory
            
            if (target.equals(this.tifSourceDirLocation)) {
                final File parent = new File(target.getText()).getParentFile();
                
                this.tifTargetDirLocation.setText(new File(parent, "zaloha_png").getAbsolutePath());
                this.logDirLocation.setText(new File(parent, "zaloha_log").getAbsolutePath());
            }
        }
    }
}
