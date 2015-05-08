package net.f4fs.filesystem.fsfilemonitor;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;


public interface ICompleteWriteEventListener {

    public void handleEvent(P2PFS pP2PFS, FSPeer pFsPeer, String pPath, ByteBuffer pContent)
            throws ClassNotFoundException, InterruptedException, IOException;
}
