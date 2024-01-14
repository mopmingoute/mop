/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosContratoCombustibleCorrida is part of MOP.
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
import java.util.Collection;

import tiempo.Evolucion;
import utilitarios.Constantes;

/**
 * Datatype que representa un contrato de combustible
 * @author ut602614
 *
 */

public class DatosContratoCombustibleCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;				/**Nombre del Contrato*/
	private String barra;				/**Identificador de la barra asociada*/
	
	private String comb;
	private Evolucion<Integer> cantModInst;
	private Evolucion<Integer> mantProgramado;

	private Integer cantModIni;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;

	
	private Evolucion<Double> caudalMax;
	private Evolucion<Double> precioComb;
	
	private boolean salDetallada;
	private Evolucion<Double> costoFijo;
	
	
	
	public DatosContratoCombustibleCorrida(String nombre, String barra,
			String comb, Evolucion<Integer> cantModInst, Integer cantModIni,
			Evolucion<Double> dispMedia, Evolucion<Double> tMedioArreglo,
			Evolucion<Double> caudalMax, Evolucion<Double> precioComb, boolean salDetallada, Evolucion<Integer> mantProgramado, Evolucion<Double> costoFijo) {
		super();
		this.nombre = nombre;
		this.barra = barra;
		this.comb = comb;
		this.cantModInst = cantModInst;
		this.cantModIni = cantModIni;
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;
		this.caudalMax = caudalMax;
		this.precioComb = precioComb;
		this.mantProgramado = mantProgramado;
		this.salDetallada = salDetallada;
		this.setCostoFijo(costoFijo);
	}



	public String getNombre() {
		return nombre;
	}



	public void setNombre(String nombre) {
		this.nombre = nombre;
	}



	public String getBarra() {
		return barra;
	}



	public void setBarra(String barra) {
		this.barra = barra;
	}



	public String getComb() {
		return comb;
	}



	public void setComb(String comb) {
		this.comb = comb;
	}



	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}



	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
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



	public Evolucion<Double> getCaudalMax() {
		return caudalMax;
	}



	public void setCaudalMax(Evolucion<Double> caudalMax) {
		this.caudalMax = caudalMax;
	}



	public Evolucion<Double> getPrecioComb() {
		return precioComb;
	}



	public void setPrecioComb(Evolucion<Double> precioComb) {
		this.precioComb = precioComb;
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


		if(nombre.trim().equals("")) errores.add("Contrato Combustible " + nombre +": Nombre vacío.");
		if(barra == null) errores.add("Contrato Combustible " + nombre + ": Barra vacío.");
		if(comb.trim().equals("")) errores.add("Contrato Combustible " + nombre +": comb vacío.");
		if(cantModInst == null ) { errores.add("Contrato Combustible " + nombre +": cantModInst vacío."); }
		else {
			if(cantModInst.controlDatosCompletos().size() > 0)  errores.add("Contrato Combustible " + nombre +": cantModInst vacío.");
		}


		if(cantModIni == null ) { errores.add("Contrato Combustible " + nombre + ": cantModIni vacío."); }

		if(dispMedia == null ) { errores.add("Contrato Combustible " + nombre + ": dispMedia vacío."); }
		else {
			if(dispMedia.controlDatosCompletos().size() > 0)  errores.add("Contrato Combustible " + nombre +": dispMedia vacío.");
		}
		if(tMedioArreglo == null ) { errores.add("Contrato Combustible " + nombre + ": dispMedia vacío."); }
		else {
			if(tMedioArreglo.controlDatosCompletos().size() > 0)  errores.add("Contrato Combustible " + nombre +": tMedioArreglo vacío.");
		}
		if(mantProgramado == null ) { errores.add("Contrato Combustible "  + nombre + ": mantProgramado vacío."); }
		else {
			if(mantProgramado.controlDatosCompletos().size() > 0)  errores.add("Contrato Combustible " + nombre +": mantProgramado vacío.");
		}
		if(caudalMax == null ) { errores.add("Contrato Combustible "  + nombre + ": caudalMax vacío."); }
		else {
			if(caudalMax.controlDatosCompletos().size() > 0)  errores.add("Contrato Combustible " + nombre +": caudalMax vacío.");
		}
		if(precioComb == null ) { errores.add("Contrato Combustible "  + nombre + ": precioComb vacío."); }
		else {
			if(precioComb.controlDatosCompletos().size() > 0)  errores.add("Contrato Combustible " + nombre +": precioComb vacío.");
		}
		if(costoFijo == null ) { errores.add("Contrato Combustible " + nombre + ": costoFijo vacío."); }
		else {
			if(costoFijo.controlDatosCompletos().size() > 0)  errores.add("Contrato Combustible " + nombre +": costoFijo vacío.");
		}
		return errores;
	}
}

