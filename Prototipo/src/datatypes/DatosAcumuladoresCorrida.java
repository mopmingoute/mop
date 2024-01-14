/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosAcumuladoresCorrida is part of MOP.
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

public class DatosAcumuladoresCorrida implements Serializable {
	private Hashtable<String, Evolucion<String>> valoresComportamiento;
	private ArrayList<String> listaUtilizados;
	private Hashtable<String, DatosAcumuladorCorrida> acumuladores;
	/** Diccionario que tiene los datos de cada tórmico indexados por nombre */
	private ArrayList<String> atributosDetallados;

	private ArrayList<String> ordenCargaXML;

	public DatosAcumuladoresCorrida() {
		super();

		this.listaUtilizados = new ArrayList<String>();
		this.ordenCargaXML = new ArrayList<String>();
		this.acumuladores = new Hashtable<String, DatosAcumuladorCorrida>();
		this.valoresComportamiento = new Hashtable<String, Evolucion<String>>();
		this.atributosDetallados = new ArrayList<String>();
	}

	public ArrayList<String> getListaUtilizados() {
		return listaUtilizados;
	}

	public void setListaUtilizados(ArrayList<String> listaUtilizados) {
		this.listaUtilizados = listaUtilizados;
	}

	public Hashtable<String, DatosAcumuladorCorrida> getAcumuladores() {
		return acumuladores;
	}

	public void setAcumuladores(Hashtable<String, DatosAcumuladorCorrida> acumuladores) {
		this.acumuladores = acumuladores;
	}

	public Hashtable<String, DatosAcumuladorCorrida> getTermicos() {
		return acumuladores;
	}

	public Hashtable<String, Evolucion<String>> getValoresComportamiento() {
		return valoresComportamiento;
	}

	public void setValoresComportamiento(Hashtable<String, Evolucion<String>> valoresComportamiento) {
		this.valoresComportamiento = valoresComportamiento;
	}

	public ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		this.atributosDetallados = atributosDetallados;
	}


	public ArrayList<String> getOrdenCargaXML() {	return ordenCargaXML; }

	public void setOrdenCargaXML(ArrayList<String> ordenCargaXML) {	this.ordenCargaXML = ordenCargaXML;  }


	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();
		if (acumuladores.size() > 0) {
			if (valoresComportamiento == null || valoresComportamiento.size() == 0)
				errores.add("Acumuladores: valoresComportamiento vacío.");
			if (listaUtilizados == null || listaUtilizados.size() == 0)
				errores.add("Acumuladores: listaUtilizados vacío.");
			acumuladores.forEach((k, v) -> errores.addAll(v.controlDatosCompletos()));
		}
		return errores;
	}
}
