/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedCompSim is part of MOP.
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
import parque.RedElectrica;
import compdespacho.RedCompDesp;
import compgeneral.RedComp;
import utilitarios.Constantes;
import datatypesProblema.DatosSalidaProblemaLineal;

public class RedCompSim extends CompSimulacion {
	private RedElectrica red;
	private RedCompDesp rcd;
	private RedComp rcg;

	public RedCompSim() {
		super();
	}

	public RedCompSim(RedElectrica red, RedCompDesp rcd, RedComp rcg) {
		super();
		this.setParticipante(red);
		this.setCompdespacho(rcd);
		this.setCompgeneral(rcg);
		red = (RedElectrica) this.getParticipante();
		this.rcd = (RedCompDesp) this.getCompdespacho();
		this.rcg = (RedComp) this.getCompgeneral();

	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		String valCompRed = this.getValsCompGeneral().get(Constantes.COMPRED);

		if (iter == 1) {
			rcd.setUninodal(valCompRed.equalsIgnoreCase(Constantes.UNINODAL));
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
		return true;
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

	}

	public RedElectrica getRed() {
		return red;
	}

	public void setRed(RedElectrica red) {
		this.red = red;
	}

	public RedCompDesp getRcd() {
		return rcd;
	}

	public void setRcd(RedCompDesp rcd) {
		this.rcd = rcd;
	}

	public RedComp getRcg() {
		return rcg;
	}

	public void setRcg(RedComp rcg) {
		this.rcg = rcg;
	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN ////////////////

	@Override
	public void actualizarOtrosDatosIniciales() {
		// TODO Auto-generated method stub

	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
//		// TODO Auto-generated method stub
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

	////////////////////////////////////////////////////////////////////////////////

}
