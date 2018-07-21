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

import java.io.File;
import java.text.SimpleDateFormat;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with constants and other application settings. The configuration values
 * are either fixed or loaded from system properties.
 * 
 * @author Vojtěch Hordějčuk
 * @author Michal Rydlo
 */
public final class Settings {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);
    /**
     * DEBUG MODE
     */
    public static final boolean DEBUG;
    /**
     * database server URL
     */
    public static final String DB_HOST;
    /**
     * database server port
     */
    public static final int DB_PORT;
    /**
     * database name
     */
    public static final String DB_NAME;
    /**
     * CSV log output directory
     */
    public static final File CSV_LOG_DIRECTORY;
    /**
     * attribute definition file location
     */
    public static final File ATTRIBUTE_DEFINITION_FILE;
    /**
     * index naming file location
     */
    public static final File INDEX_NAMING_FILE;
    /**
     * image cache timeout (in ms)
     */
    public static final long IMAGE_CACHE_TIMEOUT;
    /**
     * daily maintenance start hour (0-24)
     */
    protected static final int MAINTENANCE_HOUR;
    /**
     * absolute server URL for use in e-mails - MUST be in format http://URL/
     */
    public static final String SERVER_URL_FOR_EMAIL;
    /**
     * number of cards considered "many" - useful while scheduling long tasks
     */
    public static final int MANY_CARDS;
    /**
     * number of cards considered "many" in basket - this is a limit for all
     * batch card modifications (memory limit)
     */
    public static final int MANY_BASKET_CARDS;
    /**
     * system source e-mail
     */
    public static final String SOURCE_EMAIL;
    /**
     * URL pointing to an external web statistics service
     */
    public static final String WEB_STATS_URL;
    /**
     * main central view design document ID
     */
    public static final String VIEW_DOCUMENT_ID = "_design/views";
    /**
     * fulltext view document ID
     */
    public static final String INDEX_DOCUMENT_ID = "_design/index";
    /**
     * attribute value index document ID
     */
    public static final String VALUE_INDEX_DOCUMENT_ID = "_design/values";
    /**
     * the default index name
     */
    public static final String DEFAULT_INDEX_NAME = "basic_ocr_best";
    /**
     * default basket size limit
     */
    public static final int DEFAULT_GUEST_BASKET_LIMIT = 20;
    /**
     * default user basket size limit
     */
    public static final int DEFAULT_USER_BASKET_LIMIT = 1000;
    /**
     * default cardset count limit
     */
    public static final int DEFAULT_GUEST_CARDSET_LIMIT = 10;
    /**
     * default user cardset count limit
     */
    public static final int DEFAULT_USER_CARDSET_LIMIT = 10;
    /**
     * a time before a next message can be sent by a guest (in ms)
     */
    public static final long GUEST_MESSAGE_TIMEOUT = 60000;
    /**
     * a time before a next message can be sent by an user (in ms)
     */
    public static final long USER_MESSAGE_TIMEOUT = 5000;
    /**
     * a limit for old messages to download or delete
     */
    public static final int OLD_MESSAGE_LIMIT = 5000;
    /**
     * long task thread pool size (the maximal number of threads that may
     * process all-users' long tasks at once)
     */
    public static final int TASK_POOL_SIZE = 10;
    /**
     * task queue update interval (in MS)
     */
    public static final int TASK_QUEUE_UPDATE = 3000;
    /**
     * e-mail subject prefix (useful for filtering)
     */
    public static final String RETROBI_SUBJECT_PREFIX = "[RETROBI] ";
    /**
     * ideal number of digits in the scanned files
     */
    public static final int SCAN_DIGIT_COUNT = 7;
    /**
     * maximal card image width (in database)
     */
    public static final int TARGET_IMAGE_WIDTH = 800;
    /**
     * maximal card image width (on website)
     */
    public static final int DISPLAY_IMAGE_WIDTH = 730;
    /**
     * maximal card preview image width (on website)
     */
    public static final int PREVIEW_IMAGE_WIDTH = 480;
    /**
     * preview image height crop ratio
     */
    public static final double CROP_RATIO = 2.2;
    /**
     * coefficient (%) that multiplies luminance of a paper color to produce a
     * text color luminance<br>
     * <b>note:</b> text luminance = paper luminance * (threshold/100)
     */
    public static final int DEFAULT_NONBLANK_DARK_THRESHOLD = 70;
    /**
     * minimal relative amount (%) of dark pixels on a page to be considered
     * non-blank<br>
     * <b>note:</b> "dark pixel" is a pixel with luminance < text luminance,
     * "non-blank page" is a page with (dark pixels/total pixels >
     * tolerance/100)
     */
    public static final int DEFAULT_NONBLANK_DARK_TOLERANCE = 10;
    /**
     * amount (%) of edge shaving (centered cropping) of a page before empty
     * detector is started - the given amount is a relative amount of lost space
     * in each dimension (e.g. 5% shave means 2.5% of the page is cropped from
     * each side)
     */
    public static final int DEFAULT_SHAVE = 4;
    /**
     * simple date format
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d.M.yyyy HH:mm:ss");
    /**
     * line separator (platform independent)
     */
    public static final String LINE_END = System.getProperty("line.separator");
    /**
     * note separator (if multiple notes)
     */
    public static final String NOTE_SEPARATOR = ";\n";
    /**
     * symbol for segmenting the OCR (note - update unit tests if changed)
     */
    public static final String SYMBOL_SEGMENT = "|";
    /**
     * symbol for segmenting the OCR encoded for use in Javascript
     */
    public static final String SYMBOL_SEGMENT_ENCODED = " | ";
    /**
     * default CSV file encoding (java.io)
     */
    public static final String CSV_ENCODING = "windows-1250";
    /**
     * CSV column separator (note - update unit tests if changed)
     */
    public static final String CSV_COLUMN = ";";
    /**
     * CSV row separator (note - update unit tests if changed)
     */
    public static final String CSV_ROW = "\n";
    /**
     * CSV quote (note - update unit tests if changed)
     */
    public static final String CSV_QUOTE = "\"";
    /**
     * CSV quote (note - update unit tests if changed)
     */
    public static final String CSV_QUOTE_ESCAPE = "\\\"";
    
    /**
     * Sets the default look and feel.
     */
    public static void setupLookAndFeel() {
        // nothing
    }
    
    /**
     * Do not allow to create instances.
     */
    private Settings() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns the property value from the JNDI environment or a default value
     * when the property is not defined.
     * 
     * @param key
     * property key
     * @param defaultValue
     * default value
     * @return JNDI property value
     */
    private static String getJndiProperty(final String key, final String defaultValue) {
        try {
            // obtain the application component's environment naming context
            
            final javax.naming.Context ctx = new javax.naming.InitialContext();
            final javax.naming.Context env = (javax.naming.Context) ctx.lookup("java:comp/env");
            
            // lookup the value
            
            return (String) env.lookup(key);
        } catch (final Exception x) {
            return defaultValue;
        }
    }
    
    /**
     * Tries to lookup a property in JNDI, then in system environment and
     * finally falls back to the default value. The system property name is the
     * same as the JNDI key with <code>"retrobi."</code> prefix.
     * 
     * @param key
     * property key
     * @param defaultValue
     * default value
     * @return JNDI property value
     */
    private static String getProperty(final String key, final String defaultValue) {
        return Settings.getJndiProperty(key, System.getProperty("retrobi." + key, defaultValue));
    }
    
    static {
        Settings.LOG.debug("Loading settings from the system properties...");
        
        // try to load the debug flag
        
        DEBUG = Boolean.valueOf(Settings.getProperty("debug", "false"));
        
        // setup the logger
        
        LogManager.getLoggerRepository().setThreshold(Settings.DEBUG ? Level.DEBUG : Level.INFO);
        
        // try to load connection settings from system properties
        
        DB_NAME = Settings.getProperty("db_name", "retrobi");
        DB_HOST = Settings.getProperty("db_host", "localhost");
        DB_PORT = Integer.valueOf(Settings.getProperty("db_port", "5984"));
        
        // load the other settings
        
        CSV_LOG_DIRECTORY = new File(Settings.getProperty("csv_log_dir", "/tmp/"));
        ATTRIBUTE_DEFINITION_FILE = new File(Settings.getProperty("attribute_file", "/tmp/attributes.json"));
        INDEX_NAMING_FILE = new File(Settings.getProperty("index_file", "/tmp/index.json"));
        IMAGE_CACHE_TIMEOUT = Long.valueOf(Settings.getProperty("image_cache_timeout", "86400000"));
        MAINTENANCE_HOUR = Integer.valueOf(Settings.getProperty("maintenance_hour", "3"));
        SERVER_URL_FOR_EMAIL = Settings.getProperty("server_url", "http://retrobi.ucl.cas.cz/");
        MANY_BASKET_CARDS = Integer.valueOf(Settings.getProperty("many_basket_cards", "35000"));
        MANY_CARDS = Integer.valueOf(Settings.getProperty("many_cards", "100"));
        WEB_STATS_URL = Settings.getProperty("web_stats_url", "http://retrobi.ucl.cas.cz/awstats/");
        SOURCE_EMAIL = Settings.getProperty("source_email", "retrobi@ucl.cas.cz");
        
        // log the settings used
        
        Settings.LOG.info("DEBUG mode = " + Settings.DEBUG);
        Settings.LOG.info("DB name = " + Settings.DB_NAME);
        Settings.LOG.info("DB host = " + Settings.DB_HOST);
        Settings.LOG.info("DB port = " + Settings.DB_PORT);
        Settings.LOG.info("CSV log directory = " + Settings.CSV_LOG_DIRECTORY.getAbsolutePath());
        Settings.LOG.info("attribute definition file = " + Settings.ATTRIBUTE_DEFINITION_FILE.getAbsolutePath());
        Settings.LOG.info("index naming file = " + Settings.INDEX_NAMING_FILE.getAbsolutePath());
        Settings.LOG.info("image cache timeout = " + Settings.IMAGE_CACHE_TIMEOUT);
        Settings.LOG.info("daily maintenance start hour = " + Settings.MAINTENANCE_HOUR);
        Settings.LOG.info("server URL = " + Settings.SERVER_URL_FOR_EMAIL);
    }
}
