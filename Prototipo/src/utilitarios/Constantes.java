/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Constantes is part of MOP.
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

package utilitarios;

import datatypesProcEstocasticos.*;
import persistencia.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Clase utilizada para almacenar todas las constantes
 * @author ut602614
 *
 */
public class Constantes {
	

	/**
	 * Constantes asociadas a procesos generales como simulación, optimización clósica, etc.
	 */
	public static final String FASE_SIM = "simulación";
	public static final String FASE_OPT = "optimización";
	
	/**
	 * Constantes generales de la corrida
	 */
	public static final String XML_CORRIDA = "corrida";
	public static final String XML_IDCORRIDA = "id";
	public static final String XML_NOMBRECORRIDA = "nombre";
	public static final String XML_DESCRIPCIONCORRIDA = "descripcion";
	public static final String XML_PARAMETROSCORRIDA= "parametrosCorrida";
	public static final String XML_CANTPOSTESCORRIDA= "cantidadPostes";
	public static final String XML_DURACIONPOSTESCORRIDA= "duracionPostes";
	public static final String XML_TIPORESOLUCIONCORRIDA= "tipoResolucionValoresBellman";
	public static final String XML_CONDICIONESITERACIONES = "condicionesIteraciones";
	public static final String XML_MAXITERACIONES = "maximoIteraciones";
	
	
	/**
	 * Nombres de las duraciones de los pasos usados en la interfase
	 */
		
	public static final String PASO_HORARIO = "HORARIO";
	public static final String PASO_DIARIO = "DIARIO";
	public static final String PASO_SEMANAL = "SEMANAL";

		
	/**COSTANTES ASOCIADAS A DIRECTORIOS DE ENTRADA Y SALIDA	
	 */
	//public static final String DIR_SALIDAS = "D:/SalidasModeloOp";
	
	/**Constantes asociadas a la corrida*/
	public static final String COMPVALORESBELLMAN = "tipoResolucionValoresBellman";
	public static final String COMPDEMANDA = "tipoDemanda";
	public static final String PROBHIPERPLANOS = "hiperplanos";
	public static final String PROBINCREMENTOS = "incrementos";
	public static final String DEMRESIDUAL = "residual";
	public static final String DEMTOTAL = "total";
	
	
	/**Constantes de los Tórmicos*/
	public static final String COMPMINTEC = "compMinimosTecnicos";
	public static final String TERSINMINTEC = "SinMinTec"; 
	public static final String TERMINTECFORZADO = "MinTecForzado";
	public static final String TERVARENTERAS = "VarEnteras";
	public static final String TERVARENTERASYVARESTADO = "VarEnterasYVarEstado";
	public static final String TERDOSPASOS="dosPasos"; //primera iteracion sinMinTec Segunda iteracion MinTecForzado
	public static final String COMPFLEXIBILIDAD = "flexibilidadMin";
	public static final String TERFLEXHORARIO = "horaria";
	public static final String TERFLEXSEMANAL = "semanal";
	
	
	
		
	/**Constantes de las iteraciones*/
	public static final String PORNUMEROITERACIONES = "porNumeroIteraciones";
	public static final String PORUNANIMIDADPARTICIPANTES = "porUnanimidadParticipantes";
	
	
	/**Constantes del acumulador*/
	public static final String COMPPASO = "compPaso";
	public static final String ACUCIERRAPASO = "cierraPaso";
	public static final String ACUMULTIPASO ="multiPaso";
	public static final String ACUBALANCECRONOLOGICO ="balanceCronologico";
	
	/**Constantes de las hidróulicas */
	public static final String COMPLAGO = "compLago";
	public static final String COMPCOEFENERGETICO = "compCoefEnergetico";
	public static final String HIDROCONLAGO = "ConLago";
	public static final String HIDROSINLAGO = "SinLago";
	public static final String HIDROSINLAGOENOPTIM = "SinLagoEnOptim";  // con lago en simulación y sin lago en optimización
	public static final String HIDROCOEFENERGCONSTANTES = "CoefEnergConstantes";
	public static final String HIDROPOTENCIACAUDAL = "PotenciaCaudal";
	public static final double FRACCION_APORTES_S0FINT_OPTIM = 1.0;  // fracción de los aportes que se incluyen en el volumen final en la heurística del s0fint    
	public static final double EPSILONVOLAGUA = 100.0;    // m3
	public static final double EPSILONVALAGUA = 0.0; // En USD/hm3, se toma como mónimo valor del agua de centrales con lago en el comportamiento simulación
	// public static final double EPSILONCAUDALAGUA = 0.01;    // m3/s
	public static final double EPSILONCAUDALAGUA = 0.000001;    // m3/s
	public static final double AUMENTO_ERO_ABAJO = 1.2; // Se aplica en la limitación de erogados
//	public static final double PENALIZACION_AGUA_DESTRUIDA = 0.01;  // En USD/m3 - Penaliza la destrucción de agua en centrales sin lago para poder usar restricción de desigualdad
	public static final double PENALIZACION_AGUA_DESTRUIDA = 0.01; // 0.000001
	public static final double TOLERANCIA_DESTRUCCION_AGUA = 6000;    // En m3 - Avisa si se destruye mós que esta cantidad en centrales sin lago
	public static final int TOLERANCIA_CREACION_AGUA = 6000;   // En m3 - Avisa si se fabrica mós que esta cantidad en centrales sin lago
	public static final double VARIA_EROGADO_ENTRE_POSTES = 1; // Porcentraje de variación del erogado para centrales sin lago 
	// por el turbinado móximo de centrales aguas abajo
	
	
	/**Constantes de contratos de combustible top*/
	public static final String COMPCOSTOSTOP = "compCostos";
	public static final String COSTOSCONVEXOS = "CostosConvexos";  // la función de costos del gas en el despacho es convexa
	public static final String COSTOSNOCONVEXOS ="CostosNoConvexos";
	
	
	/**Constantes de red de combustible y sus elementos*/
	public static final String COMPREDCOMB = "compRedComb";
	public static final String VARSPORPASO = "varsPorPaso";   // los elementos de la red tienen una variable por paso
	public static final String VARSPORSUBCONJPOSTES = "varsPorSubConjPostes";  // id. una variable por cada subconjunto de postes definidos
	
