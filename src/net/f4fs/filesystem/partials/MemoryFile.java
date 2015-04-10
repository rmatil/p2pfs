package net.f4fs.filesystem.partials;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import net.f4fs.fspeer.FSPeer;
import net.fusejna.ErrorCodes;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class MemoryFile
        extends AMemoryPath {

    private ByteBuffer contents = ByteBuffer.allocate(0);

    private Logger     logger   = Logger.getLogger("MemoryFile.class");

    /**
     * Creates a new instance of this file in the DHT
     * 
     * @param name The name of this file
     * @param peer The peer
     */
    public MemoryFile(final String name, FSPeer peer) {
        super(name, peer);
        logger.info("Created File '" + name + "' without parent");
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
        logger.info("Created File '" + name + "' in parent directory " + parent.getName());
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
            final byte[] contentBytes = text.getBytes("UTF-8");
            contents = ByteBuffer.wrap(contentBytes);

            // only update value on the content key because file was already 
            // created in parent constructor
            String stringContent = new String(contents.array(), StandardCharsets.UTF_8);
            FuturePut futurePut = super.getPeer().putData(Number160.createHash(getPath()), new Data(stringContent));
            futurePut.await();
            
        } catch (final IOException | InterruptedException e) {
            logger.warning("Could not create file " + name + ". Message: " + e.getMessage());
            
            try {
                // remove file (also the content key in the location keys)
                FutureRemove futureRemove = super.getPeer().removeData(Number160.createHash(getPath()));
                futureRemove.await();
                futureRemove = super.getPeer().removePath(Number160.createHash(getPath()));
                futureRemove.await();
            } catch (InterruptedException e1) {
                logger.warning("Could not create file " + name + ". Message: " + e.getMessage());
            }
        }
    }

    @Override
    public void getattr(final StatWrapper stat) {
        stat.setMode(NodeType.FILE).size(contents.capacity());
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
        final int bytesToRead = (int) Math.min(contents.capacity() - offset, size);
        final byte[] bytesRead = new byte[bytesToRead];
        synchronized (this) {
            try {
                FutureGet futureGet = super.getPeer().getData(Number160.createHash(getPath()));
                futureGet.await();
                String stringContent = (String) futureGet.data().object();
                
                // replace current content with the content stored in the DHT
                ByteBuffer byteBuffer = ByteBuffer.allocate(stringContent.getBytes().length);
                byteBuffer.put(stringContent.getBytes(StandardCharsets.UTF_8));
                contents = byteBuffer;

            } catch (ClassNotFoundException | IOException | InterruptedException e) {
                logger.warning("Could not read contents of path segment " + getPath() + ". Message: " + e.getMessage());
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
        if (size < contents.capacity()) {
            // Need to create a new, smaller buffer
            final ByteBuffer newContents = ByteBuffer.allocate((int) size);
            final byte[] bytesRead = new byte[(int) size];

            // writes as much bytes of contents into bytesRead
            contents.get(bytesRead);

            String stringContent = new String(bytesRead, StandardCharsets.UTF_8);
            try {
                // try to update the shortened value
                FuturePut futurePut = super.getPeer().putData(Number160.createHash(getPath()), new Data(stringContent));
                futurePut.await();

                // only if DHT update succeeds update the value on disk
                newContents.put(bytesRead);
                contents = newContents;
            } catch (IOException | InterruptedException e) {
                logger.warning("Could not truncate the contents of the file " + getPath() + ". Message: " + e.getMessage());
            }
        }
    }

    /**
     * Writes up to <i>bufSize</i> bytes to the
     * file referenced by the file descriptor <i>buffer</i>
     * from the buffer starting at <i>writeOffset</i>
     * 
     * @param buffer The byteBuffer with the file content in it
     * @param bufSize The size of the buffer to write
     * @param writeOffset Position where the write begins
     * 
     * @return ErrorCode if failed, <i>bufSize</i> if succeeded
     */
    public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset) {
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

            String stringContent = new String(bytesToWrite, StandardCharsets.UTF_8);
            try {
                // try to update the value in the DHT
                FuturePut futurePut = super.getPeer().putData(Number160.createHash(getPath()), new Data(stringContent));
                futurePut.await();

                // only if DHT update succeeds udpate the value on disk
                contents.position((int) writeOffset);
                contents.put(bytesToWrite);
                contents.position(0); // Rewind
            } catch (IOException | InterruptedException e) {
                logger.warning("Could not write to file " + getPath() + ". Message; " + e.getMessage());
                return -ErrorCodes.EIO();
            }
        }

        return (int) bufSize;
    }

}
