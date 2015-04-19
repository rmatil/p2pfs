package test;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import test.TestUtils;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;
import net.f4fs.vdht.VDHTOperations;

/**
 * Standalone test for VDHTOperations.
 * Based on the VDHT example of Thomas Bocek's TomP2P.
 * 
 * @author Christian
 */
public class TestVDHTOperations {

	private static final Random RND = new Random(42L);
	
	public static void main(String[] args)
			throws IOException, ClassNotFoundException, InterruptedException {
		PeerDHT master = null;
		final int nrPeers = 100;
		final int port = 4001;

		try {
			PeerDHT[] peers = TestUtils.createAndAttachPeersDHT(nrPeers,
					port);
			TestUtils.bootstrap(peers);
			master = peers[0];
			Number160 nr = new Number160(RND);
			testVDHT(peers, nr);
		} finally {
			if (master != null) {
				master.shutdown();
			}
		}
	}

	private static void testVDHT(final PeerDHT[] peers, Number160 nr)
			throws IOException, ClassNotFoundException, InterruptedException {
		FuturePut fp = peers[0].put(Number160.ONE).data(new Data("start -"))
				.start().awaitUninterruptibly();
		System.out.println("stored initial value: " + fp.failedReason());
		final CountDownLatch cl = new CountDownLatch(3);

		// store them simultaneously
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					VDHTOperations.store(peers[1], Number160.ONE, new Data(" one "));
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
					VDHTOperations.store(peers[2], Number160.ONE, new Data(" two "));
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
					VDHTOperations.store(peers[3], Number160.ONE, new Data(" three "));
				} catch (Exception e) {
					e.printStackTrace();
				}
				cl.countDown();
			}
		}).start();

		// wait until all 3 threads are finished
		cl.await();

		// get latest version
		FutureGet fg = peers[5].get(Number160.ONE).getLatest().start()
				.awaitUninterruptibly();
		// you will see all three versions, however, not in the right order
		System.out.println("res: "
				+ fg.rawData().values().iterator().next().values().iterator()
						.next().object());

		Pair<Number640, Data> finalPair = VDHTOperations.retrieve(peers[1], Number160.ONE);
		System.out.println("retrieve: " + finalPair.element1().object());
	}

}
