package dono.dev.klaxon;

import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * On first load enter username/password.  check prefs and prompt if null;
 * @author EricDonovan
 */
public class MainActivity extends Activity implements OnClickListener{

    private static final String TAG = "MainActivity";

    private static final String URL = "https://triton.ironhelmet.com/arequest/login";
    private String alias;
    private String password;

    private SharedPreferences prefs;

    private RequestQueue queue;

    private TextView resultsView;
    
    private MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener);

        alias    = prefs.getString("neptunesUsername", "");
        password = prefs.getString("neptunesPassword", "");

        Button loginButton = (Button) findViewById(R.id.authorizeButton);
        loginButton.setOnClickListener(this);
        Button pullUserButton = (Button) findViewById(R.id.getUserDataButton);
        pullUserButton.setOnClickListener(this);

        resultsView = (TextView) findViewById(R.id.resultView);

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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            displaySettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        StringRequest request;
        switch (v.getId()) {
        case R.id.authorizeButton:
            Toast.makeText(this, "authorizing", Toast.LENGTH_SHORT).show();
            // Add the request to the RequestQueue.
            request = createLoginPostRequest();
            if(request != null)
                queue.add(request);
            break;
        case R.id.getUserDataButton:
            Toast.makeText(this, "pulling user data", Toast.LENGTH_SHORT).show();
            request = createPullUserRequest();
            if(request != null)
                queue.add(request);
            break;
        default:
            break;
        }
    }

    private StringRequest createLoginPostRequest() {
        StringRequest stringRequest = null;
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("type", "login");
            jsonBody.put("alias", alias);
            jsonBody.put("password", password);
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

                        final StringBuilder builder = new StringBuilder();
                        for(Entry<String,String> header : response.headers.entrySet()){
                            builder.append(header.toString()).append("\n");
                            Log.i(TAG, "Response header: " + header);
                        }
                        mainActivity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                resultsView.setText(builder.toString());
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

    private StringRequest createPullUserRequest(){
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // your stuff here
        }
    };

    private void displaySettingsDialog(){
        Drawable icon = getResources().getDrawable(R.drawable.ic_launcher);

        //setup dialog view
        View view = getLayoutInflater().inflate(R.layout.settings_dialog_view, null);

        final EditText usernameET = (EditText) view.findViewById(R.id.usernameEditText);
        final EditText passwordET = (EditText) view.findViewById(R.id.passwordEditText);

        //set default values
        usernameET.setText(alias);
        if(!password.equals(""));
            passwordET.setText("********");

        passwordET.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                passwordET.setText("");
                return false;
            }
        });

        //setup dialog
        AlertDialog.Builder adb = new AlertDialog.Builder(this)
        .setTitle("Klaxon Settings")
        .setIcon(icon)
        .setView(view)
        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int whichButton) {
                        String alias    = usernameET.getText().toString();
                        String password = passwordET.getText().toString();

                        if(alias.equals("") || password.equals("")){
                            Toast.makeText(getBaseContext(), "Please enter an email and password", Toast.LENGTH_SHORT).show();
                        } else {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("neptunesUsername", alias);
                            editor.putString("neptunesPassword", password);
                            editor.commit();
                            setAliasPassword(alias, password);
                            dialog.dismiss();
                        }
                    }
        })
        .setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int whichButton) {
                        dialog.dismiss();
                    }
        });
        final AlertDialog ad = adb.create();
        ad.show();
    }

    private void setAliasPassword(String alias, String password){
        this.alias    = alias;
        this.password = password;
    }
}
