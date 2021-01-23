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

        saveTextToClipboard(readTextMessage(text));
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
        System.out.println("WebSocketConnectionAdapter: onDisconnected()");
        service.setConnectionStatus(false);
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
    }

    public String readTextMessage(String json) {
        Gson gson = new Gson();
        Message msg = gson.fromJson(json, Message.class);
        return msg.getContent();
    }

    public void saveTextToClipboard(String text) {
        ClipData clip = ClipData.newPlainText(service.getString(R.string.clipboard_item_from_server), text);
        clipboardManager.setPrimaryClip(clip);
        service.clipIsSetByServer(text);
    }
}
