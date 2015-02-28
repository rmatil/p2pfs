package net.tomp2p.exercise;

import java.io.IOException;
import java.util.Random;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;

public class Exercise2 {
    
    public static final int NUMBER_OF_PEERS = 10;
    public static final int PORT = 4001;
    static final Random RND = new Random(42L);
    
    public static void main(String[] args) {
        PeerDHT[] peers = null;
        
        try {
            peers = createAndAttachPeersDHT(NUMBER_OF_PEERS, PORT);
            bootstrap(peers);
            
            //TODO: put and get
            
            //TODO: future.getRawData().entrySet()
            
            peersShutdown(peers);
        } catch (IOException pEx) {
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
    public static PeerDHT[] createAndAttachPeersDHT( int nr, int port ) throws IOException {
        PeerDHT[] peers = new PeerDHT[nr];
        for ( int i = 0; i < nr; i++ ) {
            if ( i == 0 ) {
                peers[0] = new PeerBuilderDHT(new PeerBuilder( new Number160( RND ) ).ports( port ).start()).start();
            } else {
                peers[i] = new PeerBuilderDHT(new PeerBuilder( new Number160( RND ) ).masterPeer( peers[0].peer() ).start()).start();
            }
        }
        return peers;
    }
    
    /**
     * Bootstraps peers to the first peer in the array.
     * 
     * @param peers The peers that should be bootstrapped
     */
    public static void bootstrap( PeerDHT[] peers ) {
        //make perfect bootstrap, the regular can take a while
        for(int i=0;i<peers.length;i++) {
            for(int j=0;j<peers.length;j++) {
                peers[i].peerBean().peerMap().peerFound(peers[j].peerAddress(), null, null, null);
            }
        }
    }
    
    public static void peersShutdown(PeerDHT[] pPeers){
        for(int i = 0; i < pPeers.length; i++){
            pPeers[i].shutdown();
        }
    }
}