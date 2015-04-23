package net.f4fs.persistence;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
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
     * @param pPeerDHT local DHT of the peer
     * @param pLocationKey location key of the requested entry
     */
    public static Pair<Number640, Data> retrieve(PeerDHT pPeerDHT, Number160 pLocationKey)
            throws InterruptedException, ClassNotFoundException, IOException {
        Pair<Number640, Data> pair = null;
        
        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
            FutureGet fg = pPeerDHT.get(pLocationKey).getLatest().start().awaitUninterruptibly();
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
        return pair;
    }

    /**
     * Retrieves a specific version of a VDHT entry.
     * 
     * @param pPeerDHT local DHT of the peer
     * @param pLocationKey location key of the requested entry
     * @param pVersionKey version key of the requested entry
     */
    public static Pair<Number640, Data> retrieve(PeerDHT pPeerDHT, Number160 pLocationKey, Number160 pVersionKey)
            throws InterruptedException, ClassNotFoundException, IOException {
        Pair<Number640, Data> pair = null;

        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
            FutureGet fg = pPeerDHT.get(pLocationKey).versionKey(pVersionKey).start().awaitUninterruptibly();
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
        return pair;
    }

    /**
     * Stores a data entry in the VDHT at the specified location key.
     * 
     * @param pPeerDHT local DHT of the peer
     * @param pLocationKey location key of the data to save
     * @param pFileData data to be stored at specified location
     */
    public static void store(PeerDHT pPeerDHT, Number160 pLocationKey, Data pFileData)
            throws ClassNotFoundException, InterruptedException, IOException {
        Pair<Number640, Byte> pair2 = null;
        
        for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
            Pair<Number160, Data> pair = getAndUpdate(pPeerDHT, pLocationKey, pFileData);
            if (pair == null) {
                logger.warning("we cannot handle this kind of inconsistency automatically, handing over the the API dev");
                return;
            }

            FuturePut fp = pPeerDHT.put(pLocationKey).data(Number160.ZERO, pair.element1().prepareFlag(), pair.element0()).start().awaitUninterruptibly();
            pair2 = checkVersions(fp.rawResult());
            // 1 is PutStatus.OK_PREPARED
            if (pair2 != null && pair2.element1() == 1) {
                break;
            }
            
            logger.info("Get delay or fork - get");
            
            // if not removed, a low ttl will eventually get rid of it
            pPeerDHT.remove(pLocationKey).versionKey(pair.element0()).start().awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
        }

        if (pair2 != null && pair2.element1() == 1) {
            FuturePut fp = pPeerDHT.put(pLocationKey).versionKey(pair2.element0().versionKey()).putConfirm().data(new Data()).start().awaitUninterruptibly();
            logger.warning("Stored: " + fp.failedReason());
        } else {
            logger.warning("we cannot handle this kind of inconsistency automatically, handing over the the API dev");
        }
    }

    /**
     * get the latest version and do modification.
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
     * check if all other peers agree with the local version.
     * @param rawData
     * @return
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
        return pair.element1();
	}

	@Override
	public Data getDataOfVersion(PeerDHT pPeer, Number160 pLocationKey,
			Number160 pVersionKey) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putData(PeerDHT pPeer, Number160 pLocationKey, Data pData)
			throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeData(PeerDHT pPeer, Number160 pKey)
			throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeData(PeerDHT pPeer, Number160 pKey, Number160 pVersionKey)
			throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
