/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConCP is part of MOP.
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

package cp_despacho;

import java.util.ArrayList;

public class ConCP {
	
	
	
	// RESOLVEDORES LINEALES POSIBLES
	public static final String LPMOP = "MOP";
	public static final String LPOTRO = "OTRO";   // TODO ESTO HAY QUE CAMBIARLO 
	public static final String LPUSADO = LPMOP; 
	
	// PARAMETROS DE FORMULACION DEL PROBLEMA LINEAL
	public static final double MINIMOCOEFNONULO = 1E-6;  // Por debajo de esta cota los coeficientes se toma iguales a cero
	
	
	// CONSTANTES DE IMPOEXPO
	
	public static final String OBASE = "BASE";
	public static final String OPOT_MEDIA = "POTMEDIA";
	public static final String OLIBRE = "LIBRE";
	public static final String OALEAT = "ALEAT";  // EL PRECIO Y LA POTENCIA SON VARIABLES ALEATORIAS DEL GRAFO DE ESCENARIOS
	
	public static final String PAISARG = "ARG";
	public static final String PAISBRA = "BRA";
	
	// CONSTANTES DE CICLO COMBINADO
	
	public static final String SINLIMITE = "SIN_LIMITE";
	
	
	// CONSTANTES DE TERMICO
	
	// Resultados
	public static final String CMG1TER = "CMG1TER"; 
	
	// CONSTANTES DE HIDRAULICOS
	
	// Forma del vertimiento máximo
	public static final String AL = "AL";   // para aproximación lineal por puntos
	public static final String FIJO_COTA = "FIJO_COTA";  // se toma el vertimiento máximo para el volumen inicial del CP
	public static final String RECTA = "RECTA";   // se aproxima por una única recta
	public static final String COTA = "COTA";
	
	// Relación potencia-caudal
	public static final String COEF_FIJO = "COEF_FIJO";  // La potencia es igual al caudal por un coeficiente constante, sujeta a cota de potencia máxima
	public static final String FUNCPQ = "FUNCPQ";    // Funciones PQ, que limitan con rectas la potencia, dado el caudal	
	public static final double FQCOEF = 0.5;   // Fracción del caudal máximo para el que se calcula el coeficiente energético medio 
	// prefijo del nombre de la variable aleatoria del aporte
	public static final String APORTE = "APORTE"; // El nombre de la VA se forma por "APORTE" + "_" + nombre del participante hidro
	
	// Resultados
	public static final String APORTEM3S = "APORTEM3S";
	public static final String APORTEHM3 = "APORTEHM3";
	public static final String EVAFILHM3 = "EVAFILHM3";
	public static final String VALAGUA = "VALAGUA";
	public static final String VALENERG = "VALENERGIA";
	
	// CONSTANTES DE CONTRUCTOR DE HIPERPLANOS
	public static final String CORRIDA = "CORRIDA";
	public static final String EXTERNO = "EXTERNO";
	
	public static final String NOMBRE_CONST_HIP_PE = "constructorHiperplanosPE";
	public static final double EPSIHIPERPLANOS_USD = 0;  // Tolerancia para encontrar hiperplanos de valor máximo en la solución óptima hallada
	public static final double ESCALADOR_DIVISOR = 1.0; // Se dividen las restricciones de hiperplanos por este número
	
	
	
	// CONSTANTES DE SALIDAS
	public static final String DIA = "DIA";  // un valor por día para cada escenario
	public static final String POSTE = "POSTE"; // un valor por poste para cada escenario
	public static final String UNICO = "UNICO"; // un único valor por escenario
	
	public static final String COSTOPASO = "COSTOPASO";
	public static final String COSTOTOT = "COSTOTOT";
	public static final String COSTOFUT = "COSTOFUT";
	public static final String DESPACHO = "DESPACHO";  // nombre de participante atribuido a los resultados únicos del despacho en el escenario como costo total
	
	
	// CONSTANTES DE LAS BARRAS
	public static final String CMG = "CMG";  // denota resultados de costo marginal
	// ATENCIÓN: DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DE LA BARRA
	
	
	/**
	 * CONSTANTES QUE SE USAN PARA NOMBRAR VARIABLES DE CONTROL
	 */

