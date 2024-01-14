/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Observacion is part of MOP.
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

package procesosEstocasticos;

public class Observacion {
	

	private double[] valoresSeries;
	private int estadoActual; // estado compuesto de la observación
	private int estadoProximo; // estado compuesto al que se pasa en el tiempo siguiente
	private int paso;  // el paso del año de la observación
	private int anio;  // el año de la observación
	
	public Observacion(double[] valSer, int estAct, int estProx, int anio, int paso){
		this.valoresSeries = valSer;
		this.estadoActual = estAct;
		this.estadoProximo = estProx;
		this.paso = paso;
		this.anio = anio;
	}

	public double[] getValoresSeries() {
		return valoresSeries;
	}

	public void setValoresSeries(double[] valoresSeries) {
		this.valoresSeries = valoresSeries;
	}

	public int getEstadoActual() {
		return estadoActual;
	}

	public void setEstadoActual(int estadoActual) {
		this.estadoActual = estadoActual;
	}

	public int getEstadoProximo() {
		return estadoProximo;
	}

	public void setEstadoProximo(int estadoProximo) {
		this.estadoProximo = estadoProximo;
	}

	public int getPaso() {
		return paso;
	}

	public void setPaso(int paso) {
		this.paso = paso;
	}

	public int getAnio() {
		return anio;
	}

	public void setAnio(int anio) {
		this.anio = anio;
	}


	
}
