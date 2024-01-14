/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * IServidor is part of MOP.
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


import futuro.ClaveDiscreta;
import futuro.InformacionValorPunto;

public interface IServidor {
	
	public boolean hayNuevaCorrida();

	public String dameRutaNueva();

	public int dameOperacion();

	public int obtenerPasoOptim();

	public void pasarPaqueteAResuelto(Paquete paquete);

	public boolean hayQueFinalizarOptimizacion();
		
	public boolean hayQueFinalizarSimulacion();
		
	public int[] devuelveCodigoControlesDEOpt(String corrida, int paso, ClaveDiscreta sInit);

	public void cargaCodigoControlesDEOpt(String corrida, int paso, int[] sInit, int[] codigoOpt);

	public Paquete obtenerPaqueteAResolver(int paso);

	public PaqueteEscenarios obtenerPaqueteEscenariosAResolver();

	void pasarPaqueteEscenarioAResuelto(PaqueteEscenarios paquete);

	void pasarPaqueteAEnResolucion(Paquete paquete);
	
}
