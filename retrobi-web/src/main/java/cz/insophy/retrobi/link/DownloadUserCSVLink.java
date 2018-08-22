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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * A link that downloads user list as CSV after click.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DownloadUserCSVLink extends Link<Object> {
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
    public DownloadUserCSVLink(final String id) {
        super(id);
    }
    
    @Override
    public void onClick() {
        final ResourceStreamRequestTarget target = new ResourceStreamRequestTarget(new AbstractResourceStreamWriter() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void write(final OutputStream output) {
                // try to load all users
                
                final List<User> users;
                
                try {
                    users = RetrobiApplication.db().getUserRepository().getAllUsers();
                } catch (final NotFoundRepositoryException x) {
                    DownloadUserCSVLink.this.error(x.getMessage());
                    return;
                } catch (final GeneralRepositoryException x) {
                    DownloadUserCSVLink.this.error(x.getMessage());
                    return;
                }
                
                Writer writer = null;
                
                try {
                    writer = new BufferedWriter(new OutputStreamWriter(output, Settings.CSV_ENCODING), 1024);
                    
                    // write CSV header
                    
                    DownloadUserCSVLink.writeHeader(writer);
                    
                    // write users to CSV
                    
                    for (final User user : users) {
                        DownloadUserCSVLink.writeUser(writer, user);
                    }
                    
                    writer.flush();
                } catch (final IOException x) {
                    DownloadUserCSVLink.this.error(x.getMessage());
                } finally {
                    // close the writer
                    
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (final IOException x) {
                            DownloadUserCSVLink.this.error(x.getMessage());
                        }
                    }
                    
                    try {
                        // flush and close stream
                        
                        output.flush();
                        output.close();
                    } catch (final IOException x) {
                        DownloadUserCSVLink.this.error(x.getMessage());
                    }
                }
            }
            
            @Override
            public String getContentType() {
                return "text/csv";
            }
        });
        
        target.setFileName("uzivatele.csv");
        RequestCycle.get().setRequestTarget(target);
    }
    
    /**
     * Writes the header.
     * 
     * @param writer
     * target writer
     * @throws IOException
     * I/O exception
     */
    private static void writeHeader(final Writer writer) throws IOException {
        writer.write(SimpleStringUtils.escapeColsForCSV(
                true,
                "Login",
                "E-mail",
                "Druh uživatele",
                "Odborné zaměření",
                "Instituce",
                "Limit: velikost schránky",
                "Limit: počet schránek",
                "Počet přihlášení",
                "Počet přepisů",
                "Poslední přihlášení",
                "Čas registrace",
                "ID"));
    }
    
    /**
     * Writes a user.
     * 
     * @param writer
     * target writer
     * @param user
     * user to write
     * @throws IOException
     * I/O exception
     */
    private static void writeUser(final Writer writer, final User user) throws IOException {
        writer.write(SimpleStringUtils.escapeColsForCSV(
                true,
                user.getLogin(),
                user.getEmail(),
                user.getType(),
                user.getBranch(),
                user.getAlma(),
                String.valueOf(user.getBasketLimit()),
                String.valueOf(user.getCardsetLimit()),
                String.valueOf(user.getLoginCount()),
                String.valueOf(user.getEditCount()),
                String.valueOf(user.getLastLoginDate()),
                String.valueOf(user.getRegistrationDate()),
                user.getId()));
    }
}
