/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TermicoComp is part of MOP.
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

package compgeneral;

import compdespacho.TermicoCompDesp;
import compsimulacion.TermicoCompSim;
import parque.GeneradorTermico;
import utilitarios.Constantes;

public class TermicoComp extends CompGeneral {
	private TermicoCompSim compS;
	private TermicoCompDesp compD;
	private GeneradorTermico gt;

	public TermicoComp() {

	}

	public TermicoComp(GeneradorTermico generadorTermico) {
		this.setParticipante(generadorTermico);
		compS = generadorTermico.getCompS();
		compD = generadorTermico.getCompD();
		setGt(generadorTermico);
	}

	public TermicoComp(GeneradorTermico generadorTermico, TermicoCompDesp tcd, TermicoCompSim tcs) {
		this.setParticipante(generadorTermico);
		compS = tcs;
		compD = tcd;
		setGt(generadorTermico);
	}

	public void actualizarVarsEstadoSimulacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPMINTEC);
		getVarsEstadoSimulacion().clear();
		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			getVarsEstadoSimulacion().add(gt.getCantModIni());
		}

	}

	public void actualizarVarsEstadoOptimizacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPMINTEC);
		getVarsEstadoSimulacion().clear();
		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			getVarsEstadoSimulacion().add(gt.getCantModIniOpt());
		}
	}

	public void cargarValVEOptimizacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			gt.getCantModIniOpt().setEstadoS0fint(gt.getCantModIni().getEstadoS0fint());
			gt.getCantModIniOpt().setEstado(gt.getCantModIni().getEstado());
			gt.getCantModIniOpt().setEstadoDespuesDeCDE(gt.getCantModIni().getEstadoDespuesDeCDE());
		}

	}

	public TermicoCompSim getCompS() {
		return compS;
	}

	public void setCompS(TermicoCompSim compS) {
		this.compS = compS;
	}

	public TermicoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(TermicoCompDesp compD) {
		this.compD = compD;
	}

	public GeneradorTermico getGt() {
		return gt;
	}

	public void setGt(GeneradorTermico gt) {
		this.gt = gt;
	}
}
