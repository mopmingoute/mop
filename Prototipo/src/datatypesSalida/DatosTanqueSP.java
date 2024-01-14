/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTanqueSP is part of MOP.
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

public class DatosTanqueSP {

	private double capacidad;
	/** Capacidad del tanque de combustible */
	private double volIni; // volumen al inicio del paso en unidades del combustible
	private double volFin; // volumen al fin del paso en unidades del combustible

	public DatosTanqueSP(double capacidad, double volIni, double volFin) {
		super();
		this.capacidad = capacidad;
		this.volIni = volIni;
		this.volFin = volFin;
	}

	public double getCapacidad() {
		return capacidad;
	}

	public void setCapacidad(double capacidad) {
		this.capacidad = capacidad;
	}

	public double getVolIni() {
		return volIni;
	}

	public void setVolIni(double volIni) {
		this.volIni = volIni;
	}

	public double getVolFin() {
		return volFin;
	}

	public void setVolFin(double volFin) {
		this.volFin = volFin;
	}

	public void imprimir() {
		// TODO Auto-generated method stub

	}

}
