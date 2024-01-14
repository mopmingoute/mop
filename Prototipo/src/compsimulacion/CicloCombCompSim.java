/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CicloCombCompSim is part of MOP.
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
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import compdespacho.CicloCombCompDesp;
import compgeneral.CicloCombComp;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosTermicoSPAuxComb;
import logica.CorridaHandler;
import parque.CicloCombinado;
import parque.Combustible;
import parque.ContratoCombustible;
import parque.ContratoCombustibleCanio;
import parque.GeneradorTermico;
import parque.RedCombustible;
import pizarron.PizarronRedis;
import tiempo.Evolucion;
import utilitarios.Constantes;

public class CicloCombCompSim extends CompSimulacion {

	private CicloCombinado cc;
	private CicloCombCompDesp cccd;
	private CicloCombComp compG;

	/**
	 * Si la central está en las condiciones de diseño, debe ser coherente con los
	 * datos de potencia máxima y cantidad de módulos de TGs y CVs Se usa para
	 * calcular aproximadamente la energía de los ciclos de vapor, a partir de la
	 * energía de las TGs combinadas, INCLUSO CUANDO NO ESTÁN A PLENA CARGA.
	 */
	private double relPot; // cociente entre potencia de una TG combinada y potencia de 1 TG en ciclo
							// abierto

	public CicloCombCompSim() {
		super();
		this.cc = (CicloCombinado) this.getParticipante();
		cccd = (CicloCombCompDesp) this.getCompdespacho();

	}

	public CicloCombCompSim(CicloCombinado cc, CicloCombCompDesp tcd, CicloCombComp tcg) {
		this.setParticipante(cc);
		setCompG(tcg);
		this.cccd = tcd;
		this.cc = cc;

	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		String valComp = this.getValsCompGeneral().get(Constantes.COMPCC);
		this.getCompdespacho().getParametros().put(Constantes.COMPCC, valComp);

	}

