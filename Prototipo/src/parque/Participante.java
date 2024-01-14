/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Participante is part of MOP.
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

package parque;

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import datatypesSalida.ResultadoIteracionSP;
import logica.CorridaHandler;
import optimizacion.OptimizadorEstado;
import optimizacion.OptimizadorPaso;
import pizarron.PizarronRedis;
import procesosEstocasticos.ProcesoEstocastico;
import java.util.ArrayList;
import java.util.Hashtable;

import simulacion.SimuladorPaso;
import tiempo.LineaTiempo;
import utilitarios.Constantes;
import compdespacho.CompDespacho;
import compgeneral.CompGeneral;
import compsimulacion.CompSimulacion;
import control.VariableControl;
import control.VariableControlDE;
import cp_compdespProgEst.CompDespPE;

/**
 * Clase que representa un participante
 * 
 * @author ut602614
 *
 */
public abstract class Participante {
	private String nombre;
	
	private String tipo;

	private LineaTiempo lt;

	private CompDespacho comportamiento;

	private CompGeneral compGeneral;

	private CompSimulacion compSimulacion;
	
	private CompDespPE compDespPE;

	private boolean ultimoInfactible = false;

	protected SimuladorPaso simPaso;

	protected OptimizadorPaso optimPaso;

	protected OptimizadorEstado optimEstado;

	/**
	 * Si no son null, son los procesos estocósticos de los que proceden las
	 * variables aleatorias del participante
	 */
	private Hashtable<String, ProcesoEstocastico> procesosDelParticipante;
	
	private ArrayList<Impacto> impactosQueLoInvolucran;