	public static final String COMPTANQUECOMP = "compTanqueComp";
	public static final String TANQUECIERRAPASO = "tanqueCierraPaso"; // un ónico cierre por paso
	public static final String TANQUECIERRASUBCONJPOSTES = "tanqueCierraSubConjPostes"; // un cierre en cada subconjunto de pasos
	
	
	/** Constantes del ciclo combinado */
	// Comportamientos generales
	public static final String COMPCC = "compCC";  // nombre de la variable de comportamiento general
	public static final String TGSMEJORES = "tgsmejores";   // Son TGs con potencia y rendimiento de turbinas combinadas, no hay ciclo de vapor
	public static final String CCSINESTADO = "ccsinestado";  // Se elige el uso de las TG en ciclo abierto o combinado
	public static final String CCCONESTADO = "ccconestado";   //  Variables de estado de TGs combinadas inicialmente y perfiles de rampa
	public static final String CCDETALLADO = "ccdetallado";   // Se hará algún día a partir del convenio de corto plazo
	
	
	/**Red*/
	public static final String COMPRED = "compUsoRed";
	public static final String CONRED = "conRed";
	public static final String UNINODAL = "uninodal";
	public static final String RED_ELECTRICA = "redElectrica";  // NOMBRE DEL PARTICIPANTE RED ELÉCTRICA 
	
	/**Comportamientos Rama*/
	public static final String COMPRAMA = "compRama";
	public static final String RAMASIMPLE = "simple";
	public static final String RAMADC = "dc";
	
	/**Comportamientos Falla*/
	public static final String COMPFALLA= "compFalla";
	public static final String FALLA_CONESTADO_SINDUR= "conEstadoSinDur";
	public static final String FALLA_CONESTADO_CONDUR= "conEstadoConDur";
	public static final String FALLASINESTADO= "sinEstado";
	

	
	/**BINARIA, ENTERA, CONTINUA*/
	
	/**Tipos de variable de control para resolver el problema lineal*/
	public static final int VCESTANDAR = 0;
	public static final int VCENTERA = 1;
	public static final int VCCONTINUA = 2;
	
	public static final int VCLIBRE = 3;
	public static final int VCPOSITIVA = 4;
	public static final int VCSEMICONTINUA = 5;
	public static final int VCBINARIA = 6;
	public static final String[] TIPOSVC = {"ESTANDAR", "ENTERA", "CONTINUA", "LIBRE", "POSITIVA", "SEMICONTINUA", "BINARIA"};
	
	
	/**Tipos de restriccion*/
	public static final int RESTMENOROIGUAL = -1;
	public static final int RESTIGUAL = 0;
	public static final int RESTMAYOROIGUAL = 1;
	public static final String[] TIPOSREST = {"MENOROIGUAL" , "IGUAL" , "MAYOROIGUAL"};
	
	
	/**Tipos de valPostizacion*/
	public static final int VALPPROMEDIO = 0;
	public static final int VALPALEAT = 1;
	public static final int VALPMIN = 2;
	public static final int VALPMAX = 3;
	
	
	/**Tipos de palier de importación o exportación*/
	public static final String IEEVOL = "IEEVOL";
	public static final String IEALEATFORMUL = "IEALEATFORMUL";
	public static final String IEALEATPRPOT = "IEALEATPRPOT";
	

