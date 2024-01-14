/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VAR is part of MOP.
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


/**
 * Proceso VAR
 * @author ut469262
 *
 */
public class VAR {
	
	private int nlags;    // cantidad de rezagos	
	private double[][] matrices;   // matrices de lags 1, 2, ....

	
	private Object[][] prueba;
	
	public void pruebita(){
		
		prueba = new Object[5][2];
		
	}
	
}
