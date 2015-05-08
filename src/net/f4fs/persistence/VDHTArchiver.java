package net.f4fs.persistence;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import net.f4fs.fspeer.PersistenceFactory;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class VDHTArchiver {

    private final IPathPersistence   pathOperations;
    private final IPersistence       simpleDHTOperations;

    private static final int         MAX_VERSIONS = 5;
    private static Logger            logger       = Logger.getLogger("VDHTArchiver.class");

    private PeerDHT                  peerDHT;

    private String                   directoryPath;
    private String                   fileName;
    private String                   versionFolderPath;
    private String                   versionQueuePath;
    private String                   fileExtension;

    public VDHTArchiver(PeerDHT pPeerDHT) {
        this.peerDHT = pPeerDHT;
        this.pathOperations = PersistenceFactory.getPathPersistence();
        this.simpleDHTOperations = PersistenceFactory.getDhtOperations();
    }

    public void archive(Number160 pLocationKey, Data pOldFile)
            throws ClassNotFoundException, IOException, InterruptedException {

        // Create paths from locationKey
        extractPaths(pLocationKey, pOldFile);

        // Check & create Version Folder
        if (!versionFolderExists()) {
            createVersionFolder();
        }

        // Save current file to version folder
        saveOldFileToVersionFolder(pOldFile);

        // Make sure the version folder doesn't bloat.
        pruneVersionFolder();
    }


    private void extractPaths(Number160 pLocationKey, Data pOldFile)
            throws IOException, ClassNotFoundException, InterruptedException {

        String filePath = this.pathOperations.getPath(peerDHT, pLocationKey);

        if (filePath == null) {
            logger.warning("Could not retrieve filePath for versionFolder.");
            throw new IOException();
        }

        int slashIndex = filePath.lastIndexOf("/");
        int dotIndex = filePath.lastIndexOf(".");

        this.directoryPath = filePath.substring(0, slashIndex + 1);
        this.fileName = filePath.substring(slashIndex + 1);
        this.fileExtension = filePath.substring(dotIndex + 1);
        this.versionFolderPath = directoryPath.concat(".").concat((fileName).replace('.', '_'));
        this.versionQueuePath = versionFolderPath.concat("/.versionQueue");
        System.out.println("Extracted paths");
    }


    private boolean versionFolderExists()
            throws ClassNotFoundException, InterruptedException, IOException {

        String retrievedVersionFolderPath = pathOperations.getPath(peerDHT, Number160.createHash(versionFolderPath));

        if (retrievedVersionFolderPath != null) {
            System.out.println("Version folder already exists");
            return true;
        }
        System.out.println("Version folder does not exist");
        return false;
    }


    private void createVersionFolder()
            throws InterruptedException, IOException, ClassNotFoundException {
        // Initialize version queue
        ArrayBlockingQueue<String> versionQueue = new ArrayBlockingQueue<String>(MAX_VERSIONS + 1);

        // Put version queue
        simpleDHTOperations.putData(peerDHT, Number160.createHash(versionQueuePath), new Data(versionQueue));
        pathOperations.putPath(peerDHT, Number160.createHash(versionFolderPath), new Data(versionFolderPath));

        System.out.println("create version folder");
    }


    private void saveOldFileToVersionFolder(Data pOldFile)
            throws InterruptedException, IOException, ClassNotFoundException {

        String currentVersion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"));
        String pathToArchive = versionFolderPath.concat("/").concat(fileName.replace('.', '_')).concat("_").concat(currentVersion).concat(".").concat(fileExtension);

        System.out.println("FileversionPath " + pathToArchive);
        // Put data
        simpleDHTOperations.putData(peerDHT, Number160.createHash(pathToArchive), pOldFile);
        // Put path
        System.out.println("number160.createHash: " + Number160.createHash(pathToArchive) + ", path: " + pathToArchive);
        pathOperations.putPath(peerDHT, Number160.createHash(pathToArchive), new Data(pathToArchive));
        System.out.println("Archiver DHT Paths " + pathOperations.getAllPaths(peerDHT));
        // get version queue from version folder
        ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peerDHT, Number160.createHash(versionQueuePath)).object();
        // add new version to queue
        versionQueue.put(pathToArchive);
        // put version queue back to version folder
        simpleDHTOperations.putData(peerDHT, Number160.createHash(versionQueuePath), new Data(versionQueue));
        System.out.println("Save old version");
    }

    private void pruneVersionFolder()
            throws InterruptedException, ClassNotFoundException, IOException {

        // get version queue from version folder
        ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peerDHT, Number160.createHash(versionQueuePath)).object();

        if (versionQueue.size() > MAX_VERSIONS) {
            // delete version
            String versionToDelete = versionQueue.remove();
            // Remove file
            simpleDHTOperations.removeData(peerDHT, Number160.createHash(versionToDelete));
            // Remove path
            pathOperations.removePath(peerDHT, Number160.createHash(versionToDelete));
            System.out.println("Pruned version folder");
        } else {
            System.out.println("Did not prune version folder");
        }

        // put version queue back to version folder
        simpleDHTOperations.putData(peerDHT, Number160.createHash(versionQueuePath), new Data(versionQueue));
    }
}
