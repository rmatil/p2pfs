package net.f4fs.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.f4fs.config.FSStatConfig;
import net.f4fs.filesystem.partials.AMemoryPath;
import net.f4fs.filesystem.partials.MemoryDirectory;
import net.f4fs.filesystem.partials.MemoryFile;
import net.f4fs.filesystem.util.FSFileSyncer;
import net.f4fs.filesystem.util.FSFileUtils;
import net.f4fs.fspeer.FSPeer;
import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.StructStatvfs.StatvfsWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.util.FuseFilesystemAdapterFull;


/**
 * 
 * 
 * <b>Description of idea</b>: storing each path as [location key: hash('keys')], [content key: hash(path)] and [value: path]
 * and storing the path a second time as [location key: path] and [value: file_content]
 * like this it is possible to get all keys with new GetBuilder(hash('keys')).keys() --> returns a map with all [hash(path), path]
 * 
 */

public class P2PFS
        extends FuseFilesystemAdapterFull {

    /**
     * Root directory relative to the mount point
     */
    private final MemoryDirectory rootDirectory;

    /**
     * Logger instance
     */
    private static final Logger   logger = Logger.getLogger("P2PFS.class");

    private Thread                fileSyncerThread;

    private FSFileSyncer          fileSyncer;

    /**
     * Creates a new instance of this file system.
     * Enables logging
     * 
     * @param peer Peer which mounts this filesystem
     * 
     * @throws IOException
     */
    public P2PFS(FSPeer peer)
            throws IOException {

        rootDirectory = new MemoryDirectory("/", peer);

        // start thread to get all file keys from dht
        fileSyncer = new FSFileSyncer(this, peer);
        fileSyncerThread = new Thread(fileSyncer);
        fileSyncerThread.start(); // use start() instead of run()

        super.log(false);
    }

    /**
     * Check file access permissions. All path segments get
     * checked for the provided access permissions
     * 
     * @param path Path to access
     * @param access Access mode flags
     */
    @Override
    public int access(final String path, final int access) {
        return 0;
    }

    /**
     * Removes the mount point directory on disk.
     * Gets called in {@link net.fusejna.FuseFilesystem#_destroy} after the Filesystem
     * is unmounted.
     */
    @Override
    public void afterUnmount(final File mountPoint) {
        if (mountPoint.exists()) {
            FSFileUtils.deleteFileOrFolder(mountPoint);
        }

        if (null != fileSyncerThread) {
            // indicate stop flag on runnable
            fileSyncer.terminate();

            try {
                // wait until run() of runnable is terminated
                fileSyncerThread.join();
                logger.info("FSFileSyncer stopped successfully");
            } catch (InterruptedException e) {
                logger.warning("Could not terminate FSFileSyncer properly");
            }
        }
    }

    /**
     * Gets called in {@link net.fusejna.FuseFilesystem#mount} before FuseJna
     * explicitly mounts the file system
     */
    @Override
    public void beforeMount(final File mountPoint) {
    }

    /**
     * Used to map a block number offset in a file to
     * the physical block offset on the block device
     * backing the file system. This is intended for
     * filesystems that are stored on an actual block
     * device, with the 'blkdev' option passed.
     */
    @Override
    public int bmap(final String path, final FileInfoWrapper info) {
        return 0;
    }


    /**
     * Change permissions represented by mode on the
     * file / directory / simlink / device on path
     * 
     * @param path The path to the file of which permissions should be changed
     * @param mode Permissions which should get applied
     */
    @Override
    public int chmod(final String path, final ModeWrapper mode) {
        // TODO: set permissions
        // TODO: how to store file permissions in dht?
        return 0;
    }

    /**
     * Changes the ownership
     * of the file / directory / simlink / device specified at path
     * 
     * @param path Path to file to change ownership
     * @param uid Id of the user
     * @param gid Id of the group
     */
    @Override
    public int chown(final String path, final long uid, final long gid) {
        // TODO: set ownership
        // TODO: how to store file ownership in dht?
        return 0;
    }

    /**
     * Create a file with the path indicated, then open a
     * handle for reading and/or writing with the supplied
     * mode flags. Can also return a file handle like open()
     * as part of the call.
     * 
     * @param path Path to file to create
     * @param mode Create mask
     * @param info Open mode flags
     */
    @Override
    public int create(final String path, final ModeWrapper mode, final FileInfoWrapper info) {
        if (getPath(path) != null) {
            logger.info("File could not be created. A file with the same name already exists (Error code " + -ErrorCodes.EEXIST() + ").");
            return -ErrorCodes.EEXIST();
        }
        final AMemoryPath parent = getParentPath(path);
        if (parent instanceof MemoryDirectory) {

            String fileName = getLastComponent(path);
            // check if it is a file based on the filename
            if (isFile(fileName)) {
                ((MemoryDirectory) parent).mkfile(getLastComponent(path));
            } else {
                ((MemoryDirectory) parent).mkdir(getLastComponent(path));
            }

            logger.info("Created file on path: " + path);
            return 0;
        }

        logger.warning("File could not be created. No such file or directory (Error code " + -ErrorCodes.ENOENT() + ").");
        return -ErrorCodes.ENOENT();
    }

    /**
     * Sets different statistics about the entity located at path
     * like remaining capacity in the given StatWrapper.
     * 
     * @param path The path to the file of which the information gets obtained
     * @param stat The wrapper in which the particular information gets stored
     */
    @Override
    public int getattr(final String path, final StatWrapper stat) {
        final AMemoryPath p = getPath(path);
        if (p != null) {
            p.getattr(stat);
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }

    /**
     * Returns the last substring delimited by <i>/</i>.
     * 
     * @param path The path of which to get the last part
     * @return The last part delimited by <i>/</i>
     */
    private String getLastComponent(String path) {
        while (path.substring(path.length() - 1).equals("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return "";
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

    /**
     * Get path to parent from provided path
     * 
     * @param path Path of which to get the path to its parent
     * @return The parent path
     */
    private AMemoryPath getParentPath(final String path) {
        return rootDirectory.find(path.substring(0, path.lastIndexOf("/")));
    }

    public AMemoryPath getPath(final String path) {
        return rootDirectory.find(path);
    }

    @Override
    public int mkdir(final String path, final ModeWrapper mode) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        final AMemoryPath parent = getParentPath(path);
        if (parent instanceof MemoryDirectory) {
            ((MemoryDirectory) parent).mkdir(getLastComponent(path));
            return 0;
        }

        return -ErrorCodes.ENOENT();
    }

    /**
     * Checks whether the user has the correct access rights
     * to open the file on the provided path
     * 
     * @param path The path of the file to check
     * @param info The FileInfoWrapper which has to be updated
     */
    @Override
    public int open(final String path, final FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            logger.warning("Failed to read file on " + path + ". No such file or directory (Error code " + -ErrorCodes.ENOENT() + ").");
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryFile)) {
            logger.warning("Failed to read file on " + path + ". Path is a directory (Error code " + -ErrorCodes.EISDIR() + ").");
            return -ErrorCodes.EISDIR();
        }

        logger.info("Read file on path " + path);

        return ((MemoryFile) p).read(buffer, size, offset);
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        ((MemoryDirectory) p).read(filler);
        return 0;
    }

    @Override
    public int rename(final String path, final String newName) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        final AMemoryPath newParent = getParentPath(newName);
        if (newParent == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(newParent instanceof MemoryDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.rename(newName.substring(newName.lastIndexOf("/")));
        return 0;
    }

    @Override
    public int rmdir(final String path) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        return 0;
    }

    @Override
    public int truncate(final String path, final long offset) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryFile)) {
            return -ErrorCodes.EISDIR();
        }
        ((MemoryFile) p).truncate(offset);
        return 0;
    }

    @Override
    public int unlink(final String path) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        p.delete();
        return 0;
    }

    @Override
    public int write(final String path, final ByteBuffer buf, final long bufSize, final long writeOffset,
            final FileInfoWrapper wrapper) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            logger.warning("Could not write to file on path " + path + ". No such file or directory (Error code " + -ErrorCodes.ENOENT() + ").");
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryFile)) {
            logger.warning("Could not write to file on path " + path + ". Path is a directory (Error code " + -ErrorCodes.EISDIR() + ").");
            return -ErrorCodes.EISDIR();
        }

        logger.info("Wrote to file on path " + path);

        return ((MemoryFile) p).write(buf, bufSize, writeOffset);
    }

    @Override
    public int statfs(final String path, final StatvfsWrapper wrapper) {
        wrapper.bsize(FSStatConfig.DEFAULT.getBsize()); // block size of 4000 bytes
        wrapper.blocks(FSStatConfig.DEFAULT.getBlocks()); // TODO: manually update this, when a new peer joins
        wrapper.bfree(FSStatConfig.DEFAULT.getBfree());
        wrapper.bavail(FSStatConfig.DEFAULT.getBavail());
        wrapper.files(FSStatConfig.DEFAULT.getFiles());
        wrapper.ffree(FSStatConfig.DEFAULT.getFfree());
        return 0;
    }

    /**
     * Creates the provided mount point if it does not exists already.
     * Then mounts the filesystem at the mountpoint
     * 
     * @param mountPoint The mountpoint where to mount the FS
     * @return The mounted P2PFS
     * 
     * @throws FuseException
     */
    public P2PFS mountAndCreateIfNotExists(String mountPoint)
            throws FuseException {
        File file = new File(mountPoint);
        if (!file.exists()) {
            logger.info("Created mount point directory at path " + mountPoint + ".");
            file.mkdir();
        }

        this.mount(file);

        return this;
    }

    /**
     * Returns a set of paths which are saved on the local FS
     * 
     * @return All paths to locally existent files
     */
    public Set<String> getAllPaths() {
        Set<String> allPaths = new HashSet<>();

        List<AMemoryPath> contents = rootDirectory.getContents();
        for (AMemoryPath path : contents) {
            allPaths.add(path.getName());
        }

        return allPaths;
    }

    /**
     * Checks if the provided file is a file or a directory
     * based on the existence of a dot in the last component of the path.
     * 
     * @param path The path to check
     * @return True if it is a file, false otherwise
     */
    public boolean isFile(String path) {
        String lastCompoment = getLastComponent(path);

        if (!lastCompoment.contains(".")) {
            return false;
        }

        return true;
    }
}
