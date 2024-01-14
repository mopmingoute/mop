/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ResOptim is part of MOP.
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

package optimizacion;




import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import control.VariableControlDE;
import datatypesResOptim.DatosTablaVByValRec;
import utilitarios.Constantes;
import utilitarios.EnumeradorLexicografico;
import estado.Discretizacion;
import estado.VariableEstado;
import estado.VariableEstadoPE;
import estado.VariableEstadoPar;
import futuro.AFIncrementos;
import futuro.ClaveDiscreta;
import futuro.Hiperplano;
import futuro.InformacionValorPunto;
import futuro.TablaControlesDE;
import futuro.TablaControlesDEMemoria;
import futuro.TablaControlesDERedis;
import futuro.TablaHiperplanos;
import futuro.TablaVByValRecursos;
import futuro.TablaVByValRecursosMemoria;
import futuro.TablaVByValRecursosRedis;
import logica.CorridaHandler;
import parque.Corrida;
import pizarron.PizarronRedis;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.LineaTiempo;


public abstract class ResOptim {
	
	/**
	 * Clase que representa el resultado obtenido de la optimización que debe ser utilizado en la simulación
	 * @author ut469262
	 * 
	 * Se supone que las VE y la discretización de las mismas empleadas en el despacho del paso t, y que forman el 
	 * s0fint son las mismas que existen durante todo el paso.
	 * 
	 * La optimización tiene que dar cuenta de los cambios en las VE o en su discretización, cuando se
	 * estó haciendo la recursión hacia atrós desde el paso t+1 al t. 
	 *    
	 */
	
	private long instanteRef;  // Instante para determinar las discretizaciones
	private int cantPasos;  // Cantidad de pasos de la tabla
	private GregorianCalendar fechaInicioTabla; // el inicio del primer paso de tiempo en cuyo final se tiene aproximación futura

	/* 
	 * Tabla que almacena los controles óptimos de las variables de control discretas exhaustivas
	 * en cada punto de discretización
	 */
	protected TablaControlesDE tablaControlesDE;

	private int pasoCorriente;

	/**
	 * Lista de las variables de estado que son relevantes en un paso dado de la simulación u optimización 
	 * Se va actualizando al cambiar de paso.
	 */
	private ArrayList<VariableEstado> varsEstadoCorrientes;

	/**
	 * Dado el nombre de una VE devuelve el ordinal de la misma en varsEstadoCorrientes
	 */
	private Hashtable<String, Integer> ordinalDeVEEnVarsEstadoCorrientes;

	/**
	 * Lista de las variables de estado continuas, subconjunto de varsEstadoCorrientes 
	 */
	private ArrayList<VariableEstado> varsEstadoContinuas;
	
	
	/**
	 * Para cada nombre de VE, da el nombre de la variable asociada en el 
	 * problema numórico de despacho
	 */
	private Hashtable<String, String> variableDespachoDeVEContinua;
	
	/**
	 * Lista de las variables de estado dicretas no incrementales, subconjunto de varsEstadoCorrientes
	 * ATENCIóN: En el comportamiento global Hiperplanos, como no hay discretas incrementales, 
	 * el conjunto de las VE discretas no incrementales coincide con el conjunto de las VE discretas.
	 */
	private ArrayList<VariableEstado> varsEstadoDisNoInc;

	/**
	 * Para cada variable de estado de varsEstadoCorrientes
	 * - si la VE es continua da una posición empezando en 0 en el enumerador lexicogrófico (que es tambión
	 *   el ordinal en InformacionValorPunto y en los hiperplanos.
	 * - si la VE no es continua da el móximo entero
	 */	
	private ArrayList<Integer> ordinalEnEnumDeContinuas;


	/**
	 * Dado el nombre de una variable continua devuelve su ordinal ENTRE LAS CONTINUAS (que es el mismo en InformacionValorPunto
	 * y en los Hiperplanos)
	 */
	private Hashtable<String, Integer> tablaOrdinalDeContinuas;
	
	
	/**
	 * Para cada entero del enumerador lexicogrófico, da el ordinal empezando en cero
	 * en el vector varsEstadoCorrientes de la variable de estado continua asociada
	 */
	private ArrayList<Integer> ordinalEnVarsEstadoDeContinuas;
	
	/**
	 * Variables usadas para comandar la impresión de encabezamientos de las tablas de VB y valores de los recursos.
	 */
	private boolean cambioCantVE; // true si desde el paso anterior cambió la cantidad de VE
	private boolean inicio; // true si se está en el primer paso del algoritmo, último de la linea de tiempo


	/*
	 * Para cada variable de estado de varsEstadoCorrientes
	 * - si la VE es discreta incremental (es decir se calculan incrementos respecto a sus variaciones),
	 *   da una posición empezando en cero en InformacionValorPunto
	 * - si la VE no es discreta incremental da el móximo entero
	 * NO SE EMPLEA CUANDO SE USAN HIPERPLANOS
	 *   
	 */
	private ArrayList<Integer> ordinalEnInfoPuntoDeDiscretasIncr;

