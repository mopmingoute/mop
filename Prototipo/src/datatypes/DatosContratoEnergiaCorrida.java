/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosContratoEnergiaCorrida is part of MOP.
 *
 * MOP is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * MOP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MOP. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package datatypes;

import java.io.Serializable;
import java.util.ArrayList;

import tiempo.Evolucion;

public class DatosContratoEnergiaCorrida implements Serializable {
	private static final long serialVersionUID = 1L;
	private String nombre;
	private ArrayList<String> involucrados; /* Lista de participantes que contribuyen al impacto */
	private Evolucion<Double> precioBase;
	private Evolucion<Double> energiaBase;
	private String fechaInicial; 
	private int cantAnios; // cantidad de a�os a partir de la fecha inicial
	private double energiaInicial; // energ�a acumulada en el per�odo anual vigente al inicio de la corrida
	private String tipo;    
	/**
	 * Por ahora el �nico es LIM_ENERGIA_ANUAL = "contratoLimEnergia"
	 * El precio del contrato es el precio base mientras la energ�a anual se menor que
	 * la energiaBase y cuando se excede esa cantidad es el costo marginal con cotas
	 * inferior y superior.
	 * Los per�odos anuales comienzan en la fechaInicial y se repiten por cantAnios
	 * 
	 */
	private Evolucion<Double> cotaInf;

	private Evolucion<Double> cotaSup;
	private boolean salidaDetallada;
	
	
	
	
	public DatosContratoEnergiaCorrida(String nombre, ArrayList<String> involucrados,
			Evolucion<Double> precioBase, Evolucion<Double> energiaBase, String fechaInicial, int cantAnios,
			double energiaInicial, String tipo, Evolucion<Double> cotaInf, Evolucion<Double> cotaSup,
			boolean salidaDetallada) {
		super();
		this.nombre = nombre;
		this.involucrados = involucrados;
		this.precioBase = precioBase;
		this.energiaBase = energiaBase;
		this.fechaInicial = fechaInicial;
		this.cantAnios = cantAnios;
		this.energiaInicial = energiaInicial;
		this.tipo = tipo;
		this.cotaInf = cotaInf;
		this.cotaSup = cotaSup;
		this.salidaDetallada = salidaDetallada;
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	
	public ArrayList<String> getInvolucrados() {
		return involucrados;
	}
	public void setInvolucrados(ArrayList<String> nombresInvolucrados) {
		this.involucrados = nombresInvolucrados;
	}
	public Evolucion<Double> getPrecioBase() {
		return precioBase;
	}
	public void setPrecioBase(Evolucion<Double> precioBase) {
		this.precioBase = precioBase;
	}
	public Evolucion<Double> getEnergiaBase() {
		return energiaBase;
	}
	public void setEnergiaBase(Evolucion<Double> energiaBase) {
		this.energiaBase = energiaBase;
	}
	public String getFechaInicial() {
		return fechaInicial;
	}
	public void setFechaInicial(String fechaInicial) {
		this.fechaInicial = fechaInicial;
	}
	public int getCantAnios() {
		return cantAnios;
	}
	public void setCantAnios(int cantAnios) {
		this.cantAnios = cantAnios;
	}
	public double getEnergiaInicial() {
		return energiaInicial;
	}
	public void setEnergiaInicial(double energiaInicial) {
		this.energiaInicial = energiaInicial;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public Evolucion<Double> getCotaInf() {
		return cotaInf;
	}
	public void setCotaInf(Evolucion<Double> cotaInf) {
		this.cotaInf = cotaInf;
	}
	public Evolucion<Double> getCotaSup() {
		return cotaSup;
	}
	public void setCotaSup(Evolucion<Double> cotaSup) {
		this.cotaSup = cotaSup;
	}
	public boolean isSalDetallada() {
		return salidaDetallada;
	}
	public void setSalDetallada(boolean salidaDetallada) {
		this.salidaDetallada = salidaDetallada;
	}
	
	public boolean isSalidaDetallada() {
		return salidaDetallada;
	}

	public void setSalidaDetallada(boolean salidaDetallada) {
		this.salidaDetallada = salidaDetallada;
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(nombre.trim().equals("")) { errores.add("Contrato Combustible: Nombre vacío."); }
		if(involucrados == null) { errores.add("Contrato " +  nombre + ": involucrados vacío."); }
		else {
			involucrados.forEach((v) -> { if (v.trim().equals("")) errores.add("Contrato " +  nombre + ": involucrados vacío."); });
		}

		if(fechaInicial.trim().equals("")) { errores.add("Contrato " +  nombre + ": fecha inicial vacío."); }

		if(cantAnios == 0) { errores.add("Contrato " +  nombre + ": cantidad años vacío."); }

		if(energiaInicial == 0 ) { errores.add("Contrato " +  nombre + ": cantidad años vacío."); }
		if(energiaBase == null) { errores.add("Contrato " +  nombre + ": energia base vacío."); }
		else if(energiaBase.controlDatosCompletos().size() >0 ){  errores.add("Contrato " +  nombre + ": energia base vacío."); }

		if(precioBase == null) { errores.add("Contrato " +  nombre + ": precio base vacío."); }
		else if(precioBase.controlDatosCompletos().size() >0 ){  errores.add("Contrato " +  nombre + ": precio base vacío."); }

		if(tipo.trim().equals("")){ errores.add("Contrato " +  nombre + ": tipo vacío."); }

		return errores;
	}

    // OJOJOJOJOJOJO private static ArrayList<String> atributosDetallados;
	
	
	

}
