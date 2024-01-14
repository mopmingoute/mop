/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Recta is part of MOP.
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

import java.io.Serializable;
import java.util.ArrayList;

import datatypes.Pair;

public class Recta implements Serializable{
	public double a;
	public double b;
	// La recta es a*x + b

	public Recta() {
	}

	
	
	public Recta(double a, double b) {
		super();
		this.a = a;
		this.b = b;
	}

	public Recta interpolarPorDistancia(ArrayList<Pair<Double,Double>> puntosGrilla, Pair<Double,Double> puntoAInterpolar, ArrayList<Recta> valsGrilla) {
		double [] inv_distancias = new double [valsGrilla.size()];
		if (puntosGrilla.contains(puntoAInterpolar)) {
			return valsGrilla.get(puntosGrilla.indexOf(puntoAInterpolar));			
		}
		int i = 0;
		double suma_inv_distancias = 0;
		ArrayList<Recta> rectasPonderadas = new ArrayList<Recta>();
		for (Pair<Double, Double> ptoGrilla: puntosGrilla) {
			
			inv_distancias[i] = 1/distancia(ptoGrilla, puntoAInterpolar);
			rectasPonderadas.add(multiplicar(valsGrilla.get(i),inv_distancias[i]));
			suma_inv_distancias += inv_distancias[i];
			i++;
		}
		
		return multiplicar(sumar(rectasPonderadas), 1/suma_inv_distancias);
		
	}
	
	public Recta multiplicar(Recta r, double m) {
		return new Recta(m*r.a, m*r.b);
	}
	
	private Recta sumar(ArrayList<Recta> rectas) {
		double ares = 0;
		double bres = 0;
		for (Recta r: rectas) {
			ares+=r.a;
			bres+=r.b;
		}
		return new Recta(ares,bres);
	}
	
	private double distancia(Pair<Double, Double> a, Pair<Double, Double> b) {
		return Math.sqrt((b.second-a.second)*(b.second-a.second)+(b.first-a.first)*(b.first-a.first));
	}
	
	public void imprimir() {

		System.out.println("RECTA: " + a + "*x + " + b);
		
		
		
	}
	
	
	
	
	public double getA() {
		return a;
	}



	public void setA(double a) {
		this.a = a;
	}



	public double getB() {
		return b;
	}



	public void setB(double b) {
		this.b = b;
	}



	public static void main(String[] args) {
		
		Pair<Double, Double> a = new Pair<Double, Double>(0.0, 0.0);
		Pair<Double, Double> b = new Pair<Double, Double>(2.0, 0.0);
		Pair<Double, Double> c = new Pair<Double, Double>(0.0, 2.0);
		Pair<Double, Double> d = new Pair<Double, Double>(2.0, 2.0);
		Pair<Double, Double> e = new Pair<Double, Double>(0.0, 0.0);
		
		Pair<Double, Double> p = new Pair<Double, Double>(3.0, 1.0);
		
		Recta rA = new Recta(1,0);
		Recta rB = new Recta(1,-2);
		Recta rC = new Recta(1,2);
		Recta rD = new Recta(1,4);
		
		ArrayList<Recta> rs = new ArrayList<Recta>();
		rs.add(rA);
		rs.add(rB);
		rs.add(rC);
		rs.add(rD);
		
		ArrayList<Pair<Double,Double>> pts = new ArrayList<Pair<Double, Double>>();
		pts.add(a);
		pts.add(b);
		pts.add(c);
		pts.add(d);
		
		boolean prueba = pts.contains(e);
		Recta res = rA.interpolarPorDistancia(pts, p, rs);
		
		res.imprimir();
		
		
		Pair<Double, Double> ainterp = new Pair<Double, Double>(0.0, 0.0);
	}
}
