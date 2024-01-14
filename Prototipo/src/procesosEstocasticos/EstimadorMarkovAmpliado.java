/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimadorMarkovAmpliado is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.EnumeradorLexicografico;
import utilitarios.LectorDireccionArchivoDirectorio;
import utilitarios.LeerDatosArchivo;

import matrices.Oper;



	/**
	 * Estima procesos de Markov-DE con matrices de transición (discretos exhaustivos)
	 * y procesos de Markov-MON para ser usados con sorteos Montecarlo (guardando correlación entre
	 * variable aleatoria y transición) 
	 * @author ut469262
	 * 
	 * Markov-DE  se estima una matriz de transición entre clases para cada paso de tiempo (semana)y un conjunto
	 * de observaciones de las VA para cada estado. Para eso se toman los datos históricos del propio paso y de
	 * una ventana de pasos hacia adelante y hacia atrás (con un orden circular).
	 * Así por ejemplo si ventana = 2, y se está estimando el paso semanal 20, se toman los pasos
	 * semanales 18, 19, 20, 21 y 22. Para el paso semanal 1, se toman las semanas 51, 52, 1, 2, 3
	 * 
	 * Markov-MON se estima con el mismo criterio de ventana, para cada paso de tiempo, y para cada estado 
	 * un conjunto de observaciones de las VA y el estado al cual se transita cuando ocurren esas observaciones.
	 * 
	 * A cada tiempo de la serie histórica se asigna un valor de las variables de estado continuas
	 * y de su discretización. 
	 * 
	 * Cada variable de estado (VE) continua da lugar a una clasificación en clases numeradas empezando en 0
	 * A cada dato de tiempo se le atribuye un estado compuesto según los valores de las clases para cada VE.
	 * Los estados compuestos se numeran a partir del 0
	 * Así por ejemplo si hay dos VE con estados {0,1,2} y {0,1}
	 * Los estados compuestos son: 
	 * Estado 0: (0, 0), Estado 1: (1, 1), Estado 2 (1, 0) ........
	 * 
	 *
	 */
	public class EstimadorMarkovAmpliado {
		
		private String identificadorEstimacion;         
	    private int cantSeries;    
	    private String[] nombresSeries;
	    private String[] nombresVE; // nombres de las variables de estado
	    private int cantMaxPasos; // cantidad máxima de pasos que puede tener un año
	    private String nombreDurPaso; // SEMANA, DIA, HORA, ETC.
	    private int cantVE;   // cantidad de variables de estado
	    
	    /**
	     * Radio del entorno de pasos que se consideran de la misma poblacion para obtener
	     * las matrices de transicion y observaciones representativas
	     * Ejemplo: si radioEntorno =1, para el paso n se toman las observaciones de los
	     * pasos n-1, n y n+1 como pertenecientes a la misma poblacion.
	     */
	    private int radioEntorno; 
	    
	    /**
	     * Coeficientes de los datos para crear la variable de estado.
	     * Primer índice: variable de estado
	     * Segundo índice: serie de datos
	     * Tercer índice: rezago en la serie empezando de 0, -1, -2,....
	     * Define la combinación lineal de datos rezagados que determina cada estado.
	     */
	    private double[][][] defVarEst;  
	    
	    /**
		 * Los datos para estimar. En primer óndice el tiempo,
		 * en el segundo óndice el ordinal de la serie.
		 */
//	    private double[][] datos;  
	    
	    /**
	     * Etiqueta entera de cada crónica, en el orden en que aparecen las crónicas:
	     * Ejemplo: 1909, 1910, .....2013
	     */
	    private int[] etiquetaCron;
	    
	    /**
	     * EtiquetasCronDatos
	     * La etiqueta asociada a cada dato t
	     */
	    private int[] etiquetaCronDatos;
	    
	    /**
	     * Cantidad de crónicas leídas
	     */
	    private int cantCron;   
	    
		private int cantDatos;
		
	    /**
	     * Para cada tiempo indica el paso (entero entre 1 y la cantidad móxima de pasos en el aóo)
	     */
	    private int[] pasos;
		
	    /**
	     * Para cada tiempo indica el año
	     */
	    private int[] anios;
	    
	    private String nombrePaso;
		
//		   /**
//	     * Valores de las variables de estado continuas que se calculan.
//	     * Primer óndice tiempo, segundo óndice variable de estado.
//	     */
//	    private double[][] valVarEst;  
	    
	    /**
	     * Probabilidades de cada clase para Variable de estado definida.
	     * Primer indice variable de estado, segundo óndice clase.
	     * Las clases se numeran del 0 en adelante.
	     * Puede haber diferente cantidad de clases en cada serie.
	     */
	    private double[][] probCla;      
	        
	    /**
	     * Cantidad de clases de cada variable de estado
	     */
	    private int[] cantCla;     
	    
	    /**
	     * Límite superior de cada clase para cada variable de estado y estación    
	     * Primer óndice variable de estado, segundo óndice estación, tercer índice clase-1.
	     * Las observaciones de la clase k de la variable i en la estación e
	     * son menores o iguales a limSupVE[e][i][k]
	     * Es igual al promedio de la mayor observación de la clase inferior 
	     * y la menor observación de la clase superior 
	     */
	    private double[][][] limSupVE;
	    

	    
	    /**
	     * Para cada serie, estación y estado compuesto, 
	     * guarda la media de la serie en la estación y estado compuesto
	     * en un array
	     * Primer índice estación, segundo óndice serie, tercer indice estado compuesto.
	     */
	    private double[][][] medias;
	    
	    /**
	     * Para cada paso del año y serie, guarda la media, es decir no condicionada 
	     * a ningón estado.
	     * Primer óndice estación, segundo óndice serie
	     */
	    private double[][] mediaAbs;
	    
		

	    private Hashtable<String, Serie> clases; // para cada nombre de VE su clase discreta empezando de 0
	    
	    private int[][] cantObsPasoEstadoComp;
	    
	    /**
	     * Límites superiores de valores de las clases de cada VE
	     * primer índice VE
	     * segundo índice paso empezando en 1
	     * tercer índice clase
	     */
	    private double[][][] cuantiles; 
	    
	    
	    /**
	     * El estado numerado a partir de 0 calculado a partir de las clases de las VE.
	     */
	    private Serie estadoCompuesto; 
	    
	     
	    
	    private int cantEstComp;
	    
	    /**
	     * Hash map de matrices de probabilidades de transición
	     * La clave es el paso inicial de la transición
	     * 
	     * El orden de filas y columnas en las matrices de transición es
	     * el de los estados compuestos.
	     */
	    private Hashtable<Integer, MatPTrans> matricesT;	  	    
	    
	    /**
	     * Para cada paso del año las observaciones de la serie
	     * primer índice paso del año
	     * segundo índice recorre los estados compuestos
	     * tercer índice recorre las observaciones
	     */
	    private ArrayList<ArrayList<ArrayList<Observacion>>> observaciones;
	    

	    
	    
	    /**
	     * Cantidad de rezagos (incluso el valor corriente) en la definición de las VE
	     * primer índice VE, segundo índice serie
	     */
	    private int[][] cantRezagos;   
	    
	    
	    /**
	     * True si la serie se estandariza 0-1 para crear la respectiva VE
	     * primer índice VE, segundo índice serie
	     */
	    private boolean[][] estandarizada;   
	    
	    private Hashtable<String, Serie> seriesLeidas; // las series originales, clave el nombre de serie original
	    private Hashtable<String, Serie> series01;  // las series estandarizadas 0,1,  , clave el nombre de serie original
	    private Hashtable<String, Serie> series;  // las series a emplear para hallar cada VE, clave el nombre de serie original
	    	    
	    private Hashtable<String, Serie> varsEstado; // para cada nombre de VE, la serie de sus valores continuos	    
	    
	    /**
	     * Los nombres de los grupos de series de cada VE
	     * Clave nombre de VE
	     * Valor lista de nombres de grupos de la VE
	     */
	    private Hashtable<String, ArrayList<String>> gruposDeVE;
	    
	    /**
	     * Nombre del grupo al que pertenece cada serie en cada VE.
	     * Clave: String con nombre de VE + nombre de serie
	     * Valor: nombre del grupo
	     * No puede haber el mismo nombre de grupo en dos VE distintas
	     * Dentro de un grupo de series, cada una tiene un ponderador, para formar una combinación
	     * lineal a partir de los valores de la serie.
	     * El valor resultante de cada grupo se estandariza 0,1 y se pondera por el
	     * respectivo ponderador grupo
	     */
	    private Hashtable<String, String> grupoDeVESerie;
	    
	  
	    /**
	     * Operador para combinar los valores de los grupos dentro de una VE
	     * puede ser COMBINACION_LINEAL, MINIMO, etc.
	     * Clave: nombre de VE
	     * Valor: constante del operador
	     */
	    private Hashtable<String, String> operadorEnVE;
	    
	    /**
	     * Ponderador de un grupo dentro de la VE si el operador es COMBINACION_LINEAL
	     * Clave: String con nombre de VE + nombre del grupo
	     * Valor: ponderador del grupo dentro de la VE
	     */
	    private Hashtable<String, Double> ponderadorDeGruposEnVE;
	    
	    
	    
	    /**
	     * Lee las series y carga datos principales
	     */
	    public void cargaTodo(String dirArchivo){
	    	ConjuntoDeSeries conjSeriesLeido = MetodosSeries.leeConjuntoDeSeries(dirArchivo +"/datos.txt");
	    	seriesLeidas = conjSeriesLeido.getSeries();
	    	nombreDurPaso = conjSeriesLeido.getNombrePaso();
	    	cantCron = conjSeriesLeido.getCantCron();
	    	nombresSeries = conjSeriesLeido.getNombresSeries();
	    	cantSeries = seriesLeidas.values().size();
	    	cantDatos = seriesLeidas.get(nombresSeries[0]).getDatos().length;
	    	anios = seriesLeidas.get(nombresSeries[0]).getAnio();
	    	pasos = seriesLeidas.get(nombresSeries[0]).getPaso();
	    	cantMaxPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombreDurPaso);
	    	nombrePaso = conjSeriesLeido.getNombrePaso();	
	    	series01 = new Hashtable<String, Serie>();
	    	series = new Hashtable<String, Serie>();
	    	mediaAbs = new double[cantMaxPasos][cantSeries];
	    	// Crea las series estandarizadas y calcula las medias
	    	for(int is=0; is<cantSeries; is++){
	    		String ns= nombresSeries[is];
	    		Serie sl = seriesLeidas.get(ns);
	    		Serie s = MetodosSeries.estandariza01(sl, 0.0);
	    		series01.put(ns, s);
	    		double[] ms = MetodosSeries.mediaPorPaso(sl);
	    		mediaAbs[is]=ms;
	    	}	    	    	
	    	cargaParamVEAmp2(dirArchivo +"/defVE.txt" );	   
	    }	    
	    	    
	    
	    /**
	     * Lee los parámetros de las variables de estado del archivo dirArchivo
	     * @param dirArchivo 
	     */
	    public void cargaParamVEAmp(String dirArchivo){
	        ArrayList<ArrayList<String>> texto;
	        texto = LeerDatosArchivo.getDatos(dirArchivo);
	        int i=0;
	        cantVE = Integer.parseInt(texto.get(i).get(1));
	        nombresVE = new String[cantVE];
	        defVarEst = new double[cantVE][cantSeries][];
	        cantRezagos = new int[cantVE][cantSeries];
	        estandarizada = new boolean[cantVE][cantSeries];
	        probCla = new double[cantVE][];  
	        cantCla = new int[cantVE];
	        double[] aux;
	        boolean[] auxb;
	        i++;
	        for(int ive=0; ive< cantVE; ive++){
	            nombresVE[ive]=texto.get(i).get(1);
	            i++;
	            for(int iserie = 0; iserie< cantSeries; iserie++){
	            	// verifica que cada serie aparezca en su orden
	            	if(!texto.get(i).get(1).equalsIgnoreCase(nombresSeries[iserie])){
	            		System.out.println("error en lectura de serie" + nombresSeries[iserie]);
	            		System.exit(1);
	            	}
	            	i++;
	            	double pondSerie = Double.parseDouble(texto.get(i).get(1));
	            	i++;
	            	auxb = new boolean[cantSeries];
	            	boolean b=false;
	            	if(texto.get(i).get(1).equals("SI")){
	            		b = true;
	            	}
	            	estandarizada[ive][iserie] = b;
	            	i++;
	            	int cr = Integer.parseInt(texto.get(i).get(1));
	            	cantRezagos[ive][iserie] = cr;
	            	i++;
		            aux = new double[cr];
		            defVarEst[ive][iserie]=aux;
		            for(int j=1; j<texto.get(i).size(); j++){
		                defVarEst[ive][iserie][j-1] = Double.parseDouble(texto.get(i).get(j))*pondSerie;
		            }
		            i++;	           
	            }
	            aux = new double[texto.get(i).size()-1];
	            probCla[ive]= aux;            
	            for(int j=1; j<texto.get(i).size(); j++){
	                probCla[ive][j-1] = Double.parseDouble(texto.get(i).get(j));
	            }   
	            cantCla[ive] = probCla[ive].length;
	            i++;            
	        }
	        System.out.println("TERMINO LA LECTURA DE PARAMETROS DE VE");
	        
	    }
	    	    

	    /**
	     * Lee los parámetros de las variables de estado del archivo dirArchivo
	     * incluyendo los grupos de series
	     * @param dirArchivo 
	     */
	    
	    public void cargaParamVEAmp2(String dirArchivo){
	        ArrayList<ArrayList<String>> texto;
	        texto = LeerDatosArchivo.getDatos(dirArchivo);
	        AsistenteLectorEscritorTextos lector = new AsistenteLectorEscritorTextos(texto, dirArchivo);	        

	        int i=0;
	        cantVE = lector.cargaEntero(i,"CANT_VE");
	        nombresVE = new String[cantVE];
	        defVarEst = new double[cantVE][cantSeries][];
	        cantRezagos = new int[cantVE][cantSeries];
	        estandarizada = new boolean[cantVE][cantSeries];
	        probCla = new double[cantVE][];  
	        cantCla = new int[cantVE];
	        gruposDeVE = new Hashtable<String, ArrayList<String>>();
	        grupoDeVESerie = new Hashtable<String, String>();
		    operadorEnVE = new Hashtable<String, String>();
		    ponderadorDeGruposEnVE = new Hashtable<String, Double>();	  	        
	        double[] aux;
	        boolean[] auxb;
	        i++;
	        radioEntorno = lector.cargaEntero(i, "RADIO_ENTORNO");
	        i++;
	        for(int ive=0; ive< cantVE; ive++){	        	
	            nombresVE[ive]=lector.cargaPalabra(i, "NOMBRE_VE");
	            i++;	            
            	ArrayList<String> auxG = lector.cargaLista(i, "GRUPOS");
            	gruposDeVE.put(nombresVE[ive], auxG);
            	i++;
            	String oper = lector.cargaPalabra(i, "OPERADOR_GRUPOS");
            	operadorEnVE.put(nombresVE[ive], oper);
            	i++;
            	if(oper.equalsIgnoreCase(utilitarios.Constantes.COMBINACION_LINEAL)){
            		ArrayList<Double> auxD = lector.cargaListaReales(i, "PONDERADORES_GRUPOS");
            		for(int g=0; g<auxD.size(); g++){
            			ponderadorDeGruposEnVE.put(nombresVE[ive] + auxG.get(g), auxD.get(g));
            		}
            	}
            	i++;            	
	            for(int iserie = 0; iserie< cantSeries; iserie++){
	            	// verifica que cada serie aparezca en su orden
	            	if(!texto.get(i).get(1).equalsIgnoreCase(nombresSeries[iserie])){
	            		System.out.println("error en lectura de serie" + nombresSeries[iserie]);
	            		System.exit(1);
	            	}
	            	i++;
	            	String sg = lector.cargaPalabra(i, "GRUPO");
	            	grupoDeVESerie.put(nombresVE[ive]+nombresSeries[iserie], sg);
	            	i++;

	            	double pondSerie = Double.parseDouble(texto.get(i).get(1));
	            	i++;
	            	auxb = new boolean[cantSeries];
	            	boolean b=false;
	            	if(texto.get(i).get(1).equals("SI")){
	            		b = true;
	            	}
	            	estandarizada[ive][iserie] = b;
	            	i++;
	            	int cr = Integer.parseInt(texto.get(i).get(1));
	            	cantRezagos[ive][iserie] = cr;
	            	i++;
		            aux = new double[cr];
		            defVarEst[ive][iserie]=aux;
		            for(int j=1; j<texto.get(i).size(); j++){
		                defVarEst[ive][iserie][j-1] = Double.parseDouble(texto.get(i).get(j))*pondSerie;
		            }
		            i++;	           
	            }
	            aux = new double[texto.get(i).size()-1];
	            probCla[ive]= aux;            
	            for(int j=1; j<texto.get(i).size(); j++){
	                probCla[ive][j-1] = Double.parseDouble(texto.get(i).get(j));
	            }   
	            cantCla[ive] = probCla[ive].length;
	            i++;            
	        }
	        System.out.println("TERMINO LA LECTURA DE PARAMETROS DE VE");
	        
	    }
	    	    	    
	    
	    /**
	     * Hace los cálculos
	     */
	    public void estimaMarkovAmpliado(){
	    	calculaVEContinuas();
	    	calculaClasesYEstadosCompuestos();
	    	calculaMatricesYObservaciones();	    	
	    }	    	    
	    
	    
	    /**
	     * Calcula los valores continuos de cada VE en las series de varsEstado
	     * Cada VE puede tener más de un grupo de series que contribuyen a ella
	     * La contribución de cada grupo se estandariza a 0,1 por paso del año, antes
	     * de ponderarse con el ponderador del grupo.
	     */
	    public void calculaVEContinuas(){
	    	varsEstado = new Hashtable<String, Serie>();
    		for(int ive=0; ive<cantVE; ive ++){	    			
    			series = new Hashtable<String, Serie>();
    			String nombreVE = nombresVE[ive];
	    		Serie sve = new Serie(nombreDurPaso);
	    		sve.setAnio(anios);
	    		sve.setPaso(pasos);
	    		sve.setDatos(new double[cantDatos]);    			
				if(operadorEnVE.get(nombreVE).equalsIgnoreCase(utilitarios.Constantes.COMBINACION_LINEAL)){
					sve.inicializa(0.0);
    			}else if(operadorEnVE.get(nombreVE).equalsIgnoreCase(utilitarios.Constantes.MINIMO)){
    				sve.inicializa(Double.MAX_VALUE);
    			}else{
    				System.out.println("Error en MarkovAmpliado en operador de variable de estado " + nombreVE);
    			}
    			ArrayList<String> grupos = gruposDeVE.get(nombreVE);	    			
    			for(String ng: grupos){
    				Serie sg = new Serie(ng, nombreDurPaso, new double[cantDatos], anios, pasos);		
	    			for(int is=0; is<cantSeries; is++){
	    				String nombreS = nombresSeries[is];
	    				if(grupoDeVESerie.get(nombreVE+nombreS).equalsIgnoreCase(ng)){
		    				if(estandarizada[ive][is]==true){
		    					series.put(nombreS, series01.get(nombreS));
		    				}else{
		    					series.put(nombreS, seriesLeidas.get(nombreS));
		    				}
    						for(int t=0; t<cantDatos; t++ ){
			    				for(int ir=0; ir< cantRezagos[ive][is]; ir++){
			    					if(t-ir >= 0){
			    						sg.getDatos()[t] += series.get(nombreS).getDatos()[t-ir]*defVarEst[ive][is][ir];
			    					}else{
			    						sg.getDatos()[t] += series.get(nombreS).getDatos()[t]*defVarEst[ive][is][ir];
			    					}
			    				}
    						}
	    				}	
	    			}
					if(operadorEnVE.get(nombreVE).equalsIgnoreCase(utilitarios.Constantes.COMBINACION_LINEAL)){
		    			double epsi = 0.0;
		    			sve = MetodosSeries.estandariza01(sve, epsi);
						double pondg = ponderadorDeGruposEnVE.get(nombreVE+ng);
						sve = MetodosSeries.combilin2S(sve, sg, 1.0, pondg);
	    			}else if(operadorEnVE.get(nombreVE).equalsIgnoreCase(utilitarios.Constantes.MINIMO)){
						sve = MetodosSeries.min2S(sve, sg);	    				
	    			}
	    		}


	    		varsEstado.put(nombreVE, sve);
	    	}
	    }	    	
	    	
	    /**
	     * Devuelve el estado compuesto empezando en 0 si el vector de clases
	     * de las VE es clases (con valores también empezando en 0)
	     * El dígito más significativo es clases[0], luego clases[1], etc.
	     * @param clases
	     * @return
	     */
	    public int estadoCompuesto(int[] clases){
	    	int ec = 0;
	    	for(int c=0; c<cantVE; c++){
	    		if(c<cantVE-1){
	    			ec += clases[c]*cantCla[c+1];
	    		}else{
	    			ec += clases[c];
	    		}
	    	}
	    	return ec;	    	
	    }
	    
	    
	    /**
	     * Devuelve las clases asociadas a un estado compuestos.
	     * El dígito más significativo es clases[0]
	     * Los estados compuestos se numeran a partir de 0
	     */
	    public int[] clasesDeEstadoComp(int ec){
	    	int valor = ec;
	    	int[] clases = new int[cantVE];
	    	for(int ic=cantVE-1; ic>=0; ic--){
	    		 clases[ic] = valor % cantCla[ic];
	    		 valor = valor / cantCla[ic];
	    	}
	    	return clases;
	    }
	    
	    
	    
	    	
	    /**
	     * Calcula clases y estados compuestos para cada tiempo
	     */
	    public void calculaClasesYEstadosCompuestos(){
	    	cuantiles = new double[cantVE][cantMaxPasos][];	    	
	    	clases = new Hashtable<String, Serie>();
	    	cantEstComp = 1;
	    	for(int ive=0; ive<cantVE; ive++){
	    		cantEstComp *= cantCla[ive];
	    		double[] probCuantiles = new double[cantCla[ive]]; 
	    		double probAcum = 0;
	    		for(int ic=0; ic<cantCla[ive]-1; ic++){	    			
	    			probAcum += probCla[ive][ic];
	    			probCuantiles[ic] = probAcum;
	    		}
	    		probCuantiles[cantCla[ive]-1] = 1.0;
	    		String nombreVE = nombresVE[ive];
	    		Serie sve = varsEstado.get(nombreVE);
	    		double[][] cuantiles1VE = MetodosSeries.calculaCuantiles(sve, probCuantiles);
	    		cuantiles[ive] = cuantiles1VE;
	    		Serie cve = new Serie(nombreDurPaso);
	    		clases.put(nombreVE, cve);
	    		cve.setAnio(sve.getAnio());
	    		cve.setPaso(sve.getPaso());
	    		cve.setDatos(new double[cantDatos]);
	    		for(int t=0; t<cantDatos; t++){
	    			int ic=0;
	    			while(cuantiles1VE[cve.getPaso()[t]-1][ic]<sve.getDatos()[t]){
	    				ic++;
	    			}
	    			cve.getDatos()[t]=ic;
	    		}
	    	}
	    	
	    	// Se terminó de calcular la clase de todas las VE
	    	// Ahora se calcula el estadoCompuesto
	    	estadoCompuesto = new Serie(nombreDurPaso);	    
	    	estadoCompuesto.setAnio(anios);
	    	estadoCompuesto.setPaso(pasos);  
	    	estadoCompuesto.setDatos(new double[cantDatos]);
	    	for(int t=0; t<cantDatos; t++){
	    		int[] auxClas = new int[cantVE];
	    		for(int ive=0; ive<cantVE; ive++){
	    			String nVE = nombresVE[ive];
	    			auxClas[ive] = (int)clases.get(nVE).getDatos()[t];
	    		}
	    		estadoCompuesto.getDatos()[t] = estadoCompuesto(auxClas);
	    	}	    	
	    	
	    	// Cálculo de las medias de las series originales por serie, paso y estado compuesto
	    	medias = new double[cantMaxPasos][cantSeries][cantEstComp];
	    	for(int is=0; is<cantSeries; is++){
	    		String ns = nombresSeries[is];
	    		Serie s = seriesLeidas.get(ns);
	    		double[] aux;
	    		for(int iec=0; iec<cantEstComp; iec++){
	    			aux = MetodosSeries.mediaPorPasoDadoOtra(s, estadoCompuesto, iec);
	    			for(int ip=0; ip<cantMaxPasos; ip++){
	    				medias[ip][is][iec]=aux[ip];
	    			}
	    		}
	    	}
	    	cantObsPasoEstadoComp = new int[cantMaxPasos][cantEstComp];
	    	for(int t=0; t<cantDatos; t++){
	    		cantObsPasoEstadoComp[estadoCompuesto.getPaso()[t]-1][(int)estadoCompuesto.getDatos()[t]]++;
	    	}
	    }
	    
	    
	    
	    /**
	     * Crea las matrices de transición entre estados compuestos 
	     * y las observaciones por paso y estado compuesto
	     */
	    public void calculaMatricesYObservaciones(){
	    	
	    	matricesT =  new Hashtable<Integer, MatPTrans>();
	    	Hashtable<Integer, MatPTrans> matricesTIni = new Hashtable<Integer, MatPTrans>();
	    	
	    	observaciones = new ArrayList<ArrayList<ArrayList<Observacion>>>();
	    	ArrayList<ArrayList<ArrayList<Observacion>>> observacionesIni = new ArrayList<ArrayList<ArrayList<Observacion>>>();
	    	
	    	// Crea las estructuras de matrices y observaciones	    	
	    	for(int ip=1; ip<=cantMaxPasos; ip++){
	    		int ipsig = estadoCompuesto.posteriorCircular(ip, 1);
	    		ArrayList<ArrayList<Observacion>> aux1 = new ArrayList<ArrayList<Observacion>>();
	    		ArrayList<ArrayList<Observacion>> aux1Ini = new ArrayList<ArrayList<Observacion>>();
	    		double[][] matIni = new double[cantEstComp][cantEstComp];
	    		matricesTIni.put(ip, new MatPTrans(ip, ipsig, matIni));
	    		double[][] mat = new double[cantEstComp][cantEstComp];
	    		matricesT.put(ip, new MatPTrans(ip, ipsig, matIni));
	    		for(int iec=0; iec<cantEstComp; iec++){
	    			ArrayList<Observacion> aux2 = new ArrayList<Observacion>(); 
	    			aux1.add(aux2);
	    			ArrayList<Observacion> aux2Ini = new ArrayList<Observacion>(); 
	    			aux1Ini.add(aux2Ini);	    			
	    		}
	    		observaciones.add(aux1);
	    		observacionesIni.add(aux1Ini);
	    	}

	    	// Recorre la serie de estadosCompuestos
	    	for(int t=0; t<cantDatos-1;t++){
	    		int ip = estadoCompuesto.pasoDeT(t);
	    		int ipsig = estadoCompuesto.posteriorCircular(ip, 1);
	    		int ep = (int)estadoCompuesto.getDatos()[t];
	    		int epsig = (int)estadoCompuesto.getDatos()[t+1];
	    		matricesTIni.get(ip).getProbs()[ep][epsig]+= 1.0;
	    		double[] valores = new double[cantSeries];
	    		for(int is=0; is<cantSeries; is++){
	    			String ns = nombresSeries[is];
	    			valores[is] = seriesLeidas.get(ns).getDatos()[t];
	    		}
	    		Observacion obs = new Observacion(valores, ep, epsig, anios[t], pasos[t]);
	    		observacionesIni.get(ip-1).get(ep).add(obs);
	    	}
	    	
	    	// Incorporación de los entornos al calculo de las matrices
	    	// Ejemplo: si radioEntorno = 1, se supone que a la poblacion del paso n
	    	// pertenecen las observaciones del paso n-1 y n+1 tambien.
	    	for(int ip=1; ip<= cantMaxPasos; ip++){
	    		double[][] ad = new double[cantEstComp][cantEstComp]; 
	    		for(int iv = -radioEntorno; iv<=radioEntorno; iv++){
	    			int ipiv = estadoCompuesto.avancePasoCircular(ip, iv); 
	    			ad = matrices.Oper.suma(ad, matricesTIni.get(ipiv).getProbs());	    		
	    		}	    		
	    		matricesT.get(ip).setProbs(matrices.Oper.normalizaFilas(ad));	  
	    	}
	    	
	    	// Incorporación de las radioEntornos a las observaciones
	    	for(int ip=1; ip<= cantMaxPasos; ip++){
	    		for(int iv = -radioEntorno; iv<=radioEntorno; iv++){
	    			int ipiv = estadoCompuesto.avancePasoCircular(ip, iv); 
	    			for(int iec=0; iec<cantEstComp; iec++){
	    				observaciones.get(ip-1).get(iec).addAll(observacionesIni.get(ipiv-1).get(iec));
	    			}
	    		}	 	    		  		
	    	}	
	    		    	
	    }
	    
	    

	    
	    public void imprimeTextosParaDatatypesAmp(String directorio){
   	    
            System.out.println("COMIENZA IMPRESION DE TEXTOS PARA DATATYPE");     
            
            /**
             *  Genera salida de datos de series y sus estados en DatosSal.xlt
             *  para cada tiempo t con datos:
             *  -cronica
             *  -estaci�n
             *  -valor de las series
             *  -valor de las VE (clases discretas)
             *  -valor de las VE (continuos)
             *  en DatosSal.xlt 
             */
                  
            Date fecha1 = new Date();
            
            String dirDatos = directorio + "/datosProcHistorico.xlt";
            boolean existe = DirectoriosYArchivos.existeArchivo(dirDatos);
            if(existe) DirectoriosYArchivos.eliminaArchivo(dirDatos);
            
            StringBuilder sb = new StringBuilder();
            sb.append(identificadorEstimacion + " " + fecha1.toString());
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());        
            
            sb = new StringBuilder();
            sb.append("cantSeries" + " " + cantSeries);
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
            
            sb = new StringBuilder();
            sb.append("cantVarEst " + " " +  cantVE);
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());   
            
            for(int ive=0; ive<cantVE; ive++){
            	 sb = new StringBuilder();
            	 sb.append("VARIABLE_ESTADO  " + nombresVE[ive] + " CANT_VALORES " + cantCla[ive]);
            }
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
            
            sb = new StringBuilder();
            sb.append("cantCron" + " " + cantCron);
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
            
            sb = new StringBuilder();
            sb.append("nombreDurPaso" + " " + "SEMANA");  // nombre de la duraci�n del paso
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());     
            
            sb = new StringBuilder();
            sb.append("cantDatos" + " " + cantDatos);  // cantidad de datos 
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());             
                      
            
            sb = new StringBuilder();
            sb.append("Cronica ");
            sb.append("Estacion ");               
            for(int is=0; is<cantSeries; is++){
                sb.append(nombresSeries[is]); 
                sb.append(" ");                                      
            }
            for(int ive=0; ive<cantVE; ive++){
                sb.append(nombresVE[ive] + " ");                               
            }  
            for(int ive=0; ive<cantVE; ive++){
                sb.append(nombresVE[ive] + "-");
            	sb.append("V.Estado-continua");            
                sb.append(" ");                                                    
            }          
            sb.append("Ind.est.compuesto,");              
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
            sb = new StringBuilder();    
            for(int t=0; t<cantDatos; t++){
            	System.out.println("Impresión proc.histórico tiempo " + t);            
                sb.append(estadoCompuesto.getAnio()[t]);
                sb.append(" "); 
                sb.append(estadoCompuesto.getPaso()[t]);
                sb.append(" ");             
                for(int is=0; is<cantSeries; is++){
                	String ns = nombresSeries[is];
                    sb.append(seriesLeidas.get(ns).getDatos()[t]);            
                    sb.append(" ");                            
                }
                for(int ive=0; ive<cantVE; ive++){
                	String nve = nombresVE[ive];
                    sb.append(clases.get(nve).getDatos()[t]);
                    sb.append(" ");                                
                }   
                for(int ive=0; ive<cantVE; ive++){
                	String nve = nombresVE[ive];
                    sb.append(varsEstado.get(nve).getDatos()[t]);
                    sb.append(" ");                                
                }                               
                sb.append(estadoCompuesto.getDatos()[t]);            
                sb.append("\n");     
            }
            DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
            
            /**
             * Genera salida de las matrices de transici�n
             * en MatTrans.xlt
             */
            
            String dirMat = directorio + "/MatTrans.xlt";        
            
            existe = DirectoriosYArchivos.existeArchivo(dirMat);
            if(existe) DirectoriosYArchivos.eliminaArchivo(dirMat);        
            
            sb = new StringBuilder();
            sb.append(identificadorEstimacion + " " + fecha1.toString());
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());        
            
            sb = new StringBuilder();
            sb.append("cantSeries" + " " + cantSeries);
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());
            
            sb = new StringBuilder();
            sb.append("cantVarEst " + " " +  cantVE);
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString()); 
            
            sb = new StringBuilder();
            sb.append("cantVarEstOptim " + " " +  cantVE);
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());         
            
            sb = new StringBuilder();
            sb.append("cantEstac" + " " + cantMaxPasos);
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());        

            sb = new StringBuilder();
            sb.append("cantCron" + " " + cantCron);
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());
            
            sb = new StringBuilder();
            sb.append("nombreDurPaso" + " " + "SEMANA");  // nombre de la duración del paso
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());   
            
            sb = new StringBuilder();
            sb.append("cantEstadosCompuestos" + " " + cantEstComp);  // cantidad de estados compuestos
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());           
            
            sb = new StringBuilder();
            sb.append("cantDatos" + " " + cantDatos);  // cantidad de datos 
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());                

            sb = new StringBuilder();
            sb.append("Nombres-VA" + " ");
            for(int is=0; is<cantSeries; is++){
                sb.append(nombresSeries[is]); 
                sb.append(" ");                                      
            }
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString()); 
            
            sb = new StringBuilder();                 
            sb.append("Nombres-VE" + " ");        
            for(int ive=0; ive<cantVE; ive++){
                sb.append(nombresVE[ive] + " ");                               
            }          
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());  
            
            sb = new StringBuilder();                 
            sb.append("Nombres-VE-Optim" + " ");        
            for(int ive=0; ive<cantVE; ive++){
                sb.append(nombresVE[ive] + " ");                               
            }          
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());          
            
            sb = new StringBuilder(); 
            sb.append("Cant-Clases-VE" + " ");           
            for(int ive=0; ive<cantVE; ive++){
                sb.append(cantCla[ive] + " ");                               
            } 
            DirectoriosYArchivos.agregaTexto(dirMat, sb.toString());   

            
            Set<Integer> set = matricesT.keySet();
            // Hay que ordenar las matrices para imprimirlas
            ArrayList<Integer> lista = new ArrayList<Integer>();
            lista.addAll(set);
            Collections.sort(lista);
            for(int i: lista){
                MatPTrans mPT = (MatPTrans)matricesT.get(i);  
                String st = mPT.toString();
                DirectoriosYArchivos.agregaTexto(dirMat, st);                        
            }         
            

            /**
             * Genera salida de observaciones por estación y estado compuesto
             */
            String dirDatosEE = directorio + "/ObservPorEstacEC.xlt";
            existe = DirectoriosYArchivos.existeArchivo(dirDatosEE);
            if(existe) DirectoriosYArchivos.eliminaArchivo(dirDatosEE);  
            sb = new StringBuilder();
            sb.append("CANTIDAD_ESTADOS_COMPUESTOS ");
            sb.append(cantEstComp);
            DirectoriosYArchivos.agregaTexto(dirDatosEE, sb.toString());  
            sb = new StringBuilder();
            for(int ip=0; ip<cantMaxPasos; ip++){
            	System.out.println("Impresión observaciones paso " + (ip+1));
                for(int iec=0; iec<cantEstComp; iec++){                    
                    sb.append("Paso ");
                    sb.append(ip+1);
                    sb.append(" ");                
                    sb.append("Estado-compuesto= ");
                    sb.append(iec);              
                    sb.append("\n");
                    for(int ive=0; ive<cantVE; ive++){
                    	sb.append(nombresVE[ive]);
                    	sb.append(" ");
                    	sb.append(clasesDeEstadoComp(iec)[ive]);
                    	sb.append(" ");
                    	sb.append("\n");           	
                    }                                                 
                    for(int is=0; is<cantSeries; is++){
                    	sb.append(nombresSeries[is]);
                    	sb.append(" ");         
                    }
                    sb.append("est_comp_siguiente ");
                    sb.append("anio-obs ");
                    sb.append("paso-obs ");
                    sb.append("\n");        
                    ArrayList<Observacion> aux = observaciones.get(ip).get(iec);                    
                    for(Observacion ob: aux){
                        for(int is=0; is<cantSeries; is++){
                            sb.append(ob.getValoresSeries()[is]);                        
                            sb.append(" ");                            
                        }
                        sb.append(ob.getEstadoProximo());
                        sb.append(" ");     
                        sb.append(ob.getAnio());     
                        sb.append(" ");     
                        sb.append(ob.getPaso());     
                        sb.append("\n"); 
                    }                
                }
                sb.append("\n"); 
            }               	
            DirectoriosYArchivos.agregaTexto(dirDatosEE, sb.toString());
            System.out.println("TERMINA IMPRESION DE TEXTOS PARA DATATYPE");     
    		
    	}


	    /**
	     * Imprime salidas para verificación
	     * @param directorio
	     */
	    public void imprimeResultVerif(String directorio) {
	    	
	        System.out.println("COMIENZA IMPRESION DE SALIDAS DE VERIFICACION");     
	      
	        // Genera salida de  datos, estaci�n, valor de las var. de Estado y clase 
	        // para cada tiempo t
	        String dirDatos = directorio + "/DatosProcHistorico.xlt";
	        boolean existe = DirectoriosYArchivos.existeArchivo(dirDatos);
	        if(existe) DirectoriosYArchivos.eliminaArchivo(dirDatos);
	        
	        StringBuilder sb = new StringBuilder();
	        sb.append("t,");
	        sb.append("Estacion,");               
	        for(int is=0; is<cantSeries; is++){
	            sb.append(nombresSeries[is]); 
	            sb.append(",");                                      
	        }
	        for(int ive=0; ive<cantVE; ive++){
	            sb.append(nombresVE[ive] + " - ");
	        	sb.append("Valor V.Estado ");            
	            sb.append(",");                                
	            sb.append("Clase V.Estado ");            
	            sb.append(",");                      
	        }   
	        sb.append("Ind.est.compuesto,");              
	        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
	            
	        for(int t=0; t<cantDatos; t++){
	            sb = new StringBuilder();
	            sb.append(t);
	            sb.append(","); 
	            sb.append(pasos[t]);
	            sb.append(",");             
	            for(int is=0; is<cantSeries; is++){
	            	String ns = nombresSeries[is];
	            	Serie s = seriesLeidas.get(ns);	            	
	                sb.append(s.getDatos()[t]);            
	                sb.append(",");                            
	            }
	            for(int ive=0; ive<cantVE; ive++){
	            	String nve = nombresVE[ive];
	            	Serie sve = varsEstado.get(nve);
	                sb.append(sve.getDatos()[t]);
	                sb.append(",");                                
	                sb.append(clases.get(nve).getDatos()[t]);            
	                sb.append(",");  
	            }             
	            sb.append(estadoCompuesto.getDatos()[t]);            
	            sb.append("\n");
	        }
	        DirectoriosYArchivos.agregaTexto(dirDatos, sb.toString());
	        
	        // Genera salida de límites de variables de estado
	        String dirVarEst = directorio + "/LimSupClasesVarEst.xlt";
	        existe = DirectoriosYArchivos.existeArchivo(dirVarEst);
	        if(existe) DirectoriosYArchivos.eliminaArchivo(dirVarEst);
	        
	        for(int ive=0; ive<cantVE; ive++){
	        	sb = new StringBuilder();
	            sb.append("Variable de estado,");            
	            sb.append(nombresVE[ive]);
	            sb.append(" Límites superiores de clases\n");            
	            DirectoriosYArchivos.agregaTexto(dirVarEst, sb.toString());
	            sb = new StringBuilder();
	            for(int ip=0; ip<cantMaxPasos; ip++){
	                sb.append("Estación,");
	                sb.append(ip+1);
	                sb.append(",");                
	                for(int iclas = 0; iclas<cantCla[ive]; iclas++){
	                    sb.append(cuantiles[ive][ip][iclas]);
	                    sb.append(",");                      
	                }
	                sb.append("\n");
	            }
	            sb.append("\n");
	            DirectoriosYArchivos.agregaTexto(dirVarEst, sb.toString());            
	        }
	        
	        /**
	         * Genera salida de las medias absolutas de cada serie en cada estaci�n
	         */
	        String dirMedAbs = directorio + "/MediasAbsolutas.xlt";
	        existe = DirectoriosYArchivos.existeArchivo(dirMedAbs);
	        if(existe) DirectoriosYArchivos.eliminaArchivo(dirMedAbs);           

	        sb = new StringBuilder();
	        sb.append(",");                    
	        for (int is=0; is<cantSeries; is++){
	            sb.append(nombresSeries[is]);
	            sb.append(",");            
	        }
	        sb.append("\n");
	        for(int ip=0; ip<cantMaxPasos; ip++){
	        	sb.append("\n");     
	            sb.append(ip+1);
	            sb.append(",");
	            for(int is=0; is<cantSeries; is++){
	                sb.append(mediaAbs[ip][is]);
	                sb.append(",");                
	            }
	            sb.append("\n");     
	        }
	        DirectoriosYArchivos.agregaTexto(dirMedAbs, sb.toString());
	        
	        /**
	         * Genera salida por paso del año y estado compuesto de la cantidad de observaciones en cada estado compuesto
	         */
	        String dirObsE = directorio + "/cantObsPasoEstadoComp.xlt";
	        existe = DirectoriosYArchivos.existeArchivo(dirObsE);
	        if(existe) DirectoriosYArchivos.eliminaArchivo(dirObsE);
	        sb = new StringBuilder();
	        sb.append(",");                    
	        for (int iec=0; iec<cantEstComp; iec++){
	            sb.append(iec);
	            sb.append(",");            
	        }
	        sb.append("\n");     
	        for(int iest=0; iest<cantMaxPasos; iest++){
	        	sb.append("\n");     
	            sb.append(iest+1);
	            sb.append(",");
	            for(int iec=0; iec<cantEstComp; iec++){
	                sb.append(cantObsPasoEstadoComp[iest][iec]);
	                sb.append(",");                
	            }
	            sb.append("\n");     	            
	        }        
	        DirectoriosYArchivos.agregaTexto(dirObsE, sb.toString()); 
	        
	        /**
	         * Genera salida de los valores promedio de cada serie en cada estación y estado compuesto         
	         */
	        for (int is=0; is<cantSeries; is++){
	            String dirMed = directorio + "/Media" + nombresSeries[is] + ".xlt";
	            existe = DirectoriosYArchivos.existeArchivo(dirMed);
	            if(existe) DirectoriosYArchivos.eliminaArchivo(dirMed);   
	            sb = new StringBuilder();
	            sb.append("ESTACION,");
	            for(int iec=0; iec<cantEstComp; iec++){
	                sb.append(iec+1);
	                sb.append(",");                
	            }
	            sb.append("\n");     	
	            for(int ip = 0; ip<cantMaxPasos; ip++){
	                sb = new StringBuilder();
	                sb.append(ip+1);
	                sb.append(",");
	                for(int iec=0; iec<cantEstComp; iec++){
	                    sb.append(medias[ip][is][iec]);
	                    sb.append(",");                    
	                }
	                sb.append("\n");     	
	            }
		        DirectoriosYArchivos.agregaTexto(dirMed, sb.toString());   
	        }    
  
	        
	        // Genera salida de comparación de clases con EDF
	        String dirComparaClasesEDF = directorio + "/ComparaClasesEDF.xlt";
	        existe = DirectoriosYArchivos.existeArchivo(dirComparaClasesEDF);
	        if(existe) DirectoriosYArchivos.eliminaArchivo(dirComparaClasesEDF);
	        DirectoriosYArchivos.agregaTexto(dirComparaClasesEDF, "CLASES POR CRONICA Y SEMANA");
	        int ind = 0; 
	        while (ind<cantDatos){
	        	int r = ind % 52; 
	        	if(r == 0){
	        		int numCron = ind/52 + 1;
	        		sb.append("Cronica " + numCron + "\n");
	        	}
	    		for(int ifil=1; ifil<=6; ifil++){        		
		        		int maxcol = 10;
		        		if(ifil == 6) maxcol = 2;
		        		sb = new StringBuilder();	        		
		        		for(int icol=0; icol<maxcol; icol++){
		        			sb.append((estadoCompuesto.getDatos()[ind]+1) + "\t");
			        		ind++;
		        		}
		        		String texto = sb.toString();
		        		DirectoriosYArchivos.agregaTexto(dirComparaClasesEDF, texto);
	    		}	
	        }	        
	        System.out.println("TERMINO LA IMPRESION DE SALIDAS DE VERIFICACION");
	    }	    
	    
	    
	    
	    
	    
	    public int getCantCron() {
			return cantCron;
		}

		public void setCantCron(int cantCron) {
			this.cantCron = cantCron;
		}

		public int[] getCantCla() {
			return cantCla;
		}

		public void setCantCla(int[] cantCla) {
			this.cantCla = cantCla;
		}


		public String getIdentificadorEstimacion() {
			return identificadorEstimacion;
		}


		public void setIdentificadorEstimacion(String identificadorEstimacion) {
			this.identificadorEstimacion = identificadorEstimacion;
		}


		public int getCantSeries() {
			return cantSeries;
		}


		public void setCantSeries(int cantSeries) {
			this.cantSeries = cantSeries;
		}


		public int getCantMaxPasos() {
			return cantMaxPasos;
		}


		public void setCantMaxPasos(int cantMaxPasos) {
			this.cantMaxPasos = cantMaxPasos;
		}


		public String getNombreDurPaso() {
			return nombreDurPaso;
		}


		public void setNombreDurPaso(String nombreDurPaso) {
			this.nombreDurPaso = nombreDurPaso;
		}


		public double[][][] getDefVarEst() {
			return defVarEst;
		}


		public void setDefVarEst(double[][][] defVarEst) {
			this.defVarEst = defVarEst;
		}




		public int[] getEtiquetaCron() {
			return etiquetaCron;
		}


		public void setEtiquetaCron(int[] etiquetaCron) {
			this.etiquetaCron = etiquetaCron;
		}


		public int[] getEtiquetaCronDatos() {
			return etiquetaCronDatos;
		}


		public void setEtiquetaCronDatos(int[] etiquetaCronDatos) {
			this.etiquetaCronDatos = etiquetaCronDatos;
		}


		public int getCantDatos() {
			return cantDatos;
		}


		public void setCantDatos(int cantDatos) {
			this.cantDatos = cantDatos;
		}



		public int getRadioEntorno() {
			return radioEntorno;
		}


		public void setRadioEntorno(int radioEntorno) {
			this.radioEntorno = radioEntorno;
		}


		public int[] getPasos() {
			return pasos;
		}


		public void setPasos(int[] pasos) {
			this.pasos = pasos;
		}


		public double[][] getProbCla() {
			return probCla;
		}


		public void setProbCla(double[][] probCla) {
			this.probCla = probCla;
		}


		public double[][][] getLimSupVE() {
			return limSupVE;
		}


		public void setLimSupVE(double[][][] limSupVE) {
			this.limSupVE = limSupVE;
		}




		public double[][][] getMedias() {
			return medias;
		}


		public void setMedias(double[][][] medias) {
			this.medias = medias;
		}


		public double[][] getMediaAbs() {
			return mediaAbs;
		}


		public void setMediaAbs(double[][] mediaAbs) {
			this.mediaAbs = mediaAbs;
		}


		/**
	     * El programa 
	     * - lee los datos de un conjunto de series y la definición de variables de estado
	     * - estima las matrices de un proceso de Markov y las observaciones representativas
	     * - crea los archivos para cargar los datatypes legibles por CargadorPEHistorico y CargadorPEMarkov
	     */
	    public static void main(String[] args) {              
	        EstimadorMarkovAmpliado eM = new EstimadorMarkovAmpliado();
	        
	    	String dirArchConf = "resources/ESTIMADORES.conf";
	    	String nombreProp = "rutaMarkovAmpliado";
	        
//	        // Elige directorios de entradas y salidas
//	        boolean soloDirectorio = true;  
//	        String titulo1 = "ELIJA EL DIRECTORIO DE ENTRADA DE DATOS DONDE LEER ARCHIVOS: datos.txt, defVE.txt, defEstac.txt ";
//	        String dirEntradas = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo1, dirArchConf, nombreProp);
//	        
//	        String titulo2 ="ELIJA EL DIRECTORIO DE SALIDA DE LA ESTIMACION, ARCHIVOS: datosProcHistorico.xlt, MatTrans.xlt, ObservPorEstacEC.xlt, defEstac.txt"; 
//	        String dirSalidas =  LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo2, dirArchConf, nombreProp);
//	 
//
//	        String titulo3 = "ELIJA EL DIRECTORIO PARA COPIAR SALIDA PARA EL PROCESO HISTóRICO ASOCIADO, ARCHIVO datosProcHistorico.xlt";
//	        String dirHist =  LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo3, dirArchConf, nombreProp);
//
//	    	String titulo4 = "Entre la ventana de pasos anteriores y posteriores a usar";
//	    	String sventana = utilsVentanas.VentanaEntradaString.leerTexto(titulo4);
//	    	eM.setVentana(Integer.parseInt(sventana));
	        /**
	         * CODIGO PARA CARGAR DIRECTORIOS
	         */
	    	String dirEntradas = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS-MARKOV-AMPLIADO/Entradas";
	    	String dirSalidas = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS-MARKOV-AMPLIADO/Salidas";
	    	String dirHist = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS-MARKOV-AMPLIADO/historicoAportesClases20";
	    	eM.cargaTodo(dirEntradas);
	
	        // Estima proceso de Markov
	        eM.estimaMarkovAmpliado();
	        
	        // Crea salidas de verificación de resultados

	        String dirSalVerif = dirSalidas + "/SalidasVerif";   
	        if(!DirectoriosYArchivos.existeDirectorio(dirSalVerif)) DirectoriosYArchivos.creaDirectorio(dirSalidas,"SalidasVerif");
	        eM.imprimeResultVerif(dirSalVerif);
	        
	        // Crea textos legibles por CargadorPEHistorico y CargadorPEMarkovAmpliado
	        eM.imprimeTextosParaDatatypesAmp(dirSalidas);
	        
	        // Copia el archivo DatosSal.xlt al directorio dirHist
	        String origen = dirSalidas + "/datosProcHistorico.xlt";
	        String destino = dirHist + "/datosProcHistorico.xlt";                   
	        try{
	        	DirectoriosYArchivos.copy2(origen, destino);
	        }catch(Exception e){
	        	System.out.println("Error al copiar el archivo " + origen);
	        	System.exit(1);
	        }    
	        
	        System.out.println("TERMINA LA TOTALIDAD DEL PROCESO DE ESTIMACIóN");
	                
	    }

	   
	}
	


