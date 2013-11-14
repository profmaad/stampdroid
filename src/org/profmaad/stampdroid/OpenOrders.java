package org.profmaad.stampdroid;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class OpenOrders extends ListActivity
{
	private String log_tag;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_orders);

		log_tag = getString(R.string.app_name);

		ActionBar action_bar = getActionBar();
		action_bar.setDisplayHomeAsUpEnabled(true);

		refresh();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();		
		inflater.inflate(R.menu.open_orders_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.action_refresh:
			refresh(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh()
	{
		refresh(false);
	}
	private void refresh(boolean bypass_cache)
	{
		final boolean bypass_cache_async = bypass_cache;
		
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
	}

	private void updateOpenOrders(JSONArray open_orders)
	{
		Log.i(log_tag, "Got new open orders: "+open_orders.toString());

		OpenOrdersArrayAdapter open_orders_array_adapter = OpenOrdersArrayAdapter.create(this, open_orders);
		
		setListAdapter(open_orders_array_adapter);
	}
}
