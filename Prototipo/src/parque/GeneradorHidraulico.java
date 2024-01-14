/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorHidraulico is part of MOP.
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

// import interfacesParticipantes.Actualizable;
import interfacesParticipantes.AportanteEstado;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import procesosEstocasticos.VariableAleatoriaEntera;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.Par;
import utilitarios.Polinomio;
import utilitarios.Recta;
import compdespacho.HidraulicoCompDesp;
import compgeneral.HidraulicoComp;
import compsimulacion.HidraulicoCompSim;
import control.VariableControlDE;
import cp_compdespProgEst.AcumuladorCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import cp_compdespProgEst.HidraulicoCompDespPE;
import logica.CorridaHandler;
import persistencia.CargadorFuncionesPQ;
import persistencia.CargadorPEEscenarios;
import datatypes.DatosHidraulicoCorrida;
import datatypes.DatosVariableEstado;
import datatypes.Pair;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProcEstocasticos.DatosPEEscenarios;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosHidraulicoSP;
import datatypesSalida.DatosSalidaPaso;
import datatypesTiempo.DatosLineaTiempo;
import estado.VariableEstado;
import estado.VariableEstadoPar;
import futuro.AFIncrementos;

/**
 * Clase que representa el generador hidróulico
 * 
 * @author ut602614
 *
 */

// public class GeneradorHidraulico extends Generador implements AportanteEstado, Actualizable{
public class GeneradorHidraulico extends Generador implements AportanteEstado {
	// Aporte
	private ArrayList<GeneradorHidraulico> generadoresArriba;
	/** Colección de generadores hidróulicos inmediatamente aguas arriba */
	private GeneradorHidraulico generadorAbajo;
	/** Generador Hidróulico aguas abajo */
	private ArrayList<GeneradorHidraulico> todosAbajo; // generadores aguas abajo directa e indirectamente

	private HidraulicoCompDesp compD;
	private HidraulicoCompSim compS;
	private HidraulicoComp compG;

	private static ArrayList<String> atributosDetallados;

	private VariableAleatoria aporte; // aporte en m3/s
	private Evolucion<Double> qTur1Max;

	private Evolucion<Double> factorCompartir;
	private Evolucion<Boolean> tieneCaudalMinEcol;
	private Evolucion<ArrayList<Double>> caudalMinEcol; // caudal mónimo ecológico m3/s aplicable a todos los postes
	private Evolucion<Double> penalizacionFaltanteCaudal; // USD/hm3 de penalización

	private double saltoMin;
	private double cotaInundacionAguasAbajo; // cota a la que se inunda la central desde aguas abajo
	private double cotaInundacionAguasArriba; // cota a la que se inunda la central desde aguas arriba

	private Polinomio fCotaAguasAbajo; // recibe erogado en hm3/s y cota aguas arriba de la central aguas abajo en
										// metros
	private Polinomio fCovo; // cota en función del volumen en hm3
	private Polinomio fVoco; // volumen en hm3 en función de la cota
	private Polinomio fQEroMin; // Caudal erogado mónimo obligatorio en m3/s en función de la cota
	private Polinomio fEvaporacion; //
	private Evolucion<Double> coefEvaporacion; // coeficiente mensual multiplicador de la función de evaporación
												// (Hidróulica UTE)
	private Polinomio fFiltracion; //
	private Polinomio fQVerMax; // Caudal móximo vertible en m3/s en función de la cota
	private Evolucion<Double> volFijo; // es el valor que se usa si no hay lago expresado en hm3
	private VariableEstadoPar volumen; // Variable de estado en la simulación expresado en hm3
	private VariableEstadoPar volumenOpt; // Variable de estado en la optimización expresado en hm3
	private double epsilonCaudalErogadoIteracion;

	private String nombreVA; // Nombre de la variable aleatoria aporte
	private Hashtable<Pair<Double, Double>, ArrayList<Recta>> funcionesPQ;
	private Evolucion<Double> volReservaEstrategica; // volumen en hm3 debajo del cual se revaloriza el agua del embalse
	private Evolucion<Double> valorMinReserva; // valor en USD/hm3 mónimo por debajo del volumen anterior. Se aplica
												// como cota inferior al valor del agua
	private boolean valorAplicaOptim; // si es true se usa tamnbión en la optimización, de lo contrario solo en
										// simulación.
	private boolean hayReservaEstrategica;
	private boolean vertimientoConstante;
	private boolean hayVolObjVert;
	private Evolucion<Double> volObjVert; // volumen a partir del cual se eroga todo lo que llega para manteniendo el
											// volumen
	private boolean hayControldeCotasMinimas;
	private Evolucion<Double> volumenControlMinimo;
	private Evolucion<Double> penalidadControlMinimo;
	private boolean hayControldeCotasMaximas;
	private Evolucion<Double> volumenControlMaximo;
	private Evolucion<Double> penalidadControlMaximo;

