/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FotovoltaicoComp is part of MOP.
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

import parque.GeneradorFotovoltaico;
import compdespacho.FotovoltaicoCompDesp;
import compsimulacion.FotovoltaicoCompSim;

public class FotovoltaicoComp extends CompGeneral {
	private GeneradorFotovoltaico ge;
	private FotovoltaicoCompDesp compD;
	private FotovoltaicoCompSim compS;

	public FotovoltaicoComp() {
		// TODO Auto-generated constructor stub
	}

	public FotovoltaicoComp(GeneradorFotovoltaico generadorFotovoltaico, FotovoltaicoCompDesp ecd,
			FotovoltaicoCompSim ecs) {
		super();
		ge = generadorFotovoltaico;

		compS = ecs;
		compD = ecd;
	}

	public GeneradorFotovoltaico getGe() {
		return ge;
	}

	public void setGe(GeneradorFotovoltaico ge) {
		this.ge = ge;
	}

	public FotovoltaicoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(FotovoltaicoCompDesp compD) {
		this.compD = compD;
	}

	public FotovoltaicoCompSim getCompS() {
		return compS;
	}

	public void setCompS(FotovoltaicoCompSim compS) {
		this.compS = compS;
	}

}
