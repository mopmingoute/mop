/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AFIncrementos is part of MOP.
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

package futuro;

import java.util.ArrayList;
import java.util.Hashtable;



/**
 * Subclase de Aproximacion futura representada por incrementos 
 * @author ut602614
 *
 */
public class AFIncrementos extends AFutura {
	
	double valorBellman;  // función de valor en el punto 
	
	/**
	 * La clave es el nombre de la variable de estado, y el valor es la lista de valores de incrementos o la derivada parcial respecto a 
	 * la variable de estado
	 */
	Hashtable<String, ArrayList<Double>> incrementosYDerivadasParciales;
	
	
	public AFIncrementos(){
		incrementosYDerivadasParciales = new Hashtable<String, ArrayList<Double>>();
	}
	

	public Hashtable<String, ArrayList<Double>> getIncrementosYDerivadasParciales() {
		return incrementosYDerivadasParciales;
	}

	public void setIncrementosYDerivadasParciales(
			Hashtable<String, ArrayList<Double>> incrementosYDerivadasParciales) {
		this.incrementosYDerivadasParciales = incrementosYDerivadasParciales;
	}

	public double getValorBellman() {
		return valorBellman;
	}

	public void setValorBellman(double valorBellman) {
		this.valorBellman = valorBellman;
	}
	
	
	

}
