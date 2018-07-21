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

package cz.insophy.retrobi.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.form.MessageListFilterForm;
import cz.insophy.retrobi.link.DeleteOldMessagesLink;
import cz.insophy.retrobi.link.DownloadOldMessageCSVLink;
import cz.insophy.retrobi.model.setup.AdminViewMode;
import cz.insophy.retrobi.panel.MessageListPanel;
import cz.insophy.retrobi.utils.component.OnClickConfirmer;
import cz.insophy.retrobi.utils.component.PagedLazyListViewPager;

/**
 * Page for managing messages.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AdminMessagesPage extends AbstractAdminPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public AdminMessagesPage(final PageParameters parameters) { // NO_UCD
        super(parameters, AdminViewMode.MESSAGES);
        
        // create components
        
        final MessageListPanel panel = new MessageListPanel("list.message");
        final Component pager = new PagedLazyListViewPager("panel.pager", panel.getPagedView());
        final Component form = new MessageListFilterForm("form.filter", panel);
        final Component limitLabel = new Label("label.limit", Model.of(Settings.OLD_MESSAGE_LIMIT));
        final Component downloadLink = new DownloadOldMessageCSVLink("link.download");
        final Component deleteLink = new DeleteOldMessagesLink("link.delete") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                
                // reset after old message delete
                
                panel.reset();
            }
        };
        
        // setup components
        
        deleteLink.add(new OnClickConfirmer("Opravdu chcete smazat stará potvrzená hlášení?"));
        
        // place components
        
        this.add(panel);
        this.add(pager);
        this.add(form);
        this.add(limitLabel);
        this.add(downloadLink);
        this.add(deleteLink);
    }
}
