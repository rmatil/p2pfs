package net.tomp2p.exercise.retowettstein.ex03;

import net.tomp2p.dht.FutureSend;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;


public class SendOperations {
    
    /**
     * Setup a reply handler for every peer in the network
     * 
     * @param peers Array with the peers who need a reply handler
     */
    public static void setupReplyHandler(PeerDHT[] peers) {
        for (final PeerDHT peer : peers) {
            peer.peer().objectDataReply(new ObjectDataReply() {

                @Override
                public Object reply(PeerAddress sender, Object request)
                        throws Exception {
                    System.out.println("PEER " + peer.peerID().intValue() + ": received [Message: " + request + "] from peer " + sender.peerId().intValue());
                    return "world";
                }
            });
        }
    }

    
    /**
     * Send a direct message from one peer to another
     * 
     * @param sender The peer sending the message
     * @param receiver The peer address of the receiving peer
     * @param message The message to be sent
     */
    public static void send(PeerDHT sender, PeerAddress receiver, String message) {
        RequestP2PConfiguration requestP2PConfiguration = new RequestP2PConfiguration(1, 10, 0);
        FutureSend futureSend = sender.send(receiver.peerId()).object(message).requestP2PConfiguration(requestP2PConfiguration).start();
        
        // non-blocking operation
        futureSend.addListener(new BaseFutureAdapter<FutureSend>() {

            @Override
            public void operationComplete(FutureSend future)
                    throws Exception {
                if (!future.isSuccess()) {
                   // Some error message  
                }
            }
        });
    }
}
