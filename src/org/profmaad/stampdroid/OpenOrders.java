package org.profmaad.stampdroid;

import java.util.Arrays;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
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
				mode.setTitle(String.format("%d selected", getListView().getCheckedItemCount()));

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
				Log.i(log_tag, "CAB ITEM CLICKED: "+item.getItemId());
				switch(item.getItemId())
				{
				case R.id.action_edit_order:
					startEditOrderActivity();
					mode.finish();
					return true;
				case R.id.action_cancel_order:
					cancelOrders(getListView().getCheckedItemIds());
					mode.finish();
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
		Log.i(log_tag, "Canceling orders: "+Arrays.toString(ids));

		final long async_ids[] = ids;

		new AsyncTask<Context, Void, JSONObject>()
		{
			@Override
			protected JSONObject doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0], true);

				JSONObject result = null;
				for(long order_id : async_ids)
				{
					result = bitstamp.cancelOrder(order_id);
					try
					{
						if(result.isNull("success") || result.getInt("success") != 1)
						{
							// we stop after the first failed cancelation
							break;
						}
					}
					catch(JSONException e)
					{
						break;
					}
				}
				
				return result;
			}
            
			@Override
			protected void onPostExecute(JSONObject result)
			{
				displayCancelOrderResult(result);
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
	}
	private void displayCancelOrderResult(JSONObject result)
	{
		try
		{
			if(!result.isNull("success") && result.getInt("success") == 1)
			{
				Toast.makeText(this, "Orders canceled successfully", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(this, "Failed to cancel some orders: "+result.getString("error"), Toast.LENGTH_LONG).show();
			}
		}
		catch(JSONException e)
		{
			Log.e(log_tag, "Invalid reply from order cancelation", e);
			Toast.makeText(this, "Failed to cancel some orders: "+e.toString(), Toast.LENGTH_LONG).show();			
		}

		refresh(true);
	}

	public void startAddOrderActivity()
	{
		Intent intent = new Intent(this, AddOrder.class);
		startActivityForResult(intent, 0);
	}
	public void startEditOrderActivity()
	{
		SparseBooleanArray checked_items = getListView().getCheckedItemPositions();
		Log.i(log_tag, "SELECTED ITEMS: "+checked_items.toString());
		Log.i(log_tag, "SELECTED ITEMS SIZE: "+String.valueOf(checked_items.size()));
		int first_selected_item_position = -1;

		for(int i = 0; i < checked_items.size(); i++)
		{
			Log.i(log_tag, "SELECTED ITEM AT "+String.valueOf(checked_items.keyAt(i))+": "+String.valueOf(checked_items.valueAt(i)));
			if(checked_items.valueAt(i))
			{
				first_selected_item_position = checked_items.keyAt(i);
				break;
			}
		}
		
		if(first_selected_item_position < 0)
		{
			Log.i(log_tag, "NO VALID ITEM SELECTED");
			return;
		}
		
		Intent intent = new Intent(this, AddOrder.class);

		JSONObject open_order = (JSONObject)(getListAdapter().getItem(first_selected_item_position));

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
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
	}

	private void updateOpenOrders(JSONArray open_orders)
	{
		Log.i(log_tag, "Got new open orders: "+open_orders.toString());

		OpenOrdersArrayAdapter open_orders_array_adapter = OpenOrdersArrayAdapter.create(this, open_orders);
		
		setListAdapter(open_orders_array_adapter);
	}
}
