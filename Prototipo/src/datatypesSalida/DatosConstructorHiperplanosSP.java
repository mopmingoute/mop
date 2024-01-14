/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosConstructorHiperplanosSP is part of MOP.
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

import datatypesResOptim.DatosHiperplano;

public class DatosConstructorHiperplanosSP {

	private ArrayList<String> nombresHipActivos;
	private ArrayList<DatosHiperplano> hiperplanosActivos;

	public DatosConstructorHiperplanosSP(ArrayList<String> nombresHipActivos,
			ArrayList<DatosHiperplano> hiperplanosActivos) {
		super();
		this.nombresHipActivos = nombresHipActivos;
		this.hiperplanosActivos = hiperplanosActivos;

	}

	public ArrayList<String> getNombresHipActivos() {
		return nombresHipActivos;
	}

	public void setNombresHipActivos(ArrayList<String> nombresHipActivos) {
		this.nombresHipActivos = nombresHipActivos;
	}

	public ArrayList<DatosHiperplano> getHiperplanosActivos() {
		return hiperplanosActivos;
	}

	public void setHiperplanosActivos(ArrayList<DatosHiperplano> hiperplanosActivos) {
		this.hiperplanosActivos = hiperplanosActivos;
	}

}