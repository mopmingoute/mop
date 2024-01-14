/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConstructorHiperplanosCompSim is part of MOP.
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

package compsimulacion;

import java.util.Hashtable;

import compdespacho.ConstructorHiperplanosCompDesp;
import compgeneral.ConstructorHiperplanosComp;
import datatypesProblema.DatosSalidaProblemaLineal;
import futuro.AFHiperplanos;
import optimizacion.ResOptimHiperplanos;
import parque.ConstructorHiperplanos;

public class ConstructorHiperplanosCompSim extends CompSimulacion {

	private ConstructorHiperplanos ch;
	private ConstructorHiperplanosCompDesp compD;
	private ConstructorHiperplanosComp compG;

	public ConstructorHiperplanosCompSim(ConstructorHiperplanosCompDesp compD, ConstructorHiperplanos ch,
			ConstructorHiperplanosComp compG) {
		super();
		this.compD = compD;
		this.ch = ch;
		this.compG = compG;
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// Deliberadamente en blanco

	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> compsGlobales) {
		// Deliberadamente en blanco
	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// Deliberadamente en blanco
	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// Deliberadamente en blanco
		return false;
	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		AFHiperplanos afh = this.getParticipante().getSimPaso().getAproxFuturaHiperplanos();
		compD.setAfh(afh);
	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		// Deliberadamente en blanco
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// Deliberadamente en blanco

	}

	@Override
	public void inicializarParaEscenario() {
		// Deliberadamente en blanco

	}

	@Override
	public void actualizarOtrosDatosIniciales() {
		// Deliberadamente en blanco

	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		// Deliberadamente en blanco

	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		// Deliberadamente en blanco
		return 0;
	}

//	@Override
//	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
//		// Deliberadamente en blanco
//		
//	}

	/**
	 * Carga la aproximación futura del paso en this y en el comportamiento despacho
	 * 
	 * @param paso
	 */
	public void preparaParaPaso(int paso, ResOptimHiperplanos roptimH) {
		AFHiperplanos afh = roptimH.devuelveAproxFuturaHiperplanos(paso);
		ch.setAfhiperplanos(afh);
		compD.setAfh(afh);
	}

	public ConstructorHiperplanos getCh() {
		return ch;
	}

	public void setCh(ConstructorHiperplanos ch) {
		this.ch = ch;
	}

	public ConstructorHiperplanosCompDesp getCompD() {
		return compD;
	}

	public void setCompD(ConstructorHiperplanosCompDesp compD) {
		this.compD = compD;
	}

	public ConstructorHiperplanosComp getCompG() {
		return compG;
	}

	public void setCompG(ConstructorHiperplanosComp compG) {
		this.compG = compG;
	}

}
