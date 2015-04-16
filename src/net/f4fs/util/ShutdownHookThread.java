package net.f4fs.util;

import net.f4fs.bootstrapserver.BootstrapServerAccess;


/**
 * Removes the specified IP-port pair from the bootstrap server.
 * Gets invoked when JVM gets terminate or the user hits ^c.
 * 
 * @author Raphael
 */
public class ShutdownHookThread
        extends Thread {

    /**
     * The IP address to remove
     * from the bootstrap server
     */
    private String ipAddress;
    
    /**
     * The port to the IP address to remove 
     * from the bootstrap server
     */
    private int port;
    
    /**
     * Creates a new instance of this object
     * 
     * @param pIpAddress The IP address to remove from the bootstrap server
     * @param pPort The corresponding port
     */
    public ShutdownHookThread(String pIpAddress, int pPort) {
        this.ipAddress = pIpAddress;
        this.port = pPort;
    }
    
    public void run() {
        BootstrapServerAccess bootsrapServerAccess = new BootstrapServerAccess();
        System.out.println();
        System.out.println("[Shutdown Hook]: Remove IP address from bootstrap server if still exists...");
        bootsrapServerAccess.removeIpPortPair(ipAddress, port);
        System.out.println("[Shutdown Hook]: Removal done");
    }
}
