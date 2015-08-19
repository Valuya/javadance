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

import jcifs.smb.SmbFile;

/**
 * Rename Folder Test Class
 *
 * @author gkspencer
 */
public class RenameFolderIT extends ParameterizedJcifsTest {

    /**
     * Default constructor
     */
    public RenameFolderIT() {
        super("RenameFolderIT");
    }

    private void doTest(final int iteration) throws Exception {
        final String testFolderName = getPerTestFolderName(iteration);
        final String newFolderName = testFolderName.substring(0, testFolderName.lastIndexOf('/')) + "_new/";
        registerFolderNameForDelete(newFolderName);
        SmbFile sf = new SmbFile(getRoot(), testFolderName);
        SmbFile sfn = new SmbFile(getRoot(), newFolderName);
        assertFalse(sf.exists(), "Old folder exists before test");
        assertFalse(sfn.exists(), "New folder exists before test");
        sf.mkdir();
        assertTrue(sf.exists(), "Old folder exists after create");
        sf.renameTo(sfn);
        assertTrue(sfn.exists(), "New folder exists after rename");
        assertFalse(sf.exists(), "Old folder exists after rename");
    }

    @Parameters({"iterations"})
    @Test(groups = "xfunctest")
    public void test(@Optional("1") int iterations) throws Exception {
        for (int i = 0; i < iterations; i++) {
            doTest(i);
        }
    }
}
