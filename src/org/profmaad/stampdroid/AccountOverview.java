package org.profmaad.stampdroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

		new AsyncTask<Context, Void, JSONObject>()
		{
			@Override
			protected JSONObject doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0]);
				
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
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0]);
				
				return bitstamp.balance();
			}
			
			@Override
			protected void onPostExecute(JSONObject balance)
			{
				updateBalance(balance);
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
}
