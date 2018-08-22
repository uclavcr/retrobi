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

package cz.insophy.retrobitool.processor.table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.table.AbstractTableModel;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;
import cz.insophy.retrobitool.ProcessorFileMetaInfo;
import cz.insophy.retrobitool.processor.model.ProcessorModel;
import cz.insophy.retrobitool.processor.model.ProcessorModelListener;

/**
 * Table model containing files loaded by the processor. It has 5 columns:
 * batch, first card number, filename, last page number and temporary new name
 * of the file. Each row represents one file.
 */
public class FilesTableModel extends AbstractTableModel implements ProcessorModelListener {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * processor model
     */
    private final ProcessorModel model;
    
    /**
     * Creates a new instance.
     * 
     * @param model
     * processor model
     */
    public FilesTableModel(final ProcessorModel model) {
        super();
        this.model = model;
    }
    
    /**
     * Export all the file information to the CSV file specified.
     * 
     * @param outputFile
     * output file
     * @throws IOException
     * I/O exception
     */
    public void exportToCSV(final File outputFile) throws IOException {
        if (outputFile.exists()) {
            throw new IllegalStateException("Soubor pro zápis údajů o souborech již existuje: " + outputFile.getAbsolutePath());
        }
        
        FileWriter writer = null;
        
        try {
            // write the table contents into the CSV file
            
            writer = new FileWriter(outputFile);
            
            for (int i = -1; i < this.getRowCount(); i++) {
                if (i == -1) {
                    // write CSV header
                    
                    for (int j = 0; j < this.getColumnCount(); j++) {
                        writer.append(SimpleStringUtils.escapeForCSV(this.getColumnName(j)));
                        
                        if (j < this.getColumnCount() - 1) {
                            writer.append(Settings.CSV_COLUMN);
                        }
                    }
                    
                    writer.append(Settings.CSV_ROW);
                } else {
                    // write CSV values
                    
                    for (int j = 0; j < this.getColumnCount(); j++) {
                        writer.append(SimpleStringUtils.escapeForCSV(String.valueOf(this.getValueAt(i, j))));
                        
                        if (j < this.getColumnCount() - 1) {
                            writer.append(Settings.CSV_COLUMN);
                        }
                    }
                    
                    writer.append(Settings.CSV_ROW);
                }
            }
        } finally {
            if (writer != null) {
                // flush and close the writer
                
                writer.flush();
                writer.close();
            }
        }
    }
    
    @Override
    public int getRowCount() {
        return this.model.getFiles().size();
    }
    
    @Override
    public int getColumnCount() {
        return 6;
    }
    
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final File file = this.model.getFiles().get(rowIndex);
        final ProcessorFileMetaInfo fileinf = this.model.getFileMetaInfo(file);
        
        switch (columnIndex) {
            case 0:
                // file batch
                
                return fileinf.getBatch();
            case 1:
                // first card number
                
                return fileinf.getFirstNumber();
            case 2:
                // filename
                
                return file.getName();
            case 3:
                // last page number
                
                return fileinf.getLastPage();
            case 4:
                // temporary new name
                
                return fileinf.getTempNewName();
            case 5:
                // empty
                
                if (!fileinf.wasCheckedEmpty()) {
                    return "?";
                }
                
                if (fileinf.isEmpty()) {
                    return "ANO";
                } else if (fileinf.getPage() != fileinf.getLastPage()) {
                    return "ne (nesmí)";
                } else {
                    return "ne";
                }
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case 0:
                return "Dávka";
            case 1:
                return "Poslední lístek v záloze";
            case 2:
                return "Původní název";
            case 3:
                return "Stránek";
            case 4:
                return "Nový název";
            case 5:
                return "Prázdný";
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 2:
            case 4:
            case 5:
                return String.class;
            case 1:
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
    public void filesUpdated() {
        this.fireTableDataChanged();
    }
    
    @Override
    public void processFailed() {
        this.fireTableDataChanged();
    }
    
    @Override
    public void processFinished() {
        this.fireTableDataChanged();
    }
    
    @Override
    public void processStarted() {
        // nothing
    }
    
    @Override
    public void processStatusUpdated(final int total, final int done) {
        // nothing
    }
}
