/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombCanioComp is part of MOP.
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

import parque.ContratoCombustibleCanio;
import compdespacho.ContratoCombCanioCompDesp;
import compsimulacion.ContratoCombCanioCompSim;

public class ContratoCombCanioComp extends CompGeneral {

	private ContratoCombustibleCanio contrato;

	private ContratoCombCanioCompDesp compD;
	private ContratoCombCanioComp compG;
	private ContratoCombCanioCompSim compS;

	public ContratoCombCanioComp(ContratoCombustibleCanio contratoCombustibleCanio, ContratoCombCanioCompDesp compD,
			ContratoCombCanioCompSim compS) {
		contrato = contratoCombustibleCanio;
		this.compD = compD;
		this.compS = compS;
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

	public ContratoCombCanioComp getCompG() {
		return compG;
	}

	public void setCompG(ContratoCombCanioComp compG) {
		this.compG = compG;
	}

	public ContratoCombCanioCompSim getCompS() {
		return compS;
	}

}
