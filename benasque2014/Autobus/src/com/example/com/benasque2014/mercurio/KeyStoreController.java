package com.example.com.benasque2014.mercurio;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/** 
 * This class manages the communication between the application and the persistent store.
 * It contains pairs keys-value.
 * @author XplosiveLabs (xpslabs@gmail.com)
 *
 */  
public class KeyStoreController{

	/** SharePreference name */
	//protected final static String SETTINGS = "cuantocomic";


	private static KeyStoreController singleton = null;
	private SharedPreferences preferences = null;
	 

	

	private KeyStoreController(Context c){
		//preferences = c.getSharedPreferences(SETTINGS, 0); //MODE_PRIVATE
		preferences = PreferenceManager.getDefaultSharedPreferences(c);
	}
	 
	public static KeyStoreController getKeyStore(){
		if( singleton == null){
			singleton = new KeyStoreController(MainApplication.getContext());
		}
		return singleton;
	}	

	public void appOpened() {
		long timesOpened=getLong("timesOpened", 0);	
		timesOpened+=1;
		setPreference("timesOpened", timesOpened);
	}
	
	public long getTimesOpened(){
		return getLong("timesOpened", (long)1);
	}

	
	public void setPreference(String key, Object value) {
		// The SharedPreferences editor - must use commit() to submit changes
		SharedPreferences.Editor editor = preferences.edit();
		if(value instanceof Integer )
			editor.putInt(key, ((Integer) value).intValue());
		else if (value instanceof String)
			editor.putString(key, (String)value);
		else if (value instanceof Boolean)
			editor.putBoolean(key, (Boolean)value);
		else if (value instanceof Long)
			editor.putLong(key, (Long)value);
		editor.commit();
	}
	
	public int getInt(String key, int defaultValue) {
		return preferences.getInt(key, defaultValue);
	}
	
	public String getString(String key, String defaultValue) {
		return preferences.getString(key, defaultValue);
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		return preferences.getBoolean(key, defaultValue);
	}
	
	public long getLong(String key, long defaultValue) {
		return preferences.getLong(key, defaultValue);
	}

	public boolean isFirstLaunch() {
		boolean isFirstLaunch = preferences.getBoolean("first_launch", true);
		if(isFirstLaunch)
			setPreference("first_launch", false);
		return isFirstLaunch;
	}


	
	
}
