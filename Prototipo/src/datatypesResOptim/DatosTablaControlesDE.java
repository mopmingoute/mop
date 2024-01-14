/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTablaControlesDE is part of MOP.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import futuro.ClaveDiscreta;

public class DatosTablaControlesDE implements Serializable{
	
	private static final long serialVersionUID = 1L;
	int cantPasos;
	private ArrayList<Hashtable<ClaveDiscreta, int[]>> tablaControles;
	public int getCantPasos() {
		return cantPasos;
	}
	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}
	public ArrayList<Hashtable<ClaveDiscreta, int[]>> getTablaControles() {
		return tablaControles;
	}
	public void setTablaControles(ArrayList<Hashtable<ClaveDiscreta, int[]>> tablaControles) {
		this.tablaControles = tablaControles;
	}
	
	

}
