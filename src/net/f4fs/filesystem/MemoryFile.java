package net.f4fs.filesystem;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;


public class MemoryFile
        extends AMemoryPath {

    private ByteBuffer contents = ByteBuffer.allocate(0);

    public MemoryFile(final String name) {
        super(name);
    }

    public MemoryFile(final String name, final MemoryDirectory parent) {
        super(name, parent);
    }

    public MemoryFile(final String name, final String text) {
        super(name);
        try {
            final byte[] contentBytes = text.getBytes("UTF-8");
            contents = ByteBuffer.wrap(contentBytes);
        } catch (final UnsupportedEncodingException e) {
            // Not going to happen
        }
    }

    @Override
    protected void getattr(final StatWrapper stat) {
        stat.setMode(NodeType.FILE).size(contents.capacity());
    }

    public int read(final ByteBuffer buffer, final long size, final long offset) {
        final int bytesToRead = (int) Math.min(contents.capacity() - offset, size);
        final byte[] bytesRead = new byte[bytesToRead];
        synchronized (this) {
            contents.position((int) offset);
            contents.get(bytesRead, 0, bytesToRead);
            buffer.put(bytesRead);
            contents.position(0); // Rewind
        }
        return bytesToRead;
    }

    public synchronized void truncate(final long size) {
        if (size < contents.capacity()) {
            // Need to create a new, smaller buffer
            final ByteBuffer newContents = ByteBuffer.allocate((int) size);
            final byte[] bytesRead = new byte[(int) size];
            contents.get(bytesRead);
            newContents.put(bytesRead);
            contents = newContents;
        }
    }

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
            contents.position((int) writeOffset);
            contents.put(bytesToWrite);
            contents.position(0); // Rewind
        }
        return (int) bufSize;
    }

}
