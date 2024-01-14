/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoBootstrapDiscreto is part of MOP.
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

import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProcEstocasticos.DatosPEBootstrapDiscreto;
import datatypesProcEstocasticos.DatosPEMarkov;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import estado.Discretizacion;
import estado.VariableEstado;
import futuro.AFIncrementos;
import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;
import persistencia.CargadorPEBootstrapDiscreto;
import pizarron.PizarronRedis;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.LineaTiempo;
import tiempo.SentidoTiempo;
import utilitarios.DirectoriosYArchivos;
import utilitarios.EnumeradorLexicografico;
import utilitarios.LeerDatosArchivo;
import utilitarios.Par;



/**
 * Clase para representar procesos donde se generan valores por muestreo 
 * de datos históricos. 
 * Para fijar ideas se muestrean por ejemplo días con 24 horas de datos cada uno.
 * En ese caso el paso es el día y el intervalo es la hora.
 * 
 * ATENCION:
 * En esta clase se emplea la palabra hora como el intervalo para 
 * el que hay un valor y la palabra día como el conjunto de horas que se muestrean todas
 * juntas sucesivamente.
 * Supongamos que se muestrean dias de 24 horas, con un valor por hora.
 * En el lenguaje de la clase ProcesoEstocastico, el paso del PE sería la hora.
 * 
 * Si se muestreasen días con 48 medias horas, la "hora" sería la media hora
 * 
 * 
 * El muestreo de un nuevo día está condicionado por la clase discreta a la
 * que pertenece el día anterior. Para eso se definen estados compuestos según 
 * los estados de las variables que componen los datos.
 * 
 * @author ut469262
 *
 */
public class ProcesoBootstrapDiscreto extends ProcesoEstocastico implements AportanteEstado{
	
	/**
	 * Cantidad de pasos del año hacia adelante y hacia atrás que constituyen la misma
	 * población. Si rango = 20, la población comprende los pasos t y t-1,....t-20, t+1,....t+20
	 * donde t es un paso del año
	 */
	private int rangoPasos;  
		
	/**
     * Ponderadores para construir las variable de estado parciales.
     * primer indice variable de estado parcial
     * segundo �ndice hora dentro del paso.
     * Las variables de estado se numeran del 0 en adelante.
     * 
     */    
    private double[][] ponderadores;
    private double[] sumapond; // suma de los ponderadores de cada variable de estado parcial

    
    /**
     * Cantidad de clases en la variable de estado continua para cada variable de datos;
     */
    private int[] cantCla;
    
	/**
     * Probabilidades de cada clase para variable de estado parcial definida.
     * Primer indice variable de estado, segundo �ndice clase.
     * Las clases se numeran del 0 en adelante.
     * Puede haber diferente cantidad de clases en cada serie.
     */
    private double[][] probCla; 
    
	/**
	 * L�mites superiores de las clases de las variables de estado continuas
	 * primer índice: ordinal de dia del año
	 * segundo índice: ordinal de variable aleatoria
	 * tercer índice: ordinal de clase de la variable de estado continua
	 */
	private double[][][] limitesSupClases;	

	
	/**
	 * Datos de la variable aleatoria observados históricamente
	 * Primer índice: día de datos históricos, por ejemplo días desde 0 a 730 si hay 2 anos de días 
	 * de datos históricos.
	 * Segundo índice: hora dentro del paso.
	 * Tercer índice: variable aleatoria, por ejemplo factor eólico, factor solar, etc.
	 */
	private double[][][] datosHistoricos;
		
	private int cantHoras;  // cantidad de horas por día
	private int durHora;   // duración de la hora en segundos  (recordar que la hora puede tener otra duración que 3600 segundos)
 	private int cantMaxDias;  // cantidad m�xima de dias que tiene un año (recordar que puede ser distinto de 366 si se muestrea un período distinto)
	private int cantDiasDatos; // cantidad de días que hay en los datos
 	private String estimacionVE;   // identificación de la estimación de las VE que se empleó
	private boolean varEstadoEnOptim; // true si se emplea la variable de estado en la optimización	
    private int cantEstadosComp; // cantidad de estados compuestos	
	

    /*
     * Enumerador de los estados compuestos a partir de los estados
     * discretos de todas las VA
     */
    private EnumeradorLexicografico enumL;
    
	/**
	 * Probabilidades de los estados compuestos en cada "dia" del año
	 * primer índice dia del año
	 * segundo índice estado compuesto
	 */
	private double[][] probabilidadesEstadosCompuestos;
	
	/**
	 * Probabilidades acumuladas de los estados compuestos en cada "dia" del año
	 * probEstadosAcum[i] es la probabilidad de estadoAcum<= i
	 * primer índice dia del año
	 * segundo índice estado compuesto
	 */
	private double[][] probEstadosAcum;	
	
    

	
	/**
	 * Población de la que se extraen los sucesores en el bootstrap
	 * 
	 * Primer índice: dia del año del que se consideran los sucesores posibles
	 * (Ejemplo días de 0 a 365)
	 * Segundo índice: estado discreto compuesto
	 * 
	 * El object es un ArrayList<Integer>
	 * Tercer índice sobre el ArrayList: observaciones para el paso y estado discreto 
	 * 
	 * A PARTIR DE ESTE ATRIBUTO SE CREAN LAS REALIZACIONES DEL PE 
	 * SE TOMA EL Integer que apunta a datosHistoricos
	 *  
	 */
	private  Object[][] poblacionesSucesores;

