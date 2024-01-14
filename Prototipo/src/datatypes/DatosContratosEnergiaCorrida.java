/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosContratosEnergiaCorrida is part of MOP.
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

public class DatosContratosEnergiaCorrida implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private ArrayList<String> listaUtilizados;
	private Hashtable<String,DatosContratoEnergiaCorrida> contratosEnergia;
	private ArrayList<String> atributosDetallados;
	private ArrayList<String> ordenCargaXML;
	
	public DatosContratosEnergiaCorrida() {
		listaUtilizados =  new ArrayList<String>();
		ordenCargaXML = new ArrayList<String>();
		setContratosEnergia(new Hashtable<String, DatosContratoEnergiaCorrida>());
		atributosDetallados = new ArrayList<String>();
		listaUtilizados = new ArrayList<String>();
	}

	public ArrayList<String> getListaUtilizados() {
		return listaUtilizados;
	}
	public void setListaUtilizados(ArrayList<String> listaUtilizados) {
		this.listaUtilizados = listaUtilizados;
	}
	

	public ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		this.atributosDetallados = atributosDetallados;
	}

	public Hashtable<String, DatosContratoEnergiaCorrida> getContratosEnergia() {
		return contratosEnergia;
	}

	public void setContratosEnergia(Hashtable<String, DatosContratoEnergiaCorrida> contratosEnergia) {
		this.contratosEnergia = contratosEnergia;
	}

	public ArrayList<String> getOrdenCargaXML() {	return ordenCargaXML; }

	public void setOrdenCargaXML(ArrayList<String> ordenCargaXML) {	this.ordenCargaXML = ordenCargaXML;  }



	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(contratosEnergia.size() > 0) {
			if (listaUtilizados == null || listaUtilizados.size() == 0)
				errores.add("ContratosEnergia: listaUtilizados vacío.");
			contratosEnergia.forEach((k, v) -> errores.addAll(v.controlDatosCompletos()));
		}


		return errores;
	}
}
