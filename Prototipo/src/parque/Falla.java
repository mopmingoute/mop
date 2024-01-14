/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Falla is part of MOP.
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

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.DemandaCompDesp;
import compdespacho.FallaCompDesp;
import compgeneral.FallaComp;
import compsimulacion.FallaCompSim;
import compsimulacion.HidraulicoCompSim;
import control.VariableControlDE;
import cp_compdespProgEst.DemandaCompDespPE;
import cp_compdespProgEst.FallaCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import cp_compdespProgEst.RedCompDespPE;
import cp_compdespProgEst.TermicoCompDespPE;
import datatypes.DatosFallaEscalonadaCorrida;
import datatypes.DatosVariableControlDE;
import datatypes.DatosVariableEstado;
import datatypes.Pair;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import datatypesTiempo.DatosLineaTiempo;
import estado.Discretizacion;
import estado.VariableEstado;
import estado.VariableEstadoPar;
import futuro.AFIncrementos;
import interfacesParticipantes.AportanteControlDE;
import interfacesParticipantes.AportanteEstado;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import utilitarios.Constantes;

/**
 * Clase que representa la falla
 * 
 * @author ut602614
 *
 */

public class Falla extends Participante implements AportanteControlDE, AportanteEstado {

	private ArrayList<Pair<Double, Double>> escalones; // El primer elemento de cada par es la profundidad en
														// por uno de la demanda el segundo el costo USD/MWh
	private int cantEscalones;

	/**
	 * Los primeros cantEscProgram escalones deben estar forzados mediante variables
	 * de control DE para ser despachados en su totalidad, no admiten despacho
	 * parcial. De lo contrario no se despachan. Por ejemplo si cantEscProgram = 2,
	 * los escalones 0 y 1, deber ser forzados para despacharse.
	 * 
	 * En los comportamientos FALLA_CONESTADO_SINDUR y FALLA_CONESTADO_CONDUR, Los
	 * escalones programados que no estón forzados por variables de control DE, no
	 * se despachan en absoluto. Los escalones no programados pueden despacharse
	 * siempre. Es decir que los escalones programados no forzados son los que no
	 * aparecen en el despacho.
	 */
	private int cantEscProgram;

	/**
	 * Duración minima del forzamiento en segundos para cada escalón programado en
	 * el comportamiento con estado con duración. En el momento en que se fuerza un
	 * escalón e, se determina la cantidad de peróodos (NPCDE) de la variable de
	 * control DE durante los cuales se mantiene el forzamiento. NPCDE se calcula
	 * como: NPCDE = durMinForzamientosSeg[e]/periodo donde periodo es el peróodo de
	 * la variable de control DE.
	 * 
	 */
	//private int[] durMinForzamientosSeg;

	/**
	 * Variables de estado y de control DE que se usan sólo son los comportamientos
	 * con estado
	 */
	private VariableEstadoPar cantEscForzados; // cantidad de escalones forzados
	private VariableEstadoPar cantEscForzadosOptim;
	private VariableEstadoPar perForzadosRestantes; // cantidad de peróodos de forzamiento restantes, incluso el paso
													// corriente
	private VariableEstadoPar perForzadosRestantesOptim;
	private VariableControlDE cantEscAForzar; // cantidad de escalones que fuerza el control

	private Demanda demanda;
	private FallaCompSim compS;
	private FallaCompDesp compD;
	private FallaComp compG;
	private static ArrayList<String> atributosDetallados;

