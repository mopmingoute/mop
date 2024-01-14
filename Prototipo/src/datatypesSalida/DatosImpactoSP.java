
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosImpactoSP is part of MOP.
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

public class DatosImpactoSP {
	private String nombre;
	private double costoTotalPaso;
	private double[] costoPoste;
	private double magnitudTotal;
	private double[] magnitudPoste;

	public DatosImpactoSP(String nombre, double costoTotalPaso, double[] costoPoste, double magnitudTotal,
			double magnitudPoste[]) {
		super();
		this.nombre = nombre;
		this.costoTotalPaso = costoTotalPaso;
		this.costoPoste = costoPoste;
		this.magnitudTotal = magnitudTotal;
		this.magnitudPoste = magnitudPoste;
	}

	public double[] getCostoPoste() {
		return costoPoste;
	}

	public void setCostoPoste(double[] costoPoste) {
		this.costoPoste = costoPoste;
	}

	public double getMagnitudTotal() {
		return magnitudTotal;
	}

	public void setMagnitudTotal(double magnitudTotal) {
		this.magnitudTotal = magnitudTotal;
	}

	public double[] getMagnitudPoste() {
		return magnitudPoste;
	}

	public void setMagnitudPoste(double[] magnitudPoste) {
		this.magnitudPoste = magnitudPoste;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getCostoTotalPaso() {
		return costoTotalPaso;
	}

	public void setCostoTotalPaso(double costoTotalPaso) {
		this.costoTotalPaso = costoTotalPaso;
	}

}