	// NOMBRES GENERALES (en particular se usan para GeneradorTermico
	public static final String POT = "pot";   // potencia por poste
	public static final String NMOD = "NMOD";  // cantidad de módulos de una central
	public static final String ARR = "ArranquesEnElPoste";  // cantidad de arranques de módulos en el poste 
	public static final String CANTARRTOT = "CantTotalDeArranques";  // cantidad de arranques de módulos en el poste 
	
	// VARIABLES DE CONTROL DE BARRA -----------------------------------------------------------------
	public static final String DELTA = "delta";  // angulo de barra en radianes por poste

	
	//VARIABLES DE CONTROL DE RAMA ------------------------------------------------------------------
	public static final String POTR12 = "potr12";   // potencia de rama simple en sentido 12 por poste
	public static final String POTR21 = "potr21";   // id. 21
	public static final String POTR = "potr";    // potencia de rama DC por poste
	
	// VARIABLES DE CONTROL DE IMPOEXPO -------------------------------------------------------------
	
	// Variables de control de ImpoExpo como interconexiones
	public static final String POTIETOTENT = "potIETot";  // potencia total entrante al sistema por la interconexión
	
	// Variables de control binarias de ofertas ImpoExpo
	public static final String XINID = "xiniD";    // para binarias que valen 1 en días de entrada de ofertas
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE OFERTA
	
	// Variables de control binarias de la interconexión para controlar el mínimo técnico
	public static final String XIMPOEXPENT = "xIEEnt";   // para binaria que vale 1 si se usa la interconexión con potencia entrante
	public static final String XIMPOEXPSAL = "xIESal";   // para binaria que vale 1 si se usa la interconexión con potencia entrante

	// Variable de energía faltante cuando hay una potencia media mínima de la oferta
	public static final String ENERFALT = "ENERFALT";
	
	// VARIABLES DE CONTROL DE CICLO COMBINADO ----------------------------------------------------
	
	// Variables de control binarias de turbinas combinadas 
	public static final String XAINI = "xAIni";   // binarias de arranques iniciales de CC
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DEL ARRANQUE INICIAL 
	
	public static final String XASIM = "xASim";   // binarias de arranques simples de CC
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DEL ARRANQUE SIMPLE
	
	public static final String YPAR = "yPar";   // binarias de paradas de CC
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DE LA PARADA 
	
	// Variables de control binarias de prohibición de arranques iniciales
	public static final String ZINI = "zIni";  
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DEL ARRANQUE INICIAL 

	// Variables de control enteras de ciclos combinados
	public static final String NMODTG = "nModTG";  // cantidad de TGs funcionando en ciclo abierto en el poste
	public static final String ARRTGAB = "arrTGAb";  // entera con cantidad de arranques de TGs en ciclo abierto en el poste
	public static final String NMODCC = "nModCC";   // cantidad de módulos combinados en régimen en el poste
	public static final String NMODCCINI = "nModCCIni";   // cantidad de módulos combinados al inicio del poste antes de las decisiones de parada
	
	// Cantidad de arranques 
	public static final String CANTARRTGAB = "cantArrTGAb";  // de TGs en ciclo abierto
	public static final String CANTARRINI = "cantArrIni";  // iniciales del CV
	public static final String CANTARRSIM = "cantArrSim";  // simples en ciclo combinado
	
	// Variables de potencia por tipo de régimen de las TGs del ciclo combinado
	public static final String POTCC = "potCC";  // potencia de turbinas en régimen combinadas
	public static final String POTAINI = "potAIni"; // potencia de turbinas en arranques iniciales
	public static final String POTASIM = "potASim"; // potencia de turbinas en arranques simples
	public static final String POTPAR = "potPar"; // potencia de turbinas en parada
	
	public static final String POTTGAB = "potTGAb";  // potencia de turbinas en ciclo abierto
	// ATENCION: La potencia total del ciclo tiene el prefijo POT
	
	// Variables de control de energía térmica por poste y combustible. TAMBIÉN LA USAN LOS TÉRMICOS
	public static final String ENERTPC = "enerTpc";  // energía térmica por poste y combustible
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DEL COMBUSTIBLE
	
	
	
	// ---------------------------------------------------------------------------------------------
	
	// VARIABLES DE DUCTO DE COMBUSTIBLE
	public static final String FLUJO = "flujo";  // flujo de combustible en unidades por hora
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DEL COMBUSTIBLE
	
