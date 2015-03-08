package net.tomp2p.exercise.retowettstein.ex03;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;


/**
 * @author Reto Wettstein 12-716-221
 * @author Christian Tresch 06-923-627
 */
public class Main {

    public static final int    NUMBER_OF_PEERS    = 10;
    public static final int    STORING_PEER_INDEX = 2;          // peerIndex is 1 smaller than peerId: peerIndex 0 is peerId 1
    public static final int    GETTER_PEER_INDEX  = 4;          // peerIndex is 1 smaller than peerId: peerIndex 0 is peerId 1
    public static final String KEY                = "Max Power";
    public static final int    PORT               = 4001;

    public static void main(String[] args) {
        PeerDHT[] peers = null;

        try {
            peers = Util.createAndAttachPeersDHT(NUMBER_OF_PEERS, PORT);
            Util.bootstrap(peers);
            
            PeerAddress value = peers[STORING_PEER_INDEX].peerAddress();

            putNonBlocking(peers[STORING_PEER_INDEX], KEY, value);
            
            Thread.sleep(1000);

            //getNonBlocking(peers[GETTER_PEER_INDEX], KEY);
            get(peers[GETTER_PEER_INDEX], KEY);

            Util.peersShutdown(peers);
        } catch (IOException pEx) {
            pEx.printStackTrace();
        } catch (InterruptedException pEx) {
            pEx.printStackTrace();
        } catch (ClassNotFoundException pEx) {
            pEx.printStackTrace();
        }
    }

    public static void putNonBlocking(PeerDHT pPeer, String pKey, PeerAddress pValue)
            throws IOException {
        FuturePut futurePut = pPeer.put(Number160.createHash(pKey)).data(new Data(pValue)).start();

        // non-blocking operation
        futurePut.addListener(new BaseFutureAdapter<FuturePut>() {

            @Override
            public void operationComplete(FuturePut future)
                    throws Exception {
                if(future.isSuccess()){      
                    System.out.println("Peer with id " + pPeer.peerAddress().peerId().intValue() + " stored " + "[Key: " + pKey + ", Value: " + pValue + "]");
                }
            }
        });

        System.out.println("this may happen before put finishes");
    }


    //doesnt work yet
    private static void getNonBlocking(PeerDHT peers, String pKey) {
        FutureGet futureGet = peers.get(Number160.createHash(pKey)).start();
        // non-blocking operation
        futureGet.addListener(new BaseFutureAdapter<FutureGet>() {
            @Override
            public void operationComplete(FutureGet future) throws Exception {
                if(future.isSuccess()){   
                    System.out.println("result non-blocking: " + future.data().object());
                }
            }
            
        });
        System.out.println("this may happen before printing the result");
    }
    
    
    public static Object get(PeerDHT pPeer, String pKey)
            throws ClassNotFoundException, IOException {
        Object returnValue;

        FutureGet futureGet = pPeer.get(Number160.createHash(pKey)).start();
        futureGet.awaitUninterruptibly();

        Set<Entry<PeerAddress, Map<Number640, Data>>> replies = futureGet.rawData().entrySet();

        returnValue = futureGet.data().object();
        System.out.println("Peer with id " + pPeer.peerAddress().peerId().intValue() + " received for key " + pKey + " the data: " + returnValue);

        System.out.print("The peers with the following id's replied: ");
        Iterator<Entry<PeerAddress, Map<Number640, Data>>> iter = replies.iterator();
        while (iter.hasNext()) {
            Entry<PeerAddress, Map<Number640, Data>> entry = iter.next();
            System.out.print(entry.getKey().peerId().intValue() + " ");
        }

        return returnValue;
    }
}