	/**
	 * Asocia a un nombre de variable aleatoria en una fase del algoritmo un
	 * proceso estocóstico Si no existe la colección la crea. Si el proceso
	 * estocóstico no tiene una VA con ese nombre detiene la ejecución
	 * 
	 * @param nombreVA
	 *            nombre de la variable aleatoria
	 * @param fase
	 *            optimización, simulación, etc. segón las constantes de
	 *            utilitarios.Constantes
	 * @param proc
	 *            el proceso estocástico
	 * 
	 */
	public void agregarProcesoAParticipante(String nombreVA, String fase, ProcesoEstocastico proc) {
		if (procesosDelParticipante == null) {
			procesosDelParticipante = new Hashtable<String, ProcesoEstocastico>();
		}
		
		if(!proc.getNombresVarsAleatorias().contains(nombreVA)){
			System.out.println("El proceso " + proc.getNombre() + " no contiene la variable aleatoria " + nombreVA);
		}
		if(fase.equalsIgnoreCase(utilitarios.Constantes.FASE_OPT) & !proc.isUsoOptimizacion()){
			System.out.println("El proceso " + proc.getNombre() + " no se puede usar en la optimizacion");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		if(fase.equalsIgnoreCase(utilitarios.Constantes.FASE_SIM) & !proc.isUsoSimulacion()){
			System.out.println("El proceso " + proc.getNombre() + " no se puede usar en la simulacion");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
//				pp.matarServidores();
			}
			System.exit(1);
		}		
		String cp = nombreVA + "-" + fase;
		procesosDelParticipante.put(cp, proc);

	}
	
	
	/**
	 * Verifica que la informacion de las VA del participante sobre uso de los procesos
	 * en simulacion y optimizacion sea coherente con la informacion de los propios procesos.
	 */
	public void chequearProcesosConAsociadoEnOptim(ProcesoEstocastico proc, ProcesoEstocastico procOptim){
		if(proc.getProcesoAsociadoEnOptim()!=null && proc.getProcesoAsociadoEnOptim()!=procOptim){
			System.out.println("El proceso " + proc.getNombre() + " no tiene asociado a " + procOptim.getNombre() + " en optimizaci�n");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		
	}

	/**
	 * Devuelve el proceso estocóstico que el participante tiene asociado a un
	 * nombre de VA en una fase del algoritmo
	 * 
	 * @param nombreVA
	 * @param fase
	 *            optimización, simulación, etc. segón las constantes de
	 *            utilitarios.Constantes
	 * @return proc el proceso estocóstico
	 */
	public ProcesoEstocastico devuelveProceso(String nombreVA, String fase) {
		String cp = nombreVA + "-" + fase;
		if (procesosDelParticipante != null) {
			ProcesoEstocastico pe = procesosDelParticipante.get(cp);
			return pe;
		} else {
			return null;
		}
	}


	public Hashtable<String, ProcesoEstocastico> getProcesosDelParticipante() {
		return procesosDelParticipante;
	}

	public void setProcesosDelParticipante(Hashtable<String, ProcesoEstocastico> procesosDelParticipante) {
		this.procesosDelParticipante = procesosDelParticipante;
	}



	public OptimizadorEstado getOptimEstado() {
		return optimEstado;
	}

	public void setOptimEstado(OptimizadorEstado optimEstado) {
		this.optimEstado = optimEstado;
	}

	private boolean aportanteEstado;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	

	public String getTipo() {
		return tipo;
	}


	public void setTipo(String tipo) {
		this.tipo = tipo;
	}


	public void contribuirObjetivo() {
		// TODO Auto-generated method stub
	}

	public void cargarRestricciones() {
		// TODO Auto-generated method stub
	}

	public int getCantPostes() {
		// TODO
		if (optimPaso.isOptimizando()) {			
			return optimPaso.getCorrida().getCantidadPostes();
		} else if (simPaso.isSimulando()) {
			return simPaso.getCorrida().getCantidadPostes();
		} else {
			return 0;
		}
	}
	
	public long[] getInstantesMuestreo() {
		if (optimPaso.isOptimizando()) {
			return optimPaso.getInstantesMuestreo();
		} else if (simPaso.isSimulando()) {
			return simPaso.getInstantesMuestreo();
		} else {
			return null;
		}
	}
	
	public int getDuracionPostes(int i) {
		if (optimPaso.isOptimizando()) {
			return optimPaso.getDuracionPostes()[i];
		} else if (simPaso.isSimulando()) {
			return simPaso.getDuracionPostes()[i];
		}
		return 0;
	}

	public int getDuracionPaso() {
		if (optimPaso.isOptimizando()) {
			return optimPaso.getDuracionPaso();
		} else if (simPaso.isSimulando()) {
			return simPaso.getDuracionPaso();
		}
		return 0;
	}

	public double devuelveTasaDescuento(){
		if (optimPaso.isOptimizando()) {
			return optimPaso.getCorrida().getTasa();
		} else if (simPaso.isSimulando()) {
			return simPaso.getCorrida().getTasa();
		} else {
			return 0;
		}				
	}

	/**
	 * Asigna la VA del participante con la VA del proceso estocóstico que tiene
	 * el mismo nombre del proceso correspondiente en la optimización o la
	 * simulación
	 */
	public abstract void asignaVAOptim();

	public abstract void asignaVASimul();

	public ArrayList<VariableControl> getVarsControl() {
		if (compGeneral == null)
			return new ArrayList<VariableControl>();
		return compGeneral.getVarsControl();
	}

	public ArrayList<VariableControlDE> getVarsControlDE() {
		if (compGeneral == null)
			return new ArrayList<VariableControlDE>();
		return compGeneral.getVarsControlDE();
	}

	public CompDespacho getCompDesp() {
		return comportamiento;
	}

	public void setCompDesp(CompDespacho comportamiento) {
		this.comportamiento = comportamiento;
	}
	
	

	public CompDespPE getCompDespPE() {
		return compDespPE;
	}


	public void setCompDespPE(CompDespPE compDespPE) {
		this.compDespPE = compDespPE;
	}


	public CompGeneral getCompGeneral() {
		return compGeneral;
	}

	public void setCompGeneral(CompGeneral compGeneral) {
		this.compGeneral = compGeneral;
	}

	public CompSimulacion getCompSimulacion() {
		return compSimulacion;
	}

	public void setCompSimulacion(CompSimulacion compSimulacion) {
		this.compSimulacion = compSimulacion;
	}

	public void actualizarVarsCompGeneral(long instante) {
		// sacar la foto y guardarla en comportamiento simulación
		if (compSimulacion != null && compGeneral != null)
			compSimulacion.setValsCompGeneral(compGeneral.getFotoComportamientos(instante));
	}

	/**
	 * carga los datos que estón en el CompSimulacion en el CompDespacho: i) el
	 * estado inicial, ii) los valores postizados de las variables aleatorias,
	 * iii) los atributos del participante cargarDatosSimulacion invoca para
	 * cada participante al Postizador. El Postizador conoce quó variables
	 * aleatorias de los participantes son muestradas por intervalo, y para el
	 * conjunto de ellas sabe si la postización es acoplada (interna) // o
	 * desacoplada (externa). Segón sea la opción, el Postizador muestrea y
	 * postiza o simplemente lee los datos de archivo y devuelve un dato por
	 * poste (DatosPostizados)
	 * 
	 * @param instante es el instante inicial del paso
	 */
	public void cargarDatosCompDespacho(long instante) {
		if (compSimulacion != null && compGeneral != null)
			this.compSimulacion.cargarDatosCompDespacho(instante);
	}

	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		if (compSimulacion != null && compGeneral != null)
			this.compSimulacion.actualizarParaProximoPaso(salidaIter);
	}

