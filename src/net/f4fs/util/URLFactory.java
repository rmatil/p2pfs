package net.f4fs.util;

import net.f4fs.config.Config;

/**
 * Created by samuel on 31.03.15.
 */
public class URLFactory {

    public static URLBuilder.Builder createBasicBootstrapServerBuilder() {
        return new URLBuilder.Builder(Config.DEFAULT);
    }

    public static String createIPListURL() {
        return createBasicBootstrapServerBuilder().path(Config.DEFAULT.getGETPath()).build();
    }

    public static String createStoreURL() {
        return createBasicBootstrapServerBuilder().path(Config.DEFAULT.getPOSTPath()).build();
    }

    public static String createRemoveURL() {
        return createBasicBootstrapServerBuilder().path(Config.DEFAULT.getREMOVEPath()).build();
    }

    public static String createKeepAliveURL() {
        return createBasicBootstrapServerBuilder().path(Config.DEFAULT.getKeepAlivePath()).build();
    }
}
