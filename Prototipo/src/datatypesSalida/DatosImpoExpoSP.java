/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosImpoExpoSP is part of MOP.
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

public class DatosImpoExpoSP {

	private String nombre;
	private String operacionCompraVenta;
	private double[][] potencias; // por bloque y postes
	private double costoTotalPaso;
	private double precioMed;
	private double CMgPais;

	public DatosImpoExpoSP(String nombre, String operacionCompraVenta, double[][] potencias, double costoTot, double CMgPais, double precioMed) {
		super();
		this.nombre = nombre;
		this.operacionCompraVenta = operacionCompraVenta;
		this.potencias = potencias;
		this.costoTotalPaso = costoTot;
		this.precioMed = precioMed;
		this.CMgPais = CMgPais;
	}

	public String getOperacionCompraVenta() {
		return operacionCompraVenta;
	}

	public void setOperacionCompraVenta(String operacionCompraVenta) {
		this.operacionCompraVenta = operacionCompraVenta;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double[][] getPotencias() {
		return potencias;
	}

	public void setPotencias(double[][] potencias) {
		this.potencias = potencias;
	}

	public double getCostoTotPaso() {
		return costoTotalPaso;
	}

	public void setCostoTotalPaso(double costoTotPaso) {
		this.costoTotalPaso = costoTotPaso;
	}

	public double getPrecioMed() {
		return precioMed;
	}

	public void setPrecioMed(double precioMed) {
		this.precioMed = precioMed;
	}

	public double getCMgPais() {
		return CMgPais;
	}

	public void setCMgPais(double cMgPais) {
		CMgPais = cMgPais;
	}

	public double getCostoTotalPaso() {
		return costoTotalPaso;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Proveedor Elóctrico:" + nombre);
		System.out.println("Operación Compra o Venta:" + operacionCompraVenta);
		System.out.println("Potencias: " + Arrays.toString(potencias));
		System.out.println("------------------------------------------------------------------------");
	}

}
