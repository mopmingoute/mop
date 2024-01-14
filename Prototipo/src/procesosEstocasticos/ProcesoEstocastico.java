/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoEstocastico is part of MOP.
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
import java.util.GregorianCalendar;
import java.util.Hashtable;

import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProcEstocasticos.DatosGeneralesPE;
import parque.Azar;
import pizarron.PizarronRedis;
import estado.Discretizacion;
import estado.VariableEstado;
import estado.VariableEstadoPE;
import logica.CorridaHandler;
import optimizacion.OptimizadorPaso;
import simulacion.SimuladorPaso;
import tiempo.Evolucion;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;

/**
 * Clase que representa un proceso estocástico
 * @author ut602614
 * Todos los procesos estocásticos (PE) son de periodicidad anual, es decir que se les exige que:
 *	H1) En cada año calendario entra una cantidad de pasos enteros de los PE.
 *	    el primer paso del PE de cada año de todo proceso, tiene como instante inicial el instante inicial del a�o 
 *	    y el último paso del PE de cada año tiene como instante final el instante final del año
 *	H2) La validez de las variables de estado y variables aleatorias es ABIERTA AL INICIO DEL PASO 
 *	    y cerrada al final del paso del PE.
 *	H3) Excepto en los procesos de paso semanal, todos los pasos de un PE tienen igual duración, 
 *	    que se establece en segundos, en la variable durPaso.
 *
 *  H4) IMPORTANTE:  Los procesos estocásticos muestreados tiene paso de tiempo de largo igual
 *      al intervalo de muestreo. ESTO NO SE ENTIENDE ?????????
 */
public abstract class ProcesoEstocastico implements Comparable<ProcesoEstocastico> {

	/**
	 * ATENCION: LOS NOMBRES DE LAS VA DEL PROCESO ASOCIADAS A PARTICIPANTES NO PUEDEN PONERSE ARBITRARIAMENTE
	 * 
	 * SE DEBE EMPLEAR EL NOMBRE DEL ATRIBUTO + "-" + EL NOMBRE DEL PARTIPANTE ASOCIADO
	 * TODO EN MAYUSCULA
	 * 
	 */
	private String nombre;
	private String nombreProcAsociadoEnOptim;
	private boolean usoSimulacion;
	private boolean usoOptimizacion;	
	private String nombreEstimacion;   // ojo debería se un objetoEstimacion
	private boolean discretoExhaustivo;
	private String ruta;      // directorio de lectura de datos del proceso
	private int prioridadSorteo; // orden parcial en los procesos para la realización de sorteos, empezando en cero.
	
	
	private int isort; // índice de sorteo Montecarlo en la optimización
	/**
	 *  Si es true todas las VA del PE son muestreadas.
	 *  EL PASO DEL PROCESO DEBE SER IGUAL AL INTERVALO DE MUESTREO !!!!!!!!!!!
	 *  ESTO DE ARRIBA NO LO ENTIENDO 
	 */
	private boolean muestreado;
	protected boolean optim;  // True si se está usando el proceso en la optimización
	private ProcesoEstocastico procesoAsociadoEnOptim;

	private long instanteCorrienteInicial;	// Instante inicial del último paso en el que se produjo una realización 
	private long instanteCorrienteFinal;		// Instante final del último paso en el que se produjo una realización 
	protected long instanteAnteriorInvocacion; // Al inicio de la simulación vale 0
	
	/**
	 * Escenario corriente en la simulación, un número mayor o igual a cero
	 * En la optimización tiene -1.
	 */
	private int escenario;	  

	
	private int cantVA;                  // cantidad de variables aleatorias
	private int cantVE;		             // cantidad de variables de estado
	private int cantidadInnovaciones;    // tamaño de la colección innovacionesCorrientes	
	
	private Semilla semGeneral;
	private ArrayList<Double> innovacionesCorrientes;			//innovaciones en el instante corriente inicial
	private ArrayList<GeneradorDistUniforme> generadoresAleatorios;
	private GregorianCalendar inicioSorteos;
	private GregorianCalendar inicioCorrida;
	
	private ArrayList<String> nombresVarsEstado;				//	colección que establece un orden
	private ArrayList<VariableEstado> varsEstado;
	private boolean usaVarsEstadoEnOptim;
	
	private AgregadorDeEstados agregadorEstados;
	
	private Hashtable<String,Integer>   indiceVE;  // da el �ndice en la lista varsEstado de la VE según su nombre 

	private ArrayList<String> nombresVarsAleatorias;			//	colección que establece un orden
	private ArrayList<VariableAleatoria> variablesAleatorias; 	// Colección genérica de variables aleatorias, los valores de las variables se encuentran en la colección valoresAleatorios
	private Hashtable<String,Integer>   indiceVA;  // da el índice en la lista variablesAleatorias de la VA según su nombre 
	
	private boolean tieneVAExogenas;  
	private ArrayList<String> nombresVAExogenas;  // nombres de las VA exógenas
	private ArrayList<String> nombresProcesosExogenas; // nombres respectivos de los procesos de las VA Exógenas
	private ArrayList<VariableAleatoria> varsExogenas;
	private ArrayList<ProcesoEstocastico> procesosVarsExogenas;
	
	/**
	 * Primer índice recorre las variables aleatorias del proceso
	 * Segundo índice recorre los pasos del año
	 */
	private TransformacionesPE transformaciones;
	
	private String nombrePaso; // nombre de la duración del paso Ejemplo "semana"   
	private int durPaso;  // duración del paso en segundos; para la semana es la duración de 7 días
	private SimuladorPaso simuladorPaso;
	private OptimizadorPaso optimizadorPaso;
	private long instIniPasoOptim; // instante inicial del paso corriente de la optimización
	protected int cantSorteos;
	
	private Azar azar;
	
	/**
	 * Cantidad de pasos que tiene un año; los procesos de paso semanal tienen 52 pasos por año
	 * La última semana tiene 8 o 9 días para el proceso semanal.
	 * Para los procesos históricos no debe emplearse porque no hay una cantidad fija de paso en un año	
	 */
	private int cantPasosAnio; 
	
	/**
	 * Para cada nombre de variable de estado, el valor del estado inicial de la variable
	 */	
	private Hashtable<String, Double> estadosIniciales;
	
	/**
	 * Para cada nombre de una variable aleatoria del proceso, su pronóstico
	 */
	private Hashtable<String, Pronostico> pronosticos;
	

	
//	private static int[] instInicioAnio;	// para cada año de la corrida da el instante inicial y al final tiene el inicio del
	// primer a�o posterior a la corrida;
	// TODO: DEBE INICIALIZARSE AL COMIENZO DEL ESCENARIO
	
	/**
	 * indAnio es el índice empezando en 0 del año al que pertenece el instante inicial
	 * del paso corriente del proceso estocástico.
	 * indAnio recorre de 0 en adelante los años de la corrida al avanzar la simulación
	 * de un escenario a medida que el proceso va devolviendo valores, y simétricamente va recorriendo
	 * los años hacia atrás en la optimización.
	 * DEBE INICIALIZARSE en 0 AL COMIENZO DEL ESCENARIO EN SIMULACI�N
	 * DEBE INICIALIZARSE EN EL MAXIMO DE AÑOS AL COMIENZO DE LA SIMULACI�N
	 */
		
	private int indAnioLlamadaAnterior;	// último indice indAnio en las llamadas de pasoDelAnio
	private int anioLlamadaAnterior;	// último año en las llamadas de paso del año (ejemplo 2022)
	
	/*
	 * Innovaciones que se emplean en la optimización del PE con VE en la optimzación
	 * primer índice: índice de sorteo
	 * segundo indice: índice de innovación (puede haber mas de una)
	 * tercer índice: recorre intervalos de muestreo 
	 */
	private double[][][] innovacionesOptim; 
	
	private long[] aux1Inst;
	

