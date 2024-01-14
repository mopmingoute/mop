/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Pizarron is part of MOP.
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

package pizarron;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import datatypesSalida.DatosEPPUnEscenario;
import futuro.ClaveDiscreta;
import futuro.InformacionValorPunto;

public abstract class Pizarron {
	/**
	 * FUNCIONES DE CARGAR CORRIDA
	 */
	public abstract void cargarCorrida(String corrida, String ruta);

	public abstract boolean hayNuevaCorrida();

	public abstract String dameRutaNueva();

	// FIN FUNCIONES CARGAR CORRIDA

	/**
	 * ============================================ FUNCIONES DE LA OPTIMIZACION
	 * ============================================
	 */

	/**
	 * 
	 * METODOS DEL CLIENTE
	 */
	public abstract void optimizar();

	public abstract void escribirPasoActual(int paso);

	public abstract void cargarPaquetesAResolver(int paso, ArrayList<Paquete> paquetes);
	
	public abstract ArrayList<Paquete> obtenerPaquetesResueltos(int paso);
	
	public abstract void finalizarOptimizacion();

	// FIN METODOS CLIENTE

	/***
	 * METODOS DEL SERVIDOR
	 */

	public abstract int dameOperacion();

	public abstract int obtenerPasoOptim();

	public abstract void pasarPaqueteAResuelto(Paquete paquete);

	public abstract Paquete obtenerPaqueteAResolver(int paso);
	
	public abstract boolean hayQueFinalizarOptimizacion();
	
	

	// FIN METODOS SERVIDOR

	/**
	 * ============================================ FUNCIONES DE LA SIMULACION
	 * ============================================
	 */

	/**
	 * 
	 * METODOS DEL CLIENTE
	 */
	public abstract void simular();

	public abstract void cargarPaquetesEscenarioAResolver(ArrayList<PaqueteEscenarios> paquetes);
	
	public abstract ArrayList<PaqueteEscenarios> obtenerPaquetesEscenariosResueltos();
	
	public abstract void finalizarSimulacion();

	// FIN METODOS CLIENTE

	/***
	 * METODOS DEL SERVIDOR
	 */

	public abstract PaqueteEscenarios obtenerPaqueteEscenariosAResolver();

	public abstract boolean hayQueFinalizarSimulacion();

	public abstract void pasarPaqueteEscenarioAResuelto(PaqueteEscenarios paquete);


	// FIN METODOS SERVIDOR


	/***
	 *
	 * MóTODOS QUE LEEN Y ACTUALIZAN TABLAS. SON USADOS DESDE EL CLIENTE Y DESDE EL
	 * SERVIDOR
	 */
	
	public abstract int[] devuelveCodigoControlesDEOpt(String corrida, int paso, ClaveDiscreta sInit);

	public abstract void cargaCodigoControlesDEOpt(String corrida, int paso, int[] sInit, int[] codigoOpt);

	public abstract void imprimirListaAResolver();

	public abstract void imprimirListaResueltos();

	public abstract int obtenercantServidores();

	public abstract void registrarMaquina();
	
	public abstract void cargaTablaAuxiliar(int paso, Hashtable<ClaveDiscreta, InformacionValorPunto> tabla);

	public abstract void cargaTabla(int paso, Hashtable<ClaveDiscreta, InformacionValorPunto> tabla);

	public abstract Hashtable<ClaveDiscreta, InformacionValorPunto> devuelveTabla(int paso);

	public abstract Hashtable<ClaveDiscreta, InformacionValorPunto> devuelveTablaAuxiliar(int paso, int cantPaquetes, int cantEstados);

	public abstract void guardarEscenario(int numero, DatosEPPUnEscenario esc);
	
	public abstract DatosEPPUnEscenario levantarEscenario(int numero);

	protected abstract Set<String> obtenerServidores();

	public abstract void pasarPaqueteAEnResolucion(Paquete paquete);

	public abstract ArrayList<Paquete> obtenerPaquetesEnResolucion(int paso);

	public abstract void cargaTablaControles(int paso, Hashtable<ClaveDiscreta, int[]> aCargar);

	public abstract Hashtable<ClaveDiscreta, int[]> devuelveTablaControles(int paso, int cantPaquetes, int cantEstados);

	public abstract Hashtable<ClaveDiscreta, int[]> devuelveTablaControlesDE(int paso);

	
	




	/***
	 * 
	 * FIN MóTODOS QUE LEEN Y ACTUALIZAN TABLAS. SON USADOS DESDE EL CLIENTE Y DESDE
	 * EL SERVIDOR
	 */

	
}
