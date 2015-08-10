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

/**
 * Delete File Test Class
 *
 * @author gkspencer
 */
public class DeleteFileIT extends ParameterizedIntegrationtest {

    /**
     * Default constructor
     */
    public DeleteFileIT() {
        super("DeleteFileIT");
    }

    private void doTest(int iteration) throws Exception {
        DiskSession s = getSession();
        assertTrue(s instanceof CIFSDiskSession, "Not an NT dialect CIFS session");
        String testFileName = getPerTestFileName(iteration);
        if (s.FileExists(testFileName)) {
            LOGGER.info("File {} exists", testFileName);
        } else {
            SMBFile testFile = s.CreateFile(testFileName);
            assertTrue(s.FileExists(testFileName));
        }
        CIFSDiskSession cifsSess = (CIFSDiskSession)s;
        try {
            cifsSess.DeleteFile(testFileName);
        } catch ( Exception ex) {
            fail("Error deleting file " + testFileName + " on server " + s.getServer(), ex);
        }
        assertFalse(cifsSess.FileExists(testFileName), "File exists after delete");
    }

    @Parameters({"iterations"})
    @Test(groups = "functest")
    public void test(@Optional("1") int iterations) throws Exception {
        for (int i = 0; i < iterations; i++) {
            doTest(i);
        }
    }
}
