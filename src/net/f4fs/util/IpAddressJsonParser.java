package net.f4fs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Responsible to convert JSON from Bootstrap Server
 * @author Reto
 *
 */
public class IpAddressJsonParser {

    /**
     * Parses the given JSON string to a List<Map<String, String>>.
     * <b>Note</b>: The given string must represent a JSON object containing 
     * an array of objects with keys <i>address</i> and <i>port</i> located
     * on the key <i>addresses</i>.
     * E.g. 
     * <pre>
     * {
     *   "addresses": [
     *      {
     *        "address": "102.12.12.12",
     *        "port": 4000
     *      }
     *   ]
     * }
     * </pre>
     * 
     * @param pJsonString JSON String to convert
     * @return A list of maps representing the values stored in key "addresses"
     * 
     * @throws ParseException
     */
    public static List<Map<String, String>> parse(String pJsonString)
            throws ParseException {
       
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        JSONParser parser = new JSONParser();
        
        Object obj = parser.parse(pJsonString);
        JSONObject jsonObject = (JSONObject) obj;

        JSONArray addresses = (JSONArray) jsonObject.get("addresses");
        for(int i = 0; i< addresses.size(); i++){
            Map<String, String> map = new HashMap<String, String>();
            JSONObject address = (JSONObject) addresses.get(i);
            map.put("address", (String) address.get("address")); 
            map.put("port", (String) address.get("port")); 

            result.add(map);
        }
        
        return result;
    }
}
