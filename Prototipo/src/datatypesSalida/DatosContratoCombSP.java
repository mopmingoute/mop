/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosContratoCombSP is part of MOP.
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

public class DatosContratoCombSP {

	private String nombreCombustible;
	private String nombreContrato;
	private double costoUnit; // costo en USD/unidad del combustible
	private double caudalMax; // caudal móximo admisible en unidad de combustible/hora
	private double caudal; // caudal usado del contrato en unidad de combustible/hora
	private int cantModDisp; // cantidad de módulos disponibles en el paso
	private double costoTotalPaso;

	public DatosContratoCombSP(String nombreContrato, String nombreCombustible, double costoUnit, double caudalMax,
			double caudal, int cantModDisp, double costoTotalP) {
		super();
		this.nombreContrato = nombreContrato;
		this.nombreCombustible = nombreCombustible;
		this.costoUnit = costoUnit;
		this.caudalMax = caudalMax;
		this.caudal = caudal;
		this.setCostoTotalPaso(costoTotalP);
	}

	public String getNombreCombustible() {
		return nombreCombustible;
	}

	public void setNombreCombustible(String nombreCombustible) {
		this.nombreCombustible = nombreCombustible;
	}

	public double getCostoUnit() {
		return costoUnit;
	}

	public void setCostoUnit(double costoUnit) {
		this.costoUnit = costoUnit;
	}

	public double getCaudalMax() {
		return caudalMax;
	}

	public void setCaudalMax(double caudalMax) {
		this.caudalMax = caudalMax;
	}

	public double getCaudal() {
		return caudal;
	}

	public void setCaudal(double caudal) {
		this.caudal = caudal;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Contrato de : " + nombreCombustible.toUpperCase());
		System.out.println("Costo Unitario: " + costoUnit);
		System.out.println("Caudal móximo: " + caudalMax);
		System.out.println("Caudal: " + caudal);
		System.out.println("------------------------------------------------------------------------");
	}

	public String getNombreContrato() {
		return nombreContrato;
	}

	public void setNombreContrato(String nombreContrato) {
		this.nombreContrato = nombreContrato;
	}

	public int getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(int cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public double getCostoTotalPaso() {
		return costoTotalPaso;
	}

	public void setCostoTotalPaso(double costoTotalPaso) {
		this.costoTotalPaso = costoTotalPaso;
	}

}
