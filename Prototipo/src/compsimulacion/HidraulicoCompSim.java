/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * HidraulicoCompSim is part of MOP.
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

package compsimulacion;

import java.util.ArrayList;
import java.util.Hashtable;

import parque.GeneradorHidraulico;
import parque.Impacto;
import pizarron.PizarronRedis;
import utilitarios.Constantes;
import utilitarios.Recta;
import compdespacho.HidraulicoCompDesp;
import compdespacho.ImpactoCompDesp;
import compgeneral.HidraulicoComp;
import datatypesProblema.DatosSalidaProblemaLineal;
import estado.VariableEstado;
import logica.CorridaHandler;

public class HidraulicoCompSim extends CompSimulacion {
	private GeneradorHidraulico gh;
	private HidraulicoCompDesp hcd;
	private HidraulicoComp hcg;
	/**
	 * ATENCIóN UNIDADES En ComportamientoSimulacion los volómenes de lagos estón en
	 * hm3 En ComportamientoDespacho se transforman a m3 para usarlos en las
	 * ecuaciones
	 * 
	 */

	/*
	 * Representa el erogado por poste del paso anterior se actualiza en
	 * actualizarParaProximoPaso
	 */
	private double[] caudalErogadoPorPostePasoAnterior;

	private double volumenErogadoTotalPasoAnterior;
	private double caudalErogadoMedioPasoAnterior;
	private double volumenErogadoAArriba; // volumen erogado desde la central aguas arriba

	private double[] caudalErogadoPorPosteIterAnterior;
	private double volumenErogadoTotalIterAnterior; // en m3
	private double volfilthm3; // en hm3
	private double volevaphm3; // en hm3
	private double penalizEco; // penalización por caudal ecológico en USD
	private double caudalErogadoMedioIterAnterior;

	public HidraulicoCompSim(GeneradorHidraulico gen, HidraulicoCompDesp hcd, HidraulicoComp hcg) {
		super();
		this.setParticipante(gen);
		this.setCompdespacho(hcd);
		this.setCompgeneral(hcg);
		gh = (GeneradorHidraulico) this.getParticipante();

		this.hcd = (HidraulicoCompDesp) this.getCompdespacho();
		this.hcg = (HidraulicoComp) this.getCompgeneral();

	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		boolean optim = false;
		cargarDatosCompDespachoAuxiliar(optim, instante);
	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		boolean optim = true;
		cargarDatosCompDespachoAuxiliar(optim, instante);
	}

