package net.f4fs.util;

import net.f4fs.bootstrapserver.util.URLBuilder;
import net.f4fs.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Util class to send a keep-alive message to a certain node in the network.
 */
public class KeepAlive {

    private final int                _period;
    private final TimeUnit           _period_t;
    private ScheduledExecutorService _scheduler;
    private String                   _targetIp;

    private String                   _peerIp;
    private int                      _peerPort;

    private final Logger             _logger;

    public KeepAlive(int period, TimeUnit period_t, String targetIp, String peerIp, int peerPort) {
        _period = period;
        _period_t = period_t;
        _targetIp = targetIp;
        _peerIp = peerIp;
        _peerPort = peerPort;

        _logger = LoggerFactory.getLogger(this.getClass());

        _scheduler = Executors.newScheduledThreadPool(1); // Only one thread is needed. Maybe a single executor for the hole app?
    }

    /**
     * Uses the default values to connect to the bootstrap server.
     */
    public KeepAlive() {
        this(Config.DEFAULT.getKeepAliveMsgPeriod(), Config.DEFAULT.getKeepAliveMsgPeriodType(), Config.DEFAULT.getBootstrapServer(), null, Config.DEFAULT.getPort());
    }

    public KeepAlive setIp(String ip) {
        this._peerIp = ip;
        return this;
    }

    public KeepAlive setPort(int port) {
        this._peerPort = port;
        return this;
    }

    /**
     * Sends a single message to the defined target.
     */
    public void sendMsg() {
        String url = new URLBuilder.Builder()
                .protocol("HTTP")
                .host(_targetIp)
                .appendAuthToken()
                .path(Config.DEFAULT.getKeepAlivePath())
                .build();

        URL postUrl;
        HttpURLConnection connection;

        try {
            postUrl = new URL(url);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes("address=" + _peerIp + "&port=" + _peerPort);
            dataOutputStream.flush();
            dataOutputStream.close();

            _logger.info("[" + connection.getRequestMethod() + "][" + connection.getResponseCode() + "]:  " + url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exec() {
        _scheduler.scheduleAtFixedRate(() -> sendMsg(), 0, _period, _period_t);
    }
}
