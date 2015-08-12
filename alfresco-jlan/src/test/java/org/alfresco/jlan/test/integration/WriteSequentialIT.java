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

import java.util.Arrays;

import static org.testng.Assert.*;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import org.alfresco.jlan.client.CIFSDiskSession;
import org.alfresco.jlan.client.DiskSession;
import org.alfresco.jlan.client.SMBFile;
import org.alfresco.jlan.client.info.FileInfo;
import org.alfresco.jlan.server.filesys.AccessMode;
import org.alfresco.jlan.util.MemorySize;

/**
 * Sequential File Write Test Class
 *
 * @author gkspencer
 */
public class WriteSequentialIT extends ParameterizedIntegrationtest {

	// Maximum/minimum allowed file size and write size

    private static final long MIN_FILESIZE	= 100 * MemorySize.KILOBYTE;
    private static final long MAX_FILESIZE	= 2 * MemorySize.GIGABYTE;

    private static final int MIN_WRITESIZE	= 128;
    private static final int MAX_WRITESIZE	= (int) (64 * MemorySize.KILOBYTE);

	// Characters to use in the write buffer patterns

    private static final String WRITEPATTERN = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZ0123456789";

	/**
	 * Default constructor
	 */
	public WriteSequentialIT() {
		super("WriteSequentialIT");
	}

    private void doTest(final int iteration, final long fileSize, final int writeSize) throws Exception {
        SMBFile tf = null;
        DiskSession s = getSession();
        assertTrue(s instanceof CIFSDiskSession, "Not an NT dialect CIFS session");
        String testFileName = getUniqueFileName(iteration, s);
        if (s.FileExists(testFileName)) {
            LOGGER.debug("Opening existing file {} via {}", testFileName, s.getServer());
            tf = s.OpenFile(testFileName, AccessMode.ReadWrite);
        } else {
            LOGGER.debug("Creating file {} via {}", testFileName, s.getServer());
            tf = s.CreateFile(testFileName);
            assertTrue(s.FileExists(testFileName), "File exists after create");
        }
        assertNotNull(tf);
        try (SMBFile testFile = tf) {
			// Allocate the read/write buffer
			byte[] ioBuf = new byte[writeSize];
			// Write to the file until we hit the required file size
			long written = 0L;
			int patIdx = 0;
			while (written < fileSize) {
				// Fill each buffer with a different test pattern
				if (patIdx == WRITEPATTERN.length()) {
					patIdx = 0;
                }
				byte fillByte = (byte)WRITEPATTERN.charAt(patIdx++);
				Arrays.fill(ioBuf, fillByte);
				// Write to the file
				testFile.Write(ioBuf, ioBuf.length, 0);
				// Update the file size
				written += ioBuf.length;
			}
			// Make sure all data has been written to the file
			testFile.Flush();
			// Refresh the file information to get the latest file size
			testFile.refreshFileInformation();
			// Check the file is the expected size
            assertEquals(fileSize, testFile.getFileSize(), "Filesize");
			// Read the file back and check the test patterns
			long readPos = 0L;
			patIdx = 0;

			while (readPos < fileSize) {
				// Read a buffer of data from the file
				int rdlen = testFile.Read(ioBuf);
                assertEquals(ioBuf.length, rdlen, "read length");
				// Check that the buffer contains the expected pattern
				if (patIdx == WRITEPATTERN.length())
					patIdx = 0;
				byte chkByte = (byte)WRITEPATTERN.charAt(patIdx++);
				int chkIdx = 0;
				while (chkIdx < ioBuf.length) {
                    assertEquals(chkByte, ioBuf[chkIdx], "Pattern");
					chkIdx++;
				}
				// Update the read position
				readPos += ioBuf.length;
			}
			// Close the file
			testFile.Close();
			// Check the test file size
            FileInfo fInfo = s.getFileInformation(testFileName);
            assertNotNull(fInfo, "File information");
            assertEquals(fileSize, fInfo.getSize(), "File size");
        }
    }

    @Parameters({"iterations", "filesize", "writesize"})
        @Test(groups = "functest")
        public void test(@Optional("1") final int iterations, @Optional("10M") final String fs,
                @Optional("8K") final String ws) throws Exception {
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
            for (int i = 0; i < iterations; i++) {
                doTest(i, fileSize, writeSize);
            }
        }
}
