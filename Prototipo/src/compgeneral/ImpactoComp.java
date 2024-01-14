/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ImpactoComp is part of MOP.
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

import compdespacho.ImpactoCompDesp;
import compsimulacion.ImpactoCompSim;
import parque.Impacto;

public class ImpactoComp extends CompGeneral {
	private ImpactoCompSim compS;
	private ImpactoCompDesp compD;
	private Impacto impacto;

	public ImpactoComp(Impacto impacto) {
		this.setParticipante(impacto);
		compS = (ImpactoCompSim) impacto.getCompSimulacion();
		compD = (ImpactoCompDesp) impacto.getCompDesp();
		this.impacto = impacto;
	}

	public ImpactoComp(Impacto Impacto, ImpactoCompDesp acd, ImpactoCompSim acs) {
		super();
		this.impacto = Impacto;
		compS = acs;
		compD = acd;
	}

	public void actualizarVarsEstadoSimulacion() {

	}

	public void actualizarVarsEstadoOptimizacion() {

	}

	public void cargarValVEOptimizacion() {

	}

	public ImpactoCompSim getCompS() {
		return compS;
	}

	public void setCompS(ImpactoCompSim compS) {
		this.compS = compS;
	}

	public ImpactoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(ImpactoCompDesp compD) {
		this.compD = compD;
	}

	public Impacto getAc() {
		return impacto;
	}

	public void setAc(Impacto ac) {
		this.impacto = ac;
	}
}
