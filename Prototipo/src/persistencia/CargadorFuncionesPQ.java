/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorFuncionesPQ is part of MOP.
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
import java.util.Hashtable;

import datatypes.DatosHidraulicoCorrida;
import datatypes.Pair;
import datatypesProcEstocasticos.DatosPEBootstrapDiscreto;
import datatypesProcEstocasticos.DatosPEMarkov;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import procesosEstocasticos.MatPTrans;
import utilitarios.LeerDatosArchivo;
import utilitarios.Recta;

public class CargadorFuncionesPQ {
	
	
	/**
	 * Crea un datatype DatosPEBootstrapDiscreto a partir de datos que lee de una ruta contenida en 
	 * el argumento dpe.
	 * 
	 *  
	 * @author ut469262
	 *
	 */	
	public static DatosHidraulicoCorrida devuelveDatosFuncionPQ(DatosHidraulicoCorrida dhc){
		
			
		// Lee el archivo que tiene las rectas PQ y carga los valores en DatosHidraulicoCorrida
        ArrayList<ArrayList<String>> texto;
        String dirArch = dhc.getRutaPQ() + "/RectasPotenciaCaudal.txt";		
        texto = LeerDatosArchivo.getDatos(dirArch);	
              
        // SEGUIMOS POR ACó  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        int i=0;  //filas
        int j=0; //columnas
        while(i<texto.size()){
        	 // Encontrar el nombre de la central 
            if(dhc.getNombre().equalsIgnoreCase(texto.get(i).get(0))){
            	i++;
            	
            	// Mientras no aparezca un nombre en el cabezal
            	// Completo los parómetros de las funciones PQ de la central      	
            	while(!texto.get(i).get(0).equalsIgnoreCase("FIN")){  
            		j=0;
                	Pair cotas = new Pair(Double.parseDouble(texto.get(i).get(j)), Double.parseDouble(texto.get(i).get(j+1)));  // cargo las cotas, (si no tiene generador AAbajo la cotaAA vale 0)
                	j=j+2;
                	ArrayList rectas = new ArrayList<Recta>();
                	while(j<texto.get(i).size()){
                		rectas.add(new Recta(Double.parseDouble(texto.get(i).get(j)), Double.parseDouble(texto.get(i).get(j+1))));
                    	j=j+2;
                	}        	   	
                	dhc.getFuncionesPQ().put(cotas, rectas);
                	i++;
            	}     	         	       	
            }
            i++;
        }
    	return dhc;	        
	}	
	
	
	
	
	
	public static void main (String[] args){
		
		DatosHidraulicoCorrida dhc = new DatosHidraulicoCorrida();
		dhc.setRutaPQ("D:/Proyectos/modelopadmin/resources/funcionesPQ");
		dhc.setNombre("salto");
		dhc.setFuncionesPQ(new Hashtable<Pair<Double,Double>, ArrayList<Recta>>());
		CargadorFuncionesPQ.devuelveDatosFuncionPQ(dhc);
	}
	

}
