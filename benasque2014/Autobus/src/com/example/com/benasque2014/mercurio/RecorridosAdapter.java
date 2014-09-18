package com.example.com.benasque2014.mercurio;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.benasque2014.mercurio.connections.CCPClient;
import com.example.com.benasque2014.mercurio.model.Recorrido;
import com.example.com.benasque2014.mercurio.utils.Utils;

public class RecorridosAdapter extends ArrayAdapter<Recorrido> {
	private final Activity context;
	private final List<Recorrido> recorridos;
	private boolean canEdit;

	static class ViewHolder {
		public TextView text;
		public ImageView image;
	}

	public RecorridosAdapter(Activity context, List<Recorrido> recorridos,
			boolean canEdit) {
		super(context, R.layout.list_item_recorrido);
		this.context = context;
		this.recorridos = recorridos;
		this.canEdit = canEdit;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.list_item_recorrido, null);
			// configure view holder
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.text);
			viewHolder.image = (ImageView) rowView
					.findViewById(R.id.delete_button);
			rowView.setTag(viewHolder);
		}

		final int pos = position;
		// fill data
		ViewHolder holder = (ViewHolder) rowView.getTag();
		final Recorrido r = recorridos.get(position);
		holder.text.setText(r.getName());
		holder.image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getContext());
				builder.setMessage(
						"¿Seguro que quieres eliminar la ruta?")
						.setCancelable(false)
						.setPositiveButton("Si",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										if (Utils.isOnline(getContext())){
											CCPClient.delRuta(r.getCodigo(), null);
											recorridos.remove(pos);
											notifyDataSetChanged();
										} else {
											Toast.makeText(getContext(), "No internet.", Toast.LENGTH_SHORT).show();
										}
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
		if (canEdit)
			holder.image.setVisibility(View.VISIBLE);
		else
			holder.image.setVisibility(View.GONE);

		holder.text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle b = new Bundle();
				b.putSerializable(Recorrido.KEY, r);
				if (canEdit){
					Intent i = new Intent(context, SendLocationActivity.class);
					i.putExtras(b);
					context.startActivity(i);
				} else {
					Intent i=new Intent(context, FamiliaMapaActivity.class);
					i.putExtras(b);
					context.startActivity(i);
				}
			}
		});

		return rowView;
	}

	@Override
	public int getCount() {
		if (recorridos == null)
			return 0;
		return recorridos.size();
	}

	@Override
	public Recorrido getItem(int position) {
		if (recorridos == null)
			return null;
		return recorridos.get(position);
	}

}
