package net.f4fs.controller;

import java.io.IOException;
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
import net.fusejna.FuseException;

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
            if (nrOfIpAddresses == 0) {
                fsPeer.startAsBootstrapPeer(myIp, Config.DEFAULT.getPort());
            } else {
                boolean success = false;
                int counter = 0;

                while (!success && (counter < nrOfIpAddresses)) {
                    success = fsPeer.startPeer(myIp,
                            Config.DEFAULT.getPort(),
                            ipList.get(counter).get("address"),
                            Integer.parseInt(ipList.get(counter).get("port")));
                    counter++;
                }

                if (success) {
                    System.out.println("[Peer@" + myIp + "]: Bootstrap successfull");
                    
                    // start file system with the connected peer
                    new P2PFS(fsPeer).mount(Config.DEFAULT.getMountPoint());
                    
                    // start command line interface
                    if (Config.DEFAULT.getStartCommandLineInterface()) {
                        DhtOperationsCommand.startCommandLineInterface(fsPeer);   
                    }
                } else {
                    System.out.println("[Peer@" + myIp + "]: No connection possible");
                }
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
