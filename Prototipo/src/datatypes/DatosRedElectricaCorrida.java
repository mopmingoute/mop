/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosRedElectricaCorrida is part of MOP.
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

/**
 * Datatype que representa los datos asociados a la red elóctrica
 * @author ut602614
 *
 */
public class DatosRedElectricaCorrida implements Serializable{

	private static final long serialVersionUID = 1L;
	private Hashtable<String,Evolucion<String>> valoresComportamiento;
	private ArrayList<String> listaBarrasUtilizadas;
	private ArrayList<String> listaRamasUtilizadas;
	private ArrayList<String> listaProveedoresUtilizados;
	
	private Hashtable<String, DatosBarraCorrida> barras;
	private Hashtable<String, DatosRamaCorrida> ramas;
	
	private ArrayList<String> atributosDetallados;
	
	private String flotante;
	
	public DatosRedElectricaCorrida(){	
		valoresComportamiento = new Hashtable<String,Evolucion<String>>();
		listaBarrasUtilizadas = new ArrayList<String>();
		listaRamasUtilizadas = new ArrayList<String>(); 
		listaProveedoresUtilizados = new ArrayList<String>();
		
		barras = new Hashtable<String, DatosBarraCorrida>();
		ramas = new Hashtable<String, DatosRamaCorrida>();
		
		this.atributosDetallados = new ArrayList<String>();
		
	}

	public DatosRedElectricaCorrida(
			Hashtable<String, Evolucion<String>> valoresComportamiento,
			ArrayList<String> listaBarrasUtilizadas,
			ArrayList<String> listaRamasUtilizadas,
			ArrayList<String> listaProveedoresUtilizados,
			Hashtable<String, DatosBarraCorrida> barras,
			Hashtable<String, DatosRamaCorrida> ramas,			
			String flotante) {
		super();
		this.valoresComportamiento = valoresComportamiento;
		this.listaBarrasUtilizadas = listaBarrasUtilizadas;
		this.listaRamasUtilizadas = listaRamasUtilizadas;
		this.listaProveedoresUtilizados = listaProveedoresUtilizados;
		this.barras = barras;
		this.ramas = ramas;
		this.flotante = flotante;
		this.atributosDetallados = new ArrayList<String>();
	}
	public Hashtable<String, DatosBarraCorrida> getBarras() {
		return barras;
	}
	public void setBarras(Hashtable<String,DatosBarraCorrida> barras) {
		this.barras = barras;
	}
	public Hashtable<String,DatosRamaCorrida> getRamas() {
		return ramas;
	}
	public void setRamas(Hashtable<String,DatosRamaCorrida> ramas) {
		this.ramas = ramas;
	}
	
	public ArrayList<String> getListaBarrasUtilizadas() {
		return listaBarrasUtilizadas;
	}
	public void setListaBarrasUtilizadas(ArrayList<String> listaBarrasUtilizadas) {
		this.listaBarrasUtilizadas = listaBarrasUtilizadas;
	}
	public ArrayList<String> getListaRamasUtilizadas() {
		return listaRamasUtilizadas;
	}
	public void setListaRamasUtilizadas(ArrayList<String> listaRamasUtilizadas) {
		this.listaRamasUtilizadas = listaRamasUtilizadas;
	}
	public String getFlotante() {
		return flotante;
	}
	public void setFlotante(String flotante) {
		this.flotante = flotante;
	}


	public ArrayList<String> getListaProveedoresUtilizados() {
		return listaProveedoresUtilizados;
	}

	public void setListaProveedoresUtilizados(
			ArrayList<String> listaProveedoresUtilizados) {
		this.listaProveedoresUtilizados = listaProveedoresUtilizados;
	}

	
	public Hashtable<String,Evolucion<String>> getValoresComportamiento() {
		return valoresComportamiento;
	}
	public void setValoresComportamiento(Hashtable<String,Evolucion<String>> valoresComportamiento) {
		this.valoresComportamiento = valoresComportamiento;
	}

	public ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		this.atributosDetallados = atributosDetallados;
	}


	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if( valoresComportamiento == null || valoresComportamiento.size() == 0) { errores.add("Red Electrica: valoresComportamiento vacío."); }
		if( listaBarrasUtilizadas == null || listaBarrasUtilizadas.size() == 0) { errores.add("Red Electrica: listaBarrasUtilizadas vacío."); }
		//if( listaRamasUtilizadas == null || listaRamasUtilizadas.size() == 0) { errores.add("Red Electrica: listaRamasUtilizadas vacío."); }
		//if( listaProveedoresUtilizados == null || listaProveedoresUtilizados.size() == 0) {errores.add("Red Electrica: listaProveedoresUtilizados vacío.");}

		barras.forEach((k,v) -> { if (v.controlDatosCompletos().size() > 0) errores.add("Red Electrica: barras vacío."); });
		ramas.forEach((k,v) -> { if (v.controlDatosCompletos().size() > 0) errores.add("Red Electrica: ramas vacío."); });

		//if( atributosDetallados == null || atributosDetallados.size() == 0) errores.add("Red Electrica: atributosDetallados vacío.");
		if(flotante.trim().equals("")) { errores.add("Red Electrica: flotante vacío."); }


		return errores;
	}
}
