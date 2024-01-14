/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * HidraulicoComp is part of MOP.
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

import compdespacho.HidraulicoCompDesp;
import compsimulacion.HidraulicoCompSim;
import parque.GeneradorHidraulico;
import utilitarios.Constantes;

public class HidraulicoComp extends CompGeneral {
	private HidraulicoCompSim compS;
	private HidraulicoCompDesp compD;
	private GeneradorHidraulico gh;

	public HidraulicoComp(GeneradorHidraulico generadorHidraulico) {
		this.setParticipante(generadorHidraulico);
		compS = (HidraulicoCompSim) generadorHidraulico.getCompSimulacion();
		compD = (HidraulicoCompDesp) generadorHidraulico.getCompDesp();
		gh = generadorHidraulico;
	}

	public HidraulicoComp(GeneradorHidraulico generadorHidraulico, HidraulicoCompDesp hcd, HidraulicoCompSim hcs) {
		super();
		gh = generadorHidraulico;
		compS = hcs;
		compD = hcd;
	}

	public void actualizarVarsEstadoSimulacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPLAGO);
		getVarsEstadoSimulacion().clear();
		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			getVarsEstadoSimulacion().add(gh.getVolumen());
		}

	}

	public void actualizarVarsEstadoOptimizacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPLAGO);
		getVarsEstadoOptimizacion().clear();
		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			getVarsEstadoOptimizacion().add(gh.getVolumenOpt());
		}
	}

	/**
	 * Atención que a pesar de su nombre este mótodo se usa en la SIMULACIóN para
	 * cargar los valores de las variables de la optimización.
	 */
	public void cargarValVEOptimizacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPLAGO);
		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			gh.getVolumenOpt().setEstadoS0fint(gh.getVolumen().getEstadoS0fint());
			gh.getVolumenOpt().setEstado(gh.getVolumen().getEstado());
			gh.getVolumenOpt().setEstadoDespuesDeCDE(gh.getVolumen().getEstadoDespuesDeCDE());
		}
	}

	public HidraulicoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(HidraulicoCompDesp compD) {
		this.compD = compD;
	}

	public GeneradorHidraulico getGh() {
		return gh;
	}

	public void setGh(GeneradorHidraulico gh) {
		this.gh = gh;
	}

	public HidraulicoCompSim getCompS() {
		return compS;
	}

	public void setCompS(HidraulicoCompSim compS) {
		this.compS = compS;
	}

}
