package net.f4fs.persistence.chunked;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.f4fs.config.Config;
import net.f4fs.fspeer.GetListener;
import net.f4fs.fspeer.PutListener;
import net.f4fs.persistence.IPersistence;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureImpl;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Chunks the data before storage and dechunks them on retrieval.
 */
public class ChunkedDHTOperations implements IPersistence {
    @Override
    public Data getData(PeerDHT pPeer, Number160 pLocationKey) throws InterruptedException {
        // Get chunk list
        FutureGet listFutureGet = pPeer.get(pLocationKey).start();
        listFutureGet.addListener(new GetListener(
                pPeer.peerAddress().inetAddress().toString(),
                "Get chunk list"));
        listFutureGet.await();

        // Check for directory: data.toBytes -> null
        if (null == listFutureGet.data()) {
            return listFutureGet.data();
        }

        Type chunkHashesType = new TypeToken<ArrayList<Number160>>(){}.getType();
        ArrayList<Number160> chunkHashes = null;
        try {
            chunkHashes = new Gson().fromJson(new String(listFutureGet.data().toBytes(), "UTF-8"), chunkHashesType);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (chunkHashes != null) {
            ArrayList<byte[]> chunks = new ArrayList<>(chunkHashes.size());

            ArrayList<FutureGet> chunkFutureGets = new ArrayList<>();

            // Get all the chunks
            for (int i = 0; i < chunkHashes.size(); i++) {
                FutureGet ft = pPeer.get(chunkHashes.get(i)).start();
                ft.addListener(new GetListener(
                        pPeer.peerAddress().inetAddress().toString(),
                        "Get chunk " + i + " of " + (chunkHashes.size() - 1)));

                chunkFutureGets.add(ft);
            }

            // Wait for the chunks to arrive, and store them in the list.
            for (int i = 0; i < chunkFutureGets.size(); i++) {
                chunkFutureGets.get(i).await();
                System.out.println("Wait for chunk " + i + " of " + (chunkFutureGets.size() - 1));

                chunks.add(i, chunkFutureGets.get(i).data().toBytes());
            }

            //int totalBytes = chunks.stream().mapToInt(c -> c.length).sum();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            chunks.forEach(chunk -> byteArrayOutputStream.write(chunk, 0, chunk.length));

            return new Data(byteArrayOutputStream.toByteArray());

        } else {
            System.out.println("Couldn't get the chunks!");
            return null;
        }
    }

    @Override
    public Data getDataOfVersion(PeerDHT pPeer, Number160 pLocationKey, Number160 pVersionKey)
            throws InterruptedException {
        return this.getData(pPeer, pLocationKey);
    }

    @Override
    public void putData(PeerDHT pPeer, Number160 pLocationKey, Data pData)
            throws InterruptedException, ClassNotFoundException, IOException {
        // Save a list of chunks under the key.
        // Key for chunks is the hash of that chunk -> Reduced storage usage. Great success!

        byte[] bytes = pData.toBytes();

        ArrayList<byte[]> chunks = new ArrayList<>();

        int lob = bytes.length;

        int chunkSize = Config.DEFAULT.getChunkSizeBytes();

        // Breaking up the chunks
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

        ArrayList<Number160> chunkHashes = new ArrayList<>();

        for (byte[] chunk : chunks) {
            chunkHashes.add(Number160.createHash(chunk.toString()));
        }

        ArrayList<FuturePut> futurePuts = new ArrayList<>();

        // Storing the chunk list
        FuturePut futurePutList = pPeer.put(pLocationKey).data(new Data(new Gson().toJson(chunkHashes).getBytes(Charset.forName("UTF-8")))).start();
        futurePutList.addListener(new PutListener(
                pPeer.peerAddress().inetAddress().toString(),
                "Put chunk list"));
        futurePuts.add(futurePutList);

        // Storing the chunks
        for (int i = 0; i < chunks.size(); i++) {
            FuturePut fp = pPeer.put(chunkHashes.get(i)).data(new Data(chunks.get(i))).start();
            futurePutList.addListener(new PutListener(
                    pPeer.peerAddress().inetAddress().toString(),
                    "Put chunk " + i + " of " + (chunks.size() - 1)));
            futurePuts.add(fp);
        }

        for (FuturePut fp : futurePuts) { fp.await(); }
    }

    @Override
    public void removeData(PeerDHT pPeer, Number160 pKey) throws InterruptedException {
        // remove data && hash list
    }

    @Override
    public void removeDataOfVersion(PeerDHT pPeer, Number160 pKey, Number160 pVersionKey)
            throws InterruptedException {
        this.removeData(pPeer, pKey);
    }
}
