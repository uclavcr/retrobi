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

package cz.insophy.retrobi.utils.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;

import cz.insophy.retrobi.Settings;

/**
 * Simple resource that serves JNLP files that are all same and differ only in
 * some parts. The template for the JNLP is loaded from classpath as a resource
 * and is dynamically filled for each client request.
 * 
 * @author Michal Rydlo
 */
public class JnlpResource extends Resource {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * the JNLP template file containing ${vars} that have to be replaced before
     * sending to the client
     */
    private static final String JNLP_TEMPLATE;
    /**
     * JNLP name
     */
    private final String name;
    /**
     * JNLP title (tool name)
     */
    private final String title;
    /**
     * name of the main class
     */
    private final String mainClassName;
    
    /**
     * Creates a new instance. The name must be specified which will be used
     * for:
     * <ol>
     * <li>naming the JNLP in the URL
     * <li>selecting package containing the {@code MainFrame} class to be
     * executed at client JVM
     * </ol>
     * 
     * @param name
     * name
     * @param title
     * application title
     */
    public JnlpResource(final String name, final String title) {
        super();
        
        this.name = name;
        this.title = title;
        this.mainClassName = "cz.insophy.retrobitool." + name + "." + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase() + "MainFrame";
    }
    
    static {
        // tries to load the JNLP template from resources
        
        final InputStream jnlpStream = JnlpResource.class.getResourceAsStream("template.jnlp");
        
        InputStreamReader reader = null;
        
        try {
            try {
                reader = new InputStreamReader(jnlpStream, "UTF-8");
                final StringBuffer data = new StringBuffer(1000);
                final char[] buf = new char[16 * 1024];
                int numRead = 0;
                while ((numRead = reader.read(buf)) != -1) {
                    data.append(buf, 0, numRead);
                }
                JNLP_TEMPLATE = data.toString();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (final IOException x) {
            throw new IllegalStateException(x);
        }
    }
    
    @Override
    public IResourceStream getResourceStream() {
        if (!(RequestCycle.get() instanceof WebRequestCycle)) {
            return null;
        }
        
        // tricky way to find the URL the user entered in the browser
        // (needed to correctly set the codebase in the JNLP)
        
        final WebRequestCycle w = (WebRequestCycle) RequestCycle.get();
        
        final HttpServletRequest request = w.getWebRequest().getHttpServletRequest();
        final StringBuffer requestURL = request.getRequestURL();
        final int i = requestURL.lastIndexOf("/");
        String newBase = requestURL.substring(0, i + 1);
        
        // find server name
        // (we use the host name that the user typed into the address bar)
        
        String serverHost = request.getHeader("X-Forwarded-Host");
        
        if (serverHost == null) {
            serverHost = request.getServerName();
        } else {
            // modify codebase if behind proxy
            
            try {
                final URI oldBase = new URI(newBase);
                
                newBase = new URI(
                        oldBase.getScheme(),
                        oldBase.getUserInfo(),
                        serverHost,
                        -1,
                        oldBase.getPath(),
                        oldBase.getQuery(),
                        oldBase.getFragment()).toString();
            } catch (final URISyntaxException e) {
                // nothing
            }
        }
        
        // preparation for variable substitution in JNLP template
        
        final HashMap<String, Object> params = new HashMap<String, Object>();
        
        params.put("codebase", newBase);
        params.put("name", this.name);
        params.put("title", this.title);
        params.put("className", this.mainClassName);
        params.put("db_host", serverHost);
        params.put("db_port", Settings.DB_PORT + "");
        params.put("db_name", Settings.DB_NAME);
        
        // return template with substituted variables and correct MIME-type
        
        return new StringResourceStream(new MapVariableInterpolator(
                JnlpResource.JNLP_TEMPLATE, params).toString(),
                "application/x-java-jnlp-file");
    }
}
