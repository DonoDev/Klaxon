package dono.dev.klaxon;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

    private static final String TAG = "MainActivity";

    private static final String URL      = "https://triton.ironhelmet.com/arequest/login";
    private static final String ALIAS    = "emdonova@ncsu.edu";
    private static final String PASSWORD = "Iwantabelt3";

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.authorizeButton);
        button.setOnClickListener(this);

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.authorizeButton:
            Toast.makeText(this, "Testing", Toast.LENGTH_SHORT).show();
            // Add the request to the RequestQueue.
            StringRequest request = createPostRequest();
            if(request != null)
                queue.add(request);
            break;
        default:
            break;
        }
    }

    private StringRequest createPostRequest() {
        StringRequest stringRequest = null;
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("type", "login");
            jsonBody.put("alias", ALIAS);
            jsonBody.put("password", PASSWORD);
            final String requestBody = jsonBody.toString();

            stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
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

                        Log.i(TAG, "Status Code: " + responseString);
                        for(String header : response.headers.values()){
                            Log.i(TAG, "Response header: " + header);
                        }
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stringRequest;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
