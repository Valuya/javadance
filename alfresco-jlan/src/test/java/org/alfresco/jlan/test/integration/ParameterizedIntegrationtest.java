package org.alfresco.jlan.test.integration;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Method;

import static org.testng.Assert.*;
import org.testng.ITestContext;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.jlan.client.DiskSession;
import org.alfresco.jlan.client.SessionFactory;
import org.alfresco.jlan.client.SessionSettings;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.smb.PCShare;

public class ParameterizedIntegrationtest {

    protected static final Logger LOGGER = LoggerFactory
        .getLogger(ParameterizedIntegrationtest.class);

    private static String m_host;
    private static String m_user;
    private static String m_pass;
    private static String m_share;
    private static Integer m_nbport;
    private static Integer m_cifsport;
    private static DiskSession sess;

    private String testname;
    private int tId;
    private String m_path;
    private List<String> filesToDelete = new ArrayList<>();
    private List<String> foldersToDelete = new ArrayList<>();

    protected ParameterizedIntegrationtest(final String name) {
        testname = name;
    }

    @Parameters({"host", "user", "pass", "share", "provclass", "nbport", "cifsport"})
        @BeforeSuite(alwaysRun = true)
        public static void initSuite(final String host, final String user, final String pass,
                final String share, final String provclass, @Optional Integer nbport, @Optional Integer cifsport) {
            m_host = host;
            m_user = user;
            m_pass = pass;
            m_share = share;
            m_nbport = nbport;
            m_cifsport = cifsport;
            // Load security provider
            try {
                Provider provider = (Provider)Class.forName(provclass).newInstance();
                Security.addProvider(provider);
            } catch (Exception e) {
                fail("Could not load security provider", e);
            }
            // Global JLAN Client setup
            SessionFactory.setSMBSigningEnabled(false);
        }

    protected DiskSession getSession() {
        return sess;
    }

    protected String getTestname() {
        return testname;
    }

    @BeforeTest(alwaysRun = true)
        public void beforeTest() {
            tId = (int)Thread.currentThread().getId();
            assertNotNull(m_host, "Target host");
            assertNotNull(m_share, "Target share");
            assertNotNull(m_user, "Target user");
            assertNotNull(m_pass, "Target pass");
            final PCShare share = new PCShare(m_host, m_share, m_user, m_pass);
            assertNotNull(share, "Created share");
            // Give each session a different virtual circuit id, this allows the test to be run against a Windows file
            // server without the file server closing sessions down
            SessionSettings sessSettings = new SessionSettings();
            sessSettings.setVirtualCircuit(tId);
            if (null != m_nbport) {
                sessSettings.setNetBIOSSessionPort(m_nbport.intValue());
            }
            if (null != m_cifsport) {
                sessSettings.setNativeSMBPort(m_cifsport.intValue());
            }
            try {
                sess = SessionFactory.OpenDisk(share, sessSettings);
            } catch (Exception e) {
                fail("Could not connect to server", e);
            }
            assertNotNull(sess, "Open session");
            sess.setProcessId(tId);
        }

    @AfterTest(alwaysRun = true)
        public void afterTest() {
            if (null != sess) {
                try {
                    sess.CloseSession();
                } catch (Exception ex) {
                    fail("Failed to close session", ex);
                }
                sess = null;
            }
        }

    @BeforeMethod(alwaysRun = true)
        public void BeforeMethod(Method m) throws Exception {
            LOGGER.info("Starting {}.{}", getTestname(), m.getName());
        }

    @AfterMethod(alwaysRun = true)
        public void afterMethod(Method m) throws Exception {
            // Delete the test files
            for (String name : filesToDelete) {
                try {
                    getSession().DeleteFile(name);
                } catch (Exception e) {
                    LOGGER.warn("Cleanup file {} failed", name, e);
                }
            }
            filesToDelete.clear();
            // Delete the test folders in reverse order
            Collections.reverse(foldersToDelete);
            for (String name : foldersToDelete) {
                try {
                    getSession().DeleteDirectory(name);
                } catch (Exception e) {
                    LOGGER.warn("Cleanup folder {} failed", name, e);
                }
            }
            foldersToDelete.clear();
            LOGGER.info("Finished {}.{}", getTestname(), m.getName());
        }

