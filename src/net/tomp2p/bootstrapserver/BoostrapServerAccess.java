package net.tomp2p.bootstrapserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class BoostrapServerAccess {
    
    String mBase_URL;
    
    public BoostrapServerAccess(String pBase_URL){
        mBase_URL = pBase_URL;
    }
    
    
    public String get(){
        String stringURL = mBase_URL + "/ip-addresses?token=tabequals4";
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
        
    public StringBuffer post(String pKey, String pValue){
        String postURL = mBase_URL + "/ip-addresses/new?token=tabequals4";
        StringBuffer result = new StringBuffer();
        
        URL stringURL;
        HttpURLConnection connection;
        
        String content = pKey + "=" + pValue;
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

}