package test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import net.f4fs.vdht.VDHTOperations;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Standalone test for VDHTOperations.
 * Based on the VDHT example of Thomas Bocek's TomP2P.
 * 
 * @author Christian
 */
public class VDHTOperationsTest {

    private static final int NR_OF_PEERS = 100;
    private static final int PORT        = 4001;

    private static PeerDHT[] peers;


    @BeforeClass
    public static void initTest()
            throws IOException {
        peers = TestUtils.createAndAttachPeersDHT(NR_OF_PEERS, PORT);
        TestUtils.bootstrap(peers);
    }

    @Test
    public void testVdht()
            throws IOException, InterruptedException, ClassNotFoundException {
        FuturePut fp = peers[0].put(Number160.ONE).data(new Data("start -")).start().awaitUninterruptibly();
        System.out.println("stored initial value: " + fp.failedReason());
        final CountDownLatch cl = new CountDownLatch(3);

        // store them simultaneously
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    VDHTOperations.store(peers[1], Number160.ONE, new Data("one"));
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
                    VDHTOperations.store(peers[2], Number160.ONE, new Data("two"));
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
                    VDHTOperations.store(peers[3], Number160.ONE, new Data("three"));
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
