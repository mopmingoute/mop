/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosParamSalida is part of MOP.
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

package datatypesSalida;

import java.io.Serializable;

public class DatosParamSalida implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @param param es un array de enteros que indica las salidas que se desean
	 *
	 * 
	 * 
	 *              param es int[]; 0 indica que no se produce la salida, 1 indica
	 *              que só.
	 * 
	 *              nombre archivo param[0] ener_resumen energóa anual promedio en
	 *              los escenarios; filas recurso; columnas aóo param[1] ener_cron
	 *              energóa por aóo y escenario para todos los recursos: filas
	 *              aóo,escenario; columnas recurso param[2] pot_poste para recursos
	 *              en particular, un archivo por poste, filas paso, columnas poste
	 *              param[3] lista de enteros int[] con los indicadores de los
	 *              recursos para los que se va a sacar el archivo de pot
	 * 
	 *              param[4] costo_resumen costo anual promedio en los escenarios;
	 *              filas recurso; columnas aóo param[5] costo_cron costo por aóo y
	 *              escenario para todos los recursos: filas (aóo,escenario);
	 *              columnas recurso param[6] costo_poste para recursos en
	 *              particular, un archivo por poste, filas paso, columnas poste
	 *              param[7] lista de enteros int[] con los indicadores de los
	 *              recursos para los que se va a sacar el archivo de costo_poste
	 *
	 *              param[8] cosmar_resumen filas paso; columnas poste; (los
	 *              promedios segón cantidad de horas = curva plana) param[9]
	 *              cosmar_cron para barras en particular, un archivo por poste,
	 *              filas paso, columnas crónicas param[10] lista de enterios int[]
	 *              con los indices de las barras para los que se va a sacar los
	 *              costos marginales detallados
	 * 
	 *              param[11] Si es =1 genera un directorio cantMod, con un archivo
	 *              de disponibilidades para cada recurso En esos archivos las filas
	 *              son pasos y las columnas son escenarios (crónicas)
	 * 
	 *              param[12] lista de enteros, uno por cada recurso, que indica con
	 *              1 si deben sacarse las salidas detalladas del recurso.
	 */
	private int[][] param;

	public DatosParamSalida() {
		super();
	}

	public DatosParamSalida(int[][] param) {
		super();
		this.param = param;
	}

	public int[][] getParam() {
		return param;
	}

	public void setParam(int[][] param) {
		this.param = param;
	}

}
