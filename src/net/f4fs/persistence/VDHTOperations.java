package net.f4fs.persistence;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import net.f4fs.fspeer.RemoveListener;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;


/**
 * VDHTOperations adds automated versioning functionality to the integrated peerDHT.
 * Handling the inter-peer negotiations for consensus.
 * Based on the VDHT example of Thomas Bocek's TomP2P.
 * 
 * @author Christian
 */
public class VDHTOperations implements IPersistence {

    private static final Random RND               = new Random(42L);
    private static final int    NUMBER_OF_RETRIES = 5;
    private static Logger       logger            = Logger.getLogger("VDHTOperations.class");


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
        
        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
            FutureGet fg = pPeer.get(pLocationKey).getLatest().start().awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not
            // wait a little and try again
            pair = checkVersions(fg.rawData());
            
            if (pair != null) {
                break;
            }

            logger.info("Get delay or fork - get");
            Thread.sleep(RND.nextInt(500));
        }

        // we got the latest data
        Data latestData = pair.element1();
        return latestData;
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
            Thread.sleep(RND.nextInt(500));
        }

        // we got the latest data
        Data versionOfData = pair.element1();
        return versionOfData;
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
        
        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
            Pair<Number160, Data> pair = getAndUpdate(pPeer, pLocationKey, pData);
            if (pair == null) {
                logger.warning("We cannot handle this kind of inconsistency automatically, handing over the the API dev");
                return;
            }

            FuturePut fp = pPeer.put(pLocationKey).data(Number160.ZERO, pair.element1().prepareFlag(), pair.element0()).start().awaitUninterruptibly();
            pair2 = checkVersions(fp.rawResult());
            // 1 is PutStatus.OK_PREPARED
            if (pair2 != null && pair2.element1() == 1) {
                break;
            }
            
            logger.info("Get delay or fork - get");
            
            // if not removed, a low ttl will eventually get rid of it
            pPeer.remove(pLocationKey).versionKey(pair.element0()).start().awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
        }

        if (pair2 != null && pair2.element1() == 1) {
            FuturePut fp = pPeer.put(pLocationKey).versionKey(pair2.element0().versionKey()).putConfirm().data(new Data()).start().awaitUninterruptibly();
            logger.warning("Stored: " + fp.failedReason());
        } else {
            logger.warning("We cannot handle this kind of inconsistency automatically, handing over the the API dev");
        }
		
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
        FutureRemove futureRemove = pPeer.remove(pKey).versionKey(pVersionKey).start(); //TODO: Verify if version or location is remove!
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
    private static Pair<Number160, Data> getAndUpdate(PeerDHT peerDHT, Number160 pLocationKey, Data pFileData)
            throws InterruptedException, ClassNotFoundException, IOException {
        Pair<Number640, Data> pair = null;
        for (int i = 0; i < 5; i++) {
            FutureGet fg = peerDHT.get(pLocationKey).getLatest().start().awaitUninterruptibly();

            // check if all the peers agree on the same latest version, if not
            // wait a little and try again
            pair = checkVersions(fg.rawData());
            if (pair != null) {
                break;
            }
            
            logger.info("Get delay or fork - get");
            Thread.sleep(RND.nextInt(500));
        }
        
        // we got the latest data
        if (pair != null) {
            // update operation
            // TODO: let user know about old versions
            Data newData = pFileData;
            Number160 v = pair.element0().versionKey();
            long version = v.timestamp() + 1;
            newData.addBasedOn(v);
            // since we create a new version, we can access old versions as well
            return new Pair<Number160, Data>(new Number160(version, newData.hash()), newData);
        }
        
        return null;
    }

    /**
     * Check if all other peers agree with the local version.
     * 
     * @param rawData of FutureGet request
     * 
     * @return a new Pair with the latest Key & latest Data
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
