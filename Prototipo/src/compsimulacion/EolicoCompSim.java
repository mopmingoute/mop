/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EolicoCompSim is part of MOP.
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

import interfacesParticipantes.AportantePost;
import logica.CorridaHandler;

import java.util.ArrayList;
import java.util.Hashtable;

import parque.GeneradorEolico;
import simulacion.ValPostizador;
import utilitarios.Constantes;
import compdespacho.EolicoCompDesp;
import compgeneral.CompGeneral;
import compgeneral.EolicoComp;
import datatypesProblema.DatosSalidaProblemaLineal;

public class EolicoCompSim extends CompSimulacion implements AportantePost {
	private GeneradorEolico ge;
	private EolicoCompDesp ecd;
	private EolicoComp compG;

	public EolicoCompSim() {
		super();
		this.setGe((GeneradorEolico) this.getParticipante());
		this.setEcd((EolicoCompDesp) this.getCompdespacho());
	}

	public EolicoCompSim(GeneradorEolico ge, EolicoCompDesp ecd, EolicoComp compG) {
		super();
		this.setGe(ge);
		this.setEcd(ecd);
		this.setCompG(compG);
	}

	@Override
	public ArrayList<Double> aportaParaPost(int sorteo) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		ArrayList<Double> resultado = new ArrayList<Double>();

		double[] muestra;

		if (!ge.getSimPaso().isSimulando()) {
			muestra = ge.getFactor().getUltimoMuestreoOptim()[sorteo];
		} else {
			muestra = ge.getFactor().getUltimoMuestreo();
		}

		boolean residual = this.ge.getSimPaso().getCorrida().getCompDemanda().equalsIgnoreCase(Constantes.DEMRESIDUAL);

		for (int i = 0; i < muestra.length; i++) {
			if (residual)
				resultado.add(-muestra[i] * ge.getPotenciaMaxima().getValor(instanteActual));
			else
				resultado.add(0.0);
		}

		return resultado;
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {

	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		ValPostizador valP = ge.getSimPaso().getValPostizador();
		cargarDatosCompDespachoAuxiliar(instante, valP);

	}

	/**
	 * Mótodo auxiliar que es llamado tanto por cargarDatosCompDespacho como por
	 * cargarDatosCompDespachoOptim, las llamadas difieren en el origen del
	 * Valpostizador
	 * 
	 * @param instante
	 * @param valP
	 */
	public void cargarDatosCompDespachoAuxiliar(long instante, ValPostizador valP) {
		ecd.setListaPotenciaPorPoste(new double[ge.getCantPostes()]);

		// DATOS QUE VIENEN DE EVOLUCIONES
		ecd.setPotenciaMaxima(ge.getPotenciaMaxima().getValor(instante));
		ecd.setNpotp(new String[ge.getCantPostes()]);

		// DATOS QUE VIENEN DE VARIABLES DE ESTADO

		// DATOS QUE VIENEN DE VARIABLES ALEATORIAS
		double[] listaPots;
		if (valP.isExterna()) {
			listaPots = valP.valPostizar(ge.getFactor().getPe(), ge.getNombreVA());
		} else {

			listaPots = valP.valPostizar(ge.getFactor().getUltimoMuestreo(), Constantes.VALPALEAT,
					ge.getFactor().getValor());

		}

		for (int i = 0; i < ge.getCantPostes(); i++) {
			listaPots[i] *= ge.getPotenciaMaxima().getValor(instante);
		}
		// System.out.println(ge.getNombre()+": "+Arrays.toString(listaPots));
		Integer cantModDisp = ge.getCantModDisp().getValor().intValue();
		ecd.setCantModDisp(cantModDisp);
		ecd.setListaPotenciaPorPoste(listaPots);

		// DATOS QUE VIENEN DEL RESOPTIM

	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		String compValoresBellman = CompGeneral.getCompsGlobales().get(Constantes.COMPVALORESBELLMAN);
		this.getCompdespacho().getParametros().put(Constantes.COMPVALORESBELLMAN, compValoresBellman);
	}

	public GeneradorEolico getGe() {
		return ge;
	}

	public void setGe(GeneradorEolico ge) {
		this.ge = ge;
	}

	public EolicoCompDesp getEcd() {
		return ecd;
	}

	public void setEcd(EolicoCompDesp ecd) {
		this.ecd = ecd;
	}

	public EolicoComp getCompG() {
		return compG;
	}

	public void setCompG(EolicoComp compG) {
		this.compG = compG;
	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN ////////////////

	@Override
	public void actualizarOtrosDatosIniciales() {
		// TODO Auto-generated method stub

	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		Double costo = 0.0;
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		for (int p = 0; p < ge.getCantPostes(); p++) {
			costo += salidaUltimaIter.getSolucion().get(ecd.getNpotp()[p]) * ge.getDuracionPostes(p)
					* ge.getCostoVariable().getValor(instanteActual) / Constantes.SEGUNDOSXHORA;
		}
	//	System.out.println("costo paso " + this.getGe().getNombre() + " : " + costo);
		return costo;
	}

//	@Override
//	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);

	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		ValPostizador valP = ge.getOptimPaso().getValPostizador();
		cargarDatosCompDespachoAuxiliar(instante, valP);

	}

	////////////////////////////////////////////////////////////////////////////////

}
