/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorCargaTxtParaEstimaRecursosAlea is part of MOP.
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

import datatypesProcEstocasticos.DatosPEDemandaAnioBase;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import procesosEstocasticos.EstimadorBootstrapDiscreto;
import procesosEstocasticos.ProcesoDemandaAnioBase;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LectorDireccionArchivoDirectorio;

public class GeneradorCargaTxtParaEstimaRecursosAlea {
	
	
	
	
    /**
     * El programa 
     * - lee los datos de energías futuras y potencias de años históricos
     * - genera el archivo CARGA.txt que es empleado por ModeloEstimaRecursosAlea
     *   que a su vez produce procesos escenarios para la postización externa.
     *   
     */
	
	 	
    public static void main(String[] args) {    
    	
        StringBuilder sb = new StringBuilder("//FECHA HORA SEMANA TIPODIA POTENCIA\n");
    	
        // Elige directorios de entradas y salidas
        boolean soloDirectorio = true;        
        String titulo1 = "ELIJA EL DIRECTORIO DE ENTRADA DE DATOS DONDE LEER LOS ARCHIVOS potenciasBase.txt y datos.txt";
        String dirEntradas = LectorDireccionArchivoDirectorio.direccionLeida(soloDirectorio, ".", titulo1);
        String[] partes = dirEntradas.split("\\\\");
        String nombre = partes[partes.length-1];
        String titulo2 = "ELIJA EL DIRECTORIO DE SALIDA DE LA ESTIMACION, ARCHIVO: CARGA.txt";
        String dirSalidas =  LectorDireccionArchivoDirectorio.direccionLeida(soloDirectorio, ".", titulo2);    	
    	    	
        // Construye un ProcesoDemandaAnioBase para generar las potencias futuras
        DatosProcesoEstocastico dpe = new DatosProcesoEstocastico(nombre, null, null, dirEntradas, false, false, null, null, "");        
        DatosPEDemandaAnioBase dpdab = CargadorPEDemandaAnioBase.devuelveDatosPEDemandaBase(dpe);
        ProcesoDemandaAnioBase procDAB = new ProcesoDemandaAnioBase(dpdab, null, 2012, 10);

        int anioHorizonteIni = procDAB.getAnioHorizonteIni();
        int anioHorizonteFin = procDAB.getAnioHorizonteFin();
        
		GregorianCalendar cal = new GregorianCalendar(anioHorizonteIni, Calendar.JANUARY, 1);        
		GregorianCalendar cal2 = new GregorianCalendar(anioHorizonteIni, Calendar.JANUARY, 1);    
		for(int ian=anioHorizonteIni; ian<=anioHorizonteFin; ian++){
        	double[] facAnio = procDAB.getFactores().get(ian);
        	int indAnio = ian - anioHorizonteIni;
        	int cantDias = 365;
			if(cal.isLeapYear(ian)) cantDias = 366;
			int isem = 1;
			for(int idia=1; idia<= cantDias; idia++){
				int mes = cal2.get(Calendar.MONTH);
				int diames =cal2.get(Calendar.DAY_OF_MONTH);
				int ordinalBase = procDAB.getDiaAnioBaseAsociado()[indAnio][idia-1];
				
				for(int ih=1; ih<25; ih++){
					double pot = procDAB.getPotenciasAnioBase()[ordinalBase][ih-1][0]*facAnio[0];
					int numFecha = ian*10000 + (mes+1)*100 + diames;
					// El tipo de dóa no es necesario
					String linea = numFecha + " " + ih + " " + isem + " -99 " + pot;
					System.out.println(linea);
					linea = linea + "\n";
					sb.append(linea); 
				}
				if(idia%7 == 0  && isem<52) isem++;
				cal2.add(Calendar.DAY_OF_MONTH, 1);
			}		
        }
		String nombreSal = dirSalidas+"/CARGA.txt";
		if(DirectoriosYArchivos.existeArchivo(nombreSal)) 
			DirectoriosYArchivos.eliminaArchivo(nombreSal);
        DirectoriosYArchivos.agregaTexto(dirSalidas+"/CARGA.txt", sb.toString());
        System.out.println("TERMINó LA GENERACIóN DE CARGA.txt");
    }
	
	
	
	

}
