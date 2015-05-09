package net.f4fs.filesystem.fsfilemonitor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.filesystem.event.EventDispatcher;
import net.f4fs.filesystem.event.events.AfterCompleteWriteEvent;
import net.f4fs.filesystem.event.events.AfterWriteEvent;
import net.f4fs.filesystem.event.events.BeforeCompleteWriteEvent;
import net.f4fs.filesystem.event.events.BeforeWriteEvent;
import net.f4fs.filesystem.event.events.CompleteWriteEvent;
import net.f4fs.filesystem.event.listeners.IEventListener;
import net.f4fs.filesystem.util.FSFileUtils;
import net.f4fs.fspeer.FSPeer;
import net.tomp2p.utils.Pair;


public class FSFileMonitor
        implements Runnable {

    private Logger                                 logger = Logger.getLogger("FSFileMonitor.class");

    private EventDispatcher                        eventDispatcher;

    private Map<String, Pair<Integer, ByteBuffer>> monitoredFiles;

    private P2PFS                                  filesystem;

    private FSPeer                                 fsPeer;

    private boolean                                isRunning;

    public FSFileMonitor(P2PFS pFilesystem, FSPeer pFsPeer) {
        this.eventDispatcher = new EventDispatcher();
        this.monitoredFiles = new HashMap<>();
        this.filesystem = pFilesystem;
        this.fsPeer = pFsPeer;
        this.isRunning = true;
    }
    
    public void addEventListener(IEventListener pEventListener) {
        this.eventDispatcher.addEventListener(pEventListener);
    }

    public synchronized void addMonitoredFile(String pPath, ByteBuffer pContents) {
        // NOTE: we do not save FUSE's temporary files. They
        // always start with "._<FILENAME>"
        if (FSFileUtils.getLastComponent(pPath).startsWith("._")) {
            return;
        }

        Pair<Integer, ByteBuffer> pair = new Pair<>(new Integer(1), pContents);
        this.monitoredFiles.put(pPath, pair);
        logger.info("Wrote chunk to file on path '" + pPath + "' containing '" + pContents.capacity() + "' bytes to FSFileMonitor");
    }

    public synchronized ByteBuffer getFileContent(String pPath) {
        Pair<Integer, ByteBuffer> file = this.monitoredFiles.get(pPath);

        if (null == file) {
            return null;
        }

        return file.element1();
    }

    public void terminate() {
        this.isRunning = false;
    }


    @Override
    public void run() {
        
        while (this.isRunning) {
            // update FS
            BeforeWriteEvent beforeWriteEvent = new BeforeWriteEvent(this.filesystem, this.fsPeer);
            this.eventDispatcher.dispatchEvent(BeforeWriteEvent.eventName, beforeWriteEvent);

            Map<String, Pair<Integer, ByteBuffer>> notWrittenFiles = new HashMap<>();
            for (Entry<String, Pair<Integer, ByteBuffer>> entry : this.monitoredFiles.entrySet()) {
                if (!entry.getValue().element0().equals(0)) {
                    // file is not ready yet to write to DHT
                    Pair<Integer, ByteBuffer> decreasedCounterPair = entry.getValue().element0(entry.getValue().element0() - 1);
                    notWrittenFiles.put(entry.getKey(), decreasedCounterPair);
                    logger.info("Decrease counter for file on path '" + entry.getKey() + "'.");
                } else {
                    // dispatch beforeCompleteWriteEvent
                    BeforeCompleteWriteEvent beforeCompleteWriteEvent = new BeforeCompleteWriteEvent(this.filesystem, this.fsPeer, entry.getKey());
                    this.eventDispatcher.dispatchEvent(BeforeCompleteWriteEvent.eventName, beforeCompleteWriteEvent);
                    
                    // dispatch completeWriteEvent
                    CompleteWriteEvent completeWriteEvent = new CompleteWriteEvent(this.filesystem, this.fsPeer, entry.getKey(), entry.getValue().element1());
                    this.eventDispatcher.dispatchEvent(CompleteWriteEvent.eventName, completeWriteEvent);
                    
                    // dispatch afterCompleteWriteEvent
                    AfterCompleteWriteEvent afterCompleteWriteEvent = new AfterCompleteWriteEvent(this.filesystem, this.fsPeer, entry.getKey());
                    this.eventDispatcher.dispatchEvent(AfterCompleteWriteEvent.eventName, afterCompleteWriteEvent);
                }
            }

            // dispatch afterWriteEvent
            AfterWriteEvent afterWriteEvent = new AfterWriteEvent(this.filesystem, this.fsPeer);
            this.eventDispatcher.dispatchEvent(AfterWriteEvent.eventName, afterWriteEvent);

            this.monitoredFiles = notWrittenFiles;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.info("FSFileMonitor got interrupted");
            }
        }

    }


}
