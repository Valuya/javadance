/*
 * Copyright (C) 2006-2011 Alfresco Software Limited.
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

/**
 * Rename Folder Test Class
 *
 * @author gkspencer
 */
public class RenameFolderIT extends ParameterizedIntegrationtest {

    // Constants

    private static final String TestFolderName 	= "fromFolder";
    private static final String TestFolderNew	= "toFolder";

    /**
     * Default constructor
     */
    public RenameFolderIT() {
        super("RenameFolderIT");
    }

    private void doTest(int iteration) throws Exception {
        DiskSession s = getSession();
        assertTrue(s instanceof CIFSDiskSession, "Not an NT dialect CIFS session");
        String testFolderName = getPerTestFolderName(iteration);
        String newFolderName = testFolderName + "_new";
        registerFolderNameForDelete(newFolderName);
        assertFalse(s.FileExists(testFolderName), "Old folder exists before test");
        assertFalse(s.FileExists(newFolderName), "New folder exists before test");
        s.CreateDirectory(testFolderName);
        assertTrue(s.FileExists(testFolderName), "Old folder exists after create");
        s.RenameFile(testFolderName, newFolderName);
        assertTrue(s.FileExists(newFolderName), "New folder exists after rename");
        assertFalse(s.FileExists(testFolderName), "Old folder exists after rename");
    }

    @Parameters({"iterations"})
    @Test(groups = "functest")
    public void test(@Optional("1") int iterations) throws Exception {
        for (int i = 0; i < iterations; i++) {
            doTest(i);
        }
    }
}
