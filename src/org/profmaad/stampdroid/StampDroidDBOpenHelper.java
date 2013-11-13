package org.profmaad.stampdroid;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class StampDroidDBOpenHelper extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "StampDroid";

	StampDroidDBOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE user_transactions (id INTEGER PRIMARY KEY NOT NULL, bitstamp_id INTEGER UNIQUE NOT NULL, order_id INTEGER, type INTEGER NOT NULL, usd_amount REAL, btc_amount REAL, fee_usd REAL, timestamp INTEGER NOT NULL, btc_usd_exchange_rate REAL);");
	}

	public void onUpgrade(SQLiteDatabase db, int old_version, int new_version)
	{
		if(old_version < 1)
		{
			db.execSQL("CREATE TABLE user_transactions (id INTEGER PRIMARY KEY NOT NULL, bitstamp_id INTEGER UNIQUE NOT NULL, order_id INTEGER, type INTEGER NOT NULL, usd_amount REAL, btc_amount REAL, fee_usd REAL, timestamp INTEGER NOT NULL, btc_usd_exchange_rate REAL);");
		}
	}
}
