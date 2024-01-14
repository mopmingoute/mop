/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorTextosGeneralesPE is part of MOP.
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

import procesosEstocasticos.Serie;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;


/**
 * Clase con métodos que escriben textos para ser levantados por cargadores
 * para construir procesos estocásticos
 * @author ut469262
 */
public class EscritorTextosGeneralesPE {
	
	
	/**
	 * Escribe un texto que puede ser levantado por el CargadorAgregadorLineal
	 */
	public static void escribeTextoAgregadorLineal(String dirSimul, String nombreEstimacion,
			String nombreProcSimul, String nombreProcOptim, String[] nombresVESimul,
			String[] nombresVExo, String[] nombresVEOpt, double[][] matAg){
        String dirAg = dirSimul + "/agregadorEstados.txt";
        boolean existe = DirectoriosYArchivos.existeArchivo(dirAg);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirAg);
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("TIPO_AGREGADOR LINEAL \n");
        sb.append("NOMBRE_ESTIMACION  " + nombreEstimacion +  "\n");
        sb.append("PROCESO_SIMULACION  " + nombreProcSimul +  "\n");
        sb.append("PROCESO_OPTIMIZACION  " + nombreProcOptim +  "\n");
        sb.append("VARS_ESTADO_SIMULACION ");
        for(String s: nombresVESimul){
        	sb.append(s + "  ");       	
        }
        sb.append("\n");    
        sb.append("VARS_EXOGENAS ");
        for(String s: nombresVExo){
        	sb.append(s + "  ");       	
        }
        sb.append("\n"); 
        sb.append("VARS_ESTADO_OPTIMIZACION ");
        for(String s: nombresVEOpt){
        	sb.append(s + "  ");       	
        }
        sb.append("\n");
        sb.append("MATRIZ_POR_FILAS\n"); 
        
        AsistenteLectorEscritorTextos esc = new AsistenteLectorEscritorTextos();
        sb.append(esc.escribeMatrizReal(matAg," "));

