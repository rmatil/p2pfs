package net.f4fs.vdht;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.test.ExampleUtils;
import net.tomp2p.utils.Pair;

public class VDHTOperations {

	private static Pair<Number640, Data> retrieve(PeerDHT pPeerDHT, Number160 pLocationKey)
			throws InterruptedException,
			ClassNotFoundException, IOException {
		Pair<Number640, Data> pair = null;
		for (int i = 0; i < 5; i++) {
			FutureGet fg = pPeerDHT.get(pLocationKey).getLatest().start()
					.awaitUninterruptibly();
			// check if all the peers agree on the same latest version, if not
			// wait a little and try again
			pair = checkVersions(fg.rawData());
			if (pair != null) {
				break;
			}
			System.out.println("get delay or fork - get");
			Thread.sleep(RND.nextInt(500));
		}
		// we got the latest data
		if (pair != null) {
			return pair;
		}
		return null;
	}

	private static void retrieve(PeerDHT pPeerDHT, Number160 pLocationKey,
			Number160 pVersionKey) {
		//TODO: Retrieve particular version
	}

	private static void store(PeerDHT pPeerDHT, Number160 pLocationKey, Data pFileData)
			throws ClassNotFoundException, InterruptedException, IOException {
		Pair<Number640, Byte> pair2 = null;
		for (int i = 0; i < 5; i++) {
			Pair<Number160, Data> pair = getAndUpdate(pPeerDHT, pLocationKey, pFileData);
			if (pair == null) {
				System.out.println("we cannot handle this kind of inconsistency automatically, handing over the the API dev");
				return;
			}
			FuturePut fp = pPeerDHT.put(pLocationKey)
					.data(Number160.ZERO, pair.element1().prepareFlag(),
							pair.element0()).start().awaitUninterruptibly();
			pair2 = checkVersions(fp.rawResult());
			// 1 is PutStatus.OK_PREPARED
			if (pair2 != null && pair2.element1() == 1) {
				break;
			}
			System.out.println("get delay or fork - put");
			// if not removed, a low ttl will eventually get rid of it
			pPeerDHT.remove(pLocationKey).versionKey(pair.element0()).start()
					.awaitUninterruptibly();
			Thread.sleep(RND.nextInt(500));
		}
		if (pair2 != null && pair2.element1() == 1) {
			FuturePut fp = pPeerDHT.put(Number160.ONE)
					.versionKey(pair2.element0().versionKey()).putConfirm()
					.data(new Data()).start().awaitUninterruptibly();
			System.out.println("stored!: " + fp.failedReason());
		} else {
			System.out.println("we cannot handle this kind of inconsistency automatically, handing over the the API dev");
		}
	}

	// get the latest version and do modification. In this case, append the
	// string
	private static Pair<Number160, Data> getAndUpdate(PeerDHT peerDHT, Number160 pLocationKey,
			Data pFileData) throws InterruptedException, ClassNotFoundException,
			IOException {
		Pair<Number640, Data> pair = null;
		for (int i = 0; i < 5; i++) {
			FutureGet fg = peerDHT.get(pLocationKey).getLatest().start()
					.awaitUninterruptibly();
			// check if all the peers agree on the same latest version, if not
			// wait a little and try again
			pair = checkVersions(fg.rawData());
			if (pair != null) {
				break;
			}
			System.out.println("get delay or fork - get");
			Thread.sleep(RND.nextInt(500));
		}
		// we got the latest data
		if (pair != null) {
			// update operation
			// TODO: let user know about old versions
			Data newData = pFileData;
			Number160 v = pair.element0().versionKey();
			long version = v.timestamp() + 1;
			newData.addBasedOn(v);
			// since we create a new version, we can access old versions as well
			return new Pair<Number160, Data>(new Number160(version,
					newData.hash()), newData);
		}
		return null;
	}

	private static <K> Pair<Number640, K> checkVersions(
			Map<PeerAddress, Map<Number640, K>> rawData) {
		Number640 latestKey = null;
		K latestData = null;
		for (Map.Entry<PeerAddress, Map<Number640, K>> entry : rawData
				.entrySet()) {
			if (latestData == null && latestKey == null) {
				latestData = entry.getValue().values().iterator().next();
				latestKey = entry.getValue().keySet().iterator().next();
			} else {
				if (!latestKey.equals(entry.getValue().keySet().iterator()
						.next())
						|| !latestData.equals(entry.getValue().values()
								.iterator().next())) {
					return null;
				}
			}
		}
		return new Pair<Number640, K>(latestKey, latestData);
	}

	// STANDALONE TEST RUN SECTION ***

	private static final Random RND = new Random(42L);

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {
		PeerDHT master = null;
		final int nrPeers = 100;
		final int port = 4001;

		try {
			PeerDHT[] peers = ExampleUtils.createAndAttachPeersDHT(nrPeers,
					port);
			ExampleUtils.bootstrap(peers);
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
					store(peers[1], Number160.ONE, new Data(" one "));
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
					store(peers[2], Number160.ONE, new Data(" two "));
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
					store(peers[3], Number160.ONE, new Data(" three "));
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
		// you will see all three versions, however, not in the right order
		System.out.println("res: "
				+ fg.rawData().values().iterator().next().values().iterator()
						.next().object());

		Pair<Number640, Data> finalPair = retrieve(peers[1], Number160.ONE);
		System.out.println("retrieve: "+finalPair.element1().object());
	}

}
