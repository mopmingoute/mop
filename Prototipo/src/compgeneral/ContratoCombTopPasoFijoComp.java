/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombTopPasoFijoComp is part of MOP.
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

import compdespacho.ContratoCombTopPasoFijoCompDesp;
import compsimulacion.ContratoCombTopPasoFijoCompSim;
import parque.ContratoCombustibleTopPasoFijo;

public class ContratoCombTopPasoFijoComp {

	private ContratoCombustibleTopPasoFijo contrato;
	private ContratoCombTopPasoFijoCompDesp compD;
	private ContratoCombTopPasoFijoCompSim compS;

	public ContratoCombTopPasoFijoComp(ContratoCombustibleTopPasoFijo contratoCombTopPasoFijo,
			ContratoCombTopPasoFijoCompDesp compD, ContratoCombTopPasoFijoCompSim compS) {
		contrato = contratoCombTopPasoFijo;
		this.compD = compD;
		this.compS = compS;
	}

	public ContratoCombustibleTopPasoFijo getContrato() {
		return contrato;
	}

	public void setContrato(ContratoCombustibleTopPasoFijo contrato) {
		this.contrato = contrato;
	}

	public ContratoCombTopPasoFijoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(ContratoCombTopPasoFijoCompDesp compD) {
		this.compD = compD;
	}

	public ContratoCombTopPasoFijoCompSim getCompS() {
		return compS;
	}

	public void setCompS(ContratoCombTopPasoFijoCompSim compS) {
		this.compS = compS;
	}

}
