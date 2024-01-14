/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTermicoCorrida is part of MOP.
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
import java.util.Hashtable;

import tiempo.Evolucion;

/**
 * Datatype que representa los datos de un generador tórmico
 * @author ut602614
 *
 */
public class DatosTermicoCorrida implements Serializable{	
	private static final long serialVersionUID = 1L;
	private String nombre;
	private String propietario;
	private String barra;
	private ArrayList<String> listaCombustibles;
	private Hashtable<String,String> combustiblesBarras;
	private Evolucion<Integer> cantModInst;
	private Evolucion<Double> potMin;
	private Evolucion<Double> potMax;
	private Hashtable<String,Evolucion<Double>> rendimientosPotMin;
	private Hashtable<String,Evolucion<Double>> rendimientosPotMax;
	private String flexibilidadMin;
	private Hashtable<String,Evolucion<String>> valoresComportamientos;
	private Integer cantModIni;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	private boolean salDetallada;
	private Evolucion<Integer> mantProgramado;
	private Evolucion<Double> costoFijo;
	private Evolucion<Double> costoVariable;
	
	
	public DatosTermicoCorrida(String nombre, String propietario, String barra,
			Evolucion<Integer> cantModInst,
			ArrayList<String> listaCombustibles,
			Hashtable<String, String> combustiblesBarras,
			Evolucion<Double> potMin, Evolucion<Double> potMax,
			Hashtable<String,Evolucion<Double>> rendPotMax, Hashtable<String,Evolucion<Double>> rendPotMin,
			String flexibilidadMin, Integer cantModIni,
			Evolucion<Double> dispMedia, Evolucion<Double> tMedioArreglo, boolean salDetallada, Evolucion<Integer> mantProgramado, Evolucion<Double> costoFijo, Evolucion<Double> costoVariable) {
		super();
		this.nombre = nombre;
		this.propietario = propietario;
		this.barra = barra;
		this.cantModInst = cantModInst;
		this.listaCombustibles = listaCombustibles;
		this.combustiblesBarras = combustiblesBarras;
		this.potMax = potMax;
		this.potMin = potMin;
		this.rendimientosPotMax = rendPotMax;
		this.rendimientosPotMin = rendPotMin;
		this.flexibilidadMin = flexibilidadMin;
		this.cantModIni = cantModIni;
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;
		this.salDetallada = salDetallada;
		this.valoresComportamientos = new Hashtable<String, Evolucion<String>>();
		this.mantProgramado = mantProgramado;
		this.setCostoFijo(costoFijo);
		this.setCostoVariable(costoVariable);
	}
	
	
	/**
	 * Se carga objetos vacíos en los atributos que no existen en al argumento 
	 * DatosCicloCombParte dp
	 * @param nombre
	 * @param dp
	 */
	public DatosTermicoCorrida(String nombre, String propietario, DatosCicloCombParte dp) {
		
		this.nombre = nombre;
		this.propietario = propietario;
		this.barra = null;		
		this.cantModInst = dp.getCantModInst();
		this.combustiblesBarras = new Hashtable<String, String>();
		this.listaCombustibles = new ArrayList<String>();
		this.potMax = dp.getPotMax();
		this.potMin = dp.getPotMin();
		this.rendimientosPotMax = dp.getRendimientosPotMax();
		this.rendimientosPotMin = dp.getRendimientosPotMin();
		this.flexibilidadMin = "";		
		this.cantModIni = dp.getCantModIni();
		this.dispMedia = dp.getDispMedia();
		this.tMedioArreglo = dp.gettMedioArreglo();
		this.salDetallada = false;
		this.valoresComportamientos = new Hashtable<String, Evolucion<String>>();
		this.mantProgramado = dp.getMantProgramado();
		this.costoFijo = dp.getCostoFijo();
		this.costoVariable = dp.getCostoVariable();
		
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

	public String getFlexibilidadMin() {
		return flexibilidadMin;
	}
	public void setFlexibilidadMin(String flexibilidadMin) {
		this.flexibilidadMin = flexibilidadMin;
	}
	public Hashtable<String, Evolucion<String>> getValoresComportamientos() {
		return valoresComportamientos;
	}
	public void setValoresComportamientos(
			Hashtable<String, Evolucion<String>> valoresComportamientos) {
		this.valoresComportamientos = valoresComportamientos;
	}
	public Integer getCantModIni() {
		return cantModIni;
	}
	public void setCantModIni(Integer cantModIni) {
		this.cantModIni = cantModIni;
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

	
	public ArrayList<String> getListaCombustibles() {
		return listaCombustibles;
	}
	public void setListaCombustibles(ArrayList<String> listaCombustibles) {
		this.listaCombustibles = listaCombustibles;
	}
	public Hashtable<String, String> getCombustiblesBarras() {
		return combustiblesBarras;
	}
	public void setCombustiblesBarras(Hashtable<String, String> combustiblesBarras) {
		this.combustiblesBarras = combustiblesBarras;
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
	public Evolucion<Double> getCostoVariable() {
		return costoVariable;
	}
	public void setCostoVariable(Evolucion<Double> costoVariable) {
		this.costoVariable = costoVariable;
	}

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}
	
    public ArrayList<String> controlDatosCompletos(Boolean esCicloCombinado, String nombre) {
		nombre = (esCicloCombinado) ? "Ciclo Combinado: " + nombre : "Termico: " + getNombre();
		ArrayList<String> errores = new ArrayList<>();

		if(nombre.trim().equals("") && !esCicloCombinado) errores.add(nombre + " Nombre vacío.");
		if(barra == null && !esCicloCombinado) errores.add(nombre + " Barra vacío.");

		if(cantModInst == null ){  errores.add(nombre + " cantModInst vacío."); }
		else if(cantModInst.controlDatosCompletos().size() > 0) {  errores.add(nombre + " cantModInst vacío."); }

		if( combustiblesBarras == null || combustiblesBarras.size() == 0 && !esCicloCombinado ) errores.add(nombre + " combustiblesBarras vacío.");
		if( listaCombustibles == null || listaCombustibles.size() == 0 && !esCicloCombinado ) errores.add(nombre + " listaCombustibles vacío.");

		if(potMax == null ) errores.add(nombre + " potMax vacío.");
		else if(potMax.controlDatosCompletos().size() > 0) {  errores.add(nombre + " potMax vacío."); }

		if(potMin == null ) errores.add(nombre + " potMin vacío.");
		else if(potMin.controlDatosCompletos().size() > 0) {  errores.add(nombre + " potMin vacío."); }

		if( rendimientosPotMax == null || rendimientosPotMax.size() == 0) errores.add(nombre + " rendimientosPotMax vacío.");
		else { rendimientosPotMax.forEach((k,v) -> {if (v.controlDatosCompletos().size()> 0)  errores.add("Ciclo Combinado:  rendimientosPotMax vacío.");  }); }

		if( rendimientosPotMin == null || rendimientosPotMax.size() == 0) errores.add(nombre + " rendimientosPotMin vacío.");
		else { rendimientosPotMin.forEach((k,v) -> {if (v.controlDatosCompletos().size()> 0)  errores.add("Ciclo Combinado:  rendimientosPotMin vacío.");  }); }

		if(flexibilidadMin.trim().equals("") && !esCicloCombinado) errores.add(nombre + " flexibilidadMin vacío.");
		if(cantModIni == null) errores.add(nombre + " cantModIni vacío.");
		if(dispMedia == null ) errores.add(nombre + " dispMedia vacío.");
		if(tMedioArreglo == null ) { errores.add(nombre + " dispMedia vacío.");}
		else if(tMedioArreglo.controlDatosCompletos().size() > 0 ) { errores.add(nombre + " dispMedia vacío."); }
		if( !esCicloCombinado && (valoresComportamientos == null || valoresComportamientos.size() == 0) ) errores.add(nombre + " valoresComportamientos vacío.");
		if(mantProgramado == null ) errores.add(nombre + " mantProgramado vacío.");
		else if(mantProgramado.controlDatosCompletos().size() > 0 ) { errores.add(nombre + " mantProgramado vacío."); }
		if(costoFijo == null ) errores.add(nombre + " costoFijo vacío.");
		else if(costoFijo.controlDatosCompletos().size() > 0 ) { errores.add(nombre + " costoFijo vacío."); }
		if(costoVariable == null ) errores.add(nombre + " costoVariable vacío.");
		else if(costoVariable.controlDatosCompletos().size() > 0 ) { errores.add(nombre + " costoVariable vacío."); }

		return errores;
	}


}
