/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BarraCompSim is part of MOP.
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

import compdespacho.BarraCompDesp;
import parque.Barra;
import datatypesProblema.DatosSalidaProblemaLineal;

public class BarraCompSim extends CompSimulacion {
	private BarraCompDesp compD;
	private Barra barra;

	public BarraCompSim(Barra barra, BarraCompDesp compD) {
		super();
		this.compD = compD;
		this.barra = barra;
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		compD.setNdeltap(new String[barra.getCantPostes()]);

	}

	public BarraCompDesp getCompD() {
		return compD;
	}

	public void setCompD(BarraCompDesp compD) {
		this.compD = compD;
	}

	public Barra getBarra() {
		return barra;
	}

	public void setBarra(Barra barra) {
		this.barra = barra;
	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN ////////////////

	@Override
	public void actualizarOtrosDatosIniciales() {
		// No hace nada deliberadamente

	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		return 0;
	}

//	@Override
//	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
//		// No hace nada deliberadamente				
//	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);

	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		cargarDatosCompDespacho(instante);

	}

	///////////////////////////////////////////////////////////////////////////////////

}
