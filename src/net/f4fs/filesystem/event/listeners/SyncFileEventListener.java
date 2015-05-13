package net.f4fs.filesystem.event.listeners;

import java.util.Set;
import java.util.logging.Logger;

import net.f4fs.filesystem.event.events.AEvent;
import net.f4fs.filesystem.event.events.AfterWriteEvent;
import net.f4fs.filesystem.util.FSFileUtils;
import net.tomp2p.peers.Number160;

/**
 * Synchronizes files on disk with the ones on the DHT.
 * Gets invoked after all completely written files are written to the DHT.
 * 
 * @author Reto
 *
 */
public class SyncFileEventListener
        implements IEventListener {

    private Logger logger = Logger.getLogger("SyncFileEventListener.class");

    @Override
    public void handleEvent(AEvent pEvent) {
        if (!(pEvent instanceof AfterWriteEvent)) {
            return;
        }
        
        AfterWriteEvent afterWriteEvent = (AfterWriteEvent) pEvent;
        
        try {
            Set<String> localPaths = afterWriteEvent.getFilesystem().getAllPaths();
            Set<String> keys = afterWriteEvent.getFsPeer().getAllPaths();

            // create local non-existing files
            for (String key : keys) {
                if (FSFileUtils.isRootDirectory(key)) {
                    // no changes are allowed to root directory
                    continue;
                }

                if (afterWriteEvent.getFilesystem().getPath(key) == null) {
                    // check whether the path is a link, that means key and target are different
                    String foundPath = afterWriteEvent.getFsPeer().getPath(Number160.createHash(key));
                    if (null != foundPath && !key.equals(foundPath)) {
                        // target key is different from source key -> is a symlink
                        logger.info("Call 'symlink' for target '" + foundPath + "' on path '" + key + "'");
                        afterWriteEvent.getFilesystem().symlink(foundPath, key);
                    } else {
                        logger.info("Call 'create' for file/dir on path '" + key + "'");
                        afterWriteEvent.getFilesystem().create(key, null, null);
                    }
                }
            }

            // remove deleted files / dirs / symlinks / ...
            localPaths.removeAll(keys); // list of all localPaths which are removed in the DHT
            for (String pathToDelete : localPaths) {
                if (FSFileUtils.isRootDirectory(pathToDelete)) {
                    // no changes are allowed to root directory
                    continue;
                }
                logger.info("Call removal of element on path '" + pathToDelete + "'. LocalPaths: " + localPaths + ", DHTPaths: " + keys);
                afterWriteEvent.getFilesystem().unlink(pathToDelete);
            }

        } catch (Exception pEx) {
            logger.warning(pEx.getMessage());
        }

    }

    @Override
    public String getEventName() {
        return AfterWriteEvent.eventName;
    }
}
