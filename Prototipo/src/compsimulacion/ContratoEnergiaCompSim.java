/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoEnergiaCompSim is part of MOP.
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

import compdespacho.ContratoEnergiaCompDesp;
import compdespacho.EolicoCompDesp;
import compdespacho.FotovoltaicoCompDesp;
import compdespacho.HidraulicoCompDesp;
import compdespacho.ImpoExpoCompDesp;
import compdespacho.TermicoCompDesp;
import compgeneral.ContratoEnergiaComp;
import datatypesProblema.DatosSalidaProblemaLineal;
import logica.CorridaHandler;
import parque.Barra;
import parque.ContratoEnergia;
import parque.Generador;
import parque.GeneradorEolico;
import parque.GeneradorFotovoltaico;
import parque.GeneradorHidraulico;
import parque.GeneradorTermico;
import parque.ImpoExpo;
import parque.Participante;
import pizarron.PizarronRedis;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;

/**
 * La clase permite calcular el monto de transacciones por contratos que no
 * afectan el despacho ni entran en los costos de optimizaci�n. Los montos de
 * esos contratos en las salidas de la simulaci�n se presentan separados de los
 * costos de los participantes.
 * 
 * @author ut469262
 *
 */

public class ContratoEnergiaCompSim extends CompSimulacion {

	private ContratoEnergia contrato;
	private ContratoEnergiaCompDesp contratocd;
	private ContratoEnergiaComp contratocg;

	/**
	 * True si el paso anterior termina antes o igual a la fecha del contrato False
	 * si el paso anterior termina después de la fecha del contrato
	 * 
	 * @param gen
	 * @param acd
	 * @param acg
	 */
	boolean pasoAntTerminaAntesOIgualFechaInicial;

