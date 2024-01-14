/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CompGeneral is part of MOP.
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

package compgeneral;

import compdespacho.CompDespacho;
import compsimulacion.CompSimulacion;
import parque.Participante;
import tiempo.Evolucion;

import java.util.*;

import control.VariableControl;
import control.VariableControlDE;
import estado.VariableEstado;

public abstract class CompGeneral {
	private Participante participante;
	private static Hashtable<String, String> compsGlobales;
	private Hashtable<String, Evolucion<String>> evolucionComportamientos; // Evolucion de los comportamientos generales
																			// par
																			// (varComportamiento,valComportamiento), la
																			// foto estó en comp. Simulación
	private ArrayList<VariableEstado> varsEstadoSimulacion; // Variables de estado de simulación activas en el paso
	private ArrayList<VariableEstado> varsEstadoOptimizacion; // Variables de estado de optimización activas en el paso
	private ArrayList<VariableControl> varsControl; // Variables de control activas en el paso
	private ArrayList<VariableControlDE> varsControlDE; // Variables de controlDE activas en el paso
	private CompSimulacion compSimulacion;
	private CompDespacho compDespacho;

	public Hashtable<String, Evolucion<String>> getEvolucionComportamientos() {
		return evolucionComportamientos;
	}

	public void setEvolucionComportamientos(Hashtable<String, Evolucion<String>> evolucionComportamientos) {
		this.evolucionComportamientos = evolucionComportamientos;
	}

	public CompGeneral() {
		this.evolucionComportamientos = new Hashtable<String, Evolucion<String>>();
		this.varsControl = new ArrayList<VariableControl>();
		this.varsControlDE = new ArrayList<VariableControlDE>();
		this.setVarsEstadoSimulacion(new ArrayList<VariableEstado>());
		this.setVarsEstadoOptimizacion(new ArrayList<VariableEstado>());

	}

	public Participante getParticipante() {
		return participante;
	}

	public void setParticipante(Participante participante) {
		this.participante = participante;
	}

	public CompSimulacion getCompSimulacion() {
		return compSimulacion;
	}

	public void setCompSimulacion(CompSimulacion compSimulacion) {
		this.compSimulacion = compSimulacion;
	}

	public CompDespacho getCompDespacho() {
		return compDespacho;
	}

	public void setCompDespacho(CompDespacho compDespacho) {
		this.compDespacho = compDespacho;
	}

	public Hashtable<String, String> getFotoComportamientos(long instante) {
		if (evolucionComportamientos == null)
			return null;
		Hashtable<String, String> resultado = new Hashtable<String, String>();
		Set<String> claves = evolucionComportamientos.keySet();
		Iterator<String> it = claves.iterator();

		while (it.hasNext()) {
			String clave = it.next();
			resultado.put(clave, evolucionComportamientos.get(clave).getValor(instante));
		}
		return resultado;
	}

	public static Hashtable<String, String> getCompsGlobales() {
		return compsGlobales;
	}

	public static void setCompsGlobales(Hashtable<String, String> compsGlobales) {
		CompGeneral.compsGlobales = compsGlobales;
	}

	public ArrayList<VariableControl> getVarsControl() {
		return varsControl;
	}

	public void setVarsControl(ArrayList<VariableControl> varsControl) {
		this.varsControl = varsControl;
	}

	public ArrayList<VariableControlDE> getVarsControlDE() {
		return varsControlDE;
	}

	public void setVarsControlDE(ArrayList<VariableControlDE> varsControlDE) {
		this.varsControlDE = varsControlDE;
	}

	public ArrayList<VariableEstado> getVarsEstadoOptimizacion() {
		return varsEstadoOptimizacion;
	}

	public void setVarsEstadoOptimizacion(ArrayList<VariableEstado> varsEstadoOptimizacion) {
		this.varsEstadoOptimizacion = varsEstadoOptimizacion;
	}

	public ArrayList<VariableEstado> getVarsEstadoSimulacion() {
		return varsEstadoSimulacion;
	}

	public void setVarsEstadoSimulacion(ArrayList<VariableEstado> varsEstadoSimulacion) {
		this.varsEstadoSimulacion = varsEstadoSimulacion;
	}

}
