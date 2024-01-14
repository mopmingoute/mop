/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorResOptimSIMSEE is part of MOP.
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
import java.util.GregorianCalendar;
import java.util.Hashtable;

import optimizacion.LectorValOptEDF;
import optimizacion.LectorValOptSimsee;
import optimizacion.ResOptimIncrementos;
import utilitarios.Constantes;
import datatypesResOptim.DatosResOptimIncrementos;
import futuro.ClaveDiscreta;
import futuro.InformacionValorPunto;
import futuro.TablaControlesDE;
import futuro.TablaControlesDEMemoria;
import futuro.TablaVByValRecursos;
import futuro.TablaVByValRecursosMemoria;


/*
 * Crea un ResOptimIncrementos a partir de la lectura del VAGUA.D del EDF
 * Supone que la variable de estado hidrológica es la primera variable de estado y
 * el paso de stock de Bonete es la segunda variable de estado.
 */
public class CargadorResOptimSIMSEE {
	
	
	public static ResOptimIncrementos devuelveResOptimIncrementos(DatosResOptimIncrementos droi){
		ResOptimIncrementos resOptim = new ResOptimIncrementos(); 
		TablaVByValRecursos tablaVB = new TablaVByValRecursosMemoria();
			
		String ruta = droi.getRuta();        
        LectorValOptSimsee lecVagua = new LectorValOptSimsee();
        double[] volumenesBon = new double[10];
        for(int i=0; i<10; i++){
        	volumenesBon[i] = i*(8252.7/9);
        }        
        lecVagua.creaValorDelAgua(ruta, volumenesBon);    
	    int anini = lecVagua.getAnini();
	    int anfin = lecVagua.getAnfin();
	    int cantPasosStock = lecVagua.getCantPasoStock();
	    int cantEstados = lecVagua.getCantVarEst();	      
	    
	    lecVagua.leerVagua(ruta);        
	    
        /**
         * Carga los Hashtable del ResOptim a partir del lecVagua
         */
        for(int ian=anini; ian<=anfin; ian++){
        	for(int isem = 1; isem<=52; isem++){        		
        		Hashtable<ClaveDiscreta, InformacionValorPunto> ht = new Hashtable<ClaveDiscreta, InformacionValorPunto>() ;        		
        		int indicePasoTiempo = (ian - anini)*52 + isem - 1;
        		for(int iest = 1; iest<= cantEstados; iest++){
        			// Hay un paso mós de stock en las derivadas en puntos que en los cocientes incrementales
        			for(int ipas = 1; ipas<= cantPasosStock+1; ipas++){ 
        				double[] derivadasParciales = new double[1];
        				derivadasParciales[0] = lecVagua.getValRecurso().get(indicePasoTiempo)[iest-1][ipas-1];
                		InformacionValorPunto ivp = new InformacionValorPunto();
                		ivp.setDerivadasParciales(derivadasParciales);   
                		int[] codigoIndices = new int[2];
                		codigoIndices[0]= iest - 1;
                		codigoIndices[1]= ipas - 1;
                		ClaveDiscreta cd = new ClaveDiscreta(codigoIndices);
                		ht.put(cd, ivp);
        			}
        		}
        		((TablaVByValRecursosMemoria)tablaVB).getTablaValores().add(ht);
        	}        
        }
            
 
        
        GregorianCalendar instanteInicial = new GregorianCalendar();
        instanteInicial.set(GregorianCalendar.YEAR, lecVagua.getAnini());        
        instanteInicial.set(GregorianCalendar.DAY_OF_YEAR, 1);          
        instanteInicial.set(GregorianCalendar.HOUR, 0);  
        
        
        resOptim.setTablaValores(tablaVB);
        
        return resOptim;
	}
	
	
	public static void main(String[] args){
		
		DatosResOptimIncrementos droi = new DatosResOptimIncrementos();
		droi.setNombre("Prueba de cargador EDF");
		droi.setRuta("G:/PLA/Pla_datos/Archivos/ModeloOp/ValaguaEDF");
		droi.setTipoSoporte(" ");
		ResOptimIncrementos resOptim = CargadorResOptimSIMSEE.devuelveResOptimIncrementos(droi);
		System.out.println("Cargó el ResOptim a partir de EDF ");
		TablaVByValRecursosMemoria tabla = (TablaVByValRecursosMemoria)resOptim.getTablaValores();
		int paso = 10;		
		int codigoEstadoHidro = 2;
		int[] codigos = new int[2];  
		codigos[0]=codigoEstadoHidro;
		System.out.println("Salen derivadas para paso de tiempo 10 empezando de cero, eshy = 2 empezando de cero");
		for(int ips = 0; ips<10; ips++){
			codigos[1] = ips;
			ClaveDiscreta clave = new ClaveDiscreta(codigos);		
			InformacionValorPunto ivp = tabla.getTablaValores().get(paso).get(clave);
			System.out.println(ivp.getDerivadasParciales()[0]);	
			
		}
	}

}
