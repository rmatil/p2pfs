package net.tomp2p.util;

import java.io.IOException;
import java.util.Scanner;

import net.tomp2p.fspeer.FSPeer;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class InputReader {

    public static final String QUIT = "q";

    @SuppressWarnings("static-access")
    public static void readAndProcess(FSPeer peer){
        String input = "";
        Scanner scanner = new Scanner(System.in); 
        
        while(!input.equals(QUIT)){
            System.out.println("Enter <put>:<key>:<value>, <get>:<key> or 'q' for quit:");
            System.out.println("Input: ");
            input = scanner.nextLine();
            
            String[] inputArray =  input.split(":");
            
            try {
                if(inputArray[0].equals("put")){
                    peer.put(new Number160().createHash(inputArray[1]), new Data(inputArray[2]));
                 }
                 else if(inputArray[0].equals("get")){
                    System.out.println("Output: " + peer.get(new Number160().createHash(inputArray[1])));
                 } else if (inputArray[0].equals(QUIT)){
                     System.out.println("Shutdown of input console");
                 } else {
                     System.out.println("Wrong input format: use get or put");
                 }
            } catch (IOException | ClassNotFoundException pEx) {
                pEx.printStackTrace();
            
            }
        }
        
        scanner.close();
    }
 }
