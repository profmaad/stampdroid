package org.profmaad.stampdroid;

import android.app.Activity;
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

public class UserTransactions extends Activity
{
	private String log_tag;

	private ListView transactions_list;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_transactions);

		log_tag = getString(R.string.app_name);

		ActionBar action_bar = getActionBar();
		action_bar.setDisplayHomeAsUpEnabled(true);

		transactions_list = (ListView)findViewById(R.id.transactions_list);
		
		refresh();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();		
		inflater.inflate(R.menu.account_overview_actions, menu); //TODO

		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.action_refresh:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh()
	{
		new AsyncTask<Context, Void, JSONArray>()
		{
			@Override
			protected JSONArray doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0]);
                
				return bitstamp.userTransactions(1000); //HACK
			}
            
			@Override
			protected void onPostExecute(JSONArray transactions)
			{
				updateTransactions(transactions);
			}
		}.execute(this);
	}

	private void updateTransactions(JSONArray transactions)
	{
		Log.i(log_tag, "Got new past transactions: "+transactions.toString());

		PastTransactionsArrayAdapter past_transactions_array_adapter = PastTransactionsArrayAdapter.create(this, transactions);
		
		transactions_list.setAdapter(past_transactions_array_adapter);
	}
}
