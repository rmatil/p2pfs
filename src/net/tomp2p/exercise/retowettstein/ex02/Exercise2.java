package net.tomp2p.exercise.retowettstein.ex02;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;


/**
 * @author Reto Wettstein 12-716-221
 * @author Christian Tresch 06-923-627
 */
public class Exercise2 {

    public static final int       NUMBER_OF_PEERS    = 10;
    public static final int       STORING_PEER_INDEX = 2; // peerIndex is 1 smaller than peerId: peerIndex 0 is peerId 1
    public static final int       GETTER_PEER_INDEX  = 4; // peerIndex is 1 smaller than peerId: peerIndex 0 is peerId 1
    public static final Number160 KEY                = new Number160(12345);
    public static final int       PORT               = 4001;

    public static void main(String[] args) {
        PeerDHT[] peers = null;

        try {
            peers = createAndAttachPeersDHT(NUMBER_OF_PEERS, PORT);
            bootstrap(peers);

            put(peers[STORING_PEER_INDEX], KEY, "Max Power");
            get(peers[GETTER_PEER_INDEX], KEY);

            peersShutdown(peers);
        } catch (IOException pEx) {
            pEx.printStackTrace();
        } catch (ClassNotFoundException pEx) {
            pEx.printStackTrace();
        }

    }


    /**
     * Create peers with a port and attach it to the first peer in the array.
     * 
     * @param nr The number of peers to be created
     * @param port The port that all the peer listens to. The multiplexing is done via the peer Id
     * @return The created peers
     * @throws IOException IOException
     */
    public static PeerDHT[] createAndAttachPeersDHT(int nr, int port)
            throws IOException {
        PeerDHT[] peers = new PeerDHT[nr];
        for (int i = 0; i < nr; i++) {
            if (i == 0) {
                peers[0] = new PeerBuilderDHT(new PeerBuilder(new Number160(i + 1)).ports(port).start()).start();
            } else {
                peers[i] = new PeerBuilderDHT(new PeerBuilder(new Number160(i + 1)).masterPeer(peers[0].peer()).start()).start();
            }
        }

        return peers;
    }


    /**
     * Bootstraps peers to the first peer in the array.
     * 
     * @param peers The peers that should be bootstrapped
     */
    public static void bootstrap(PeerDHT[] peers) {
        // make perfect bootstrap, the regular can take a while
        for (int i = 0; i < peers.length; i++) {
            for (int j = 0; j < peers.length; j++) {
                peers[i].peerBean().peerMap().peerFound(peers[j].peerAddress(), null, null, null);
            }
        }
    }

    /**
     * Put data into the DHT.
     * 
     * @param pPeer The storing peer
     * @param pKey The key for storing the data
     * @param pValue The data to be stored
     * @throws IOException IOException
     */
    public static void put(PeerDHT pPeer, Number160 pKey, String pValue)
            throws IOException {
        FuturePut futurePut = pPeer.put(pKey).data(new Data(pValue)).start();
        futurePut.awaitUninterruptibly();

        System.out.println("Peer with id " + pPeer.peerAddress().peerId().intValue() + " stored " + "[Key: " + pKey.intValue() + ", Value: " + pValue + "]");
    }

    /**
     * Get data from the DHT.
     * 
     * @param pPeer The peer that searches the information
     * @param pKey The key for the data
     * @return returnValue The retrieved data
     * @throws IOException IOException
     * @throws ClassNotFoundException ClassNotFoundException.
     */
    public static Object get(PeerDHT pPeer, Number160 pKey)
            throws ClassNotFoundException, IOException {
        Object returnValue;

        FutureGet futureGet = pPeer.get(pKey).start();
        futureGet.awaitUninterruptibly();

        Set<Entry<PeerAddress, Map<Number640, Data>>> replies = futureGet.rawData().entrySet();

        returnValue = futureGet.data().object();
        System.out.println("\nPeer with id " + pPeer.peerAddress().peerId().intValue() + " received for key " + pKey.intValue() + " the data: " + returnValue);

        System.out.print("\nThe peers with the following id's replied: ");
        Iterator<Entry<PeerAddress, Map<Number640, Data>>> iter = replies.iterator();
        while (iter.hasNext()) {
            Entry<PeerAddress, Map<Number640, Data>> entry = iter.next();
            System.out.print(entry.getKey().peerId().intValue() + " ");
        }

        return returnValue;
    }


    /**
     * Shutdown peers.
     * 
     * @param pPeers The peers that should be shutdown
     */
    public static void peersShutdown(PeerDHT[] pPeers) {
        for (int i = 0; i < pPeers.length; i++) {
            pPeers[i].shutdown();
        }
    }
}