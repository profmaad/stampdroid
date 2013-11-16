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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BitstampWebserviceConsumer
{
	private String api_base_uri;

	private String api_key;
	private String api_secret;	
	private String client_id;

	private String log_tag;

	private boolean bypass_cache;

	final protected static char[] hex_chars = "0123456789ABCDEF".toCharArray();	

	public BitstampWebserviceConsumer(Context context)
	{
		this(context, false);
	}
	public BitstampWebserviceConsumer(Context context, boolean bypass_cache)
	{
		api_base_uri = context.getString(R.string.api_base_uri);
		if(!api_base_uri.endsWith("/"))
		{
			api_base_uri.concat("/");
		}

		log_tag = context.getString(R.string.app_name);
		this.bypass_cache = bypass_cache;

		loadAccountSettings(context);
	}

	private boolean loadAccountSettings(Context context)
	{
		AccountSettingsHelper helper = new AccountSettingsHelper(context);

		try
		{
			client_id = helper.getClientID();
			api_key = helper.getAPIKey();
			api_secret = helper.getAPISecret();
		}
		catch(Exception e)
		{
			Log.e(log_tag, "Failed to access API secrets: "+e.toString());
			e.printStackTrace();
			return false;
		}

		return !(client_id.isEmpty() || api_key.isEmpty() || api_secret.isEmpty());
	}
	public boolean isReady()
	{
		return !(client_id == null || api_key == null || api_secret == null ||
				 client_id.isEmpty() || api_key.isEmpty() || api_secret.isEmpty());
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
			Log.e(log_tag, e.toString());
		}

		return new JSONObject();
	}
	public JSONObject balance()
	{
		if(BitstampWebserviceCache.getInstance().getBalance() != null && !bypass_cache)
		{
			return BitstampWebserviceCache.getInstance().getBalance();
		}

		try
		{
			String result_body = doRequest("balance", new HashMap<String, String>(), true);
			JSONObject balance_object = (JSONObject)new JSONTokener(result_body).nextValue();

			BitstampWebserviceCache.getInstance().setBalance(balance_object);

			return balance_object;
		}
		catch(Exception e)
		{
			Log.e(log_tag, e.toString());
		}

		return new JSONObject();
	}
	public JSONArray openOrders()
	{
		if(BitstampWebserviceCache.getInstance().getOpenOrders() != null && !bypass_cache)
		{
			return BitstampWebserviceCache.getInstance().getOpenOrders();
		}

		try
		{
			String result_body = doRequest("open_orders", new HashMap<String, String>(), true);
			JSONArray open_orders_array = (JSONArray)new JSONTokener(result_body).nextValue();

			BitstampWebserviceCache.getInstance().setOpenOrders(open_orders_array);

			return open_orders_array;
		}
		catch(Exception e)
		{
			Log.e(log_tag, e.toString());
		}
		return new JSONArray();
	}

	public JSONArray userTransactions()
	{
		return userTransactions(0, 100, false);
	}
	public JSONArray userTransactions(int limit)
	{
		return userTransactions(0, limit, false);
	}
	public JSONArray userTransactions(int offset, int limit)
	{
		return userTransactions(offset, limit, false);
	}
	public JSONArray userTransactions(int offset, int limit, boolean sort_ascending)
	{
		if(BitstampWebserviceCache.getInstance().getUserTransactions(offset, limit, sort_ascending) != null && !bypass_cache)
		{
			return BitstampWebserviceCache.getInstance().getUserTransactions(offset, limit, sort_ascending);
		}

		HashMap<String, String> parameters = new HashMap<String, String>(3);
		parameters.put("offset", String.valueOf(offset));
		parameters.put("limit", String.valueOf(limit));
		parameters.put("sort", sort_ascending ? "asc" : "desc");

		try
		{
			String result_body = doRequest("user_transactions", parameters, true);
			JSONArray user_transactions_array = (JSONArray)new JSONTokener(result_body).nextValue();

			BitstampWebserviceCache.getInstance().setUserTransactions(user_transactions_array, offset, limit, sort_ascending);

			return user_transactions_array;
		}
		catch(Exception e)
		{
			Log.e(log_tag, e.toString());
		}
		return new JSONArray();
	}

	public JSONObject addBuyLimitOrder(double btc_amount, double usd_price)
	{
		HashMap<String, String> parameters = new HashMap<String, String>(3);
		parameters.put("amount", String.valueOf(btc_amount));
		parameters.put("price", String.valueOf(usd_price));

		try
		{
			String result_body = doRequest("buy", parameters, true);
			JSONObject order = (JSONObject)new JSONTokener(result_body).nextValue();

			return order;
		}
		catch(Exception e)
		{
			Log.e(log_tag, e.toString());
		}
		return new JSONObject();
	}
	public JSONObject addSellLimitOrder(double btc_amount, double usd_price)
	{
		HashMap<String, String> parameters = new HashMap<String, String>(3);
		parameters.put("amount", String.valueOf(btc_amount));
		parameters.put("price", String.valueOf(usd_price));

		try
		{
			String result_body = doRequest("sell", parameters, true);
			JSONObject order = (JSONObject)new JSONTokener(result_body).nextValue();

			return order;
		}
		catch(Exception e)
		{
			Log.e(log_tag, e.toString());
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

		Log.i(log_tag, post_request_data_builder.toString());

		return post_request_data_builder.toString();
	}
}
