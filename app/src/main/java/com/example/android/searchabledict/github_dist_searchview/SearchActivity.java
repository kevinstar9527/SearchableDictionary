package com.example.android.searchabledict.github_dist_searchview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.android.searchabledict.R;


public class SearchActivity extends Activity
{
    public static Intent createIntent(Context context)
    {
        return new Intent(context, SearchActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchDistDBManager dbManager = new SearchDistDBManager(this);
        dbManager.addResult("1");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu) | true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        final SearchView searchView;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            searchView = (SearchView) menu.findItem(R.id.search).getActionView();

            searchView.setSuggestionsAdapter(new SearchSuggestionsAdapter(this));

            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionClick(int position) {
                    Toast.makeText(SearchActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(SearchActivity.this, query, Toast.LENGTH_SHORT).show();
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Cursor cursor = TextUtils.isEmpty(newText) ? null : query(newText);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        searchView.getSuggestionsAdapter().changeCursor(cursor);
                    }
                    return false;
                }
            });
        }
        return super.onPrepareOptionsMenu(menu) | true;
    }

    private Cursor query(String newText) {
        SearchDistDBManager distDBManager = new SearchDistDBManager(this);
        return distDBManager.query(newText);
    }

    public static class SearchSuggestionsAdapter extends SimpleCursorAdapter
    {
        private static final String[] mFields  = { "_id","result" };
        private static final String[] mVisible = { "result" };
        private static final int[]    mViewIds = { android.R.id.text1 };


        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public SearchSuggestionsAdapter(Context context)
        {
            super(context, android.R.layout.simple_list_item_1, null, mVisible, mViewIds, 0);
        }


    }
}