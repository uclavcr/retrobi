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

package cz.insophy.retrobi;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.pagestore.DiskPageStore;
import org.apache.wicket.protocol.http.request.InvalidUrlException;
import org.apache.wicket.request.target.basic.StringRequestTarget;
import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.session.pagemap.LeastRecentlyAccessedEvictionStrategy;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.DatabaseConnector;
import cz.insophy.retrobi.database.entity.type.AbstractCardIndex;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.pages.AboutPage;
import cz.insophy.retrobi.pages.AccessDeniedErrorPage;
import cz.insophy.retrobi.pages.AdminCardsPage;
import cz.insophy.retrobi.pages.AdminMessagesPage;
import cz.insophy.retrobi.pages.AdminReportsPage;
import cz.insophy.retrobi.pages.AdminTextEditorPage;
import cz.insophy.retrobi.pages.AdminTextsPage;
import cz.insophy.retrobi.pages.AdminToolsPage;
import cz.insophy.retrobi.pages.AdminUsersPage;
import cz.insophy.retrobi.pages.AutologinPage;
import cz.insophy.retrobi.pages.BasketPage;
import cz.insophy.retrobi.pages.BrowserPage;
import cz.insophy.retrobi.pages.CatalogPage;
import cz.insophy.retrobi.pages.ExpiredErrorPage;
import cz.insophy.retrobi.pages.HelpPage;
import cz.insophy.retrobi.pages.IndexPage;
import cz.insophy.retrobi.pages.LetterPage;
import cz.insophy.retrobi.pages.LostPasswordPage;
import cz.insophy.retrobi.pages.MessagePage;
import cz.insophy.retrobi.pages.NotFoundErrorPage;
import cz.insophy.retrobi.pages.ProfilePage;
import cz.insophy.retrobi.pages.RegisterPage;
import cz.insophy.retrobi.pages.RuntimeErrorPage;
import cz.insophy.retrobi.pages.SearchPage;
import cz.insophy.retrobi.pages.StatsPage;
import cz.insophy.retrobi.utils.CSVHistoryLogger;
import cz.insophy.retrobi.utils.resource.CardImageResource;
import cz.insophy.retrobi.utils.resource.CardImageResourceReference;
import cz.insophy.retrobi.utils.resource.JnlpResource;

/**
 * Main application class. It initializes and terminates the whole application
 * and does all necessary operations - manages database connection, user session
 * and shared application resources. It also provides a method for accessing
 * main database object.
 * 
 * @author Vojtěch Hordějčuk
 */
public class RetrobiWebApplication extends WebApplication {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(RetrobiWebApplication.class);
    /**
     * CSV logger instance
     */
    private static final CSVHistoryLogger CSV_LOG = CSVHistoryLogger.getInstance();
    /**
     * long task thread pool size
     */
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(Settings.TASK_POOL_SIZE);
    /**
     * maintenance timer (periodically doing the maintenance)
     */
    private static final Timer MAINTENANCE_TIMER = new Timer("Maintenance timer");
    /**
     * the last maintenance start time
     */
    private static long LAST_MAINTENANCE_START = 0l;
    /**
     * the last maintenance end time
     */
    private static long LAST_MAINTENANCE_END = 0l;
    /**
     * the next maintenance start time
     */
    private static long NEXT_MAINTENANCE_START = 0l;
    /**
     * URL parameter: catalog
     */
    public static final String PARAM_CATALOG = "cast";
    /**
     * URL parameter: catalog letter
     */
    public static final String PARAM_LETTER = "pismeno";
    /**
     * URL parameter: batch
     */
    public static final String PARAM_BATCH = "skupina";
    /**
     * URL parameter: card ID
     */
    public static final String PARAM_CARD = "listek";
    /**
     * URL parameter: image name
     */
    public static final String PARAM_IMAGE = "obrazek";
    /**
     * URL parameter: text type
     */
    public static final String PARAM_TEXT = "text";
    /**
     * URL parameter: image width
     */
    public static final String PARAM_WIDTH = "sirka";
    /**
     * URL parameter: crop flag
     */
    public static final String PARAM_CROP = "orez";
    /**
     * URL parameter: auto-login login
     */
    public static final String PARAM_AUTO_LOGIN = "login";
    /**
     * URL parameter: auto-login password
     */
    public static final String PARAM_AUTO_PASSWORD_HASH = "heslo";

