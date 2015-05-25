package test.persistence;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import net.f4fs.config.Config;
import net.f4fs.persistence.data.DHTOperations;
import net.f4fs.persistence.data.VDHTOperations;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.junit.BeforeClass;


/**
 * Standalone test for VDHTOperations.
 * Based on the VDHT example of Thomas Bocek's TomP2P.
 * 
 * @author Christian
 */
public class VDHTOperationsTest {

    private static final int                NR_OF_PEERS         = 100;
    private static final int                PORT                = 4001;
    private static final DHTOperations      simpleDHTOperations = new DHTOperations();


    private static PeerDHT[]                peers;

    private VDHTOperations                  vdhtOperations      = new VDHTOperations();


    @BeforeClass
    public static void initTest()
            throws IOException {
        peers = TestUtils.createAndAttachPeersDHT(NR_OF_PEERS, PORT);
        TestUtils.bootstrap(peers);
    }

    public void testVdht()
            throws IOException, InterruptedException, ClassNotFoundException {
        String testKey = "/asdf/ghjk/yxvc.zip";
        String versionQueueKey = "/asdf/ghjk/.yxvc_zip/.versionQueue";

        FuturePut fpPath = peers[0].put(Number160.createHash(Config.DEFAULT.getMasterLocationPathsKey())).data(Number160.createHash(testKey), new Data(testKey)).start()
                .awaitUninterruptibly();
        FuturePut fpData = peers[0].put(Number160.createHash(testKey)).data(new Data("start -")).start().awaitUninterruptibly();
        System.out.println("stored initial path: " + fpPath.failedReason());
        System.out.println("stored initial value: " + fpData.failedReason());

        final CountDownLatch cl = new CountDownLatch(3);


        // store them simultaneously
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    vdhtOperations.putData(peers[1], Number160.createHash(testKey), new Data("one"));
                     ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peers[1], Number160.createHash(versionQueueKey)).object();
                     System.out.println(versionQueue.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cl.countDown();
            }
        }).start();


        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    vdhtOperations.putData(peers[2], Number160.createHash(testKey), new Data("two"));
                    ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peers[2], Number160.createHash(versionQueueKey)).object();
                    System.out.println(versionQueue.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cl.countDown();
            }
        }).start();


        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    vdhtOperations.putData(peers[3], Number160.createHash(testKey), new Data("three"));
                    ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peers[3], Number160.createHash(versionQueueKey)).object();
                    System.out.println(versionQueue.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cl.countDown();
            }
        }).start();
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    vdhtOperations.putData(peers[4], Number160.createHash(testKey), new Data("four"));
                    ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peers[4], Number160.createHash(versionQueueKey)).object();
                    System.out.println(versionQueue.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cl.countDown();
            }
        }).start();

        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    vdhtOperations.putData(peers[5], Number160.createHash(testKey), new Data("five"));
                    ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peers[5], Number160.createHash(versionQueueKey)).object();
                    System.out.println(versionQueue.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cl.countDown();
            }
        }).start();
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    vdhtOperations.putData(peers[6], Number160.createHash(testKey), new Data("six"));
                    ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peers[6], Number160.createHash(versionQueueKey)).object();
                    System.out.println(versionQueue.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cl.countDown();
            }
        }).start();
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    vdhtOperations.putData(peers[7], Number160.createHash(testKey), new Data("seven"));
                    ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peers[7], Number160.createHash(versionQueueKey)).object();
                    System.out.println(versionQueue.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cl.countDown();
            }
        }).start();


        // wait until all 3 threads are finished
        cl.await();

        // get latest version
        FutureGet fg = peers[5].get(Number160.ONE).getLatest().start().awaitUninterruptibly();


        assertEquals("Did not save three versions of data", 3, fg.rawData().values().size());
        // check values
        ArrayList<String> results = new ArrayList<>();
        for (Data entry : fg.rawData().values().iterator().next().values()) {
            results.add((String) entry.object());
        }
        assertThat("Results are not the same", results, anyOf(contains("one"), contains("two"), contains("three")));

    }
}
