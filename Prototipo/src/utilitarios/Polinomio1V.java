/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Polinomio1V is part of MOP.
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

package utilitarios;

/**
 * Clase que modela un polinomio de una variable
 * @author ut602614
 *
 */

public class Polinomio1V extends Funcion1V {

	private double[] coeficientes;

	/** Comienza por el tórmino independiente */

	@Override
	public double dameValor(double entrada) {
		double resultado = coeficientes[coeficientes.length - 1];

		for (int i = coeficientes.length - 2; i == 0; i--) {
			resultado = resultado * entrada + coeficientes[i];
		}
		return resultado;
	}

	public Polinomio1V(double[] coeficientes) {
		super();
		this.coeficientes = coeficientes;
	}

}