	/** Factores de conversión*/
	public static final int MINUTOSXHORA = 60;
	public static final int SEGUNDOSXMINUTO = 60;
	public static final int SEGUNDOSXHORA = 60*60;
	public static final int SEGUNDOSXDIA = 24*60*60;
	public static final int SEGUNDOSXSEMANA = 24*7*60*60;
	public static final int SEGUNDOSXANIO = 24*365*60*60;
	public static final int MWHXGWH = 1000;
	public static final int USDXMUSD = 1000000;
	public static final int USDXkUSD = 1000;
	public static final int KWXMW = 1000;
	public static final int HORASXDIA = 24;
	public static final int M2XKM2 = 1000000;
	
	
	
	public static final String NOMBREBARRAUNICA = "barraUnica";
	public static final double EPSILONCOEF = 1E-8;
	public static final double DESLIZAMIENTOMUESTREO = 0.5;
	public static final String PROVVENTA = "venta";
	public static final String PROVCOMPRA = "compra";
	public static final int EPSILONSALTOTIEMPO = 1;

	
	/** Duración de pasos de procesos estocósticos*/
	public static final String PASOSEMANA = "SEMANA";
	public static final String PASODIA = "DIA";
	public static final String PASOHORA = "HORA";	
	public static final String PASOMES = "MES";
	public static final ArrayList<String> NOMBRESPASOS = new ArrayList<>(Arrays.asList(PASOSEMANA, PASODIA, PASOHORA, PASOMES)); 
	public static Hashtable<String, Integer> CANTMAXPASOS = new Hashtable<String, Integer>();  // cantidad máxima de pasos por año según el nombre del paso
	public static Hashtable<String, Integer> SEGSPORPASO =  new Hashtable<String, Integer>(); // Duración en segundos de hora, día y semana

	/**
	 * Cantidad máxima de pasos en un año según la duración
	 */
	public static final int SEMANASPORANIOMAX = 52;
	public static final int DIASPORANIOMAX = 366;
	public static final int HORASPORANIOMAX = 8784;
	public static final int MESESPORANIOMAX = 12;

	static {
		CANTMAXPASOS.put(PASOSEMANA, SEMANASPORANIOMAX);
		CANTMAXPASOS.put(PASODIA, DIASPORANIOMAX);
		CANTMAXPASOS.put(PASOHORA, HORASPORANIOMAX);
		CANTMAXPASOS.put(PASOMES, MESESPORANIOMAX);
		SEGSPORPASO.put(PASOSEMANA, SEGUNDOSXSEMANA);
		SEGSPORPASO.put(PASODIA, SEGUNDOSXDIA);
		SEGSPORPASO.put(PASOHORA, SEGUNDOSXHORA);
	}
	
	public static final int CANT_DIAS_ANIO_NOBISIESTO = 365;
	public static final int CANT_HORAS_DIA = 24;
	public static final int CANT_MESES_ANIO = 12;
	    
	// para cada semana del año, el número de mes al que pertenece, del 1 al 12.
	public static final int[] DEF_MESES = 
			{1, 1, 1, 1, 1, 2, 2, 2, 2,       //9
			 3, 3, 3, 3, 4, 4, 4, 4,          //17
			 5, 5, 5, 5, 5, 6, 6, 6, 6,       //26
			 7, 7, 7, 7, 8, 8, 8, 8, 8,       //35
			 9, 9, 9, 9, 10, 10, 10, 10, 11,   //44
			 11, 11, 11, 12, 12, 12, 12, 12};  //52
	
	public static final int[] CANT_SEM_MESES = {5,4,4,4,5,4,4,5,4,4,4,5};


	public static final int[] SEG_PASOS_SEMANALES = {604800,691200, 777600};  // segundos transcurridos en 7, 8 y 9 dias
	public static final int SEG_PASO_DIARIO = 86400;  // segundos transcurridos en 24 hs
	
	
	/**
	 * Constantes para agregar grupos de series que definen variables de estado de procesos estocásticos
	 */
	public static final String COMBINACION_LINEAL = "COMBINACION_LINEAL";
	public static final String MINIMO = "MINIMO";
	
	/**FISICAS*/
	public static final double G = 9.80655;
	public static final double DENSIDADDELAGUA = 1000;
	public static final double PESOESPDELAGUA = G*DENSIDADDELAGUA;
	public static final double M3XHM3 = 1000000;
	public static final double CONHM3AM3 = 1000000;
	public static final double INFNUESTRO = 1e12;
	
	
	
