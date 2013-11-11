package org.profmaad.stampdroid;

import org.json.JSONObject;
import org.json.JSONArray;

import android.util.Log;

public class BitstampWebserviceCache
{
	private static final long EXPIRY = 60*5 * 1000;

	private static BitstampWebserviceCache instance = null;

	private JSONObject balance = null;
	private long balance_timestamp = -1;

	private JSONArray open_orders = null;
	private long open_orders_timestamp = -1;

	private JSONArray user_transactions = null;
	private int user_transactions_offset = -1;
	private int user_transactions_limit = -1;
	private boolean user_transactions_sort_ascending = false;
	private long user_transactions_timestamp = -1;

	private BitstampWebserviceCache()
	{
	}

	public synchronized static BitstampWebserviceCache getInstance()
	{
		if(instance == null)
		{
			instance = new BitstampWebserviceCache();
		}

		return instance;
	}

	public JSONObject getBalance()
	{
		if(System.currentTimeMillis() > balance_timestamp + EXPIRY)
		{
			balance = null;
		}

		return balance;
	}
	public synchronized void setBalance(JSONObject balance)
	{
		this.balance = balance;

		this.balance_timestamp = System.currentTimeMillis();
	}

	public JSONArray getOpenOrders()
	{
		if(System.currentTimeMillis() > open_orders_timestamp + EXPIRY)
		{
			open_orders = null;
		}

		return open_orders;
	}
	public synchronized void setOpenOrders(JSONArray open_orders)
	{
		this.open_orders = open_orders;

		this.open_orders_timestamp = System.currentTimeMillis();
	}

	public JSONArray getUserTransactions(int offset, int limit, boolean sort_ascending)
	{
		if(System.currentTimeMillis() > user_transactions_timestamp + EXPIRY)
		{
			user_transactions = null;
		}

		if(offset == user_transactions_offset
		   && limit == user_transactions_limit
		   && sort_ascending == user_transactions_sort_ascending)
		{
			return user_transactions;
		}
		else
		{
			return null;
		}
	}
	public synchronized void setUserTransactions(JSONArray user_transactions, int offset, int limit, boolean sort_ascending)
	{
		this.user_transactions = user_transactions;

		this.user_transactions_offset = offset;
		this.user_transactions_limit = limit;
		this.user_transactions_sort_ascending = sort_ascending;

		this.user_transactions_timestamp = System.currentTimeMillis();
	}
}
