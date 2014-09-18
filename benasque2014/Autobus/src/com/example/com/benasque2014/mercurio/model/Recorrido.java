package com.example.com.benasque2014.mercurio.model;

import java.io.Serializable;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Recorrido implements Serializable{
	private String name;
	private List<SLatLng> puntos;
	private String frecuencia;
	private String codigo;
	private String clase;
	private String horaInicio;
	private String horaFin;
	private boolean[] trayectoPeriodico;
	private String incidencia;
	
	public static String KEY="Recorrido";
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<SLatLng> getPuntos() {
		return puntos;
	}
	public void setPuntos(List<SLatLng> puntos) {
		this.puntos = puntos;
	}
	public String getFrecuencia() {
		return frecuencia;
	}
	public void setFrecuencia(String frecuencia) {
		this.frecuencia = frecuencia;
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getClase() {
		return clase;
	}
	public void setClase(String clase) {
		this.clase = clase;
	}
	public String getHoraInicio() {
		return horaInicio;
	}
	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}
	public String getHoraFin() {
		return horaFin;
	}
	public void setHoraFin(String horaFin) {
		this.horaFin = horaFin;
	}
	public boolean[] isTrayectoPeriodico() {
		return trayectoPeriodico;
	}
	public void setTrayectoPeriodico(boolean[] trayectoPeriodico) {
		this.trayectoPeriodico = trayectoPeriodico;
	}
	public String getIncidencia() {
		return incidencia;
	}
	public void setIncidencia(String incidencia) {
		this.incidencia = incidencia;
	}
	
	
}
