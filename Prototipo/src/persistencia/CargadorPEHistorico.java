/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEHistorico is part of MOP.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import utilitarios.Constantes;
import utilitarios.LeerDatosArchivo;
import datatypesProcEstocasticos.DatosPEHistorico;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import logica.CorridaHandler;
import pizarron.PizarronRedis;


/**
 * Crea un datatype PEHistórico a partir de datos que lee de una ruta.
 * Las crónicas deben estar ordenadas por su etiqueta cronica. 
 * @author ut469262
 *
 */
public class CargadorPEHistorico {	

	private static String idEstimacionVE;   // identificación de la estimación de las VE que se empleó
	private static double[][][]  datos;   // datos de las VA, la segunda dimensión es la cantidad de pasos por crónica móxima posible
	private static double[][][]  valoresVE;
	private static int cantVA;   		// cantidad de variables aleatorias
	private static int cantVE;	
	private static int cantCron; 		// es la cantidad de crónicas anuales disponibles
	private static int cantDatos;      // cantidad de datos
	private static int[] cronicas;     // da una etiqueta a cada crónica; por ejemplo "1909"
	private static int[] cantPasosCronica; 	// cantidad de pasos que entran en cada crónica; por los bisiestos varóa
	private static int cantPasosMax;   // móxima cantidad de pasos que puede tener una crónica
	private static int[] cantValoresVE; // cantidad de valores de cada VE
	
	
	/**
	 * Crea un datatype PEHistórico a partir de datos que lee de una ruta contenida en 
	 * el argumento dpe.
	 * El formato de los datos es:
	 * primera columna - rótulo entero de la crónica
	 *  
	 * @author ut469262
	 *
	 */	
	public static DatosPEHistorico devuelveDatosPEHistorico(DatosProcesoEstocastico dpe){
		DatosPEHistorico dph = new DatosPEHistorico();
		try {
			String pp = new File(".").getCanonicalPath();
			System.out.println(pp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String ruta = "./resources/" + dpe.getNombre();
		dph.setDatGen(CargadorDatosGeneralesPE.devuelveDatosGeneralesPE(ruta, dpe.getNombre()));
		dph.setNombre(dpe.getNombre());
		//String ruta = dpe.getRuta();
		//String ruta = "./resources/" + dph.getNombre();
		
		dph.setRuta(ruta);
		
	
        ArrayList<ArrayList<String>> texto;
        String dirDatosSal = ruta + "/datosProcHistorico.xlt";
        texto = LeerDatosArchivo.getDatos(dirDatosSal);
        int i=0;
        // Lee identificador de la estimación de VE usada  
        idEstimacionVE = "";
        for(int j=1; j<texto.get(i).size(); j++){
        	idEstimacionVE = idEstimacionVE + texto.get(i).get(j);
        }
        dph.setEstimacionVEUsada(texto.get(i).get(1));
        i++;
        // Lee cantidad de VA 
        cantVA = Integer.parseInt(texto.get(i).get(1));
        dph.setCantVA(cantVA);
        i++;        
        // Lee cantidad de VE
        cantVE = Integer.parseInt(texto.get(i).get(1));
        dph.setCantVE(cantVE);
        i++; 
        
        // Lee la cantidad de valores que puede tener cada VE (fijos en todo el período)
        cantValoresVE = new int[cantVE];
        for(int ive=0; ive<cantVE; ive++){
       	 cantValoresVE[ive]=Integer.parseInt(texto.get(i).get(3));
       	 i++;
        }
        dph.setCantValoresVE(cantValoresVE);
        
        // Lee cantidad de crónicas
        cantCron = Integer.parseInt(texto.get(i).get(1));
        dph.setCantCron(cantCron);
        i++;
        // Lee nombre del paso
        dph.setNombrePaso(texto.get(i).get(1)); 
        i++;
		if(dph.getNombrePaso().equalsIgnoreCase(Constantes.PASOSEMANA)) {
			dph.setCantPasosMax(52);
			cantPasosMax = 52;
			dph.setSaltoAtrasAlRepetir(1);
		}
		if(dph.getNombrePaso().equalsIgnoreCase(Constantes.PASODIA)){
			dph.setCantPasosMax(366);
			cantPasosMax = 366;			
			dph.setSaltoAtrasAlRepetir(1);
		}
		if(dph.getNombrePaso().equalsIgnoreCase(Constantes.PASOHORA)){
			dph.setCantPasosMax(8784);
			cantPasosMax = 8784;			
			dph.setSaltoAtrasAlRepetir(24);
		}
		
        // Lee cantidad de datos
		cantDatos = Integer.parseInt(texto.get(i).get(1));
        dph.setCantDatos(cantDatos); 
        i++;		
		

        // Lee nombres de VA y VE
        
        dph.setNombresVA(new ArrayList<String>());
        dph.setNombresVE(new ArrayList<String>());
        
        for (int j=0; j<cantVA; j++){
        	dph.getNombresVA().add(texto.get(i).get(j+2));
        }
        
        for (int j=0; j<cantVE; j++){
        	dph.getNombresVE().add(texto.get(i).get(j+cantVA+2));
        }
        i++;        
       
             
        // Lee los datos
        int filaIni = i;
        int rotuloAnt = Integer.MAX_VALUE;
        int pasoAnt = 0;
        int icron = -1;
        int ifil;
        int ipaso;
        datos = new double[cantCron][cantPasosMax][cantVA];        
        valoresVE = new double[cantCron][cantPasosMax][cantVE];
        cronicas = new int[cantCron];
        cantPasosCronica = new int[cantCron];
        
        for(i=0; i<cantDatos; i++){
        	ifil = i+filaIni;
        	int rotuloLeido = Integer.parseInt(texto.get(ifil).get(0));
        	int pasoLeido = Integer.parseInt(texto.get(ifil).get(1));
        	if (rotuloLeido!= rotuloAnt){
        		icron++;
        		cronicas[icron] = rotuloLeido;
        		if(icron>0) cantPasosCronica[icron-1]=pasoAnt;
        		if(pasoLeido!=1){
        			System.out.println("Error en crónica" + rotuloLeido);
        			if (CorridaHandler.getInstance().isParalelo()){
        				//PizarronRedis pp = new PizarronRedis();
        				//pp.matarServidores();
        			}
        			System.exit(1);
        		}
        	}else{
        		if(pasoLeido!= pasoAnt+1){
            		System.out.println("Error en crónica" + rotuloLeido);
            		if (CorridaHandler.getInstance().isParalelo()){
        				//PizarronRedis pp = new PizarronRedis();
        				//pp.matarServidores();
        			}
            		System.exit(1);            		        			
        		}
        	}
        	ipaso = pasoLeido - 1;
            cronicas[icron] = rotuloLeido;            
            for(int j=2; j<2+cantVA;j++){
                datos[icron][ipaso][j-2] = Double.parseDouble(texto.get(ifil).get(j)); 
            }   
            for(int j=2+cantVA; j<2+cantVA+cantVE;j++){
                valoresVE[icron][ipaso][j-2-cantVA] = Double.parseDouble(texto.get(ifil).get(j)); 
            }  
    		rotuloAnt = rotuloLeido;            
            pasoAnt = pasoLeido;         
        }   
        cantPasosCronica[cantCron-1] =  pasoAnt;
        dph.setDatos(datos);
        dph.setValoresVE(valoresVE);
        dph.setCronicas(cronicas);
        dph.setCantPasosCron(cantPasosCronica);
        dph.setCronIni(cronicas[0]);
        dph.setCronFin(cronicas[cronicas.length-1]);

		return dph;		
	}


}
