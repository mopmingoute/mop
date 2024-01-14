/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoHistorico is part of MOP.
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

import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;

import java.util.ArrayList;
import java.util.Hashtable;

import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;
import persistencia.CargadorPEHistorico;
import pizarron.PizarronRedis;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.LineaTiempo;
import tiempo.SentidoTiempo;
import datatypesProcEstocasticos.DatosPEHistorico;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import estado.Discretizacion;
import estado.VariableEstado;
import futuro.AFIncrementos;



/**
 * Almacena datos por cr�nicas de duraci�n anual y los devuelve como realizaciones
 * Las cr�nicas son enteros que representan a�os pasados sucesivos.
 * Si hay m�s de un proceso hist�rico en la corrida, el usuario es responsable de su coherencia. 
 * 
 * CADA VARIABLE DE ESTADO PUEDE TOMAR UN CANTIDAD DE ESTADOS QUE NO CAMBIA A LO LARGO DEL TIEMPO
 * 
 * EL PROCESO SOLO SE EMPLEA EN SIMULACI�N Y PARA LA OPTIMIZACI�N TIENE ASOCIADO
 * UN PROCESO ESTOC�STICO.
 * 
 * Se supone que la discretizaci�n de las variables de estado no cambia en el tiempo.
 * 
 * TODO: HAY QUE VERIFICAR QUE LA CANTIDAD DE ESCENARIOS SEA M�LTIPLO DE LA CANTIDAD DE CR�NICAS
 * DEL PROCESO HIST�RICO ELEGIDO
 * @author ut469262
 *
 */

public class ProcesoHistorico extends ProcesoEstocastico implements AportanteEstado{
	
	
	
	
	
	// ATRIBUTOS PARA LA SIMULACI�N
	
	/**
	 * datos de las VA: primer índice "fila" es la crónica
	 * segundo  índice "columna" recorre el ordinal del paso en la crónica, por ejemplo las semanas
	 * tercer  índice recorre las VA 
	 */
	private double[][][]  datos;
	
	/**
	 * valores de las variables de estado, asociados a cada dato.
	 * tercer índice recorre las VE
	 */
	private double[][][]  valoresVE;
	
	private String estimacionVE;   // identificaci�n de la estimaci�n de las VE que se emple�	

	private int[] cronicas;     // da una etiqueta a cada cr�nica; por ejemplo "1909"

	private int cantCron;      // cantidad de cr�nicas
	
	private int cronIni;   // la primera cr�nica del proceso, ejemplo 1909
	private int cronFin;   // la �ltima cr�nica del proceso, ejemplo 2016
	
	/**
	 * Cantidad de pasos que entran en cada cr�nica disponible; por los bisiestos puede variar
	 * Si se est� simulando un a�o con m�s pasos que la cr�nica respectiva, se repiten los datos
	 * de los pasos finales de la cr�nica comenzando por el paso:
	 * cantPasosCron[icron] - saltoAtrasAlRepetir
	 * Ejemplo: El paso es horario y una cr�nica tiene 8760 horas; si se est� simulando un a�o bisiesto
	 * se repite el valor de la hora 8760.
	 */	
	private int[] cantPasosCron; 
	private int cantPasosMax;   // m�xima cantidad de pasos que puede tener una cr�nica
	private int saltoAtrasAlRepetir;

	private int iFila = 0;
	private int iCol = 0;
	private int iVA = 0;
	private int iVE = 0;	
	

