package net.f4fs.fspeer;

import net.f4fs.persistence.DHTOperations;
import net.f4fs.persistence.IPersistence;

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
}
