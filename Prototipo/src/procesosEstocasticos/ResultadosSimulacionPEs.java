/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ResultadosSimulacionPEs is part of MOP.
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
import java.util.GregorianCalendar;
import java.util.Hashtable;

import tiempo.LineaTiempo;
import tiempo.PasoTiempo;

/**
 * Contiene los escenarios de simulación de una variable aleatoria
 * construídos por producirRealizacion en la linea de tiempo.
 * Si el proceso al que pertenece la variable aleatoria es muestreado
 * se tiene un valor por cada intervalo de muestreo.
 * 
 * Todos los ResultadosSimulacionPEs de una simulación tienen asociado el mismo
 * nombrePaso de ProcesoEstocastico.
 *  
 * 
 * @author ut469262
 *
 */
public class ResultadosSimulacionPEs {

	private static int cantEscenarios;
	private static LineaTiempo lt;
	private static String nombrePaso; // El paso de PE que será la base para todas las VA.
	private static int durPasoPEs;  // la duración en segundos correspondiente a nombrePaso
	/**
	 * anio y paso se construyen a partir de la línea de tiempo.
	 * A cada paso de la línea de tiempo se le atribuye un valor de anio y paso,
	 * 
	 */	
	private static int[] anio;
	private static int[] paso;   // paso del año de los PE empezando en 1
	
	

	
	/**
	 * Para las variables aleatorias MUESTREADAS
	 * Clave nombre de la serie de la variable aleatoria generada.
	 * Valor 
	 * 		primer índice escenario
	 * 		segundo índice paso de la simulación
	 * 	   	tercer indice intervalo de muestreo, solo varia si la VA es muestreada.
	 */
	private static Hashtable<String, double[][][]> variablesAleat;	
	
	
	/**
	 * Dada la linea de tiempo lt, construye los atributos anio y paso (paso del año de los PE)
	 */
	public static void construyeAnioPaso(){
		durPasoPEs = utilitarios.Constantes.SEGSPORPASO.get(nombrePaso);
		lt.setSentidoTiempo(1);
		lt.reiniciar();
		PasoTiempo paso = lt.devuelvePasoActual();
		int np;
		while (paso != null) {	
			np = lt.getNumPaso();
			long instIniRef = paso.getInstanteInicial() + utilitarios.Constantes.EPSILONSALTOTIEMPO;
			GregorianCalendar gc = lt.dameTiempo(instIniRef);
			int anioCorr = lt.getAnioPaso(np);   // anioCorr es el año corriente
			anio[np] = anioCorr;
			long instIniAnio = lt.getInstInicioAnioHT().get(anioCorr);
			int pasoCorr = (int)((instIniRef - instIniAnio)/durPasoPEs + 1);
			lt.avanzarPaso();
			paso = lt.devuelvePasoActual();
		}	
	}
}
