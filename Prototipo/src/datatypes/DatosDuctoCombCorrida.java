/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosDuctoCombCorrida is part of MOP.
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



/**
 * Datatype que representa los datos de un ducto de combustible
 * @author ut602614
 *
 */
public class DatosDuctoCombCorrida implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String nombre;				/**Nombre del ducto*/
	private Evolucion<Integer> cantModInst;	
	private Evolucion<Integer> mantProgramado;
	private String barra1;				/**Barra asociada al ducto*/
	private String barra2;				/**Barra asociada al ducto*/
	private Evolucion<Double> capacidad12; 		/**Expresada en unidad de combustible por unidad de tiempo*/
	private Evolucion<Double> capacidad21; 		/**Expresada en unidad de combustible por unidad de tiempo*/
	private Evolucion<Double> perdidas12;  		/**Perdidas en x1 de la cantidad que sale de 1*/
	private Evolucion<Double> perdidas21;  
	private Integer cantModIni;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	private Evolucion<Double> costoFijo;
	
	
	private boolean salDetallada;
	
	
	
	public DatosDuctoCombCorrida(String nombre, Evolucion<Integer> cantModInst,
			String barra1, String barra2, Evolucion<Double> capacidad12,
			Evolucion<Double> capacidad21, Evolucion<Double> perdidas12,
			Evolucion<Double> perdidas21, Integer cantModIni,
			Evolucion<Double> dispMedia, Evolucion<Double> tMedioArreglo, boolean salDetallada, Evolucion<Integer> mantProgramado, Evolucion<Double> costoFijo) {
		super();
		this.nombre = nombre;
		this.cantModInst = cantModInst;
		this.barra1 = barra1;
		this.barra2 = barra2;
		this.capacidad12 = capacidad12;
		this.capacidad21 = capacidad21;
		this.perdidas12 = perdidas12;
		this.perdidas21 = perdidas21;
		this.cantModIni = cantModIni;
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;
		this.salDetallada = salDetallada;
		this.mantProgramado = mantProgramado;
		this.setCostoFijo(costoFijo);
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}
	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
	}
	public String getBarra1() {
		return barra1;
	}
	public void setBarra1(String barra1) {
		this.barra1 = barra1;
	}
	public String getBarra2() {
		return barra2;
	}
	public void setBarra2(String barra2) {
		this.barra2 = barra2;
	}
	public Evolucion<Double> getCapacidad12() {
		return capacidad12;
	}
	public void setCapacidad12(Evolucion<Double> capacidad12) {
		this.capacidad12 = capacidad12;
	}
	public Evolucion<Double> getCapacidad21() {
		return capacidad21;
	}
	public void setCapacidad21(Evolucion<Double> capacidad21) {
		this.capacidad21 = capacidad21;
	}
	public Evolucion<Double> getPerdidas12() {
		return perdidas12;
	}
	public void setPerdidas12(Evolucion<Double> perdidas12) {
		this.perdidas12 = perdidas12;
	}
	public Evolucion<Double> getPerdidas21() {
		return perdidas21;
	}
	public void setPerdidas21(Evolucion<Double> perdidas21) {
		this.perdidas21 = perdidas21;
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
	public boolean isSalDetallada() {
		return salDetallada;
	}
	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
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


	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(nombre.trim().equals("")) { errores.add("Ducto Combustible: Nombre vacío."); }

		if(cantModInst == null ) { errores.add("Ducto Combustible: " +nombre+ " cantModInst vacío."); }
		else {
			if(cantModInst.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " cantModInst vacío."); }
		}


		if(mantProgramado == null ) { errores.add("Ducto Combustible: " +nombre+ " mantProgramado vacío."); }
		else {
			if(mantProgramado.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " mantProgramado vacío."); }
		}

		if(barra1.trim().equals("")) { errores.add("Ducto Combustible " +nombre+": barra1 vacío."); }
		if(barra2.trim().equals("")) { errores.add("Ducto Combustible " +nombre+": barra2 vacío."); }

		if(capacidad12 == null ) { errores.add("Ducto Combustible: " +nombre+ " capacidad12 vacío."); }
		else {
			if(capacidad12.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " capacidad12 vacío."); }
		}

		if(capacidad21 == null ) { errores.add("Ducto Combustible: " +nombre+ " capacidad21 vacío."); }
		else {
			if(capacidad21.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " capacidad21 vacío."); }
		}

		if(perdidas12 == null ) { errores.add("Ducto Combustible: " +nombre+ " perdidas12 vacío."); }
		else {
			if(perdidas12.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " perdidas12 vacío."); }
		}

		if(perdidas21 == null ) { errores.add("Ducto Combustible: " +nombre+ " perdidas21 vacío."); }
		else {
			if(perdidas21.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " perdidas21 vacío."); }
		}

		if(cantModIni == null || cantModIni == 0) { errores.add("Ducto Combustible: " +nombre+ " cantModIni vacío."); }

		if(dispMedia == null ) { errores.add("Ducto Combustible: " +nombre+ " dispMedia vacío."); }
		else {
			if(dispMedia.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " dispMedia vacío."); }
		}

		if(tMedioArreglo == null ) { errores.add("Ducto Combustible: " +nombre+ " tMedioArreglo vacío."); }
		else {
			if(tMedioArreglo.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " tMedioArreglo vacío."); }
		}

		if(costoFijo == null ) { errores.add("Ducto Combustible: " +nombre+ " costoFijo vacío."); }
		else {
			if(costoFijo.controlDatosCompletos().size() > 0)  { errores.add("Ducto Combustible: " +nombre+ " costoFijo vacío."); }
		}

		return errores;

	}
}