	/**
	 * Períodos para agrupar las salidas
	 */
	public static final String SEMANA = "SEMANA";
	public static final String MES = "MES";
	public static final int cantMesAnio = 12;
	
	
	/**
	 * Constantes de parómetros de la salida provisoria
	 *
	 * ATENCION MANOLO: ACA INVOCA AL MóTODO QUE CREA LOS RESULTADOS RESUMIDOS
	 * 
	 * param es int[]; 0 indica que no se produce la salida, 1 indica que só.
	 * 
	 *          nombre archivo
	 * param[0]	ener_resumen  	energóa anual promedio en los escenarios; filas recurso; columnas aóo
	 * param[1]	ener_cron  		energóa por aóo y escenario para todos los recursos: filas aóo,escenario; columnas recurso   
	 * param[2]	pot				para recursos en particular, un archivo por poste, filas paso, columnas poste
	 * param[3]					lista de enteros int[] con los indicadores de los recursos para los que se va a sacar el archivo de pot
	 * 
	 * param[4]	costo_resumen 	costo anual promedio en los escenarios; filas recurso; columnas aóo
	 * param[5]	costo_cron 		costo por aóo y escenario para todos los recursos: filas (aóo,escenario); columnas recurso   
	 * param[6]	costo_poste		para recursos en particular, un archivo por poste, filas paso, columnas poste
	 * param[7]					lista de enteros int[] con los indicadores de los recursos para los que se va a sacar el archivo de costo_poste		 
	 *
	 * param[8]	cosmar_resumen filas paso; columnas poste; (los promedios segón cantidad de horas = curva plana)
	 * param[9]	cosmar_cron    un archivo por poste, filas paso, columnas crónicas
	 * 
	 * param[10]		       lista de enteros int[] con los indices de las barras para los que se va a sacar los costos marginales detallados	 
	 * 
	 * param[11]	Si es =1 genera un directorio cantMod, con un archivo de disponibilidades para cada recurso
	 * 				En esos archivos las filas son pasos y las columnas son escenarios (crónicas)
	 * param[12]    lista de enteros int[] con los óndices de los recursos para los que se sacan atributos detallados
	 * 
	 * param[13]    Si es =1 genera el archivo de salidas detalladas por cada paso SalidaDetalladaSP
	 * param[14]    Si es =1 genera el archivo de costo por paso y por crónica
	 * 
	 */
	
	
	public static final int PARAMSAL_RESUMEN = 0;
	public static final int PARAMSAL_ENERCRON = 1;
	public static final int PARAMSAL_POT = 2;	
	public static final int PARAMSAL_IND_POT = 3;		
	public static final int PARAMSAL_COSTO_RESUMEN = 4;
	public static final int PARAMSAL_COSTO_CRON = 5;
	public static final int PARAMSAL_COSTO_POSTE = 6;	
	public static final int PARAMSAL_IND_COSTO_POSTE = 7;	
	public static final int PARAMSAL_COSMAR_RESUMEN = 8;
	public static final int PARAMSAL_COSMAR_CRON = 9;
	public static final int PARAMSAL_IND_COSMAR_CRON = 10;	
	public static final int PARAMSAL_CANTMOD = 11;
	public static final int PARAMSAL_IND_ATR_DET = 12;
	public static final int PARAMSAL_SALIDA_DET_PASO = 13;
	public static final int PARAMSAL_COSTO_PASO_CRON = 14;
	public static final int PARAMSAL_CANT_PARAM = 50;   // Constante para dimensionar el array de parómetros
	
	/**
	 * CONSTANTES PARA FORMATEAR LA SALIDA DETALLADA POR PASO Y POSTE DE ATRIBUTOS DETALLADOS
	 * Si es true cada poste es una fila en la salida detallada y las columnas son escenarios
	 * Si es false cada paso es una fila y en las columnas se anidan escenarios y postes
	 */
	public static final boolean UN_POSTE_POR_FILA = true;  
	
	/**
	 * NIVEL DE DETALLE EN LO QUE SE IMPRIME EN CONSOLA
	 * 
	 * 0: NADA
	 * 1: SOLO LO INDISPENSABLE
	 * 2: TODO, INCLUSO LO DE PRUEBA
	 */
	
	public static final int NIVEL_CONSOLA = 0;  
	
	
	/**
	 * Prefijos para indicar tipos de recursos (o afines ya que está la falla también)
	 */
	public static final String TER = "TER";
	public static final String CC = "CC";
	public static final String HID = "HID";
	public static final String PROV = "PROV";
	public static final String DEM = "DEM";
	public static final String FALLA = "FALLA";
	public static final String EOLO = "EOLO";
	public static final String FOTOV = "FOTOV";
	public static final String IMPACTO = "IMPACTO";
	public static final String CONTRATOENERGIA = "CONTRATOENERGIA";
	public static final String CONTRATOINTERRUMPIBLE = "CONTRATOINTERRUMPIBLE";
	
	public static final String ACUM = "ACUM";
	public static final String IMPOEXPO = "IMPOEXPO";
	
	public static final String CONST_HIPERPLANOS = "CONST_HIPERPLANOS";
	
	// Operaciones de las ImpoExpo
	public static final String COMPRA = "COMPRA";
	public static final String VENTA = "VENTA";
	
	private static ArrayList<String> tiposDeRecursos;
	
	static{
		tiposDeRecursos = new ArrayList<String>();
		tiposDeRecursos.add(TER);
		tiposDeRecursos.add(CC);
		tiposDeRecursos.add(HID);
		tiposDeRecursos.add(PROV);
		tiposDeRecursos.add(DEM);
		tiposDeRecursos.add(FALLA);
		tiposDeRecursos.add(EOLO);
		tiposDeRecursos.add(FOTOV);
		tiposDeRecursos.add(IMPACTO);
		tiposDeRecursos.add(CONTRATOENERGIA);
		tiposDeRecursos.add(CONTRATOINTERRUMPIBLE);
		tiposDeRecursos.add(ACUM);
		tiposDeRecursos.add(IMPOEXPO);
	}

