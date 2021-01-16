package pl.adrian.sharedclipboard;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;

import java.util.List;
import java.util.Map;

public class WebSocketConnectionAdapter extends WebSocketAdapter {

    ConnectionService service;

    WebSocketConnectionAdapter(ConnectionService service) {
        this.service = service;
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

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ConnectionStatus.setConnectionStatus(ConnectionStatus.ConnectionStatusState.CONNECTED);
        super.onConnected(websocket, headers);
    }
}
