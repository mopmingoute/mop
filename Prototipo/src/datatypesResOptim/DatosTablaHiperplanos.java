/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTablaHiperplanos is part of MOP.
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

package datatypesResOptim;

import java.util.ArrayList;
import java.util.Hashtable;

import futuro.ClaveDiscreta;

public class DatosTablaHiperplanos {

	private int cantPasos;

	private ArrayList<Hashtable<ClaveDiscreta, ArrayList<DatosHiperplano>>> tablaHiperplanos;

	private ArrayList<Hashtable<ClaveDiscreta, DatosHiperplano>> tablaHiperplanosPorPunto;

	public DatosTablaHiperplanos(int cantPasos) {
		this.setCantPasos(cantPasos);
		tablaHiperplanos = new ArrayList<Hashtable<ClaveDiscreta, ArrayList<DatosHiperplano>>>();
		tablaHiperplanosPorPunto = new ArrayList<Hashtable<ClaveDiscreta, DatosHiperplano>>();
		for (int ip = 0; ip < cantPasos; ip++) {
			tablaHiperplanos.add(new Hashtable<ClaveDiscreta, ArrayList<DatosHiperplano>>());
			tablaHiperplanosPorPunto.add(new Hashtable<ClaveDiscreta, DatosHiperplano>());
		}
	}

	public ArrayList<Hashtable<ClaveDiscreta, ArrayList<DatosHiperplano>>> getTablaHiperplanos() {
		return tablaHiperplanos;
	}

	public void setTablaHiperplanos(ArrayList<Hashtable<ClaveDiscreta, ArrayList<DatosHiperplano>>> tablaHiperplanos) {
		this.tablaHiperplanos = tablaHiperplanos;
	}

	public ArrayList<Hashtable<ClaveDiscreta, DatosHiperplano>> getTablaHiperplanosPorPunto() {
		return tablaHiperplanosPorPunto;
	}

	public void setTablaHiperplanosPorPunto(
			ArrayList<Hashtable<ClaveDiscreta, DatosHiperplano>> tablaHiperplanosPorPunto) {
		this.tablaHiperplanosPorPunto = tablaHiperplanosPorPunto;
	}

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

}
