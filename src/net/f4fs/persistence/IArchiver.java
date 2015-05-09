package net.f4fs.persistence;

import java.io.IOException;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


/**
 * Defines an interface for all archiver within this application.
 * They are responsible to create versions for the old file.
 * 
 * @author Raphael
 *
 */
public interface IArchiver {

    /**
     * Archives the given old data according to the archiver which implements this interface.
     * 
     * @param pPeerDht The PeerDHT to access the current state of the DHT
     * @param pLocationKey The location key of the file to archive
     * @param pOldFile The data of the old file
     * 
     * @throws ClassNotFoundException When fetching/putting data to the DHT fails
     * @throws IOException If the version folder could not be retrieved
     * @throws InterruptedException If the thread of fetching/putting data from/to the DHT has been interrupted
     */
    public void archive(PeerDHT pPeerDht, Number160 pLocationKey, Data pOldFile)
            throws ClassNotFoundException, IOException, InterruptedException;

}