	// VARIABLES DE TANQUE DE COMBUSTIBLE
	public static final String VARVOL = "variacionAumentoDeVol";  // aumento de volumen en unidades	
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DEL COMBUSTIBLE
	
	// VARIABLES DE CONTRATO DE COMBUSTIBLE
	public static final String CAUDAL = "caudalComb";  // caudal aportado por el contrato en unidades por hora
	// ATENCIÓN: AL NOMBRE DE VARIABLE DEBE AGREGARSE "_" MAS EL SUFIJO DE NOMBRE DEL COMBUSTIBLE
	
	// ---------------------------------------------------------------------------------------------

	// VARIABLES DE CONTROL DE ACUMULADOR ---------------------------------------------------------
	public static final String POTALM = "potalm";  // potencia almacenada sale de la red
	
	
	//VARIABLES DE CONTROL DE FALLAS ----------------------------------------------------------------
	public static ArrayList<String> FALLAPOTESC;  // potencia por escalón por poste. El ArrayList recorre los escalones
	
	static {
		FALLAPOTESC = new ArrayList<String>();
		
	}
	
	
	// VARIABLES DE CONTROL DE HIDRAULICO -----------------------------------------------------------
	public static final String QERO = "qero";  // caudal erogado en m3/s por poste
	public static final String QTUR = "qtur";  // caudal turbinado en m3/s por poste
	public static final String QVER = "qver";  // caudal vertido en m3/s por poste
	public static final String QVERMAX = "qvermax";  // caudal máximo vertible en m3/s función del volumen embalsado al inicio del poste NO SE USA TODAVÍA
	public static final String QEROMIN = "qeromi";  // caudal mínimo que debe erogarse en m3/s función del volumen embalsado al inicio del poste NO SE USA TODAVÍA
	public static final String VOLINI = "volini";  // volumen en hm3 al inicio del poste
	public static final String VOLFIN = "volfin";  // volumen en hm3 al fin del poste
	public static final String VOLEXCED = "volexced"; // volumen en hm3 por encima del volumen al que se empieza a penalizar la inundación
	public static final String VOLFALT = "volfalt"; // volumen en hm3 por debajo del volumen al que se empieza a penalizar el faltante de volumen
	public static final String QFALT = "qerofaltante"; // faltante en m3/s para llegar al erogado mínimo por debajo del que se empieza a penalizar
	public static final String VOLEXCEDXMINVER = "volexcedXminVer"; // volumen en hm3 por encima del volumen correspondiente a cota xmin de la función de vertimiento máximo 

	
	// VARIABLES DE CONTROL DEL CONSTRUCTOR HIPERPLANOS -----------------------------------------------
	public static final String ZVB = "vBfinal"; // valor de Bellman al fin del horizonte
	// hay una variable de éstas para cada escenario desde el origen hasta el fin del horizonte
	
	// VARIABLES DE CONTROL DE COSTOS RESULTANTES DE LA OPTIMIZACION ----------------------------------
	public static final String COSTOESC = "COSTOESC"; // COSTO EN UN ESCENARIO e0_e1_....eE
	// ATENCIÓN: DEBE AGREGARSE EL SUFIJO e0_e1_....eE para construir el costo dado que ocurre el escenario e0_e1_....eE
	
	
	
	/**
	 * CONSTANTES QUE SE USAN PARA NOMBRAR RESTRICCIONES
	 */
	
	// RESTRICCIONES GENERALES  (se usan por ejemplo en generador térmico) ------------------------
	public static final String RPOTMAX = "PotenciaMaxima";
	public static final String RPOTMIN = "PotenciaMinimoTecnico";
	public static final String RCANTARRTOT = "DefineCantidadDeArranquesTotales";
	public static final String RCANTARRPOS = "DefineCantidadDeArranquesPorPoste";

