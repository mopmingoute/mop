/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorEscParaSalidas is part of MOP.
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

package cp_persistencia;

import java.util.ArrayList;

import cp_datatypesEntradas.DatosGeneralesCP;
import cp_despacho.GrafoEscenarios;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LeerDatosArchivo;

public class CargadorEscParaSalidas {
	
	
	public static ArrayList<int[]> devuelveEscenariosSalidaCP(String ruta, GrafoEscenarios ge) {
		ArrayList<int[]> escenarios = new ArrayList<int[]>();
		String dirArchivo = ruta + "/EscParaSalidas.txt";		
        ArrayList<ArrayList<String>> texto; 
        if(DirectoriosYArchivos.existeArchivo(dirArchivo)) {
	        texto = LeerDatosArchivo.getDatos(dirArchivo);	
	            	
	    	int i=0;
			int largoEsc = texto.get(i).size();
			do {
				if(texto.get(i).size()!=largoEsc) {
					System.out.println("Error en cantidad de etapas de escanario fila " + i);
					System.exit(1);
				}
				int[] aux = new int[largoEsc];
				for(int j=0; j<texto.get(i).size(); j++) {
					aux[j] = Integer.parseInt(texto.get(i).get(j));
				}
				escenarios.add(aux);
				i++;
			}while(i<texto.size());
			return escenarios;
        }else {
        	int canEtapas = ge.getEnumeradores().size();
        	utilitarios.EnumeradorLexicografico eL = ge.getEnumeradores().get(canEtapas-1);
        	eL.inicializaEnum();
        	int[] vec = eL.devuelveVector();
        	while(vec!=null) {
        		escenarios.add(vec);
        		vec = eL.devuelveVector();
        	}
        	return escenarios;
        	
        }
	}
	
	

}
