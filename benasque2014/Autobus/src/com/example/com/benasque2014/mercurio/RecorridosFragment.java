package com.example.com.benasque2014.mercurio;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.benasque2014.mercurio.connections.CCPClient;
import com.example.com.benasque2014.mercurio.connections.CCPClient.CCPClientHandle;
import com.example.com.benasque2014.mercurio.connections.Parsers;
import com.example.com.benasque2014.mercurio.model.Recorrido;
import com.example.com.benasque2014.mercurio.utils.Utils;
import com.google.gson.JsonArray;

public class RecorridosFragment extends Fragment implements OnItemClickListener {

	private ListView list;
	private View loading;
	private View emptyView;

	private List<Recorrido> recorridos;
	public RecorridosFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_recorridos,
				container, false);
		list=(ListView) rootView.findViewById(R.id.list);
		list.setEmptyView(rootView.findViewById(R.id.textEmpty));
		loading=rootView.findViewById(R.id.progress);
		emptyView=rootView.findViewById(R.id.textEmpty);
		list.setAdapter(new RecorridosAdapter(getActivity(), new ArrayList<Recorrido>(), true));
		list.setOnItemClickListener(this);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.recorridos, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.action_add) {
			startActivity(new Intent(getActivity().getApplicationContext(),AddRecorridoActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		startTaskGetRecorridos();
	}

	
	@Override
	public void onPause() {
		//taskRecorridos.cancel(true);
		super.onPause();
	}

	private void startTaskGetRecorridos(){
		emptyView.setVisibility(View.GONE);
		loading.setVisibility(View.VISIBLE);
		list.setVisibility(View.GONE);
		
		if (!Utils.isOnline(getActivity())){
			emptyView.setVisibility(View.VISIBLE);
			((TextView)emptyView).setText("Compruebe su conexión antes de continuar");
			loading.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
			return;
		}
		CCPClient.getRutas(new CCPClientHandle() {

			@Override
			public void result(boolean error, JsonArray data) {
				emptyView.setVisibility(View.VISIBLE);
				loading.setVisibility(View.GONE);
				list.setVisibility(View.VISIBLE);
				
				if (data==null) {
					if (getActivity()!=null){ // Por si hemos vuelto atras
						Toast.makeText(getActivity(), "No internet", Toast.LENGTH_SHORT).show();
						startTaskGetRecorridos();
					}
				} else {
					List<Recorrido> parseData = Parsers.parseRecorridos(data);
					list.setAdapter(new RecorridosAdapter(getActivity(), parseData, true));
				}
			}
		});
	}
	

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Recorrido r=recorridos.get(arg2);
		Log.v("r", r.toString());
	}
}