package net.tomp2p.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class MyJSONParser {

   
    public static List<Map<String, String>> parse(String pValue){
       
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(pValue);
            JSONObject jsonObject = (JSONObject) obj;
            
           JSONArray addresses = (JSONArray) jsonObject.get("addresses");
           for(int i = 0; i< addresses.size(); i++){
               Map<String, String> map = new HashMap<String, String>();
               JSONObject address = (JSONObject) addresses.get(i);
               map.put("address", (String) address.get("address")); 
               map.put("port", (String) address.get("port")); 
               
               result.add(map);
           }
           
        } catch (ParseException pEx) {
            
            pEx.printStackTrace();
        }

        
        return result;
    }
}