	/**
	 * TODO: OJOJOJO AGREGADO 25 ENERO
	 */
	private boolean aportanteEstado;
	
	
	/**
	 * Se usa en producirRealizacionPEEstadoOptim() en los procesos en los que
	 * el paso del proceso es mayor que el paso de la optimización, para guardar
	 * los valores de las realizaciones de las variables aleatorias que se sortean 
	 * solo en el paso de optimización que es el último dentro del paso del PE
	 * 
	 * clave String construido a partir de los valores de las variables de estado, con el método
	 * this.claveDeEstado() y con el número de sorteo.
	 * valor contiene los valores de las VA sorteadas en el último paso de la optimización
	 * que se asocia al paso corriente del PE this
	 * el índice recorre las variables aleatorias en el orden en que aparecen en la colección
	 * del proceso.
	 */
	private Hashtable<String, ArrayList<Double>> valoresSorteadosFinPasoPE;
	
	
	
	
	public ProcesoEstocastico(){
        this.nombresVarsAleatorias = new ArrayList<String>();
        this.indiceVA = new Hashtable<String, Integer>();
        this.variablesAleatorias = new ArrayList<VariableAleatoria>();
        this.estadosIniciales = new Hashtable<String,Double>();
        this.pronosticos = new Hashtable<String,Pronostico>();
        this.nombresVarsEstado = new ArrayList<String>();
        this.indiceVE = new Hashtable<String, Integer>();
        this.varsEstado = new ArrayList<VariableEstado>();   
        this.innovacionesCorrientes = new ArrayList<Double>();
        this.generadoresAleatorios = new ArrayList<GeneradorDistUniforme>();     
        this.inicioSorteos= new GregorianCalendar();
        this.inicioCorrida = new GregorianCalendar(); 
        this.aux1Inst = new long[1];
        this.prioridadSorteo = 0;
        this.valoresSorteadosFinPasoPE = new Hashtable<String, ArrayList<Double>>();
	}
	
    public ProcesoEstocastico(DatosGeneralesPE dat) {
//        this.nombresVarsAleatorias = new ArrayList<String>();
    	this.setNombre(dat.getNombre());
    	iniciaConstruccion(dat);
        this.indiceVA = new Hashtable<String, Integer>();
        this.variablesAleatorias = new ArrayList<VariableAleatoria>();
        this.estadosIniciales = new Hashtable<String,Double>();   
        this.pronosticos = new Hashtable<String,Pronostico>();
//        this.nombresVarsEstado = new ArrayList<String>();
        this.indiceVE = new Hashtable<String, Integer>();
        this.varsEstado = new ArrayList<VariableEstado>();  
        this.procesosVarsExogenas = new ArrayList<ProcesoEstocastico>();
        this.innovacionesCorrientes = new ArrayList<Double>();
        this.generadoresAleatorios = new ArrayList<GeneradorDistUniforme>();     
        this.inicioSorteos= new GregorianCalendar();
        this.inicioCorrida = new GregorianCalendar(); 
        this.aux1Inst = new long[1];
                
        if(dat.isUsaTransformaciones()){
        	TransformacionesPE tpe = new TransformacionesPE(dat.getDatTransformaciones());
        	this.setTransformaciones(tpe);
        }
        if(dat.isUsoSimulacion() && !dat.isUsoOptimizacion()){
        	AgregadorLineal al = new AgregadorLineal(dat.getDatAgregadorEstados());
        	this.setAgregadorEstados(al);
        }       
    }
	
    
    /**
     * Este  método de entrada lo va a usar solo proceso VARMA pero debería
     * usarse en todos cuando se reestructure
     */
    public void iniciaConstruccion(DatosGeneralesPE datGen){
    	this.setMuestreado(datGen.isMuestreado());
		this.setNombre(datGen.getNombre());
		this.setNombreProcAsociadoEnOptim(datGen.getNombrePEAsociadoEnOptim());
		this.setDiscretoExhaustivo(datGen.isDiscretoExhaustivo());		
		this.setNombrePaso(datGen.getNombrePaso());
		this.setCantPasosAnio(utilitarios.Constantes.CANTMAXPASOS.get(this.getNombrePaso()));
		this.setNombreEstimacion(datGen.getNombreEstimacion());
		this.setNombresVarsAleatorias(datGen.getNombresVariables());
		this.setNombresVarsEstado(datGen.getNombresVarsEstado());
		this.setTieneVAExogenas(datGen.isTieneVAExogenas());
		this.setNombresVAExogenas(datGen.getNombresVAExogenas());
		this.setNombresProcesosExogenas(datGen.getNombresProcesosExogenas()); 
		this.setUsoOptimizacion(datGen.isUsoOptimizacion());
		this.setUsoSimulacion(datGen.isUsoSimulacion());
		this.setUsaVarsEstadoEnOptim(datGen.isUsaVarsEstadoEnOptim());
		this.setCantVA(this.getNombresVarsAleatorias().size());
		this.setCantVE(this.getNombresVarsEstado().size());
    }

	/*
	 *  Hace algunas tareas de construcción que son comunes a todos los
	 *  procesos.
	 *  Se crean las variables aleatorias y las variables de estado de los procesos.
	 *  
	 *  Las VA que son atributos de los participantes SON CREADAS PRIMERO en los
	 *  procesos estocásticos.
	 */
    public void completaConstruccion(){
    	 // Carga las tablas que dan el indice de las VA y VE a partir de sus nombres}
    	// Crea las variables aleatorias y las variables de estado
		 int ind = 0;
		 for(String nva: nombresVarsAleatorias){
			this.indiceVA.put(nva, ind);
			VariableAleatoria va =  new VariableAleatoria(nva,false,this,0.0);
			this.variablesAleatorias.add(va);
			va.setMuestreada(this.isMuestreado());
			ind++;
		 }
		 ind = 0;
		 for(String nve: nombresVarsEstado){
			this.indiceVE.put(nve, ind);
			VariableEstadoPE ve =  new VariableEstadoPE(nve);
			ve.setPe(this);
			this.varsEstado.add(ve);
			ind++;
		 }	
		 ind = 0;

		 this.durPaso = durPasoDeNombreDur(this.getNombrePaso());			 
		
	}
	

