package net.tomp2p.bootstrapserver;

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
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;


public class BootstrapPeer {
        
    public static void main(String args[]){
        
//        BoostrapServerAccess boostrapServerAccess = new BoostrapServerAccess();
//        
//        // 2 ways to get the ip address
//        String myIP = "";
//        String myIP2 = "";
//        BufferedReader bufferedReader;
//        try {
//            // way 1
//            URL ipRequest = new URL("http://checkip.amazonaws.com");
//            bufferedReader = new BufferedReader(new InputStreamReader(ipRequest.openStream()));
//            myIP = bufferedReader.readLine(); 
//            bufferedReader.close();
//            
//            // way 2
//            myIP2 = Inet4Address.getLocalHost().getHostAddress();
//        } catch (IOException pEx) {
//            pEx.printStackTrace();
//        }
//
//        System.out.println(myIP);
//        System.out.println(myIP2);
//        System.out.println("");
//        
//        System.out.println(boostrapServerAccess.get());
//        System.out.println("");
//        System.out.println(boostrapServerAccess.post(myIP, 4000));
//        
        try {
            startServer("");
            startClient("", "");
        } catch (Exception pEx) {
            pEx.printStackTrace();
        }
    }
    
    public static void startServer(String myIP) throws Exception {
        Random rnd = new Random(43L);
        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(
                InetAddress.getByName(myIP));
        // b.addInterface("eth0");
        Peer master = new PeerBuilder(new Number160(rnd)).ports(4000).bindings(b).start();
        System.out.println("Server started Listening to: " + DiscoverNetworks.discoverInterfaces(b));
        System.out.println("address visible to outside is " + master.peerAddress());
        while (true) {
            for (PeerAddress pa : master.peerBean().peerMap().all()) {
                System.out.println("PeerAddress: " + pa);
            }
            
            Thread.sleep(1500);
        }
    }
    
    public static void startClient(String myIP, String ipAddress) throws Exception {
        Random rnd = new Random();
        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(
                InetAddress.getByName(myIP));
        // b.addInterface("eth0");
        Peer client = new PeerBuilder(new Number160(rnd)).ports(4001).bindings(b).start();
        System.out.println("Client started and Listening to: " + DiscoverNetworks.discoverInterfaces(b));
        System.out.println("address visible to outside is " + client.peerAddress());

        InetAddress address = Inet4Address.getByName(ipAddress);
        int masterPort = 4000;
        PeerAddress pa = new PeerAddress(Number160.ZERO, address, masterPort, masterPort);

        System.out.println("PeerAddress: " + pa);
        
        // Future Discover
        FutureDiscover futureDiscover = client.discover().inetAddress(address).ports(masterPort).start();
        futureDiscover.awaitUninterruptibly();

        // Future Bootstrap - slave
        FutureBootstrap futureBootstrap = client.bootstrap().inetAddress(address).ports(masterPort).start();
        futureBootstrap.awaitUninterruptibly();

        Collection<PeerAddress> addressList = client.peerBean().peerMap().all();
        System.out.println(addressList.size());

        if (futureDiscover.isSuccess()) {
            System.out.println("found that my outside address is " + futureDiscover.peerAddress());
        } else {
            System.out.println("failed " + futureDiscover.failedReason());
        }
        client.shutdown();
    }

}
