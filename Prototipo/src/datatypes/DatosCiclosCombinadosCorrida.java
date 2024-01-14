/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCiclosCombinadosCorrida is part of MOP.
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

public class DatosCiclosCombinadosCorrida implements Serializable{

	private static final long serialVersionUID = 1L;
	private Hashtable<String, Evolucion<String>> valoresComportamiento;
	private ArrayList<String> listaUtilizados;
	private Hashtable<String, DatosCicloCombinadoCorrida> ccombinados ;/**Diccionario que tiene los datos de cada ccombinado indexados por nombre*/
	private ArrayList<String> atribtosDetallados;

	private ArrayList<String> ordenCargaXML;
	
	public DatosCiclosCombinadosCorrida() {
		// TODO Auto-generated constructor stub
		super();
		this.setValoresComportamiento(new Hashtable<String, Evolucion<String>>());
		this.listaUtilizados = new ArrayList<String>();
		this.ordenCargaXML = new ArrayList<String>();
		this.ccombinados = new Hashtable<String, DatosCicloCombinadoCorrida>();
		this.atribtosDetallados = new ArrayList<String>();
	}
	

	public Hashtable<String, Evolucion<String>> getValoresComportamiento() {
		return valoresComportamiento;
	}

	public void setValoresComportamiento(Hashtable<String, Evolucion<String>> valoresComportamiento) {
		this.valoresComportamiento = valoresComportamiento;
	}

	public ArrayList<String> getListaUtilizados() {
		return listaUtilizados;
	}

	public void setListaUtilizados(ArrayList<String> listaUtilizados) {
		this.listaUtilizados = listaUtilizados;
	}

	public Hashtable<String, DatosCicloCombinadoCorrida> getCcombinados() {
		return ccombinados;
	}

	public void setCcombinados(Hashtable<String, DatosCicloCombinadoCorrida> ccombinados) {
		this.ccombinados = ccombinados;
	}

	public ArrayList<String> getAtribtosDetallados() {
		return atribtosDetallados;
	}

	public void setAtribtosDetallados(ArrayList<String> atribtosDetallados) {
		this.atribtosDetallados = atribtosDetallados;
	}


	public ArrayList<String> getOrdenCargaXML() {	return ordenCargaXML; }

	public void setOrdenCargaXML(ArrayList<String> ordenCargaXML) {	this.ordenCargaXML = ordenCargaXML;  }



	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(ccombinados.size() > 0 ) {
			if (valoresComportamiento == null || valoresComportamiento.size() == 0)
				errores.add("CiclosCombinados: valoresComportamiento vacío.");
			if (listaUtilizados == null || listaUtilizados.size() == 0)
				errores.add("CiclosCombinados: listaUtilizados vacío.");
			ccombinados.forEach((k, v) -> errores.addAll(v.controlDatosCompletos()));
			}


		return errores;
    }
}
