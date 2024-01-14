/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ParReales is part of MOP.
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

import java.util.ArrayList;
import java.util.Collections;

public class ParReales implements Comparable{
	
	double real1;
	double real2;
	double ordenador;
	public ParReales(double real1, double real2) {
		super();
		this.real1 = real1;
		this.real2 = real2;
	}
	

	/**
	 * Reordena una lista de pares en orden creciente por el primer valor real1 de cada par
	 * @param lp
	 * @return
	 */
	public static ArrayList<ParReales> ordenaListaPorReal1(ArrayList<ParReales> lp){
		for(ParReales pr: lp){
			pr.setOrdenador(pr.real1);
		}
		Collections.sort(lp);
		return lp;
	}
	
	/**
	 * Reordena una lista de pares en orden creciente por el segundo valor real2 de cada par
	 * @param lp
	 * @return
	 */
	public static ArrayList<ParReales> ordenaListaPorReal2(ArrayList<ParReales> lp){
		for(ParReales pr: lp){
			pr.setOrdenador(pr.real2);
		}
		Collections.sort(lp);
		return lp;
	}


	public double getOrdenador() {
		return ordenador;
	}


	public void setOrdenador(double ordenador) {
		this.ordenador = ordenador;
	}


	@Override
	public int compareTo(Object arg0) {
		ParReales p0 = (ParReales)arg0;
		if(this.ordenador < p0.getOrdenador()) return -1;
		if(this.ordenador>p0.getOrdenador()) return 1;
		return 0;
	}


	public double getReal1() {
		return real1;
	}


	public void setReal1(double real1) {
		this.real1 = real1;
	}


	public double getReal2() {
		return real2;
	}


	public void setReal2(double real2) {
		this.real2 = real2;
	}
	
	
	
}