	/*
	 * Para cada variable discretas incremental, tomadas en el orden en que aparecen en InformacionValroPunto
	 * devuelve el ordinal en el vector varsEstadoCorrientes (empezando en cero).
	 * NO SE EMPLEA CUANDO SE USAN HIPERPLANOS
	 */
	private ArrayList<Integer> ordinalEnVarsEstadoDeDiscretasIncr;


	/**
	 * Para cada nombre de proceso estocóstico DE devuelve el ordinal en varsEstadoCorrientes
	 * de su variable de estado
	 */
	private Hashtable<String, Integer> ordinalDePEDEEnVarsEstadoOptimizacion;

	// Tabla con las variables de control DE del paso corriente.
	private ArrayList<VariableControlDE> varsControlDECorrientes;


	private int cantVE;  // Cantidad total de variables de estado

	private int cantVECont; // Cantidad de variables continuas que emplearón el enumerador lexicogrófico 
	// enumLexPoliedro en la interpolación en las variables de estado continuas 

	private int cantVEDisInc; // Cantidad de variables de estado discretas incrementales


	private int cantVEDisNoInc; // Cantidad de variables de estado discretas no incrementales

	/*
	 * Enumerador lexicogrófico para los estados
	 */
	private EnumeradorLexicografico enumLexEstados; 
	private int[] cotasInferioresEsta;  
	private int[] cotasSuperioresEsta;  


	/*
	 * Variables auxiliares
	 */
	private int[] codigoUnVertice;
	private ClaveDiscreta clave;
	private double x0, x1;
	private int pasoInf;
	private ArrayList<Double> ponderadoresVertices;
	private ArrayList<Double> ponderadoresVerticesVB;


	public ResOptim(int cantPasos){		
		this.cantPasos = cantPasos;
		completaConstruccion();			
	}

	public ResOptim(){		
		completaConstruccion();			
	}	

	public void completaConstruccion(){	
		if (CorridaHandler.getInstance().isParalelo()) {
			tablaControlesDE = new TablaControlesDERedis(cantPasos);
		} else {
			tablaControlesDE = new TablaControlesDEMemoria(cantPasos);
		}
		
		varsEstadoCorrientes = new ArrayList<VariableEstado>();
		varsEstadoContinuas = new ArrayList<VariableEstado>();
		varsEstadoDisNoInc = new ArrayList<VariableEstado>();
		ordinalEnEnumDeContinuas = new ArrayList<Integer>();
		tablaOrdinalDeContinuas = new Hashtable<String, Integer>();
		ordinalEnVarsEstadoDeContinuas = new ArrayList<Integer>();		
		ordinalEnInfoPuntoDeDiscretasIncr = new ArrayList<Integer>() ;		
		ordinalEnVarsEstadoDeDiscretasIncr = new ArrayList<Integer>();
		ordinalDeVEEnVarsEstadoCorrientes = new Hashtable<String, Integer>(); 
		ordinalDePEDEEnVarsEstadoOptimizacion = new Hashtable<String, Integer>();
		variableDespachoDeVEContinua = new Hashtable<String, String>(); 
		clave = new ClaveDiscreta();
		
		cambioCantVE = false;
		inicio = true;
		
	}



