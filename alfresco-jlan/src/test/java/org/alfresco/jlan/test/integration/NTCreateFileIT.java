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
import jcifs.smb.SmbException;

/**
 * NTCreate File Test Class
 *
 * @author gkspencer
 */
public class NTCreateFileIT extends ParameterizedJcifsTest {

	/**
	 * Default constructor
	 */
	public NTCreateFileIT() {
		super("NTCreateFileIT");
	}

    private void doTest(final int iteration) throws Exception {
        // Create a test file name for this iteration
        final String testFileName = getPerTestFileName(iteration);
        final SmbFile sf = new SmbFile(getRoot(), testFileName, SmbFile.FILE_SHARE_READ);

        // Check if the test file exists
        if (sf.exists()) {
            LOGGER.info("File {} already exists", testFileName);
        }
        try {
            sf.createNewFile();
            assertTrue(sf.exists(), "File exists after create");
        } catch ( SmbException ex) {
            // Check for an access denied error code
            if (ex.getNtStatus() == SmbException.NT_STATUS_ACCESS_DENIED) {
                LOGGER.info("Create of {} failed with access denied error (expected)", testFileName);
            } else if (ex.getNtStatus() == SmbException.NT_STATUS_OBJECT_NAME_COLLISION) {
                LOGGER.info("Create of {} failed with object name collision (expected)", testFileName);
            } else {
                fail("Caught exception", ex);
            }
        }
    }

    @Parameters({"iterations"})
    @Test(groups = "functest")
    public void test(@Optional("1") int iterations) throws Exception {
        for (int i = 0; i < iterations; i++) {
            doTest(i);
        }
    }
}
