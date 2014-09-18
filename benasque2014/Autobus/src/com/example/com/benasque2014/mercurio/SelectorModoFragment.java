package com.example.com.benasque2014.mercurio;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * Selector del modo de uso de la app
 */
public class SelectorModoFragment extends Fragment {

	public SelectorModoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_seleccion,
				container, false);
		rootView.findViewById(R.id.selectbus).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (KeyStoreController.getKeyStore()
								.getString("transmitiendo", "").length() > 0) {
							Intent i = new Intent(getActivity(),
									SendLocationActivity.class);
							startActivity(i);
						} else {

							startActivity(new Intent(getActivity()
									.getApplicationContext(), BusActivity.class));
						}
					}
				});
		rootView.findViewById(R.id.selectfamilia).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						startActivity(new Intent(getActivity()
								.getApplicationContext(),
								FamiliaListaRutasActivity.class));
					}
				});
		return rootView;
	}
}
