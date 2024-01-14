/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedCombCompSim is part of MOP.
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

import parque.ContratoCombustible;
import parque.RedCombustible;
import compdespacho.RedCombCompDesp;
import compgeneral.RedCombComp;
import utilitarios.Constantes;
import datatypesProblema.DatosSalidaProblemaLineal;

public class RedCombCompSim extends CompSimulacion {
	private RedCombustible red;
	private RedCombComp compG;
	private RedCombCompDesp compD;

	public RedCombCompSim() {
		super();
	}

	public RedCombCompSim(RedCombustible redCombustible, RedCombCompDesp compD, RedCombComp compG) {
		this.red = redCombustible;
		this.compD = compD;
		this.compG = compG;
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {

		String valCompRed = this.getValsCompGeneral().get(Constantes.COMPRED);

		if (iter == 1) {
			compD.setUninodal(valCompRed.equalsIgnoreCase(Constantes.UNINODAL));
		}

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
		// TODO Auto-generated method stub

	}

	public RedCombustible getRed() {
		return red;
	}

	public void setRed(RedCombustible red) {
		this.red = red;
	}

	public RedCombComp getCompG() {
		return compG;
	}

	public void setCompG(RedCombComp compG) {
		this.compG = compG;
	}

	public RedCombCompDesp getCompD() {
		return compD;
	}

	public void setCompD(RedCombCompDesp compD) {
		this.compD = compD;
	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN ////////////////

	@Override
	public void actualizarOtrosDatosIniciales() {
		// No hace nada deliberadamente

	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		double costo = 0.0;
		for (ContratoCombustible cont : red.getContratos().values()) {
			costo += cont.calculaCostoPaso(salidaUltimaIter);
		}
		return costo;
	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);

	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		cargarDatosCompDespacho(instante);

	}

	////////////////////////////////////////////////////////////////////////////////

}
