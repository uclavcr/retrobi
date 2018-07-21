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

package cz.insophy.retrobi.panel;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.link.BookmarkableAboutLink;
import cz.insophy.retrobi.link.BookmarkableBasketLink;
import cz.insophy.retrobi.link.BookmarkableCatalogLink;
import cz.insophy.retrobi.link.BookmarkableHelpLink;
import cz.insophy.retrobi.link.BookmarkableSearchLink;
import cz.insophy.retrobi.link.BookmarkableStatsLink;
import cz.insophy.retrobi.model.setup.AdminViewMode;

/**
 * Sidebar panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SidebarPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * basket size label
     */
    private final Component basketSizeLabel;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public SidebarPanel(final String id) {
        super(id);
        
        // create models
        
        final IModel<Integer> basketSizeModel = new AbstractReadOnlyModel<Integer>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Integer getObject() {
                return RetrobiWebSession.get().getCardContainer().getBasketSize();
            }
        };
        
        // create components
        
        this.basketSizeLabel = new Label("label.basket", basketSizeModel);
        
        final Component longTaskPanel = new LongTaskPanel("taskPanel") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final FooterPanel footerPanel = new FooterPanel("footerPanel");
        
        final AbstractLink catalogLink = new BookmarkableCatalogLink("link.catalog");
        final AbstractLink searchLink = new BookmarkableSearchLink("link.search");
        final AbstractLink basketLink = new BookmarkableBasketLink("link.basket");
        final AbstractLink statsLink = new BookmarkableStatsLink("link.stats");
        final AbstractLink helpLink = new BookmarkableHelpLink("link.help");
        final AbstractLink aboutLink = new BookmarkableAboutLink("link.about");
        
        final AbstractLink adminLink = new Link<Object>("link.admin") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
                    return false;
                }
                
                return super.isVisible();
            }
            
            @Override
            public void onClick() {
                final List<AdminViewMode> modes = AdminViewMode.valuesForRole(RetrobiWebSession.get().getUserRole());
                
                if (modes.size() > 0) {
                    // redirect to the default page for this role
                    
                    final BookmarkablePageLink<?> link = modes.get(0).createLink("TEMP");
                    this.setRedirect(true);
                    this.setResponsePage(link.getPageClass(), link.getPageParameters());
                }
            }
        };
        
        // setup components
        
        this.basketSizeLabel.setOutputMarkupId(true);
        
        // place components
        
        this.add(this.basketSizeLabel);
        this.add(longTaskPanel);
        this.add(footerPanel);
        this.add(adminLink);
        this.add(catalogLink);
        this.add(searchLink);
        this.add(basketLink);
        this.add(statsLink);
        this.add(helpLink);
        this.add(aboutLink);
    }
    
    /**
     * Returns the basket size label.
     * 
     * @return the basket size label
     */
    public Component getBasketSizeLabel() {
        return this.basketSizeLabel;
    }
}
