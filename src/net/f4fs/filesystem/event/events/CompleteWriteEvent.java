package net.f4fs.filesystem.event.events;

import java.nio.ByteBuffer;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;

/**
 * An event which gets dispatched when the file is completely
 * written, i.e. no chunks should be added anymore.
 * 
 * @author Raphael
 *
 */
public class CompleteWriteEvent
        extends AEvent {
    
    protected P2PFS filesystem;
    
    protected FSPeer fsPeer;
    
    protected String path;
    
    protected ByteBuffer content;
    
    public static String eventName = "filesystem.complete_write_event";

    public CompleteWriteEvent(P2PFS pFilesystem, FSPeer pFsPeer, String pPath, ByteBuffer pContent) {
        this.filesystem = pFilesystem;
        this.fsPeer = pFsPeer;
        this.path = pPath;
        this.content = pContent;
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

    public ByteBuffer getContent() {
        return content;
    }

    public void setContent(ByteBuffer content) {
        this.content = content;
    }
    

}
