/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BarraCombustibleCompSim is part of MOP.
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

import compdespacho.BarraCombustibleCompDesp;
import compgeneral.BarraCombustibleComp;
import datatypesProblema.DatosSalidaProblemaLineal;
import parque.BarraCombustible;

public class BarraCombustibleCompSim extends CompSimulacion {

	private BarraCombustible b;
	private BarraCombustibleCompDesp bcd;
	private BarraCombustibleComp bcg;

	public BarraCombustibleCompSim(BarraCombustible b, BarraCombustibleCompDesp bcd, BarraCombustibleComp bcg) {
		super();
		this.setParticipante(b);
		this.setCompdespacho(bcd);
		this.setCompgeneral(bcg);
		this.b = (BarraCombustible) this.getParticipante();

		this.bcd = (BarraCombustibleCompDesp) this.getCompdespacho();
		this.bcg = (BarraCombustibleComp) this.getCompgeneral();

	}

	public void cargarDatosSimulacion() {
		// Deliberadamente en blanco
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// Deliberadamente en blanco
	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// Deliberadamente en blanco

	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// Deliberadamente en blanco

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// Deliberadamente en blanco
		return false;
	}

	@Override
	public void inicializarParaEscenario() {
		// Deliberadamente en blanco
	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		// TODO Auto-generated method stub

	}

	public double devuelveVarDualBalance(DatosSalidaProblemaLineal resultado) {
		String nombreRBalance = b.getCompD().getNombreRestBalance();
		return resultado.getDuales().get(nombreRBalance);
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
//		
//	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);

	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		cargarDatosCompDespacho(instante);

	}

	////////////////////////////////////////////////////////////////////////////

}
