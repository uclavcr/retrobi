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

package cz.insophy.retrobi.database.entity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.svenson.JSONProperty;

import cz.insophy.retrobi.database.document.StandaloneDocument;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.ImageFlag;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Cards are main entities in the catalog. Each card in the database represents
 * a group of paper cards with relevant extracted meta-information. These paper
 * cards are scanned and grouped together during the import process. All scanned
 * images are resized and attached as attachments of the document.<br>
 * <br>
 * Catalog structure is as follows:
 * <ol>
 * <li>Master catalog contains catalogs (represented by <code>Catalog</code>
 * class)</li>
 * <li>Catalog contains batches (represented by <code>String</code> class)</li>
 * <li>Batch contains cards (represented by <code>Card</code> class)</li>
 * <li>Card contains images (represented by <code>DocumentAttachment</code>
 * class)</li>
 * </ol>
 * 
 * @author Vojtěch Hordějčuk
 */
public class Card extends StandaloneDocument implements Comparable<Card> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * date of document addition
     */
    private Time added;
    /**
     * date of last document update
     */
    private Time updated;
    /**
     * card state
     */
    private CardState state;
    /**
     * card catalog
     */
    private Catalog catalog;
    /**
     * card batch (must be non-empty)
     */
    private String batch;
    /**
     * card batch for sorting only (must be non-empty)
     */
    private String batchForSort;
    /**
     * card number in batch (for ordering purposes)
     */
    private int numberInBatch;
    /**
     * final OCR
     */
    private String ocr;
    /**
     * OCR fix suggestion
     */
    private String ocrFix;
    /**
     * ID of the last user that fixed the OCR
     */
    private String ocrFixUserId;
    /**
     * head segment
     */
    private String segmentHead;
    /**
     * title segment
     */
    private String segmentTitle;
    /**
     * bibliographic segment
     */
    private String segmentBibliography;
    /**
     * annotation segment
     */
    private String segmentAnnotation;
    /**
     * excerpter segment
     */
    private String segmentExcerpter;
    /**
     * original drawer
     */
    private String drawer;
    /**
     * original files
     */
    private List<String> files;
    /**
     * URL address
     */
    private String url;
    /**
     * note
     */
    private String note;
    
    /**
     * Creates a new default instance.
     */
    public Card() {
        super();
        
        this.added = Time.now();
        this.updated = Time.now();
        this.state = CardState.FRESH;
        this.batch = "";
        this.batchForSort = "";
        this.numberInBatch = 0;
        this.ocr = "";
        this.ocrFix = "";
        this.files = new LinkedList<String>();
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the document type flag.
     * 
     * @return document type flag
     */
    @JSONProperty(value = "TAG_card", readOnly = true)
    public boolean isCard() {
        return true;
    }
    
    /**
     * Returns the date of addition.
     * 
     * @return date of addition
     */
    @JSONProperty(value = "date_added")
    public Time getAdded() {
        return this.added;
    }
    
    /**
     * Returns the date of last update.
     * 
     * @return date of last update
     */
    @JSONProperty(value = "date_updated")
    public Time getUpdated() {
        return this.updated;
    }
    
    /**
     * Returns the card state.
     * 
     * @return card state
     */
    public CardState getState() {
        return this.state;
    }
    
    /**
     * Checks if the card has lower state than given. The lower state means less
     * informed (less information available on the card).
     * 
     * @param otherState
     * the other state
     * @return <code>true</code> if the current card state is lower (less
     * informed) than the state specified, <code>false</code> otherwise
     */
    @JSONProperty(ignore = true)
    public boolean hasLowerState(final CardState otherState) {
        if (otherState == null) {
            throw new NullPointerException("Other state cannot be NULL.");
        }
        
        if ((this.state == null) || this.state.isLowerThan(otherState)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns the catalog.
     * 
     * @return catalog
     */
    public Catalog getCatalog() {
        return this.catalog;
    }
    
    /**
     * Returns the batch name.
     * 
     * @return batch name
     */
    public String getBatch() {
        return this.batch;
    }
    
    /**
     * Returns the batch name.
     * 
     * @return batch name
     */
    @JSONProperty(value = "batch_sort")
    public String getBatchForSort() {
        return this.batchForSort;
    }
    
    /**
     * Returns the card number in a batch.
     * 
     * @return card number in a batch
     */
    @JSONProperty(value = "nr")
    public int getNumberInBatch() {
        return this.numberInBatch;
    }
    
    /**
     * Returns the final OCR.
     * 
     * @return final OCR
     */
    @JSONProperty(value = "ocr")
    public String getOcr() {
        return this.ocr;
    }
    
    /**
     * Returns the OCR fix suggestion.
     * 
     * @return OCR fix suggestion
     */
    @JSONProperty(value = "ocr_fix")
    public String getOcrFix() {
        return this.ocrFix;
    }
    
    /**
     * Returns the OCR fix suggestion or the regular OCR if the suggestion is
     * empty (<code>null</code> or just empty).
     * 
     * @return OCR fix suggestion or the default OCR if the suggestion is empty
     */
    @JSONProperty(ignore = true)
    public String getOcrFixOrDefault() {
        if (!SimpleStringUtils.isEmpty(this.ocrFix)) {
            return this.ocrFix;
        }
        
        return this.ocr;
    }
    
    /**
     * Returns the ID of the last user that fixed the OCR or <code>null</code>
     * if the OCR have not been fixed yet.
     * 
     * @return the user ID or <code>null</code>
     */
    @JSONProperty(value = "ocr_fix_user_id")
    public String getOcrFixUserId() {
        return this.ocrFixUserId;
    }
    
    /**
     * Returns the head segment.
     * 
     * @return the head segment
     */
    @JSONProperty(value = "segment_head", ignoreIfNull = true)
    public String getSegmentHead() {
        return this.segmentHead;
    }
    
    /**
     * Returns the title segment.
     * 
     * @return the title segment
     */
    @JSONProperty(value = "segment_title", ignoreIfNull = true)
    public String getSegmentTitle() {
        return this.segmentTitle;
    }
    
    /**
     * Returns the bibliography segment.
     * 
     * @return the bibliography segment
     */
    @JSONProperty(value = "segment_bibliography", ignoreIfNull = true)
    public String getSegmentBibliography() {
        return this.segmentBibliography;
    }
    
    /**
     * Returns the annotation segment.
     * 
     * @return the annotation segment
     */
    @JSONProperty(value = "segment_annotation", ignoreIfNull = true)
    public String getSegmentAnnotation() {
        return this.segmentAnnotation;
    }
    
    /**
     * Returns the excerpter segment.
     * 
     * @return the excerpter segment
     */
    @JSONProperty(value = "segment_excerpter", ignoreIfNull = true)
    public String getSegmentExcerpter() {
        return this.segmentExcerpter;
    }
    
    /**
     * Returns the original drawer.
     * 
     * @return original drawer
     */
    public String getDrawer() {
        return this.drawer;
    }
    
    /**
     * Returns the original file list.
     * 
     * @return original file list
     */
    public List<String> getFiles() {
        return Collections.unmodifiableList(this.files);
    }
    
    /**
     * Returns the page count of this card. The page count is defined as a
     * number of original images attached to this card.
     * 
     * @return the page count of this card
     */
    @JSONProperty(ignore = true)
    public int getPageCount() {
        return ImageFlag.filterImageNames(this.getAttachmentNames(), ImageFlag.ORIGINAL).size();
    }
    
    /**
     * Returns the URL.
     * 
     * @return the URL
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * Returns the note.
     * 
     * @return the note
     */
    public String getNote() {
        return this.note;
    }
    
    @Override
    public String toString() {
        return String.format("Lístek %d (%s: %s)", this.numberInBatch, this.catalog.name(), this.batch);
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets date added.
     * 
     * @param value
     * date added
     */
    public void setAdded(final Time value) {
        this.added = value;
    }
    
    /**
     * Sets date updated.
     * 
     * @param value
     * date update
     */
    public void setUpdated(final Time value) {
        this.updated = value;
    }
    
    /**
     * Sets card state.
     * 
     * @param value
     * state
     */
    public void setState(final CardState value) {
        this.state = value;
    }
    
    /**
     * Sets catalog.
     * 
     * @param value
     * catalog
     */
    public void setCatalog(final Catalog value) {
        this.catalog = value;
    }
    
    /**
     * Sets batch.
     * 
     * @param value
     * batch
     */
    public void setBatch(final String value) {
        this.batch = value;
    }
    
    /**
     * Sets batch for sort.
     * 
     * @param value
     * batch for sort
     */
    public void setBatchForSort(final String value) {
        this.batchForSort = value;
    }
    
    /**
     * Sets number in a batch.
     * 
     * @param value
     * number in a batch
     */
    public void setNumberInBatch(final int value) {
        this.numberInBatch = value;
    }
    
    /**
     * Sets the final OCR.
     * 
     * @param value
     * final OCR
     */
    public void setOcr(final String value) {
        this.ocr = value;
    }
    
    /**
     * Sets the OCR fix suggestion.
     * 
     * @param value
     * OCR fix suggestion
     */
    public void setOcrFix(final String value) {
        this.ocrFix = value;
    }
    
    /**
     * Sets the ID of the last user that fixed the OCR.
     * 
     * @param value
     * the user ID
     */
    public void setOcrFixUserId(final String value) {
        this.ocrFixUserId = value;
    }
    
    /**
     * Resets the OCR fix back to default.
     */
    public void resetRewrite() {
        this.state = CardState.FRESH;
        this.ocrFix = "";
        this.ocrFixUserId = null;
        this.segmentAnnotation = null;
        this.segmentBibliography = null;
        this.segmentExcerpter = null;
        this.segmentHead = null;
        this.segmentTitle = null;
    }
    
    /**
     * Sets the head segment.
     * 
     * @param value
     * the new segment value
     */
    public void setSegmentHead(final String value) {
        this.segmentHead = value;
    }
    
    /**
     * Sets the title segment.
     * 
     * @param value
     * the new segment value
     */
    public void setSegmentTitle(final String value) {
        this.segmentTitle = value;
    }
    
    /**
     * Sets the bibliography segment.
     * 
     * @param value
     * the new segment value
     */
    public void setSegmentBibliography(final String value) {
        this.segmentBibliography = value;
    }
    
    /**
     * Sets the annotation segment.
     * 
     * @param value
     * the new segment value
     */
    public void setSegmentAnnotation(final String value) {
        this.segmentAnnotation = value;
    }
    
    /**
     * Sets the excerpter segment.
     * 
     * @param value
     * the new segment value
     */
    public void setSegmentExcerpter(final String value) {
        this.segmentExcerpter = value;
    }
    
    /**
     * Sets the original drawer.
     * 
     * @param value
     * original drawer
     */
    public void setDrawer(final String value) {
        this.drawer = value;
    }
    
    /**
     * Adds an original file.
     * 
     * @param value
     * original file
     */
    public void addFile(final String value) {
        this.files.add(value);
    }
    
    /**
     * Clears original files.
     */
    public void clearFiles() {
        this.files.clear();
    }
    
    /**
     * Sets the original files.
     * 
     * @param value
     * original files
     */
    public void setFiles(final List<String> value) {
        this.files = value;
    }
    
    /**
     * Sets the URL. Automatically adds "http://" prefix if needed.
     * 
     * @param value
     * the URL
     */
    public void setUrl(final String value) {
        if (value == null) {
            this.url = null;
        } else if (value.length() < 1) {
            this.url = "";
        } else if (value.toLowerCase().startsWith("http://")) {
            this.url = value.toLowerCase();
        } else {
            this.url = "http://" + value.toLowerCase();
        }
    }
    
    /**
     * Sets the note.
     * 
     * @param value
     * the note
     */
    public void setNote(final String value) {
        this.note = value;
    }
    
    // ==========
    // COMPARSION
    // ==========
    
    @Override
    public int compareTo(final Card o) {
        if (o == this) {
            return 0;
        }
        
        // compare catalog
        
        int c = this.catalog.compareTo(o.catalog);
        
        if (c != 0) {
            return c;
        }
        
        // compare batch
        
        c = this.batch.compareTo(o.batch);
        
        if (c != 0) {
            return c;
        }
        
        // compare numbers (last resort)
        
        return this.numberInBatch - o.numberInBatch;
    }
}
