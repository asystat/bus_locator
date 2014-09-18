/*
 * Copyright 2014 Little Fluffy Toys Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlefluffytoys.littlefluffylocationlibrary;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A simple-to-use library that broadcasts location updates to your app without killing your battery.
 * 
 * Project home and documentation: {@link code.google.com/p/little-fluffy-location-library}
 * 
 * @author Kenton Price, Little Fluffy Toys Ltd - {@link www.littlefluffytoys.mobi}
 */
public class LocationLibrary implements ConnectionCallbacks, OnConnectionFailedListener {
    
    protected static boolean showDebugOutput = false;
    protected static boolean useFineAccuracyForRequests = false;
    protected static boolean broadcastEveryLocationUpdate = false;
    protected static int stableLocationTimeoutInSeconds = 5; // how many seconds to wait during a flurry of location updates, until it can be assumed no more updates are forthcoming

    private static final String TAG = "LocationLibrary";
    
    protected static String broadcastPrefix;

    private static boolean initialised = false;
    
    private static long alarmFrequency = LocationLibraryConstants.DEFAULT_ALARM_FREQUENCY;
    private static int locationMaximumAge = (int) LocationLibraryConstants.DEFAULT_MAXIMUM_LOCATION_AGE;
    
    protected static int getLocationMaximumAge() {
        return locationMaximumAge;
    }
    
    protected static long getAlarmFrequency() {
        return alarmFrequency;
    }
    
	private static LocationLibrary mLocationLibrary;
	private static Context mContext;

	private LocationClient mLocationClient;
	
	private static LocationLibrary getInstance(Context context) {
		if (mLocationLibrary == null) {
			mLocationLibrary = new LocationLibrary();
			mContext = context.getApplicationContext();
		}
		return mLocationLibrary;
	}

    @TargetApi(Build.VERSION_CODES.FROYO)
	public static void startAlarmAndListener(final Context context) {
        if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": startAlarmAndListener: alarmFrequency=" + (alarmFrequency == LocationLibraryConstants.DEFAULT_ALARM_FREQUENCY ? "default:" : "") + alarmFrequency/1000 + " secs, locationMaximumAge=" + (locationMaximumAge == LocationLibraryConstants.DEFAULT_MAXIMUM_LOCATION_AGE ? "default:" : "") + locationMaximumAge/1000 + " secs");
        
