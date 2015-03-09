package net.tomp2p.exercise.retowettstein.ex03;

import java.io.IOException;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;


public class DHTOperations {

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


    public static void putNonBlocking(PeerDHT pPeer, String pKey, PeerAddress pValue)
            throws IOException {
        FuturePut futurePut = pPeer.put(Number160.createHash(pKey)).data(new Data(pValue)).start();

        // non-blocking operation
        futurePut.addListener(new BaseFutureAdapter<FuturePut>() {

            @Override
            public void operationComplete(FuturePut future)
                    throws Exception {
                if (future.isSuccess()) {
                    System.out.println("PEER " + pPeer.peerAddress().peerId().intValue() + ": stored " + "[Key: " + pKey + ", Value: " + pValue + "]");
                }
            }
        });
    }


    public static void getAndSendNonBlocking(PeerDHT pPeer, String pKey, String pMessage) {
        FutureGet futureGet = pPeer.get(Number160.createHash(pKey)).start();

        // non-blocking operation
        futureGet.addListener(new BaseFutureAdapter<FutureGet>() {

            @Override
            public void operationComplete(FutureGet future)
                    throws Exception {
                if (future.isSuccess()) {
                    PeerAddress address = (PeerAddress) future.data().object();
                    System.out.println("PEER " + pPeer.peerAddress().peerId().intValue() + ": looked up [Key: " + pKey + "], received [Value: " + address + "]");
                    
                    SendOperations.send(pPeer, address, pMessage);
                }
            }

        });
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
