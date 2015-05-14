package net.f4fs.filesystem.partials;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import net.f4fs.config.FSStatConfig;
import net.f4fs.fspeer.FSPeer;
import net.fusejna.ErrorCodes;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MemoryFile
        extends AMemoryPath {

    private ByteBuffer   contents = ByteBuffer.allocate(0);

    private final Logger logger   = LoggerFactory.getLogger(MemoryFile.class);

    /**
     * Creates a new instance of this file in the DHT
     * 
     * @param name The name of this file
     * @param peer The peer
     */
    public MemoryFile(final String name, FSPeer peer) {
        super(name, peer);
        this.logger.info("Created File with name '" + name + "' without parent");
    }

    /**
     * Creates this file as a child of parent. Stores this instance
     * also in the DHT
     * 
     * @param name The name of this file
     * @param parent The directory in which to store this file
     * @param peer The peer
     */
    public MemoryFile(final String name, final MemoryDirectory parent, final FSPeer peer) {
        super(name, parent, peer);
        this.logger.info("Created File with name '" + name + "' on path '" + getPath() + "'.");
    }

    /**
     * Creates this file in the DHT and puts its content in it
     * 
     * @param name The name of this file
     * @param text The content
     * @param peer The peer
     */
    public MemoryFile(final String name, final String text, final FSPeer peer) {
        // stores an empty file in the DHT
        super(name, peer);
        try {
            final byte[] contentBytes = text.getBytes(StandardCharsets.UTF_8);
            contents = ByteBuffer.wrap(contentBytes);

            // only update value on the content key because file was already
            // created in parent constructor
            String stringContent = new String(contents.array(), StandardCharsets.UTF_8);
            super.getPeer().putData(Number160.createHash(getPath()), new Data(stringContent));

            logger.info("Created File with name '" + name + "' on path '" + getPath() + "'.");

        } catch (final IOException | InterruptedException | ClassNotFoundException e) {
            logger.error("Could not create file with name '" + name + "' on path '" + getPath() + "'. StackTrace: " + e.getStackTrace().toString());

            try {
                // remove file (also the content key in the location keys)
                super.getPeer().removeData(Number160.createHash(getPath()));
                super.getPeer().removePath(Number160.createHash(getPath()));
            } catch (InterruptedException e1) {
                logger.error("Could not create file with name '" + name + "' on path '" + getPath() + "'. Message: " + e.getStackTrace().toString());
            }
        }
    }

    @Override
    public void getattr(final StatWrapper stat) {
        // time of modification time
        stat.atime(super.getLastModificationTimestamp());
        // time of last access time
        stat.mtime(super.getLastAccessTimestamp());
        // Time when file status was last changed (inode data modification).
        // Changed by the chmod(2), chown(2), link(2), mknod(2), rename(2), unlink(2), utimes(2) and write(2) system calls.
        stat.ctime(super.getLastModificationTimestamp());

        // sets the optimal transfer block size:
        // usually the one of the FS
        stat.blksize(FSStatConfig.BIGGER.getBsize());
        // The actual number of blocks allocated for the file in 512-byte units.
        // As short symbolic links are stored in the inode, this number may be zero.
        stat.blocks(contents.capacity() / 512l);

        // ID of device containing file
        // stat.dev(dev);

        // file generation number
        // stat.gen(gen);

        // Group ID of the file
        // stat.gid(gid);

        // Note: if ino and rdev are taken together, they uniquely
        // identify the file among multiple filesystems
        // File serial number
        // stat.ino(ino); // only unique on the current FS
        // Device ID
        // stat.rdev(rdev);

        // Number of hard links which link to this file
        // Hard links are multiple directory entries which link to the same file -> created by link system call.
        // From man link: "A hard link to a file is indistinguishable from the original directory entry; any changes to a file are effectively inde-
        // pendent of the name used to reference the file. Hard links may not normally refer to directories and may not span file systems."
        // TODO: how do we check these?
        // stat.nlink(0);

        // set access modes
        stat.setMode(NodeType.FILE, true, true, true, true, true, true, true, true, true);
        stat.size(contents.capacity());

        // NOTE: according to the manual entry of man 2 stat these fields should not be changed
        // RESERVED: DO NOT USE!
        // stat.lspare(lspare);
        // RESERVED: DO NOT USE!
        // stat.qspare(qspare);
    }

    /**
     * Reads <i>size</i> bytes from the content of this file starting at <i>offset</i>.
     * 
     * @param buffer The buffer to which the read bytes are written
     * @param size The amount of bytes which should get read
     * @param offset The position of the content of which reading should be started
     * 
     * @return Number of bytes which got read
     */
    public int read(final ByteBuffer buffer, final long size, final long offset) {
        super.setLastAccessTimestamp((System.currentTimeMillis() / 1000l));
        final int bytesToRead = (int) Math.min(contents.capacity() - offset, size);
        final byte[] bytesRead = new byte[bytesToRead];

        synchronized (this) {
            try {
                Data data = super.getPeer().getData(Number160.createHash(getPath()));

                if (null == data) {
                    logger.warn("Could not read file on path '" + getPath() + "' from the DHT. Data was null");
                    return -ErrorCodes.EIO();
                }

                // replace current content with the content stored in the DHT
                contents = ByteBuffer.wrap(data.toBytes());

            } catch (ClassNotFoundException | IOException | InterruptedException e) {
                logger.error("Could not read contents of file on path '" + getPath() + "'. StackTrace: " + e.getStackTrace().toString());
                return -ErrorCodes.EIO();
            }


            contents.position((int) offset);
            contents.get(bytesRead, 0, bytesToRead);
            buffer.put(bytesRead);
            contents.position(0); // Rewind
        }

        return bytesToRead;
    }

    /**
     * Causes this file to be truncated to a
     * size of precisely <i>size</i> bytes.
     * If the file is currently larger than <i>size</i>,
     * the contents after are lost.
     * 
     * @param size The size to which it should be truncated
     */
    public synchronized void truncate(final long size) {
        super.setLastModificationTimestamp((System.currentTimeMillis() / 1000l));
        if (size < contents.capacity()) {
            // Need to create a new, smaller buffer
            final ByteBuffer newContents = ByteBuffer.allocate((int) size);
            final byte[] bytesRead = new byte[(int) size];

            // writes as much bytes of contents into bytesRead
            contents.get(bytesRead);

            // update the value on disk
            newContents.put(bytesRead);
            contents = newContents;
            
            this.logger.info("Truncated '" + this.getPath() + "' to '" + size + "' bytes");
        }
    }

    /**
     * Writes up to <i>bufSize</i> bytes to the
     * file referenced by the file descriptor <i>buffer</i>
     * from the buffer starting at <i>writeOffset</i> <br>
     * <b style="color:red">NOTE: This method gets called multiple times for a certain file because it gets written in chunks</b>
     * 
     * @param buffer The byteBuffer with the file content in it
     * @param bufSize The size of the buffer to write
     * @param writeOffset Position where the write begins
     * 
     * @return ErrorCode if failed, <i>bufSize</i> if succeeded
     */
    public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset) {
        super.setLastModificationTimestamp((System.currentTimeMillis() / 1000l));
        final int maxWriteIndex = (int) (writeOffset + bufSize);
        final byte[] bytesToWrite = new byte[(int) bufSize];
        synchronized (this) {
            if (maxWriteIndex > contents.capacity()) {
                // Need to create a new, larger buffer
                final ByteBuffer newContents = ByteBuffer.allocate(maxWriteIndex);
                newContents.put(contents);
                contents = newContents;
            }
            buffer.get(bytesToWrite, 0, (int) bufSize);

            // update with this chunk
            contents.position((int) writeOffset);
            contents.put(bytesToWrite);
            contents.position(0); // Rewind
        }
        
        this.logger.info("Wrote '" + bufSize + "' bytes starting at offset '" + writeOffset + "' to file on path '" + this.getPath() + "'");

        return (int) bufSize;
    }

    /**
     * Return the content of this MemoryFile
     * 
     * @return Contents ByteBuffer
     */
    public ByteBuffer getContent() {
        return contents;
    }
}
