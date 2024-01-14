/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTermicoCP is part of MOP.
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

package cp_datatypesEntradas;

public class DatosTermicoCP extends DatosPartCP {
	
	private double costoArr1Mod;  // costo de arranque de un módulo en USD
	
	private int cantModIni;  // cantidad de módulos en funcionamiento al fin del poste anterior al inicio del horizonte CP

	public DatosTermicoCP(String nombrePart, String tipoPart, double costoArr1Mod, int cantModIni) {
		super(nombrePart, tipoPart);
		this.costoArr1Mod = costoArr1Mod;
		this.cantModIni = cantModIni;
	}

	public double getCostoArr1Mod() {
		return costoArr1Mod;
	}

	public void setCostoArr1Mod(double costoArr1Mod) {
		this.costoArr1Mod = costoArr1Mod;
	}

	public int getCantModIni() {
		return cantModIni;
	}

	public void setCantModIni(int cantModIni) {
		this.cantModIni = cantModIni;
	}



}
