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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Random;

import static org.testng.Assert.*;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.Reporter;

import org.alfresco.jlan.util.MemorySize;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;
import jcifs.smb.SmbException;

/**
 * Random Access File Write Test Class
 *
 * @author gkspencer
 */
public class WriteRandomIT extends ParameterizedJcifsTest {

    // Maximum/minimum allowed file size, write size and write count

    private static final long MIN_FILESIZE	= 100 * MemorySize.KILOBYTE;
    private static final long MAX_FILESIZE	= 2 * MemorySize.GIGABYTE;

    private static final int MIN_WRITESIZE	= 128;
    private static final int MAX_WRITESIZE	= (int) (64 * MemorySize.KILOBYTE);

    private static final int MIN_WRITECOUNT	= 10;
    private static final int MAX_WRITECOUNT	= 100000;

    // Characters to use in the write buffer patterns

    private static final String WRITEPATTERN = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZ0123456789";

    /**
     * Default constructor
     */
    public WriteRandomIT() {
        super("WriteRandomIT");
    }

    private void doTest(final int iteration, final long fileSize, final int writeSize, int writeCount) throws Exception {
        final String testFileName = getUniqueFileName(iteration);
        final SmbFile sf = new SmbFile(getRoot(), testFileName);
        if (sf.exists()) {
            LOGGER.debug("Opening existing file {} via {}", testFileName, sf.getServer());
        } else {
            LOGGER.debug("Creating file {} via {}", testFileName, sf.getServer());
            sf.createNewFile();
            assertTrue(sf.exists(), "File exists after create");
        }
        final SmbRandomAccessFile tf = new SmbRandomAccessFile(sf, "rw");
        assertNotNull(tf);
        try {
            // Extend the file to the required size
            tf.setLength(fileSize);
            // Check that the file was extended
            assertEquals(sf.length(), fileSize, "File size after extend");
            // Allocate the read/write buffer
            byte[] iBuf = new byte[writeSize];
            byte[] oBuf = new byte[writeSize];
            // Use a random file position for each write
            Random randomPos = new Random();
            int maxPos = (int)(fileSize - writeSize);

            // Write to the file until we hit the required write count
            int wc = 0;
            int patIdx = 0;
            while (wc < writeCount) {
                // Fill each buffer with a different test pattern
                if (patIdx == WRITEPATTERN.length()) {
                    patIdx = 0;
                }
                byte fillByte = (byte)WRITEPATTERN.charAt(patIdx++);
                Arrays.fill(oBuf, fillByte);

                // Set the write position
                final long writePos = randomPos.nextInt(maxPos);
                tf.seek(writePos);

                // Write to the file
                tf.write(oBuf);

                // Read the data back from the file
                tf.seek(writePos);
                tf.readFully(iBuf);
                // Check that the buffer contains the expected pattern
                if (!Arrays.equals(iBuf, oBuf)) {
                    fail("Pattern check failed at position " + writePos + ", writeCount=" + wc);
                }
                // Update the write count
                wc++;
            }
        } finally {
            tf.close();
        }
    }

    @Parameters({"iterations", "filesize", "writesize", "writecount"})
        @Test(groups = "functest")
        public void test(@Optional("1") final int iterations, @Optional("10M") final String fs,
                @Optional("8K") final String ws, @Optional("100") final int writeCount) throws Exception {
            long fileSize = 0;
            int writeSize = 0;
            try {
                fileSize = MemorySize.getByteValue(fs);
                if (fileSize < MIN_FILESIZE || fileSize > MAX_FILESIZE) {
                    fail("Invalid file size (" + MIN_FILESIZE + " - " + MAX_FILESIZE + ")");
                }
            } catch (NumberFormatException ex) {
                fail("Invalid file size " + fs);
            }
            try {
                writeSize = MemorySize.getByteValueInt(ws);
                if (writeSize < MIN_WRITESIZE || writeSize > MAX_WRITESIZE) {
                    fail("Invalid write buffer size (" + MIN_WRITESIZE + " - " + MAX_WRITESIZE + ")");
                }
            } catch (NumberFormatException ex) {
                fail("Invalid write size " + ws);
            }
            if (writeCount < MIN_WRITECOUNT || writeCount > MAX_WRITECOUNT) {
                fail("Invalid write count (" + MIN_WRITECOUNT + " - " + MAX_WRITECOUNT + ")");
            }
            for (int i = 0; i < iterations; i++) {
                doTest(i, fileSize, writeSize, writeCount);
            }
        }
}