	/*
	 * Carga el indice de paso (que comienza en 0, 1,...) y las variables de estado TODO:??????????????
	 * que se emplean en el paso corriente.
	 * Crea los enumeradores lexicogróficos para los poliedros de interpolación y para recorrer los estados
	 * discretos (este óltimo se requiere en la optimización y en el cólculo de las derivadas parciales)
	 * TODO: ESTE MóTODO DEBERóA INVOCARSE EN LA SIMULACIóN SOLO CUANDO CAMBIAN LAS VARIABLES DE ESTADO
	 */
	public void inicializaResOptimParaNuevoPaso(int numpaso, long instante, ArrayList<VariableEstado> varsEstado,
			ArrayList<VariableControlDE> varsControlDE){		
		pasoCorriente = numpaso;
		varsEstadoCorrientes = varsEstado;	
		instanteRef= instante;
		cantVECont = 0;
		cantVE = varsEstado.size();
		varsEstadoContinuas.clear();
		varsEstadoDisNoInc.clear();
		ordinalEnEnumDeContinuas.clear();
		tablaOrdinalDeContinuas.clear();
		ordinalEnVarsEstadoDeContinuas.clear();
		ordinalEnInfoPuntoDeDiscretasIncr.clear();
		ordinalEnVarsEstadoDeDiscretasIncr.clear();
		ordinalDeVEEnVarsEstadoCorrientes.clear();	
		ordinalDePEDEEnVarsEstadoOptimizacion.clear();
		int indCont = 0;
		int indDisInc = 0;
		for(int ive = 0; ive<varsEstadoCorrientes.size(); ive++){
			VariableEstado ve = varsEstadoCorrientes.get(ive);
			if(ve.isDiscreta()) {
				// La variable de estado no es continua
				ordinalEnEnumDeContinuas.add(Integer.MAX_VALUE);
				if(ve.isDiscretaIncremental()){
					// La variable de estado es discreta incremental
					ordinalEnInfoPuntoDeDiscretasIncr.add(indDisInc);
					indDisInc++;
					ordinalEnVarsEstadoDeDiscretasIncr.add(ive);
				}else{
					// La variable es discreta pero no discreta incremental
					varsEstadoDisNoInc.add(ve);
					ordinalEnInfoPuntoDeDiscretasIncr.add(Integer.MAX_VALUE);					
				}

			}else{
				// La variable de estado es continua
				varsEstadoContinuas.add(ve);
				tablaOrdinalDeContinuas.put(ve.getNombre(), indCont);
				ordinalEnEnumDeContinuas.add(indCont);
				ordinalEnVarsEstadoDeContinuas.add(ive);
				ordinalEnInfoPuntoDeDiscretasIncr.add(Integer.MAX_VALUE);
				indCont++;
			}
			// carga la tabla de ordinales de VE para entrar por nombre

			ordinalDeVEEnVarsEstadoCorrientes.put(ve.getNombre(), ive);

			// carga ordinal en variables de estado de VE de PEDE
			if(ve instanceof VariableEstadoPE){
				VariableEstadoPE vepe = (VariableEstadoPE)ve;
				ProcesoEstocastico pe = vepe.getPe();
				if(pe.isDiscretoExhaustivo()) {
					ordinalDePEDEEnVarsEstadoOptimizacion.put(ve.getNombre(), ive);
				}
			}

		}
		cantVECont = indCont;
		cantVEDisInc = indDisInc;
		cantVEDisNoInc = cantVE - cantVECont - cantVEDisInc;


		// Crea el enumerador lexicogrófico para recorrer los estados discretizados de VE discretas y continuas
		cotasInferioresEsta = new int[cantVE];
		cotasSuperioresEsta = new int[cantVE];		
		for(int ic=0; ic<cantVE; ic++){
			cotasInferioresEsta[ic]=0;
			cotasSuperioresEsta[ic]= varsEstadoCorrientes.get(ic).cantValoresPosibles(instanteRef)-1;
		}
		enumLexEstados = new EnumeradorLexicografico(cantVE, cotasInferioresEsta, cotasSuperioresEsta);
		enumLexEstados.creaTablaYListaOrdinales();   // Crea la lista de vectores binarios que se usarón

		// Carga la lista de vars. de control DE corrientes:
		varsControlDECorrientes = varsControlDE;

	}


	/**
	 * Devuelve en forma sucesiva los códigos enteros de las VE corrientes de los estados posibles
	 * de la grilla de estados en el paso t.
	 * 
	 * Se emplea el enumerador lexicogrófico enumLexEstados que cuando se crea en el mótodo 
	 * inicializaParaNuevasVE() se inicializa en el primer valor
	 * @return
	 */	
	public int[] devuelveCodigoEstadoDeLaGrilla(){		
		int[] vector = enumLexEstados.devuelveVector();
		return vector;		
	}



