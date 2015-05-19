package net.f4fs.util;

import net.f4fs.bootstrapserver.util.URLBuilder;
import net.f4fs.config.Config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Util class to send a keep-alive message to a certain node in the network.
 * <p>
 * Created by samuel on 31.03.15.
 */
public class KeepAlive {

    private final int                _period;
    private final TimeUnit           _period_t;
    private ScheduledExecutorService _scheduler;
    private String                   _targetIp;
    private int                      _targetPort;

    public KeepAlive(int period, TimeUnit period_t, String targetIp, int targetPort) {
        _period = period;
        _period_t = period_t;
        _targetIp = targetIp;
        _targetPort = targetPort;

        _scheduler = Executors.newScheduledThreadPool(1); // Only one thread is needed. Maybe a single executor for the hole app?
    }

    /**
     * Uses the default values defined in Config.DEFAULT for the period and the period type.
     *
     * @param target
     * @param port
     */
    public KeepAlive(String target, int port) {
        this(Config.DEFAULT.getKeepAliveMsgPeriod(), Config.DEFAULT.getKeepAliveMsgPeriodType(), target, port);
    }

    /**
     * Uses the default values to connect to the bootstrap server.
     */
    public KeepAlive() {
        this(Config.DEFAULT.getBootstrapServer(), Config.DEFAULT.getPort());
    }

    /**
     * Sends a single message to the defined target.
     *
     * @param target The targets host. Path is given by the system structure.
     */
    public void sendMsg() {
        String url = new URLBuilder.Builder()
                .protocol("HTTP")
                .host(_targetIp)
                .appendAuthToken()
                .path(Config.DEFAULT.getKeepAlivePath())
                .build();

        URL getURL;
        HttpURLConnection connection;

        try {
            getURL = new URL(url);
            connection = (HttpURLConnection) getURL.openConnection();
            connection.setRequestMethod("GET");

            System.out.println("[GET][" + connection.getResponseCode() + "]:  " + url);
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
