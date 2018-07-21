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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import cz.insophy.retrobitool.importer.model.ImporterModel;
import cz.insophy.retrobitool.importer.table.CardListTableModel;

/**
 * Card panel contains a table with all cards and their properties. Properties
 * displayed in the header are based on the property list specified in the batch
 * panel. The table is not editable. It serves as a check reference only.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardPanel extends JPanel {
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
    private final CardListTableModel tableModel;
    /**
     * cards table
     */
    private final JTable table;
    /**
     * table scroll
     */
    private final JScrollPane tableScroll;
    
    /**
     * Creates a new instance.
     * 
     * @param model
     * main model
     */
    protected CardPanel(final ImporterModel model) {
        super();
        
        // prepare models
        
        this.model = model;
        this.tableModel = new CardListTableModel(this.model);
        
        // create components
        
        this.table = new JTable(this.tableModel);
        this.tableScroll = new JScrollPane(this.table);
        
        // setup components
        
        this.setLayout(new BorderLayout());
        
        // place components
        
        this.add(new JLabel("<html><big>Přehled lístků</big><br>Přehled všech lístků a jejich vlastností. Můžete se vrátit na předchozí panel a vlastnosti upravit.<br>Tyto úpravy se projeví ihned i zde.</html>"), BorderLayout.NORTH);
        this.add(this.tableScroll, BorderLayout.CENTER);
        
        // setup listeners
        
        this.model.addListener(this.tableModel);
        this.tableModel.addTableModelListener(this.table);
    }
}
