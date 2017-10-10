package dono.dev.http;

import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import dono.dev.klaxon.MainActivity;
import dono.dev.klaxon.R;

public class HttpManager {

    private static final String TAG = "HttpManager";

    public static RequestQueue queue;

    public static void initialize(Context context){
        queue = Volley.newRequestQueue(context);
    }

    public static StringRequest createLoginPostRequest(Context context) {
        StringRequest stringRequest = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String alias    = prefs.getString(getResString(R.string.username), getResString(R.string.defaultValue));
        String password = prefs.getString(getResString(R.string.password), getResString(R.string.defaultValue));

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("type", "login");
            jsonBody.put("alias", alias);
            jsonBody.put("password", password);
            final String requestBody = jsonBody.toString();

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
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers

                        final StringBuilder builder = new StringBuilder();
                        for(Entry<String,String> header : response.headers.entrySet()){
                            builder.append(header.toString()).append("\n");
                            Log.i(TAG, "Response header: " + header);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stringRequest;
    }

    public static StringRequest createPullUserRequest(Context context){
        return null;
    }

    public static void addStringRequestToQueue(StringRequest request){
        queue.add(request);
    }

    private static String getResString(int id){
        return MainActivity.mainActivity.getResources().getString(id);
    }
}
