package net.tomp2p.bootstrapserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class BoostrapServerAccess {

    public static final String BOOTSTRAP_SERVER = "http://188.226.178.35";
    public static final String TOKEN            = "?token=tabequals4";
    public static final String GET_URL          = "/ip-addresses";
    public static final String POST_URL         = "/ip-addresses/new";
    public static final String REMOVE_URL       = "/ip-addresses/remove";

    public String get() {
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

            System.out.println("GET: " + connection.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public StringBuffer post(String pAddress, int pPort) {
        String postURL = BOOTSTRAP_SERVER + POST_URL + TOKEN;
        StringBuffer result = new StringBuffer();

        URL stringURL;
        HttpURLConnection connection;

        String content = "address=" + pAddress + "&port=" + pPort;
        try {

            stringURL = new URL(postURL);
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

            System.out.println("POST: " + connection.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    
    public StringBuffer remove(String pAddress, int pPort){
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

            System.out.println("REMOVE: " + connection.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
