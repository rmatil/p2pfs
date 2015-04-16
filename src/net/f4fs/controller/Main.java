package net.f4fs.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.f4fs.bootstrapserver.BootstrapServerAccess;
import net.f4fs.config.Config;
import net.f4fs.filesystem.P2PFS;
import net.f4fs.fspeer.FSPeer;
import net.f4fs.util.DhtOperationsCommand;
import net.f4fs.util.IpAddressJsonParser;
import net.f4fs.util.ShutdownHookThread;

import org.json.simple.parser.ParseException;


public class Main {

    public static void main(String[] args) {
        BootstrapServerAccess boostrapServerAccess = new BootstrapServerAccess();
        FSPeer fsPeer = new FSPeer();

        String myIp = fsPeer.findLocalIp();

        List<Map<String, String>> ipList = new ArrayList<>();
        try {
            ipList = IpAddressJsonParser.parse(boostrapServerAccess.getIpAddressList());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int nrOfIpAddresses = ipList.size();
        boostrapServerAccess.postIpPortPair(myIp, Config.DEFAULT.getPort());

        // Connect to other peers if any are available, otherwise start as bootstrap peer
        try {
            boolean success = false;

            if (nrOfIpAddresses == 0) {
                fsPeer.startAsBootstrapPeer(myIp, Config.DEFAULT.getPort());
                success = true;
            } else {
                int counter = 0;

                while (!success && (counter < nrOfIpAddresses)) {
                    success = fsPeer.startPeer(myIp,
                            Config.DEFAULT.getPort(),
                            ipList.get(counter).get("address"),
                            Integer.parseInt(ipList.get(counter).get("port")));
                    counter++;
                }
            }
            
            if (!success) {
                boostrapServerAccess.removeIpPortPair(myIp, Config.DEFAULT.getPort());
                fsPeer.shutdown();
                System.out.println("[Shutdown]: Bootstrap failed");
                System.exit(1);
            }
            
            // start file system with the connected peer
            new P2PFS(fsPeer).mountAndCreateIfNotExists(Config.DEFAULT.getMountPoint());
            
            // maybe start command line interface
            if (Config.DEFAULT.getStartCommandLineInterface()) {
                DhtOperationsCommand.startCommandLineInterface(fsPeer);   
            }

            // Add shutdown hook so that IP address gets removed from server when
            // user does not terminate program correctly on
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(myIp, Config.DEFAULT.getPort()));

            boostrapServerAccess.removeIpPortPair(myIp, Config.DEFAULT.getPort());
            fsPeer.shutdown();
        } catch (Exception pEx) {
            pEx.printStackTrace();
        }
    }
}
