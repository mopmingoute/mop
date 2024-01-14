/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosImpoExposCorrida is part of MOP.
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
 * Datatype que represeta los datos asociados a la colección de eólicos de la corrida 
 * @author ut602614
 *
 */
public class DatosImpoExposCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private Hashtable<String,String> valoresComportamiento;		/**Lista de valores de comportamiento*/
	private ArrayList<String> listaUtilizados; 
	private Hashtable<String, DatosImpoExpoCorrida> impoExpos;
	private ArrayList<String> atributosDetallados;
	private ArrayList<String> ordenCargaXML;
	
	public DatosImpoExposCorrida() {
		this.valoresComportamiento = new Hashtable<String, String>();
		this.listaUtilizados = new ArrayList<String>();
		this.ordenCargaXML = new ArrayList<String>();
		this.impoExpos = new Hashtable<String, DatosImpoExpoCorrida>();
		this.atributosDetallados = new ArrayList<String>();
	}
	
	public ArrayList<String> getListaUtilizados() {
		return listaUtilizados;
	}
	public void setListaUtilizados(ArrayList<String> listaUtilizados) {
		this.listaUtilizados = listaUtilizados;
	}
	public Hashtable<String, DatosImpoExpoCorrida> getImpoExpos() {
		return impoExpos;
	}
	public void setImpoExpos(Hashtable<String, DatosImpoExpoCorrida>  impoExpos) {
		this.impoExpos = impoExpos;
	}
	public Hashtable<String,String> getValoresComportamiento() {
		return valoresComportamiento;
	}
	public void setValoresComportamiento(Hashtable<String,String> valoresComportamiento) {
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

		//if( valoresComportamiento == null || valoresComportamiento.size() == 0) errores.add("ImpoExpos: valoresComportamiento vacío.");
		if( listaUtilizados == null || listaUtilizados.size() == 0) errores.add("ImpoExpos: listaUtilizados vacío.");
		impoExpos.forEach((k,v) -> errores.addAll(v.controlDatosCompletos()));

		return errores;
	}
}
