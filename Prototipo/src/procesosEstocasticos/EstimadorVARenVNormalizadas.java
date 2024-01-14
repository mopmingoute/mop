/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimadorVARenVNormalizadas is part of MOP.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import matrices.Oper;
import modelolineal.EstimaVARExo;
import modelolineal.EstimacionVARs;
import persistencia.EscritorTextosGeneralesPE;
import utilitarios.*;


public class EstimadorVARenVNormalizadas {

	/**
	 * El programa tiene dos opciones
	 * 
	 * ESTIMA SOLO EL VAR: 
	 * - Estima un proceso VAR en variables normalizadas. 
	 * - Lee una definición de VE agregadas (vector H) para usar en la
	 *   optimización. La agregación se hace en las variables de estado
	 *   normalizadas del VAR (vector V) mediante una aplicación lineal (T) 
	 *   H = TV
	 * - Las tablas de VB y valores de los recursos de la optimización se construyen
	 *   con discretizaciones de las variables de estado H normalizadas. 
	 * - Se construye un ProcesoHistorico que incluye los valores históricos de esas
	 *   variables H.
	 * - Luego se puede simular usando el VAR o el ProcesoHistorico.
	 * 
	 * - En la optimización se usa la distribución normal N(media, var) de
	 *   Prob(V/H) donde media y var resultan de los coeficienes del VAR y de la
	 *   matriz T según fórmulas que están en 54-Vnn-IMPLEMENTACIÓN DEL VAR Y SU
	 *   PROCESO LINEAL AGREGADO.docx 
	 * - En la simulación se pueden usar el VAR con series sintéticas o el ProcesoHistorico creado.
	 * 
	 * ESTIMA EL VAR y un PVA:
	 * - Estima un proceso VAR y adicionalmente un PVA,
	 *   que es un proceso en el que se construyen variables de estado por
	 *   agregación de las variables de estado originales (no normalizadas)
	 *   empleadas en el VAR.
	 * - Lee una definición de VE agregadas para usar en la
	 *   optimización. La agregación puede hacerse por grupos. Cada grupo resulta
	 *   de la combinación lineal de variables de estado afines, por ejemplo
	 *   aportes rezagados cuyo resultado se estandariza (0,1] en un grupo y El
	 *   Niño en otro grupo. La agregación de las variables de estado originales
	 *   podría hacerse con operaciones no lineales como redes neuronales. Los
	 *   resultados de cada grupo se combinan linealmente entre sí para formar
	 *   variables de estado. Finalmente esas variables de estado se normalizan y
	 *   se obtienen variables H El PVA es de la forma [Y*(t+1)|H*(t+1)]* =
	 *   función lineal de H*(t) + errores correlacionados espacialmente.
	 * - Luego se puede simular usando el VAR o el ProcesoHistorico, que tiene
	 *   distintas VE que de la opción anterior.
	 * 
	 * EN RESUMEN LAS ALTERANATIVAS DE OPTIMIZACIÓN Y SIMULACIÓN SON:
	 * 
	 * OPTIMIZACIÓN:
	 * O-VAR) La optimización emplea las VE normalizadas agregadas
	 * (vector H) construídas a partir de las normalizadas del VAR, y usa la
	 * distribución Prob(V/H) para sortear estados completos. 
	 * O-PVA) La optimización usa un PVA que se estima, en el que las VE resultan de la
	 * agregación de VE originales (antes de las transformación que las pasa a
	 * las VE normalizadas del VAR).
	 * 
	 * SIMULACIÓN 
	 * S1) O-VAR + Se simula con el ProcesoHistorico PHVAR asociado
	 * a O-VAR) 
	 * S2) O-PVA + Se simula con el ProcesoHistorico PHPVA asociado a
	 * O-PVA, que tiene otras VE que PHVAR) 
	 * S3) O-VAR + Se simula con el VAR con series sintéticas 
	 * S4) O-PVA + Se simula con el VAR con series sintéticas
	 * 
	 */

	private static String nombreEstimacion;
	private EstimaVARExo eVAR;
	private String formaEstimacion; // SOLO_VAR o VAR_Y_PVA
	private int ordenDelVAR; // cantidad de pasos de tiempo rezagados del VAR a
								// estimar
	/**
	 * true si en la optimizacion se usan VE agregadas y false si se usan las mismas del VAR
	 * Con la forma de estimacion VAR_Y_PVA, debe ser necesariamente true.
	 */
	private boolean tieneVEAgregadas;  
	
	private boolean tieneExogenas;
	private int cantVariables; // cantidad de variables autoregresivas del VAR
	private int cantExogenas;  // cantidad de variables exogenas del VAR
	private int cantVEAgregadas;  // cantidad de variables de estado agregadas si se usa forma estimacion VAR_Y_PVA
	private String[] nombresVariables; // nombres de variables autoregresivas del VAR
	private String[] nombresExogenas; // nombres de las variables exógenas contemporáneas empleadas
	private String[] nombresProcesosExogenas; // nombres de los PE respectvos de las variables exógenas
	private String[] nombresVEAgregadas; // nombres de las VE si son distintas de las del VAR en simulación (autoregresivas más exógenas)
	
	/**
	 * Los valores de cada serie fuera de las cotas de outliers se llevan al valor de la cota
	 */
	private Hashtable<String, Double> cotaInfOutliers; // clave nombre de serie, valor cota inferios para outliers
	private Hashtable<String, Double> cotaSupOutliers; // idem con cota superior.
	
	private static String dirEntradas; // directorio de datos de entradas
	private static String dirSalidas; // directorio de datos de salidas de estimación
	private static String dirProcHist; // directorio del proceso historico asociado
	private static String dirPVA; // directorio de salidas para el PVA asociado
	
	
	/**
	 * COMIENZA UNA SERIE DE ATRIBUTOS USADOS EN LA FORMA DE ESTIMACION SOLO_VAR
	 */
	
	/**
	 * Matriz para calcular la esperanza condicional de V dado H
     *  ME es la matriz tal que 
	 *  E(V│H )= Σ_V T * (T Σ_V T* )^(-1)  H =: ME H
	 *  Σ_V es la matriz de covarianzas de las VE totales del VAR.
	 *  ^(-1) es el operador invertir la matriz
	 *  T es la matriz de agregación de las VE
	 */
	private double[][] matrizEspCondicional; // matriz para calcular la esperanza condicional de V dado H
	
	
	/**
	 * Matriz para calcular la varianza condicional de V dado H
     *  MV es la matriz tal que 
	 *  Var(V│H) = Σ_V - Σ_V T* [ (T Σ_V T*)^(-1) ] T Σ_V  =: MV
	 *  Σ_V es la matriz de covarianzas muestrales de las VE del VAR completo.
	 *  T es la matriz de agregación de las VE
	 */	
	private double[][] matrizVarCondicional;
	