    /**
     * Generate a test file name that is unique per test
     *
     * @param iter int
     * @return String
     */
    public final String getPerTestFileName(int iter) {

        StringBuilder fName = new StringBuilder();

        if (getPath() != null)
            fName.append(getPath());
        else
            fName.append(FileName.DOS_SEPERATOR_STR);

        fName.append(getTestname());
        fName.append("_");
        fName.append(iter);
        fName.append(".txt");

        filesToDelete.add(fName.toString());
        return fName.toString();
    }

    /**
     * Generate a test file name that is unique per thread
     *
     * @param iter int
     * @return String
     */
    public final String getPerThreadFileName(int iter) {

        StringBuilder fName = new StringBuilder();

        if (getPath() != null)
            fName.append(getPath());
        else
            fName.append(FileName.DOS_SEPERATOR_STR);

        fName.append(getTestname());
        fName.append("_");
        fName.append(Thread.currentThread().getId());
        fName.append("_");
        fName.append(iter);
        fName.append(".txt");

        filesToDelete.add(fName.toString());
        return fName.toString();
    }

    /**
     * Generate a test folder name that is unique per test
     *
     * @param iter int
     * @return String
     */
    public final String getPerTestFolderName(int iter) {

        StringBuilder fName = new StringBuilder();

        if (getPath() != null)
            fName.append(getPath());
        else
            fName.append(FileName.DOS_SEPERATOR_STR);

        fName.append(getTestname());
        fName.append("_");
        fName.append(iter);

        foldersToDelete.add(fName.toString());
        return fName.toString();
    }

    /**
     * Generate a test folder name that is unique per thread
     *
     * @param iter int
     * @return String
     */
    public final String getPerThreadFolderName(int iter) {

        StringBuilder fName = new StringBuilder();

        if (getPath() != null)
            fName.append(getPath());
        else
            fName.append(FileName.DOS_SEPERATOR_STR);

        fName.append(getTestname());
        fName.append("_");
        fName.append(Thread.currentThread().getId());
        fName.append("_");
        fName.append(iter);

        foldersToDelete.add(fName.toString());
        return fName.toString();
    }

    /**
     * Generate a unique test file name
     *
     * @param iter int
     * @param sess DiskSession
     * @return String
     */
    public final String getUniqueFileName(int iter, DiskSession sess) {

        StringBuilder fName = new StringBuilder();

        if (getPath() != null)
            fName.append(getPath());
        else
            fName.append(FileName.DOS_SEPERATOR_STR);

        fName.append(getTestname());
        fName.append("_");
        fName.append(Thread.currentThread().getId());
        fName.append("_");
        fName.append(iter);
        fName.append("_");
        fName.append(sess.getServer());
        fName.append(".txt");

        filesToDelete.add(fName.toString());
        return fName.toString();
    }

    /**
     * Generate a unique test folder name
     *
     * @param iter int
     * @param sess DiskSession
     * @return String
     */
    public final String getUniqueFolderName(int iter, DiskSession sess) {

        StringBuilder fName = new StringBuilder();

        if (getPath() != null)
            fName.append(getPath());
        else
            fName.append(FileName.DOS_SEPERATOR_STR);

        fName.append(getTestname());
        fName.append("_");
        fName.append(Thread.currentThread().getId());
        fName.append("_");
        fName.append(iter);
        fName.append("_");
        fName.append(sess.getServer());

        foldersToDelete.add(fName.toString());
        return fName.toString();
    }

    public final void registerFileNameForDelete(final String name) {
        filesToDelete.add(name);
    }

    public final void registerFolderNameForDelete(final String name) {
        foldersToDelete.add(name);
    }

    /**
     * Set the test path
     *
     * @param path String
     */
    public final void setPath(String path) {
        m_path = path;

        if (m_path != null) {
            if (m_path.startsWith(FileName.DOS_SEPERATOR_STR) == false)
                m_path = FileName.DOS_SEPERATOR_STR + m_path;
            if (m_path.endsWith(FileName.DOS_SEPERATOR_STR) == false)
                m_path = m_path + FileName.DOS_SEPERATOR_STR;
        }
    }

    /**
     * Return the test relative path
     *
     * @return String
     */
    public final String getPath() {
        return m_path;
    }

    /**
     * Sleep for a while
     *
     * @param sleepMs long
     */
    protected final void testSleep(long sleepMs) {
        try {
            Thread.sleep( sleepMs);
        } catch (InterruptedException ex) {
        }
    }
}
