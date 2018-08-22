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

package cz.insophy.retrobitool.importer.table;

import java.io.File;

import javax.swing.table.AbstractTableModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobitool.importer.model.ImporterModel;
import cz.insophy.retrobitool.importer.model.ImporterModelListener;

/**
 * Card list table model. Shows all cards in the model with their properties.
 * Number of columns equals to the number of property values. Some of these
 * properties are system generated (catalog, batch, etc.). Each row represents
 * one card.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardListTableModel extends AbstractTableModel implements ImporterModelListener {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * importer model
     */
    private final ImporterModel model;
    
    /**
     * Creates a new instance.
     * 
     * @param model
     * importer model
     */
    public CardListTableModel(final ImporterModel model) {
        super();
        this.model = model;
    }
    
    @Override
    public int getRowCount() {
        return this.model.getCards().size();
    }
    
    @Override
    public int getColumnCount() {
        return 7;
    }
    
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final Card card = this.model.getCards().get(rowIndex);
        
        switch (columnIndex) {
            case 0:
                // card catalog
                
                return card.getCatalog();
            case 1:
                // card batch
                
                return card.getBatch();
            case 2:
                // card batch for sort
                
                return card.getBatchForSort();
            case 3:
                // card number
                
                return card.getNumberInBatch();
            case 4:
                // OCR
                
                return card.getOcr();
            case 5:
                // drawer
                
                return card.getDrawer();
            case 6:
                // files
                
                return card.getFiles().size();
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case 0:
                return "Katalog";
            case 1:
                return "Skupina";
            case 2:
                return "Řazení";
            case 3:
                return "Pořadí";
            case 4:
                return "OCR";
            case 5:
                return "Šuplík";
            case 6:
                return "Soubory";
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
            case 2:
            case 4:
            case 5:
            case 6:
                return String.class;
            case 3:
                return Integer.class;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }
    
    @Override
    public void filesChanged() {
        this.fireTableStructureChanged();
        this.fireTableDataChanged();
    }
    
    @Override
    public void cardsChanged() {
        this.fireTableStructureChanged();
        this.fireTableDataChanged();
    }
    
    @Override
    public void uploadStarted() {
        // nothing
    }
    
    @Override
    public void uploadFinished() {
        // nothing
    }
    
    @Override
    public void uploadStatusChanged(final String message, final Card card, final File file) {
        // nothing
    }
    
    @Override
    public void uploadStatusChanged(final String message, final Card card, final File file, final int done, final int total) {
        // nothing
    }
}