	/** Constructor del generador hidróulico a partir de sus datos */
	public GeneradorHidraulico(DatosHidraulicoCorrida datosHidraulicoCorrida, DatosLineaTiempo lt) {
		this.setVertimientoConstante(datosHidraulicoCorrida.isVertimientoConstante());
		this.factorCompartir = datosHidraulicoCorrida.getFactorCompartir();
		this.setNombre(datosHidraulicoCorrida.getNombre());
		this.setBarra(CorridaHandler.getInstance().getBarra(datosHidraulicoCorrida.getBarra()));
		this.setCantModInst(datosHidraulicoCorrida.getCantModInst());
		this.setPropietario(datosHidraulicoCorrida.getPropietario());
		this.setDispMedia(datosHidraulicoCorrida.getDispMedia());
		this.settMedioArreglo(datosHidraulicoCorrida.gettMedioArreglo());
		this.setPotenciaMaxima(datosHidraulicoCorrida.getPotMax());
		this.setRendMax(datosHidraulicoCorrida.getRendPotMax());
		this.setRendMin(datosHidraulicoCorrida.getRendPotMin());
		this.setMinimoTecnico(datosHidraulicoCorrida.getPotMin());
		this.setSaltoMin(datosHidraulicoCorrida.getSaltoMin());
		this.setCotaInundacionAguasAbajo(datosHidraulicoCorrida.getCotaInundacionAguasAbajo());
		this.setCotaInundacionAguasArriba(datosHidraulicoCorrida.getCotaInundacionAguasArriba());
		this.setMantProgramado(datosHidraulicoCorrida.getMantProgramado());
		this.setCostoFijo(datosHidraulicoCorrida.getCostoFijo());
		this.setCostoVariable(datosHidraulicoCorrida.getCostoVariable());
		this.setVolReservaEstrategica(datosHidraulicoCorrida.getVolReservaEstrategica());
		this.setValorMinReserva(datosHidraulicoCorrida.getValorMinReserva());
		this.setValorAplicaOptim(datosHidraulicoCorrida.isValorAplicaOptim());
		this.setHayReservaEstrategica(datosHidraulicoCorrida.isHayReservaEstrategica());
		this.setHayVolObjVert(datosHidraulicoCorrida.isHayVolObjVert());
		this.setVolObjVert(datosHidraulicoCorrida.getVolObjVert());
		this.hayControldeCotasMinimas = datosHidraulicoCorrida.isHayControldeCotasMinimas();
		this.volumenControlMinimo = datosHidraulicoCorrida.getVolumenControlMinimo();
		this.penalidadControlMinimo = datosHidraulicoCorrida.getPenalidadControlMinimo();
		this.hayControldeCotasMaximas = datosHidraulicoCorrida.isHayControldeCotasMaximas();
		this.volumenControlMaximo = datosHidraulicoCorrida.getVolumenControlMaximo();
		this.penalidadControlMaximo = datosHidraulicoCorrida.getPenalidadControlMaximo();

		compD = new HidraulicoCompDesp(this);
		compG = new HidraulicoComp(this, compD, compS);
		compS = new HidraulicoCompSim(this, compD, compG);
		compG.setCompS(compS);

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

		compG.setEvolucionComportamientos(datosHidraulicoCorrida.getValoresComportamientos());
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		fCotaAguasAbajo = new Polinomio(datosHidraulicoCorrida.getfCoAA().getValor(instanteActual));

		fCovo = new Polinomio(datosHidraulicoCorrida.getfCoVo().getValor(instanteActual));
		fVoco = new Polinomio(datosHidraulicoCorrida.getfVoCo().getValor(instanteActual));
		fQEroMin = new Polinomio(datosHidraulicoCorrida.getfQEroMin().getValor(instanteActual));
		fEvaporacion = new Polinomio(datosHidraulicoCorrida.getfEvaporacion().getValor(instanteActual));
		this.setCoefEvaporacion(datosHidraulicoCorrida.getCoefEvaporacion());
		fFiltracion = new Polinomio(datosHidraulicoCorrida.getfFiltracion().getValor(instanteActual));
		fQVerMax = new Polinomio(datosHidraulicoCorrida.getfQVerM().getValor(instanteActual));
		CargadorFuncionesPQ.devuelveDatosFuncionPQ(datosHidraulicoCorrida);
		funcionesPQ = datosHidraulicoCorrida.getFuncionesPQ();

//		for (int i =0; i < 100; i++) {
//			System.out.println(this.getNombre() + " Volumen: " + i*100 + " Cota: " + fCovo.dameValor(100*i));
//		}
//		
		this.setqTur1Max(datosHidraulicoCorrida.getqTur1Max());
		this.setVolFijo(datosHidraulicoCorrida.getVolFijo());
		this.setGeneradoresArriba(new ArrayList<GeneradorHidraulico>());

		if (datosHidraulicoCorrida.getVarsEstado() != null) {
			DatosVariableEstado vol = datosHidraulicoCorrida.getVarsEstado().get("volumen");
			if (vol != null) {
				this.volumen = new VariableEstadoPar(datosHidraulicoCorrida.getVarsEstado().get("volumen"), lt,
						getNombre());
				this.volumen.setParticipante(this);
				this.volumenOpt = new VariableEstadoPar(datosHidraulicoCorrida.getVarsEstado().get("volumen"), lt,
						getNombre());
				this.volumenOpt.setParticipante(this);
			}
		}

		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		String nombreD = "Disp-" + this.getNombre();
		PEDisponibilidadGeometrica pedg = new PEDisponibilidadGeometrica(nombreD, this.getNombre(), this.getDispMedia(),
				this.gettMedioArreglo(), this.getCantModInst(), this.getMantProgramado(), null,
				this.getCantModInst().getValor(instanteActual), datosHidraulicoCorrida.getCantModIni(),
				actual.getInstanteInicialPPaso(), actual.getInstanteFinalPPaso());
		int cantDispIni = Math.min(datosHidraulicoCorrida.getCantModIni(),
				datosHidraulicoCorrida.getCantModInst().getValor(instanteActual));
		this.setCantModDisp(new VariableAleatoria());
		getCantModDisp().setValor((double) cantDispIni);
		pedg.setCantDisponibles(this.getCantModDisp());
		for (int i = 0; i < getCantModInst().getValor(instanteActual); i++) {
			VariableAleatoria modI = new VariableAleatoria();
			modI.setNombre(this.getNombre() + "modDisp_" + i);
			modI.setMuestreada(false);
			if (cantDispIni > 0) {
				modI.setValor(1.0);
			} else {
				modI.setValor(0.0);
			}
			pedg.getVariablesAleatorias().add(modI);
			pedg.getNombresVarsAleatorias().add(modI.getNombre());
			cantDispIni--;
		}

		actual.agregarPE(pedg);

		/**
		 * TODO: OJO CANTIDAD DE INNOVACIONES VARIABLES
		 */
		pedg.setCantidadInnovaciones(this.getCantModInst().getValor(instanteActual));
		this.getCantModDisp().setPe(pedg);

		/**
		 * VARIABLE ALEATORIA DE APORTES
		 */
		ProcesoEstocastico proc = actual.dameProcesoEstocastico(datosHidraulicoCorrida.getAporte().getProcSimulacion());
		ProcesoEstocastico procOptim = actual
				.dameProcesoEstocastico(datosHidraulicoCorrida.getAporte().getProcOptimizacion());
		nombreVA = datosHidraulicoCorrida.getAporte().getNombre();

		this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_OPT, procOptim);
		this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_SIM, proc);
		this.chequearProcesosConAsociadoEnOptim(proc, procOptim);

		aporte = new VariableAleatoria(this.getNombre(), false, null, null);

	}

	public boolean isHayControldeCotasMinimas() {
		return hayControldeCotasMinimas;
	}

	public void setHayControldeCotasMinimas(boolean hayControldeCotasMinimas) {
		this.hayControldeCotasMinimas = hayControldeCotasMinimas;
	}

	public Evolucion<Double> getVolumenControlMinimo() {
		return volumenControlMinimo;
	}

	public void setVolumenControlMinimo(Evolucion<Double> volumenControlMinimo) {
		this.volumenControlMinimo = volumenControlMinimo;
	}

	public Evolucion<Double> getPenalidadControlMinimo() {
		return penalidadControlMinimo;
	}

	public void setPenalidadControlMinimo(Evolucion<Double> penalidadControlMinimo) {
		this.penalidadControlMinimo = penalidadControlMinimo;
	}

	public boolean isHayControldeCotasMaximas() {
		return hayControldeCotasMaximas;
	}

	public void setHayControldeCotasMaximas(boolean hayControldeCotasMaximas) {
		this.hayControldeCotasMaximas = hayControldeCotasMaximas;
	}

	public Evolucion<Double> getVolumenControlMaximo() {
		return volumenControlMaximo;
	}

	public void setVolumenControlMaximo(Evolucion<Double> volumenControlMaximo) {
		this.volumenControlMaximo = volumenControlMaximo;
	}

	public Evolucion<Double> getPenalidadControlMaximo() {
		return penalidadControlMaximo;
	}

	public void setPenalidadControlMaximo(Evolucion<Double> penalidadControlMaximo) {
		this.penalidadControlMaximo = penalidadControlMaximo;
	}

	public void setVertimientoConstante(boolean vertidoConstante) {
		this.vertimientoConstante = vertidoConstante;
	}

	private void setVolFijo(Evolucion<Double> volFijo2) {
		this.volFijo = volFijo2;

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
	public void inicializarParaEscenario() {
		this.getCompSimulacion().inicializarParaEscenario();
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
		HidraulicoCompSim hcs = (HidraulicoCompSim) this.getCompSimulacion();
		hcs.contribuirAS0fint();
	}

	public void contribuirAS0fintOptim() {
		HidraulicoCompSim hcs = (HidraulicoCompSim) this.getCompSimulacion();
		hcs.contribuirAS0fintOptim();
	}

	@Override
	public void cargarValVEOptimizacion() {
		compG.cargarValVEOptimizacion();
	}

	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos af) {
		String valCompLago = compS.getValsCompGeneral().get(Constantes.COMPLAGO);
		if (valCompLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			String nomVarEs = this.volumenOpt.getNombre();
			this.volumenOpt.setValorRecurso(af.getIncrementosYDerivadasParciales().get(nomVarEs));
		}

	}

	@Override
	public void cargarValRecursoVESimulacion() {
		String valCompLago = compS.getValsCompGeneral().get(Constantes.COMPLAGO);
		if (valCompLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			this.volumen.setValorRecurso(this.volumenOpt.getValorRecurso());
			this.volumen.setEstado(this.volumenOpt.getEstado());
			this.volumen.setEstadoDespuesDeCDE(this.volumenOpt.getEstado());
		}
	}

	@Override
	public void asignaVAOptim() {
		if (this.getProcesosDelParticipante() != null) {
			ProcesoEstocastico peOptim = this.devuelveProceso(nombreVA, Constantes.FASE_OPT);
			if (peOptim == null) {
				System.out.println(
						"La variable aleatoria " + nombreVA + " no aparece en procesos en " + Constantes.FASE_OPT);
				aporte = new VariableAleatoria();
				aporte.setValor(0.0);
			} else {
				VariableAleatoria vaporte = peOptim.devuelveVADeNombre(nombreVA);
				aporte = vaporte;
			}
		} else {
			aporte = new VariableAleatoria();
			aporte.setValor(0.0);
			if (Constantes.NIVEL_CONSOLA > 1) {
				System.out.println(
						"El generador hidróulico " + this.getNombre() + " no tiene aporte aleatorio en optimización");
			}
		}
	}

	@Override
	public void asignaVASimul() {
		if (this.getProcesosDelParticipante() != null) {
			ProcesoEstocastico peSim = this.devuelveProceso(nombreVA, Constantes.FASE_SIM);
			if (peSim == null) {
				aporte = new VariableAleatoria();
				aporte.setValor(0.0);
			} else {
				VariableAleatoria vaporte = peSim.devuelveVADeNombre(nombreVA);
				if (vaporte == null) {
					aporte = new VariableAleatoria();
					aporte.setValor(0.0);
				} else {
					aporte = vaporte;
				}
			}
		} else {
			aporte = new VariableAleatoria();
			aporte.setValor(0.0);
		}
	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();
		ret.add(this.getCantModDisp().getPe());
		String nombreVA = "APORTE" + "-" + this.getNombre().toUpperCase();
		ProcesoEstocastico peAporteOptim = this.devuelveProceso(nombreVA, Constantes.FASE_OPT);
		if (peAporteOptim != null)
			ret.add(peAporteOptim);
		return ret;

	}

	/**
	 * Como los controles DE no afectan las variables de estado en este participante
	 * se carga el valor de estado en estadoDespuesDeCE
	 * 
	 * @param instInicioPaso
	 * @param varsControlDE
	 */
	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		ArrayList<VariableEstado> varsEst = aportarEstadoSimulacion();
		for (VariableEstado ve : varsEst) {
			// esto es para cuando "crece" el espacio de estado
			if (ve.getEstado() == null) {
				ve.setEstado(ve.getValorInicial());
			}
			ve.setEstadoDespuesDeCDE(ve.getEstado());
		}
	}

	/**
	 * Como los controles DE no afectan las variables de estado en este participante
	 * se carga el valor de estado en estadoDespuesDeCE
	 * 
	 * @param instInicioPaso
	 * @param varsControlDE
	 */
	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		ArrayList<VariableEstado> varsEst = aportarEstadoOptimizacion();
		for (VariableEstado ve : varsEst) {
			if (ve.getEstado() == null) {
				ve.setEstado(ve.getValorInicial());
			}
			ve.setEstadoDespuesDeCDE(ve.getEstado());
		}
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String fase, long instante) {
		double[] potencia = new double[this.getCantPostes()];
		double[] coefEnerg = new double[this.getCantPostes()];
		double[] qturb = new double[this.getCantPostes()];
		double[] salto = new double[this.getCantPostes()];
		double costoPaso = this.compS.calculaCostoPaso(salidaUltimaIter);
		double dualVert = 0.0;

		/**
		 * En la optimización no se usa la VE de simulación volumen, para usarla en la
		 * salida se la carga
		 */
		if (fase.equalsIgnoreCase(Constantes.FASE_OPT)) {
			this.volumen = this.getVolumenOpt();
		}

		for (int p = 0; p < this.getCantPostes(); p++) {
			potencia[p] = salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("pot", Integer.toString(p)));
			qturb[p] = this.compD.getQTur(salidaUltimaIter, p);

			salto[p] = this.compD.calcularSalto();
			coefEnerg[p] = this.compD.getCoefEnergetico();

		}

		String nombarra = this.getBarra().getNombre();
		double[] qvert = new double[this.getCantPostes()];
		for (int ip = 0; ip < this.getCantPostes(); ip++) {
			qvert[ip] = this.compD.getQVer(salidaUltimaIter, ip);
		}
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		double volini = this.getVolFijo().getValor(instanteActual);
		double dualVol = 0;
		boolean conlago = this.getCompD().getCompLago().equalsIgnoreCase(Constantes.HIDROCONLAGO);
		if (conlago) {
			volini = this.getVolumen().getEstadoDespuesDeCDE();
			try {
				dualVol = salidaUltimaIter.getDuales().get(compD.getNombreRestriccionBalanceLago());
			} catch (Exception e) {
				dualVol = 0.0;
			}
		}

		// dualVert = salidaUltimaIter.getDuales().get(compD.generarNombre("vex"));

		double volfin = 0; // volumen final cuando la opción es HIPERPLANOS en hm3
