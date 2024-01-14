
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosRedCombustibleSP is part of MOP.
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

import java.io.Serializable;
import java.util.ArrayList;

public class DatosRedCombustibleSP implements Serializable {

	private static final long serialVersionUID = 1L;
	private String nombre;
	private DatosCombustibleSP combustible;
	private ArrayList<DatosBarraCombSP> barras;
	private ArrayList<DatosDuctoSP> ductos;

	public DatosRedCombustibleSP(String nombre, DatosCombustibleSP combustible) {
		super();
		this.nombre = nombre;
		this.combustible = combustible;
		this.barras = new ArrayList<DatosBarraCombSP>();
		this.ductos = new ArrayList<DatosDuctoSP>();
	}

	public DatosCombustibleSP getCombustible() {
		return combustible;
	}

	public void setCombustible(DatosCombustibleSP combustible) {
		this.combustible = combustible;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ArrayList<DatosBarraCombSP> getBarras() {
		return barras;
	}

	public void setBarras(ArrayList<DatosBarraCombSP> barras) {
		this.barras = barras;
	}

	public ArrayList<DatosDuctoSP> getDuctos() {
		return ductos;
	}

	public void setDuctos(ArrayList<DatosDuctoSP> ductos) {
		this.ductos = ductos;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Red de: " + nombre.toUpperCase());
		System.out.println("------------------------------------------------------------------------");
		for (DatosBarraCombSP dbcsp : barras) {
			dbcsp.imprimir();
		}
		for (DatosDuctoSP ddcsp : ductos) {
			ddcsp.imprimir();
		}
		System.out.println("------------------------------------------------------------------------");
		// TODO Auto-generated method stub

	}

}
