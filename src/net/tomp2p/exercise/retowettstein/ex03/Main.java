package net.tomp2p.exercise.retowettstein.ex03;

import java.io.IOException;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.PeerAddress;


/**
 * @author Reto Wettstein 12-716-221
 * @author Christian Tresch 06-923-627
 */
public class Main {

    public static final int    NUMBER_OF_PEERS    = 10;
    public static final int    STORING_PEER_INDEX = 2; // peerIndex is 1 smaller than peerId: peerIndex 0 is peerId 1
    public static final int    GETTER_PEER_INDEX  = 4; // peerIndex is 1 smaller than peerId: peerIndex 0 is peerId 1
    public static final String KEY                = "Max Power";
    public static final int    PORT               = 4001;

    public static void main(String[] args) {
        PeerDHT[] peers = null;

        try {
            peers = DHTOperations.createAndAttachPeersDHT(NUMBER_OF_PEERS, PORT);
            DHTOperations.bootstrap(peers);
            SendOperations.setupReplyHandler(peers);
            
            PeerAddress value = peers[STORING_PEER_INDEX].peerAddress();
            String message = "Hello World";

            DHTOperations.putNonBlocking(peers[STORING_PEER_INDEX], KEY, value);
            Thread.sleep(1000);
            DHTOperations.getAndSendNonBlocking(peers[GETTER_PEER_INDEX], KEY, message);
            
            Thread.sleep(1000);
            
            DHTOperations.peersShutdown(peers);
        } catch (IOException pEx) {
            pEx.printStackTrace();
        } catch (InterruptedException pEx) {
            pEx.printStackTrace();
        } 
    }

}