	public ContratoEnergiaCompSim(ContratoEnergia gen, ContratoEnergiaCompDesp acd, ContratoEnergiaComp acg) {
		super();
		this.setParticipante(gen);
		this.setCompdespacho(acd);
		this.setCompgeneral(acg);
		contrato = (ContratoEnergia) this.getParticipante();

		this.contratocd = (ContratoEnergiaCompDesp) this.getCompdespacho();
		this.contratocg = (ContratoEnergiaComp) this.getCompgeneral();

	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> compsGlobales) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		LineaTiempo lt = contrato.getSimPaso().getCorrida().getLineaTiempo();
		PasoTiempo paso = contrato.getSimPaso().getPasoActual();
		double enerpasoGWh = 0;
		ArrayList<Participante> pars = contrato.getInvolucrados();
		if (contrato.getTipoContratoEnergia().equalsIgnoreCase(utilitarios.Constantes.LIM_ENERGIA_ANUAL)) {
			// Tipo de contrato LIM_ENERGIA_ANUAL
			long instIniPaso = paso.getInstanteInicial();
			long instFinPaso = paso.getInstanteFinal();
			int durPaso = (int) (instFinPaso - instIniPaso);
			if (instIniPaso < contrato.getInstFechaFinalContrato()
					&& instIniPaso >= contrato.getInstFechaInicialContrato()) {
				double enerInicioGWh = contrato.getEnerAcumAnioCorrienteGWh();
				// el método calculaEnergiaPasoContrato da la energía en MWh se pasa a GWh
				enerpasoGWh = calculaEnergiaPasoMWh(pars, salidaIter) / utilitarios.Constantes.MWHXGWH;
				double enerFinGWh = enerInicioGWh + enerpasoGWh;
				contrato.setEnerAcumAnioCorrienteGWh(enerFinGWh);
				if (terminaAnioContrato(instIniPaso, instFinPaso)) {
					contrato.setEnerAcumAnioCorrienteGWh(0.0);
				}

			} else {
				contrato.setEnerAcumAnioCorrienteGWh(0.0);
			}
		} else {
			System.out.println("Se pidió un tipo de contrato no programado: " + contrato.getTipoContratoEnergia());
			if (CorridaHandler.getInstance().isParalelo()) {
				////PizarronRedis pp = new PizarronRedis();
				// pp.matarServidores();
			}
			System.exit(1);
		}
	}

	/**
	 * Devuelve true si en algún instante del paso, abierto por izquierda (----| se
	 * encuentra el cumpleaños del contrato (mismo segundo, hora, dia del mes, mes
	 * del inicio del contrato)
	 * 
	 * @param instanteIniPaso
	 * @param instanteFinPaso
	 * @return
	 */
	public boolean terminaAnioContrato(long instanteIniPaso, long instanteFinPaso) {
		LineaTiempo lt = contrato.getSimPaso().getCorrida().getLineaTiempo();
		if (lt.comparaFechasDeInstantes(contrato.getInstFechaInicialContrato(), instanteIniPaso) == 1
				&& lt.comparaFechasDeInstantes(contrato.getInstFechaInicialContrato(), instanteFinPaso) <= 0
				|| lt.comparaFechasDeInstantes(contrato.getInstFechaInicialContrato(), instanteIniPaso) == 1
						&& lt.comparaFechasDeInstantes(instanteFinPaso, instanteIniPaso) <= 0
				|| lt.comparaFechasDeInstantes(contrato.getInstFechaInicialContrato(), instanteFinPaso) <= 0
						&& lt.comparaFechasDeInstantes(instanteFinPaso, instanteIniPaso) <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * Calcula la energía del conjunto de participantes pars en el paso en MWh
	 * 
	 * @param salidaIter
	 * @return
	 */
	public static double calculaEnergiaPasoMWh(ArrayList<Participante> pars, DatosSalidaProblemaLineal salidaIter) {
		double energia = 0.0; // se acumular� la energía del paso en MWh
		for (Participante p : pars) {
			int cantPostes = p.getCantPostes();
			if (p instanceof ImpoExpo) {
				ImpoExpo ie = (ImpoExpo) p;
				ImpoExpoCompDesp cd = ie.getCompD();
				int cantBloques = ie.getCantBloques();
				String[][] npotbp = cd.getNpotbp();
				for (int ip = 0; ip < cantPostes; ip++) {
					double pot = 0;
					for (int ib = 0; ib < cantBloques; ib++) {
						pot += salidaIter.getSolucion().get(npotbp[ib][ip]);
					}
					// Se divide entre los segundos de una hora para pasar de MW.s a MWh
					energia += pot * p.getDuracionPostes(ip) / utilitarios.Constantes.SEGUNDOSXHORA;
				}
			} else {
				String[] npots = null;
				if (p instanceof GeneradorHidraulico) {
					GeneradorHidraulico g = (GeneradorHidraulico) p;
					HidraulicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorTermico) {
					GeneradorTermico g = (GeneradorTermico) p;
					TermicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorEolico) {
					GeneradorEolico g = (GeneradorEolico) p;
					EolicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorFotovoltaico) {
					GeneradorFotovoltaico g = (GeneradorFotovoltaico) p;
					FotovoltaicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				}
				for (int ip = 0; ip < cantPostes; ip++) {
					// Se divide entre los segundos de una hora para pasar de MW.s a MWh
					energia += p.getDuracionPostes(ip) * salidaIter.getSolucion().get(npots[ip])
							/ utilitarios.Constantes.SEGUNDOSXHORA;
				}
			}
		}
		return energia;
	}

	/**
	 * 
	 * Devuelve las potencias por poste del conjunto de participantes pars en el
	 * paso en MW
	 * 
	 * @param salidaIter
	 * @return
	 */
	public static double[] devuelvePotenciasPasoMW(ArrayList<Participante> pars, DatosSalidaProblemaLineal salidaIter) {
		int cantPostes = pars.get(0).getCantPostes();
		double[] potencias = new double[cantPostes]; // se acumular� la energ�a del paso en MWh
		for (Participante p : pars) {
			if (p instanceof ImpoExpo) {
				ImpoExpo ie = (ImpoExpo) p;
				ImpoExpoCompDesp cd = ie.getCompD();
				int cantBloques = ie.getCantBloques();
				String[][] npotbp = cd.getNpotbp();
				for (int ip = 0; ip < cantPostes; ip++) {
					for (int ib = 0; ib < cantBloques; ib++) {
						potencias[ip] += salidaIter.getSolucion().get(npotbp[ib][ip]);
					}
				}
			} else {
				String[] npots = null;
				if (p instanceof GeneradorHidraulico) {
					GeneradorHidraulico g = (GeneradorHidraulico) p;
					HidraulicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorTermico) {
					GeneradorTermico g = (GeneradorTermico) p;
					TermicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorEolico) {
					GeneradorEolico g = (GeneradorEolico) p;
					EolicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorFotovoltaico) {
					GeneradorFotovoltaico g = (GeneradorFotovoltaico) p;
					FotovoltaicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				}
				for (int ip = 0; ip < cantPostes; ip++) {
					potencias[ip] += salidaIter.getSolucion().get(npots[ip]);
				}
			}
		}
		return potencias;
	}

	/**
	 * 
	 * Calcula el valor en USD de la energ�a atribu�da a los participantes pars en
	 * el paso, al spot, con cotas inferior y superior para el costo marginal
	 * 
	 * 
	 * En el spot se considera el tope propio respecto al costo marginal de
	 * utilitarios.Constantes.TOPE_SPOT
	 * 
	 * @param salidaIter
	 * @return
	 */
	public static double calculaValorEnerAlSpotUSD(ArrayList<Participante> pars, DatosSalidaProblemaLineal salidaIter,
			double cotaInf, double cotaSup) {
		double valor = 0.0;
		Barra b;
		for (Participante p : pars) {
			int cantPostes = p.getCantPostes();
			double[] pots = new double[cantPostes]; // potencias por poste en MW
			if (p instanceof ImpoExpo) {
				ImpoExpo ie = (ImpoExpo) p;
				ImpoExpoCompDesp cd = ie.getCompD();
				int cantBloques = ie.getCantBloques();
				String[][] npotbp = cd.getNpotbp();
				for (int ip = 0; ip < cantPostes; ip++) {
					pots[ip] = 0;
					for (int ib = 0; ib < cantBloques; ib++) {
						pots[ip] += salidaIter.getSolucion().get(npotbp[ib][ip]);
					}
				}
				b = ie.getBarra();
			} else {
				String[] npots = null;
				if (p instanceof GeneradorHidraulico) {
					GeneradorHidraulico g = (GeneradorHidraulico) p;
					HidraulicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorTermico) {
					GeneradorTermico g = (GeneradorTermico) p;
					TermicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorEolico) {
					GeneradorEolico g = (GeneradorEolico) p;
					EolicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof GeneradorFotovoltaico) {
					GeneradorFotovoltaico g = (GeneradorFotovoltaico) p;
					FotovoltaicoCompDesp cd = g.getCompD();
					npots = cd.getNpotp();
				} else if (p instanceof ImpoExpo) {
					// Queda por hacer
					System.out.println("En clase ContratoEnergia no se implement� a�n valoraci�n al spot con ImpoExpo");
					if (CorridaHandler.getInstance().isParalelo()) {
						////PizarronRedis pp = new PizarronRedis();
						// pp.matarServidores();
					}
					System.exit(1);
				}
				Generador g = (Generador) p;
				b = g.getBarra();
				for (int ip = 0; ip < cantPostes; ip++) {
					pots[ip] += salidaIter.getSolucion().get(npots[ip]);
				}
			}
			Barra b1 = b;
			if (b.getRedAsociada().getCompD().isUninodal())
				b1 = b.getRedAsociada().getBarraUnica();
			for (int ip = 0; ip < cantPostes; ip++) {
				String nomDual = b1.getCompDesp().generarNombre("demandaPoste", Integer.toString(ip));
				double cosMar = salidaIter.getDuales().get(nomDual) * Constantes.SEGUNDOSXHORA
						/ b1.getDuracionPostes(ip);
				double cotaSup2 = Math.min(utilitarios.Constantes.TOPE_SPOT, cotaSup);
				if (cosMar > cotaSup2) {
					cosMar = cotaSup;
				} else if (cosMar < cotaInf)
					cosMar = cotaInf;
				valor += pots[ip] * p.getDuracionPostes(ip) * cosMar / utilitarios.Constantes.SEGUNDOSXHORA;
			}
		}
		// el valor se expresa en USD
		return valor;
	}

	/**
	 * Calcula el costo del contrato en este paso, en USD
	 * 
	 * ATENCI�N: NO ES UN COSTO QUE AFECTE LA OPTIMIZACIÓN, ESTE MÉTODO CALCULA EL
	 * MONTO DE UNA TRANSACCI�N POR EL CONTRATO, QUE NO AFECTA EL DESPACHO Y SE
	 * ATRIBUYE EN LAS SALIDAS COMO UN COSTO DEL CONTRATO Y NO DE UN PARTICIPANTE EN
	 * PARTICULAR
	 *
	 * ATENCIÓN! HAY ERRORES DE BORDE SI EL AÑO TERMINA ANTES DE ALCANZAR LA ENERGÍA
	 * BASE. SE CALCULA EL COSTO OMITIENDO EL ANÁLISIS DEL FIN DEL AÑO DE CONTRADO
	 * DURANTE EL PASO Y MIRANDO SOLO LA ENERGÍA BASE. HAY ERRORES DE BORDE SI EL
	 * ULTIMO PASO CONTIENE EL FIN DEL AÑO DEL CONTRATO
	 * 
	 */
	public double calculaValorPasoContratoUSD(DatosSalidaProblemaLineal salidaIter) {
		PasoTiempo paso = contrato.getSimPaso().getPasoActual();
		double enerpasoGWh = 0;
		double valor = 0;
		long instIniPaso = paso.getInstanteInicial();
		long instFinPaso = paso.getInstanteFinal();
		ArrayList<Participante> pars = contrato.getInvolucrados();
		if (contrato.getTipoContratoEnergia().equalsIgnoreCase(utilitarios.Constantes.LIM_ENERGIA_ANUAL)) {
			if (instIniPaso < contrato.getInstFechaFinalContrato()
					&& instIniPaso >= contrato.getInstFechaInicialContrato()) {
				double cotaInf = contrato.getCotaInf().getValor(instIniPaso);
				double cotaSup = contrato.getCotaSup().getValor(instIniPaso);
				double enerBase = contrato.getEnergiaBase().getValor(instIniPaso);
				double enerInicio = contrato.getEnerAcumAnioCorrienteGWh(); // en GWh
				enerpasoGWh = calculaEnergiaPasoMWh(pars, salidaIter) / utilitarios.Constantes.MWHXGWH;
				double enerFin = enerInicio + enerpasoGWh;
				double precioBase = contrato.getPrecioBase().getValor(instIniPaso);
				double valorBase = enerpasoGWh * precioBase * utilitarios.Constantes.MWHXGWH;
				double valorMarginal = calculaValorEnerAlSpotUSD(pars, salidaIter, cotaInf, cotaSup);
				if (enerFin <= enerBase) {
					return valorBase;
				} else if (enerInicio >= enerBase) {
					return valorMarginal;
				} else {
					// enerFin>enerBase y enerInicio<enerBase
					double propBase = (enerBase - enerInicio) / enerpasoGWh;
					return (valorBase * propBase + valorMarginal * (1 - propBase));
				}
			}
			return 0.0; // El contrato no está vigente
		} else {
			System.out.println("El tipo de CONTRATOENERGIA " + contrato.getTipoContratoEnergia() + " no existe");
			if (CorridaHandler.getInstance().isParalelo()) {
				////PizarronRedis pp = new PizarronRedis();
				// pp.matarServidores();
			}
			System.exit(1);
			return -999999999.9; // esta sentencia no debería alcanzarse nunca;
		}
	}

	@Override
	public void inicializarParaEscenario() {
		contrato.setEnerAcumAnioCorrienteGWh(contrato.getEnergiaInicial());
	}

	@Override
	public void actualizarOtrosDatosIniciales() {
		// Deliberadamente en blanco
	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		// Deliberadamente en blanco
	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		// Deliberadamente en blanco

	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		/**
		 * El ContratoEnergia no afecta el despacho ni interviene en la optimizaci�n.
		 * Por eso el aporte es cero. Sus costos se computan por fuera y despu�s de
		 * determinar la operaci�n, a la que no afectan.
		 */
		return 0;
	}

}
