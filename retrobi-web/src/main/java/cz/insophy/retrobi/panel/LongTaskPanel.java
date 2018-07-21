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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.model.task.LongTask;
import cz.insophy.retrobi.pages.AbstractBasicPage;

/**
 * Long task panel that shows status of the long task queue.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LongTaskPanel extends Panel {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(LongTaskPanel.class);
    /**
     * component update interval
     */
    private static final Duration UPDATE_INTERVAL = Duration.milliseconds(Settings.TASK_QUEUE_UPDATE);
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * the last task manager version which was known before the page rendering
     */
    private long lastTaskManagerVersion;
    /**
     * errors that were caused by the tasks
     */
    private final Set<Exception> errors;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public LongTaskPanel(final String id) {
        super(id);
        
        // reset the last task manager version
        
        this.lastTaskManagerVersion = -1;
        
        // create models
        
        this.errors = new LinkedHashSet<Exception>();
        
        final IModel<LongTask> activeTask = new AbstractReadOnlyModel<LongTask>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public LongTask getObject() {
                return RetrobiWebSession.get().getActiveTask();
            }
        };
        
        final IModel<String> statusModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (activeTask.getObject() == null) {
                    return "Neprobíhá žádná úloha.";
                }
                
                return activeTask.getObject().getStatus();
            }
        };
        
        final AjaxSelfUpdatingTimerBehavior updater = new AjaxSelfUpdatingTimerBehavior(LongTaskPanel.UPDATE_INTERVAL) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onPostProcessTarget(final AjaxRequestTarget target) {
                // stop on error
                
                if (LongTaskPanel.this.getPage().isErrorPage()) {
                    LongTaskPanel.LOG.debug("Stopping AJAX update after error.");
                    this.stop();
                    return;
                }
                
                // gather all task errors (if any)
                
                this.gatherErrorsFromActiveTask();
                
                // reload page if needed
                
                this.reloadPageIfNeeded();
            }
            
            /**
             * Reloads the current page if a task queue version differs.
             */
            private void reloadPageIfNeeded() {
                // get the current task queue version
                
                final long currentTaskQueueVersion = RetrobiWebSession.get().getTaskQueueVersion();
                
                if (currentTaskQueueVersion != LongTaskPanel.this.lastTaskManagerVersion) {
                    // inform user about the completion
                    
                    RetrobiWebSession.get().info("Dlouhá úloha byla dokončena.");
                    
                    // show gathered error messages and clear them afterwards
                    
                    for (final Exception x : LongTaskPanel.this.errors) {
                        RetrobiWebSession.get().error(x.getMessage());
                    }
                    
                    LongTaskPanel.this.errors.clear();
                    
                    // update task queue version
                    
                    LongTaskPanel.this.lastTaskManagerVersion = currentTaskQueueVersion;
                    
                    // reload the page
                    
                    LongTaskPanel.LOG.debug("Tasks were just finished, reloading.");
                    
                    final Page parentPage = LongTaskPanel.this.getPage();
                    
                    if (parentPage instanceof AbstractBasicPage) {
                        ((AbstractBasicPage) parentPage).onLongTaskFinished();
                    }
                    
                    LongTaskPanel.this.setRedirect(true);
                    throw new RestartResponseException(LongTaskPanel.this.getPage());
                }
            }
            
            /**
             * Gathers all errors from the current task (if any).
             */
            private void gatherErrorsFromActiveTask() {
                if (activeTask.getObject() != null) {
                    for (final Exception x : activeTask.getObject().getErrors()) {
                        LongTaskPanel.LOG.error(x.getMessage(), x);
                        LongTaskPanel.this.errors.add(x);
                    }
                }
            }
        };
        
        // create components
        
        final WebMarkupContainer wrapper = new WebMarkupContainer("task");
        
        final WebMarkupContainer image = new WebMarkupContainer("image") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (activeTask.getObject() == null) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final Label nameLabel = new Label("label.name", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (activeTask.getObject() == null) {
                    return "(žádná úloha)";
                }
                
                return activeTask.getObject().getName();
            }
        });
        
        final Label statusLabel = new Label("label.status", statusModel) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return (activeTask.getObject() != null) && super.isVisible();
            }
        };
        
        final Component refreshLink = new Link<Object>("link.refresh") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                throw new RestartResponseException(LongTaskPanel.this.getPage());
            }
        };
        
        final Component stopActiveLink = new Link<Object>("link.stop_active") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                if (RetrobiWebSession.get().stopActiveTask()) {
                    this.info("Aktivní úloha bude předčasně ukončena.");
                }
            }
        };
        
        // setup components
        
        this.setOutputMarkupId(true);
        
        // place components
        
        wrapper.add(image);
        wrapper.add(nameLabel);
        wrapper.add(statusLabel);
        wrapper.add(refreshLink);
        wrapper.add(stopActiveLink);
        this.add(updater);
        this.add(wrapper);
    }
    
    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        
        // load the last task manager version
        
        this.lastTaskManagerVersion = RetrobiWebSession.get().getTaskQueueVersion();
    }
}
