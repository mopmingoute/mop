/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Graficador is part of MOP.
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

package procEstocUtils;

import logica.CorridaHandler;
import pizarron.PizarronRedis;

/**
 * Contiene métodos útiles para graficar datos de distribuciones empiricas, como histogramas
 * @author ut469262
 *
 */
public class Graficador {
	
	private double[] datos;
	private int cantDatos;

	public Graficador(double[] datos) {
		super();
		this.datos = datos;
		cantDatos = datos.length;
	}
	
	
	
	/**
	 * Devuelve los límites de las divisiones y la frecuencia empirica de valores de datos que caen en 
	 * cada división, de modo que la suma de frecuencias suma 1.
	 * El histograma tiene cantDiv divisiones, de las que cantRangos-2 están equidistribuídas
	 * entre valorMin y valorMax. La primera división son los valores menoes a valorMin.
	 * La última división son los valores mayores a valorMax.
	 * Las divisiones son abiertas por derecha     |---------)
	 * @param valorMin
	 * @param valorMax
	 * @param cantDiv
	 * @return hist tiene en hist[0][i] el valor máximo de la división i-esima, i=0,...cantDiv-1
	 * 		   hist[0][cantDiv-1] tiene el máximo de Double, Double.MAX_VALUE
	 * 		   hist[1][i] tiene la frecuencia empírica de la división respectiva.
	 */
	public double[][] histogramaStd(double valorMin, double valorMax, int cantDiv){
		double[][] hist = new double[2][cantDiv];
		if(valorMin>=valorMax || cantDiv<2){
			System.out.println("error en llamado de histograma");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		for(int id =0; id<cantDiv-1; id++){			
			hist[0][id] = valorMin + id*(valorMax - valorMin)/(cantDiv-2);
		}
		hist[0][cantDiv-1] = Double.MAX_VALUE;
		for(int t=0; t<cantDatos; t++){
			int id=0; 
			while(datos[t]>=hist[0][id]){
				id++;
			}
			hist[1][id]++;
		}
		return hist;
	}
	
	/**
	 * Devuelve un texto con valores separados por comas con el histograma
	 * Ejemplo
	 * 
	 * menor a -5, -5 a -4, -4 a -3, ....., mayor a 40
	 *       2  ,  20    , 4     ,       , 0
	 * 
	 * @param hist
	 * @return
	 */
	public String textoHistograma(String titulo, double[][] hist){
		int cantDiv = hist[0].length;
		StringBuilder sb = new StringBuilder(titulo + "\n");
		sb.append("menor a " + hist[0][0] + "\t");
		for(int id=0; id<cantDiv-2; id++){
			sb.append(hist[0][id] + " a " + hist[0][id+1] + "\t");			
		}
		sb.append("mayor a " + hist[0][cantDiv-2] + "\n");
		for(int id=0; id<cantDiv; id++){
			sb.append(hist[1][id] + "\t");
		}
		return sb.toString();
	}

}
