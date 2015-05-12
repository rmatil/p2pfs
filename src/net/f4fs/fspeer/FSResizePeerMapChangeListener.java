package net.f4fs.fspeer;

import net.f4fs.config.FSStatConfig;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatistic;


public class FSResizePeerMapChangeListener implements PeerMapChangeListener{
    
    private PeerDHT peer;
    
    public FSResizePeerMapChangeListener(PeerDHT peer) {
        this.peer = peer;
    }

    @Override
    public void peerInserted(PeerAddress arg0, boolean arg1) {
        resize();
    }

    @Override
    public void peerRemoved(PeerAddress arg0, PeerStatistic arg1) {
      resize();
    }

    @Override
    public void peerUpdated(PeerAddress arg0, PeerStatistic arg1) {

    }
    
    private void resize() {
        int size = peer.peerBean().peerMap().all().size();
        FSStatConfig.RESIZE.setBlocks(size * FSStatConfig.RESIZEINIT.getBlocks());
        FSStatConfig.RESIZE.setBfree(size * FSStatConfig.RESIZEINIT.getBfree());
        FSStatConfig.RESIZE.setBavail(size * FSStatConfig.RESIZEINIT.getBavail());
    }

}
