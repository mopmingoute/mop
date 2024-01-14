/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AportanteEstado is part of MOP.
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

package interfacesParticipantes;

import estado.VariableEstado;
import futuro.AFIncrementos;

import java.util.ArrayList;
import java.util.Hashtable;

import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;

public interface AportanteEstado {
	
	/**
	 * Carga en la colección del participante o PE las variables de estado de simulación que se usan en el paso corriente
	 * 	
	 */
	public  void actualizarVarsEstadoSimulacion();
	
	/**
	 * Carga en la colección del participante o PE las variables de estado de optimización que se usan en el paso corriente
	 * de la simulación
	 * 	
	 */	
	public void actualizarVarsEstadoOptimizacion();
	
	/**
	 *	En la simulación carga mediante una heurística los valores de las variables de estado del participante al fin del paso. 
	 */
	public void contribuirAS0fint();
	
	
	
	/**
	 *	En la optimización carga mediante una heurística los valores de las variables de estado del participante al fin del paso. 
	 */
	public void contribuirAS0fintOptim();	
	
	
	/**
	 * Devuelve la colección de variables de estado de simulación a ser considerada en la colección global, que está en el simuladorPaso
	 * @return
	 */
	public ArrayList<VariableEstado> aportarEstadoSimulacion();
	

	/**
	 * Devuelve la colección de variables de estado de optimización a ser considerada en la colección global, que está en el simuladorPaso
	 * @return
	 */
	public ArrayList<VariableEstado> aportarEstadoOptimizacion();
	
	/**
	 * A pesar de su nombre este método se usa EN LA SIMULACIÓN
	 * Carga en los valores S0fint de las variables de estado de la optimización los valores de estados agregados a partir de los valores de 
	 * las variables de estado de la simulación, para poder hacer la consulta en el Resoptim (Agregacion de estados)
	 * En los procesos estocásticos el método se implementa en la clase padre ProcesoEstocastico
	 */
	public void cargarValVEOptimizacion();
	
	/**
	 * Carga a partir de la aproximacion futura los valores de los recursos de las variables de estado de la optimización
	 * @param aproxFuturaOpt 
	 */
	public void cargarValRecursoVEOptimizacion(AFIncrementos aproxFuturaOpt);
	

	/**
	 *  Anti-Reducción: Carga en las variables de estado de la simulación los valores reducidos a partir de los valores de 
	 * los recursos de las VE de la optimización. A través de la visibilidad hacia el simulador paso se conoce toda la corrida, en particular 
	 * los valores de los recursos de todas las VE de la optimización. 
	 */
	public void cargarValRecursoVESimulacion();
	
	
	
	/**
	 * Actualiza las variables de estado del participante que son afectadas por
	 * la elección de los controles discretos exhaustivos
	 * PARA EMPLEAR EN LA SIMULACIÓN
	 * 
	 * @param instInicioPaso es el instante inicial del paso
	 * @param varsControlDE son las variables de control que tienen cargado sus valores
	 */
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE);
	
	
	/**
	 * Actualiza las variables de estado del participante que son afectadas por
	 * la elección de los controles discretos exhaustivos
	 * PARA EMPLEAR EN LA OPTIMIZACIÓN
	 * 
	 * @param instInicioPaso es el instante inicial del paso
	 * @param varsControlDE son las variables de control que tienen cargado sus valores
	 */
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE);	
	
	
	
	/**
	 * SE APLICA SOLO PARA LAS VE CONTINUAS DE PARTICIPANTES QUE TIENEN VALORES DEL RECURSO
	 * Para cada una de sus VE continuas el AportanteEstado carga en la tabla el par
	 * (nombre de la VE, nombre de la variable del problema de despacho asociado) 
	 * Esto es necesario AL MENOS EN EL COMPORTAMIENTO HIPERPLANOS para pasar de las variables
	 * duales del problema lineal a los valores de los recursos de las VE continuas.
	 *  
	 */
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla);
		
	
	
	
	
	///////////////  METODOS QUE SE USAN EN LA OPTIMIZACIÓN  ///////////////////////////
	
	
	/**
	 * Carga los valores finales de las VE de los participantes en la transición de la optimización
	 * @param resultado
	 */
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado);
	
	
	/**
	 * Devuelve la derivada del objetivo del problema de despacho respecto a la variable de estado continua vec
	 * Ese valor se obtiene a partir de la variable dual de la ecuación de estado asociada a esa VE.
	 * EL VALOR ESTÁ DATADO EN EL INSTANTE MEDIO DEL PASO DE TIEMPO
	 * 
	 * @param vec variable de estado continua del aportante estado de la que se quiere la variable dual asociada.
	 * @param resultado salida de un problema lineal óptimo
	 * @return 
	 * 
	 */
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado);

	
	public ArrayList<VariableEstado> getVarsEstado();

	/**
	 * Si la discretización o sus valores cambian a lo largo del tiempo, hay que implementar este método en el AportanteEstado
	 * @param instante
	 */
	
	public void actualizaValoresVEDiscretizacionesVariables(long instante);
	
//	public ArrayList<VariableEstado> getVarsEstadoOptim();
	
	////////////////////////////////////////////////////////////////////////////////////
	
}
