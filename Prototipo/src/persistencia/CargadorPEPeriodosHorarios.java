/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEPeriodosHorarios is part of MOP.
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

import datatypesProcEstocasticos.DatosPEPorPeriodos;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import utilitarios.LeerDatosArchivo;

public class CargadorPEPeriodosHorarios {
	
	/**
	 * Crea un datatype PEPorPeriodos a partir de datos que lee de una ruta contenida en 
	 * el argumento dpe.
	 *  
	 * @author ut469262
	 *
	 */	
	public static DatosPEPorPeriodos devuelveDatosPEPorPeriodo(DatosProcesoEstocastico dpe){
		DatosPEPorPeriodos dpp = new DatosPEPorPeriodos();
		dpp.setNombre(dpe.getNombre());
		String ruta = dpe.getRuta();
		dpp.setRuta(ruta);
		
	
        ArrayList<ArrayList<String>> texto;
        String dirDatosSal = ruta + "/datosPEPorPeriodos.txt";
        texto = LeerDatosArchivo.getDatos(dirDatosSal);
        int i=0;
        // Verifica el nombre del proceso          
        if(!dpp.getNombre().equalsIgnoreCase(texto.get(i).get(1))){
        	System.out.println("El nombre del proceso " + dpp.getNombre() + " es incorrecto");
        }        
        i++;  
        // Lee estimación
        dpp.setEstimacionVE(texto.get(i).get(1));
        i++;
        // Lee nombre del proceso base 
        dpp.setNombreProcesoBase(texto.get(i).get(1));
        i++;
        // Lee cantidad de "horas" del paso de tiempo       
        int cantHoras = Integer.parseInt(texto.get(i).get(1));
        dpp.setCantHoras(cantHoras);
        i++;        
        // Lee cantidad de peróodos
        int cantPeriodos = Integer.parseInt(texto.get(i).get(1));
        dpp.setCantPeriodos(cantPeriodos);
        i++;        
        // Lee el peróodo de cada "hora"
        int[] aux = new int[cantHoras];
        for(int ih=0; ih<cantHoras; ih++){
        	int per = Integer.parseInt(texto.get(i).get(1+ih));
        	aux[ih]=per;
        }
        dpp.setPeriodoDeHoras(aux);        
        return dpp;
		
	}


}
