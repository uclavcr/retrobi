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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.ImageFlag;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.library.SimpleImageUtils;

/**
 * Internal add form.
 */
public class ImageUploadForm extends Form<Card> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * file upload model
     */
    private final IModel<FileUpload> uploadModel;
    /**
     * page number model
     */
    private final IModel<String> imageNameModel;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent panel
     * @param card
     * card model
     */
    public ImageUploadForm(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, card);
        
        this.parent = parent;
        
        // setup the form
        
        this.setMultiPart(true);
        this.setMaxSize(Bytes.megabytes(32));
        
        // create models
        
        this.uploadModel = new Model<FileUpload>();
        this.imageNameModel = new Model<String>("1");
        
        // create components
        
        final FileUploadField fileInput = new FileUploadField("input.file", this.uploadModel);
        
        final DropDownChoice<String> pageInput = new DropDownChoice<String>(
                "input.page",
                this.imageNameModel,
                Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
        
        // place components
        
        this.add(fileInput);
        this.add(pageInput);
    }
    
    @Override
    protected void onSubmit() {
        // check privileges
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
            this.error("Nemáte dostatečná oprávnění k provedení úpravy.");
            return;
        }
        
        // check values
        
        if (this.uploadModel.getObject() == null) {
            this.error("Nebyl vybrán žádný soubor.");
            return;
        }
        
        // check MIME type
        
        final String mime = this.uploadModel.getObject().getContentType().toLowerCase();
        
        if (!mime.toLowerCase().equals("image/png") && !mime.toLowerCase().equals("image/tiff")) {
            this.error(String.format("Nepodporovaný typ souboru (měl být 'image/png' nebo 'image/tiff', byl '%s').", mime));
            return;
        }
        
        // save uploaded image into the temporary file
        
        final File temp;
        
        try {
            temp = this.uploadModel.getObject().writeToTempFile();
        } catch (final IOException x) {
            this.error("Chyba při ukládání odeslaného souboru." + x.getMessage());
            return;
        }
        
        try {
            final BufferedImage image = SimpleImageUtils.loadImageFromFile(temp);
            SimpleImageUtils.makeThumbnailImage(image, Settings.TARGET_IMAGE_WIDTH, false);
            SimpleImageUtils.saveImageToPngFile(image, temp);
        } catch (final IOException x) {
            this.error("Chyba při generování náhledu: " + x.getMessage());
            return;
        }
        
        // assign file to the card
        
        try {
            RetrobiApplication.db().getCardImageRepository().addImageToCard(
                    this.getModelObject(),
                    temp,
                    ImageFlag.produceImageName(Integer.valueOf(this.imageNameModel.getObject()), ImageFlag.ORIGINAL),
                    "image/png");
        } catch (final GeneralRepositoryException x) {
            this.error("Chyba při přiřazování obrázku k lístku: " + x.getMessage());
            return;
        }
        
        this.info("Upload a zpracování proběhlo úspěšně.");
        
        // reload viewer after edit
        
        this.parent.requestCardViewerUpdate();
    }
}
