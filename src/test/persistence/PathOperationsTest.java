package test.persistence;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.f4fs.fspeer.FSPeer;
import net.f4fs.persistence.path.PathOperations;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.junit.BeforeClass;
import org.junit.Test;


public class PathOperationsTest {

    private static Number160      TEST_DATA_DIR_KEY = null;
    private static Data           TEST_DATA_DIR      = null;

    private static Number160      TEST_DATA_FILE_KEY = null;
    private static Data           TEST_DATA_FILE     = null;

    private static PeerDHT        peerDht            = null;
    private static PathOperations pathOperations     = null;

    @BeforeClass
    public static void initTest()
            throws Exception {
        pathOperations = new PathOperations();

        FSPeer fsPeer = new FSPeer();

        fsPeer.startAsBootstrapPeer();
        peerDht = fsPeer.getPeerDHT();

        TEST_DATA_DIR_KEY = Number160.createHash("/this/is/a/path");
        TEST_DATA_DIR = new Data("/this/is/a/path");
        
        TEST_DATA_FILE_KEY = Number160.createHash("/this/is/a/file.txt");
        TEST_DATA_FILE = new Data("/this/is/a/file.txt");
    }

    @Test
    public void getAllPathsTest()
            throws InterruptedException, ClassNotFoundException, IOException {
        pathOperations.putPath(peerDht, TEST_DATA_DIR_KEY, TEST_DATA_DIR);
        pathOperations.putPath(peerDht, TEST_DATA_FILE_KEY, TEST_DATA_FILE);
    
        Set<String> results = pathOperations.getAllPaths(peerDht);
        
        assertEquals("Did not contain all paths", this.getExpectdResults(), results);
    }

    @Test
    public void putPathTets()
            throws InterruptedException, ClassNotFoundException, IOException {
        pathOperations.removePath(peerDht, TEST_DATA_DIR_KEY);
        pathOperations.removePath(peerDht, TEST_DATA_FILE_KEY);
        
        pathOperations.putPath(peerDht, TEST_DATA_DIR_KEY, TEST_DATA_DIR);
        pathOperations.putPath(peerDht, TEST_DATA_FILE_KEY, TEST_DATA_FILE);
        
        Set<String> results = pathOperations.getAllPaths(peerDht);
        
        assertEquals("Put did not store paths", this.getExpectdResults(), results);
    }

    @Test
    public void removePathTest()
            throws InterruptedException, ClassNotFoundException, IOException {
        pathOperations.putPath(peerDht, TEST_DATA_DIR_KEY, TEST_DATA_DIR);
        pathOperations.putPath(peerDht, TEST_DATA_FILE_KEY, TEST_DATA_FILE);
        
        Set<String> results = pathOperations.getAllPaths(peerDht);
        
        assertEquals("Put did not work", this.getExpectdResults(), results);
        
        pathOperations.removePath(peerDht, TEST_DATA_DIR_KEY);
        pathOperations.removePath(peerDht, TEST_DATA_FILE_KEY);
        
        results = pathOperations.getAllPaths(peerDht);
        
        assertEquals("Remove did not work", new HashSet<String>(), results);
    }
    
    @Test
    public void orverwritePathTest()
            throws InterruptedException, ClassNotFoundException, IOException {
        pathOperations.removePath(peerDht, TEST_DATA_DIR_KEY);
        pathOperations.removePath(peerDht, TEST_DATA_FILE_KEY);
        
        pathOperations.putPath(peerDht, TEST_DATA_DIR_KEY, TEST_DATA_DIR);
        pathOperations.putPath(peerDht, TEST_DATA_DIR_KEY, TEST_DATA_DIR);
        
        pathOperations.putPath(peerDht, TEST_DATA_FILE_KEY, TEST_DATA_FILE);
        pathOperations.putPath(peerDht, TEST_DATA_FILE_KEY, TEST_DATA_FILE);
        
        Set<String> results = pathOperations.getAllPaths(peerDht);
        
        assertEquals("Overwrite did not work", this.getExpectdResults(), results);
    }
    
    protected Set<String> getExpectdResults() {
        Set<String> ret = new HashSet<>();
        ret.add("/this/is/a/path");
        ret.add("/this/is/a/file.txt");
        
        return ret;
    }

}
