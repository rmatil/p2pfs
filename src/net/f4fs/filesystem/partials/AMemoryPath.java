package net.f4fs.filesystem.partials;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.StructStat.StatWrapper;
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
            // If some data already exists in the DHT, do not update the value of the key
            // (E.g. could be the case, when invoked from FSFileSyncer)
            Data data = peer.getData(Number160.createHash(getPath()));

            if (null != data) {
                logger.info("MemoryPath with name '" + name + "' already existed in the DHT on path '" + getPath() + "'. Creating it locallay...");
                return;
            }

            peer.putData(Number160.createHash(getPath()), new Data(new byte[0]));
            peer.putPath(Number160.createHash(getPath()), new Data(getPath()));

        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            logger.warning("Could not create MemoryPath with name '" + name + "' on path '" + getPath() + "'. Message: " + e.getMessage());
        }
    }
    
    /**
     * Submits a symbolic link to the DHT named as provided in <b>target</b> pointing
     * to the file located represented by <b>existingPath</b>.
     * 
     * <p style="color:red">Note: Does not add the content to the local disk</p>
     * 
     * @param existingPath The already existing path on disk to which the link should point
     * @param target The name of the symbolic link
     * @param parent The parent directory in which the link should be placed
     * @param peer The peer
     */
    public AMemoryPath(final AMemoryPath existingPath, final String target, final MemoryDirectory parent, FSPeer peer) {
        this.name = target;
        this.parent = parent;
        this.peer = peer;

        try {
            // a symbolic link must contain the name of the target as content
            // as stated in <code>man ln</code>
            peer.putData(Number160.createHash(getPath()), new Data(target.getBytes()));
            
            // create the symlink to the target
            peer.putPath(Number160.createHash(getPath()), new Data(existingPath.getPath()));

        } catch (InterruptedException | IOException | ClassNotFoundException e) {
           logger.warning("Could not create symlink '" + target + "' on path '" + getPath() + "'");
        }
    }

    public synchronized void delete() {
        if (parent != null) {
            try {
                String path = getPath();
                
                peer.removePath(Number160.createHash(path));
                peer.removeData(Number160.createHash(path));

                // be aware that this must be after getPath() 
                // otherwise the parent dir will 
                // be empty and another file gets deleted
                parent.deleteChild(this);
                parent = null;

                logger.info("Removed file on path " + path + " from the DHT");
            } catch (InterruptedException e) {
                logger.warning("Could not remove file on path " + getPath() + ". Message: " + e.getMessage());
            }
        }
    }

    public AMemoryPath find(String path) {
        path.replace("^/*", ""); // removes / suffixes.
        if (path.equals(name) || path.isEmpty()) {
            return this;
        }
        return null;
    }

    public abstract void getattr(StatWrapper stat);

    /**
     * Renames the memory path to the given name.
     * Includes removal of the old path on the DHT and storing it on the new one.
     * Transfers content to it.
     * 
     * @param newName The new name
     */
    public synchronized void rename(String newName) {
        while (newName.startsWith("/")) {
            newName = newName.substring(1);
        }

        String oldName = this.name;
        try {
            Data data = peer.getData(Number160.createHash(getPath()));

            ByteBuffer content = null;
            if (null == data) {
                // memoryPath is a directory and has no content
                content = ByteBuffer.wrap(new byte[0]);
            } else {
                // memoryPath is a file
                content = ByteBuffer.wrap(data.toBytes()); // content stores some bytes as string
            }

            // remove content key and the corresponding value from the dht
            // Note: remove path first to prevent inconsistent state
            peer.removePath(Number160.createHash(getPath()));
            peer.removeData(Number160.createHash(getPath()));

            this.name = newName;

            // update content key and store the files content on the updated key again
            peer.putData(Number160.createHash(getPath()), new Data(content.array()));
            peer.putPath(Number160.createHash(getPath()), new Data(getPath()));
            
            logger.info("Renamed file with name '" + oldName + "' to '" + newName + "' on path '" + getPath() + "'.");
        } catch (InterruptedException | ClassNotFoundException | IOException e) {
            logger.warning("Could not rename to '" + newName + "' on path '" + getPath() + "'. Message: " + e.getMessage());
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
