package net.tomp2p.bootstrapserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.URL;


public class BootstrapPeer {
    
    public static final String BOOTSTRAP_SERVER = "http://188.226.178.35";
    
    public static void main(String args[]){
        
        BoostrapServerAccess boostrapServerAccess = new BoostrapServerAccess(BOOTSTRAP_SERVER);
        
        // 2 ways to get the ip address
        String myIP = "";
        String myIP2 = "";
        BufferedReader bufferedReader;
        try {
            // way 1
            URL ipRequest = new URL("http://checkip.amazonaws.com");
            bufferedReader = new BufferedReader(new InputStreamReader(ipRequest.openStream()));
            myIP = bufferedReader.readLine(); 
            bufferedReader.close();
            
            // way 2
            myIP2 = Inet4Address.getLocalHost().getHostAddress();
        } catch (IOException pEx) {
            pEx.printStackTrace();
        }

        System.out.println(myIP);
        System.out.println(myIP2);
        
        System.out.println(boostrapServerAccess.get());
        System.out.println("");
        System.out.println(boostrapServerAccess.post("address", myIP));
    }

}
