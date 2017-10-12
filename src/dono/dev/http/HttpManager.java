package dono.dev.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.Request.Method;
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

/**
 * TODO update url strings
 * TODO make one request method
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

        Log.d(TAG, "Alias: " + alias);
        Log.d(TAG, "Password: " + password);
        Log.d(TAG, getResString(R.string.loginUrl));

        stringRequest = new StringRequest(Request.Method.POST, getResString(R.string.loginUrl), new Response.Listener<String>() {
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
                            MainActivity.resultsView.setText(builder.toString());
                        }
                    });
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        return stringRequest;
    }

    /**
     * TODO unused testing this out
     * @param context
     * @return
     */
    public static JsonObjectRequest createInitPlayerJsonRequest(Context context){

        JsonObjectRequest jsonRequest = null;

        Log.d(TAG, "Auth Token: " + authToken);
        Log.d(TAG, getResString(R.string.initUrl));

        if(authToken.equals("") || authToken == null)
            Log.e(TAG, "Do not have a valid auth token");

        jsonRequest = new JsonObjectRequest(Method.POST, getResString(R.string.initUrl), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "RESPONSE: " + response.toString());
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("cookie", authToken);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "init_player");
                return params;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try{
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
//                String responseString = "";
//                if (response != null) {
//                    responseString = String.valueOf(response.statusCode);
//                    // can get more details such as response.headers
//                    for(Entry<String,String> header : response.headers.entrySet()){
//                        Log.i(TAG, "Response header: " + header);
//                    }
//                    try {
//                        String str = new String(response.data, "UTF-8");
//                        Log.d(TAG, "Response data: " + str);
//                    } catch (UnsupportedEncodingException e) {
//                        Log.e(TAG, "Error encoding string: " + e.toString());
//                    }
//                }
//                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        return jsonRequest;
    }

    public static StringRequest createInitPlayerStringRequest(Context context){

        StringRequest stringRequest = null;

        Log.d(TAG, "Auth Token: " + authToken);
        Log.d(TAG, getResString(R.string.initUrl));

        if(authToken.equals("") || authToken == null)
            Log.e(TAG, "Do not have a valid auth token");

        stringRequest = new StringRequest(Request.Method.POST, getResString(R.string.initUrl), new Response.Listener<String>() {
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
                        MainActivity.mainActivity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                MainActivity.resultsView.setText(jsonString);
                            }
                        });
                        MainActivity.initPlayerObjectAdapter = new InitPlayerObjectAdapter(jsonArray);
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

    public static void addStringRequestToQueue(StringRequest request){
        queue.add(request);
    }

    public static void addJsonObjectRequestToQueue(JsonObjectRequest request){
        queue.add(request);
    }

    private static String getResString(int id){
        return MainActivity.mainActivity.getResources().getString(id);
    }
}
