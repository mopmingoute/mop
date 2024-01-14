/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoPorEvolucion is part of MOP.
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

import datatypesProcEstocasticos.DatosPEEscenarios;
import datatypesProcEstocasticos.DatosPEEvolucion;
import tiempo.Evolucion;

/**
 * Cada escenario consiste en una serie de valores del PE en los pasos
 * sucesivos.
 * 
 * @author ut602614
 * @param <T>
 *
 */

public class ProcesoPorEvolucion extends ProcesoEstocastico {

	private Evolucion<Double> ev;

	public ProcesoPorEvolucion() {
	}

	public ProcesoPorEvolucion(DatosPEEvolucion dpesc) {

	//	super(dpesc.getDatGen());

		this.setMuestreado(dpesc.isMuestreado());
		this.setNombre(dpesc.getNombre());
		this.setNombresVarsAleatorias(dpesc.getNombresVA());
		this.setUsoOptimizacion(true);
		this.setUsoSimulacion(true);
		this.setCantVA(this.getNombresVarsAleatorias().size());
		this.setCantVE(0);
	}

	@Override
	public void producirRealizacion(long instante) {
		this.getVariablesAleatorias().get(0).setValor(ev.getValor(instante)); // solo hay una V.A de ordinal 0 en el
																				// vector.
	}
	
	public void producirRealizacionSinPronostico(long instante) {
		// DELIBERADAMENTE EN BLANCO
	}
	

	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean tieneVEOptim() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void prepararPasoOptim(int cantSortMontecarlo) {
		// TODO Auto-generated method stub

	}

}