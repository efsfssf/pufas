package com.dandomi.pufas;

import static com.dandomi.pufas.MainActivity.KEY_HISTORY_LIST;
import static com.dandomi.pufas.MainActivity.PREFS_HISTORY;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dandomi.adapters.HistoryAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadFromHistory();

        MaterialToolbar topBar = findViewById(R.id.topBar);
        topBar.post(() -> {
            View navButton = topBar.getChildAt(1); // navigation icon
            if (navButton != null) {
                ViewCompat.setTooltipText(navButton, null);
            }
        });
        topBar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // закрывает Activity
        return true;
    }


    private void loadFromHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
        Gson gson = new Gson();

        String json = prefs.getString(KEY_HISTORY_LIST, "");
        List<HistoryItem> historyList;

        if (!(json.isEmpty() || json.equals("[]"))) {
            Type type = new TypeToken<List<HistoryItem>>() {}.getType();
            historyList = gson.fromJson(json, type);
        } else {
            historyList = new ArrayList<>();
        }

        HistoryAdapter adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);
    }
}
