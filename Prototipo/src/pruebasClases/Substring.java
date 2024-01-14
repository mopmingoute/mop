/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Substring is part of MOP.
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

public class Substring {
	
	public static void main(String[] args){
		Double d1 = 4.334876;
		String s1 = d1.toString();
		String s2;
		int largo = s1.length();
		if(largo<8){
			s2 = s1;
		}else{
			s2 = s1.substring(0,8);
		}
		System.out.println(s2);
	}

}
