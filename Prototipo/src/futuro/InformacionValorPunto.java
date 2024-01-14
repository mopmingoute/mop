/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * InformacionValorPunto is part of MOP.
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

import java.io.Serializable;

public class InformacionValorPunto implements Serializable{
	
	private double valorVB;
	
	/*
	 * Valores de las derivadas parciales de las variables continuas, en el orden en que estas variables aparecen 
	 * en el conjunto de variables de estado
	 */
	private double[] derivadasParciales;
	
	
	/*
	 * Vectores de incrementos del valor, para las variables discretas incrementales, en el orden en que
	 * estas variables aparecen en el conjunto de variables de estado
	 * Primer óndice variable de estado discreta incremental
	 * Segundo óndice valor posible esa variable
	 */
	private double[][] incrementosValor;
	
	
	public InformacionValorPunto(double valorVB, double[] derivadasParciales, double[][] incrementosValor) {
		super();
		this.valorVB = valorVB;
		this.derivadasParciales = derivadasParciales;
		this.incrementosValor = incrementosValor;
	}
	
	
	public InformacionValorPunto(InformacionValorPunto ivp) {
		super();
		this.valorVB = ivp.valorVB;
		this.derivadasParciales = ivp.derivadasParciales;
		this.incrementosValor = ivp.incrementosValor;
	}

	
	
	public InformacionValorPunto() {
		// TODO Auto-generated constructor stub
	}

	public double getValorVB() {
		return valorVB;
	}
	public void setValorVB(double valorVB) {
		this.valorVB = valorVB;
	}
	public double[] getDerivadasParciales() {
		return derivadasParciales;
	}
	public void setDerivadasParciales(double[] derivadasParciales) {
		this.derivadasParciales = derivadasParciales;
	}
	public double[][] getIncrementosValor() {
		return incrementosValor;
	}
	public void setIncrementosValor(double[][] incrementosValor) {
		this.incrementosValor = incrementosValor;
	}
	
	
	public String toString() {
		String resultado = "";
		
		resultado += "VB: "+ Double.toString(valorVB) + " Derivadas: "; 
		if (derivadasParciales!=null) {
			for (int i = 0; i < derivadasParciales.length; i++) 
				resultado+= derivadasParciales[i] + " ";
		}
 		return resultado;
		
	}
	
	
	
	

}
