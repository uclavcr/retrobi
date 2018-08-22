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

import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Abstract base class for all lazy list views. Lazy list view is a standard
 * list view whose item list is cached. The cache can be invalidated. This
 * component was introduced, because the default list view from Wicket is
 * getting the list too often (8-times or more) during the rendering and this
 * can be expensive (e.g. when loading from the database).
 * 
 * @author Vojtěch Hordějčuk
 * @param <T>
 * type of an item in the list
 */
abstract public class LazyListView<T> extends ListView<T> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * item list cache
     */
    private List<? extends T> cache;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    protected LazyListView(final String id) {
        super(id);
        
        // initialize cache
        
        this.cache = null;
        
        // initialize model
        
        super.setModel(new AbstractReadOnlyModel<List<T>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<T> getObject() {
                if (LazyListView.this.cache == null) {
                    LazyListView.this.cache = LazyListView.this.getFreshList();
                }
                
                return Collections.unmodifiableList(LazyListView.this.cache);
            }
        });
    }
    
    /**
     * Returns a fresh list of items to be displayed in the list view.
     * 
     * @return a list of items
     */
    protected abstract List<? extends T> getFreshList();
    
    /**
     * Invalidates the cache so the list is refreshed next time during the
     * rendering.
     */
    public void invalidateCache() {
        this.cache = null;
    }
    
    /**
     * Checks if the list is empty.
     * 
     * @return <code>true</code> if the list is empty or <code>null</code>,
     * <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return ((this.getModel() == null) || (this.getModelObject() == null) || this.getModelObject().isEmpty());
    }
}