//		if (compD.getNvolfin() != null) {
//			volfin = salidaUltimaIter.getSolucion().get(compD.getNvolfin()) / 1e6;
//		} else {
//			volfin = Double.NaN;
//		}
		double cotaArribaIni;
		if (conlago) {
			cotaArribaIni = this.fCovo.dameValor(volini);
		} else {
			cotaArribaIni = this.fCovo.dameValor(this.volFijo.getValor(instanteActual));
		}

		double voleromin = this.getCompD().getVolEroMin();
		if (!conlago) {
			voleromin = -1;
		}
		double qvermax = this.getCompD().getQverMax();
		double valaguaneto = this.getCompD().getValAgua();
		if (this.getGeneradorAbajo() != null) {
			valaguaneto -= this.generadorAbajo.getCompD().getValAgua();
		}

		/**
		 * CONTROL DE COTAS
		 */

		double volumenSuperiorIncumplido = 0;
		double cotaSuperiorIncumplida = 0;
		double costoIncuplimientoSuperior = 0;
		double cotaInferiorIncumplida = 0;
		double volumenInferiorIncumplido = 0;
		double costoIncuplimientoInferior = 0;

		if (compS.getValsCompGeneral().get(Constantes.COMPLAGO).equalsIgnoreCase(Constantes.HIDROCONLAGO)) {

			if (isHayControldeCotasMaximas()) {
				volfin = salidaUltimaIter.getSolucion().get(compD.getNvolfin());
				volumenSuperiorIncumplido = salidaUltimaIter.getSolucion().get(compD.getnVolPenMax());
				double cotaControlMax = this.getfCovo().dameValor(this.getVolumenControlMaximo().getValor(instanteActual));
				double dVdCenCotaControlSup = this.compD.calcularDerivadaIncremental(cotaControlMax, this.getfVoco());
				double fraccionDia = this.getDuracionPaso() / Constantes.SEGUNDOSXDIA;
				double cvpenSup = this.getPenalidadControlMaximo().getValor(instanteActual) * Constantes.CONHM3AM3 * fraccionDia
						/ dVdCenCotaControlSup;
				cotaSuperiorIncumplida = this.getfCovo().dameValor(
						volumenSuperiorIncumplido + this.getVolumenControlMaximo().getValor(instanteActual)) - cotaControlMax;
				costoIncuplimientoSuperior = volumenSuperiorIncumplido * cvpenSup;
			}

			if (isHayControldeCotasMinimas()) {
				volfin = salidaUltimaIter.getSolucion().get(compD.getNvolfin());
				volumenInferiorIncumplido = salidaUltimaIter.getSolucion().get(compD.getnVolPenMin());
				double cotaControlMin = this.getfCovo().dameValor(this.getVolumenControlMinimo().getValor(instanteActual));
				double dVdCenCotaControlInf = this.compD.calcularDerivadaIncremental(cotaControlMin, this.getfVoco());
				double fraccionDia = this.getDuracionPaso() / Constantes.SEGUNDOSXDIA;
				double cvpenInf = this.getPenalidadControlMinimo().getValor(instanteActual) * Constantes.CONHM3AM3 * fraccionDia
						/ dVdCenCotaControlInf;
				cotaInferiorIncumplida = cotaControlMin - this.getfCovo()
						.dameValor(this.getVolumenControlMinimo().getValor(instanteActual) - volumenInferiorIncumplido);
				costoIncuplimientoInferior = volumenInferiorIncumplido * cvpenInf;
			}
		}

		/**
		 * FIN CONTROL DE COTAS
		 */
		

		DatosHidraulicoSP hid = new DatosHidraulicoSP(this.getNombre(), nombarra, this.getPotenciaMaxima().getValor(instanteActual),
				this.getqTur1Max().getValor(instanteActual), this.getCantModDisp().getValor().intValue(), qvert,
				this.getCompD().getAporte(), volini, volfin, dualVol, cotaArribaIni, voleromin, qvermax,
				this.getCompD().getValAgua(), valaguaneto, potencia, coefEnerg, qturb, salto, costoPaso,
				this.getCompS().getPenalizEco(), dualVert, cotaSuperiorIncumplida, volumenSuperiorIncumplido,
				cotaInferiorIncumplida, volumenInferiorIncumplido, costoIncuplimientoSuperior,
				costoIncuplimientoInferior);
		for (DatosBarraSP dbsp : resultadoPaso.getRed().getBarras()) {
			if (nombarra.equalsIgnoreCase(dbsp.getNombre())
					|| this.getBarra().getRedAsociada().getCompD().isUninodal()) {
				dbsp.agregarHidraulico(hid);
				break;
			}
		}
	}

	// TODO: CUIDAR PERFORMANCE
	public double cotaAguasArriba(boolean optim) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		String valCompLago = compS.getValsCompGeneral().get(Constantes.COMPLAGO);
		if (valCompLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			if (!optim) {
				return fCovo.dameValor(volumen.getEstado());
			} else {
				return fCovo.dameValor(volumenOpt.getEstado());
			}
		} else if (valCompLago.equalsIgnoreCase(Constantes.HIDROSINLAGO)) {
			return fCovo.dameValor(volFijo.getValor(instanteActual));
		}
		return 0;
	}

	/*
	 * Coeficiente energótico expresado en MW/(m3/s)
	 */
	public double coefEnergetico(double cotaArriba, double cotaAguasAbajo) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		return (cotaArriba - cotaAguasAbajo) * Constantes.PESOESPDELAGUA * this.getRendMax().getValor(instanteActual) / 1e6;
	}

	public HidraulicoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(HidraulicoCompDesp compD) {
		this.compD = compD;
	}

	public HidraulicoCompSim getCompS() {
		return compS;
	}

	public void setCompS(HidraulicoCompSim compS) {
		this.compS = compS;
	}

	public HidraulicoComp getCompG() {
		return compG;
	}

	public void setCompG(HidraulicoComp compG) {
		this.compG = compG;
	}

	public Evolucion<Double> getVolFijo() {
		return volFijo;
	}

	public VariableEstadoPar getVolumenOpt() {
		return volumenOpt;
	}

	public void setVolumenOpt(VariableEstadoPar volumenOpt) {
		this.volumenOpt = volumenOpt;
	}

	public ArrayList<GeneradorHidraulico> getGeneradoresArriba() {
		return generadoresArriba;
	}

	public void setGeneradoresArriba(ArrayList<GeneradorHidraulico> generadoresArriba) {
		this.generadoresArriba = generadoresArriba;
	}

	public GeneradorHidraulico getGeneradorAbajo() {
		return generadorAbajo;
	}

	public ArrayList<GeneradorHidraulico> getTodosAbajo() {
		return todosAbajo;
	}

	public void setTodosAbajo(ArrayList<GeneradorHidraulico> todosAbajo) {
		this.todosAbajo = todosAbajo;
	}

	public String getNombreVA() {
		return nombreVA;
	}

	public void setNombreVA(String nombreVA) {
		this.nombreVA = nombreVA;
	}

	public void setGeneradorAbajo(GeneradorHidraulico generadorAbajo) {
		this.generadorAbajo = generadorAbajo;
	}

	public void actualizarAguasArribaAbajo(ArrayList<GeneradorHidraulico> aguasArriba, GeneradorHidraulico aguasAbajo) {
		setGeneradoresArriba(aguasArriba);
		generadorAbajo = aguasAbajo;
	}

	public VariableAleatoria getAporte() {
		return aporte;
	}

	public void setAporte(VariableAleatoria aporte) {
		this.aporte = aporte;
	}

	public Evolucion<Double> getqTur1Max() {
		return qTur1Max;
	}

	public void setqTur1Max(Evolucion<Double> qTur1Max) {
		this.qTur1Max = qTur1Max;
	}

	public Polinomio getfCovo() {
		return fCovo;
	}

	public void setfCovo(Polinomio fCovo) {
		this.fCovo = fCovo;
	}

	public Polinomio getfVoco() {
		return fVoco;
	}

	public void setfVoco(Polinomio fVoco) {
		this.fVoco = fVoco;
	}

	public Polinomio getfQEroMin() {
		return fQEroMin;
	}

	public void setfQEroMin(Polinomio fQEroMin) {
		this.fQEroMin = fQEroMin;
	}

	public Polinomio getfEvaporacion() {
		return fEvaporacion;
	}

	public void setfEvaporacion(Polinomio fEvaporacion) {
		this.fEvaporacion = fEvaporacion;
	}

	public Evolucion<Double> getCoefEvaporacion() {
		return coefEvaporacion;
	}

	public void setCoefEvaporacion(Evolucion<Double> coefEvaporacion) {
		this.coefEvaporacion = coefEvaporacion;
	}

	public Polinomio getfFiltracion() {
		return fFiltracion;
	}

	public void setfFiltracion(Polinomio fFiltracion) {
		this.fFiltracion = fFiltracion;
	}

	public Polinomio getfQVerMax() {
		return fQVerMax;
	}

	public void setfQVerMax(Polinomio fQVerMax) {
		this.fQVerMax = fQVerMax;
	}

	public VariableEstadoPar getVolumen() {
		return volumen;
	}

	public void setVolumen(VariableEstadoPar volumen) {
		this.volumen = volumen;
	}

	public Polinomio getfCotaAguasAbajo() {
		return fCotaAguasAbajo;
	}

	public void setfCotaAguasAbajo(Polinomio fCotaAguasAbajo) {
		this.fCotaAguasAbajo = fCotaAguasAbajo;
	}

	public double getSaltoMin() {
		return saltoMin;
	}

	public void setSaltoMin(double saltoMin) {
		this.saltoMin = saltoMin;
	}

	public double getCotaInundacionAguasAbajo() {
		return cotaInundacionAguasAbajo;
	}

	public void setCotaInundacionAguasAbajo(double cotaInundacionAguasAbajo) {
		this.cotaInundacionAguasAbajo = cotaInundacionAguasAbajo;
	}

	public double getCotaInundacionAguasArriba() {
		return cotaInundacionAguasArriba;
	}

	public void setCotaInundacionAguasArriba(double cotaInundacionAguasArriba) {
		this.cotaInundacionAguasArriba = cotaInundacionAguasArriba;
	}

	public double getEpsilonCaudalErogadoIteracion() {
		return epsilonCaudalErogadoIteracion;
	}

	public void setEpsilonCaudalErogadoIteracion(double epsilonCaudalErogadoIteracion) {
		this.epsilonCaudalErogadoIteracion = epsilonCaudalErogadoIteracion;
	}

