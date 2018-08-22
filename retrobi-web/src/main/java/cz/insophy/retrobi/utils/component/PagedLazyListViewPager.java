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

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import cz.insophy.retrobi.form.PageSkipForm;

/**
 * Pager panel for a paged list view.
 * 
 * @author Vojtěch Hordějčuk
 */
public class PagedLazyListViewPager extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * page skip form
     */
    private final PageSkipForm form;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param target
     * target paged list view
     */
    public PagedLazyListViewPager(final String id, final PagedLazyListView<?> target) {
        super(id);
        
        this.form = new PageSkipForm("form", target) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && target.isVisible();
            }
        };
        
        this.form.add(this.createFirstPageLink("link.first", target));
        this.form.add(this.createPreviousPageLink("link.previous", target));
        this.form.add(this.createNextPageLink("link.next", target));
        this.form.add(this.createLastPageLink("link.last", target));
        this.add(this.form);
    }
    
    @Override
    public boolean isVisible() {
        return this.form.isVisible();
    }
    
    /**
     * Creates a link that advances to the first page when clicked.
     * 
     * @param id
     * component ID
     * @param target
     * target paged list view
     * @return a link
     */
    private AbstractLink createFirstPageLink(final String id, final PagedLazyListView<?> target) {
        return new Link<Object>(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                target.setCurrentPage(0);
            }
            
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && !target.isOnFirstPage();
            }
        };
    }
    
    /**
     * Creates a link that advances to the last page when clicked.
     * 
     * @param id
     * component ID
     * @param target
     * target paged list view
     * @return a link
     */
    private AbstractLink createLastPageLink(final String id, final PagedLazyListView<?> target) {
        return new Link<Object>(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                target.setCurrentPage(target.getMaxPage());
            }
            
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && !target.isOnLastPage();
            }
        };
    }
    
    /**
     * Creates a link that advances to the previous page when clicked.
     * 
     * @param id
     * component ID
     * @param target
     * target paged list view
     * @return a link
     */
    private AbstractLink createPreviousPageLink(final String id, final PagedLazyListView<?> target) {
        return new Link<Object>(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                target.setCurrentPage(target.getCurrentPage() - 1);
            }
            
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && !target.isOnFirstPage();
            }
        };
    }
    
    /**
     * Creates a link that advances to the next page when clicked.
     * 
     * @param id
     * component ID
     * @param target
     * target paged list view
     * @return a link
     */
    private AbstractLink createNextPageLink(final String id, final PagedLazyListView<?> target) {
        return new Link<Object>(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                target.setCurrentPage(target.getCurrentPage() + 1);
            }
            
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && !target.isOnLastPage();
            }
        };
    }
}
