
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosEPPResumen is part of MOP.
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

package datatypesSalida;

import java.util.ArrayList;
import java.util.Hashtable;

import parque.Corrida;
import parque.Participante;

public class DatosEPPResumen {

	private Corrida corrida;

	private ArrayList<String> listaRecursos; // lista con los nombres de los recursos
	private ArrayList<String> listaTiposRec; // lista con los nombres de los tipos de recursos
	private Hashtable<String, Integer> indiceDeRecurso; // dado el nombre del recurso devuelve el índice en las tablas
														// de [][][]
	private Hashtable<String, Participante> participanteDeRecurso; // dado el nombre del recurso devuelve el
																	// Participante asociado
	private Hashtable<Integer, Participante> participanteDeIndice; // dado el índice en datos devuelve el Participante
	private Hashtable<String, String> tipoDeRecurso; // dado el nombre del recurso devuelve el tipo (ej: "HID")

	private Hashtable<String, Integer> indiceBarraDeRecurso; // dado el nombre del recurso devuelve el índice de la
																// barra en la lista de barras

	/**
	 * Dada la clave String nombre del recurso, devuelve una lista de nombre de
	 * atributos que se deben sacar en la salida detallada Ejemplo: bonete -->
	 * caudal, vertimiento
	 */
	private Hashtable<String, String[]> nombresAtributos;

	private int cantRec; // cantidad de recursos para los que se guardan valores

	private int cantPasos; // cantidad de pasos de tiempo simulados

	// private ArrayList<DatosEPPUnEscenario> datosEnerYCostoEPP; // almacena la
	// totalidad de los datos de los escenarios

	public DatosEPPResumen() {

	}

	public void agregaUnEscenario(DatosEPPUnEscenario d1e) {

	}

	public Corrida getCorrida() {
		return corrida;
	}

	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}

	public int getCantRec() {
		return cantRec;
	}

	public void setCantRec(int cantRec) {
		this.cantRec = cantRec;
	}

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

	public Hashtable<String, String[]> getNombresAtributos() {
		return nombresAtributos;
	}

	public void setNombresAtributos(Hashtable<String, String[]> nombresAtributos) {
		this.nombresAtributos = nombresAtributos;
	}

	public ArrayList<String> getListaRecursos() {
		return listaRecursos;
	}

	public void setListaRecursos(ArrayList<String> listaRecursos) {
		this.listaRecursos = listaRecursos;
	}

	public ArrayList<String> getListaTiposRec() {
		return listaTiposRec;
	}

	public void setListaTiposRec(ArrayList<String> listaTiposRec) {
		this.listaTiposRec = listaTiposRec;
	}

	public Hashtable<String, Integer> getIndiceDeRecurso() {
		return indiceDeRecurso;
	}

	public void setIndiceDeRecurso(Hashtable<String, Integer> indiceDeRecurso) {
		this.indiceDeRecurso = indiceDeRecurso;
	}

	public Hashtable<String, Participante> getParticipanteDeRecurso() {
		return participanteDeRecurso;
	}

	public void setParticipanteDeRecurso(Hashtable<String, Participante> participanteDeRecurso) {
		this.participanteDeRecurso = participanteDeRecurso;
	}

	public Hashtable<Integer, Participante> getParticipanteDeIndice() {
		return participanteDeIndice;
	}

	public void setParticipanteDeIndice(Hashtable<Integer, Participante> participanteDeIndice) {
		this.participanteDeIndice = participanteDeIndice;
	}

	public Hashtable<String, String> getTipoDeRecurso() {
		return tipoDeRecurso;
	}

	public void setTipoDeRecurso(Hashtable<String, String> tipoDeRecurso) {
		this.tipoDeRecurso = tipoDeRecurso;
	}

	public Hashtable<String, Integer> getIndiceBarraDeRecurso() {
		return indiceBarraDeRecurso;
	}

	public void setIndiceBarraDeRecurso(Hashtable<String, Integer> indiceBarraDeRecurso) {
		this.indiceBarraDeRecurso = indiceBarraDeRecurso;
	}

}