    /**
     * Inicializa el proceso para comenzar a producir realizaciones en un escenario
     * o en la optimización
     * @param semGeneral
     * @param inicioSorteos
     * @param inicioCorrida
     * @param escenario
     * 
     */
	public void inicializar(Semilla semGeneral, GregorianCalendar inicioSorteos, GregorianCalendar inicioCorrida, int escenario) {
		this.semGeneral = semGeneral;
		this.inicioSorteos = inicioSorteos;
		this.inicioCorrida = inicioCorrida;
		this.escenario = escenario;
		// TODO: ATENCION QUE EL FOR NO SE TIENE QUE HACER EN LOS PROCESOS HIST�RICOS. 
		
		generadoresAleatorios.clear();
		innovacionesCorrientes.clear();
		ArrayList<String> nombresAleatorios = new ArrayList<String>();
		nombresAleatorios.addAll(this.getNombresVarsAleatorias());
		
		
		int ind = 1;
		if (! (this instanceof PEDisponibilidadGeometrica) ){
			for(int isem=this.getCantVA(); isem<cantidadInnovaciones; isem++){
				int ordNom = isem % this.getCantVA();
				String nuevoNom = utilitarios.UtilStrings.avanzaAlfabeto(this.getNombresVarsAleatorias().get(ordNom),ind);
				nombresAleatorios.add(nuevoNom);
				ind++;
			}
		}
			
			
		for (int i = 0; i < cantidadInnovaciones; ++i) {
	//		System.out.println("ESCENARIO: " + escenario);
			generadoresAleatorios.add(new GeneradorDistUniformeLCXOr(generarInnovacionInicial(semGeneral, nombresAleatorios.get(i), inicioSorteos, escenario, i)));
			innovacionesCorrientes.add(generadoresAleatorios.get(i).generarValor());
		}			
		
		if(!optim){
			/**
			 * Se está en la simulación, iniciando un escenario
			 * Se setean los instantes del paso corriente, que es el �ltimo paso del PE que termina antes o en el mismo 
			 * instante del inicio de la corrida.
			 */
			LineaTiempo lt = this.getSimuladorPaso().getCorrida().getLineaTiempo();
			this.anioLlamadaAnterior = lt.getAnioInic();
			long instanteInicialCorrida = this.getSimuladorPaso().getCorrida().getInstanteInicial();
			int pasoPEInstanteInicial = this.pasoDelAnio(instanteInicialCorrida); // Los pasos empiezan en cero;
			Hashtable<Integer, Long> inicioAniosHT = lt.getInstInicioAnioHT();
			int anioInstanteInicial = this.anioDeInstante(instanteInicialCorrida);

			this.setInstanteCorrienteFinal(inicioAniosHT.get(anioInstanteInicial)+pasoPEInstanteInicial*this.getDurPaso());
			this.setInstanteCorrienteInicial(this.getInstanteCorrienteFinal()-this.getDurPaso());
			
			for(VariableEstado ve: varsEstado){
				boolean inicializa = true;
				if(ve instanceof VariableEstadoPE) {
					VariableEstadoPE vpe = (VariableEstadoPE)ve;
					ProcesoEstocastico pe = vpe.getPe();
					if(!pe.isUsoSimulacion()) inicializa = false;
				}
				Double estadoInicial = estadosIniciales.get(ve.getNombre());
				if(estadoInicial==null){
					if (Constantes.NIVEL_CONSOLA>1) System.out.println("no existe estado inicial en simulación para la variable de estado " + ve.getNombre());
					continue;
				}
							
				if(inicializa)ve.setEstado(devuelveEstadoInicial(ve,estadoInicial, pasoPEInstanteInicial));
			}
		} 
			
	}
	
	/**
	 * Este método debe ser sobreescrito cuando algún PE que emplea transformaciones 
	 * que se aplican a los estados, deba pasar del estadoInicial leído en el xml al estado inicial 
	 * transformado.
	 * Por ejemplo en un PE de la clase ProcesoVAR aplicado a los aportes, los estados iniciales son 
	 * los aportes rezagados tal cual son medidos, pero en el método interiormente las variables de estado
	 * de ProcesoVAR.varsEstadoVA son variables normales (0,1). Por lo tanto este método debe ser sobreescrito.
	 * Este método se invoca en ProcesoEstocastico.inicializar que inicializa el proceso para un escenario, incluso
	 * cargando los valores iniciales de las VE.
	 * @param ve  nombre de la VE a inicializar
	 * @param estadoInicial  valor del estado inicial
	 * @param pasoPEInstanteInicial  paso del año del PE
	 * @return
	 */	
	public double devuelveEstadoInicial(VariableEstado ve, double estadoInicial, int pasoPEInstanteInicial) {
		return estadoInicial;	
	}
	
