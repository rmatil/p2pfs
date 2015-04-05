package net.f4fs.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.f4fs.bootstrapserver.BootstrapServerAccess;
import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;
import net.f4fs.util.DhtOperationsCommand;
import net.f4fs.util.IpAddressJsonParser;
import net.f4fs.util.ShutdownHookThread;

import org.json.simple.parser.ParseException;


public class Main {
    
    public static final String MOUNT_POINT = "./P2PFS";
    public static final int MY_PORT = 4000;

    public static void main(String[] args) {
        BootstrapServerAccess boostrapServerAccess = new BootstrapServerAccess();
        FSPeer fsPeer = new FSPeer();

        // 2 ways to get the ip address
        String myIP = fsPeer.findLocalIp();
        // String myIP = fsPeer.findExternalIp();

        List<Map<String, String>> ipList = new ArrayList<Map<String, String>>();
        try {
            ipList = IpAddressJsonParser.parse(boostrapServerAccess.getIpAddressList());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int nrOfIpAddresses = ipList.size();
        boostrapServerAccess.postIpPortPair(myIP, 4000);

        // start as bootstrap peer or connect to other peers
        try {
            if (nrOfIpAddresses == 0) {
                fsPeer.startAsBootstrapPeer(myIP, MY_PORT);
            } else {
                boolean success = false;
                int counter = 0;

                while (!success && counter < nrOfIpAddresses) {
                    success = fsPeer.startPeer(myIP, 4000, ipList.get(counter).get("address"), Integer.parseInt(ipList.get(counter).get("port")));
                    counter++;
                }

                if (success) {
                    System.out.println("[Peer@" + myIP + "]: Bootstrap successfull");
                } else {
                    System.out.println("[Peer@" + myIP + "]: No connection possible");
                }
            }

//            // for testing on own pc
//            String myIP = "172.20.10.5";
//            int myPort = 4000;
//            // fsPeer.startAsBootstrapPeer(myIP, myPort);
//            fsPeer.startPeer(myIP, myIP, myPort, myPort);
            
            // Add shutdown hook so that IP address gets removed from server when 
            // user does not terminate program correctly on 
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(myIP, MY_PORT));

            // start file system with the connected peer
//            new P2PFS(fsPeer).log(true).mount(mountPoint);
            new P2PFS(fsPeer).mount(MOUNT_POINT);
            
//            // probably not needed anymore
//            DhtOperationsCommand.readAndProcess(fsPeer);

            boostrapServerAccess.removeIpPortPair(myIP, MY_PORT);
            fsPeer.shutdown();
        } catch (Exception pEx) {
            pEx.printStackTrace();
        }
    }
}
