/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEMarkov is part of MOP.
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
import procesosEstocasticos.MatPTrans;
import utilitarios.LeerDatosArchivo;
import datatypesProcEstocasticos.DatosPEMarkov;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import logica.CorridaHandler;
import pizarron.PizarronRedis;

public class CargadorPEMarkov {
	


	
	
	/**
	 * Crea un datatype DatosPEMarkov a partir de datos que lee de una ruta contenida en 
	 * el argumento dpe.
	 * 
	 *  
	 * @author ut469262
	 *
	 */	
	public static DatosPEMarkov devuelveDatosPEMarkov(DatosProcesoEstocastico dpe){
		
	
		final int COLINI = 2;
		DatosPEMarkov dpm = new DatosPEMarkov();
		String ruta = "./resources/" + dpe.getNombre();
		dpm.setDatGen(CargadorDatosGeneralesPE.devuelveDatosGeneralesPE(ruta, dpe.getNombre()));
				
		dpm.setNombre(dpe.getNombre());
		//String ruta = dpe.getRuta();
//		String ruta = "./resources/" + dpm.getNombre();
		dpm.setRuta(ruta);
		

		
		// Lee la definición de estaciones
        ArrayList<ArrayList<String>> texto;
        String dirDefEst = ruta + "/defEstac.txt";		
        texto = LeerDatosArchivo.getDatos(dirDefEst);	
        
    	int i=0;   
    	int cantMaxPasos = Integer.parseInt(texto.get(i).get(1));  
    	dpm.setCantMaxPasos(cantMaxPasos);
    	i++;
    	int cantEstac = Integer.parseInt(texto.get(i).get(1));   
    	dpm.setCantEstac(cantEstac);
    	i++;    	
    	int[] estDelPaso = new int[cantEstac];    	
    	int ifil = i;
    	for(i = 0; i<cantMaxPasos; i++){
    		int paso = Integer.parseInt(texto.get(ifil + i).get(1));
    		if(paso!=i+1){
    			System.out.println("Error en lectura de estaciones en el paso " + paso);
    			if (CorridaHandler.getInstance().isParalelo()){
    				//PizarronRedis pp = new PizarronRedis();
    				//pp.matarServidores();
    			}
    			System.exit(1);
    		}    		
    		estDelPaso[i] = Integer.parseInt(texto.get(ifil+i).get(1));    	
    	}    
    	dpm.setEstDelPaso(estDelPaso);
		
	    // Lee las matrices de transición 		
		

        String dirDatosMat = ruta + "/MatTrans.xlt";
        texto = LeerDatosArchivo.getDatos(dirDatosMat);
        // Lee identificador de la estimación de VE usada  (0)
        i=0;
        String idEstimacionVE = "";
        for(int j=1; j<texto.get(i).size(); j++){
        	idEstimacionVE = idEstimacionVE + texto.get(i).get(j);
        }
        dpm.setEstimacionVE(idEstimacionVE);
        i++;
        // Lee cantidad de VA   (1)
        int cantVA = Integer.parseInt(texto.get(i).get(1));
        dpm.setCantVA(cantVA);
        i++;  
        // Lee cantidad de VE   (2)
        int cantVE = Integer.parseInt(texto.get(i).get(1));
        dpm.setCantVE(cantVE);
        i++;
        // Lee cantidad de VE Optimización  (2)
        int cantVEOptim = Integer.parseInt(texto.get(i).get(1));
        dpm.setCantVEOptim(cantVEOptim);
        i++;
        // Lee la cantidad de estaciones (3)
        cantEstac = Integer.parseInt(texto.get(i).get(1));
        // No se carga, porque ya estó cargada en dpm
        i++;
        
        // Lee cantidad de crónicas usadas en la estimación   (4)
        int cantCron = Integer.parseInt(texto.get(i).get(1));
        dpm.setCantCron(cantCron);
        i++;                        
        // Lee nombre del paso     (5)   
        dpm.setNombrePaso(texto.get(i).get(1)); 
        i++;
        // Lee la cantidad de estados compuestos  (6)
        dpm.setCantEstadosComp(Integer.parseInt(texto.get(i).get(1)));
        i++;        
        // Saltea cantidad de datos (7)
        i++;
     
             
        // Lee nombres de VA y VE y VEOptim   (8)   
        ArrayList<String> nombresVA = new ArrayList<String>(); 
        for (int j=0; j<cantVA; j++){
        	nombresVA.add(texto.get(i).get(j+1));
        }
        dpm.setNombresVA(nombresVA);
        i++; 
        
        ArrayList<String> nombresVE = new ArrayList<String>();
        for (int j=0; j<cantVE; j++){
        	nombresVE.add(texto.get(i).get(j+1));
        }
        dpm.setNombresVE(nombresVE);
        i++; 
        
        ArrayList<String> nombresVEOptim = new ArrayList<String>();
        for (int j=0; j<cantVEOptim; j++){
        	nombresVEOptim.add(texto.get(i).get(j+1));
        }
        dpm.setNombresVEOptim(nombresVEOptim);
        i++; 
        
        
        // Lee cantidad de clases de cada VE
        int[] cantClases = new int[cantVEOptim];
        for (int j=0; j<cantVEOptim; j++){
        	cantClases[j]= Integer.parseInt(texto.get(i).get(j+1));
        } 
        dpm.setCantCla(cantClases);
        i++;

        
        
        // Lee los objetos MatPTrans
        
        Hashtable<Integer,MatPTrans> matrices = new Hashtable<Integer,MatPTrans>();  
        
        while(i < texto.size()  && texto.get(i).get(0).equalsIgnoreCase("PROBABILIDADES-DE-TRANSICION")){ 
        	int est = Integer.parseInt(texto.get(i+1).get(1));
        	
        	MatPTrans mpt = new MatPTrans(texto, i, COLINI, dpm.getCantEstadosComp());
        	matrices.put(est, mpt);
        	i = i + dpm.getCantEstadosComp() + 2;
        }
        dpm.setMatrices(matrices);
        
        // Lee las observaciones para cada estado compuesto
        
        String dirDatosObs = ruta + "/ObservPorEstacEC.xlt";
        texto = LeerDatosArchivo.getDatos(dirDatosObs);  
        i=0;
        int cantLeida = Integer.parseInt(texto.get(i).get(1));
        if(cantLeida!=dpm.getCantEstadosComp()){
        	System.out.println("La cantidad de estados compuestos estó equivocada");
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }        
        i++;
        
        
        /**
         * observaciones:
         * 
         * Estructura que almacena los valores de las series representativos para cada
         * estación (paso dentro del aóo) y estado compuesto.
         * 
         * Primer óndice del array: estación (paso dentro del aóo)
         * Segundo óndice del array: estado compuesto 
         * 
         * El ArrayList tiene Double[] con los datos de cada serie.
         */
        Object[][] observaciones = new Object[dpm.getCantEstac()][dpm.getCantEstadosComp()];
        
        /** 
         * defEstadosComp:
         * 
         * Definición de estados compuestos
         * Estado compuesto: resulta de las ordenación
         * lexicogrófica de los estados de cada variable de estado.
         * Ejemplo para dos variables de estado:
         * (1,1),(1,2), ....;(2,1),(2,2),(2,3),... 
         */
        ArrayList<int[]> defEstadosComp = new ArrayList<int[]>();
        
        for(int iest=0; iest<dpm.getCantEstac(); iest++){
        	
        	int estLeida = Integer.parseInt(texto.get(i).get(1));
        	if(! (iest==(estLeida-1)) ){
        		System.out.println("Error en la estación " + estLeida + " al leer observaciones");
        		if (CorridaHandler.getInstance().isParalelo()){
    				//PizarronRedis pp = new PizarronRedis();
    				//pp.matarServidores();
    			}
        		System.exit(1);
        	}
        	
        	for(int iec=0; iec<dpm.getCantEstadosComp(); iec++){
        		
        		int eCL = Integer.parseInt(texto.get(i).get(3));
            	if(! (iec==eCL) ){
            		System.out.println("Error en la estación " + estLeida + " estado compuesto " + eCL);
            		if (CorridaHandler.getInstance().isParalelo()){
        				//PizarronRedis pp = new PizarronRedis();
        			//	pp.matarServidores();
        			}
            		System.exit(1);
            	}
            	i++;
            	
        		int[] aux = new int[cantVEOptim];        	
        		
            	for(int ive = 0; ive<cantVEOptim; ive++){
            		String nomVELeido= texto.get(i).get(0);
            		if(!nomVELeido.equalsIgnoreCase(dpm.getNombresVEOptim().get(ive))){
                		System.out.println("Error la variable de estado " + ive + " en la estación " + iest);
                		if (CorridaHandler.getInstance().isParalelo()){
            				//PizarronRedis pp = new PizarronRedis();
            				//pp.matarServidores();
            			}
                		System.exit(1);            			
            		}
            		aux[ive] = Integer.parseInt(texto.get(i).get(1));
            		i++;
            	}
            	defEstadosComp.add(aux);        		
        	
        		
	        	// Lee las observaciones para las VA
	        	for(int iva = 0; iva<dpm.getCantVA(); iva++){
	        		if(! texto.get(i).get(iva).equalsIgnoreCase(dpm.getNombresVA().get(iva)) ){
	            		System.out.println("Error la variable "  + dpm.getNombresVA().get(iva) +
	            				" en la estación" + iest);
	            		if (CorridaHandler.getInstance().isParalelo()){
	        				//PizarronRedis pp = new PizarronRedis();
	        			//	pp.matarServidores();
	        			}
	            		System.exit(1);                   			
	        		}
	        	}        	
	        	i++;
	
				ArrayList<Double[]> aux2 = new ArrayList<Double[]>();        	
	        	while(i<texto.size() && ! texto.get(i).get(0).equalsIgnoreCase("estacion")){
	    			Double[] aux3 = new Double[dpm.getCantVA()]; 
	        		for(int iva = 0; iva<dpm.getCantVA(); iva++){
	        			aux3[iva] = Double.parseDouble(texto.get(i).get(iva));        		       				        		
	        		}
	        		aux2.add(aux3);  
	        		i++;
	        	}
        		observaciones[iest][iec] = aux2;	        	
        	}
	        dpm.setObservaciones(observaciones);
	        dpm.setDefEstadosComp(defEstadosComp);

        }
    	return dpm;	        
	}
	
}
