package org.fox.ttrss;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import icepick.State;

public class LogcatActivity extends CommonActivity {
    private static final int MAX_LOG_ENTRIES = 500;
    private final String TAG = this.getClass().getSimpleName();
    @State protected ArrayList<String> m_items = new ArrayList<>();
    ArrayAdapter<String> m_adapter;
    ListView m_list;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_logcat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            refresh();
        }

        m_adapter = new ArrayAdapter<>(this, R.layout.logcat_row, m_items);

        m_list = findViewById(R.id.logcat_output);
        m_list.setAdapter(m_adapter);

        final SwipeRefreshLayout swipeLayout = findViewById(R.id.logcat_swipe_container);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeLayout.setRefreshing(false);
            }
        });
    }

    private void refresh() {
        m_items.clear();

        try {
            Process process = Runtime.getRuntime().exec("logcat -d -t " +  MAX_LOG_ENTRIES);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                m_items.add(0, line);
            }

        } catch (Exception e) {
            m_items.add(e.toString());
        }

        if (m_adapter != null) m_adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_logcat, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.logcat_copy:
                shareLogcat();
                return true;
            case R.id.logcat_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareLogcat() {
        StringBuilder buf = new StringBuilder();

        for (String item : m_items)
            buf.append(item + "\n");

        copyToClipboard(buf.toString());
    }
}
