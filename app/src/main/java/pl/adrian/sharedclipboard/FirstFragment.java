package pl.adrian.sharedclipboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {


    private RecyclerView historyItemsRecyclerView;
    private ClipboardHistoryAdapter clipboardHistoryAdapter;
    private List<String> clipboardHistoryList;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_connection).setOnClickListener(view1 -> {
            Intent intent = new Intent(view1.getContext(), ConnectionActivity.class);
            startActivity(intent);
        });
        clipboardHistoryList = new ArrayList<>();
        historyItemsRecyclerView = view.findViewById(R.id.clipboardHistoryRecyclerView);
        historyItemsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        clipboardHistoryAdapter = new ClipboardHistoryAdapter(this);
        historyItemsRecyclerView.setAdapter(clipboardHistoryAdapter);

        clipboardHistoryList.add("First item placeholder");
        clipboardHistoryList.add("Second item placeholder");
        clipboardHistoryList.add("Third item placeholder");

        clipboardHistoryAdapter.setHistoryItems(clipboardHistoryList);
    }
}