package pl.adrian.sharedclipboard;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;

public class WebSocketConnectionAdapter extends WebSocketAdapter {

    MainActivity activity;

    WebSocketConnectionAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) {
        Log.println(
                Log.DEBUG,
                "WebSocket",
                "Message: " + text + " | ws: " + websocket.toString());
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        Log.println(Log.ERROR, "WebSocket", exception.getMessage());
    }
}
