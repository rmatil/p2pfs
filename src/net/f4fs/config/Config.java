package net.f4fs.config;

import java.util.concurrent.TimeUnit;


/**
 * Main configuration file. Adapt if necessary.
 */
public enum Config {
    DEFAULT("http", "188.226.178.35", 4000, "tabequals4", "ip-addresses", "ip-addresses/new", "ip-addresses/remove", "keepalive", 42, TimeUnit.SECONDS, "./P2PFS", false, "keys", 4096),
    CLI("http", "188.226.178.35", 4000, "tabequals4", "ip-addresses", "ip-addresses/new", "ip-addresses/remove", "keepalive", 42, TimeUnit.SECONDS, "./P2PFS", true, "keys", 4096);
   


    private String   _protocol;

    /**
     * The url to the bootstrap server
     */
    private String   _host;

    private int      _port;

    /**
     * The authentication token to access any
     * operation on the bootstrap server
     */
    private String   _authToken;

    /**
     * The URL to get a list of all IP addresses
     * currently connected with the P2P network
     */
    private String   _getPath;

    /**
     * The URL to which new addresses can be posted
     */
    private String   _postPath;

    /**
     * The URL on which the removal of
     * IP addresses are handled
     */
    private String   _removePath;

    private String   _keepAlivePath;

    /**
     * Period in between the keep alive messages
     */
    private int      _keepAliveMsgPeriod;

    private TimeUnit _keepAliveMsgPeriod_T;

    /**
     * Default mount point for the filesystem
     */
    private String   _mountPoint;

    /**
     * Indicates whether the cli should be started or not
     */
    private boolean  _startCommandLineInterface;

    /**
     * Key in the DHT to get al list of all paths
     */
    private String   _masterLocationPathsKey;

    /**
     * Size of chunks in bytes.
     */
    private int _chunkSizeBytes;

    Config(String protocol, String host, int port, String authToken, String getPath, String postPath, String removePath, String keepAlivePath, int keepAliveMsgPeriod,
            TimeUnit keepAliveMsgPeriod_T, String mountPoint, boolean startCommandLineInterface, String masterLocationPathsKey, int chunkSizeBytes) {
        _protocol = protocol;
        _port = port;
        _keepAliveMsgPeriod = keepAliveMsgPeriod;
        _keepAliveMsgPeriod_T = keepAliveMsgPeriod_T;
        _host = host;
        _authToken = authToken;
        _getPath = getPath;
        _postPath = postPath;
        _removePath = removePath;
        _keepAlivePath = keepAlivePath;
        _mountPoint = mountPoint;
        _startCommandLineInterface = startCommandLineInterface;
        _masterLocationPathsKey = masterLocationPathsKey;
        _chunkSizeBytes = chunkSizeBytes;
    }

    public String getProtocol() {
        return _protocol;
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    public int getKeepAliveMsgPeriod() {
        return _keepAliveMsgPeriod;
    }

    public TimeUnit getKeepAliveMsgPeriodType() {
        return _keepAliveMsgPeriod_T;
    }

    public String getBootstrapServer() {
        return _host;
    }

    public String getAuthToken() {
        return _authToken;
    }

    public String getGETPath() {
        return _getPath;
    }

    public String getPOSTPath() {
        return _postPath;
    }

    public String getREMOVEPath() {
        return _removePath;
    }

    public String getKeepAlivePath() {
        return _keepAlivePath;
    }

    public String getMountPoint() {
        return _mountPoint;
    }

    public boolean getStartCommandLineInterface() {
        return _startCommandLineInterface;
    }

    public String getMasterLocationPathsKey() {
        return _masterLocationPathsKey;
    }

    public int getChunkSizeBytes() { return _chunkSizeBytes; }
}