	public static ArrayList<String> getTiposDeRecursos() {
		return tiposDeRecursos;
	}


	/***
	 * Constantes de operaciones Paralelismo
	 */
	public static final double FACTOR_CARGA_NUCLEOS = 1;
	
	public static final int CARGAR_CORRIDA = 0;
	public static final int OPTIMIZAR = 1;
	public static final int OPTIMIZAR_ESTADOS = 2;
	public static final int RETROCEDER_PASO = 3;
	public static final int SIMULAR = 4;
	public static final int CERRARSERVIDOR = 33;
	public static final int SIMULAR_ESCENARIOS = 5;
	public static final int ESPERANDO_OPERACION = 10;
	public static final int CANT_ESTADOS_PAQUETE = 25; //POR AHORA ESTO NO SE PUEDE TOCAR
	public static final int CANT_ESTADOS_PAQUETE_ESCENARIO = 55; //POR AHORA ESTO NO SE PUEDE TOCAR
	public static final int ENESPERA = 0;
	public static final int ENRESOLUCION = 1;
	public static final int TERMINADO = 2;
	
	public static final int TD = 1000; //Demora móxima admisible EN MILISEGUNDOS POR ESTADO
	public static final int TDESCENARIOS = 120000; //Demora móxima admisible EN MILISEGUNDOS POR ESCENARIOS 

	//public static final String ruta_log_paralelismo = "d:\\salidasmodeloop\\logparalelismo\\";
	public static final boolean PROFILER = true;
	
	/***
	 * Constantes de solvers
	 */

	/**
	 * Determinacion de solver
	 */
	public static final int RES_LP_SOLVE = 0;
	public static final int RES_GLPK = 1;
	public static final int RES_XPRESS = 2;
	public static final int RESOLVEDOR_PRINCIPAL = RES_LP_SOLVE;
	//public static final int RESOLVEDOR_PRINCIPAL = RES_XPRESS;
	
	/**
	 * Constantes de LPSOLVE
	 */
	public static final int NUM_MAX_RELAX_PARAM_LP_SOLVE = 3;
	public static final int MULTIP_AUMENTO_EPS_PARAM_LP_SOLVE = 10;
	
	/**
	 * Constantes de glpk
	 */
	// constantes para glpk como solver principal
	public static final boolean GLP_RP_primario = true; // reutilizacion de problema
	public static final boolean GLP_RB_primario = true; // reutilizacion de base
	 
	// constantes para glpk como solver secundario
	public static final boolean GLP_RP_secundario = true; // reutilizacion de problema
	public static final boolean GLP_RB_secundario = true; // reutilizacion de base

	public static final boolean RES_GLP_RP = RESOLVEDOR_PRINCIPAL == RES_LP_SOLVE ? GLP_RP_secundario : GLP_RP_primario; 
	public static final boolean RES_GLP_RB = RESOLVEDOR_PRINCIPAL == RES_LP_SOLVE ? GLP_RB_secundario : GLP_RB_primario; 
	public static final boolean RES_GLP_RESET_EXACTO = false; // utilizar solver exacto para problema lineal en caso de falla
	public static final boolean RES_GLP_IMPRIMIR_LP_SOL = true;  // imprimir los problemas y soluciones
	public static final double RES_GLP_TOL = 1e-11; // tolerancia numerica
	public static final int RES_GLP_TIEMPO_MAX = 1*1000; // tiempo maximo inicial de ejecucion del glpk
	//public static final String RES_GLP_IMPRIMIR_LP_RUTA = "d:\\salidasModeloOp\\lp\\";
//	public static final String RUTA_SALIDA_SIM_PARALELA = "\\\\svwplaima\\corre$\\UTE\\MOP\\SalidaParalela";
//	public static final String RUTA_SALIDA_SIM_LOCAL = "d:\\salidasModeloOp\\serializados\\";
	public static final String VERSION_NUM = "v2.3";
	public static final String VERSION_ET = " - " + VERSION_NUM;
	public static final boolean IMPRIMIR_TIEMPOS = true;
	
	// Directorio de salida del problema de entrada
	public static final String RES_GLP_IMPRIMIR_LP_RUTA_CP = "D:\\salidasModeloOp";

	// Tipo de problemas lineales
	public static final int TP_LP = 1;
	public static final int TP_MIP = 2;
	// tipos del metodos de resolución
	public static final int TMR_SIMPLEX = 1;
	public static final int TMR_PINTERNO = 2;
	public static final int TMR_intopt = 3;
	// Cantidad de esfuerzo realizado por GLPK para encontrar una soluciòn factible
	public static final int EF_BAJO = 1;
	public static final int EF_MEDIO = 2;
	public static final int EF_ALTO = 3;
	public static final boolean UTILIZAR_GLPK_NUEVO = false;