        final PendingIntent alarmIntent = PendingIntent.getService(context, LocationLibraryConstants.LOCATION_BROADCAST_REQUEST_CODE_REPEATING_ALARM, new Intent(context, LocationBroadcastService.class), PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // cancel any existing alarm
        am.cancel(alarmIntent);

        // Schedule the alarm
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), getAlarmFrequency(), alarmIntent);

        if (LocationLibraryConstants.SUPPORTS_FROYO) {
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            final Intent passiveIntent = new Intent(context, PassiveLocationChangedReceiver.class);
            final PendingIntent locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListenerPassivePendingIntent);
        }

        if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": startAlarmAndListener completed");
    }
    
    public static void stopAlarmAndListener(final Context context) {
       
    	final PendingIntent alarmIntent = PendingIntent.getService(context, LocationLibraryConstants.LOCATION_BROADCAST_REQUEST_CODE_REPEATING_ALARM, new Intent(context, LocationBroadcastService.class), PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // cancel any existing alarm
        am.cancel(alarmIntent);

        if (LocationLibraryConstants.SUPPORTS_FROYO) {
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            final Intent passiveIntent = new Intent(context, PassiveLocationChangedReceiver.class);
            final PendingIntent locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            locationManager.removeUpdates(locationListenerPassivePendingIntent);
        }

        if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": stopAlarmAndListener completed");
    }
    
    /**
     * To use this library, call initialiseLibrary from your Application's onCreate method,
     * having set up your manifest as detailed in the project documentation.
     * 
     * This constructor uses defaults specified in LocationLibraryConstants for alarmFrequency ({@link android.app.AlarmManager#INTERVAL_FIFTEEN_MINUTES AlarmManager.INTERVAL_FIFTEEN_MINUTES})
     * and locationMaximumAge ({@link android.app.AlarmManager#INTERVAL_HOUR AlarmManager.INTERVAL_HOUR}), and broadcastEveryLocationUpdate by default is false.
     * 
     * @param broadcastPrefix The prefix to the broadcast intent string that tells the client app the location has changed.
     * 
     * @throws UnsupportedOperationException if the location service doesn't exist, or if the device has no location providers
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void initialiseLibrary(final Context context, final String broadcastPrefix) throws UnsupportedOperationException {
        if (!initialised) {
            if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": initialiseLibrary");
            LocationLibrary.broadcastPrefix = broadcastPrefix;
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            if (!prefs.getBoolean(LocationLibraryConstants.SP_KEY_RUN_ONCE, Boolean.FALSE)) {
                if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": initialiseLibrary: first time ever run -> start alarm and listener");
                startAlarmAndListener(context);
                final Editor prefsEditor = prefs.edit();
                prefsEditor.putBoolean(LocationLibraryConstants.SP_KEY_RUN_ONCE, Boolean.TRUE);
                if (LocationLibraryConstants.SUPPORTS_GINGERBREAD) {
                	prefsEditor.apply();
                }
                else {
                	prefsEditor.commit();
                }

                // see if we know where we are already
        	    if (LocationLibraryConstants.SUPPORTS_GINGERBREAD && GooglePlayServicesUtil.isGooglePlayServicesAvailable(context.getApplicationContext()) == ConnectionResult.SUCCESS) {
                    if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": initialiseLibrary: using Google GMS Location");
                    final LocationLibrary locationLibrary = getInstance(context);
                    locationLibrary.mLocationClient = new LocationClient(context.getApplicationContext(), locationLibrary, locationLibrary);
                    locationLibrary.mLocationClient.connect(); // this will cause an onConnected() or onConnectionFailed() callback
        	    }
        	    else {
                    if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": initialiseLibrary: using Android AOSP location");
	                final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	                if (lm != null) {
	                    final List<String> providers = lm.getAllProviders();
	                    if (providers.size() > 0) {
	                        Location bestLocation = null;
	                        for (String provider: lm.getAllProviders()) {
	                            final Location lastLocation = lm.getLastKnownLocation(provider);
	                            if (lastLocation != null) {
	                                if (bestLocation == null || !bestLocation.hasAccuracy() || (lastLocation.hasAccuracy() && lastLocation.getAccuracy() < bestLocation.getAccuracy())) {
	                                    bestLocation = lastLocation;
	                                }
	                            }
	                        }
	                        if (bestLocation != null) {
	                            if (showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": initialiseLibrary: remembering best location " + bestLocation.getLatitude() + "," + bestLocation.getLongitude());
	                            PassiveLocationChangedReceiver.processLocation(context, bestLocation, false, false);
	                        }
	                    }
	                    else {
	                        throw new UnsupportedOperationException("No location providers found on this device");
	                    }
	                }
	                else {
	                    throw new UnsupportedOperationException("Location service not found on this device");
	                }
        	    }
            }
            initialised = true;
        }
    }
    
    /**
     * To use this library, call initialiseLibrary from your Application's onCreate method,
     * having set up your manifest as detailed in the project documentation.
     *
     * @param alarmFrequency How often to broadcast a location update in milliseconds, if one was received.
     * 
     * For battery efficiency, this should be one of the available inexact recurrence intervals
     * recognised by {@link android.app.AlarmManager#setInexactRepeating(int, long, long , PendingIntent) AlarmManager.setInexactRepeating}.
     * You are not prevented from using any other value, but in that case Android's alarm manager uses setRepeating instead of setInexactRepeating,
     * and this results in poorer battery life. The default is {@link android.app.AlarmManager#INTERVAL_FIFTEEN_MINUTES AlarmManager.INTERVAL_FIFTEEN_MINUTES}.
     * 
     * @param locationMaximumAge The maximum age of a location update. If when the alarm fires the location is
     * older than this, a location update will be requested. The default is {@link android.app.AlarmManager#INTERVAL_HOUR AlarmManager.INTERVAL_HOUR}
     * 
     * @see #initialiseLibrary(Context, String)
     */
    public static void initialiseLibrary(final Context context, final long alarmFrequency, final int locationMaximumAge, final String broadcastPrefix) throws UnsupportedOperationException {
        if (!initialised) {
            LocationLibrary.alarmFrequency = alarmFrequency;
            LocationLibrary.locationMaximumAge = locationMaximumAge;
            initialiseLibrary(context, broadcastPrefix);
         }
    }
    
    /**
     * To use this library, call initialiseLibrary from your Application's onCreate method,
     * having set up your manifest as detailed in the project documentation.
     *
     * This constructor uses defaults specified in LocationLibraryConstants for alarmFrequency ({@link android.app.AlarmManager#INTERVAL_FIFTEEN_MINUTES AlarmManager.INTERVAL_FIFTEEN_MINUTES})
     * and locationMaximumAge ({@link android.app.AlarmManager#INTERVAL_HOUR AlarmManager.INTERVAL_HOUR}).
     * 
     * @param broadcastEveryLocationUpdate If true, broadcasts every location update using intent action 
     * LocationLibraryConstants.LOCATION_CHANGED_TICKER_BROADCAST_ACTION
     * 
     * @see #initialiseLibrary(Context, String)
     */
    public static void initialiseLibrary(final Context context, final boolean broadcastEveryLocationUpdate, final String broadcastPrefix) throws UnsupportedOperationException {
        if (!initialised) {
            LocationLibrary.broadcastEveryLocationUpdate = broadcastEveryLocationUpdate;
            initialiseLibrary(context, broadcastPrefix);
         }
    }
    
    /**
     * To use this library, call initialiseLibrary from your Application's onCreate method,
     * having set up your manifest as detailed in the project documentation.
     * 
     * @param alarmFrequency How often to broadcast a location update in milliseconds, if one was received.
     * 
     * For battery efficiency, this should be one of the available inexact recurrence intervals
     * recognised by {@link android.app.AlarmManager#setInexactRepeating(int, long, long , PendingIntent) AlarmManager.setInexactRepeating}.
     * You are not prevented from using any other value, but in that case Android's alarm manager uses setRepeating instead of setInexactRepeating,
     * and this results in poorer battery life. The default is {@link android.app.AlarmManager#INTERVAL_FIFTEEN_MINUTES AlarmManager.INTERVAL_FIFTEEN_MINUTES}.
     * 
     * @param locationMaximumAge The maximum age of a location update. If when the alarm fires the location is
     * older than this, a location update will be requested. The default is {@link android.app.AlarmManager#INTERVAL_HOUR AlarmManager.INTERVAL_HOUR}
     * 
     * @param broadcastEveryLocationUpdate If true, broadcasts every location update using intent action 
     * LocationLibraryConstants.LOCATION_CHANGED_TICKER_BROADCAST_ACTION
     * 
     * @see #initialiseLibrary(Context, long, int, String)
     * @see #initialiseLibrary(Context, boolean, String)
     * @see #initialiseLibrary(Context, String)
     */
    public static void initialiseLibrary(final Context context, final long alarmFrequency, final int locationMaximumAge, final boolean broadcastEveryLocationUpdate, final String broadcastPrefix) throws UnsupportedOperationException {
        if (!initialised) {
            LocationLibrary.broadcastEveryLocationUpdate = broadcastEveryLocationUpdate;
            initialiseLibrary(context, alarmFrequency, locationMaximumAge, broadcastPrefix);
        }    
    }
    
    /**
     * To force an on-demand location update, call this method.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void forceLocationUpdate(final Context context) {
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": forceLocationUpdate called to force a location update");
        final Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
        prefsEditor.putBoolean(LocationLibraryConstants.SP_KEY_FORCE_LOCATION_UPDATE, true);
        if (LocationLibraryConstants.SUPPORTS_GINGERBREAD) {
        	prefsEditor.apply();
        }
        else {
        	prefsEditor.commit();
        }

        context.startService(new Intent(context, LocationBroadcastService.class));
    }
  
    /**
     * When the library asks for an on-demand location update, by default this is coarse accuracy e.g. network or wifi
     * (unless the user doesn't have coarse location services switched on, in which case it asks for fine accuracy e.g. GPS).
     * If you always want the library to request fine accuracy, i.e. GPS, set this to true.
     */
    public static void useFineAccuracyForRequests(final boolean useFineAccuracyForRequests) {
        LocationLibrary.useFineAccuracyForRequests = useFineAccuracyForRequests;
    }

    /**
     * Debug output is off by default. To switch it on, call showDebugOutput(true)
     * from your Application's onCreate method.
     */
    public static void showDebugOutput(final boolean showDebugOutput) {
        LocationLibrary.showDebugOutput = showDebugOutput;
    }
    
	/**
	 * Google GMS Location
	 */
	public void onConnectionFailed(ConnectionResult arg0)
	{
        if (LocationLibrary.showDebugOutput) Log.w(LocationLibraryConstants.TAG, TAG + ": onConnectionFailed (Google GMS Location)");
		mLocationLibrary = null;
		mLocationClient = null;
	}

	/**
	 * Google GMS Location
	 */
	public void onConnected(Bundle arg0)
	{
		if (mLocationClient != null) {
			final Location mCurrentLocation = mLocationClient.getLastLocation();
			mLocationClient.disconnect();
			if (mCurrentLocation != null) {
				if (LocationLibrary.showDebugOutput) Log.d(TAG, "Last location lat=" + mCurrentLocation.getLatitude() + " long=" + mCurrentLocation.getLongitude());
	            PassiveLocationChangedReceiver.processLocation(mContext, mCurrentLocation, false, false);
			}
		}
	}

	/**
	 * Google GMS Location
	 */
	public void onDisconnected()
	{
		mLocationLibrary = null;
		mLocationClient = null;
	}
}
