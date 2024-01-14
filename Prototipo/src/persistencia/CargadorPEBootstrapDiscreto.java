/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEBootstrapDiscreto is part of MOP.
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

import datatypesProcEstocasticos.DatosPEBootstrapDiscreto;
import datatypesProcEstocasticos.DatosPEMarkov;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import procesosEstocasticos.MatPTrans;
import utilitarios.LeerDatosArchivo;

public class CargadorPEBootstrapDiscreto {
	
	
	/**
	 * Crea un datatype DatosPEBootstrapDiscreto a partir de datos que lee de una ruta contenida en 
	 * el argumento dpe.
	 * 
	 *  
	 * @author ut469262
	 *
	 */	
	public static DatosPEBootstrapDiscreto devuelveDatosPEBootstrap(DatosProcesoEstocastico dpe){
		final int COLINI = 2;
		
		DatosPEBootstrapDiscreto dpb = new DatosPEBootstrapDiscreto();


		String ruta = "./resources/" + dpe.getNombre();
		
		dpb.setDatGen(CargadorDatosGeneralesPE.devuelveDatosGeneralesPE(ruta, dpe.getNombre()));
		dpb.setNombre(dpe.getNombre());
				
		/*String ruta = dpe.getRuta();
		dpb.setRuta(ruta);*/
	//	String ruta = "./resources/" + dpb.getNombre();
		
		
    	// Carga el nombre del archivo con los datos
    	dpb.setArchDatos(ruta + "/datos.txt");		

		// Lee el archivo que tiene todo excepto los datos
		// tiene sólo la dirección de los datos históricos de las series
        ArrayList<ArrayList<String>> texto;
        String dirArch = ruta + "/TextoParaCargador.txt";		
        texto = LeerDatosArchivo.getDatos(dirArch);	
        
    	int i=0;  
    	// Verifica nombre del proceso
    	if(!dpb.getNombre().equalsIgnoreCase(texto.get(i).get(1))){
    		System.out.println("Error de nombre del proceso en el cargador del proceso bootstrap " + dpb.getNombre());
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(1);
    	}
    	i++;


    	
    	// Toma el atributo muestreado del datatype de la clase padre dpe
    	dpb.setMuestreado(dpe.getMuestreado());
    	
    	// Lee los restantes parómetros
    	int cantVA = Integer.parseInt(texto.get(i).get(1));  
    	dpb.setCantVA(cantVA);
    	i++;
    	
    	dpb.setNombrePasoPE(texto.get(i).get(1));
    	i++;
    	
    	int durHora = Integer.parseInt(texto.get(i).get(1));   
    	dpb.setDurHora(durHora);
    	i++;    	
    	
    	int cantHoras = Integer.parseInt(texto.get(i).get(1));
    	dpb.setCantHoras(cantHoras);
    	i++;
    	
    	int cantMaxDiasAnio = Integer.parseInt(texto.get(i).get(1));
    	dpb.setCantMaxDias(cantMaxDiasAnio);
    	i++;    	
    
    	// No lee el ancho de la ventana móvil;
    	i++;
    	
    	
    	// 
    	
    	dpb.setCantDiasDatos(Integer.parseInt(texto.get(i).get(1)));
    	i++;
    	
        // Lee nombres de VA y VE      
        ArrayList<String> nombresVA = new ArrayList<String>(); 
        for (int j=0; j<cantVA; j++){
        	nombresVA.add(texto.get(i).get(j+1));
        }
        dpb.setNombresVA(nombresVA);
        i++;     
                
        // Hay una sola variable de estado
        dpb.setCantVE(1);
        ArrayList<String> nombresVE = new ArrayList<String>(); 
        for (int j=0; j<dpb.getCantVE(); j++){
        	nombresVE.add(texto.get(i).get(j+1));
        }
        dpb.setNombresVE(nombresVE);
        i++; 
        
        dpb.setNombresVEOptim(new ArrayList<String>()); 
        boolean varEstadoEnOptim = false;
        if(texto.get(i).get(1).equalsIgnoreCase("true")){
        	varEstadoEnOptim = true;
            dpb.setNombresVEOptim(nombresVE);
            // la VE de la optimización tiene el mismo nombre
        }
        dpb.setVarEstadoEnOptim(varEstadoEnOptim); 
        i++;
        
        dpb.setCantCla(new int[cantVA]);
        double[][] probCla = new double[cantVA][];
        // Lee las probabilidades de las clases
        for(int iv=0; iv<cantVA; iv++){
        	if(!texto.get(i).get(1).equalsIgnoreCase(nombresVA.get(iv))){
        		System.out.println("error en cargador de bootstrap en nombre de variable para probabilidades");
        		if (CorridaHandler.getInstance().isParalelo()){
    				//PizarronRedis pp = new PizarronRedis();
    				//pp.matarServidores();
    			}
        		System.exit(0);
        	}
        	dpb.getCantCla()[iv]=texto.get(i).size()-2;
        	double[] aux = new double[dpb.getCantCla()[iv]];        	
        	for(int iprob=0; iprob<dpb.getCantCla()[iv]; iprob++){
        		aux[iprob] = Double.parseDouble(texto.get(i).get(2+iprob));
        	}        	
        	probCla[iv]=aux;
        	i++;
        }
        dpb.setProbCla(probCla);
        
        // Lee la cantidad de estados compuestos
        int cantEstadosComp = Integer.parseInt(texto.get(i).get(1));
        dpb.setCantEstadosComp(cantEstadosComp);
    	i++;
        
    	
    	
    	double[][] ponderadores = new double[cantVA][cantHoras];
        // Lee los ponderadores para crear las VE continuas de cada VA
        for(int iv=0; iv<cantVA; iv++){
        	if(!texto.get(i).get(1).equalsIgnoreCase(nombresVA.get(iv))){
        		System.out.println("error en cargador de bootstrap en nombre de variable para ponderadores");
        		if (CorridaHandler.getInstance().isParalelo()){
    				//PizarronRedis pp = new PizarronRedis();
    				//pp.matarServidores();
    			}
        		System.exit(0);
        	}
        	for(int ipon=0; ipon<dpb.getCantHoras(); ipon++){
        		ponderadores[iv][ipon] = Double.parseDouble(texto.get(i).get(2+ipon));
        	}
        	i++;
        }       
        dpb.setPonderadores(ponderadores);
    	
        double[][][] limSup = new double[cantMaxDiasAnio][cantVA][];
        // Lee los lómites superiores de las clases continuas
        for(int ip=0; ip<cantMaxDiasAnio; ip++){
	        for(int iv=0; iv<cantVA; iv++){
	        	double[] aux = new double[dpb.getCantCla()[iv]];
	        	if(ip!=Integer.parseInt(texto.get(i).get(1))){
	        		System.out.println("error en cargador de bootstrap en nómero de dóa para ponderadores");
	        		System.exit(0);        			        		
	        	}	        	
	        	if(!texto.get(i).get(3).equalsIgnoreCase(nombresVA.get(iv))){
	        		System.out.println("error en cargador de bootstrap en nombre de variable para lómites superiores");
	        		if (CorridaHandler.getInstance().isParalelo()){
	    				//PizarronRedis pp = new PizarronRedis();
	    				//pp.matarServidores();
	    			}
	        		System.exit(0);
	        	}
	        	for(int iprob=0; iprob<dpb.getCantCla()[iv]; iprob++){
	        		aux[iprob] = Double.parseDouble(texto.get(i).get(4+iprob));	        		
	        	}
	        	limSup[ip][iv]=aux;
	        	i++;
	        }     
        }
        dpb.setLimitesSupClases(limSup);
        
        // Lee las poblaciones de sucesores para cada dóa del aóo y estado compuesto
       
        Object[][] poblacionesSucesores = new Object[cantMaxDiasAnio][cantEstadosComp]; 
        for(int ip=0; ip<cantMaxDiasAnio; ip++){
	        for(int iec=0; iec<cantEstadosComp; iec++){
	        	if(ip!=Integer.parseInt(texto.get(i).get(1))){
	        		System.out.println("error en cargador de bootstrap en nómero de dóa para ponderadores");
	        		System.exit(0);        			        		
	        	}	        	
	        	if(iec!=Integer.parseInt(texto.get(i).get(3))){
	        		System.out.println("error en cargador de bootstrap en nómero de estado compuesto");
	        		if (CorridaHandler.getInstance().isParalelo()){
	    				//PizarronRedis pp = new PizarronRedis();
	    			//	pp.matarServidores();
	    			}
	        		System.exit(0);
	        	}
	        	ArrayList<Integer> pob1PasoYEstado = new ArrayList<Integer>();
	        	for(int is=0; is<texto.get(i).size()-5;is++){
	        		pob1PasoYEstado.add(Integer.parseInt(texto.get(i).get(5+is)));
	        	}
	        	
	        	poblacionesSucesores[ip][iec]=pob1PasoYEstado;
	        	i++;
	        }     
        }  
        dpb.setPoblacionesSucesores(poblacionesSucesores);  
        
        // Lee las probabilidades de los estados compuestos de cada dóa
        double[][] probabilidadesEstadosCompuestos = new double[cantMaxDiasAnio][cantEstadosComp];
        for(int ip=0; ip<cantMaxDiasAnio; ip++){	        
	        if(ip!=Integer.parseInt(texto.get(i).get(1))){
	        	System.out.println("error en cargador de bootstrap en nómero de dóa para ponderadores");
	        	if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
	        	System.exit(0);        			        		
	        }
	        for(int iec=0; iec<cantEstadosComp; iec++){
	        	probabilidadesEstadosCompuestos[ip][iec] = Double.parseDouble(texto.get(i).get(iec+2));
	        } 
	     	i++;
        }  
        dpb.setProbabilidadesEstadosCompuestos(probabilidadesEstadosCompuestos);         
        
		
	    // No se leen los datos historicos porque los leeró el constructor de ProcesoBootstrapDiscreto 		
		

    	return dpb;	        
	}	

}
