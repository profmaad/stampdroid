package org.profmaad.stampdroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class AccountOverview extends Activity
{
	private String log_tag;

	private TextView ticker_last_label;
	private TextView ticker_volume_label;

	private TextView ticker_bid_label;
	private TextView ticker_ask_label;

	private TextView ticker_low_label;
	private TextView ticker_high_label;

	private TextView balance_usd_total_label;
	private TextView balance_usd_reserved_label;
	private TextView balance_btc_total_label;
	private TextView balance_btc_reserved_label;

	private ListView open_orders_list;
	private ListView past_transactions_list;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_overview);

		log_tag = getString(R.string.app_name);

		ticker_last_label = (TextView)findViewById(R.id.ticker_last);
		ticker_volume_label = (TextView)findViewById(R.id.ticker_volume);

		ticker_bid_label = (TextView)findViewById(R.id.ticker_bid);
		ticker_ask_label = (TextView)findViewById(R.id.ticker_ask);

		ticker_low_label = (TextView)findViewById(R.id.ticker_low);
		ticker_high_label = (TextView)findViewById(R.id.ticker_high);

		balance_usd_total_label = (TextView)findViewById(R.id.balance_usd_total);
		balance_usd_reserved_label = (TextView)findViewById(R.id.balance_usd_reserved);
		balance_btc_total_label = (TextView)findViewById(R.id.balance_btc_total);
		balance_btc_reserved_label = (TextView)findViewById(R.id.balance_btc_reserved);

		open_orders_list = (ListView)findViewById(R.id.overview_open_orders_list);
		past_transactions_list = (ListView)findViewById(R.id.overview_past_transactions_list);
		
		BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(this);

		if(bitstamp.isReady())
		{
			refresh();
		}
		else
		{
			openAccountSettings(getString(R.string.account_settings_first_start));
		}
    }

	@Override
	protected void onResume()
	{
		super.onResume();

		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();		
		inflater.inflate(R.menu.account_overview_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.action_refresh:
			refresh(true);
			return true;
		case R.id.action_account_settings:
			openAccountSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void openAccountSettings()
	{
		openAccountSettings("");
	}
	public void openAccountSettings(String help_text)
	{
		Intent intent = new Intent(this, AccountSettings.class);
		intent.putExtra("org.profmaad.stampdroid.account_settings_help", help_text);
		startActivityForResult(intent, 0);
	}

	public void openOpenOrdersList(View view)
	{
		Intent intent = new Intent(this, OpenOrders.class);
		startActivity(intent);
	}
	public void openPastTransactionsList(View view)
	{
		Intent intent = new Intent(this, UserTransactions.class);
		startActivity(intent);
	}

	private void refresh()
	{
		refresh(false);
	}
	private void refresh(boolean bypass_cache)
	{
		final boolean bypass_cache_async = bypass_cache;

		new AsyncTask<Context, Void, JSONObject>()
		{
			@Override
			protected JSONObject doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0], bypass_cache_async);

				return bitstamp.ticker();
			}
            
			@Override
			protected void onPostExecute(JSONObject ticker)
			{
				updateTicker(ticker);
			}
		}.execute(this);
		
		new AsyncTask<Context, Void, JSONObject>()
		{
			@Override
			protected JSONObject doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0], bypass_cache_async);
                
				return bitstamp.balance();
			}
            
			@Override
			protected void onPostExecute(JSONObject balance)
			{
				updateBalance(balance);
			}
		}.execute(this);
		
		new AsyncTask<Context, Void, JSONArray>()
		{
			@Override
			protected JSONArray doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0], bypass_cache_async);
                
				return bitstamp.openOrders();
			}
            
			@Override
			protected void onPostExecute(JSONArray open_orders)
			{
				updateOpenOrders(open_orders);
			}
		}.execute(this);
		
		new AsyncTask<Context, Void, JSONArray>()
		{
			@Override
			protected JSONArray doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0], bypass_cache_async);
                
				return bitstamp.userTransactions(5);
			}
            
			@Override
			protected void onPostExecute(JSONArray past_transactions)
			{
				updatePastTransactions(past_transactions);
			}
		}.execute(this);
	}

	private void updateTicker(JSONObject ticker)
	{
		try
		{
			double last = ticker.getDouble("last");
			double volume = ticker.getDouble("volume");

			double bid = ticker.getDouble("bid");
			double ask = ticker.getDouble("ask");

			double low = ticker.getDouble("low");
			double high = ticker.getDouble("high");

			ticker_last_label.setText(String.format("%.2f", last));
			ticker_volume_label.setText(String.format("%.2f", volume));

			ticker_bid_label.setText(String.format("%.2f", bid));
			ticker_ask_label.setText(String.format("%.2f", ask));

			ticker_low_label.setText(String.format("%.2f", low));
			ticker_high_label.setText(String.format("%.2f", high));
		}
		catch(JSONException e)
		{
			Log.e(log_tag, "Failed to update ticker: "+e.toString());

			Toast.makeText(this, "Failed to update ticker", Toast.LENGTH_SHORT);
		}
	}
	private void updateBalance(JSONObject balance)
	{
		try
		{
			double usd_total = balance.getDouble("usd_balance");
			double usd_reserved = balance.getDouble("usd_reserved");

			double btc_total = balance.getDouble("btc_balance");
			double btc_reserved = balance.getDouble("btc_reserved");

			balance_usd_total_label.setText(String.format("$ %.2f", usd_total));
			balance_usd_reserved_label.setText(String.format("- $ %.2f reserved", usd_reserved));

			balance_btc_total_label.setText(String.format("฿ %.2f", btc_total));
			balance_btc_reserved_label.setText(String.format("- ฿ %.2f reserved", btc_reserved));
		}
		catch(JSONException e)
		{
			Log.e(log_tag, "Failed to update account balance: "+e.toString());

			Toast.makeText(this, "Failed to update account balance", Toast.LENGTH_SHORT);
		}
	}
	
	private void updateOpenOrders(JSONArray open_orders)
	{
		Log.i(log_tag, "Got new open orders: "+open_orders.toString());

		OpenOrdersArrayAdapter open_orders_array_adapter = OpenOrdersArrayAdapter.create(this, open_orders);
		
		open_orders_list.setAdapter(open_orders_array_adapter);
	}
	private void updatePastTransactions(JSONArray past_transactions)
	{
		Log.i(log_tag, "Got new past transactions: "+past_transactions.toString());

		PastTransactionsArrayAdapter past_transactions_array_adapter = PastTransactionsArrayAdapter.create(this, past_transactions);
		
		past_transactions_list.setAdapter(past_transactions_array_adapter);
	}
}
