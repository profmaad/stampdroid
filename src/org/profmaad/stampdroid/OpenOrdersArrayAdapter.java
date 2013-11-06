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

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class OpenOrdersArrayAdapter extends ArrayAdapter<JSONObject>
{
	private final Context context;
	private final List<JSONObject> open_orders;

	private final int buy_colour;
	private final int sell_colour;

	private final String log_tag;

	static class ViewHolder
	{
		public TextView timestamp_label;
		public TextView type_label;
		public TextView amount_label;
		public TextView price_label;
	}

	public static OpenOrdersArrayAdapter create(Context context, JSONArray open_orders)
	{
		List<JSONObject> open_orders_list = new ArrayList<JSONObject>();
		
		for(int i = 0; i < open_orders.length(); i++)
		{
			try
			{
				open_orders_list.add(open_orders.getJSONObject(i));
			}
			catch(JSONException e)
			{
				Log.e(context.getResources().getString(R.string.app_name), "Failed to get open order "+String.valueOf(i)+" from JSONArray: "+e.toString());
			}
		}

		return new OpenOrdersArrayAdapter(context, open_orders_list);
	}

	public OpenOrdersArrayAdapter(Context context, List<JSONObject> open_orders)
	{
		super(context, R.layout.open_order_row_layout, open_orders);
		
		this.context = context;
		this.open_orders = open_orders;

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
			row_view = layout_inflater.inflate(R.layout.open_order_row_layout, parent, false);

			Log.e(log_tag, "Created new row layout: "+row_view.toString());

			view_holder = new ViewHolder();
			view_holder.timestamp_label = (TextView)row_view.findViewById(R.id.open_order_row_timestamp);
			view_holder.type_label = (TextView)row_view.findViewById(R.id.open_order_row_type);
			view_holder.amount_label = (TextView)row_view.findViewById(R.id.open_order_row_amount);
			view_holder.price_label = (TextView)row_view.findViewById(R.id.open_order_row_price);

			Log.e(log_tag, "Created new view holder: "+view_holder.toString());
			
			row_view.setTag(view_holder);
		}
		else
		{
			view_holder = (ViewHolder)convert_view.getTag();
		}

		JSONObject open_order = open_orders.get(position);

		try
		{
			Log.e(log_tag, "View holder: "+view_holder.toString());
			Log.e(log_tag, "\t"+view_holder.timestamp_label.toString());
			Log.e(log_tag, "\t"+view_holder.type_label.toString());
			Log.e(log_tag, "\t"+view_holder.amount_label.toString());
			Log.e(log_tag, "\t"+view_holder.price_label.toString());
			view_holder.timestamp_label.setText(open_order.getString("datetime"));

			view_holder.type_label.setText((open_order.getInt("type") == 1) ? "Sell" : "Buy");
			view_holder.type_label.setTextColor((open_order.getInt("type") == 1) ? sell_colour : buy_colour);

			view_holder.amount_label.setText(String.format("฿ %.8f", open_order.getDouble("amount")));
			view_holder.price_label.setText(String.format("$ %.2f", open_order.getDouble("price")));
		}
		catch(JSONException e)
		{
			Log.e(log_tag, "Failed to parse open order '"+open_order.toString()+"': "+e.toString());
		}

		return row_view;
	}
}
