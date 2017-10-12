package dono.dev.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class KlaxonUtils {

    private static final String TAG = "KlaxonUtils";

    public static Collection<JsonObjectKeysPair> parseJSONArray(JSONArray jsonArray){
        Collection<JsonObjectKeysPair> objectkeys = new ArrayList<JsonObjectKeysPair>();
        try {
            Log.d(TAG, "Array Length: " + jsonArray.length());
            for(int i = 0; i < jsonArray.length(); i++){
                if(jsonArray.get(i) instanceof String){
                    Log.d(TAG, "JSONString: " + jsonArray.get(i));
                }
                else if(jsonArray.get(i) instanceof JSONObject) {
                    JSONObject jObject = (JSONObject) jsonArray.get(i);
                    Log.d(TAG, "JSONObject: " + jObject.toString());
                    objectkeys.add(parseJSONObject(jObject));
                } 
                else if(jsonArray.get(i) instanceof JSONArray) {
                    JSONArray jArray = (JSONArray) jsonArray.get(i);
                    Log.d(TAG, "JSONArray: " + jArray.toString());
                    parseJSONArray(jArray);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e);
        }
        return objectkeys;
    }

    public static JsonObjectKeysPair parseJSONObject(JSONObject jsonObject){
        Collection<String> keyList = new ArrayList<String>();
        try {
            Log.d(TAG, "Object Length: " + jsonObject.length());
            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()){
                String key = keys.next();
                Log.d(TAG, "Key: " + key + "   Value: " + jsonObject.get(key).toString());
                keyList.add(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e);
        }
        return new JsonObjectKeysPair(jsonObject, keyList);
    }
}
