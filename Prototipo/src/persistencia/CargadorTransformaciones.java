/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorTransformaciones is part of MOP.
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

import datatypesProcEstocasticos.DatosAgregadorLineal;
import datatypesProcEstocasticos.DatosTransformaciones;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.LeerDatosArchivo;

public class CargadorTransformaciones {
	
	
	/**
	 * Crea un datatype DatosTransformaciones a partir de datos que lee de
	 * la ruta, que tiene el directorio del PE de simulación.
	 * En ese directorio levanta un archivo de nombre "transformaciones.txt" 
	 * 
	 * @author ut469262
	 *
	 */	
	public static DatosTransformaciones devuelveDatosTransformaciones(String ruta){

		DatosTransformaciones dat = new DatosTransformaciones();
				
		String dirTrans = ruta + "/transformaciones.txt";
		
        ArrayList<ArrayList<String>> texto;        
        texto = LeerDatosArchivo.getDatos(dirTrans);	
        
        AsistenteLectorEscritorTextos lector = new AsistenteLectorEscritorTextos(texto, dirTrans);
        
    	int i=0;   	
 
    	dat.setNombreEstimacion(lector.cargaPalabra(i, "NOMBRE_ESTIMACION"));
    	i++;
    	
    	ArrayList<String> nombresS = lector.cargaLista(i, "NOMBRES_SERIES");
    	dat.setNombresSeries(nombresS);    	
    	i++;
    	 	
    	ArrayList<String> aux = lector.cargaLista(i, "TIPO_TRANSFORMACIONES");
    	Hashtable<String, String> tipoT = new Hashtable<String, String>();
    	int is=0;
    	for(String s: dat.getNombresSeries()){
    		tipoT.put(s, aux.get(i));
    		is++;
    	}    	
    	dat.setTipoTransformaciones(tipoT);
    	i++;
    	
    	dat.setNombrePaso(lector.cargaPalabraDeLista(i, "PASO_DEL_PROCESO", utilitarios.Constantes.NOMBRESPASOS));	
    	i++;
    	
    	dat.setCantPasos(utilitarios.Constantes.CANTMAXPASOS.get(dat.getNombrePaso()));
    	if(Integer.parseInt(texto.get(i).get(1))!=dat.getCantPasos()){
			System.out.println("Error en la lectura de cantidad de pasos de serie, archivo " + dirTrans);
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(1);
		}
    	i++;
    	
    	Hashtable<String, ArrayList<ArrayList<Double>>> parametros = new Hashtable<String, ArrayList<ArrayList<Double>>>();
    	
		for(int ive=0; ive<nombresS.size(); ive++){
			String nom = dat.getNombresSeries().get(ive);
			if(!texto.get(i).get(0).equalsIgnoreCase(nom)){
				System.out.println("Error en la lectura de transformaciones de serie " + nom);
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
					//pp.matarServidores();
				}
				System.exit(1);
			}
			i++;
			if(dat.getTipoTransformaciones().get(nombresS.get(ive)).equalsIgnoreCase(utilitarios.Constantes.BOXCOX))
				dat.setCantParametros((lector.cargaEntero(i, "CANT_PARAMETROS")));
			i++;
			i++;
			ArrayList<ArrayList<Double>> p1 = new ArrayList<ArrayList<Double>>();
			for(int ipa=0; ipa<dat.getCantPasos(); ipa++){
				ArrayList<Double> p2 = new ArrayList<Double>();
				for(int ipr=0; ipr<texto.get(i).size(); ipr++){
					p2.add(Double.parseDouble(texto.get(i).get(ipr)));
				}
				p1.add(p2);
				i++;
			}				
			parametros.put(nom, p1);
			dat.setParametros(parametros);
		}
    	return dat;	
	}
	
	
	public static void main(String[] args){
		DatosTransformaciones dat = new DatosTransformaciones();
		String archTrans = "D:/Proyectos/ModeloOp/resources/varmaAportes";
		dat = devuelveDatosTransformaciones(archTrans);
		System.out.println("termina prueba");
	}
	
}


	
