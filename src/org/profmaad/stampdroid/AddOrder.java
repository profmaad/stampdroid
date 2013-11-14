package org.profmaad.stampdroid;

import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.text.TextWatcher;
import android.text.Editable;

import org.json.JSONObject;
import org.json.JSONException;

public class AddOrder extends Activity
{
	private static final int TYPE_BUY = 0;
	private static final int TYPE_SELL = 1;

	private int insufficient_funds_colour;
	private ColorStateList total_original_colors;
	private ColorStateList amount_original_colors;
	
	private String log_tag;

	private RadioGroup order_type_radiogroup;

	private TextView funds_label;

	private EditText amount_edit;
	private EditText price_edit;

	private TextView fee_label;
	private TextView total_label;

	private Button add_order_button;

	private double fee_rate;
	private double usd_available;
	private double btc_available;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_order);

		log_tag = getString(R.string.app_name);
		insufficient_funds_colour = getResources().getColor(R.color.insufficient_funds);

		order_type_radiogroup = (RadioGroup)findViewById(R.id.add_order_type);

		funds_label = (TextView)findViewById(R.id.add_order_funds_label);

		amount_edit = (EditText)findViewById(R.id.add_order_amount);
		price_edit = (EditText)findViewById(R.id.add_order_price);

		fee_label = (TextView)findViewById(R.id.add_order_fee);
		total_label = (TextView)findViewById(R.id.add_order_total);

		add_order_button = (Button)findViewById(R.id.add_order_button);

		amount_original_colors = amount_edit.getTextColors();
		total_original_colors = total_label.getTextColors();

		amount_edit.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s)
			{
				refreshValues();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		price_edit.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s)
			{
				refreshValues();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		order_type_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup group, int checked_id)
			{
				refreshValues();
			}
		});

		refreshBalance();
    }

	private int getOrderType()
	{
		switch(order_type_radiogroup.getCheckedRadioButtonId())
		{
		case R.id.add_order_type_buy:
			return TYPE_BUY;
		case R.id.add_order_type_sell:
			return TYPE_SELL;
		}

		return -1;			
	}

	private void refreshBalance()
	{
		new AsyncTask<Context, Void, JSONObject>()
		{
			@Override
			protected JSONObject doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0], true);
                
				return bitstamp.balance();
			}
            
			@Override
			protected void onPostExecute(JSONObject balance)
			{
				updateBalance(balance);
			}
		}.execute(this);
	}
	private void updateBalance(JSONObject balance)
	{
		try
		{
			usd_available = balance.getDouble("usd_available");
			btc_available = balance.getDouble("btc_available");

			fee_rate = balance.getDouble("fee")/100.0;
		}
		catch(JSONException e)
		{
			Log.e(log_tag, "Failed to update balance: "+e.toString());
			Toast.makeText(this, "Failed to retrieve balance", Toast.LENGTH_LONG).show();
			finish();
		}

		updateFundsLabel();
	}
	private void updateFundsLabel()
	{
		switch(getOrderType())
		{
		case TYPE_BUY:
			funds_label.setText(String.format("Available: $ %.2f", usd_available));
			break;
		case TYPE_SELL:
			funds_label.setText(String.format("Available: ฿ %.8f", btc_available));
			break;			
		}
	}

	private void refreshValues()
	{
		double amount = 0.0;
		double price = 0.0;

		try
		{
			amount = Double.parseDouble(amount_edit.getText().toString());
			price = Double.parseDouble(price_edit.getText().toString());
		}
		catch(NumberFormatException e)
		{
			Log.w(log_tag, "Failed to parse order values: "+e.toString());
		}

		double total = amount*price;
		double fee = total*fee_rate;
		switch(getOrderType())
		{
		case TYPE_BUY:
			total += fee;
			break;
		case TYPE_SELL:
			total -= fee;
			break;
		}

		fee_label.setText(String.format("$ %.02f (%.02f%%)", fee, fee_rate*100.0));
		total_label.setText(String.format("$ %.02f", total));

		if(total > usd_available && getOrderType() == TYPE_BUY)
		{
			total_label.setTextColor(insufficient_funds_colour);
			amount_edit.setTextColor(amount_original_colors);
			add_order_button.setEnabled(false);
		}
		else if(amount > btc_available && getOrderType() == TYPE_SELL)
		{
			total_label.setTextColor(total_original_colors);
			amount_edit.setTextColor(insufficient_funds_colour);
			add_order_button.setEnabled(false);
		}
		else
		{
			total_label.setTextColor(total_original_colors);
			amount_edit.setTextColor(amount_original_colors);
			add_order_button.setEnabled(true);
		}
	}
			
	public void addOrder(View view)
	{
		Toast.makeText(this, "Order creation not yet implemented", Toast.LENGTH_SHORT).show();
		
		finish();
	}
}