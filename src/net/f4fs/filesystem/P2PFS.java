package net.f4fs.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.StructStatvfs.StatvfsWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.util.FuseFilesystemAdapterFull;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


/*
 * TODO: 
 * Put and Get of files into file system
 * 
 * IDEA: storing each path as [location key: hash('keys')], [content key: hash(path)] and [value: path]
 * and storing the path a second time as [location key: path] and [value: file_content]
 * like this it is possible to get all keys with new GetBuilder(hash('keys')).keys() --> returns a map with all [hash(path), path]
 * 
 */
public class P2PFS
        extends FuseFilesystemAdapterFull {

    FSPeer _peer;
    private final MemoryDirectory rootDirectory = new MemoryDirectory("");

    public P2PFS(FSPeer peer)
            throws IOException {
       
        _peer = peer;
        
        String filename = "README.txt";
        String filecontent = "Welcome to the p2p filesystem of f4fs.";
        rootDirectory.add(new MemoryFile(filename, filecontent));
        
        //super.log(true);
    }

    @Override
    public int access(final String path, final int access) {
        return 0;
    }

    @Override
    public int create(final String path, final ModeWrapper mode, final FileInfoWrapper info) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        final AMemoryPath parent = getParentPath(path);
        if (parent instanceof MemoryDirectory) {
            ((MemoryDirectory) parent).mkfile(getLastComponent(path));
            
            _peer.put(Number160.createHash(path), new Data()); 
            return 0;
        }

        return -ErrorCodes.ENOENT();
    }

    @Override
    public int getattr(final String path, final StatWrapper stat) {
        final AMemoryPath p = getPath(path);
        if (p != null) {
            p.getattr(stat);
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }
    

    private String getLastComponent(String path) {
        while (path.substring(path.length() - 1).equals("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return "";
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

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

    @Override
    public int open(final String path, final FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info) {
        final AMemoryPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryFile)) {
            return -ErrorCodes.EISDIR();
        }
        
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
        p.delete();
        p.rename(newName.substring(newName.lastIndexOf("/")));
        ((MemoryDirectory) newParent).add(p);
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
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof MemoryFile)) {
            return -ErrorCodes.EISDIR();
        }
        
        try {
            _peer.put(Number160.createHash(path), new Data(buf));
        } catch (IOException pEx) {
            pEx.printStackTrace();
        }
        
        return ((MemoryFile) p).write(buf, bufSize, writeOffset);
    }
    
    @Override
    public int statfs(final String path, final StatvfsWrapper wrapper) {
        wrapper.bsize(4000L); // block size of 4000 bytes
        wrapper.blocks(1000L); // TODO: manually update this, when a new peer joins
        wrapper.bfree(200L);
        wrapper.bavail(180L);
        wrapper.files(5L);
        wrapper.ffree(29L);
        return 0;
    }
    
    public P2PFS createIfNotExists(String mountPoint) {
        File file = new File(mountPoint);
        if (!file.exists()) {
            file.mkdir();
        }
        
        return this;
    }
}
