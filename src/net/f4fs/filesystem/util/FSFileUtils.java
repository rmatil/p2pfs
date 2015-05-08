package net.f4fs.filesystem.util;

import java.io.File;

import net.f4fs.filesystem.partials.AMemoryPath;
import net.f4fs.filesystem.partials.MemoryDirectory;


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
    
    /**
     * Returns the last substring delimited by <i>/</i>.
     * 
     * @param pPath The path of which to get the last part
     * @return The last part delimited by <i>/</i>
     */
    public static String getLastComponent(String pPath) {
        while (pPath.substring(pPath.length() - 1).equals("/")) {
            pPath = pPath.substring(0, pPath.length() - 1);
        }
        if (pPath.isEmpty()) {
            return "";
        }
        return pPath.substring(pPath.lastIndexOf("/") + 1);
    }
    
    /**
     * Checks if the provided file is a file or a directory
     * based on the existence of a dot in the last component of the path.
     * 
     * @param pPath The path to check
     * @return True if it is a file, false otherwise
     */
    public static boolean isFile(String pPath) {
        String lastCompoment = getLastComponent(pPath);

        if (!lastCompoment.contains(".")) {
            // does not contain a dot -> dir
            return false;
        }

        if (lastCompoment.startsWith(".") && !lastCompoment.substring(1).contains(".")) {
            // is a hidden directory
            return false;
        }

        return true;
    }
    
    /**
     * Returns true, if the given Path is contained
     * in a version folder, i.e. a folder starting
     * with a dot.
     * 
     * @param pPath Path segment to check
     * @return True, if contained, otherwise false
     */
    public static boolean isContainedInVersionFolder(AMemoryPath pPath) {
        MemoryDirectory parentDir = pPath.getParent();
        if (parentDir.getName().startsWith(".")) {
            return true;
        }
        
        return false;   
    }
}
