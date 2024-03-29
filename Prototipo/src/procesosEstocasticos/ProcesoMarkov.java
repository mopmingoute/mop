/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoMarkov is part of MOP.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProcEstocasticos.DatosPEHistorico;
import datatypesProcEstocasticos.DatosPEMarkov;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import estado.Discretizacion;
import estado.VariableEstado;
import futuro.AFIncrementos;
import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;
import matrices.Oper;
import persistencia.CargadorPEHistorico;
import persistencia.CargadorPEMarkov;
import pizarron.PizarronRedis;
import procesosEstocasticos.EstimadorMarkovDiscreto.ClaveParInt;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.LineaTiempo;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.Par;


/**
 * Formalmente el ProcesoMarkov tiene dos innovaciones uniforme [0,1]:
 * La primera es la que elige la observación entre las del estado compuesto corriente
 * La segunda es la que elige el salto de estado compuesto para el paso siguiente.
 * @author ut469262
 *
 */
public class ProcesoMarkov extends ProcesoEstocastico implements DiscretoExhaustivo, AportanteEstado{
	
	
	private String estimacionVE;   // identificación de la estimación de las VE que se empleó

	/**
	 * Cantidad de clases posibles de cada VE
	 * el �ndice es la variable de estado
	 */
	private int[] cantCla;   
	

    /**
     * Hash map de matrices de probabilidades de transición de un paso
     * La clave es la estación inicial
     * El orden de filas y columnas en las matrices de transición es
     * el de defEstadosComp.
     */
    private Hashtable<Integer, MatPTrans> matrices;
    
    
    /**
     * Hashtable de matrices de probabilidades de transición de más de un paso
     * La clave es la pareja (pasoinicial, paso final)
     * Ac� se van almacenando las matrices una vez que se calculan, para no repetir el c�lculo 
     */
    private Hashtable<Par, MatPTrans> matVariosPasos;
    
    
    /**
     * Estructura que almacena los valores de las series representativos para cada
     * estación (paso dentro del año) y estado compuesto.
     * 
     * Primer índice del array: estación (paso dentro del año)
     * Segundo índice del array: estado compuesto 
     * 
     * El Object es un ArrayList<Double[]> con los datos de cada serie.
     * El ArrayList recorre las distintas observaciones, el Double[] contiene los valores de cada VA
     */
    private Object[][] observaciones;
    
    
    /** 
     * Definici�n de estados compuestos
     * Estado compuesto: resulta de las ordenaci�n
     * lexicogr�fica de los estados de cada variable de estado.
     * Ejemplo para dos variables de estado:
     * (1,1),(1,2), ....;(2,1),(2,2),(2,3),... 
     * ATENCI�N:  La VE del proceso es �nica y es la que representa el estado compuesto.
     */
    private ArrayList<int[]> defEstadosComp;
    
    private String nombreVEComp;   // nombre de la VE compuesta, es "nombre del proceso" + "VEComp" 
    
    private int cantEstadosComp;    // cantidad de estados compuestos

    private int cantEstac;     // cantidad de estaciones (per�odos en que vale cada modelo)
    
    /**
     * Un entero mayor que cero consecutivo entre 1 y cantEstac que indica la estaci�n
     * a la que pertenece cada paso del año
     * estDelPaso[0] es la estación del paso 1
     * estDelPaso[1] es la estaci�n del paso 2 y as� sucesivamente
     */
    int[] estDelPaso;    


