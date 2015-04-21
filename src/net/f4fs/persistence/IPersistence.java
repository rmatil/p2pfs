package net.f4fs.persistence;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public interface IPersistence {
    
    /**
     * Get (latest) data from the peer with the given location key
     * 
     * @param pPeer Peer to fetch data from
     * @param pLocationKey Key from file
     *  
     * @return The fetched file as Data 
     */
    public Data getData(PeerDHT pPeer, Number160 pLocationKey);
    
    /**
     * Get the specified version of the file identified by its key.
     * In case, versions are not supported, get the only data.
     * 
     * @param pPeer Peer to fetch data from
     * @param pLocationKey Key from file
     * @param pVersionKey Version key
     * 
     * @return The fetched file as Data
     */
    public Data getDataOfVersion(PeerDHT pPeer, Number160 pLocationKey, Number160 pVersionKey);
    
    /**
     * Puts a new data object with the given location key to the DHT
     * 
     * @param pPeer Peer
     * @param pLocationKey Location key of the file
     * @param pData The content of the file
     */
    public void putData(PeerDHT pPeer, Number160 pLocationKey, Data pData);
    
    /**
     * Removes the specified data from the DHT.
     * In case versions are supported, remove all versions.
     *   
     * @param pPeer Peer
     * @param pKey Key which identifies the file to remove
     */
    public void removeData(PeerDHT pPeer, Number160 pKey);
    
    /**
     * Removes the data with the given version key. In case versions 
     * are not supported, remove just the file
     * 
     * @param pPeer Peer
     * @param pKey Key which identifies the file to remove
     * @param pVersionKey The version to delete
     */
    public void removeData(PeerDHT pPeer, Number160 pKey, Number160 pVersionKey);
}
