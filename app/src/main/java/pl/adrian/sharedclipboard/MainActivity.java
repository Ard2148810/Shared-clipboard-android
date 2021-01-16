package pl.adrian.sharedclipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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

import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConnectionStatus.ConnectionStatusListener {

    private ClipboardManager clipboardManager;

    private RecyclerView historyItemsRecyclerView;
    private ClipboardHistoryAdapter clipboardHistoryAdapter;
    private List<String> clipboardHistoryList;

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        status = findViewById(R.id.status_value);
        setSupportActionBar(toolbar);
        ConnectionStatus.addListener(this);

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
        //setConnectionStatus();
    }

    @Override
    protected void onDestroy() {
        ConnectionStatus.removeListener(this);
        super.onDestroy();
    }

    public void setConnectionStatus(ConnectionStatus.ConnectionStatusState state) {
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
        this.status.setText(msg);
    }



    private void initClipboardHistory() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        List<String> items = SharedPrefManager.read(sharedPref, "clipboard_history_items");
        clipboardHistoryList = new ArrayList<>();
        if(items != null) {
            clipboardHistoryList.addAll(items);
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
        this.clipboardHistoryList.add(text);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPrefManager.save(sharedPref, this.clipboardHistoryList, "clipboard_history_items");
        this.clipboardHistoryAdapter.notifyDataSetChanged();
    }

    public void removeItemFromClipboardHistory(int position) {
        this.clipboardHistoryList.remove(position);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPrefManager.save(sharedPref, this.clipboardHistoryList, "clipboard_history_items");
        this.clipboardHistoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void statusChanged(ConnectionStatus.ConnectionStatusState state) {
        setConnectionStatus(state);
    }
}