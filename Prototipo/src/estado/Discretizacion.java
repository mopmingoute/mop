/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Discretizacion is part of MOP.
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

package estado;


import java.util.ArrayList;
import java.util.Collections;

//import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;

import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import datatypes.DatosDiscretizacion;
import logica.CorridaHandler;
import pizarron.PizarronRedis;


public class Discretizacion{
	/*
	 * Valores que puede tomar la variable de estado, sea discreta o continua discretizada
	 * ordenados en forma creciente
	 */
	private ArrayList<Double> valores;   // deben estar ordenados
	private Variable varAsoc; // variable a la que estó asociada las discretización this;
	private final double EPSIVALOR = 0.00001;  // Tolerancia para identificar un valor discretizado del estado
	
	private int cantValores; // la cantidad de valores, dimensión de la lista valores
	private double valMin; // valor mónimo de la discretización
	private double valMax; // valor móximo de la discretización
	private boolean equiespaciada;  // TRUE si los valores de la discretización son equiespaciados
	 
	private double paso;  // Si equiespaciada = true, es el paso de discretización entre dos valores cualesquiera.

	public Discretizacion(Variable varAsoc, ArrayList<Double> valores, boolean equi) {
		this.setVarAsoc(varAsoc);
		this.setValores(valores);
		this.setEquiespaciada(equi);
		completaConstruccion();
	}
	

	public Discretizacion(DatosDiscretizacion datos) {		
		valores = new ArrayList<Double>();
		for (int i = 0; i < datos.getParticion().length; i++)
			valores.add(datos.getParticion()[i]);
		completaConstruccion();
		
	}
	
	public void completaConstruccion(){
		Collections.sort(valores);
		valMin = valores.get(0);
		cantValores = valores.size();
		if(cantValores<3){
			System.out.println("UNA DISCRETIZACIóN TIENE MENOS DE TRES VALORES");
		}
		valMax = valores.get(cantValores-1);
		paso = 0;
		if(equiespaciada) paso = (valMax-valMin)/(cantValores-1);		
	}
	
	
	/**
	 * Devuelve el valor de ordinal iord, empezando de cero
	 * @param iord 
	 */
	public double devuelveValorOrdinal(int iord){
		return valores.get(iord);
	}


	/**
	 * Devuelve el código entero ordinal (empezando en 0) de la discretización inmediatamente inferior o igual a valor
	 * si la variable es continua y el código entero exacto si la variables es discreta.
	 * La excepción es para las variables continuas, si el valor es menor que la menor discretización o mayor que la óltima
	 * 
	 * Si ademós de discreta es ordinal (0, 1, 2...) y no importa el cardinal, devuelve el valor como entero
	 * 
	 * Si la variable es continua:
	 * - Si valor es menor que el primer (menor) punto de discretización se devuelve 0
	 * - Si valor es mayor que el óltimo (mayor) punto de discretización se devuelve el penóltimo punto.
	 * 
	 * @param valor : es el valor del que se quiere encontrar el código entero
	 */	 	
	public int devuelvePasoInferiorParaInterpolar(double valor){	
		if(varAsoc.isOrdinal()){
			// la variable es ordinal, es decir que su propio valor (0, 1, 2...) es la discretización
			return (int)valor;
		}
		else if(varAsoc.isDiscreta()){
			// como la variable es discreta se recorren los valores exhaustivamente
			int ind=0;
			while(Math.abs((valores.get(ind)-valor))>EPSIVALOR){
				ind++;
				if(ind==cantValores){
					System.out.println("Clase Discretizacion: Error en discretización de variable " + varAsoc.getNombre());
					if (CorridaHandler.getInstance().isParalelo()){
						//PizarronRedis pp = new PizarronRedis();
				//		pp.matarServidores();
					}
					System.exit(1);
				}				
			}
			return ind;			
		}else{
			// la variable es continua
			double indReal;		
			if(equiespaciada){
				// la variable es equiespaciada
				indReal = (valor - valMin)/paso;
				if(indReal<0){
					return 0;				
				}else if (indReal> cantValores-1){
					return cantValores-2;
				}else{
					return (int)Math.floor(indReal);					
				}
				
			}else{
				// la discretización de una variable continua no es equiespaciada
				if(valor<=valMin){
					return 0;
				}else if(valor>= valMax){    
					return cantValores-2;
				}else{
					// como los valores no estón equiespaciados se recorre sistemóticamente desde el menor
					int ind=0;
					while(valor>=valores.get(ind+1)){
						ind++;
						if(ind==cantValores-1){
							System.out.println("Clase Discretizacion: Error en discretización de variable " + varAsoc.getNombre());
							if (CorridaHandler.getInstance().isParalelo()){
								//PizarronRedis pp = new PizarronRedis();
								//pp.matarServidores();
							}
							System.exit(1);
						}				
					}
					return ind;	
				}

			}
		
		}
	}
	
	
	
	
	public ArrayList<Double> getValores() {
		return this.valores;
	}

	public void setValores(ArrayList<Double> valores) {
		this.valores = valores;
	}
	
	
	
	
    public Variable getVarAsoc() {
		return varAsoc;
	}




	public void setVarAsoc(Variable varAsoc) {
		this.varAsoc = varAsoc;
	}




	public boolean isEquiespaciada() {
		return equiespaciada;
	}




	public void setEquiespaciada(boolean equiespaciada) {
		this.equiespaciada = equiespaciada;
	}

	
	



	public int getCantValores() {
		return cantValores;
	}




	public void setCantValores(int cantValores) {
		this.cantValores = cantValores;
	}




	public double getValMin() {
		return valMin;
	}




	public void setValMin(double valMin) {
		this.valMin = valMin;
	}




	public double getValMax() {
		return valMax;
	}




	public void setValMax(double valMax) {
		this.valMax = valMax;
	}




	public double getPaso() {
		return paso;
	}




	public void setPaso(double paso) {
		this.paso = paso;
	}




	public double getEPSIVALOR() {
		return EPSIVALOR;
	}


	
}
