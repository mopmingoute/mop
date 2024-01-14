/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorAgregadorLineal is part of MOP.
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

import datatypesProcEstocasticos.DatosAgregadorLineal;
import datatypesProcEstocasticos.DatosPEBootstrapDiscreto;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.LeerDatosArchivo;

public class CargadorAgregadorLineal {
	
	
	
	
	/**
	 * Crea un datatype DatosAgregadorLineal a partir de datos que lee de
	 * la ruta, que tiene el directorio del PE de simulacion,
	 * En ese directorio levanta un archivo de nombre "agregador.txt" 
	 * 
	 * @author ut469262
	 *
	 */	
	public static DatosAgregadorLineal devuelveDatosAgregadorLineal(String dirAgregador, String nombreProcSim, String nombreEstimacion){

		DatosAgregadorLineal dat = new DatosAgregadorLineal();	
		
        ArrayList<ArrayList<String>> texto;        
        texto = LeerDatosArchivo.getDatos(dirAgregador);	
        
    	int i=0;   	
    	
    	if(!texto.get(i).get(0).equalsIgnoreCase("TIPO_AGREGADOR")  || !texto.get(i).get(1).equalsIgnoreCase("LINEAL")){
    		System.out.println("Error en etiqueta de TIPO_AGREGADOR archivo " + dirAgregador);
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(0);
    	}
    	
    	i++;
    	
    	if(!texto.get(i).get(0).equalsIgnoreCase("NOMBRE_ESTIMACION")  || !texto.get(i).get(1).equalsIgnoreCase(nombreEstimacion)){
    		System.out.println("Error en etiqueta de NOMBRE_ESTIMACION archivo " + dirAgregador);
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(0);
    	}   	
    	i++;
    	    	  	
//    	if(!texto.get(i).get(0).equalsIgnoreCase("PROCESO_SIMULACION")  || !texto.get(i).get(1).equalsIgnoreCase(nombreProcSim)){
//    		System.out.println("Error en etiqueta de PROCESO_SIMULACION archivo " + dirAgregador);
//    		System.exit(0);
//    	}
    	dat.setNombrePESimul(nombreProcSim);
    	 
    	i++;
    	
//    	if(!texto.get(i).get(0).equalsIgnoreCase("PROCESO_OPTIMIZACION")){
//    		System.out.println("Error en etiqueta de PROCESO_OPTIMIZACION archivo " + dirAgregador);
//    		System.exit(0);
//    	} 
    	String nombreProcOptim = texto.get(i).get(1);
    	dat.setNombrePEOptim(nombreProcOptim);  // OJO VERIFICAR CON EL PROCESO ASOCIADO

    	i++;
    	if(!texto.get(i).get(0).equalsIgnoreCase("VARS_ESTADO_SIMULACION")){
    		System.out.println("Error en etiqueta de VARS_ESTADO_SIMULACION archivo " + dirAgregador);
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(0);
    	}   
    	ArrayList<String> aux = new ArrayList<String>();
    	for(int j=1; j<texto.get(i).size(); j++){
    		aux.add(texto.get(i).get(j));    		
    	}
    	dat.setNombresVESimul(aux);
    	dat.setCantVESimul(dat.getNombresVESimul().size());

    	i++;
    	if(!texto.get(i).get(0).equalsIgnoreCase("VARS_EXOGENAS")){
    		System.out.println("Error en etiqueta de VARS_EXOGENAS archivo " + dirAgregador);
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(0);
    	} 
    	aux = new ArrayList<String>();
    	for(int j=1; j<texto.get(i).size(); j++){
    		aux.add(texto.get(i).get(j));    		
    	}
    	dat.setNombresVExo(aux);
    	dat.setCantVExo(dat.getNombresVExo().size());    	
    	
    	i++;
    	if(!texto.get(i).get(0).equalsIgnoreCase("VARS_ESTADO_OPTIMIZACION")){
    		System.out.println("Error en etiqueta de VARS_ESTADO_OPTIMIZACION archivo " + dirAgregador);
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(0);
    	}    
    	aux = new ArrayList<String>();
    	for(int j=1; j<texto.get(i).size(); j++){
    		aux.add(texto.get(i).get(j));    		
    	} 
    	dat.setNombresVEOptim(aux);
    	dat.setCantVEOptim(dat.getNombresVEOptim().size());    	
 
    	i++;    	
    	if(!texto.get(i).get(0).equalsIgnoreCase("MATRIZ_POR_FILAS")){
    		System.out.println("Error en etiqueta de MATRIZ_POR_FILAS archivo " + dirAgregador);
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
    		System.exit(0);
    	}        	   	
    	
    	i++;  
    	double[][] auxM = new double[dat.getCantVEOptim()][dat.getCantVESimul()];
    	for(int ifil=0; ifil<dat.getCantVEOptim(); ifil++){
    		for(int icol=0; icol<dat.getCantVESimul(); icol++){
    			auxM[ifil][icol]=Double.parseDouble(texto.get(i).get(icol));
    		}
    		i++;
    	}
		dat.setMatrizAgregacion(auxM);
		
		return dat;
	}


}
