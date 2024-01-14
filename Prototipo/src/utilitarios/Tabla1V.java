/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Tabla1V is part of MOP.
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
 * Clase que modela una función de una variable por segmentos dados mediante una tabla
 * de puntos (xi, yi)
 * Para valores del argumento x menores que el menor xi o mayores que el mayor xi 
 * puede extrapolar o dar el valor extremo respectivo
 * @author ut469262
 *
 */

public class Tabla1V extends Funcion1V{
	
	private double[] x;   // valores de los argumentos, EN ORDEN NO DECRECIENTE
	private double[] y;   // valores correspondientes de la función
	private boolean extrapola; // si es true se extrapola si no se devuelve el valor de y extremo
	private boolean equiespaciada; // si es true los valores de x están equiespaciados
	private int cantDatos;
	
	public Tabla1V(double[] x, double[] y, boolean extrapola, boolean equiespaciada ) {
		super();
		this.x = x;
		this.y = y;
		this.extrapola = extrapola;
		this.equiespaciada = equiespaciada;
		cantDatos = x.length;
		
	}



	@Override
	public double dameValor(double valor) {
		double alfa = 0.0;
		int ordInf = ordinalInf(x, valor);
		if(ordInf == 0){
			if(!extrapola){
				return y[0];
			}else{
				alfa = (x[ordInf+1]-valor)/(x[ordInf+1] - x[ordInf]);
				return alfa*y[ordInf] + (1-alfa)*y[ordInf+1];
			}
		}else if(ordInf == cantDatos){
			if(!extrapola){
				return y[cantDatos-1];
			}else{
				alfa = (x[ordInf-1]-valor)/(x[ordInf-1] - x[ordInf-2]);
				return alfa*y[ordInf-2] + (1-alfa)*y[ordInf-1];
			}
		}		
		if(ordInf == cantDatos && !extrapola) return y[cantDatos-1];
		if(x[ordInf] == x[ordInf-1]) return y[ordInf];
		alfa = (x[ordInf]-valor)/(x[ordInf] - x[ordInf-1]);
		return alfa*y[ordInf-1] + (1-alfa)*y[ordInf];
	}
	
	
	
	/* 
	 * 
	 * Devuelve, buscando secuencialmente: 
	 * 0 si valorBuscado es menor que el menor valor de x;
	 * cantDatos si valor buscado es mayor que el mayor valor de x
	 * en otro caso devuelve el menor ordinal i (empezando en 1) tal que 
	 * x[i-1] <= valor <= x[i]
	 * @param datos
	 * @param valorBuscado
	 * @return
	 */
	public int ordinalInf(double[] x, double valor){
		
		if(valor<x[0]) return 0;
		if(valor>x[cantDatos-1]) return cantDatos;
		if(!equiespaciada){
			for(int i=1; i<cantDatos-1; i++){
				if(x[i-1] <= valor && valor <= x[i]) return i;
			}
			return cantDatos-1;
		}else{   // los valores de x están equiespaciados
			return (int)Math.ceil((valor - x[0])/(x[cantDatos-1]-x[0])*(cantDatos-1));			
		}
	}
	
	
	
	
	
	public double[] getX() {
		return x;
	}



	public void setX(double[] x) {
		this.x = x;
	}



	public double[] getY() {
		return y;
	}



	public void setY(double[] y) {
		this.y = y;
	}



	public static void main(String[] args){
		double[] x = {0,0,0,1,2,3,4,5,10};
		double[] y = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9};
		Tabla1V tabla = new Tabla1V(x,y,true,true);
		double x1 = 3.5;
		double fx = tabla.dameValor(x1);
		System.out.println(x1 + "  " + fx);
	}
	
}
