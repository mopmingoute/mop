
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosDemandaSP is part of MOP.
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

public class DatosDemandaSP {
	private String nombre;
	private String nombreBarra;
	private double[] potencias; // potencia demandada
	private DatosFallaSP falla;

	public DatosDemandaSP() {

	}

	public DatosDemandaSP(String nombreBarra, double[] potencias, DatosFallaSP falla) {
		super();
		this.nombreBarra = nombreBarra;
		this.potencias = potencias;
		this.falla = falla;
	}

	public String getNombreBarra() {
		return nombreBarra;
	}

	public void setNombreBarra(String nombreBarra) {
		this.nombreBarra = nombreBarra;
	}

	public double[] getPotencias() {
		return potencias;
	}

	public void setPotencias(double[] potencias) {
		this.potencias = potencias;
	}

	public DatosFallaSP getFalla() {
		return falla;
	}

	public void setFalla(DatosFallaSP falla) {
		this.falla = falla;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;

	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Demanda : " + nombre);
		System.out.println("Barra Original: " + nombreBarra);
		System.out.println("Potencias: " + Arrays.toString(potencias));
		System.out.println("Falla Asociada: ");
		falla.imprimir();
		System.out.println("------------------------------------------------------------------------");
	}

	public String getNombre() {
		return nombre;
	}

}