	// RESTRICCIONES DE BARRA ---------------------------------------------------------------------
	public static final String RBALBARRA ="BalanceBarra"; 
	
	
	// RESTRICCIONES DE RAMA ----------------------------------------------------------------------
	public static final String RRAMAPORTA = "RamaAportaABarra";  // Restricción que construye el aporte de la rama a la restricción de balance de barra
	
	
	// RESTRICCIONES DE LAS IMPOEXPO
	public static final String RDIAENT = "DiaEnt";   // restricciones de día único de entrada
	public static final String RPOTBASE = "PotBase"; // restricciones de potencia igual al máximo en ofertas BASE
	public static final String RPOTMED = "PotMed";   // restricciones de potencia media mayor o igual a un mínimo en ofertas POTMED	o que crean holguras para penalizar
	public static final String RSUMPOT = "SumPot";   // restricciones de suma de pot de ofertas menor o igual al máximo de interconexión
	public static final String RPOTMAXIE_ENT = "PotMaxImpoExpoEnt";  // suma de potencias de ofertas entrantes netas menor o igual a la potencia máxima de la ImpoExpo cuando se usa la interconexión.
	public static final String RPOTMAXIE_SAL = "PotMaxImpoExpoSal";  // suma de potencias de ofertas salientes netas menor o igual a la potencia máxima de la ImpoExpo cuando se usa la interconexión.
	public static final String RPOTMINIE_ENT = "PotMinImpoExpoEnt";  // id min entrante
	public static final String RPOTMINIE_SAL = "PotMinImpoExpoSal";  // id min saliente
	public static final String RDEFPOTIETOT = "DefPotIETot"; // define la potencia total entrante por una ImpoExpo

	public static final String RDIAOFYACONV = "DiaOfYaConv"; // fija el día de entrada de ofertas ya convocada

	// RESTRICCIONES DE EOLIOC ------------------------------------------------------------------
	
	public static final String RPOTEOL = "PotenciaDeEolico"; 
	
	// RESTRICCIONES DE CICLO COMBINADO  ------------------------------------------------------------
	
	public static final String RNMODCC = "NModCC";     // Define nMODCC cant de módulos combinados en paso p
	public static final String RNMODCCINI = "NModCCIni";     // Define nMODCCIni cant de módulos combinados al inicio de paso p		
	public static final String RINCOMPARRYPAR = "IncompArrYPar";  // Incompatibilidad entre arranques y entre arranques y paradas
	public static final String RINCOMPINIREG = "IncompArrYRegimen";  	// incompatibilidad entre arranques iniciales y estado de régimen de CC
	public static final String RREQPARADA = "RequisitosParada";     // requisitos para que puedan iniciarse paradas en p 
	public static final String RREQARRSIM = "RequisitosArranqueSimple";  // requisitos para que pueda iniciarse arranques simples en p
	public static final String RTOTMODDESP = "TotalModulosDespachados";  // el total de módulos despachados de TGs no excede los disponibles

	public static final String RCANTARRTGABTOT = "CantidadArranquesTGAbTotales";   // define la suma de arranques total de turbinas en ciclo abierto
	public static final String RCANTARRTGABPOS = "CantidadArranquesTGAbPorPoste";

	public static final String RCANTARRINI = "CantidadArranquesIniciales";   // define la suma de arranques iniciales
	// ATENCIÓN: AL NOMBRE DE RESTRICCION DEBE AGREGARSE "_" MAS EL SUFIJO DEL NOMBRE DEL ARRANQUE INICIAL
	
	public static final String RCANTARRSIM = "CantidadArranquesSimples";   // define la suma de arranques simples
	// ATENCIÓN: AL NOMBRE DE RESTRICCION DEBE AGREGARSE "_" MAS EL SUFIJO DEL NOMBRE DEL ARRANQUE INICIAL
	
	public static final String RPOTMAXCC = "PotenciaMaximaTGsCombinadas";
	public static final String RPOTMINCC = "PotenciaMinimaTGsCombinadas";
	
	public static final String RPOTARRINI = "PotenciaDeArrIniciales";   // define la potencia del total de arranques iniciales
	public static final String RPOTARRSIM = "PotenciaDeArrSimples";   // define la potencia del total de arranques simples
	public static final String RPOTPAR = "PotenciaDeParadas";   // define la potencia del total de paradas
	public static final String RPOTTOTAL = "PotenciaTotalSumada"; // iguala la potencia total a la suma de potencias de TGs combinadas, abiertas, arranques y paradas
	
	public static final String RPOTMAXTGAB = "PotenciaMaximaTGsAb";  
	public static final String RPOTMINTGAB = "PotenciaMinimaTGsAb";  

	// La restricción de potencia máxima POT del ciclo combinado total por la disponibilidad de TGs y CVs se puso como cota superior
	
	public static final String RENERTERMICAS = "EnergiasTermicas"; // La usa también el GeneradorTermico
	
