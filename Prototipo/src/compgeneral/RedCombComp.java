/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedCombComp is part of MOP.
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

import parque.RedCombustible;
import compdespacho.RedCombCompDesp;
import compsimulacion.RedCombCompSim;
import tiempo.Evolucion;

public class RedCombComp extends CompGeneral {
	private Hashtable<String, Evolucion<String>> compsGenerales;
	private RedCombustible red;
	private RedCombCompDesp compD;
	private RedCombCompSim compS;

	public RedCombComp() {
		super();
	}

	public RedCombComp(RedCombustible redCombustible, RedCombCompDesp compD, RedCombCompSim compS) {

		this.setRed(redCombustible);
		this.setCompD(compD);
		this.setCompS(compS);

	}

	public Hashtable<String, Evolucion<String>> getCompsGenerales() {
		return compsGenerales;
	}

	public void setCompsGenerales(Hashtable<String, Evolucion<String>> compsGenerales) {
		this.compsGenerales = compsGenerales;
	}

	public RedCombustible getRed() {
		return red;
	}

	public void setRed(RedCombustible red) {
		this.red = red;
	}

	public RedCombCompDesp getCompD() {
		return compD;
	}

	public void setCompD(RedCombCompDesp compD) {
		this.compD = compD;
	}

	public RedCombCompSim getCompS() {
		return compS;
	}

	public void setCompS(RedCombCompSim compS) {
		this.compS = compS;
	}

}
