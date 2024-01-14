/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * HazEsc is part of MOP.
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

public class HazEsc {
	
	/**
	 * Almacena múltiples escenarios (un haz de escenarios) de una misma variable aleatoria
	 */
	private String nombre; 
	private String nombrePaso;
	private int canEsc;  // cantidad de escenarios
	private int cantMaxPasos;
	private int cantDatos;
	double[][] datos;  // primer índice tiempo, segundo índice escenario.
	int[] anio;
	int[] paso; // número de paso dentro del año empezando en 1, ej. 1 a 52 si nombrePaso es semana.
	// datos, anio y paso tiene la misma longitud	
	
	public HazEsc(String nombrePaso){
		this.nombrePaso = nombrePaso;
		this.cantMaxPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombrePaso);
	}

	public HazEsc(String nombre, String nombrePaso, double[][] datos, int[] anio, int[] paso) {
		super();
		this.nombre = nombre;
		this.nombrePaso = nombrePaso;
		this.datos = datos;
		this.anio = anio;
		this.paso = paso;
		this.cantMaxPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombrePaso);
		this.cantDatos = datos.length;
	}
	
	
	
	
	
	
}
