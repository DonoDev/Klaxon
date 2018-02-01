package dono.dev.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import dono.dev.klaxon.MainActivity;
import dono.dev.utils.KlaxonAlarmManager;

public class TicBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TicBroadcastReceiver", "received: " + intent.getAction().toString());
        if(intent.getAction().equals(TicBroadcastReceiver.class.toString())){
            Log.d("TicBroadcastReceiver", "equal");
            MainActivity.mainActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    Log.d("TicBroadcastReceiver", "toasty");
                    Toast.makeText(MainActivity.mainActivity, "TEST", Toast.LENGTH_LONG).show();
                    KlaxonAlarmManager.getInstance().setAlarm();
                }
            });
        }
    }
}
