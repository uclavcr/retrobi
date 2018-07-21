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
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.table.AbstractTableModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobitool.ImporterFileMetaInfo;
import cz.insophy.retrobitool.importer.model.ImporterModel;
import cz.insophy.retrobitool.importer.model.ImporterModelListener;

/**
 * Table model containing files and preview of cards created based on them. It
 * has 5 columns: batch, card number, page, filename and OCR length. Each row
 * represents one file.
 * 
 * @author Vojtěch Hordějčuk
 */
public class FileListTableModel extends AbstractTableModel implements ImporterModelListener {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * importer model
     */
    private final ImporterModel model;
    /**
     * flag of sorting by OCR (files without OCR on top)
     */
    private boolean sortByOcr;
    /**
     * cache array for files (for OCR sorting purposes)
     */
    private Object[] filesCache;
    
    /**
     * Creates a new instance.
     * 
     * @param model
     * importer model
     */
    public FileListTableModel(final ImporterModel model) {
        super();
        this.model = model;
        this.sortByOcr = false;
        this.filesCache = null;
    }
    
    @Override
    public int getRowCount() {
        return this.model.getFiles().size();
    }
    
    @Override
    public int getColumnCount() {
        return 7;
    }
    
    /**
     * Switches the sorting mode.
     * 
     * @param byOcr
     * <code>true</code> means enable sorting by OCR, <code>false</code> means
     * default sorting
     */
    public void switchSortMode(final boolean byOcr) {
        this.sortByOcr = byOcr;
        this.filesChanged();
    }
    
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final File file = this.getFile(rowIndex);
        final ImporterFileMetaInfo info = this.model.getFileMetaInfo(file);
        
        switch (columnIndex) {
            case 0:
                // drawer
                
                return info.getDrawer();
            case 1:
                // catalog
                
                return info.getCatalog().toString();
            case 2:
                // batch
                
                return info.getBatch();
            case 3:
                // card number
                
                return info.getNumber();
            case 4:
                // card page
                
                return info.getPage();
            case 5:
                // filename
                
                return file.getName();
            case 6:
                // OCR length
                
                return info.getOcr().length() > 0;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case 0:
                return "Šuplík";
            case 1:
                return "Část katalogu";
            case 2:
                return "Dávka";
            case 3:
                return "Pořadí lístku v dávce";
            case 4:
                return "Stránka";
            case 5:
                return "Soubor";
            case 6:
                return "OCR načteno";
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
            case 5:
                return String.class;
            case 3:
            case 4:
                return Integer.class;
            case 6:
                return Boolean.class;
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
        this.filesCache = null;
        this.fireTableDataChanged();
    }
    
    @Override
    public void cardsChanged() {
        // nothing
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
    
    /**
     * Helper method for OCR sorting. It returns a file on the given index and
     * it takes the file from an ordered cache.
     * 
     * @param rowIndex
     * index of the row
     * @return file on the given index (in cache)
     */
    private File getFile(final int rowIndex) {
        if (this.filesCache == null) {
            // it is necessary to fill the file cache
            
            this.filesCache = this.model.getFiles().toArray();
            
            if (this.sortByOcr) {
                // sort files by OCR
                
                Arrays.sort(this.filesCache, new Comparator<Object>() {
                    @Override
                    public int compare(final Object o1, final Object o2) {
                        // get both files
                        
                        final File f1 = (File) o1;
                        final File f2 = (File) o2;
                        
                        // get file meta information
                        
                        final ImporterFileMetaInfo i1 = FileListTableModel.this.model.getFileMetaInfo(f1);
                        final ImporterFileMetaInfo i2 = FileListTableModel.this.model.getFileMetaInfo(f2);
                        
                        // compare by OCR length
                        
                        final int c = i1.getOcr().length() - i2.getOcr().length();
                        
                        if (c != 0)
                        {
                            return c;
                        }
                        
                        // sorting is stable, the rest will stay as is
                        
                        return 0;
                    }
                });
            }
        }
        
        return (File) this.filesCache[rowIndex];
    }
}
