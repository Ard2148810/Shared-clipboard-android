package pl.adrian.sharedclipboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import pl.adrian.sharedclipboard.ConnectionService.LocalBinder;

public class ConnectionActivity extends AppCompatActivity {


    Button btnConnect;
    TextView statusValue;

    boolean isBound = false;
    boolean isConnected = false;
    ConnectionService connectionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("ConnectionActivity: onCreate()");

        setContentView(R.layout.activity_connection);
        this.btnConnect = findViewById(R.id.btn_connect);
        this.statusValue = findViewById(R.id.connection_state);


        this.btnConnect.setOnClickListener(listener -> {
            if(connectionService != null) {
                btnConnect.setEnabled(false);
                Intent intent = new Intent(this, ConnectionService.class);
                if(!isConnected) {
                    System.out.println("ConnectionActivity: starting service...");
                    startService(intent);
                    this.statusValue.setText(R.string.ws_connecting);
                } else {
                    System.out.println("ConnectionActivity: stopping service...");
                    connectionService.disconnectWebSocket();
                    stopService(intent);
                    this.statusValue.setText(R.string.ws_disconnected);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("Connection Activity: onStart()");

        bindConnectionService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Connection Activity: onStop()");

        unbindConnectionService();
    }

    private void setConnectionStatus(boolean value) {
        System.out.println("Setting connection status to: " + value);
        this.isConnected = value;
        this.btnConnect.setEnabled(true);
        if(value) {
            this.statusValue.setText(R.string.ws_connected);
            this.btnConnect.setText(R.string.btn_disconnect);
        } else {
            this.statusValue.setText(R.string.ws_disconnected);
            this.btnConnect.setText(R.string.btn_connect);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ConnectionActivity: onDestroy()");
    }

    private void bindConnectionService() {
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, serviceHandler, Context.BIND_AUTO_CREATE);
    }

    private void unbindConnectionService() {
        unbindService(serviceHandler);
        isBound = false;
    }

    public void getStatus() {
        connectionService.isConnected.observe(this, this::setConnectionStatus);
    }

    private ServiceConnection serviceHandler = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            connectionService = binder.getService();
            isBound = true;
            getStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

    };

}