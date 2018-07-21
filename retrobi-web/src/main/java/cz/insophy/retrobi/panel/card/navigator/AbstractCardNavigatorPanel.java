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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.form.ImageViewModeForm;
import cz.insophy.retrobi.link.BookmarkableHelpLink;
import cz.insophy.retrobi.link.SetCompactModeLink;
import cz.insophy.retrobi.model.setup.CardSearchSettings;
import cz.insophy.retrobi.model.setup.CardViewSettings;
import cz.insophy.retrobi.pages.AbstractCardPage;
import cz.insophy.retrobi.utils.CardCatalogRange;
import cz.insophy.retrobi.utils.CardRange;

/**
 * Abstract card navigator panel. Base class for all navigator panels.
 * 
 * @author Vojtěch Hordějčuk
 */
abstract public class AbstractCardNavigatorPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent page
     */
    private final AbstractCardPage page;
    /**
     * the panel is always compact
     */
    private final boolean alwaysCompact;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param page
     * parent page
     * @param alwaysCompact
     * make this panel always compact
     */
    protected AbstractCardNavigatorPanel(final String id, final AbstractCardPage page, final boolean alwaysCompact) {
        super(id);
        
        // prepare models
        
        this.page = page;
        this.alwaysCompact = alwaysCompact;
        
        // create components
        
        final Component headerComponent = this.createHeaderComponents("wrap.header");
        final Component settingsComponent = this.createSettingsPanel("wrap.settings");
        
        final WebMarkupContainer wrapper = new WebMarkupContainer("wrap.table") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && (headerComponent.isVisible() || settingsComponent.isVisible());
            }
        };
        
        // place components
        
        wrapper.add(headerComponent);
        wrapper.add(settingsComponent);
        this.add(wrapper);
    }
    
    /**
     * Creates a wrapper for header components.
     * 
     * @param id
     * component ID
     * @return wrapper component
     */
    private WebMarkupContainer createHeaderComponents(final String id) {
        // create components
        
        final WebMarkupContainer wrapper = this.createNonCompactWrapperOnlyAlways(id, this.getViewSettings());
        final Component nameLabel = new Label("label.name", this.page.getPageName());
        final Component titleLabel = new Label("label.title", this.page.getPageTitleModel());
        final WebMarkupContainer headLink = this.page.getPageLink("link.head");
        final WebMarkupContainer helpLink = new BookmarkableHelpLink("link.help", this.page.getHelpTextType());
        
        final Component showLink = new SetCompactModeLink("link.show", false) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && AbstractCardNavigatorPanel.this.getViewSettings().isCompactModeEnabled() && !AbstractCardNavigatorPanel.this.alwaysCompact;
            }
        };
        
        final Component hideLink = new SetCompactModeLink("link.hide", true) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && !showLink.isVisible() && !AbstractCardNavigatorPanel.this.alwaysCompact;
            }
        };
        
        // place components
        
        headLink.add(nameLabel);
        wrapper.add(headLink);
        wrapper.add(helpLink);
        wrapper.add(titleLabel);
        wrapper.add(showLink);
        wrapper.add(hideLink);
        return wrapper;
    }
    
    /**
     * Creates a wrapper for settings components.
     * 
     * @param id
     * component ID
     * @return wrapper component
     */
    private WebMarkupContainer createSettingsPanel(final String id) {
        final WebMarkupContainer wrapper = this.createNonCompactWrapper(id, this.getViewSettings());
        final WebMarkupContainer settingsForm = new ImageViewModeForm("form.settings", this);
        
        wrapper.add(settingsForm);
        return wrapper;
    }
    
    // ==========
    // OPERATIONS
    // ==========
    
    /**
     * Requests card view update.
     */
    public void requestCardViewerUpdate() {
        this.page.updateViewer();
    }
    
    /**
     * Requests card view reset.
     */
    public void requestCardViewerReset() {
        this.page.resetViewer();
        this.page.updateViewer();
    }
    
    // =======
    // QUERIES
    // =======
    
    /**
     * Returns the view settings.
     * 
     * @return view settings
     */
    public CardViewSettings getViewSettings() {
        return this.page;
    }
    
    /**
     * Returns the search settings.
     * 
     * @return search settings
     */
    public CardSearchSettings getSearchSettings() {
        return this.page;
    }
    
    // ==============
    // LINK FACTORIES
    // ==============
    
    /**
     * Creates the "first" link.
     * 
     * @param id
     * component ID
     * @param mutableRangeModel
     * target range model
     * @return a link
     */
    protected static AbstractLink createFirstLink(final String id, final IModel<CardRange> mutableRangeModel) {
        return AbstractCardNavigatorPanel.createRangeLink(id, new Model<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                final CardRange oldRange = mutableRangeModel.getObject();
                
                if (oldRange.isOnFirst() || !oldRange.hasMoreCards()) {
                    return null;
                }
                
                return oldRange.createFirst();
            }
            
            @Override
            public void setObject(final CardRange object) {
                mutableRangeModel.setObject(object);
            }
        });
    }
    
    /**
     * Creates the "last" link.
     * 
     * @param id
     * component ID
     * @param mutableRangeModel
     * target range model
     * @return a link
     */
    protected static AbstractLink createLastLink(final String id, final IModel<CardRange> mutableRangeModel) {
        return AbstractCardNavigatorPanel.createRangeLink(id, new Model<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                final CardRange oldRange = mutableRangeModel.getObject();
                
                if (oldRange.isOnLast() || !oldRange.hasMoreCards()) {
                    return null;
                }
                
                return oldRange.createLast();
            }
            
            @Override
            public void setObject(final CardRange object) {
                mutableRangeModel.setObject(object);
            }
        });
    }
    
    /**
     * Creates the "left" link.
     * 
     * @param id
     * component ID
     * @param mutableRangeModel
     * target range model
     * @return a link
     */
    protected static AbstractLink createLeftLink(final String id, final IModel<CardRange> mutableRangeModel) {
        return AbstractCardNavigatorPanel.createRangeLink(id, new Model<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                final CardRange oldRange = mutableRangeModel.getObject();
                
                if (!oldRange.hasPrevious()) {
                    return null;
                }
                
                return oldRange.createPrevious();
            }
            
            @Override
            public void setObject(final CardRange object) {
                mutableRangeModel.setObject(object);
            }
        });
    }
    
    /**
     * Creates the "right" link.
     * 
     * @param id
     * component ID
     * @param mutableRangeModel
     * target range model
     * @return a link
     */
    protected static AbstractLink createRightLink(final String id, final IModel<CardRange> mutableRangeModel) {
        return AbstractCardNavigatorPanel.createRangeLink(id, new Model<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                final CardRange oldRange = mutableRangeModel.getObject();
                
                if (!oldRange.hasNext()) {
                    return null;
                }
                
                return oldRange.createNext();
            }
            
            @Override
            public void setObject(final CardRange object) {
                mutableRangeModel.setObject(object);
            }
        });
    }
    
    /**
     * Creates the "up" link.
     * 
     * @param id
     * component ID
     * @param mutableRangeModel
     * target range model
     * @return a link
     */
    protected static AbstractLink createUpLink(final String id, final IModel<CardRange> mutableRangeModel) {
        return AbstractCardNavigatorPanel.createRangeLink(id, new Model<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                final CardRange oldRange = mutableRangeModel.getObject();
                
                if (!oldRange.hasUpper()) {
                    return null;
                }
                
                return oldRange.createUpper();
            }
            
            @Override
            public void setObject(final CardRange object) {
                mutableRangeModel.setObject(object);
            }
        });
    }
    
    /**
     * Creates the "down" link.
     * 
     * @param id
     * component ID
     * @param offset
     * a new offset (row index)
     * @param mutableRangeModel
     * target range model
     * @return a link
     */
    protected static AbstractLink createDownLink(final String id, final int offset, final IModel<CardRange> mutableRangeModel) {
        return AbstractCardNavigatorPanel.createRangeLink(id, new Model<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                final CardRange oldRange = mutableRangeModel.getObject();
                
                if (!oldRange.hasLower(offset)) {
                    return null;
                }
                
                return oldRange.createLower(offset);
            }
            
            @Override
            public void setObject(final CardRange object) {
                mutableRangeModel.setObject(object);
            }
        });
    }
    
    // ================
    // COMMON FACTORIES
    // ================
    
    /**
     * Creates a down link. Down link is a link that takes user to the more
     * detailed view on a card list or to the individual card.
     * 
     * @param id
     * component ID
     * @param row
     * row index
     * @return a link
     */
    public abstract AbstractLink createDownLink(String id, int row);
    
    /**
     * Creates a generic range link. After clicking, the model is assigned a new
     * value. The recommended usage is to override model setter and getter to do
     * actions.
     * 
     * @param id
     * component ID
     * @param mutableRange
     * mutable range model
     * @return a link
     */
    private static AbstractLink createRangeLink(final String id, final IModel<CardRange> mutableRange) {
        return new Link<CardRange>(id, mutableRange) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                mutableRange.setObject(this.getModelObject());
            }
            
            @Override
            public boolean isVisible() {
                if (mutableRange.getObject() == null) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
    }
    
    /**
     * Creates a web markup container, which wraps non-compact components. If
     * the compact mode is enabled OR the <code>alwaysCompact</code> property is
     * <code>true</code>, this container with all its contents is hidden.
     * 
     * @param id
     * component ID
     * @param view
     * card view settings to be used
     * @return a web markup container
     */
    protected WebMarkupContainer createNonCompactWrapper(final String id, final CardViewSettings view) {
        return new WebMarkupContainer(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && !view.isCompactModeEnabled() && !AbstractCardNavigatorPanel.this.alwaysCompact;
            }
        };
    }
    
    /**
     * Creates a web markup container, which wraps non-compact components. These
     * components are hidden only if the <code>alwaysCompact</code> property is
     * enabled.
     * 
     * @param id
     * component ID
     * @param view
     * card view settings to be used
     * @return a web markup container
     */
    private WebMarkupContainer createNonCompactWrapperOnlyAlways(final String id, final CardViewSettings view) {
        return new WebMarkupContainer(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && !AbstractCardNavigatorPanel.this.alwaysCompact;
            }
        };
    }
    
    /**
     * Creates a web markup container, which wraps the catalog range links. If
     * there is less than two cards in the range, the wrapped links are hidden.
     * 
     * @param id
     * component ID
     * @param range
     * card catalog range
     * @return a web markup container
     */
    protected static WebMarkupContainer createCatalogRangeWrapper(final String id, final IModel<CardCatalogRange> range) {
        return new WebMarkupContainer(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && range.getObject().getRange().hasMoreCards();
            }
        };
    }
    
    /**
     * Creates a web markup container, which wraps the range links. If there is
     * less than two cards in the range, the wrapped links are hidden.
     * 
     * @param id
     * component ID
     * @param range
     * card catalog range
     * @return a web markup container
     */
    protected static WebMarkupContainer createCardRangeWrapper(final String id, final IModel<CardRange> range) {
        return new WebMarkupContainer(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && range.getObject().hasMoreCards();
            }
        };
    }
}