	/**
	 * OJO ESTE PRESUPONE QUE PUEDE HABER VARIAS VE Y SOLO HAY UNA
	 * @param datos
	 */
    public ProcesoMarkov(DatosPEMarkov datos) {
		super(datos.getDatGen());
		this.setNombre(datos.getNombre());
		this.setDiscretoExhaustivo(datos.isDiscretoExhaustivo());
		this.setRuta(datos.getRuta());
		this.setNombrePaso(datos.getNombrePaso());
		this.setNombresVarsAleatorias(datos.getNombresVA());
		this.setNombresVarsEstado(datos.getNombresVE());
		this.setCantVA(datos.getCantVA());
		this.setCantVE(datos.getCantVE());		
		
		matrices = datos.getMatrices();
		matVariosPasos = new Hashtable<Par, MatPTrans>();
		observaciones = datos.getObservaciones();		
		cantEstadosComp = datos.getCantEstadosComp();
		defEstadosComp = datos.getDefEstadosComp();		
		estimacionVE = datos.getEstimacionVE();
		cantCla = datos.getCantCla();
		cantEstac = datos.getCantEstac();
		this.setCantidadInnovaciones(2);
		estDelPaso = datos.getEstDelPaso();
//		this.setCantPasosAnio(estDelPaso.length);  // ojo, �es redundante con  nombrepaso semana y encima est� mal?
		observaciones = datos.getObservaciones();
		// TODO:  hay que setear los instanteCorrienteInicial y Final
	
		// TODO: hay que setear la semilla general, creo que ya se hace en inicializar
		
		// hay una sola variable de estado y se carga ese nombre
		nombreVEComp = getNombresVarsEstado().get(0);
		this.completaConstruccion();
		
		// Crea la discretizaci�n de las VE
		for(int ive=0; ive<this.getCantVE(); ive++){
			ArrayList<Double> datosDisc = new ArrayList<Double>();
			for(int ival=0; ival<datos.getCantCla()[ive]; ival++){
				datosDisc.add((double)ival);
			}
			VariableEstado ve = this.devuelveVEDeNombre(datos.getNombresVE().get(ive));
			ve.setEstado(ve.getValorInicial());
			Discretizacion disc = new Discretizacion(ve, datosDisc, true);
			Evolucion<Discretizacion> ed = new EvolucionConstante<Discretizacion>(disc, new SentidoTiempo(1));
			ve.setEvolDiscretizacion(ed);
			ve.setDiscreta(true);
			ve.setOrdinal(true);
		}
	}



	@Override
	public void producirRealizacionSinPronostico(long instante) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * Solo funciona si los instantes de muestreo no se apartan más de un año del 
	 * instante inicial del paso de la optimización
	 * En ese caso si el paso del año de dos instantes es el mismo, el paso del PE es el mismo para ambos instantes
	 * 
	 * innovaciones1Sort 
	 * primer índice: índice de innovación (puede haber mas de una)
	 * segundo índice: recorre intervalos de muestreo 
	 */	
	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		
		int cantIM = instantesMuestreo.length;  // cantidad de instantes de muestreo
		
		int pasoPEInstIniPasoOptim = pasoDelAnio(this.getInstIniPasoOptim()); 
//		System.out.println("Inipaso: " + this.getInstIniPasoOptim());
		// Hay una sola variable de estado por eso se usa el get(0)
		Integer estadoInicial = (int)Math.rint(this.getVarsEstado().get(0).getEstado());
		
		int pasoPEImFinal = pasoDelAnio(instantesMuestreo[cantIM-1]);
		int estacion = estDelPaso[pasoPEImFinal];
		ArrayList<Double[]> obs1 = (ArrayList<Double[]>)observaciones[estacion-1][estadoInicial];
		int cantObs = obs1.size();
		double innov;
		int iva;
		int indObs;
		if(pasoPEInstIniPasoOptim==pasoPEImFinal || !this.isMuestreado()){
			// Todos los instantes de muestreo y el instante inicial del paso de la optimizaci�n
			// pertenecen al mismo paso del PE  
			// no hay transiciones a otro paso	
			// Un caso particular es el Markov de los aportes
			
			if(this.isMuestreado()){		
				for(int im = 0; im<cantIM; im++){					
					innov = innovaciones1Sort[0][im];
					indObs = (int)Math.floor(innov*cantObs);
					iva=0;							
					for(VariableAleatoria va: this.getVariablesAleatorias()){					
						va.getUltimoMuestreo()[im]=obs1.get(indObs)[iva];
						iva++;
					}					
				}	
			}else{				
				iva = 0;
				innov = innovaciones1Sort[0][0];
				indObs = (int)Math.floor(innov*cantObs);
				for(VariableAleatoria va: this.getVariablesAleatorias()){					
					va.setValor(obs1.get(indObs)[iva]);
					iva++;										
				}
				//System.out.println();
			}
		}else{
			/**
			 * Hay instantes de muestreo que requieren transiciones a partir del paso del PE
			 * correspondiente al instante inicial del paso de la optimizaci�n
			 */
			System.out.println("Se invocó producirRealizacionPEEstadoOptim de un ProcesoMarkov y requiere transiciones");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(0);
		}
		
	}



	





    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        // Imprime líneas de títulos
        sb.append("PROCESO MARKOV " + this.getNombre() + "\n");    
        sb.append("ESTIMACION " + this.getEstimacionVE() + "\n");
        sb.append("MATRICES " + "\n");
        
        // Imprime matrices
        Set<Integer> set = matrices.keySet();
        // Hay que ordenar las matrices para imprimirlas
        ArrayList<Integer> lista = new ArrayList<Integer>();
        lista.addAll(set);
        Collections.sort(lista);
        for(Integer i: lista){
            MatPTrans mPT = (MatPTrans)matrices.get(i);  
            String st = mPT.toString() + "\n";
            sb.append(st);                        
        }     
               
