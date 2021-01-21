package pl.adrian.sharedclipboard;

import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    private ClipboardManager clipboardManager;

    private RecyclerView historyItemsRecyclerView;
    private ClipboardHistoryAdapter clipboardHistoryAdapter;
    private List<String> clipboardHistoryList;

    private ConnectionService connectionService;

    private TextView status;

    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        status = findViewById(R.id.status_value);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        findViewById(R.id.btn_connection).setOnClickListener(view1 -> {
            Intent intent = new Intent(view1.getContext(), ConnectionActivity.class);
            startActivity(intent);
        });

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(this::onClipboardChanged);

        historyItemsRecyclerView = findViewById(R.id.clipboardHistoryRecyclerView);
        historyItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        clipboardHistoryAdapter = new ClipboardHistoryAdapter(this);
        historyItemsRecyclerView.setAdapter(clipboardHistoryAdapter);

        initClipboardHistory();
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("Main Activity: onStart()");

        bindConnectionService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Main Activity: onStop()");

        unbindConnectionService();
    }


    private void initClipboardHistory() {
        List<String> items = SharedPrefManager.read(getString(R.string.clipboard_history_items), this, getString(R.string.preferences_file_key_history));
        if(items != null) {
            this.clipboardHistoryList = new ArrayList<>(items);
        } else {
            this.clipboardHistoryList = new ArrayList<>();
        }
        this.clipboardHistoryAdapter.setHistoryItems(this.clipboardHistoryList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClipboardChanged() {
        addItemToClipboardHistory(getFromClipboard());
    }

    public String getFromClipboard() {
        return clipboardManager
                .getPrimaryClip()
                .getItemAt(0)
                .getText()
                .toString();
    }

    public void addItemToClipboardHistory(String text) {
        this.clipboardHistoryList.add(0, text);
        SharedPrefManager.save(
                "clipboard_history_items",
                this.clipboardHistoryList,
                this,
                getString(R.string.preferences_file_key_history)
        );
        this.clipboardHistoryAdapter.notifyDataSetChanged();
    }

    public void removeItemFromClipboardHistory(int position) {
        this.clipboardHistoryList.remove(position);
        SharedPrefManager.save(
                getString(R.string.clipboard_history_items),
                this.clipboardHistoryList,
                this,
                getString(R.string.preferences_file_key_history)
        );
        this.clipboardHistoryAdapter.notifyDataSetChanged();
    }

    private void bindConnectionService() {
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, serviceHandler, Context.BIND_AUTO_CREATE);
    }

    private void unbindConnectionService() {
        unbindService(serviceHandler);
        isBound = false;
    }

    private void setConnectionStatus(boolean value) {
        if(value) {
            this.status.setText(R.string.ws_connected);
        } else {
            this.status.setText(R.string.ws_disconnected);
        }
    }

    public void getStatus() {
        connectionService.isConnected.observe(this, this::setConnectionStatus);
    }

    private ServiceConnection serviceHandler = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) service;
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