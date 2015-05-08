package net.f4fs.filesystem.fsfilemonitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class WriteFileEventListener
        implements ICompleteWriteEventListener {
    
    private Logger logger = Logger.getLogger("WriteFileEventListener.class");

    @Override
    public void handleEvent(P2PFS pP2PFS, FSPeer pFsPeer, String pPath, ByteBuffer pContent)
            throws ClassNotFoundException, InterruptedException, IOException {
        pFsPeer.putData(Number160.createHash(pPath), new Data(pContent.array()));
        logger.info("Wrote whole file on path '" + pPath + "' containing '" + pContent.capacity() + "' bytes to DHT");
    }

}
