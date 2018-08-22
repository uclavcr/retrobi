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

import java.awt.BorderLayout;

import javax.swing.JFrame;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobitool.processor.model.ProcessorModel;

/**
 * Image processor main frame.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ProcessorMainFrame extends JFrame { // NO_UCD
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
     */
    public static void main(final String[] args) {
        Settings.setupLookAndFeel();
        
        String sourceDir = "";
        String targetDir = "";
        
        if (args.length == 2) {
            sourceDir = args[0];
            targetDir = args[1];
        }
        
        new ProcessorMainFrame(sourceDir, targetDir).setVisible(true);
    }
    
    /**
     * Creates a new instance with default filenames.
     * 
     * @param sourceDir
     * default source directory (or "")
     * @param targetDir
     * default target directory (or "")
     */
    public ProcessorMainFrame(final String sourceDir, final String targetDir) {
        super("Nástroj pro konverzi obrázků");
        
        // prepare model
        
        final ProcessorModel model = new ProcessorModel();
        
        // create components
        
        final FilesPanel filesPanel = new FilesPanel(model);
        final ProcessPanel processPanel = new ProcessPanel(model, sourceDir, targetDir, filesPanel.getTableModel());
        
        // place components
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(filesPanel, BorderLayout.CENTER);
        this.add(processPanel, BorderLayout.SOUTH);
        this.pack();
        this.setSize(800, 500);
        this.setLocationRelativeTo(null);
    }
}