//        for(int i=1; i<=this.cantEstac; i++){
//          MatPTrans mPT = (MatPTrans)matrices.get(i);  
//          String st = mPT.toString() + "\n";
//          sb.append(st);          	
//        }

        sb.append("OBSERVACIONES" + "\n");
        for(int iest=0; iest< this.getCantEstac(); iest++){
            sb.append("Estaci�n" + (iest+1) + "\n");        	
        	for(int iec=0; iec<this.getCantEstadosComp(); iec++){
                sb.append("Estado compuesto " + iec + "\n");                
                ArrayList<Double[]> obs1 = (ArrayList<Double[]>) this.getObservaciones()[iest][iec];                
                for(int iobs=0; iobs<obs1.size(); iobs++){
                	for(int iVA = 0; iVA<this.getCantVA(); iVA++){
                		sb.append(obs1.get(iobs)[iVA] + " ");	
                	}
                	
                }
                sb.append("\n");      
        	}
        }
        
        
        String st = sb.toString();        
        return st;
    }
	
    
    

    

    ////////////////////////////////////////////////////////
    //
    //   COMIENZA IMPLEMENTACI�N DE M�TODOS DE LA INTERFASE
    //   DiscretoExhaustivo
    //
    //
    ////////////////////////////////////////////////////////    



	@Override
	public int devuelveCantPosibles(long instante) {
		return cantEstadosComp;
	}


	@Override
	public ArrayList<String> devuelveNombreValor(long instante, int valor) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	/**
	 * Devuelve la probabilidad de transici�n entre valores de estados compuestos
	 * ATENCI�N: instanteIni e instanteFin no pueden estar separados por m�s de 364 d�as
	 * de a�o.
	 * Ejemplo con (instanteIni 30 de octubre de 2020, instanteFin 4 de noviembre de 2022) no funciona
	 * 
	 * @param instanteIni
	 * @param instanteFin
	 * @param valorIni c�digo entero empezando en cero del estado compuesto en instanteIni
	 * @para valorFin c�digo entero empezando en cero del estado compuesto en instanteFin
	 * 
	 * ATENCI�N: LAS VARIABLES pini y pfin son �ndices que empiezan en cero
	 * 
	 */
	public double devuelveProbTransicion(long instanteIni, long instanteFin, int valorIni, int valorFin) {
		if(instanteIni>instanteFin){
			System.out.println("el instante inicial" + instanteIni  + " es posterior instante final "
					+ instanteFin);
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		if(instanteFin-instanteIni> Constantes.SEGUNDOSXDIA*364){
			System.out.println("el instante final" + instanteIni  + "difiere en m�s de 364 d�as del instante final "
					+ instanteFin);
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				
				//pp.matarServidores();
			}
			System.exit(1);
		}
		LineaTiempo lt = this.getOptimizadorPaso().getCorrida().getLineaTiempo();
		Hashtable<Integer,Long> instInicioAnioHT = lt.getInstInicioAnioHT();
		int anioCorrienteIni = anioDeInstante(instanteIni); // anio del instante inicial
		// Se corrige por la eventualidad de que el instanteIni est� en el a�o anterior al indicado por indAnio
		// if(instanteIni < instInicioAnio[this.getIndAnio()]) anioCorrienteIni--;
		int pini = pasoDelAnio(instanteIni);   // usa el m�todo pasoDelA�o de la clase padre ProcesoEstocastico

		long instFinAnioCorriente = instInicioAnioHT.get(anioCorrienteIni+1);
		int pfin;
		if(instanteFin<instFinAnioCorriente){
			pfin = pasoDelAnio(instanteFin);			
		}else{
			// el instanteFin es del a�o siguiente al corriente
			pfin = pasoDelAnio(instanteFin, anioCorrienteIni+1);  
		}		

		if(pfin==pini){
			// no hay transici�n porque se permanece en el mismo paso
			if(valorIni==valorFin) return 1.0;
			return 0.0;
		}else if(pfin==pini+1 || pini == (this.getCantPasosAnio()-1) && pfin==0){
			// hay una sola transici�n
			MatPTrans m = matrices.get(estDelPaso[pini]);
			double[][] probs = m.getProbs();
			return probs[valorIni][valorFin];
			
		}else{			
			// hay varias transiciones entre pini y pfin
			Par pi = new Par(pini,pfin);
			double[][] probs;
			if(matVariosPasos.get(pi)== null){
				// la matriz no existe hay que crearla
				MatPTrans m = matrices.get(estDelPaso[pini]);
				probs = m.getProbs();
				for(int paso=pini+1; paso<=this.getCantPasosAnio(); paso++){
					double[][] probs2 = matrices.get(estDelPaso[paso]).getProbs();
					probs = Oper.prod(probs, probs2);
				}
				for(int paso=0; paso<pfin; paso++){
					double[][] probs2 = matrices.get(estDelPaso[paso]).getProbs();
					probs = Oper.prod(probs, probs2);
				}
				// guarda la matriz generada
				m = new MatPTrans(pini, pfin, probs);
				matVariosPasos.put(pi, m);
			}else{
				// la matriz existe
				probs = matVariosPasos.get(pi).getProbs();
			}
			return probs[valorIni][valorFin];
		}	
		
	}


	@Override
	public String getNombrePE() {		
		return this.getNombre();
	}





	@Override
	public String getNombreVEPEDE() {
		return nombreVEComp;
		
	}	
	
	// FIN DE IMPLEMENTACI�N DE M�TODOS DE LA INTERFASE
    //   DiscretoExhaustivo





	
	public int[] getCantCla() {
		return cantCla;
	}


	public void setCantCla(int[] cantCla) {
		this.cantCla = cantCla;
	}

	public String getEstimacionVE() {
		return estimacionVE;
	}

	public void setEstimacionVE(String estimacionVE) {
		this.estimacionVE = estimacionVE;
	}


	public Hashtable<Integer, MatPTrans> getMatrices() {
		return matrices;
	}

	public void setMatrices(Hashtable<Integer, MatPTrans> matrices) {
		this.matrices = matrices;
	}


	public int getCantEstadosComp() {
		return cantEstadosComp;
	}

	public void setCantEstadosComp(int cantEstadosComp) {
		this.cantEstadosComp = cantEstadosComp;
	}

	public Object[][] getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(Object[][] observaciones) {
		this.observaciones = observaciones;
	}

	public ArrayList<int[]> getDefEstadosComp() {
		return defEstadosComp;
	}


	public void setDefEstadosComp(ArrayList<int[]> defEstadosComp) {
		this.defEstadosComp = defEstadosComp;
	}


	public int getCantEstac() {
		return cantEstac;
	}


	public void setCantEstac(int cantEstac) {
		this.cantEstac = cantEstac;
	}


	public int[] getEstDelPaso() {
		return estDelPaso;
	}


	public void setEstDelPaso(int[] estDelPaso) {
		this.estDelPaso = estDelPaso;
	}


