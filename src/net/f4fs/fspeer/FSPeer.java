package net.f4fs.fspeer;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;

import net.f4fs.bootstrapserver.BootstrapServerAccess;
import net.f4fs.config.Config;
import net.f4fs.persistence.path.IPathPersistence;
import net.f4fs.persistence.data.IDataPersistence;
import net.f4fs.persistence.PersistenceFactory;
import net.f4fs.util.RandomDevice;
import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.DiscoverNetworks;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The file system peer
 * 
 * @author Reto
 */
public class FSPeer {

    private PeerDHT               peer;

    private IDataPersistence persistence;

    private IPathPersistence      pathPersistence;

    private BootstrapServerAccess bootstrapServerAccess;

    private String                ip;

    private Logger                logger;

    public FSPeer() {
        this.persistence = PersistenceFactory.getConsensuDhtOperations();
        this.pathPersistence = PersistenceFactory.getConsensusPathOperations();
        this.bootstrapServerAccess = new BootstrapServerAccess();

        this.logger = LoggerFactory.getLogger(FSPeer.class);
    }

    /**
     * Starts this peer as the first, i.e. bootstrap peer
     *
     * @throws Exception
     */
    public void startAsBootstrapPeer()
            throws Exception {

        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(
                Inet4Address.getLocalHost());

        // b.addInterface("eth0");
        peer = new PeerBuilderDHT(new PeerBuilder(new Number160(RandomDevice.INSTANCE.getRand())).ports(Config.DEFAULT.getPort()).bindings(b).start()).start();

        ip = peer.peerAddress().inetAddress().getHostAddress();

        bootstrapServerAccess.postIpPortPair(ip, Config.DEFAULT.getPort());

        logger.info("[Peer@" + ip + "]: Server started listening to: " + DiscoverNetworks.discoverInterfaces(b));
        logger.info("[Peer@" + ip + "]: Address visible to outside is " + peer.peerAddress());
    }

    /**
     * Starts this peer with the given parameters
     * 
     * @param connectionIpAddress The IP address to which this peer should be connected
     * @param connectionPort The port to which this peer should be connected
     * @return True, if started successfully, false otherwise
     * 
     * @throws Exception
     */
    public boolean startPeer(String connectionIpAddress, int connectionPort)
            throws Exception {

        peer = PeerFactory.DHTPeer();

        ip = peer.peerAddress().inetAddress().getHostAddress();

        bootstrapServerAccess.postIpPortPair(ip, Config.DEFAULT.getPort());

        logger.info("[Peer@" + ip + "]: Address visible to outside is " + peer.peerAddress());

        InetAddress address = Inet4Address.getByName(connectionIpAddress);
        PeerAddress connectionPeerAddress = new PeerAddress(Number160.ZERO, address, connectionPort, connectionPort);

        logger.info("[Peer@" + ip + "]: Connected to " + connectionPeerAddress);
        bootstrapServerAccess.postIpPortPair(ip, Config.DEFAULT.getPort());

        // Future Discover
        FutureDiscover futureDiscover = peer.peer().discover().inetAddress(address).ports(connectionPort).start();
        futureDiscover.awaitUninterruptibly();

        // Future Bootstrap - slave
        FutureBootstrap futureBootstrap = peer.peer().bootstrap().inetAddress(address).ports(connectionPort).start();
        futureBootstrap.awaitUninterruptibly();

        Collection<PeerAddress> addressList = peer.peerBean().peerMap().all();
        logger.info("[Peer@" + ip + "]: Address list size: " + addressList.size());

        if (futureDiscover.isSuccess()) {
            logger.info("[Peer@" + ip + "]: Outside IP address is " + futureDiscover.peerAddress());

            return true;
        }

        logger.info("[Peer@" + ip + "]: Failed " + futureDiscover.failedReason());
        return false;
    }

    /**
     * Shuts down this peer
     */
    public void shutdown() {
        removeIpPortPair(peer.peerAddress().toString(), Config.DEFAULT.getPort());
        peer.shutdown();
    }

    /**
     * Prints a list of connected peers to
     * stdout
     * 
     * @throws InterruptedException
     */
    public void printConnectedPeers()
            throws InterruptedException {

        logger.info("Listing connected peers: ");
        for (PeerAddress pa : peer.peerBean().peerMap().all()) {
            logger.info("[ConnectedPeer]: " + pa);
        }
        logger.info("Done");
    }


    /**
     * Gets the value stored on the given key
     * 
     * @param pKey The key to retrieve its value from
     * @return An object containing the stored data
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws InterruptedException If a failure happened during await of future
     */
    public Data getData(Number160 pKey)
            throws ClassNotFoundException, IOException, InterruptedException {
        return this.persistence.getData(this.peer, pKey);
    }

    /**
     * Gets the assigned data (the path to the file) of the given content key on the default location key
     * 
     * @param pContentKey The content key specifying the path of the file
     * 
     * @return FutureGet to get the data
     * 
     * @throws IOException
     * @throws InterruptedException If a failure happened during await of future
     * @throws ClassNotFoundException
     */
    public String getPath(Number160 pContentKey)
            throws ClassNotFoundException, InterruptedException, IOException {
        return this.pathPersistence.getPath(this.peer, pContentKey);
    }

    /**
     * Gets all keys of all files stored in the dht
     * 
     * @return keys List with all keys to the files in the dht
     * 
     * @throws IOException
     * @throws InterruptedException If a failure happened during await of future
     * @throws ClassNotFoundException
     * 
     * @throws Exception
     */
    public Set<String> getAllPaths()
            throws ClassNotFoundException, InterruptedException, IOException {
        return this.pathPersistence.getAllPaths(this.peer);
    }

    /**
     * Stores the given data on the given key
     * 
     * @param pKey The key to store the data
     * @param pValue The data to store
     * 
     * @throws IOException
     * @throws InterruptedException If a failure happened during await of future
     * @throws ClassNotFoundException
     */
    public void putData(Number160 pKey, Data pValue)
            throws InterruptedException, ClassNotFoundException, IOException {
        this.persistence.putData(this.peer, pKey, pValue);
    }

    /**
     * Stores the given data with the given content key on the default location key
     * 
     * @param pContentKey The key to store the data
     * @param pValue The data to store
     * 
     * @throws InterruptedException If a failure happened during await of future
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void putPath(Number160 pContentKey, Data pValue)
            throws InterruptedException, ClassNotFoundException, IOException {
        this.pathPersistence.putPath(this.peer, pContentKey, pValue);
    }

    /**
     * Removes the assigned data from the peer
     * 
     * @param pKey Key of which the data should be removed
     * 
     * @throws InterruptedException If a failure happened during await of future
     */
    public void removeData(Number160 pKey)
            throws InterruptedException {
        this.persistence.removeData(this.peer, pKey);
    }

    /**
     * Removes the file key from the file keys which are stored with the default location key
     * 
     * @param pContentKey
     * 
     * @throws InterruptedException If a failure happened during await of future
     */
    public void removePath(Number160 pContentKey)
            throws InterruptedException {
        this.pathPersistence.removePath(this.peer, pContentKey);
    }

    public PeerDHT getPeerDHT() {
        return this.peer;
    }

    private void removeIpPortPair(String ip, int port) {
        bootstrapServerAccess.removeIpPortPair(ip, port);
    }

    private void setIp() {
        if (peer != null) {
            ip = peer.peerAddress().inetAddress().getHostAddress();
        }
    }

    public String getIp() {
        return ip;
    }
}
