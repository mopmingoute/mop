/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosSalidaProblemaLineal is part of MOP.
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Hashtable;


/**
 * Datatype que representa los datos de salida de un problema lineal
 * @author ut602614
 *
 */

public class DatosSalidaProblemaLineal implements Serializable{
	private Hashtable<String, Double> solucion;
	private Hashtable<String, Double> duales;
	private Double valorOptimo;
	private boolean infactible;

	
	public DatosSalidaProblemaLineal() {
		super();
		this.solucion = new Hashtable<String,Double>();
		this.duales = new Hashtable<String,Double>();
		this.valorOptimo = 0.0;
		this.infactible = false;
		
	}
	public Double getValorOptimo() {
		return valorOptimo;
	}
	public void setValorOptimo(Double valorOptimo) {
		this.valorOptimo = valorOptimo;
	}
	public Hashtable<String, Double> getSolucion() {
		return solucion;
	}
	public void setSolucion(Hashtable<String, Double> solucion) {
		this.solucion = solucion;
	}
	public Hashtable<String, Double> getDuales() {
		return duales;
	}
	public void setDuales(Hashtable<String, Double> duales) {
		this.duales = duales;
	}
	public boolean isInfactible() {
		return infactible;
	}
	public void setInfactible(boolean infactible) {
		this.infactible = infactible;
	}
	public void printSolucion(String path) {
		try {
			File f = new File(path);
			PrintWriter writer = new PrintWriter(path);
			writer.println("opt\t" + valorOptimo);

			writer.println("Vars");
			double val;
	        for(String nVar: solucion.keySet()){
	        	val = solucion.get(nVar);
	    		writer.println(nVar + '\t' + val);
			}
			writer.println("Dual");
	        for(String nDual: duales.keySet()){
	        	val = duales.get(nDual);
	    		writer.println(nDual + '\t' + val);
			}
			writer.close();
//			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
