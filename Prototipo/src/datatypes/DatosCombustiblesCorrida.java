/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCombustiblesCorrida is part of MOP.
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
 * Datatype que representa la colección de combustibles asociados a una corrida 
 * @author ut602614
 *
 */
public class DatosCombustiblesCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private Hashtable<String,String> valoresComportamiento;					/**Lista de valores de comportamiento para cada variable de comportamiento*/
	private ArrayList<String> listaUtilizados;								/**Lista de Combustibles utilizados*/
	private Hashtable<String, DatosCombustibleCorrida> combustibles;		/**Lista total de Combustibles*/

	private ArrayList<String> ordenCargaXML;
	public DatosCombustiblesCorrida() {
		super();
		this.valoresComportamiento = new Hashtable<String,String>();
		this.listaUtilizados = new ArrayList<String>();
		this.ordenCargaXML = new ArrayList<String>();
		this.combustibles = new Hashtable<String, DatosCombustibleCorrida>();
	}
	public ArrayList<String> getListaUtilizados() {
		return listaUtilizados;
	}

	public void setListaUtilizados(ArrayList<String> listaUtilizados) {
		this.listaUtilizados = listaUtilizados;
	}
	public Hashtable<String,DatosCombustibleCorrida> getCombustibles() {
		return combustibles;
	}
	public void setCombustibles(Hashtable<String,DatosCombustibleCorrida> combustibles) {
		this.combustibles = combustibles;
	}
	public Hashtable<String,String> getValoresComportamiento() {
		return valoresComportamiento;
	}
	public void setValoresComportamiento(Hashtable<String,String> valoresComportamiento) {
		this.valoresComportamiento = valoresComportamiento;
	}


	public ArrayList<String> getOrdenCargaXML() {	return ordenCargaXML; }

	public void setOrdenCargaXML(ArrayList<String> ordenCargaXML) {	this.ordenCargaXML = ordenCargaXML;  }



	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(combustibles.size() > 0 ) {
			// if (valoresComportamiento == null || valoresComportamiento.size() == 0) { errores.add("Combustibles: valoresComportamiento vacío."); }
			if (listaUtilizados == null || listaUtilizados.size() == 0) { errores.add("Combustibles: listaUtilizados vacío."); }
			combustibles.forEach((k, v) -> errores.addAll(v.controlDatosCompletos()));
		}
		return errores;
    }
}
