package net.f4fs.filesystem;

import net.f4fs.filesystem.MemoryDirectory;
import net.fusejna.StructStat.StatWrapper;


public abstract class AMemoryPath {

    private String name;
    private MemoryDirectory parent;

    public AMemoryPath(final String name)
    {
        this(name, null);
    }

    public AMemoryPath(final String name, final MemoryDirectory parent)
    {
        this.name = name;
        this.parent = parent;
    }

    public synchronized void delete()
    {
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
        }
    }

    protected AMemoryPath find(String path)
    {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.equals(name) || path.isEmpty()) {
            return this;
        }
        return null;
    }

    protected abstract void getattr(StatWrapper stat);

    public void rename(String newName)
    {
        while (newName.startsWith("/")) {
            newName = newName.substring(1);
        }
        name = newName;
    }
    
    
    public String getName() {
        return name;
    }
    
    
    public void setName(String pName) {
        name = pName;
    }
    
    
    public MemoryDirectory getParent() {
        return parent;
    }
    
    
    public void setParent(MemoryDirectory pParent) {
        parent = pParent;
    }
}
