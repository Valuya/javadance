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
import org.alfresco.jlan.client.DiskSession;
import org.alfresco.jlan.debug.Debug;

/**
 * Delete Folder Test Class
 *
 * @author gkspencer
 */
public class DeleteFolderIT extends ParameterizedIntegrationtest {

	/**
	 * Default constructor
	 */
	public DeleteFolderIT() {
		super();
	}

    @Override
    protected void doTest(int iteration) throws Exception {
        DiskSession s = getSession();
        String testFolderName = getPerTestFolderName(iteration);
        if (s.FileExists(testFolderName) && s.isDirectory(testFolderName)) {
            Reporter.log("Folder " + testFolderName + "already exists.");
        } else {
            s.CreateDirectory(testFolderName);
            assertTrue(s.FileExists(testFolderName));
        }

        // Delete the folder
        CIFSDiskSession cifsSess = (CIFSDiskSession)s;
        try {
            cifsSess.DeleteDirectory(testFolderName);
        } catch ( Exception ex) {
            fail("Error deleting folder " + testFolderName + " on server " + s.getServer(), ex);
        }

        // Check if the folder exists
        assertFalse(cifsSess.FileExists(testFolderName) && cifsSess.isDirectory(testFolderName));
    }

    @Parameters({"iterations"})
    @Test(groups = "functest")
    public void test(@Optional("1") int iterations) throws Exception {
        for (int i = 0; i < iterations; i++) {
            doTest(i);
        }
    }
}
