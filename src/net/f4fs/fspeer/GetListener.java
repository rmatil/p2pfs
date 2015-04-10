package net.f4fs.fspeer;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.futures.BaseFutureAdapter;


public class GetListener extends BaseFutureAdapter<FutureGet> {
    
    private String _output;
    private String _peerIP;
    
    public GetListener(String peerIP, String output){
        _output = output;
        _peerIP = peerIP;
    }

    @Override
    public void operationComplete(FutureGet future)
            throws Exception {
       if (future.isSuccess()){ 
           System.out.println("[Peer@" + _peerIP + "] " + _output + " successfull") ;
       } else {
           System.out.println("[Peer@" + _peerIP + "] " + _output + " failed") ;
       }
    }
}