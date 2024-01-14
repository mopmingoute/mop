/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosRestriccion is part of MOP.
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

package datatypesProblema;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;

/**
 * Datatype que representa los datos asociados a una restricción del problema lineal
 * @author ut602614
 *
 */
public class DatosRestriccion implements Comparable{
	private String nombre;
//	private ArrayList<String> vars;
//	private ArrayList<Double> coefs;
	private Hashtable<String,Double> terminos;
	
	private Double segundoMiembro;
	
	private int tipo; /**Igualdad, menoroigual, mayoroigual*/
	
	
	public void agregarTermino(String var, Double coef) {
			
		if (Math.abs(coef) > Constantes.EPSILONCOEF ) { 
//			vars.add(var);
//			coefs.add(coef);
			terminos.put(var,coef);
		}
	}

	public Double getTermino(String nomTerm) {
		return terminos.get(nomTerm);
	}
	
	public DatosRestriccion() {
//		this.vars = new ArrayList<String>();
//		this.coefs = new ArrayList<Double>();
		this.terminos = new Hashtable<String,Double> ();
		segundoMiembro = 0.0;
	}


	/**
	 * 
	 * @param dr Atención, no cambia el tipo
	 */
	public void contribuir(DatosRestriccion dr) {
/*		int j =0;
		for (String var: dr.getVars()) {
			int indice =vars.indexOf(var);
			if (indice >=0) {
				coefs.set(indice, coefs.get(indice) + dr.getCoefs().get(j));
			} else {
				this.agregarTermino(var, dr.getCoefs().get(j));
			}			
			j++;
		}
*/		
		HashSet<String> drVars = new HashSet<String>(dr.terminos.keySet());
		Iterator<String> itdV = drVars.iterator();
		while (itdV.hasNext()) {
			String currKey = itdV.next();
			double currVal = terminos.getOrDefault(currKey, 0.0) + dr.terminos.get(currKey);
			terminos.remove(currKey);
    		if (Math.abs(currVal) > Constantes.EPSILONCOEF ) { 
    			terminos.put(currKey, currVal);
			}		
			
		}
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Devuelve los coeficientes ordenados por el orden alfabético de las variables respectivas
	 * @return
	 */
	public ArrayList<Double> getCoefs() {
		Set<String> conjS = terminos.keySet();
		ArrayList<String> al = new ArrayList<String>();
		al.addAll(conjS);
		Collections.sort(al);
		ArrayList<Double> ret = new ArrayList<Double>();
		for(String s: al) {
			ret.add(terminos.get(s));
		}
		return ret;
	}
/*	
 	public void setCoefs(ArrayList<Double> coefs) {
		this.coefs = coefs;
	}
*/
	/**
	 * Devuelve los nombres de variables en orden alfabético 
	 * @return
	 */
	public ArrayList<String> getVars() {
		Set<String> conjS = terminos.keySet();
		ArrayList<String> al = new ArrayList<String>();
		al.addAll(conjS);
		Collections.sort(al);
		return al;
	}
/*	
	public void setVars(ArrayList<String> vars) {
		this.vars = vars;
	}
*/
	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public Double getSegundoMiembro() {
		return segundoMiembro;
	}

	public void setSegundoMiembro(Double segundoMiembro) {
		this.segundoMiembro = segundoMiembro;
	}

	
	
	
	public Hashtable<String, Double> getTerminos() {
		return terminos;
	}


	public void setTerminos(Hashtable<String, Double> terminos) {
		this.terminos = terminos;
	}


	public String creaSalida(){
		
		String a_imprimir = this.nombre + "  :  \t";
		DecimalFormat df = new DecimalFormat("0.000000000000000000000000");
		df.setMaximumFractionDigits(4);
		ArrayList<String> vars = this.getVars();
		ArrayList<Double> coefs = this.getCoefs();
		for (int i = 0; i < vars.size(); i++) {
			String coefSt = coefs.get(i).toString();
			Double coef = Double.parseDouble(coefSt);
			df.setMaximumFractionDigits(24);
			coefSt = df.format(coef) + ".";
			 
			if (coefs.get(i) == -1) coefSt = "-";
			if (coefs.get(i) == 1) coefSt = "";
		
			if (i==0 || coefs.get(i)<0) {
				a_imprimir =  a_imprimir + coefSt  + vars.get(i) + "\t";
			} else {
				a_imprimir =  a_imprimir +  " + " + coefSt + vars.get(i) + "\t";
			}
		
		}
		String segundoSt = segundoMiembro.toString();
		Double coef = Double.parseDouble(segundoSt);
		df.setMaximumFractionDigits(2);
		segundoSt = df.format(coef);
		String tipoString = null;
		if (this.tipo == Constantes.RESTIGUAL) tipoString = " = ";
		if (this.tipo == Constantes.RESTMAYOROIGUAL) tipoString = " >= ";
		if (this.tipo == Constantes.RESTMENOROIGUAL) tipoString = " <= ";
		a_imprimir = a_imprimir + tipoString + "\t" + segundoSt.toString();		
		return a_imprimir;
		
		
	}
	
	
	public String creaSalidaLpSolve(){
		
		String a_imprimir = this.nombre + ":  ";
		ArrayList<String> vars = this.getVars();
		ArrayList<Double> coefs = this.getCoefs();
		String sig = "";
		double num;
		for (int i = 0; i < vars.size(); i++) {	
			if(coefs.get(i)>0) {
				if(i>=0) sig = " + ";
				num = coefs.get(i);
			}else {
				sig = " - ";
				num = - coefs.get(i);
			}
			a_imprimir =  a_imprimir + sig + num + " " + vars.get(i) + " ";			
		}

		String tipoString = null;
		if (this.tipo == Constantes.RESTIGUAL) tipoString = " = ";
		if (this.tipo == Constantes.RESTMAYOROIGUAL) tipoString = " >= ";
		if (this.tipo == Constantes.RESTMENOROIGUAL) tipoString = " <= ";
		a_imprimir = a_imprimir + tipoString + " " +segundoMiembro + ";";		
		return a_imprimir;
		
		
	}
	
	

	public void imprimir() {
	
		System.out.println(creaSalida() + ";");
	}


	public void guardar(String ruta) {

		DirectoriosYArchivos.agregaTexto(ruta, creaSalida() + ";");
		
	}

	public DatosRestriccion diferencias(DatosRestriccion drestr) {
		/**
		 * Retorna las diferencias entre ambas restricciones
		 * ret == this - dobj o dobj == this - ret
		 * 
		*/
		DatosRestriccion ret = null;
		boolean cambio = false;
		if (drestr.tipo == tipo && drestr != null) {
			if (nombre.equals(drestr.nombre)) {
				
				ret = new DatosRestriccion();
				
				ret.tipo = drestr.tipo;
				ret.nombre = nombre;
				
				HashSet<String> misKeys = new HashSet<String>(terminos.keySet());
				HashSet<String> difKeys = new HashSet<String>(drestr.terminos.keySet());
				misKeys.addAll(difKeys);
				
				double termIndep = drestr.segundoMiembro;
				cambio = cambio || (Math.abs(segundoMiembro - drestr.segundoMiembro) > Constantes.EPSILONCOEF);
				ret.segundoMiembro = termIndep;
					
				
				Iterator<String> itmK = misKeys.iterator();
				while (itmK.hasNext()) {
					String currKey = itmK.next();
					double currVal = drestr.terminos.getOrDefault(currKey, 0.0);
					ret.terminos.put(currKey, currVal);
					cambio = cambio || Math.abs(terminos.getOrDefault(currKey, 0.0) - drestr.terminos.getOrDefault(currKey, 0.0)) > Constantes.EPSILONCOEF;
				}
			} 
		}
		if (!cambio) {
			ret = null;
		}
			

		return ret;
	}


	@Override
	public int compareTo(Object o) {
		String nomO = ((DatosRestriccion)o).getNombre();
		return this.getNombre().compareTo(nomO);
	}

	
	
}


