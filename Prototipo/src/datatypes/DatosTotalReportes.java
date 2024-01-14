/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTotalReportes is part of MOP.
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
import java.util.HashMap;

import datatypesSalida.DatosEPPUnEscenario;

public class DatosTotalReportes {
	
	// para cada año un arraylist en donde el lugar i corresponde al escenario y el valor a la crónica asociada
	private HashMap<Integer, ArrayList<Integer>> anioEscenarioCronica;

	//Nombres según el tipo de tecnología
	private HashMap<String, ArrayList<String>> nombreTipo;
	private ArrayList<String> nombresRecursos;
	
	//clave nombrerecurso, escenario, paso, poste -> valor potencia
	private HashMap<String,ArrayList<ArrayList<ArrayList<Double>>>> potencias;  
	
	//clave nombrerecurso, escenario, paso -> valor energia
	private HashMap<String,ArrayList<ArrayList<Double>>> energias;  

	//clave nombrerecurso, escenario, paso, poste -> valor cmarg
	private HashMap<String,ArrayList<ArrayList<ArrayList<Double>>>> costosMarginales;
	
	//clave nombrerecurso, escenario, paso -> costo
	private HashMap<String,ArrayList<ArrayList<Double>>> costos;
	
	
	private HashMap<String,ArrayList<ArrayList<Double>>> aportesPorHidro;  
	private HashMap<String,ArrayList<ArrayList<ArrayList<Double>>>> turbinadosPorHidro;
	private HashMap<String,ArrayList<ArrayList<Double>>> vertidosPorHidro;
	private HashMap<String,ArrayList<ArrayList<Double>>> cotaDelLagoPorHidro;
	private HashMap<String,ArrayList<ArrayList<Double>>> valorDelAguaPorHidro;
	
	
	
	private HashMap<String,ArrayList<ArrayList<Integer>>> candModulosRecursos;
	private HashMap<String,ArrayList<ArrayList<Double>>> factorUsoRecursos;
	
	private HashMap<String,ArrayList<ArrayList<Double>>> magnitudesImpactos;
	
	private int[][] durPos;
	




	public DatosTotalReportes(ArrayList<DatosEPPUnEscenario> datosEscenarios) {
//		this.durPos = datosEscenarios.get(0).getDurPos();
//		nombreTipo = new HashMap<String, ArrayList<String>>();
//		energias = new HashMap<String,ArrayList<ArrayList<Double>>>();  
//		
//		
//		
//		llenarTablaNombreTipo(datosEscenarios.get(0));
//		
//		
//		
//		for (DatosEPPUnEscenario de: datosEscenarios) {
//			
//			for (String rec: nombresRecursos) {
//				 
//				double[] enerEsc = de.dameEnergias(rec);
//				ArrayList<Double> enerEscarr= DoubleStream.of(enerEsc).boxed().collect(Collectors.toCollection(ArrayList::new));
//				//energi
//			}
//			
//		}
		
	}





	private void llenarTablaNombreTipo(DatosEPPUnEscenario datosEPPUnEscenario) {
		
		String [] tipoSepNom;
		String pref;
		String nomb;
		for (String tipo_nom: datosEPPUnEscenario.getTablaInd().keySet()) {
			tipoSepNom = tipo_nom.split("_");
			pref = tipoSepNom[0];
			nomb = tipoSepNom[1];
			if (this.nombreTipo.get(pref)==null){
				this.nombreTipo.put(pref, new ArrayList<String>());
			}
			this.nombreTipo.get(pref).add(tipo_nom);
			
		}
		
	}






	public HashMap<String, ArrayList<ArrayList<ArrayList<Double>>>> getPotencias() {
		return potencias;
	}


	public void setPotencias(HashMap<String, ArrayList<ArrayList<ArrayList<Double>>>> potencias) {
		this.potencias = potencias;
	}



	public HashMap<String, ArrayList<ArrayList<ArrayList<Double>>>> getCostosMarginales() {
		return costosMarginales;
	}


	public void setCostosMarginales(HashMap<String, ArrayList<ArrayList<ArrayList<Double>>>> costosMarginales) {
		this.costosMarginales = costosMarginales;
	}




	public int[][] getDurPos() {
		return durPos;
	}


