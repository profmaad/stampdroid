package org.profmaad.stampdroid;

import java.security.KeyStore;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

public class AccountSettings extends Activity
{
	private String log_tag;

	private EditText client_id_edit;
	private EditText api_key_edit;
	private EditText api_secret_edit;

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
		KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
		keystore.load(null);

		KeyStore.SecretKeyEntry client_id_entry = (KeyStore.SecretKeyEntry)keystore.getEntry("org.profmaad.stampdroid.client_id", null);
		KeyStore.SecretKeyEntry api_key_entry = (KeyStore.SecretKeyEntry)keystore.getEntry("org.profmaad.stampdroid.api_key", null);
		KeyStore.SecretKeyEntry api_secret_entry = (KeyStore.SecretKeyEntry)keystore.getEntry("org.profmaad.stampdroid.api_secret", null);

		if(client_id_entry != null)
		{
			String client_id = new String(client_id_entry.getSecretKey().getEncoded(), "UTF-8");
			client_id_edit.setText(client_id);
		}
		if(api_key_entry != null)
		{
			String api_key = new String(api_key_entry.getSecretKey().getEncoded(), "UTF-8");
			api_key_edit.setText(api_key);
		}
		if(api_secret_entry != null)
		{
			String api_secret = new String(api_secret_entry.getSecretKey().getEncoded(), "UTF-8");
			api_secret_edit.setText(api_secret);
		}
	}

	public void save(View view)
	{
		String client_id = client_id_edit.getText().toString();
		String api_key = api_key_edit.getText().toString();
		String api_secret = api_secret_edit.getText().toString();

		try
		{
			KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
			keystore.load(null);

			keystore.setEntry("org.profmaad.stampdroid.client_id", new KeyStore.SecretKeyEntry(new SecretKeySpec(client_id.getBytes("UTF-8"), "BitStampAPI")), null);
			keystore.setEntry("org.profmaad.stampdroid.api_key", new KeyStore.SecretKeyEntry(new SecretKeySpec(api_key.getBytes("UTF-8"), "BitStampAPI")), null);
			keystore.setEntry("org.profmaad.stampdroid.api_secret", new KeyStore.SecretKeyEntry(new SecretKeySpec(api_secret.getBytes("UTF-8"), "BitStampAPI")), null);

			keystore.store(null);
		}
		catch(Exception e)
		{
			Log.e(log_tag, "Failed to store keys: "+e.toString());
			e.printStackTrace();
		}

		finishActivity(0);
	}
}
