/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCicloCombParte is part of MOP.
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
import java.util.Hashtable;

import tiempo.Evolucion;

/**
 * Datatype que representa los datos de un generador tórmico
 * @author ut602614
 *
 */
public class DatosCicloCombParte implements Serializable{

	private static final long serialVersionUID = 1L;
	private Evolucion<Integer> cantModInst;
	private Evolucion<Double> potMin;
	private Evolucion<Double> potMax;

	private Hashtable<String,Evolucion<Double>> rendimientosPotMin;	
	private Hashtable<String,Evolucion<Double>> rendimientosPotMax;
	private String flexibilidadMin;
	private Integer cantModIni;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	private Evolucion<Integer> mantProgramado;
	private Evolucion<Double> costoFijo;
	private Evolucion<Double> costoVariable;
	
	
	
	public DatosCicloCombParte(Evolucion<Integer> cantModInst,
			Evolucion<Double> potMin, Evolucion<Double> potMax,
			Hashtable<String,Evolucion<Double>> rendimientosPotMax, Hashtable<String,Evolucion<Double>> rendimientosPotMin,
			Integer cantModIni, Evolucion<Double> dispMedia, Evolucion<Double> tMedioArreglo, 
			Evolucion<Integer> mantProgramado, Evolucion<Double> costoFijo, Evolucion<Double> costoVariable) {
		super();
		this.cantModInst = cantModInst;

		this.potMax = potMax;
		this.potMin = potMin;

		this.rendimientosPotMax = rendimientosPotMax;
		this.rendimientosPotMin = rendimientosPotMin;
		this.cantModIni = cantModIni;
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;

		this.mantProgramado = mantProgramado;
		this.setCostoFijo(costoFijo);
		this.setCostoVariable(costoVariable);
	}
	
	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}
	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
	}
	public Evolucion<Double> getPotMin() {
		return potMin;
	}
	public void setPotMin(Evolucion<Double> potMin) {
		this.potMin = potMin;
	}
	public Evolucion<Double> getPotMax() {
		return potMax;
	}
	public void setPotMax(Evolucion<Double> potMax) {
		this.potMax = potMax;
	}
	
	public Hashtable<String, Evolucion<Double>> getRendimientosPotMin() {
		return rendimientosPotMin;
	}

	public void setRendimientosPotMin(Hashtable<String, Evolucion<Double>> rendimientosPotMin) {
		this.rendimientosPotMin = rendimientosPotMin;
	}

	public Hashtable<String, Evolucion<Double>> getRendimientosPotMax() {
		return rendimientosPotMax;
	}

	public void setRendimientosPotMax(Hashtable<String, Evolucion<Double>> rendimientosPotMax) {
		this.rendimientosPotMax = rendimientosPotMax;
	}
	
	public String getFlexibilidadMin() {
		return flexibilidadMin;
	}
	public void setFlexibilidadMin(String flexibilidadMin) {
		this.flexibilidadMin = flexibilidadMin;
	}

	public Integer getCantModIni() {
		return cantModIni;
	}
	public void setCantModIni(Integer cantModIni) {
		this.cantModIni = cantModIni;
	}

	public Evolucion<Double> getDispMedia() {
		return dispMedia;
	}
	public void setDispMedia(Evolucion<Double> dispMedia) {
		this.dispMedia = dispMedia;
	}
	public Evolucion<Double> gettMedioArreglo() {
		return tMedioArreglo;
	}
	public void settMedioArreglo(Evolucion<Double> tMedioArreglo) {
		this.tMedioArreglo = tMedioArreglo;
	}

	public Evolucion<Integer> getMantProgramado() {
		return mantProgramado;
	}
	public void setMantProgramado(Evolucion<Integer> mantProgramado) {
		this.mantProgramado = mantProgramado;
	}
	public Evolucion<Double> getCostoFijo() {
		return costoFijo;
	}
	public void setCostoFijo(Evolucion<Double> costoFijo) {
		this.costoFijo = costoFijo;
	}
	public Evolucion<Double> getCostoVariable() {
		return costoVariable;
	}
	public void setCostoVariable(Evolucion<Double> costoVariable) {
		this.costoVariable = costoVariable;
	}
	

}
