/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedComp is part of MOP.
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

import parque.RedElectrica;
import compdespacho.RedCompDesp;
import compsimulacion.RedCompSim;
import tiempo.Evolucion;

public class RedComp extends CompGeneral {
	private RedElectrica red;
	private RedCompSim compS;
	private RedCompDesp compD;

	public RedComp() {
		super();
	}

	public RedComp(RedElectrica redElectrica, RedCompDesp compD, RedCompSim compS) {
		this.red = redElectrica;
		this.compS = compS;
		this.compD = compD;

	}

	public RedElectrica getRed() {
		return red;
	}

	public void setRed(RedElectrica red) {
		this.red = red;
	}

	public RedCompSim getCompS() {
		return compS;
	}

	public void setCompS(RedCompSim compS) {
		this.compS = compS;
	}

	public RedCompDesp getCompD() {
		return compD;
	}

	public void setCompD(RedCompDesp compD) {
		this.compD = compD;
	}

	private Hashtable<String, Evolucion<String>> compsGenerales;

	public Hashtable<String, Evolucion<String>> getCompsGenerales() {
		return compsGenerales;
	}

	public void setCompsGenerales(Hashtable<String, Evolucion<String>> compsGenerales) {
		this.compsGenerales = compsGenerales;
	}

}
