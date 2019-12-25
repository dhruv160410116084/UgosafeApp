package droidad.contract.carrental.ServerOperations;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ServerOperationFunctions {


    public JSONObject getDataFromServer(JSONObject jsonObject, String link){

        JSONObject outputJSONObject = new JSONObject();

        Iterator<String> iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            try {
                Object value = jsonObject.get(key);
                System.out.println("Key: "+key+" Value: "+value.toString());

                outputJSONObject.put(key, value);

            } catch (JSONException e) {
                // Something went wrong!
            }
        }
        return outputJSONObject;
    }


}
