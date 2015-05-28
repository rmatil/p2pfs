package net.f4fs.filesystem.fsfilemonitor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


/**
 * Dispatches the following events according to the state of the file system:
 * <ul>
 * <li>
 * <code>filesystem.before_write_event</code>: Each time, before a check is made, if the file is complete and should be written (including writing the file if it is complete)</li>
 * <li>
 * <code>filesystem.after_write_event</code>: Each time, after an attempt is made to check if the file is complete and should be written (including writing the file if it is
 * complete)</li>
 * <li>
 * <code>filesystem.before_complete_write_event</code>: Each time, a file is complete and before it should be written</li>
 * <li>
 * <code>filesystem.complete_write_event</code>: Each time, a file is complete and should be written</li>
 * <li>
 * <code>filesystem.after_complete_write_event</code>: Each time, a file is complete and was written</li>
 * </ul>
 * 
 * Until now, the {@link net.f4fs.filesystem.event.listeners.WriteFileEventListener WriteFileEventListener} is
 * registered to the <code>filesystem.complete_write_event</code>. That means, it writes each
 * file to the DHT when it is complete on the physical disk. <br>
 * The {@link net.f4fs.filesystem.event.listeners.SyncFileEventListener SyncFileEventListeners} is invoked
 * each time a <code>filesystem.after_write_event</code> is dispatched. That
 * means, it only synchronizes the physical disk after all missing files are completely written to the DHT.
 * 
 * @author Raphael
 *
 */
public class FSFileMonitor
        implements Runnable {

    private final Logger                           logger = LoggerFactory.getLogger(FSFileMonitor.class);

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

    /**
     * Registers an event listener
     * 
     * @param pEventListener The event listener to be registered
     */
    public void addEventListener(IEventListener pEventListener) {
        this.eventDispatcher.addEventListener(pEventListener);
    }

    /**
     * Adds a <i>monitored</i> file to the FileMonitor. Overwrites an already existing
     * entry with the same path, i.e. the provided input must be complete until the current chunk
     * written to the physical disk.
     * 
     * <p style="color:red">
     * Note: Make sure that not only single chunks are provided to this method as they would get overwritten each time you call this method with the same path
     * </p>
     * 
     * <p>
     * <b>Note</b>: To prevent FUSE's temporary files from monitoring, all files which start with <i>._FILENAME</i> are ignored
     * <p>
     * 
     * @param pPath The path to the file which should be monitored if completely written
     * @param pContents All contents written until now for the file (i.e. not only single chunks)
     */
    public synchronized void addMonitoredFile(String pPath, ByteBuffer pContents) {
        // NOTE: we do not save FUSE's temporary files. They
        // always start with "._<FILENAME>"
        if (FSFileUtils.getLastComponent(pPath).startsWith("._")) {
            return;
        }

        Pair<Integer, ByteBuffer> pair = new Pair<>(new Integer(1), pContents);
        this.monitoredFiles.put(pPath, pair);
        this.logger.trace("Wrote chunk to file on path '" + pPath + "' containing '" + pContents.capacity() + "' bytes to FSFileMonitor");
    }
    
    public synchronized void removeMonitoredFile(String pPath) {
        this.monitoredFiles.remove(pPath);
    }

    /**
     * Returns the current written file contents of the file located at <code>pPath</code>
     * 
     * @param pPath The path to the file which contents should be returned
     * 
     * @return The file contents written until now
     */
    public synchronized ByteBuffer getFileContent(String pPath) {
        Pair<Integer, ByteBuffer> file = this.monitoredFiles.get(pPath);

        if (null == file) {
            return null;
        }

        return file.element1();
    }

    public synchronized Set<String> getMonitoredFilePaths() {
        return this.monitoredFiles.keySet();
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
                if (entry.getValue().element0() > 0) {
                    // file is not ready yet to write to DHT
                    Pair<Integer, ByteBuffer> decreasedCounterPair = entry.getValue().element0(entry.getValue().element0() - 1);
                    notWrittenFiles.put(entry.getKey(), decreasedCounterPair);
                    logger.debug("Decrease counter for file on path '" + entry.getKey() + "'.");
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
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                this.logger.error(e.getMessage());
                e.printStackTrace();
            }
        }

    }


}
