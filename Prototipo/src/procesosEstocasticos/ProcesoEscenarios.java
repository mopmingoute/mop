/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoEscenarios is part of MOP.
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

import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProcEstocasticos.DatosPEEscenarios;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import persistencia.CargadorPEEscenarios;
import pizarron.PizarronRedis;
import utilitarios.Constantes;
import estado.VariableEstado;
import futuro.AFIncrementos;

/**
 * Cada escenario consiste en una serie de valores del PE en los pasos sucesivos.
 * 
 * @author ut469262
 *
 */
// public class ProcesoEscenarios extends ProcesoEstocastico implements AportanteEstado{
	
public class ProcesoEscenarios extends ProcesoEstocastico {
	
	/**
	 * Para la optimización se supone que existe una única innovación que elige
	 * entre todos los escenarios del paso, los valores de uno de ellos
	 */
	
	
	
	
	/*
	 * Es el instante en segundos inicial del primer paso (SI BIEN EL INTERVALO ES ABIERTO AL INICO).
	 * RECORDAR QUE LOS PASOS SON CONJUNTOS ABIERTOS AL INICIO Y CERRADOS AL FINAL DEL INTERVALO.
	 * 
	 * TODO: DEBE HACERSE UN CHEQUEO DE QUE EFECTIVAMENTE ESE INSTANTE INICIA UN INTERVALO, DADO EL PASO DEL PE.
	 * 
	 */
//	private int instInicEscenarios; 
	
	/*
	 * 
	 */

	private int anioInicialPE;   // año del primer paso del PE de los escenarios, ejemplo: 2016.
	private int pasoInicialPE;   
	private int anioFinalPE;
	private int pasoFinalPE;
	// paso del año del primer paso del PE, ejemplo paso 10, si arranca en la semana décima empezando de cero del 2016
	// los pasos son 0, 1, 2, .....
	private int cantMaxPasos;   // cantidad máxima de pasos que tiene puede tener el proceso en un año
	
	/**
	 * datos de las VA:
	 * primer índice "fila" es el escenario (ya no es una crónica histórica sino un índice
	 * de escenario generado
	 * segundo índice año a partir de anioInicialPE 
	 * tercer índice recorre el ordinal del paso en el año, por ejemplo las semanas
	 * cuarto índice recorre las VA 
	 */
	private double[][][][]  datos;
	
	/**
	 * valores de las variables de estado, asociados a cada dato.
	 * tercer índice recorre las VE
	 */
	private double[][][][]  valoresVE;
	
	private String estimacionVE;   // identificación de la estimación de las VE que se empleó	

	private int[][] etiquetaCron;     // da una etiqueta de crónica a cada escenario para cada año; por ejemplo "1909", "1910", etc.
	

	private int cantEsc;      // cantidad de escenarios
	
	/**
     *  Para cada año en los que existen pasos del escenario, ordinal (columna) de dicho primer
     *  paso en el escenario. La clave es el año y el valor es el ordinal.
     *  Ejemplo: un proceso semanal cuyos escenarios empiezan en la semana 26 del año 2016 tiene en ordinalPrimerPasoAnio
     *  las entradas: (2016, 1), (2017,27) , (2018, 27+52=79), .....
	 */	
	private Hashtable<Integer,Integer> ordinalPrimerPasoAnio; 
	
	
	private int cantPasosMax;   // máxima cantidad de pasos que puede tener un año
	private int cantAnios;    // cantidad de años que abarca el proceso
	
	/**
	 * Atributos necesarios para emplear el proceso en la optimización
	 */
	
	/**
	 * Los números de escenarios que quedan sorteados al comienzo del paso, que se numeran
	 * a partir de 1 !!!
	 */
	private int[] escenariosSorteados;  
	private int isort; // indice de sorteos correlativo con el empleado en los Montecarlos
	
	
	
	private int iFila = 0;
	private int iCol = 0;
	private int iVA = 0;
	private int iVE = 0;
	
	public ProcesoEscenarios(){}
	
	public ProcesoEscenarios(DatosPEEscenarios dpesc) {
		super(dpesc.getDatGen());
		this.setNombre(dpesc.getNombre());
		this.setDiscretoExhaustivo(dpesc.isDiscretoExhaustivo());
		this.setRuta(dpesc.getRuta());
		datos = dpesc.getDatos();
		valoresVE = dpesc.getValoresVE();
		estimacionVE = dpesc.getEstimacionVEUsada();
		this.setNombresVarsAleatorias(dpesc.getNombresVA());
		this.setNombresVarsEstado(dpesc.getNombresVE());		
		this.setCantVA(dpesc.getCantVA());
		this.setCantVE(dpesc.getCantVE());
		this.setNombrePaso(dpesc.getNombrePaso());
		cantEsc = dpesc.getCantEsc();
		cantMaxPasos = dpesc.getCantMaxPasos(); 		

		anioInicialPE = dpesc.getAnioInicialPE();
		pasoInicialPE = dpesc.getPasoInicialPE();
		cantAnios = anioFinalPE -  anioInicialPE + 1;
		anioFinalPE = dpesc.getAnioFinalPE();
		etiquetaCron = dpesc.getEtiquetaCron();
		
		this.completaConstruccion();	
		this.setCantidadInnovaciones(1);
			
	}		
	