	/**
	 * Devuelve un String con los valores de Bellman o de los recursos
	 * @param paso paso del resoptim a imprimir
	 * @param pasoImpresion nómero de paso que debe a aparecer en la impresión
	 * @param resoptim debe ser el del paso paso, cargado con toda la información
	 * @param varR si es null devuelve valores de Bellman, si no es null devuelve valores del recurso asociado a varR
	 * @return
	 */
	public String publicaUnPasoValores(int paso, int pasoImpresion, long instanteRef, VariableEstado varR, Corrida corrida, String unidad){
		String retorno = "";
		if(this instanceof ResOptimIncrementos){
			ResOptimIncrementos roi = (ResOptimIncrementos)this;
			retorno = roi.getTablaValores().publicaUnPasoValoresIncrementosV2(paso, pasoImpresion, instanteRef, roi, varR, false, corrida, unidad);
		}else if(this instanceof ResOptimHiperplanos){
			ResOptimHiperplanos roh = (ResOptimHiperplanos)this;
			retorno = roh.getTablaHiperplanos().publicaUnPasoValoresHiperplanos(paso, pasoImpresion, instanteRef, roh, varR);	
			
		}
		return retorno;		
	}
	
	

	
	public String publicaUnPasoControlesDEOpt(int paso, int pasoImpresion, long instanteRef, Corrida corrida){
		String encabezado = "";
		StringBuilder sb = new StringBuilder();
		if(this.isCambioCantVE() || this.isInicio()){
			encabezado = publicaEncabezadoTablaControlesDEOpt(instanteRef);			
			sb.append(encabezado+"\n");
		}
		TablaControlesDE tab = this.getTablaControlesDE();		
		this.getEnumLexEstados().inicializaEnum();
		
		LineaTiempo lt = corrida.getLineaTiempo();	
		
		int cantCDE = varsControlDECorrientes.size();  // son las activas en el paso
		ArrayList<StringBuilder> lineas = new ArrayList<StringBuilder>();
		for(int ic=0; ic<cantCDE;ic++) {
			lineas.add(new StringBuilder());
		}
		ArrayList<int[]> estados = new ArrayList<int[]>();
		ArrayList<int[]> controles = new ArrayList<int[]>();
		int[] codigoEstado = devuelveCodigoEstadoDeLaGrilla();			
		while (codigoEstado != null) {
			estados.add(codigoEstado);
			controles.add(tab.devuelveCodigoControlesDEOpt(paso, codigoEstado));
			codigoEstado = devuelveCodigoEstadoDeLaGrilla();	
		}
		if(cantCDE>0) {
			for(int ic=0; ic<cantCDE;ic++) {
				lineas.get(ic).append(pasoImpresion + "-");
				lineas.get(ic).append(lt.fechaYHoraDeInstante(instanteRef)+"\t");	
				lineas.get(ic).append(varsControlDECorrientes.get(ic).getNombre()+"\t");
				if(varsControlDECorrientes.get(ic).isActiva()) {			
					for(int ie=0; ie<estados.size(); ie++) {
						int[] codigoControl = controles.get(ie); 			
						lineas.get(ic).append(codigoControl[ic]+"\t");							
					}
				}
				sb.append(lineas.get(ic));
				if(ic<cantCDE-1) sb.append("\n");
			}
		}else {
			sb.append(pasoImpresion+"-"+lt.fechaYHoraDeInstante(instanteRef)+"\tNO HAY VARIABLES DE CONTROL DE ACTIVAS");
		}
		return sb.toString();
	}
	
	
	
	public String publicaEncabezadoTablaControlesDEOpt(long instanteRef){
		this.getEnumLexEstados().inicializaEnum();

		StringBuilder sb = new StringBuilder("Controles discretos exhaustivos\n");
		EnumeradorLexicografico enumLex = this.getEnumLexEstados();
		enumLex.inicializaEnum();
		int cantEst = enumLex.getCantTotalVectores();
		sb.append("Cantidad de vars. estado\t");
		sb.append(this.getCantVE());
		sb.append("\n");
		sb.append("Cantidad de estados\t");
		sb.append(cantEst);
		ArrayList<StringBuilder> lineas = new ArrayList<StringBuilder>();
		ArrayList<VariableEstado> variables = this.getVarsEstadoCorrientes();
		for(int i=0; i<this.getCantVE(); i++){
			lineas.add(new StringBuilder(variables.get(i).getNombre()+"\t"+variables.get(i).getEvolDiscretizacion().getValor(instanteRef).getCantValores()+"\t"));
		}
		int[] vt = enumLex.devuelveVector();
		while(vt!= null){
			for(int i=0; i<this.getCantVE(); i++){				
				lineas.get(i).append(vt[i]);
				lineas.get(i).append("\t");
			}
			vt = enumLex.devuelveVector();
		}
		StringBuilder sbtot = new StringBuilder(sb);
		sbtot.append("\n");
		for(int i=0; i<this.getCantVE(); i++){
			sbtot.append(lineas.get(i).toString());
			sbtot.append("\n");
		}
		return sbtot.toString();
	
	}
	
	
	
	public String publicaUnPasoValoresParaML(int paso, int pasoImpresion, long instanteRef, VariableEstado varR, Corrida corrida, String unidad){
		String retorno = "";
		ResOptimIncrementos roi = (ResOptimIncrementos)this;
		retorno = roi.getTablaValores().publicaUnPasoValoresIncrementosV2ML(paso, pasoImpresion, instanteRef, roi, varR, false, corrida, unidad);
	
		return retorno;		
	}
	
	/**
	 * Devuelve un String con los valores de Bellman en el instante inicial del paso, despuós del salto
	 * de las VE de los procesos DE
	 * @param pasoImpresion nómero de paso, es el que aparece en la impresión
	 * @param resoptim debe ser el del paso paso, cargado con toda la información
	 * @param varR si es null devuelve valores de Bellman, si no es null devuelve valores del recurso asociado a varR
	 * @return
	 */	

