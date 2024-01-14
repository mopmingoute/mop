/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Polinomio is part of MOP.
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import datatypes.DatosPolinomio;
import datatypes.Pair;

public class Polinomio {
	private String tipo;
	private double[] coefs;
	private Double xmin;
	private Double xmax;
	private Double valmin;
	private Double valmax;
	private Hashtable<String, Polinomio> colpols;
	private ArrayList<Polinomio> polsrango;
	private Polinomio fueraRango;
	private ArrayList<Pair<Double, Double>> rangos;
	private ArrayList<Pair<Double, Double>> segmentos;

	public Polinomio() {
		tipo = "poli";
		coefs = new double[3];

	}

	public Polinomio(String tipo, double[] coefs, Double xmin, Double xmax, Double valmin, Double valmax) {
		super();
		this.tipo = tipo;
		this.coefs = coefs;
		this.xmin = xmin;
		this.xmax = xmax;
		this.valmin = valmin;
		this.valmax = valmax;
	}

	public Polinomio(DatosPolinomio datosPolinomio) {
		this.tipo = datosPolinomio.getTipo();
		this.coefs = datosPolinomio.getCoefs();
		this.xmin = datosPolinomio.getXmin();
		this.xmax = datosPolinomio.getXmax();
		this.valmin = datosPolinomio.getValmin();
		this.valmax = datosPolinomio.getValmax();
		this.colpols = new Hashtable<String, Polinomio>();
		this.polsrango = new ArrayList<Polinomio>();
		

		if (tipo.equalsIgnoreCase("poliMulti")) {
			Set<String> claves = datosPolinomio.getPols().keySet();
			Iterator<String> it = claves.iterator();
			while (it.hasNext()) {
				String clave = it.next();
				Polinomio nuevo = new Polinomio(datosPolinomio.getPols().get(clave));
				colpols.put(clave, nuevo);
			}
		}
		if (tipo.equalsIgnoreCase("porRangos")) {
			this.setFueraRango(new Polinomio(datosPolinomio.getFueraRango()));
			this.setRangos(datosPolinomio.getRangos());
			for (DatosPolinomio dp : datosPolinomio.getPolsrangos())
				this.polsrango.add(new Polinomio(dp));
		}
		
		if (tipo.equalsIgnoreCase("porSegmentos")) {
			this.setSegmentos(datosPolinomio.getSegmentos());			
		}
	}

	public double dameValor(double entrada) {
		if (tipo.equalsIgnoreCase("porSegmentos")) {
			double anterior = -1000000000;
			double yanterior = 0;
			for (Pair<Double,Double> xy: this.getSegmentos()) {
				if (entrada>anterior && entrada<=xy.first) {
					return (entrada-anterior)/(xy.first-anterior)*(xy.second-yanterior)+yanterior;
				}
				anterior = xy.first;
				yanterior = xy.second;
			}
		}

		if (tipo.equalsIgnoreCase("policoncotas")) {
			if (entrada < xmin) {
				return valmin;
			}
			if (entrada > xmax) {
				return valmax;
			}
		} else if (tipo.equalsIgnoreCase("porRangos")) {
			int i = 0;
			for (Pair<Double, Double> rango : this.rangos) {
				if (entrada >= rango.first && entrada < rango.second)
					return this.polsrango.get(i).dameValor(entrada);
				i++;
			}
			return this.fueraRango.dameValor(entrada);
		}

		double resultado = coefs[coefs.length - 1];

		for (int i = coefs.length - 2; i >= 0; i--) {
			resultado = resultado * entrada + coefs[i];
		}		
	
		return resultado;
	}

	public double dameValor(Hashtable<String, Double> parametros) {
		double resultado = 0;
		Set<String> claves = parametros.keySet();
		Iterator<String> it = claves.iterator();
		String clave;
		while (it.hasNext()) {
			clave = it.next();
			resultado += colpols.get(clave).dameValor(parametros.get(clave));
		}

		return resultado;
	}

	public double dameValor(String varA, String varB, double a, double b) {
		if (tipo.equalsIgnoreCase("poliMulti")) {
			double resultado = 0;
		
			
			ArrayList<String> claves = new ArrayList<String>();
			claves.add(varA);
			claves.add(varB);
			Iterator<String> it = claves.iterator();


//			System.out.println("A: " + a);
//			System.out.println("B: " + b);
			
			resultado += colpols.get(it.next()).dameValor(a);

//			System.out.println("evaluo : " + resultado );
			
			resultado += colpols.get(it.next()).dameValor(b);
		//	System.out.println("sumo B: " + b);

			return resultado;
		} 
		if (tipo.equalsIgnoreCase("porRangos")) {
			int i = 0;
			for (Pair<Double, Double> rango : this.rangos) {
				if (a >= rango.first && a < rango.second)
					return this.polsrango.get(i).dameValor(varA,varB,a,b);
				i++;
			}
			return this.fueraRango.dameValor(varA,varB,a,b);
		}
		return 0;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public double[] getCoefs() {
		return coefs;
	}

	public void setCoefs(double[] coefs) {
		this.coefs = coefs;
	}

	public Double getXmin() {
		return xmin;
	}

	public void setXmin(Double xmin) {
		this.xmin = xmin;
	}

	public Double getXmax() {
		return xmax;
	}

	public void setXmax(Double xmax) {
		this.xmax = xmax;
	}

	public Double getValmin() {
		return valmin;
	}

	public void setValmin(Double valmin) {
		this.valmin = valmin;
	}

	public Double getValmax() {
		return valmax;
	}

	public void setValmax(Double valmax) {
		this.valmax = valmax;
	}

	public Hashtable<String, Polinomio> getColpols() {
		return colpols;
	}

	public void setColpols(Hashtable<String, Polinomio> colpols) {
		this.colpols = colpols;
	}

	public ArrayList<Polinomio> getPolsrango() {
		return polsrango;
	}

	public void setPolsrango(ArrayList<Polinomio> polsrango) {
		this.polsrango = polsrango;
	}

	public Polinomio getFueraRango() {
		return fueraRango;
	}

	public void setFueraRango(Polinomio fueraRango) {
		this.fueraRango = fueraRango;
	}

	public ArrayList<Pair<Double, Double>> getRangos() {
		return rangos;
	}

	public void setRangos(ArrayList<Pair<Double, Double>> rangos) {
		this.rangos = rangos;
	}
	

	public ArrayList<Pair<Double, Double>> getSegmentos() {
		return segmentos;
	}

	public void setSegmentos(ArrayList<Pair<Double, Double>> segmentos) {
		this.segmentos = segmentos;
	}



}

