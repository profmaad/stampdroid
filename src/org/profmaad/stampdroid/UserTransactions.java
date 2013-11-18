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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserTransactions extends ListActivity
{
	private String log_tag;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_transactions);

		log_tag = getString(R.string.app_name);

		ActionBar action_bar = getActionBar();
		action_bar.setDisplayHomeAsUpEnabled(true);

		UserTransactionsHelper helper = new UserTransactionsHelper(this);
		Cursor transactions_current_cursor = helper.getDatabase().query(helper.getTableName(), null, null, null, null, null, "timestamp DESC");
		updateTransactions(transactions_current_cursor);

		refresh();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();		
		inflater.inflate(R.menu.user_transactions_actions, menu);

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
		new AsyncTask<Context, Void, Cursor>()
		{
			@Override
			protected Cursor doInBackground(Context... params)
			{
				UserTransactionsHelper helper = new UserTransactionsHelper(params[0]);

				try
				{
					helper.update();
				}
				catch(Exception e)
				{
					Log.e(log_tag, "Failed to update user transactions table: "+e.toString());
				}

				return helper.getDatabase().query(helper.getTableName(), null, null, null, null, null, "timestamp DESC", null);
			}
            
			@Override
			protected void onPostExecute(Cursor transactions_cursor)
			{
				updateTransactions(transactions_cursor);
			}
		}.execute(this);
	}

	private void updateTransactions(Cursor transactions_cursor)
	{
		Log.i(log_tag, "Updating transactions cursor: "+transactions_cursor.toString());

		PastTransactionsCursorAdapter transactions_cursor_adapter = new PastTransactionsCursorAdapter(this, transactions_cursor);
		
		setListAdapter(transactions_cursor_adapter);
	}
}
