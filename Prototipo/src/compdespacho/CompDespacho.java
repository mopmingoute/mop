/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CompDespacho is part of MOP.
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

package compdespacho;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import parque.Participante;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProblema.DatosVariableControl;

/**
 * Clase abstracta creada con el objetivo de modelar el comportamiento de los
 * participantes en el problema de despacho
 * 
 * @author ut602614
 * 
 *         TODO: ATENCION EL OBJETIVO ES EL COSTO EN EL INSTANTE MEDIO DEL PASO
 *         DE TIEMPO
 *
 */
public abstract class CompDespacho {

	protected static DatosSalidaProblemaLineal ultimoDespacho;
	protected Hashtable<String, DatosVariableControl> variablesControl;
	protected Hashtable<String, DatosRestriccion> restricciones;
	protected DatosObjetivo objetivo;
	protected Participante participante;
	protected Hashtable<String, String> parametros; // en esta colección se cargan las variables y valores de
													// comportamiento

	// nombres de las variables de control de potencia total del problema
	protected String[] npotp; // nombres de las potencias por poste


	public CompDespacho() {
		parametros = new Hashtable<String, String>();
		variablesControl = new Hashtable<String, DatosVariableControl>();
		restricciones = new Hashtable<String, DatosRestriccion>();
		objetivo = new DatosObjetivo();
	}

	public static DatosSalidaProblemaLineal getUltimoDespacho() {
		return ultimoDespacho;
	}

	public void setUltimoDespacho(DatosSalidaProblemaLineal ultimoDespacho) {
		CompDespacho.ultimoDespacho = ultimoDespacho;
	}

	public DatosObjetivo getObjetivo() {
		return objetivo;
	}

	public void setObjetivo(DatosObjetivo objetivo) {
		this.objetivo = objetivo;
	}

	public Participante getParticipante() {
		return participante;
	}

	public void setParticipante(Participante participante) {
		this.participante = participante;
	}

	public void resetearVariablesControl() {
		this.variablesControl = new Hashtable<String, DatosVariableControl>();
	}

	public void resetearRestricciones() {
		this.restricciones = new Hashtable<String, DatosRestriccion>();
	}

	public void resetearObjetivo() {
		this.objetivo = new DatosObjetivo();

	}

	public abstract void crearVariablesControl();

	public abstract void cargarRestricciones();

	public abstract void contribuirObjetivo();

	public String generarNombre(String string) {
		return this.participante.getNombre() + "_" + string;
	}

	public String generarNombre(String string, String ip) {
		return string + "_" + this.participante.getNombre() + "_" + ip;
	}

	public String generarNombre(String string, String ib, String ip) {
		return string + "_" + this.participante.getNombre() + "_" + ib + "_" + ip;
	}

	public Hashtable<String, DatosVariableControl> getVariablesControl() {
		return variablesControl;
	}

	public void setVariablesControl(Hashtable<String, DatosVariableControl> variablesControl) {
		this.variablesControl = variablesControl;
	}

	public Hashtable<String, DatosRestriccion> getRestricciones() {
		return restricciones;
	}

	public void setRestricciones(Hashtable<String, DatosRestriccion> restricciones) {
		this.restricciones = restricciones;
	}

	public DatosObjetivo getContribucionObjetivo() {

		return this.objetivo;
	}

	public DatosObjetivo getContrObjetivo() {
		return objetivo;
	}

	public void setContrObjetivo(DatosObjetivo contrObjetivo) {
		this.objetivo = contrObjetivo;
	}

	public Hashtable<String, String> getParametros() {
		return parametros;
	}

	public void setParametros(Hashtable<String, String> parametros) {
		this.parametros = parametros;
	}

	public ArrayList<DatosVariableControl> getVariablesControlArray() {
		ArrayList<DatosVariableControl> nuevos = new ArrayList<DatosVariableControl>();
		Set<String> set = this.variablesControl.keySet();

		Iterator<String> itr = set.iterator();

		while (itr.hasNext()) {
			nuevos.add(this.variablesControl.get(itr.next()));

		}
		return nuevos;
	}

	public String[] getNpotp() {
		return npotp;
	}

	public void setNpotp(String[] npotp) {
		this.npotp = npotp;
	}

}
