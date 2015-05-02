package net.f4fs.filesystem.util;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;


/**
 * Syncs the list of files in the FileSystem.
 * 
 * @author Reto
 */
public class FSFileSyncer
        implements Runnable {

    private Logger       logger = Logger.getLogger("FSFileSyncer.class");

    /**
     * List representing all fetched keys
     */
    private Set<String>  keys   = new HashSet<>();

    /**
     * Filesystem to update its paths
     */
    private final P2PFS  _filesystem;

    /**
     * Peer of which to get a list of all stored
     * Paths in the P2P Network
     */
    private final FSPeer _peer;

    /**
     * Indicates whether this runnable is still running
     */
    private boolean      _isRunning;

    /**
     * Create a new Thread to "sync" the list of stored files
     * among the P2P network.
     * 
     * @param filesystem The filesystem to update
     * @param peer The peer from which to get the keys
     */
    public FSFileSyncer(P2PFS filesystem, FSPeer peer) {
        _filesystem = filesystem;
        _peer = peer;
        _isRunning = true;
    }

    /**
     * Terminates this runnable on the next iteration
     */
    public void terminate() {
        _isRunning = false;
    }

    @Override
    public void run() {
        while (_isRunning) {
            try {
                Set<String> localPaths = _filesystem.getAllPaths();
                keys = _peer.getAllPaths();

                // create local non-existing files
                for (String key : keys) {
                    if (_filesystem.getPath(key) == null) {

                        // check whether the path is a link, that means key and target are different
                        FutureGet futureGet = _peer.getPath(Number160.createHash(key));
                        futureGet.await();
                        if (null != futureGet.data() && !key.equals((String) futureGet.data().object())) {
                            // target key is different from source key -> is a symlink
                            _filesystem.symlink((String) futureGet.data().object(), key);
                        } else {
                            _filesystem.create(key, null, null);
                        }
                    }
                }

                // remove deleted files / dirs / symlinks / ...
                localPaths.removeAll(keys); // list of all localPaths which are removed in the DHT
                for (String pathToDelete : localPaths) {
                    logger.info("Call removal of element on path '" + pathToDelete + "'. LocalPaths: " + localPaths + ", DHTPaths: " + keys);
                    _filesystem.unlink(pathToDelete);
                }

                Thread.sleep(1000);
            } catch (Exception pEx) {
                pEx.printStackTrace();
                _isRunning = false;
            }
        }
    }
}
