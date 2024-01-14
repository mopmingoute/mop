/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorDiscretizacionesVEPE is part of MOP.
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

import datatypes.DatosDiscretizacion;
import datatypesProcEstocasticos.DatosDiscretizacionesVEPE;
import datatypesProcEstocasticos.DatosTransformaciones;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.LeerDatosArchivo;

public class CargadorDiscretizacionesVEPE {
	
	private static AsistenteLectorEscritorTextos lector;
	
	public static DatosDiscretizacionesVEPE devuelveDatosDiscretizacionesVEPE(String ruta){

		DatosDiscretizacionesVEPE dat = new DatosDiscretizacionesVEPE();
				
		String dirDisc = ruta + "/discretizaciones.txt";
		
        ArrayList<ArrayList<String>> texto;        
        texto = LeerDatosArchivo.getDatos(dirDisc);	
        
        lector = new AsistenteLectorEscritorTextos(texto, dirDisc);
        
    	int i=0;   	
 
    	ArrayList<String> nombresVE = lector.cargaLista(i,"NOMBRES_VE_CONTINUAS");
    	i++;
    	for(String nom: nombresVE){
    		lector.verificaEtiqueta(i, nom);
    		i++;
    		int inuevo = cargaUnaDiscretizacion(i, nom, dat);
    		i = inuevo;
    	}
    	return dat;
	}
	
	/**
	 * Carga en el DatosDiscretizacionesVEPE una discretización y 
	 * devuelve la fila siguiente al fin de lo leído.
	 * Verifica que corresponda a la VE de nombreVE
	 * @param i
	 * @return
	 */
	public static int cargaUnaDiscretizacion(int i, String nombreVE, DatosDiscretizacionesVEPE dat){

		DatosDiscretizacion dd = new DatosDiscretizacion();
		dd.setMinimo(lector.cargaReal(i, "MINIMO"));
		i++;
		dd.setMaximo(lector.cargaReal(i, "MAXIMO"));
		i++;
		int cantPuntos = lector.cargaEntero(i, "CANT_PUNTOS");
		i++;
		boolean equi = lector.cargaBooleano(i, "EQUIESPACIADA");
		i++;
		double[] particion = new double[cantPuntos];
		dd.setParticion(particion);		
		if(equi){
			double salto = (dd.getMaximo() - dd.getMinimo()) / (cantPuntos - 1);
			for (int j = 0; j < cantPuntos; j++) {
				particion[j] = dd.getMinimo() + j * salto;
			}		
		}else{
			ArrayList<String> listaVal = lector.cargaLista(i, "VALORES");
			int is = 0;
			for(String s: listaVal){
				particion[is] = Double.parseDouble(s);
				is++;
			}
			i++;
		}
		dat.cargaDatosDisc(nombreVE, dd);
		return i;
	}

}
