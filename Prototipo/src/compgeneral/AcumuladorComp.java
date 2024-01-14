/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AcumuladorComp is part of MOP.
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

import compdespacho.AcumuladorCompDesp;
import compsimulacion.AcumuladorCompSim;
import parque.Acumulador;
import utilitarios.Constantes;

public class AcumuladorComp extends CompGeneral {
	private AcumuladorCompSim compS;
	private AcumuladorCompDesp compD;
	private Acumulador ac;

	public AcumuladorComp(Acumulador acumulador) {
		this.setParticipante(acumulador);
		compS = (AcumuladorCompSim) acumulador.getCompSimulacion();
		compD = (AcumuladorCompDesp) acumulador.getCompDesp();
		ac = acumulador;
	}

	public AcumuladorComp(Acumulador acumulador, AcumuladorCompDesp acd, AcumuladorCompSim acs) {
		super();
		this.ac = acumulador;
		compS = acs;
		compD = acd;
	}

	public void actualizarVarsEstadoSimulacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPPASO);
		getVarsEstadoSimulacion().clear();
		if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {
			getVarsEstadoSimulacion().add(ac.getEnergAcumulada());
		}

	}

	public void actualizarVarsEstadoOptimizacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPPASO);
		getVarsEstadoOptimizacion().clear();
		if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {

			getVarsEstadoOptimizacion().add(ac.getEnergAcumuladaOpt());
		}

	}

	/**
	 * Atención que a pesar de su nombre este mótodo se usa en la SIMULACIóN para
	 * cargar los valores de las variables de la optimización.
	 */
	public void cargarValVEOptimizacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPPASO);
		if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {
			ac.getEnergAcumuladaOpt().setEstadoS0fint(ac.getEnergAcumulada().getEstadoS0fint());
			ac.getEnergAcumuladaOpt().setEstado(ac.getEnergAcumulada().getEstado());
			ac.getEnergAcumuladaOpt().setEstadoDespuesDeCDE(ac.getEnergAcumulada().getEstadoDespuesDeCDE());
		}
	}

	public AcumuladorCompSim getCompS() {
		return compS;
	}

	public void setCompS(AcumuladorCompSim compS) {
		this.compS = compS;
	}

	public AcumuladorCompDesp getCompD() {
		return compD;
	}

	public void setCompD(AcumuladorCompDesp compD) {
		this.compD = compD;
	}

	public Acumulador getAc() {
		return ac;
	}

	public void setAc(Acumulador ac) {
		this.ac = ac;
	}
}
