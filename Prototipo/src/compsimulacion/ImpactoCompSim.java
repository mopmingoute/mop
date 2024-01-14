/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ImpactoCompSim is part of MOP.
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

import compdespacho.ImpactoCompDesp;
import compgeneral.ImpactoComp;

import datatypesProblema.DatosSalidaProblemaLineal;
import estado.VariableEstado;

import parque.Impacto;
import parque.Participante;

public class ImpactoCompSim extends CompSimulacion {

	private Impacto impacto;
	private ImpactoCompDesp impactocd;
	private ImpactoComp impactocg;

	public ImpactoCompSim(Impacto gen, ImpactoCompDesp acd, ImpactoComp acg) {
		super();
		this.setParticipante(gen);
		this.setCompdespacho(acd);
		this.setCompgeneral(acg);
		impacto = (Impacto) this.getParticipante();

		this.impactocd = (ImpactoCompDesp) this.getCompdespacho();
		this.impactocg = (ImpactoComp) this.getCompgeneral();

	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		boolean optim = false;
		cargarDatosCompDespachoAuxiliar(optim, instante);
	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		boolean optim = true;
		cargarDatosCompDespachoAuxiliar(optim, instante);
	}

	/**
	 * Mótodo auxiliar de cargarDatosCompDespacho y cargarDatosCompDespachoOptim
	 * 
	 * @param optim
	 */
	public void cargarDatosCompDespachoAuxiliar(boolean optim, long instante) {
		impactocd.setnExcesop(new String[impacto.getCantPostes()]);

	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {

	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal resultado) {

	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		return true;
	}

	@Override

	public void inicializarParaEscenario() {

	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		this.getCompdespacho().getParametros().putAll(comps);
	}

	public void contribuirAS0fint() {
	}

	public void contribuirAS0fintOptim() {

	}

	@Override
	public void actualizarOtrosDatosIniciales() {
		// TODO Auto-generated method stub

	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		Double costo = 0.0;
		long instante = impacto.getSimPaso().getCorrida().getLineaTiempo().pasoActual().getInstanteFinal();
		if (impacto.getActivo().getValor(instante)) {

			for (Participante p : impacto.getInvolucrados()) {
				costo += p.aportarCostoImpacto(impacto, salidaUltimaIter);
			}
		}

		return costo;
	}

	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {

	}

	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		return 0.0;
	}

	public Impacto getImpacto() {
		return impacto;
	}

	public void setImpacto(Impacto impacto) {
		this.impacto = impacto;
	}

	public ImpactoCompDesp getImpactocd() {
		return impactocd;
	}

	public void setImpactocd(ImpactoCompDesp impactocd) {
		this.impactocd = impactocd;
	}

	public ImpactoComp getImpactocg() {
		return impactocg;
	}

	public void setImpactocg(ImpactoComp impactocg) {
		this.impactocg = impactocg;
	}

}