	public ProcesoHistorico(DatosPEHistorico dph) {
		super(dph.getDatGen());
		this.setNombre(dph.getNombre());
		this.setUsoOptimizacion(false);
		this.setUsoSimulacion(true);
		this.setDiscretoExhaustivo(dph.isDiscretoExhaustivo());
		this.setRuta(dph.getRuta());
		this.setNombrePaso(dph.getNombrePaso());
		datos = dph.getDatos();
		valoresVE = dph.getValoresVE();
		estimacionVE = dph.getEstimacionVEUsada();
		this.setNombresVarsAleatorias(dph.getNombresVA());
		this.setNombresVarsEstado(dph.getNombresVE());		
		this.setCantVA(dph.getCantVA());
		this.setCantVE(dph.getCantVE());
		cantCron = dph.getCantCron();
		cronIni = dph.getCronIni();
		cronFin = dph.getCronFin();		
		cronicas = dph.getCronicas();
		cantPasosCron = dph.getCantPasosCron();
		saltoAtrasAlRepetir = dph.getSaltoAtrasAlRepetir();
		cantPasosMax = dph.getCantPasosMax();
		// completaConstruccion es un método general de la clase ProcesoEstocastico
		completaConstruccion();

//		// Crea la discretización de las VE
//		for(int ive=0; ive<this.getCantVE(); ive++){
//			ArrayList<Double> datos = new ArrayList<Double>();
//			for(int ival=0; ival<dph.getCantValoresVE()[ive]; ival++){
//				datos.add((double)ival);
//			}
//			VariableEstado ve = this.devuelveVEDeNombre(dph.getNombresVE().get(ive));
//			Discretizacion disc = new Discretizacion(ve, datos, true);
//			Evolucion<Discretizacion> ed = new EvolucionConstante<Discretizacion>(disc, new SentidoTiempo(1));
//			ve.setEvolDiscretizacion(ed);
//			ve.setDiscreta(true);
//			ve.setOrdinal(true);
//			// En el xml se carga la definici�n acerca de si las variables del proceso son discretas exhaustivas
//		}
	}	
	

//	@Override
//	public Hashtable<String,ArrayList<Double>> dameRealizacionesIntervalo(
//			int instanteInicial, int instanteFinal) {
//		// TODO Auto-generated method stub
//		return null;
//	}




	

//	/**
//	 * Devuelve los valores de las variables de estado solicitadas, seg�n un proceso de agregaci�n establecido en el par�metro modoReduccion
//	 * Est� pensado suponiendo que el proceso this se usa en la simulaci�n y se requiere obtener los valores de las variables de estado de otro proceso
//	 * usado en la optimizaci�n.
//	 */
//	@Override
//	public Hashtable<String, VariableEstado> dameEstadoAgregado(
//			ArrayList<String> nomVarEstado, String modoReduccion) {
//		Hashtable<String, VariableEstado> ea = new Hashtable<String, VariableEstado>();
//		for (VariableEstado ve: this.getVarsEstado()) {
//			ea.put(ve.getNombre(), ve);			
//		}		
//		return ea;
//	}


//	@Override
//	public Hashtable<String, ArrayList<Double>> dameNRealizacionesInstanteEstado(
//			int instante, int n, ArrayList<VariableEstado> estado) {
//		// TODO Auto-generated method stub
//		return null;
//	}


//	/**
//	 * Se sobreescribe el m�todo general porque en los ProcesoHistoricos las cr�nicas
//	 * no tienen todas la misma cantidad de pasos
//	 * Devuelve el ordinal del paso empezando en 0
//	 */
	@Override
	public int pasoDelAnio(long instante){
		Hashtable<Integer, Long> instInicioAnio = this.getSimuladorPaso().getCorrida().getLineaTiempo().getInstInicioAnioHT();
		int paso = (int)((instante-instInicioAnio.get(this.anioDeInstante(instante))) / this.getDurPaso());
		if(paso >= cantPasosCron[iFila]) paso = paso - saltoAtrasAlRepetir;		
		return paso;
	}	
	
	
	

	@Override
	public void producirRealizacionSinPronostico(long instante) {
		LineaTiempo lt = this.getSimuladorPaso().getCorrida().getLineaTiempo();
		iFila = (this.anioDeInstante(instante)-lt.getAnioInic()+this.getEscenario()-1) % cantCron;
		
		int paso = pasoDelAnio(instante);
		iCol = paso;

		// carga los valores de las variables aleatorias
		for(VariableAleatoria va: this.getVariablesAleatorias()){
			iVA = this.getIndiceVA().get(va.getNombre());
			va.setValor(datos[iFila][iCol][iVA]);				
		}
		
		// carga los valores de las variables de estado
		for(VariableEstado ve: this.getVarsEstado()){
			iVE = this.getIndiceVE().get(ve.getNombre());
			ve.setEstado(valoresVE[iFila][iCol][iVE]);				
		}			
		
	}

	
//	/**
//	 * Devuelve la cr�nica (ejemplo 1920) de la que el proceso extrae el dado, dado el
//	 * escenario corriente y el instante instante
//	 * @param instante
//	 * @return
//	 */
//	public int cronicaRequerida(int instante){
//		
//		
//		return cron;
//		
//	}



	public double[][][] getDatos() {
		return datos;
	}



	public void setDatos(double[][][] datos) {
		this.datos = datos;
	}



	public double[][][] getValoresVE() {
		return valoresVE;
	}



	public void setValoresVE(double[][][] valoresVE) {
		this.valoresVE = valoresVE;
	}



	public String getEstimacionVE() {
		return estimacionVE;
	}



	public void setEstimacionVE(String estimacionVE) {
		this.estimacionVE = estimacionVE;
	}




	public int getCantCron() {
		return cantCron;
	}



	public void setCantCron(int cantCron) {
		this.cantCron = cantCron;
	}





	public int[] getCronicas() {
		return cronicas;
	}


	public void setCronicas(int[] cronicas) {
		this.cronicas = cronicas;
	}


	public int[] getCantPasosCron() {
		return cantPasosCron;
	}



