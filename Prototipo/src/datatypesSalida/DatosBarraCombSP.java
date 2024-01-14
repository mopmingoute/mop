/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosBarraCombSP is part of MOP.
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

import java.util.ArrayList;

public class DatosBarraCombSP {
	private String nombreCombustible;
	private String nombre;
	private ArrayList<DatosContratoCombSP> contratos;
	private ArrayList<DatosTanqueSP> tanques;
	private double costoMarg; // costo marginal en USD/unidad de combustible

	public DatosBarraCombSP() {
		this.contratos = new ArrayList<DatosContratoCombSP>();
		this.tanques = new ArrayList<DatosTanqueSP>();
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ArrayList<DatosContratoCombSP> getContratos() {
		return contratos;
	}

	public void setContratos(ArrayList<DatosContratoCombSP> contratos) {
		this.contratos = contratos;
	}

	public ArrayList<DatosTanqueSP> getTanques() {
		return tanques;
	}

	public void setTanques(ArrayList<DatosTanqueSP> tanques) {
		this.tanques = tanques;
	}

	public double getCostoMarg() {
		return costoMarg;
	}

	public void setCostoMarg(double costoMarg) {
		this.costoMarg = costoMarg;
	}

	public void agregarContrato(DatosContratoCombSP dcc) {
		this.contratos.add(dcc);

	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Barra Combustible: " + nombre);

		for (DatosContratoCombSP dcsp : contratos) {
			dcsp.imprimir();

			for (DatosTanqueSP dtsp : tanques) {
				dtsp.imprimir();
			}

			System.out.println("------------------------------------------------------------------------");
		}

	}

}