    // ========
    // SETTINGS
    // ========
    
    @Override
    public Class<IndexPage> getHomePage() {
        return IndexPage.class;
    }
    
    @Override
    public String getConfigurationType() {
        if (Settings.DEBUG) {
            return Application.DEVELOPMENT;
        }
        
        return Application.DEPLOYMENT;
    }
    
    // ==============
    // INITIALIZATION
    // ==============
    
    @Override
    public Session newSession(final Request request, final Response response) {
        RetrobiWebApplication.LOG.debug("Creating a new session...");
        
        // create a new session
        
        return new RetrobiWebSession(request);
    }
    
    @Override
    public RequestCycle newRequestCycle(final Request request, final Response response) {
        return new WebRequestCycle(this, (WebRequest) request, (WebResponse) response) {
            @Override
            protected void onBeginRequest() {
                super.onBeginRequest();
                
                RetrobiLocker.MAINTENANCE_LOCK.lock();
                
                try {
                    if (RetrobiWebApplication.LAST_MAINTENANCE_START > RetrobiWebApplication.LAST_MAINTENANCE_END) {
                        // if the maintenance is running, end the request NOW
                        
                        this.setRequestTarget(new StringRequestTarget("Probiha systemova udrzba [MAINTENANCE], zkuste to prosim pozdeji."));
                    }
                } finally {
                    RetrobiLocker.MAINTENANCE_LOCK.unlock();
                }
            }
            
            @Override
            public Page onRuntimeException(final Page page, final RuntimeException e) {
                if (e instanceof PageExpiredException) {
                    // page expired = not so important exception
                    
                    RetrobiWebApplication.LOG.debug(e.getMessage());
                    return new ExpiredErrorPage(this.getPageParameters());
                } else if (e instanceof InvalidUrlException) {
                    // invalid URL = not so important exception
                    
                    RetrobiWebApplication.LOG.info(e.getMessage());
                    return new ExpiredErrorPage(this.getPageParameters());
                } else {
                    // other runtime exception - serious stuff!
                    
                    RetrobiWebApplication.LOG.error(String.format(
                            "%s: %s (page = %s)",
                            e.getClass().getSimpleName(),
                            e.getMessage(),
                            page), e);
                    
                    return new RuntimeErrorPage(this.getPageParameters(), e);
                }
            }
        };
    }
    
    @Override
    protected ISessionStore newSessionStore() {
        // the first level is a server memory
        // the second level is a disk
        
        return new SecondLevelCacheSessionStore(this, new DiskPageStore(
                (int) Bytes.megabytes(32).bytes(),
                (int) Bytes.megabytes(128).bytes(), 50));
    }
    
    @Override
    protected void init() {
        // setup the application
        
        RetrobiWebApplication.LOG.info("Setting up the application...");
        
        this.getMarkupSettings().setDefaultMarkupEncoding("utf-8");
        this.getMarkupSettings().setCompressWhitespace(!Settings.DEBUG);
        this.getMarkupSettings().setStripWicketTags(!Settings.DEBUG);
        this.getMarkupSettings().setStripComments(!Settings.DEBUG);
        this.getPageSettings().setVersionPagesByDefault(false);
        this.getPageSettings().setAutomaticMultiWindowSupport(true);
        this.getSessionSettings().setMaxPageMaps(10);
        this.getSessionSettings().setPageIdUniquePerSession(true);
        this.getSessionSettings().setPageMapEvictionStrategy(new LeastRecentlyAccessedEvictionStrategy(1));
        this.getSecuritySettings().setEnforceMounts(true);
        this.getResourceSettings().setDefaultCacheDuration((int) (Math.min(1, Settings.IMAGE_CACHE_TIMEOUT / 1000)));
        this.getResourceSettings().setThrowExceptionOnMissingResource(Settings.DEBUG);
        this.getResourceSettings().setUseDefaultOnMissingResource(true);
        this.getSharedResources().setThrowExceptionIfNotMapped(false);
        this.getRequestCycleSettings().setBufferResponse(true);
        this.getRequestCycleSettings().setTimeout(Duration.ONE_MINUTE);
        this.getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.REDIRECT_TO_BUFFER);
        this.getApplicationSettings().setDefaultMaximumUploadSize(Bytes.megabytes(32));
        