	public Falla(DatosFallaEscalonadaCorrida datosFallaEscalonadaCorrida, Demanda d, DatosLineaTiempo lt) {
		this.setNombre(datosFallaEscalonadaCorrida.getNombre());
		this.setEscalones(datosFallaEscalonadaCorrida.getEscalones());
		this.cantEscalones = escalones.size();
		this.setCantEscProgram(datosFallaEscalonadaCorrida.getCantEscProgram());
	//	this.setDurMinForzamientosSeg(datosFallaEscalonadaCorrida.getDurMinForzSeg());

		// Completamos las variables de estado
		if (datosFallaEscalonadaCorrida.getVarsEstado() != null) {
			DatosVariableEstado perForz = datosFallaEscalonadaCorrida.getVarsEstado().get("perForzadosRestantes");
			if (perForz != null) {

				this.perForzadosRestantes = new VariableEstadoPar(perForz, lt, getNombre());
				this.perForzadosRestantes.setParticipante(this);
				this.perForzadosRestantesOptim = new VariableEstadoPar(perForz, lt, getNombre());
				this.perForzadosRestantesOptim.setParticipante(this);
			}
			// Hay que reconstruir la discretización de la variable de estado
		}
		if (datosFallaEscalonadaCorrida.getVarsEstado() != null) {
			DatosVariableEstado cantEscF = datosFallaEscalonadaCorrida.getVarsEstado().get("cantEscForzados");
			if (cantEscF != null) {
				this.cantEscForzados = new VariableEstadoPar(cantEscF, lt, getNombre());
				this.cantEscForzados.setParticipante(this);
				this.cantEscForzadosOptim = new VariableEstadoPar(cantEscF, lt, getNombre());
				this.cantEscForzadosOptim.setParticipante(this);
			}
			// Hay que reconstruir la discretización de la variable de estado
		}

		if (datosFallaEscalonadaCorrida.getVarsControlDE() != null) {
			DatosVariableControlDE cantEscAF = datosFallaEscalonadaCorrida.getVarsControlDE().get("cantEscAForzar");
			if (cantEscAF != null) {
				this.cantEscAForzar = new VariableControlDE(cantEscAF);
				this.cantEscForzados.setParticipante(this);
			}
			ArrayList<Double> valores = new ArrayList<Double>();
			for (int i = 0; i <= cantEscProgram; i++) {
				valores.add((double) i);
			}
			Discretizacion dis = new Discretizacion(cantEscAForzar, valores, false);
			Evolucion<Discretizacion> evolDis = new EvolucionConstante<Discretizacion>(dis, lt.getSentido());
			cantEscAForzar.setEvolDiscretizacion(evolDis);
		}

		demanda = d;

		compD = new FallaCompDesp(this);
		compG = new FallaComp(this, compD, compS);
		compS = new FallaCompSim(this, compD, compG);
		compG.setCompS(compS);
		compG.setEvolucionComportamientos(datosFallaEscalonadaCorrida.getValsComps());
		compS.setCompgeneral(compG);
		compS.setCompdespacho(compD);
		compG.setCompSimulacion(compS);
		compG.setCompDespacho(compD);
		this.setCompDesp(compD);
		this.setCompGeneral(compG);
		this.setCompSimulacion(compS);
		compD.setParticipante(this);
		compS.setParticipante(this);
		compG.setParticipante(this);
	}

	/**
	 * Devuelve la potencia móxima de la falla en el poste p y escalon e
	 * 
	 * @param p
	 * @param e
	 * @return
	 */
	public Double damePotenciaPosteEscalon(int p, int e) {
		Pair<Double, Double> par = getEscalones().get(e);
		Double poruno = par.first / 100;
		Double potActiva = dameComportamientoDemanda().getPotActivaPorPoste()[p];
		return poruno * potActiva;

	}

	/**
	 * Devuelve el costo unitario en el poste p escalón e en USD/MJ
	 * 
	 * @param p
	 * @param e
	 * @return
	 */
	public Double dameCostoUnitarioPosteEscalon(int p, int e) {
		return getEscalones().get(e).second;
	}

	public ArrayList<Pair<Double, Double>> getEscalones() {
		return escalones;
	}

	public void setEscalones(ArrayList<Pair<Double, Double>> escalones) {
		this.escalones = escalones;
	}

	public Integer getCantEscalones() {
		return cantEscalones;
	}

	public void setCantEscalones(Integer cantEscalones) {
		this.cantEscalones = cantEscalones;
	}

