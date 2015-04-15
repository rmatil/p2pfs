package net.f4fs.filesystem.util;

import java.util.HashSet;
import java.util.Set;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;


/**
 * Syncs the list of files in the FileSystem.
 * 
 * @author Reto
 */
public class FSFileSyncer
        implements Runnable {

    /**
     * List representing all fetched keys
     */
    private Set<String>  keys = new HashSet<>();

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

                // remove deleted files / dirs / symlinks / ...
                localPaths.removeAll(keys); // list of all localPaths which are removed in the DHT
                for (String pathToDelete : localPaths) {
                    _filesystem.unlink(pathToDelete);
                }

                // create local non-existing files
                for (String key : keys) {
                    if (_filesystem.getPath(key) == null) {
                        
                        _filesystem.create(key, null, null);
                    }
                }

                Thread.sleep(10000);
            } catch (Exception pEx) {
                pEx.printStackTrace();
                _isRunning = false;
            }
        }
    }
}
