/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.searchabledict;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import static com.example.android.searchabledict.DicOpenHelper.FTS_VIRTUAL_TABLE;
import static com.example.android.searchabledict.DicOpenHelper.KEY_DEFINITION;
import static com.example.android.searchabledict.DicOpenHelper.KEY_WORD;

/**
 * Contains logic to return specific words from the dictionary, and load the
 * dictionary table when it needs to be created.
 */
public class DicDBManager
{

	private final DicOpenHelper mDatabaseOpenHelper;


	private  SQLiteDatabase mDatabase;

	private static final String TAG = DicDBManager.class.getCanonicalName();
	private static final HashMap<String, String> mColumnMap = buildColumnMap();
	private Context mContext;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The Context within which to work, used to create the DB
	 */
	public DicDBManager(Context context)
	{
		mContext = context;
		mDatabaseOpenHelper = new DicOpenHelper(context);
		openDatabase();
	}

	/**
	 * Builds a map for all columns that may be requested, which will be given
	 * to the SQLiteQueryBuilder. This is a good way to define aliases for
	 * column names, but must include all columns, even if the value is the key.
	 * This allows the ContentProvider to request columns w/o the need to know
	 * real column names and create the alias itself.
	 */
	private static HashMap<String, String> buildColumnMap()
	{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(KEY_WORD, KEY_WORD);
		map.put(KEY_DEFINITION, KEY_DEFINITION);
		map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
		map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS "
				+ SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS "
				+ SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
		return map;
	}

	/**
	 * Returns a Cursor positioned at the word specified by rowId
	 * 
	 * @param rowId
	 *            id of word to retrieve
	 * @param columns
	 *            The columns to include, if null then all are included
	 * @return Cursor positioned to matching word, or null if not found.
	 */
	public Cursor getWord(String rowId, String[] columns)
	{

		/*
		 * This builds a query that looks like: SELECT <columns> FROM <table>
		 * WHERE rowid = <rowId>
		 */
		String selection = "rowid = ?";
		String[] selectionArgs = new String[]{ rowId };

		return query(selection, selectionArgs, columns);
	}

	/**
	 * Returns a Cursor over all words that match the given query
	 * 
	 * @param query
	 *            The string to search for
	 * @param columns
	 *            The columns to include, if null then all are included
	 * @return Cursor over all words that match, or null if none found.
	 */
	public Cursor getWordMatches(String query, String[] columns)
	{
		String selection = KEY_WORD + " LIKE ?";
		String[] selectionArgs = new String[]
		{ "%" + query + "%" };

		return query(selection, selectionArgs, columns);

		/*
		 * This builds a query that looks like: SELECT <columns> FROM <table>
		 * WHERE <KEY_WORD> MATCH 'query*' which is an FTS3 search for the query
		 * text (plus a wildcard) inside the word column.
		 * 
		 * - "rowid" is the unique id for all rows but we need this value for
		 * the "_id" column in order for the Adapters to work, so the columns
		 * need to make "_id" an alias for "rowid" - "rowid" also needs to be
		 * used by the SUGGEST_COLUMN_INTENT_DATA alias in order for suggestions
		 * to carry the proper intent data. These aliases are defined in the
		 * DictionaryProvider when queries are made. - This can be revised to
		 * also search the definition text with FTS3 by changing the selection
		 * clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
		 * the entire table, but sorting the relevance could be difficult.
		 */
	}

	/**
	 * Performs a database query.
	 * 
	 * @param selection
	 *            The selection clause
	 * @param selectionArgs
	 *            Selection arguments for "?" components in the selection
	 * @param columns
	 *            The columns to return
	 * @return A Cursor over all rows matching the query
	 */
	private Cursor query(String selection, String[] selectionArgs,
			String[] columns)
	{
		/*
		 * The SQLiteBuilder provides a map for all possible columns requested
		 * to actual columns in the database, creating a simple column alias
		 * mechanism by which the ContentProvider does not need to know the real
		 * column names
		 */
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(FTS_VIRTUAL_TABLE);
		builder.setProjectionMap(mColumnMap);

		Cursor cursor = builder.query(
				mDatabaseOpenHelper.getReadableDatabase(), columns, selection,
				selectionArgs, null, null, null);

		if (cursor == null)
		{
			return null;
		}
		else if (!cursor.moveToFirst())
		{
			cursor.close();
			return null;
		}
		return cursor;
	}
	/**
	 * Starts a thread to load the database table with words
	 */
	public void loadDictionary()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					loadWords();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}).start();
	}


	private void loadWords() throws IOException
	{
		Log.d(TAG, "Loading words...");
		final Resources resources = mContext.getResources();
		InputStream inputStream = resources.openRawResource(R.raw.definitions);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));

		try
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] strings = TextUtils.split(line, "-");
				if (strings.length < 2)
					continue;
				long id = addWord(strings[0].trim(), strings[1].trim());
				if (id < 0)
				{
					Log.e(TAG, "unable to add word: " + strings[0].trim());
				}
			}
			addWord("你好", "双方见面打招呼");
		}
		finally
		{
			reader.close();
		}
		Log.d(TAG, "DONE loading words.");
	}

	/**
	 * Add a word to the dictionary.
	 *
	 * @return rowId or -1 if failed
	 */
	public long addWord(String word, String definition)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_WORD, word);
		initialValues.put(KEY_DEFINITION, definition);

		return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
	}


	public void openDatabase() {
		mDatabase = mDatabaseOpenHelper.getWritableDatabase();
	}

	public void closeDatabase() {
		mDatabase.close();
	}

}
