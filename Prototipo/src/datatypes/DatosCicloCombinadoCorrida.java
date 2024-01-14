/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCicloCombinadoCorrida is part of MOP.
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import parque.Combustible;
import tiempo.Evolucion;

public class DatosCicloCombinadoCorrida {
	private String nombre;
	private String propietario;
	private DatosTermicoCorrida datosTGs;
	private DatosTermicoCorrida datosCCs;
	
	private Evolucion<Double> potMax1CV;
	
	private Hashtable<String,Evolucion<String>> valoresComportamientos;
	
	private ArrayList<String> listaCombustibles;  // Lista de nombres de los combustibles
	private Hashtable<String,Combustible> combustibles;						/**Lista de combustibles asociados al generador tórmico*/					
	private Hashtable<String,String> barrasCombustible;			/**Barras correspondientes a cada uno de los combustibles de la lista anterior*/
	

	private String barra;
	private Evolucion<Double> costoArranque1TGCicloAbierto;  // en USD
	private Evolucion<Double> costoArranque1TGCicloCombinado; // en USD

	private boolean salDetallada;

	private List<Pair<Double, Double>> posiblesArranques;
	private List<Double> potRampaArranque;
	private List<Double> posiblesParadas;
	

	
	
	public DatosCicloCombinadoCorrida(DatosTermicoCorrida datosTGs, DatosTermicoCorrida datosCCs, Evolucion<Double> potMax1CV) {
		super();
//		kk aca hay que agregar todos los atributos?
		this.valoresComportamientos = new Hashtable<String, Evolucion<String>>();
		this.datosTGs = datosTGs;
		this.datosCCs = datosCCs;
		this.potMax1CV = potMax1CV;
		
	}	
	
	public Hashtable<String, Evolucion<String>> getValoresComportamientos() {
		return valoresComportamientos;
	}

	public void setValoresComportamientos(Hashtable<String, Evolucion<String>> valoresComportamientos) {
		this.valoresComportamientos = valoresComportamientos;
	}
	
	public DatosTermicoCorrida getDatosTGs() {
		return datosTGs;
	}
	public void setDatosTGs(DatosTermicoCorrida datosTGs) {
		this.datosTGs = datosTGs;
	}
	public DatosTermicoCorrida getDatosCCs() {
		return datosCCs;
	}
	public void setDatosCCs(DatosTermicoCorrida datosCCs) {
		this.datosCCs = datosCCs;
	}
	
	public Evolucion<Double> getCostoArranque1TGCicloAbierto() {
		return costoArranque1TGCicloAbierto;
	}


	public void setCostoArranque1TGCicloAbierto(Evolucion<Double> costoArranque1TGCicloAbierto) {
		this.costoArranque1TGCicloAbierto = costoArranque1TGCicloAbierto;
	}


	public Evolucion<Double> getCostoArranque1TGCicloCombinado() {
		return costoArranque1TGCicloCombinado;
	}


	public void setCostoArranque1TGCicloCombinado(Evolucion<Double> costoArranque1TGCicloCombinado) {
		this.costoArranque1TGCicloCombinado = costoArranque1TGCicloCombinado;
	}

	public String getNombre() {
		return nombre;
	}

	public String getBarra() {
		return barra;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setBarra(String barra) {
		this.barra = barra;
	}


	
	public ArrayList<String> getListaCombustibles() {
		return listaCombustibles;
	}

	public void setListaCombustibles(ArrayList<String> listaCombustibles) {
		this.listaCombustibles = listaCombustibles;
	}

	
	
	public Hashtable<String, Combustible> getCombustibles() {
		return combustibles;
	}

	public void setCombustibles(Hashtable<String, Combustible> combustibles) {
		this.combustibles = combustibles;
	}



	public Hashtable<String, String> getBarrasCombustible() {
		return barrasCombustible;
	}

	public void setBarrasCombustible(Hashtable<String, String> barrasCombustible) {
		this.barrasCombustible = barrasCombustible;
	}


	public boolean isSalDetallada() {
		return salDetallada;
	}
	
	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}
	
	
	public Evolucion<Double> getPotMax1CV() {
		return potMax1CV;
	}


