/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EolicoComp is part of MOP.
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

import parque.GeneradorEolico;
import compdespacho.EolicoCompDesp;
import compsimulacion.EolicoCompSim;

public class EolicoComp extends CompGeneral {
	private GeneradorEolico ge;
	private EolicoCompDesp compD;
	private EolicoCompSim compS;

	public EolicoComp() {
		// TODO Auto-generated constructor stub
	}

	public EolicoComp(GeneradorEolico generadorEolico, EolicoCompDesp ecd, EolicoCompSim ecs) {
		super();
		ge = generadorEolico;

		compS = ecs;
		compD = ecd;
	}

	public GeneradorEolico getGe() {
		return ge;
	}

	public void setGe(GeneradorEolico ge) {
		this.ge = ge;
	}

	public EolicoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(EolicoCompDesp compD) {
		this.compD = compD;
	}

	public EolicoCompSim getCompS() {
		return compS;
	}

	public void setCompS(EolicoCompSim compS) {
		this.compS = compS;
	}

}
