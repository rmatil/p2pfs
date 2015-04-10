package net.f4fs.filesystem;

import java.io.IOException;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.StructStat.StatWrapper;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public abstract class AMemoryPath {

    /**
     * Logger instance
     */
    private static final Logger logger = Logger.getLogger("AMemoryPath.class");

    private String              name;
    private MemoryDirectory     parent;
    /**
     * The peer which mounted this file system
     */
    private FSPeer              peer;

    public AMemoryPath(final String name, final FSPeer peer) {
        this(name, null, peer);
    }

    public AMemoryPath(final String name, final MemoryDirectory parent, final FSPeer peer) {
        this.name = name;
        this.parent = parent;
        this.peer = peer;

        // Store an empty element
        try {
            FuturePut futurePut = peer.put(Number160.createHash(getPath()), new Data(""));
            futurePut.awaitUninterruptibly();
            futurePut = peer.putContentKey(Number160.createHash(getPath()), new Data(getPath()));
            futurePut.awaitUninterruptibly();

            logger.info("Created new MemoryPath " + name + " successfully on path " + getPath());
        } catch (IOException e) {
            logger.warning("Could not create MemoryPath " + name + ". Message: " + e.getMessage());
        }
    }

    public synchronized void delete() {
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
            try {
                FutureRemove futureRemove = peer.remove(Number160.createHash(getPath()));
                futureRemove.await();
                futureRemove = peer.removeContentKey(Number160.createHash(getPath()));
                futureRemove.await();
            } catch (InterruptedException e) {
                logger.warning("Could not delete MemoryPath " + name + ". Message: " + e.getMessage());
            }
        }
    }

    protected AMemoryPath find(String path) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.equals(name) || path.isEmpty()) {
            return this;
        }
        return null;
    }

    protected abstract void getattr(StatWrapper stat);

    /**
     * Renames the memory path to the given name
     * 
     * @param newName The new name
     */
    public void rename(String newName) {
        while (newName.startsWith("/")) {
            newName = newName.substring(1);
        }

        String oldName = this.name;
        try {
            FutureGet futureGet = peer.get(Number160.createHash(getPath()));
            futureGet.awaitUninterruptibly();

            Object content = new Object();
            if (null == futureGet.data()) {
                // memoryPath is a directory and has no content
                content = new String("");
            } else {
                content = futureGet.data().object();
            }

            // remove content key and the corresponding value from the dht
            peer.remove(Number160.createHash(getPath()));
            peer.removeContentKey(Number160.createHash(getPath()));

            name = newName;

            // update content key and store the files content on the updated key again
            peer.put(Number160.createHash(getPath()), new Data(content));
            peer.putContentKey(Number160.createHash(getPath()), new Data(getPath()));
        } catch (ClassNotFoundException | IOException e) {
            logger.warning("Could not rename to " + newName + ". Message: " + e.getMessage());
            // reset in case renaming didn't work as expected
            name = oldName;
        }
    }


    /**
     * Returns the name of this path segment
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public MemoryDirectory getParent() {
        return parent;
    }

    public void setParent(MemoryDirectory pParent) {
        parent = pParent;
    }

    public FSPeer getPeer() {
        return this.peer;
    }

    public void setPeer(FSPeer peer) {
        this.peer = peer;
    }

    /**
     * Returns the path of this path segment (incl. its name)
     * 
     * @return The memory path
     */
    public String getPath() {
        String path = this.name;

        if (null != this.parent) {
            if ("/" != this.parent.getPath()) {
                path = this.parent.getPath() + "/" + path;
            } else {
                path = "/" + path;
            }
        }

        return path;
    }
}
