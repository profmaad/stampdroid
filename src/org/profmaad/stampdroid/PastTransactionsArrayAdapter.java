package org.profmaad.stampdroid;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.util.Log;
import android.content.res.ColorStateList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class PastTransactionsArrayAdapter extends ArrayAdapter<JSONObject>
{
	private final Context context;
	private final List<JSONObject> past_transactions;

	private final int buy_colour;
	private final int sell_colour;
	private ColorStateList original_colors;

	private final String log_tag;

	static class ViewHolder
	{
		public TextView timestamp_label;
		public TextView type_label;
		public TextView from_label;
		public TextView relation_label;
		public TextView to_label;
		public TextView fee_label;
	}

	public static PastTransactionsArrayAdapter create(Context context, JSONArray past_transactions)
	{
		return create(context, past_transactions, -1);
	}
	public static PastTransactionsArrayAdapter create(Context context, JSONArray past_transactions, int limit)
	{
		List<JSONObject> past_transactions_list = new ArrayList<JSONObject>();
		
		if(limit < 0) { limit = past_transactions.length(); }

		limit = Math.min(limit, past_transactions.length());

		Log.i(context.getResources().getString(R.string.app_name), "Limit for past transactions: "+String.valueOf(limit));

		for(int i = 0; i < limit; i++)
		{
			try
			{
				past_transactions_list.add(past_transactions.getJSONObject(i));
			}
			catch(JSONException e)
			{
				Log.e(context.getResources().getString(R.string.app_name), "Failed to get past transaction "+String.valueOf(i)+" from JSONArray: "+e.toString());
			}
		}

		return new PastTransactionsArrayAdapter(context, past_transactions_list);
	}

	public PastTransactionsArrayAdapter(Context context, List<JSONObject> past_transactions)
	{
		super(context, R.layout.past_transaction_row_layout, past_transactions);
		
		this.context = context;
		this.past_transactions = past_transactions;

		this.buy_colour = context.getResources().getColor(R.color.buy);
		this.sell_colour = context.getResources().getColor(R.color.sell);

		this.log_tag = context.getResources().getString(R.string.app_name);
	}

	// optimizations adapted from http://www.vogella.com/articles/AndroidListView/article.html#adapterperformance_example
	@Override
	public View getView(int position, View convert_view, ViewGroup parent)
	{
		View row_view = convert_view;
		ViewHolder view_holder;

		if(row_view == null)
		{
			LayoutInflater layout_inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
			row_view = layout_inflater.inflate(R.layout.past_transaction_row_layout, parent, false);

			view_holder = new ViewHolder();
			view_holder.timestamp_label = (TextView)row_view.findViewById(R.id.past_transaction_row_timestamp);
			view_holder.type_label = (TextView)row_view.findViewById(R.id.past_transaction_row_type);
			view_holder.from_label = (TextView)row_view.findViewById(R.id.past_transaction_row_from);
			view_holder.relation_label = (TextView)row_view.findViewById(R.id.past_transaction_row_relation);
			view_holder.to_label = (TextView)row_view.findViewById(R.id.past_transaction_row_to);
			view_holder.fee_label = (TextView)row_view.findViewById(R.id.past_transaction_row_fee);

			if(original_colors == null)
			{
				original_colors = view_holder.type_label.getTextColors();
			}
			
			row_view.setTag(view_holder);
		}
		else
		{
			view_holder = (ViewHolder)convert_view.getTag();
		}

		JSONObject past_transaction = past_transactions.get(position);

		try
		{
			view_holder.timestamp_label.setText(past_transaction.getString("datetime"));

			switch(past_transaction.getInt("type"))
			{
			case 0:
				view_holder.type_label.setText("Deposit");
				view_holder.type_label.setTextColor(original_colors);

				view_holder.from_label.setText("");
				view_holder.relation_label.setText("");
				view_holder.to_label.setText(String.format("$ %.2f", past_transaction.getDouble("usd")));

				break;
			case 1:
				view_holder.type_label.setText("Withdrawal");
				view_holder.type_label.setTextColor(original_colors);

				view_holder.from_label.setText("");
				view_holder.relation_label.setText("");
				view_holder.to_label.setText(String.format("$ %.2f", past_transaction.getDouble("usd")));

				break;
			case 2:
				if(past_transaction.getDouble("usd") < 0.0)
				{
					view_holder.type_label.setText("Bought");
					view_holder.type_label.setTextColor(buy_colour);

					view_holder.from_label.setText(String.format("฿ %.8f", past_transaction.getDouble("btc")));
					view_holder.relation_label.setText("for");
					view_holder.to_label.setText(String.format("$ %.2f", -past_transaction.getDouble("usd")));
				}
				else
				{
					view_holder.type_label.setText("Sold");
					view_holder.type_label.setTextColor(sell_colour);

					view_holder.from_label.setText(String.format("฿ %.8f", past_transaction.getDouble("btc")));
					view_holder.relation_label.setText("for");
					view_holder.to_label.setText(String.format("$ %.2f", past_transaction.getDouble("usd")));
				}
				break;
			default:
				view_holder.type_label.setText("Unknown");
				view_holder.type_label.setTextColor(original_colors);

				view_holder.from_label.setText("");
				view_holder.relation_label.setText("");
				view_holder.to_label.setText("");

				break;
			}

			if(past_transaction.getDouble("fee") == 0.0)
			{
				view_holder.fee_label.setText("");
			}
			else
			{
				view_holder.fee_label.setText(String.format("Fee: $ %.2f", past_transaction.getDouble("fee")));
			}
		}
		catch(JSONException e)
		{
			Log.e(log_tag, "Failed to parse past transaction '"+past_transaction.toString()+"': "+e.toString());
		}

		return row_view;
	}
}