	private int duracionDelDia; // duración en segundos del "día" que se muestrea entero
	
	private int diaDelAnioRealizacionAnterior; // "dia" del año del instante del último llamado a producirRealizacion
	// este valor debe inicializarse en la simulación, según el instante inicial de la corrida.
	
	private int indiceEnDHRealizacionAnterior; // indice en datos históricos del último día sorteado
	


	public ProcesoBootstrapDiscreto(DatosPEBootstrapDiscreto datos) {
//		kk ojo que si es para la simulaci�n debe estar cargado el instantecorriente inicial
//		kk y el estado inicial
		//
		super(datos.getDatGen());
		this.setNombre(datos.getNombre());
		this.setDiscretoExhaustivo(datos.isDiscretoExhaustivo()); 
		this.setEstimacionVE(datos.getEstimacionVE());
		this.setRuta(datos.getRuta());
//		this.setNombrePaso(datos.getNombrePasoPE());		
		this.setCantidadInnovaciones(1);
		this.setCantMaxDias(datos.getCantMaxDias());
//		this.setNombresVarsAleatorias(datos.getNombresVA());
//		this.setNombresVarsEstado(datos.getNombresVE());
//		this.setVarEstadoEnOptim(datos.isVarEstadoEnOptim());
		this.setCantDiasDatos(datos.getCantDiasDatos());
		this.setCantHoras(datos.getCantHoras());
		this.setDurHora(datos.getDurHora());
		this.setCantVA(datos.getCantVA());
		this.setCantVE(datos.getCantVE());

		this.setCantEstadosComp(datos.getCantEstadosComp());
		this.setCantCla(datos.getCantCla());
		this.setProbCla(datos.getProbCla());
		this.setLimitesSupClases(datos.getLimitesSupClases());
		this.setPoblacionesSucesores(datos.getPoblacionesSucesores());
		this.setProbabilidadesEstadosCompuestos(datos.getProbabilidadesEstadosCompuestos());
		this.setPonderadores(datos.getPonderadores());
		// crea la suma de ponderadores
		double[] auxSuma = new double[this.getCantVA()];
		for (int iv=0; iv<this.getCantVA(); iv++){
			double suma = 0;
			for(int ih = 0; ih<this.getCantHoras(); ih++){
				suma += this.getPonderadores()[iv][ih];
			}
			auxSuma[iv] = suma;
		}
		this.setSumapond(auxSuma);
		
		this.completaConstruccion();
		
		
		// 	Carga los datos del archivo de datos del datatype
		int idia = 0;
		int ihora = 0;		
        ArrayList<ArrayList<String>> texDatos;
        String dirArchivo = datos.getArchDatos(); 
        texDatos = LeerDatosArchivo.getDatos(dirArchivo);
        double[][][] datosHistoricos = new double[cantDiasDatos][cantHoras][this.getCantVA()];		
		for(int i=1; i<texDatos.size(); i++){
			for(int j=0; j<this.getCantVA(); j++){
				datosHistoricos[idia][ihora][j]=Double.parseDouble(texDatos.get(i).get(j+2));
			}
			ihora++;
			if(ihora==cantHoras){
				ihora = 0;
				idia++;
			}
			if(idia==cantDiasDatos) break;
		}	
		this.setDatosHistoricos(datosHistoricos);
		
		// Crea la discretización de las VE
		for(int ive=0; ive<this.getCantVE(); ive++){
			ArrayList<Double> datosDisc = new ArrayList<Double>();
			for(int ival=0; ival<datos.getCantCla()[ive]; ival++){
				datosDisc.add((double)ival);
			}
			VariableEstado ve = this.devuelveVEDeNombre(datos.getDatGen().getNombresVarsEstado().get(ive));
			Discretizacion disc = new Discretizacion(ve, datosDisc, true);
			Evolucion<Discretizacion> ed = new EvolucionConstante<Discretizacion>(disc, new SentidoTiempo(1));
			ve.setEvolDiscretizacion(ed);
			ve.setDiscreta(true);
			ve.setOrdinal(true);
		}

		// Crea el enumerador lexicografico de estados compuestos
		int[] cotasInferiores = new int[this.getCantVA()];
		int[] cotasSuperiores = new int[this.getCantVA()];
		for(int iv=0; iv<this.getCantVA(); iv++){
			cotasInferiores[iv]=0;
			cotasSuperiores[iv]=cantCla[iv]-1;
		}
		enumL = new EnumeradorLexicografico(this.getCantVA(), cotasInferiores, cotasSuperiores);	
		enumL.creaTablaYListaOrdinales();
		// crea las probabilidades absolutas de cada estado compuesto
		probEstadosAcum = new double[cantMaxDias][cantEstadosComp];
		enumL.inicializaEnum();
		
		for(int id=0; id<cantMaxDias; id++){
			double probAcum = 0.0;
			for(int ie=0; ie<cantEstadosComp; ie++){
				probEstadosAcum[id][ie] = probAcum + probabilidadesEstadosCompuestos[id][ie]; 
				probAcum += probabilidadesEstadosCompuestos[id][ie];
			}			
		}

		duracionDelDia = durHora*cantHoras;
			
	}
	

		
	
