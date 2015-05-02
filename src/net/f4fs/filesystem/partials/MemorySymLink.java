package net.f4fs.filesystem.partials;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;


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
     * @param parent The parent directory
     * @param peer The FSPeer
     */
    public MemorySymLink(final AMemoryPath path, final String target, final MemoryDirectory parent, final FSPeer peer) {
        super(target, parent, peer);
        this.existingPath = path;
        this.target = target;
        
        // add the target's path as content as required
        // and stated in <code>man ln</code>
        contents = ByteBuffer.wrap(target.getBytes());
        
        logger.info("Created symlink '" + target + "' on path '" + getPath() + "' to file on path '" + existingPath.getPath() + "'.");
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
