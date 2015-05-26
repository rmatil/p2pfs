package net.f4fs.persistence.path;

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
 * ConsensusPathOperations retrieves and stores path keys only after all other peers agree on the latest content.
 * 
 * @author Christian
 */
public class ConsensusPathOperations
        implements IPathPersistence {

    private static final IPathPersistence directPathOperations = PersistenceFactory.getDirectPathOperations();
    private static final int              NUMBER_OF_RETRIES    = 10;
    private static final int              SLEEP_TIME           = 500;
    private static Logger                 logger               = LoggerFactory.getLogger(ConsensusPathOperations.class);


    /**
     * Retrieves all path strings from the DHTs master location path key.
     * 
     * @param pPeer local DHT of the peer
     * @param pContentKey content key of the requested entry
     * 
     * @return keys Map<string> containing all paths currently in the DHT & that all peers agree on otherwise null.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Override
    public Set<String> getAllPaths(PeerDHT pPeer)
            throws InterruptedException, ClassNotFoundException, IOException {

        Set<String> keys = new HashSet<>();

        FutureGet futureGet = pPeer.get(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).all().start();
        futureGet.addListener(new GetListener(pPeer.peerAddress().inetAddress().toString(), "Get all paths"));
        futureGet.await();

        Map<Number640, Data> map = futureGet.dataMap();
        Collection<Data> collection = map.values();

        Iterator<Data> iter = collection.iterator();
        while (iter.hasNext()) {
            keys.add((String) iter.next().object());
        }

        return keys;
    }

    /**
     * Retrieves the path string to a requested content key from the DHTs master location path key.
     * 
     * @param pPeer local DHT of the peer
     * @param pContentKey content key of the requested entry
     * 
     * @return String with path that all peers agree on otherwise null.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Override
    public String getPath(PeerDHT pPeer, Number160 pContentKey)
            throws InterruptedException, ClassNotFoundException, IOException {

        Pair<Number640, Data> pair = null;

        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {

            FutureGet futureGet = pPeer.get(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).contentKey(pContentKey).start();
            futureGet.addListener(new GetListener(pPeer.peerAddress().inetAddress().toString(), "Get path for content key " + pContentKey.toString(true)));
            futureGet.await();

            // Check if all the peers agree on the same latest version, if no wait for a while and try again
            pair = checkVersions(futureGet.rawData());

            if (pair != null) {
                // Peers already agree
                break;
            }

            logger.info("getPath: Peers did not agree on version - Retry :" + i + " of " + NUMBER_OF_RETRIES);
            Thread.sleep(SLEEP_TIME);
        }


        if (pair == null || pair.element1() == null) {
            // Retries are over and peers still didn't agree
            return null;
        }

        // Peers agreed with the following data
        return (String) pair.element1().object();
    }


    /**
     * Stores a path entry in the DHT under the master location path key.
     * If there is an inconsistency the function attempts to wait before writing.
     * 
     * @param pPeer local DHT of the peer
     * @param pContentKey content key of the data to save
     * @param pData path string wrapped in a Data element
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Override
    public void putPath(PeerDHT pPeer, Number160 pContentKey, Data pData)
            throws InterruptedException, ClassNotFoundException, IOException {

        // Check if path key already exists
        FutureGet fg = pPeer.get(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).contentKey(pContentKey).getLatest().start().awaitUninterruptibly();
        if (fg.data() == null || getPath(pPeer, pContentKey) != null) {
            // Path doesn't exist yet or Path exists and all peers agree -> direct putPath function is used
            directPathOperations.putPath(pPeer, pContentKey, pData);
            logger.info("Direct putPath to DHT");
            return;
        }

        // Path exists but peers don't agree -> waiting for DHT to settle
        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {

            if (getPath(pPeer, pContentKey) != null) {
                // peers finally agree
                break;
            }

            logger.info("putPath: Peers did not agree on version - Retry :" + i + " of " + NUMBER_OF_RETRIES);
            Thread.sleep(SLEEP_TIME);
        }

        // writing even if they still disagree
        directPathOperations.putPath(pPeer, pContentKey, pData);
        logger.info("Direct putPath to DHT");

    }


    /**
     * Removes the path of a specific content key
     * 
     * @param pPeer local DHT of the peer
     * @param pContentKey content key of the path to be removed
     */
    @Override
    public void removePath(PeerDHT pPeer, Number160 pContentKey)
            throws InterruptedException {

        FutureRemove futureRemove = pPeer.remove(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).contentKey(pContentKey).start();
        futureRemove.addListener(new RemoveListener(pPeer.peerAddress().inetAddress().toString(), "Remove path"));

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

}