	public static final String RZIMPIDEARRINI = "DefineVariableEntZImpideArrIni";
	// ATENCIÓN: AL NOMBRE DE RESTRICCION DEBE AGREGARSE "_" MAS EL SUFIJO DEL NOMBRE DEL ARRANQUE INICIAL

	public static final String RUSAZ = "UsaZParaImpedirArranqueIni";
	// ATENCIÓN: AL NOMBRE DE RESTRICCION DEBE AGREGARSE "_" MAS EL SUFIJO DEL NOMBRE DEL ARRANQUE INICIAL
		
	// RESTRICCIONES DE COMBUSTIBLE, BARRA, CONTRATOS, ETC. --------------------------------------
	
	public static final String RBALBARRACOMB = "BalanceBarraDeCombustible";   // balance de combustible en la barra
	// ATENCIÓN: AL NOMBRE DE RESTRICCION DEBE AGREGARSE "_" MAS EL SUFIJO DEL NOMBRE DEL COMBUSTIBLE
		
	
	// RESTRICCIONES DE LA FALLA

	public static ArrayList<String> RFALLAFORZESC;  // Restriccion de falla forzada o nula para las programadas. El ArrayList recorre escalones.
	static {
		RFALLAFORZESC = new ArrayList<String>();
	}
	
	// RESTRICCIONES DEL HIDRÁULICO --------------------------------------------------------------
	
	public static final String RBALLAGO = "BalanceDeLagoHm3";   // volumen final del poste en función del inicial	
	public static final String RVOLFIJOSINLAGO = "VolumenFijoSinLago"; // vol. final igual al vol. inicial si no hay lago
	public static final String RVOLINIFIN = "VolumenInicialIgualFinalAnterior"; 
	public static final String RVOLINI0 = "VolumenInicialPoste0";  // fija el volumen al inicio del poste 0
	public static final String RDEFQERO = "DefineErogadoSumaTurbYVert";  // define caudal erogado como suma de turbinado y vertido
	
	public static final String RCOEFENERG = "CoeficienteEnergetico"; // crea la restricción proporcional entre potencia y caudal
	
	public static final String RQERO = "ErogadoSumaTurbYVert";
	public static final String REROMIN = "ErogadoMinimo";
	public static final String RVERMAXAL = "VertimientoMaximoConAproxLinealPorPuntos";
	public static final String RVERMAXRECTA = "VertimientoMaximoConUnaSolaRecta";
	
	public static final String RVOLEXCED = "volexced"; // Restricción que define el volumen en hm3 penalizado por inundación
	public static final String RVOLFALT = "volfalt"; // Restricción que define el volumen faltante en hm3
	public static final String RQFALT = "qerofalt"; // Restricción que define el faltante en m3/s para llegar al erogado mínimo
	
	
	// RESTRICCIONES DEL CONSTRUCTOR HIPERPLANOS --------------------------------------------------
	
	
	public static final String RHIP = "Hiperplano";  
	// ATENCIÓN: SE LE AGREGA UN SUFIJO "_(número del hiperplano)
	// restricción del hiperplano, habrá un conjunto de hiperplanos para cada escenario desde el origen
	// los coeficientes de los hiperplanos son los mismos en todos los escenarios, pero las variables
	// son los estados finales en cada escenario
	
	
	// RESTRICCIONES DE COSTOS RESULTANTES DE LA OPTIMIZACION ----------------------------------
	public static final String RCOSTOESC = "RCOSTOESC"; // RESTRICCIÓN QUE DEFINE EL COSTO EN UN ESCENARIO e0_e1_....eE
	// ATENCIÓN: DEBE AGREGARSE EL SUFIJO e0_e1_....eE para construir el costo dado que ocurre el escenario e0_e1_....eE
	
	
	
	
	/**
	 * CONSTANTES DE LA CONSTRUCCIÓN DEL GRAFO DE ESCENARIOS A PARTIR DE LOS ENSAMBLES
	 */
	public static final String DEMANDA_RESIDUAL = "DEMANDA-RESIDUAL";   // demanda menos eólico menos solar en MW
	public static final String POT_DISP_HORARIA = "POT_DISP_HORARIA";   // energía media horaria en MW de las renovables, hidro, eolo y solar
	
	
	
	
	
	
	
	
}