	/**
	 * Matrices de la descomposición espectral de la matrizVarCondicional MV
	 * dVC matriz diagonal de los valores propios
	 * dVCC matriz diagonal corregida haciendo cero exacto los valores
	 * que están por debajo de utilitarios.CONSTANTES.EPSILON_VAL_PROPIOS
	 * bVC matriz de cambio de base
	 */
	private double[][] dVC;
	private double[][] dVCC;
	private double[][] bVC;
	/**
	 * En la descomposición espectral, ordinal de los valores propios nulos 
	 * en la matriz diagonal dVC
	 */
	private ArrayList<Integer> ordinalDeValoresPropiosNulos;
	
	/**
	 * Matriz Σ_V de covarianza muestral de las VE del VAR completo
	 */
	private double[][] sigmaV;
	
	/**
	 * Matriz T de pasaje para construir las variables de estado en la agregación lineal
	 */
	private double[][] matTAgLin;
	
	/**
	 * TERMINAN LOS ATRIBUTOS PARA SOLO_VAR
	 */
	
	
	// Atención: los nombres de los procesos podrían tener que guardar relación con los de los directorios
	private static String nombrePESimul;  // nombre del proceso estimado para la simulación
	private static String nombrePEOptim;  // nombre del proceso estimado para la optimización
	private static String nombrePEHist;  // nombre del proceso histórico asociado al VAR en la optimización
	/**
	 * Series leidas, incluso las exogenas. Clave nombre de la serie
	 */
	private Hashtable<String, Serie> seriesLeidas; 

	/**
	 * Series normalizadas, incluso las exogenas y sus transformaciones de normalizacio por paso.
	 * Clave nombre de la serie
	 */
	private Hashtable<String, SerieTransformadaYTransformaciones> seriesNormalizadas; 
	
	
	/**
	 * Las series estandarizadas 0,1,clave el nombre de serie original
	 */
	private Hashtable<String, Serie> series01; 
	
	/**
	 * Las series a emplear para hallar cada VE, clave el nombre de serie original
	 */
	private Hashtable<String, Serie> series; 
												
	/**
	 * Cantidad de series leidas, las autoregresivas del VAR y las exógenas										
	 */
	private int cantSeriesLeidas; 
	
	/** 
	 * La totalidad de los nombres de las series, incluso los de las variables exógenas
	*/								
	private String[] nombresSeriesLeidas; 
	
	/**
	 * Series históricas de variables agregadas
	 */
	private Hashtable<String, Serie> seriesVEAgHistoricas; 
	
	private int cantMaxPasos; // cantidad máxima de pasos que puede tener un año
	private String nombreDurPaso; // SEMANA, DIA, HORA, ETC.

	private int cantCron; // cantidad de crónicas anuales leídas.

	private int cantDatos; // cantidad de datos leídos

	/**
	 * Para cada tiempo indica el paso (entero entre 1 y la cantidad móxima de
	 * pasos en el año)
	 */
	private int[] pasos;

	/**
	 * Para cada tiempo indica el año
	 */
	private int[] anios;

	private String nombrePaso;
	
	private int cantPasos;

//	private int cantVE;
	
	/**
	 * Coeficientes de los datos para crear la variable de estado. Primer
	 * índice: variable de estado Segundo índice: serie de datos Tercer índice:
	 * rezago en la serie empezando de 0, -1, -2,.... Define la combinación
	 * lineal de datos rezagados que determina cada estado.
	 */
	private double[][][] defVarEst;

	/**
	 * Cantidad de rezagos (incluso el valor corriente) en la definición de las
	 * VE primer índice VE, segundo índice serie
	 */
	private int[][] cantRezagos;

	/**
	 * Para cada paso del año y serie, guarda la media, es decir no condicionada
	 * a ningón estado. Primer óndice estación, segundo óndice serie
	 */
	private double[][] mediaAbs;

	/**
	 * Tipo de las trasformaciones de normalización de cada serie leída incluso para las variables exógenas
	 * Clave nombre de la serie, valor tipo de transformacion de normalizacion
	 */
	private Hashtable<String, String> tipoTransformacion; 
	//String tipoTransformacion;														
																											
	/**
	 * Definicion de las poblaciones de cada serie leida incluso para las variables exogenas
	 * a ser empleada para definir las transformaciones de normalizacion
	 * Cada DefPoblacionSerie tiene parametros radioEntorno, periodo y cantPeriodos
	 */
	private Hashtable<String,DefPoblacionSerie> defPoblaciones;
	
	/**
	 * Cotas superiores e inferiores de las realizaciones de cada VA del proceso
	 * clave nombre de VA, valor cota inferior o superior
	 */
	private Hashtable<String, Double> cotaInfRealiz;
	private Hashtable<String, Double> cotaSupRealiz;
	
	
	/**
	 * Para cada variable de estado los percentiles de las discretizaciones
	 * en los valores de la VE continuas normales.
	 */
	private Hashtable<String, double[]> percentilesDiscretizaciones;
	
	/**
	 * Los nombres de los grupos de series de cada VE 
	 * Clave nombre de VE 
	 * Valor lista de nombres de grupos de la VE
	 */
	private Hashtable<String, ArrayList<String>> gruposDeVE;

	/**
	 * Nombre del grupo al que pertenece cada serie en cada VE. Clave: String
	 * con nombre de VE + nombre de serie Valor: nombre del grupo No puede haber
	 * el mismo nombre de grupo en dos VE distintas Dentro de un grupo de
	 * series, cada una tiene un ponderador, para formar una combinación lineal
	 * a partir de los valores de la serie. El valor resultante de cada grupo se
	 * estandariza 0,1 y se pondera por el respectivo ponderador grupo
	 */
	private Hashtable<String, String> grupoDeVESerie;

	/**
	 * Operador para combinar los valores de los grupos dentro de una VE puede
	 * ser COMBINACION_LINEAL, MINIMO, etc. Clave: nombre de VE Valor: constante
	 * del operador
	 */
	private Hashtable<String, String> operadorEnVE;

	/**
	 * Ponderador de un grupo dentro de la VE si el operador es
	 * COMBINACION_LINEAL Clave: String con nombre de VE + nombre del grupo
	 * Valor: ponderador del grupo dentro de la VE
	 */
	private Hashtable<String, Double> ponderadorDeGruposEnVE;
	
	private CalculadorVE calculadorVE;  // Objeto que se usara para calcular las VE
	

