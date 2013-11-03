package org.profmaad.stampdroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONTokener;
import org.json.JSONObject;

public class AccountOverview extends Activity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		new AsyncTask<Context, Void, String>()
		{
			@Override
			protected String doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0]);
				
				bitstamp.ticker();
				return bitstamp.balance();			
			}
			
			@Override
			protected void onPostExecute(String result)
			{
				double usd_balance = 0.0;
				double btc_balance = 0.0;

				try
				{
					JSONObject balance_object = (JSONObject)new JSONTokener(result).nextValue();

					usd_balance = balance_object.getDouble("usd_balance");
					btc_balance = balance_object.getDouble("btc_balance");
				}
				catch(Exception e)
				{
				}

				TextView text = (TextView)findViewById(R.id.text);
				text.setText(new StringBuilder().append("Current USD balance: ").append(usd_balance).append('\n').append("Current BTC balance: ").append(btc_balance).toString());
			}
		}.execute(this);
    }
}
