package com.example.com.benasque2014.mercurio;

import android.app.Application;
import android.content.Context;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

public class MainApplication extends Application {
	static Context mContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext=getApplicationContext();
		//LocationLibrary.initialiseLibrary(getBaseContext(), "com.example.com.benasque2014.mercurio");
		LocationLibrary.initialiseLibrary(getBaseContext(), 5000, 20000, "com.example.com.benasque2014.mercurio");
		
	}

	public static Context getContext(){
		return mContext;
	}
}
