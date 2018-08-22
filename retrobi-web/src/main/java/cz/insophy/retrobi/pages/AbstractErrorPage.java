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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;

import cz.insophy.retrobi.link.BookmarkableCatalogLink;

/**
 * Abstract error page. Provides a basic framework for showing errors.
 * 
 * @author Vojtěch Hordějčuk
 */
abstract public class AbstractErrorPage extends AbstractBasicPage {
    /**
     * page title
     */
    private final String title;
    
    /**
     * Creates a new instance. Use for unknown / default errors.
     * 
     * @param parameters
     * page parameters
     */
    protected AbstractErrorPage(final PageParameters parameters) {
        this(parameters, "Blíže nepopsaná chyba", "Nastala blíže nepopsaná chyba. " +
                "Omlouváme se Vám. " +
                "Pomůže nám, když tuto chybu nahlásíte administrátorovi. " +
                "Děkujeme Vám za jakoukoliv pomoc.");
    }
    
    /**
     * Creates a new instance. Use for custom errors or for errors with no stack
     * trace available.
     * 
     * @param parameters
     * page parameters
     * @param title
     * page title
     * @param message
     * error message
     */
    protected AbstractErrorPage(final PageParameters parameters, final String title, final String message) {
        super(parameters);
        this.title = title;
        this.addComponents(message, null);
    }
    
    /**
     * Creates a new instance. Use for errors with a stack trace.
     * 
     * @param parameters
     * page parameters
     * @param title
     * page title
     * @param error
     * error that was thrown
     */
    protected AbstractErrorPage(final PageParameters parameters, final String title, final Throwable error) {
        super(parameters);
        this.title = title;
        this.addComponents(error.getMessage(), AbstractErrorPage.getStackTrace(error));
    }
    
    /**
     * Adds components to the page.
     * 
     * @param message
     * message to be shown
     * @param stack
     * stack trace as string (or some kind of placeholder)
     */
    private void addComponents(final String message, final String stack) {
        // create components
        
        final Component link = new BookmarkableCatalogLink("link.return");
        final Component titleLabel = new Label("label.title", this.title);
        final Component textLabel = new Label("label.text", message);
        
        final Component stackLabel = new Label("label.stack", stack == null ? "" : stack) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && (stack != null);
            }
        };
        
        // place components
        
        this.add(link);
        this.add(titleLabel);
        this.add(textLabel);
        this.add(stackLabel);
    }
    
    @Override
    protected String getPageTitle() {
        return this.title;
    }
    
    @Override
    public boolean isErrorPage() {
        return true;
    }
    
    /**
     * Converts a stack trace to a string.
     * 
     * @param error
     * an error to be converted
     * @return stack trace as string
     */
    private static String getStackTrace(final Throwable error) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        error.printStackTrace(printWriter);
        return result.toString();
    }
}
