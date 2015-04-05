package net.f4fs.filesystem;

import java.util.ArrayList;
import java.util.List;

import net.fusejna.DirectoryFiller;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;


public class MemoryDirectory extends AMemoryPath{

    private final List<AMemoryPath> contents = new ArrayList<AMemoryPath>();

    public MemoryDirectory(final String name)
    {
        super(name);
    }

    public MemoryDirectory(final String name, final MemoryDirectory parent)
    {
        super(name, parent);
    }

    public synchronized void add(final AMemoryPath p)
    {
        contents.add(p);
        p.setParent(this);
    }

    public synchronized void deleteChild(final AMemoryPath child)
    {
        contents.remove(child);
    }

    @Override
    protected AMemoryPath find(String path)
    {
        if (super.find(path) != null) {
            return super.find(path);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        synchronized (this) {
            if (!path.contains("/")) {
                for (final AMemoryPath p : contents) {
                    if (p.getName().equals(path)) {
                        return p;
                    }
                }
                return null;
            }
            final String nextName = path.substring(0, path.indexOf("/"));
            final String rest = path.substring(path.indexOf("/"));
            for (final AMemoryPath p : contents) {
                if (p.getName().equals(nextName)) {
                    return p.find(rest);
                }
            }
        }
        return null;
    }

    @Override
    protected void getattr(final StatWrapper stat)
    {
        stat.setMode(NodeType.DIRECTORY);
    }

    public synchronized void mkdir(final String lastComponent)
    {
        contents.add(new MemoryDirectory(lastComponent, this));
    }

    public synchronized void mkfile(final String lastComponent)
    {
        contents.add(new MemoryFile(lastComponent, this));
    }

    public synchronized void read(final DirectoryFiller filler)
    {
        for (final AMemoryPath p : contents) {
            filler.add(p.getName());
        }
    }

}
