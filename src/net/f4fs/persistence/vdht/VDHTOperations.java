package net.f4fs.persistence.vdht;

import net.f4fs.fspeer.PersistenceFactory;
import net.f4fs.fspeer.RemoveListener;
import net.f4fs.persistence.IPersistence;
import net.f4fs.persistence.VersionArchiver;
import net.f4fs.util.RandomDevice;
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

import java.io.IOException;
import java.util.Map;
import java.util.Random;


/**
 * VDHTOperations adds automated versioning functionality to the integrated peerDHT.
 * Handling the inter-peer negotiations for consensus.
 * Based on the VDHT example of Thomas Bocek's TomP2P.
 * 
 * @author Christian
 */
public class VDHTOperations
        implements IPersistence {

    private static final Random       RND                 = RandomDevice.INSTANCE.getRand();
    private static final IPersistence simpleDHTOperations = PersistenceFactory.getDhtOperations();
    private static final int          NUMBER_OF_RETRIES   = 5;
    private static final int          SLEEP_TIME          = 500;
    private final Logger              logger              = LoggerFactory.getLogger(VDHTOperations.class);



    /**
     * Retrieves the latest VDHT entry according to the specified key.
     * 
     * @param pPeer local DHT of the peer
     * @param pLocationKey location key of the requested entry
     * 
     * @return latestData latest Data all peers agree on
     */
    @Override
    public Data getData(PeerDHT pPeer, Number160 pLocationKey)
            throws InterruptedException {
        Pair<Number640, Data> pair = null;

//        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
//            FutureGet fg = pPeer.get(pLocationKey).getLatest().start().awaitUninterruptibly();
        return simpleDHTOperations.getData(pPeer, pLocationKey);

//            // check if all the peers agree on the same latest version, if not
//            // wait a little and try again
//            pair = checkVersions(fg.rawData());
//
//            if (pair != null) {
//                break;
//            }
//
//            logger.info("Get delay or fork - get");
//            Thread.sleep(RND.nextInt(SLEEP_TIME));
//        }
//
//        // we got the latest data
//        return pair.element1();
    }

    /**
     * Retrieves a specific version of a VDHT entry.
     * 
     * @param pPeer local DHT of the peer
     * @param pLocationKey location key of the requested entry
     * @param pVersionKey version key of the requested entry
     * 
     * @return versionOfData all peers agree on
     */
    @Override
    public Data getDataOfVersion(PeerDHT pPeer, Number160 pLocationKey, Number160 pVersionKey)
            throws InterruptedException {
        Pair<Number640, Data> pair = null;

        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
            FutureGet fg = pPeer.get(pLocationKey).versionKey(pVersionKey).start().awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not
            // wait a little and try again
            pair = checkVersions(fg.rawData());

            if (pair != null) {
                break;
            }

            logger.info("Get delay or fork - get");
            Thread.sleep(RND.nextInt(SLEEP_TIME));
        }

        // we got the latest version of data
        return pair.element1();
    }


    /**
     * Stores a data entry in the VDHT at the specified location key.
     * 
     * @param pPeer local DHT of the peer
     * @param pLocationKey location key of the data to save
     * @param pData data to be stored at specified location
     */
    @Override
    public void putData(PeerDHT pPeer, Number160 pLocationKey, Data pData)
            throws InterruptedException, ClassNotFoundException, IOException {
        Pair<Number640, Byte> pair2 = null;

        // Check if location key already exists
        FutureGet fg = pPeer.get(pLocationKey).getLatest().start().awaitUninterruptibly();
        if (fg.data() == null) { // TODO: is there a better check for this? What if it generally exists but hasn't spread to this peer yet?
            // location key doesn't exist - simple put function is used
            simpleDHTOperations.putData(pPeer, pLocationKey, pData);
            System.out.println("put to dht");
            return;
        }

        System.out.println("put to vdht");

        // Archive old file with VDHTArchiver
        VersionArchiver archiver = new VersionArchiver();
        throw new IOException("Breaking changes introduced here. Archiving not possible. Aborting...");
//        archiver.archive(pPeer, pLocationKey, fg.data());


        // location key already exists versioning is applied
//        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
//            System.out.println("Retry " + i + " of " + (NUMBER_OF_RETRIES - i));
//            Pair<Number160, Data> pair = getAndUpdate(pPeer, pLocationKey, pData);
//            if (pair == null) {
//                logger.warning("We cannot handle this kind of inconsistency automatically, handing over the the API dev");
//                return;
//            }
//
//            FuturePut fp = pPeer.put(pLocationKey).data(Number160.ZERO, pair.element1().prepareFlag(), pair.element0()).start().awaitUninterruptibly();
//            pair2 = checkVersions(fp.rawResult());
//            // 1 is PutStatus.OK_PREPARED
//            if (pair2 != null && pair2.element1() == 1) {
//                break;
//            }
//
//            logger.info("Get delay or fork - get");
//
//            // if not removed, a low ttl will eventually get rid of it
//            pPeer.remove(pLocationKey).versionKey(pair.element0()).start().awaitUninterruptibly();
//            Thread.sleep(RND.nextInt(SLEEP_TIME));
//        }
//
//        if (pair2 != null && pair2.element1() == 1) {
//            System.out.println("Confirm write");
//            FuturePut fp = pPeer.put(pLocationKey).versionKey(pair2.element0().versionKey()).putConfirm().data(new Data()).start().awaitUninterruptibly();
//        } else {
//            logger.warning("We cannot handle this kind of inconsistency automatically, handing over the the API dev");
//        }

    }


    /**
     * Removes the data of a specific location key
     * 
     * @param pPeer local DHT of the peer
     * @param pnKey location key of the data to be removed
     */
    @Override
    public void removeData(PeerDHT pPeer, Number160 pKey)
            throws InterruptedException {
        FutureRemove futureRemove = pPeer.remove(pKey).start();
        futureRemove.addListener(new RemoveListener(pPeer.peerAddress().inetAddress().toString(), "Remove latest data"));

        futureRemove.await();
    }

    /**
     * Removes the data of a specific version of a location key
     * 
     * @param pPeer local DHT of the peer
     * @param pnKey location key of the data to be removed
     * @param pVersionKey version key of the data to be removed
     */
    @Override
    public void removeDataOfVersion(PeerDHT pPeer, Number160 pKey, Number160 pVersionKey)
            throws InterruptedException {
        FutureRemove futureRemove = pPeer.remove(pKey).versionKey(pVersionKey).start(); // TODO: Verify if version or location is removed!
        futureRemove.addListener(new RemoveListener(pPeer.peerAddress().inetAddress().toString(), "Remove version data"));

        futureRemove.await();
    }


    /**
     * Get the latest version and do modification.
     * Create new FilePath.
     * 
     * @param peerDHT
     * @param pLocationKey
     * @param pFileData
     * @return
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private Pair<Number160, Data> getAndUpdate(PeerDHT peerDHT, Number160 pLocationKey, Data pFileData)
            throws InterruptedException, ClassNotFoundException, IOException {

        Pair<Number640, Data> oldFile = null;
        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
            FutureGet fg = peerDHT.get(pLocationKey).getLatest().start().awaitUninterruptibly();

            // check if all the peers agree on the same latest version, if not
            // wait a little and try again
            oldFile = checkVersions(fg.rawData());
            if (oldFile != null) {
                break;
            }

            logger.info("Get delay or fork - get");
            Thread.sleep(RND.nextInt(SLEEP_TIME));
        }

        // we got the latest data
        if (oldFile != null) {
            // update operation

            Number160 oldVersionKey = oldFile.element0().versionKey();

            // Create handle new file with new version key
//            Data newData = pFileData;
//            long newVersionKey = oldVersionKey.timestamp() + 1;
//            newData.addBasedOn(oldVersionKey);

            // Return new new file version pair
            // Note: since we create a new version, we can access old versions as well
            return new Pair<Number160, Data>(pLocationKey, pFileData);
        }

        return null;
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
