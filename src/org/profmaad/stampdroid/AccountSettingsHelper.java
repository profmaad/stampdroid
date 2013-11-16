package org.profmaad.stampdroid;

import java.security.KeyStore;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import javax.security.auth.x500.X500Principal;
import javax.crypto.Cipher;

import java.util.Calendar;
import java.util.GregorianCalendar;

import java.math.BigInteger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.preference.PreferenceManager;
import android.security.KeyPairGeneratorSpec;

public class AccountSettingsHelper
{
	private final static String KEYSTORE_KEY_ALIAS = "org.profmaad.stampdroid.api_secrets_key";
	private String log_tag;

	private Context context;

	public AccountSettingsHelper(Context context)
	{
		this.context = context;
		
		log_tag = context.getString(R.string.app_name);

		try
		{
			if(!isKeyPresent())
			{
				generateKey();
			}
		}
		catch(Exception e)
		{
			Log.e(log_tag, "Failed to access key from keystore: "+e.toString());
			e.printStackTrace();
		}
	}

	public String getClientID() throws Exception
	{
		return getValue("org.profmaad.stampdroid.client_id");
	}

	public String getAPIKey() throws Exception
	{
		return getValue("org.profmaad.stampdroid.api_key");
	}

	public String getAPISecret() throws Exception
	{
		return getValue("org.profmaad.stampdroid.api_secret");
	}

	public void setClientID(String client_id) throws Exception
	{
		setValue("org.profmaad.stampdroid.client_id", client_id);
	}

	public void setAPIKey(String api_key) throws Exception
	{
		setValue("org.profmaad.stampdroid.api_key", api_key);
	}

	public void setAPISecret(String api_secret) throws Exception
	{
		setValue("org.profmaad.stampdroid.api_secret", api_secret);
	}

	private String getValue(String key) throws Exception
	{
		KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
		keystore.load(null);

		if(!keystore.containsAlias(KEYSTORE_KEY_ALIAS))
		{
			throw new Exception("no key present");
		}

		KeyStore.PrivateKeyEntry key_entry = (KeyStore.PrivateKeyEntry)keystore.getEntry(KEYSTORE_KEY_ALIAS, null);
		RSAPrivateKey private_key = (RSAPrivateKey)key_entry.getPrivateKey();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(!preferences.contains(key)) { return ""; }

		Cipher rsa_cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		rsa_cipher.init(Cipher.DECRYPT_MODE, private_key);

		byte value_encrypted[] = preferences.getString(key, "").getBytes("UTF-8");
		byte value_decrypted_raw[] = new byte[rsa_cipher.getOutputSize(value_encrypted.length)];

		Log.i(log_tag, "ENCRYPTED SIZE: "+value_encrypted.length);
		Log.i(log_tag, "DECRYPTED SIZE: "+value_decrypted_raw.length);

		int offset = 0;
		int output_offset = 0;
		while(offset < value_encrypted.length)
		{
			int block_size = rsa_cipher.getBlockSize();
			if(offset + block_size > value_encrypted.length)
			{
				block_size = value_encrypted.length - offset;
			}

			output_offset = rsa_cipher.update(value_encrypted, offset, block_size, value_decrypted_raw, output_offset);
			Log.i(log_tag, String.format("Offset: %d, block size: %d, output offset: %d", offset, block_size, output_offset));

			offset += block_size;
		}
		Log.i(log_tag, String.format("output offset: %d", output_offset));
		rsa_cipher.doFinal(value_decrypted_raw, output_offset);

		String value = new String(value_decrypted_raw, "UTF-8");

		return value;
	}

	private void setValue(String key, String value) throws Exception
	{
		KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
		keystore.load(null);

		if(!keystore.containsAlias(KEYSTORE_KEY_ALIAS))
		{
			throw new Exception("no key present");
		}

		KeyStore.PrivateKeyEntry key_entry = (KeyStore.PrivateKeyEntry)keystore.getEntry(KEYSTORE_KEY_ALIAS, null);
		RSAPublicKey public_key = (RSAPublicKey)key_entry.getCertificate().getPublicKey();
		Log.i(log_tag, "Public key: "+public_key.toString());

		Cipher rsa_cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

		Log.i(log_tag, "Value: "+value);
		byte value_decrypted[] = value.getBytes("UTF-8");
		
		rsa_cipher.init(Cipher.ENCRYPT_MODE, public_key);

		byte value_encrypted_raw[] = new byte[rsa_cipher.getOutputSize(value_decrypted.length)];

		Log.i(log_tag, "DECRYPTED SIZE: "+value_decrypted.length);
		Log.i(log_tag, "ENCRYPTED SIZE: "+value_encrypted_raw.length);

		int offset = 0;
		int output_offset = 0;
		while(offset < value_decrypted.length)
		{
			int block_size = rsa_cipher.getBlockSize();
			if(offset + block_size > value_decrypted.length)
			{
				block_size = value_decrypted.length - offset;
			}

			output_offset += rsa_cipher.update(value_decrypted, offset, block_size, value_encrypted_raw, output_offset);
			Log.i(log_tag, String.format("Offset: %d, block size: %d, output offset: %d", offset, block_size, output_offset));

			offset += block_size;
		}
		Log.i(log_tag, String.format("output offset: %d", output_offset));
		rsa_cipher.doFinal(value_encrypted_raw, output_offset);
		
		String value_encrypted = new String(value_encrypted_raw, "UTF-8");
		Log.i(log_tag, "Value encrypted: "+value_encrypted.length());

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		preferences.edit()
			.putString(key, value_encrypted)
			.commit();

		Log.i(log_tag, "Value from prefs: "+preferences.getString(key, "").length());
	}

	private boolean isKeyPresent() throws Exception
	{
		KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
		keystore.load(null);

		return keystore.containsAlias(KEYSTORE_KEY_ALIAS);
	}
	private void generateKey() throws Exception
	{
		Calendar start = new GregorianCalendar();
		Calendar end = new GregorianCalendar();
		end.add(10, Calendar.YEAR);
		
		KeyPairGeneratorSpec keypair_generator_spec = new KeyPairGeneratorSpec.Builder(context)
			.setAlias(KEYSTORE_KEY_ALIAS)
			.setSubject(new X500Principal(String.format("CN=%s, OU=%s", KEYSTORE_KEY_ALIAS, context.getPackageName())))
			.setSerialNumber(BigInteger.ONE)
			.setStartDate(start.getTime())
			.setEndDate(end.getTime())
			.setEncryptionRequired()
			.build();

		KeyPairGenerator keypair_generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
		keypair_generator.initialize(keypair_generator_spec);

		KeyPair keypair = keypair_generator.generateKeyPair();
	}
}
