/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEVAR is part of MOP.
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
import datatypesProcEstocasticos.DatosPEVAR;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesProcEstocasticos.DatosTransformaciones;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LeerDatosArchivo;

public class CargadorPEVAR {
	
	public static DatosPEVAR devuelveDatosPEVar(DatosProcesoEstocastico dpe){
		
		DatosPEVAR dat = new DatosPEVAR();
		//String ruta = dpe.getRuta();
		String ruta = "./resources/" + dpe.getNombre();
		DatosGeneralesPE datGen = persistencia.CargadorDatosGeneralesPE.devuelveDatosGeneralesPE(ruta, dpe.getNombre());
		dat.setDatGen(datGen);
		int cantVariables = datGen.getCantVariables();
		dat.setNVA(cantVariables);
				
		// Lee los datos de parámetros del modelo
		String archPar = ruta + "/parametros.txt";   
	    ArrayList<ArrayList<String>> texto;
	    texto = LeerDatosArchivo.getDatos(archPar);	
		AsistenteLectorEscritorTextos lector = new AsistenteLectorEscritorTextos(texto, archPar); 
		int i=0;	
		String formaEstimacion = lector.cargaPalabra(i, "FORMA_ESTIMACION");
		dat.setFormaEstimacion(formaEstimacion);
		i++;
		
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
				//pp.matarServidores();
			}
    		if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
    		System.exit(1);
    	}
    	i++;    	   	
		
    	ArrayList<String> nomLeidosExo = lector.cargaLista(i, "NOMBRES_VAR_EXOGENAS");
    	int cantExo = nomLeidosExo.size();
    	if(!nomLeidosExo.equals(datGen.getNombresVAExogenas())){
    		System.out.println("Error en nombres de variables exógenas del proceso " + dpe.getNombre());
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
    	
    	// carga las matrices M de medias móviles
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
    	
    	// carga los vectores de efectos de variables exógenas
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
		B = lector.cargaMatriz(i, cantVariables, dat.getCantInno());
		dat.setB(B);	
		i = i + cantVariables;

			
		// carga la matriz D diagonal de valores propios para generar u(t) a partir de e(t) distribuido N(0, I)
    	double[][] D = new double[cantVariables][dat.getCantInno()];
		lector.verificaEtiqueta(i, "D");
		i++;
		D = lector.cargaMatriz(i, cantVariables, dat.getCantInno());
		dat.setD(D);	
		i = i + cantVariables;
		
		
		// carga las cotas superiores e inferiores para la generación de realizaciones		
		dat.setCotaInfRealiz(new Hashtable<String, Double>());
		dat.setCotaSupRealiz(new Hashtable<String, Double>());
		for(int j=0; j<cantVariables; j++){
			ArrayList<String> aux = lector.cargaLista(i,"COTAS_REALIZACIONES");
			String nl = nomLeidos.get(j);
			if(!aux.get(0).equalsIgnoreCase(nl)){
				System.out.println("Error de nombres de series en la lectura de archivo " + archPar);
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
				System.exit(0);
			}
			dat.getCotaInfRealiz().put(nl, Double.parseDouble(aux.get(1)));
			dat.getCotaSupRealiz().put(nl, Double.parseDouble(aux.get(2)));
			i++;
		}
				
		// Si el proceso se usa en la optimización lee los datos de la discretización de las variables de estado
		// y las matrices para obtener esperanza y varianza condicional de estados dadas los valores de las variables agregadas
		if(datGen.isUsoOptimizacion()){
			DatosDiscretizacionesVEPE dDis = CargadorDiscretizacionesVEPE.devuelveDatosDiscretizacionesVEPE(ruta);
			dat.setDatDis(dDis);
			
			String archDistCond = ruta + "/matricesDistCondicionada.txt";   
		    texto = LeerDatosArchivo.getDatos(archDistCond);	
			lector = new AsistenteLectorEscritorTextos(texto, archDistCond); 
			i=0;
			
			// lee matriz vector de esperanza condicional
			lector.verificaEtiqueta(i, "MAT_ESP_COND");
			i++;
			int nfil = dat.getNVA()*dat.getNA()+dat.getNEx();
			int ncol = dat.getDatGen().getNombresVarsEstado().size();
			dat.setMatrizEspCondicional(lector.cargaMatriz(i, nfil, ncol));
			i = i+nfil;
			
			// lee matriz de varianza condicional
			lector.verificaEtiqueta(i, "MAT_VAR_COND");
			i++;
			ncol = dat.getNVA()*dat.getNA()+dat.getNEx();
			dat.setMatrizVarCondicional(lector.cargaMatriz(i, nfil, ncol));
			i = i+nfil;	
			
			// lee matriz de valores propios de la matriz de varianza condicional
			lector.verificaEtiqueta(i, "MAT_VALPROP");
			i++;
			dat.setdVC(lector.cargaMatriz(i, nfil, ncol));
			i = i+nfil;				

			// lee matriz de valores propios corregidos de la matriz de varianza condicional
			lector.verificaEtiqueta(i, "MAT_VALPROP_C");
			i++;
			dat.setdVCC(lector.cargaMatriz(i, nfil, ncol));
			i = i+nfil;			
			
			// lee matriz de vectores propios de la matriz de varianza condicional
			lector.verificaEtiqueta(i, "MAT_VECPROP");
			i++;
			dat.setbVC(lector.cargaMatriz(i, nfil, ncol));			
			
		}			
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
		DatosPEVAR dat = devuelveDatosPEVar(dpe);
		
		System.out.println("Terminó la prueba");

		
		
		
		
		
	}

}
