package net.f4fs.filesystem.util;

import java.io.File;


/**
 * Utils for operations with files
 * 
 * @author Raphael
 */
public class FSFileUtils {

    /**
     * Deletes all files in the specified path if it is a directory, 
     * if path is a file, remove file from disk.
     * 
     * @param path Path to file or directory to remove
     */
    public static void deleteFileOrFolder(File path) { 
        if (path.isDirectory()) {
            for (File fileInDir : path.listFiles()) {
                deleteFileOrFolder(fileInDir);
            }
        }
        
        if (path.exists()) {
            path.delete();
        }
    }
}
