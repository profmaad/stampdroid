package org.profmaad.stampdroid;

import java.security.KeyStore;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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

		client_id_scan_button = (ImageButton)findViewById(R.id.account_settings_client_id_scan_button);
		api_key_scan_button = (ImageButton)findViewById(R.id.account_settings_api_key_scan_button);
		api_secret_scan_button = (ImageButton)findViewById(R.id.account_settings_api_secret_scan_button);
		
		try
		{
			load();
		}
		catch(Exception e)
		{
			Log.e(log_tag, "Failed to load keys: "+e.toString());
			e.printStackTrace();
		}
    }

	public void load() throws Exception
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		client_id_edit.setText("");
		if(preferences.contains("org.profmaad.stampdroid.client_id"))
		{
			client_id_edit.setHint(R.string.hidden);
		}
		else
		{
			client_id_edit.setHint("");
		}

		api_key_edit.setText("");
		if(preferences.contains("org.profmaad.stampdroid.api_key"))
		{
			api_key_edit.setHint(R.string.hidden);
		}
		else
		{
			api_key_edit.setHint("");
		}

		api_secret_edit.setText("");
		if(preferences.contains("org.profmaad.stampdroid.api_secret"))
		{
			api_secret_edit.setHint(R.string.hidden);
		}
		else
		{
			api_secret_edit.setHint("");
		}
	}

	public void save(View view)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		preferences.edit()
			.putString("org.profmaad.stampdroid.client_id", client_id_edit.getText().toString())
			.putString("org.profmaad.stampdroid.api_key", api_key_edit.getText().toString())
			.putString("org.profmaad.stampdroid.api_secret", api_secret_edit.getText().toString())
			.commit();

		finish();
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
