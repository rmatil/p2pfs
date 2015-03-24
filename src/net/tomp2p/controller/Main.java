package net.tomp2p.controller;

import java.util.List;
import java.util.Map;

import net.tomp2p.bootstrapserver.BoostrapServerAccess;
import net.tomp2p.fspeer.FSPeer;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.util.MyJSONParser;


public class Main {

    public static void main(String[] args) {
        BoostrapServerAccess boostrapServerAccess = new BoostrapServerAccess();
        FSPeer fsPeer = new FSPeer();
        
        // 2 ways to get the ip address
        String myIP = fsPeer.findLocalIp();
        // String myIP = fsPeer.findExternalIp();
        int myPort = 4000;
        
        List<Map<String, String>> ipList = MyJSONParser.parse(boostrapServerAccess.get());
        boostrapServerAccess.post(myIP, 4000);
        
        try {
            if(ipList.size() == 0){   
                fsPeer.startAsBootstrapPeer(myIP, myPort);
                fsPeer.put(new Number160(100), new Data("This is my first put"));
                System.out.println("Put Successfull");
            } else {
                fsPeer.startPeer(myIP, myIP, 4000, 4000);
                System.out.println(fsPeer.get(new Number160(100)));
            }
                       
//            fsPeer.shutdown();
        } catch (Exception pEx) {
            pEx.printStackTrace();
        }
        

    }

}