    public int getRangoPasos() {
		return rangoPasos;
	}

	public void setRangoPasos(int rangoPasos) {
		this.rangoPasos = rangoPasos;
	}


	public double[][] getPonderadores() {
		return ponderadores;
	}

	public void setPonderadores(double[][] ponderadores) {
		this.ponderadores = ponderadores;
	}

		
	public double[] getSumapond() {
		return sumapond;
	}


	public void setSumapond(double[] sumapond) {
		this.sumapond = sumapond;
	}


	public EnumeradorLexicografico getEnumL() {
		return enumL;
	}


	public void setEnumL(EnumeradorLexicografico enumL) {
		this.enumL = enumL;
	}



	public int getDuracionDelDia() {
		return duracionDelDia;
	}


	public void setDuracionDelDia(int duracionDelDia) {
		this.duracionDelDia = duracionDelDia;
	}


	public int[] getCantCla() {
		return cantCla;
	}


	public void setCantCla(int[] cantCla) {
		this.cantCla = cantCla;
	}
	
	


	public double[][] getProbCla() {
		return probCla;
	}




	public void setProbCla(double[][] probCla) {
		this.probCla = probCla;
	}







	public double[][] getProbabilidadesEstadosCompuestos() {
		return probabilidadesEstadosCompuestos;
	}




	public void setProbabilidadesEstadosCompuestos(double[][] probabilidadesEstadosCompuestos) {
		this.probabilidadesEstadosCompuestos = probabilidadesEstadosCompuestos;
	}




	public double[][][] getLimitesSupClases() {
		return limitesSupClases;
	}

	public void setLimitesSupClases(double[][][] limitesSupClases) {
		this.limitesSupClases = limitesSupClases;
	}

	public double[][][] getDatosHistoricos() {
		return datosHistoricos;
	}

	public void setDatosHistoricos(double[][][] datosHistoricos) {
		this.datosHistoricos = datosHistoricos;
	}

	public int getCantHoras() {
		return cantHoras;
	}

	public void setCantHoras(int cantHoras) {
		this.cantHoras = cantHoras;
	}

	public int getDurHora() {
		return durHora;
	}

	public void setDurHora(int durHora) {
		this.durHora = durHora;
	}

	public int getCantMaxDias() {
		return cantMaxDias;
	}

	public void setCantMaxDias(int cantMaxDias) {
		this.cantMaxDias = cantMaxDias;
	}

	public int getCantDiasDatos() {
		return cantDiasDatos;
	}

	public void setCantDiasDatos(int cantDiasDatos) {
		this.cantDiasDatos = cantDiasDatos;
	}


	public String getEstimacionVE() {
		return estimacionVE;
	}

	public void setEstimacionVE(String estimacionVE) {
		this.estimacionVE = estimacionVE;
	}



	public boolean isVarEstadoEnOptim() {
		return varEstadoEnOptim;
	}




	public void setVarEstadoEnOptim(boolean varEstadoEnOptim) {
		this.varEstadoEnOptim = varEstadoEnOptim;
	}




	public int getDiaDelAnioRealizacionAnterior() {
		return diaDelAnioRealizacionAnterior;
	}




	public void setDiaDelAnioRealizacionAnterior(int diaDelAnioRealizacionAnteriorSim) {
		this.diaDelAnioRealizacionAnterior = diaDelAnioRealizacionAnteriorSim;
	}




	public int getIndiceEnDHRealizacionAnterior() {
		return indiceEnDHRealizacionAnterior;
	}




	public void setIndiceEnDHRealizacionAnterior(int indiceEnDHRealizacionAnteriorSim) {
		this.indiceEnDHRealizacionAnterior = indiceEnDHRealizacionAnteriorSim;
	}




	public int getCantEstadosComp() {
		return cantEstadosComp;
	}

	public void setCantEstadosComp(int cantEstadosComp) {
		this.cantEstadosComp = cantEstadosComp;
	}

	public Object[][] getPoblacionesSucesores() {
		return poblacionesSucesores;
	}


	public void setPoblacionesSucesores(Object[][] poblacionesSucesores) {
		this.poblacionesSucesores = poblacionesSucesores;
	}
	


	
	/**
	 * Devuelve el estado compuesto de un d�a de datos hist�ricos
	 * dado el d�a del a�o del que se est� considerando la poblaci�n
	 * @param diaEnDH ordinal del d�a en datosHistoricos
	 * @param diaDelAnio dia del a�o cuya poblaci�n se est� considerando
	 * 
	 */
	public int devuelveEstadoCompuesto(int diaEnDH, int diaDelAnio){
		double estadoContinuo;
		int[] clasesDiscretas = new int[this.getCantVA()];
		for(int iva=0; iva<this.getCantVA(); iva++){
			estadoContinuo=0;
			for(int ih=0; ih<cantHoras; ih++){
				estadoContinuo += datosHistoricos[diaEnDH][ih][iva]*
						ponderadores[iva][ih];
			}
			estadoContinuo = estadoContinuo/sumapond[iva];
			clasesDiscretas[iva]=0;
			int ic=0;
			while(estadoContinuo>limitesSupClases[diaDelAnio][iva][ic]){
				ic++;
			}
			clasesDiscretas[iva]=ic;
		}
		return enumL.devuelveOrdinalDeVector(clasesDiscretas);
	}
	
