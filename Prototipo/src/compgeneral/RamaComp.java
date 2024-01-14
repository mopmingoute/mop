/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RamaComp is part of MOP.
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

import compdespacho.HidraulicoCompDesp;
import compdespacho.RamaCompDesp;
import compsimulacion.HidraulicoCompSim;
import compsimulacion.RamaCompSim;
import parque.GeneradorHidraulico;
import parque.Rama;
import tiempo.Evolucion;

public class RamaComp extends CompGeneral {
	
	private RamaCompSim compS;
	private RamaCompDesp compD;
	private Rama r;
	
	
	private Hashtable<String, Evolucion<String>> compsGenerales;

	public RamaComp() {
		super();
	}

	public Hashtable<String, Evolucion<String>> getCompsGenerales() {
		return compsGenerales;
	}

	public void setCompsGenerales(Hashtable<String, Evolucion<String>> compsGenerales) {
		this.compsGenerales = compsGenerales;
	}

	
	
	
	
}
