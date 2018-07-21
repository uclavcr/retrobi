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

package cz.insophy.retrobi.utils.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

/**
 * Grid list view. Shows the given list as a grid (table) with N columns.
 * 
 * @author Vojtěch Hordějčuk
 * @param <T>
 * cell contents type
 */
public abstract class GridListView<T> extends WebMarkupContainer {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param rowInnerId
     * row inner ID
     * @param colInnerId
     * column inner ID
     * @param list
     * list to be shown
     * @param cols
     * number of columns
     */
    protected GridListView(final String id, final String rowInnerId, final String colInnerId, final List<? extends T> list, final int cols) {
        super(id);
        
        // initialize models
        
        final String cssEmptyClass = String.format("col%d colempty", cols);
        final String cssNonEmptyClass = String.format("col%d", cols);
        
        final int rows = GridListView.getRowCount(list, cols);
        
        final List<List<T>> data = new ArrayList<List<T>>(rows);
        
        for (int r = 0; r < rows; r++) {
            data.add(new ArrayList<T>(cols));
            
            for (int c = 0; c < cols; c++) {
                data.get(r).add(GridListView.getValue(list, r, c, cols));
            }
        }
        
        // create components
        
        final ListView<List<T>> rowl = new ListView<List<T>>(rowInnerId, data) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<List<T>> item1) {
                final ListView<T> coll = new ListView<T>(colInnerId, item1.getModelObject()) {
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    protected void populateItem(final ListItem<T> item2) {
                        item2.add(new ClassSwitcher(cssNonEmptyClass, cssEmptyClass, item2.getModelObject() == null));
                        
                        if (item2.getModelObject() == null) {
                            GridListView.this.populateEmptyCell(item2);
                        } else {
                            GridListView.this.populateCell(item2);
                        }
                    }
                };
                
                item1.add(coll);
            }
        };
        
        // place components
        
        this.add(rowl);
    }
    
    /**
     * Computes the row count.
     * 
     * @param list
     * list to be shown
     * @param cols
     * number of columns
     * @return the row count
     */
    private static int getRowCount(final List<?> list, final int cols) {
        return (int) Math.ceil((double) list.size() / (double) cols);
    }
    
    /**
     * Returns the value on the given X, Y index or <code>null</code> if X or Y
     * is out of range. Basically a 2D to 1D mapping function.
     * 
     * @param <E>
     * item type
     * @param list
     * source list
     * @param row
     * row index (from 0)
     * @param col
     * column index (from 0)
     * @param cols
     * number of columns
     * @return value or <code>null</code> if out of range
     */
    private static <E> E getValue(final List<E> list, final int row, final int col, final int cols) {
        final int index = row * cols + col;
        
        if ((index < 0) || (index > list.size() - 1)) {
            return null;
        }
        
        return list.get(index);
    }
    
    /**
     * Populates the non-empty cell.
     * 
     * @param item
     * item model
     */
    abstract protected void populateCell(ListItem<T> item);
    
    /**
     * Populates the empty cell.
     * 
     * @param item
     * item model
     */
    abstract protected void populateEmptyCell(ListItem<T> item);
}
