package pl.adrian.sharedclipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;
import java.util.Map;

public class WebSocketConnectionAdapter extends WebSocketAdapter {

    ConnectionService service;
    ClipboardManager clipboardManager;

    final int INCORRECT_ROOM_ID = 4001;

    WebSocketConnectionAdapter(ConnectionService service) {
        this.service = service;
        this.clipboardManager = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) {
        Log.println(
                Log.DEBUG,
                "WebSocket",
                "Message: " + text + " | ws: " + websocket.toString());
        Message msg = readTextMessage(text);
        if(msg.getType().equals("room-id")) {
            service.setRoomId(msg.getContent());
        } else {
            saveTextToClipboard(msg.getContent());
        }
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        System.out.println("WebSocketConnectionAdapter: onConnectError()");
        Log.println(Log.ERROR, "WebSocket:CA", exception.getMessage());

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(service.getApplicationContext(), "Server connection error", Toast.LENGTH_SHORT).show());

        service.setConnectionStatus(false);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        System.out.println("WebSocketConnectionAdapter: onConnected()");

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(service.getApplicationContext(), "Successfully connected", Toast.LENGTH_SHORT).show());

        service.setConnectionStatus(true);
        super.onConnected(websocket, headers);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        System.out.println("WebSocketConnectionAdapter: onDisconnected()");

        if(serverCloseFrame.getCloseCode() == INCORRECT_ROOM_ID) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(service.getApplicationContext(), serverCloseFrame.getCloseReason(), Toast.LENGTH_SHORT).show());
        }

        service.setConnectionStatus(false);
        service.setRoomId("");
    }

    public Message readTextMessage(String json) {
        return new Gson().fromJson(json, Message.class);
    }

    public void saveTextToClipboard(String text) {
        ClipData clip = ClipData.newPlainText(service.getString(R.string.clipboard_item_from_server), text);
        clipboardManager.setPrimaryClip(clip);
        service.clipIsSetByServer(text);
    }
}
