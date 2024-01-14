/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PEDisponibilidadGeometricaComplejo is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;
import java.util.Hashtable;

import estado.VariableEstado;

/**
 * El proceso se aplica a disponibilidades de m�dulos de recursos. Tiene como variables :
 * cantidad de m�dulos disponibles y conectados en el instante
 * cantidad de m�dulos disponibles y no conectados
 * 
 * Se va a aplicar en el instante inicial del paso, antes del saltito.
 * 
 * 
 * 
 * los restantes hasta la cantidad de m�dulos de la Evolucion est�n indisponibles
 * Es un caso particular de PE en el que el estado del PE en t+1 depende del estado de un recurso asociado en t
 * 
 *  
 *  
 *  
 *  
 *  
 * @author ut602614
 *
 */
public class PEDisponibilidadGeometricaComplejo extends ProcesoEstocastico{

	@Override
	public void producirRealizacion(long instante) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void alimentarPronosticoEnAutoregresivos(long instante) {
		// Deliberadamente en blanco
	}

//	@Override
//	public Hashtable<String, ArrayList<Double>> dameRealizacionesIntervalo(
//			int instanteInicial, int instanteFinal) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Hashtable<String, ArrayList<Double>> dameNRealizacionesInstanteEstado(
//			int instante, int n, ArrayList<VariableEstado> estado) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public Hashtable<String, VariableEstado> dameEstadoAgregado(
//			ArrayList<String> nomVarEstado, String modoReduccion) {
//		// TODO Auto-generated method stub
//		return null;
//	}


@Override
public boolean tieneVEOptim() {
	// TODO Auto-generated method stub
	return false;
}

@Override
public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
	// TODO Auto-generated method stub
	
}


@Override
public void prepararPasoOptim(int cantSortMontecarlo) {
	// TODO Auto-generated method stub
	
}

@Override
public void producirRealizacionSinPronostico(long instante) {
	// DELIBERADAMENTE EN BLANCO
	
}


}