	public String publicaValoresIniT(int pasoImpresion, long instanteRef, OptimizadorPaso op, String unidad){
		String retorno = "";
		if(this instanceof ResOptimIncrementos){
			ResOptimIncrementos roi = (ResOptimIncrementos)this;
			Corrida corrida = op.getCorrida();
			retorno = op.getvBellmanIniT().publicaUnPasoValoresIncrementosV2(0, pasoImpresion, instanteRef, roi, null, true, corrida, unidad);
		}else if(this instanceof ResOptimHiperplanos){
			ResOptimHiperplanos roh = (ResOptimHiperplanos)this;
			retorno = op.getHipersIniT().publicaUnPasoValoresHiperplanos(0, pasoImpresion, instanteRef, roh, null);			
		}
		return retorno;				
		
	}
	

//	/**	
//	 * Genera un String con los valores del Bellman del ResOptim this
//	 * @param paso paso del ResOptim a imprimir
//	 * @param pasoImpresion nómero de paso que debe a aparecer en la impresión
//	 * @param instanteRef cualquier instante del paso, se usa para obtener las discretizaciones de las VE
//	 * @param varR si es null devuelve valores de Bellman, si no es null devuelve valores del recurso asociado a varR
//	 * @return null si se pidió valor de recurso asociado a variable discreta que no es discreta incremental
//	 */
//	public String publicaUnPasoValores(int paso, int pasoImpresion, int instanteRef, VariableEstado varR){
//		String titulo;
//		int ordinalCont = -1;
//		int ordinalDE = -1;
//		ArrayList<VariableEstado> varsEstado;
//		varsEstado = this.getVarsEstadoCorrientes();
//		EnumeradorLexicografico enuml = this.getEnumLexEstados();
//		if(varR!=null && varR.isDiscreta() && !varR.isDiscretaIncremental()){
//			// varR es discreta no incremental no se hace nada
//			System.out.println("Se pidió en TablaVByValRecursos el valor asociado "
//					+ "a la variable de estado discreta exhaustiva " + varR.getNombre());
//			return null;
//		}
//	
//		if(varR==null){
//			titulo = "Valores de Bellman \n";
//		}else{
//			int ordinalVE = this.getOrdinalDeVEEnVarsEstadoCorrientes().get(varR.getNombre());	
//			titulo = "Valores del recurso asociado a " + varR.getNombre(); 
//			if(!varR.isDiscreta()){
//				// la variable es continua, se halla su ordinal entre las continuas
//				ordinalCont = this.getOrdinalEnEnumDeContinuas().get(ordinalVE);
//			}else if(varR.isDiscretaIncremental()){
//				// la variable es discreta incremental, se halla su ordinal entre las discretas incrementales 
//				ordinalDE = this.getOrdinalEnInfoPuntoDeDiscretasIncr().get(ordinalVE);
//			}else{
//				System.out.println("Se pidió en TablaVByValRecursos el valor asociado "
//						+ "a la variable de estado discreta exhaustiva " + varR.getNombre());
//			}
//		}
//		int cantVE = varsEstado.size();
//		StringBuilder sb = new StringBuilder();
//		sb.append(titulo);
//		sb.append("\n");
//		sb.append("PASO = " + pasoImpresion + "\n");	
//		sb.append("NOMBRES VE");
//		sb.append("\t");
//		for(int ive=0; ive<cantVE; ive++){
//			sb.append(varsEstado.get(ive).getNombre());
//			sb.append("\t");
//		}
//		sb.append("\n");
//		sb.append("CANT.VALORES");
//		sb.append("\t");
//		for(int ive=0; ive<cantVE; ive++){
//			sb.append(varsEstado.get(ive).devuelveDiscretizacion(instanteRef).getCantValores());
//			sb.append("\t");
//		}		
//		sb.append("\n");
//		sb.append("Código estado");
//		sb.append("\n");
//		
//		enuml.inicializaEnum();
//		int[] vt = enuml.devuelveVector();
//		while(vt!=null){
//			for(int ive=0; ive<cantVE; ive++){
//				sb.append(vt[ive]);
//				sb.append("\t");
//			}
//			ClaveDiscreta clave = new ClaveDiscreta(vt);
//			
//			if(this instanceof ResOptimIncrementos){
//				ResOptimIncrementos thisI = (ResOptimIncrementos)this;
//				TablaVByValRecursos tabla = thisI.getTablaValores();
//				InformacionValorPunto ivp = tabla.devuelveInfoValoresPunto(paso, clave);
//				if(varR==null){
//					// se carga el valor de Bellman
//					sb.append(ivp.getValorVB());
//				}else{
//					if(!varR.isDiscreta()){
//						// la variable es continua se carga la derivada parcial
//						sb.append(ivp.getDerivadasParciales()[ordinalCont]);
//					}else if(varR.isDiscretaIncremental()){
//						double[] incrementos = ivp.getIncrementosValor()[ordinalDE];
//						// la variable es discreta incremental se cargan los incrementos
//						for(int id=0; id<incrementos.length; id++){
//							sb.append(incrementos[id]);
//							sb.append("\t");
//						}
//					}				
//				}
//			}else if(this instanceof ResOptimHiperplanos){
//				ResOptimHiperplanos thisH = (ResOptimHiperplanos)this;
//				TablaHiperplanos tabla = thisH.getTablaHiperplanos();
//				Hiperplano hip = tabla.devuelveElHiperplanoDeUnPunto(paso, clave);
//				if(varR==null){
//					// se carga el valor de Bellman
//					sb.append(hip.getvBellman());
//				}else{
//					if(!varR.isDiscreta()){
//						// la variable es continua se carga el coeficiente del hiperplano con signo negativo
//						sb.append(-hip.getCoefs()[ordinalCont]);
//					}else{
//						System.out.println("Error: se pidió el valor del recurso de " + varR.getNombre() + " que no es continua");
//						System.exit(1);
//					}				
//				}
//			}
//			sb.append("\n");
//			vt = enuml.devuelveVector();	
//		}
//		return sb.toString();
//	}


