/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ICliente is part of MOP.
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

import futuro.ClaveDiscreta;
import futuro.InformacionValorPunto;

public interface ICliente {
	
	public void cargarCorrida(String corrida, String ruta);

	public void optimizar();

	public void escribirPasoActual(int paso);

	void cargarPaquetesAResolver(int paso);
	
	public ArrayList<Paquete> obtenerPaquetesResueltos(int paso);
	
	public void finalizarOptimizacion();

	public void simular();

	public void finalizarSimulacion();

	public int[] devuelveCodigoControlesDEOpt(String corrida, int paso, ClaveDiscreta sInit);

	public void cargaCodigoControlesDEOpt(String corrida, int paso, int[] sInit, int[] codigoOpt);

	public void resolucionPaquetes();

	void resolucionPaquetesEscenarios();

	public void cargarPaquetes(int totalEstados, int paso, int estadosPorPaquete);
	
	public int obtenercantServidores();

	public ArrayList<PaqueteEscenarios> obtenerPaquetesEscenariosResueltos();

	void cargarPaquetesEscenariosAResolver();

	void cargarPaquetesEscenarios(int totalEscenarios, int escenariosPorPaquete);
	
}
