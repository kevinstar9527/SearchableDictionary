package com.example.android.searchabledict.recent_history;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.View;

import com.example.android.searchabledict.R;

/**
 * Created by Administrator on 2017/8/29.
 */

public class MyRecentActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_suggestion);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(this,
                    MySuggestionsProvider.AUTHORITY, MySuggestionsProvider.MODE);
            searchRecentSuggestions.saveRecentQuery(query,null);
        }
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }
}
