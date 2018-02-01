package dono.dev.utils;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import dono.dev.klaxon.R;
import dono.dev.service.TicBroadcastReceiver;

public class KlaxonAlarmManager {

    private static final String TAG = "KlaxonAlarmManager";

    private static KlaxonAlarmManager instance = null;
    private NotificationManager mNotificationManager;
    private PendingIntent ticIntent;

    private Context context;
    private AlarmManager alarmMgr;

    private KlaxonAlarmManager(){
        
    }

    public static KlaxonAlarmManager getInstance(){
        if(instance == null)
            instance = new KlaxonAlarmManager();
        return instance;
    }

    public void initialize(Context context){
        this.context = context;
    }

    public void blank(){

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("Klaxon")
                .setContentText("Klaxon Early Warning System Enabled")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher);
        //You can add a class notification here to go off when you click it
        Intent notifIntent = new Intent();
        PendingIntent pIntent = PendingIntent.getActivity(context, 1337 , notifIntent, 0);
        builder.setContentIntent(pIntent);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notif = builder.build();
        mNotificationManager.notify(1337, notif);

        setAlarm();
    }

    public void setAlarm(){
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(TicBroadcastReceiver.class.toString());
        ticIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
//                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
//                AlarmManager.INTERVAL_HOUR,
//                ticIntent);
        alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP
                , (SystemClock.elapsedRealtime() + 15000)
                , ticIntent);
    }

    public void cancel(){
        if (alarmMgr!= null) {
            alarmMgr.cancel(ticIntent);
        }
        if(mNotificationManager != null){
            mNotificationManager.cancel(1337);
        }
    }
}
