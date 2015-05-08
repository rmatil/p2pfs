package net.f4fs.filesystem.fsfilemonitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.filesystem.util.FSFileUtils;
import net.f4fs.fspeer.FSPeer;
import net.tomp2p.utils.Pair;


public class FSFileMonitor
        implements Runnable {

    private Logger                                  logger = Logger.getLogger("FSFileMonitor.class");

    private List<IBeforeCompleteWriteEventListener> beforeCompleteWriteEventListeners;

    private List<IAfterCompleteWriteEventListener>  afterCompleteWriteEventListeners;

    private List<ICompleteWriteEventListener>       completeWriteEventListeners;

    private Map<String, Pair<Integer, ByteBuffer>>  monitoredFiles;

    private P2PFS                                   filesystem;

    private FSPeer                                  fsPeer;

    private boolean                                 isRunning;

    public FSFileMonitor(P2PFS pFilesystem, FSPeer pFsPeer) {
        this.beforeCompleteWriteEventListeners = new ArrayList<>();
        this.afterCompleteWriteEventListeners = new ArrayList<>();
        this.completeWriteEventListeners = new ArrayList<>();
        this.monitoredFiles = new HashMap<>();
        this.filesystem = pFilesystem;
        this.fsPeer = pFsPeer;
        this.isRunning = true;
    }

    public void registerAfterCompleteWriteEventListener(IAfterCompleteWriteEventListener pEvent) {
        this.afterCompleteWriteEventListeners.add(pEvent);
    }

    public void registerBeforeCompleteWriteEventListener(IBeforeCompleteWriteEventListener pEvent) {
        this.beforeCompleteWriteEventListeners.add(pEvent);
    }

    public void registerCompleteWriteEventListener(ICompleteWriteEventListener pEvent) {
        this.completeWriteEventListeners.add(pEvent);
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
            for (IBeforeCompleteWriteEventListener beforeCompleteWriteEventListener : this.beforeCompleteWriteEventListeners) {
                beforeCompleteWriteEventListener.handleEvent(this.filesystem, this.fsPeer);
            }

            Map<String, Pair<Integer, ByteBuffer>> notWrittenFiles = new HashMap<>();
            for (Entry<String, Pair<Integer, ByteBuffer>> entry : this.monitoredFiles.entrySet()) {
                if (!entry.getValue().element0().equals(0)) {
                    // file is not ready yet to write to DHT
                    Pair<Integer, ByteBuffer> decreasedCounterPair = entry.getValue().element0(entry.getValue().element0() - 1);
                    notWrittenFiles.put(entry.getKey(), decreasedCounterPair);
                } else {
                    // update FS / save whole files
                    for (ICompleteWriteEventListener completeWriteEventListener : this.completeWriteEventListeners) {
                        try {
                            completeWriteEventListener.handleEvent(this.filesystem, this.fsPeer, entry.getKey(), entry.getValue().element1());
                        } catch (ClassNotFoundException | InterruptedException | IOException e) {
                            // An error occured during event (e.g. peer could not save data)
                            // Add this file again for next try
                            notWrittenFiles.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }

            // update FS
            for (IAfterCompleteWriteEventListener afterCompleteWriteEventListener : this.afterCompleteWriteEventListeners) {
                afterCompleteWriteEventListener.handleEvent(this.filesystem, this.fsPeer);
            }

            this.monitoredFiles = notWrittenFiles;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
