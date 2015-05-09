package net.f4fs.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.f4fs.bootstrapserver.BootstrapServerAccess;
import net.f4fs.config.Config;
import net.f4fs.filesystem.P2PFS;
import net.f4fs.filesystem.event.listeners.SyncFileEventListener;
import net.f4fs.filesystem.event.listeners.WriteFileEventListener;
import net.f4fs.filesystem.fsfilemonitor.FSFileMonitor;
import net.f4fs.fspeer.FSPeer;
import net.f4fs.util.DhtOperationsCommand;
import net.f4fs.util.IpAddressJsonParser;
import net.f4fs.util.ShutdownHookThread;

import org.json.simple.parser.ParseException;


/**
 * Handles startup of the system
 * 
 * @author retwet
 */
public class Startup {

    private BootstrapServerAccess bootstrapServerAccess;
    private FSPeer                fsPeer;
    private String                myIP;

    public Startup() {
        bootstrapServerAccess = new BootstrapServerAccess();
        fsPeer = new FSPeer();
        myIP = fsPeer.findLocalIp();
    }

    /**
     * Starts the system normal, getting the peers connected to
     * the DHT (if some exist) from the bootstrap server
     */
    public void startWithBootstrapServer() {

        List<Map<String, String>> ipList = new ArrayList<>();
        try {
            ipList = IpAddressJsonParser.parse(bootstrapServerAccess.getIpAddressList());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        start(ipList);
    }

    /**
     * Starts the system if the IP and port of the bootstrap peer is known in advance
     * 
     * @param The IP address of the bootstrap peer
     * @param The port of the bootstrap peer
     */
    public void startWithoutBootstrapServer(String connectionIP, String connectionPort) {
        List<Map<String, String>> ipList = new ArrayList<Map<String, String>>();

        Map<String, String> connection = new HashMap<String, String>();
        connection.put("address", connectionIP);
        connection.put("port", connectionPort);

        ipList.add(connection);

        start(ipList);
    }

    /**
     * Start the peer as bootstrap peer if the size of the IP list is zero
     * or connect the peer to the DHT if the size of the IP list > 0
     * and start the file system
     * 
     * @param List of IP/port pairs of peers connected to the DHT
     */
    private void start(List<Map<String, String>> ipList) {
        int nrOfIpAddresses = ipList.size();
        bootstrapServerAccess.postIpPortPair(myIP, Config.DEFAULT.getPort());

        // Connect to other peers if any are available, otherwise start as bootstrap peer
        try {
            boolean success = false;

            if (nrOfIpAddresses == 0) {
                fsPeer.startAsBootstrapPeer(myIP, Config.DEFAULT.getPort());
                success = true;
            } else {
                int counter = 0;

                while (!success && (counter < nrOfIpAddresses)) {
                    success = fsPeer.startPeer(myIP,
                            Config.DEFAULT.getPort(),
                            ipList.get(counter).get("address"),
                            Integer.parseInt(ipList.get(counter).get("port")));
                    counter++;
                }
            }

            if (!success) {
                bootstrapServerAccess.removeIpPortPair(myIP, Config.DEFAULT.getPort());
                fsPeer.shutdown();
                System.out.println("[Shutdown]: Bootstrap failed");
                System.exit(1);
            }

            // Add shutdown hook so that IP address gets removed from server when
            // user does not terminate program correctly on
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(myIP, Config.DEFAULT.getPort()));

            // start file system with the connected peer
            P2PFS filesystem = new P2PFS(fsPeer);
            
            WriteFileEventListener writeFileEventListener = new WriteFileEventListener();
            SyncFileEventListener syncFileEventListener = new SyncFileEventListener();

            FSFileMonitor fsFileMonitor = new FSFileMonitor(filesystem, fsPeer);
            fsFileMonitor.addEventListener(writeFileEventListener);
            fsFileMonitor.addEventListener(syncFileEventListener);
            ExecutorService executorService = Executors.newCachedThreadPool();

            // start thread to maintain local FS
            // note, that thrown exceptions are not shown...
            executorService.submit(fsFileMonitor);

            filesystem.mountAndCreateIfNotExists(Config.DEFAULT.getMountPoint());
            
            // shutdown file monitor here, as P2PFS can not be terminated,
            // if this would be done in afterUnmount due to the still running
            // process of fileMonitor (P2PFS would wait for FSFileMonitor, which would
            // wait, until P2PFS gets unmounted...)
            try {
                fsFileMonitor.terminate();
                
                executorService.shutdown();
                executorService.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("Still running tasks interrupted");
            } finally {
                if (!executorService.isTerminated()) {
                    System.out.println("Cancel non-finished tasks");
                }
                executorService.shutdownNow();
                System.out.println("Shutdown of executor service finished");
            }

            // maybe start command line interface
            if (Config.DEFAULT.getStartCommandLineInterface()) {
                DhtOperationsCommand.startCommandLineInterface(fsPeer);
            }


            bootstrapServerAccess.removeIpPortPair(myIP, Config.DEFAULT.getPort());
            fsPeer.shutdown();
        } catch (Exception pEx) {
            pEx.printStackTrace();
        }
    }
}
