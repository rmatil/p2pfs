package net.f4fs.filesystem.fsfilemonitor;

import java.util.Set;
import java.util.logging.Logger;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;
import net.tomp2p.peers.Number160;


public class SyncFileEventListener
implements IAfterCompleteWriteEventListener {

    private Logger       logger = Logger.getLogger("SyncFileEventListener.class");

    @Override
    public void handleEvent(P2PFS pFilesystem, FSPeer pFsPeer) {
        try {
            Set<String> localPaths = pFilesystem.getAllPaths();
            Set<String> keys = pFsPeer.getAllPaths();

            // create local non-existing files
            for (String key : keys) {
                if (pFilesystem.getPath(key) == null) {

                    // check whether the path is a link, that means key and target are different
                    String foundPath = pFsPeer.getPath(Number160.createHash(key));
                    if (null != foundPath && !key.equals(foundPath)) {
                        // target key is different from source key -> is a symlink
                        pFilesystem.symlink(foundPath, key);
                    } else {
                        pFilesystem.create(key, null, null);
                    }
                }
            }

            // remove deleted files / dirs / symlinks / ...
            localPaths.removeAll(keys); // list of all localPaths which are removed in the DHT
            for (String pathToDelete : localPaths) {
                logger.info("Call removal of element on path '" + pathToDelete + "'. LocalPaths: " + localPaths + ", DHTPaths: " + keys);
                pFilesystem.unlink(pathToDelete);
            }

        } catch (Exception pEx) {
            logger.warning(pEx.getMessage());
        }

    }

}
