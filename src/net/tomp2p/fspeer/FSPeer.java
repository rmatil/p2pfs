package net.tomp2p.fspeer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collection;
import java.util.Random;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.DiscoverNetworks;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;


/**
 * The file system peer
 * 
 * @author Reto
 */
public class FSPeer {

    private PeerDHT peer;

    /**
     * Starts this peer as the first, i.e. bootstrap peer
     * 
     * @param myIP The IP Address of this peer
     * @param myPort The corresponding port
     * 
     * @throws Exception
     */
    public void startAsBootstrapPeer(String myIP, int myPort)
            throws Exception {

        Random rnd = new Random(43L);
        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(
                InetAddress.getByName(myIP));

        // b.addInterface("eth0");
        peer = new PeerBuilderDHT(new PeerBuilder(new Number160(rnd)).ports(myPort).bindings(b).start()).start();
        System.out.println("[Peer@" + myIP + "]: Server started listening to: " + DiscoverNetworks.discoverInterfaces(b));
        System.out.println("[Peer@" + myIP + "]: Address visible to outside is " + peer.peerAddress());
    }

    /**
     * Starts this peer with the given parameters
     * 
     * @param myIP The IP address of this peer
     * @param myPort The port of this peer
     * @param connectionIpAddress The IP address to which this peer should be connected
     * @param connectionPort The port to which this peer should be connected
     * @return True, if started successfully, false otherwise
     * 
     * @throws Exception
     */
    public boolean startPeer(String myIP, int myPort, String connectionIpAddress, int connectionPort)
            throws Exception {

        Random rnd = new Random();
        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(
                InetAddress.getByName(myIP));

        // b.addInterface("eth0");
        peer = new PeerBuilderDHT(new PeerBuilder(new Number160(rnd)).ports(myPort).bindings(b).start()).start();
        System.out.println("[Peer@" + myIP + "]: Client started and listening to: " + DiscoverNetworks.discoverInterfaces(b));
        System.out.println("[Peer@" + myIP + "]: Address visible to outside is " + peer.peerAddress());

        InetAddress address = Inet4Address.getByName(connectionIpAddress);
        PeerAddress connectionPeerAddress = new PeerAddress(Number160.ZERO, address, connectionPort, connectionPort);

        System.out.println("[Peer@" + myIP + "]: Connected to " + connectionPeerAddress);

        // Future Discover
        FutureDiscover futureDiscover = peer.peer().discover().inetAddress(address).ports(connectionPort).start();
        futureDiscover.awaitUninterruptibly();

        // Future Bootstrap - slave
        FutureBootstrap futureBootstrap = peer.peer().bootstrap().inetAddress(address).ports(connectionPort).start();
        futureBootstrap.awaitUninterruptibly();

        Collection<PeerAddress> addressList = peer.peerBean().peerMap().all();
        System.out.println("[Peer@" + myIP + "]: Address list size: " + addressList.size());

        if (futureDiscover.isSuccess()) {
            System.out.println("[Peer@" + myIP + "]: Outside IP address is " + futureDiscover.peerAddress());
            return true;
        }

        System.out.println("[Peer@" + myIP + "]: Failed " + futureDiscover.failedReason());
        return false;
    }

    /**
     * Shuts down this peer
     */
    public void shutdown() {
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

        System.out.println("Listing connected peers: ");
        for (PeerAddress pa : peer.peerBean().peerMap().all()) {
            System.out.println("[ConnectedPeer]: " + pa);
        }
        System.out.println("Done");
    }

    /**
     * Tries to fetch the IP address of this peer in the local network
     * 
     * @return The found IP address
     */
    public String findLocalIp() {
        String ip = "";

        try {
            ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (IOException pEx) {
            pEx.printStackTrace();
        }
        return ip;
    }


    /**
     * Tries to fetch the IP address seen from outside
     * 
     * @return The IP Address
     */
    public String findExternalIp() {
        BufferedReader bufferedReader;
        String ip = "";

        try {
            URL ipRequest = new URL("http://checkip.amazonaws.com");
            bufferedReader = new BufferedReader(new InputStreamReader(ipRequest.openStream()));
            ip = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException pEx) {
            pEx.printStackTrace();
        }

        return ip;
    }

    /**
     * Gets the value stored on the given key
     * 
     * @param pKey The key to retrieve its value from
     * @return An object containing the stored data
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Object get(Number160 pKey)
            throws ClassNotFoundException, IOException {
        FutureGet futureGet = peer.get(pKey).start();
        futureGet.awaitUninterruptibly();

        return futureGet.data().object();
    }

    /**
     * Stores the given data on the given key
     * 
     * @param pKey The key to store the data
     * @param pValue The data to store
     * 
     * @throws IOException
     */
    public void put(Number160 pKey, Data pValue) {
        FuturePut futurePut = peer.put(pKey).data(pValue).start();
        futurePut.awaitUninterruptibly();
    }

}
