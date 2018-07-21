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

package cz.insophy.retrobi.utils.component;

import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.model.SearchQuery;
import cz.insophy.retrobi.utils.library.SimpleSearchUtils;

/**
 * A HTML label that shows the best search fragment with highlighted results for
 * the given card and search settings. This label is being hidden if no
 * highlighting data are available.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SearchFragmentLabel extends HtmlLabel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * highlighter formatter start tag
     */
    private static final String HIGHLIGHT_PRE_TAG = "<strong>";
    /**
     * highlighter formatter end tag
     */
    private static final String HIGHLIGHT_POST_TAG = "</strong>";
    /**
     * default highlighter formatter
     */
    private static final Formatter FORMATTER = new Formatter() {
        @Override
        public String highlightTerm(final String originalText, final TokenGroup tokenGroup) {
            if (tokenGroup.getTotalScore() <= 0) {
                return originalText;
            }
            
            // compute buffer width
            
            final int prelen = SearchFragmentLabel.HIGHLIGHT_PRE_TAG.length();
            final int postlen = SearchFragmentLabel.HIGHLIGHT_POST_TAG.length();
            final int length = originalText.length() + prelen + postlen + 10;
            
            // generate buffer contents
            
            final StringBuilder returnBuffer = new StringBuilder(length);
            returnBuffer.append(SearchFragmentLabel.HIGHLIGHT_PRE_TAG);
            returnBuffer.append(HtmlLabel.escapeHtml(originalText));
            returnBuffer.append(SearchFragmentLabel.HIGHLIGHT_POST_TAG);
            return returnBuffer.toString();
        }
    };
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param card
     * card model
     * @param query
     * search query or <code>null</code> if none
     */
    public SearchFragmentLabel(final String id, final IModel<Card> card, final SearchQuery query) {
        this(id, new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (query == null) {
                    return "";
                }
                
                try {
                    final String highlight = SimpleSearchUtils.highlight(
                            SearchFragmentLabel.FORMATTER,
                            query.getIndex().getHighlightData(card.getObject()),
                            query.getQuery(),
                            query.isSensitive());
                    
                    if (highlight == null) {
                        return "";
                    }
                    
                    return highlight;
                }
                catch (final GeneralRepositoryException x) {
                    return "Chyba: " + x.getMessage();
                }
            }
        });
    }
    
    /**
     * Creates a new instance using the model specified.
     * 
     * @param id
     * component ID
     * @param model
     * data model
     */
    private SearchFragmentLabel(final String id, final IModel<String> model) {
        super(id, model);
    }
    
    @Override
    public boolean isVisible() {
        if ((this.getDefaultModel().getObject() == null) || (this.getDefaultModelObjectAsString().length() < 1)) {
            return false;
        }
        
        return super.isVisible();
    }
}
