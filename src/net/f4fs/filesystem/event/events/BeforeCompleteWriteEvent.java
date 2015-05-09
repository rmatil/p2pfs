package net.f4fs.filesystem.event.events;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;

/**
 * An event which gets dispatched before the file is completely written
 * 
 * @author Raphael
 *
 */
public class BeforeCompleteWriteEvent
        extends AEvent {
    
    protected P2PFS filesystem;
    
    protected FSPeer fsPeer;
    
    protected String path;
        
    public static String eventName = "filesystem.before_complete_write_event";

    public BeforeCompleteWriteEvent(P2PFS pFilesystem, FSPeer pFsPeer, String pPath) {
        this.filesystem = pFilesystem;
        this.fsPeer = pFsPeer;
        this.path = pPath;
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
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
