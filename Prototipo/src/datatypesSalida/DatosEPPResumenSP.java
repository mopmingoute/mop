/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosEPPResumenSP is part of MOP.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Permite serializar las tablas de la clase DatosEPPResumen excepto los
 * atributos que tienen tablas que devuelven Participante
 * 
 * @author ut469262
 *
 */
public class DatosEPPResumenSP implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<String> listaRecursos; // lista con los nombres de los recursos
	private ArrayList<String> listaTiposRec; // lista con los nombres de los tipos de recursos
	private Hashtable<String, Integer> indiceDeRecurso; // dado el nombre del recurso devuelve el índice en las tablas
														// de [][][]
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

	public DatosEPPResumenSP(DatosEPPResumen dres) {
		this.listaRecursos = dres.getListaRecursos();
		this.listaTiposRec = dres.getListaTiposRec();
		this.indiceDeRecurso = dres.getIndiceDeRecurso();
		this.tipoDeRecurso = dres.getTipoDeRecurso();
		this.indiceBarraDeRecurso = dres.getIndiceBarraDeRecurso();
		this.nombresAtributos = dres.getNombresAtributos();
		this.cantRec = dres.getCantRec();
		this.cantPasos = dres.getCantPasos();
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

	public Hashtable<String, String[]> getNombresAtributos() {
		return nombresAtributos;
	}

	public void setNombresAtributos(Hashtable<String, String[]> nombresAtributos) {
		this.nombresAtributos = nombresAtributos;
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

}
