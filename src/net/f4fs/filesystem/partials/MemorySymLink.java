package net.f4fs.filesystem.partials;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    private Logger      logger   = Logger.getLogger("MemorySymLink.class");
    private String      target;
    private AMemoryPath existingPath;

    /**
     * Must contain the target file name
     */
    private ByteBuffer  contents = ByteBuffer.allocate(0);

    /**
     * A symbolic link <code>target</code> is created to <code>path</code>.
     * (<code>target</code> is the name of the file created, <code>path</code> is the string used in creating the symbolic link)
     * 
     * @param path The already existing file to which the link should be created
     * @param target The path of the symlink which points to <code>path</code>
     * @param peer The FSPeer
     */
    public MemorySymLink(final AMemoryPath path, final String target, final FSPeer peer) {
        super(target, peer);
        this.existingPath = path;
        this.target = target;

        this.init();
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
    public MemorySymLink(final AMemoryPath path, final String target, final MemoryDirectory parent, final FSPeer peer) {
        super(target, parent, peer);
        this.existingPath = path;
        this.target = target;

        this.init();
    }

    private void init() {
        // because AMemoryPath also stores data in the DHT of this symlink
        // and we have to extend from it, we have to remove this here again...
        // TODO: If anyone can think of a better idea, please change
        try {
            // a symbolic link must contain the name of the target as content
            // as stated in <code>man ln</code>
            contents = ByteBuffer.wrap(target.getBytes());
            FuturePut futurePut = super.getPeer().putData(Number160.createHash(getPath()), new Data(contents.array()));
            futurePut.await();

            // create the symlink to the target
            futurePut = super.getPeer().putPath(Number160.createHash(getPath()), new Data(this.existingPath.getPath()));
            futurePut.await();

            logger.info("Created symlink '" + target + "' on path '" + getPath() + "' to file on path '" + this.existingPath.getPath() + "'.");
        } catch (InterruptedException | IOException e) {
            logger.warning("Could not create symlink '" + target + "' on path '" + getPath() + "' to file on path '" + this.existingPath.getPath() + "'. Message: " + e.getMessage());
        }
    }

    @Override
    public void getattr(StatWrapper stat) {
        stat.setMode(NodeType.SYMBOLIC_LINK);
    }
    
    public AMemoryPath getExistingPath() {
        return existingPath;
    }
    
    public void setExistingPath(AMemoryPath existingPath) {
        this.existingPath = existingPath;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    
    public ByteBuffer getContents() {
        return this.contents;
    }

}
