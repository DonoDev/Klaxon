package dono.dev.klaxon;

import com.android.volley.toolbox.StringRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import dono.dev.http.HttpManager;
import dono.dev.model.InitPlayerObjectAdapter;
import dono.dev.model.OpenGame;

/**
 * On first load enter username/password.  check prefs and prompt if null;
 * @author Eric Donovan, /u/burnbarrelncs, donodev, ericdonovandev@gmail.com
 * 
 */
public class MainActivity extends Activity implements OnClickListener{

    private static final String TAG = "MainActivity";

    private String alias;
    private String password;

    private SharedPreferences prefs;

    private static ProgressBar spinner;
    public static ListView resultsView;
    public static MainActivity mainActivity;

    //model
    public static InitPlayerObjectAdapter initPlayerObjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener);

        alias    = prefs.getString(getResString(R.string.username), getResString(R.string.defaultValue));
        password = prefs.getString(getResString(R.string.password), getResString(R.string.defaultValue));

        resultsView = (ListView)    findViewById(R.id.resultView);
        spinner     = (ProgressBar) findViewById(R.id.progressBar);

        // Instantiate the RequestQueue.
        HttpManager.initialize(this);
        displayLoginDialog();
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
        if (id == R.id.action_login) {
            displayLoginDialog();
            return true;
        } else if (id == R.id.action_quit){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//        case R.id.:
//            break;
        default:
            break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (HttpManager.queue != null) {
            HttpManager.queue.cancelAll(TAG);
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // your stuff here
        }
    };

    private void displayLoginDialog(){
        Drawable icon = getResources().getDrawable(R.drawable.ic_launcher);

        //setup dialog view
        View view = getLayoutInflater().inflate(R.layout.settings_dialog_view, null);

        final EditText usernameET = (EditText) view.findViewById(R.id.usernameEditText);
        final EditText passwordET = (EditText) view.findViewById(R.id.passwordEditText);

        //set default values
        usernameET.setText(alias);
        passwordET.setText(password);

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
                            return;
                        } else {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("neptunesUsername", alias);
                            editor.putString("neptunesPassword", password);
                            editor.commit();
                            setAliasPassword(alias, password);
                            dialog.dismiss();
                        }

                        StringRequest stringRequest = HttpManager.createLoginPostRequest(mainActivity);
                        if(stringRequest != null)
                            HttpManager.addStringRequestToQueue(stringRequest);
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

    private void startService(){
        
    }

    private String getResString(int id){
        return getResources().getString(id);
    }

    @Override
    public void onBackPressed() {
        Drawable icon = getResources().getDrawable(R.drawable.ic_launcher);

        //setup dialog
        AlertDialog.Builder adb = new AlertDialog.Builder(this)
        .setTitle("Exit Klaxon?")
        .setIcon(icon)
        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int whichButton) {
                        dialog.dismiss();
                        finish();
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

    public static void setAdapter(){
        spinner.setVisibility(View.GONE);
        resultsView.setAdapter(initPlayerObjectAdapter);
    }

    public void displayGameInfo(OpenGame openGame, String gameData){

        Drawable icon = getResources().getDrawable(R.drawable.ic_launcher);

        //setup dialog view
        View view = getLayoutInflater().inflate(R.layout.game_data_view, null);

        TextView gameDataTV = (TextView) view.findViewById(R.id.gameDataTV);
        gameDataTV.setText(gameData);

        //setup dialog
        AlertDialog.Builder adb = new AlertDialog.Builder(this)
        .setTitle(openGame.getName())
        .setIcon(icon)
        .setView(view)
        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int whichButton) {
                        dialog.dismiss();
                    }
        });
        final AlertDialog ad = adb.create();
        ad.show();
    }
}
