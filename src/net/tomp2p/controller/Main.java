package net.tomp2p.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.tomp2p.bootstrapserver.BootstrapServerAccess;
import net.tomp2p.fspeer.FSPeer;
import net.tomp2p.util.DhtOperationsCommand;
import net.tomp2p.util.IpAddressJsonParser;
import net.tomp2p.util.ShutdownHookThread;

import org.json.simple.parser.ParseException;


public class Main {

    public static void main(String[] args) {
        BootstrapServerAccess boostrapServerAccess = new BootstrapServerAccess();
        FSPeer fsPeer = new FSPeer();

        // 2 ways to get the ip address
        String myIP = fsPeer.findLocalIp();
        // String myIP = fsPeer.findExternalIp();
        int myPort = 4000;

        List<Map<String, String>> ipList = new ArrayList<>();
        try {
            ipList = IpAddressJsonParser.parse(boostrapServerAccess.getIpAddressList());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int nrOfIpAddresses = ipList.size();
        boostrapServerAccess.postIpPortPair(myIP, 4000);

        try {
            if (nrOfIpAddresses == 0) {
                fsPeer.startAsBootstrapPeer(myIP, myPort);
            } else {
                boolean success = false;
                int counter = 0;

                while (!success && counter < nrOfIpAddresses) {
                    success = fsPeer.startPeer(myIP, 4000, ipList.get(0).get("address"), Integer.parseInt(ipList.get(0).get("port")));
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
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(myIP, myPort));

            DhtOperationsCommand.readAndProcess(fsPeer);

            boostrapServerAccess.removeIpPortPair(myIP, myPort);
            fsPeer.shutdown();
        } catch (Exception pEx) {
            pEx.printStackTrace();
        }
    }

}
