package net.f4fs.filesystem.event.events;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;

/**
 * An event which gets dispatched after
 * all files got either written completely to the DHT or not.
 * Provides access to the file system and the peer.
 * 
 * @author Raphael
 *
 */
public class AfterWriteEvent
        extends AEvent {
    
    protected P2PFS filesystem;
    
    protected FSPeer fsPeer;
    
    public static String eventName = "filesystem.after_write_event";
    
    public AfterWriteEvent(P2PFS pFilesystem, FSPeer pFsPeer) {
        this.filesystem = pFilesystem;
        this.fsPeer = pFsPeer;
    }

    
    public P2PFS getFilesystem() {
        return filesystem;
    }

    
    public void setFilesystem(P2PFS filesystem) {
        this.filesystem = filesystem;
    }

    
    public FSPeer getFsPeer() {
        return fsPeer;
    }

    
    public void setFsPeer(FSPeer fsPeer) {
        this.fsPeer = fsPeer;
    }

}
