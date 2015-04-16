package net.f4fs.config;

/**
 * Configuration for the filesystem
 * 
 * @author Raphael
 */
public enum FSStatConfig {
    DEFAULT(4000L, 1000L, 200L, 180L, 5L, 29L);

    /**
     * Optimal transfer block size
     */
    private long _bsize;

    /**
     * Total data blocks in the the file system
     */
    private long _blocks;

    /**
     * Total free blocks on the file system
     */
    private long _bfree;

    /**
     * Free blocks available for non-superuser
     */
    private long _bavail;

    /**
     * Total file nodes in the file system
     */
    private long _files;

    /**
     * Free file nodes in the file system
     */
    private long _ffree;


    /**
     * Configures the file system with the provided parameters
     * 
     * @param bsize Optimal transfer block size
     * @param blocks Total data blocks in the the file system
     * @param bfree Total free blocks on the file system
     * @param bavail Free blocks available for non-superuser
     * @param files Total file nodes in the file system
     * @param ffree Free file nodes in the file system
     */
    FSStatConfig(long bsize, long blocks, long bfree, long bavail, long files, long ffree) {
        _bsize = bsize;
        _blocks = blocks;
        _bfree = bfree;
        _bavail = bavail;
        _files = files;
        _ffree = ffree;
    }


    
    /**
     * @return Optimal transfer block size
     */
    public long getBsize() {
        return _bsize;
    }

    
    /**
     * @return Total data blocks in the the file system
     */
    public long getBlocks() {
        return _blocks;
    }


    
    /**
     * @return Total free blocks on the file system
     */
    public long getBfree() {
        return _bfree;
    }

    
    /**
     * @return Free blocks available for non-superuser
     */
    public long getBavail() {
        return _bavail;
    }

    /**
     * @return Total file nodes in the file system
     */
    public long getFiles() {
        return _files;
    }
    
    /**
     * @return Free file nodes in the file system
     */
    public long getFfree() {
        return _ffree;
    }

}
