package com.example.android.searchabledict.github_dist_searchview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 2017/8/30.
 */

public class SearchDistDBManager {

    private SearchDistDBHelper mDBHelper;
    private SQLiteDatabase db;
    private Context mContext;

    public SearchDistDBManager(Context context) {
        mContext = context;
        mDBHelper = new SearchDistDBHelper(context);
        openDatabase();
    }
    public void openDatabase() {
        db = mDBHelper.getWritableDatabase();
    }
    public long addResult(String result){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SearchDistDBHelper.KEY_WORD,result);
        return db.insert(SearchDistDBHelper.TABLE,null,contentValues);
    }
    public Cursor query(String result){
        return db.rawQuery("select * from "+SearchDistDBHelper.TABLE,null);
    }
}
