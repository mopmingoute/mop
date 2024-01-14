/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Azar is part of MOP.
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

import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import datatypes.DatosPronostico;
import datatypesProcEstocasticos.DatosPEBootstrapDiscreto;
import datatypesProcEstocasticos.DatosPECronicas;
import datatypesProcEstocasticos.DatosPEDemandaAnioBase;
import datatypesProcEstocasticos.DatosPEDemandaEscenarios;
import datatypesProcEstocasticos.DatosPEEscenarios;
import datatypesProcEstocasticos.DatosPEEvolucion;
import datatypesProcEstocasticos.DatosPEHistorico;
import datatypesProcEstocasticos.DatosPEMarkov;
import datatypesProcEstocasticos.DatosPEVAR;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import estado.VariableEstado;
import persistencia.CargadorPEBootstrapDiscreto;
import persistencia.CargadorPECronicas;
import persistencia.CargadorPEDemandaAnioBase;
import persistencia.CargadorPEEscenarios;
import persistencia.CargadorPEEvolucion;
import persistencia.CargadorPEHistorico;
import persistencia.CargadorPEMarkov;
import persistencia.CargadorPEMarkovAmpliado;
import persistencia.CargadorPEVAR;
import persistencia.CargadorPEDemandaEscenarios;
import procesosEstocasticos.DiscretoExhaustivo;
import procesosEstocasticos.Estimacion;
import procesosEstocasticos.ProcesoBootstrapDiscreto;
import procesosEstocasticos.ProcesoCronicas;
import procesosEstocasticos.ProcesoDemandaAnioBase;
import procesosEstocasticos.ProcesoDemandaEscenarios;
import procesosEstocasticos.ProcesoEscenarios;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.ProcesoHistorico;
import procesosEstocasticos.ProcesoMarkov;
import procesosEstocasticos.ProcesoMarkovAmpliado;
import procesosEstocasticos.ProcesoPorEvolucion;
import procesosEstocasticos.ProcesoVAR;
import procesosEstocasticos.Pronostico;
import procesosEstocasticos.Semilla;
import procesosEstocasticos.VariableAleatoria;
import tiempo.Evolucion;

/**
 * Clase encargada de condensar todo lo vinculado al modelo estocóstico
 * 
 * @author ut602614
 *
 */

public class Azar {
	private ArrayList<ProcesoEstocastico> procesos; // proceso estocósticos que
													// se usan en la simulación
	private ArrayList<ProcesoEstocastico> procesosOptim; // procesos
															// estocósticos que
															// se usan en la
															// optimización
	
	/**
	 * Colecci�n de estimaciones. Una estimaci�n es el proceso externo al MOP
	 * en el que se constuyen los datos de la pareja (proceso de simulación, proceso de optimización)
	 * que contiene las variables aleatorias de uno o m�s participantes.
	 * OJO QUE PASA CON PROCESOS EXOGENOS QUE NO ESTAN ASOCIADOS A PARTICIPANTES
	 */
	private Hashtable<String, Estimacion> estimaciones;
	
	/**
	 * Procesos que se usan solo en simulación y requieren un proceso asociado 
	 * en la optimizaci�n y un AgregadorDeEstados
	 */


	private ArrayList<DiscretoExhaustivo> procesosDE;
	private ArrayList<DiscretoExhaustivo> procesosDEOptim;

	private ArrayList<ProcesoEstocastico> procesosNODE;
	private ArrayList<ProcesoEstocastico> procesosNODEOptim;

	private ArrayList<VariableEstado> varsEstadoPE;
	private ArrayList<VariableEstado> varsEstadoPEDE;
	private ArrayList<VariableEstado> varsEstadoPENODE;
	private Hashtable<String, ProcesoEstocastico> procesosPorNombre;

	private Semilla semillaGeneral; // Pedazo de semilla que se incluiró en
									// todas las semillas de todos los PEs
	private GregorianCalendar inicioSorteos; // Es la fecha a partir de la cuól
												// se inicializa la semilla y se
												// comienzan a simular los
												// procesos estocósticos
	private GregorianCalendar inicioCorrida;

	private Corrida corrida;
	// naturaleza lista de iniciadores (nómeros, letras, etc.) para
	// ProcesoEstocóstico
	
	
	/**
	 * Procesos que tienen alguna VA ex�gena (que se sortea en otro PE) necesaria
	 * para producir la realizaci�n. Requieren una pasada para enganchar esos procesos.
	 */
	private ArrayList<ProcesoEstocastico> procesosConVAExogenas;
	