	/**
	* Constantes referentes a la dual
	*/

	public static final String prefijoRestriccionCaja = "_caja";


	
	
	/** 
	 * TIPOS IMPACTOS 
	 */
	public static final int HIDRO_VERTIMIENTO_EXTERNO = 6;
	public static final int HIDRO_CAUDAL_ECOLOGICO = 4; 
	public static final int HIDRO_PROD_MAQUINA = 3;
	public static final int HIDRO_INUN_AGUAS_ARRIBA = 0;
	public static final int HIDRO_INUN_AGUAS_ABAJO = 1;
	public static final int TER_EMISIONES_CO2 = 5;

	
	
	/**
	 * TIPOS CONTRATOS DE ENERGíA
	 */
	public static final String LIM_ENERGIA_ANUAL = "contratoLimEnergiaAnual";
	public static final String INT_TOMA_SIEMPRE = "interrumpibleTomaSiempre";
		
	
	/**
	 * CONSTANTES QUE DEBERÍAN SER LEIDAS DE XML Y EN LA INTERFASE Y LUEGO ELIMINADAS DEL CÓDIGO
	 */
	public static final double TOPE_SPOT = 250.0;   // Tope al costo marginal para definir el spot, en USD/MWh
	public static final double[] PERCENTILES_LOLE = {0.03, 0.05};  // Percentiles que se excluyen para el cálculo de horas esperadas de falla
	public static final boolean POR_INT_MUESTREO = true; // Si es true se almacena en memoria e imprime los números de poste 
														 // de los intervalos de muestreo en cada escenario y produce los costos marginales por intervalo de muestreo. 
														 // Solo aplica con demanda residual 
	
	/**
	 * PROPIETARIOS DE RECURSOS
	 */
	public static final String UTE = "UTE";
	public static final String PRIV = "PRIV";
	
	
	/**
	 * NOMBRES DE ATRIBUTOS DETALLADOS DE TODOS LOS PARTICIPANTES
	 */
	// atributos genéricos de participantes
	public static final String COSTOS = "costos";
	public static final String ENERGIAS = "energias";
	public static final String POTENCIAS = "potencias";
	
	// atributos de hidráulicos
	public static final String TURBINADOS = "turbinados";	
	public static final String VERTIDO = "vertido";
	public static final String TURB_PASO = "turb_paso";	
	public static final String VERT_PASO = "vert_paso";

	public static final String APORTE = "aporte";
	
	public static final String COEFENERGETICO = "coefEnergetico";
	public static final String COTAAGUASARRIBA = "cotaAguasArriba";
	public static final String COSTOSPENECO = "costosPenEco";
	public static final String VALAGUA = "valAgua";
	public static final String DUALVERTIDO = "dualVertimiento";
	
	public static final String COSTOPENCCOTASSUP = "costosPenCCotaSup";
	public static final String COSTOPENCCOTASINF = "costosPenCCotaInf";
	public static final String VOLPENSUP = "volumenPenalizadoSuperior";
	public static final String VOLPENINF = "volumenPenalizadoInferior";
	public static final String COTAPENSUP = "cotaPenalizadaSuperior";
	public static final String COTAPENINF = "cotaPenalizadaInferior";
	
	
	// atributos adicionales de fallas
	public static final String POTENCIASTOT="potenciastot";
	public static final String ENERGIASTOT="energiastot";
		
	// atributos de termicos
	public static final String VOLCOM1 = "volcom1";
	public static final String VOLCOM2 = "volcom2";
	public static final String ENERCOM1 = "enercom1";
	public static final String ENERCOM2 = "enercom2";
	
	// atributos de ciclos combinados, adicionales a los de térmicos
	public static final String POTSTG = "potsTG";
	public static final String POTSCV = "potsCV";
	public static final String POTSAB = "potsAB";   // potencia de TGs en ciclo abierto
	public static final String POTSCOMB = "potsCOMB"; // potencia de las TGs combinadas y CV juntas
	public static final String CANTMODTGDISP = "cantModTGdisp";  // cantidad de TGs disponibles
	public static final String CANTMODCVDISP = "cantModCVdisp";  // cantidad de CVs disponibles	
	
	public static final String POTENCIASINY = "potenciasIny";
	public static final String POTENCIASALM = "potenciasAlm";
	public static final String POTENCIASNETAS = "potenciasNetas";
	public static final String ENERGIASINY = "energiasIny";
	public static final String ENERGIASALM = "energiasAlm";
	public static final String ENERGIASNETAS = "energiasNetas";
	public static final String ENERGIAENACUMULADOR = "energiaEnAcumulador"; //energia al inicio del poste
	
	// atributos de impactos
	public static final String MAGNITUDES ="magnitudes";
	
