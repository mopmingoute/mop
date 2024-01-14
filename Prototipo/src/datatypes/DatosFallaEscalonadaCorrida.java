/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosFallaEscalonadaCorrida is part of MOP.
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

import logica.CorridaHandler;
import tiempo.Evolucion;
import utilitarios.Constantes;

/**
 * Datatype que representa los datos asociados a la falla escalonada
 * @author ut602614
 *
 */

public class DatosFallaEscalonadaCorrida implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String nombre;
	private Evolucion<String> compFalla;
	private Hashtable<String,Evolucion<String>> valsComps;

	private String demanda;
	private ArrayList<Pair<Double, Double>> escalones;
	private int cantEscProgram;
	//private int[] durMinForzSeg;
	private Hashtable<String, DatosVariableEstado> varsEstado;
	private Hashtable<String, DatosVariableControlDE> varsControlDE;
	
	private boolean salDetallada;
	
	public DatosFallaEscalonadaCorrida(String nombre, Evolucion<String> compFalla,Hashtable<String, Evolucion<String>> valsComps,		 
			 String demanda, ArrayList<Pair<Double, Double>> escalones,int cantEscProgram, int[] durMinForzDia,
			 Hashtable<String, DatosVariableEstado> varsEstado, Hashtable<String, DatosVariableControlDE> varsControlDE, boolean salDetallada) {
		super();
		this.nombre = nombre;
		this.compFalla = compFalla;
		this.valsComps = valsComps;		
		this.demanda = demanda;
		this.escalones = escalones;
		this.salDetallada = salDetallada;
		this.cantEscProgram = cantEscProgram;
	//this.durMinForzSeg = durMinForzDia;
		// Cambio de unidades
		/*for(int i =0; i<durMinForzDia.length; i++){
			durMinForzSeg[i]= durMinForzDia[i]*Constantes.SEGUNDOSXDIA;
		}*/
		this.setVarsEstado(varsEstado);
		this.setVarsControlDE(varsControlDE);
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Evolucion<String> getCompFalla() {
		return compFalla;
	}
	public void setCompFalla(Evolucion<String> compFalla) {
		this.compFalla = compFalla;
	}
	public Hashtable<String, Evolucion<String>> getValsComps() {
		return valsComps;
	}
	public void setValsComps(Hashtable<String, Evolucion<String>> valsComps) {
		this.valsComps = valsComps;
	}

	public String getDemanda() {
		return demanda;
	}
	public void setDemanda(String demanda) {
		this.demanda = demanda;
	}
	public ArrayList<Pair<Double, Double>> getEscalones() {
		return escalones;
	}
	public void setEscalones(ArrayList<Pair<Double, Double>> escalones) {
		this.escalones = escalones;
	}

	public boolean isSalDetallada() {
		return salDetallada;
	}

	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}

	public int getCantEscProgram() {
		return cantEscProgram;
	}

	public void setCantEscProgram(int cantEscProgram) {
		this.cantEscProgram = cantEscProgram;
	}

	/*public int[] getDurMinForzSeg() {
		return durMinForzSeg;
	}

	public void setDurMinForzSeg(int[] durMinForzSeg) {
		this.durMinForzSeg = durMinForzSeg;
	}*/

	public Hashtable<String, DatosVariableEstado> getVarsEstado() {
		return varsEstado;
	}

	public void setVarsEstado(Hashtable<String, DatosVariableEstado> varsEstado) {
		this.varsEstado = varsEstado;
	}

	public Hashtable<String, DatosVariableControlDE> getVarsControlDE() {
		return varsControlDE;
	}

	public void setVarsControlDE(Hashtable<String, DatosVariableControlDE> varsControlDE) {
		this.varsControlDE = varsControlDE;
	}


    public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(nombre.trim().equals("")) { errores.add("Falla: Nombre vacío."); }
		if(cantEscProgram == 0){ errores.add("Falla: Cant escalones prog. vacío."); }
	//	if(durMinForzSeg == null) { errores.add("Falla: Duración mínima forzamientos vacío."); }
		if(escalones == null)  { errores.add("Falla: Escalones porciento vacío."); }
		else {
			escalones.forEach((v) -> {
				if (v.first == null) errores.add("Falla: Escalones porciento vacío.");
			});
		}
		if(demanda.trim().equals("")) { errores.add("Falla: Demanda vacío."); }

		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		if(compFalla == null){ errores.add("Falla: compFalla vacío."); }
		else {
			if (compFalla.controlDatosCompletos().size() > 0 ) { errores.add("Falla: compFalla vacío.");}
			if(!compFalla.getValor(instanteActual).equals(Constantes.FALLASINESTADO)){
				if(varsEstado == null ) { errores.add("Falla: varsEstado vacío."); }
				else {
					varsEstado.forEach((k, v) -> {
						if (v.controlDatosCompletos().size() > 0) errores.add("Falla: varsEstado vacío.");
					});
				}
				/*if(varsControlDE == null ) { errores.add("Falla: varsControlDE vacío."); }
				else {
					varsControlDE.forEach((k, v) -> {
						if (v.controlDatosCompletos().size() > 0) errores.add("Falla: varsControlDE vacío.");
					});
				}*/
			}
		}


		return errores;
    }
}