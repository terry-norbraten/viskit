/*
 * TempDir.java
 *
 * Created on August 3, 2006, 12:09 PM
 *
 */
package viskit.xsd.assembly;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author rmgoldbe
 */

// Lifted from TempDir.java
// http://forum.java.sun.com/thread.jspa?threadID=470197&messageID=2169110
// Since we need a "private" local cache for any .class files from each
// DesignPoint adjusted Assembly. These caches should be deleted on exit,
// however Java IO doesn't recurse a directory on delete, so the deleterThread
// here hooks on exit; no need to set deleteOnExit to any file in this directory

public class TempDir {

    private static DirDeleter deleterThread;
    static {
        deleterThread = new DirDeleter();
        Runtime.getRuntime().addShutdownHook(deleterThread);
    }

    /**
     * Creates a temp directory with a generated name (given a certain prefix) in a given directory.
     * The directory (and all its content) will be destroyed on exit.
     * @param prefix
     * @param directory
     * @return 
     * @throws IOException
     */
    public static File createGeneratedName(String prefix, File directory)
            throws IOException {
        File tempFile = File.createTempFile(prefix, "", directory);
        if (!tempFile.delete()) {
            throw new IOException();
        }
        if (!tempFile.mkdir()) {
            throw new IOException();
        }
        deleterThread.add(tempFile);
        return tempFile;
    }

    /**
     * Creates a temp directory with a given name in a given directory.
     * The directory (and all its content) will be destroyed on exit.
     * @param name
     * @param directory
     * @return
     * @throws IOException 
     */
    public static File createNamed(String name, File directory)
            throws IOException {
        File tempFile = new File(directory, name);
        if (!tempFile.mkdir()) {
            throw new IOException();
        }
        deleterThread.add(tempFile);
        return tempFile;
    }
}