/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEEscenarios is part of MOP.
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

import utilitarios.LeerDatosArchivo;
import datatypesProcEstocasticos.DatosPEEscenarios;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import logica.CorridaHandler;
import pizarron.PizarronRedis;

public class CargadorPEEscenarios {
	
	/**
	 * Crea un datatype ProcesoEscenarios a partir de datos que lee de una ruta contenida en 
	 * el argumento dpe.
	 *  
	 * LOS NOMBRES DE LAS VA Y VE SE PASAN A MAYUSCULA 
	 * @author ut469262
	 *
	 */	
	public static DatosPEEscenarios devuelveDatosPEEscenarios(DatosProcesoEstocastico dpe){
		DatosPEEscenarios dpesc = new DatosPEEscenarios();
		String ruta = "./resources/" + dpe.getNombre();
		
		dpesc.setDatGen(CargadorDatosGeneralesPE.devuelveDatosGeneralesPE(ruta, dpe.getNombre()));
		dpesc.setNombre(dpe.getNombre());
		//String ruta = dpe.getRuta();
		
		//String ruta = "./resources/" + dpesc.getNombre();
		dpesc.setRuta(ruta);
		
	
		// Lee el archivo resumen del PE por escenarios
		
	
        ArrayList<ArrayList<String>> texto;
        String dirResumen = ruta + "/resumenPE.txt";
        texto = LeerDatosArchivo.getDatos(dirResumen);
        int i=0;
        // Lee identificador de la estimación de VE usada  
        String idEstimacionVE = "";
        for(int j=1; j<texto.get(i).size(); j++){
        	idEstimacionVE = idEstimacionVE + texto.get(i).get(j);
        }
        dpesc.setEstimacionVEUsada(texto.get(i).get(1));
        i++;
        // Lee cantidad de VA 
        if(! texto.get(i).get(0).equalsIgnoreCase("CANTIDAD_VA")){
        	System.out.println("ERROR EN CANTIDAD VA AL LEER EL PROCESO EN " + dirResumen);
        	System.exit(1);
        }
        int cantVA = Integer.parseInt(texto.get(i).get(1));
        dpesc.setCantVA(cantVA);
        i++;        
        // Lee cantidad de VE
        if(! texto.get(i).get(0).equalsIgnoreCase("CANTIDAD_VE")){
        	System.out.println("ERROR EN CANTIDAD VE AL LEER EL PROCESO EN " + dirResumen);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }        
        int cantVE = Integer.parseInt(texto.get(i).get(1));
        dpesc.setCantVE(cantVE);
        i++;             
        
        // Lee cantidad de escenarios
        if(! texto.get(i).get(0).equalsIgnoreCase("CANTIDAD_ESCENARIOS")){
        	System.out.println("ERROR EN CANTIDAD DE ESCENARIOS AL LEER EL PROCESO EN " + dirResumen);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }        
        int cantEsc = Integer.parseInt(texto.get(i).get(1));
        dpesc.setCantEsc(cantEsc);     
        i++;    
        
        // Lee nombre de la duración del paso
        if(! texto.get(i).get(0).equalsIgnoreCase("NOMBRE_PASO")){
        	System.out.println("ERROR EN NOMBRE DE DURACIóN DEL PASO " + dirResumen);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }        
        String nombrePaso = texto.get(i).get(1);
        dpesc.setNombrePaso(nombrePaso);     
        i++;           
        
        // Lee el aóo inicial
        if(! texto.get(i).get(0).equalsIgnoreCase("ANIO_INI")){
        	System.out.println("ERROR EN ANIO_INI AL LEER EL PROCESO EN " + dirResumen);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }        
        int anioIni = Integer.parseInt(texto.get(i).get(1));
        dpesc.setAnioInicialPE(anioIni);     
        i++;            

        
        // Lee el paso inicial
        if(! texto.get(i).get(0).equalsIgnoreCase("PASO_INI")){
        	System.out.println("ERROR EN PASO_INI AL LEER EL PROCESO EN " + dirResumen);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }        
        int pasoIni = Integer.parseInt(texto.get(i).get(1));
        dpesc.setPasoInicialPE(pasoIni);     
        i++;   
        
        // Lee el año final
        if(! texto.get(i).get(0).equalsIgnoreCase("ANIO_FIN")){
        	System.out.println("ERROR EN ANIO_FIN AL LEER EL PROCESO EN " + dirResumen);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }        
        int anioFin = Integer.parseInt(texto.get(i).get(1));
        dpesc.setAnioFinalPE(anioFin);   
        int cantAnios = anioFin - anioIni + 1;
        i++;           
        
        
        // Lee el paso final
        if(! texto.get(i).get(0).equalsIgnoreCase("PASO_FIN")){
        	System.out.println("ERROR EN PASO_FIN AL LEER EL PROCESO EN " + dirResumen);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
        	System.exit(1);
        }        
        int pasoFin = Integer.parseInt(texto.get(i).get(1));
        dpesc.setPasoFinalPE(pasoFin);     
        i++;            
        
        
        
        // Lee cantidad móxima de pasos por aóo
        if(! texto.get(i).get(0).equalsIgnoreCase("CANT_MAXIMA_PASOS")){
        	System.out.println("ERROR EN CANTIDAD MAXIMA DE PASOS AL LEER EL PROCESO EN " + dirResumen);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }        
        int cantMaxPasos = Integer.parseInt(texto.get(i).get(1));
        dpesc.setCantMaxPasos(cantMaxPasos);  
        i++;             
        
        
        //
        // Lee el archivo de datos de las VARIABLES ALEATORIAS
        //
        String dirDatos = ruta + "/DatosVA.prn";
        texto = LeerDatosArchivo.getDatos(dirDatos);    
        i=0;        
        // Lee los nombres de las VA
        if(! texto.get(i).get(0).equalsIgnoreCase("NOMBRES_VA") || texto.get(i).size()!=(cantVA+1) ){
        	System.out.println("ERROR EN NOMBRES DE VA AL LEER EL PROCESO EN " + dirDatos);
        	if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
        	System.exit(1);
        }  
        ArrayList<String> nombresVA = new ArrayList<String>();        
        for(int j=1; j<=cantVA; j++){
        	nombresVA.add(texto.get(i).get(j).toUpperCase());
        }
        dpesc.setNombresVA(nombresVA);
        i++;
        double[][][][] datos = new double[cantEsc][cantAnios][cantMaxPasos][cantVA];
        int etiquetaCron[][] = new int[cantEsc][cantAnios];
        // lee los datos de las VA
        
        int ifil;
        int icol;
        int primerPaso;
        Hashtable<Integer,Integer> ordinalPrimerPasoAnio = new Hashtable<Integer,Integer>(); 
        for(int iesc=0; iesc<dpesc.getCantEsc();iesc++){       	
        	/**
        	 *  Entre otras cosas hay que cargar el hashtable ordinalPrimerPasoAnio
             *  Para cada aóo en los que existen pasos del escenario, ordinal ("columna") empezando en 1 de dicho primer
             *  paso en el escenario. La clave es el año y el valor es el ordinal.
             *  Ejemplo: un proceso semanal cuyos escenarios empiezan en la semana 26 del aóo 2016 tiene en ordinalPrimerPasoAóo
             *  las entradas: (2016, 1), (2017,27) , (2018, 27+52=79), .....
        	 */	
        	int ordinal = 1; 
        	for(int ian=0; ian<anioFin-anioIni+1; ian++){  
        		etiquetaCron[iesc][ian] = Integer.parseInt(texto.get(1 + ian*(cantEsc*cantVA) + iesc*cantVA).get(1));
        		primerPaso = 0;
        		if(ian==0) primerPaso = pasoIni-1;
            	if(iesc==0) ordinalPrimerPasoAnio.put(anioIni+ian, ordinal);
        		int cantPasosCorr = texto.get(1 + ian*(cantEsc*cantVA) + iesc*cantVA).size()-3;
        		for(int ip=primerPaso; ip<cantPasosCorr; ip++){	        	
	        		icol = ip + 3;
        			for(int iva=0; iva<dpesc.getCantVA(); iva++){ 
		        		ifil = 1 + ian*(cantEsc*cantVA) + iesc*cantVA + iva;
		        		datos[iesc][ian][ip][iva]=Double.parseDouble(texto.get(ifil).get(icol));        					        			
		        		i++;        		        		
		        	}
        			if(iesc==0) ordinal++;
	        	}
	        }
        }
        dpesc.setDatos(datos);
        dpesc.setEtiquetaCron(etiquetaCron);
      
        // Si hay variables de estado dee los datos de las VARIABLES DE ESTADO
        ArrayList<String> nombresVE = new ArrayList<String>();      
        dpesc.setNombresVE(nombresVE);            
        if(cantVE!=0){
            String dirDatosVE = ruta + "/DatosVE.prn";
            texto = LeerDatosArchivo.getDatos(dirDatosVE);    
            i=0;          
            // Lee los nombres de las VE
            if(! texto.get(i).get(1).equalsIgnoreCase("NOMBRES_VE") || texto.get(i).size()!=(cantVA+1) ){
            	System.out.println("ERROR EN NOMBRES DE VE AL LEER EL PROCESO EN " + dirDatosVE);
            	if (CorridaHandler.getInstance().isParalelo()){
    				//PizarronRedis pp = new PizarronRedis();
    				//pp.matarServidores();
    			}
            	System.exit(1);
            }        
            for(int j=1; j<=cantVA; j++){
            	nombresVE.add(texto.get(i).get(j).toUpperCase());
            }         
            i++;
            double[][][][] valoresVE = new double[cantEsc][cantAnios][cantMaxPasos][cantVA];                       
            // lee los valores de las VE
            for(int iesc=0; iesc<dpesc.getCantEsc();iesc++){
            	for(int ian=0; ian<anioFin-anioIni; ian++){  
            		primerPaso = 0;
            		if(ian==0) primerPaso = pasoIni-1;
            		for(int ip=primerPaso; ip<=texto.get(i).size()-4; ip++){	        	
    	        		icol = ip + 3;
            			for(int iva=0; iva<dpesc.getCantVA(); iva++){ 
    		        		ifil = 1 + ian*(cantEsc*cantVA) + iesc*cantVA + iva;
    		        		valoresVE[iesc][ian][ip][iva]=Double.parseDouble(texto.get(ifil).get(icol));        					        			
    		        		i++;        		        		
    		        	}		       
    	        	}
    	        }
            }  
            dpesc.setValoresVE(valoresVE);
        }        
		return dpesc;		
	}


}
