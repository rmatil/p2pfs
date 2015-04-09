package net.f4fs.filesystem;

import java.util.ArrayList;
import java.util.List;

import net.f4fs.fspeer.FSPeer;


public class Keys implements Runnable {
    
    List<String> keys = new ArrayList<>();
    P2PFS _filesystem;
    FSPeer _peer;
    
    public Keys(P2PFS filesystem, FSPeer peer){
        _filesystem = filesystem;
        _peer = peer;
    }
    
    public void run() {
        while(true){
            try {
                keys = _peer.getAllKeys();
                
                for(String key : keys){
                    if(_filesystem.getPath(key) == null){ 
                        _filesystem.create(key, null, null);
                    }
                }
                
                Thread.sleep(1000);
            } catch (Exception pEx) {
                pEx.printStackTrace();
            }
        }
    }
}
