package net.f4fs.persistence;

import java.io.IOException;
import java.util.Set;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


/**
 * Provides an interface to get, put and remove
 * paths to the DHT.
 */
public interface IPathPersistence {

    /**
     * Retrieves all content keys currently stored on the DHT
     * 
     * @param pPeer Peer from which to get the DHT data
     * @return A set containing all paths to the data
     * 
     * @throws InterruptedException If a failure happened during await of future
     * @throws IOException If cast to string set failed
     * @throws ClassNotFoundException If cast to string set failed
     */
    public Set<String> getAllPaths(PeerDHT pPeer)
            throws InterruptedException, ClassNotFoundException, IOException;


    /**
     * Retrieves the path on the given content key
     * 
     * @param pPeer Peer from which to get the path
     * @param pContentKey The key which identifies the provided path in the DHT
     * 
     * @return The path found on the given key
     * 
     * @throws InterruptedException If a failure happened during await of future
     * @throws ClassNotFoundException If cast to string failed
     * @throws IOException If cast to string failed
     */
    public String getPath(PeerDHT pPeer, Number160 pContentKey)
            throws InterruptedException, ClassNotFoundException, IOException;

    /**
     * Puts the given data on the specified content key into the DHT.
     * 
     * @param pPeer Peer which gets used to store data in the DHT
     * @param pContentKey The key which identifies the provided data in the DHT
     * @param pValue The path which should get stored
     * 
     * @throws InterruptedException If a failure happened during await of future
     */
    public void putPath(PeerDHT pPeer, Number160 pContentKey, Data pValue)
            throws InterruptedException;

    /**
     * Removes a path from the DHT which is identified by the given key
     * 
     * @param pPeer The peer which provides access to the DHT
     * @param pContentKey The key of the data which should be removed
     * 
     * @throws InterruptedException If a failure happened during await of future
     */
    public void removePath(PeerDHT pPeer, Number160 pContentKey)
            throws InterruptedException;

}
