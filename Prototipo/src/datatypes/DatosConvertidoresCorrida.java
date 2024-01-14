/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosConvertidoresCorrida is part of MOP.
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
 * Datatype que representa la colección de convertidores asociados a la corrida
 * @author ut602614
 *
 */
public class DatosConvertidoresCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private Hashtable<String,DatosConvertidorCombustibleSimpleCorrida> convertidores;
	private ArrayList<String> listaUtilizados;
	
	public DatosConvertidoresCorrida() {
		super();
		this.listaUtilizados = new ArrayList<String>();
		this.convertidores = new Hashtable<String, DatosConvertidorCombustibleSimpleCorrida>();
	}
	public Hashtable<String,DatosConvertidorCombustibleSimpleCorrida> getConvertidores() {
		return convertidores;
	}
	public void setConvertidores(Hashtable<String,DatosConvertidorCombustibleSimpleCorrida> convertidores) {
		this.convertidores = convertidores;
	}
	public ArrayList<String> getListaUtilizados() {
		return listaUtilizados;
	}
	public void setListaUtilizados(ArrayList<String> listaUtilizados) {
		this.listaUtilizados = listaUtilizados;
	}

	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();
		if(convertidores == null) { errores.add("DatosConvertidoresCorrida: convertidores vacío."); }
		else {
			convertidores.forEach((k, v)-> { if(v.controlDatosCompletos().size() > 0) errores.add("DatosConvertidoresCorrida: convertidores vacío.");});
		}

		if(listaUtilizados == null) { errores.add("DatosConvertidoresCorrida: listaUtilizados vacío."); }
		else {
			listaUtilizados.forEach((v)-> { if(v.trim().equals("")) errores.add("DatosConvertidoresCorrida: listaUtilizados vacío.");});
		}

		return errores;
    }
}
