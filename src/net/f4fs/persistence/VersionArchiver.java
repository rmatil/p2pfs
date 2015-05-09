package net.f4fs.persistence;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import net.f4fs.fspeer.PersistenceFactory;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


/**
 * Creates for each passed file a version folder, named
 * like <code>.filename_fileExtension</code>. In there
 * at most <i>MAX_VERSIONS<i> versions are stored. If <i>MAX_VERSIONS</i>
 * is exceeded, the latest version is pruned. <br>
 * Versions are named like <code>filename_fileExtension_year_month_day_hourMinutesSeconds.fileExtension</code>.
 * 
 * @author Christian
 *
 */
public class VersionArchiver
        implements IArchiver {

    public static final int        MAX_VERSIONS        = 5;

    protected final String         DIRECTORY_PATH      = "directroy_path";
    protected final String         FILE_NAME           = "file_name";
    protected final String         FILE_EXTENSION      = "file_extension";
    protected final String         VERSION_FOLDER_PATH = "version_folder_path";
    protected final String         VERSION_QUEUE_PATH  = "version_queue_path";

    private Logger                 logger              = Logger.getLogger("VersionArchiver.class");

    /**
     * Access to all stored paths
     */
    private final IPathPersistence pathOperations;

    /**
     * Access to all stored files
     */
    private final IPersistence     simpleDHTOperations;

    /**
     * Access to the DHT
     */
    private PeerDHT                peerDHT;


    public VersionArchiver() {
        this.pathOperations = PersistenceFactory.getPathPersistence();
        this.simpleDHTOperations = PersistenceFactory.getDhtOperations();
    }

    /**
     * {@inheritDoc}
     */
    public void archive(PeerDHT pPeerDht, Number160 pLocationKey, Data pOldFile)
            throws ClassNotFoundException, IOException, InterruptedException {

        this.peerDHT = pPeerDht;

        // Create paths from locationKey
        Map<String, String> extractedPaths = this.extractPaths(pLocationKey);

        // Check & create Version Folder
        if (!this.versionFolderExists(extractedPaths.get(this.VERSION_FOLDER_PATH))) {
            this.createVersionFolder(extractedPaths.get(this.VERSION_QUEUE_PATH), extractedPaths.get(this.VERSION_FOLDER_PATH));
        }

        // Save current file to version folder
        this.saveOldFileToVersionFolder(extractedPaths.get(this.VERSION_FOLDER_PATH), extractedPaths.get(this.FILE_NAME), extractedPaths.get(this.FILE_EXTENSION),
                extractedPaths.get(this.VERSION_QUEUE_PATH), pOldFile);

        // Make sure the version folder doesn't bloat.
        this.pruneVersionFolder(extractedPaths.get(this.VERSION_QUEUE_PATH));
    }

    /**
     * Extract paths for the given file of the location key
     * 
     * @param pLocationKey The location key of the file to archive
     * @return A map containing path segments for the following keys:
     *         <ul>
     *         <li><code>directory_path</code>: The directory path to the file (without the filename)</li>
     *         <li><code>file_name</code>: The file name</li>
     *         <li><code>file_extension</code>: The file extension</li>
     *         <li><code>version_folder_path</code>: The path to the version folder of the file</li>
     *         <li><code>version_queue_path</code>: The path to the version queue in the DHT of the file</li>
     *         </ul>
     * 
     * @throws IOException If the path of the file could not be fetched (i.e. the given location key was wrong or the path does not exist anymore)
     * @throws ClassNotFoundException If an error happened during getting the path of the file
     * @throws InterruptedException If an error happened during getting the path of the file
     */
    protected Map<String, String> extractPaths(Number160 pLocationKey)
            throws IOException, ClassNotFoundException, InterruptedException {

        String filePath = this.pathOperations.getPath(peerDHT, pLocationKey);

        if (filePath == null) {
            logger.warning("Could not retrieve filePath for versionFolder.");
            throw new IOException();
        }

        int slashIndex = filePath.lastIndexOf("/");
        int dotIndex = filePath.lastIndexOf(".");

        String directoryPath = filePath.substring(0, slashIndex + 1);
        String fileName = filePath.substring(slashIndex + 1);
        String fileExtension = filePath.substring(dotIndex + 1);
        String versionFolderPath = directoryPath.concat(".").concat((fileName).replace('.', '_'));
        String versionQueuePath = versionFolderPath.concat("/.versionQueue");

        Map<String, String> extractedPaths = new HashMap<>();
        extractedPaths.put(this.DIRECTORY_PATH, directoryPath);
        extractedPaths.put(this.FILE_NAME, fileName);
        extractedPaths.put(this.FILE_EXTENSION, fileExtension);
        extractedPaths.put(this.VERSION_FOLDER_PATH, versionFolderPath);
        extractedPaths.put(this.VERSION_QUEUE_PATH, versionQueuePath);

        return extractedPaths;
    }


    /**
     * Checks if the version folder already exists in the DHT
     * 
     * @param pVersionFolderPath The path of the version folder
     * @return True, if it exists, false otherwise
     * 
     * @throws IOException If an error happened during getting the path of the file
     * @throws ClassNotFoundException If an error happened during getting the path of the file
     * @throws InterruptedException If an error happened during getting the path of the file
     */
    protected boolean versionFolderExists(String pVersionFolderPath)
            throws ClassNotFoundException, InterruptedException, IOException {

        String retrievedVersionFolderPath = pathOperations.getPath(peerDHT, Number160.createHash(pVersionFolderPath));

        if (retrievedVersionFolderPath != null) {
            logger.info("Version folder does exist already on path '" + pVersionFolderPath + "'");
            return true;
        }

        logger.info("Version folder does not exist on path '" + pVersionFolderPath + "'");
        return false;
    }


    /**
     * Creates the version folder on the given path.
     * The version queue gets stored in the DHT on the path given
     * 
     * @param pVersionQueuePath The path to the version queue in the DHT
     * @param pVersionFolderPath The path of the version folder
     * 
     * @throws IOException If an error happened during getting the path of the file
     * @throws ClassNotFoundException If an error happened during getting the path of the file
     * @throws InterruptedException If an error happened during getting the path of the file
     */
    protected void createVersionFolder(String pVersionQueuePath, String pVersionFolderPath)
            throws InterruptedException, IOException, ClassNotFoundException {
        // Initialize version queue
        ArrayBlockingQueue<String> versionQueue = new ArrayBlockingQueue<String>(MAX_VERSIONS + 1);

        // Put version queue
        simpleDHTOperations.putData(peerDHT, Number160.createHash(pVersionQueuePath), new Data(versionQueue));
        pathOperations.putPath(peerDHT, Number160.createHash(pVersionFolderPath), new Data(pVersionFolderPath));

        logger.info("Added version folder on path '" + pVersionFolderPath + "' to the DHT");
    }


    /**
     * Saves the given file to the version folder
     * 
     * @param pVersionFolderPath The path to the version folder
     * @param pFilename The file name of the file to archive
     * @param pFileExtension The extension of the file to archive
     * @param pVersionQueuePath The path to the version queue in the DHT
     * @param pOldFile The data of the old file
     * 
     * @throws IOException If an error happened during getting the path of the file
     * @throws ClassNotFoundException If an error happened during getting the path of the file
     * @throws InterruptedException If an error happened during getting the path of the file
     */
    protected void saveOldFileToVersionFolder(String pVersionFolderPath, String pFilename, String pFileExtension, String pVersionQueuePath, Data pOldFile)
            throws InterruptedException, IOException, ClassNotFoundException {

        String currentVersion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"));
        String pathToArchive = pVersionFolderPath.concat("/").concat(pFilename.replace('.', '_')).concat("_").concat(currentVersion).concat(".").concat(pFileExtension);

        // Put data of old version
        simpleDHTOperations.putData(peerDHT, Number160.createHash(pathToArchive), pOldFile);
        pathOperations.putPath(peerDHT, Number160.createHash(pathToArchive), new Data(pathToArchive));

        // get version queue from version folder and add new version to queue
        @SuppressWarnings("unchecked")
        ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peerDHT, Number160.createHash(pVersionQueuePath)).object();
        versionQueue.put(pathToArchive);

        // put version queue back to version folder
        simpleDHTOperations.putData(peerDHT, Number160.createHash(pVersionQueuePath), new Data(versionQueue));
    }

    /**
     * Prunes the version folder to a maximum of <i>MAX_VERSIONS</i> versions
     * in the version folder
     * 
     * @param pVersionQueuePath The path to the version queue in the DHT
     * 
     * @throws IOException If an error happened during getting the path of the file
     * @throws ClassNotFoundException If an error happened during getting the path of the file
     * @throws InterruptedException If an error happened during getting the path of the file
     */
    protected void pruneVersionFolder(String pVersionQueuePath)
            throws InterruptedException, ClassNotFoundException, IOException {

        // get version queue from version folder
        @SuppressWarnings("unchecked")
        ArrayBlockingQueue<String> versionQueue = (ArrayBlockingQueue<String>) simpleDHTOperations.getData(peerDHT, Number160.createHash(pVersionQueuePath)).object();

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
        simpleDHTOperations.putData(peerDHT, Number160.createHash(pVersionQueuePath), new Data(versionQueue));
    }
}
