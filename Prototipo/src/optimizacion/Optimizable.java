/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Optimizable is part of MOP.
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

package optimizacion;

import datatypesSalida.DatosPEsUnPaso;
import parque.Azar;
import parque.Corrida;
import procesosEstocasticos.ProcesadorSorteosOptimPEs;
import tiempo.PasoTiempo;


public interface Optimizable {
	
	
	public void inicializarOptimizable();   // entre otras cosas crea el resoptim
		
	public void cargarPasoCorriente(int numpaso, PasoTiempo paso);

	public ResOptim devuelveResOptim();  // devuelve el resoptim que creo
	
	public void sortearInnovMontPEVE();   // sortea innovaciones para PE con VE en la optimización
	
	public void sortearVAMontPENoVE();	// sortea valores VA para PE sin VE en la optimización	
					
    public void optimizarPaso();
	
    public void actualizarParaPasoAnterior();

	public void determinarInstantesMuestreo();

	public void sortearInnovPEDE();

	public void inicializarPEPasoOptim();  // inicializa los tiempos inicial y final del paso en todos los PE

	public void inicializarAzarParaOptimizacion();

	public void setDirSalidas(String dirCompleto);
	
	public String getDirSalidas();

	public void guardarTablasResOptimEnDisco();

	public void optimizarPasoCliente();

	public void actualizarParaPasoAnteriorCliente();
	
	public void actualizarParaPasoAnteriorServidor();

	public void optimizarPasoServidor();

	public void optimizarPasoAproximada();  
	
	public void finalizarOptimizacion();
	
	
	
	
	
	
	/**
	 * Métodos que se usan en los sorteos de prueba de PEs para la optimización
	 */
	
	public DatosPEsUnPaso sortearPEsOptimPaso(ProcesadorSorteosOptimPEs proc);  
	
	public long[] devuelveInstantesMuestreo();  
	
	public Azar devuelveAzar(); 

}
