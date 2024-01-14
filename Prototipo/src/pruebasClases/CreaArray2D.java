/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CreaArray2D is part of MOP.
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

package pruebasClases;

public class CreaArray2D {
	public static void main(String[] args) {
		
		String[][] vector = null;
		
		vector[0] = new String[3];
		
		vector[1]= new String[3];
		
		for(int i=0; i<2; i++ ) {
			for(int j=0; j<vector[i].length; j++) {
				vector[i][j]= "hola" + i + j;
			}
		}
		
		
			
	    System.out.print("terminó");
		
		
		
		
	}

}
