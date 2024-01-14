/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombTopPasoFijoCompSim is part of MOP.
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

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.ContratoCombTopPasoFijoCompDesp;
import compgeneral.ContratoCombTopPasoFijoComp;
import datatypesProblema.DatosSalidaProblemaLineal;
import parque.ContratoCombustibleTopPasoFijo;
import parque.TakeOrPayPasoFijo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.Recta;

public class ContratoCombTopPasoFijoCompSim extends CompSimulacion {

	private ContratoCombustibleTopPasoFijo contrato;
	private ContratoCombTopPasoFijoCompDesp compD;
	private ContratoCombTopPasoFijoComp compG;
	private double caudalUsado; // caudal usado resultante del despacho

	public ContratoCombTopPasoFijoCompSim(ContratoCombustibleTopPasoFijo contratoCombustible,
			ContratoCombTopPasoFijoCompDesp compD, ContratoCombTopPasoFijoComp compG) {
		contrato = contratoCombustible;
		this.compD = compD;
		this.compG = compG;

	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// DEBE ESTAR VACíO
	}

	@Override

	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		this.getCompdespacho().getParametros().putAll(comps);
	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// DEBE ESTAR VACíO

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		return true;
	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		double[] volPrepago = contrato.devuelvePrepagosEstado();
		double[] valorPrepago = contrato.devuelveValoresRecursoPrepago();
		cargarDatosCompDespachoAuxiliar(instante, volPrepago, valorPrepago);
	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		double[] volPrepago = contrato.devuelvePrepagosEstadoOptim();
		double[] valorPrepago = contrato.devuelveValoresRecursoPrepagoOptim();
		cargarDatosCompDespachoAuxiliar(instante, volPrepago, valorPrepago);
	}

	public void cargarDatosCompDespachoAuxiliar(long instante, double[] volPrepago, double[] valorPrepago) {
		String valCompCostos = this.getValsCompGeneral().get(Constantes.COMPCOSTOSTOP);
		if (valCompCostos.equalsIgnoreCase(Constantes.COSTOSCONVEXOS)) {
			TakeOrPayPasoFijo top = contrato.getTop();
			int durpaso = contrato.getDuracionPaso();
			ArrayList<Recta> rectas = top.devuelveRectasCostoVarPasoConvex(instante, durpaso, volPrepago, valorPrepago);
			compD.setRectasCosto(rectas);
			double volPrepagoTot = 0;
			for (int ip = 0; ip < volPrepago.length; ip++) {
				volPrepagoTot += volPrepago[ip];
			}
			double caudalMaxPaso = top.devuelveCaudalMax(instante, durpaso, volPrepagoTot);
			compD.setCaudalMax(caudalMaxPaso);
		} else {
			System.out.println("se pidió top sin costos convexos que no estó programada");
			System.exit(1);
		}
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		Hashtable<String, Double> sol = salidaIter.getSolucion();
		double caudalOpt = sol.get(compD.getNCaudalComb()); // en unidades del combustible por hora
		TakeOrPayPasoFijo top = contrato.getTop();
		PasoTiempo paso = contrato.getSimPaso().getPasoActual();
		long instFinPaso = paso.getInstanteFinal();
		int durpaso = paso.getDuracionPaso();
		double volumenUsado = (durpaso / Constantes.SEGUNDOSXHORA) * caudalOpt;
		double[] volPrepagosAnteriores = contrato.devuelvePrepagosEstado();
		double[] nuevosPrepagos = top.calculaNuevosVolumenesPrepagos(instFinPaso, durpaso, volumenUsado,
				volPrepagosAnteriores);
		contrato.cargaPrepagosEstado(nuevosPrepagos);
	}

	@Override
	public void inicializarParaEscenario() {
		TakeOrPayPasoFijo top = contrato.getTop();
		double[] prepagosIniciales = top.getVolumenesPrepagosIniciales();
		contrato.cargaPrepagosEstado(prepagosIniciales);
	}

	@Override
	public void actualizarOtrosDatosIniciales() {
		// DEBE ESTAR VACÍO

	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		// DEBE ESTAR VACÍO
	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		double[] prepagos = null;
		if (contrato.getOptimPaso() != null) {
			// se estó en optimización
			prepagos = contrato.devuelvePrepagosEstadoOptim();
		} else if (contrato.getSimPaso() != null) {
			// se estó en simulación
			prepagos = contrato.devuelvePrepagosEstado();
		}
		double volPrepago = 0.0;
		for (int i = 0; i < prepagos.length; i++) {
			volPrepago += prepagos[i];
		}
		Hashtable<String, Double> sol = salidaUltimaIter.getSolucion();
		double caudalOpt = sol.get(compD.getNCaudalComb()); // en unidades del combustible por hora
		TakeOrPayPasoFijo top = contrato.getTop();
		PasoTiempo paso = contrato.getSimPaso().getPasoActual();
		long instFinPaso = paso.getInstanteFinal();
		int durpaso = paso.getDuracionPaso();
		return top.calculaCostoPaso(instFinPaso, caudalOpt, volPrepago, durpaso);
	}

	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
		Hashtable<String, Double> sol = salidaUltimaIter.getSolucion();
		double caudalOpt = sol.get(compD.getNCaudalComb()); // en unidades del combustible por hora
		TakeOrPayPasoFijo top = contrato.getTop();
		PasoTiempo paso = contrato.getSimPaso().getPasoActual();
		long instFinPaso = paso.getInstanteFinal();
		int durpaso = paso.getDuracionPaso();
		double volumenUsado = (durpaso / Constantes.SEGUNDOSXHORA) * caudalOpt;
		double[] volPrepagosAnteriores = contrato.devuelvePrepagosEstado();
		double[] nuevosPrepagos = top.calculaNuevosVolumenesPrepagos(instFinPaso, durpaso, volumenUsado,
				volPrepagosAnteriores);
		contrato.cargaPrepagosEstadoFinalOptim(nuevosPrepagos);
	}

}
