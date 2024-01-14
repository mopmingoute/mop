/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GestorDemandaUnPaso is part of MOP.
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

import cp_compdespProgEst.CicloCombCompDespPE;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import procesosEstocasticos.ProcesoEstocastico;

/**
 * Clase que representa el gestor de demanda de un paso
 * @author ut602614
 *
 */

public class GestorDemandaUnPaso extends GestorDemanda{

	@Override
	public void cargarDatosSimulacion() {
		// TODO Auto-generated method stub
		
	}
	/**TODO: LUEGO DEL PROTOTIPO*/

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub
		
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
		// DELIBERADAMENTE EN BLANCO
		
	}
	@Override
	public void asignaVASimul() {
		// DELIBERADAMENTE EN BLANCO
		
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
		// Deliberadamente en blanco
		
	}
	
}
