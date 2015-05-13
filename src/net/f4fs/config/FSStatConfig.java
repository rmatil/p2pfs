package net.f4fs.config;

/**
 * Configuration for the filesystem
 * 
 * @author Raphael
 */
public enum FSStatConfig {
    DEFAULT(4000L, 1000L, 200L, 180L, 5L, 29L),
    BIGGER(4000L, 100000L, 98000L, 90000L, 10L, 58L),
    RESIZEINIT(4000L, 10000L, 9800L, 9000L, 5L, 29L),
    RESIZE(4000L, 10000L, 9800L, 9000L, 5L, 29L);

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
     * Set initial FS size if dynamic resizing is enabled.
     * 
     * @param fsSize number of connected peers to the DHT
     */
    public static void initialFsSize(int fsSize){
        resize(fsSize);
    }
    
    /**
     * Resize the FS if dynamic resizing is enabled.
     * 
     * @param fsSize the number of connected peers to the DHT
     */
    public static void resize(int fsSize){
        RESIZE.setBlocks(fsSize * RESIZEINIT.getBlocks());
        RESIZE.setBfree(fsSize * RESIZEINIT.getBfree());
        RESIZE.setBavail(fsSize * RESIZEINIT.getBavail());
    }
    
    /**
     * @return Optimal transfer block size
     */
    public long getBsize() {
        return _bsize;
    }
    
    
    /**
     * @param bsize
     */
    public void setBsize(long bsize) {
        this._bsize = bsize;
    }

    
    /**
     * @return Total data blocks in the the file system
     */
    public long getBlocks() {
        return _blocks;
    }
    
    /** 
     * @param blocks
     */
    public void setBlocks(long blocks) {
        this._blocks = blocks;
    }
    
    /**
     * @return Total free blocks on the file system
     */
    public long getBfree() {
        return _bfree;
    }
    
    /**
     * @param bfree
     */
    public void setBfree(long bfree) {
        this._bfree = bfree;
    }

    
    /**
     * @return Free blocks available for non-superuser
     */
    public long getBavail() {
        return _bavail;
    }
    
    /** 
     * @param bavail
     */
    public void setBavail(long bavail) {
        this._bavail = bavail;
    }

    /**
     * @return Total file nodes in the file system
     */
    public long getFiles() {
        return _files;
    }
    
    /**
     * @param files
     */
    public void setFiles(long files) {
        this._files = files;
    }
    
    /**
     * @return Free file nodes in the file system
     */
    public long getFfree() {
        return _ffree;
    }
    
    /**
     * @param ffree
     */
    public void setFfree(long ffree) {
        this._ffree = ffree;
    }

}
