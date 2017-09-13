package com.example.android.searchabledict;

import android.app.SearchManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This creates/opens the database.
 */
public class DicOpenHelper extends SQLiteOpenHelper
{

	private static final String TAG = DicOpenHelper.class.getCanonicalName();


	private static final String DATABASE_NAME = "dictionary";
	private static final int DATABASE_VERSION = 2;

	public static final String FTS_VIRTUAL_TABLE = "FTSdictionary";

	// The columns we'll include in the dictionary table
	public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2;

	/*
	 * Note that FTS3 does not support column constraints and thus, you cannot
	 * declare a primary key. However, "rowid" is automatically used as a unique
	 * identifier, so when making requests, we will use "_id" as an alias for
	 * "rowid"
	 */
	private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
			+ FTS_VIRTUAL_TABLE + " USING fts3 (" + KEY_WORD + ", "
			+ KEY_DEFINITION + ");";

	DicOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{

		db.execSQL(FTS_TABLE_CREATE);
	}



	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
		onCreate(db);
	}
}