	/*
	 * MóTODO QUE SE EMPLEA EN LA SIMULACIóN
	 * 
	 * Carga los valores de los controles DE óptimos, que deben aplicarse 
	 * suponiendo que hay al menos una variables de control DE
	 * AL INICIO del paso paso, en las respectivas variables de control DE 
	 * de varsControlDECorrientes.
	 * Los códigos enteros de los controles óptimos DE se obtiene del punto 
	 * de la discretización MAS CERCANO entre los estados discretizados.
	 * - Para las variables de estado discretas se toma el punto de discretización idóntico
	 * - Para las variables de estado continuas se toma el punto de discretización mós cercano de cada una de ellas
	 *   
	 * Se toman los valores de las variables de estado del atributo
	 * estado de las variables de estado corrientes
	 * 
	 * @param paso
	 *
	 * @return tabla con parejas (nombre de la variable de control DE, valor del control) 
	 *    	
	 */
	public void asignaControlesDEOptimos(int paso) {		
		// codigoVEAprox almacenaró el estado discretizado mós próximo al dado por valores
		int[] codigoVEAprox = new int[cantVE];   

		/*
		 *  Calcula el código discreto del estado, para las VE continuas el código inferior
		 * 
		 *  1 - alfas son los ponderadores del valor en pasoInf 
		 */
		int ic=0;
		for(int ive=0; ive<varsEstadoCorrientes.size(); ive++){	
			VariableEstado ve = varsEstadoCorrientes.get(ive);
			double valor = ve.getEstado();
			Discretizacion disc = ve.devuelveDiscretizacion(instanteRef);
			pasoInf = disc.devuelvePasoInferiorParaInterpolar(valor); // para las VE discretas ya se tiene el paso exacto
			codigoVEAprox[ive] = pasoInf;
			if(!ve.isDiscreta()){
				// la VE es continua
				// Si el valor excede al móximo de la discretización se toma el móximo
				if(valor>disc.getValMax()){
					valor = disc.getValMax();
					codigoVEAprox[ive] = disc.getCantValores()-1;
				}else{					
					x0 = disc.devuelveValorOrdinal(pasoInf);
					x1 = disc.devuelveValorOrdinal(pasoInf+1);					
					//  alfa es el ponderadores del valor de (pasoInf + 1)
					double alfa = (valor-x0)/(x1-x0);
					// cofigoAprox ya estó en pasoInf, si corresponde se cambia
					if(alfa>0.5) codigoVEAprox[ive] = pasoInf+1;
				}
				ic++;
			}			
		}			
		int[] codigoControles = tablaControlesDE.devuelveCodigoControlesDEOpt(paso, codigoVEAprox);	
		int ivc=0;
		for(VariableControlDE vcde: varsControlDECorrientes){
			vcde.cargaValorAPartirDeCodigo(codigoControles[ivc], instanteRef);		
			ivc++;
		}

	}

	
	/**
	 * Dada la clave de todas las VE, claveTot, devuelve la clave
	 * de las VE discretas. 
	 * En el comportamiento Hiperplanos, las VE discretas son todas las que no son continuas ya que no
	 * hay discretas incrementales.  
	 * @param claveTot
	 * @return
	 */
	public ClaveDiscreta claveVEDiscretasDeClaveTotal(ClaveDiscreta clavetot) {
		int[] codigoDiscretas = new int[cantVEDisInc+cantVEDisNoInc];
		int ive = 0;
		int idis = 0;
		for(VariableEstado ve: varsEstadoCorrientes){
			if(ve.isDiscreta()){				
				codigoDiscretas[idis]=clavetot.getEnterosIndices()[ive];
				idis++;
			}
			ive++;
		}
		if(idis != cantVEDisInc+cantVEDisNoInc) {
			System.out.println("Error en la cantidad de variables discretas al construir tabla hiperplanos");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		return new ClaveDiscreta(codigoDiscretas);
	}

	/**
	 * Almacena los códigos enteros de los controlesDE óptimos para el paso paso.
	 * Los controles se ejercen al comienzo del paso.
	 * 
	 * @paso 
	 * @codigoOpt son los códigos enteros que se refieren a la discretización de las variables
	 *            de control DE.
	 */
	public void cargaCodigoControlesDEOptimos(int paso, int[] codigoEstado, int[] codigoOpt){
		tablaControlesDE.cargaCodigoControlesDEOpt(paso, codigoEstado, codigoOpt);		
	}
	
	
	
	public abstract void guardarTablasResOptimEnDisco(String dirSalidas);


	public TablaControlesDE getTablaControlesDE() {
		return tablaControlesDE;
	}

	public void setTablaControlesDE(TablaControlesDE tablaControlesDE) {
		this.tablaControlesDE = tablaControlesDE;
	}

	public int getPasoCorriente() {
		return pasoCorriente;
	}

	public void setPasoCorriente(int pasoCorriente) {
		this.pasoCorriente = pasoCorriente;
	}

	public ArrayList<VariableEstado> getVarsEstadoCorrientes() {
		return varsEstadoCorrientes;
	}

	public void setVarsEstadoCorrientes(
			ArrayList<VariableEstado> varsEstadoCorrientes) {
		this.varsEstadoCorrientes = varsEstadoCorrientes;
	}

	/**
	 * Para cada variable de estado de varsEstadoCorrientes
	 * - si la VE es continua da una posición empezando en 0 en el enumerador lexicogrófico y en InformacionValorPunto
	 * - si la VE no es continua da el móximo entero
	 */
	public ArrayList<Integer> getOrdinalEnEnumDeContinuas() {
		return ordinalEnEnumDeContinuas;
	}

	public void setOrdinalEnEnumDeContinuas(
			ArrayList<Integer> ordinalEnEnumDeContinuas) {
		this.ordinalEnEnumDeContinuas = ordinalEnEnumDeContinuas;
	}

	/**
	 * Para cada entero del enumerador lexicogrófico, da el ordinal empezando en cero
	 * en el vector varsEstadoCorrientes de la variable de estado continua asociada
	 */
	public ArrayList<Integer> getOrdinalEnVarsEstadoDeContinuas() {
		return ordinalEnVarsEstadoDeContinuas;
	}

	public void setOrdinalEnVarsEstadoDeContinuas(
			ArrayList<Integer> ordinalEnVarsEstadoDeContinuas) {
		this.ordinalEnVarsEstadoDeContinuas = ordinalEnVarsEstadoDeContinuas;
	}

	/*
	 * Para cada variable de estado de varsEstadoCorrientes
	 * - si la VE es discreta incremental (es decir se calculan incrementos respecto a sus variaciones),
	 *   da una posición empezando en cero en InformacionValorPunto
	 * - si la VE no es discreta incremental da el móximo entero
	 *   
	 */
	public ArrayList<Integer> getOrdinalEnInfoPuntoDeDiscretasIncr() {
		return ordinalEnInfoPuntoDeDiscretasIncr;
	}

	public void setOrdinalEnInfoPuntoDeDiscretasInc(
			ArrayList<Integer> ordinalEnInfoPuntoDeDiscretasInc) {
		this.ordinalEnInfoPuntoDeDiscretasIncr = ordinalEnInfoPuntoDeDiscretasInc;
	}


	/*
	 * Para cada lugar en InformacionValorPunto para las variables discretas incrementales, devuelve
	 * el ordinal empezando en cero en el vector varsEstadoCorrientes
	 */	
	public ArrayList<Integer> getOrdinalEnVarsEstadoDeDiscretasIncr() {
		return ordinalEnVarsEstadoDeDiscretasIncr;
	}

	public void setOrdinalEnVarsEstadoDeDiscretasIncr(
			ArrayList<Integer> ordinalEnVarsEstadoDeDiscretasIncr) {
		this.ordinalEnVarsEstadoDeDiscretasIncr = ordinalEnVarsEstadoDeDiscretasIncr;
	}


	/**
	 * Dado el nombre de una variable de estado continua devuelve su ordinal
	 * entre las VE continuas, con el que aparece en InformacionValorPunto y en los Hiperplanos
	 * @return
	 */
	public int devuelveOrdinalDeUnaVEContinua(String nombreVEC){
		return tablaOrdinalDeContinuas.get(nombreVEC);
	}
	

	public long getInstanteRef() {
		return instanteRef;
	}

	public void setInstanteRef(long instanteRef) {
		this.instanteRef = instanteRef;
	}

	public int getCantVEDisInc() {
		return cantVEDisInc;
	}

	public void setCantVEDisInc(int cantVEDisInc) {
		this.cantVEDisInc = cantVEDisInc;
	}
	
	

	public int getCantVEDisNoInc() {
		return cantVEDisNoInc;
	}

	public void setCantVEDisNoInc(int cantVEDisNoInc) {
		this.cantVEDisNoInc = cantVEDisNoInc;
	}

	public int getCantVECont() {
		return cantVECont;
	}

	public void setCantVECont(int cantVECont) {
		this.cantVECont = cantVECont;
	}


	
	
	public Hashtable<String, String> getVariableDespachoDeVEContinua() {
		return variableDespachoDeVEContinua;
	}

	public void setVariableDespachoDeVEContinua(Hashtable<String, String> variableDespachoDeVEContinua) {
		this.variableDespachoDeVEContinua = variableDespachoDeVEContinua;
	}

	public GregorianCalendar getFechaInicioTabla() {
		return fechaInicioTabla;
	}


	public void setFechaInicioTabla(GregorianCalendar fechaInicioTabla) {
		this.fechaInicioTabla = fechaInicioTabla;
	}


	public int getCantVE() {
		return cantVE;
	}


	public void setCantVE(int cantVE) {
		this.cantVE = cantVE;
	}

	
	

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

	public EnumeradorLexicografico getEnumLexEstados() {
		return enumLexEstados;
	}


	public void setEnumLexEstados(EnumeradorLexicografico enumLexEstados) {
		this.enumLexEstados = enumLexEstados;
	}


	public int[] getCotasInferioresEsta() {
		return cotasInferioresEsta;
	}


	public void setCotasInferioresEsta(int[] cotasInferioresEsta) {
		this.cotasInferioresEsta = cotasInferioresEsta;
	}


	public int[] getCotasSuperioresEsta() {
		return cotasSuperioresEsta;
	}


	public void setCotasSuperioresEsta(int[] cotasSuperioresEsta) {
		this.cotasSuperioresEsta = cotasSuperioresEsta;
	}

	
	


	public ArrayList<VariableEstado> getVarsEstadoContinuas() {
		return varsEstadoContinuas;
	}

	public void setVarsEstadoContinuas(ArrayList<VariableEstado> varsEstadoContinuas) {
		this.varsEstadoContinuas = varsEstadoContinuas;
	}

	public ArrayList<VariableEstado> getVarsEstadoDisNoInc() {
		return varsEstadoDisNoInc;
	}

	public void setVarsEstadoDisNoInc(ArrayList<VariableEstado> varsEstadoDisNoInc) {
		this.varsEstadoDisNoInc = varsEstadoDisNoInc;
	}

	/**
	 * Dado el nombre de una VE devuelve el ordinal de la misma en varsEstadoCorrientes
	 */
	public Hashtable<String, Integer> getOrdinalDeVEEnVarsEstadoCorrientes() {
		return ordinalDeVEEnVarsEstadoCorrientes;
	}


	public void setOrdinalDeVEEnVarsEstadoCorrientes(Hashtable<String, Integer> ordinalDeVEEnVarsEstadoCorrientes) {
		this.ordinalDeVEEnVarsEstadoCorrientes = ordinalDeVEEnVarsEstadoCorrientes;
	}


	public void setOrdinalEnInfoPuntoDeDiscretasIncr(ArrayList<Integer> ordinalEnInfoPuntoDeDiscretasIncr) {
		this.ordinalEnInfoPuntoDeDiscretasIncr = ordinalEnInfoPuntoDeDiscretasIncr;
	}

	/**
	 * Para cada nombre de proceso estocóstico DE devuelve el ordinal en varsEstadoCorrientes
	 * de su variable de estado
	 */
	public Hashtable<String, Integer> getOrdinalDePEDEEnVarsEstadoOptimizacion() {
		return ordinalDePEDEEnVarsEstadoOptimizacion;
	}


	public void setOrdinalDePEDEEnVarsEstadoOptimizacion(Hashtable<String, Integer> ordinalDePEDEEnVarsEstadoOptimizacion) {
		this.ordinalDePEDEEnVarsEstadoOptimizacion = ordinalDePEDEEnVarsEstadoOptimizacion;
	}

	public int dameCantidadEstados() {		
		return cantVE;
	}

	public boolean isCambioCantVE() {
		return cambioCantVE;
	}

	public void setCambioCantVE(boolean cambioCantVE) {
		this.cambioCantVE = cambioCantVE;
	}

	public boolean isInicio() {
		return inicio;
	}

	public void setInicio(boolean inicio) {
		this.inicio = inicio;
	}

	
//	public void obtenerTablaControles(int numpaso,int cantEstados) {
//		this.tablaControlesDE.devuelveTabla(numpaso, 0, cantEstados);
//		
//	}
	



	
	

}

