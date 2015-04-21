package net.f4fs.fspeer;

import net.f4fs.persistence.DHTOperations;
import net.f4fs.persistence.IPathPersistence;
import net.f4fs.persistence.IPersistence;
import net.f4fs.persistence.PathOperations;

/**
 * Factory to retrieve multiple adapters which differ in how 
 * they manage data storage in the DHT
 */
public class PersistenceFactory {

    /**
     * Returns an adapter to write, read, remove 
     * data from the DHT. 
     * <br>
     * <b>Note:</b> No versions of stored data are supported
     * 
     * @return An adapter to store data without versions
     */
    public IPersistence getDhtOperations() {
        return new DHTOperations();
    }
    
    /**
     * Returns an adapter to store, get and remove
     * paths of files in the DHT
     * 
     * @return The adapter 
     */
    public IPathPersistence getPathPersistence() {
        return new PathOperations();
    }
}
