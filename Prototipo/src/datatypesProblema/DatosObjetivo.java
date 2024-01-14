/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosObjetivo is part of MOP.
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;

/**
 * Datatype que representa los datos del objetivo asociado al problema lineal
 * @author ut602614
 *
 */
public class DatosObjetivo {
	private Hashtable<String, Double> terminos;	
	private Double terminoIndependiente;	
	
	
	public DatosObjetivo() {
		super();
		terminos = new Hashtable<String, Double>();
		terminoIndependiente = 0.0;
		
	}
	public Double getTerminoIndependiente() {
		return terminoIndependiente;
	}
	public void setTerminoIndependiente(Double terminoIndependiente) {
		this.terminoIndependiente = terminoIndependiente;
	}
	public void contribuir(DatosObjetivo contribucionObjetivo) {
		Set<String> set = contribucionObjetivo.getTerminos().keySet();
		
		Iterator<String> itr = set.iterator();
		String clave;
		Double nuevo;
	    while (itr.hasNext()) {
	    	clave = itr.next();
    		nuevo = terminos.getOrDefault(clave, 0.0) + contribucionObjetivo.getTerminos().get(clave);
    		terminos.remove(clave);


    		if (Math.abs(nuevo) > Constantes.EPSILONCOEF ) { 
	    		terminos.put(clave, nuevo);	
			}		
	    	
	    }
		
		terminoIndependiente += contribucionObjetivo.getTerminoIndependiente();		
	}
	
	
	public void agregarTermino(String var, Double coef) {
		if (Math.abs(coef) > Constantes.EPSILONCOEF ) { 
			double currVal = terminos.getOrDefault(var, 0.0) + coef;
			terminos.put(var, currVal);
		}		
	}

	public Hashtable<String, Double> getTerminos() {
		return terminos;
	}
	public void setTerminos(Hashtable<String, Double> terminos) {
		this.terminos = terminos;
	}
	
	public void sumarTerminoIndependiente(Double nuevo) {
		this.terminoIndependiente += nuevo;
	}
	
	public String creaSalida(){
		
		String a_imprimir = "min: ";
		Set<String> set = this.terminos.keySet();
		
		String clave;
		Double valor;
		
		ArrayList<String> listaaordenar = new ArrayList<String>();
		Iterator<String> itr = set.iterator();
		while (itr.hasNext()) {
			clave = itr.next();
			listaaordenar.add(clave);
		}
		
		Collections.sort(listaaordenar);
		int i = 0;

		for (String k : listaaordenar) {
			
			valor = terminos.get(k);
			if (valor > 0) { 
				a_imprimir = a_imprimir +  " + " ;
			} else { 
				a_imprimir = a_imprimir +  " ";
			}
			a_imprimir = a_imprimir + valor.toString() + k;
			if (i==4) {
				i=0;
				a_imprimir += "\n";
			}
			i++;
	    
		}		
		
		a_imprimir = a_imprimir + " = " + terminoIndependiente.toString();
		
		return a_imprimir;
		
	}
	
	public void imprimir() {
		System.out.println(creaSalida() + ";");
	}
	
	
	public void guardar(String ruta) {
		DirectoriosYArchivos.agregaTexto(ruta, creaSalida() + ";");		
	}
	
	public DatosObjetivo diferencias(DatosObjetivo dobj) {
		/**
		 * Retorna las diferencias entre ambos objetos
		 * ret == this - dobj o dobj == this - ret
		 * 
		*/
		DatosObjetivo ret = new DatosObjetivo();
		boolean cambio = false;
		
		
		HashSet<String> misKeys = new HashSet<String>(terminos.keySet());
		HashSet<String> difKeys = new HashSet<String>(dobj.terminos.keySet());
		misKeys.addAll(difKeys);
		
		double termIndep = dobj.terminoIndependiente;
		cambio = cambio || (Math.abs(terminoIndependiente - dobj.terminoIndependiente) > Constantes.EPSILONCOEF);
		ret.terminoIndependiente = termIndep;
			
		
		Iterator<String> itmK = misKeys.iterator();
		while (itmK.hasNext()) {
			String currKey = itmK.next();
			double currVal = dobj.terminos.getOrDefault(currKey, 0.0);
			cambio = cambio || Math.abs(terminos.getOrDefault(currKey, 0.0) - dobj.terminos.getOrDefault(currKey, 0.0)) > Constantes.EPSILONCOEF;
			ret.terminos.put(currKey, currVal);
		}
		if (!cambio) {
			ret = null;
		}
		
		return ret;
	}
}


