/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorInicialesYRezagadas is part of MOP.
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
import cp_datatypesEntradas.DatosValRezagadosCP;
import cp_despacho.DespachoProgEstocastica;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.LeerDatosArchivo;

public class CargadorInicialesYRezagadas {
	
//	public static DatosCondInicialesCP devuelveCondInicialesCP(String ruta) {
//		
//		DatosCondInicialesCP dat = new DatosCondInicialesCP();		
//		String dirArchivo = ruta + "/CondicionesIniciales.txt";		
//        ArrayList<ArrayList<String>> texto;       
//        texto = LeerDatosArchivo.getDatos(dirArchivo);	       
//               
//        for(int i=0; i<texto.size(); i++) {
//        	String clave = DespachoProgEstocastica.nomBase(texto.get(i).get(1), texto.get(i).get(2));
//        	dat.getCondIniciales().put(clave, Double.parseDouble(texto.get(i).get(3)));     	
//        }
//        System.out.println("Termina la lectura de condiciones iniciales de participantes");
//        return dat;
//		
//	}
	
	
//	public static DatosValRezagadosCP devuelveValRezagados(String ruta) {		
//		DatosValRezagadosCP dat = new DatosValRezagadosCP();
//		String dirArchivo = ruta + "/CondicionesIniciales.txt";		
//        ArrayList<ArrayList<String>> texto;       
//        texto = LeerDatosArchivo.getDatos(dirArchivo);	
//        
//        for(int i=0; i<texto.size(); i++) {
//        	String clave = DespachoProgEstocastica.nomBase(texto.get(i).get(1), texto.get(i).get(2));
//        	int cantVal = texto.get(i).size() - 3;
//        	double[] aux = new double[cantVal];
//        	for(int iv=0; iv<cantVal; iv++) {
//        		aux[iv] = Double.parseDouble(texto.get(i).get(3+iv));
//        	}
//        	dat.getValRezagados().put(clave, aux);  	
//        }  
//        System.out.println("Termina la lectura de valores rezagados de variables (en poste p<0)");
//		return dat;
//		
//	}
//	

}
