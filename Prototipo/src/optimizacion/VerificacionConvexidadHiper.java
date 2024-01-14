/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VerificacionConvexidadHiper is part of MOP.
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

package optimizacion;

import java.util.ArrayList;

import futuro.Hiperplano;

public class VerificacionConvexidadHiper {
	/**
	 * Dada una lista de hiperplanos, almacena el resultado de la verificación de 
	 * convexidad esos hiperplanos que hace el ResOptimHiperplanos
	 * Un error es un hiperplano que toma mayor valor que otro en el punto de soporte del otro
	 */
	
	private ArrayList<Integer> cuentaErroresPorH;  // para cada hiperplano la cantidad de errores
	
	/**
	 * Para cada hiperplano hi da el valor hi(xj)-hj(xj) donde xj es el punto de soporte del 
	 * hiperplano j. Los errores ocurren cuando hi(xj)-hj(xj)>0.
	 */
	private ArrayList<ArrayList<Double>> hiMenosHj;
	
	
	
	
	public String imprimir(){
		StringBuilder sb = new StringBuilder("Cuenta de errores por hiperplano\n");
		for(int i=0; i<hiMenosHj.size(); i++){
			sb.append(cuentaErroresPorH.get(i)+"\t");
		}
		sb.append("\n");
		sb.append("Diferencias hi(xj)-hj(xj)\n");
		for(int i=0; i<hiMenosHj.size(); i++){
			for(int j=0; j<hiMenosHj.get(i).size(); j++){
				sb.append(hiMenosHj.get(i).get(j)+"\t");
			}
			sb.append("\n");
		}		
		return sb.toString();
	}

	public ArrayList<Integer> getCuentaErroresPorH() {
		return cuentaErroresPorH;
	}

	public void setCuentaErroresPorH(ArrayList<Integer> cuentaErroresPorH) {
		this.cuentaErroresPorH = cuentaErroresPorH;
	}

	public ArrayList<ArrayList<Double>> getHiMenosHj() {
		return hiMenosHj;
	}

	public void setHiMenosHj(ArrayList<ArrayList<Double>> hiMenosHj) {
		this.hiMenosHj = hiMenosHj;
	}






	
	

}
