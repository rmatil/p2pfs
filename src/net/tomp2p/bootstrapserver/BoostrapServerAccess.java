package net.tomp2p.bootstrapserver;

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
public class BoostrapServerAccess {

    /**
     * The url to the bootstrap server
     */
    public static final String BOOTSTRAP_SERVER = "http://188.226.178.35";

    /**
     * The authentication token to access any
     * operation on the bootstrap server
     */
    public static final String TOKEN            = "?token=tabequals4";

    /**
     * The URL to get a list of all IP addresses
     * currently connected with the P2P network
     */
    public static final String GET_URL          = "/ip-addresses";

    /**
     * The URL to which new addresses can be posted
     */
    public static final String POST_URL         = "/ip-addresses/new";

    /**
     * The URL on which the removal of
     * IP addresses are handled
     */
    public static final String REMOVE_URL       = "/ip-addresses/remove";

    /**
     * Returns a string containing the JSON representation
     * of IP-port address pairs of all connected peers
     * 
     * @return The JSON string
     */
    public String getIpAddressList() {
        String stringURL = BOOTSTRAP_SERVER + GET_URL + TOKEN;
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
        String postURL = BOOTSTRAP_SERVER + POST_URL + TOKEN;
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
        String removeURL = BOOTSTRAP_SERVER + REMOVE_URL + TOKEN;
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

}