	/**
	 * Carga el sentido de todas las evoluciones del proceso en 1 o -1
	 */
	public void cargaSentidoEvoluciones(int sentido){
		if(sentido!=1 & sentido!= -1){
			System.out.println("Error en carga de sentido evoluciones proceso " + this.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		if(this.isUsoOptimizacion()){
			for(VariableEstado ve: getVarsEstado()){
				Evolucion<Discretizacion> ed = ve.getEvolDiscretizacion();
				ed.setSentido(new SentidoTiempo(sentido));
			}
		}
	}
	
	

	/**
	 * @param general
	 * @param nombre : PARA LOS RECURSOS EL NOMBRE INCLUYE EL NUMERO DE MODULO!!!!
	 * @param inicioSorteos
	 * @param escenario
	 * @param i
	 * @return
	 */
	protected int generarInnovacionInicial(Semilla general, String nombre, GregorianCalendar inicioSorteos, int escenario, int i) {
		
		IniciadorSemilla iniciador = new IniciadorSemilla(general, nombre, inicioSorteos, escenario, i);
		return iniciador.hashCode();
	}


	/**
	 * Calcula una realización del proceso estocástico en el instante; el proceso se inicializó en inicioSorteos
	 * Esto se usa en la simulación, si se pide una realización de un instante ya visitado se devuelve el valor sin recalcular
	 * @param instante
	 * Queda almacenada la realización dentro de las variables del proceso estocástico
	 * en el atributo valor
	 * Para eso llama al método producirRealizacionSinPronostico de cada clase hija
	 * 	
	 */
	public void producirRealizacion(long instante) {

		producirRealizacionSinPronostico(instante);  // las clases hijas implementan este método
		
		/**
		 * Hace el promedio ponderado de la realización sin pronóstico y el pronóstico
		 */
		for(VariableAleatoria va: this.getVariablesAleatorias()) {
			if(this.getPronosticos().get(va.getNombre())!=null) {
				double valorPron=0.0;
				double pesoPron=0.0;
				if(this.getPronosticos().get(va.getNombre())!= null) {
					valorPron = this.getPronosticos().get(va.getNombre()).getValores().getValor(instante);
					pesoPron = this.getPronosticos().get(va.getNombre()).getPeso().getValor(instante);
				}
				pesoPron = Math.max(pesoPron, 0);
				if(this.isMuestreado()) {
					double[] um = va.getUltimoMuestreo();
					for(int im=0; im<um.length; im++) {
						um[im] = um[im]*(1-pesoPron) + valorPron*pesoPron;
					}
				}else {
					va.setValor(va.getValor()*(1-pesoPron)+valorPron*pesoPron) ;				
				}
			}
		}
		
		alimentarPronosticoEnAutoregresivos(instante);
		
	}
	
	public abstract void producirRealizacionSinPronostico(long instante);
	
	
	/**
	 * En los procesos que usan valores rezagados para producir realizaciones y los mantienen
	 * como variables de estado, debe sobreescribirse de modo que
	 * una vez que se produce la realización sin pronóstico y se la promedia con el pronóstico
	 * carga la realización producida en los valores rezagados que tienen los procesos 
	 * autoregresivos.
	 * 
	 * En los restantes procesos no se sobre escribe y queda este método en blanco.  
	 */
	public void alimentarPronosticoEnAutoregresivos(long instante) {
		// Deliberadamente en blanco
	}
	
	
	
	/**
	 * Actualiza el instante de la anterior invocación
	 */
	protected void actualizaInstanteAnteriorInvocacion(int instante){
		instanteAnteriorInvocacion = instante;		
	}
	
	
	/**
	 * Devuelve el valor corriente de la variable aleatoria de nombre nombreVA
	 * que ha sido producida con el m�todo producirRealizacion
	 * @param nombreVA
	 * @return el valor correspondiente, retorna cero si la variable no existe
	 */
	public double valorVA(String nombreVA){
		if (indiceVA.get(nombreVA.toUpperCase())!= null) {
			int iVA = indiceVA.get(nombreVA.toUpperCase());
			return variablesAleatorias.get(iVA).getValor();
		} return 0;
	}
	
	/**
	 * Devuelve la transformación de la variable nombreVA en el pasoDelAnio
	 * @param nombreVA
	 * @param pasoDelAnio empezando en 1.
	 */
	public TransformacionVA dameTrans(String nombreVA, int pasoDelAnio){
		return this.getTransformaciones().dameTrans(nombreVA, pasoDelAnio);
	}
	

	
	
	/**	
	 * ACA VIENEN METODOS DE LOS PE QUE SE EMPLEAN EN LOS PE QUE SEAN APORTANTES ESTADO Y QUE SON
	 * INOPERANTES EN LOS OTROS PE
	 */
	
	public  void actualizarVarsEstadoSimulacion(){
		// DELIBERADAMENTE NO HACE NADA PORQUE LOS PE YA TIENEN SU COLECCI�N DE VARIABLES 
		// DE ESTADO DE LAS QUE SE APORTAR� EN SIMULACIÓN U OPTIMIZACI�N SEG�N CORRESPONDA
		// SIRVE PARA LOS PROCESOS QUE SEAN APORTANTE ESTADO, PARA LOS OTROS ES INOPERANTE
	}
	

	public void actualizarVarsEstadoOptimizacion(){
		// DELIBERADAMENTE NO HACE NADA PORQUE LOS PE YA TIENEN SU COLECCI�N DE VARIABLES
		// DE ESTADO DE LAS QUE SE APORTAR� EN SIMULACI�N U OPTIMIZACI�N SEG�N CORRESPONDA
		// SIRVE PARA LOS PROCESOS QUE SEAN APORTANTE ESTADO, PARA LOS OTROS ES INOPERANTE
	}

	/**
	 * Devuelve la colección de variables de estado de simulación a ser considerada en la colección global, que está en el simuladorPaso
	 * @return
	 */	
	public ArrayList<VariableEstado> aportarEstadoSimulacion(){
		if(this.isUsoSimulacion() & this.cantVE>0){
			return this.getVarsEstado();
		}else{ 
			return null;
		}
	}
	

	/**
	 * Devuelve la colección de variables de estado de optimización a ser considerada en la colección global, que está en el simuladorPaso
	 * @return
	 */
	public ArrayList<VariableEstado> aportarEstadoOptimizacion(){
		if(this.isUsoOptimizacion() & this.cantVE>0 & this.isUsaVarsEstadoEnOptim()){
			return this.getVarsEstado();
		}else{ 
			return null;
		}		
		
	}
		
	/**	
	 * FIN DE METODOS DE LOS PE QUE SE EMPLEAN EN LOS PE QUE SEAN APORTANTES ESTADO Y QUE SON
	 * INOPERANTES EN LOS OTROS PE
	 */	
	
	
	/** Es sobreescrito por un método especial para los procesos históricos, en los que
	 * las duraciones de las crónicas difieren entre sí.
	 * 
	 * Devuelve el paso del año empezando en 0
	 * @param instante
	 * @return
	 */	
	public int pasoDelAnio(long instante){
		int anio = anioDeInstante(instante);
		Hashtable<Integer, Long> instInicioAnio = this.simuladorPaso.getCorrida().getLineaTiempo().getInstInicioAnioHT();
		int paso = (int)((instante-instInicioAnio.get(anio)) / durPaso);
 		if (nombrePaso.equalsIgnoreCase(Constantes.PASOSEMANA) && paso > 51) paso=51; 

//		if(instante == 189390600) System.out.println("pasoDelAnio: anio=" 
//		 + anio + "instInicioAnio=" + instInicioAnio.get(anio) + " paso=" + paso);
		return paso;		
	}
	
	
	
	
	/**
	 * Devuelve el paso del año del proceso estocástico (empezando en cero) en el instante en que comienza 
	 * un paso de la corrida (empezando en cero)
	 * Los pasos del año son cerrados por la izquierda |----)
	 */
	public int pasoDelPEDePasoCorrida(int pasoCorrida) {
		LineaTiempo lt = this.getSimuladorPaso().getCorrida().getLineaTiempo();
		PasoTiempo pt = lt.getLinea().get(pasoCorrida);
		int pasoPE = pasoDelAnio(pt.getInstanteInicial());
		return pasoPE;
	}

	
	/**
	 * Devuelve el año del instante (Ej. 2020)
	 * 
	 * 
	 * ATENCION: año es cerrado por izquierda   |--------)
	 * 
	 * El instante no puede ser anterior al inicio del primer año de la
	 * corrida ni posterior al fin del último año
	 * @param instante
	 * @return
	 */		
	public int anioDeInstante(long instante){
		int anio = anioLlamadaAnterior;
		LineaTiempo lt = this.simuladorPaso.getCorrida().getLineaTiempo();
		Hashtable<Integer, Long> instInicioAnioHT = lt.getInstInicioAnioHT();
		if(instInicioAnioHT.get(anio) <=instante && 
				(anio==lt.getAnioFin()  || instInicioAnioHT.get(anio+1)>instante) ){
//			No cambia el año respecto a la llamada anterior
//			System.out.println("En indiceDelAnioDeInstante indAnio " + indAnio);
//			System.exit(0);
		}else{
			// busca secuencialmente
			if(instante<instInicioAnioHT.get(anioLlamadaAnterior)){
				// el año debe retroceder
				while(true){
					anio--;
					if(instInicioAnioHT.get(anio)<= instante) break;
				}
				
			}else{
				// el año debe avanzar
				while(true){
					anio++;
					if(anio==lt.getAnioFin() || instInicioAnioHT.get(anio+1)>instante) break;
				}
			}			
//			System.out.println("indAnio " + indAnio);
//			System.exit(0);
		}
		anioLlamadaAnterior = anio;
		return anio;				
	}
	
	
	/**
	 * Devuelve el ordinal del paso del proceso estocástico asociado al instante instante
	 * empezando en 1 para el primer paso del año, considerando el a�o dado por indAnio
	 * Presupone que el instante pertenece al año indAnioLlamada 
	 * 
	 * @param instante
	 * @param indAnioLlamada
	 * @return
	 */
	public int pasoDelAnio(long instante, int anioLlamada){
		Hashtable<Integer,Long> instInicioAnio = this.simuladorPaso.getCorrida().getLineaTiempo().getInstInicioAnioHT();
		if(instante<instInicioAnio.get(anioLlamada)){
			System.out.println("se invoca paso del a�o con instante anterior al inicio del indAnioLlamada");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(0);
		}
		if(instante>instInicioAnio.get(anioLlamada+1)){
			System.out.println("se invoca paso del a�o con instante posterior al fin indAnioLlamada");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(0);
		}		
		int paso = (int)((instante-instInicioAnio.get(anioLlamada)) / durPaso);
		if (nombrePaso.equalsIgnoreCase(Constantes.PASOSEMANA) && paso > 51) paso=51; 
		return paso;		
		
	}
	

	
	/**
	 * Devuelve el instante inicial del año al que pertenece
	 * el instante
	 */
	public long instanteInicialAnioDeInstante(long instante){
		int anio = anioDeInstante(instante);
		return this.getSimuladorPaso().getCorrida().getLineaTiempo().getInstInicioAnioHT().get(anio);
	}
	
	
	/*
	 * Devuelve la duraci�n del paso en segundos dado el nombre de la duraci�n
	 * Para los procesos semanales devuelve la duraci�n de 7 d�as
	 */
	public static int durPasoDeNombreDur(String nombreDur){
		if(nombreDur.equalsIgnoreCase(Constantes.PASOSEMANA)) return Constantes.SEGUNDOSXSEMANA;
		if(nombreDur.equalsIgnoreCase(Constantes.PASODIA)) return Constantes.SEGUNDOSXDIA;
		if(nombreDur.equalsIgnoreCase(Constantes.PASOHORA)) return Constantes.SEGUNDOSXHORA;	
		return 0;
	}
	
	public static boolean verificaNombreDurPaso(String nombreDur){
		if(nombreDur.equalsIgnoreCase(Constantes.PASOSEMANA)) return true;
		if(nombreDur.equalsIgnoreCase(Constantes.PASODIA)) return true;
		if(nombreDur.equalsIgnoreCase(Constantes.PASOHORA)) return true;	
		return false;
	}
	
	
	/**
	 * Devuelve la VE del PE de nombre nombre
	 * y si no existe una variable en el proceso devuelve null
	 * @param nombre
	 * @return
	 */
	public VariableEstado devuelveVEDeNombre(String nombre){
		Integer indice = this.indiceVE.get(nombre);
		if(indice==null) return null;
		return this.varsEstado.get(indice);
	}
		
	
	
	/**
	 * Este método es de la interfase AportanteEstado
	 * para los PE que no son AportanteEstado no se invoca nunca 
	 */
	public void cargarValVEOptimizacion() {	
		/**
		 * Si el proceso this se usa solo en optimizacion, o bien se usa
		 * en ambas fases optimización y simulación, no hay que hacer nada.
		 * Si this se usa solo en optimización, porque las VE de this, son cargadas
		 * por el PE usado en la simulacion.
		 * Si this se usa en ambas fases, las VE usadas en la optimización son los mismos
		 * objetos que las usadas en simulación, no hay que hacer nada
		 * 
		 * Por lo tanto el método se usa solo cuando el proceso this se usa en la simulación,
		 * tiene variables de estado, y no se usa en la optimización
		 */
		if(!this.isUsoOptimizacion() && this.isUsoSimulacion() && this.cantVE>0){
			// el proceso this cargar� las VE del proceso asociado en optimizaci�n
			AgregadorDeEstados ag = this.getAgregadorEstados();
			double[] valVarExo = null;
			double[] valVarEst= new double[this.getVarsEstado().size()]; 
			if(this.getVarsExogenas()!=null){
				valVarExo= new double[this.getVarsExogenas().size()]; 
			}
			int i=0;
			for(VariableEstado ve: this.getVarsEstado()){
				valVarEst[i] = ve.getEstado();
				i++;
			}
			i=0;
			if(this.getVarsExogenas()!=null){
				for(VariableAleatoria va: this.getVarsExogenas()){
					valVarExo[i] = va.getValor();
					i++;
				}
			}
			double[] valVEOp = ag.devuelveEstadoOptim(valVarEst, valVarExo);
			ProcesoEstocastico popt = this.getProcesoAsociadoEnOptim();
			i=0;
			for(VariableEstado ve: popt.getVarsEstado()){
				ve.setEstado(valVEOp[i]);
				ve.setEstadoDespuesDeCDE(valVEOp[i]);
				ve.setEstadoS0fint(valVEOp[i]);
				i++;
			}
		}
	
	}

	/**
	 * Este método es de la interfase AportanteEstado
	 * para los PE que no son AportanteEstado no se invoca nunca.
	 * Se toma el valor de S0fint como estado final del paso, sin cambios 
	 */
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		for(VariableEstado ve: this.getVarsEstado()){
			ve.setEstadoFinalOptim(ve.getEstadoS0fint());
		}
	}
	
	/**
	 * Devuelve la VA del PE de nombre nombre
	 * y si no existe una variable en el proceso devuelve null
	 * @param nombre
	 * @return
	 */
	public VariableAleatoria devuelveVADeNombre(String nombre){	
		Integer indice = this.indiceVA.get(nombre);
		if(indice==null) return null;
		return this.variablesAleatorias.get(this.indiceVA.get(nombre));
	}	
	
	
	
	/**
	 * Devuelve el intervalo de muestreo del paso en segundos si el proceso es muestreado y cero si no es 
	 * muestreado
	 */
	public int devuelveIntervaloMuestreo(){
		if(optim){
			return optimizadorPaso.getPasoActual().getIntervaloMuestreo();
		}else{
			return simuladorPaso.getPasoActual().getIntervaloMuestreo();
		}		
	}
	
	
	
	/**
	 * Construye el nombre de una variable rezagada de un PE a partir del nombre de la variable original
	 * Le agrega el sufijo -Ln   n es el entero positivo o nulo igual a la cantidad de rezagos.
	 */
	public String nombreSerieRezagada(String nombre, int rezagos) {
		return nombre + "-L" + rezagos;
	}
	
	
	/**
	 * Obtiene el nombre de la variable original a partir del nombre de la rezagada
	 * @param  el nombre de una variable rezagada, que tiene por lo tanto el sufijo -Ln
	 */
	public String nombreSerieOriginal(String nombreRez) {
		String[] partes = nombreRez.split("-");
		int largoLag = partes[partes.length-1].length();
		String nuevoNombre = nombreRez.substring(0, nombreRez.length()-largoLag-1);
		return nuevoNombre;
	}
	
	/**
	 * Obtiene el lag a partir del nombre de una variable rezagada
	 * @param  el nombre de una variable rezagada, que tiene por lo tanto el sufijo -Ln
	 */
	public int lagAPartirDeNombre(String nombreRez) {
		String[] partes = nombreRez.split("-");
		String numCar = partes[partes.length-1];
		int numInt = Integer.parseInt(numCar.substring(1));
		return numInt;
	}
	
	/**
	 * COMIENZAN METODOS REQUERIDOS EN LA OPTIMIZACION
	 * 
	 */
	

		
	/**
	 * Muestrea y carga los valores de las variables aleatorias de procesos que tienen estado 
	 * en la optimización para ser empleados en 
	 * UNO DE LOS SORTEOS Montecarlo en la optimización, para cada instante de muestreo, o para 
	 * el único valor si la VA no es muestreada.
	 * Lo mismo para los valores de las VA de PE discretos exhaustivos.
	 * 
	 * Las innovaciones ya fueron sorteadas en OptimizadorPaso
	 * 
	 * 
	 * Este método carga 
	 * - valor para las VA que no son muestreadas
	 * - ultimoMuestreo[] para las VA que son muestreadas
	 * 
	 * @instantesMuestreo vector con los instantes de muestreo
	 * @innovaciones1Sort innovaciones a emplear en el sorteo
	 * 	primer índice recorre índices de innovación,
	 * 	segundo índice recorre intervalos de muestreo 
	 * @isort índice del sorteo que se está generando y se cargará en la VA
	 * 
	 * ATENCION: NO DEBE MODIFICARSE LAS VARIABLES DE ESTADO DEL PROCESO
	 * 
	 */
	public abstract void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort);		
	

	
	
