/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Metodos is part of MOP.
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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Metodos {
//	public static double sortearAleatorio(double[] datos) {
//		return datos[(int) Math.floor(Math.random()*datos.length)];		
//	}
//	
	
	public static double sortearAleatorio(double[] datos, double uniforme) {
		return datos[(int) Math.floor(uniforme*datos.length)];		
	}

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
}
