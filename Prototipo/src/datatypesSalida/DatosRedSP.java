
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosRedSP is part of MOP.
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

package datatypesSalida;

import java.util.ArrayList;

public class DatosRedSP {
	private ArrayList<DatosBarraSP> barras;
	private ArrayList<DatosRamaSP> ramas;

	public DatosRedSP() {
		barras = new ArrayList<DatosBarraSP>();
		ramas = new ArrayList<DatosRamaSP>();
	}

	public ArrayList<DatosBarraSP> getBarras() {
		return barras;
	}

	public void setBarras(ArrayList<DatosBarraSP> barras) {
		this.barras = barras;
	}

	public ArrayList<DatosRamaSP> getRamas() {
		return ramas;
	}

	public void setRamas(ArrayList<DatosRamaSP> ramas) {
		this.ramas = ramas;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Red Elóctrica");
		System.out.println("------------------------------------------------------------------------");
		for (DatosBarraSP dbsp : barras) {
			dbsp.imprimir();
		}
		for (DatosRamaSP drsp : ramas) {
			drsp.imprimir();
		}
		System.out.println("------------------------------------------------------------------------");

	}

}
