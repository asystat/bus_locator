package com.example.com.benasque2014.mercurio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.util.Log;

import com.example.com.benasque2014.mercurio.connections.CCPClient;
import com.google.android.gms.maps.model.LatLng;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;

public class LocationBroadcastReceiver extends BroadcastReceiver {
	public static LatLng lastPosition;
	public static String msg="";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("LocationBroadcastReceiver",
				"onReceive: received location update");
		String codigo = KeyStoreController.getKeyStore().getString(
				"transmitiendo", "");
		String mensaje = KeyStoreController.getKeyStore().getString(
				"message", "");
		if (!mensaje.equalsIgnoreCase(msg))
			lastPosition=null;
		
		if (codigo.length() != 0) {
			
			final LocationInfo locationInfo = (LocationInfo) intent
					.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
			
			if (lastPosition==null || !(lastPosition!=null && lastPosition.latitude==locationInfo.lastLat && lastPosition.longitude==locationInfo.lastLong)){
				CCPClient.hacer_ruta(Secure.getString(context.getContentResolver(), Secure.ANDROID_ID) , codigo, mensaje,locationInfo.lastLat, locationInfo.lastLong, null);
				// The broadcast has woken up your app, and so you could do anything
				// now -
				// perhaps send the location to a server, or refresh an on-screen
				// widget.
				// We're gonna create a notification.
	
				// Construct the notification.
				Notification notification = new Notification(
						R.drawable.ic_launcher, "Ubicación actualizada hace "
								+ locationInfo.getTimestampAgeInSeconds()
								+ " segundos", System.currentTimeMillis());
	
				Intent contentIntent = new Intent(context,
						SendLocationActivity.class);
				PendingIntent contentPendingIntent = PendingIntent.getActivity(
						context, 0, contentIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
	
				notification.setLatestEventInfo(
						context,
						"Ultima localización recibida",
						"Hora "
								+ LocationInfo.formatTimeAndDay(
										locationInfo.lastLocationUpdateTimestamp,
										true), contentPendingIntent);
	
				// Trigger the notification.
				((NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE)).notify(
						1234, notification);
				
				lastPosition=new LatLng(locationInfo.lastLat,locationInfo.lastLong);
				msg=mensaje;
			}
		}
	}
}