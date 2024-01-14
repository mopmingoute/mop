/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Simulable is part of MOP.
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

package simulacion;

import java.util.ArrayList;
import java.util.Hashtable;

import datatypesSalida.DatosEPPUnEscenario;
import optimizacion.ResOptim;
import optimizacion.ResOptimIncrementos;
import parque.Azar;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.PasoTiempo;




/**
 * Interfaz Simulable, representa un objeto que es simulado por la clase Simulador
 * @author ut602614
 *
 */
public interface Simulable {
	/**
	 * 
	 */
	public void recogerEstadoParticipantes();
	public void recogerEstadoPE();
//	public void recogerControl();
	
	public void setResOptim(ResOptim roptim);
	
	/**
	 * Se carga todo lo necesario para iniciar la simulación, incluso las variables de estado iniciales y sus valores
	 */
	public void inicializarSimulable();

	/**
	 * Se carga todo lo necesario para iniciar el paso siguiente, incluso las variables de estado del paso y sus valores
	 * Se deja el sistema en el instante inicial del paso siguiente antes del sorteo de los Procesos Estocasticos DE
	 */
	public void actualizarParaProximoPaso();
	
	/**
	 *  inicializa las innovaciones de todos los procesos estocósticos del simulable
	 *  para ejecutar un escenario
	 * @param i nómero de escenario
	 */
	public void inicializarAzarParaUnEscenario(int i); 
	
	/**
	 * Sortea los valores de las variables de estado  y VA de los procesos estocósticos DE 
	 * @param instante 
	 * @return 
	 */
	public void sortearProcEstDE(long instante);
	
	public void inicializarPEPaso();
	
	/**
	 * Este metodo hará que los participantes que tienen inicializaciones
	 * que no se hacen al iniciar el escenario sino en otros pasos, las hagan. Por ejemplo un ContratoInterrumpible
	 * cuya vida no se inicia en el primer paso de simulación sino después.
	 */
	public void inicializarParaPasoSimul();
	 
	
	/**
	 * Sortea los valores de las variables de estado  y VA de los procesos estocósticos NO DE 
	 * @param instante 
	 * @return 
	 */	
	public void sortearProcEstNODE(long instante);

	public void simularPaso(boolean paralelo);
	
	public void simularPasoCliente();
	
	public void simularPasoServidor();
	
	public void inicializarEscenario(int i);
	
	public void guardarResultadoPaso();
	
	public void finalizarSimulable();
	
	public void cargarPasoCorriente(int numpaso, PasoTiempo paso);
	
	public void setDirSalidas(String dirCompleto);
	
	public String dameDirSalidas();
	
	public ArrayList<ProcesoEstocastico> devuelvePEsSimulacion();
	
	public long[] devuelveInstantesMuestreo();
	
	public Azar devuelveAzar();
	
	/**
	 * Cargar en el resoptim las tablas levantadas de disco, previamente grabado por el OptimizadorPaso
	 * @param ruta es el directorio de las tablas
	 * @return 
	 */
	public void levantarTablasResOptimDeDisco(String ruta, ResOptim ro);
	public void finalizarEscenarioServidor();
	public void actualizarParaProximoPasoAproximada();
	void finalizarEscenario(boolean disco);
	public void finalizarSimulableDirectorio(String ruta);
	

	



	
	
}
