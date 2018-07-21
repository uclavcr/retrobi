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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import cz.insophy.retrobitool.processor.model.ProcessorModel;
import cz.insophy.retrobitool.processor.model.ProcessorModelListener;
import cz.insophy.retrobitool.processor.table.FilesTableModel;

/**
 * Files panel. Contains a table with files. Each row shows one file, its source
 * sub directory and some useful information about the batch target and batch
 * backup directory status.
 * 
 * @author Vojtěch Hordějčuk
 */
public class FilesPanel extends JPanel implements ProcessorModelListener {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * main model
     */
    private final ProcessorModel model;
    /**
     * file table model
     */
    private final FilesTableModel tableModel;
    /**
     * file table
     */
    private final JTable table;
    /**
     * file table scroll
     */
    private final JScrollPane tableScroll;
    
    /**
     * Creates a new instance.
     * 
     * @param model
     * main model
     */
    protected FilesPanel(final ProcessorModel model) {
        super();
        
        // prepare models
        
        this.model = model;
        this.tableModel = new FilesTableModel(this.model);
        
        // create components
        
        this.table = new JTable(this.tableModel);
        this.tableScroll = new JScrollPane(this.table);
        
        // setup components
        
        this.setLayout(new BorderLayout());
        
        // place components
        
        this.add(this.tableScroll, BorderLayout.CENTER);
        
        // setup listeners
        
        this.tableModel.addTableModelListener(this.table);
        this.model.addListener(this.tableModel);
        this.model.addListener(this);
    }
    
    /**
     * Returns the table model.
     * 
     * @return the table model
     */
    public FilesTableModel getTableModel() {
        return this.tableModel;
    }
    
    // ======
    // EVENTS
    // ======
    
    @Override
    public void filesUpdated() {
        // NOP
    }
    
    @Override
    public void processStarted() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // select the first row
                
                if (FilesPanel.this.table.getRowCount() > 0)
                {
                    FilesPanel.this.table.setRowSelectionInterval(0, 0);
                    FilesPanel.this.table.scrollRectToVisible(FilesPanel.this.table.getCellRect(0, 0, true));
                }
            }
        });
    }
    
    @Override
    public void processStatusUpdated(final int total, final int done) {
        // GUI update must be run in the EDT
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // repaint table
                
                FilesPanel.this.table.repaint();
                
                // try to select the last done row and scroll
                
                if ((done >= 0) && (done < FilesPanel.this.table.getRowCount()))
                {
                    FilesPanel.this.table.setRowSelectionInterval(done, done);
                    FilesPanel.this.table.scrollRectToVisible(FilesPanel.this.table.getCellRect(done, 0, true));
                }
            }
        });
    }
    
    @Override
    public void processFinished() {
        // NOP
    }
    
    @Override
    public void processFailed() {
        // NOP
    }
}
