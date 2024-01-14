/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTermicoSPAuxComb is part of MOP.
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

public class DatosTermicoSPAuxComb {

	private double[] volC; // volumen de combustible usado en el paso para cada combustible en unidades de
							// combustible
	private double[] enerTC; // energía térmica usada en el paso para cada combustible
	private double[] enerEC; // energía eléctrica generada en el paso para cada combustible
	private double[][] enerTPC; // energía térmica por poste y combustible, primr índice poste, ssegundo
								// combustible
	private double[] costoC; // costo en el paso para cada combustible
	private double[][] volPC; // volumen de combustible usado en el paso para cada poste para cada
								// combustible, primer índice poste segundo índice combustible
	private double[][] potEPC; // energía eléctrica generada en cada poste del paso para cada combustible,
								// primer índice poste, segundo índice combustible

	public DatosTermicoSPAuxComb(double[] volC, double[] enerTC, double[][] enerTPC, double[] enerEC, double[] costoC,
			double[][] volPC, double[][] potEPC) {
		super();
		this.volC = volC;
		this.enerTC = enerTC;
		this.enerTPC = enerTPC;
		this.enerEC = enerEC;
		this.costoC = costoC;
		this.volPC = volPC;
		this.potEPC = potEPC;
	}

	public double[] getVolC() {
		return volC;
	}

	public void setVolC(double[] volC) {
		this.volC = volC;
	}

	public double[] getEnerTC() {
		return enerTC;
	}

	public void setEnerTC(double[] enerTC) {
		this.enerTC = enerTC;
	}

	public double[][] getEnerTPC() {
		return enerTPC;
	}

	public void setEnerTPC(double[][] enerTPC) {
		this.enerTPC = enerTPC;
	}

	public double[] getEnerEC() {
		return enerEC;
	}

	public void setEnerEC(double[] enerEC) {
		this.enerEC = enerEC;
	}

	public double[] getCostoC() {
		return costoC;
	}

	public void setCostoC(double[] costoC) {
		this.costoC = costoC;
	}

	public double[][] getVolPC() {
		return volPC;
	}

	public void setVolPC(double[][] volPC) {
		this.volPC = volPC;
	}

	public double[][] getPotEPC() {
		return potEPC;
	}

	public void setPotEPC(double[][] potEPC) {
		this.potEPC = potEPC;
	}

}
