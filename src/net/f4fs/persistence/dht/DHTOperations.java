package net.f4fs.persistence.dht;

import net.f4fs.fspeer.GetListener;
import net.f4fs.fspeer.PutListener;
import net.f4fs.fspeer.RemoveListener;
import net.f4fs.persistence.IPersistence;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

/**
 * Implements an adapter to store, retrieve and remove data in the DHT.
 * <br>
 * <b>Note:</b> No versions of stored data are supported
 */
public class DHTOperations
        implements IPersistence {

    @Override
    public Data getData(PeerDHT pPeer, Number160 pLocationKey)
            throws InterruptedException {
        FutureGet futureGet = pPeer.get(pLocationKey).start();
        futureGet.addListener(new GetListener(pPeer.peerAddress().inetAddress().toString(), "Get data"));

        futureGet.await();

        return futureGet.data();
    }

    @Override
    public Data getDataOfVersion(PeerDHT pPeer, Number160 pLocationKey, Number160 pVersionKey)
            throws InterruptedException {
        return this.getData(pPeer, pLocationKey);
    }

    @Override
    public void putData(PeerDHT pPeer, Number160 pLocationKey, Data pData)
            throws InterruptedException {
        FuturePut futurePut = pPeer.put(pLocationKey).data(pData).start();
        futurePut.addListener(new PutListener(pPeer.peerAddress().inetAddress().toString(), "Put data"));

        futurePut.await();
    }

    @Override
    public void removeData(PeerDHT pPeer, Number160 pKey)
            throws InterruptedException {
        FutureRemove futureRemove = pPeer.remove(pKey).start();
        futureRemove.addListener(new RemoveListener(pPeer.peerAddress().inetAddress().toString(), "Remove data"));

        futureRemove.await();
    }

	@Override
	public void removeDataOfVersion(PeerDHT pPeer, Number160 pKey,
			Number160 pVersionKey) throws InterruptedException {
		this.removeData(pPeer, pKey);
	}

}
