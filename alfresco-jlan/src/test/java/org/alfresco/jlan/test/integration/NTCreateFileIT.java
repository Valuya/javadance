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

import org.alfresco.jlan.client.CIFSDiskSession;
import org.alfresco.jlan.client.DiskSession;
import org.alfresco.jlan.client.SMBFile;
import org.alfresco.jlan.server.filesys.AccessMode;
import org.alfresco.jlan.server.filesys.FileAction;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.smb.SharingMode;

/**
 * NTCreate File Test Class
 *
 * @author gkspencer
 */
public class NTCreateFileIT extends ParameterizedIntegrationtest {

	/**
	 * Default constructor
	 */
	public NTCreateFileIT() {
		super("NTCreateFileIT");
	}

    private void doTest(int iteration) throws Exception {
        DiskSession s = getSession();
        assertTrue(s instanceof CIFSDiskSession, "Not an NT dialect CIFS session");

        // Create a test file name for this iteration
        String testFileName = getPerTestFileName(iteration);

        // Check if the test file exists
        if (s.FileExists(testFileName)) {
            LOGGER.info("File {} already exists", testFileName);
        }
        CIFSDiskSession cifsSess = (CIFSDiskSession)s;
        try {
            SMBFile testFile = cifsSess.NTCreate( testFileName, AccessMode.NTReadWrite, FileAttribute.NTNormal,
                    SharingMode.READ, FileAction.NTCreate, 0, 0);
            if (testFile != null) {
                testFile.Close();
            }
            assertTrue(s.FileExists(testFileName), "File does not exist after create, " + testFileName); 
        } catch ( SMBException ex) {
            // Check for an access denied error code
            if (ex.getErrorClass() == SMBStatus.NTErr && ex.getErrorCode() == SMBStatus.NTAccessDenied) {
                LOGGER.info("Create of {} failed with access denied error (expected)", testFileName);
            } else if (ex.getErrorClass() == SMBStatus.NTErr && ex.getErrorCode() == SMBStatus.NTObjectNameCollision) {
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