	public int getAnioInicialPE() {
		return anioInicialPE;
	}

	public void setAnioInicialPE(int anioInicialPE) {
		this.anioInicialPE = anioInicialPE;
	}

	public int getPasoInicialPE() {
		return pasoInicialPE;
	}

	public void setPasoInicialPE(int pasoInicialPE) {
		this.pasoInicialPE = pasoInicialPE;
	}

	public int getAnioFinalPE() {
		return anioFinalPE;
	}
	
	public void setAnioFinalPE(int anioFinalPE) {
		this.anioFinalPE = anioFinalPE;
	}
	
	

	public int getPasoFinalPE() {
		return pasoFinalPE;
	}

	public void setPasoFinalPE(int pasoFinalPE) {
		this.pasoFinalPE = pasoFinalPE;
	}

	public int getCantAnios() {
		return cantAnios;
	}

	public void setCantAnios(int cantAnios) {
		this.cantAnios = cantAnios;
	}

	public void setEtiquetaCron(int[][] etiquetaCron) {
		this.etiquetaCron = etiquetaCron;
	}

	public int getCantMaxPasos() {
		return cantMaxPasos;
	}

	public void setCantMaxPasos(int cantMaxPasos) {
		this.cantMaxPasos = cantMaxPasos;
	}

	public double[][][][] getDatos() {
		return datos;
	}

	public void setDatos(double[][][][] datos) {
		this.datos = datos;
	}

	public double[][][][] getValoresVE() {
		return valoresVE;
	}

	public void setValoresVE(double[][][][] valoresVE) {
		this.valoresVE = valoresVE;
	}

	public String getEstimacionVE() {
		return estimacionVE;
	}

	public void setEstimacionVE(String estimacionVE) {
		this.estimacionVE = estimacionVE;
	}

	public int[][] getEtiquetaCron() {
		return etiquetaCron;
	}

	public void setEtiquetaEsc(int[][] etiquetaCron) {
		this.etiquetaCron = etiquetaCron;
	}

	public int getCantEsc() {
		return cantEsc;
	}

	public void setCantEsc(int cantEsc) {
		this.cantEsc = cantEsc;
	}

	public Hashtable<Integer, Integer> getOrdinalPrimerPasoAnio() {
		return ordinalPrimerPasoAnio;
	}

	public void setOrdinalPrimerPasoAnio(
			Hashtable<Integer, Integer> ordinalPrimerPasoAnio) {
		this.ordinalPrimerPasoAnio = ordinalPrimerPasoAnio;
	}


	public int getCantPasosMax() {
		return cantPasosMax;
	}


	public void setCantPasosMax(int cantPasosMax) {
		this.cantPasosMax = cantPasosMax;
	}




	public int getiVE() {
		return iVE;
	}

	public void setiVE(int iVE) {
		this.iVE = iVE;
	}