	// atributos de contratosEnergia
	public static final String VALORES = "valores";
	public static final String VALORESMEDIOS = "valoresMedios";
	public static final String ENERGIASACUM = "energiasAcum";
	
	
	// atributos de contratosInterrumpibles
	public static final String VENTAPASO = "ventaPaso";
	public static final String ENERGIASENTREGADASACUM = "energiaEntregadaAcum";
	public static final String VALORENERFIN = "valorEnerFin";  // valor de la energía entregada al fin del paso (valor del recurso)
	public static final String VALORIZACIONMARGINAL = "valMarginalPaso";
	public static final String VALORIZACIONMARGINALPOSTE = "valMarginalPoste";
	
	// atributos de barras
	public static final String COSTOMARGINAL = "costoMarginal";
	
	// atributos de ImpoExpos
	public static final String CMGPAIS = "CMgPais";   // Costo marginal del país con el que se comercia
	public static final String PRECIOMED = "precioMed";  // precio medio de la transacción para el conjunto de los bloques
	
	
	/**
	 * UNIDADES DE LAS SALIDAS DE ATRIBUTOS DETALLADOS
	 */
	
	// atributos genéricos de participantes
	public static final String U_COSTOS = "USD";
	public static final String U_ENERGIAS = "MWh";
	public static final String U_POTENCIAS = "MW";
	
	// atributos de térmicos y ciclos combinados
	public static final String U_ENERCOM = "MWh";
	public static final String U_VOLCOM = "unidades";
	
	
	// atributos adicionales de ciclos combinados
	public static final String U_POTSTG = "MW";
	public static final String U_POTSCV = "MW";
	
	// atributos de hidráulicos
	public static final String U_TURBINADOS = "m3/s";
	public static final String U_VERTIDO = "m3/s";
	public static final String U_TURB_PASO = "m3/s";
	public static final String U_VERT_PASO = "m3/s";	
	public static final String U_APORTE = "m3/s";
	
	public static final String U_COEFENERGETICO = "MW/(m3/s)";
	public static final String U_COTAAGUASARRIBA = "m";
	public static final String U_COSTOSPENECO = "USD";
	public static final String U_VALAGUA = "USD/hm3";
	
	
	// atributos adicionales de fallas
	public static final String U_POTENCIASTOT="MW";
	public static final String U_ENERGIASTOT="MWh";
	
	// atributos de acumuladores
	public static final String U_POTENCIASINY = "MW";
	public static final String U_POTENCIASALM = "MW";
	public static final String U_ENERGIASINY = "MWh";
	public static final String U_ENERGIASALM = "MWh";
	
	// atributos de impactos
	// public static final String MAGNITUDES ="magnitudes";
	
	// atributos de contratosEnergia
	public static final String U_VALORES = "USD";
	public static final String U_VALORESMEDIOS = "USD/MWh";
	public static final String U_ENERGIASACUM = "GWh";
	
	
	// atributos de contratos interrumpibles
	public static final String U_VENTAPASO = "USD";
	public static final String U_VALENERFIN = "USD/MWh";
	public static final String U_ENERGIASENTREGADASACUM = "GWh";
	
	// atributos de barras
	public static final String U_COSTOMARGINAL = "USD/MWh";
	
	// atributos de ImpoExpos
	public static final String U_CMGPAIS = "USD/MWh";
	public static final String U_PRECIOMED = "USD/MWh";
	
	// Tabla de unidades de salida
	private static Hashtable<String, String> unidadesAtributos;

	
	static{
		// clave nombre del atributo según las constantes de esta clase
		// valor nombre de la unidad de las salidas de atributos detallados.
		unidadesAtributos = new Hashtable<String, String>();
		
		unidadesAtributos.put(COSTOS, U_COSTOS);		
		unidadesAtributos.put(ENERGIAS, U_ENERGIAS);
		unidadesAtributos.put(POTENCIAS, U_POTENCIAS);
		
		// atributos de térmicos y ciclos combinados
		unidadesAtributos.put(ENERCOM1, U_ENERCOM);
		unidadesAtributos.put(ENERCOM2, U_ENERCOM);
		unidadesAtributos.put(VOLCOM1, U_VOLCOM);
		unidadesAtributos.put(VOLCOM2, U_VOLCOM);		
		
		// atributos adicionales de ciclos combinados
		unidadesAtributos.put(POTSTG, U_POTSTG);
		unidadesAtributos.put(POTSCV, U_POTSCV);

		
		// atributos de hidráulicos
		unidadesAtributos.put(TURBINADOS, U_TURBINADOS);
		unidadesAtributos.put(VERTIDO, U_VERTIDO);
		unidadesAtributos.put(TURB_PASO, U_TURB_PASO);
		unidadesAtributos.put(VERT_PASO, U_VERT_PASO);		
		unidadesAtributos.put(APORTE, U_APORTE);
		
		unidadesAtributos.put(COEFENERGETICO, U_COEFENERGETICO);
		unidadesAtributos.put(COTAAGUASARRIBA, U_COTAAGUASARRIBA);
		unidadesAtributos.put(COSTOSPENECO, U_COSTOSPENECO);
		unidadesAtributos.put(VALAGUA, U_VALAGUA);
				
		// atributos de acumuladores
		unidadesAtributos.put(POTENCIASINY, U_POTENCIASINY);
		unidadesAtributos.put(POTENCIASALM, U_POTENCIASALM);
		unidadesAtributos.put(ENERGIASINY, U_ENERGIASINY);
		unidadesAtributos.put(ENERGIASALM, U_ENERGIASALM); 
		
		// atributos de impactos
		// public static final String MAGNITUDES ="magnitudes";
		
		// atributos de contratosEnergia 
		unidadesAtributos.put(VALORES, U_VALORES);
		unidadesAtributos.put(VALORESMEDIOS, U_VALORESMEDIOS);
		unidadesAtributos.put(ENERGIASACUM, U_ENERGIASACUM);
		
		
		// atributos de contratos interrumpibles 
		unidadesAtributos.put(VENTAPASO, U_VENTAPASO);
		unidadesAtributos.put(VALORENERFIN, U_VALENERFIN);
		unidadesAtributos.put(ENERGIASENTREGADASACUM, U_ENERGIASENTREGADASACUM);
		
		// atributos de barras
		unidadesAtributos.put(COSTOMARGINAL, U_COSTOMARGINAL);		
		
		// atributos de ImpoExpos
		unidadesAtributos.put(CMGPAIS, U_CMGPAIS);
		unidadesAtributos.put(PRECIOMED, U_PRECIOMED);
		
	}

