package org.profmaad.stampdroid;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.ActionMode;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class OpenOrders extends ListActivity
{
	private String log_tag;

	private static final String EXTRA_OPEN_ORDER_JSON = "org.profmaad.stampdroid.EXTRA_OPEN_ORDER_JSON";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_orders);

		log_tag = getString(R.string.app_name);

		ActionBar action_bar = getActionBar();
		action_bar.setDisplayHomeAsUpEnabled(true);

		setupSelectionHandler();

		refresh();
    }

	private void setupSelectionHandler()
	{
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		getListView().setMultiChoiceModeListener(new MultiChoiceModeListener()
		{
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
			{
				mode.setTitle(String.format("%i selected", getListView().getCheckedItemCount()));

				mode.getMenu().findItem(R.id.action_edit_order).setVisible(getListView().getCheckedItemCount() == 1);
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.open_orders_context, menu);
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu)
			{
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item)
			{
				switch(item.getItemId())
				{
				case R.id.action_edit_order:
					startEditOrderActivity();
					return true;
				case R.id.action_cancel_order:
					cancelOrders(getListView().getCheckedItemIds());
					return true;
				default:
					return false;
				}
			}

			@Override
			public void onDestroyActionMode(ActionMode mode)
			{
				// TODO
			}
		});
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
		case R.id.action_add_order:
			startAddOrderActivity();
			return true;
		case R.id.action_refresh:
			refresh(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void cancelOrders(long ids[])
	{
	}

	public void startAddOrderActivity()
	{
		Intent intent = new Intent(this, AddOrder.class);
		startActivityForResult(intent, 0);
	}
	public void startEditOrderActivity()
	{
		if(getListView().getCheckedItemPosition() == ListView.INVALID_POSITION)
		{
			return;
		}
		
		Intent intent = new Intent(this, AddOrder.class);

		JSONObject open_order = (JSONObject)(getListAdapter().getItem(getListView().getCheckedItemPosition()));

		intent.putExtra(EXTRA_OPEN_ORDER_JSON, open_order.toString());
		
		startActivityForResult(intent, 0);
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