	/**
	 * Mótodo auxiliar de cargarDatosCompDespacho y cargarDatosCompDespachoOptim
	 * 
	 * @param optim
	 */
	public void cargarDatosCompDespachoAuxiliar(boolean optim, long instante) {

		if (this.caudalErogadoPorPosteIterAnterior == null) {
			this.caudalErogadoPorPosteIterAnterior = new double[gh.getSimPaso().getCorrida().getCantidadPostes()];
			this.caudalErogadoPorPostePasoAnterior = new double[gh.getSimPaso().getCorrida().getCantidadPostes()];
		}

		hcd.setCoefEnergetico(0.0);
		hcd.setNqturp(new String[gh.getCantPostes()]);
		hcd.setNqerop(new String[gh.getCantPostes()]);
		hcd.setNqverp(new String[gh.getCantPostes()]);
		hcd.setNpotp(new String[gh.getCantPostes()]);
		hcd.setNqFaltEco(new String[gh.getCantPostes()]);
		hcd.setFuncionPQ(new ArrayList<Recta>());

		// DATOS QUE VIENEN DE EVOLUCIONES
		hcd.setPotMax(gh.getPotenciaMaxima().getValor(instante));
		hcd.setqTur1Max(gh.getqTur1Max().getValor(instante));
		hcd.setFactorCompartir(gh.getFactorCompartir().getValor(instante));

		// Si se estó en la optimización, o en el primer paso de la simulación anula
		// caudales del paso anterior
		boolean anulaCaudales = optim || ((!optim) && gh.getSimPaso().getCorrida().getLineaTiempo().primerPasoBloque());
		if (anulaCaudales)
			caudalErogadoMedioPasoAnterior = 0.0;
		hcd.setCaudalErogadoAnteriorPaso(caudalErogadoMedioPasoAnterior);

		GeneradorHidraulico ghaa = gh.getGeneradorAbajo();
		if (ghaa == null)
			hcd.setCotaAguasArribaDeCentralAguasAbajo(0.0);
		else
			hcd.setCotaAguasArribaDeCentralAguasAbajo(ghaa.cotaAguasArriba(optim));

		hcd.setCotaAguasArriba(gh.cotaAguasArriba(optim));

		// DATOS QUE VIENEN DE VARIABLES DE ESTADO
		String valCompGen = this.getValsCompGeneral().get(Constantes.COMPLAGO);
		double volini;
		boolean hayLagoEnDespacho = valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)
				|| ((!optim) && valCompGen.equalsIgnoreCase(Constantes.HIDROSINLAGOENOPTIM));
		if (hayLagoEnDespacho) {
			volini = gh.getVolumenOpt().getEstadoDespuesDeCDE();
		} else {
			volini = gh.getVolFijo().getValor(instante);
		}

		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			volfilthm3 = gh.getfFiltracion().dameValor(gh.getfCovo().dameValor(volini)) * gh.getDuracionPaso()
					/ Constantes.M3XHM3;
			volevaphm3 = gh.getfEvaporacion().dameValor(gh.getfCovo().dameValor(volini)) * gh.getDuracionPaso()
					* gh.getCoefEvaporacion().getValor(instante) / Constantes.M3XHM3;
			// double volMermas = volfilthm3 + volevaphm3;

		} else {
			volfilthm3 = 0.0;
			volevaphm3 = 0.0;
		}

		hcd.setVolIni(volini);
		hcd.setVolfilthm3(volfilthm3);
		hcd.setVolevaphm3(volevaphm3);
		hcd.setCotaAguasArriba(gh.getfCovo().dameValor(volini));

		double caudaleromin = gh.getfQEroMin().dameValor(hcd.getCotaAguasArriba());

		double volIniMasApHm3 = 0.0;
		double cotaAux;
		if (hayLagoEnDespacho) {
			volIniMasApHm3 = volini + gh.getAporte().getValor() * gh.getDuracionPaso() / utilitarios.Constantes.M3XHM3;
			cotaAux = gh.getfCovo().dameValor(volIniMasApHm3);
			caudaleromin = gh.getfQEroMin().dameValor(cotaAux);

		} else {
			caudaleromin = gh.getfQEroMin().dameValor(hcd.getCotaAguasArriba());
			cotaAux = hcd.getCotaAguasArriba();

		}
		caudaleromin = Math.min(caudaleromin, gh.getfQVerMax().dameValor(cotaAux));

		// Control para que el caudal erogado minimo no exceda vertimiento máximo
		// No se tiene en cuenta el turbinado móximo porque la restricción de potencia
		// móxima de la central
		// puede acotar el turbinado segón el coeficiente energótico que se tenga
		// (inclusive se puede apagar el turbinado)

		/**
		 * Se toma el mínimo entre el caudal mínimo de control de crecidas o
		 * navegabilidad y el caudal que agota el volumen inicial mós los aportes. Se
		 * carga el volEroMin de HidraulicoComportamientoDespacho que estó en m3.
		 */
		double aporteAux = 0.0;
		if (gh.getAporte() != null)
			aporteAux = Math.max(Constantes.EPSILONCAUDALAGUA, gh.getAporte().getValor());
		// volumen mónimo a erogar en m3
		double volEroMinAux;

		// COMIENZA CODIGO PARA ENTRAR VOLUMEN DONDE COMIENZA EL VERTIDO
		double volComVertHm3 = 0;
		if (gh.isHayVolObjVert()) {
			volComVertHm3 = gh.getVolObjVert().getValor(instante);
		}

		if (hayLagoEnDespacho) {
			// TODO: ATENCIóN no se tiene en cuenta el erogado de centrales aguas arriba
			volEroMinAux = Math.min(volini * Constantes.M3XHM3 + aporteAux * gh.getDuracionPaso(),
					caudaleromin * gh.getDuracionPaso());
			
			volEroMinAux = Math.min(volEroMinAux, Math.max(
					volini * Constantes.M3XHM3 + aporteAux * gh.getDuracionPaso() - volComVertHm3 * Constantes.M3XHM3,
					0.0));

		} else {
			// Con esta formulación es posible una infactibilidad que detendría la ejecución
			volEroMinAux = caudaleromin * gh.getDuracionPaso();

		}
		hcd.setVolEroMin(volEroMinAux);

		hcd.setQverMax(gh.getfQVerMax().dameValor(cotaAux));

		// caudal móximo erogable en m3
		// para chequeo con erogados tolerables para no inundar centrales aguas abajo

		double durPaso = (double) gh.getDuracionPaso();
		double volEroMaxAux;
		double eroAbajo = 0.0;
		if (gh.getGeneradorAbajo() != null && hayLagoEnDespacho) {
			eroAbajo = gh.getCantModInst().getValor(instante) * gh.getqTur1Max().getValor(instante);
			GeneradorHidraulico ghAux = gh;
			while (ghAux.getGeneradorAbajo() != null) {
				GeneradorHidraulico gAbajo = ghAux.getGeneradorAbajo();
				eroAbajo = Math.max(eroAbajo,
						gAbajo.getCantModInst().getValor(instante) * gAbajo.getqTur1Max().getValor(instante));
				ghAux = ghAux.getGeneradorAbajo();
			}
			eroAbajo = eroAbajo * durPaso * Constantes.AUMENTO_ERO_ABAJO; // se obtienen los m3
		}
		volEroMaxAux = Math.max(eroAbajo, volEroMinAux + Constantes.EPSILONVOLAGUA);
		hcd.setVolEroMax(volEroMaxAux);

		// DATOS QUE VIENEN DE VARIABLES ALEATORIAS
		Integer cantModDisp = gh.getCantModDisp().getValor().intValue();
		hcd.setCantModDisp(cantModDisp);
		// ATENCIóN SE TOMA EL MóXIMO ENTRE EL APORTE LEóDO Y CERO !!!!!
		if (gh.getAporte() != null) {
			hcd.setAporte(Math.max(Constantes.EPSILONCAUDALAGUA, gh.getAporte().getValor()));
			gh.getAporte().setValor(hcd.getAporte());
		} else {

			// gh.setAporte(new VariableAleatoria(gh.getNombre(), false,null, null));
			hcd.setAporte(Constantes.EPSILONCAUDALAGUA);
		}
		// DATOS QUE VIENEN DEL RESOPTIM
		// Segón se estó en simulación u optimizacion la VE usada es distinta
		String compGlobalBellman = hcd.getParametros().get(Constantes.COMPVALORESBELLMAN);
		if (hayLagoEnDespacho && compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
			double valagua = 0.0; // valor en USD/hm3
			if (optim) {
				valagua = gh.getVolumenOpt().getValorRecurso().get(0);
				if (gh.isHayReservaEstrategica() && gh.isValorAplicaOptim()) {
					if (gh.getVolumenOpt().getEstado() <= gh.getVolReservaEstrategica().getValor(instante)) {
						valagua = gh.getValorMinReserva().getValor(instante);
					}
				}
				/*
				 * if(valorVE <= valorLim && menor || valorVE >= valorLim && !menor){ }
				 */
			} else {
				valagua = gh.getVolumen().getValorRecurso().get(0);
				if (gh.isHayReservaEstrategica()) {
					if (gh.getVolumen().getEstado() <= gh.getVolReservaEstrategica().getValor(instante)) {
						valagua = gh.getValorMinReserva().getValor(instante);
					}
				}

			}
			if (valagua < Constantes.EPSILONVALAGUA)
				valagua = Constantes.EPSILONVALAGUA;

			hcd.setValAgua(valagua / Constantes.M3XHM3);

		}
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {

		if (iter == 1) {
			String valCompLago = this.getValsCompGeneral().get(Constantes.COMPLAGO);
			String valCompCoefEnerg = this.getValsCompGeneral().get(Constantes.COMPCOEFENERGETICO);
			if (valCompLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)
					|| valCompLago.equalsIgnoreCase(Constantes.HIDROSINLAGOENOPTIM)) {
				this.getCompdespacho().getParametros().put(Constantes.COMPLAGO, Constantes.HIDROCONLAGO);
			} else {
				this.getCompdespacho().getParametros().put(Constantes.COMPLAGO, Constantes.HIDROSINLAGO);
			}
			this.getCompdespacho().getParametros().put(Constantes.COMPCOEFENERGETICO, valCompCoefEnerg);
		}
	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {

		if (iter == 1) {
			String valCompLago = this.getValsCompGeneral().get(Constantes.COMPLAGO);
			String valCompCoefEnerg = this.getValsCompGeneral().get(Constantes.COMPCOEFENERGETICO);
			if (valCompLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
				this.getCompdespacho().getParametros().put(Constantes.COMPLAGO, Constantes.HIDROCONLAGO);
			} else {
				this.getCompdespacho().getParametros().put(Constantes.COMPLAGO, Constantes.HIDROSINLAGO);
			}
			this.getCompdespacho().getParametros().put(Constantes.COMPCOEFENERGETICO, valCompCoefEnerg);
		}
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal resultado) {
		actualizarCaudalesYVolumenErogados(resultado);
		Double aporte = Math.max(Constantes.EPSILONCAUDALAGUA, gh.getAporte().getValor());
		String valCompLago = this.getValsCompGeneral().get(Constantes.COMPLAGO);
		if (valCompLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			double volini = gh.getVolumen().getEstadoDespuesDeCDE();
			double volincr = -volfilthm3 - volevaphm3
					+ (aporte * gh.getDuracionPaso() + volumenErogadoAArriba - volumenErogadoTotalPasoAnterior)
							/ Constantes.M3XHM3;
			gh.getVolumen().actualizarEstado(Math.max(volini + volincr, Constantes.EPSILONVOLAGUA / Constantes.M3XHM3)); // EPSILONVOLAGUA
																															// e																															// m3
		}
	}

	/**
	 * Verifica el cierre del balance de centrales sin embalse ESTE MóTODO SóLO SE
	 * EMPLEA DURANTE LAS PRUEBAS ESTE MóTODO SóLO SE APLICA A HIDRóULICAS SIN LAGO
	 * CON COMPORTAMIENTO COEF ENERGóTICOS CONSTANTS DEBE INVOCARSE INMEDIATAMENTE
	 * DESPUóS DE INVOCAR EL RESOLVEDOR DEL DESPACHO
	 * 
	 * @param iter
	 */
	public boolean verificaBalanceHidro(DatosSalidaProblemaLineal dspl, int numPaso, int[] estado, int sorteo,
			int iter) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		boolean cierra = true;
		String compLago = hcd.getCompLago();
		if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("Se invocó cierraBalanceHidro de HidraulicoCompSim en central con lago");
			if (CorridaHandler.getInstance().isParalelo()) {
			//	//PizarronRedis pp = new PizarronRedis();
				// pp.matarServidores();
			}
			System.exit(1);
		} else {
			double entradas = 0.0; // entradas de agua en m3
			double pot;
			double vol;
			double qver;
			int cantPostes = gh.getCantPostes();
			int durPaso = gh.getDuracionPaso();
			// erogado de centrales aguas arriba
			for (GeneradorHidraulico g1 : gh.getGeneradoresArriba()) {
				HidraulicoCompDesp hcd1 = g1.getCompD();
				for (int ip = 0; ip < cantPostes; ip++) {
					if (hcd1.getParametros().get(Constantes.COMPCOEFENERGETICO)
							.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
						entradas += (dspl.getSolucion().get(hcd1.getNqerop()[ip])) * g1.getDuracionPostes(ip);
					} else {
						String nombreVar = hcd1.getNpotp()[ip];
						if (nombreVar == null) {
							pot = 0.0;
						} else {
							pot = dspl.getSolucion().get(nombreVar);
						}
						vol = g1.getDuracionPostes(ip) * pot / hcd1.getCoefEnergetico()
								/ g1.getFactorCompartir().getValor(instanteActual);
						entradas += vol;
					}

				}
				if (hcd1.getParametros().get(Constantes.COMPCOEFENERGETICO)
						.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
					qver = dspl.getSolucion().get(hcd1.getNqver());
					entradas += qver * durPaso;
				}

			}
			// aportes propios
			entradas += hcd.getAporte() * durPaso;
			// erogado de la central
			for (int ip = 0; ip < cantPostes; ip++) {
				if (hcd.getParametros().get(Constantes.COMPCOEFENERGETICO)
						.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
					entradas -= (dspl.getSolucion().get(hcd.getNqerop()[ip])) * gh.getDuracionPostes(ip);
				} else {
					String nombreVar = hcd.getNpotp()[ip];
					pot = dspl.getSolucion().get(nombreVar);
					vol = gh.getDuracionPostes(ip) * pot / hcd.getCoefEnergetico()
							/ gh.getFactorCompartir().getValor(instanteActual);
					entradas -= vol;
				}

			}
			if (hcd.getParametros().get(Constantes.COMPCOEFENERGETICO)
					.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
				qver = dspl.getSolucion().get(hcd.getNqver());
				entradas -= qver * durPaso;
			}

			if (entradas < -Constantes.TOLERANCIA_CREACION_AGUA) {
				if (Constantes.NIVEL_CONSOLA > 1) {
					System.out.println("LAGO DE CENTRAL " + gh.getNombre() + " FABRICA AGUA");
					System.out.println("paso " + numPaso);
					String texto = "ESTADO ";
					for (int ie = 0; ie < estado.length; ie++) {
						texto += estado[ie] + " ";
					}
					System.out.println(texto);
					System.out.println("Sorteo " + sorteo);
				}
				cierra = false;
				// System.exit(0);
			}
			if (entradas > Constantes.TOLERANCIA_DESTRUCCION_AGUA) {
				if (Constantes.NIVEL_CONSOLA > 1) {
					System.out.println("LAGO DE CENTRAL " + gh.getNombre() + " DESTRUYE AGUA");
					if (gh.getNombre().equalsIgnoreCase("salto")) {
						System.out.println("Paro aca ");
					}
					System.out.println("paso " + numPaso);
					String texto = "ESTADO ";
					for (int ie = 0; ie < estado.length; ie++) {
						texto += estado[ie] + " ";
					}
					System.out.println(texto);
					System.out.println("Sorteo " + sorteo);
					System.out.println("Iteracion " + iter);
				}
				cierra = false;
				// System.exit(0);
			}
		}
		return cierra;
	}

	/**
	 * Es usada tanto en la simulación como en la optimización Calcula y carga
	 * atributos de la clase, a partir del despacho anterior resultado:
	 * 
	 * caudalErogadoPorPostePasoAnterior caudalErogadoMedioPasoAnterior
	 * volumenErogadoAArriba volumenErogadoTotalPasoAnterior
	 * 
	 */
	private void actualizarCaudalesYVolumenErogados(DatosSalidaProblemaLineal resultado) {
		ArrayList<GeneradorHidraulico> ghaa = gh.getGeneradoresArriba();

		Integer cantPost = gh.getCantPostes();
		caudalErogadoPorPostePasoAnterior = new double[cantPost];
		double[] tur = new double[cantPost];
		double[] ver = new double[cantPost];
		double[] turAguasArriba = new double[cantPost];
		double[] verAguasArriba = new double[cantPost];
		volumenErogadoAArriba = 0;
		volumenErogadoTotalPasoAnterior = 0;
		for (int p = 0; p < cantPost; p++) {
			tur[p] = hcd.getQTur(resultado, p);
			ver[p] = hcd.getQVer(resultado, p);
			caudalErogadoPorPostePasoAnterior[p] = tur[p] + ver[p];
			volumenErogadoTotalPasoAnterior += caudalErogadoPorPostePasoAnterior[p] * gh.getDuracionPostes(p);
			for (GeneradorHidraulico g : ghaa) {
				HidraulicoCompDesp cdaa = (HidraulicoCompDesp) g.getCompDesp();
				turAguasArriba[p] += cdaa.getQTur(resultado, p);
				verAguasArriba[p] += cdaa.getQVer(resultado, p);
			}
			volumenErogadoAArriba += (turAguasArriba[p] + verAguasArriba[p]) * gh.getDuracionPostes(p);
		}
		caudalErogadoMedioPasoAnterior = volumenErogadoTotalPasoAnterior / gh.getDuracionPaso();
		hcd.setCaudalErogadoAnteriorPaso(caudalErogadoMedioPasoAnterior);
	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		String valCompCoefEnerg = this.getValsCompGeneral().get(Constantes.COMPCOEFENERGETICO);
		Integer cantPost = gh.getCantPostes();

		if (valCompCoefEnerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
			if (iter == 1) {
				if (caudalErogadoPorPostePasoAnterior == null) {
					caudalErogadoPorPostePasoAnterior = new double[cantPost];
				}
				if (caudalErogadoPorPostePasoAnterior.length == cantPost) {
					caudalErogadoPorPosteIterAnterior = caudalErogadoPorPostePasoAnterior.clone();
					caudalErogadoMedioIterAnterior = caudalErogadoMedioPasoAnterior;
				} else {
					for (int i = 0; i < cantPost; i++) {
						caudalErogadoPorPosteIterAnterior = new double[cantPost];
						caudalErogadoPorPosteIterAnterior[i] = volumenErogadoTotalIterAnterior / gh.getDuracionPaso();
						caudalErogadoMedioIterAnterior = volumenErogadoTotalIterAnterior / gh.getDuracionPaso();
					}
				}
				hcd.setControlApagado(true);
				hcd.setApagarTurbinado(false);
			}
			if (iter > 1) {
				hcd.setControlApagado(false);
				caudalErogadoPorPosteIterAnterior = new double[cantPost];
				this.caudalErogadoMedioIterAnterior = 0;
				volumenErogadoTotalIterAnterior = 0;
				double[] tur = new double[cantPost];
				double[] ver = new double[cantPost];
				for (int p = 0; p < cantPost; p++) {
					tur[p] = hcd.getQTur(salidaIter, p);
					ver[p] = hcd.getQVer(salidaIter, p);
					caudalErogadoPorPosteIterAnterior[p] = tur[p] + ver[p];
					volumenErogadoTotalIterAnterior += caudalErogadoPorPosteIterAnterior[p] * gh.getDuracionPostes(p);
				}
				caudalErogadoMedioIterAnterior = volumenErogadoTotalIterAnterior / gh.getDuracionPaso();

			}
			hcd.setCaudalErogadoMedioIterAnterior(caudalErogadoMedioIterAnterior);
			hcd.setCaudalErogadoAnteriorPaso(caudalErogadoMedioPasoAnterior);
			hcd.actualizarCoeficienteEnergetico();
		} else if (valCompCoefEnerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
			hcd.actualizarFuncionesPQ();
			hcd.setVertConstante(false);
			volumenErogadoTotalIterAnterior = 0;
			if (gh.isVertimientoConstante() && iter > 1) {
				double[] tur = new double[cantPost];
				double[] ver = new double[cantPost];
				for (int p = 0; p < cantPost; p++) {
					tur[p] = hcd.getQTur(salidaIter, p);
					ver[p] = hcd.getQVer(salidaIter, p);
					caudalErogadoPorPosteIterAnterior[p] = tur[p] + ver[p];
					volumenErogadoTotalIterAnterior += caudalErogadoPorPosteIterAnterior[p] * gh.getDuracionPostes(p);
				}
				if (volumenErogadoTotalIterAnterior > hcd.getqTur1Max() * hcd.getCantModDisp() * gh.getDuracionPaso()) {
					hcd.setVertConstante(true);

				}
			}
		}
	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		int cantPost = gh.getCantPostes();
		double[] caudalAux = caudalErogadoPorPosteIterAnterior.clone();

		if (iter > 1) {
			double[] tur = new double[cantPost];
			double[] ver = new double[cantPost];
			for (int p = 0; p < cantPost; p++) {
				tur[p] = hcd.getQTur(salidaIter, p);
				ver[p] = hcd.getQVer(salidaIter, p);
				caudalErogadoPorPosteIterAnterior[p] = (tur[p] + ver[p]);
				if (Math.abs(caudalAux[p] - caudalErogadoPorPosteIterAnterior[p]) < gh
						.getEpsilonCaudalErogadoIteracion()) {
					return true;
				}
			}

		}
		return false;
	}

	@Override
	/**
	 * ATENCIóN MANOLO: ACó EN EL CASO ENCADENADO HABRóA QUE TOMAR EL ESTADO FINAL
	 * DEL ESCENARIO ANTERIOR SI NO ES EL PRIMERO SIMULADO LO MISMO CON LOS OTROS
	 * COMPORTAMIENTOS SIMULACION
	 */
	public void inicializarParaEscenario() {

		long instanteInicial = gh.getSimPaso().getCorrida().getInstanteInicial();
		String valCompGen = hcg.getFotoComportamientos(instanteInicial).get(Constantes.COMPLAGO);

		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			gh.getVolumen().cargarValorInicial();
		}

		double volInicial = gh.getVolFijo().getValor(instanteInicial);
		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO))
			volInicial = gh.getVolumen().getValorInicial();

		hcd.setCotaAguasArriba(gh.getfCovo().dameValor(volInicial));

		caudalErogadoMedioPasoAnterior = 0.0;
		hcd.setCaudalErogadoAnteriorPaso(caudalErogadoMedioPasoAnterior);
		GeneradorHidraulico ghaa = gh.getGeneradorAbajo();
		double volInicialAA;
		if (ghaa != null) {
			if (ghaa.getVolumen() != null) {
				// la central ghaa tiene lago y variable de estado volumen
				volInicialAA = ghaa.getVolumen().getValorInicial();
			} else {
				// la central ghaa no tiene lago ni variable de estado volumen
				volInicialAA = ghaa.getVolFijo().getValor(instanteInicial);
			}
			hcd.setCotaAguasArribaDeCentralAguasAbajo(ghaa.getfCovo().dameValor(volInicialAA));
		}
	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		this.getCompdespacho().getParametros().putAll(comps);
	}

	/*
	 * Devuelve el volumen final del lago estimado antes de hacer un paso de la
	 * simulación
	 */
	public void contribuirAS0fint() {
		// Recordar que la variable de estado volumen estó expresada en hm3
//		double erogadoAnterior = volumenErogadoPasoAnteriorm3();
		int durpaso = gh.getDuracionPaso();
		String valCompGen = getValsCompGeneral().get(Constantes.COMPLAGO);
		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			double volInicial = gh.getVolumen().getEstadoDespuesDeCDE();
			double estadoS0fint = volInicial
					+ (Math.max(Constantes.EPSILONCAUDALAGUA, gh.getAporte().getValor())) * durpaso / Constantes.M3XHM3;
			gh.getVolumen().setEstadoS0fint(estadoS0fint < 0 ? 0 : estadoS0fint);
		}
	}

	/*
	 * Devuelve el volumen final del lago estimado antes de hacer un paso de la
	 * optimización
	 */
	public void contribuirAS0fintOptim() {
		// Recordar que la variable de estado volumen está expresada en hm3
//		double erogadoAnterior = volumenErogadoPasoAnteriorm3(); 
		int durpaso = gh.getDuracionPaso();
		String valCompGen = getValsCompGeneral().get(Constantes.COMPLAGO);
		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			double volInicial = gh.getVolumenOpt().getEstadoDespuesDeCDE();
			// Se supone que el volumen no cambia respecto al inicial
			// double estadoS0fint = volInicial;
			double estadoS0fint = volInicial
					+ (Math.max(Constantes.EPSILONCAUDALAGUA, gh.getAporte().getValor())) * durpaso / Constantes.M3XHM3;
			gh.getVolumenOpt().setEstadoS0fint(estadoS0fint < 0 ? 0 : estadoS0fint);
		}
	}

	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// Como hay una sola variable de estado continua no se emplea el argumento vec
		String nombreRBalanceLago = gh.getCompD().getNombreRestriccionBalanceLago();
		return resultado.getDuales().get(nombreRBalanceLago);

	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN ////////////////

	/**
	 * TODO: Podróa omitirse esta inicialización porque deberóa estar en cero por
	 * defecto
	 */
	@Override
	public void actualizarOtrosDatosIniciales() {
		int cantPostes = gh.getCantPostes();
		caudalErogadoPorPostePasoAnterior = new double[cantPostes];
		for (int ip = 0; ip < cantPostes; ip++) {
			caudalErogadoPorPostePasoAnterior[ip] = 0.0;
		}
	}

	/**
	 * ACA NO SE VALORIZA LA VARIACIóN DE AGUA EN LOS EMBALSES QUE VIENE EN EL VALOR
	 * DE BELLMAN AL FINAL DEL PASO SINO SOLO EL COSTO VARIABLE DE GENERACIóN Y EL
	 * COSTO DE LA PENALIZACIóN POR CAUDAL MóNIMO ECOLóGICO
	 */
	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		double costo = 0.0;
		penalizEco = 0.0;
		for (int i = 0; i < gh.getCantPostes(); i++) {
			costo += salidaUltimaIter.getSolucion().get(hcd.getNpotp()[i]) * gh.getDuracionPostes(i)
					* gh.getCostoVariable().getValor(instanteActual) / Constantes.SEGUNDOSXHORA;

		}
		costo += penalizEco;
	//	System.out.println("costo paso " + this.getGh().getNombre() + " : " + costo);
		return costo;
	}

	/**
	 * TODO ESTO EXTRAJO PEDAZOS DE CóDIGO DE actualizarParaProximoPaso de la
	 * simulación Se mantienen los nombres DEBERÁ UNIFICARSE UN ÚNICO MÉTODO CON
	 * actualizarParaProximPaso que difiera sóo en la carga de distintos
	 */
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		String valCompGen = getValsCompGeneral().get(Constantes.COMPLAGO);
		if (valCompGen.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			actualizarCaudalesYVolumenErogados(resultado);
			Double aporte = Math.max(Constantes.EPSILONCAUDALAGUA, gh.getAporte().getValor());
			double volini = gh.getVolumenOpt().getEstadoDespuesDeCDE();
			double volincr = -volfilthm3 - volevaphm3
					+ (aporte * gh.getDuracionPaso() + volumenErogadoAArriba - volumenErogadoTotalPasoAnterior)
							/ Constantes.M3XHM3;
			gh.getVolumenOpt()
					.setEstadoFinalOptim(Math.max(volini + volincr, Constantes.EPSILONVOLAGUA / Constantes.M3XHM3));
		}
	}


	public GeneradorHidraulico getGh() {
		return gh;
	}

	public void setGh(GeneradorHidraulico gh) {
		this.gh = gh;
	}

	public HidraulicoCompDesp getHcd() {
		return hcd;
	}

	public void setHcd(HidraulicoCompDesp hcd) {
		this.hcd = hcd;
	}

	public HidraulicoComp getHcg() {
		return hcg;
	}

	public void setHcg(HidraulicoComp hcg) {
		this.hcg = hcg;
	}

	public double[] getErogadoPorPostePasoAnterior() {
		return caudalErogadoPorPostePasoAnterior;
	}

	public void setErogadoPorPostePasoAnterior(double[] erogadoPorPostePasoAnterior) {
		this.caudalErogadoPorPostePasoAnterior = erogadoPorPostePasoAnterior;
	}

	public double getCaudalErogadoMedioPasoAnterior() {
		return caudalErogadoMedioPasoAnterior;
	}

	public void setCaudalErogadoMedioPasoAnterior(double caudalErogadoMedioPasoAnterior) {
		this.caudalErogadoMedioPasoAnterior = caudalErogadoMedioPasoAnterior;
	}

	public double[] getErogadoPorPosteIterAnterior() {
		return caudalErogadoPorPosteIterAnterior;
	}

	public void setErogadoPorPosteIterAnterior(double[] erogadoPorPosteIterAnterior) {
		this.caudalErogadoPorPosteIterAnterior = erogadoPorPosteIterAnterior;
	}

	public double[] getCaudalErogadoPorPostePasoAnterior() {
		return caudalErogadoPorPostePasoAnterior;
	}

	public void setCaudalErogadoPorPostePasoAnterior(double[] caudalErogadoPorPostePasoAnterior) {
		this.caudalErogadoPorPostePasoAnterior = caudalErogadoPorPostePasoAnterior;
	}

	public double[] getCaudalErogadoPorPosteIterAnterior() {
		return caudalErogadoPorPosteIterAnterior;
	}

	public void setCaudalErogadoPorPosteIterAnterior(double[] caudalErogadoPorPosteIterAnterior) {
		this.caudalErogadoPorPosteIterAnterior = caudalErogadoPorPosteIterAnterior;
	}

	public double getVolumenErogadoTotalPasoAnterior() {
		return volumenErogadoTotalPasoAnterior;
	}

	public void setVolumenErogadoTotalPasoAnterior(double volumenErogadoTotalPasoAnterior) {
		this.volumenErogadoTotalPasoAnterior = volumenErogadoTotalPasoAnterior;
	}

	public double getVolumenErogadoAArriba() {
		return volumenErogadoAArriba;
	}

	public void setVolumenErogadoAArriba(double volumenErogadoAArriba) {
		this.volumenErogadoAArriba = volumenErogadoAArriba;
	}

	public double getVolumenErogadoTotalIterAnterior() {
		return volumenErogadoTotalIterAnterior;
	}

	public void setVolumenErogadoTotalIterAnterior(double volumenErogadoTotalIterAnterior) {
		this.volumenErogadoTotalIterAnterior = volumenErogadoTotalIterAnterior;
	}

	public double getVolfilthm3() {
		return volfilthm3;
	}

	public void setVolfilthm3(double volfilthm3) {
		this.volfilthm3 = volfilthm3;
	}

	public double getVolevaphm3() {
		return volevaphm3;
	}

	public void setVolevaphm3(double volevaphm3) {
		this.volevaphm3 = volevaphm3;
	}

	public double getPenalizEco() {
		return penalizEco;
	}

	public void setPenalizEco(double penalizEco) {
		this.penalizEco = penalizEco;
	}

	public Double aportarCostoImpacto(Impacto i, DatosSalidaProblemaLineal salidaUltimaIter) {
		long instanteActual = i.getSimPaso().getCorrida().getLineaTiempo().pasoActual().getInstanteFinal();
		ImpactoCompDesp icd = (ImpactoCompDesp) i.getCompDesp();
		double costo = 0.0;
		double multiplicador = 1.0;

		if (i.isPorPoste()) {

			for (int p = 0; p < i.getCantPostes(); p++) {
				if (i.isPorUnidadTiempo() || i.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO
						|| i.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO)
					multiplicador = gh.getDuracionPostes(p);
			
				costo += i.getCostoUnit().getValor(instanteActual) / Constantes.M3XHM3
						* salidaUltimaIter.getSolucion().get(icd.getnExcesop()[p]) * multiplicador;
			
			}

		} else {

			if (i.isPorUnidadTiempo() || i.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO
					|| i.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO)
				multiplicador = gh.getDuracionPaso();
			costo = i.getCostoUnit().getValor(instanteActual) / Constantes.M3XHM3
					* salidaUltimaIter.getSolucion().get(icd.getnExceso()) * multiplicador;
		}

		return costo;
	}

}