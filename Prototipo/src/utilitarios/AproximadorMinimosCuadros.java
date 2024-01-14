/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AproximadorMinimosCuadros is part of MOP.
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
import java.util.ArrayList;

public class AproximadorMinimosCuadros {
	public ArrayList<Double> x;
	public ArrayList<Double> y;
	public ArrayList<Double> ptos_quiebre;
	
	
	
	public AproximadorMinimosCuadros(ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> ptos_quiebre) {
		super();
		this.x = x;
		this.y = y;
		ptos_quiebre.add(x.get(x.size()-1));
		this.ptos_quiebre = ptos_quiebre;
	}
	
	public ArrayList<Recta>  aTrozosMinimosCuadrados() {
		int j;
		ArrayList<Recta> rectas = new ArrayList<Recta>();
		j=0;
		for (int i = 0; i < ptos_quiebre.size(); i++) {
			ArrayList<Double> xAux = new ArrayList<Double>();
			ArrayList<Double> yAux = new ArrayList<Double>();
			
			do  { 
				xAux.add(x.get(j));
				yAux.add(y.get(j));
				j++;
				if (j==x.size()) break;
			} while(x.get(j)<=ptos_quiebre.get(i) );
			rectas.add(minimosCuadrados(xAux, yAux));
		}
		
		
		return rectas;
		
	}
	
	
	
	
	private Recta minimosCuadrados(ArrayList<Double> xs, ArrayList<Double> ys) {
		Recta resultado = new Recta();
		
		int n = xs.size();
		if (n<2) {
			throw new IllegalArgumentException("Poca cantidad de datos");
		}
		
		double sumaX = 0;
		double sumaY = 0;
		double sumaXcuadrado = 0;
		double sumaXY = 0;
		
		for (int i = 0; i < n; i++) {		
			sumaX += xs.get(i);
			sumaY += ys.get(i);
			sumaXcuadrado += xs.get(i)*xs.get(i);
			sumaXY += xs.get(i)*ys.get(i);			
			
		}
		
		double sxx = sumaXcuadrado-(sumaX*sumaX)/n;
		double sxy = sumaXY-(sumaX*sumaY)/n;
		
		double xb = sumaX /n;
		double yb = sumaY /n;
		
		resultado.a = sxy / sxx;
		resultado.b = yb-resultado.a*xb;
		
		return resultado;
	}
}
