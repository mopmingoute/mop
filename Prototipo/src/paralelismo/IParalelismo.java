/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * IParalelismo is part of MOP.
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

package paralelismo;

import java.util.ArrayList;
import java.util.List;

import futuro.ClaveDiscreta;
import futuro.InformacionValorPunto;


public interface IParalelismo {

	/**
	 * El cliente informa al pizarrón que existe una nueva corrida a cargar pasando su nombre y su ruta
	 */
	public void cargarCorrida(String corrida, String ruta);
	
	
	/**
	 * El cliente informa al pizarrón que se debe optimizar la corrida que se pasa como parómetro
	 */	
	public void optimizar(String corrida);
	
	
	/**
	 * El cliente informa al pizarrón que para el paso t, la colección de estados a optimizar es estados 
	 */
	public void optimizarEstados(String corrida, int paso,  ArrayList<int[]> estados);
	
	/**
	 * 	Devuelve los estados no resueltos en el paso t
	 */
	public List<String> getEstadosPendientes(String corrida, int paso);
	

	/**
	 * Devuelve los datos del punto asociado al estado representado por la clave en el paso y corrida establecidos
	 * 
	 */
//	public InformacionValorPunto devuelveInfoValoresPunto(String corrida, int paso, ClaveDiscreta clave);
//
//	/**
//	 * Devuelve el valor de bellman asociado al estado representado por la clave en el paso y corrida establecidos
//	 * 
//	 */
//	public double devuelveValorVBPunto(String corrida, int paso, ClaveDiscreta clave);
//	
//	/**
//	 * Carga la información del punto asociado al estado representado por la clave en el paso y corrida establecidos
//	 * 
//	 */
//	public void cargaInfoValoresPunto(String corrida, int paso, ClaveDiscreta clave, InformacionValorPunto infvp);
//	
	/**
	 * Devuelve el codigo de controles discretos exhausitvos asociados al estado
	 * 
	 */	
	public int[] devuelveCodigoControlesDEOpt(String corrida, int paso, ClaveDiscreta sInit);

	/**
	 * Carga el codigo de controles discretos exhausitvos asociados al estado
	 * 
	 */	
	public void cargaCodigoControlesDEOpt(String corrida, int paso, int[] sInit, int[] codigoOpt);
	
	/**
	 * Informa al pizarron que hay que retroceder un paso
	 * 
	 */
	public void retrocederPaso(String corrida, int pasoNuevo);
	
	/**
	 * Devuelve el resultado de toda la corrida 
	 */	
//	public DatosResultadoOptimizacion getResultadosOptimizacion(String corrida);
	
	/**
	 * 
	 *Devuelve la cantidad de estados del paso establecida
	 */
	public List<String> obtenerKEstados(String corrida, int paso, int cantidad);
	
	/***
	 * Inserta el valor de Bellman en la tabla auxiliar lugo de los controles
	 * 
	 */
	void insertarValorBellmanTablaAux(String corrida, int paso, double vb, ClaveDiscreta estado);
	
	/***
	 * Coloca los estados en la colección de resueltos
	 */
	public void resolverEstados(String corrida, int paso, ArrayList<int[]> estados);
	
	
	/**
	 * El cliente informa al pizarrón que se debe simular la corrida que se pasa como parómetro
	 */
	public void simular(String corrida);
	
	/**
	 * El cliente informa al pizarrón que se deben simular los siguientest escenarios
	 */
	public void simularEscenarios(String corrida, int[] escenarios);
	
	/**
	 * 	Devuelve los escenarios pendientes de la corrida
	 */
	public int[] getEscenariosPendientes(String corrida);
	
	
	/**
	 * 
	 *Devuelve la cantidad de escenarios de la corrida  para resolver
	 */
	
	public int[] obtenerKEscenarios(String corrida,int cantidad);
	
	/**
	 * 
	 * Agrega a la lista de resueltos y elimina de la lista de pendientes a los escenarios
	 */
	
	public void resolverEscenarios(String corrida,int[] escenarios);


	/***
	 * 
	 * Devuelve true si hay una nueva corrida aón no cargada
	 */


	public boolean hayNuevaCorrida();

	/***
	 * 
	 * Devuelve la ruta de la nueva corrida
	 */



	public String dameRutaNueva();


	public int dameOperacion(String corrida);


	InformacionValorPunto devuelveInfoValoresPunto(String corrida, int paso, ClaveDiscreta clave);


	double devuelveValorVBPunto(String corrida, int paso, ClaveDiscreta clave);


	void cargaInfoValoresPunto(String corrida, int paso, ClaveDiscreta clave, InformacionValorPunto infvp);


	int damePasoActual();


	boolean hayQueFinalizarOptimizacion();


	void finalizarOptimizacion();


	void finalizarSimulacion(String string);


	int[] obtenerEscenariosEnResolucion();


	void crearAvisoDesistimiento(String codigo);


	void pasarEscenariosDeEnResolucionAResolver(List<Integer> escenarios);


	void escribirPasoActual(int paso);


	

		

	/**
	 * El cliente obtiene los resultados de un escenario
	 */
	//public DatosEPPUnEscenario getResultadosEscenario(int i)
	
	/**
	 * El servidor guarda los resultados de un escenario
	 */
	//public void guardarResultadosEscenario(DatosEPPUnEscenario, int i);


	void actualizarPedidosAutorizacion(int t);


	ArrayList<int[]> obtenerEstadosEnResolucion();


	void pasarEestadosDeEnResolucionAResolver(ArrayList<int[]> estadosAAvisar);


	ArrayList<String> obtenerAutorizacionesDeGrabacion(int paso);


	void pasarEstadosDeEnResolucionAResueltos();


	boolean existenEstadosSinResolver(String corrida, int numpaso);
}
