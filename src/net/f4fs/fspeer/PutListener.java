package net.f4fs.fspeer;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.futures.BaseFutureAdapter;


public class PutListener extends BaseFutureAdapter<FuturePut> {
    
    private final String _output;
    private final String _peerIP;
    
    public PutListener(String peerIP, String output){
        _output = output;
        _peerIP = peerIP;
    }

    @Override
    public void operationComplete(FuturePut future)
            throws Exception {
        if (future.isSuccess()){ 
            System.out.println("[Peer@" + _peerIP + "] " + _output + " successfull") ;
        } else {
            System.out.println("[Peer@" + _peerIP + "] " + _output + " failed") ;
        }
    }

}