	/**
	 * Elige un índice en datosHistoricos sucesor, dado un dia del año de origen y un estado compuesto de origen.
	 * Para eso sortea entre los sucesores dado el día del ano diaDelAnio y el estado compuesto
	 * estadoComp.
	 * @param diaDelAnio del que se consideran sus sucesores posibles
	 * @param estadoComp
	 * @param aleatUnif variable aleatoria uniforme [0,1]
	 * 
	 * @return ihsuc indice en datos históricos del dia sucesor
	 */
	public int eligeDiaSucesorEnDH(int diaDelAnio, int estadoComp, double aleatUnif){
	
		if(diaDelAnio==366){
			System.out.println("Dia del año 366 (deben ir de 0 a 365) en procesoBootstrap " + this.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		ArrayList<Integer> sucesoresPosibles = (ArrayList<Integer>) poblacionesSucesores[diaDelAnio][estadoComp];
		int cantPoblacion = sucesoresPosibles.size();
		int ihsuc = eligeUnOrdinalDeUnaPoblacion(cantPoblacion, aleatUnif);
		return sucesoresPosibles.get(ihsuc);		
	}
	

//	@Override
//	public Hashtable<String, VariableEstado> dameEstadoAgregado(ArrayList<String> nomVarEstado, String modoReduccion) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void inicializar(Semilla semGeneral, GregorianCalendar inicioSorteos, GregorianCalendar inicioCorrida, int escenario){
		super.inicializar(semGeneral, inicioSorteos, inicioCorrida, escenario);
		// se asegura un cambio de d�a al inicio del escenario en la simulaci�n
		if(!optim){
			LineaTiempo lt = this.getSimuladorPaso().getCorrida().getLineaTiempo();
			// La corrida se inicia siempre en el instante cero
			diaDelAnioRealizacionAnterior = diaDelAnio(0)-1;
			if(diaDelAnioRealizacionAnterior<0) diaDelAnioRealizacionAnterior = cantMaxDias-1;
		}
	}
	

	@Override
	/**
	 * Recordar que:
	 * Llamamos "dia" al conjunto de "horas" que se muestrean juntas 
	 * El paso del PEBootstrap es la "hora".
	 * 
	 * No puede llamarse con más de un año de distancia entre instantes
	 * de realización consecutivos
	 * La variable de estado del proceso se cambia al avanzar los días
	 */
	public void producirRealizacionSinPronostico(long instante) {	
		int nuevoDiaDelAnio=0;    // los dias del año se numeran a partir del 0 
		int nuevaHoraDelDia=0;    // las horas del día se numeran a partir de 0
		double aleatUnif = 0;
		if (instante > this.getInstanteCorrienteFinal()) {
			// el instante es posterior al fin de la "hora" corriente
			int nuevaHoraDelAnio = pasoDelAnio(instante); // la "hora" empieza en cero
			nuevaHoraDelDia = nuevaHoraDelAnio%cantHoras;
			nuevoDiaDelAnio = nuevaHoraDelAnio/cantHoras; 	
			int estado = this.getVarsEstado().get(0).getEstado().intValue(); // el estado anterior a la invocación
			if(nuevoDiaDelAnio==diaDelAnioRealizacionAnterior){
				// no hay que sortear un nuevo dia
			}else{				
				// hay que avanzar un nuevo día del año al menos
				int saltos = nuevoDiaDelAnio - diaDelAnioRealizacionAnterior;
				int cantDiasAnio = cantDiasAnio(this.getInstanteCorrienteFinal());
				if(saltos<0){
					// ejemplo: pasó del dia 364 o 365 de un año al 0 del siguiente					
					saltos = saltos+cantDiasAnio+1;
				}
				int diaDelAnioSalto = diaDelAnioRealizacionAnterior + 1; // dia del año al que se salta
				for(int is=1; is<= saltos; is++){					
					aleatUnif = this.getGeneradoresAleatorios().get(0).generarValor();
				    if(diaDelAnioSalto>=cantDiasAnio) diaDelAnioSalto = 0;
					int nuevoDiaEnDH = eligeDiaSucesorEnDH(diaDelAnioRealizacionAnterior, estado, aleatUnif);
					double aux = (double)devuelveEstadoCompuesto(nuevoDiaEnDH, diaDelAnioSalto);
					// la variable de estado del proceso se va alterando
					this.getVarsEstado().get(0).setEstado(aux);
					indiceEnDHRealizacionAnterior = nuevoDiaEnDH;
					diaDelAnioRealizacionAnterior = diaDelAnioSalto;
					diaDelAnioSalto++;
				}
				diaDelAnioRealizacionAnterior = nuevoDiaDelAnio; // si el año tiene 366 días corrige?
			}
			for(int iva=0; iva<this.getCantVA(); iva++){
				VariableAleatoria va = this.getVariablesAleatorias().get(iva);
				va.setValor(datosHistoricos[indiceEnDHRealizacionAnterior][nuevaHoraDelDia][iva]);
			}
			long instInicNuevaHora = this.instanteInicialAnioDeInstante(instante) + nuevaHoraDelAnio*durHora;
			this.setInstanteCorrienteInicial(instInicNuevaHora);
			this.setInstanteCorrienteFinal(instInicNuevaHora+durHora);
		
		}else{
			// el instante est� en la misma "hora" corriente
			// no se hace nada y quedan fijos los valores de las VA
		}

		
	}
		
	@Override
	public void alimentarPronosticoEnAutoregresivos(long instante) {
		// Deliberadamente en blanco
	}
	
	
	/**
	 * Muestrea y carga los valores de las variables aleatorias cuando el proceso
	 * tiene estado en la optimizaci�n, para ser empleados en 
	 * UNO DE LOS SORTEOS Montecarlo en la optimizaci�n, para cada instante de muestreo, o para 
	 * el �nico valor si la VA no es muestreada.
	 * 
	 * Las innovaciones ya fueron sorteadas en OptimizadorPaso
	 * 
	 * Este m�todo carga 
	 * - valor para las VA que no son muestreadas
	 * - ultimoMuestreo[] para las VA que son muestreadas
	 * 
	 * @instantesMuestreo vector con los instantes de muestreo
	 * @innovaciones1Sort innovaciones a emplear en el sorteo
	 * 	primer �ndice recorre �ndices de innovaci�n,
	 * 	segundo �ndice recorre intervalos de muestreo
	 * @isort �ndice del sorteo Montecarlo que se va a construir 
	 * 
	 * ATENCI�N: NO DEBE MODIFICARSE LAS VARIABLES DE ESTADO DEL PROCESO EN EL M�TODO
	 * DEBEN USARSE VARIABLES AUXILIARES SI EL ESTADO DEL PROCESO VA CAMBIANDO.
	 * 
	 */
	@Override
	
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		/**
		 * TODO: OJO EL ESTADO DEBE SER EL DEL PASO CORRIENTE Y NO DEL PASO ANTERIOR
		 * el diaDelAnioRealizacionAnterior se setea al inicio de los sorteos
		 * en el �ltimo d�a del a�o que termina antes o en el mismo instante del inicio
		 * del paso de la optimizaci�n
		 * 
		 */
		long instIniPasoOp = this.getOptimizadorPaso().getInstIniPaso();
		diaDelAnioRealizacionAnterior = this.pasoDelAnio(instIniPasoOp)-1; // Los pasos empiezan en cero;
		if(diaDelAnioRealizacionAnterior<0)diaDelAnioRealizacionAnterior=cantMaxDias;
		// recorre los instantes de muestreo
		for(int im=0; im<instantesMuestreo.length;im++){
			long instante = instantesMuestreo[im];
			int indInnov = 0; // indice que recorre las innovaciones que se van haciendo necesarias 

			/**
			 * El código siguiente copia el método producirRealizacion(int instante)
			 * solo que las innovaciones se van obteniendo de innovaciones1Sort[][]
			 * en la medida en que sean necesarias. Atención que la relación entre innovaciones necesarias e
			 * instantes de muestreo no es una a una. Puede obtenerse muchas realizaciones con una sola 
			 * innovación, si los instantes de muestreo están todos en el mismo día. Pueden requerirse muchas innovaciones
			 * para producir una realización en un instante único de muestreo, si hay que saltar varios días. 
			 */
			int nuevaHoraDelDia;
			// Procesa un instante de muestreo
			if (instante > this.getInstanteCorrienteFinal()) {
				// el instante es posterior al fin de la "hora" corriente
				int nuevaHoraDelAnio = pasoDelAnio(instante); // la "hora" empieza en cero
				nuevaHoraDelDia = nuevaHoraDelAnio%cantHoras;
				int nuevoDiaDelAnio = nuevaHoraDelAnio/cantHoras; 
				if(nuevoDiaDelAnio==diaDelAnioRealizacionAnterior){
					// no hay que sortear un nuevo dia
				}else{				
					// hay que avanzar un nuevo día del año al menos
					int saltos = nuevoDiaDelAnio - diaDelAnioRealizacionAnterior;
					int cantDiasAnio = cantDiasAnio(this.getInstanteCorrienteFinal());
					if(saltos<0) {
						// ejemplo: pasó del dia 364 o 365 de un año al 0 del siguiente					
						saltos = saltos+cantDiasAnio+1;
					}					
					int diaDelAnioSalto = diaDelAnioRealizacionAnterior + 1; // dia del a�o al que se salta
					int estado = this.getVarsEstado().get(0).getValorInicial().intValue();
					for(int is=1; is<= saltos; is++){
						// ac� en vez de sortear con el generador aleatorio emplea una innovacion preexistente
						// y pasa a apuntar a la siguiente
						double aleatUnif = innovaciones1Sort[0][indInnov];
						indInnov++;
						if(diaDelAnioSalto>cantDiasAnio) diaDelAnioSalto = 0;
						int nuevoDiaEnDH = eligeDiaSucesorEnDH(diaDelAnioRealizacionAnterior, estado, aleatUnif);
						Double aux = (double)devuelveEstadoCompuesto(nuevoDiaEnDH, diaDelAnioSalto);
						/**
						 * Como este m�todo se invoca en los sorteos Montecarlo de la optimizaci�n
						 * la variable de estado no se toca y lo que evoluciona es una variable estado auxiliar
						 */
						estado = aux.intValue();
						indiceEnDHRealizacionAnterior = nuevoDiaEnDH;
						diaDelAnioRealizacionAnterior = diaDelAnioSalto;
						diaDelAnioSalto++;
					}
					diaDelAnioRealizacionAnterior = nuevoDiaDelAnio; // si el a�o tiene 366 d�as corrige
				}
				// ya nuevoDiaEnDH y diaDelAnioRealizacionAnterior tienen los valores correctos
				for(int iva=0; iva<this.getCantVA(); iva++){
					VariableAleatoria va = this.getVariablesAleatorias().get(iva);
					double val = datosHistoricos[indiceEnDHRealizacionAnterior][nuevaHoraDelDia][iva];			
					if(!this.isMuestreado()){
						va.setValor(val);
					}else{
						// el proceso es muestreado
						va.getUltimoMuestreoOptim()[isort][im] = val; 
					}
				}
				long instInicNuevaHora = this.instanteInicialAnioDeInstante(instante) + nuevaHoraDelAnio*durHora;
				this.setInstanteCorrienteInicial(instInicNuevaHora);
				this.setInstanteCorrienteFinal(instInicNuevaHora+durHora);
			
			}else{
				/**
				 * Por la inicialización del instante final del paso de tiempo
				 * con que se arrancan los sorteos
				 * del PE no debería salir nunca por acá 
				 * 
				 */
				System.out.println("en el proceso " + this.getNombre() +
						 "no cambia la hora al iniciar un sorteo de optimizaci�n");
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
				System.exit(1);
			}
		}
		
			
	}
		
//		/**
//		 * Este m�todo auxiliar produce una realizaci�n del PE tomando las innovaciones de mediante
//		 * un m�todo devuelveInnovacion que:
//		 * - para el caso de que el proceso no tiene estado, genera las innovaciones con el generador aleatorio
//		 *   del proceso.
//		 * - para el caso en que el proceso tiene estado, devuelve las innovaciones del vector
//		 *   double[][][] innovacionesOptim de la clase padre ProcesoEstocastico
//		 */
//		public void producir1RealizacionAux(){
//			kk
//		}
//			
//		}
//		
//		
//		/**
//		 * Este m�todo devuelve innovaciones para generar realizaciones.
//		 * - si el proceso tiene estado saca las innovaciones en forma sucesiva de
//		 *   double[][][] innovacionesOptim de la clase padre ProcesoEstocastico
//		 * - si el proceso no tiene estado genera las innovaciones con el generador aleatorio del PE
//		 * 
//		 * @param indiceInnov si es mayor o igual a cero apunta a una instante de muestreo en innovacionesOptim
//		 * si es negativo hace que el m�todo genere un aleatorio con el generador del proceso y lo devuelva
//		 *  
//		 */
//		public double devuelveInnovacion(int indiceInstMuestreo){
//			if(indiceInnov)
//		}
//		
//		
//		
//	}
	
	
	/**
	 * Devuelve el "día" del año (ej. 0 a 365), dado un instante
	 */
	public int diaDelAnio(long instante){
		long instIniAnio = this.instanteInicialAnioDeInstante(instante);
		int dia = (int)((instante - instIniAnio)/(cantHoras*this.getDurPaso()));
		return dia;		
	}
	