	/**
	 * FIN DE METODOS REQUERIDOS EN LA OPTIMIZACION
	 */
	
	
	
	/**
	 * Método de uso general que elige un individuo al azar de una población
	 * @param cantPoblacion cantidad de indiduos de una poblaci�n ordenada
	 * @param aleatUnif variable aleatoria uniforme [0,1]
	 * @return ordinal entre 0 y cantPoblacion-1 seleccionado de acuerdo
	 * al valor de la variable aleatoria aleatUniv
	 */
	public static int eligeUnOrdinalDeUnaPoblacion(int cantPoblacion, double aleatUnif){
		return (int)Math.floor(aleatUnif*cantPoblacion);
	}

	
	
	public SimuladorPaso getSimuladorPaso() {
		return simuladorPaso;
	}


	public void setSimuladorPaso(SimuladorPaso simuladorPaso) {
		this.simuladorPaso = simuladorPaso;
	}

	
	

	public OptimizadorPaso getOptimizadorPaso() {
		return optimizadorPaso;
	}


	public void setOptimizadorPaso(OptimizadorPaso optimizadorPaso) {
		this.optimizadorPaso = optimizadorPaso;
	}

	

	public boolean isUsoSimulacion() {
		return usoSimulacion;
	}


	public void setUsoSimulacion(boolean usoSimulacion) {
		this.usoSimulacion = usoSimulacion;
	}


	public boolean isUsoOptimizacion() {
		return usoOptimizacion;
	}


	public void setUsoOptimizacion(boolean usoOptimizacion) {
		this.usoOptimizacion = usoOptimizacion;
	}


	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	
	public String getNombreProcAsociadoEnOptim() {
		return nombreProcAsociadoEnOptim;
	}

	public void setNombreProcAsociadoEnOptim(String nombreProcAsociadoEnOptim) {
		this.nombreProcAsociadoEnOptim = nombreProcAsociadoEnOptim;
	}

