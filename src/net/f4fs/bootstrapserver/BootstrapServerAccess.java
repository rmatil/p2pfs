package net.f4fs.bootstrapserver;

import net.f4fs.bootstrapserver.util.URLFactory;
import net.f4fs.util.KeepAlive;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Handles IP address storing / removal on the bootstrap server
 * 
 * @author Reto
 *
 */
public class BootstrapServerAccess {
    public KeepAlive keepAlive;

    public BootstrapServerAccess() {
        keepAlive = new KeepAlive();
    }

    /**
     * Returns a string containing the JSON representation
     * of IP-port address pairs of all connected peers
     * 
     * @return The JSON string
     */
    public String getIpAddressList() {
        String stringURL = URLFactory.createIPListURL();
        String result = "";

        URL getURL;
        HttpURLConnection connection;
        BufferedReader bufferedReader;
        String line;

        try {
            getURL = new URL(stringURL);
            connection = (HttpURLConnection) getURL.openConnection();
            connection.setRequestMethod("GET");

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            bufferedReader.close();

            System.out.println("[GET][" + connection.getResponseCode() + "]:  " + stringURL);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Stores the given IP address and the given port on
     * the bootstrap server
     * 
     * @param pAddress The IP address
     * @param pPort The corresponding port
     * 
     * @return The updated list of connected peers
     */
    public StringBuffer postIpPortPair(String pAddress, int pPort) {
        String postURL = URLFactory.createStoreURL();
        StringBuffer result = new StringBuffer();

        URL getURL;
        HttpURLConnection connection;

        String content = "address=" + pAddress + "&port=" + pPort;
        try {

            getURL = new URL(postURL);
            connection = (HttpURLConnection) getURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(content);
            dataOutputStream.flush();
            dataOutputStream.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = bufferedReader.readLine()) != null) {
                result.append(inputLine);
            }

            bufferedReader.close();

            System.out.println("[POST][" + connection.getResponseCode() + "]: " + postURL);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Removes the specified IP-port pair from the bootstrap server
     * 
     * @param pAddress The IP address to remove
     * @param pPort The corresponding port
     * 
     * @return The updated list of connected peers
     */
    public StringBuffer removeIpPortPair(String pAddress, int pPort) {
        String removeURL = URLFactory.createRemoveURL();
        StringBuffer result = new StringBuffer();

        URL stringURL;
        HttpURLConnection connection;

        String content = "address=" + pAddress + "&port=" + pPort;

        try {
            stringURL = new URL(removeURL);
            connection = (HttpURLConnection) stringURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(content);
            dataOutputStream.flush();
            dataOutputStream.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = bufferedReader.readLine()) != null) {
                result.append(inputLine);
            }

            bufferedReader.close();

            System.out.println("[POST][" + connection.getResponseCode() + "]: " + removeURL);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void sendSingleKeepAliveMessage() {
        keepAlive.sendMsg();
    }

    public void heartBeat() {
        keepAlive.exec();
    }

    public void feelTheRhythmFeelTheRhyme_ItBobsledTime() {
        heartBeat();
    }

}
