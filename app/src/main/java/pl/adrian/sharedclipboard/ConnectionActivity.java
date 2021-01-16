package pl.adrian.sharedclipboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

public class ConnectionActivity extends AppCompatActivity implements ConnectionStatus.ConnectionStatusListener {


    Button btnConnect;
    TextView statusValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        ConnectionStatus.addListener(this);
        this.btnConnect = findViewById(R.id.btn_connect);
        this.statusValue = findViewById(R.id.connection_state);
        updateStateText(ConnectionStatus.getConnectionStatus());


        this.btnConnect.setOnClickListener(listener -> {
            Intent intent = new Intent(this, ConnectionService.class);
            startService(intent);
            statusValue.setText(getString(R.string.ws_connecting));
            this.btnConnect.setEnabled(false);
        });
    }

    @Override
    protected void onDestroy() {
        ConnectionStatus.removeListener(this);
        super.onDestroy();
    }

    @Override
    public void statusChanged(ConnectionStatus.ConnectionStatusState state) {
        updateStateText(state);
    }

    private void updateStateText(ConnectionStatus.ConnectionStatusState state) {
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
        this.statusValue.setText(msg);
    }
}