	/**
	 * Si el instante es anterior al inicio del primer paso de tiempo 
	 * de los escenarios, el proceso da error
	 * En la simulación el proceso repite circularmente sus propios escenarios
	 * Ejemplo: si tiene 105 escenarios del 1 al 105, el escenario 106 repite el 1, y as� sucesivamente
	 * 
	 */
	@Override
	public void producirRealizacionSinPronostico(long instante) {
		if(optim){
			// Si el método es invocado desde la optimización se carga uno de los escenarios
			// sorteados, en secuencia circular 0,.....,cantSorteos-1,0,......
			this.setEscenario(escenariosSorteados[isort]+1); // suma 1 porque los escenarios de simulación empiezan en 1
		}
		int anioCorriente = anioDeInstante(instante);  // anio del instante pedido
		int iesc = (this.getEscenario()-1)%cantEsc;		
		int ip = pasoDelAnio(instante);   // usa el método pasoDelAnio de la clase padre ProcesoEstocastico
		int ian = anioCorriente - anioInicialPE;
		if(ian<0 || (ian==0 && ip<this.getPasoInicialPE()-1) ){
			System.out.println("el instante " + instante  + " es anterior al primer paso de los escenarios");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		if(anioCorriente>anioFinalPE 
				|| (anioCorriente==anioFinalPE && (ip+1)>pasoFinalPE)   ){			
			System.out.println("el instante " + instante  + " es posterior al �ltimo paso de los escenarios");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}			
					
		int iva;
		int ive;
//		if (Constantes.NIVEL_CONSOLA > 1) {
//			System.out.println("INSTANTE=" + instante + "proceso " + this.getNombre());
//			System.out.println("iesc=" + iesc + " ian=" + ian +  " ip=" + ip);
//		}
		
		for(VariableAleatoria va: this.getVariablesAleatorias()){
			iva = this.getIndiceVA().get(va.getNombre());
			va.setValor(datos[iesc][ian][ip][iva]);				
		}
		// carga los valores de las variables de estado
		for(VariableEstado ve: this.getVarsEstado()){
			ive = this.getIndiceVE().get(ve.getNombre());
			ve.setEstado(valoresVE[iesc][ian][ip][ive]);				
		}	
	}
	
	
	/**
	 * Carga atributo optim y determina los escenarios sorteados para
	 * ser usados en la optimización
	 * @param cantSorteos
	 */
	public void prepararPasoOptim(int cantSorteos){
		optim = true;
		escenariosSorteados = new int[cantSorteos];
		for(int is=0; is<cantSorteos; is++){
			GeneradorDistUniforme gdu = this.getGeneradoresAleatorios().get(0); // hay un único generador de innovaciones
			// Se suma uno porque los escenarios van del 1 en adelante en el ProcesoEscenario
			// y devuelve ordinal va de 0 en adelante.
			escenariosSorteados[is] = gdu.devuelveOrdinal(cantEsc)+1;
			if(escenariosSorteados[is]<0){
				System.out.println("un escenario es negativo");
			}
		}   
	}

	public void prepararPaso(){
	
	}





//	@Override
//	public Hashtable<String, ArrayList<Double>> dameRealizacionesIntervalo(
//			int instanteInicial, int instanteFinal) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Hashtable<String, ArrayList<Double>> dameNRealizacionesInstanteEstado(
//			int instante, int n, ArrayList<VariableEstado> estado) {
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

	
	public static void main(String[] args) { 
//		
//		String nombre = "Eolico";
//		String tipo = "";
//		String tipoSoporte = "";
//		String ruta = "D:/_Migro/PruebaPEEscenarios";
//		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico(nombre, tipo, tipoSoporte, ruta, false,false);
////				
////		DatosPEEscenarios dpesc = CargadorPEEscenarios.devuelveDatosPEEscenarios(dpe);
////		
////		ProcesoEscenarios pesc = new ProcesoEscenarios(dpesc);
//		System.out.println("Termina lectura ProcesoEscenarios");
		
		
	}
	
//	@Override
//	public ArrayList<VariableEstado> aportarEstadoOptimizacion() {
//		return null;
//	}
//
//
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
//
//
//	@Override
//	public void contribuirAS0fint() {
//		//Este m�todo queda vac�o
//	}
//
//
//	@Override
//	public void cargarValVEOptimizacion() {	
//		//Este m�todo queda vac�o
//	}
//
//	
//	@Override
//	public void cargarValRecursoVESimulacion() {
//		//Este m�todo queda vac�o		
//	}
//
//
//	@Override
//	public void cargarValRecursoVEOptimizacion(AFIncrementos aproxFuturaOpt) {
//		//Este m�todo queda vac�o
//		
//	}
//
//	@Override
//	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	
	/**
	 * Este método carga 
	 * - valor para las VA que no son muestreadas
	 * - ultimoMuestreo[] para las VA que son muestreadas
	 * Sortea UN ESCENARIO QUE USARA PARA TODOS LOS INSTANTES DE MUESTREO
	 * PARA ESO TOMA LA INNOVACION DEL PRIMER INSTANTE DE MUESTREO Y DESECHA LAS RESTANTES. 
	 * 
	 * @param innovaciones1Sort primer índice ordinal de innovación, segundo índice intervalo de muestreo 
	 */
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		System.out.println("Se invoca producirRealizacionPEEstadoOptim para un ProcesoEscenarios "
				+ "y es un método para procesos que tienen VE en la optimizaci�n");
		if (CorridaHandler.getInstance().isParalelo()){
			//PizarronRedis pp = new PizarronRedis();
			//pp.matarServidores();
		}
		System.exit(0);	
	}
//		
//		double innov = innovaciones1Sort[0][0];  
//		// primer �ndice 0 hay s�lo una innovaci�n
//		// segundo �ndice 0 se toma la innovaci�n del primer instante de muestreo
//		
//		int indEsc = (int)Math.floor(innov*cantEsc);  // se selecci�n el escenario que se usar�
//		
//		for(int im = 0; im<instantesMuestreo.length; im++){
//			int instante = instantesMuestreo[im];
//			int pasoDelAnioCorr = pasoDelAnio(instante);
//			
//			// anioCorr es el a�o corriente por ejemplo 2021
//			int anioCorr = this.anioCorriente(instante);  
//			// indAnio es el ordinal del a�o dentro del proceso a partir de anioInicialPE
//			int indAnio = anioCorr-this.getAnioInicialPE();
//			
//			// ordPrimerPasoAnioCorr es el ordinal del paso del proceso en que empieza el a�o corriente
//			// ejemplo, si el a�o corriente es 2020, y el proceso es semanal y empieza al comienzo 2019
//			// entonces ordPrimerPasoAnioCorr es 53.
//			int ordPrimerPasoAnioCorr = ordinalPrimerPasoAnio.get(anioCorr);  
//			
//			// indPasoProc es el paso del proceso en el que cae instante
//			int indPasoProc = pasoDelAnioCorr + ordPrimerPasoAnioCorr -1;
//			
//			/**
//			 * En datos primer �ndice "fila" es el escenario (ya no es una cr�nica hist�rica sino un �ndice		 
//		     * de escenario generado
//			 * segundo �ndice a�o a partir de anioInicialPE 
//			 * tercer �ndice recorre el ordinal del paso en el a�o, por ejemplo las semanas
//			 * cuarto �ndice recorre las VA
//			 * Se muestrea eligiendo el escenario
//		     */ 
//			
//			// El ProcesoEscenarios tiene una �nica innovaci�n que elige uno de los escenarios
//
//			int iva = 0;
//			if(this.isEsMuestreado()){
//				// se carga el atributo ultimoMuestreo de las VA porque el PE es muestreado
//				for(VariableAleatoria va: this.getVariablesAleatorias()){
//					va.setValor(datos[indEsc][indAnio][indPasoProc][iva]);
//					iva++;
//				}	
//			}else{
//				iva = 0;
//				// se carga el atributo valor de las VA porque el PE no es muestreado
//				for(VariableAleatoria va: this.getVariablesAleatorias()){
//					va.getUltimoMuestreo()[im] = datos[indEsc][indAnio][indPasoProc][iva];
//					iva++;
//				}	
//			}
//		}
//	}

