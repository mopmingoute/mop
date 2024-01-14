/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Reprocesable is part of MOP.
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

package persistencia;

import datatypesSalida.DatosEPPResumen;
import datatypesSalida.DatosEPPUnEscenario;
import datatypesSalida.DatosParamSalida;
import parque.Corrida;

/**
 * Las clases que implementan esta interfase contendrán cálculos 
 * que se hacen después de completada la simulación, 
 * o contruirán reportes que no fueron pedidos en una corrida 
 * y se quiere hacer después.
 * @param directorioEscenarios directorio donde quedan los DatosEPPUnEscenario serializados
 * @param escRes el EscritorResumenSimulacionParalelo que produjo las salidas iniciales.
 * solo es distinto de null cuando los métodos se invocan en la propia corrida inicial.
 * 
 * Los restantes argumentos son null cuando los métodos se invocan en la propia corrida
 * y en cambio se emplean cuando se están pidiendo nuevos cálculos en una segunda 
 * corrida que tiene otros parámetros de salida.
 */

public interface Reprocesable {
	
	public String devuelveNombreReproceso();
	
	/**
	 * Lee en dirDatosPrepara la información necesaria para inicializar el reproceso
	 * y hace los eventuales cálculos preparatorios para reprocesar
	 * 
	 * @param dirSerializados directorio donde están los resultados de escenarios simulados
	 * y los restantes archivos serializados resultado de una corrida anterior
	 * @param dirOtrosDatos directorio donde se leen otros datos del reproceso
	 */
	public void preparaReproceso(String dirSerializados, String dirOtrosDatos);
	
	
	/**
	 * Lee el DatosEPPUnEscenario serializado del escenario iesc
	 * y lo procesa 
	 * @param dirSalidasCorrida
	 * 
	 */
	public void procesaUnEscenario(DatosEPPUnEscenario d1esc, int iesc);
	
	/**
	 * Hace los cálculos y los graba en el subdirectorio "Postproceso"
	 * del directorio de salidas de la simulación de la corrida.
	 * @dirSalidasSim es el directorio de salidas de la corrida que postprocesa 
	 * a una anterior.
	 * @param corrida es la corrida levantada para reprocesar a la anterior.
	 */
	public void construyeSalReproceso(String dirSalidasSim);

}
