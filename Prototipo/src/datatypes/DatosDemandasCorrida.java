/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosDemandasCorrida is part of MOP.
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

/**
 * Datatype que representa la colección de demandas existentes en la corrida
 * @author ut602614
 *
 */
public class DatosDemandasCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private Hashtable<String,String> valoresComportamiento;		/**Lista Valores de comportamiento*/
	private ArrayList<String> listaUtilizados;					/**Lista de demandas utilizadas*/
	private Hashtable<String, DatosDemandaCorrida> demandas;	/**Lista con todas las demandas*/
	private ArrayList<String> atributosDetallados;

	private ArrayList<String> ordenCargaXML;

	public DatosDemandasCorrida() {
		valoresComportamiento = new Hashtable<String, String>();
		listaUtilizados = new ArrayList<String>();
		this.ordenCargaXML = new ArrayList<String>();
		demandas = new Hashtable<String, DatosDemandaCorrida>();
		this.atributosDetallados = new ArrayList<String>();
	}
	
	public Hashtable<String, String> getValoresComportamiento() {
		return valoresComportamiento;
	}

	public void setValoresComportamiento(
			Hashtable<String, String> valoresComportamiento) {
		this.valoresComportamiento = valoresComportamiento;
	}
	public ArrayList<String> getListaUtilizados() {
		return listaUtilizados;
	}
	public void setListaUtilizados(ArrayList<String> listaUtilizados) {
		this.listaUtilizados = listaUtilizados;
	}
	public Hashtable<String, DatosDemandaCorrida> getDemandas() {
		return demandas;
	}
	public void setDemandas(Hashtable<String, DatosDemandaCorrida> demandas) {
		this.demandas = demandas;
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

		//if( valoresComportamiento == null || valoresComportamiento.size() == 0) errores.add("Demandas: valoresComportamiento vacío.");
		if( listaUtilizados == null || listaUtilizados.size() == 0) errores.add("Demandas: listaUtilizados vacío.");
		demandas.forEach((k,v) -> errores.addAll(v.controlDatosCompletos()));


		return errores;
    }
}
