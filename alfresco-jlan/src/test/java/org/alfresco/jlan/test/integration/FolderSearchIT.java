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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import jcifs.smb.SmbFile;

/**
 * Folder Search Test Class
 *
 * @author gkspencer
 */
public class FolderSearchIT extends ParameterizedJcifsTest {

    // Constants

    private static final String TESTFILENAME = "testFile";
    private static final String TESTFILEEXT  = ".txt";
    private static final String TESTFOLDERNAME = "testFolder";

    // Maximum/minimum number of files/folders allowed

    private static final int MIN_FILECOUNT	= 10;
    private static final int MAX_FILECOUNT	= 5000;

    private static final int MIN_FOLDERCOUNT	= 10;
    private static final int MAX_FOLDERCOUNT	= 5000;

    /**
     * Default constructor
     */
    public FolderSearchIT() {
        super("FolderSearchIT");
    }


    private void prepareTree(final int fileCount, final int folderCount) throws Exception {
        final StringBuilder fnameStr = new StringBuilder(64);
        for (int i = 1; i <= fileCount; i++) {
            buildFileName(fnameStr, i);
            final SmbFile sf = new SmbFile(getRoot(), fnameStr.toString());
            sf.createNewFile();
            assertTrue(sf.exists(), "File exists after create");
            registerFileNameForDelete(fnameStr.toString());
        }
        for (int i = 1; i <= folderCount; i++) {
            buildFolderName(fnameStr, i);
            final SmbFile sf = new SmbFile(getRoot(), fnameStr.toString());
            sf.mkdir();
            assertTrue(sf.exists(), "Folder exists after create");
            registerFolderNameForDelete(fnameStr.toString());
        }
    }

    private void doTest(final int iteration, final int fileCount, final int folderCount) throws Exception {
        final List<String> fileList = new ArrayList<>();
        final List<String> folderList = new ArrayList<>();
        for (final SmbFile sf : getRoot().listFiles()) {
            final String baseName = sf.getName(); 
            if (sf.isDirectory()) {
                assertTrue(baseName.startsWith(TESTFOLDERNAME), "Folder name starts with " + TESTFOLDERNAME);
                assertFalse(baseName.endsWith(TESTFILEEXT), "Folder name ends with " + TESTFILEEXT);
                folderList.add(baseName);
            } else {
                assertFalse(baseName.startsWith(TESTFOLDERNAME), "Folder name starts with " + TESTFOLDERNAME);
                assertTrue(baseName.endsWith(TESTFILEEXT), "Folder name ends with " + TESTFILEEXT);
                fileList.add(baseName);
            }
        }
        final StringBuilder fnameStr = new StringBuilder(64);
        int i = 1;
        while (i <= fileCount) {
            buildFileName(fnameStr, i);
            final String name = fnameStr.toString();
            if (fileList.contains(name)) {
                fileList.remove(name);
            } else {
                fail("Found file name in the folder list, " + name);
            }
            i++;
        }
        i = 1;
        while (i <= folderCount) {
            buildFolderName(fnameStr, i);
            final String name = fnameStr.toString();
            if (folderList.contains(name)) {
                folderList.remove(name);
            } else {
                fail("Found folder name in the file list, " + name);
            }
            i++;
        }
        assertEquals(fileList.size(), 0, "File list contains unexpected entries");
        assertEquals(folderList.size(), 0, "Folder list contains unexpected entries");
    }

    /**
     * Build a test file name
     *
     * @param str StringBuilder
     * @param fileIdx int
     */
    private void buildFileName(StringBuilder str, int fileIdx) {
        str.setLength( 0);
        str.append(TESTFILENAME);
        str.append("_");
        str.append(fileIdx);
        str.append(TESTFILEEXT);
    }

    /**
     * Build a test folder name
     *
     * @param str StringBuilder
     * @param fldrIdx int
     */
    private void buildFolderName(StringBuilder str, int fldrIdx) {
        str.setLength(0);
        str.append(TESTFOLDERNAME);
        str.append("_");
        str.append(fldrIdx);
        str.append("/");
    }

    @Parameters({"iterations", "filecount", "foldercount"})
        @Test(groups = "functest")
        public void test(@Optional("1") final int iterations, @Optional("100") final int fileCount,
                @Optional("100") final int folderCount) throws Exception {
            if (fileCount < MIN_FILECOUNT || fileCount > MAX_FILECOUNT) {
                fail("Invalid filecount (" + MIN_FILECOUNT + " - " + MAX_FILECOUNT + ")");
            }
            if (folderCount < MIN_FILECOUNT || folderCount > MAX_FILECOUNT) {
                fail("Invalid foldercount (" + MIN_FOLDERCOUNT + " - " + MAX_FOLDERCOUNT + ")");
            }
            prepareTree(fileCount, folderCount);
            for (int i = 0; i < iterations; i++) {
                doTest(i, fileCount, folderCount);
            }
        }
}