//	public static void main(String[] args) { 
//		
//		String nombre = "MarkovAportes";
//		String tipo = "";
//		String tipoSoporte = "";
//		String ruta = "D:/_Migro/PruebasMarkov2/Salidas";
//		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico(nombre, tipo, tipoSoporte, ruta, true, false);
//				
//		DatosPEMarkov dpm = CargadorPEMarkov.devuelveDatosPEMarkov(dpe);
//		
//		ProcesoMarkov pm = new ProcesoMarkov(dpm);
//		System.out.println("Termin� estimaci�n ProcesoMarkov");
//		String salida = pm.toString();
//		String dirSalidaTexto = ruta + "/SalidaTexto.xlt";
//        boolean existe = DirectoriosYArchivos.existeArchivo(dirSalidaTexto);
//        if(existe) DirectoriosYArchivos.eliminaArchivo(dirSalidaTexto);  
//		DirectoriosYArchivos.agregaTexto(dirSalidaTexto, salida);
//		System.out.println("Termin� impresi�n - fin del programa");
//	}

	public boolean tieneVEOptim() {
		return true;
	}








//	@Override
//	public void actualizarVarsEstadoSimulacion() {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//	@Override
//	public void actualizarVarsEstadoOptimizacion() {
//		// TODO Auto-generated method stub
//		
//	}






	@Override
	public void contribuirAS0fint() {
		// deliberadamente vac�o, el Markov no se usa en la simulaci�n		
	}

	/**
	 * EL MARKOV APORTA LA VE DE LA OPTIMIZACI�N
	 * simplemente mantiene el estado, porque el salto del estado se di�
	 * antes del inicio del paso
	 */
	@Override
	public void contribuirAS0fintOptim() {
		this.getVarsEstado().get(0).setEstadoS0fint(this.getVarsEstado().get(0).getEstadoDespuesDeCDE());		
	}	
	

