package org.profmaad.stampdroid;

import org.json.JSONObject;
import org.json.JSONArray;

public class BitstampWebserviceCache
{
	private static BitstampWebserviceCache instance = null;

	private BitstampWebserviceCache()
	{		
	}

	public static BitstampWebserviceCache getInstance()
	{
		if(instance == null)
		{
			instance = new BitstampWebserviceCache();
		}

		return instance;
	}	
}
