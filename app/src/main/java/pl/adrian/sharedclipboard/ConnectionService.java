package pl.adrian.sharedclipboard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

public class ConnectionService extends Service {

    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "ChannelID";
    MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    private final IBinder binder = new LocalBinder();

    private WebSocket ws;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("ConnectionService: onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("ConnectionService: onStartCommand()");

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(getString(R.string.ws_connecting)));
        initWebSocket();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("ConnectionService: onDestroy()");
        if(ws != null) {
            ws.disconnect();
            ws = null;
        }
    }



    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ConnNotifChan";
            String description = "ConnNotifDesc";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void initWebSocket() {
        try {
            this.ws = new WebSocketFactory()
                    .setConnectionTimeout(8000)
                    .createSocket("ws://192.168.8.125:5001")
                    .addListener(new WebSocketConnectionAdapter(this))
                    .connectAsynchronously();
        } catch (IOException e) {
            Log.e("WebSocket:CS", e.getMessage());
            e.printStackTrace();
        }
    }


    private void updateNotificationText(String text) {
        Notification notification = createNotification(text);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification createNotification(String text) {
        Intent notificationIntent = new Intent(this, ConnectionActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(getText(R.string.notification_t))
                    .setContentText(text)
                    .setSmallIcon(R.drawable.baseline_settings_24)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.ticker_text))
                    .build();
        }
        return notification;
    }

    void setConnectionStatus(boolean value) {
        System.out.println("Posting value: " + value);
        isConnected.postValue(value);
        if(!value) {
            stopForeground(true);
        } else {
            updateNotificationText(getString(R.string.ws_connected));
        }
    }

    /*
    Has to be called before the service is explicitly stopped since no method is called when it's stopped and bounded at the same time
     */
    void disconnectWebSocket() {
        this.ws.disconnect();
    }

    public class LocalBinder extends Binder {
        ConnectionService getService() {
            return ConnectionService.this;
        }
    }
}