        RetrobiWebApplication.LOG.info("Starting the application...");
        
        // mount shared resources
        
        this.getSharedResources().putClassAlias(Application.class, "retrobi");
        this.createJnlpResource("processor", "Nástroj pro zpracování obrázků");
        this.createJnlpResource("importer", "Nástroj pro import lístků");
        this.createImageResources();
        
        // mount pages (for nicer URLs)
        
        RetrobiWebApplication.LOG.info("Mounting pages...");
        
        this.mount(new HybridUrlCodingStrategy("schranka", BasketPage.class));
        this.mount(new HybridUrlCodingStrategy("katalog", BrowserPage.class));
        this.mount(new HybridUrlCodingStrategy("domu", IndexPage.class));
        this.mount(new HybridUrlCodingStrategy("rejstrik", CatalogPage.class));
        this.mount(new HybridUrlCodingStrategy("skupiny", LetterPage.class));
        this.mount(new HybridUrlCodingStrategy("hledat", SearchPage.class));
        
        this.mount(new HybridUrlCodingStrategy("statistiky", StatsPage.class));
        this.mount(new HybridUrlCodingStrategy("projekt", AboutPage.class));
        this.mount(new HybridUrlCodingStrategy("napoveda", HelpPage.class));
        this.mount(new HybridUrlCodingStrategy("hlaseni", MessagePage.class));
        
        this.mount(new HybridUrlCodingStrategy("profil", ProfilePage.class));
        this.mount(new HybridUrlCodingStrategy("registrace", RegisterPage.class));
        this.mount(new HybridUrlCodingStrategy("autologin", AutologinPage.class));
        this.mount(new HybridUrlCodingStrategy("heslo", LostPasswordPage.class));
        
        this.mount(new HybridUrlCodingStrategy("chyba-nenalezeno", NotFoundErrorPage.class));
        this.mount(new HybridUrlCodingStrategy("chyba-autorizace", AccessDeniedErrorPage.class));
        this.mount(new HybridUrlCodingStrategy("chyba-timeout", ExpiredErrorPage.class));
        this.mount(new HybridUrlCodingStrategy("chyba-systemova", RuntimeErrorPage.class));
        
        this.mount(new HybridUrlCodingStrategy("admin-listky", AdminCardsPage.class));
        this.mount(new HybridUrlCodingStrategy("admin-hlaseni", AdminMessagesPage.class));
        this.mount(new HybridUrlCodingStrategy("admin-analyzy", AdminReportsPage.class));
        this.mount(new HybridUrlCodingStrategy("admin-texty", AdminTextsPage.class));
        this.mount(new HybridUrlCodingStrategy("admin-text", AdminTextEditorPage.class));
        this.mount(new HybridUrlCodingStrategy("admin-nastroje", AdminToolsPage.class));
        this.mount(new HybridUrlCodingStrategy("admin-uzivatele", AdminUsersPage.class));
        
        // setup error pages
        
        this.getApplicationSettings().setPageExpiredErrorPage(ExpiredErrorPage.class);
        this.getApplicationSettings().setAccessDeniedPage(AccessDeniedErrorPage.class);
        this.getApplicationSettings().setInternalErrorPage(RuntimeErrorPage.class);
        
        // ensure the DB with all repositories is ready
        
        RetrobiWebApplication.LOG.info("Ensuring DB is connected...");
        
        RetrobiApplication.db();
        
        // update design documents
        
        RetrobiWebApplication.LOG.info("Updating design documents...");
        
        try {
            RetrobiWebConfiguration.getInstance().reloadAttributeTree();
            RetrobiWebConfiguration.getInstance().updateDesignDocuments(false);
        } catch (final GeneralRepositoryException x) {
            RetrobiWebApplication.LOG.error("Could not initialize the attribute tree: " + x.getMessage());
        } catch (final IOException x) {
            RetrobiWebApplication.LOG.error("I/O exception during attribute tree initialization: " + x.getMessage());
        }
        
        RetrobiWebApplication.LOG.info("Application started.");
        
        // start the maintenance timer thread right away
        
