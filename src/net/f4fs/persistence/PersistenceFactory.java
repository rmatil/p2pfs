package net.f4fs.persistence;

import net.f4fs.persistence.data.ChunkedDHTOperations;
import net.f4fs.persistence.data.ConsensusDHTOperations;
import net.f4fs.persistence.data.DHTOperations;
import net.f4fs.persistence.data.IDataPersistence;
import net.f4fs.persistence.data.VersionedDHTOperations;
import net.f4fs.persistence.path.ConsensusPathOperations;
import net.f4fs.persistence.path.DirectPathOperations;
import net.f4fs.persistence.path.IPathPersistence;


/**
 * Factory to retrieve multiple adapters which differ in how
 * they manage data storage in the DHT
 */
public class PersistenceFactory {

    private static DHTOperations          dhtOperations;
    private static VersionedDHTOperations versionedDhtOperations;
    private static ConsensusDHTOperations consensusDhtOperations;
    private static ChunkedDHTOperations   chunkedDHTOperations;
    private static DirectPathOperations    directPathOperations;
    private static ConsensusPathOperations consensusPathOperations;

    private PersistenceFactory() {
    }

    /**
     * Returns an adapter to write, read, remove
     * data from the DHT. <br>
     * <b>Note:</b> No versions of stored data are supported
     * 
     * @return An adapter to store data without versions
     */
    public synchronized static IDataPersistence getDhtOperations() {
        if (null == dhtOperations) {
            dhtOperations = new DHTOperations();
        }

        return dhtOperations;
    }

    public synchronized static IDataPersistence getVersionedDhtOperations() {
        if (null == versionedDhtOperations) {
            versionedDhtOperations = new VersionedDHTOperations();
        }

        return versionedDhtOperations;
    }

    public synchronized static IDataPersistence getChunkedDhtOperations() {
        if (null == chunkedDHTOperations) {
            chunkedDHTOperations = new ChunkedDHTOperations();
        }

        return chunkedDHTOperations;
    }
    
    public synchronized static IDataPersistence getConsensuDhtOperations() {
        if (null == consensusDhtOperations) {
            consensusDhtOperations = new ConsensusDHTOperations();
        }
        
        return consensusDhtOperations;
    }

    /**
     * Returns an adapter to store, get and remove
     * paths of files directly from the DHT
     * 
     * @return The adapter
     */
    public synchronized static IPathPersistence getDirectPathOperations() {
        if (null == directPathOperations) {
            directPathOperations = new DirectPathOperations();
        }

        return directPathOperations;
    }

    /**
     * Returns an adapter to store, get and remove
     * paths of files through a consensus mechanism from the DHT,
     * i.e. peers agree on the returned value or the value is null
     * 
     * @return The adapter
     */
    public synchronized static IPathPersistence getConsensusPathOperations() {
        if (null == consensusPathOperations) {
            consensusPathOperations = new ConsensusPathOperations();
        }

        return consensusPathOperations;
    }
}
