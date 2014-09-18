/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.com.benasque2014.mercurio;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wizardpager.wizard.ui.PageFragmentCallbacks;
import com.example.com.benasque2014.mercurio.model.PuntosInfoPage;
import com.example.com.benasque2014.mercurio.model.RecorridoBasicInfoPage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PuntosInfoFragment extends Fragment implements OnMapClickListener, OnMarkerClickListener {
    private static final String ARG_KEY = "key";

    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private PuntosInfoPage mPage;
    
    
    /**
     * Note that this may be null if the Google Play services APK is not
     * available.
     */

    private static GoogleMap mMap;
    private static Double latitude, longitude;
    
    
    public static PuntosInfoFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        PuntosInfoFragment fragment = new PuntosInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PuntosInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = (PuntosInfoPage) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_map, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        // Passing harcoded values for latitude & longitude. Please change as per your need. This is just used to drop a Marker on the Map
        latitude = 42.6042858;
        longitude = 0.5228286000000253;

        //Toast.makeText(getActivity(), "Toque en el mapa para añadir un punto en la ruta", Toast.LENGTH_SHORT).show();
        
        setUpMapIfNeeded(); // For setting up the MapFragment
        
        //TODO:
        /*
        mNameView = ((TextView) rootView.findViewById(R.id.bus_name));
        mNameView.setText(mPage.getData().getString(RecorridoBasicInfoPage.NAME_DATA_KEY));

        mCodigoView = ((TextView) rootView.findViewById(R.id.bus_code));
        mCodigoView.setText(mPage.getData().getString(RecorridoBasicInfoPage.CODIGO_DATA_KEY));
        
        mClassView = ((TextView) rootView.findViewById(R.id.bus_class));
        mClassView.setText(mPage.getData().getString(RecorridoBasicInfoPage.CLASE_DATA_KEY));
        */
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    /***** Sets up the map if it is possible to do so *****/
    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.location_map)).getMap();
            
            mMap.setOnMapClickListener(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null)
                setUpMap();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the
     * camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap}
     * is not null.
     */
    private void setUpMap() {
        // For showing a move to my loction button
        mMap.setMyLocationEnabled(true);
        
        // For zooming automatically to the Dropped PIN Location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,
                longitude), 12.0f));
        
        mMap.setOnMarkerClickListener(this);
        
        addMarkersToMap();
    }

    
    private void addMarkersToMap() {
    	//List<LatLng> list2=mPage.getData().getParcelable(PuntosInfoPage.PUNTOS_DATA_KEY);
    	if (points==null) return;
    	markers=new ArrayList<Marker>();
    	if (mMap!=null)
    		mMap.clear();
    	Iterator<LatLng> it=points.iterator();
    	LatLng previous=null;
    	while (it.hasNext()){
    		LatLng act=it.next();
    		addPoint(act);
    		
    	}
    	//TODO: si queremos añadir lineas entre los puntos
    	//addLines();
    	
	}

    private List<Polyline> lineas;
    
	private void addLines() {
		if (lineas==null)
			lineas=new ArrayList<Polyline>();
		for (int j=0;j<lineas.size();j++){
			lineas.get(j).remove();
		}
		for (int i = 0; i < points.size() - 1; i++) {
			LatLng src = points.get(i);
			LatLng dest = points.get(i + 1);
			Polyline line = mMap.addPolyline(new PolylineOptions() //mMap is the Map Object
			.add(new LatLng(src.latitude, src.longitude),
			new LatLng(dest.latitude,dest.longitude))
			.width(5).color(Color.BLUE).geodesic(true));
			lineas.add(line);
			 }
	}

	@Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (mMap != null)
            setUpMap();

        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.location_map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null)
                setUpMap();
        }
        

        //TODO:
        /*
        mNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                    int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(RecorridoBasicInfoPage.NAME_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                
                mPage.notifyDataChanged();
            }
        });

        mCodigoView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                    int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(RecorridoBasicInfoPage.CODIGO_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });
        
        mClassView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                    int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(RecorridoBasicInfoPage.CLASE_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });
        */
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        /*
        if (mNameView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
        */
    }
    
    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then 
     **** app will crash ****/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMap != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.location_map)).commit();
            mMap = null;
        }
    }

	@Override
	public void onMapClick(LatLng arg0) {
		if (points==null)
			points=new ArrayList<LatLng>();
		points.add(arg0);
		mPage.notifyDataChanged();
		
		addPoint(arg0);
	}
	
	private List<Marker> markers;
	public static List<LatLng> points;
	
	private void addPoint(LatLng point){
		if (markers==null)
			markers=new ArrayList<Marker>();
		if (points==null)
			points=new ArrayList<LatLng>();
		MarkerOptions marker = new MarkerOptions().position(
                point).title("Parada " + markers.size()+1);

		final Marker mMarker = mMap.addMarker(marker);
		
		//if (markers.size()==0)
		//	Toast.makeText(getActivity(), "Haga click en un punto para eliminarlo", Toast.LENGTH_SHORT).show();
		
		markers.add(mMarker);
		
		//mPage.getData().putParcelableArrayList(PuntosInfoPage.PUNTOS_DATA_KEY, (ArrayList<? extends Parcelable>) points);
        //mPage.notifyDataChanged();
        
		// This causes the marker at Perth to bounce into position when it is clicked.
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - interpolator
                        .getInterpolation((float) elapsed / duration), 0);
                mMarker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
        //TODO: si queremos añadir lineas entre los puntos
        //addLines();
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		deleteMarker(arg0);
		return true;
	}
	
	private void deleteMarker(final Marker arg0){
		
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("¿Desea eliminar este punto del trayecto?")
               .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   LatLng pos=arg0.getPosition();
               		boolean e=false; int i=0;
               		while (!e && i<markers.size()){
               			e=(pos.latitude==markers.get(i).getPosition().latitude && pos.longitude==markers.get(i).getPosition().longitude);
               			if (e){
               				Marker m=markers.get(i);
               				m.remove();
               				markers.remove(i);
               				points.remove(i);
               			}
               			i++;
               		}
               		if (e) {
               			
               			repaint();
               			mPage.notifyDataChanged();
               		}
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        builder.create().show();;
        
		
	}
	private void repaint(){
		//TODO: si queremos añadir lineas entre los puntos
		//addLines();
	}
}
