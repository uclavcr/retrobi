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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Message;
import cz.insophy.retrobi.database.entity.Time;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * A link that downloads old message list as CSV after click.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DownloadOldMessageCSVLink extends Link<Object> {
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
    public DownloadOldMessageCSVLink(final String id) {
        super(id);
    }
    
    @Override
    public void onClick() {
        try {
            // get old message IDs
            
            final List<String> ids = RetrobiApplication.db().getMessageRepository().getOldMessageIds(Settings.OLD_MESSAGE_LIMIT);
            
            // load old messages by their IDs
            
            final List<Message> messages = new ArrayList<Message>(RetrobiApplication.db().getMessageRepository().getMessages(ids));
            
            // sort the old messages by date
            
            Collections.sort(messages, new Comparator<Message>() {
                @Override
                public int compare(final Message o1, final Message o2) {
                    return Time.compare(o1.getAdded(), o2.getAdded());
                }
            });
            
            // write old messages as a CSV
            
            final ResourceStreamRequestTarget target = new ResourceStreamRequestTarget(new AbstractResourceStreamWriter() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void write(final OutputStream output) {
                    Writer writer = null;
                    
                    try {
                        writer = new BufferedWriter(new OutputStreamWriter(output, Settings.CSV_ENCODING), 1024);
                        
                        // write CSV header
                        
                        DownloadOldMessageCSVLink.writeHeader(writer);
                        
                        // write messages to CSV
                        
                        for (final Message message : messages) {
                            DownloadOldMessageCSVLink.writeMessage(writer, message);
                        }
                        
                        writer.flush();
                    } catch (final IOException x) {
                        DownloadOldMessageCSVLink.this.error(x.getMessage());
                    } finally {
                        // close the writer
                        
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (final IOException x) {
                                DownloadOldMessageCSVLink.this.error(x.getMessage());
                            }
                        }
                        
                        try {
                            // flush and close stream
                            
                            output.flush();
                            output.close();
                        } catch (final IOException x) {
                            DownloadOldMessageCSVLink.this.error(x.getMessage());
                        }
                    }
                }
                
                @Override
                public String getContentType() {
                    return "text/csv";
                }
            });
            
            if (messages.isEmpty()) {
                this.info("Seznam starých hlášení je prázdný.");
            } else {
                target.setFileName("zpravy.csv");
                RequestCycle.get().setRequestTarget(target);
            }
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
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
                "ID",
                "Přidáno",
                "ID lístku",
                "Lístek",
                "Obrázek",
                "ID uživatele",
                "Uživatel",
                "Text",
                "Datum potvrzení",
                "Potvrdil"));
    }
    
    /**
     * Writes a message.
     * 
     * @param writer
     * target writer
     * @param message
     * message to write
     * @throws IOException
     * I/O exception
     */
    private static void writeMessage(final Writer writer, final Message message) throws IOException {
        writer.write(SimpleStringUtils.escapeColsForCSV(
                true,
                message.getId(),
                message.getAdded().toString(),
                message.getCardId(),
                message.getCardName(),
                message.getImageName(),
                message.getUserId(),
                message.getUserName(),
                message.getBody(),
                SimpleStringUtils.neverEmpty(message.getConfirmed()),
                message.getConfirmedByUserId()));
    }
}