	@Override
	public boolean tieneVEOptim() {
		// No tiene VE en la optimizaci�n
		return false;
	}
	
	
	/**
	 * Método de la interfase AportanteEstado
	 * No usa el argumento resultado
	 */
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		for(VariableEstado ve: this.getVarsEstado()){
			ve.setEstadoFinalOptim(ve.getEstado());
		}
	}

	

//	@Override
//	public void producirRealizacionNoEstadoOptim(int instante, double[] innovaciones) {
//
//		// ATENCI�N: pasoDelAnioCorr no es el paso del proceso que empieza en el instante inicial, quiz�s a�os antes
//		// sino el pason entro del a�o corriente		
//		int pasoDelAnioCorr = pasoDelAnio(instante);
//
//		// anioCorr es el a�o corriente por ejemplo 2021
//		int anioCorr = this.anioCorriente(instante);  
//		
//		// ordPrimerPasoAnioCorr es el ordinal del paso del proceso en que empieza el a�o corriente
//		// ejemplo, si el a�o corriente es 2020, y el proceso es semanal y empieza al comienzo 2019
//		// entonces ordPrimerPasoAnioCorr es 53.
//		int ordPrimerPasoAnioCorr = ordinalPrimerPasoAnio.get(anioCorr);  
//		
//		// pasoProc es el paso del proceso en el que cae instante
//		int pasoProc = pasoDelAnioCorr + ordPrimerPasoAnioCorr -1;
//		
//		/**
//		 *  En datos primer �ndice "fila" es el escenario (ya no es una cr�nica hist�rica sino un �ndice		 
//	     * de escenario generado
//		 * segundo �ndice a�o a partir de anioInicialPE 
//		 * tercer �ndice recorre el ordinal del paso en el a�o, por ejemplo las semanas
//		 * cuarto �ndice recorre las VA
//		 * Se muestrea eligiendo el escenario
//	     */ 
//		
//		
//		// El ProcesoEscenarios tiene una �nica innovaci�n que elige uno de los escenarios
//		int indEsc = Math.floor(innovaciones[0]*cantEsc);
//		for(VariableAleatoria va: this.getVariablesAleatorias()){
//			
//		}
		 
		
//	}


	
	
}
