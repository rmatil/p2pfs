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


public class FSPeer {
    
    private PeerDHT peer;

    public void startAsBootstrapPeer(String myIP, int myPort)
            throws Exception {
        
        Random rnd = new Random(43L);
        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(
                InetAddress.getByName(myIP));
       
        // b.addInterface("eth0");
        peer = new PeerBuilderDHT(new PeerBuilder(new Number160(rnd)).ports(myPort).bindings(b).start()).start();
        System.out.println("Server started Listening to: " + DiscoverNetworks.discoverInterfaces(b));
        System.out.println("address visible to outside is " + peer.peerAddress());

//        while (true) {
//            for (PeerAddress pa : peer.peerBean().peerMap().all()) {
//                System.out.println("PeerAddress: " + pa);
//            }
//
//            Thread.sleep(1500);
//        }
    }

    public void startPeer(String myIP, String ipAddressToConnectTo, int myPort, int connectionPort)
            throws Exception {
        Random rnd = new Random();
        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(
                InetAddress.getByName(myIP));
        
        // b.addInterface("eth0");
        peer = new PeerBuilderDHT(new PeerBuilder(new Number160(rnd)).ports(myPort).bindings(b).start()).start();
        System.out.println("Client started and Listening to: " + DiscoverNetworks.discoverInterfaces(b));
        System.out.println("address visible to outside is " + peer.peerAddress());

        InetAddress address = Inet4Address.getByName(ipAddressToConnectTo);
        PeerAddress pa = new PeerAddress(Number160.ZERO, address, connectionPort, connectionPort);

        System.out.println("PeerAddress: " + pa);

        // Future Discover
        FutureDiscover futureDiscover = peer.peer().discover().inetAddress(address).ports(connectionPort).start();
        futureDiscover.awaitUninterruptibly();

        // Future Bootstrap - slave
        FutureBootstrap futureBootstrap = peer.peer().bootstrap().inetAddress(address).ports(connectionPort).start();
        futureBootstrap.awaitUninterruptibly();

        Collection<PeerAddress> addressList = peer.peerBean().peerMap().all();
        System.out.println(addressList.size());

        if (futureDiscover.isSuccess()) {
            System.out.println("found that my outside address is " + futureDiscover.peerAddress());
        } else {
            System.out.println("failed " + futureDiscover.failedReason());
        }
    }

    public void shutdown(){
        peer.shutdown();
    }
    
    public String findLocalIp() {
        String ip = "";
        
        try {
            ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (IOException pEx){
            pEx.printStackTrace();
        }
        return ip;
    }

    
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
    
    public Object get(Number160 pKey) throws ClassNotFoundException, IOException{
        FutureGet futureGet = peer.get(pKey).start();
        futureGet.awaitUninterruptibly();
        
        return futureGet.data().object();
    }
    
    public void put(Number160 pKey, Data pValue) throws IOException {
        FuturePut futurePut = peer.put(pKey).data(pValue).start();
        futurePut.awaitUninterruptibly();
    }

}
