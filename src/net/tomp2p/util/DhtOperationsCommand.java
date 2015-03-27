package net.tomp2p.util;

import java.io.IOException;
import java.util.Scanner;

import net.tomp2p.fspeer.FSPeer;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


/**
 * Provides a simple interface to the command line
 * to store and retrieve values from the DHT
 * 
 * @author Reto
 */
public class DhtOperationsCommand {

    /**
     * String to quit the command line interface
     */
    public static final String QUIT = "q";

    /**
     * Processes the command line interface to store
     * and retrieve values to / from the DHT
     * 
     * @param peer The peer on which to store / retrieve values
     */
    @SuppressWarnings("static-access")
    public static void readAndProcess(FSPeer peer) {
        String input = "";
        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("> starting input console...");
        System.out.println("> usage: put:<key>:<value>, get:<key> or 'q' for quit");
        
        while (!input.equals(QUIT)) {
            System.out.print("> ");
            input = scanner.nextLine();

            String[] inputArray = input.split(":");

            try {
                if (inputArray[0].equals("put")) {
                    if (inputArray.length < 3) {
                        System.out.println("usage: put:<key>:<value>");
                        continue;
                    }
                    
                    peer.put(new Number160().createHash(inputArray[1]), new Data(inputArray[2]));
                } else if (inputArray[0].equals("get")) {
                    if (inputArray.length < 2) {
                        System.out.println("usage: get:<key>");
                        continue;
                    }

                    System.out.println("> " + peer.get(new Number160().createHash(inputArray[1])));
                } else if (inputArray[0].equals(QUIT)) {
                    System.out.println("> terminating input console...");
                } else {
                    System.out.println("usage: put:<key>:<value>, get:<key> or 'q' for quit");
                }
            } catch (IOException | ClassNotFoundException pEx) {
                pEx.printStackTrace();
            }
        }

        scanner.close();
    }
}
