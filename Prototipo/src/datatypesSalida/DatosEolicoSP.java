
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosEolicoSP is part of MOP.
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

public class DatosEolicoSP {
	private String nombre;
	private String nombreBarra;
	private int cantModDisp; // cantidad de módulos disponibles en el paso
	private double potmax; // potencia móxima
	private double[] potencias; // potencia efectivamente despachada por poste
	private double costoTotalPaso; // costo total del paso asociado a los costos de O&M
	private double gradGestion; // gradiente de gestión en el paso en USD/MW de potencia

	public DatosEolicoSP(String nombre, String nombreBarra, double[] potencias, int cantModDisp, double potmax,
			double costoTotalPaso, double gradGestion) {
		super();
		this.nombre = nombre;
		this.setCantModDisp(cantModDisp); // cantidad de módulos disponibles en el paso
		this.nombreBarra = nombreBarra;
		this.potencias = potencias;
		this.potmax = potmax;
		this.costoTotalPaso = costoTotalPaso;
		this.gradGestion = gradGestion;
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

	public void setPotencia(double[] potencias) {
		this.potencias = potencias;
	}

	public String getNombre() {
		return nombre;
	}

	public double getPotmax() {
		return potmax;
	}

	public void setPotmax(double potmax) {
		this.potmax = potmax;
	}

	public void setPotencias(double[] potencias) {
		this.potencias = potencias;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Generador Eólico:" + nombre);
		System.out.println("Barra:" + nombreBarra);
		System.out.println("Potencias: " + Arrays.toString(potencias));
		System.out.println("------------------------------------------------------------------------");

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

	public double getGradGestion() {
		return gradGestion;
	}

	public void setGradGestion(double gradGestion) {
		this.gradGestion = gradGestion;
	}

}
