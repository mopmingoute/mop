/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPronostico is part of MOP.
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

package datatypes;

import java.io.Serializable;

import tiempo.Evolucion;

/**
 * Clase que contiene el pronóstico de valores de una variable aleatoria
 * incluso su peso
 * @author ut469262
 *
 */

public class DatosPronostico implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombreVA; // nombre de la variables aleatoria
	private Evolucion<Double> peso;  // peso del pronóstico
	private Evolucion<Double> valores;  // valores del pronóstico
	
	public DatosPronostico(String nombreVA, Evolucion<Double> peso, Evolucion<Double> valores) {
		super();
		this.nombreVA = nombreVA;
		this.peso = peso;
		this.valores = valores;
	}

	public String getNombreVA() {
		return nombreVA;
	}

	public void setNombreVA(String nombreVA) {
		this.nombreVA = nombreVA;
	}

	public Evolucion<Double> getPeso() {
		return peso;
	}

	public void setPeso(Evolucion<Double> peso) {
		this.peso = peso;
	}

	public Evolucion<Double> getValores() {
		return valores;
	}

	public void setValores(Evolucion<Double> valores) {
		this.valores = valores;
	}
	
	

}
