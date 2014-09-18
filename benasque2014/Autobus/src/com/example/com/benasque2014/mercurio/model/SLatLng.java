package com.example.com.benasque2014.mercurio.model;

import java.io.Serializable;

import com.google.android.gms.maps.model.LatLng;

public class SLatLng implements Serializable{
	public double lat;
	public double lng;
	public SLatLng(double lat, double lng) {
		super();
		this.lat = lat;
		this.lng = lng;
	}
	
	public LatLng getLatLng(){
		return new LatLng(lat, lng);
	}
}
