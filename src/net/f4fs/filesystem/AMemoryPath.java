package net.f4fs.filesystem;

import java.io.IOException;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.StructStat.StatWrapper;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public abstract class AMemoryPath {
    
    /**
     * Logger instance
     */
    private static final Logger   logger = Logger.getLogger("AMemoryPath.class");

    private String          name;
    private MemoryDirectory parent;
    /**
     * The peer which mounted this file system
     */
    private FSPeer          peer;

    public AMemoryPath(final String name, final FSPeer peer)
    {
        this(name, null, peer);
    }

    public AMemoryPath(final String name, final MemoryDirectory parent, final FSPeer peer)
    {
        this.name = name;
        this.parent = parent;
        this.peer = peer;
        
        // Store an empty element
        peer.put(new Number160(getPath()), new Data());
    }

    public synchronized void delete()
    {
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
            peer.remove(new Number160(this.getPath()));
            peer = null;
        }
    }

    protected AMemoryPath find(String path)
    {
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
        
        try {
             Object content = peer.get(new Number160(this.getPath()));

             peer.remove(new Number160(this.getPath()));
             peer.put(new Number160(newName), new Data(content));

             name = newName;
        } catch (ClassNotFoundException | IOException e) {
           logger.warning("Could not rename to " + newName + ". Message: " + e.getMessage());
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
            path = this.parent.getPath() + "/" + path;
        }
        
        return path;
    }
}