	public int getCantEscProgram() {
		return cantEscProgram;
	}

	public void setCantEscProgram(int cantEscProgram) {
		this.cantEscProgram = cantEscProgram;
	}

	public FallaCompSim getCompS() {
		return compS;
	}

	public void setCompS(FallaCompSim compS) {
		this.compS = compS;
	}

	public FallaCompDesp getCompD() {
		return compD;
	}

	public void setCompD(FallaCompDesp compD) {
		this.compD = compD;
	}

	public FallaComp getCompG() {
		return compG;
	}

	public void setCompG(FallaComp compG) {
		this.compG = compG;
	}

	public void setCantEscalones(int cantEscalones) {
		this.cantEscalones = cantEscalones;
	}

	public Demanda getDemanda() {
		return demanda;
	}

	public void setDemanda(Demanda demanda) {
		this.demanda = demanda;
	}

	public DemandaCompDesp dameComportamientoDemanda() {
		return (DemandaCompDesp) this.demanda.getCompDesp();
	}

	@Override
	public void inicializarParaEscenario() {
		this.getCompSimulacion().inicializarParaEscenario();

	}

	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		Falla.atributosDetallados = atributosDetallados;
	}

	public double[][] dameProf(double[] potActivaPorPoste) {
		double[][] retorno = new double[potActivaPorPoste.length][this.getCantEscalones()];
		for (int p = 0; p < potActivaPorPoste.length; p++) {
			for (int e = 0; e < this.getCantEscalones(); e++) {
				retorno[p][e] = potActivaPorPoste[p] * this.getEscalones().get(e).first / 100;
			}
		}

		return retorno;
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {

//  Deliberadamente en blanco debido a que los resultados de la falla salen en guardarResultados de la demanda

	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void asignaVAOptim() {
		// Deliberadamente en blanco
	}

	@Override
	public void asignaVASimul() {
		// Deliberadamente en blanco
	}
/*
	public int[] getDurMinForzamientosSeg() {
		return durMinForzamientosSeg;
	}

	public void setDurMinForzamientosSeg(int[] durMinForzamientosSeg) {
		this.durMinForzamientosSeg = durMinForzamientosSeg;
	}*/

	public VariableEstadoPar getCantEscForzados() {
		return cantEscForzados;
	}

	public void setCantEscForzados(VariableEstadoPar cantEscForzados) {
		this.cantEscForzados = cantEscForzados;
	}

	public VariableEstadoPar getPerForzadosRestantes() {
		return perForzadosRestantes;
	}

	public void setPerForzadosRestantes(VariableEstadoPar perForzadosRestantes) {
		this.perForzadosRestantes = perForzadosRestantes;
	}

	public VariableControlDE getCantEscAForzar() {
		return cantEscAForzar;
	}

	public void setCantEscAForzar(VariableControlDE cantEscAForzar) {
		this.cantEscAForzar = cantEscAForzar;
	}

	@Override
	public void actualizarVarsControlDE() {
		String compGenFalla = this.compS.getValsCompGeneral().get(Constantes.COMPFALLA);
		this.compG.getVarsControlDE().clear();
		if (!compGenFalla.equalsIgnoreCase(Constantes.FALLASINESTADO)) {
			this.compG.getVarsControlDE().add(cantEscAForzar);
		}
	}

	@Override
	public ArrayList<VariableControlDE> aportarVarsControlDE() {
		return this.compG.getVarsControlDE();
	}

	@Override
	public void cargaControlesDEFactibles() {
		cantEscAForzar.getControlesFactibles().clear();
		String compGenFalla = this.compS.getValsCompGeneral().get(Constantes.COMPFALLA);
		if (compGenFalla.equalsIgnoreCase(Constantes.FALLA_CONESTADO_SINDUR)) {
			for (int i = 0; i <= cantEscProgram; i++) {
				cantEscAForzar.getControlesFactibles().add(i);
			}
		} else if (compGenFalla.equalsIgnoreCase(Constantes.FALLA_CONESTADO_CONDUR)) {
			int rest = perForzadosRestantes.getEstado().intValue();
			// Si hay peróodos forzados restantes no se puede hacer nada, el ArrayList queda
			// vacóo
			if (rest == 0) {
				for (int i = 0; i <= cantEscProgram; i++) {
					cantEscAForzar.getControlesFactibles().add(i);
				}
			}
		}
	}

	@Override
	public void actualizarVarsEstadoSimulacion() {
		compG.actualizarVarsEstadoSimulacion();

	}

	@Override
	public void actualizarVarsEstadoOptimizacion() {
		compG.actualizarVarsEstadoOptimizacion();

	}

	@Override
	public void contribuirAS0fint() {
		FallaCompSim fcs = (FallaCompSim) this.getCompSimulacion();
		fcs.contribuirAS0fint();
	}

	@Override
	public void contribuirAS0fintOptim() {
		FallaCompSim fcs = (FallaCompSim) this.getCompSimulacion();
		fcs.contribuirAS0fintOptim();
	}

	@Override
	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
		return compG.getVarsEstadoSimulacion();
	}

	@Override
	public ArrayList<VariableEstado> aportarEstadoOptimizacion() {
		return compG.getVarsEstadoOptimizacion();
	}

	@Override
	public void cargarValVEOptimizacion() {
		compG.cargarValVEOptimizacion();

	}

	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos aproxFuturaOpt) {
		// Deliberadamente en blanco
	}

	@Override
	public void cargarValRecursoVESimulacion() {
		// Deliberadamente en blanco
	}

	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		if (varsControlDE.contains(cantEscAForzar)) {
			cantEscForzados.setEstadoDespuesDeCDE(cantEscAForzar.getControl());
		} else {
			cantEscForzados.setEstadoDespuesDeCDE(cantEscForzados.getEstado());
		}
	}

	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		if (varsControlDE.contains(cantEscAForzar)) {
			cantEscForzadosOptim.setEstadoDespuesDeCDE(cantEscAForzar.getControl());
		} else {
			cantEscForzadosOptim.setEstadoDespuesDeCDE(cantEscForzadosOptim.getEstado());
		}
	}

	@Override
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla) {
		// Deliberadamente en blanco

	}

	@Override
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		cantEscForzadosOptim.setEstadoFinalOptim(cantEscForzadosOptim.getEstadoDespuesDeCDE());
	}

	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// Deliberadamente en blanco
		return 0;
	}

	public VariableEstadoPar getCantEscForzadosOptim() {
		return cantEscForzadosOptim;
	}

	public void setCantEscForzadosOptim(VariableEstadoPar cantEscForzadosOptim) {
		this.cantEscForzadosOptim = cantEscForzadosOptim;
	}

	public VariableEstadoPar getPerForzadosRestantesOptim() {
		return perForzadosRestantesOptim;
	}

	public void setPerForzadosRestantesOptim(VariableEstadoPar perForzadosRestantesOptim) {
		this.perForzadosRestantesOptim = perForzadosRestantesOptim;
	}

	@Override
	public ArrayList<VariableEstado> getVarsEstado() {
		ArrayList<VariableEstado> ret = new ArrayList<VariableEstado>();
		ret.add(cantEscForzados);
		ret.add(this.perForzadosRestantes);
		return ret;

	}

	public ArrayList<VariableEstado> getVarsEstadoOptim() {
		ArrayList<VariableEstado> ret = new ArrayList<VariableEstado>();
		ret.add(cantEscForzadosOptim);
		ret.add(this.perForzadosRestantesOptim);
		return ret;

	}

	@Override
	public void aportarImpacto(Impacto i, DatosObjetivo costo) {
		// TODO Auto-generated method stub

	}

	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void actualizaValoresVEDiscretizacionesVariables(long instante) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearCompDespPE() {
		FallaCompDespPE compDespPE = new FallaCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}
	
	

}
