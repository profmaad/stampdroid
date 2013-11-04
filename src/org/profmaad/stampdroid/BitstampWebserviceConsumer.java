package org.profmaad.stampdroid;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import java.net.URLConnection;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;

import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONArray;

import android.content.Context;
import android.util.Log;

public class BitstampWebserviceConsumer
{
	private String api_base_uri;

	private String api_key;
	private String api_secret;	
	private String client_id;

	private String app_name;

	final protected static char[] hex_chars = "0123456789ABCDEF".toCharArray();	

	public BitstampWebserviceConsumer(Context context)
	{
		api_base_uri = context.getString(R.string.api_base_uri);
		if(!api_base_uri.endsWith("/"))
		{
			api_base_uri.concat("/");
		}

		api_key = context.getString(R.string.api_key);
		api_secret = context.getString(R.string.api_secret);
		client_id = context.getString(R.string.bitstamp_client_id);

		app_name = context.getString(R.string.app_name);
	}

	public JSONObject ticker()
	{
		try
		{
			String result_body = doRequest("ticker", new HashMap<String, String>(), false);
			JSONObject ticker_object = (JSONObject)new JSONTokener(result_body).nextValue();

			return ticker_object;
		}
		catch(Exception e)
		{
			Log.e(app_name, e.toString());
		}

		return new JSONObject();
	}
	public JSONObject balance()
	{
		try
		{
			String result_body = doRequest("balance", new HashMap<String, String>(), true);
			JSONObject balance_object = (JSONObject)new JSONTokener(result_body).nextValue();

			return balance_object;
		}
		catch(Exception e)
		{
			Log.e(app_name, e.toString());
		}

		return new JSONObject();
	}

	private String doRequest(String api_resource, Map<String, String> parameters, boolean authenticated_request) throws Exception
	{
		StringBuilder api_resource_url_builder = new StringBuilder();

		api_resource_url_builder.append(api_base_uri).append(api_resource);
		if(!api_resource.endsWith("/"))
		{
			api_resource_url_builder.append("/");
		}

		if(!authenticated_request && ! parameters.isEmpty())
		{
			api_resource_url_builder.append("?");
			api_resource_url_builder.append(constructPostRequestData(parameters, authenticated_request));
		}

		URL api_resource_url = new URL(api_resource_url_builder.toString());

		HttpURLConnection connection = (HttpURLConnection)api_resource_url.openConnection();
		if(authenticated_request)
		{
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
		}

		if(authenticated_request)
		{
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes(constructPostRequestData(parameters, authenticated_request));

			out.flush();
			out.close();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line = new String();
		StringBuilder response_body_builder = new StringBuilder();
		while((line = in.readLine()) != null)
		{
			response_body_builder.append(line).append('\n');
		}
		in.close();

		connection.disconnect();

		return response_body_builder.toString();
	}

	private String createSignature(long nonce)
	{
		String message = new StringBuilder().append(nonce).append(client_id).append(api_key).toString();

		try
		{
			Mac hmac = Mac.getInstance("HmacSHA256");
			SecretKeySpec api_secret_spec = new SecretKeySpec(api_secret.getBytes(), hmac.getAlgorithm());
			hmac.init(api_secret_spec);

			byte[] raw_signature = hmac.doFinal(message.getBytes());
			return bytesToHex(raw_signature);
		}
		catch(NoSuchAlgorithmException e)
		{
			//TODO
		}
		catch(IllegalArgumentException e)
		{
			//TODO
		}
		catch(InvalidKeyException e)
		{
			//TODO
		}
		
		return new String(); //HACK
	}

	// adapted from http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	private String bytesToHex(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++)
		{
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hex_chars[v >>> 4];
			hexChars[j * 2 + 1] = hex_chars[v & 0x0F];
		}
		return new String(hexChars);
	}

	private String constructPostRequestData(Map<String, String> parameters, boolean include_auth_parameters)
	{
		StringBuilder post_request_data_builder = new StringBuilder();

		Set keys = parameters.keySet();
		Iterator keys_iterator = keys.iterator();

		if(include_auth_parameters)
		{
			long nonce = System.currentTimeMillis();
			String signature = createSignature(nonce);

			post_request_data_builder.append("key=").append(api_key).append("&nonce=").append(nonce).append("&signature=").append(signature);
			if(!parameters.isEmpty())
			{
				post_request_data_builder.append("&");
			}
		}

		while(keys_iterator.hasNext())
		{
			Object key = keys_iterator.next();
			
			try
			{
				post_request_data_builder.append(key).append("=").append(URLEncoder.encode(parameters.get(key), "UTF-8"));
			}
			catch(UnsupportedEncodingException e)
			{
				//TODO
			}

			if(keys_iterator.hasNext())
			{
				post_request_data_builder.append("&");
			}
		}

		Log.i(app_name, post_request_data_builder.toString());

		return post_request_data_builder.toString();
	}
}