	public void setDurPos(int[][] durPos) {
		this.durPos = durPos;
	}


	public HashMap<Integer, ArrayList<Integer>> getAnioEscenarioCronica() {
		return anioEscenarioCronica;
	}


	public void setAnioEscenarioCronica(HashMap<Integer, ArrayList<Integer>> anioEscenarioCronica) {
		this.anioEscenarioCronica = anioEscenarioCronica;
	}


	public HashMap<String, ArrayList<ArrayList<Double>>> getEnergias() {
		return energias;
	}


	public void setEnergias(HashMap<String, ArrayList<ArrayList<Double>>> energias) {
		this.energias = energias;
	}


	public HashMap<String, ArrayList<ArrayList<Double>>> getCostos() {
		return costos;
	}


	public void setCostos(HashMap<String, ArrayList<ArrayList<Double>>> costos) {
		this.costos = costos;
	}


	public HashMap<String, ArrayList<ArrayList<Double>>> getAportesPorHidro() {
		return aportesPorHidro;
	}


	public void setAportesPorHidro(HashMap<String, ArrayList<ArrayList<Double>>> aportesPorHidro) {
		this.aportesPorHidro = aportesPorHidro;
	}


	public HashMap<String, ArrayList<ArrayList<ArrayList<Double>>>> getTurbinadosPorHidro() {
		return turbinadosPorHidro;
	}


	public void setTurbinadosPorHidro(HashMap<String, ArrayList<ArrayList<ArrayList<Double>>>> turbinadosPorHidro) {
		this.turbinadosPorHidro = turbinadosPorHidro;
	}


	public HashMap<String, ArrayList<ArrayList<Double>>> getVertidosPorHidro() {
		return vertidosPorHidro;
	}


	public void setVertidosPorHidro(HashMap<String, ArrayList<ArrayList<Double>>> vertidosPorHidro) {
		this.vertidosPorHidro = vertidosPorHidro;
	}


	public HashMap<String, ArrayList<ArrayList<Double>>> getCotaDelLagoPorHidro() {
		return cotaDelLagoPorHidro;
	}


	public void setCotaDelLagoPorHidro(HashMap<String, ArrayList<ArrayList<Double>>> cotaDelLagoPorHidro) {
		this.cotaDelLagoPorHidro = cotaDelLagoPorHidro;
	}


	public HashMap<String, ArrayList<ArrayList<Double>>> getValorDelAguaPorHidro() {
		return valorDelAguaPorHidro;
	}


	public void setValorDelAguaPorHidro(HashMap<String, ArrayList<ArrayList<Double>>> valorDelAguaPorHidro) {
		this.valorDelAguaPorHidro = valorDelAguaPorHidro;
	}


	public HashMap<String, ArrayList<ArrayList<Integer>>> getCandModulosRecursos() {
		return candModulosRecursos;
	}


	public void setCandModulosRecursos(HashMap<String, ArrayList<ArrayList<Integer>>> candModulosRecursos) {
		this.candModulosRecursos = candModulosRecursos;
	}


	public HashMap<String, ArrayList<ArrayList<Double>>> getFactorUsoRecursos() {
		return factorUsoRecursos;
	}


	public void setFactorUsoRecursos(HashMap<String, ArrayList<ArrayList<Double>>> factorUsoRecursos) {
		this.factorUsoRecursos = factorUsoRecursos;
	}


	public HashMap<String, ArrayList<ArrayList<Double>>> getMagnitudesImpactos() {
		return magnitudesImpactos;
	}


	public void setMagnitudesImpactos(HashMap<String, ArrayList<ArrayList<Double>>> magnitudesImpactos) {
		this.magnitudesImpactos = magnitudesImpactos;
	}





	public ArrayList<String> getNombresRecursos() {
		return nombresRecursos;
	}





	public void setNombresRecursos(ArrayList<String> nombresRecursos) {
		this.nombresRecursos = nombresRecursos;
	}
	
	
	public HashMap<String, ArrayList<String>> getNombreTipo() {
		return nombreTipo;
	}





	public void setNombreTipo(HashMap<String, ArrayList<String>> nombreTipo) {
		this.nombreTipo = nombreTipo;
	}




	
	
}

