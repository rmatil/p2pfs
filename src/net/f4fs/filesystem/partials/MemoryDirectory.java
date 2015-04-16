package net.f4fs.filesystem.partials;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.DirectoryFiller;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;


public class MemoryDirectory
        extends AMemoryPath {

    private Logger                  logger   = Logger.getLogger("MemoryDirectory.class");
    private final List<AMemoryPath> contents = new ArrayList<AMemoryPath>();

    public MemoryDirectory(final String name, FSPeer peer) {
        super(name, peer);
        logger.info("Created Directory '" + name + "' without parent on path '" + getPath() + "'.");
    }

    public MemoryDirectory(final String name, final MemoryDirectory parent, FSPeer peer) {
        super(name, parent, peer);
        logger.info("Created Directory '" + name + "' on path '" + getPath() + "'.");
    }

    public synchronized void deleteChild(final AMemoryPath child) {
        contents.remove(child);
    }

    @Override
    public AMemoryPath find(String path) {
        if (super.find(path) != null) {
            return super.find(path);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        synchronized (this) {
            if (!path.contains("/")) {
                for (final AMemoryPath p : contents) {
                    if (p.getName().equals(path)) {
                        return p;
                    }
                }
                return null;
            }
            final String nextName = path.substring(0, path.indexOf("/"));
            final String rest = path.substring(path.indexOf("/"));
            for (final AMemoryPath p : contents) {
                if (p.getName().equals(nextName)) {
                    return p.find(rest);
                }
            }
        }
        return null;
    }

    @Override
    public void getattr(final StatWrapper stat) {
        long currentUnixTimestamp = System.currentTimeMillis() / 1000l;

        // time of last access
        stat.atime(currentUnixTimestamp);
        // time of last data modification
        stat.mtime(currentUnixTimestamp);
        // Time when file status was last changed (inode data modification).
        // Changed by the chmod(2), chown(2), link(2), mknod(2), rename(2), unlink(2), utimes(2) and write(2) system calls.
        stat.ctime(currentUnixTimestamp);

        // ID of device containing file
        // stat.dev(dev);

        // file generation number
        // stat.gen(gen);

        // Group ID of the file
        // stat.gid(gid);

        // Note: if ino and rdev are taken together, they uniquely
        // identify the file among multiple filesystems
        // File serial number
        // stat.ino(ino); // only unique on the current FS
        // Device ID
        // stat.rdev(rdev);

        // Number of hard links which link to this file
        // Hard links are multiple directory entries which link to the same file -> created by link system call.
        // From man link: "A hard link to a file is indistinguishable from the original directory entry; any changes to a file are effectively inde-
        // pendent of the name used to reference the file. Hard links may not normally refer to directories and may not span file systems."
        // TODO: how do we check these?
        // stat.nlink(0);

        // set access modes
        stat.setMode(NodeType.DIRECTORY, true, true, true, true, true, true, true, true, true);

        // NOTE: according to the manual entry of man 2 stat these fields should not be changed
        // RESERVED: DO NOT USE!
        // stat.lspare(lspare);
        // RESERVED: DO NOT USE!
        // stat.qspare(qspare);
    }

    public synchronized void mkdir(final String lastComponent) {
        // stores also the new directory in the DHT with the correct path
        // because this element was set as parent in the constructor
        contents.add(new MemoryDirectory(lastComponent, this, super.getPeer()));
    }

    public synchronized void mkfile(final String lastComponent) {
        // stores also the new file in the DHT with the correct path
        // because this element was set as parent in the constructor
        contents.add(new MemoryFile(lastComponent, this, super.getPeer()));
    }

    public synchronized void read(final DirectoryFiller filler) {
        for (final AMemoryPath p : contents) {
            filler.add(p.getName());
        }
    }

    /**
     * A symbolic link <code>target</code> is created to <code>path</code>.
     * (<code>target</code> is the name of the file created, <code>path</code> is the string used in creating the symbolic link)
     * 
     * @param path The already existing file to which the link should be created
     * @param target The path of the symlink which points to <code>path</code>
     */
    public void symlink(final String path, final String target) {
        // stores also the new directory in the DHT with the correct path
        // because this element was set as parent in the constructor
        contents.add(new MemorySymLink(path, target, this, super.getPeer()));
    }

    public List<AMemoryPath> getContents() {
        return contents;
    }
}
