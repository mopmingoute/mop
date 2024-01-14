/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosFallaSP is part of MOP.
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

import java.util.Arrays;

public class DatosFallaSP {

	private String nombreDemanda; // nombre de la demanda asociada
	// kk el nombre o la demanda
	private String nombreFalla; // nombre de la falla
	private double[] costo; // costo de falla en USD/MWh por escalón
	private double[][] profMW; // profundidad en MW en cada poste, de cada escalon ; primer óndice poste,
								// segundo óndice escalón
	private double[][] potencias; // potencia despachada en MW en cada poste de cada escalon ; primer óndice
									// poste, segundo óndice escalón
	private double costoTotalPaso; // costo total del paso asociado a la falla

	public DatosFallaSP() {

	}

	public DatosFallaSP(String nombreDemanda, String nombreFalla, double[] costo, double[][] profMW,
			double[][] potencias, double costoTotalPaso) {
		super();
		this.nombreDemanda = nombreDemanda;
		this.nombreFalla = nombreFalla;
		this.costo = costo;
		this.profMW = profMW;
		this.potencias = potencias;
		this.costoTotalPaso = costoTotalPaso;
	}

	public String getNombreDemanda() {
		return nombreDemanda;
	}

	public void setNombreDemanda(String nombreDemanda) {
		this.nombreDemanda = nombreDemanda;
	}

	public double[] getCosto() {
		return costo;
	}

	public void setCosto(double[] costo) {
		this.costo = costo;
	}

	public double[][] getProfMW() {
		return profMW;
	}

	public void setProfMW(double[][] profMW) {
		this.profMW = profMW;
	}

	public double[][] getPotencias() {
		return potencias;
	}

	public void setPotencias(double[][] potencias) {
		this.potencias = potencias;
	}

	public String getNombreFalla() {
		return nombreFalla;
	}

	public void setNombreFalla(String nombreFalla) {
		this.nombreFalla = nombreFalla;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Demanda Asociada: " + this.nombreDemanda);
		System.out.println("Costo de falla: " + Arrays.toString(costo));
		System.out.println("Profundidad en MW por poste: " + Arrays.deepToString(profMW));
		System.out.println("Potencias: " + Arrays.deepToString(potencias));
		System.out.println("------------------------------------------------------------------------");
	}

	public double getCostoTotalPaso() {
		return costoTotalPaso;
	}

	public void setCostoTotalPaso(double costoTotalPaso) {
		this.costoTotalPaso = costoTotalPaso;
	}

}
