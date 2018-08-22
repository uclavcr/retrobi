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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.AbstractReadOnlyModel;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.type.AbstractCardIndex;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.link.ReloadAttributesLink;
import cz.insophy.retrobi.link.ReloadCacheLink;
import cz.insophy.retrobi.link.ReloadIndexesLink;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.model.setup.AdminViewMode;
import cz.insophy.retrobi.utils.component.GridListView;
import cz.insophy.retrobi.utils.resource.JnlpResource;

/**
 * Page with administrative tools download.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AdminToolsPage extends AbstractAdminPage {
    /**
     * CSV log files
     */
    private List<File> csvFiles;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public AdminToolsPage(final PageParameters parameters) { // NO_UCD
        super(parameters, AdminViewMode.TOOLS);
    }
    
    @Override
    protected void initComponentModels(final PageParameters parameters) {
        super.initComponentModels(parameters);
        
        // load CSV log files
        
        final File[] csvFilesTemp = Settings.CSV_LOG_DIRECTORY.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".csv");
            }
        });
        
        this.csvFiles = (csvFilesTemp == null)
                ? Collections.<File> emptyList()
                : Arrays.asList(csvFilesTemp);
        
        Collections.sort(this.csvFiles);
        Collections.reverse(this.csvFiles);
    }
    
    @Override
    protected void initComponents(final PageParameters parameters) {
        super.initComponents(parameters);
        
        // create resource links
        
        final Component linkTool1 = new ResourceLink<JnlpResource>(
                "link.processor",
                new ResourceReference("processor.jnlp"));
        
        final Component linkTool2 = new ResourceLink<JnlpResource>(
                "link.importer",
                new ResourceReference("importer.jnlp"));
        
        // create administrative links
        
        final Component reloadCacheLink = new ReloadCacheLink("link.reload.cache");
        final Component reloadAttributesLink = new ReloadAttributesLink("link.reload.attributes");
        final Component reloadIndexesLink = new ReloadIndexesLink("link.reload.indexes");
        
        final Component cleanIndexesLink = new Link<Object>("link.clean.index") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                RetrobiApplication.db().getCardSearchRepository().cleanupIndex();
                this.info("Index - čištění zahájeno.");
            }
        };
        
        final Component optimizeIndexesLink = new Link<Object>("link.optimize.index") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                for (final AbstractCardIndex index : RetrobiWebConfiguration.getInstance().getIndexes()) {
                    RetrobiApplication.db().getCardSearchRepository().optimize(index);
                    this.info("Index - optimalizace: " + index.getTitle());
                }
            }
        };
        
        final Component pingIndexesLink = new Link<Object>("link.ping.index") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                for (final AbstractCardIndex index : RetrobiWebConfiguration.getInstance().getIndexes()) {
                    try {
                        RetrobiApplication.db().getCardSearchRepository().ping(index);
                        this.info("Index - PING: " + index.getTitle());
                    } catch (final GeneralRepositoryException x) {
                        this.error(x.getMessage());
                    }
                }
            }
        };
        
        final Component pingViewsLink = new Link<Object>("link.ping.view") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                RetrobiApplication.db().pingAllViews();
                this.info("PING na všechny pohledy byl dokončen.");
            }
        };
        
        // create list of CSV logs
        
        final Component logList = new GridListView<File>("list.csv", "row", "col", this.csvFiles, 5) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateCell(final ListItem<File> item) {
                final WebMarkupContainer link = new DownloadLink("link", item.getModelObject());
                link.add(new Label("label", item.getModelObject().getName()));
                item.add(link);
            }
            
            @Override
            protected void populateEmptyCell(final ListItem<File> item) {
                final WebMarkupContainer link = new WebMarkupContainer("link");
                link.add(new Label("label"));
                item.add(link);
            }
        };
        
        // create information labels
        
        final Label dirLabel = new Label("label.dir", Settings.CSV_LOG_DIRECTORY.getAbsolutePath());
        
        final Component maintenanceStartLabel = new Label("label.maintenance_start", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                final long time = RetrobiWebApplication.getLastMaintenanceStart();
                return time < 1 ? "(neproběhla)" : Settings.DATE_FORMAT.format(new Date(time));
            }
        });
        
        final Component maintenanceEndLabel = new Label("label.maintenance_end", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                final long time = RetrobiWebApplication.getLastMaintenanceEnd();
                return time < 1 ? "(neproběhla)" : Settings.DATE_FORMAT.format(new Date(time));
            }
        });
        
        final Component maintenanceAliveLabel = new Label("label.maintenance_next", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                final long time = RetrobiWebApplication.getNextMaintenanceStart();
                return time < 1 ? "(není)" : Settings.DATE_FORMAT.format(new Date(time));
            }
        });
        
        final Component memHeapLabel = new Label("label.mem.heap", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return String.format("%.2f MiB", Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0));
            }
        });
        
        final Component memTotalLabel = new Label("label.mem.total", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return String.format("%.2f MiB", Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0));
            }
        });
        
        final Component memFreeLabel = new Label("label.mem.free", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return String.format("%.2f MiB", Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0));
            }
        });
        
        // place components
        
        this.add(linkTool1);
        this.add(linkTool2);
        this.add(reloadCacheLink);
        this.add(reloadAttributesLink);
        this.add(reloadIndexesLink);
        this.add(cleanIndexesLink);
        this.add(optimizeIndexesLink);
        this.add(pingIndexesLink);
        this.add(pingViewsLink);
        this.add(dirLabel);
        this.add(logList);
        this.add(maintenanceStartLabel);
        this.add(maintenanceEndLabel);
        this.add(maintenanceAliveLabel);
        this.add(memFreeLabel);
        this.add(memTotalLabel);
        this.add(memHeapLabel);
    }
}
