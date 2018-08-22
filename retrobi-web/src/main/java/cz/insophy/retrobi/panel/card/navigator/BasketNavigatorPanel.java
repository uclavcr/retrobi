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

package cz.insophy.retrobi.panel.card.navigator;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.form.BasketOperationForm;
import cz.insophy.retrobi.link.ClearBasketLink;
import cz.insophy.retrobi.link.SortBasketLink;
import cz.insophy.retrobi.pages.AbstractCardPage;
import cz.insophy.retrobi.utils.CardRange;

/**
 * Basket navigator panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasketNavigatorPanel extends AbstractCardNavigatorPanel {
    /**
     * basket panel mode
     */
    private static enum BasketPanelMode {
        /**
         * no panel is displayed
         */
        NONE,
        /**
         * download panel is displayed
         */
        DOWNLOAD,
        /**
         * save panel is displayed
         */
        SAVE;
    }
    
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * panel mode
     */
    private BasketPanelMode panelMode;
    /**
     * a mutable range model
     */
    private final IModel<CardRange> mutableRangeModel;
    /**
     * operation form
     */
    private BasketOperationForm operationForm;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param page
     * parent page
     * @param alwaysCompact
     * make this panel always compact
     * @param mutableRangeModel
     * mutable card range model
     */
    public BasketNavigatorPanel(final String id, final AbstractCardPage page, final boolean alwaysCompact, final IModel<CardRange> mutableRangeModel) {
        super(id, page, alwaysCompact);
        
        // initialize models
        
        this.panelMode = BasketPanelMode.NONE;
        
        this.mutableRangeModel = new IModel<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                return mutableRangeModel.getObject();
            }
            
            @Override
            public void setObject(final CardRange object) {
                mutableRangeModel.setObject(object);
                BasketNavigatorPanel.this.operationForm.updateByRange();
                BasketNavigatorPanel.this.requestCardViewerUpdate();
            }
            
            @Override
            public void detach() {
                // NOP
            }
        };
        
        // place components
        
        this.addOperation1Components();
        this.addOperation2Components();
        this.addDownloadPanel();
        this.addSavePanel();
        this.addNavigationLinks();
    }
    
    /**
     * Adds the operation components (first group).
     */
    private void addOperation1Components() {
        // initialize models
        
        final IModel<Integer> countModel = new AbstractReadOnlyModel<Integer>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Integer getObject() {
                return BasketNavigatorPanel.this.mutableRangeModel.getObject().getCount();
            }
        };
        
        // create components
        
        final WebMarkupContainer wrapper = this.createNonCompactWrapper("wrap.operation1", this.getViewSettings());
        final Component countLabel = new Label("label.count", countModel);
        this.operationForm = new BasketOperationForm("form.operation", this, this.mutableRangeModel);
        
        // place components
        
        wrapper.add(countLabel);
        wrapper.add(this.operationForm);
        this.add(wrapper);
    }
    
    /**
     * Adds the operation components (second group).
     */
    private void addOperation2Components() {
        // create components
        
        final WebMarkupContainer wrapper = this.createNonCompactWrapper("wrap.operation2", this.getViewSettings());
        
        final Component downloadLink = new Link<Object>("link.download") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                BasketNavigatorPanel.this.showPanel(BasketPanelMode.DOWNLOAD);
            }
        };
        
        final Component saveLink = new Link<Object>("link.save") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                BasketNavigatorPanel.this.showPanel(BasketPanelMode.SAVE);
            }
        };
        
        final Component hideLink = new Link<Object>("link.hide") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                BasketNavigatorPanel.this.hidePanels();
            }
        };
        
        final Component sortLink = new SortBasketLink("link.sort") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                
                // update after clicking
                
                BasketNavigatorPanel.this.requestCardViewerReset();
            }
        };
        
        final Component clearLink = new ClearBasketLink("link.clear") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                
                // update after clicking
                
                BasketNavigatorPanel.this.requestCardViewerReset();
            }
        };
        
        // place components
        
        wrapper.add(downloadLink);
        wrapper.add(saveLink);
        wrapper.add(hideLink);
        wrapper.add(sortLink);
        wrapper.add(clearLink);
        this.add(wrapper);
    }
    
    /**
     * Adds the download panel.
     */
    private void addDownloadPanel() {
        final Component panel = new BasketDownloadPanel("panel.download", this) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return BasketPanelMode.DOWNLOAD.equals(BasketNavigatorPanel.this.panelMode);
            }
        };
        
        this.add(panel);
    }
    
    /**
     * Adds the save panel.
     */
    private void addSavePanel() {
        final Component panel = new BasketSavePanel("panel.save", this) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return BasketPanelMode.SAVE.equals(BasketNavigatorPanel.this.panelMode);
            }
        };
        
        this.add(panel);
    }
    
    /**
     * Adds navigation links.
     */
    private void addNavigationLinks() {
        // create components
        
        final WebMarkupContainer wrapper = AbstractCardNavigatorPanel.createCardRangeWrapper("wrap.pager", this.mutableRangeModel);
        
        final AbstractLink linkFirst = AbstractCardNavigatorPanel.createFirstLink("link.first", this.mutableRangeModel);
        final AbstractLink linkLast = AbstractCardNavigatorPanel.createLastLink("link.last", this.mutableRangeModel);
        final AbstractLink linkLeft = AbstractCardNavigatorPanel.createLeftLink("link.left", this.mutableRangeModel);
        final AbstractLink linkRight = AbstractCardNavigatorPanel.createRightLink("link.right", this.mutableRangeModel);
        final AbstractLink linkUp = AbstractCardNavigatorPanel.createUpLink("link.up", this.mutableRangeModel);
        
        // place components
        
        linkFirst.add(new Label("label", linkFirst.getDefaultModel()));
        linkLast.add(new Label("label", linkLast.getDefaultModel()));
        linkLeft.add(new Label("label", linkLeft.getDefaultModel()));
        linkRight.add(new Label("label", linkRight.getDefaultModel()));
        linkUp.add(new Label("label", linkUp.getDefaultModel()));
        
        wrapper.add(linkFirst);
        wrapper.add(linkLast);
        wrapper.add(linkLeft);
        wrapper.add(linkRight);
        wrapper.add(linkUp);
        this.add(wrapper);
    }
    
    @Override
    public AbstractLink createDownLink(final String id, final int offset) {
        return AbstractCardNavigatorPanel.createDownLink(id, offset, this.mutableRangeModel);
    }
    
    /**
     * Shows the given panel.
     * 
     * @param newPanelMode
     * new panel to show
     */
    private void showPanel(final BasketPanelMode newPanelMode) {
        this.panelMode = newPanelMode;
    }
    
    /**
     * Hide the current panel.
     */
    private void hidePanels() {
        this.panelMode = BasketPanelMode.NONE;
    }
}