	public Azar(Semilla semillaGeneral, GregorianCalendar inicioSorteos, GregorianCalendar inicioCorrida,
			ArrayList<ProcesoEstocastico> procs, Corrida corrida) {
		super();
		this.inicioSorteos = inicioSorteos;
		this.inicioCorrida = inicioCorrida;
		this.procesos = procs;
		this.semillaGeneral = semillaGeneral;
		this.procesosOptim = new ArrayList<ProcesoEstocastico>();
		this.procesosDEOptim = new ArrayList<DiscretoExhaustivo>();
		this.procesosNODEOptim = new ArrayList<ProcesoEstocastico>();
		this.procesosDE = new ArrayList<DiscretoExhaustivo>();
		this.procesosNODE = new ArrayList<ProcesoEstocastico>();
		this.estimaciones = new Hashtable<String, Estimacion>();
		this.setVarsEstadoPE(new ArrayList<VariableEstado>());
		this.setVarsEstadoPEDE(new ArrayList<VariableEstado>());
		this.setVarsEstadoPENODE(new ArrayList<VariableEstado>());
		this.corrida = corrida;

	}
	
	
	public void inicializarAzarParaSimulacion(){
		
		procesosDE.clear();
		procesosNODE.clear();
		varsEstadoPE.clear();
		varsEstadoPENODE.clear();
		varsEstadoPEDE.clear();
		

		for (ProcesoEstocastico pe : procesos) {
			pe.setOptim(false);
			pe.cargaSentidoEvoluciones(1);
			if (pe.isDiscretoExhaustivo()) {
				DiscretoExhaustivo ped = (DiscretoExhaustivo) pe;
				procesosDE.add(ped);
				varsEstadoPEDE.addAll(pe.getVarsEstado());
			} else {
				procesosNODE.add(pe);
				varsEstadoPENODE.addAll(pe.getVarsEstado());
			}
			varsEstadoPE.addAll(pe.getVarsEstado());
		}
		Collections.sort(procesos);
		
		
	}

	/**
	 * Se inicializan todos los procesos para un escenario
	 * 
	 * @param escenario es el nómero de escenario
	 */
	public void inicializarParaUnEscenario(int escenario) {
		for (ProcesoEstocastico p : procesos) {
			// A ESTE MóTODO HAY QUE AGREGARLE EL PARóMETRO ENCADENADO
			// CADA PROCESO DECIDE
			p.inicializar(semillaGeneral, inicioSorteos, inicioCorrida, escenario);	
		}
	}

	public void inicializarAzarParaOptimizacion() {
		for (ProcesoEstocastico p : procesos) {
			//el escenario en optimización no tiene sentido considerarlo, por lo tanto ponemos 0
			p.setOptim(true);
			p.cargaSentidoEvoluciones(-1);
			p.inicializar(semillaGeneral, inicioSorteos, inicioCorrida, 0);
			int cantAnios = corrida.getLineaTiempo().getInstInicioAnio().length;			
			p.setIndAnioLlamadaAnterior(cantAnios-1);
			p.setAnioLlamadaAnterior(this.getCorrida().getLineaTiempo().getAnioFin());
		}
		Collections.sort(procesos);
		procesosNODE.clear();
		procesosDE.clear();
		varsEstadoPE.clear();
		varsEstadoPENODE.clear();
		varsEstadoPEDE.clear();


		for (ProcesoEstocastico pe : procesos) {
			if (pe.isDiscretoExhaustivo()) {
				DiscretoExhaustivo ped = (DiscretoExhaustivo) pe;
				procesosDE.add(ped);
				varsEstadoPEDE.addAll(pe.getVarsEstado());
			} else {
				procesosNODE.add(pe);
				varsEstadoPENODE.addAll(pe.getVarsEstado());
			}

			varsEstadoPE.addAll(pe.getVarsEstado());
		}
	}


	public void sortearVEProcEstDE(long instante) {
		for (DiscretoExhaustivo pde : procesosDE) {
			ProcesoEstocastico proc = (ProcesoEstocastico) pde;
			if(proc.isUsoSimulacion()) proc.producirRealizacion(instante);
		}
	}