        DirectoriosYArchivos.agregaTexto(dirAg, sb.toString());    		
	}
	
	   /**
     *  Genera el archivo de texto DatosSal.xlt que puede ser leido por CargadorPEHistorico
     *  con los datos de series y sus estados para cada tiempo t con datos:
     *  -cronica
     *  -estacion
     *  -valor de las variables aleatorias
     *  -valor de las VE 
     *  
     *  
     */
	public static void escribeDatosPEHistorico(String identificadorEstimacion, 
			String dirPHist, int cantCron, int cantDatos, ArrayList<Serie> variablesAleatorias,
			ArrayList<Serie> varsEstado){
		
        System.out.println("COMIENZA IMPRESION DE TEXTOS PARA DATATYPE");     
        
        int cantSeries = variablesAleatorias.size();
        int cantVE = varsEstado.size();
        String[] nombresSeries = new String[cantSeries];
        String[] nombresVE = new String[cantVE];
        for(int is=0; is<cantSeries; is++){
        	nombresSeries[is] = variablesAleatorias.get(is).getNombre();
        }
        for(int is=0; is<cantVE; is++){
        	nombresVE[is] = varsEstado.get(is).getNombre();
        }   
        int[] anios = variablesAleatorias.get(0).getAnio();
        int[] pasos = variablesAleatorias.get(0).getPaso();
        
        String dirDatos = dirPHist + "/datosProcHistorico.xlt";
        boolean existe = DirectoriosYArchivos.existeArchivo(dirDatos);
        if(existe) DirectoriosYArchivos.eliminaArchivo(dirDatos);
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(identificadorEstimacion + "\n");
        
        sb.append("cantSeries" + " " + cantSeries + "\n");

        sb.append("cantVarEst " + " " +  cantVE + "\n");

        
        for(int ive=0; ive<cantVE; ive++){
        	 sb.append("VARIABLE_ESTADO  " + nombresVE[ive] + " ");
        	 sb.append("cantValoresVE ");
        	 sb.append("-1 ");  // estos datos no se usan en el ProcesoHistorico se mantienen por compatibilidad
        }
        sb.append("\n");
        sb.append("cantCron" + " " + cantCron + "\n");
        sb.append("nombreDurPaso" + " " + "SEMANA" + "\n");  // nombre de la duración del paso

        sb.append("cantDatos" + " " + cantDatos+ "\n");  // cantidad de datos 

        sb.append("Cronica ");
        sb.append("Estacion ");               
        for(int is=0; is<cantSeries; is++){
            sb.append(nombresSeries[is]); 
            sb.append(" ");                                      
        }
        for(int ive=0; ive<cantVE; ive++){
            sb.append(nombresVE[ive] + " ");                               
        }  
        sb.append("\n");           

        for(int t=0; t<cantDatos; t++){       
            sb.append(anios[t]);
            sb.append(" "); 
            sb.append(pasos[t]);
            sb.append(" ");             
            for(int is=0; is<cantSeries; is++){
            	String ns = nombresSeries[is];
                sb.append(variablesAleatorias.get(is).getDatos()[t]);            
                sb.append(" ");                            
            }
            for(int ive=0; ive<cantVE; ive++){
            	String nve = nombresVE[ive];
                sb.append(varsEstado.get(ive).getDatos()[t]);
                sb.append(" ");                                
            }            
            sb.append("\n");     
        }
        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());	
	}
	
	
	public static void escribeDatosGeneralesPE(String directorio, String nombreEstimacion,
			boolean usoSim, boolean usoOpt, String procAsociadoOptim, boolean usoTransformaciones,
			String[] nombresVariables, String[] nombresVE, boolean usaVEenOptim, boolean discretoExaustivo,
			boolean tieneVEContinuas, int prioridadSorteos, String nombrePaso, boolean tieneVarsExo,
			String[] nombresVarExo, String[] nombresProcesosExo){
		String nomArch = directorio + "/datosGenerales.txt";
		StringBuilder sb = new StringBuilder();
		
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos();
		sb.append(ale.escribeEtiqYString("NOMBRE_ESTIMACION", nombreEstimacion," "));
		sb.append(ale.escribeEtiqYSiNo("USO_SIMULACION", usoSim," "));
		sb.append(ale.escribeEtiqYSiNo("USO_OPTIMIZACION", usoOpt," "));
		sb.append(ale.escribeEtiqYString("PROC_ASOCIADO_EN_OPTIM", procAsociadoOptim," "));
		sb.append(ale.escribeEtiqYSiNo("USA_TRANSFORMACIONES", usoTransformaciones," "));
		sb.append(ale.escribeEtiqYLista("NOMBRES_VARIABLES", nombresVariables," "));
		sb.append(ale.escribeEtiqYLista("NOMBRES_VARS_ESTADO", nombresVE," "));
		sb.append(ale.escribeEtiqYSiNo("USA_VARS_ESTADO_EN_OPTIM", usaVEenOptim," "));
		sb.append(ale.escribeEtiqYSiNo("DISCRETO_EXHAUSTIVO", discretoExaustivo," "));
		sb.append(ale.escribeEtiqYSiNo("TIENE_VE_CONTINUAS", tieneVEContinuas," "));
		sb.append(ale.escribeEtiqYEntero("PRIORIDAD_SORTEOS", prioridadSorteos," "));
		sb.append(ale.escribeEtiqYString("NOMBRE_PASO", nombrePaso," "));
		sb.append(ale.escribeEtiqYSiNo("TIENE_VARS_EXOGENAS", tieneVarsExo," "));
		sb.append(ale.escribeEtiqYLista("NOMBRES_VAR_EXOGENAS", nombresVarExo," "));
		sb.append(ale.escribeEtiqYLista("NOMBRES_PROCESOS_EXOGENAS", nombresProcesosExo," "));
		
		if(DirectoriosYArchivos.existeArchivo(nomArch)) DirectoriosYArchivos.eliminaArchivo(nomArch);
		DirectoriosYArchivos.grabaTexto(nomArch, sb.toString());
	}
	
	/**
	 * Escribe un texto que puede ser levantado por el CargadorDiscretizacion
	 * se escriben los valores de la discretización, no los percentiles
	 * En el directorio direc se graba un archivo discretizaciones.txt
	 */
	public static void escribeTextoDiscretizacionVE(String direc, String[] nombresVE, double[] min, double[] max,
			int[] cantPuntos, boolean[] equiespaciada, double[][] valores){		
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos();
		StringBuilder sb = new StringBuilder();
		sb.append(ale.escribeEtiqYLista("NOMBRES_VE_CONTINUAS", nombresVE, " "));		
		for(int ive=0; ive<nombresVE.length; ive++){
			sb.append(nombresVE[ive] + "\n");		
			sb.append(ale.escribeEtiqYDouble("MINIMO", min[ive], " "));
			sb.append(ale.escribeEtiqYDouble("MAXIMO", max[ive], " "));
			sb.append(ale.escribeEtiqYEntero("CANT_PUNTOS", cantPuntos[ive], " "));
			sb.append(ale.escribeEtiqYSiNo("EQUIESPACIADA",equiespaciada[ive], " "));
			sb.append(ale.escribeEtiqYListaReales("VALORES", valores[ive], " "));
		}
		String nomArch = direc + "/discretizaciones.txt";
		if(DirectoriosYArchivos.existeArchivo(nomArch)) DirectoriosYArchivos.eliminaArchivo(nomArch);
		DirectoriosYArchivos.grabaTexto(nomArch, sb.toString());
	
	}
	
	/**
	 * Escribe en el directorio direc un texto que puede ser levantado por CargadorTransformaciones
	 * @param direc directorio donde se escribe el texto.
	 * @param nombresSeries lista con los nombres de las series a las que se aplican las transformaciones
	 * @param tipoTrans : tipo de transformación
	 * @param parametros : clave nombre de la serie, valor primer indice paso de tiempo, 
	 * 					segundo indice recorre los parámetros
	 */
	public static void escribeTransformaciones(String direc, String nombreEstimacion, 
			 Hashtable<String, String> tipoTrans, String[] nombresSeries, String nombrePaso, int cantPasos,
			Hashtable<String, ArrayList<ArrayList<Double>>> parametros){
		
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos();
		StringBuilder sb = new StringBuilder();
		sb.append(ale.escribeEtiqYString("NOMBRE_ESTIMACION", nombreEstimacion," "));

		sb.append(ale.escribeEtiqYLista("NOMBRES_SERIES", nombresSeries," "));
		String[] tipoT = new String[nombresSeries.length];
		for(int is=0; is<nombresSeries.length; is++){
			tipoT[is] = tipoTrans.get(nombresSeries[is]);
		}
		sb.append(ale.escribeEtiqYLista("TIPO_TRANSFORMACIONES", tipoT," "));
		sb.append(ale.escribeEtiqYString("PASO_DEL_PROCESO", nombrePaso," "));
		sb.append(ale.escribeEtiqYEntero("CANT_PASOS", cantPasos," "));
		sb.append("// EMPIEZAN LAS ENTRADAS ESPECIFICAS DEL TIPO DE TRANSFORMACION\n");

		for(int is =0; is <nombresSeries.length; is++){
			String ns = nombresSeries[is];
			sb.append(ns + "\n");
			ArrayList<ArrayList<Double>> p1 = parametros.get(nombresSeries[is]);			
			if(tipoTrans.get(ns).equalsIgnoreCase(utilitarios.Constantes.BOXCOX)){
				sb.append(ale.escribeEtiqYEntero("CANT_PARAMETROS", 4," "));
				sb.append("//LAMBDA  MEDIA  DESVÍO   TRASLACION");
			}else if(tipoTrans.get(ns).equalsIgnoreCase(utilitarios.Constantes.NQT)){
				sb.append(ale.escribeEtiqYString("CANT_PARAMETROS", "  VARIABLE POR PASO"," "));
				sb.append("OBSERVACIONES DEL PASO\n");
			}
			for(int ipa=0; ipa<cantPasos; ipa++){
				for(int ipr=0; ipr<p1.get(ipa).size(); ipr++){
					sb.append(p1.get(ipa).get(ipr) + " ");				
				}
				sb.append("\n");
			}
		}
	
		String nomArch = direc + "/transformaciones.txt";
		if(DirectoriosYArchivos.existeArchivo(nomArch)) DirectoriosYArchivos.eliminaArchivo(nomArch);
		DirectoriosYArchivos.agregaTexto(nomArch, sb.toString());

		
	}

}