	public void actualizarVariablesCompDespacho(int iter) {
		if (compSimulacion != null && compGeneral != null)
			this.compSimulacion.actualizarVariablesCompDespacho(iter);
	}

	public void actualizarVariablesCompGlobal(Hashtable<String, String> compsGlobales) {
		if (compSimulacion != null && compGeneral != null)
			this.compSimulacion.actualizarVariablesCompGlobal(compsGlobales);

	}

	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		if (compSimulacion != null && compGeneral != null)
			this.compSimulacion.cargarDatosParaUnaIteracion(iter, salidaIter);

	}

	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		if (compSimulacion != null && compGeneral != null)
			return this.compSimulacion.aceptaDetenerIteracion(iter, salidaIter);
		else
			return true;
	}

	public abstract void inicializarParaEscenario();

	/**
	 * Carga en resultadoPaso el aporte del participante a partir del resultado
	 * del problema lineal
	 * 
	 * @param resultadoPaso
	 * @param salidaUltimaIter
	 * @param proceso
	 *            es el proceso en que se invoca: simulación, optimización, etc.
	 * @para instante instante inicial del paso
	 */
	public abstract void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante);
	
	
	/**
	 * Carga en resIter las potencias despachadas en una iteración si el participate inyecta o extrae
	 * potencia de las barras, el costo marginal en la iteración si es una barra, o nada si el participante
	 * no aporta ninguna de ambas magnitudes
	 *  
	 * @return
	 */
	public void guardarPotYCMgUnaIteracion(ResultadoIteracionSP resIter, DatosSalidaProblemaLineal salidaUltimaIter) {
		int cantPostes = this.getCantPostes();
		double[] potencia = new double[cantPostes];		
		if(this.getTipo()!=null) {
			if(this.getTipo().equalsIgnoreCase(utilitarios.Constantes.TER)
			   || this.getTipo().equalsIgnoreCase(utilitarios.Constantes.HID)		
			   || this.getTipo().equalsIgnoreCase(utilitarios.Constantes.CC)
			   || this.getTipo().equalsIgnoreCase(utilitarios.Constantes.EOLO)
			   || this.getTipo().equalsIgnoreCase(utilitarios.Constantes.FOTOV)
			   || this.getTipo().equalsIgnoreCase(utilitarios.Constantes.ACUM)){			   
				for (int p = 0; p < cantPostes; p++) {
					String nombreVar = comportamiento.getNpotp()[p];
					potencia[p] = salidaUltimaIter.getSolucion().get(nombreVar);
				}
				resIter.getResultadoPotencias().put(this.getNombre(),potencia);	
			}else if(this instanceof Demanda) {
				Demanda d = (Demanda)this;
				potencia = d.getCompD().getPotActivaPorPoste();
				resIter.getResultadoPotencias().put(this.getNombre(),potencia);	
			}else if(this instanceof Falla) {
				Falla f = (Falla)this;				
		
				for (int esc = 0; esc <f.getCantEscalones(); esc++ ) {
					String nombreEsc= f.getDemanda().getNombre() + "_EscFalla" + esc;
					double[] potencias1Esc = new double[this.getCantPostes()];
					for (int p = 0; p < this.getCantPostes(); p++) {								
						potencias1Esc[p] = salidaUltimaIter.getSolucion().get(f.getCompDesp().generarNombre("potpe_", p + "_" + esc));	
					}
					resIter.getResultadoPotencias().put(nombreEsc,potencias1Esc);	
				}				
				
			}else if(this instanceof ImpoExpo) {				
				ImpoExpo ie = (ImpoExpo)this;
				double[][] potenciab = new double[ie.getCantBloques()][cantPostes];
				for(int ib=0; ib < ie.getCantBloques();ib++){
					for (int ip = 0; ip < this.getCantPostes(); ip++) {
						potenciab[ib][ip] = salidaUltimaIter.getSolucion().get(ie.getCompD().generarNombre("pot",Integer.toString(ib) ,Integer.toString(ip)));
						potencia[ip] += potenciab[ib][ip];
					}
				}
				resIter.getResultadoPotencias().put(this.getNombre(),potencia);	
			}else if(this instanceof Barra) {
				double [] costosMarginales = new double[this.getCantPostes()];
				for (int p = 0; p < this.getCantPostes(); p++) {
					costosMarginales[p] = salidaUltimaIter.getDuales().get(this.getCompDesp().generarNombre("demandaPoste", Integer.toString(p)))*Constantes.SEGUNDOSXHORA/this.getDuracionPostes(p);
				}
				resIter.getResultadoCMg().put(this.getNombre(), costosMarginales);
			}		
		}
	}

	
	public Barra barraDelParticipante() {
		Participante p = this;
		Barra b = null;
		if(this instanceof Generador){
			Generador p1=(Generador) p;
			b = p1.getBarra();
		}else if(p instanceof ImpoExpo) {
			ImpoExpo p1=(ImpoExpo) p;
			b = p1.getBarra();
		}else if(p instanceof Demanda) {
			Demanda p1 = (Demanda)p;
			b = p1.getBarra(); 
		}else if(p instanceof Falla) {
			Falla p1 = (Falla)p;
			b = p1.getDemanda().getBarra();
		}else if(p instanceof Acumulador) {
			Acumulador p1 = (Acumulador)p;
			b = p1.getBarra();
		}
		return b;	
		
	}

	public SimuladorPaso getSimPaso() {
		return simPaso;
	}

	public OptimizadorPaso getOptimPaso() {
		return optimPaso;
	}

	public void setSimPaso(SimuladorPaso simPaso) {
		this.simPaso = simPaso;
	}

	public void setOptimPaso(OptimizadorPaso optimPaso) {
		// TODO BORRAR ESTO
		System.out.println(this.getNombre());
		this.optimPaso = optimPaso;
	}

	/***
	* METODOS USADOS EN LA OPTIMIZACIóN
	***/

	/**
	 * @param instante es el instante inicial del paso
	 */
	public void cargarDatosCompDespachoOptim(long instante) {
		if (compSimulacion != null && compGeneral != null)
			this.compSimulacion.cargarDatosCompDespachoOptim(instante);
	}

	public void actualizarVariablesCompDespachoOptim(int iter) {
		if (compSimulacion != null && compGeneral != null)
			this.compSimulacion.actualizarVariablesCompDespachoOptim(iter);

	}

	public void actualizarOtrosDatosIniciales() {
		this.compSimulacion.actualizarOtrosDatosIniciales();
	}

	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		double costo = this.compSimulacion.calculaCostoPaso(salidaUltimaIter);
		return costo;
	}

	public abstract ArrayList<ProcesoEstocastico> getProcesosOptim();

	public boolean isAportanteEstado() {
		return aportanteEstado;
	}

	public void setAportanteEstado(boolean aportanteEstado) {
		this.aportanteEstado = aportanteEstado;
	}

	public LineaTiempo getLt() {
		return lt;
	}

	public void setLt(LineaTiempo lt) {
		this.lt = lt;
	}

	public boolean isUltimoInfactible() {
		return ultimoInfactible;
	}

	public void setUltimoInfactible(boolean ultimoInfactible) {
		this.ultimoInfactible = ultimoInfactible;
	}
	
	

	public ArrayList<Impacto> getImpactosQueLoInvolucran() {
		return impactosQueLoInvolucran;
	}


	public void setImpactosQueLoInvolucran(ArrayList<Impacto> impactosQueLoInvolucran) {
		this.impactosQueLoInvolucran = impactosQueLoInvolucran;
	}


	public String devuelveCompValorBellman(){
		if(optimPaso!=null){
			return optimPaso.getCorrida().getCompBellman();
		}else if(simPaso!=null){
			return simPaso.getCorrida().getCompBellman();
		}
		return "";
	}
	
	public Corrida devuelveCorrida(){
		if(optimPaso!=null){
			return optimPaso.getCorrida();
		}else if(simPaso!=null){
			return simPaso.getCorrida();
		}
		return null;
	}

	/**
	 * Funciones para el manejo de impactos
	 * @param i
	 * @param costo
	 */
	
	public abstract void aportarImpacto(Impacto i,DatosObjetivo costo );

	public abstract Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter);

	public abstract Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto);


	
	
	/**
	 * MÉTODOS PARA EL DESPACHO DE CORTO PLAZO Y PROGRAMACIÓN ESTOCÁSTICA 
	 */
	
	/**
	 * Construye el CompDespPE de cada participante hijo en particular
	 * con lo que carga en él el participante hijo y su comportamiento despacho
	 */
	public abstract void crearCompDespPE();
	
}
