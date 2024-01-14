/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosProveedorElecSP is part of MOP.
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

public class DatosProveedorElecSP {

	private String nombre;
	private String operacionCompraVenta;
	private double[] potencias;
	private double potenciaNominal;
	private double costoTotPaso;
	private int cantModDisp; // cantidad de módulos disponibles en el paso

	public DatosProveedorElecSP(String nombre, String operacionCompraVenta, double[] potencias, double potenciaNominal,
			int cantModDisp, double costoTot) {
		super();
		this.nombre = nombre;
		this.operacionCompraVenta = operacionCompraVenta;
		this.potencias = potencias;
		this.potenciaNominal = potenciaNominal;
		this.costoTotPaso = costoTot;
		this.setCantModDisp(cantModDisp);
	}

	public String getOperacionCompraVenta() {
		return operacionCompraVenta;
	}

	public void setOperacionCompraVenta(String operacionCompraVenta) {
		this.operacionCompraVenta = operacionCompraVenta;
	}

	public double getPotenciaNominal() {
		return potenciaNominal;
	}

	public void setPotenciaNominal(double potenciaNominal) {
		this.potenciaNominal = potenciaNominal;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double[] getPotencias() {
		return potencias;
	}

	public void setPotencia(double[] potencias) {
		this.potencias = potencias;
	}

	public double getCostoTotPaso() {
		return costoTotPaso;
	}

	public void setCostoTotPaso(double costoTotPaso) {
		this.costoTotPaso = costoTotPaso;
	}

	public void setPotencias(double[] potencias) {
		this.potencias = potencias;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Proveedor Elóctrico:" + nombre);
		System.out.println("Operación Compra o Venta:" + operacionCompraVenta);
		System.out.println("Potencias: " + Arrays.toString(potencias));
		System.out.println("Potencia Nominal: " + potenciaNominal);
		System.out.println("------------------------------------------------------------------------");
	}

	public int getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(int cantModDisp) {
		this.cantModDisp = cantModDisp;
	}
}
