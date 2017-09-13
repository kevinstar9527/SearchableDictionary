package com.example.android.searchabledict.recent_history;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Administrator on 2017/8/29.
 */

public class MySuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.MySuggestionProvider";

    public final static int MODE = DATABASE_MODE_QUERIES;



    public MySuggestionsProvider() {

        setupSuggestions(AUTHORITY, MODE);

    }
}
