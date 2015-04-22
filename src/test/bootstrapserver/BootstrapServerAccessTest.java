package test.bootstrapserver;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.f4fs.bootstrapserver.BootstrapServerAccess;
import net.f4fs.util.IpAddressJsonParser;

import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class BootstrapServerAccessTest {

    private static final String          TEST_IP   = "123.456.789.0";
    private static final String          TEST_PORT = "9999";

    private static BootstrapServerAccess serverAccess;

    @BeforeClass
    public static void initTest() {
        serverAccess = new BootstrapServerAccess();
        serverAccess.postIpPortPair(TEST_IP, Integer.parseInt(TEST_PORT));
    }

    @AfterClass
    public static void tearDown() {
        serverAccess.removeIpPortPair(TEST_IP, Integer.parseInt(TEST_PORT));
    }

    @Test
    public void getIpAddressListTest()
            throws ParseException {
        String json = serverAccess.getIpAddressList();
        List<Map<String, String>> ipList = IpAddressJsonParser.parse(json);

        Map<String, String> expectedResult = this.getExpectedResult();

        assertThat("Test ip port pair is not stored on the server", ipList, anyOf(contains(expectedResult)));
    }

    @Test
    public void postIpPortPairTest()
            throws ParseException {
        serverAccess.removeIpPortPair(TEST_IP, Integer.parseInt(TEST_PORT));

        serverAccess.postIpPortPair(TEST_IP, Integer.parseInt(TEST_PORT));
        String json = serverAccess.getIpAddressList();
        List<Map<String, String>> ipList = IpAddressJsonParser.parse(json);

        Map<String, String> expectedResult = this.getExpectedResult();

        assertThat("Test failed to retrieve stored ip address port pair", ipList, anyOf(contains(expectedResult)));
    }

    @Test
    public void removeIpPortPairTest()
            throws ParseException {
        serverAccess.postIpPortPair(TEST_IP, Integer.parseInt(TEST_PORT));

        serverAccess.removeIpPortPair(TEST_IP, Integer.parseInt(TEST_PORT));

        String json = serverAccess.getIpAddressList();
        List<Map<String, String>> ipList = IpAddressJsonParser.parse(json);

        Map<String, String> notExpectedResult = this.getExpectedResult();

        assertThat("Test DID contain the ip port pair", ipList, not(contains(notExpectedResult)));
    }

    protected Map<String, String> getExpectedResult() {
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("address", TEST_IP);
        expectedResult.put("port", TEST_PORT);

        return expectedResult;
    }
}
