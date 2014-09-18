/*
 * Copyright 2014 Little Fluffy Toys Ltd
 * Adapted from work by Reto Meier, Copyright 2011 Google Inc.
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

import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This is an example of implementing an application service that will run in
 * response to an alarm, allowing us to move long duration work out of an intent
 * receiver.
 * 
 * @see AlarmService
 * @see AlarmService_Alarm
 */
public class LocationBroadcastService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    
    private static final String TAG = "LocationBroadcastService"; 
    
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;

	
	@Override
	public void onCreate() {
		super.onCreate();
		mLocationClient = new LocationClient(getApplicationContext(), this, this);
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": onStartCommand");

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        new Thread(null, mTask, TAG).start();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": onDestroy");
    }
    
    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
		public void run() {
            boolean stopServiceOnCompletion = true;

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final long lastLocationUpdateTimestamp = prefs.getLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_TIME, 0);
            final long lastLocationBroadcastTimestamp = prefs.getLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_BROADCAST_TIME, 0);
            final boolean forceLocationToUpdate = prefs.getBoolean(LocationLibraryConstants.SP_KEY_FORCE_LOCATION_UPDATE, false);

            if (lastLocationBroadcastTimestamp == lastLocationUpdateTimestamp) {
                // no new location found
                if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": No new location update found since " + LocationInfo.formatTimestampForDebug(lastLocationUpdateTimestamp));

                if (forceLocationToUpdate || System.currentTimeMillis() - lastLocationUpdateTimestamp > LocationLibrary.getLocationMaximumAge()) {
                    if (forceLocationToUpdate) {
                        final Editor prefsEditor = prefs.edit();
                        prefsEditor.putBoolean(LocationLibraryConstants.SP_KEY_FORCE_LOCATION_UPDATE, false);
                        if (LocationLibraryConstants.SUPPORTS_GINGERBREAD) {
                        	prefsEditor.apply();
                        }
                        else {
                        	prefsEditor.commit();
                        }
                    }
                    // Current location is out of date. Force an update, and stop service if required.
                    stopServiceOnCompletion = !forceLocationUpdate();
                }
            } else {
                if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": New location update found at " + LocationInfo.formatTimestampForDebug(lastLocationUpdateTimestamp));
                
                final Editor prefsEditor = prefs.edit();
                prefsEditor.putLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_BROADCAST_TIME, lastLocationUpdateTimestamp);
                if (forceLocationToUpdate) {
                    prefsEditor.putBoolean(LocationLibraryConstants.SP_KEY_FORCE_LOCATION_UPDATE, false);
                }
                if (LocationLibraryConstants.SUPPORTS_GINGERBREAD) {
                	prefsEditor.apply();
                }
                else {
                	prefsEditor.commit();
                }
                sendBroadcast(getBaseContext(), true);
            }

            if (stopServiceOnCompletion) {
                // Done with our work... stop the service!
                LocationBroadcastService.this.stopSelf();
            }
        }
    };
    
    protected static void sendBroadcast(final Context context, final boolean isPeriodicBroadcast) {
        final Intent locationIntent = new Intent(LocationLibrary.broadcastPrefix + (isPeriodicBroadcast ? LocationLibraryConstants.LOCATION_CHANGED_PERIODIC_BROADCAST_ACTION : LocationLibraryConstants.LOCATION_CHANGED_TICKER_BROADCAST_ACTION));
        final LocationInfo locationInfo = new LocationInfo(context);
        locationIntent.putExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO, locationInfo);
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Broadcasting " + (isPeriodicBroadcast ? "periodic" : "latest") + " location update timed at " + LocationInfo.formatTimeAndDay(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_TIME, System.currentTimeMillis()), true));
        context.sendBroadcast(locationIntent, "android.permission.ACCESS_FINE_LOCATION");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * 
     * @return true if the service should stay awake, false if not
     */
    @TargetApi(9)
    public boolean forceLocationUpdate() {
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(!LocationLibrary.useFineAccuracyForRequests && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ? Criteria.ACCURACY_COARSE : Criteria.ACCURACY_FINE);

	    if (LocationLibraryConstants.SUPPORTS_GINGERBREAD) {
	    	if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
	            if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Force a single location update using Google GMS Location, as current location is beyond the oldest location permitted");
	    		mLocationClient.connect(); // this will cause an onConnected() or onConnectionFailed() callback 
	    		return true;
		    }
		    else {
	            if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Force a single location update using Android AOSP location, as current location is beyond the oldest location permitted");
	            // just request a single update. The passive provider will pick it up.
	            final Intent receiver = new Intent(getApplicationContext(), PassiveLocationChangedReceiver.class).addCategory(LocationLibraryConstants.INTENT_CATEGORY_ONE_SHOT_UPDATE);
	            final PendingIntent oneshotReceiver = PendingIntent.getBroadcast(getApplicationContext(), 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
	            try {
	                locationManager.requestSingleUpdate(criteria, oneshotReceiver);
	                if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": schedule timer to kill locationlistener in 30 seconds");
	                new Timer().schedule(new TimerTask(){
	                    public void run(){
	                        try {
	                            if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": remove updates after 30 seconds");
	                            locationManager.removeUpdates(oneshotReceiver);
	                        }
	                        catch (Exception e) {
	                            e.printStackTrace();
	                        }
	                        stopSelf();
	                    }}, 30000);
	                return true; // don't stop the service, allow the timer to do that
	            }
	            catch (IllegalArgumentException ex) {
	                // thrown if there are no providers, e.g. GPS is off
	                if (LocationLibrary.showDebugOutput) Log.w(LocationLibraryConstants.TAG, TAG + ": IllegalArgumentException during call to locationManager.requestSingleUpdate - probable cause is that all location providers are off. Details: " + ex.getMessage());
	            }
		    }
        }
        else { // pre-Gingerbread
            if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Force location updates (pre-Gingerbread), as current location is beyond the oldest location permitted");
            // one-shot not available pre-Gingerbread, so start updates, and when one is received, stop updates.
            final String provider = locationManager.getBestProvider(criteria, true);
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 0, 0, preGingerbreadUpdatesListener, LocationBroadcastService.this.getMainLooper());
                // don't stop the service, the callback will do that
                return true;
            }
        }
        // stop the service
        return false;
    }
    
    /**
     * Forces this service to be called after the given delay
     */
    public static void forceDelayedServiceCall(final Context context, final int delayInSeconds) {
        final Intent serviceIntent = new Intent(context, LocationBroadcastService.class);
        final PendingIntent pIntent = PendingIntent.getService(context, LocationLibraryConstants.LOCATION_BROADCAST_REQUEST_CODE_SINGLE_SHOT, serviceIntent, PendingIntent.FLAG_ONE_SHOT);
        final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (delayInSeconds * 1000), pIntent);
    }

    final android.location.LocationListener preGingerbreadUpdatesListener = new android.location.LocationListener() {
        public void onLocationChanged(Location location) {
            if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Single Location Update Received: " + location.getLatitude() + "," + location.getLongitude());
            ((LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).removeUpdates(preGingerbreadUpdatesListener);
            
            if (!LocationLibraryConstants.SUPPORTS_FROYO) {
                // this will not be broadcast by the passive location updater, so we will process it ourselves
                PassiveLocationChangedReceiver.processLocation(LocationBroadcastService.this, location);
            }
            
            // Broadcast it without significant delay.
            forceDelayedServiceCall(getApplicationContext(), 1);
            // Done with our work... stop the service!
            LocationBroadcastService.this.stopSelf();
        }
        
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}    
        public void onProviderDisabled(String provider) {}
      };
      
    /**
     * This is the object that receives interactions from clients. See
     * RemoteService for a more complete example.
     */
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
    
	/**
	 * Google GMS Location
	 */
	public void onConnectionFailed(ConnectionResult arg0)
	{
        if (LocationLibrary.showDebugOutput) Log.w(LocationLibraryConstants.TAG, TAG + ": onConnectionFailed (Google GMS Location)");
        stopSelf();
	}

	/**
	 * Google GMS Location
	 */
	public void onConnected(Bundle arg0)
	{
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": onConnected (Google GMS Location)");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationLibrary.useFineAccuracyForRequests ? LocationRequest.PRIORITY_HIGH_ACCURACY : LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(4000);
        mLocationRequest.setFastestInterval(1000);
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	/**
	 * Google GMS Location
	 */
	public void onDisconnected()
	{
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": onDisconnected (Google GMS Location)");
        mLocationClient.removeLocationUpdates(this);// Avoid crashes
	}

	/**
	 * Google GMS Location
	 */
	public void onLocationChanged(Location location)
	{
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": onLocationChanged (Google GMS Location)");	
		mLocationClient.disconnect();
		PassiveLocationChangedReceiver.processLocation(LocationBroadcastService.this, location, false, true);
	}

	/**
	 * Google GMS Location
	 */
	public void onProviderDisabled(String provider)
	{
	}

	/**
	 * Google GMS Location
	 */
	public void onProviderEnabled(String provider)
	{
	}

	/**
	 * Google GMS Location
	 */
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}
}
