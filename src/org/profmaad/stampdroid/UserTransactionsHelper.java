package org.profmaad.stampdroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import android.util.Log;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteConstraintException;

import org.json.JSONTokener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class UserTransactionsHelper
{
	private static final int UPDATE_BATCH_SIZE = 100;
	private static final String TABLE_NAME = "user_transactions";

	private final String log_tag;

	private Context context;
	
	private StampDroidDBOpenHelper db_open_helper;
	private SQLiteDatabase db;

	private DateFormat datetime_formatter;

	public UserTransactionsHelper(Context context) throws SQLiteException
	{
		this.context = context;
		this.log_tag = context.getResources().getString(R.string.app_name);
		
		db_open_helper = new StampDroidDBOpenHelper(context);

		db = db_open_helper.getWritableDatabase();

		datetime_formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
	}
	
	public void close()
	{
		db_open_helper.close();
	}

	public void update()
	{
		int offset = 0;
		boolean last_transaction_reached = false;
		BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(context, true);
		
		while(!last_transaction_reached)
		{
			JSONArray new_transactions = bitstamp.userTransactions(offset, UPDATE_BATCH_SIZE, false);
			if(new_transactions.length() == 0) { break; }

			last_transaction_reached = insertTransactions(new_transactions);

			offset += UPDATE_BATCH_SIZE;
		}
	}

	private boolean insertTransactions(JSONArray new_transactions)
	{
		for(int i = 0; i < new_transactions.length(); i++)
		{
			JSONObject transaction;
			try
			{
				transaction = new_transactions.getJSONObject(i);
			}
			catch(JSONException e)
			{
				Log.w(log_tag, "Got invalid transaction at position "+String.valueOf(i)+": "+e.toString());
				continue;
			}

			ContentValues transaction_values = new ContentValues(8);

			try
			{
				transaction_values.put("bitstamp_id", transaction.getInt("id"));
				transaction_values.put("type", transaction.getInt("type"));
				if(!transaction.isNull("order_id"))
				{
					transaction_values.put("order_id", transaction.getInt("order_id"));
				}
				if(transaction.getDouble("usd") != 0.0)
				{
					transaction_values.put("usd_amount", transaction.getDouble("usd"));
				}
				if(transaction.getDouble("btc") != 0.0)
				{
					transaction_values.put("btc_amount", transaction.getDouble("btc"));
				}
				if(transaction.getDouble("btc_usd") != 0.0)
				{
					transaction_values.put("btc_usd_exchange_rate", transaction.getDouble("btc_usd"));
				}
				if(transaction.getDouble("fee") != 0.0)
				{
					transaction_values.put("fee_usd", transaction.getDouble("fee"));
				}
			}
			catch(JSONException e)
			{
				Log.w(log_tag, "Failed to get values from new transaction: "+e.toString());
			}

			try
			{
				Date datetime = (Date)datetime_formatter.parse(transaction.getString("datetime"));
				transaction_values.put("timestamp", datetime.getTime());
			}
			catch(ParseException e)
			{
				Log.w(log_tag, "Failed to parse transaction datetime format: "+e.toString());
			}
			catch(JSONException e)
			{
				Log.w(log_tag, "Failed to get values from new transaction: "+e.toString());
			}

			long row_id = db.insert(TABLE_NAME, null, transaction_values);
			if(row_id < 0)
			{
				return true;
			}
		}

		return false;
	}
}
