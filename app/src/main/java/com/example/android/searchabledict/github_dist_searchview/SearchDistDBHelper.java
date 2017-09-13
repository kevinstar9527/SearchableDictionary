package com.example.android.searchabledict.github_dist_searchview;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/8/30.
 */

public class SearchDistDBHelper extends SQLiteOpenHelper {
    public static final String DATBASENAME = "searchdist";
    public static final int DATABAE_VERSION = 1;
    public static final String TABLE = "dist";
    public static final String KEY_WORD = "result";
    public static final String KEY_ID = "_id";
    private static final String TABLE_CREATE="create table "+ TABLE +"("+KEY_ID+" integer primary key autoincrement "+","+KEY_WORD+")";
    SearchDistDBHelper(Context context) {
        super(context,DATBASENAME,null,DATABAE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
