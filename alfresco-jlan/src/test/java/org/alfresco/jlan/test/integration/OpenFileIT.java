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

import static org.testng.Assert.*;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbException;

/**
 * Open File Test Class
 *
 * @author gkspencer
 */
public class OpenFileIT extends ParameterizedJcifsTest {

    /**
     * Default constructor
     */
    public OpenFileIT() {
        super("OpenFileIT");
    }

    private void doTest(final int iteration) throws Exception {
        final String testFileName = getPerTestFileName(iteration);
        final SmbFile sf = new SmbFile(getRoot(), testFileName);
        if (sf.exists()) {
            LOGGER.info("File {} exists", testFileName);
        } else {
            sf.createNewFile();
            assertTrue(sf.exists(), "File exists after create");
        }
        // Open existing file for exclusive access, else fail
        try (final SmbFileOutputStream os = new SmbFileOutputStream(sf.getCanonicalPath(), SmbFile.FILE_NO_SHARE)) {
            if (null != os) {
                // Hold the file open for a short while, other threads should fail to open the file
                testSleep(2000);
                // Close the test file done by autoclose
            }
        } catch (SmbException ex) {
            // Check for an access denied error code
            assertTrue(ex.getNtStatus() == SmbException.NT_STATUS_ACCESS_DENIED, "Open failed with wrong error");
            LOGGER.info("Open of {} failed with access denied error (expected)", testFileName);
        }
    }

    @Parameters({"iterations"})
    //@Test(groups = "functest", threadPoolSize = 3, invocationCount = 3)
    @Test(groups = "functest")
    public void test(@Optional("1") int iterations) throws Exception {
        for (int i = 0; i < iterations; i++) {
            doTest(i);
        }
    }
}
