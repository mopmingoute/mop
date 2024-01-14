/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Pronostico is part of MOP.
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

package procesosEstocasticos;

import datatypes.DatosPronostico;
import tiempo.Evolucion;

/**
 * Clase que contiene el pronóstico de valores de una variable aleatoria
 * incluso su peso
 * @author ut469262
 *
 */

public class Pronostico {
	private String nombreVA; // nombre de la variables aleatoria
	private Evolucion<Double> peso;  // peso del pronóstico, que en la ponderación se acotará inferiormente por cero.
	private Evolucion<Double> valores;  // valores del pronóstico
	
	public Pronostico(String nombreVA, Evolucion<Double> peso, Evolucion<Double> valores) {
		super();
		this.nombreVA = nombreVA;
		this.peso = peso;
		this.valores = valores;
	}
	
	public Pronostico(DatosPronostico dp) {
		super();
		this.nombreVA = dp.getNombreVA();
		this.peso = dp.getPeso();
		this.valores = dp.getValores();
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
