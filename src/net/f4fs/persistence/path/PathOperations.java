package net.f4fs.persistence.path;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.f4fs.config.Config;
import net.f4fs.fspeer.GetListener;
import net.f4fs.fspeer.PutListener;
import net.f4fs.fspeer.RemoveListener;
import net.f4fs.persistence.IPathPersistence;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;


public class PathOperations
        implements IPathPersistence {

    @Override
    public Set<String> getAllPaths(PeerDHT pPeer)
            throws InterruptedException, ClassNotFoundException, IOException {

        Set<String> keys = new HashSet<>();

        FutureGet futureGet = pPeer.get(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).all().start();
        futureGet.addListener(new GetListener(pPeer.peerAddress().inetAddress().toString(), "Get all paths"));
        futureGet.await();

        Map<Number640, Data> map = futureGet.dataMap();
        Collection<Data> collection = map.values();

        Iterator<Data> iter = collection.iterator();
        while (iter.hasNext()) {
            keys.add((String) iter.next().object());
        }

        return keys;
    }
    
    @Override
    public String getPath(PeerDHT pPeer, Number160 pContentKey)
            throws InterruptedException, ClassNotFoundException, IOException {
        FutureGet futureGet = pPeer.get(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).contentKey(pContentKey).start();
        futureGet.addListener(new GetListener(pPeer.peerAddress().inetAddress().toString(), "Get path for content key " + pContentKey.toString(true)));
        
        futureGet.await();
        
        return (String) futureGet.data().object();
    }

    @Override
    public void putPath(PeerDHT pPeer, Number160 pContentKey, Data pValue)
            throws InterruptedException {
        
        FuturePut futurePut = pPeer.put(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).data(pContentKey, pValue).start();
        futurePut.addListener(new PutListener(pPeer.peerAddress().inetAddress().toString(), "Put path"));

        futurePut.await();
    }

    @Override
    public void removePath(PeerDHT pPeer, Number160 pContentKey)
            throws InterruptedException {
       
        FutureRemove futureRemove = pPeer.remove(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).contentKey(pContentKey).start();
        futureRemove.addListener(new RemoveListener(pPeer.peerAddress().inetAddress().toString(), "Remove path"));
     
        futureRemove.await();
    }

}
