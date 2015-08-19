package org.alfresco.jlan.test.integration;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.ThreadLocal;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

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

import jcifs.smb.SmbFile;
import jcifs.smb.SmbException;
import jcifs.Config;

public class ParameterizedJcifsTest {

    protected static final Logger LOGGER = LoggerFactory
        .getLogger(ParameterizedJcifsTest.class);

    private static AtomicLong firstThreadId = new AtomicLong();

    private static String m_host;
    private static String m_user;
    private static String m_pass;
    private static String m_share;
    private static Integer m_cifsport;

    private String testname;
    private String m_path;
    private SmbFile m_root;
    private ThreadLocal<List<String>> filesToDelete = new ThreadLocal<List<String>>() {
        @Override public List<String> initialValue() {
            return new ArrayList<String>();
        }
    };
    private ThreadLocal<List<String>> foldersToDelete = new ThreadLocal<List<String>>() {
        @Override public List<String> initialValue() {
            return new ArrayList<String>();
        }
    };
    protected ParameterizedJcifsTest(final String name) {
        testname = name;
    }

    @Parameters({"host", "user", "pass", "share", "cifsport"})
        @BeforeSuite(alwaysRun = true)
        public static void initSuite(final String host, final String user, final String pass,
                final String share, @Optional Integer cifsport) {
            m_host = host;
            m_user = user;
            m_pass = pass;
            m_share = share;
            m_cifsport = cifsport;
        }

    protected SmbFile getRoot() {
        return m_root;
    }

    protected String getTestname() {
        return testname;
    }

    protected boolean isFirstThread() {
        return firstThreadId.get() == Thread.currentThread().getId();
    }

    @BeforeMethod(alwaysRun = true)
        public void BeforeMethod(Method m) throws Exception {
            firstThreadId.compareAndSet(0L, Thread.currentThread().getId());
            LOGGER.info("Starting {}.{} [T{}]", getTestname(), m.getName(), Thread.currentThread().getId());
            assertNotNull(m_host, "Target host");
            assertNotNull(m_share, "Target share");
            assertNotNull(m_user, "Target user");
            assertNotNull(m_pass, "Target pass");
            String url = "smb://" + m_user + ":" + m_pass + "@" + m_host;
            if (null != m_cifsport) {
                url += ":" + m_cifsport;
            }
            url += "/" + m_share + "/";
            Config.setProperty("jcifs.resolveOrder", "DNS");
            Config.setProperty("jcifs.smb.client.attrExpirationPeriod", "0");
            // Config.setProperty("jcifs.smb.client.ssnLimit", "1");
            m_root = new SmbFile(url);
            assertNotNull(getRoot(), "Root");
        }

    @AfterMethod(alwaysRun = true)
        public void afterMethod(final Method m) throws Exception {
            if (!filesToDelete.get().isEmpty()) {
                LOGGER.debug("Cleaning up files of test {}", getTestname());
            }
            // Delete the test files
            for (final String name : filesToDelete.get()) {
                try {
                    final SmbFile sf = new SmbFile(getRoot(), name);
                    if (sf.exists()) {
                        sf.delete();
                    }
                } catch (SmbException e) {
                    LOGGER.warn("Cleanup file {} failed: {}", name, e.getMessage());
                }
            }
            filesToDelete.get().clear();
            if (!foldersToDelete.get().isEmpty()) {
                LOGGER.debug("Cleaning up folders of test {}", getTestname());
            }
            // Delete the test folders in reverse order
            Collections.reverse(foldersToDelete.get());
            for (final String name : foldersToDelete.get()) {
                try {
                    final SmbFile sf = new SmbFile(getRoot(), name);
                    if (sf.exists()) {
                        sf.delete();
                    }
                } catch (SmbException e) {
                    LOGGER.warn("Cleanup folder {} failed: {}", name, e.getMessage());
                }
            }
            foldersToDelete.get().clear();
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

        if (getPath() != null) {
            fName.append(getPath());
        }
        fName.append(getTestname());
        fName.append("_");
        fName.append(iter);
        fName.append(".txt");

        filesToDelete.get().add(fName.toString());
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

        if (getPath() != null) {
            fName.append(getPath());
        }
        fName.append(getTestname());
        fName.append("_");
        fName.append(Thread.currentThread().getId());
        fName.append("_");
        fName.append(iter);
        fName.append(".txt");

        filesToDelete.get().add(fName.toString());
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

        if (getPath() != null) {
            fName.append(getPath());
        }
        fName.append(getTestname());
        fName.append("_");
        fName.append(iter);
        fName.append("/");

        foldersToDelete.get().add(fName.toString());
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

        if (getPath() != null) {
            fName.append(getPath());
        }
        fName.append(getTestname());
        fName.append("_");
        fName.append(Thread.currentThread().getId());
        fName.append("_");
        fName.append(iter);
        fName.append("/");

        foldersToDelete.get().add(fName.toString());
        return fName.toString();
    }

    /**
     * Generate a unique test file name
     *
     * @param iter int
     * @return String
     */
    public final String getUniqueFileName(int iter) {

        StringBuilder fName = new StringBuilder();

        if (getPath() != null) {
            fName.append(getPath());
        }
        fName.append(getTestname());
        fName.append("_");
        fName.append(Thread.currentThread().getId());
        fName.append("_");
        fName.append(iter);
        fName.append("_");
        fName.append(getRoot().getServer());
        fName.append(".txt");

        filesToDelete.get().add(fName.toString());
        return fName.toString();
    }

    /**
     * Generate a unique test folder name
     *
     * @param iter int
     * @return String
     */
    public final String getUniqueFolderName(int iter) {

        StringBuilder fName = new StringBuilder();

        if (getPath() != null) {
            fName.append(getPath());
        }
        fName.append(getTestname());
        fName.append("_");
        fName.append(Thread.currentThread().getId());
        fName.append("_");
        fName.append(iter);
        fName.append("_");
        fName.append(getRoot().getServer());
        fName.append("/");

        foldersToDelete.get().add(fName.toString());
        return fName.toString();
    }

    public final void registerFileNameForDelete(final String name) {
        filesToDelete.get().add(name);
    }

    public final void registerFolderNameForDelete(final String name) {
        foldersToDelete.get().add(name);
    }

    /**
     * Set the test path
     *
     * @param path String
     */
    public final void setPath(String path) {
        m_path = path;

        if (m_path != null) {
            if (m_path.endsWith("/") == false)
                m_path += "/";
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
