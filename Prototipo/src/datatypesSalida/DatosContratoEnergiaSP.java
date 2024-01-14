/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosContratoEnergiaSP is part of MOP.
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

public class DatosContratoEnergiaSP {
	private String nombre;
	private double energiaPasoGWh;
	private double energiaAcumAnioGWh; // energía acumulada en el año corriente, con años comenzano en el inicio del
										// contrato
	private double valorPasoUSD; // valor del contrato en el paso
	private double[] potencias; // potencias por poste en MW

	public DatosContratoEnergiaSP(String nombre, double energiaPasoGWh, double[] potencias, double energiaAcumAnioGWh,
			double valorPasoUSD) {
		super();
		this.nombre = nombre;
		this.energiaPasoGWh = energiaPasoGWh;
		this.potencias = potencias;
		this.energiaAcumAnioGWh = energiaAcumAnioGWh;
		this.valorPasoUSD = valorPasoUSD;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getEnergiaPasoGWh() {
		return energiaPasoGWh;
	}

	public void setEnergiaPasoGWh(double energiaPasoGWh) {
		this.energiaPasoGWh = energiaPasoGWh;
	}

	public double getEnergiaAcumAnioGWh() {
		return energiaAcumAnioGWh;
	}

	public void setEnergiaAcumAnioGWh(double energiaAcumAnioGWh) {
		this.energiaAcumAnioGWh = energiaAcumAnioGWh;
	}

	public double getValorPasoUSD() {
		return valorPasoUSD;
	}

	public void setValorPasoUSD(double valorPasoUSD) {
		this.valorPasoUSD = valorPasoUSD;
	}

	public double[] getPotencias() {
		return potencias;
	}

	public void setPotencias(double[] potencias) {
		this.potencias = potencias;
	}

}