        RetrobiWebApplication.MAINTENANCE_TIMER.schedule(RetrobiWebApplication.createNextMaintenanceTask(
                RetrobiWebApplication.MAINTENANCE_TIMER),
                new Date());
    }
    
    @Override
    protected void onDestroy() {
        RetrobiWebApplication.LOG.info("Destroying Retrobi...");
        
        RetrobiLocker.THREAD_POOL_LOCK.lock();
        
        try {
            RetrobiWebApplication.THREAD_POOL.shutdown();
        } finally {
            RetrobiLocker.THREAD_POOL_LOCK.unlock();
        }
        
        RetrobiLocker.MAINTENANCE_LOCK.lock();
        
        try {
            RetrobiWebApplication.MAINTENANCE_TIMER.cancel();
            RetrobiWebApplication.MAINTENANCE_TIMER.purge();
        } finally {
            RetrobiLocker.MAINTENANCE_LOCK.unlock();
        }
        
        RetrobiWebApplication.LOG.info("Retrobi destroyed. GOOD BYE!");
    }
    
    /**
     * Creates a JNLP resource and binds it to a fixed URL in the root of the
     * application context. All resources are available as "NAME.jnlp".
     * 
     * @param name
     * resource name
     * @param title
     * resource title
     */
    private void createJnlpResource(final String name, final String title) {
        // generate name and key
        
        final String resourceName = name + ".jnlp";
        final String resourceKey = Application.class.getName() + "/jnlp/" + resourceName;
        
        RetrobiWebApplication.LOG.debug(String.format("Creating and mounting JNLP resource '%s' to '%s'...", resourceName, resourceKey));
        
        // share and mount the resource
        
        this.getSharedResources().add(resourceName, new JnlpResource(name, title));
        this.mountSharedResource(resourceName, resourceKey);
    }
    
    /**
     * Creates image resources and mounts them as shared resources.
     */
    private void createImageResources() {
        this.getSharedResources().add(CardImageResourceReference.REFERENCE_NAME, new CardImageResource());
    }
    
    // ==========
    // CSV LOGGER
    // ==========
    
    /**
     * Returns the CSV history logger.
     * 
     * @return the CSV logger
     */
    public static CSVHistoryLogger getCSVLogger() {
        return RetrobiWebApplication.CSV_LOG;
    }
    
    // =====================
    // LONG TASK THREAD POOL
    // =====================
    
    /**
     * Executes the given runnable in the thread pool. When all tasks in the
     * pool are busy, the task is added into the queue.
     * 
     * @param runnable
     * runnable to execute
     */
    public static void executeInThreadPool(final Runnable runnable) {
        RetrobiLocker.THREAD_POOL_LOCK.lock();
        
        try {
            RetrobiWebApplication.THREAD_POOL.execute(runnable);
        } finally {
            RetrobiLocker.THREAD_POOL_LOCK.unlock();
        }
    }
    
    // ===========
    // MAINTENANCE
    // ===========
    
    /**
     * Returns the last maintenance start time (or zero if none).
     * 
     * @return the last maintenance start time (or zero)
     */
    public static long getLastMaintenanceStart() {
        RetrobiLocker.MAINTENANCE_LOCK.lock();
        
        try {
            return RetrobiWebApplication.LAST_MAINTENANCE_START;
        } finally {
            RetrobiLocker.MAINTENANCE_LOCK.unlock();
        }
    }
    
    /**
     * Returns the last maintenance end time (or zero if none).
     * 
     * @return the last maintenance end time (or zero)
     */
    public static long getLastMaintenanceEnd() {
        RetrobiLocker.MAINTENANCE_LOCK.lock();
        
        try {
            return RetrobiWebApplication.LAST_MAINTENANCE_END;
        } finally {
            RetrobiLocker.MAINTENANCE_LOCK.unlock();
        }
    }
    
    /**
     * Returns the next maintenance start time (or zero if none).
     * 
     * @return the next maintenance start time (or zero)
     */
    public static long getNextMaintenanceStart() {
        RetrobiLocker.MAINTENANCE_LOCK.lock();
        
        try {
            return RetrobiWebApplication.NEXT_MAINTENANCE_START;
        } finally {
            RetrobiLocker.MAINTENANCE_LOCK.unlock();
        }
    }
    
    /**
     * Starts the default maintenance tasks + web maintenance tasks.
     */
    private static void maintenance() {
        RetrobiLocker.MAINTENANCE_LOCK.lock();
        
        try {
            RetrobiWebApplication.LAST_MAINTENANCE_START = System.currentTimeMillis();
        } finally {
            RetrobiLocker.MAINTENANCE_LOCK.unlock();
        }
        
        // reload attribute tree and indexes
        
        try {
            RetrobiWebApplication.LOG.info("MAINTENANCE: Reloading attribute tree...");
            RetrobiWebConfiguration.getInstance().reloadAttributeTree();
            RetrobiWebApplication.LOG.info("MAINTENANCE: Updating design documents...");
            RetrobiWebConfiguration.getInstance().updateDesignDocuments(true);
        } catch (final Exception x) {
            RetrobiWebApplication.LOG.error(x.getMessage(), x);
        }
        
        // ping all views
        
        try {
            RetrobiWebApplication.LOG.info("MAINTENANCE: Pinging views...");
            DatabaseConnector.getInstance().pingAllViews();
        } catch (final Exception x) {
            RetrobiWebApplication.LOG.error(x.getMessage(), x);
        }
        
        // update the catalog model
        
        try {
            RetrobiWebApplication.LOG.info("MAINTENANCE: Updating catalog model...");
            CardCatalogModel.getInstance().update();
        } catch (final Exception x) {
            RetrobiWebApplication.LOG.error(x.getMessage(), x);
        }
        
        // cleanup index
        
        try {
            RetrobiWebApplication.LOG.info("MAINTENANCE: Cleaning up index...");
            RetrobiApplication.db().getCardSearchRepository().cleanupIndex();
        } catch (final Exception x) {
            RetrobiWebApplication.LOG.error(x.getMessage(), x);
        }
        
        // run test search to all indexes
        
        RetrobiWebApplication.LOG.info("MAINTENANCE: Optimising and testing search indexes...");
        
        for (final AbstractCardIndex index : RetrobiWebConfiguration.getInstance().getIndexes()) {
            try {
                RetrobiApplication.db().getCardSearchRepository().ping(index);
                RetrobiApplication.db().getCardSearchRepository().optimize(index);
            } catch (final Exception x) {
                RetrobiWebApplication.LOG.error(x.getMessage());
            }
        }
        
        RetrobiWebApplication.LOG.info("MAINTENANCE: All done!");
        
        RetrobiLocker.MAINTENANCE_LOCK.lock();
        
        try {
            RetrobiWebApplication.LAST_MAINTENANCE_END = System.currentTimeMillis();
        } finally {
            RetrobiLocker.MAINTENANCE_LOCK.unlock();
        }
    }
    
    /**
     * Creates the next maintenance timer task. The task will be scheduled for
     * execution on the next day, the hour is set according to the settings.
     * 
     * @param timer
     * timer to be used for scheduling
     * @return a timer task for the next maintenance
     */
    private static TimerTask createNextMaintenanceTask(final Timer timer) {
        return new TimerTask() {
            @Override
            public void run() {
                // do the maintenance
                
                RetrobiWebApplication.maintenance();
                
                // create the next maintenance date
                
                final Calendar nextDate = Calendar.getInstance();
                nextDate.set(Calendar.HOUR_OF_DAY, Settings.MAINTENANCE_HOUR);
                nextDate.set(Calendar.MINUTE, 0);
                nextDate.set(Calendar.SECOND, 0);
                nextDate.set(Calendar.MILLISECOND, 0);
                nextDate.add(Calendar.DATE, 1);
                
                RetrobiLocker.MAINTENANCE_LOCK.lock();
                
                try {
                    // save the next maintenance time
                    
                    RetrobiWebApplication.NEXT_MAINTENANCE_START = nextDate.getTimeInMillis();
                    
                    // schedule the next maintenance
                    
                    timer.schedule(
                            RetrobiWebApplication.createNextMaintenanceTask(timer),
                            new Date(RetrobiWebApplication.NEXT_MAINTENANCE_START));
                } finally {
                    RetrobiLocker.MAINTENANCE_LOCK.unlock();
                }
            }
        };
    }
}
