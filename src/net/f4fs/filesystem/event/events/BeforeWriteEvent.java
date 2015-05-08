package net.f4fs.filesystem.event.events;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;


public class BeforeWriteEvent
        extends AEvent {
    
    protected P2PFS filesystem;
    
    protected FSPeer fsPeer;
    
    public static String eventName = "filesystem.before_write_event";
    
    public BeforeWriteEvent(P2PFS pFilesystem, FSPeer pFsPeer) {
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
