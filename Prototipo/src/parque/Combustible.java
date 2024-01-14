/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Combustible is part of MOP.
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

package parque;


import java.util.ArrayList;
import java.util.Hashtable;

import cp_compdespProgEst.DemandaCompDespPE;
import datatypes.DatosCombustibleCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import procesosEstocasticos.ProcesoEstocastico;

/**
 * Clase que representa el combustible
 * @author ut602614
 *
 */
public class Combustible extends Participante{	
	private String unidad; 			/**La unidad en que se mide la cantidad comubstible m3*/
	private Double pci; 			/**Poder calorófico inferior en MWh/unidad*/
	//private Double densidad; 		/**Densidad en kg/unidad*/
	
	
	/**Constructor a partir de los datos*/
	public Combustible(DatosCombustibleCorrida datosCombustibleCorrida) {
		this.setNombre(datosCombustibleCorrida.getNombre());
		this.unidad = datosCombustibleCorrida.getUnidad();
		this.pci = datosCombustibleCorrida.getPciPorUnidad();
		//this.densidad = datosCombustibleCorrida.getDensidad();		
		
	}
	
	
	
	public String getUnidad() {
		return unidad;
	}
//	public Double getDensidad() {
//		return densidad;
//	}
//	public void setDensidad(Double densidad) {
//		this.densidad = densidad;
//	}
	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}
	public Double getPci() {
		return pci;
	}
	public void setPci(Double pci) {
		this.pci = pci;
	}



	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,
			DatosSalidaProblemaLineal salidaUltimaIter, String proceso, long instante) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void asignaVAOptim() {
		// Deliberadamente en blanco
		
	}



	@Override
	public void asignaVASimul() {
		// Deliberadamente en blanco
		
	}



	@Override
	public void aportarImpacto(Impacto i,DatosObjetivo costo ) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void crearCompDespPE() {
		// DELIBERADAMENTE EN BLANCO
	}
	
}
