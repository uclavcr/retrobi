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

package cz.insophy.retrobi.form;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.model.setup.ImageViewMode;
import cz.insophy.retrobi.utils.DataToExport;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;
import cz.insophy.retrobi.utils.library.SimpleExportUtils;
import cz.insophy.retrobi.utils.type.DownloadImageQuality;
import cz.insophy.retrobi.utils.type.DownloadTextSource;

/**
 * Basket download form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasketDownloadForm extends Form<Object> {
    /**
     * ZIP file name
     */
    private static final String ZIP_FILE_NAME = "retrobi_schranka.zip";
    /**
     * RTF file name
     */
    private static final String RTF_FILE_NAME = "retrobi_schranka.rtf";
    /**
     * ZIP file mime type
     */
    private static final String ZIP_MIME_TYPE = "application/zip";
    
    /**
     * Format to download.
     * 
     * @author Vojtěch Hordějčuk
     */
    private static enum DownloadImageFormat {
        /**
         * ZIP with a group of files for each card
         */
        ZIP_ONE_BY_ONE("JPG/PNG+TXT"),
        /**
         * ZIP with RTF (all files in one)
         */
        ZIP_ALL_IN_ONE("RTF");
        /**
         * constant title
         */
        private final String title;
        
        /**
         * Creates a new instance.
         * 
         * @param title
         * constant title
         */
        private DownloadImageFormat(final String title) {
            this.title = title;
        }
        
        @Override
        public String toString() {
            return this.title;
        }
    }
    
    /**
     * Download image source.
     * 
     * @author Vojtěch Hordějčuk
     */
    private static enum DownloadImageType {
        /**
         * no images at all
         */
        NONE("Žádné"),
        /**
         * original images only
         */
        ORIGINAL("Originály"),
        /**
         * best images available
         */
        BEST("Nejlepší");
        /**
         * constant title
         */
        private final String title;
        
        /**
         * Creates a new instance.
         * 
         * @param title
         * constant title
         */
        private DownloadImageType(final String title) {
            this.title = title;
        }
        
        @Override
        public String toString() {
            return this.title;
        }
    }
    
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * desired image format
     */
    private final IModel<DownloadImageFormat> format;
    /**
     * desired image type
     */
    private final IModel<DownloadImageType> imageType;
    /**
     * desired image quality
     */
    private final IModel<DownloadImageQuality> imageQuality;
    /**
     * desired text source
     */
    private final IModel<DownloadTextSource> textSource;
    /**
     * append some detail information
     */
    private final IModel<Boolean> appendDetailData;
    /**
     * append user comments
     */
    private final IModel<Boolean> appendComments;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public BasketDownloadForm(final String id) {
        super(id);
        
        // prepare models
        
        this.format = Model.of(DownloadImageFormat.ZIP_ONE_BY_ONE);
        this.imageType = Model.of(DownloadImageType.ORIGINAL);
        this.imageQuality = Model.of(DownloadImageQuality.JPEG_HIGH);
        this.textSource = Model.of(DownloadTextSource.BEST);
        this.appendDetailData = Model.of(false);
        this.appendComments = Model.of(true);
        
        // create components
        
        final DropDownChoice<DownloadImageFormat> formatSelect = new DropDownChoice<DownloadImageFormat>(
                "select.format",
                this.format,
                Arrays.asList(DownloadImageFormat.values()));
        
        final DropDownChoice<DownloadImageType> imageTypeSelect = new DropDownChoice<DownloadImageType>(
                "select.type",
                this.imageType,
                Arrays.asList(DownloadImageType.values()));
        
        final DropDownChoice<DownloadImageQuality> imageQualitySelect = new DropDownChoice<DownloadImageQuality>(
                "select.quality",
                this.imageQuality,
                Arrays.asList(DownloadImageQuality.values()));
        
        final DropDownChoice<DownloadTextSource> textSourceSelect = new DropDownChoice<DownloadTextSource>(
                "select.text",
                this.textSource,
                Arrays.asList(DownloadTextSource.values()));
        
        final CheckBox appendDetailDataCheck = new CheckBox("check.detail", this.appendDetailData);
        final CheckBox appendCommentsCheck = new CheckBox("check.comments", this.appendComments);
        
        // place components
        
        this.add(formatSelect);
        this.add(imageTypeSelect);
        this.add(imageQualitySelect);
        this.add(textSourceSelect);
        this.add(appendDetailDataCheck);
        this.add(appendCommentsCheck);
    }
    
    @Override
    protected void onSubmit() {
        // check if the basket can be downloaded
        
        if (RetrobiWebSession.get().getActiveTask() != null) {
            this.error("Stahování nelze zahájit během spuštění dlouhé úlohy.");
            return;
        }
        
        if (!RetrobiWebSession.get().canDownloadBasket()) {
            this.error("Schránku není možné stáhnout.");
            return;
        }
        
        // load cards to export
        
        final List<String> cardIds = RetrobiWebSession.get().getCardContainer().getBasketCardIds();
        
        // write into the stream
        
        final AbstractResourceStreamWriter writer = new AbstractResourceStreamWriter() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void write(final OutputStream output) {
                try {
                    switch (BasketDownloadForm.this.format.getObject()) {
                        case ZIP_ALL_IN_ONE:
                            BasketDownloadForm.this.writeRtf(cardIds, output);
                            break;
                        case ZIP_ONE_BY_ONE:
                            BasketDownloadForm.this.writeZip(cardIds, output);
                            break;
                        default:
                            throw new IndexOutOfBoundsException();
                    }
                } catch (final IOException x) {
                    BasketDownloadForm.this.error((x.getMessage() == null)
                            ? x.getClass().getSimpleName()
                            : x.getMessage());
                } finally {
                    try {
                        output.flush();
                        output.close();
                    } catch (final IOException x) {
                        BasketDownloadForm.this.error((x.getMessage() == null)
                                ? x.getClass().getSimpleName()
                                : x.getMessage());
                    }
                }
            }
            
            @Override
            public String getContentType() {
                return BasketDownloadForm.ZIP_MIME_TYPE;
            }
        };
        
        // start the download
        
        this.getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(writer, BasketDownloadForm.ZIP_FILE_NAME));
        
        // remove the current pagemap from the session
        // (this will cause all pages to expire)
        
        this.getPage().getPageMap().remove();
    }
    
    /**
     * Writes a ZIP archive into the provided output stream.
     * 
     * @param cardIds
     * card IDs to write
     * @param output
     * output stream
     * @throws IOException
     * I/O exception
     */
    private void writeZip(final List<String> cardIds, final OutputStream output) throws IOException {
        ZipOutputStream writer = null;
        
        try {
            writer = new ZipOutputStream(output);
            writer.setLevel(9);
            
            // add entries to ZIP
            
            for (final String cardId : cardIds) {
                final Card card = RetrobiApplication.db().getCardRepository().getCard(cardId);
                
                SimpleExportUtils.writeCardAsZip(
                        writer,
                        new DataToExport(
                                card,
                                BasketDownloadForm.pickAttributes(this.appendDetailData.getObject(), card),
                                BasketDownloadForm.pickImageNames(this.imageType.getObject(), card),
                                BasketDownloadForm.pickComments(this.appendComments.getObject(), card),
                                this.imageQuality.getObject(),
                                this.textSource.getObject()));
            }
            
            // finish the archive
            
            writer.finish();
            
            // flush the stream
            
            writer.flush();
            output.flush();
        } catch (final IOException x) {
            this.error(x.getMessage());
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        } finally {
            // close (if needed)
            
            if (writer != null) {
                writer.close();
            }
            
            if (output != null) {
                output.close();
            }
        }
    }
    
    /**
     * Writes a RTF file (archived in ZIP) into the provided output stream.
     * 
     * @param cardIds
     * card IDs to write
     * @param output
     * output stream
     * @throws IOException
     * I/O exception
     */
    private void writeRtf(final List<String> cardIds, final OutputStream output) throws IOException {
        ZipOutputStream zipWriter = null;
        Writer rtfWriter = null;
        
        try {
            // initialize streams
            
            zipWriter = new ZipOutputStream(output);
            rtfWriter = new BufferedWriter(new OutputStreamWriter(zipWriter, "utf-8"), 32768);
            zipWriter.setLevel(9);
            
            // get user
            
            User user = null;
            
            if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                user = RetrobiWebSession.get().getLoggedUser();
            }
            
            // open the entry
            
            zipWriter.putNextEntry(new ZipEntry(BasketDownloadForm.RTF_FILE_NAME));
            
            // write RTF
            
            SimpleExportUtils.writeRtfHeader(rtfWriter);
            
            int cardNumber = 1;
            
            for (final String cardId : cardIds) {
                final Card card = RetrobiApplication.db().getCardRepository().getCard(cardId);
                
                SimpleExportUtils.writeCardAsRtf(
                        rtfWriter,
                        new DataToExport(
                                card,
                                BasketDownloadForm.pickAttributes(this.appendDetailData.getObject(), card),
                                BasketDownloadForm.pickImageNames(this.imageType.getObject(), card),
                                BasketDownloadForm.pickComments(this.appendComments.getObject(), card),
                                this.imageQuality.getObject(),
                                this.textSource.getObject()),
                                cardNumber++,
                                cardIds.size(),
                                user);
            }
            
            SimpleExportUtils.writeRtfFooter(rtfWriter);
            
            // flush the stream
            
            rtfWriter.flush();
            output.flush();
            
            // finish the archive
            
            zipWriter.closeEntry();
            zipWriter.finish();
            zipWriter.flush();
        } catch (final IOException x) {
            this.error(x.getMessage());
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        } finally {
            // close (if needed)
            
            if (rtfWriter != null) {
                rtfWriter.close();
            }
            
            if (output != null) {
                output.close();
            }
            
            if (zipWriter != null) {
                zipWriter.close();
            }
        }
    }
    
    /**
     * Picks image names or nothing.
     * 
     * @param type
     * image type
     * @param card
     * a source card
     * @return picked image names
     */
    private static List<String> pickImageNames(final DownloadImageType type, final Card card) {
        switch (type) {
            case NONE:
                return Collections.emptyList();
            case ORIGINAL:
                return ImageViewMode.FULL_ORIGINAL.filterImageNames(card.getAttachmentNamesSorted(), false);
            case BEST:
                return ImageViewMode.FULL.filterImageNames(card.getAttachmentNamesSorted(), false);
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    /**
     * Picks attributes or nothing.
     * 
     * @param include
     * include the attributes
     * @param card
     * a source card
     * @return the root attribute node or <code>null</code>
     */
    private static AttributeNode pickAttributes(final boolean include, final Card card) {
        if (include) {
            final AttributePrototype root = RetrobiWebConfiguration.getInstance().getAttributeRoot();
            final AttributeNode node = SimpleAttributeUtils.fromDocument(card, root);
            return node;
        }
        
        return null;
    }
    
    /**
     * Picks comments or nothing.
     * 
     * @param include
     * include comments
     * @param card
     * a source card
     * @return list of comments
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    private static List<Comment> pickComments(final boolean include, final Card card) throws GeneralRepositoryException, NotFoundRepositoryException {
        if (include && RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            final String userId = RetrobiWebSession.get().getLoggedUser().getId();
            final List<String> commentIds = RetrobiApplication.db().getCommentRepository().getCommentIdsForUser(card.getId(), userId);
            final List<Comment> comments = RetrobiApplication.db().getCommentRepository().getComments(commentIds);
            return Collections.unmodifiableList(comments);
        }
        
        return Collections.emptyList();
    }
}
