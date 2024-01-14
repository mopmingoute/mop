/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorHiperplanos is part of MOP.
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

package persistencia;

import java.util.ArrayList;

import datatypesResOptim.DatosHiperplano;
import estado.VariableEstado;
import futuro.ClaveDiscreta;
import futuro.Hiperplano;

public class EscritorHiperplanos {
	
	/**
	 * Devuelve los tótulos para imprimir una sucesión de hiperplanos
	 * @param cantVC cantidad de variables de los hiperplanos
	 * @return
	 */
	public String imprimeTitulosHiperplanos(int cantVC, ArrayList<VariableEstado> veTotal, ArrayList<VariableEstado> veCont){
		
		StringBuilder sb = new StringBuilder("Hiperplano nómero \t" + "Generacion \t" );
		
		sb.append("ClaveVEDiscretas \t");
		
		sb.append("Punto \t");
		for(int i=0; i<cantVC; i++){
			sb.append(veCont.get(i).getNombre());
			sb.append("\t");
		}
		sb.append("Coeficientes \t");
		for(int i=0; i<cantVC; i++){
			sb.append(veCont.get(i).getNombre());
			sb.append("\t");
		}
		sb.append("Tórm.indep \t");
		sb.append("Valor de Bellman \t");
		sb.append("Var.dual \n");
		return sb.toString();				
		
	}
	
	/**
	 * 
	 * @param dh 
	 * @param claveVEDis clave discreta del hiper
	 * @param imprimeClaveDis
	 * @return
	 */
	public String imprimeHiperplano(DatosHiperplano dh, ClaveDiscreta claveVEDis){
		
		double[] coefs = dh.getCoefs();
		int numeroId = dh.getNumeroId();
		int generacion = dh.getGeneracion();
		double[] punto = dh.getPunto();
		double tind = dh.getTind();
		double vBellman = dh.getvBellman();
		double vDual = dh.getVdual();
		
		
		int cantVC = coefs.length;
		StringBuilder sb = new StringBuilder(numeroId + "\t" +  generacion);
		
		// Clave discreta
		if(claveVEDis!=null){
			sb.append("\t");
			for(int i=0; i<claveVEDis.getEnterosIndices().length; i++){
				sb.append(claveVEDis.getEnterosIndices()[i]);
				sb.append(" ");
			}
		}
		
		// Punto en las vars. continuas
		sb.append("\t\t");
		for(int i=0; i<cantVC; i++){
			sb.append(punto[i]);
			sb.append("\t");
		}
		
		sb.append("\t");
		// Coeficientes
		for(int i=0; i<cantVC; i++){
			sb.append(coefs[i]);
			sb.append("\t");
		}
		
		// Termino independiente
		sb.append(tind);
		sb.append("\t");
		
		// Valor de Bellman
		sb.append(vBellman);
		sb.append("\t");
		
		// Variable dual
		sb.append(vDual);
	
		sb.append("\n");
		return sb.toString();			
		
	}	
	
	
	

}