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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import java.math.BigInteger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Base64;
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
			Log.e(log_tag, "Failed to access key from keystore", e);
			e.printStackTrace();
		}
	}

	public String getClientID() throws Exception
	{
		return getValue("org.profmaad.stampdroid.client_id", "org.profmaad.stampdroid.client_id_encyption_key", "org.profmaad.stampdroid.client_id_iv");
	}

	public String getAPIKey() throws Exception
	{
		return getValue("org.profmaad.stampdroid.api_key", "org.profmaad.stampdroid.api_key_encyption_key", "org.profmaad.stampdroid.api_key_iv");
	}

	public String getAPISecret() throws Exception
	{
		return getValue("org.profmaad.stampdroid.api_secret", "org.profmaad.stampdroid.api_secret_encyption_key", "org.profmaad.stampdroid.api_secret_iv");
	}

	public void setClientID(String client_id) throws Exception
	{
		setValue("org.profmaad.stampdroid.client_id", "org.profmaad.stampdroid.client_id_encyption_key", "org.profmaad.stampdroid.client_id_iv", client_id);
	}

	public void setAPIKey(String api_key) throws Exception
	{
		setValue("org.profmaad.stampdroid.api_key", "org.profmaad.stampdroid.api_key_encyption_key", "org.profmaad.stampdroid.api_key_iv", api_key);
	}

	public void setAPISecret(String api_secret) throws Exception
	{
		setValue("org.profmaad.stampdroid.api_secret", "org.profmaad.stampdroid.api_secret_encyption_key", "org.profmaad.stampdroid.api_secret_iv", api_secret);
	}

	private String getValue(String value_key, String encryption_key_key, String iv_key) throws Exception
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
		if(!(preferences.contains(value_key) && preferences.contains(encryption_key_key) && preferences.contains(iv_key))) { return ""; }

		byte encryption_key_encrypted[] = Base64.decode(preferences.getString(encryption_key_key, ""), Base64.DEFAULT);
		byte iv_encrypted[] = Base64.decode(preferences.getString(iv_key, ""), Base64.DEFAULT);

		Cipher rsa_cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

		rsa_cipher.init(Cipher.DECRYPT_MODE, private_key);
		byte encryption_key[] = rsa_cipher.doFinal(encryption_key_encrypted);

		rsa_cipher.init(Cipher.DECRYPT_MODE, private_key);
		byte iv[] = rsa_cipher.doFinal(iv_encrypted);

		Cipher aes_cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		aes_cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryption_key, "AES"), new IvParameterSpec(iv));		
		
		byte value_encrypted[] = Base64.decode(preferences.getString(value_key, ""), Base64.DEFAULT);
		byte value_decrypted_raw[] = aes_cipher.doFinal(value_encrypted);

		String value = new String(value_decrypted_raw, "UTF-8");

		return value;
	}

	private void setValue(String value_key, String encryption_key_key, String iv_key, String value) throws Exception
	{
		KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
		keystore.load(null);

		if(!keystore.containsAlias(KEYSTORE_KEY_ALIAS))
		{
			throw new Exception("no key present");
		}

		KeyStore.PrivateKeyEntry key_entry = (KeyStore.PrivateKeyEntry)keystore.getEntry(KEYSTORE_KEY_ALIAS, null);
		RSAPublicKey public_key = (RSAPublicKey)key_entry.getCertificate().getPublicKey();

		KeyGenerator aes_key_generator = KeyGenerator.getInstance("AES");
		aes_key_generator.init(256);
		SecretKey encryption_key = aes_key_generator.generateKey();

		Cipher aes_cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		aes_cipher.init(Cipher.ENCRYPT_MODE, encryption_key);

		byte value_decrypted[] = value.getBytes("UTF-8");
		byte value_encrypted_raw[] = aes_cipher.doFinal(value_decrypted);
		String value_encrypted = Base64.encodeToString(value_encrypted_raw, Base64.DEFAULT);

		byte iv[] = aes_cipher.getIV();

		Cipher rsa_cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

		rsa_cipher.init(Cipher.ENCRYPT_MODE, public_key);
		byte iv_encrypted_raw[] = rsa_cipher.doFinal(iv);
		String iv_encrypted = Base64.encodeToString(iv_encrypted_raw, Base64.DEFAULT);

		rsa_cipher.init(Cipher.ENCRYPT_MODE, public_key);
		byte encryption_key_encrypted_raw[] = rsa_cipher.doFinal(encryption_key.getEncoded());
		String encryption_key_encrypted = Base64.encodeToString(encryption_key_encrypted_raw, Base64.DEFAULT);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		preferences.edit()
			.putString(value_key, value_encrypted)
			.putString(encryption_key_key, encryption_key_encrypted)
			.putString(iv_key, iv_encrypted)
			.commit();
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

	private void dumpPreferences()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Map<String,?> keys = preferences.getAll();
		
		for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.i("PREF VALUES",entry.getKey() + ": " + 
				  entry.getValue().toString());            
		}
	}
}
