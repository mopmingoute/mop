/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CalculadorPascua is part of MOP.
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

package utilitarios;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalculadorPascua {
	
	public static Date getEasterDate(int year) {
		int a = year % 19;
		int b = year / 100;
		int c = year % 100;
		int d = b / 4;
		int e = b % 4;
		int f = (b + 8) / 25;
		int g = (b - f + 1) / 3;
		int h = (19 * a + b - d - g + 15) % 30;
		int i = c / 4;
		int k = c % 4;
		int l = (32 + 2 * e + 2 * i - h - k) % 7;
		int m = (a + 11 * h + 22 * l) / 451;
		int n = (h + l - 7 * m + 114) / 31;
		int p = (h + l - 7 * m + 114) % 31;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.clear();
		calendar.set(year, n - 1, p + 1);
		return calendar.getTime();
	}
	
	
	
    public static void main(String[] args) {   
    	Date fecha = CalculadorPascua.getEasterDate(1998);
    	System.out.println(fecha);
    	
    	
    	
    	
    	
    }

}