//	@Override
//	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
//		return this.getVarsEstado();
//	}
//
//	@Override
//	public ArrayList<VariableEstado> aportarEstadoOptimizacion() {     
//        if(!this.optim) return new ArrayList<>();
//        return this.getVarsEstado();
//    }


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
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		VariableEstado ve = this.getVarsEstado().get(0);
		ve.setEstadoFinalOptim(ve.getEstado());
	}






//	@Override
//	public void actualizaVEPorControlesDE() {
//		// TODO Auto-generated method stub
//		
//	}






	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
	//	this.setVarsEstado(this.getVarsEstadoOptim());
		VariableEstado ve = this.getVarsEstado().get(0);
		ve.setEstadoDespuesDeCDE(ve.getEstado());			
	}






	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		VariableEstado ve = this.getVarsEstado().get(0);
		ve.setEstadoDespuesDeCDE(ve.getEstado());	
	}








	@Override
	public void prepararPasoOptim(int cantSortMontecarlo) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// Este m�todo no debe ser invocado nunca porque el PE Markov no tiene
		// variables de estado continuas
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



	@Override
	/**
	 * En la SDDP (Dual) devuelve la cantidad de sorteos de Montecarlo que requiere el proceso dado el paso de tiempo y el estado (empezando en 0) 
	 * @param pasoCorrida es el paso de tiempo de la corrida
	 * @param estado es el número de estado discreto del PE DE empezando en cero
	 */
	public int dameCantMontecarlosDual(int pasoCorrida, int estado) {
		int pasoDelAnioPE = this.pasoDelPEDePasoCorrida(pasoCorrida);   // empezando en cero
		int estacion = estDelPaso[pasoDelAnioPE];
		ArrayList<Double[]> obs1 = (ArrayList<Double[]>)observaciones[estacion-1][estado];
		int cantObs = obs1.size();
		return cantObs;
	}



	@Override
	public int dameMultiplicadorMontecarlosDual(int paso, int estado) {
		return 1;  // TODO DEBE TOMARSE EL VALOR DEL ARCHIVO DE ENTRADA QUE SE ENCUENTRA EN RESOURCES
	}



	


//	@Override
//	public ArrayList<VariableEstado> getVarsEstadoOptim() {
//		// TODO Auto-generated method stub
//		return null;
//	}







//	@Override
//	public void producirRealizacionEstadoOptim(int instante, double[] innovaciones) {
//		double innovObs = innovaciones[0];
//		int paso = pasoDelAnio(instante);
//		int iestado = estDelPaso[paso];
//		// obs contiene las observaciones dado el paso y el estado
//		ArrayList<Double[]> obs = (ArrayList<Double[]>) observaciones[paso][iestado];
//		int cantInnov = obs.size();  // cantidad de innovaciones dado el paso y el estado 
//		int indObs = (int) Math.floor(innovObs*cantInnov);
//		for(int iva = 0; iva<this.getCantVA(); iva++){
//			this.getVariablesAleatorias().get(iva).setValor(obs.get(indObs)[iva]);			
//		}		
//	}
    
    
    
    
    
    
}
