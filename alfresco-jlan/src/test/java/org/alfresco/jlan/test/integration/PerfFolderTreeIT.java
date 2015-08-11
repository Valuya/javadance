package org.alfresco.jlan.test.integration;

import static org.testng.Assert.*;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.Reporter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.alfresco.jlan.client.CIFSDiskSession;
import org.alfresco.jlan.client.DiskSession;
import org.alfresco.jlan.client.SMBFile;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.util.MemorySize;

/**
 * Folder Tree Copy Performance Test Class
 *
 * @author gkspencer
 */
public class PerfFolderTreeIT extends ParameterizedIntegrationtest {

	// Maximum/minumum folder depth, folders per level, files per level

	private static final int MIN_FOLDERDEPTH		= 2;
	private static final int MAX_FOLDERDEPTH		= 10;

	private static final int MIN_FOLDERSPERLEVEL = 2;
	private static final int MAX_FOLDERSPERLEVEL = 25;

	private static final int MIN_FILESPERLEVEL 	= 0;
	private static final int MAX_FILESPERLEVEL = 25;

	// Maximum/minimum allowed file size and write size

	private static final long MIN_FILESIZE	= 1;// byte
	private static final long MAX_FILESIZE	= 16 * MemorySize.KILOBYTE;

	private static final int MIN_WRITESIZE	= 128;
	private static final int MAX_WRITESIZE	= (int) (64 * MemorySize.KILOBYTE);

	// File/folder names

	private static final String LEVELFOLDERNAME = "Folder_";
	private static final String LEVELFILENAME = "File_";
	private static final String LEVELFILEEXT = ".txt";

	// Total folder/file count

	private int m_totalFolders;
	private int m_totalFiles;

	/**
	 * Default constructor
	 */
	public PerfFolderTreeIT() {
        super("PerfFolderTreeIT");
	}


    private void doTest(final int iteration, final long fileSize, final int writeSize,
            final int folderDepth, final int foldersPerLevel, final int filesPerLevel) throws Exception {
        DiskSession s = getSession();
        assertTrue(s instanceof CIFSDiskSession, "Not an NT dialect CIFS session");
        String testFolder = getPerTestFolderName(iteration);
        s.CreateDirectory(testFolder);
        assertTrue(s.FileExists(testFolder), "Folder exits after creation");
        byte[] ioBuf = new byte[writeSize];
        Arrays.fill(ioBuf, (byte)'A');

        // Make the current folder path a relative path
        String curFolder = testFolder;
        if (curFolder.endsWith(FileName.DOS_SEPERATOR_STR) == false) {
            curFolder = curFolder + FileName.DOS_SEPERATOR_STR;
        }

        // Record the start time
        long startTime = System.currentTimeMillis();
        long endTime = 0L;
        // Create the path stacks for the current and next tree layers
        ArrayList<String> pathStack = new ArrayList<>(250);
        ArrayList<String> nextStack = new ArrayList<>(250);
        // Stack the starting point path
        pathStack.add(curFolder);
        // Create the folder levels and files
        int curLevel = 1;
        while (curLevel <= folderDepth) {
            try {
                // Add sub-folders for the current level of paths
                for (int pathIdx = 0; pathIdx < pathStack.size(); pathIdx++) {
                    // Get the current path
                    String curPath = pathStack.get(pathIdx);
                    registerFolderNameForDelete(curPath);
                    if (curPath.endsWith(FileName.DOS_SEPERATOR_STR) == false) {
                        curPath = curPath + FileName.DOS_SEPERATOR_STR;
                    }
                    // Create the current level of files/folders
                    createFolderLevel(curPath, s, curLevel, ioBuf, nextStack, foldersPerLevel, filesPerLevel, fileSize);
                    // Add the path to the list of paths to be deleted by cleanup
                }
                // Update the folder level
                curLevel++;
                // Swap the current and next path stacks, clear the next stack
                ArrayList<String> tempStack = pathStack;
                pathStack = nextStack;
                nextStack = tempStack;
                nextStack.clear();
            } catch ( Exception ex) {
                fail("Caught exception", ex);
            }
        }
        // Save the end time
        endTime = System.currentTimeMillis();
        // Output the elapsed time
        long elapsedMs = endTime - startTime;
        int ms = (int) (elapsedMs % 1000L);
        long elapsedSecs = elapsedMs/1000;
        int secs = (int) ( elapsedSecs % 60L);
        int mins = (int) (( elapsedSecs/60L) % 60L);
        int hrs  = (int) ( elapsedSecs/3600L);
        String msg = String.format("Created folder tree %d folders deep (%d  folders/%d  files per level) in %d:%d:%d.%d (%dms)",
                folderDepth, foldersPerLevel, filesPerLevel, hrs, mins, secs, ms, elapsedMs);
        LOGGER.info(msg);
        Reporter.log(msg + "<br/>\n");
        msg = String.format("Total of %d folders, %d files", m_totalFolders, m_totalFiles);
        LOGGER.info(msg);
        Reporter.log(msg + "<br/>\n");
    }

