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

package cz.insophy.retrobi.link;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import cz.insophy.retrobi.RetrobiWebSession;

/**
 * A link that downloads the whole basket as a TXT file after clicking.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DownloadAsTxtLink extends Link<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public DownloadAsTxtLink(final String id) {
        super(id);
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().canDownloadBasket()) {
            this.error("Stahování nebude provedeno.");
            return;
        }
        
        final ResourceStreamRequestTarget target = new ResourceStreamRequestTarget(new AbstractResourceStreamWriter() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void write(final OutputStream output) {
                try {
                    // write basket down to the stream
                    
                    RetrobiWebSession.get().getCardContainer().exportBasketToStream(output);
                } catch (final IOException x) {
                    DownloadAsTxtLink.this.error(x.getMessage());
                } finally {
                    try {
                        // flush and close
                        
                        output.flush();
                        output.close();
                    } catch (final IOException x) {
                        DownloadAsTxtLink.this.error(x.getMessage());
                    }
                }
            }
            
            @Override
            public String getContentType() {
                return "text/plain";
            }
        });
        
        target.setFileName("schranka.txt");
        RequestCycle.get().setRequestTarget(target);
    }
}