	@Override
	public void cargarDatosCompDespacho(long instante) {

		this.relPot = cc.getCCs().getPotenciaMaxima().getValor(instante)
				/ cc.getTGs().getPotenciaMaxima().getValor(instante);
		cccd.setNnmodTGp(new String[cc.getCantPostes()]);
		cccd.setNenerTpc(new String[cc.getCantPostes()][]);
		cccd.setNpotp(new String[cc.getCantPostes()]);
		cccd.setNpotTGp(new String[cc.getCantPostes()]);
		cccd.setNpotCCp(new String[cc.getCantPostes()]);
		cccd.setRelPot(relPot);
		cccd.setPotMax1CV(cc.getPotMax1CV().getValor(instante));

		String combustibleBase = cc.getTGs().getRendsPotMax().keys().nextElement();

		// DATOS QUE VIENEN DE EVOLUCIONES

		double potMin1TG = cc.getTGs().getMinimoTecnico().getValor(instante);
		double potMax1TG = cc.getTGs().getPotenciaMaxima().getValor(instante);
		double rendMinBase1TG = cc.getTGs().getRendsPotMin().get(combustibleBase).getValor(instante);
		double rendMaxBase1TG = cc.getTGs().getRendsPotMax().get(combustibleBase).getValor(instante);

		Hashtable<String, Double> coefsEnerTer = new Hashtable<String, Double>();

		for (Map.Entry<String, Evolucion<Double>> c : cc.getTGs().getRendsPotMax().entrySet()) {
			coefsEnerTer.put(c.getKey(), c.getValue().getValor(instante) / rendMaxBase1TG);
		}

		cccd.setCoefsEnerTer(coefsEnerTer);
		cc.getTGs().getCompD().setCoefsEnerTer(coefsEnerTer);
		cccd.setPotMax1TG(potMax1TG);
		cccd.setPotMin1TG(potMin1TG);

		cccd.setPotTerMinTecTG(cccd.calcPotTerMinTec(potMin1TG, rendMinBase1TG));
		cccd.setPotTerPropTG(cccd.calcPotEspTerProp(potMin1TG, potMax1TG, rendMinBase1TG, rendMaxBase1TG));

		double potMin1CC = cc.getCCs().getMinimoTecnico().getValor(instante);
		double potMax1CC = cc.getCCs().getPotenciaMaxima().getValor(instante);
		double rendMinBase1CC = cc.getCCs().getRendsPotMin().get(combustibleBase).getValor(instante);
		double rendMaxBase1CC = cc.getCCs().getRendsPotMax().get(combustibleBase).getValor(instante);

		cccd.setPotMax1CC(potMax1CC);
		cccd.setPotMin1CC(potMin1CC);
		cccd.setPotTerMinTecCC(cccd.calcPotTerMinTec(potMin1CC, rendMinBase1CC));
		cccd.setPotTerPropCC(cccd.calcPotEspTerProp(potMin1CC, potMax1CC, rendMinBase1CC, rendMaxBase1CC));

		// DATOS QUE VIENEN DE VARIABLES ALEATORIAS

		int cantModTGDisp = cc.getTGs().getCantModDisp().getValor().intValue();
		int cantModTVDisp = cc.getCCs().getCantModDisp().getValor().intValue();
//		System.out.println("NOMBRE: " + gt.getNombre() + "CANTMOD: " + cantModDisp);
		cccd.setCantModDispTGs(cantModTGDisp);
		cccd.setCantModDispTVs(cantModTVDisp);

	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// String valComp = this.getValsCompGeneral().get(Constantes.COMPCC);

	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		/** TODO, ACA HAY QUE HACER LAS ROTURAS **/

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {

		boolean res = true;

		return res;

	}

	@Override
	public void inicializarParaEscenario() {
//		GeneradorTermico participante = (GeneradorTermico) this.getParticipante();
//
//		String valCompGen = this.getValsCompGeneral().get(Constantes.COMPMINTEC);
//		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
//			participante.getCantModIni().cargarValorInicial();
//		}
	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		this.getCompdespacho().getParametros().putAll(comps);

	}

	/**
	 * En la heuróstica se mantiene la cantidad de módulos iniciales conectada. Si
	 * la cantidad de módulos a arrancar y parar son VC discretas exhaustivas no se
	 * requiere esta heuróstica porque se hacen las pruebas exhaustivas
	 */
	public void contribuirAS0fint() {

	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN
	//////////////////////// ////////////////

	public CicloCombinado getCc() {
		return cc;
	}

	public void setCc(CicloCombinado cc) {
		this.cc = cc;
	}

	public CicloCombCompDesp getCccd() {
		return cccd;
	}

	public void setCccd(CicloCombCompDesp cccd) {
		this.cccd = cccd;
	}

	public CicloCombComp getCompG() {
		return compG;
	}

	public void setCompG(CicloCombComp compG) {
		this.compG = compG;
	}

	@Override
	public void actualizarOtrosDatosIniciales() {
		// TODO Auto-generated method stub

	}

	@Override
	/**
	 * Calcula el costo variable de OyM no incluye combustible
	 * 
	 * @param salidaUltimaIter
	 * @return
	 * 
	 *         Relación de potencia entre el CC a plena carga y la suma de las TGs a
	 *         plena carga, cuando todas las unidades están disponibles. pot TGCC =
	 *         potencia del ciclo combinado
	 * 
	 *         relPot = pot TGCC / pot TGs ciclo abierto (todo a plena carga) relPot
	 *         = (pot TGs + pot CVs) / potTGs potCVs = (relPot - 1) potTGs potTGs =
	 *         pot TGCC/ relPot
	 * 
	 *         Se calcula en el programa a partir de los datos de entrada de los
	 *         atributos tGs y tVs de potencia máxima y cantidad de unidades. Se usa
	 *         para calcular aproximadamente la energía de los ciclos de vapor, a
	 *         partir de la energía de las TGs combinadas, INCLUSO CUANDO NO ESTÁN A
	 *         PLENA CARGA.
	 */
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		/**
		 * El costo del combustible del tórmico se atribuye a los contratos de su
		 * combustible. Acá va sólo el variable
		 */
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		double costo = 0.0;
		for (int i = 0; i < cc.getCantPostes(); i++) {
			costo += (salidaUltimaIter.getSolucion().get(cccd.getNpotTGp()[i])
					+ salidaUltimaIter.getSolucion().get(cccd.getNpotCCp()[i]) / relPot) * cc.getDuracionPostes(i)
					/ Constantes.SEGUNDOSXHORA * cc.getTGs().getCostoVariable().getValor(instanteActual); // el costo
																											// variable
																											// estó en
																											// USD/MWh
			costo += salidaUltimaIter.getSolucion().get(cccd.getNpotCCp()[i]) * cc.getDuracionPostes(i) * (relPot - 1)
					/ Constantes.SEGUNDOSXHORA * cc.getCCs().getCostoVariable().getValor(instanteActual);

		}
	//	System.out.println("costo paso " + this.getCc().getNombre() + " : " + costo);
		return costo;

	}

	/**
	 * Este mótodo se usa sólo para las salidas, para estimar un costo de cada
	 * combustible para el tórmico
	 * 
	 * @param salidaUltimaIter
	 * @param instante         instante del paso para valor de Evoluciones
	 * @return un costo para cada combustible Atribuye el costo total de combustible
	 *         en proporción del consumo de cada central
	 */
	public double[] calculaCostoDeCombustiblesPaso(DatosSalidaProblemaLineal salidaUltimaIter, long instante) {

		double[] resultado = new double[cc.getCombustibles().size()];
		Set<String> keyset = cc.getCombustibles().keySet();
		Iterator<String> it = keyset.iterator();
		int indCombus = 0;
		// Calcula el costo total y el caudal total de cada combustible
		while (it.hasNext()) {
			double volumenTotalCombustible = 0;
			double costoTotalCombustible = 0;
			double volumenTotalEsteTer = 0;
			String nombComb = it.next();
			RedCombustible redComb = cc.getSimPaso().getCorrida().getRedesCombustible().get(nombComb);
			Combustible comb = redComb.getCombustible();
			Collection<ContratoCombustible> contratos = redComb.getContratos().values();

			Iterator<ContratoCombustible> itCont = contratos.iterator();

			while (itCont.hasNext()) {
				ContratoCombustible contrato = itCont.next();
				String nombVarControlCaudal = "";
				if (contrato instanceof ContratoCombustibleCanio) {
					ContratoCombustibleCanio contratoCanio = (ContratoCombustibleCanio) contrato;
					nombVarControlCaudal = contratoCanio.getCompD().getNCaudalComb();
					if (salidaUltimaIter.getSolucion().get(nombVarControlCaudal) == null) {
						System.out.println("No se entontró variable del caudal de combustible" + nombVarControlCaudal);
						if (CorridaHandler.getInstance().isParalelo()) {
							////PizarronRedis pp = new PizarronRedis();
							// pp.matarServidores();
						}
						System.exit(1);
					}
				} else {
					System.out.println("Error en clase de contrato " + contrato.getNombre());
					if (CorridaHandler.getInstance().isParalelo()) {
						////PizarronRedis pp = new PizarronRedis();
						// pp.matarServidores();
					}
					System.exit(1);
				}
				volumenTotalCombustible += salidaUltimaIter.getSolucion().get(nombVarControlCaudal)
						* cc.getDuracionPaso() / Constantes.SEGUNDOSXHORA;
				costoTotalCombustible += volumenTotalCombustible * contrato.costoMedio(instante);
			}

			for (int ip = 0; ip < cc.getCantPostes(); ip++) {
				String nombreEnerTpc = cccd.getNenerTpc(ip, nombComb);
				volumenTotalEsteTer += salidaUltimaIter.getSolucion().get(nombreEnerTpc) / comb.getPci();
			}
			if (volumenTotalCombustible == 0) {
				resultado[indCombus] = 0.0;
			} else {
				resultado[indCombus] = volumenTotalEsteTer / volumenTotalCombustible * costoTotalCombustible;
			}
			indCombus++;
		}

		return resultado;

	}

	/**
	 * Este mótodo se usa sólo para las salidas, para estimar para un térmico
	 * double[] volC; // volumen de combustible usado en el paso para cada
	 * combustible en unidades de combustible double[] enerTC; // energía térmica
	 * usada en el paso para cada combustible (MWh) double[][] enerTPC; // energía
	 * térmica por poste y por combustile, primer índice combustible (MWh) double[]
	 * enerEC; // energía eléctrica generada en el paso para cada combustible (MWh)
	 * double[] costoC; // costo variable en el paso para cada combustible (USD/MWh)
	 * double[][] volPC; // volumen de combustible usado en el paso para cada poste
	 * para cada combustible, primer índice poste segundo índice combustible
	 * (unidades de comb.) double[][] potEPC; // energía eléctrica generada en cada
	 * poste del paso para cada combustible, primer índice poste, segundo índice
	 * combustible (MWh)
	 *
	 * @param salidaUltimaIter
	 * @param instante         instante del paso para valor de Evoluciones
	 * @return DatosTermicoSPAuxComb que junta los resultados de esos arrays
	 * 
	 *         Se atribuye el costo total de combustible en proporción del consumo
	 *         de cada central
	 */
	public DatosTermicoSPAuxComb calculaResultadosDeCombustiblesPaso(DatosSalidaProblemaLineal salidaUltimaIter,
			long instante) {

		ArrayList<String> listaComb = cc.getListaCombustibles();
		int cantP = cc.getCantPostes();
		int cantC = listaComb.size();
		double[] volC = new double[cantC];
		double[] enerTC = new double[cantC];
		double[][] enerTPC = new double[cantP][cantC];
		double[] enerEC = new double[cantC];
		double[] costoC = new double[cantC];
		;
		double[][] volPC = new double[cantP][cantC];
		double[][] potEPC = new double[cantP][cantC];

		double[] enerTP = new double[cantP]; // energía térmica total del poste
		int indCombus = 0;
		// Calcula el costo total y el caudal total de cada combustible de todos los
		// tórmicos
		for (String nombComb : listaComb) {
			double volumenTotalCombustible = 0;
			double costoTotalCombustible = 0;
			double volumenTotalEsteTer = 0;
			RedCombustible redComb = cc.getSimPaso().getCorrida().getRedesCombustible().get(nombComb);
			Combustible comb = redComb.getCombustible();
			Collection<ContratoCombustible> contratos = redComb.getContratos().values();

			Iterator<ContratoCombustible> itCont = contratos.iterator();

			while (itCont.hasNext()) {
				ContratoCombustible contrato = itCont.next();
				String nombVarControlCaudal = "";
				if (contrato instanceof ContratoCombustibleCanio) {
					ContratoCombustibleCanio contratoCanio = (ContratoCombustibleCanio) contrato;
					nombVarControlCaudal = contratoCanio.getCompD().getNCaudalComb();
					// resultado[indCombus] +=
					// salidaUltimaIter.getSolucion().get(nombVarControlCaudal) *
					// contrato.getPrecio().getValor();
					if (salidaUltimaIter.getSolucion().get(nombVarControlCaudal) == null) {
						System.out.println("No se entontró variable del caudal de combustible" + nombVarControlCaudal);
						if (CorridaHandler.getInstance().isParalelo()) {
							////PizarronRedis pp = new PizarronRedis();
							// pp.matarServidores();
						}
						System.exit(1);
					}
				} else {
					System.out.println("Error en clase de contrato " + contrato.getNombre());
					if (CorridaHandler.getInstance().isParalelo()) {
						////PizarronRedis pp = new PizarronRedis();
						// pp.matarServidores();
					}
					System.exit(1);
				}
				volumenTotalCombustible += salidaUltimaIter.getSolucion().get(nombVarControlCaudal)
						* cc.getDuracionPaso() / Constantes.SEGUNDOSXHORA;
				costoTotalCombustible += volumenTotalCombustible * contrato.costoMedio(instante);
			}
			for (int ip = 0; ip < cc.getCantPostes(); ip++) {
				String nombreEnerTpc = cccd.getNenerTpc(ip, nombComb);
				double ener = salidaUltimaIter.getSolucion().get(nombreEnerTpc);
				enerTPC[ip][indCombus] = ener;
				volPC[ip][indCombus] = ener / comb.getPci();
				volumenTotalEsteTer += ener / comb.getPci();
				enerTC[indCombus] += ener;
				enerTP[ip] += ener;
			}
			if (volumenTotalCombustible == 0) {
				costoC[indCombus] = 0.0;
				volC[indCombus] = 0.0;
			} else {
				volC[indCombus] = volumenTotalEsteTer;
				costoC[indCombus] = (volumenTotalEsteTer / volumenTotalCombustible) * costoTotalCombustible;
			}
			indCombus++;
		}

		indCombus = 0;
		for (String nombComb : listaComb) {
			for (int ip = 0; ip < cc.getCantPostes(); ip++) {
				String nombreVarPot = cccd.generarNombre("pot", Integer.toString(ip));
				double potp = salidaUltimaIter.getSolucion().get(nombreVarPot);
				if (enerTP[ip] == 0.0) {
					potEPC[ip][indCombus] = 0.0;
				} else {
					potEPC[ip][indCombus] = potp * (enerTPC[ip][indCombus] / enerTP[ip]);
					enerEC[indCombus] += potp * (enerTPC[ip][indCombus] / enerTP[ip]) * cc.getDuracionPostes(ip)
							/ utilitarios.Constantes.SEGUNDOSXHORA;
				}
			}
			indCombus++;
		}
		DatosTermicoSPAuxComb resultado = new DatosTermicoSPAuxComb(volC, enerTC, enerTPC, enerEC, costoC, volPC,
				potEPC);
		return resultado;
	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);

	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		// TODO: OJOJOJOJO VER ACA NO ENTEND
		cargarDatosCompDespacho(instante);
//		System.out.println("Generador: "+ this.gt.getNombre() + " CantModDisp:" + this.gt.getCantModDisp().getValor() );
	}

	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		// TODO Auto-generated method stub

	}

}
