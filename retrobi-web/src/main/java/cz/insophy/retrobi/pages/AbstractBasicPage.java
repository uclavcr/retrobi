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

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.link.BookmarkableIndexLink;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.panel.LoginPanel;
import cz.insophy.retrobi.panel.SidebarPanel;

/**
 * Abstract base class for all pages.
 * 
 * @author Vojtěch Hordějčuk
 */
abstract public class AbstractBasicPage extends WebPage {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBasicPage.class);
    /**
     * login panel
     */
    private LoginPanel loginPanel;
    /**
     * sidebar panel
     */
    private SidebarPanel sidebarPanel;
    /**
     * feedback panel
     */
    private FeedbackPanel feedbackPanel;
    
    /**
     * Creates a new instance. Allows to specify the title.
     * 
     * @param parameters
     * page parameters
     */
    protected AbstractBasicPage(final PageParameters parameters) {
        this(parameters, UserRole.GUEST);
    }
    
    /**
     * Creates a new instance. Allows to specify the title and the minimal user
     * role. If the minimal user role is not met, user is redirected to the
     * error page.
     * 
     * @param parameters
     * page parameters
     * @param minRole
     * minimal user role needed for the page (privileges required)
     */
    protected AbstractBasicPage(final PageParameters parameters, final UserRole minRole) {
        super(parameters);
        
        AbstractBasicPage.LOG.debug(String.format(
                "Creating page '%s' with parameters '%s'...",
                this.getClass().getName(),
                parameters == null ? "(none)" : parameters.toString()));
        
        this.setVersioned(false);
        
        // redirect to error page when the user has not enough privileges
        
        final RetrobiWebSession session = RetrobiWebSession.get();
        
        if (!session.refreshLoggedUser()) {
            this.error("Přihlašte se prosím znovu.");
            throw new RestartResponseException(WebApplication.get().getHomePage());
        }
        
        if (!session.hasRoleAtLeast(minRole)) {
            this.error(String.format("Nemáte požadované oprávnění '%s', budete přesměrováni.", minRole.toString()));
            throw new AbortWithHttpStatusException(403, true);
        }
        
        if (CardCatalogModel.getInstance().isUpdating()) {
            this.info("Probíhá aktualizace katalogu: " + CardCatalogModel.getInstance().getUpdateStatus());
        }
        
        if (!session.getCardContainer().getBatchModificationResult().isEmpty()) {
            this.info("POZOR! V hromadné editaci zůstávají neuložené změny.");
        }
        
        if (session.getCardContainer().getBasketSize() > Settings.MANY_BASKET_CARDS) {
            this.info(String.format("POZOR! Ve schránce máte velmi mnoho lístků. Hromadné operace lze najednou spustit maximáně pro %d lístků.", Settings.MANY_BASKET_CARDS));
        }
        
        this.initComponentModels(parameters);
        this.initComponents(parameters);
    }
    
    /**
     * Initializes the page component models. This method is called always
     * BEFORE the components are created.
     * 
     * @param parameters
     * page parameters
     */
    protected void initComponentModels(final PageParameters parameters) {
        // NOP
    }
    
    /**
     * Initializes the page components. This method is called always AFTER the
     * models are initialized. Warning: If you plan to add your components on
     * page and do not require complex models, DO NOT use this method with your
     * models as it is called in the <code>super(...)</code> constructor.
     * 
     * @param parameters
     * page parameters
     */
    protected void initComponents(final PageParameters parameters) {
        // create components
        
        this.loginPanel = new LoginPanel("main_panel.login");
        this.sidebarPanel = new SidebarPanel("main_panel.sidebar");
        this.feedbackPanel = new FeedbackPanel("main_panel.feedback");
        
        final WebMarkupContainer indexLink = new BookmarkableIndexLink("main_link.home");
        
        // setup components
        
        this.feedbackPanel.setOutputMarkupId(true);
        
        // place components
        
        this.add(indexLink);
        
        this.add(new Label("main_label.title", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return AbstractBasicPage.this.getPageTitleModel().getObject() +
                        " - Retrospektivní bibliografie české literární vědy";
            }
        }));
        
        this.add(this.loginPanel);
        this.add(this.feedbackPanel);
        this.add(this.sidebarPanel);
    }
    
    /**
     * Returns the page specific help text type
     * 
     * @return the help text type or <code>null</code> if none
     */
    public TextType getHelpTextType() {
        return null;
    }
    
    /**
     * Returns the page title model.
     * 
     * @return the page title model
     */
    public IModel<String> getPageTitleModel() {
        return new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return AbstractBasicPage.this.getPageTitle();
            }
        };
    }
    
    /**
     * Returns the page title. May be dynamically generated, because the method
     * is called from a page title model during the page rendering.
     * 
     * @return the page title
     */
    abstract protected String getPageTitle();
    
    /**
     * Modifies the AJAX request target by adding some common components.
     * 
     * @param target
     * AJAX request target
     */
    public void modifyAjaxTarget(final AjaxRequestTarget target) {
        target.addComponent(this.sidebarPanel.getBasketSizeLabel());
        target.addComponent(this.feedbackPanel);
    }
    
    /**
     * This method is called after a long task is finished.
     */
    public void onLongTaskFinished() {
        // NOP
    }
}