	public void cargaTodo(String dirEntradas) {
		
		cotaInfOutliers = new Hashtable<String, Double>(); 
		cotaSupOutliers = new Hashtable<String, Double>();
		cotaInfRealiz = new Hashtable<String, Double>(); 
		cotaSupRealiz = new Hashtable<String, Double>();
		
		// Lee parámetros generales el VAR
		String dirDefVar = dirEntradas + "/defVar.txt";
		leeDefVAR(dirDefVar);

		// Lee series
		ConjuntoDeSeries conjSeriesLeido = MetodosSeries.leeConjuntoDeSeries(dirEntradas + "/datos.txt");
		seriesLeidas = conjSeriesLeido.getSeries();
		nombreDurPaso = conjSeriesLeido.getNombrePaso();
		cantPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombreDurPaso);
		cantCron = conjSeriesLeido.getCantCron();
		nombresSeriesLeidas = conjSeriesLeido.getNombresSeries();
		cantSeriesLeidas = seriesLeidas.values().size();
		cantDatos = seriesLeidas.get(nombresSeriesLeidas[0]).getDatos().length;
		anios = seriesLeidas.get(nombresSeriesLeidas[0]).getAnio();
		pasos = seriesLeidas.get(nombresSeriesLeidas[0]).getPaso();
		cantMaxPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombreDurPaso);
		nombrePaso = conjSeriesLeido.getNombrePaso();
		series01 = new Hashtable<String, Serie>();
		series = new Hashtable<String, Serie>();
		mediaAbs = new double[cantMaxPasos][cantSeriesLeidas];

		// Crea las series estandarizadas y calcula las medias
		for (int is = 0; is < cantSeriesLeidas; is++) {
			String ns = nombresSeriesLeidas[is];
			Serie sl = seriesLeidas.get(ns);
			Serie s = MetodosSeries.estandariza01(sl, 0.0);
			series01.put(ns, s);
			double[] ms = MetodosSeries.mediaPorPaso(sl);
			mediaAbs[is] = ms;
		}

		// Lee definición de variables de estado
		String[] nombresVariablesYExo = matrices.Oper.yuxtaListas(nombresVariables, nombresExogenas);
		calculadorVE = new CalculadorVE(nombresVariablesYExo,
				nombresVEAgregadas, nombreDurPaso, seriesLeidas);

		String dirDefVE = dirEntradas + "/defVE.txt";
		calculadorVE.cargaParamVE(dirDefVE);

	}

	public void leeDefVAR(String dirDefVar) {
		
		ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(dirDefVar);
		AsistenteLectorEscritorTextos lector = new AsistenteLectorEscritorTextos(texto, dirDefVar);

		int i = 0;
		ArrayList<String> nomSeries = lector.cargaLista(i, "NOMBRES_SERIES");
		nombresVariables = new String[nomSeries.size()];
		nombresVariables = nomSeries.toArray(nombresVariables);
		cantVariables = nombresVariables.length;
		i++;
		
		ordenDelVAR = lector.cargaEntero(i, "ORDEN_DEL_VAR");
		i++;
		
		formaEstimacion = lector.cargaPalabra(i, "FORMA_ESTIMACION");
		i++;
		
		ArrayList<String> ntr = lector.cargaLista(i, "TIPO_TRANSFORMACIONES_SERIES");
		tipoTransformacion = new Hashtable<String, String>();
		
		for (int is = 0; is < nombresVariables.length; is++) {
			tipoTransformacion.put(nombresVariables[is], ntr.get(is));
		}
		i++;

		tieneExogenas = lector.cargaBooleano(i, "TIENE_EXOGENAS");
		i++;
		cantExogenas = 0;
		nombresExogenas = new String[0];
		if (tieneExogenas) {
			ArrayList<String> nomExo = lector.cargaLista(i, "NOMBRES_VARIABLES_EXOGENAS");
			nombresExogenas = new String[nomExo.size()];
			nombresExogenas = nomExo.toArray(nombresExogenas);
			cantExogenas = nombresExogenas.length;
		}
		i++;
		nombresProcesosExogenas = new String[0];
		if (tieneExogenas) {
			ArrayList<String> nomPExo = lector.cargaLista(i, "NOMBRES_PROCESOS_EXOGENAS");
			nombresProcesosExogenas = new String[nomPExo.size()];
			nombresProcesosExogenas = nomPExo.toArray(nombresProcesosExogenas);
		}				
		i++;		
		if (tieneExogenas){
			ntr = lector.cargaLista(i, "TIPO_TRANSFORMACIONES_EXOGENAS");
			for (int is = 0; is < nombresExogenas.length; is++) {
				tipoTransformacion.put(nombresExogenas[is], ntr.get(is));
			}
		}
		i++;
		
		nombresSeriesLeidas = matrices.Oper.yuxtaListas(nombresVariables,nombresExogenas);
	
		tieneVEAgregadas = lector.cargaBooleano(i, "TIENE_VE_AGREGADAS");
		i++;	
		
		nombresVEAgregadas = new String[0];
		if(tieneVEAgregadas){
			ArrayList<String> nomVEAgregadas = lector.cargaLista(i, "NOMBRES_VE_AGREGADAS");
			nombresVEAgregadas = nomVEAgregadas.toArray(nombresVEAgregadas);
		}
		i++;		
		if(tieneVEAgregadas && formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.VAR_Y_PVA)){
			ntr = lector.cargaLista(i, "TIPO_TRANSFORMACIONES_VE_PVA");
			nombresVEAgregadas = ntr.toArray(nombresVEAgregadas);
		}
		cantVEAgregadas = nombresVEAgregadas.length;
		i++;
		
		// Lee definición de poblaciones 
		defPoblaciones = new Hashtable<String, DefPoblacionSerie>();
		
		for(int in=1; in<= cantVariables + cantExogenas; in++){
			ArrayList<String> al = lector.cargaLista(i, "POBLACION");
			String nombreSerie = al.get(0);
			al.remove(0);
			DefPoblacionSerie dps = new DefPoblacionSerie(al);
			defPoblaciones.put(nombreSerie, dps);
			i++;
		}

		for(int in=1; in<= cantVEAgregadas; in++){
			ArrayList<String> al = lector.cargaLista(i, "POBLACION");
			String nombreSerie = al.get(0);
			al.remove(0);
			DefPoblacionSerie dps = new DefPoblacionSerie(al);
			defPoblaciones.put(nombreSerie, dps);
			i++;
		}
		
		// Lee cotas para outliers
		for(int in=1; in<= cantVariables + cantExogenas; in++){
			ArrayList<String> al = lector.cargaLista(i, "COTAS_OUTLIERS");
			String nombreSerie = al.get(0);
			al.remove(0);
			double cotaInf = Double.parseDouble(al.get(0));
			double cotaSup = Double.parseDouble(al.get(1));
			cotaInfOutliers.put(nombreSerie, cotaInf);
			cotaSupOutliers.put(nombreSerie, cotaSup);
			i++;
		}	
		
		// Lee cotas para realizaciones
		for(int in=1; in<= cantVariables + cantExogenas; in++){
			ArrayList<String> al = lector.cargaLista(i, "COTAS_REALIZACIONES");
			String nombreSerie = al.get(0);
			al.remove(0);
			double cotaInf = Double.parseDouble(al.get(0));
			double cotaSup = Double.parseDouble(al.get(1));
			cotaInfRealiz.put(nombreSerie, cotaInf);
			cotaSupRealiz.put(nombreSerie, cotaSup);
			i++;
		}			
		
		percentilesDiscretizaciones = new Hashtable<String, double[]>();
		for(int in=1; in<= cantVEAgregadas; in++){
			ArrayList<String> al = lector.cargaLista(i, "PERCENTILES_DISCRETIZACION");
			String nve = al.get(0);
			al.remove(0);
			double[] aux = utilitarios.UtilArrays.dameArrayD(utilitarios.UtilArrays.dameALDouble(al));
			percentilesDiscretizaciones.put(nve, aux);
			i++;
		}		
	}

	/**
	 * Topea outliers de variables del VAR y exógenas
	 * Normaliza variables del VAR y exógenas 
	 * Estima matrices del VAR 
	 * Escribe parámetros de las transformaciones de normalización y parámetros del VAR
	 * if (no usa PVA) // Se calculan media y varianda de la distribución de V/H
	 *   Calcula media y varianza de la distribución de V/H usando la matriz de
	 *   pasaje T, que es la que define las variables de estado normalizadas H=TV
	 *   que sale de la definición de variables de estado. 
	 *   (V variables rezagadas normalizadas y exógenas normalizadas, H=TV variables de estado
	 *   normalizadas del estado agregado) 
	 *   Escribe media y varianza de V/H 
	 *   
	 * else // Usa PVA hay que calcularlo 
	 *  Calcula variables de estado agregadas usando
	 *  estandarización y grupos sobre las variables originales de aportes y
	 *  variables climáticas. 
	 *  Normaliza esas variables de estado Estima el PVA
	 *  Escribe parámetros de las transformaciones de normalización de las VE y
	 *  parámetros del PVA
	 */
	public void estima() {

		cargaTodo(dirEntradas);
		double[][] datos = new double[cantDatos][cantVariables];
		double[][] datosExo = new double[cantDatos][cantExogenas];
		eVAR = new EstimaVARExo(datos, datosExo);		
		
		seriesNormalizadas = new Hashtable<String, SerieTransformadaYTransformaciones>();
		
		if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.SOLO_VAR)){
			// Se estima un VAR en variables normalizadas
			// Se crean las series normalizadas de las variables del VAR y exogenas
			String[] nombresVarYExo = utilitarios.UtilArrays.yuxtaS(nombresVariables, nombresExogenas);
			for(String ns: nombresVarYExo){
				Serie s = seriesLeidas.get(ns);
				double cotaInf = cotaInfOutliers.get(ns);
				double cotaSup = cotaSupOutliers.get(ns);
				Serie sc = s.acota(cotaInf, cotaSup);
				String tt = tipoTransformacion.get(ns);
				DefPoblacionSerie dpob = defPoblaciones.get(ns);
				SerieTransformadaYTransformaciones snyt = MetodosSeries.normalizar(sc, tt, dpob);
				seriesNormalizadas.put(ns, snyt);
			}				
			// Se carga el EstimadorVARExo con los datos de las series normalizadas	
			for (int iv = 0; iv < nombresVariables.length; iv++) {
				for (int t = 0; t < cantDatos; t++) {
					datos[t][iv] = seriesNormalizadas.get(nombresVariables[iv]).getSerie().getDatos()[t];
				}
			}
			for (int iv = 0; iv < nombresExogenas.length; iv++) {
				for (int t = 0; t < cantDatos; t++) {
					datosExo[t][iv] = seriesNormalizadas.get(nombresExogenas[iv]).getSerie().getDatos()[t];
				}
			}
			boolean esIndep = false;
			eVAR.estimaVAR(ordenDelVAR, esIndep);
			// matSAgLin es la matriz definida en la definición del VAR procesados por el calculadorVE
			double[][] matSAgLin = calculadorVE.construyeAgregadorLinealParaVAR(ordenDelVAR, nombresVariables, nombresExogenas);

			if(tieneVEAgregadas){
				// calcula matriz de varianza muestral de las VE del VAR completo
				calculaMatSigmaV();
				
				// calcula la matriz T a partir de la matSAgLin, para que las variables de estado tengan varianza uno.
				calculaMatT(matSAgLin);
				
				// calcula matriz ME para esperanza condicional dado el estado
				// E(V│H )= Σ_V T * (T Σ_V T* )^(-1)  H = ME H
				calculaMatrizEspCond();		
				
				// calcula matriz MV para varianza condicional dado el estado
				//  Var(V│H) = Σ_V - Σ_V T* [ (T Σ_V T*)^(-1) ] T Σ_V  =: MV
				calculaMatrizVarCond();
				
				calculaValoresHistDeVEAgr();
			}
					
			
		}else if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.VAR_Y_PVA)){
			// Se estima un VAR y un PVA en variables de estado agregadas
			// Se crean las variables de estado para el PVA
		
			//calculadorVE.construyeAgregadorLinealParaVAR(ordenDelVAR, nombresVariablesVAR, nombresVariablesExogenas)
			
		}else{
			System.out.println("Error en formaEstimacion");
			System.exit(1);
		}	
	}

	public void estima(String dirEntradas) {

		cargaTodo(dirEntradas);
		double[][] datos = new double[cantDatos][cantVariables];
		double[][] datosExo = new double[cantDatos][cantExogenas];
		eVAR = new EstimaVARExo(datos, datosExo);

		seriesNormalizadas = new Hashtable<String, SerieTransformadaYTransformaciones>();

		if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.SOLO_VAR)){
			// Se estima un VAR en variables normalizadas
			// Se crean las series normalizadas de las variables del VAR y exogenas
			String[] nombresVarYExo = utilitarios.UtilArrays.yuxtaS(nombresVariables, nombresExogenas);
			for(String ns: nombresVarYExo){
				Serie s = seriesLeidas.get(ns);
				double cotaInf = cotaInfOutliers.get(ns);
				double cotaSup = cotaSupOutliers.get(ns);
				Serie sc = s.acota(cotaInf, cotaSup);
				String tt = tipoTransformacion.get(ns);
				DefPoblacionSerie dpob = defPoblaciones.get(ns);
				SerieTransformadaYTransformaciones snyt = MetodosSeries.normalizar(sc, tt, dpob);
				seriesNormalizadas.put(ns, snyt);
			}
			// Se carga el EstimadorVARExo con los datos de las series normalizadas
			for (int iv = 0; iv < nombresVariables.length; iv++) {
				for (int t = 0; t < cantDatos; t++) {
					datos[t][iv] = seriesNormalizadas.get(nombresVariables[iv]).getSerie().getDatos()[t];
				}
			}
			for (int iv = 0; iv < nombresExogenas.length; iv++) {
				for (int t = 0; t < cantDatos; t++) {
					datosExo[t][iv] = seriesNormalizadas.get(nombresExogenas[iv]).getSerie().getDatos()[t];
				}
			}
			boolean esIndep = false;
			eVAR.estimaVAR(ordenDelVAR, esIndep);
			// matSAgLin es la matriz definida en la definición del VAR procesados por el calculadorVE
			double[][] matSAgLin = calculadorVE.construyeAgregadorLinealParaVAR(ordenDelVAR, nombresVariables, nombresExogenas);

			if(tieneVEAgregadas){
				// calcula matriz de varianza muestral de las VE del VAR completo
				calculaMatSigmaV();

				// calcula la matriz T a partir de la matSAgLin, para que las variables de estado tengan varianza uno.
				calculaMatT(matSAgLin);

				// calcula matriz ME para esperanza condicional dado el estado
				// E(V│H )= Σ_V T * (T Σ_V T* )^(-1)  H = ME H
				calculaMatrizEspCond();

				// calcula matriz MV para varianza condicional dado el estado
				//  Var(V│H) = Σ_V - Σ_V T* [ (T Σ_V T*)^(-1) ] T Σ_V  =: MV
				calculaMatrizVarCond();

				calculaValoresHistDeVEAgr();
			}


		}else if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.VAR_Y_PVA)){
			// Se estima un VAR y un PVA en variables de estado agregadas
			// Se crean las variables de estado para el PVA

			//calculadorVE.construyeAgregadorLinealParaVAR(ordenDelVAR, nombresVariablesVAR, nombresVariablesExogenas)

		}else{
			System.out.println("Error en formaEstimacion");
			System.exit(1);
		}
	}
	
	/**
	 * Calcula la matriz Σ_V , de covarianza muestral de las variables de estado del 
	 * VAR completo 
	 *  Las series que se deben tomar son en su orden:
	 *  las variables del VAR rezagadas 1 paso
	 *  las variables del VAR rezagadas 2 pasos
	 *  ...... hasta llegar al orden del VAR
	 *  las variables exogenas.
	 *  
	 */
	public void calculaMatSigmaV(){
		ArrayList<Serie> seriesV = new ArrayList<Serie>();
		for(int il=1; il<=ordenDelVAR;il++){
			for(String nv: nombresVariables){
				Serie sv = seriesNormalizadas.get(nv).getSerie();
				seriesV.add(sv.rezagaL(il));
			}
		}
		for(String ne: nombresExogenas){
			seriesV.add(seriesNormalizadas.get(ne).getSerie());
		}
		sigmaV = MetodosSeries.covarianza(seriesV);
	}
	
	/**
	 * Calcula la matriz T a partir de la matSAgLin
	 * para que las variables de estado tengan varianza uno.
	 * Para eso multiplica cada fila de matSAgLin por un número.
	 * S es matSAgLin 
	 * Σ_HS = S Σ_V S* 
	 * Se divide la fila i-esima de S por la raíz de elemento 
	 * diagonal Σ_HS(i,i)
	 * 
	 * @return
	 */
	public void calculaMatT(double[][] matSAgLin){
		double[][] aux = Oper.prod(sigmaV, Oper.transpuesta(matSAgLin));
		double[][] sigmaHS = Oper.prod(matSAgLin, aux);
		int cantFil = matSAgLin.length;
		double[] v = new double[cantFil];
		for(int i=0; i<cantFil; i++){
			v[i] = 1/Math.sqrt(sigmaHS[i][i]);
		}
		matTAgLin = Oper.escalaFilas(v, matSAgLin);
	}
	
	/**
	 *  Calcula la matriz ME para esperanza condicional dado el estado H
	 *  E(V│H)= Σ_V T* (T Σ_V T* )^(-1)H =: ME H
	 *  Entonces
	 *  ME = Σ_V T* (T Σ_V T* )^(-1) 
	 *  
	 *  Σ_V es la matriz de covarianzas muestrales de las VE totales del VAR.
	 *  T es la matriz de agregación de las VE
	 *  
	 * @return
	 */ 	
	public void calculaMatrizEspCond(){
		double[][] ttrans = Oper.transpuesta(matTAgLin);
		double[][] aux1 = Oper.prod(sigmaV, ttrans);
		double[][] aux2 = Oper.prod(matTAgLin, aux1);
		double[][] inv = Oper.inv(aux2);
		double[][] aux3 = Oper.prod(ttrans,inv);
		matrizEspCondicional = Oper.prod(sigmaV, aux3);
	}
	
	/**
	 *  Calcula la matriz MV que sirve para calcular la varianza condicional de V dado H
     *  MV es la matriz 
	 *  MV := Var(V│H) = Σ_V - Σ_V T* [ (T Σ_V T*)^(-1) ] T Σ_V  
	 *  Σ_V es la matriz de covarianzas muestrales de las VE del VAR completo.
	 *  T es la matriz de agregación de las VE
	 *  
	 *  Calcula también dBC y bVC las matrices de los valores propios y vectores propios 
	 *  de la descomposición espectral de MV
	 */		
	public void calculaMatrizVarCond(){
		double[][] ttrans = Oper.transpuesta(matTAgLin);
		double[][] aux1 = Oper.prod(sigmaV, ttrans);
		double[][] aux2 = Oper.prod(matTAgLin, aux1);
		double[][] inv = Oper.inv(aux2);
		double[][] aux3 = Oper.prod(matTAgLin, sigmaV);
		double[][] aux4 = Oper.prod(inv, aux3);
		double[][] aux5 = Oper.prod(ttrans, aux4);
		double[][] aux6 = Oper.prod(sigmaV, aux5);
		matrizVarCondicional = Oper.suma(sigmaV, Oper.opuesta(aux6));
		
		dVC = new double[cantVariables][cantVariables];
		dVCC = new double[cantVariables][cantVariables];
		bVC = new double[cantVariables][cantVariables];
		ordinalDeValoresPropiosNulos = new ArrayList<Integer>();
		double[][] bVCt = new double[cantVariables][cantVariables];		
		matrices.Oper.eigenDecomp(matrizVarCondicional, dVC, bVC, bVCt);
		for(int iv=0; iv<cantVariables; iv++){
			if(dVC[iv][iv]<utilitarios.Constantes.EPSILON_VAL_PROPIO){
				ordinalDeValoresPropiosNulos.add(iv);
				dVC[iv][iv] = 0.0;
			}else{
				dVCC[iv][iv] = dVC[iv][iv];
			}
		}
		/**
		 * Verifica varianza 1 de variables de estado agregadas H
		 * H = TV , Var(H) = T Var(V) T*
		 */
		double[][] varH = Oper.prod(matTAgLin, Oper.prod(sigmaV, Oper.transpuesta(matTAgLin)));
		for(int ie=0; ie<cantVEAgregadas; ie++){
			System.out.println("VARIANZA DE VARIABLE AGREGADA " + nombresVEAgregadas[ie] + " =" + varH[ie][ie]);
		}
	}
	
	/**
	 * Calcula los valores históricos de las variables de estado agregadas
	 * y los carga en el Hashtable seriesVEagHistoricas
	 */
	public void calculaValoresHistDeVEAgr(){
		seriesVEAgHistoricas = new Hashtable<String, Serie>();
		String[] nombresVariablesYExo = matrices.Oper.yuxtaListas(nombresVariables, nombresExogenas);
		ArrayList<Serie> lista = new ArrayList<Serie>();
		for(String s: nombresVariablesYExo){
			lista.add(seriesNormalizadas.get(s).getSerie());
		}
		ArrayList<Serie> aux = MetodosSeries.transLineal(matTAgLin, nombresVEAgregadas, lista);
		for(Serie sr: aux){
			seriesVEAgHistoricas.put(sr.getNombre(), sr);
		}	
	}
	
	/**
	 * Escribe los archivos DatosGenerales en subdirectorios con los nombres
	 * de los procesos de optimizacion y simulacion.by
	 * Si no existen los subdirectorios los crea.
	 * Si el proceso se usa tanto en simulación como en optimización crea un solo proceso
	 * @param dirSalidas
	 */
	public void escribeArchivosDatosGeneralesPE(String dirSalidas){
		System.out.println("--->"+dirSalidas);
		// Escribe salidas para proceso estocástico de simulación
		String dirPESim = dirSalidas + "/" + nombrePESimul;
		if(!DirectoriosYArchivos.existeDirectorio(dirPESim)) DirectoriosYArchivos.creaDirectorio(dirSalidas, nombrePESimul);
		// Cuando el VAR se usa en simulación son VE todas las VA rezagadas y las exógenas
		String[] nombresVE = new String[cantVariables*ordenDelVAR+cantExogenas];
		int is=0;
		for(int ir=1; ir<= ordenDelVAR; ir++){
			for(int iv=0; iv<cantVariables; iv++){
				nombresVE[is] = nombresVariables[iv] + "-L" + ir;
				is++;
			}
		}
		boolean usoSim = true;
		boolean usoOpt = false;
		boolean usoTransformaciones = true;
		boolean usaVEEnOptim = true;
		boolean discretoExhaustivo = false;
		boolean tieneVEContinuas = true;
		int prioridadSorteos = 1;   // OJO DEBERÍA TENER NUMERO MAYOR QUE SUS PROCESOS EXOGENOS
		EscritorTextosGeneralesPE.escribeDatosGeneralesPE(dirPESim, nombreEstimacion, 
				usoSim, usoOpt, nombrePEOptim, usoTransformaciones, 
				nombresVariables, nombresVE, usaVEEnOptim, discretoExhaustivo, 
				tieneVEContinuas, prioridadSorteos, nombrePaso, tieneExogenas, 
				nombresExogenas, nombresProcesosExogenas);		
		
		// Escribe salidas para proceso estocástico de optimizacion
		String dirPEOpt = dirSalidas + "/" + nombrePEOptim;
		if(!DirectoriosYArchivos.existeDirectorio(dirPEOpt)) DirectoriosYArchivos.creaDirectorio(dirSalidas, nombrePEOptim);		
		nombresVE = nombresVEAgregadas;
		usoSim = false;
		usoOpt = true;
		usoTransformaciones = true;
		usaVEEnOptim = true;
		discretoExhaustivo = false;
		tieneVEContinuas = true;
		prioridadSorteos = 1;   // OJO DEBERÍA TENER NUMERO MAYOR QUE SUS PROCESOS EXOGENOS 
		EscritorTextosGeneralesPE.escribeDatosGeneralesPE(dirPEOpt, nombreEstimacion, 
				usoSim, usoOpt, nombrePEOptim, usoTransformaciones, 
				nombresVariables, nombresVE, usaVEEnOptim, discretoExhaustivo, 
				tieneVEContinuas, prioridadSorteos, nombrePaso, tieneExogenas, 
				nombresExogenas, nombresProcesosExogenas);	
		
		// Escribe salidas para proceso historico asociado 
		String dirPEHist = dirSalidas + "/" + nombrePEHist;
		if(!DirectoriosYArchivos.existeDirectorio(dirPEHist)) DirectoriosYArchivos.creaDirectorio(dirSalidas, nombrePEHist);		
		nombresVE = nombresVEAgregadas;
		usoSim = true;
		usoOpt = false;
		usoTransformaciones = false;
		usaVEEnOptim = true;
		discretoExhaustivo = false;
		tieneVEContinuas = true;
		prioridadSorteos = 1;   // OJO DEBERÍA TENER NUMERO MAYOR QUE SUS PROCESOS EXOGENOS	
		EscritorTextosGeneralesPE.escribeDatosGeneralesPE(dirPEHist, nombreEstimacion, 
				usoSim, usoOpt, nombrePEOptim, usoTransformaciones, 
				nombresVariables, nombresVE, usaVEEnOptim, discretoExhaustivo, 
				tieneVEContinuas, prioridadSorteos, nombrePaso, tieneExogenas, 
				nombresExogenas, nombresProcesosExogenas);			
		
		
	}
	
	
	
	public void escribeTextosParaDatatypes(String dirSalidas){
		ArrayList<Serie> listaVA = new ArrayList<Serie>();
		ArrayList<Serie> listaVE = new ArrayList<Serie>();
		String dirPSimul = dirSalidas + "/" + nombrePESimul;
		String dirPOptim = dirSalidas + "/" + nombrePEOptim;
		String dirPHist = dirSalidas+ "/" + nombrePEHist;
		// Escribe los archivos DatosGenerales.txt de los procesos 
		// de simulación optimización e histórico asociado al de optimización
		escribeArchivosDatosGeneralesPE(dirSalidas);	
		
		// Escribe los restantes archivos que dependen de la formaEstimacion
		if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.SOLO_VAR)){
				
			String textoParam = eVAR.devuelveTextoParametrosVAR(nombresVariables, nombresExogenas, formaEstimacion);
			// imprime las cotas para la generación de realizaciones
			StringBuilder sb = new StringBuilder();
			for(String nv :nombresVariables){
				sb.append("COTAS_REALIZACIONES" + "\t" + nv + "\t" + cotaInfRealiz.get(nv) +
				"\t"  + cotaSupRealiz.get(nv) + "\n");
			}
			textoParam = textoParam + sb.toString();

			// Escribe parámetros del VAR en el proceso de simulación
			String archS = dirPSimul + "/parametros.txt";
			DirectoriosYArchivos.siExisteElimina(archS);
			DirectoriosYArchivos.agregaTexto(archS, textoParam);
			
			// Escribe parámetros del VAR en el proceso de simulación solo por compatibilidad
			// porque no se usan
			String archO = dirPOptim + "/parametros.txt";
			DirectoriosYArchivos.siExisteElimina(archO);
			DirectoriosYArchivos.agregaTexto(archO, textoParam);
			
			for(String nva: nombresVariables){
				listaVA.add(seriesLeidas.get(nva));
			}
			
			// Escribe matrices de la distribución condicionada para el proceso de optimización
			if(tieneVEAgregadas){
				escribeMatricesDistCondicionada(dirPSimul);
				escribeMatricesDistCondicionada(dirPOptim);
			}
			
			// Escribe discretización de las VE para el proceso de optimización
			escribeDiscretizaciones(dirPOptim);	
			escribeDiscretizaciones(dirPSimul);	
			
			// Escribe texto para CargadorProcesoHistorico
			if(tieneVEAgregadas){
				// Si no hay variables de estado agregadas, las variables de estado del ProcesoHistorico son las propias variables aleatorias
				for(String nve: nombresVEAgregadas){
					listaVE.add(seriesVEAgHistoricas.get(nve));
				}
			}
			EscritorTextosGeneralesPE.escribeDatosPEHistorico(nombreEstimacion,
					dirPHist, cantCron, cantDatos, listaVA, listaVE);	
			
			// Escribe texto para CargadorAgregadorLineal de procesos de simulación 
			// Si no hay variables agregadas, el mismo VAR se usa en simulacion y optimización
			// y no se requiere AgregadorDeEstados
			if(tieneVEAgregadas){
				persistencia.EscritorTextosGeneralesPE.escribeTextoAgregadorLineal(dirPSimul, nombreEstimacion,
					nombrePESimul, nombrePEOptim, nombresVariables,
					nombresExogenas, nombresVEAgregadas, matTAgLin);
				persistencia.EscritorTextosGeneralesPE.escribeTextoAgregadorLineal(dirPOptim, nombreEstimacion,
					nombrePESimul, nombrePEOptim, nombresVariables,
					nombresExogenas, nombresVEAgregadas, matTAgLin);
			}
			// Para el proceso histórico la matriz de agregación es la matriz identidad.
			// TODO: HAY PROBLEMA CON LAS VARIABLES EXOGENAS
			double[][] matHist = new double[cantVEAgregadas][cantVEAgregadas];
			for(int ive=0; ive<cantVEAgregadas; ive++){
				matHist[ive][ive] = 1.0;
			}
			// Para el proceso histórico las VE son las mismas del proceso VAR que se usa en la optimización
			persistencia.EscritorTextosGeneralesPE.escribeTextoAgregadorLineal(dirPHist, nombreEstimacion,
					nombrePEHist, nombrePEOptim, nombresVEAgregadas,
					nombresExogenas, nombresVEAgregadas, matHist);
			Hashtable<String, ArrayList<ArrayList<Double>>> parametros = new Hashtable<String, ArrayList<ArrayList<Double>>>();
			String[] nombresSeries = matrices.Oper.yuxtaListas(nombresVariables, nombresExogenas);
			for(String ns: nombresSeries){
				SerieTransformadaYTransformaciones styt = seriesNormalizadas.get(ns);
				// cada paso tiene una transformación
				ArrayList<TransformacionVA> trs = styt.getTransformaciones();
				ArrayList<ArrayList<Double>> p1 = new ArrayList<ArrayList<Double>>();
				for(int ip=0; ip<cantPasos; ip++){
					TransformacionVA trans = trs.get(ip);
					p1.add(trans.dameParametros());
				}
				parametros.put(ns, p1);
			}
			
			EscritorTextosGeneralesPE.escribeTransformaciones(dirPSimul, nombreEstimacion, 
					tipoTransformacion, nombresSeries, nombrePaso, cantPasos, parametros);
			EscritorTextosGeneralesPE.escribeTransformaciones(dirPOptim, nombreEstimacion, 
					tipoTransformacion, nombresSeries, nombrePaso, cantPasos, parametros);
			
			
		}else{	
			/**
			 * ACA VA LA OPCIÓN PVA_Y_VAR
			 */
		}
	}
	

	/**
	 * Imprime las matrices para calcular la esperanza condicionada y la
	 * varianza condicionada dado el estado H
	 */
	public void escribeMatricesDistCondicionada(String directorio){
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos();
		StringBuilder sb = new StringBuilder("// MATRIZ PARA CALCULAR ESPERANZA CONDICIONAL\n");
		sb.append("MAT_ESP_COND\n");
		sb.append(ale.escribeMatrizReal(matrizEspCondicional," "));
		sb.append("// MATRIZ PARA CALCULAR VARIANZA CONDICIONAL\n");
		sb.append("MAT_VAR_COND\n");
		sb.append(ale.escribeMatrizReal(matrizVarCondicional," "));
		sb.append("MAT_VALPROP\n");
		sb.append(ale.escribeMatrizReal(dVC," "));
		sb.append("MAT_VALPROP_C\n");
		sb.append(ale.escribeMatrizReal(dVCC," "));		
		sb.append("MAT_VECPROP\n");
		sb.append(ale.escribeMatrizReal(bVC," "));		
		
		String dirArchivo = directorio + "/" + "matricesDistCondicionada.txt";
		if(DirectoriosYArchivos.existeArchivo(dirArchivo)) 
			DirectoriosYArchivos.eliminaArchivo(dirArchivo);
		DirectoriosYArchivos.agregaTexto(dirArchivo, sb.toString());
	}
	
	
	public void escribeDiscretizaciones(String dirPOptim){
		if(tieneVEAgregadas){
			int cantVE = nombresVEAgregadas.length;
			double[][] valores = new double[cantVE][];
			double[] min = new double[cantVE];
			double[] max = new double[cantVE];
			boolean[] equiespaciada = new boolean[cantVE];
			int[] cantPuntos = new int[cantVE];
			for(int ive=0; ive<cantVE; ive++){
				String nve = nombresVEAgregadas[ive];
				double[] disc = percentilesDiscretizaciones.get(nve);
				int cantVD = disc.length;
				double[] auxPer = new double[cantVD];
				for(int id=0; id<cantVD; id++){
					auxPer[id] = procEstocUtils.DistribucionNormal.inversacdf2(disc[id]);
				}	
				valores[ive]=auxPer;
				min[ive]=UtilArrays.minimo(auxPer);
				max[ive]=UtilArrays.maximo(auxPer);
				cantPuntos[ive]=cantVD;
			}
			EscritorTextosGeneralesPE.escribeTextoDiscretizacionVE(dirPOptim, nombresVEAgregadas, min,  max,
				 cantPuntos, equiespaciada, valores);			
		}else{
			// las variables de estado de la optimización son las mismas del VAR de la simulación
			int cantVE = nombresVariables.length;
			double[][] valores = new double[cantVE][];
			double[] min = new double[cantVE];
			double[] max = new double[cantVE];
			boolean[] equiespaciada = new boolean[cantVE];
			int[] cantPuntos = new int[cantVE];
			for(int ive=0; ive<cantVE; ive++){
				String nve = nombresVariables[ive];
				double[] disc = percentilesDiscretizaciones.get(nve);
				int cantVD = disc.length;
				double[] auxPer = new double[cantVD];
				for(int id=0; id<cantVD; id++){
					auxPer[id] = procEstocUtils.DistribucionNormal.inversacdf2(disc[id]);
				}	
				valores[ive]=auxPer;
				min[ive]=UtilArrays.minimo(auxPer);
				max[ive]=UtilArrays.maximo(auxPer);
				cantPuntos[ive]=cantVD;
			}
			EscritorTextosGeneralesPE.escribeTextoDiscretizacionVE(dirPOptim, nombresVariables, min,  max,
				 cantPuntos, equiespaciada, valores);	
		}
	}

	/**
	 * Escribe estadístico de las series originales a emplear para la verificacion
	 * de las simulaciones que se hagan con el proceso estimado
	 */
	public void imprimeResultVerif(String dirSalVerif){		
		MetodosSeries.imprimeEstadisticosSeries(nombresSeriesLeidas, seriesLeidas, dirSalVerif);
	}
	
	
	/**
	 * Recibe los directorios: 
	 * -de entrada de datos 
	 * -de salida de resultados de estimación del VAR 
	 * -de salida de resultados del PVA asociado 
	 * -de salida de resultados de un proceso histórico asociado al PVA
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		EstimadorVARenVNormalizadas eVARVN = new EstimadorVARenVNormalizadas();
		Date fecha1 = new Date();
		eVARVN.nombreEstimacion = "VARVN-"	+ fecha1.toString();
		String dirArchConf = "resources/ESTIMADORES.conf";
		String nombreProp = "rutaVAR";

//		 // Elige directorios de entradas y salidas
		
//		 String nombrePESimul = utilsVentanas.VentanaEntradaString.leerTexto("ENTRE NOMBRE DEL PROCESO PARA SIMULACION");
//		 String nombrePEOptim = utilsVentanas.VentanaEntradaString.leerTexto("ENTRE NOMBRE DEL PROCESO PARA OPTIMIZACION");
//		 boolean soloDirectorio = true;
//		 String titulo1 = "ELIJA EL DIRECTORIO DE ENTRADA DE DATOS DONDE LEER ARCHIVOS:   ";
//		 String dirEntradas = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio,
//		 titulo1, dirArchConf, nombreProp);
//		
//		 String titulo2 ="ELIJA EL DIRECTORIO DE SALIDA DE LA ESTIMACION DEL VAR, ARCHIVOS:    ";
//		 String dirSalidas = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio,
//		 titulo2, dirArchConf, nombreProp);
//		
//		
//		 String titulo3 = "ELIJA EL DIRECTORIO PARA SALIDA DEL PROCESO EN VARIABLES AGREGADAS ASOCIADO, ARCHIVO .......xlt";
//		 String dirPVA = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio,
//		 titulo3, dirArchConf, nombreProp);
//		 
//		 String titulo4 = "ELIJA EL DIRECTORIO PARA SALIDA DEL PROCESO HISTORICO ASOCIADO, ARCHIVO .......xlt";
//		 String dirProcHist = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio,
//		 titulo3, dirArchConf, nombreProp);

		
		dirEntradas = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\DATOS-ESTABLES\\PROCESOS ESTOCASTICOS\\ESTIMACION DE PROCESOS\\3-Estimados agosto 2023\\VAR APORTES ENSO CMGBR 3VEH\\Entradas";
		dirSalidas = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\DATOS-ESTABLES\\PROCESOS ESTOCASTICOS\\ESTIMACION DE PROCESOS\\3-Estimados agosto 2023\\VAR APORTES ENSO CMGBR 3VEH\\Salidas";
		dirPVA = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\DATOS-ESTABLES\\PROCESOS ESTOCASTICOS\\ESTIMACION DE PROCESOS\\3-Estimados agosto 2023\\VAR APORTES ENSO CMGBR 3VEH\\Salidas";
		dirProcHist = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\DATOS-ESTABLES\\PROCESOS ESTOCASTICOS\\ESTIMACION DE PROCESOS\\3-Estimados agosto 2023\\VAR APORTES ENSO CMGBR 3VEH\\Salidas";
		nombrePESimul = "varAportesENSOBR5C-ENSOAgosto-3VE";
		nombrePEOptim = "varAportesENSOBROptim5C-ENSOAgosto-3VE";
		nombrePEHist = "varAportesENSOBRHist5C-ENSOAgosto-3VE";
		
		/**
		 * Como se explica en Preliminares/48-Vnn-... si el VAR se usa tanto en simulación
		 * como en optimización, pero con distinta cantidad de variables de estado, porque
		 * hay un agregador lineal que no es la identidad, DEBE FORMALMENTE APARECER COMO
		 * UN PROCESO DISTINTO EN SIMULACION QUE EN OPTIMIZACION
		 */

		// Estima proceso VAR y eventualmente PVA
		eVARVN.estima();

		// Crea textos legibles por CargadorPEVAR y CargadorPEHistorico 
		eVARVN.escribeTextosParaDatatypes(dirSalidas);
		
		
		// Crea salidas de verificación de resultados

		String dirSalVerif = dirSalidas + "/salidasVerif";
		if (!DirectoriosYArchivos.existeDirectorio(dirSalVerif))
			DirectoriosYArchivos.creaDirectorio(dirSalidas, "salidasVerif");
		eVARVN.imprimeResultVerif(dirSalVerif);



