/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ServidorHandler is part of MOP.
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

public class ServidorHandler implements IServidor{

	private static ServidorHandler instance; 
	private Pizarron pizarron;
	
	/**Función del singleton que devuelve siempre la misma instancia*/	
	public static ServidorHandler getInstance()
	{
		if (instance  == null)  
			instance = new ServidorHandler();
	
		return instance;
	}
	
	
	
	public ServidorHandler() {
		super();
		pizarron  = PizarronRedis.getInstance();
	}



	@Override
	public boolean hayNuevaCorrida() {
		return pizarron.hayNuevaCorrida();
	}

	@Override
	public String dameRutaNueva() {		
		return pizarron.dameRutaNueva();
	}

	@Override
	public int dameOperacion() {		
		return pizarron.dameOperacion();
	}

	@Override
	public int obtenerPasoOptim() {
		return pizarron.obtenerPasoOptim();
	}

	@Override
	public void pasarPaqueteAResuelto(Paquete paquete) {
		pizarron.pasarPaqueteAResuelto(paquete);		
	}
	
	@Override
	public void pasarPaqueteAEnResolucion(Paquete paquete) {
		pizarron.pasarPaqueteAEnResolucion(paquete);		
	}

	@Override
	public void pasarPaqueteEscenarioAResuelto(PaqueteEscenarios paquete) {
		pizarron.pasarPaqueteEscenarioAResuelto(paquete);		
	}
	
	@Override
	public boolean hayQueFinalizarOptimizacion() {
		return pizarron.hayQueFinalizarOptimizacion();
	}


	@Override
	public boolean hayQueFinalizarSimulacion() {		
		return pizarron.hayQueFinalizarSimulacion();
	}



	@Override
	public int[] devuelveCodigoControlesDEOpt(String corrida, int paso, ClaveDiscreta sInit) {		
		return pizarron.devuelveCodigoControlesDEOpt(corrida, paso, sInit);
	}

	@Override
	public void cargaCodigoControlesDEOpt(String corrida, int paso, int[] sInit, int[] codigoOpt) {
		pizarron.cargaCodigoControlesDEOpt(corrida, paso, sInit, codigoOpt);		
	}


	@Override
	public Paquete obtenerPaqueteAResolver(int paso) {
		
		return pizarron.obtenerPaqueteAResolver(paso);
	}
	
	@Override
	public PaqueteEscenarios obtenerPaqueteEscenariosAResolver() {
		
		return pizarron.obtenerPaqueteEscenariosAResolver();
	}
	
	public Pizarron getPizarron() {
		return pizarron;
	}

	public void setPizarron(Pizarron pizarron) {
		this.pizarron = pizarron;
	}



	public void imprimirListaAResolver() {
		pizarron.imprimirListaAResolver();
		
	}



	public void imprimirListaResueltos() {
		pizarron.imprimirListaResueltos();
		
		
	}


	

}
