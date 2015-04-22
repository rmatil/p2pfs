package test.persistence;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import net.f4fs.fspeer.FSPeer;
import net.f4fs.persistence.DHTOperations;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.junit.BeforeClass;
import org.junit.Test;


public class DHTOperationsTest {

    private static String        TEST_IP       = "";
    private static int           TEST_PORT     = 4000;

    private static Number160     TEST_KEY      = null;
    private static Data          TEST_DATA     = null;

    private static PeerDHT       peerDht       = null;
    private static DHTOperations dhtOperations = null;

    @BeforeClass
    public static void initTest()
            throws Exception {
        dhtOperations = new DHTOperations();

        FSPeer fsPeer = new FSPeer();
        TEST_IP = fsPeer.findLocalIp();

        fsPeer.startAsBootstrapPeer(TEST_IP, TEST_PORT);
        peerDht = fsPeer.getPeerDHT();

        TEST_KEY = Number160.createHash(2);
        TEST_DATA = new Data("test data");
    }

    @Test
    public void getData()
            throws ClassNotFoundException, IOException, InterruptedException {

        dhtOperations.putData(peerDht, TEST_KEY, TEST_DATA);
        Data retrievedData = dhtOperations.getData(peerDht, TEST_KEY);
        assertEquals("Stored data was not equal", TEST_DATA, retrievedData);
    }

    @Test
    public void getDataOfVersion()
            throws InterruptedException {
        // this should ignore the key since dhtOperations does not provide versions
        dhtOperations.putData(peerDht, TEST_KEY, TEST_DATA);
        Data retrievedData = dhtOperations.getDataOfVersion(peerDht, TEST_KEY, TEST_KEY);
        assertEquals("Versioned data did not ignore versions", TEST_DATA, retrievedData);
    }

    @Test
    public void putData()
            throws InterruptedException {
        dhtOperations.putData(peerDht, TEST_KEY, TEST_DATA);
        dhtOperations.removeData(peerDht, TEST_KEY);

        Data retrievedData = dhtOperations.getData(peerDht, TEST_KEY);
        assertEquals("Did not remove data on DHT", null, retrievedData);
    }

    @Test
    public void removeData()
            throws InterruptedException {
        dhtOperations.putData(peerDht, TEST_KEY, TEST_DATA);

        Data retrievedData = dhtOperations.getData(peerDht, TEST_KEY);
        assertEquals("Did not put data to DHT", TEST_DATA, retrievedData);

        dhtOperations.removeData(peerDht, TEST_KEY);

        retrievedData = dhtOperations.getData(peerDht, TEST_KEY);
        assertEquals("Did not remove data from DHT", null, retrievedData);
    }

    @Test
    public void removeDataWithVersion()
            throws InterruptedException {
        dhtOperations.putData(peerDht, TEST_KEY, TEST_DATA);

        Data retrievedData = dhtOperations.getDataOfVersion(peerDht, TEST_KEY, TEST_KEY);
        assertEquals("Did not put data to DHT", TEST_DATA, retrievedData);

        dhtOperations.removeData(peerDht, TEST_KEY, TEST_KEY);

        retrievedData = dhtOperations.getDataOfVersion(peerDht, TEST_KEY, TEST_KEY);
        assertEquals("Did not remove data from DHT", null, retrievedData);
    }
}
