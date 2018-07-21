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

import java.util.List;

import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;

/**
 * Paged version of a lazy list view.
 * 
 * @author Vojtěch Hordějčuk
 * @param <T>
 * type of an item in the list
 */
abstract public class PagedLazyListView<T> extends LazyListView<T> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * page number
     */
    private int page;
    /**
     * maximal page number
     */
    private int maxPage;
    /**
     * row number limit
     */
    private final int limit;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param limit
     * default row number limit
     */
    protected PagedLazyListView(final String id, final int limit) {
        super(id);
        
        this.page = 0;
        this.maxPage = 0;
        this.limit = limit;
    }
    
    /**
     * Resets the paged view.
     */
    public void reset() {
        this.reset(0);
    }
    
    /**
     * Updates the bounds.
     * 
     * @param pageCount
     * page count to be used
     */
    public void reset(final int pageCount) {
        this.maxPage = Math.max(0, pageCount - 1);
        this.page = SimpleGeneralUtils.limit(this.page, 0, this.maxPage);
        this.invalidateCache();
    }
    
    /**
     * Sets a new page number and invalidates cache.
     * 
     * @param newPage
     * new page to set
     */
    public void setCurrentPage(final int newPage) {
        this.page = SimpleGeneralUtils.limit(newPage, 0, this.maxPage);
        this.invalidateCache();
    }
    
    /**
     * Checks if the paged view is on the first page.
     * 
     * @return <code>true</code> if the paged view is on the first page,
     * <code>false</code> otherwise
     */
    public boolean isOnFirstPage() {
        return this.page == 0;
    }
    
    /**
     * Checks if the paged view is on the first page.
     * 
     * @return <code>true</code> if the paged view is on the first page,
     * <code>false</code> otherwise
     */
    public boolean isOnLastPage() {
        return this.page == this.maxPage;
    }
    
    /**
     * Returns the maximum page number.
     * 
     * @return the maximum page number
     */
    public int getMaxPage() {
        return this.maxPage;
    }
    
    /**
     * Returns the current page number.
     * 
     * @return the current page number
     */
    public int getCurrentPage() {
        return this.page;
    }
    
    @Override
    protected List<? extends T> getFreshList() {
        return this.getFreshList(this.page, this.limit);
    }
    
    /**
     * Returns a fresh list of items to be displayed in the list view.
     * 
     * @param currentPage
     * the current page (from 0 to N)
     * @param currentLimit
     * the current limit
     * @return a list of items
     */
    protected abstract List<? extends T> getFreshList(int currentPage, int currentLimit);
}
