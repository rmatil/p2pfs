package net.f4fs.persistence.data;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.f4fs.config.Config;
import net.f4fs.fspeer.GetListener;
import net.f4fs.fspeer.RemoveListener;
import net.f4fs.persistence.PersistenceFactory;
import net.f4fs.persistence.path.IPathPersistence;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ConsensusDHTOperations retrieves and stores data into the DHT only after all other peers agree on the latest content.
 * 
 * @author Christian
 */
public class ConsensusDHTOperations
        implements IDataPersistence {

    private static final IDataPersistence directDHTOperations = PersistenceFactory.getDhtOperations();
    private static final int              NUMBER_OF_RETRIES   = 10;
    private static final int              SLEEP_TIME          = 500;
    private static Logger                 logger              = LoggerFactory.getLogger(ConsensusDHTOperations.class);


    /**
     * Retrieves the data to a requested location key from the DHT.
     * 
     * @param pPeer local DHT of the peer
     * @param pLocationKey location key of the requested entry
     * 
     * @return Data with data that all peers agree on otherwise null.
     * @throws InterruptedException
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    @Override
    public Data getData(PeerDHT pPeer, Number160 pLocationKey)
            throws InterruptedException, ClassNotFoundException, IOException {

        Pair<Number640, Data> pair = null;

        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {

            FutureGet futureGet = pPeer.get(pLocationKey).start();
            futureGet.addListener(new GetListener(pPeer.peerAddress().inetAddress().toString(), "Get data for location key " + pLocationKey.toString(true)));
            futureGet.await();

            // Check if all the peers agree on the same latest version, if no wait for a while and try again
            pair = checkVersions(futureGet.rawData());

            if (pair != null) {
                // Peers already agree
                break;
            }

            logger.info("getData: Peers did not agree on version - Retry :" + i + " of " + NUMBER_OF_RETRIES);
            Thread.sleep(SLEEP_TIME);
        }


        if (pair == null || pair.element1() == null) {
            // Retries are over and peers still didn't agree
            return null;
        }

        // Peers agreed with the following data
        return pair.element1();
    }


    /**
     * Stores a data entry in the DHT under the location key.
     * If there is an inconsistency the function attempts to wait before writing.
     * 
     * @param pPeer local DHT of the peer
     * @param pLocationKey location key of the data to save
     * @param pData Data element
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Override
    public void putData(PeerDHT pPeer, Number160 pLocationKey, Data pData)
            throws InterruptedException, ClassNotFoundException, IOException {

        // Check if path key already exists
        FutureGet fg = pPeer.get(pLocationKey).getLatest().start().awaitUninterruptibly();
        if (fg.data() == null || getData(pPeer, pLocationKey) != null) {
            // Path doesn't exist yet or Path exists and all peers agree -> direct putPath function is used
            directDHTOperations.putData(pPeer, pLocationKey, pData);
            logger.info("Direct putData to DHT");
            return;
        }

        // Path exists but peers don't agree -> waiting for DHT to settle
        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {

            if (getData(pPeer, pLocationKey) != null) {
                // peers finally agree
                break;
            }

            logger.info("putData: Peers did not agree on version - Retry :" + i + " of " + NUMBER_OF_RETRIES);
            Thread.sleep(SLEEP_TIME);
        }

        // writing even if they still disagree
        directDHTOperations.putData(pPeer, pLocationKey, pData);
        logger.info("Direct putData to DHT");

    }


    /**
     * Removes the data entry of a specific location key
     * 
     * @param pPeer local DHT of the peer
     * @param pLocationKey location key of the data to be removed
     */
    @Override
    public void removeData(PeerDHT pPeer, Number160 pLocationKey)
            throws InterruptedException {

        FutureRemove futureRemove = pPeer.remove(pLocationKey).start();
        futureRemove.addListener(new RemoveListener(pPeer.peerAddress().inetAddress().toString(), "Remove data"));

        futureRemove.await();
    }


    /**
     * Check if all other peers agree with the local version.
     * 
     * @param rawData of FutureGet request
     * 
     * @return a new Pair with the latest Key & latest Data if all other peers agree on it null otherwise
     */
    private static <K> Pair<Number640, K> checkVersions(Map<PeerAddress, Map<Number640, K>> rawData) {
        Number640 latestKey = null;
        K latestData = null;

        for (Map.Entry<PeerAddress, Map<Number640, K>> entry : rawData.entrySet()) {
            if (latestData == null && latestKey == null) {
                latestData = entry.getValue().values().iterator().next();
                latestKey = entry.getValue().keySet().iterator().next();
            } else {
                if (!latestKey.equals(entry.getValue().keySet().iterator().next())
                        || !latestData.equals(entry.getValue().values().iterator().next())) {

                    return null;
                }
            }
        }

        return new Pair<Number640, K>(latestKey, latestData);
    }


    @Override
    public Data getDataOfVersion(PeerDHT pPeer, Number160 pLocationKey, Number160 pVersionKey)
            throws InterruptedException {
        // unused
        return null;
    }


    @Override
    public void removeDataOfVersion(PeerDHT pPeer, Number160 pKey, Number160 pVersionKey)
            throws InterruptedException {
        // unused
        
    }

}
