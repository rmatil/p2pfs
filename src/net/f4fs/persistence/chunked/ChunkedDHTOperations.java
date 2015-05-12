package net.f4fs.persistence.chunked;

import net.f4fs.config.Config;
import net.f4fs.fspeer.PutListener;
import net.f4fs.persistence.IPersistence;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Chunks the data before storage and dechunks them on retrieval.
 */
public class ChunkedDHTOperations implements IPersistence {
    @Override
    public Data getData(PeerDHT pPeer, Number160 pLocationKey) throws InterruptedException {
        return null;
    }

    @Override
    public Data getDataOfVersion(PeerDHT pPeer, Number160 pLocationKey, Number160 pVersionKey) throws InterruptedException {
        return null;
    }

    @Override
    public void putData(PeerDHT pPeer, Number160 pLocationKey, Data pData) throws InterruptedException, ClassNotFoundException, IOException {
        // Save a list of chunks under the key.
        // Key for chunks is the hash of that chunk -> Reduced storage usage. Great success!

        byte[] bytes = pData.toBytes();

        ArrayList<byte[]> chunks = new ArrayList<>();

        int lob = bytes.length;

        int chunkSize = Config.DEFAULT.getChunkSizeBytes();

        if (lob <= chunkSize) {
            chunks.add(bytes);
        } else {
            for (int i = 0; i < lob - chunkSize + 1; i += chunkSize) {
                chunks.add(Arrays.copyOfRange(bytes, i, i + chunkSize));
            }

            if (lob % chunkSize != 0) {
                chunks.add(Arrays.copyOfRange(bytes, lob - lob % chunkSize, lob));
            }
        }

        ArrayList<Number160> hashesOfChunks = new ArrayList<>();

        for (byte[] chunk : chunks) {
            hashesOfChunks.add(new Number160(chunk));
        }

        ArrayList<FuturePut> futurePuts = new ArrayList<>();

        // Storing the chunk list
        FuturePut futurePutList = pPeer.put(pLocationKey).data(new Data(hashesOfChunks)).start();
        futurePutList.addListener(new PutListener(pPeer.peerAddress().inetAddress().toString(), "Put chunk list"));
        futurePuts.add(futurePutList);

        // Storing the chunks
        ArrayList<FuturePut> chunkFutures = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            FuturePut fp = pPeer.put(hashesOfChunks.get(i)).data(new Data(chunks.get(i))).start();
            futurePutList.addListener(new PutListener(pPeer.peerAddress().inetAddress().toString(), "Put chunk " + i + " of " + chunks.size()));
            futurePuts.add(fp);
        }

        for (FuturePut fp : futurePuts) { fp.await(); }
    }

    @Override
    public void removeData(PeerDHT pPeer, Number160 pKey) throws InterruptedException {

    }

    @Override
    public void removeDataOfVersion(PeerDHT pPeer, Number160 pKey, Number160 pVersionKey) throws InterruptedException {

    }
}