	public void setCantPasosCron(int[] cantPasosCron) {
		this.cantPasosCron = cantPasosCron;
	}



	public int getCantPasosMax() {
		return cantPasosMax;
	}



	public void setCantPasosMax(int cantPasosMax) {
		this.cantPasosMax = cantPasosMax;
	}



	public int getSaltoAtrasAlRepetir() {
		return saltoAtrasAlRepetir;
	}



	public void setSaltoAtrasAlRepetir(int saltoAtrasAlRepetir) {
		this.saltoAtrasAlRepetir = saltoAtrasAlRepetir;
	}



	public int getiFila() {
		return iFila;
	}



	public void setiFila(int iFila) {
		this.iFila = iFila;
	}



	public int getiCol() {
		return iCol;
	}



	public void setiCol(int iCol) {
		this.iCol = iCol;
	}



	public int getiVA() {
		return iVA;
	}



	public void setiVA(int iVA) {
		this.iVA = iVA;
	}



	public int getiVE() {
		return iVE;
	}



	public void setiVE(int iVE) {
		this.iVE = iVE;
	}
	
	
	public int getCronIni() {
		return cronIni;
	}


	public void setCronIni(int cronIni) {
		this.cronIni = cronIni;
	}


	public int getCronFin() {
		return cronFin;
	}


	public void setCronFin(int cronFin) {
		this.cronFin = cronFin;
	}




//    @Override
//    public ArrayList<VariableEstado> aportarEstadoOptimizacion() {            
//        /**
//         * Un proceso hist�rico no aporta variables de optimizaci�n, sino 
//         * que se tomar�n del proceso asociado en optimizaci�n
//         */
//    	return new ArrayList<VariableEstado>();
//    }
//
//
//
//	@Override
//	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
//		return getVarsEstado();		
//	}


//	@Override
//	public void actualizarVarsEstadoSimulacion() {
//		//Este m�todo queda vac�o
//	}
//
//
//	@Override
//	public void actualizarVarsEstadoOptimizacion() {		
//		//Este m�todo queda vac�o
//	}


	@Override
	public void contribuirAS0fint() {
		for (VariableEstado ve: this.getVarsEstado()) {
			ve.setEstadoS0fint(ve.getEstado());
		}
	}


	/**
	 * El proceso hist�rico no tiene VE de optimizaci�n por lo que el m�todo
	 * es vac�o
	 */
	@Override
	public void contribuirAS0fintOptim() {
		// deliberadamente vac�o
	}

	
	
	
	
//	@Override
//	/**
//	 * El proceso hist�rico no tiene VE de la optimizaci�n por lo tanto
//	 * carga el s0fint del proceso Markov asociado
//	 */
//	public void cargarValVEOptimizacion() {	
//		super();
//	}

	
	@Override
	public void cargarValRecursoVESimulacion() {
		//Este m�todo queda vac�o		
	}


	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos aproxFuturaOpt) {
		//Este m�todo queda vac�o
		
	}


	
	public static void main(String[] args) { 
//		
//		String nombre = "AportesHist�ricos";
//		String tipo = "";
//		String tipoSoporte = "";
//		String ruta = "D:/_Migro/PruebasMarkov2/Salidas";
//		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico(nombre, tipo, tipoSoporte, ruta, true, false);
//				
//		DatosPEHistorico dph = CargadorPEHistorico.devuelveDatosPEHistorico(dpe);
//		
//		ProcesoHistorico ph = new ProcesoHistorico(dph);
//		System.out.println("Termin� lectura ProcesoHistorico");
	}




	@Override
	public boolean tieneVEOptim() {
		System.out.println("ERROR: SE USA EN PE HIST�RICO EN LA OPTIMIZACI�N");
		if (CorridaHandler.getInstance().isParalelo()){
			//PizarronRedis pp = new PizarronRedis();
		//	pp.matarServidores();
		}
		System.exit(0);
		return false;
	}

	
	/**
	 * M�todo de la interfase AportanteEstado
	 * No usa el argumento resultado
	 */
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		for(VariableEstado ve: this.getVarsEstado()){
			ve.setEstadoFinalOptim(ve.getEstado());
		}
	}
		

	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		// TODO Auto-generated method stub
		
	}



//	@Override
//	public void actualizaVEPorControlesDE() {
//		// TODO Auto-generated method stub
//		
//	}


	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// Deliberadamente vac�o
	}


	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// Deliberadamente vac�o
		
	}




	@Override
	public void prepararPasoOptim(int cantSortMontecarlo) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// DELIBERADAMENTE EN BLANCO PORQUE NUNCA SER� INVOCADO PORQUE
		// EL GeneradorTermico NO TIENE VE CONTINUAS;
		return 0.0;
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
