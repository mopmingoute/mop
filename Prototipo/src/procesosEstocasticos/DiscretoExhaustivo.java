/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DiscretoExhaustivo is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;

import estado.VariableEstado;

/**
 * Interfase que deben cumplir los procesos discretos exhaustivo DE
 * @author ut469262
 * Los PE que son DE tienen en la optimizaci�n un comportamiento especial.
 * En lugar de sortear innovaciones por Montecarlo, para los PEDE se computan las probabilidades
 * de transici�n para todas las transiciones posibles de las VE del proceso entre
 * el instante fin t-1 y el instante ini t  ("el saltito")
 * 
 * Los procesos discretos exhaustivos tienen una �nica variable de estado discreta.
 * Siempre se puede reducir un proceso con muchas variables de estado discretas a 
 * un proceso con una sola, con los estados compuestos.
 * 
 * Los procesos discretos exhaustivos no cambian su variable de estado a lo largo del tiempo
 *
 */
public interface DiscretoExhaustivo {
	
	
	
	/**
	 * Devuelve la cantidad de valores enteros posibles de la variable discreta exhaustiva
	 * Los valores son los enteros entre 0 y la cantidad de valores posibles menos 1
	 * @param instante
	 * @return
	 */
	public int devuelveCantPosibles(long instante);	
	

	/**
	 * Devuelve el nombre asociado a un valor entero posibles de la variable discreta exhaustiva
	 * @param instante
	 * @return
	 */
	public ArrayList<String> devuelveNombreValor(long instante, int valor);
	
	
	
	/**
	 * Devuelve la probabilidad de que la variable de estado pase del valor valorIni en el instante instanteIni
	 * al valor valorFin en el instante instanteFin
	 * 
	 * @param instanteIni
	 * @param instanteFin
	 * @param valorPasoIni  c�digo entero asociado al valor en el instante inicial
	 * @param valorPasoFin  c�digo entero asociado al valor en el instante final
	 * @return
	 */
	public double devuelveProbTransicion(long instanteIni, long instanteFin, int valorIni, int valorFin);
	
	
	/**
	 * En la SDDP (Dual) devuelve la cantidad de sorteos de Montecarlo que requiere el proceso dado el paso de tiempo y el estado (empezando en 0) 
	 * @param paso es el paso de tiempo de la corrida
	 * @param estado es el número de estado discreto del PE DE empezando en cero
	 */
	public int dameCantMontecarlosDual(int paso, int estado);
	
	
	/**
	 * En la SDDP (Dual) devuelve el multiplicador de la cantidad de sorteos de Montecarlo de todos los procesos que no son DE que este proceso DE requiere en el paso de tiempo t y 
	 * el estado e.
	 * Ejemplo: El proceso Markov en los estados secos requiere que por cada Montecarlo de Markov, los procesos de eólica y solar y de roturas de centrales produzcan tres
	 * realizaciones, es decir que la cantidad de Montecarlos del Markov se multiplica por tres. 
	 */
	public int dameMultiplicadorMontecarlosDual(int paso, int estado);
	
	
	
	/**
	 * Devuelve el nombre del proceso estoc�stico discreto exhaustivo
	 */
	public String getNombrePE();
	
	/**
	 * Devuelve el nombre de la variable de estado del proceso
	 * que es �nica
	 */
	public String getNombreVEPEDE();
	
//	public ArrayList<VariableEstado> aportarEstadoPEDE();
	

}
