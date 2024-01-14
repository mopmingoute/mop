/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DemandaComp is part of MOP.
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

import parque.Demanda;
import compdespacho.DemandaCompDesp;
import compsimulacion.DemandaCompSim;

public class DemandaComp extends CompGeneral {

	private Demanda demanda;
	private DemandaCompDesp compD;

	public Demanda getDemanda() {
		return demanda;
	}

	public void setDemanda(Demanda demanda) {
		this.demanda = demanda;
	}

	public DemandaCompDesp getCompD() {
		return compD;
	}

	public void setCompD(DemandaCompDesp compD) {
		this.compD = compD;
	}

	public DemandaCompSim getCompS() {
		return compS;
	}

	public void setCompS(DemandaCompSim compS) {
		this.compS = compS;
	}

	private DemandaCompSim compS;

	public DemandaComp(Demanda demanda, DemandaCompDesp compD, DemandaCompSim compS) {
		this.demanda = demanda;
		this.compD = compD;
		this.compS = compS;
	}

}