//		// Copia el archivo DatosSal.xlt al directorio dirHist
//		String origen = dirSalidas + "/datosProcAgr.xlt";
//		String destino = dirPVA + "/datosProcAgr.xlt";
//		try {
//			DirectoriosYArchivos.copy2(origen, destino);
//		} catch (Exception e) {
//			System.out.println("Error al copiar el archivo " + origen);
//			System.exit(1);
//		}

		System.out.println("TERMINA LA TOTALIDAD DEL PROCESO DE ESTIMACIóN");

	}

	public static boolean estimar(String dirEntradas, String dirPVA, String nombrePESimul, String nombrePEOptim, String nombrePEHist) {
		EstimadorVARenVNormalizadas eVARVN = new EstimadorVARenVNormalizadas();
		Date fecha1 = new Date();
		eVARVN.nombreEstimacion = "VARVN-"	+ fecha1.toString();
		String dirArchConf = "resources/ESTIMADORES.conf";
		String nombreProp = "rutaVAR";


		String dirResources = "";
		LectorPropiedades lprop = new LectorPropiedades(".\\resources\\mop.conf");
		try {
			dirResources = lprop.getProp("rutaEntradas") + "\\resources\\";
			dirSalidas= dirResources ;



		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		eVARVN.dirEntradas = dirEntradas;
		eVARVN.dirSalidas = dirSalidas;
		eVARVN.dirPVA = dirPVA;
		eVARVN.dirProcHist = dirProcHist;
		eVARVN.nombrePESimul = nombrePESimul;
		eVARVN.nombrePEOptim = nombrePEOptim;
		eVARVN.nombrePEHist = nombrePEHist;



		/**
		 * Como se explica en Preliminares/48-Vnn-... si el VAR se usa tanto en simulación
		 * como en optimización, pero con distinta cantidad de variables de estado, porque
		 * hay un agregador lineal que no es la identidad, DEBE FORMALMENTE APARECER COMO
		 * UN PROCESO DISTINTO EN SIMULACION QUE EN OPTIMIZACION
		 */

		// Estima proceso VAR y eventualmente PVA
		eVARVN.estima(dirEntradas);

		// Crea textos legibles por CargadorPEVAR y CargadorPEHistorico
		eVARVN.escribeTextosParaDatatypes(dirSalidas);


		// Crea salidas de verificación de resultados

		String dirSalVerif = dirSalidas + "/salidasVerif";
		if (!DirectoriosYArchivos.existeDirectorio(dirSalVerif))
			DirectoriosYArchivos.creaDirectorio(dirSalidas, "salidasVerif");
		eVARVN.imprimeResultVerif(dirSalVerif);

		System.out.println("TERMINA LA TOTALIDAD DEL PROCESO DE ESTIMACIóN");
		return true;

	}
	
	
	

}
