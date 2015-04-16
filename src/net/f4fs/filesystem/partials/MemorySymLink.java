package net.f4fs.filesystem.partials;

import java.io.IOException;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class MemorySymLink
        extends AMemoryPath {

    private Logger logger = Logger.getLogger("MemorySymLink.class");
    private String target;

    /**
     * A symbolic link <code>target</code> is created to <code>path</code>.
     * (<code>target</code> is the name of the file created, <code>path</code> is the string used in creating the symbolic link)
     * 
     * @param path The already existing file to which the link should be created
     * @param target The path of the symlink which points to <code>path</code>
     * @param peer The FSPeer
     */
    public MemorySymLink(final String path, final String target, final FSPeer peer) {
        super(target, peer);
        this.target = target;

        // because AMemoryPath also stores data in the DHT of this symlink
        // and we have to extend from it, we have to remove this here again...
        // TODO: If anyone can think of a better idea, please change
        try {
            FutureRemove futureRemove = peer.removeData(Number160.createHash(getPath()));
            futureRemove.await();

            // create the symlink to the target
            FuturePut futurePut = peer.putPath(Number160.createHash(path), new Data(target));
            futurePut.await();

            logger.info("Created symlink '" + target + "' to file on path '" + path + "'.");
        } catch (InterruptedException | IOException e) {
            logger.warning("Could not create symlink '" + target + "' to file on path '" + path + "'. Message: " + e.getMessage());
        }
    }

    /**
     * A symbolic link <code>target</code> is created to <code>path</code>.
     * (<code>target</code> is the name of the file created, <code>path</code> is the string used in creating the symbolic link)
     * 
     * @param path The already existing file to which the link should be created
     * @param target The path of the symlink which points to <code>path</code>
     * @param parent The parent directory
     * @param peer The FSPeer
     */
    public MemorySymLink(final String path, final String target, final MemoryDirectory parent, final FSPeer peer) {
        super(target, parent, peer);
        this.target = target;

        // because AMemoryPath also stores data in the DHT of this symlink
        // and we have to extend from it, we have to remove this here again...
        // TODO: If anyone can think of a better idea, please change
        try {
            FutureRemove futureRemove = peer.removeData(Number160.createHash(getPath()));
            futureRemove.await();

            // create the symlink to the target
            FuturePut futurePut = peer.putPath(Number160.createHash(path), new Data(target));
            futurePut.await();

            logger.info("Created symlink '" + target + "' to file on path '" + path + "'.");
        } catch (InterruptedException | IOException e) {
            logger.warning("Could not create symlink '" + target + "' to file on path '" + path + "'. Message: " + e.getMessage());
        }
    }

    @Override
    public void getattr(StatWrapper stat) {
        stat.setMode(NodeType.SYMBOLIC_LINK);
    }

    public String getTarget() {
        return target;
    }

}
