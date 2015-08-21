/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.jlan.test.integration;

import java.io.InputStream;
import java.io.OutputStream;

import static org.testng.Assert.*;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbException;

/**
 * Open File With Shared Read Test Class
 *
 * @author gkspencer
 */
public class OpenFileShareReadIT extends ParameterizedJcifsTest {

    /**
     * Default constructor
     */
    public OpenFileShareReadIT() {
        super("OpenFileShareReadIT");
    }

    private void doTest(final int iteration) throws Exception {
        final String testFileName = getPerTestFileName(iteration);
        final SmbFile sf = new SmbFile(getRoot(), testFileName, SmbFile.FILE_SHARE_READ);
        if (isFirstThread()) {
            if (sf.exists()) {
                LOGGER.debug("File {} exists", testFileName);
            } else {
                sf.createNewFile();
                assertTrue(sf.exists(), "File exists after create");
            }
        }
        boolean openForRead = false;
        final SmbFile testFile = new SmbFile(getRoot(), testFileName, SmbFile.FILE_SHARE_READ);
        try (OutputStream os = testFile.getOutputStream()) {
            assertNotNull(os, "OutputStream");
            testSleep(2000);
        } catch (SmbException ex) {
            if (ex.getNtStatus() == SmbException.NT_STATUS_ACCESS_DENIED) {
                LOGGER.info("Open of {} failed with access denied error (expected)", testFileName);
                openForRead = true;
            } else if (ex.getNtStatus() == SmbException.NT_STATUS_SHARING_VIOLATION) {
                LOGGER.info("Open of {} failed with sharing violation error (expected)", testFileName);
                openForRead = true;
            } else {
                fail("Caught exception", ex);
            }
        }
        if (openForRead == true) {
            try (OutputStream is = testFile.getOutputStream()) {
                assertNotNull(is, "InputStream");
            } catch (SmbException ex) {
                fail("Caught exception", ex);
            }
        }
    }

    @Parameters({"iterations"})
    @Test(groups = "broken", threadPoolSize = 3, invocationCount = 10)
    public void test(@Optional("1") int iterations) throws Exception {
        for (int i = 0; i < iterations; i++) {
            doTest(i);
        }
    }
}
