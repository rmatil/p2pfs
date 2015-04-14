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
        stat.setMode(NodeType.DIRECTORY);
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

    public List<AMemoryPath> getContents() {
        return contents;
    }
}
