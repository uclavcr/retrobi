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

package cz.insophy.retrobi.panel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Resource;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.ContextRelativeResource;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Message;
import cz.insophy.retrobi.database.entity.type.MessageStateOption;
import cz.insophy.retrobi.database.entity.type.MessageType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.link.BookmarkableCardLink;
import cz.insophy.retrobi.link.ConfirmMessageLink;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.component.ClassSwitcher;
import cz.insophy.retrobi.utils.component.PagedLazyListView;
import cz.insophy.retrobi.utils.component.TagTitleAppender;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Message list panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class MessageListPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * message list view
     */
    private final MessageListView list;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public MessageListPanel(final String id) {
        super(id);
        
        // create components
        
        this.list = new MessageListView("list");
        
        // place components
        
        this.add(this.list);
    }
    
    /**
     * Returns the paged view.
     * 
     * @return the paged view
     */
    public PagedLazyListView<?> getPagedView() {
        return this.list;
    }
    
    /**
     * Sets the card ID filter.
     * 
     * @param value
     * card ID filter value
     */
    public void setCardId(final String value) {
        this.list.setCardId(value);
    }
    
    /**
     * Sets the state filter.
     * 
     * @param value
     * state filter value
     */
    public void setState(final MessageStateOption value) {
        this.list.setState(value);
    }
    
    /**
     * Sets the type filter.
     * 
     * @param value
     * type filter value
     */
    public void setType(final MessageType value) {
        this.list.setType(value);
    }
    
    /**
     * Sets the user filter.
     * 
     * @param value
     * user filter value
     */
    public void setUser(final String value) {
        this.list.setUser(value);
    }
    
    /**
     * Resets the view settings.
     */
    public void reset() {
        this.list.reset();
    }
    
    /**
     * Message list view for viewing the events / user messages, for example in
     * the administration mode.
     * 
     * @author Vojtěch Hordějčuk
     */
    private static class MessageListView extends PagedLazyListView<Message> {
        /**
         * default serial version
         */
        private static final long serialVersionUID = 1L;
        /**
         * card ID filter value
         */
        private String cardIdFilter;
        /**
         * state filter value
         */
        private MessageStateOption stateFilter;
        /**
         * type filter value
         */
        private MessageType typeFilter;
        /**
         * user filter value
         */
        private String userFilter;
        
        /**
         * Creates a new instance.
         * 
         * @param id
         * component ID
         */
        public MessageListView(final String id) {
            super(id, 10);
            
            // initialize filter
            
            this.cardIdFilter = null;
            this.stateFilter = MessageStateOption.UNCONFIRMED_ALL;
            this.typeFilter = null;
        }
        
        /**
         * Sets the card ID and invalidates cache.
         * 
         * @param value
         * the card ID
         */
        public void setCardId(final String value) {
            this.cardIdFilter = value;
            this.invalidateCache();
        }
        
        /**
         * Sets the state filter and invalidates cache.
         * 
         * @param value
         * the state
         */
        public void setState(final MessageStateOption value) {
            this.stateFilter = value;
            this.invalidateCache();
        }
        
        /**
         * Sets the type filter and invalidates cache.
         * 
         * @param value
         * the type
         */
        public void setType(final MessageType value) {
            this.typeFilter = value;
            this.invalidateCache();
        }
        
        /**
         * Sets the user filter and invalidates cache.
         * 
         * @param value
         * the user
         */
        public void setUser(final String value) {
            this.userFilter = SimpleStringUtils.emptyToNull(value);
            this.invalidateCache();
        }
        
        @Override
        protected List<? extends Message> getFreshList(final int page, final int limit) {
            try {
                if (this.cardIdFilter != null) {
                    // load by card, ignore the rest
                    
                    final List<String> ids;
                    
                    switch (this.stateFilter) {
                        case UNCONFIRMED_EVENTS:
                            ids = RetrobiApplication.db().getMessageRepository().getEventMessageIds(this.cardIdFilter);
                            break;
                        case UNCONFIRMED_PROBLEMS:
                            ids = RetrobiApplication.db().getMessageRepository().getProblemMessageIds(this.cardIdFilter);
                            break;
                        default:
                            this.error("Tento režim hlášení není pro jeden lístek podporován.");
                            ids = Collections.emptyList();
                            break;
                    }
                    
                    return RetrobiApplication.db().getMessageRepository().getMessages(ids);
                }
                
                Tuple<Integer, List<String>> result = Tuple.of(0, (List<String>) new LinkedList<String>());
                
                if (this.stateFilter == null) {
                    // state filter is empty
                    
                    this.error("Zvolte prosím, zda chcete procházet uzavřená či otevřená hlášení.");
                } else if ((this.userFilter != null) && (this.typeFilter != null)) {
                    // invalid filter setting, fallback
                    
                    this.error("Filtrovat je možné vždy jen podle jednoho kritéria.");
                } else {
                    if (this.userFilter == null) {
                        // filter by type
                        
                        final boolean emptyFilters = ((this.typeFilter == null) || (this.stateFilter == null));
                        final boolean compatibleFilters = (!emptyFilters && this.stateFilter.isCompatible(this.typeFilter));
                        
                        if (emptyFilters || compatibleFilters) {
                            result = RetrobiApplication.db().getMessageRepository().getMessageIdsByType(
                                    this.stateFilter,
                                    this.typeFilter,
                                    page,
                                    limit);
                        } else {
                            this.error("Nesprávně nastavený filtr - neplatná kombinace událostí a hlášení.");
                        }
                    } else {
                        // filter by user
                        
                        result = RetrobiApplication.db().getMessageRepository().getMessageIdsByUser(
                                this.stateFilter,
                                this.userFilter,
                                page,
                                limit);
                    }
                }
                
                // update the pager
                
                this.reset(result.getFirst());
                
                // return the list of results
                
                final List<Message> messages = RetrobiApplication.db().getMessageRepository().getMessages(result.getSecond());
                return Collections.unmodifiableList(messages);
            } catch (final NotFoundRepositoryException x) {
                this.error(x.getMessage());
            } catch (final GeneralRepositoryException x) {
                this.error(x.getMessage());
            }
            
            return Collections.emptyList();
        }
        
        @Override
        protected void populateItem(final ListItem<Message> item) {
            // create models
            
            final IModel<String> userLabelModel = new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public String getObject() {
                    if (item.getModelObject().getUserName() != null) {
                        return item.getModelObject().getUserName();
                    }
                    
                    return "(bez uživatele)";
                }
            };
            
            final IModel<String> bodyLabelModel = new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public String getObject() {
                    if (SimpleStringUtils.isEmpty(item.getModelObject().getBody())) {
                        return "(bez textu)";
                    }
                    
                    return item.getModelObject().getBody();
                }
            };
            
            final IModel<String> subjectLabelModel = new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public String getObject() {
                    return item.getModelObject().getType().toString();
                }
            };
            
            final IModel<String> dateLabelModel = new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public String getObject() {
                    return item.getModelObject().getAdded().toString();
                }
            };
            
            // create components
            
            final Label dateLabel = new Label("label.date", dateLabelModel);
            final Label subjectLabel = new Label("label.subject", subjectLabelModel);
            
            final Label userLabel = new Label("label.user", userLabelModel) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public boolean isVisible() {
                    if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
                        return false;
                    }
                    
                    return super.isVisible();
                }
            };
            
            final Label bodyLabel = new Label("label.body", bodyLabelModel) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public boolean isVisible() {
                    if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
                        if (item.getModelObject().getBody() == null) {
                            return false;
                        }
                        
                        if (item.getModelObject().getBody().equals(item.getModelObject().getType().getTemplate())) {
                            return false;
                        }
                    }
                    
                    return super.isVisible();
                }
            };
            
            final WebMarkupContainer confirmLink = this.getConfirmLink(item.getModel());
            final WebMarkupContainer cardLink = this.getCardLink(item.getModel());
            
            // place components
            
            item.add(dateLabel);
            item.add(subjectLabel);
            item.add(confirmLink);
            item.add(bodyLabel);
            item.add(cardLink);
            item.add(userLabel);
            
            // create class appender for unconfirmed messages
            
            item.add(new ClassSwitcher("unconfirmed", "confirmed", new AbstractReadOnlyModel<Boolean>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public Boolean getObject() {
                    return item.getModelObject().getConfirmedByUserId() != null;
                }
            }));
        }
        
        /**
         * Returns a link that shows the card in the message.
         * 
         * @param message
         * message
         * @return a link
         */
        private WebMarkupContainer getCardLink(final IModel<Message> message) {
            final WebMarkupContainer link;
            
            if (message.getObject().getCardId() != null) {
                link = new Link<Object>("link.card") {
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public void onClick() {
                        final BookmarkablePageLink<?> temp = new BookmarkableCardLink("TEMP", message.getObject().getCardId());
                        RetrobiWebSession.get().getCardView().setCardViewMode(CardViewMode.MESSAGE);
                        throw new RestartResponseException(temp.getPageClass(), temp.getPageParameters());
                    }
                    
                    @Override
                    public boolean isVisible() {
                        if (message.getObject().getCardId() == null) {
                            return false;
                        }
                        
                        return super.isVisible();
                    }
                };
            } else {
                link = new WebMarkupContainer("link.card");
            }
            
            final Label cardLabel = new Label("label.card", new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public String getObject() {
                    if (message.getObject().getCardName() != null) {
                        return message.getObject().getCardName();
                    }
                    
                    return "";
                }
            });
            
            final Label imageLabel = new Label("label.image", new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public String getObject() {
                    if (message.getObject().getImageName() != null) {
                        return message.getObject().getImageName();
                    }
                    
                    return "";
                }
            });
            
            link.add(cardLabel);
            link.add(imageLabel);
            
            return link;
        }
        
        /**
         * Returns the confirmation link.
         * 
         * @param message
         * message to confirm
         * @return a link
         */
        private WebMarkupContainer getConfirmLink(final IModel<Message> message) {
            final WebMarkupContainer link = new ConfirmMessageLink("link.confirm", message) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void onClick() {
                    super.onClick();
                    
                    // refresh the message list model
                    
                    MessageListView.this.invalidateCache();
                }
                
                @Override
                public boolean isVisible() {
                    if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
                        return false;
                    }
                    
                    return super.isVisible();
                }
            };
            
            final Component confirmIcon = new Image("image", new AbstractReadOnlyModel<Resource>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public Resource getObject() {
                    if (message.getObject().getConfirmedByUserId() != null) {
                        return new ContextRelativeResource("icon_lock24.png");
                    }
                    
                    return new ContextRelativeResource("icon_unlock24.png");
                }
            });
            
            link.add(confirmIcon);
            
            link.add(new TagTitleAppender(new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public String getObject() {
                    if (message.getObject().getConfirmedByUserId() == null) {
                        return "převést do stavu HOTOVO";
                    }
                    
                    return "převést do stavu NEVYŘÍZENO";
                }
            }));
            
            return link;
        }
    }
}
