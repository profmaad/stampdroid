package org.profmaad.stampdroid;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageButton;
import android.view.View;
import android.preference.PreferenceManager;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AccountSettings extends Activity
{
	private final static String SCAN_INTENT_EXTRA = "org.profmaad.stampdroid.scan_type";
	private String log_tag;

	private EditText client_id_edit;  // 0
	private EditText api_key_edit;    // 1
	private EditText api_secret_edit; // 2

	private TextView help_label;
	private TextView test_result_label;

	private ImageButton client_id_scan_button;
	private ImageButton api_key_scan_button;
	private ImageButton api_secret_scan_button;
	private int current_scan_type; // not really a pretty solution, but the barcode scanner API doesn't really allow for better approaches

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings);

		log_tag = getString(R.string.app_name);

		client_id_edit = (EditText)findViewById(R.id.account_settings_client_id);
		api_key_edit = (EditText)findViewById(R.id.account_settings_api_key);
		api_secret_edit = (EditText)findViewById(R.id.account_settings_api_secret);

		help_label = (TextView)findViewById(R.id.account_settings_help_label);
		test_result_label = (TextView)findViewById(R.id.account_settings_test_result);

		client_id_scan_button = (ImageButton)findViewById(R.id.account_settings_client_id_scan_button);
		api_key_scan_button = (ImageButton)findViewById(R.id.account_settings_api_key_scan_button);
		api_secret_scan_button = (ImageButton)findViewById(R.id.account_settings_api_secret_scan_button);

		Intent starting_intent = getIntent();
		if(starting_intent != null && starting_intent.hasExtra("org.profmaad.stampdroid.account_settings_help"))
		{
			help_label.setText(starting_intent.getStringExtra("org.profmaad.stampdroid.account_settings_help"));
		}
		
		try
		{
			load();
		}
		catch(Exception e)
		{
			Log.e(log_tag, "Failed to load keys", e);
			e.printStackTrace();
		}
    }

	public void load() throws Exception
	{
		AccountSettingsHelper helper = new AccountSettingsHelper(this);

		client_id_edit.setText(helper.getClientID());
		api_key_edit.setText(helper.getAPIKey());
		api_secret_edit.setText(helper.getAPISecret());
	}

	public void save(View view) throws Exception
	{
		AccountSettingsHelper helper = new AccountSettingsHelper(this);

		helper.setClientID(client_id_edit.getText().toString());
		helper.setAPIKey(api_key_edit.getText().toString());
		helper.setAPISecret(api_secret_edit.getText().toString());

		finish();
	}
	public void test(View view)
	{
		new AsyncTask<Context, Void, Boolean>()
		{
			@Override
			protected Boolean doInBackground(Context... params)
			{
				BitstampWebserviceConsumer bitstamp = new BitstampWebserviceConsumer(params[0]);

				JSONObject balance = bitstamp.balance();
				return (balance.has("usd_balance") && balance.has("btc_balance"));
			}
            
			@Override
			protected void onPostExecute(Boolean success)
			{
				test_result_label.setText(getString(success ? R.string.api_access_test_success : R.string.api_access_test_failure));
				test_result_label.setTextColor(success ? getResources().getColor(R.color.success) : getResources().getColor(R.color.failure));
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
	}

	public void scan(View view)
	{
		IntentIntegrator integrator = new IntentIntegrator(this);

		current_scan_type = -1;
		if(view == client_id_scan_button)
		{
			current_scan_type = 0;
		}
		else if(view == api_key_scan_button)
		{
			current_scan_type = 1;
		}
		else if(view == api_secret_scan_button)
		{
			current_scan_type = 2;
		}

		integrator.initiateScan();		
	}

	public void onActivityResult(int request_code, int result_code, Intent intent)
	{
		IntentResult scan_result = IntentIntegrator.parseActivityResult(request_code, result_code, intent);

		if(scan_result != null && intent != null)
		{
			String contents = scan_result.getContents();

			switch(current_scan_type)
			{
			case 0:
				client_id_edit.setText(contents);
				break;
			case 1:
				api_key_edit.setText(contents);
				break;
			case 2:
				api_secret_edit.setText(contents);
				break;
			}
		}
	}
}
