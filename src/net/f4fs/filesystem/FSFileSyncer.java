package net.f4fs.filesystem;

import java.util.ArrayList;
import java.util.List;

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
    private List<String> keys = new ArrayList<>();

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
     * Create a new Thread to "sync" the list of stored files
     * among the P2P network.
     * 
     * @param filesystem The filesystem to update
     * @param peer The peer from which to get the keys
     */
    public FSFileSyncer(P2PFS filesystem, FSPeer peer) {
        _filesystem = filesystem;
        _peer = peer;
    }

    public void run() {
        while (true) {
            try {
                keys = _peer.getAllPaths();

                // TODO: what about removal?
                for (String key : keys) {
                    if (_filesystem.getPath(key) == null) {

                        _filesystem.create(key, null, null);
                    }
                }

                Thread.sleep(1000);
            } catch (Exception pEx) {
                pEx.printStackTrace();
            }
        }
    }
}
