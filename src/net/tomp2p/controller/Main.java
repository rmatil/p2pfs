package net.tomp2p.controller;

import java.util.List;
import java.util.Map;

import net.tomp2p.bootstrapserver.BoostrapServerAccess;
import net.tomp2p.fspeer.FSPeer;
import net.tomp2p.util.InputReader;
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
        int size = ipList.size();
        
        boostrapServerAccess.post(myIP, 4000);
       
        try {
            if(size == 0){   
                fsPeer.startAsBootstrapPeer(myIP, myPort);
            } else {
                boolean success = false;
                int counter = 0;
                
                while(!success || counter < size){
                  success = fsPeer.startPeer(myIP, ipList.get(0).get("address"), 4000, Integer.parseInt(ipList.get(0).get("port")));
                  counter++;
                }
                
                if(success){
                    System.out.println("Bootstrap succesfull");
                } else {
                    System.out.println("No connection possible");
                }
            }
        
//            // for testing on own pc
//            String myIP = "172.20.10.5";
//            int myPort = 4000;
//            // fsPeer.startAsBootstrapPeer(myIP, myPort);
//            fsPeer.startPeer(myIP, myIP, myPort, myPort);
            
            InputReader.readAndProcess(fsPeer);    
            
            boostrapServerAccess.remove(myIP, myPort);
            fsPeer.shutdown();
        } catch (Exception pEx) {
            pEx.printStackTrace();
        }
        

    }

}
