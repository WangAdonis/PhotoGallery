package cn.adonis.photogallery.service;

import android.app.AlarmManager;
import android.app.IntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import cn.adonis.photogallery.FlickrFetchr;
import cn.adonis.photogallery.GalleryItem;
import cn.adonis.photogallery.R;
import cn.adonis.photogallery.activity.PhotoGalleryActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PollService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this


    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL=1000*15;

    public PollService() {
        super("PollService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable=cm.getBackgroundDataSetting()&&
                cm.getActiveNetworkInfo()!=null;
        if(!isNetworkAvailable) return;

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String query=prefs.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        String lastRusultID=prefs.getString(FlickrFetchr.PREF_LAST_RESULT_ID,null);
        ArrayList<GalleryItem> items;
        if(query!=null){
            items=new FlickrFetchr().search(query);
        }else {
            items=new FlickrFetchr().fetchItems();
        }

        if(items.size()==0){
            return;
        }
        String resultId=items.get(0).getId();
        if(!resultId.equals(lastRusultID)){
            Log.e(TAG,"Got a new result: "+resultId);

            Resources r=getResources();
            PendingIntent pi=PendingIntent.getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);
            Notification notification=new NotificationCompat.Builder(this)
                    .setTicker(r.getString(R.string.new_pictures_title))
                    .setContentTitle(r.getString(R.string.new_pictures_title))
                    .setContentText(r.getString(R.string.new_pictures_text))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0,notification);

        }else {
            Log.e(TAG,"Got an old result: "+resultId);
        }
        prefs.edit().putString(FlickrFetchr.PREF_LAST_RESULT_ID,resultId).commit();
    }

    public static void setServiceAlarm(Context context,boolean isOn){
        Intent i=new Intent(context,PollService.class);
        PendingIntent pi=PendingIntent.getService(context,0,i,0);
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(isOn){
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
        }else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = new Intent(context,PollService.class);
        PendingIntent pi=PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi!=null;
    }
}
