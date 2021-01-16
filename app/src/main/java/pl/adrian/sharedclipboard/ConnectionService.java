package pl.adrian.sharedclipboard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

public class ConnectionService extends Service implements ConnectionStatus.ConnectionStatusListener {

    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "ChannelID";

    private WebSocket ws;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ConnectionStatus.addListener(this);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(getString(R.string.ws_connecting)));
        initWebSocket();
        ConnectionStatus.setConnectionStatus(ConnectionStatus.ConnectionStatusState.CONNECTING);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        ConnectionStatus.removeListener(this);
        super.onDestroy();
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
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initWebSocket() {
        try {
            this.ws = new WebSocketFactory().createSocket("ws://192.168.43.206:5001");
            this.ws.addListener(new WebSocketConnectionAdapter(this));
            this.ws.connectAsynchronously();
        } catch (IOException e) {
            Log.println(Log.ERROR, "WebSocket", e.getMessage());
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

    @Override
    public void statusChanged(ConnectionStatus.ConnectionStatusState state) {
        String msg = "...";
        switch (state) {
            case CONNECTED:
                msg = getString(R.string.ws_connected);
                break;
            case CONNECTING:
                msg = getString(R.string.ws_connecting);
                break;
            case DISCONNECTED:
                msg = getString(R.string.ws_disconnected);
                break;
            case PAUSED:
                msg = getString(R.string.ws_paused);
                break;
        }
        updateNotificationText(msg);
    }
}