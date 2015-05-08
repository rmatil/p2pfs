package net.f4fs.filesystem.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;
import net.tomp2p.utils.Pair;


public class FSFileMonitor
        implements Runnable {

    private List<ICompleteWriteEvent>       completeWriteEvents;

    private List<IBeforeCompleteWriteEvent> beforeCompleteWriteEvents;
    
    private Map<String, Pair<Integer, ByteBuffer>> filesToWrite;

    private ExecutorService                 executorService;

    private P2PFS                           filesystem;

    private FSPeer                          fsPeer;

    public FSFileMonitor(P2PFS pFilesystem, FSPeer pFsPeer) {
        this.completeWriteEvents = new ArrayList<>();
        this.beforeCompleteWriteEvents = new ArrayList<>();
        this.filesToWrite = new HashMap<>();
        this.executorService = Executors.newSingleThreadExecutor();
        this.filesystem = pFilesystem;
        this.fsPeer = pFsPeer;
    }

    public void registerCompleteWriteEvent(ICompleteWriteEvent pEvent) {
        this.completeWriteEvents.add(pEvent);
    }

    public void registerBeforeCompleteWriteEvent(IBeforeCompleteWriteEvent pEvent) {
        this.beforeCompleteWriteEvents.add(pEvent);
    }
    
    public synchronized void addChunkToFile(String pPath, ByteBuffer pChunk) {
        // TODO: add chunk
    }

    
    @Override
    public void run() {
        
        for (Entry<String, Pair<Integer, ByteBuffer>> entry : this.filesToWrite.entrySet()) {
            // TODO: decrease countdown, if zero -> write to DHT
        }
        
    }


}
