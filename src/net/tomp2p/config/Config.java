package net.tomp2p.config;

/**
 * Main configuration file. Adapt if necessary.
 *
 * Created by samuel on 31.03.15.
 */
public enum Config {
    DEFAULT(4000);

    private int _port;

    private Config(int port) {
        _port  = port;
    }

    public int getPort() { return _port; }
}

