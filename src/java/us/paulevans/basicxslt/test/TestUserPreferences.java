package us.paulevans.basicxslt.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;
import us.paulevans.basicxslt.AppConstants;
import us.paulevans.basicxslt.UserPreferences;

/**
 * Test case for UserPreferences class
 * @author pevans
 *
 */
public class TestUserPreferences extends TestCase {

        private static final String USERPREFSFILE_SYS_PROP = "preferencesfile";

        // instance member...
        private UserPreferences userPrefs;

        /**
         * Test fixture setup
         *
         */
        public void setUp() {
                userPrefs = new UserPreferences();
        }

        /**
         * Test fixture for setConfiguration(String,boolean)
         *
         */
        public void testSetConfiguration() {

                String defaultConfig;

                userPrefs.setConfiguration("test_config", true);
                assertEquals("test_config", userPrefs.getConfiguration());
                defaultConfig = userPrefs.getPropertyNoPrefix(
                                AppConstants.DEFAULT_CONFIGURATION_PROP);
                assertEquals("test_config", defaultConfig);

                // clear-out the default config...
                userPrefs.setConfiguration("", true);

                // call setConfiguration again...
                userPrefs.setConfiguration("new_test_config", false);
                defaultConfig = userPrefs.getPropertyNoPrefix(
                                AppConstants.DEFAULT_CONFIGURATION_PROP);
                assertFalse("new_test_config".equals(defaultConfig));
        }

        /**
         * Test fixture for getConfiguration()
         *
         */
        public void testGetConfiguration() {
                userPrefs.setConfiguration("test_config", true);
                assertEquals("test_config", userPrefs.getConfiguration());
        }

        /**
         * Test fixture for setPropertyNoPrefix(String,String)
         *
         */
        public void testSetPropertyNoPrefix() {
                userPrefs.setPropertyNoPrefix("some_key", "some_value");
                assertEquals("some_value",
                                userPrefs.getPropertyNoPrefix("some_key"));
                try {
                        userPrefs.setPropertyNoPrefix("some_prefix", null);
                        fail("should have thrown exception");
                } catch (NullPointerException e) {
                        // do nothing...
                }
                try {
                        userPrefs.setPropertyNoPrefix(null, "some_value");
                        fail("should have thrown exception");
                } catch (NullPointerException e) {
                        // do nothing...
                }
                try {
                        userPrefs.setPropertyNoPrefix(null, null);
                        fail("should have thrown exception");
                } catch (NullPointerException e) {
                        // do nothing...
                }
        }

        /**
         * Test fixture for setProperty(String,String)
         *
         */
        public void testSetProperty() {
                userPrefs.setConfiguration("test", true);
                userPrefs.setProperty("some_key", "some_value");
                assertEquals("some_value", userPrefs.getProperty("some_key"));
                try {
                        userPrefs.setProperty("some_key", null);
                        fail("should have thrown exception");
                } catch (NullPointerException e) {
                        // do nothing...
                }
                try {
                        userPrefs.setProperty(null, "some_value");
                        fail("should have thrown exception");
                } catch (NullPointerException e) {
                        // do nothing...
                }
                try {
                        userPrefs.setProperty(null, null);
                        fail("should have thrown exception");
                } catch (NullPointerException e) {
                        // do nothing...
                }
        }

        /**
         * Test fixture for getProperty(String)
         *
         */
        public void testGetProperty() {
                userPrefs.setConfiguration("test_config", true);
                userPrefs.setProperty("some_key", "some_value");
                assertEquals("some_value", userPrefs.getProperty("some_key"));
        }

        /**
         * Test fixture for getProperty(String,String)
         *
         */
        public void testGetPropertyStringString() {
                userPrefs.setConfiguration("test_config", true);
                userPrefs.setProperty("some_key", "some_value");
                assertEquals("some_value", userPrefs.getProperty("some_key"));
                assertEquals("some_default_value",
                                userPrefs.getProperty("another_key", "some_default_value"));
        }

        /**
         * Test fixture for getPropertyNoPrefix(String)
         *
         */
        public void testGetPropertyNoPrefix() {
                userPrefs.setPropertyNoPrefix("some_key", "some_value");
                assertEquals("some_value", userPrefs.getPropertyNoPrefix("some_key"));
        }

        /**
         * Test fixture for getAllConfigurations()
         *
         */
        public void _testGetAllConfigurations() throws FileNotFoundException,
        IOException {

                String configs[];

                // load test user preferences...
                userPrefs.load(new FileInputStream(new File(System.getProperty(
                                USERPREFSFILE_SYS_PROP))));

                // set the current configuration...
                userPrefs.setConfiguration(AppConstants.DEFAULT_CONFIGURATION, true);

                // get the configurations...
                configs = userPrefs.getAllConfigurations();
                assertEquals(2, configs.length);
        }

        /**
         * Test fixture for clearDynamicPrefs()
         *
         */
        public void _testClearDynamicPrefs() throws FileNotFoundException,
        IOException {

                int totalNumProps;

                // load test user preferences...
                userPrefs.load(new FileInputStream(new File(System.getProperty(
                                USERPREFSFILE_SYS_PROP))));

                // set the configuration configuration...
                userPrefs.setConfiguration("cdjdn-demo", true);

                // get the total number of props...
                totalNumProps = userPrefs.size();

                // clear the dynamic prefs...
                userPrefs.clearDynamicPrefs();

                // assert that 26 dyanmic prefs have been removed...
                assertEquals(totalNumProps - 26, userPrefs.size());
        }

        /**
         * Test fixture for copyCurrentPreferences(String)
         *
         */
        public void _testCopyCurrentPreferences() throws FileNotFoundException,
        IOException {

                String newConfig;
                int totalNumProps;

                // load test user preferences...
                userPrefs.load(new FileInputStream(new File(System.getProperty(
                                USERPREFSFILE_SYS_PROP))));
                newConfig = "new_config";
                userPrefs.setConfiguration("cdjdn-demo", true);

                // get the existing number of properties...
                totalNumProps = userPrefs.size();

                // copy the "cdjdn-demo" properties into "new_config"...
                userPrefs.copyCurrentPreferences(newConfig);

                // assert that 46 more properties now exists...
                assertEquals(totalNumProps + 46, userPrefs.size());
        }

        /**
         * Test fixture for loadDefaultConfiguration()
         *
         */
        public void testLoadDefaultConfiguration() {
                assertEquals(AppConstants.DEFAULT_CONFIGURATION,
                                userPrefs.loadDefaultConfiguration());

                // set the default configuration...
                userPrefs.setConfiguration("new_default", true);
                assertEquals("new_default", userPrefs.loadDefaultConfiguration());
        }

        /**
         * Test fixture for persistUserPrefs()
         *
         */
        public void testPersistUserPrefs() {
                // will not unit test since this activity writes a file to the
                // user's home folder - this is hard-coded.  This method has been
                // functionally-tested though...
        }
}