	public String getNombrePaso() {
		return nombrePaso;
	}

	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}

	

	public String getNombreEstimacion() {
		return nombreEstimacion;
	}


	public void setNombreEstimacion(String nombreEstimacion) {
		this.nombreEstimacion = nombreEstimacion;
	}



	public AgregadorDeEstados getAgregadorEstados() {
		return agregadorEstados;
	}


	public void setAgregadorEstados(AgregadorDeEstados agregadorEstados) {
		this.agregadorEstados = agregadorEstados;
	}


	public TransformacionesPE getTransformaciones() {
		return transformaciones;
	}


	public void setTransformaciones(TransformacionesPE transformaciones) {
		this.transformaciones = transformaciones;
	}


	public void setProcesoAsociadoEnOptim(ProcesoEstocastico procesoAsociadoEnOptim) {
		this.procesoAsociadoEnOptim = procesoAsociadoEnOptim;
	}


	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public Hashtable<String, Integer> getIndiceVE() {
		return indiceVE;
	}

	public void setIndiceVE(Hashtable<String, Integer> indiceVE) {
		this.indiceVE = indiceVE;
	}

	public Hashtable<String, Integer> getIndiceVA() {
		return indiceVA;
	}

	public void setIndiceVA(Hashtable<String, Integer> indiceVA) {
		this.indiceVA = indiceVA;
	}

	public int getDurPaso() {
		return durPaso;
	}

	public void setDurPaso(int durPaso) {
		this.durPaso = durPaso;
	}

	public Semilla getSemGeneral() {
		return semGeneral;
	}

	public void setSemGeneral(Semilla semGeneral) {
		this.semGeneral = semGeneral;
	}

	
	
	public ArrayList<Double> getInnovacionesCorrientes() {
		return innovacionesCorrientes;
	}

	public void setInnovacionesCorrientes(ArrayList<Double> innovacionesCorrientes) {
		this.innovacionesCorrientes = innovacionesCorrientes;
	}

	public ArrayList<GeneradorDistUniforme> getGeneradoresAleatorios() {
		return generadoresAleatorios;
	}

	public void setGeneradoresAleatorios(
			ArrayList<GeneradorDistUniforme> generadoresAleatorios) {
		this.generadoresAleatorios = generadoresAleatorios;
	}

	public GregorianCalendar getInicioSorteos() {
		return inicioSorteos;
	}

	public void setInicioSorteos(GregorianCalendar inicioSorteos) {
		this.inicioSorteos = inicioSorteos;
	}

	public GregorianCalendar getInicioCorrida() {
		return inicioCorrida;
	}

	public void setInicioCorrida(GregorianCalendar inicioCorrida) {
		this.inicioCorrida = inicioCorrida;
	}

	
	public int getCantidadInnovaciones() {
		return cantidadInnovaciones;
	}

	public void setCantidadInnovaciones(int cantidadInnovaciones) {
		this.cantidadInnovaciones = cantidadInnovaciones;
	}

	public long getInstanteCorrienteInicial() {
		return instanteCorrienteInicial;
	}

	public void setInstanteCorrienteInicial(long instanteCorrienteInicial) {
		this.instanteCorrienteInicial = instanteCorrienteInicial;
	}

	public long getInstanteCorrienteFinal() {
		return instanteCorrienteFinal;
	}

	public void setInstanteCorrienteFinal(long instanteCorrienteFinal) {
		this.instanteCorrienteFinal = instanteCorrienteFinal;
	}

	public int getEscenario() {
		return escenario;
	}

	public void setEscenario(int escenario) {
		this.escenario = escenario;
	}
	public boolean isDiscretoExhaustivo() {
		return discretoExhaustivo;
	}

	public void setDiscretoExhaustivo(boolean discretoExhaustivo) {
		this.discretoExhaustivo = discretoExhaustivo;
	}

	public ArrayList<String> getNombresVarsEstado() {
		return nombresVarsEstado;
	}

	public void setNombresVarsEstado(ArrayList<String> nombresVarsEstado) {
		this.nombresVarsEstado = nombresVarsEstado;
	}
	
	

	public boolean isUsaVarsEstadoEnOptim() {
		return usaVarsEstadoEnOptim;
	}


	public void setUsaVarsEstadoEnOptim(boolean usaVarsEstadoEnOptim) {
		this.usaVarsEstadoEnOptim = usaVarsEstadoEnOptim;
	}


	public ArrayList<String> getNombresVarsAleatorias() {
		return nombresVarsAleatorias;
	}

	public void setNombresVarsAleatorias(ArrayList<String> nombresVarsAleatorias) {
		this.nombresVarsAleatorias = nombresVarsAleatorias;
	}

	

	public boolean isTieneVAExogenas() {
		return tieneVAExogenas;
	}


	public void setTieneVAExogenas(boolean tieneVAExogenas) {
		this.tieneVAExogenas = tieneVAExogenas;
	}


	public ArrayList<String> getNombresVAExogenas() {
		return nombresVAExogenas;
	}


	public void setNombresVAExogenas(ArrayList<String> nombresVAExogenas) {
		this.nombresVAExogenas = nombresVAExogenas;
	}


	public ArrayList<String> getNombresProcesosExogenas() {
		return nombresProcesosExogenas;
	}


	public void setNombresProcesosExogenas(ArrayList<String> nombresProcesosExogenas) {
		this.nombresProcesosExogenas = nombresProcesosExogenas;
	}

	
	

	public ArrayList<VariableAleatoria> getVarsExogenas() {
		return varsExogenas;
	}


	public void setVarsExogenas(ArrayList<VariableAleatoria> varsExogenas) {
		this.varsExogenas = varsExogenas;
	}

	

	public ArrayList<ProcesoEstocastico> getProcesosVarsExogenas() {
		return procesosVarsExogenas;
	}


	public void setProcesosVarsExogenas(ArrayList<ProcesoEstocastico> procesosVarsExogenas) {
		this.procesosVarsExogenas = procesosVarsExogenas;
	}


	public ArrayList<VariableEstado> getVarsEstado() {
		return varsEstado;
	}

	public void setVarsEstado(ArrayList<VariableEstado> varsEstado) {
		this.varsEstado = varsEstado;
	}

	public ArrayList<VariableAleatoria> getVariablesAleatorias() {
		return variablesAleatorias;
	}

	public void setVariablesAleatorias(ArrayList<VariableAleatoria> variablesAleatorias) {
		this.variablesAleatorias = variablesAleatorias;
	}

	
	
	public int getCantVA() {
		return cantVA;
	}

	public void setCantVA(int cantVA) {
		this.cantVA = cantVA;
	}

	public int getCantVE() {
		return cantVE;
	}

	public void setCantVE(int cantVE) {
		this.cantVE = cantVE;
	}


	public Azar getAzar() {
		return azar;
	}

	public void setAzar(Azar azar) {
		this.azar = azar;
	}


	public int getCantPasosAnio() {
		return cantPasosAnio;
	}


	public void setCantPasosAnio(int cantPasosAnio) {
		this.cantPasosAnio = cantPasosAnio;
	}


	public double[][][] getInnovacionesOptim() {
		return innovacionesOptim;
	}


	public void setInnovacionesOptim(double[][][] innovacionesOptim) {
		this.innovacionesOptim = innovacionesOptim;
	}


	public boolean isMuestreado() {
		return muestreado;
	}


	public void setMuestreado(boolean muestreado) {
		this.muestreado = muestreado;
	}
	
	
	

	
	public int getAnioLlamadaAnterior() {
		return anioLlamadaAnterior;
	}


	public void setAnioLlamadaAnterior(int anioLlamadaAnterior) {
		this.anioLlamadaAnterior = anioLlamadaAnterior;
	}


	/*
	 * Sortea innovaciones que se emplean en la optimización del PE con VE en la optimzaci�n
	 * primer índice: índice de sorteo
	 * segundo índice: índice de innovación (puede haber mas de una)
	 * tercer índice: recorre intervalos de muestreo 
	 */		
	public void sortearInnovacionesOptim(int cantSortMontecarlo, long[] instantesMuestreo) {	
		innovacionesOptim = new double[cantSortMontecarlo][this.cantidadInnovaciones][instantesMuestreo.length];	
		for(int isort=0; isort<cantSortMontecarlo; isort++){
			for(int iinv=0; iinv<instantesMuestreo.length; iinv++){
				int iga=0;
				for (GeneradorDistUniforme gdu: generadoresAleatorios){
					innovacionesOptim[isort][iga][iinv] =gdu.generarValor();
					iga++;
				}
			}
		}		
	}

	/**
	 * Se aplica a todos los procesos con y sin VE en la optimizaci�n.
	 * 
	 * Carga los valores de las VA en el atributo
	 * - valor para los procesos que no son muestreados
	 * - ultimoMuestreo para los procesos muestreados
	 * para el sorteo isort 
	 * 
	 * Cada PE sabe si es muestreado o no y si tiene VE en la optimización o no, y según el caso carga
	 * los valores de las VA.
	 * 
	 * @param isort es el índice del sorteo Montecarlo
	 * 
	 * 
	 * 
	 * 
	 */
	public void cargarVAOptim(int isort, long[] instantesMuestreo) {
		if(!this.tieneVEOptim()){
			/**
			 * El PE no tiene VE en la optimización, se usa el valor
			 * de las VA sorteado fuera del loop en los estados, para el 
			 * sorteo isort
			 * 
			 * ESOS VALORES FUERON HECHOS CON EL METODO producirRealizacion
			 * POR LO QUE YA VIENEN PROMEDIADOS CON EL PRONOSTICO
			 * 
			 */
			for(VariableAleatoria va: this.getVariablesAleatorias()){
				if(this.isMuestreado()){
					va.setUltimoMuestreo(va.getUltimoMuestreoOptim()[isort]);
				}else{
					va.setValor(va.getUltimoMuestreoOptim()[isort][0]);
				}
			}						
		}else{
			/**
			 * El PE tiene VE en la optimización
			 *
			 * Las innovaciones ya fueron sorteadas en OptimizadorPaso
			 * Innovaciones que se emplean en la optimización del PE con VE en la optimizaci�n
			 * 
			 * innovacionesOptim
			 * primer índice: índice de sorteo
			 * segundo índice: índice de innovación (puede haber mas de una)
			 * tercer índice: recorre intervalos de muestreo 
			 * 
			 * innovacionesOptim[isort] es entonces un double[][]
			 * 
			 * ATENCION: LOS VALORES OBTENIDOR POR producirRealizacionPEEstadoOptim NO DEBEN 
			 * VENIR PONDERADOS POR EL PRONOSTICO, PORQUE LA PONDERACIÓN SE HACE AQUI. 
			 * POR LO TANTO EL METODO producirRealizacionPEEstadoOptim NO DEBE RECURRIR EN 
			 * NINGUN CASO AL METODO producirRealizacion del proceso hijo sino evuentualmente a
			 * producirRealizacionSinPronostico SI FUERA EL CASO.
			 * 
			 */
			if(this.isMuestreado()){
				producirRealizacionPEEstadoOptim(instantesMuestreo, innovacionesOptim[isort], isort);
			}else{				
				aux1Inst[0] = instantesMuestreo[0];
				producirRealizacionPEEstadoOptim(aux1Inst, innovacionesOptim[isort], isort);
			}	
			
			/**
			 * Hace el promedio ponderado de la realización sin pronóstico y el pronóstico
			 */
			for(VariableAleatoria va: this.getVariablesAleatorias()) {
				double valorPron;
				double pesoPron;
				long instante;
				if(this.getPronosticos().get(va.getNombre())!= null){
					if(this.isMuestreado()) {				
						double[] um = va.getUltimoMuestreo();
						for(int im=0; im<um.length; im++) {
							instante = instantesMuestreo[im];
							valorPron = this.getPronosticos().get(va.getNombre()).getValores().getValor(instante);
							pesoPron = this.getPronosticos().get(va.getNombre()).getPeso().getValor(instante);
							pesoPron = Math.max(pesoPron, 0);
							um[im] = um[im]*(1-pesoPron) + valorPron*pesoPron;
						}
					}else {
						instante = instantesMuestreo[0];
						valorPron = this.getPronosticos().get(va.getNombre()).getValores().getValor(instante);
						pesoPron = this.getPronosticos().get(va.getNombre()).getPeso().getValor(instante);
						pesoPron = Math.max(pesoPron, 0);
						va.setValor(va.getValor()*(1-pesoPron)+valorPron*pesoPron) ;				
					}
				}
			}
		}
		

		
	}

	/**
	 * Devuelve true si el m�todo tiene variables de estado que 
	 * son parte del estado de la optimizaci�n.
	 * Cada PE en particular lo implementa en su c�digo
	 * @return
	 */
	public abstract boolean tieneVEOptim();
	

	public long getInstIniPasoOptim() {
		return instIniPasoOptim;
	}

	public void setInstIniPasoOptim(long instIniPasoOptim) {
		this.instIniPasoOptim = instIniPasoOptim;
	}


	
	public boolean isOptim() {
		return optim;
	}

	public void setOptim(boolean optim) {
		this.optim = optim;
	}

	public long getInstanteAnteriorInvocacion() {
		return instanteAnteriorInvocacion;
	}

	public void setInstanteAnteriorInvocacion(int instanteAnteriorInvocacion) {
		this.instanteAnteriorInvocacion = instanteAnteriorInvocacion;
	}



	public int getCantSorteos() {
		return cantSorteos;
	}

	public void setCantSorteos(int cantSorteos) {
		this.cantSorteos = cantSorteos;
	}
	
	
	public ProcesoEstocastico getProcesoAsociadoEnOptim() {
		return procesoAsociadoEnOptim;
	}


	public void setProcAsociadoEnOptim(ProcesoEstocastico procesoAsociadoEnOptim) {
		this.procesoAsociadoEnOptim = procesoAsociadoEnOptim;
	}

	public Hashtable<String, ArrayList<Double>> getValoresSorteadosFinPasoPE() {
		return valoresSorteadosFinPasoPE;
	}

	public void setValoresSorteadosFinPasoPE(Hashtable<String, ArrayList<Double>> valoresSorteadosFinPasoPE) {
		this.valoresSorteadosFinPasoPE = valoresSorteadosFinPasoPE;
	}

	public int compareTo(ProcesoEstocastico pe){
		if(this.getPrioridadSorteo()<pe.getPrioridadSorteo()){
			return -1;
		}else if(this.getPrioridadSorteo() == pe.getPrioridadSorteo()){
			return 0;
		}
		return 1;		
	}


	
	
