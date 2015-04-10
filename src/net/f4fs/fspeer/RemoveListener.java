package net.f4fs.fspeer;

import net.tomp2p.dht.FutureRemove;
import net.tomp2p.futures.BaseFutureAdapter;

public class RemoveListener extends BaseFutureAdapter<FutureRemove> {
    
    private final String _output;
    private final String _peerIP;
    
    public RemoveListener(String peerIP, String output){
        _output = output;
        _peerIP = peerIP;
    }

    @Override
    public void operationComplete(FutureRemove future)
            throws Exception {
        
        if (future.isSuccess()){ 
            System.out.println("[Peer@" + _peerIP + "] " + _output + " successfull") ;
        } else {
            System.out.println("[Peer@" + _peerIP + "] " + _output + " failed") ;
        }
    }
}
