package dono.dev.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.android.volley.toolbox.StringRequest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import dono.dev.http.HttpManager;
import dono.dev.klaxon.MainActivity;
import dono.dev.klaxon.R;
import dono.dev.utils.JsonObjectKeysPair;
import dono.dev.utils.KlaxonUtils;

/**
 * TODO move strings to strings xml
 * @author EricDonovan
 *
 */
public class InitPlayerObjectAdapter extends BaseAdapter{

    private static final String TAG = "InitPlayerObject";

    private LayoutInflater inflater = null;

    private ArrayList<OpenGame> openGames;

    public InitPlayerObjectAdapter(JSONArray jsonArray){

        inflater = (LayoutInflater) MainActivity.mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<JsonObjectKeysPair> objectKeysList = (List<JsonObjectKeysPair>) KlaxonUtils.parseJSONArray(jsonArray);

        try{
            //pull the keys we are interested in
            for(JsonObjectKeysPair objectKeyPair : objectKeysList) {
                for(String key : objectKeyPair.getKeys()){
                    Log.d(TAG, "Key: " + key);
                    if(key.equals("open_games"))
                        parseOpenGames((JSONArray)objectKeyPair.getObject().get(key));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e);
        }
    }

    private void parseOpenGames(JSONArray jsonArray){

        openGames = new ArrayList<OpenGame>();
        String name    = null;
        String number  = null;
        String players = null;
        String version = null;

        List<JsonObjectKeysPair> objectKeysList = (List<JsonObjectKeysPair>) KlaxonUtils.parseJSONArray(jsonArray);
        Log.d(TAG, "   List Length: " + objectKeysList.size());

        try {
            for(JsonObjectKeysPair objectKeyPair : objectKeysList){
                Log.d(TAG, "   Keys Length " + objectKeyPair.getKeys().size());
                for(String key : objectKeyPair.getKeys()){
                    Log.d(TAG, "      Key: " + key);
                    
                    if (key.equals("name")){
                        name = objectKeyPair.getObject().get(key).toString();
                    } else if (key.equals("number")) {
                        number = objectKeyPair.getObject().get(key).toString();
                    } else if (key.equals("players")) {
                        players = objectKeyPair.getObject().get(key).toString();
                    } else if (key.equals("version")) {
                        version = objectKeyPair.getObject().get(key).toString();
                    }
                }
                openGames.add(new OpenGame(name, number, players, version));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e);
        }
    }

    @Override
    public int getCount() {
        return openGames.size();
    }

    @Override
    public Object getItem(int position) {
        return openGames.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final OpenGame openGame = openGames.get(position);

        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.player_games_row_item, parent, false);
        } else {
            view = convertView;
        }

        view.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
//                MainActivity.mainActivity.runOnUiThread(new Runnable(){
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.mainActivity, openGame.getNumber(), Toast.LENGTH_SHORT).show();
//                    }
//                });
                StringRequest stringRequest = HttpManager.createOrderStringRequest(MainActivity.mainActivity, openGame);
                if(stringRequest != null)
                    HttpManager.addStringRequestToQueue(stringRequest);
            }
        });

        TextView gameNameTV      = (TextView) view.findViewById(R.id.gameName);
        TextView gameVersionTV   = (TextView) view.findViewById(R.id.gameType);
        TextView numberPlayersTV = (TextView) view.findViewById(R.id.gamePlayers);
        TextView gameIdTV        = (TextView) view.findViewById(R.id.gameId);

        gameNameTV.setText(openGame.getName());
        gameVersionTV.setText(openGame.getVersion());
        numberPlayersTV.setText(openGame.getPlayers());
        gameIdTV.setText(openGame.getNumber());

        return view;
    }
}