	public static Hashtable<String, String> getUnidadesAtributos() {
		return unidadesAtributos;
	}
	
	
	/**
	 * CONSTANTES VINCULADAS A PROCESOS ESTOCASTICOS
	 */
	
	// Tipos de procesos estocásticos
	
	public static final String HISTORICO = "historico";
	public static final String MARKOV = "markov";
	public static final String MARKOV_AMPLIADO = "markovAmpliado";
	public static final String POR_ESCENARIOS = "porEscenarios";
	public static final String BOOTSTRAP_DISCRETO ="BootstrapDiscreto";
	public static final String POR_CRONICAS = "porCronicas";
	public static final String DEMANDA_ANIO_BASE = "demandaAnioBase";
	public static final String DEMANDA_ESCENARIOS = "demandaEscenarios";
	public static final String POREVOLUCION = "POREVOLUCION";
	public static final String VAR = "VAR";


	
	
	// Tipos de transformaciones de normalización
	public static final String BOXCOX = "BOXCOX";
	public static final String NQT = "NQT";
	public static final ArrayList<String> NOMBRESTRANS = new ArrayList<>(Arrays.asList(BOXCOX, NQT));
	
	// Cantidad de caracteres del estado que van a la clave del estado para almacenar valores sorteados
	// se usa en producirRealizacionPEEstadosOptim en procesos con sorteos Montecarlo y con paso de duracion mayor que el paso de la optimización
	public static final int cantCarac = 6;

	
	// Estimación de transformaciones Box-Cox
	
	// EPSIMIN_BOXCOX se usa así: si los datos para construir la BOX-COX tienen valor mínimo
	// datmin negativo, o positivo pero que cumple datim >datmed*EPSITRAS_BOXCOX  
	// se suma a todos los datos un valor traslacion
	// tal que: datmin + traslacion = datmed *EPSIMIN_BOXCOX
	public static final double EPSITRAS_BOXCOX = 0.01; 
	public static final double EPSIMIN_BOXCOX = 0.001; 
	
	public static final int LIMINF = -10;   // Menor valor para probar el lambda en la primera etapa
	public static final int LIMSUP =  10;   // Mayor valor para probar el lambda en la primera etapa
	public static final int NGRILLA = 200;   // Cantidad de valores de lambda equidistantes en cada etapa
	public static final int ETAPAS = 8;    // Cantidad de etapas en la iteración

	// Formas de estimacion de un proceso VAR
	public static final String SOLO_VAR = "SOLO_VAR"; 
	public static final String VAR_Y_PVA = "VAR_Y_PVA";
	
	// Epsilon para tomar como cero un valor propio en la descomposición espectral de una matriz de varianza
	public static final double EPSILON_VAL_PROPIO = 1.0E-12;
	
	// Tipos de simulación de procesos estocásticos y su procesamiento
	public static final String ESC_UNICO = "ESC_UNICO";
	public static final String ESC_MULT = "ESC_MULT";
	
	// Constantes de Redis
	public static final int CODIGO_BASE_REDIS = 1;
	public static final int CODIGO_BASE_SERVICIO = 0;
	
	public static final String CURVA_DE_OFERTA = "globalCostosVariables";
	
	
	// Constantes de la serialización de escenarios
	public static final String DIR_SERIALIZADOS = "DIR_SERIALIZADOS";
	
	
	
	//public static final int CANT_DISCRETIZACIONES_CONTRATO=30;
	
}

