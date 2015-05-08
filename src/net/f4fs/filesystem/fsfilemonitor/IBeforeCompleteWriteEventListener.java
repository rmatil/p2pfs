package net.f4fs.filesystem.fsfilemonitor;

import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;


public interface IBeforeCompleteWriteEventListener {

    public void handleEvent(P2PFS pfilesystem, FSPeer pFsPeer);
}