	/**
	 * Devuelve la cantidad de "días" del año al que pertenece el instante
	 * TODO: OJOJOJOJOJOJOJO El año debería ser cerrado por izquierda |--------)
	 */
	public int cantDiasAnio(long instante){
		int anio = this.anioDeInstante(instante);
		LineaTiempo lt = this.getSimuladorPaso().getCorrida().getLineaTiempo();
		int duracion = lt.getDurAnio().get(anio);
		int cantDias = duracion/(cantHoras*this.getDurPaso());
		return cantDias;
		
	}

	
	
	/**
	 * Se emplea cuando se invoca producirRealizacion en la optimizaci�n, porque
	 * el proceso no tiene VE en la optimizaci�n
	 * @param cantSortMont
	 */
	public void prepararPasoOptim(int cantSortMont){
	// TODO   	
	}

	@Override
	public boolean tieneVEOptim() {
		return varEstadoEnOptim;
	}
	
	
	/*		
	 * Se usa en la optimizaci�n si este PE no tiene VE en la optimizaci�n
	 * 
	 * Carga el diaDelAnioRealizacionAnterior con el "dia" del a�o cuyo instante final coincide con
	 * o en su defecto es el primero posterior al instante inicial del paso de la optimizaci�n
	 * corriente.
	 * 
	 * Elige al azar un estado inicial con su probabilidad absoluta
	 * 
	 * Carga indiceEnDHRealizacionAnterior con un indiceEnDH al azar dado 
	 * el diaDelAnioRealizacionAnterior y el estado elegido
	 *
	 */		
	@Override	 
	public void preparaUnSorteoMontecarloPEsinVEOptim(int sort) {
		
		long instante = this.devuelveInicioPasoCorrienteDeOptimizacion();      
		int hora = pasoDelAnio(instante);  
		int dia = hora/cantHoras;   // dia del a�o asociado al instante de inicio del paso de optimizaci�n				
//		int instIniPasoPE = this.instanteInicialAnioDeInstante(instante)+ dia*durHora;
//		this.setInstanteCorrienteInicial(instIniPasoPE);
//  	int instFinPasoPE = instIniPasoPE+durHora;
		this.setInstanteCorrienteFinal(instante); // se asegura que haya que cambiar la hora al producirRealizacion
		// determina el diaDelAnioRealizacionAnterior					
		dia--; // Se toma el d�a anterior al asociado al instante inicial del paso de optimizaci�n
		if(dia<0) dia = cantMaxDias-1;  // los d�as del a�o empiezan en cero se resta 1
		diaDelAnioRealizacionAnterior = dia;
		// elige al azar un estado con las probabilidades absolutas de los estados compuestos		
		double aleat = this.getGeneradoresAleatorios().get(0).generarValor();
		int iec=0;
		while(probEstadosAcum[dia][iec]<aleat){
			iec++;
		}		
		this.getVarsEstado().get(0).setEstado((double)iec);
		// no es necesario precisar un valor de indiceEnDHRealizacionAnterior
		// porque al invocar el proceso habr� un salto de d�a, se carga -1
		indiceEnDHRealizacionAnterior=-1;
	}



//	@Override
//	public void actualizarVarsEstadoSimulacion() {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//
//
//	@Override
//	public void actualizarVarsEstadoOptimizacion() {
//		// TODO Auto-generated method stub
//		
//	}




