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
import org.testng.Reporter;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import org.alfresco.jlan.client.CIFSDiskSession;
import org.alfresco.jlan.client.CIFSFile;
import org.alfresco.jlan.client.DiskSession;
import org.alfresco.jlan.client.SMBFile;
import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.server.filesys.AccessMode;
import org.alfresco.jlan.server.filesys.FileAction;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.smb.SharingMode;

/**
 * Open File Test Class
 *
 * @author gkspencer
 */
public class OpenFileIT extends ParameterizedIntegrationtest {

    /**
     * Default constructor
     */
    public OpenFileIT() {
        super();
    }

    @Override
    protected void doTest(int iteration) throws Exception {
        DiskSession s = getSession();
        assertTrue(s instanceof CIFSDiskSession, "Not an NT dialect CIFS session");
        String testFileName = getPerTestFileName(iteration);
        if (s.FileExists(testFileName)) {
            Reporter.log("File " + testFileName + " exists");
        } else {
            SMBFile testFile = s.CreateFile(testFileName);
            if (testFile != null) {
                testFile.Close();
            }
            assertTrue(s.FileExists(testFileName));
        }
        CIFSDiskSession cifsSess = (CIFSDiskSession)s;
        CIFSFile openFile = null;
        try {
            // Open existing file for exclusive access, else fail
            openFile = cifsSess.NTCreate("\\" + testFileName, AccessMode.NTReadWrite, FileAttribute.NTNormal,
                    SharingMode.NOSHARING, FileAction.NTOpen, 0, 0);
            if (null != openFile) {
                // Hold the file open for a short while, other threads should fail to open the file
                testSleep(2000);
                // Close the test file
                openFile.Close();
            }
        } catch (SMBException ex) {
            // Check for an access denied error code
            assertTrue(ex.getErrorClass() == SMBStatus.NTErr && ex.getErrorCode() == SMBStatus.NTAccessDenied,
                    "Open failed with wrong error");
            Reporter.log("Open failed with access denied error (expected)");
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
