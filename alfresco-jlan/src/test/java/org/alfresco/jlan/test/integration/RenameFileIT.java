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
 * Rename File Test Class
 *
 * @author gkspencer
 */
public class RenameFileIT extends ParameterizedJcifsTest {

    /**
     * Default constructor
     */
    public RenameFileIT() {
        super("RenameFileIT");
    }

    private void doTest(final int iteration) throws Exception {
        final String testFileName = getPerTestFileName(iteration);
        final String newFileName = testFileName.replace(".txt", ".ren");
        registerFileNameForDelete(newFileName);
        SmbFile sf = new SmbFile(getRoot(), testFileName);
        SmbFile sfn = new SmbFile(getRoot(), newFileName);
        assertFalse(sf.exists(), "File already exits");
        assertFalse(sfn.exists(), "Renamed file already exits");
        sf.createNewFile();
        assertTrue(sf.exists(), "File exits after creation");
        // Rename the file
        sf.renameTo(sfn);
        assertTrue(sfn.exists(), "New file exits after rename");
        assertFalse(sf.exists(), "Old file exits after rename");
    }

    @Parameters({"iterations"})
    @Test(groups = "functest")
    public void test(@Optional("1") int iterations) throws Exception {
        for (int i = 0; i < iterations; i++) {
            doTest(i);
        }
    }
}
