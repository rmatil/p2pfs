package net.f4fs.filesystem.partials;

import java.nio.ByteBuffer;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MemorySymLink
        extends AMemoryPath {

    private final Logger logger   = LoggerFactory.getLogger(MemorySymLink.class);
    private String       target;
    private AMemoryPath  existingPath;

    /**
     * Must contain the target file name
     */
    private ByteBuffer   contents = ByteBuffer.allocate(0);


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
        // time of modification time
        stat.atime(super.getLastModificationTimestamp());
        // time of last access time
        stat.mtime(super.getLastAccessTimestamp());
        // Time when file status was last changed (inode data modification).
        // Changed by the chmod(2), chown(2), link(2), mknod(2), rename(2), unlink(2), utimes(2) and write(2) system calls.
        stat.ctime(super.getLastModificationTimestamp());

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