	/**
	 * Sortea los procesos estocósticos no discretos exhaustivos que no son muestreados
	 * y que se usan en la simulación
	 * @param instante
	 */
	public void sortearProcEstNODE(long instante) {
		for (ProcesoEstocastico pnde : procesosNODE) {
			if(!pnde.isMuestreado() && pnde.isUsoSimulacion()) pnde.producirRealizacion(instante);
		}
	}


	
	public void agregarPE(DatosProcesoEstocastico dpe, Evolucion<Integer> cantSortMont) {
		if (!existeProcesoEstocastico(dpe.getNombre())) {
			// REVISAR NOMBRES, SACAR SEMANAL...
			ProcesoEstocastico peh = null;
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.HISTORICO)) {
				DatosPEHistorico dpeh = CargadorPEHistorico.devuelveDatosPEHistorico(dpe);
				dpeh.getDatGen().setMuestreado(dpe.getMuestreado());
				peh = new ProcesoHistorico(dpeh);
				corrida.getAportantesEstado().add((AportanteEstado) peh);
				peh.setAportanteEstado(true);

			}
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.MARKOV)) {
				DatosPEMarkov dpeh = CargadorPEMarkov.devuelveDatosPEMarkov(dpe);
				dpeh.getDatGen().setMuestreado(dpe.getMuestreado());
				peh = new ProcesoMarkov(dpeh);
				corrida.getAportantesEstado().add((AportanteEstado) peh);
				corrida.getEstudio().getAzar().getProcesosDE().add((DiscretoExhaustivo) peh);
				peh.setAportanteEstado(true);
			}
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.MARKOV_AMPLIADO)) {
				DatosPEMarkov dpeh = CargadorPEMarkovAmpliado.devuelveDatosPEMarkovAmpliado(dpe);
				dpeh.getDatGen().setMuestreado(dpe.getMuestreado());
				peh = new ProcesoMarkovAmpliado(dpeh);
				corrida.getAportantesEstado().add((AportanteEstado) peh);
				corrida.getEstudio().getAzar().getProcesosDE().add((DiscretoExhaustivo) peh);
				peh.setAportanteEstado(true);
			}			
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.POR_ESCENARIOS)) {
				DatosPEEscenarios dpeh = CargadorPEEscenarios.devuelveDatosPEEscenarios(dpe);
				dpeh.getDatGen().setMuestreado(dpe.getMuestreado());
				peh = new ProcesoEscenarios(dpeh);
//				corrida.getAportantesEstado().add((AportanteEstado) peh);
				peh.setAportanteEstado(false);
			}
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.POR_CRONICAS)) {
				DatosPECronicas dpeh = CargadorPECronicas.devuelveDatosPECronicas(dpe);
				dpeh.getDpEsc().getDatGen().setMuestreado(dpe.getMuestreado());
				peh = new ProcesoCronicas(dpeh);
//				corrida.getAportantesEstado().add((AportanteEstado) peh);
				peh.setAportanteEstado(false);
			}
			
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.BOOTSTRAP_DISCRETO)) {
				DatosPEBootstrapDiscreto dpeh = CargadorPEBootstrapDiscreto.devuelveDatosPEBootstrap(dpe);
				dpeh.getDatGen().setMuestreado(dpe.getMuestreado());
				peh = new ProcesoBootstrapDiscreto(dpeh);
//				corrida.getAportantesEstado().add((AportanteEstado) peh);
				peh.setAportanteEstado(false);
			}	
			
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.DEMANDA_ANIO_BASE)) {
				DatosPEDemandaAnioBase dpeh = CargadorPEDemandaAnioBase.devuelveDatosPEDemandaBase(dpe);
				dpeh.setMuestreado(dpe.getMuestreado());
				peh = new ProcesoDemandaAnioBase(dpeh,this,0,0); 
//				corrida.getAportantesEstado().add((AportanteEstado) peh);
				peh.setAportanteEstado(false);
			}
			
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.DEMANDA_ESCENARIOS)) {
				DatosPEDemandaEscenarios dpeh = CargadorPEDemandaEscenarios.devuelveDatosPEDemandaEscenarios(dpe);
				dpeh.setMuestreado(dpe.getMuestreado());
				peh = new ProcesoDemandaEscenarios(dpeh); 
//				corrida.getAportantesEstado().add((AportanteEstado) peh);
				peh.setAportanteEstado(false);
			}	
			
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.POREVOLUCION)) {
				DatosPEEvolucion dpeh = CargadorPEEvolucion.devuelveDatosPEEvolucion(dpe);
				dpeh.setMuestreado(dpe.getMuestreado());
				peh = new ProcesoPorEvolucion(dpeh);
//				corrida.getAportantesEstado().add((AportanteEstado) peh);
				peh.setAportanteEstado(false);
			}			
			
			if (dpe.getTipo().equalsIgnoreCase(utilitarios.Constantes.VAR)) {
				DatosPEVAR dpeh = CargadorPEVAR.devuelveDatosPEVar(dpe);
				dpeh.setMuestreado(dpe.getMuestreado());
				peh = new ProcesoVAR(dpeh); 
				corrida.getAportantesEstado().add((AportanteEstado) peh);
				peh.setAportanteEstado(true);		
			}							
			if (dpe.isDiscretoExhaustivo()) {
				peh.setDiscretoExhaustivo(true);
				this.procesosDE.add((DiscretoExhaustivo) peh);
				this.varsEstadoPEDE.addAll(peh.getVarsEstado());
			} else {
				peh.setDiscretoExhaustivo(false);
				this.procesosNODE.add(peh);
				this.varsEstadoPENODE.addAll(peh.getVarsEstado());
			}
			cargarEstimacion(peh);
			long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
			peh.setCantSorteos(cantSortMont.getValor(instanteActual));
			peh.setEstadosIniciales(dpe.getEstadosIniciales());
			Hashtable<String, DatosPronostico> h = dpe.getPronosticos();
			for(DatosPronostico dp: h.values()) {
				Pronostico pr = new Pronostico(dp);
				peh.getPronosticos().put(pr.getNombreVA(), pr);
			}
			peh.setAzar(this);
			this.procesos.add(peh);
			this.varsEstadoPE.addAll(peh.getVarsEstado());

		}
	}
	
	private boolean existeProcesoEstocastico(String nombre) {
		for (ProcesoEstocastico pe : procesos) {
			if (pe.getNombre().equalsIgnoreCase(nombre))
				return true;
		}
		return false;
	}

	public void cargarPEstocasticos(Hashtable<String, DatosProcesoEstocastico> procesosEstocasticos, Evolucion<Integer> cantSortMont) {
		Set<String> claves = procesosEstocasticos.keySet();
		Iterator<String> it = claves.iterator();

		while (it.hasNext()) {
			agregarPE(procesosEstocasticos.get(it.next()), cantSortMont);

		}
	}
	
	/**
	 * Construye las Estimaciones y carga el proceso asociado en optimización
	 * de los procesos que se usan en simulación
	 * @param pe
	 */
	public void cargarEstimacion(ProcesoEstocastico pe){
		String nombreNuevaEst = pe.getNombreEstimacion();
		if(!estimaciones.contains(nombreNuevaEst)){
			Estimacion est = new Estimacion();
			if(pe.isUsoSimulacion()){
				est.setProcSim(pe);
				est.setAgregador(pe.getAgregadorEstados());
			}
			if(pe.isUsoOptimizacion()) est.setProcOptim(pe);
			estimaciones.put(nombreNuevaEst, est);
		}else{
			// ya existe la estimación y aparece un nuevo proceso con ella
			Estimacion est = estimaciones.get(nombreNuevaEst);
			if(pe.isUsoSimulacion()){
				if(est.getProcSim()==null){
					est.setProcSim(pe);
					est.setAgregador(pe.getAgregadorEstados());					
				}else if(est.getProcSim()!=pe){
					// ya hay un proceso de simulaci�n y es distinto de pe: error
					System.out.println("El proceso " + pe.getNombre() + "de simulacion pertenece a la estimacion " + est.getNombre() +
							" que ya tiene otro proceso de simulaci�n, el proceso " + est.getProcSim().getNombre());
				}
				// si el proceso de simulacion no es null y es el propio pe no se hace nada
			}
			if(pe.isUsoOptimizacion()){
				if(est.getProcOptim()==null){
					est.setProcOptim(pe);		
				}else if(est.getProcOptim()!=pe){
					// ya hay un proceso de optimizaci�n y es distinto de pe: error
					System.out.println("El proceso " + pe.getNombre() + "de optimizacion pertenece a la estimacion " + est.getNombre() +
							" que ya tiene otro proceso de optimizaci�n, el proceso " + est.getProcOptim().getNombre());
				}
				// si el proceso de optimizacion no es null y es el propio pe no se hace nada
			}				
		}	
	}
	
	/**
	 * Si corresponde, carga en cada PE los objetos PE asociados en la optimizaci�n y ex�genos (con sus VA)
	 */
	public void vincularAsocOptimYExogenos(){
		this.procesosPorNombre = new Hashtable<String, ProcesoEstocastico>();
		for(ProcesoEstocastico pe: procesos){
			if(pe.getNombre()!=null)
			procesosPorNombre.put(pe.getNombre(), pe);
		}
		for(ProcesoEstocastico pe: procesos){
			if(pe.isUsoSimulacion() && !pe.isUsoOptimizacion() && pe.getCantVE()>0){
				pe.setProcAsociadoEnOptim(procesosPorNombre.get(pe.getNombreProcAsociadoEnOptim()));;
			}
			if(pe.isTieneVAExogenas()){
				for(int np=0; np<pe.getNombresProcesosExogenas().size(); np++){
					ProcesoEstocastico pee = procesosPorNombre.get(np);
					String nva = pe.getNombresVAExogenas().get(np);
					VariableAleatoria vae = pee.devuelveVADeNombre(nva);
					pe.getVarsExogenas().add(vae);
					pe.getProcesosVarsExogenas().add(pee);
				}				
			}			
		}		
	}
	
	public ProcesoEstocastico damePEstocastico(String nombre) {
		for (ProcesoEstocastico p : this.getProcesos()) {
			if (p.getNombre().equalsIgnoreCase(nombre))
				return p;
		}

		return null;
	}

	public ProcesoEstocastico devuelveProcesoDeNombre(String nombreProcesoOrigen) {
		return procesosPorNombre.get(nombreProcesoOrigen);
	}

	public Corrida getCorrida() {
		return corrida;
	}

	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}

	public void agregarPE(ProcesoEstocastico pe) {
		this.procesos.add(pe);

	}

	public void agregarPEOptim(ProcesoEstocastico procOptim) {
		/**
		 * TODO: EFICIENCIA- VER SI LOS PROCESOS NO SE CARGAN DUPLICADOS O SI SE SORTEAN PROCESOS QUE NO SE USAN
		 */
		if(!procesosOptim.contains(procOptim)){
			this.procesosOptim.add(procOptim);
			if (procOptim.isDiscretoExhaustivo()) {
				this.procesosDEOptim.add((DiscretoExhaustivo) procOptim);
			} else {
				this.procesosNODEOptim.add(procOptim);
			}
		}

	}

	public void agregarPEsOptim(ArrayList<ProcesoEstocastico> procesosOptim2) {
		if (procesosOptim2 != null) {
			for (ProcesoEstocastico proc : procesosOptim2) {
				this.agregarPEOptim(proc);
			}
		}
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

	public Semilla getSemillaGeneral() {
		return semillaGeneral;
	}

	public void setSemillaGeneral(Semilla semillaGeneral) {
		this.semillaGeneral = semillaGeneral;
	}

	public ArrayList<VariableEstado> getVarsEstadoPE() {
		return varsEstadoPE;
	}

	public void setVarsEstadoPE(ArrayList<VariableEstado> varsEstadoPE) {
		this.varsEstadoPE = varsEstadoPE;
	}

	public ArrayList<VariableEstado> getVarsEstadoPEDE() {
		return varsEstadoPEDE;
	}

	public void setVarsEstadoPEDE(ArrayList<VariableEstado> varsEstadoPEDE) {
		this.varsEstadoPEDE = varsEstadoPEDE;
	}

	public ArrayList<VariableEstado> getVarsEstadoPENODE() {
		return varsEstadoPENODE;
	}

	public void setVarsEstadoPENODE(ArrayList<VariableEstado> varsEstadoPENODE) {
		this.varsEstadoPENODE = varsEstadoPENODE;
	}
	public ArrayList<ProcesoEstocastico> getProcesos() {
		return procesos;
	}

	public void setProcesos(ArrayList<ProcesoEstocastico> procesos) {
		this.procesos = procesos;
	}

	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		return procesosOptim;
	}

	public void setProcesosOptim(ArrayList<ProcesoEstocastico> procesosOptim) {
		this.procesosOptim = procesosOptim;
	}

	public ArrayList<DiscretoExhaustivo> getProcesosDE() {
		return procesosDE;
	}

	public void setProcesosDE(ArrayList<DiscretoExhaustivo> procesosDE) {
		this.procesosDE = procesosDE;
	}

	public ArrayList<DiscretoExhaustivo> getProcesosDEOptim() {
		return procesosDEOptim;
	}

	public void setProcesosDEOptim(ArrayList<DiscretoExhaustivo> procesosDEOptim) {
		this.procesosDEOptim = procesosDEOptim;
	}

	public ArrayList<ProcesoEstocastico> getProcesosNODE() {
		return procesosNODE;
	}

	public void setProcesosNODE(ArrayList<ProcesoEstocastico> procesosNODE) {
		this.procesosNODE = procesosNODE;
	}

	public ArrayList<ProcesoEstocastico> getProcesosNODEOptim() {
		return procesosNODEOptim;
	}

	public void setProcesosNODEOptim(ArrayList<ProcesoEstocastico> procesosNODEOptim) {
		this.procesosNODEOptim = procesosNODEOptim;
	}


}
