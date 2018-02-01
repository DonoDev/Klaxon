package dono.dev.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import dono.dev.klaxon.MainActivity;
import dono.dev.klaxon.R;
import dono.dev.model.InitPlayerObjectAdapter;
import dono.dev.model.OpenGame;
import dono.dev.utils.JsonObjectKeysPair;
import dono.dev.utils.KlaxonUtils;

/**
 * TODO update url strings
 * TODO make one request method
 * TODO implement callbacks
 * TODO move ui code out of httpmanager
 * @author EricDonovan
 *
 */
public class HttpManager {

    private static final String TAG = "HttpManager";

    public static RequestQueue queue;

    static String authToken = "";

    public static void initialize(Context context){
        queue = Volley.newRequestQueue(context);
    }

    public static StringRequest createLoginPostRequest(Context context) {
        StringRequest stringRequest = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final String alias    = prefs.getString(getResString(R.string.username), getResString(R.string.defaultValue));
        final String password = prefs.getString(getResString(R.string.password), getResString(R.string.defaultValue));

        stringRequest = new StringRequest(Request.Method.POST, getResString(R.string.loginUrlRequest), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=utf-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "login");
                params.put("alias", alias);
                params.put("password", password);
                return params;
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);

                    final StringBuilder builder = new StringBuilder();
                    for(Entry<String,String> header : response.headers.entrySet()){
                        builder.append("KEY: ").append(header.getKey()).append(" VALUE: ").append(header.getValue()).append("\n");
                        if(header.getKey().equals(getResString(R.string.setCookie))){
                            authToken = header.getValue();
                        }
                    }
                    Log.i(TAG, "Response header: " + builder.toString());
                    try {
                        String str = new String(response.data, "UTF-8");
                        Log.d(TAG, "Response data: " + str);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Error encoding string: " + e.toString());
                    }
                    MainActivity.mainActivity.runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            StringRequest stringRequest = createInitPlayerStringRequest(MainActivity.mainActivity);
                            if(stringRequest != null)
                                addStringRequestToQueue(stringRequest);
                        }
                    });
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        return stringRequest;
    }

    public static StringRequest createInitPlayerStringRequest(Context context){

        StringRequest stringRequest = null;

        if(authToken.equals("") || authToken == null)
            Log.e(TAG, "Do not have a valid auth token");

        stringRequest = new StringRequest(Request.Method.POST, getResString(R.string.initUrlRequest), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=utf-8";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                    for(Entry<String,String> header : response.headers.entrySet()){
                        Log.i(TAG, "Response header: " + header);
                    }
                    try {
                        final String jsonString = new String(response.data, "UTF-8");
                        JSONArray jsonArray = new JSONArray(jsonString);
                        MainActivity.initPlayerObjectAdapter = new InitPlayerObjectAdapter(jsonArray);
                        MainActivity.mainActivity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                MainActivity.setAdapter();
                            }
                        });
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Error encoding string: " + e.toString());
                    } catch (JSONException e) {
                        Log.e(TAG, "Error Converting to JSON: " + e);
                    }
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "init_player");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("cookie", authToken);
                return headers;
            }
        };
        return stringRequest;
    }

    public static StringRequest createOrderStringRequest(Context context, final OpenGame openGame){

        StringRequest stringRequest = null;

        if(authToken.equals("") || authToken == null)
            Log.e(TAG, "Do not have a valid auth token");

        stringRequest = new StringRequest(Request.Method.POST, getResString(R.string.orderRequest), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=utf-8";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                    for(Entry<String,String> header : response.headers.entrySet()){
                        Log.i(TAG, "Response header: " + header);
                    }
                    try {
                        final String jsonString = new String(response.data, "UTF-8");
                        Log.d(TAG, jsonString);
                        parseFullReport(jsonString);

                        //openGame.createFullReport(jsonString);

                        MainActivity.mainActivity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                MainActivity.mainActivity.displayGameInfo(openGame, jsonString);
                            }
                        });
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Error encoding string: " + e.toString());
                    }
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "order");
                params.put("order", "full_universe_report");
                params.put("game_number", openGame.getNumber());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("cookie", authToken);
                return headers;
            }
        };
        return stringRequest;
    }

    public static void addStringRequestToQueue(StringRequest request){
        queue.add(request);
    }

    public static void addJsonObjectRequestToQueue(JsonObjectRequest request){
        queue.add(request);
    }

    private static String getResString(int id){
        return MainActivity.mainActivity.getResources().getString(id);
    }

    private static void parseFullReport(String jsonString){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JsonObjectKeysPair objectKeys = KlaxonUtils.parseJSONObject(jsonObject);
            //grab the report of out the response
            JSONObject reportJsonObject = (JSONObject) objectKeys.getObject().get("report");
            JsonObjectKeysPair reportObjectKeys = KlaxonUtils.parseJSONObject(reportJsonObject);
            //grab the fleets, stars, players
            JSONObject fleetsJsonObject = (JSONObject) reportObjectKeys.getObject().get("fleets");
            JsonObjectKeysPair fleetsObjectKeys = KlaxonUtils.parseJSONObject(fleetsJsonObject);
            for(String key : fleetsObjectKeys.getKeys()){
                Log.d(TAG, "Fleet Key: " + key);
                Log.d(TAG, "..." + fleetsObjectKeys.getObject().get(key).toString());
            }
            
            JSONObject starsJsonObject = (JSONObject) reportObjectKeys.getObject().get("stars");
            JsonObjectKeysPair starsObjectKeys = KlaxonUtils.parseJSONObject(starsJsonObject);
            for(String key : starsObjectKeys.getKeys()){
                Log.d(TAG, "Stars Key: " + key);
                Log.d(TAG, "..." + starsObjectKeys.getObject().get(key).toString());
            }
            
            JSONObject playersJsonObject = (JSONObject) reportObjectKeys.getObject().get("players");
            JsonObjectKeysPair playersObjectKeys = KlaxonUtils.parseJSONObject(playersJsonObject);
            for(String key : playersObjectKeys.getKeys()){
                Log.d(TAG, "Players Key: " + key);
                Log.d(TAG, "..." + playersObjectKeys.getObject().get(key).toString());
            }

            for(String key : objectKeys.getKeys()){
                Log.d(TAG, "Key: " + key);
                Log.d(TAG, "..." + objectKeys.getObject().get(key).toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception: " + e.toString());
        }
    }
}
