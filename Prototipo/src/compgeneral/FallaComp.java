/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FallaComp is part of MOP.
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

import java.util.Hashtable;

import parque.Falla;
import compdespacho.FallaCompDesp;
import compsimulacion.FallaCompSim;
import tiempo.Evolucion;
import utilitarios.Constantes;

public class FallaComp extends CompGeneral {
	private Hashtable<String, Evolucion<String>> compsGenerales;
	private Falla falla;
	private FallaCompDesp compD;
	private FallaCompSim compS;

	public FallaComp() {
		super();
	}

	public FallaComp(Falla falla, FallaCompDesp compD, FallaCompSim compS) {
		this.falla = falla;
		this.compD = compD;
		this.compS = compS;
	}

	public Hashtable<String, Evolucion<String>> getCompsGenerales() {
		return compsGenerales;
	}

	public void setCompsGenerales(Hashtable<String, Evolucion<String>> compsGenerales) {
		this.compsGenerales = compsGenerales;
	}

	public FallaCompSim getCompS() {
		return compS;
	}

	public void setCompS(FallaCompSim compS) {
		this.compS = compS;
	}

	public FallaCompDesp getCompD() {
		return compD;
	}

	public void setCompD(FallaCompDesp compD) {
		this.compD = compD;
	}

	public Falla getFalla() {
		return falla;
	}

	public void setFalla(Falla falla) {
		this.falla = falla;
	}

	public void actualizarVarsEstadoSimulacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPFALLA);
		getVarsEstadoSimulacion().clear();
		if (valCompGen.equalsIgnoreCase(Constantes.FALLA_CONESTADO_SINDUR)) {
			getVarsEstadoSimulacion().add(falla.getCantEscForzados());
		} else if (valCompGen.equalsIgnoreCase(Constantes.FALLA_CONESTADO_CONDUR)) {
			getVarsEstadoSimulacion().add(falla.getCantEscForzados());
			getVarsEstadoSimulacion().add(falla.getPerForzadosRestantes());
		}
	}

	public void actualizarVarsEstadoOptimizacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPFALLA);
		getVarsEstadoOptimizacion().clear();
		if (valCompGen.equalsIgnoreCase(Constantes.FALLA_CONESTADO_SINDUR)) {
			getVarsEstadoOptimizacion().add(falla.getCantEscForzadosOptim());
		} else if (valCompGen.equalsIgnoreCase(Constantes.FALLA_CONESTADO_CONDUR)) {
			getVarsEstadoOptimizacion().add(falla.getCantEscForzadosOptim());
			getVarsEstadoOptimizacion().add(falla.getPerForzadosRestantesOptim());
		}
	}

	/**
	 * Atención que a pesar de su nombre este mótodo se usa en la SIMULACIóN para
	 * cargar los valores de las variables de la optimización.
	 */
	public void cargarValVEOptimizacion() {
		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPFALLA);
		if (!valCompGen.equalsIgnoreCase(Constantes.FALLASINESTADO)) {
			falla.getCantEscForzadosOptim().setEstadoS0fint(falla.getCantEscForzados().getEstadoS0fint());
			falla.getCantEscForzadosOptim().setEstado(falla.getCantEscForzados().getEstado());
			falla.getCantEscForzadosOptim().setEstadoDespuesDeCDE(falla.getCantEscForzados().getEstadoDespuesDeCDE());
		}
	}

}