	@Override
	public void contribuirAS0fint() {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void contribuirAS0fintOptim() {
		// TODO Auto-generated method stub
		
	}




//	@Override
//	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//
//
//
//	@Override
//	public ArrayList<VariableEstado> aportarEstadoOptimizacion() {
//		// TODO Auto-generated method stub
//		return null;
//	}




	@Override
	public void cargarValVEOptimizacion() {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos aproxFuturaOpt) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void cargarValRecursoVESimulacion() {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * El programa construye el proceso usando el cargador 
	 * y lo hace generar series de un a�o de duraci�n
	 * que luego guarda en un texto.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
//		String dirTextoParaCargador = "D:/Proyectos/modelopadmin/resources/eoloSolBootstrap/Salidas";
//		
//		Hashtable<String, Double> estadosIniciales = new Hashtable<String, Double>();
//		estadosIniciales.put("eolosol", 3.);
//		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico("EoloSolBootstrap","NADA", "NADA", 
//				dirTextoParaCargador, false, false, estadosIniciales);
//		
//		
//		DatosPEBootstrapDiscreto dpb = CargadorPEBootstrapDiscreto.devuelveDatosPEBootstrap(dpe);
//		ProcesoBootstrapDiscreto procBD = new ProcesoBootstrapDiscreto(dpb);
//		System.out.println("Construy� el Proceso");
//		// Genera cantEscenarios escenarios de un a�o de 8760 horas de datos
//		long semilla = 1283479483279723124L;
//		int largoDias = 365;  // largo en d�as de la simulaci�n
//		int largoHoras = largoDias*24;
//		GeneradorDistUniformeLCXOr genUnif = new GeneradorDistUniformeLCXOr(semilla);
//		int cantEscenarios = 105;
//		double[][][] seriesSimuladas = new double [cantEscenarios][largoHoras][2];
//		double[][] promedioSeries = new double[largoHoras][2];
//		int[][] estadoSim = new int[cantEscenarios][largoHoras];
//		int[][] indEnDHSim = new int[cantEscenarios][largoHoras];
//		int indiceEnDH = 0;
//		double valor;
//		double[] factoresAnuales = new double[2];
//		int cantMaxDias = 366;
//		// Graba archivo con estados e indiceEnDH simulados
//		String archEstadoEIndices = "D:/Proyectos/modelopadmin/resources/eoloSolBootstrap/Salidas/estind.xlt";
//		if(DirectoriosYArchivos.existeArchivo(archEstadoEIndices)) DirectoriosYArchivos.eliminaArchivo(archEstadoEIndices);
//		StringBuilder sb = new StringBuilder("escenario dia_del_anio hora factor");
//		DirectoriosYArchivos.agregaTexto(archEstadoEIndices, sb.toString());			
//		for(int iesc=0; iesc<cantEscenarios; iesc++){
//			System.out.println("Empieza escenario " + iesc);
//			int estado = 4; // estado antes de la primera hora de simulaci�n
//			for(int dia = 0; dia<=364; dia++){
//				int diaDelAnio = dia-1;
//				if(diaDelAnio<0) diaDelAnio = cantMaxDias-1;
//				double aleatUnif = genUnif.generarValor();
//				indiceEnDH=procBD.eligeDiaSucesorEnDH(diaDelAnio, estado, aleatUnif);
//				for(int ih=0; ih<24; ih++){	
//					sb = new StringBuilder();
//					for(int iv=0; iv<2; iv++){
//						indEnDHSim[iesc][dia*24+ih]=indiceEnDH;
//						estadoSim[iesc][dia*24+ih] = estado;
//						valor = procBD.getDatosHistoricos()[indiceEnDH][ih][iv];
//						seriesSimuladas[iesc][dia*24+ih][iv] = valor;
//						promedioSeries[dia*24+ih][iv]+=valor/cantEscenarios;
//						factoresAnuales[iv] += valor/(cantEscenarios*largoHoras);
//					}
//					sb.append(iesc + " " + dia + " " + ih + " " + seriesSimuladas[iesc][dia*24+ih][1]);
//					DirectoriosYArchivos.agregaTexto(archEstadoEIndices, sb.toString());			
//				}							
//				estado = procBD.devuelveEstadoCompuesto(indiceEnDH, dia);
//			}
//		}
//		System.out.println("Termin� la simulaci�n de valores");
//		// Graba archivo con promedios
//		String archPromedios = "D:/Proyectos/modelopadmin/resources/eoloSolBootstrap/Salidas/promedios.xlt";
//		if(DirectoriosYArchivos.existeArchivo(archPromedios)) DirectoriosYArchivos.eliminaArchivo(archPromedios);
//
//		sb = new StringBuilder("Dia_del_a�o hora solar eolo");
//		DirectoriosYArchivos.agregaTexto(archPromedios, sb.toString());
//		for(int diaDelAnio = 0; diaDelAnio<365; diaDelAnio++){
//			for(int ih=0; ih<24; ih++){
//				sb = new StringBuilder();
//				sb.append(diaDelAnio);
//				sb.append(" ");
//				sb.append(ih);
//				sb.append(" ");
//				for(int iv=0; iv<2; iv++){
//					sb.append(promedioSeries[diaDelAnio*24+ih][iv]);
//					sb.append(" ");
//				}
//				DirectoriosYArchivos.agregaTexto(archPromedios, sb.toString());
//			}
//		}
//		// carga factores anuales
//		sb = new StringBuilder();
//		for(int iv=0; iv<2; iv++){
//			sb.append(factoresAnuales[iv]);
//			sb.append(" ");
//		}		
//		DirectoriosYArchivos.agregaTexto(archPromedios, sb.toString());
//		
//			
//
////		for(int iesc=0; iesc<5; iesc++){
////			System.out.println("escenario " + iesc );
////			for(int diaDelAnio = 0; diaDelAnio<365; diaDelAnio++){
////				for(int ih=0; ih<24; ih++){
////					sb = new StringBuilder();
////					sb.append(iesc);
////					sb.append(" ");
////					sb.append(diaDelAnio);
////					sb.append(" ");
////					sb.append(ih);
////					sb.append(" ");					
////					sb.append(estadoSim[iesc][diaDelAnio*24+ih]);
////					sb.append(" ");
////					sb.append(indEnDHSim[iesc][diaDelAnio*24+ih]);
////					DirectoriosYArchivos.agregaTexto(archEstadoEIndices, sb.toString());	
////				}
////			}
////		}
//		System.out.println("Termin� la grabaci�n de estados e indiceEnDH");
//		
////		// Graba factores horarios para algunos escenarios
////		String archFactoresHorarios = "D:/Proyectos/modelopadmin/resources/eoloSolBootstrap/Salidas/factoresHorarios.xlt";
////		if(DirectoriosYArchivos.existeArchivo(archFactoresHorarios)) DirectoriosYArchivos.eliminaArchivo(archFactoresHorarios);
////		sb = new StringBuilder("escenario dia_del_anio hora factor_solar factor_e�lico");
////		DirectoriosYArchivos.agregaTexto(archFactoresHorarios, sb.toString());	
////		for(int iesc=0; iesc<10; iesc++){
////			System.out.println("escenario " + iesc );
////			for(int diaDelAnio = 0; diaDelAnio<365; diaDelAnio++){
////				for(int ih=0; ih<24; ih++){
////					sb = new StringBuilder();
////					sb.append(iesc);
////					sb.append(" ");
////					sb.append(diaDelAnio);
////					sb.append(" ");
////					sb.append(ih);
////					sb.append(" ");					
////					sb.append(seriesSimuladas[iesc][diaDelAnio*24+ih][0]);
////					sb.append(" ");
////					sb.append(seriesSimuladas[iesc][diaDelAnio*24+ih][1]);
////					DirectoriosYArchivos.agregaTexto(archFactoresHorarios, sb.toString());	
////				}
////			}
////		}		
//		
//		System.out.println("Termin� el main");		
	}




	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// Este m�todo es invocado nunca porque el PE BootstrapDiscreto no tiene VE continuas
		return 0;
	}




	@Override
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla) {
		// Deliberadamente en blanco
		
	}




	@Override
	public void actualizaValoresVEDiscretizacionesVariables(long instante) {
		// TODO Auto-generated method stub
		
	}



}