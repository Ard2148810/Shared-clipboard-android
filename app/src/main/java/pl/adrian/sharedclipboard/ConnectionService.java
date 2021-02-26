package pl.adrian.sharedclipboard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

public class ConnectionService extends Service {

    private static final int NOTIFICATION_ID = 123;
    final int WEBSOCKET_TIMEOUT = 8000;
    private static final String CHANNEL_ID = "ChannelID";
    MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    MutableLiveData<Message> message = new MutableLiveData<>();
    MutableLiveData<String> roomId = new MutableLiveData<>();
    private final IBinder binder = new LocalBinder();
    private ClipboardManager clipboardManager;
    private ClipboardListener listener;
    private String requestedRoomId = "";

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

        listener = new ClipboardListener(this);
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(listener);

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
                    .setConnectionTimeout(WEBSOCKET_TIMEOUT)
                    .createSocket(getString(R.string.SERVER_ADDRESS))
                    .addListener(new WebSocketConnectionAdapter(this));
            if(!requestedRoomId.isEmpty()) {
                this.ws.addProtocol(this.requestedRoomId);
            }
            this.ws.connectAsynchronously();
        } catch (IOException e) {
            Log.e("WebSocket:CS", e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendTextMessage(String message) {
        Message msg = new Message("text", message);
        String jsonMsg = new Gson().toJson(msg);
        if(isConnected.getValue()) {
            this.ws.sendText(jsonMsg);
        }
    }

    public void setRequestedRoomId(String id) {
        this.requestedRoomId = id;
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
                    .setSmallIcon(R.drawable.ic_baseline_content_paste_24)
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
            if(clipboardManager != null) {
                clipboardManager.removePrimaryClipChangedListener(listener);
            }
        } else {
            updateNotificationText(getString(R.string.ws_connected));
        }
    }

    /*
    Has to be called before the service is explicitly stopped (but not unbound) since no method is called when it's stopped and bounded at the same time
     */
    void disconnectWebSocket() {
        this.ws.disconnect();
    }

    public void clipIsSetByServer(String text) {
        addItemToClipboardHistory(text);
    }

    public void setRoomId(String content) {
        this.roomId.postValue(content);
    }

    public class LocalBinder extends Binder {
        ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    private class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {

        private final ConnectionService service;

        ClipboardListener(ConnectionService service) {
            this.service = service;
        }

        @Override
        public void onPrimaryClipChanged() {
            System.out.println("ConnectionService: Clipboard changed");
            ClipData clip = clipboardManager.getPrimaryClip();
            CharSequence label = null;
            if(clip != null) {
                label = clip.getDescription().getLabel();
                CharSequence item = clip
                        .getItemAt(0)
                        .getText();
                if(item != null) {
                    String text = item.toString();
                    boolean fromServer = false;
                    if(label != null) {
                        fromServer = label.toString().equals(getString(R.string.clipboard_item_from_server));
                    }
                    if(!fromServer && service.isConnected.getValue()) {
                        this.service.sendTextMessage(text);
                        service.addItemToClipboardHistory(text);
                    }
                }
            }
        }
    }

    public void addItemToClipboardHistory(String text) {
        System.out.println("ConnectionService: addItemToClipboardHistory()");
        SharedPrefManager.addItem(
                getString(R.string.clipboard_history_items),
                text,
                this,
                getString(R.string.preferences_file_key_history)
        );
        message.postValue(new Message("text", text));
    }
}