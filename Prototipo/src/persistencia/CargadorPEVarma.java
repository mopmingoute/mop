/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEVarma is part of MOP.
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
import datatypesProcEstocasticos.DatosDiscretizacionesVEPE;
import datatypesProcEstocasticos.DatosGeneralesPE;
import datatypesProcEstocasticos.DatosPEBootstrapDiscreto;
import datatypesProcEstocasticos.DatosPEVarma;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesProcEstocasticos.DatosTransformaciones;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import utilitarios.AsistenteLectorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LeerDatosArchivo;

public class CargadorPEVarma {
	
	public static DatosPEVarma devuelveDatosPEVarma(DatosProcesoEstocastico dpe){
		
		DatosPEVarma dat = new DatosPEVarma();
		//String ruta = dpe.getRuta();
		String ruta = "./resources/" + dpe.getNombre();
		DatosGeneralesPE datGen = persistencia.CargadorDatosGeneralesPE.devuelveDatosGeneralesPE(ruta, dpe.getNombre());
		dat.setDatGen(datGen);
		int cantVariables = datGen.getCantVariables();
				
		// Lee los datos de par�metros del modelo
		String archPar = ruta + "/parametros.txt";   
	    ArrayList<ArrayList<String>> texto;
	    texto = LeerDatosArchivo.getDatos(archPar);	
		AsistenteLectorTextos lector = new AsistenteLectorTextos(texto, archPar); 
		int i=0;	
		
    	int cantVariablesLeida = lector.cargaEntero(i, "CANT_VARIABLES");    	
    	i++;
    	
    	if(cantVariablesLeida!=cantVariables){
    		System.out.println("Cantidad de variables no coincide en proceso " + dpe.getNombre());
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(0);
    	}
    	
    	dat.setNA(lector.cargaEntero(i, "CANT_REZAGOS_AR"));    
    	i++;
		
    	dat.setNM(lector.cargaEntero(i, "CANT_REZAGOS_MA"));    
    	i++;    	

    	dat.setCantInno(lector.cargaEntero(i, "CANT_INNOVACIONES"));    
    	i++;        	
    	
    	ArrayList<String> nomLeidos = lector.cargaLista(i, "NOMBRES_SERIES");
    	if(!nomLeidos.equals(datGen.getNombresVariables())){
    		System.out.println("Error en nombres de variables del proceso " + dpe.getNombre());
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
    		System.exit(1);
    	}
    	i++;    	   	
		
    	nomLeidos = lector.cargaLista(i, "NOMBRES_VAR_EXOGENAS");
    	int cantExo = nomLeidos.size();
    	if(!nomLeidos.equals(datGen.getNombresVAExogenas())){
    		System.out.println("Error en nombres de variables ex�genas del proceso " + dpe.getNombre());
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(1);
    	}
    	i++;    
    	
    	// carga las matrices A autoregresivas
    	double[][][] A = new double[dat.getNA()][][];
    	for(int p = 0; p<dat.getNA(); p++){
    		String etiqueta = "A" + (p+1);
    		lector.verificaEtiqueta(i, etiqueta);
    		i++;
    		A[p] = lector.cargaMatriz(i, cantVariables, cantVariables);
    		i = i + cantVariables;
    	}
    	dat.setA(A);
    	
    	// carga las matrices M de medias m�viles
    	// tienen tantas columnas como innovaciones es decir residuos
    	double[][][] M = new double[dat.getNM()][][];    	
    	for(int p = 0; p<dat.getNM(); p++){
    		String etiqueta = "M" + (p+1);
    		lector.verificaEtiqueta(i, etiqueta);
    		i++;
    		M[p] = lector.cargaMatriz(i, cantVariables, dat.getCantInno());
    		i = i + cantVariables;
    	} 
    	dat.setM(M);
    	
    	// carga los vectores de efectos de variables ex�genas
    	double[][] X = new double[cantExo][cantVariables];
    	for(int ive=0; ive<cantExo; ive++){
    		lector.verificaEtiqueta(i, dat.getDatGen().getNombresVAExogenas().get(ive));
    		i++;
    		double[] aux = new double[cantVariables];
    		aux = lector.cargaVector(i, cantVariables);
    		X[ive] = aux;
    		i++;
    	}
		dat.setX(X);
			
    	
		// carga la matriz B de vectores propios para generar u(t) a partir de e(t) distribuido N(0, I)
    	double[][] B = new double[cantVariables][dat.getCantInno()];
		lector.verificaEtiqueta(i, "B");
		i++;
		lector.cargaMatriz(i, cantVariables, dat.getCantInno());
		i = i + cantVariables;
			
			
		// carga la matriz D diagonal de valores propios para generar u(t) a partir de e(t) distribuido N(0, I)
    	double[][] D = new double[cantVariables][dat.getCantInno()];
		lector.verificaEtiqueta(i, "D");
		i++;
		lector.cargaMatriz(i, cantVariables, dat.getCantInno());
		i = i + cantVariables;			
			
			
  

		

		
		return dat;
	
	}
	
	public static void main(String[] args){
		
		String nombre = "varmaAportes";
		String tipo = "VARMA";
		String tipoSoporte = null; 
		boolean discretoExhaustivo = false;
		boolean muestreado = true;
		Hashtable<String, Double> estadosIniciales = null;
		String ruta = "D:/Proyectos/ModeloOp/resources/varmaAportes";		
		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico(nombre, tipo, tipoSoporte, ruta, discretoExhaustivo, muestreado, estadosIniciales, null);
		DatosPEVarma dat = devuelveDatosPEVarma(dpe);
		
		System.out.println("Termin� la prueba");

		
		
		
		
		
	}

}
