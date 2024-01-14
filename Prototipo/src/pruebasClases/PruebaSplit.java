/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PruebaSplit is part of MOP.
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

import java.util.ArrayList;

public class PruebaSplit {
	
	
	
	public static void main(String[] args) {
		
		int n = 100000;
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<=n; i++) {
			sb.append("palabra" + i);
			sb.append(" ");
		}
		String largo = sb.toString();
		String[] texto = largo.split("[\\s]+");
		
		System.out.println();
		
	}

}
