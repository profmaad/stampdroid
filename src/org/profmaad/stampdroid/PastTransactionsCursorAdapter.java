package org.profmaad.stampdroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.util.Log;
import android.content.res.ColorStateList;
import android.database.Cursor;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class PastTransactionsCursorAdapter extends CursorAdapter
{
	private final Context context;

	private final int buy_colour;
	private final int sell_colour;
	private ColorStateList original_colors;

	private final String log_tag;

	private DateFormat datetime_formatter;

	static class ViewHolder
	{
		public TextView timestamp_label;
		public TextView type_label;
		public TextView from_label;
		public TextView relation_label;
		public TextView to_label;
		public TextView fee_label;
	}

	public PastTransactionsCursorAdapter(Context context, Cursor cursor)
	{
		super(context, cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
		this.context = context;

		this.buy_colour = context.getResources().getColor(R.color.buy);
		this.sell_colour = context.getResources().getColor(R.color.sell);

		this.log_tag = context.getResources().getString(R.string.app_name);

		this.datetime_formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
	}

	// optimizations adapted from http://www.vogella.com/articles/AndroidListView/article.html#adapterperformance_example
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		LayoutInflater layout_inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
		View row_view = layout_inflater.inflate(R.layout.past_transaction_row_layout, parent, false);

		ViewHolder view_holder = new ViewHolder();
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

		bindView(row_view, context, cursor);

		return row_view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		ViewHolder view_holder = (ViewHolder)view.getTag();

		Date timestamp = new Date(cursor.getLong(cursor.getColumnIndex("timestamp")));		
		view_holder.timestamp_label.setText(datetime_formatter.format(timestamp));

		double usd_amount = cursor.getDouble(cursor.getColumnIndex("usd_amount"));
		double btc_amount = cursor.getDouble(cursor.getColumnIndex("btc_amount"));
		double btc_usd_exchange_rate = cursor.getDouble(cursor.getColumnIndex("btc_usd_exchange_rate"));
		double fee_usd = cursor.getDouble(cursor.getColumnIndex("fee_usd"));
		
		switch(cursor.getInt(cursor.getColumnIndex("type")))
		{
		case 0:
			view_holder.type_label.setText("Deposit");
			view_holder.type_label.setTextColor(original_colors);

			view_holder.from_label.setText("");
			view_holder.relation_label.setText("");

			if(btc_amount == 0.0)
			{
				view_holder.to_label.setText(String.format("$ %.2f", usd_amount));
			}
			else
			{
				view_holder.to_label.setText(String.format("฿ %.8f", btc_amount));
			}

			break;
		case 1:
			view_holder.type_label.setText("Withdrawal");
			view_holder.type_label.setTextColor(original_colors);

			view_holder.from_label.setText("");
			view_holder.relation_label.setText("");

			if(btc_amount == 0.0)
			{
				view_holder.to_label.setText(String.format("$ %.2f", -usd_amount));
			}
			else
			{
				view_holder.to_label.setText(String.format("฿ %.8f", -btc_amount));
			}

			break;
		case 2:
			if(usd_amount < 0.0)
			{
				view_holder.type_label.setText("Bought");
				view_holder.type_label.setTextColor(buy_colour);

				view_holder.from_label.setText(String.format("฿ %.8f", btc_amount));
				view_holder.relation_label.setText("for");
				view_holder.to_label.setText(String.format("$ %.2f", -usd_amount));
			}
			else
			{
				view_holder.type_label.setText("Sold");
				view_holder.type_label.setTextColor(sell_colour);

				view_holder.from_label.setText(String.format("฿ %.8f", -btc_amount));
				view_holder.relation_label.setText("for");
				view_holder.to_label.setText(String.format("$ %.2f", usd_amount));
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

		if(fee_usd == 0.0)
		{
			view_holder.fee_label.setText("");
		}
		else
		{
			view_holder.fee_label.setText(String.format("Fee: $ %.2f", fee_usd));
		}
	}
}
