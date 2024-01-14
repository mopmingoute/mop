/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombCanioCompSim is part of MOP.
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

import parque.ContratoCombustibleCanio;
import utilitarios.Constantes;
import compdespacho.ContratoCombCanioCompDesp;
import compgeneral.ContratoCombCanioComp;
import datatypesProblema.DatosSalidaProblemaLineal;

public class ContratoCombCanioCompSim extends CompSimulacion {
	private ContratoCombustibleCanio contrato;
	private ContratoCombCanioCompDesp compD;
	private ContratoCombCanioComp compG;

	public ContratoCombCanioCompSim(ContratoCombustibleCanio contratoCombustibleCanio, ContratoCombCanioCompDesp compD2,
			ContratoCombCanioComp compG) {
		contrato = contratoCombustibleCanio;
		compD = compD2;
		this.compG = compG;

	}

	public ContratoCombCanioComp getCompG() {
		return compG;
	}

	public void setCompG(ContratoCombCanioComp compG) {
		this.compG = compG;
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// ESTA PRONTO, DEBE ESTAR VACóO
	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// ESTA PRONTO, DEBE ESTAR VACóO
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// ESTA PRONTO, DEBE ESTAR VACóO
	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// ESTA PRONTO
		return true;
	}

	@Override
	public void inicializarParaEscenario() {
		// ESTA PRONTO, DEBE ESTAR VACóO
	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		// ESTA PRONTO, DEBE ESTAR VACóO
	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		compD.setPrecioComb(contrato.getPrecio().getValor(instante));
		compD.setCaudalMax(contrato.getCaudalMaximo().getValor(instante));
		Integer cantModDisp = contrato.getCantModDisp().getValor().intValue();
//		System.out.println("NOMBRE: " + gt.getNombre() + "CANTMOD: " + cantModDisp);
		compD.setCantModDisp(cantModDisp);
	}

	public ContratoCombustibleCanio getContrato() {
		return contrato;
	}

	public void setContrato(ContratoCombustibleCanio contrato) {
		this.contrato = contrato;
	}

	public ContratoCombCanioCompDesp getCompD() {
		return compD;
	}

	public void setCompD(ContratoCombCanioCompDesp compD) {
		this.compD = compD;
	}

	/////////////// METODOS DE LA OPTIMIZACIóN ////////////////////////

	@Override
	public void actualizarOtrosDatosIniciales() {
		// no hace nada deliberadamente
	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		String nCaudalComb = compD.getNCaudalComb();
		double precio = compD.getPrecioComb();
		double caudal = salidaUltimaIter.getSolucion().get(nCaudalComb); // caudal en unidades por hora
		ContratoCombustibleCanio ccc = (ContratoCombustibleCanio) this.getParticipante();
		double costo = caudal * ccc.getDuracionPaso() * precio / Constantes.SEGUNDOSXHORA;
	//	System.out.println("costo paso " + this.getContrato().getNombre() + " : " + costo);
		return costo;
	}

//	@Override
//	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
//		// no hace nada deliberadamente		
//	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);

	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		cargarDatosCompDespacho(instante);

	}

	///////////////////////////////////////////////////////////////////

}