    /**
     * Create a folder level at the specified path
     *
     * @param rootPath String
     * @param sess DiskSession
     * @param curLevel int
     * @param ioBuf byte[]
     * @param pathStack ArrayList<String>
     */
    private void createFolderLevel(final String rootPath, final DiskSession sess, final int curLevel,
            final byte[] ioBuf, final ArrayList<String> pathStack,
            final int foldersPerLevel, final int filesPerLevel, final long fileSize) throws SMBException, IOException {

        // Create the folder levels and files

        StringBuilder pathStr = new StringBuilder(256);

        // Create the folders
        for (int folderIdx = 1; folderIdx <= foldersPerLevel; folderIdx++) {
            // Create a unique folder name
            pathStr.setLength( 0);
            pathStr.append(rootPath);
            pathStr.append(LEVELFOLDERNAME);
            pathStr.append(curLevel);
            pathStr.append("_");
            pathStr.append(folderIdx);
            // Create the folder
            String folderName = pathStr.toString();
            registerFolderNameForDelete(folderName);
            try {
                sess.CreateDirectory(folderName);
            } catch (SMBException ex) {
                LOGGER.warn("Error creating folder {}", folderName, ex);
                throw ex;
            }
            // Add the folder to the path stack
            pathStack.add(folderName);
            // Update the folder count
            m_totalFolders++;
        }
        // Create the files
        if (filesPerLevel > 0) {
            // Create the test files
            for (int fileIdx = 1; fileIdx <= filesPerLevel; fileIdx++) {
                // Create a unique file name
                pathStr.setLength(0);
                pathStr.append(rootPath);
                pathStr.append(LEVELFILENAME);
                pathStr.append(fileIdx);
                pathStr.append(LEVELFILEEXT);
                String fileName = pathStr.toString();
                registerFileNameForDelete(fileName);
                // Create a new file
                SMBFile testFile = sess.CreateFile(fileName);
                // Write to the file until we hit the required file size
                long fs = 0L;
                while (fs < fileSize) {
                    // Write to the file
                    testFile.Write(ioBuf, ioBuf.length, 0);
                    // Update the file size
                    fs += ioBuf.length;
                }
                // Make sure all data has been written to the file
                testFile.Flush();
                // Close the test file
                testFile.Close();
                // Update the file count
                m_totalFiles++;
            }
        }
    }

    @Parameters({"iterations", "filesize", "writesize", "folderdepth", "foldersperlevel", "filesperlevel"})
        @Test(groups = "perftest", singleThreaded = true)
        public void test(@Optional("1") final int iterations,
                @Optional("80") final String fs, @Optional("8K") final String ws, @Optional("5") final int folderDepth,
                @Optional("5") final int foldersPerLevel, @Optional("5") final int filesPerLevel) throws Exception {
            long fileSize = 0;
            int writeSize = 0;
            try {
                fileSize = MemorySize.getByteValue(fs);
                if (fileSize < MIN_FILESIZE || fileSize > MAX_FILESIZE) {
                    fail("Invalid filesize (" + MIN_FILESIZE + " - " + MAX_FILESIZE + ")");
                }
            } catch (NumberFormatException ex) {
                fail("Invalid filesize " + fs);
            }
            try {
                writeSize = MemorySize.getByteValueInt(ws);
                if (writeSize < MIN_WRITESIZE || writeSize > MAX_WRITESIZE) {
                    fail("Invalid writesize (" + MIN_WRITESIZE + " - " + MAX_WRITESIZE + ")");
                }
            } catch (NumberFormatException ex) {
                fail("Invalid writesize " + ws);
            }
            if (folderDepth < MIN_FOLDERDEPTH || folderDepth > MAX_FOLDERDEPTH) {
                fail("Invalid folderdepth (" + MIN_FOLDERDEPTH + " - " + MAX_FOLDERDEPTH + ")");
            }
            if (foldersPerLevel < MIN_FOLDERSPERLEVEL || foldersPerLevel > MAX_FOLDERSPERLEVEL) {
                fail("Invalid foldersperlevel (" + MIN_FOLDERSPERLEVEL + " - " + MAX_FOLDERSPERLEVEL + ")");
            }
            if (filesPerLevel < MIN_FILESPERLEVEL || filesPerLevel > MAX_FILESPERLEVEL) {
                fail("Invalid filesperlevel (" + MIN_FILESPERLEVEL + " - " + MAX_FILESPERLEVEL + ")");
            }
            for (int i = 0; i < iterations; i++) {
                doTest(i, fileSize, writeSize, folderDepth, foldersPerLevel, filesPerLevel);
            }
        }
}