	public void setPotMax1CV(Evolucion<Double> potMax1CV) {
		this.potMax1CV = potMax1CV;
	}


	public List<Pair<Double, Double>> getPosiblesArranques() {
		return posiblesArranques;
	}

	public void setPosiblesArranques(List<Pair<Double, Double>> posiblesArranques) {
		this.posiblesArranques = posiblesArranques;
	}

	public List<Double> getPotRampaArranque() {
		return potRampaArranque;
	}

	public void setPotRampaArranque(List<Double> potRampaArranque) {
		this.potRampaArranque = potRampaArranque;
	}

	public List<Double> getPosiblesParadas() {
		return posiblesParadas;
	}

	public void setPosiblesParadas(List<Double> posiblesParadas) {
		this.posiblesParadas = posiblesParadas;
	}
	
	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}

    public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();

		if(nombre.trim().equals("")) errores.add("CicloCombinado: Nombre vacío.");

		errores.addAll(datosTGs.controlDatosCompletos(true, nombre));
		errores.addAll(datosCCs.controlDatosCompletos(true, nombre));
		if(potMax1CV == null ) { errores.add("CicloCombinado " + nombre + ": potMax1CV vacío."); }
		else {
			if(potMax1CV.controlDatosCompletos().size() >0 ) errores.add("CicloCombinado " + nombre + ": potMax1CV vacío.");
		}
		if( valoresComportamientos == null || valoresComportamientos.size() == 0) errores.add("CicloCombinado " + nombre + ": valoresComportamientos vacío.");
		if( listaCombustibles == null || listaCombustibles.size() == 0) errores.add("CicloCombinado " + nombre + ": listaCombustibles vacío.");
		//if( combustibles == null || combustibles.size() == 0) errores.add("CicloCombinado: combustiblesBarras vacío.");
		if( barrasCombustible == null || barrasCombustible.size() == 0) errores.add("CicloCombinado " + nombre + ": combustiblesBarras vacío.");
		if(barra == null) errores.add("CicloCombinado " + nombre + ": Barra vacío.");


		if(costoArranque1TGCicloAbierto == null ) errores.add("CicloCombinado " + nombre + ": costoArranque CicloAbierto vacío.");
		else {
			if(costoArranque1TGCicloAbierto.controlDatosCompletos().size() >0 ) errores.add("CicloCombinado " + nombre + ": costoArranque1TGCicloAbierto vacío.");
		}

		if(costoArranque1TGCicloCombinado == null ) errores.add("CicloCombinado " + nombre + ": costoArranque CicloCombinado vacío.");
		else {
			if(costoArranque1TGCicloCombinado.controlDatosCompletos().size() >0 ) errores.add("CicloCombinado " + nombre + ": costoArranque1TGCicloCombinado vacío.");
		}

		if(posiblesArranques == null) errores.add("CicloCombinado " + nombre + ": posiblesArranques vacío.");
		else {
			posiblesArranques.forEach((v) -> {
				if (v.first == null) errores.add("CicloCombinado " + nombre + ": posiblesArranques vacío.");
			});
		}

		if(potRampaArranque == null) errores.add("CicloCombinado: potRampaArranque vacío.");
		else {
			potRampaArranque.forEach((v) -> {
				if (v == null) errores.add("CicloCombinado: potRampaArranque vacío.");
			});
		}

		if(posiblesParadas == null) errores.add("CicloCombinado: posiblesParadas vacío.");
		else {
			posiblesParadas.forEach((v) -> {
				if (v == null) errores.add("CicloCombinado: posiblesParadas vacío.");
			});
		}

		return errores;
    }


}
