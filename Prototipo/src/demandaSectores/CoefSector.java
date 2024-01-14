/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CoefSector is part of MOP.
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

package demandaSectores;

/**
 * Almacena los coeficientes y parámetros de las curvas de carga de un sector
 * 
 * @author ut469262
 *
 */
public class CoefSector {

	private DatosGenSectores datGen;
	private double[] coefSem; // coeficientes multiplicativos semanales, suman cantSem
	private double[][] coefDia; // primer índice estación, segundo tipo de día;
								// para cada estación el tipo de dia 1 tiene coeficiente 1
	private double[][][] coefHora; // primer índice estación, segundo tipo de día, tercero hora del día
									// en cada estación y tipo de día la suma de coeficientes es 24

}