/**
 * Carga en ultimoMuestreo de todas las variables aleatorias del proceso
 * los valores de realizaciones para cada uno de los instantes de instantesM	
 * @param instantesM
 */
	public void muestrearVariablesAleats(long[] instantesM) {
		// TODO  ESTA CONSULTA DEBER�A SACARSE
		boolean procesoTieneMuestreadas = false;
		for (VariableAleatoria va: variablesAleatorias) {
			if (va.isMuestreada()) {
				procesoTieneMuestreadas = true;
				break;
			}			
		}
		//////////////////////////////////////
		if (procesoTieneMuestreadas) {
			for (VariableAleatoria va: variablesAleatorias) {
				va.crearDatosMuestreados(instantesM.length);
			}
			for (int i = 0; i < instantesM.length; i++) {
				producirRealizacion(instantesM[i]);
				for (VariableAleatoria va: variablesAleatorias) {
					va.guardarDatoMuestreado(i);
				}
			
			}	
		}
		
	}
	
	/**
	 * Este método sólo se ejecuta para los PE que no tienen VE en la optimización 
	 * @param cantSorteos
	 * @param instantesM
	 */
	public void muestrearVariablesAleatsOptim(int cantSorteos, long[] instantesM) {
		
		for (VariableAleatoria va: variablesAleatorias) {
			va.crearDatosMuestreadosOptim(cantSorteos, instantesM.length);			
		}
		
		for(int is=0; is<cantSorteos; is++){
			/**
			 * Como para sortear es necesario partir de algún valor de las VE del proceso
			 * y posiblemente hacer otras cosas dependiendo del tipo de PE antes de cada sorteo
			 * se llama a preparaUnSorteoPEsinVEOptim, que carga heurísticas de las VE del PE
			 * y ejecuta esas otras cosas.
			 */			 
			preparaUnSorteoMontecarloPEsinVEOptim(is);
			for (int i = 0; i < instantesM.length; i++) {
								
				producirRealizacion(instantesM[i]);				
				/*
				 * Las VA toman la realización corriente y la guardan en la estructura
				 * para guardar las realizaciones por sorteo e instante de muestreo.
				 */
				for (VariableAleatoria va: variablesAleatorias) {
					va.guardarDatoMuestreadoOptim(is, i);
				}
			}
		}
	}

	
	/**
	 * Se aplica a todos los procesos al inicio del sorteo Montecarlo
	 * en optimizadorEstado.
	 * Obliga a que todos los PE tengan que producir una realización
	 */
	public void preparaUnSorteoMontecarlo(){
		this.setInstanteCorrienteFinal(this.getInstIniPasoOptim()-utilitarios.Constantes.EPSILONSALTOTIEMPO);
	}
	
	/**
	 * Devuelve true si:
	 * - la duracion del paso PasoPE del PE this (al que pertenece el instante inicial del 
	 * paso de la optimización corriente) es mayor que la duracion del paso de la optimización 
	 * corriente PasoOp
	 * - y además PasoPE es el último paso del PE del paso de la optimización PasoOp
	 * 
	 * Se emplea en producirRealizacionPEEstadoOptim
	 * 
	 */
	public boolean esUltimoPasoDelPasoPE(){
		if(!durPasoPEMayorPasoOptim()) return false;
		int pasoIni = pasoDelAnio(this.getOptimizadorPaso().getInstIniPaso());
		int pasoFin = pasoDelAnio(this.getOptimizadorPaso().getInstFinPaso()+utilitarios.Constantes.EPSILONSALTOTIEMPO);
		return(pasoIni<pasoFin);  
	}
	
	
	/**
	 * Devuelve true si la duracion del paso del PE this es mayor que la duracion del paso
	 * corriente de la optimización
	 * @return
	 */
	public boolean durPasoPEMayorPasoOptim(){
		if(this.nombrePaso.contentEquals(utilitarios.Constantes.PASO_SEMANAL) &&
			this.getOptimizadorPaso().getPasoActual().getDuracionPaso()<utilitarios.Constantes.SEGUNDOSXSEMANA)
			return true;
		if(this.nombrePaso.contentEquals(utilitarios.Constantes.PASO_DIARIO) &&
				this.getOptimizadorPaso().getPasoActual().getDuracionPaso()<utilitarios.Constantes.SEGUNDOSXDIA)
				return true;
		if(this.nombrePaso.contentEquals(utilitarios.Constantes.PASO_HORARIO) &&
				this.getOptimizadorPaso().getPasoActual().getDuracionPaso()<utilitarios.Constantes.SEGUNDOSXHORA)
				return true;				
		else return false;	
	}
		
	

	/**
	 * Construye un String que sirve como clave de hash para el conjunto de los valores
	 * de las variables de estado corrientes del proceso this y el número de sorteo.
	 * Del valor de cada estado se toman los ncar caracteres del string que representa al double del estado.
	 * @param ncar cantidad de caracteres de la representación String del valor del estado que se toman
	 */
	public String claveEstSort(int ncar, Integer sorteo){
		StringBuilder clave = new StringBuilder("");
		for(VariableEstado ve: this.getVarsEstado()){
			String s1 = ve.getEstado().toString();
			if(s1.length()>ncar) s1 = s1.substring(0,ncar);
			clave.append(s1 + "_");
		}
		clave.append(sorteo.toString());
		return clave.toString();
	}
	
	/*
	 * Se usa además del anterior preparaUnSorteoMontecarlo, cuando el proceso
	 * no tiene VE en la optimización.
	 * Se invoca en muestrearVariablesAleatsOptim			
	 * Como para sortear es necesario partir de algún valor de las VE del proceso
	 * y posiblemente hacer otras cosas dependiendo del tipo de PE, antes de cada sorteo
	 * se llama a prepararUnSorteoPEsinVEOptim, que carga heur�sticas de las VE del PE
	 * y ejecuta esas otras cosas.
	 */			 
	public void preparaUnSorteoMontecarloPEsinVEOptim(int isort) {
		this.isort = isort;
	}

	/**
	 * Devuelve el inicio del paso corriente de la optimizaci�n
	 * a trav�s del OptimizadorPaso
	 * @return
	 */
	public long devuelveInicioPasoCorrienteDeOptimizacion(){
		return optimizadorPaso.getPasoActual().getInstanteInicial();

	}
	

	public boolean isAportanteEstado() {
		return aportanteEstado;
	}


	public void setAportanteEstado(boolean aportanteEstado) {
		this.aportanteEstado = aportanteEstado;
	}


	public abstract void prepararPasoOptim(int cantSortMontecarlo);


	public Hashtable<String, Double> getEstadosIniciales() {
		return estadosIniciales;
	}


	public void setEstadosIniciales(Hashtable<String, Double> estadosIniciales) {
		this.estadosIniciales = estadosIniciales;
	}	

	
	
	
    public int getIndAnioLlamadaAnterior() {
		return indAnioLlamadaAnterior;
	}


	public void setIndAnioLlamadaAnterior(int indAnioLlamadaAnterior) {
		this.indAnioLlamadaAnterior = indAnioLlamadaAnterior;
	}

	
	public int getPrioridadSorteo() {
		return prioridadSorteo;
	}


	public void setPrioridadSorteo(int prioridadSorteo) {
		this.prioridadSorteo = prioridadSorteo;
	}	
	

	public Hashtable<String, Pronostico> getPronosticos() {
		return pronosticos;
	}

	public void setPronosticos(Hashtable<String, Pronostico> pronosticos) {
		this.pronosticos = pronosticos;
	}

	public static void main(String[] args) {       
    	
    	/**
    	 * Devuelve el indice del a�o del instante
    	 * El �ndice se refiere a la posici�n del a�o en 
    	 * la extensi�n de la corrida empezando en 0 para el primer a�o
    	 * 
    	 * ATENCI�N: el �ndice del a�o es cerrado por izquierda   |--------)
    	 * 
    	 * El instante no puede ser anterior al inicio del primer a�o de la
    	 * corrida ni posterior al fin del �ltimo a�o
    	 * @param instante
    	 * @return
    	 */
    	int instante = 94608010;
    	int indAnioLlamadaAnterior=3;
		int indAnio = indAnioLlamadaAnterior;
		System.out.println("instante " + instante + " indAnioLlamadaAnterior " + indAnioLlamadaAnterior);

		int[] instInicioAnio = new int[] {1, 31536001, 63072001, 94608001};
		if(instInicioAnio[indAnio]<=instante && 
				(indAnio==instInicioAnio.length-1  || instInicioAnio[indAnio+1]>instante) ){
			System.out.println("indAnio " + indAnio);
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(0);
		}else{
			// busca secuencialmente
			if(instante<instInicioAnio[indAnioLlamadaAnterior]){
				// el �ndice debe retroceder
				while(true){
					indAnio--;
					if(instInicioAnio[indAnio]<= instante) break;
				}
				
			}else{
				// el �ndice tiene que avanzar
				while(true){
					indAnio++;
					if(indAnio==instInicioAnio.length-1 || instInicioAnio[indAnio+1]>instante) break;
				}
				
			}			
			System.out.println("indAnio " + indAnio);
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(0);
		}		
	
    	    	
	}

	public void preparaUnSorteoMontecarloPEsinVEOptim() {
		// TODO Auto-generated method stub
		
	}

	public int getIsort() {
		return isort;
	}

	public void setIsort(int isort) {
		this.isort = isort;
	}




}