//	public double getCotaAguasArriba() {
//		int instante = this.simPaso.getCorrida().getLineaTiempo().getLinea().get(this.simPaso.getNumpaso()).getInstanteInicial();
//		String valCompGen = this.getCompG().getFotoComportamientos(instante).get(Constantes.COMPLAGO);
//
//		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
//			return this.fCovo.dameValor(this.volumen.getEstado());
//		} else {
//			return this.fCovo.dameValor(this.volFijo.getValor(instanteActual));
//		}
//		
//	}

	public Evolucion<Double> getFactorCompartir() {
		return factorCompartir;
	}

	public void setFactorCompartir(Evolucion<Double> factorCompartir) {
		this.factorCompartir = factorCompartir;
	}

	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		GeneradorHidraulico.atributosDetallados = atributosDetallados;
	}

	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		return this.getCompS().devuelveVarDualVEContinua(vec, resultado);
	}

	@Override
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla) {
		String compGlobalBellman = this.devuelveCompValorBellman();
		if (volumenOpt != null)
			tabla.put(volumenOpt.getNombre(), compD.getNvolfin());

	}

	public Hashtable<Pair<Double, Double>, ArrayList<Recta>> getFuncionesPQ() {
		return funcionesPQ;
	}

	public void setFuncionesPQ(Hashtable<Pair<Double, Double>, ArrayList<Recta>> funcionesPQ) {
		this.funcionesPQ = funcionesPQ;
	}

	@Override
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		this.compS.cargarVEfinPasoOptim(resultado);

	}

	public Evolucion<Boolean> getTieneCaudalMinEcol() {
		return tieneCaudalMinEcol;
	}

	public void setTieneCaudalMinEcol(Evolucion<Boolean> tieneCaudalMinEcol) {
		this.tieneCaudalMinEcol = tieneCaudalMinEcol;
	}

	public Evolucion<ArrayList<Double>> getCaudalMinEcol() {
		return caudalMinEcol;
	}

	public void setCaudalMinEcol(Evolucion<ArrayList<Double>> caudalMinEcol) {
		this.caudalMinEcol = caudalMinEcol;
	}

	public Evolucion<Double> getPenalizacionFaltanteCaudal() {
		return penalizacionFaltanteCaudal;
	}

	public void setPenalizacionFaltanteCaudal(Evolucion<Double> penalizacionFaltanteCaudal) {
		this.penalizacionFaltanteCaudal = penalizacionFaltanteCaudal;
	}

	public Evolucion<Double> getVolReservaEstrategica() {
		return volReservaEstrategica;
	}

	public void setVolReservaEstrategica(Evolucion<Double> volReservaEstrategica) {
		this.volReservaEstrategica = volReservaEstrategica;
	}

	public Evolucion<Double> getValorMinReserva() {
		return valorMinReserva;
	}

	public void setValorMinReserva(Evolucion<Double> valorMinReserva) {
		this.valorMinReserva = valorMinReserva;
	}

	public boolean isValorAplicaOptim() {
		return valorAplicaOptim;
	}

	public void setValorAplicaOptim(boolean valorAplicaOptim) {
		this.valorAplicaOptim = valorAplicaOptim;
	}

	public boolean isHayReservaEstrategica() {
		return hayReservaEstrategica;
	}

	public void setHayReservaEstrategica(boolean hayReservaEstrategica) {
		this.hayReservaEstrategica = hayReservaEstrategica;
	}

	@Override
	public ArrayList<VariableEstado> getVarsEstado() {
		ArrayList<VariableEstado> ret = new ArrayList<VariableEstado>();
		ret.add(volumen);
		return ret;

	}

	public ArrayList<VariableEstado> getVarsEstadoOptim() {
		ArrayList<VariableEstado> ret = new ArrayList<VariableEstado>();
		ret.add(volumenOpt);
		return ret;

	}

	@Override
	public void aportarImpacto(Impacto i, DatosObjetivo costo) {
		this.compD.aportarImpacto(i, costo);

	}

	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		return this.compS.aportarCostoImpacto(impacto, salidaUltimaIter);

	}

	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		return this.compD.cargarRestriccionesImpacto(impacto);
	}

	public boolean isVertimientoConstante() { //
		return vertimientoConstante;
	}

	public boolean isHayVolObjVert() {
		return hayVolObjVert;
	}

	public void setHayVolObjVert(boolean hayVolObjVert) {
		this.hayVolObjVert = hayVolObjVert;
	}

	public Evolucion<Double> getVolObjVert() {
		return volObjVert;
	}

	public void setVolObjVert(Evolucion<Double> volObjVert) {
		this.volObjVert = volObjVert;
	}

	public boolean tengoLago() {
		return this.getCompD().getCompLago().equalsIgnoreCase(Constantes.HIDROCONLAGO);
	}

	@Override
	public void actualizaValoresVEDiscretizacionesVariables(long instante) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearCompDespPE() {
		HidraulicoCompDespPE compDespPE = new HidraulicoCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
	}

	@Override
	public GeneradorCompDespPE devuelveCompDespPE() {
		return (HidraulicoCompDespPE) getCompDespPE();
	}

	
	
}
