package net.f4fs.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.DirectoryFiller;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class MemoryDirectory
        extends AMemoryPath {

    private Logger                  logger   = Logger.getLogger("MemoryDirectory.class");
    private final List<AMemoryPath> contents = new ArrayList<AMemoryPath>();

    public MemoryDirectory(final String name, FSPeer peer) {
        super(name, peer);
        logger.info("Created Directory '" + name + "' without parent");
    }

    public MemoryDirectory(final String name, final MemoryDirectory parent, FSPeer peer) {
        super(name, parent, peer);
        logger.info("Created Directory '" + name + "' in parent " + parent.getName());
    }

    public synchronized void add(final AMemoryPath p) {
        // TODO: hope, that removal from old dir is done from memory path when it is moved
        // TODO: what about the content if p is a file?

        // remove dir from old place
//        super.getPeer().remove(new Number160(HexFactory.stringToHex(getPath())));

        try {
            // update the value of getPath of p
            p.setParent(this);
            // try to store the new memory segment in DHT
            FuturePut futurePut = super.getPeer().put(Number160.createHash(getPath()), new Data(""));
            futurePut.awaitUninterruptibly();
            futurePut = super.getPeer().putContentKey(Number160.createHash(getPath()), new Data(getPath()));
            futurePut.awaitUninterruptibly();

            contents.add(p);
            logger.info("Added MemoryPath " + p.getPath() + " to " + getPath());
        } catch (IOException e) {
            logger.warning("Could not add MemoryPath " + p.getPath() + " to " + getPath() + ". Message: " + e.getMessage());
            // remove parent again
            p.setParent(null);
        }
    }

    public synchronized void deleteChild(final AMemoryPath child) {
        boolean ret = contents.remove(child);

//        if (ret) {
//            // file was contained in contents
//            super.getPeer().remove(new Number160(HexFactory.stringToHex(getPath())));
//            logger.info("Removed MemoryPath " + child.getPath() + " from " + super.getPath());
//        }
    }

    @Override
    protected AMemoryPath find(String path) {
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
    protected void getattr(final StatWrapper stat) {
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

}
