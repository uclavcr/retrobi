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
import java.io.IOException;
import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.ImageFlag;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.library.SimpleImageUtils;

/**
 * Internal edit form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ImageEditorForm extends Form<Card> {
    /**
     * Image operation enumeration.
     * 
     * @author Vojtěch Hordějčuk
     */
    private static enum Operation {
        /**
         * rotate by 180 degrees
         */
        ROTATE,
        /**
         * draw a cross
         */
        CROSS,
        /**
         * remove image
         */
        REMOVE;
        
        @Override
        public String toString() {
            switch (this.ordinal()) {
                case 0:
                    return "Otočit o 180°";
                case 1:
                    return "Zaškrtnout / Odškrtnout";
                case 2:
                    return "Smazat";
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }
    
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent navigator panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * page number model
     */
    private final IModel<String> imageNameModel;
    /**
     * operation model
     */
    private final IModel<Operation> operationModel;
    
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
    public ImageEditorForm(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, card);
        
        this.parent = parent;
        
        // create models
        
        this.imageNameModel = new Model<String>("1");
        this.operationModel = new Model<Operation>();
        
        // create components
        
        final DropDownChoice<Operation> operation = new DropDownChoice<Operation>(
                "input.operation",
                this.operationModel,
                Arrays.asList(Operation.values()));
        
        final DropDownChoice<String> pageInput = new DropDownChoice<String>(
                "input.page",
                this.imageNameModel,
                this.getModelObject().getAttachmentNamesSorted());
        
        // setup components
        
        operation.setNullValid(true);
        pageInput.setNullValid(true);
        
        // place components
        
        this.add(operation);
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
        
        if (this.imageNameModel.getObject() == null) {
            this.error("Vyberte prosím obrázek k úpravě.");
            return;
        }
        
        if (this.operationModel.getObject() == null) {
            this.error("Vyberte prosím operaci.");
            return;
        }
        
        // get image name
        
        final String imageName = this.imageNameModel.getObject();
        
        // do the operation selected
        
        switch (this.operationModel.getObject()) {
            case ROTATE:
                // rotate image
                
                try {
                    this.rotateImage(this.getModelObject(), imageName);
                    this.info("Obrázek byl úspěšně otočen.");
                } catch (final GeneralRepositoryException x) {
                    this.error("Obecná chyba při rotaci: " + x.getMessage());
                } catch (final IOException x) {
                    this.error("Chyba I/O při rotaci: " + x.getMessage());
                }
                
                break;
            case CROSS:
                // cross image
                
                try {
                    this.crossImage(this.getModelObject(), imageName);
                    this.info("Zaškrtnutí obrázku bylo úspěšně změněno.");
                } catch (final GeneralRepositoryException x) {
                    this.error("Obecná chyba při škrtání: " + x.getMessage());
                } catch (final NotFoundRepositoryException x) {
                    this.error("Chyba I/O při škrtání: " + x.getMessage());
                }
                
                break;
            case REMOVE:
                // remove image
                
                try {
                    RetrobiApplication.db().getCardImageRepository().removeImageFromCard(this.getModelObject(), imageName);
                    this.info("Obrázek byl úspěšně smazán.");
                } catch (final GeneralRepositoryException x) {
                    this.error("Chyba při mazání: " + x.getMessage());
                } catch (final NotFoundRepositoryException x) {
                    this.error("Chyba při mazání: " + x.getMessage());
                }
                
                break;
            default:
                throw new IllegalStateException();
        }
        
        // reload viewer after edit
        
        this.parent.requestCardViewerUpdate();
    }
    
    /**
     * Uploads a rotated image to the card.
     * 
     * @param card
     * card
     * @param imageName
     * image name
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws IOException
     * I/O exception
     */
    private void rotateImage(final Card card, final String imageName) throws GeneralRepositoryException, IOException {
        // load old image
        
        final BufferedImage image = RetrobiApplication.db().getCardImageRepository().getCardImage(card.getId(), imageName);
        
        // generate rotated image
        
        final BufferedImage newImage = SimpleImageUtils.makeImageRotatedBy180(image);
        
        // save rotated image
        
        RetrobiApplication.db().getCardImageRepository().addImageToCard(card, newImage, imageName, "png", "image/png");
    }
    
    /**
     * Crosses or un-crosses the card image.
     * 
     * @param card
     * card
     * @param imageName
     * image name
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    private void crossImage(final Card card, final String imageName) throws GeneralRepositoryException, NotFoundRepositoryException {
        // decide new name
        
        final String newImageName = ImageFlag.CROSSOUT.inImageName(imageName)
                ? ImageFlag.CROSSOUT.removeFromImageName(imageName)
                : ImageFlag.CROSSOUT.addToImageName(imageName);
        
        if (imageName.equals(newImageName)) {
            this.error("Původní a nový obrázek se shodují v názvu. Obrázek bude zachován beze změny.");
            return;
        }
        
        // load the original image
        
        final BufferedImage image = RetrobiApplication.db().getCardImageRepository().getCardImage(
                card.getId(),
                imageName);
        
        // add crossed image
        
        RetrobiApplication.db().getCardImageRepository().addImageToCard(
                card,
                image,
                newImageName,
                "png",
                "image/png");
        
        // remove un-crossed image
        
        RetrobiApplication.db().getCardImageRepository().removeImageFromCard(
                card,
                imageName);
        
        // log the change
        
        RetrobiApplication.db().getMessageRepository().eventImageCrossUpdated(
                RetrobiWebApplication.getCSVLogger(),
                imageName,
                newImageName,
                card,
                RetrobiWebSession.get().getLoggedUser());
    }
}
