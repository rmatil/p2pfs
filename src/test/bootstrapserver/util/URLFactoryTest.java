package test.bootstrapserver.util;

import net.f4fs.bootstrapserver.util.URLFactory;
import net.f4fs.config.Config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URLFactoryTest {

    @Test
    public void testCreateBasicBootstrapServerBuilder() throws Exception {
        String url = Config.DEFAULT.getProtocol() + "://" + Config.DEFAULT.getHost() + "?token=" + Config.DEFAULT.getAuthToken();

        assertEquals("Even the basics don't work!", url, URLFactory.createBasicBootstrapServerBuilder().build());
    }

    @Test
    public void testCreateIPListURL() throws Exception {
        String url = Config.DEFAULT.getProtocol() + "://" + Config.DEFAULT.getHost() + "/" + Config.DEFAULT.getGETPath() + "?token=" + Config.DEFAULT.getAuthToken();

        assertEquals("get ip list url is wrong", url, URLFactory.createIPListURL());
    }

    @Test
    public void testCreateStoreURL() throws Exception {
        String url = Config.DEFAULT.getProtocol() + "://" + Config.DEFAULT.getHost() + "/" + Config.DEFAULT.getPOSTPath() + "?token=" + Config.DEFAULT.getAuthToken();

        assertEquals("get ip list url is wrong", url, URLFactory.createStoreURL());
    }

    @Test
    public void testCreateRemoveURL() throws Exception {
        String url = Config.DEFAULT.getProtocol() + "://" + Config.DEFAULT.getHost() + "/" + Config.DEFAULT.getREMOVEPath() + "?token=" + Config.DEFAULT.getAuthToken();

        assertEquals("get ip list url is wrong", url, URLFactory.createRemoveURL());
    }

    @Test
    public void testCreateKeepAliveURL() throws Exception {
        String url = Config.DEFAULT.getProtocol() + "://" + Config.DEFAULT.getHost() + "/" + Config.DEFAULT.getKeepAlivePath() + "?token=" + Config.DEFAULT.getAuthToken();

        assertEquals("get ip list url is wrong", url, URLFactory.createKeepAliveURL());
    }
}