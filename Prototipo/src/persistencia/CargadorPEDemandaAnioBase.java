/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEDemandaAnioBase is part of MOP.
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;

import datatypesProcEstocasticos.DatosPEDemandaAnioBase;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesTiempo.DatosTiposDeDia;
import utilitarios.LeerDatosArchivo;
import utilitarios.Par;

public class CargadorPEDemandaAnioBase {
	
	
	/**
	 * Crea un Data type para ProcesoDemandaAnioBAse
	 */
	public static DatosPEDemandaAnioBase devuelveDatosPEDemandaBase(DatosProcesoEstocastico dpe){
		DatosPEDemandaAnioBase dpdem = new DatosPEDemandaAnioBase();
		String ruta = "./resources/" + dpe.getNombre();
		dpdem.setDatGen(CargadorDatosGeneralesPE.devuelveDatosGeneralesPE(ruta, dpe.getNombre()));
		
		dpdem.setNombre(dpe.getNombre());
//		String ruta = dpe.getRuta();
		//String ruta = "./resources/"+ dpdem.getNombre() ;
		dpdem.setRuta(ruta);
		
		// Lee el archivo que tiene todo excepto los datos de potencia de aóos base
		// tiene sólo la dirección del archivo de potencias
		// Las potencias base se leen en el constructor
		
		
        ArrayList<ArrayList<String>> texto;
        String dirArch = ruta + "/datos.txt";		
        texto = LeerDatosArchivo.getDatos(dirArch);			
		
	   	int i=0;  
    	// Verifica nombre del proceso
    	if(!dpdem.getNombre().equalsIgnoreCase(texto.get(i).get(1))){
    		System.out.println("Error de nombre del proceso en el cargador del proceso demanda año base " + dpdem.getNombre());
    		System.exit(1);
    	}
    	i++;
    	
		if(!texto.get(i).get(0).equalsIgnoreCase("ESTIMACION")){
			System.out.println("Error en nombre de estimación de PEDemanda - " + dirArch);
			System.exit(0);
		}else{   	
			dpdem.setEstimacionVE(texto.get(i).get(1));
		}		
    	i++;
    	
    	
    	// Carga la ruta de los datos de potencia de los aóos base
//    	dpdem.setArchPotencias(texto.get(i).get(1));
    	String dirPotencias = ruta + "/potenciasBase.txt";		
    	dpdem.setArchPotencias(dirPotencias);
//    	i++;
    	
    	// Toma el atributo muestreado del datatype de la clase padre dpe
    	dpdem.setMuestreado(dpe.getMuestreado());
    	
    	/*
    	 *  Lee los restantes parómetros
    	 */
    	
    	// lee cantidad de variables de demanda
    	int cantVA=0;
		if(!texto.get(i).get(0).equalsIgnoreCase("CANT_VARIABLES")){
			System.out.println("Error en cant variables de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{    	
	    	cantVA = Integer.parseInt(texto.get(i).get(1));  
	    	dpdem.setCantVA(cantVA);		
	    	i++;
		}
		
		// lee si hay suma de variables: es decir si habrá una única V.A en el proceso, suma de las leídas
		if(!texto.get(i).get(0).equalsIgnoreCase("SUMA_VARIABLES")){
			System.out.println("Error en suma variables de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{  
			if(texto.get(i).get(1).equalsIgnoreCase("SI")) {
				dpdem.setSumaVar(true);
				i++;
				if(texto.get(i).get(0).equalsIgnoreCase("NOMBRE_VAR_SUMA")){
					dpdem.setNombre_var_suma(texto.get(i).get(1));
				}else {
					System.out.println("Error en nombre var suma de PEDemanda - "+ dirArch);
					System.exit(0);
				}
				i++;
			}else {
				i++;
				i++;
			}
		}		
		
		
    	// lee nombres de variables
		if(!texto.get(i).get(0).equalsIgnoreCase("NOMBRES_VARIABLES")){
			System.out.println("Error en nombres de variables de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{    	
	    	ArrayList<String> auxNom = new ArrayList<String>();
	    	for(int iva=0; iva<cantVA; iva++){
	    		auxNom.add(texto.get(i).get(iva+1));
	    	}
	    	i++;
	    	dpdem.setNombresVA(auxNom);
		}
		
		// lee nombre de la duración del paso
		if(!texto.get(i).get(0).equalsIgnoreCase("NOMBRE_PASO")){
			System.out.println("Error en nombre del paso de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{   		
	    	dpdem.setNombrePasoPE(texto.get(i).get(1));
	    	i++;
		}
		
		// lee años base
    	int[] aniosBase = null;
		if(!texto.get(i).get(0).equalsIgnoreCase("ANIOS_BASE")){
			System.out.println("Error en aóos base de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{
			aniosBase = new int[texto.get(i).size()-1];
			for(int j=0; j<texto.get(i).size()-1; j++){
				aniosBase[j]=Integer.parseInt(texto.get(i).get(j+1));
			}
			dpdem.setAniosBase(aniosBase);
		}
    	i++;
    	
    	
    	// lee el año base elegido
    	int anioBase = 0;
		if(!texto.get(i).get(0).equalsIgnoreCase("ANIO_BASE_ELEGIDO")){
			System.out.println("Error en aóo base elegido de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{
			anioBase = Integer.parseInt(texto.get(i).get(1));
			boolean esBase = false;
			for(int ind = 0; ind<dpdem.getAniosBase().length; ind++){
				if(anioBase==dpdem.getAniosBase()[ind]) esBase = true;
			}
			if(!esBase){
				System.out.println("El aóo base elegido es incorrecto de PEDemanda - "+ dirArch);
				System.exit(0);
			}
			dpdem.setAnioBaseElegido(anioBase);
		}		
    	i++;    	
    	
		// Lee años de inicio y fin del horizonte
		if(!texto.get(i).get(0).equalsIgnoreCase("ANIO_INICIAL_HORIZONTE")){
			System.out.println("Error en aóo inicial horizonte de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{			
			dpdem.setAnioInicialHorizonte(Integer.parseInt(texto.get(i).get(1)));
		}		
    	i++;		
		
		if(!texto.get(i).get(0).equalsIgnoreCase("ANIO_FINAL_HORIZONTE")){
			System.out.println("Error en año final horizonte de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{			
			dpdem.setAnioFinalHorizonte(Integer.parseInt(texto.get(i).get(1)));
		}		
    	i++;	
		int anini = dpdem.getAnioInicialHorizonte();
		int anfin = dpdem.getAnioFinalHorizonte();
    	
    	// Lee energías anuales
		if(!texto.get(i).get(0).equalsIgnoreCase("ENERGIAS_GWH")){
			System.out.println("Error en energóas de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{	
			i++;
			Hashtable<Integer, double[]> energias = new Hashtable<Integer, double[]>();
			// Lee energóa de aóos base
			for(int ian=0; ian< aniosBase.length; ian++){
				if(Integer.parseInt(texto.get(i).get(0))!=aniosBase[ian]){
					System.out.println("Error en energía del año " + anini+ian +" de PEDemanda - "+ dirArch);
					System.exit(0);
				}
				double[] aux = new double[cantVA];
				for(int id=0; id<cantVA; id++){
					aux[id]= Double.parseDouble(texto.get(i).get(1+id));
				}
				energias.put(aniosBase[ian], aux);

				i++;
			}			
			// Lee energía de años del horizonte de estimación
			for(int ian=0; ian< anfin-anini+1; ian++){
				if(Integer.parseInt(texto.get(i).get(0))!=anini+ian){
					System.out.println("Error en energía del año " + anini+ian +" de PEDemanda - "+ dirArch);
					System.exit(0);
				}
				double[] aux = new double[cantVA];
				for(int id=0; id<cantVA; id++){
					aux[id]= Double.parseDouble(texto.get(i).get(1+id));
				}
				energias.put(ian+anini, aux);
				i++;
			}
			dpdem.setEnergias(energias);
		}
		
		// Lee si se ajusta la energía anual
		if(!texto.get(i).get(0).equalsIgnoreCase("AJUSTE_ENERGIAS_ANUALES")){
			System.out.println("Error en ajuste energias anuales de PEDemanda - "+ dirArch);
			System.exit(0);
		}else{			
			dpdem.setAjusteEnergiasAnuales(false);
			if(texto.get(i).get(1).equalsIgnoreCase("si")) dpdem.setAjusteEnergiasAnuales(true);
		}		
    	i++;	 
    	
    	
		String rutaTiposDia = "./resources/" + "tiposDeDia.txt";
		DatosTiposDeDia tiposDeDia = persistencia.CargadorTiposDeDia.cargaTiposDeDia(rutaTiposDia, anioBase, anioBase);
    	
		dpdem.setTiposDeDia(tiposDeDia);
		

		return dpdem;
	}
	
	

}

