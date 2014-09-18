package com.example.com.benasque2014.mercurio.connections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.com.benasque2014.mercurio.model.Recorrido;
import com.example.com.benasque2014.mercurio.model.SLatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Parsers {
	public static List<Recorrido> parseRecorridos(JsonArray data){
		List<Recorrido> list=new ArrayList<Recorrido>();
		Iterator<JsonElement> it = data.iterator();
		Recorrido r;
		while (it.hasNext()){
			try{
			JsonObject jr = it.next().getAsJsonObject();
			
			r=new Recorrido();
			r.setName(jr.get("Nombre").getAsString());
			r.setCodigo(jr.get("Codigo").getAsString());
			r.setClase(jr.get("Clase").getAsString());
			r.setHoraInicio(jr.get("HoraInicio").getAsString());
			r.setHoraFin(jr.get("HoraFin").getAsString());
			r.setFrecuencia(jr.get("FrecuenciaDePaso").getAsString());
			String[] sFrecuencias=jr.get("Periodicidad").getAsString().split(":");
			boolean[] frecs=new boolean[7];
			frecs[0]=sFrecuencias[0].equalsIgnoreCase("1")?true:false;
			frecs[1]=sFrecuencias[1].equalsIgnoreCase("1")?true:false;
			frecs[2]=sFrecuencias[2].equalsIgnoreCase("1")?true:false;
			frecs[3]=sFrecuencias[3].equalsIgnoreCase("1")?true:false;
			frecs[4]=sFrecuencias[4].equalsIgnoreCase("1")?true:false;
			frecs[5]=sFrecuencias[5].equalsIgnoreCase("1")?true:false;
			frecs[6]=sFrecuencias[6].equalsIgnoreCase("1")?true:false;
			r.setTrayectoPeriodico(frecs);
			List<SLatLng> paradas=new ArrayList<SLatLng>();
			String[] sParadas = jr.get("ListaDeParadas").getAsString().split(":");
			int i=0;
			while (i<sParadas.length){
				SLatLng point=new SLatLng(Double.parseDouble(sParadas[i].split(",")[0]), Double.parseDouble(sParadas[i].split(",")[1]));
				paradas.add(point);
				i++;
			}
			r.setPuntos(paradas);
			try {
				r.setIncidencia(jr.get("Incidencias").getAsString());
			} catch (Exception e){
				r.setIncidencia("");
			}
			list.add(r);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return list;
	}
}
