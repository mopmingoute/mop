/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FallaCompSim is part of MOP.
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

import compdespacho.FallaCompDesp;
import compgeneral.FallaComp;
import datatypes.Pair;
import parque.Demanda;
import parque.Falla;
import utilitarios.Constantes;
import datatypesProblema.DatosSalidaProblemaLineal;
import interfacesParticipantes.AportantePost;

public class FallaCompSim extends CompSimulacion implements AportantePost {
	private FallaCompDesp fcd;
	private Falla falla;
	private FallaComp fcg;

	public FallaCompSim() {
		super();
		this.fcd = (FallaCompDesp) this.getCompdespacho();
		this.falla = (Falla) this.getParticipante();
		this.fcg = (FallaComp) this.getCompgeneral();
	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		fcd.setNxe(new String[falla.getCantPostes()]);
		fcd.setNpotpe(new String[falla.getCantPostes()][]);
		fcd.setCostosArranque(new double[falla.getCantPostes()]);
		if (!getValsCompGeneral().get(Constantes.COMPFALLA).equalsIgnoreCase(Constantes.FALLASINESTADO))
			fcd.setCantEscForzados(falla.getCantEscForzados().getEstadoDespuesDeCDE().intValue());
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		String valCompFalla = this.getValsCompGeneral().get(Constantes.COMPFALLA);
		if (iter == 1) {
			this.getCompdespacho().getParametros().put(Constantes.COMPFALLA, valCompFalla);
		}
	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		String valCompFalla = this.getValsCompGeneral().get(Constantes.COMPFALLA);
		if (valCompFalla.equalsIgnoreCase(Constantes.FALLA_CONESTADO_CONDUR)) {
			if (falla.getCantEscAForzar().isActiva()) {
				/**
				 * El paso que estó terminando tuvo la variable de control DE activa, por lo
				 * tanto se ha terminado un peróodo de la variable
				 */
				Integer faltantes = falla.getPerForzadosRestantes().getEstado().intValue();
				if (faltantes > 0)
					faltantes--;
				falla.getPerForzadosRestantes().setEstado(faltantes.doubleValue());
			}
		}

		if (!valCompFalla.equalsIgnoreCase(Constantes.FALLASINESTADO))
			falla.getCantEscForzados().setEstado(falla.getCantEscForzados().getEstadoDespuesDeCDE());
	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		return true;
	}

	@Override
	public void inicializarParaEscenario() {
		String valCompFalla = this.getValsCompGeneral().get(Constantes.COMPFALLA);
		if (!valCompFalla.equalsIgnoreCase(Constantes.FALLASINESTADO)) {
			falla.getCantEscForzados().cargarValorInicial();
			if (valCompFalla.equalsIgnoreCase(Constantes.FALLA_CONESTADO_CONDUR)) {
				falla.getPerForzadosRestantes().cargarValorInicial();
			}
		}
	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		this.getCompdespacho().getParametros().putAll(comps);
	}

	@Override
	public ArrayList<Double> aportaParaPost(int sorteo) {
		Falla f = this.getFalla();
		Demanda demanda = f.getDemanda();
		ArrayList<Double> resultado = new ArrayList<Double>();
		boolean residual = f.getSimPaso().getCorrida().getCompDemanda().equalsIgnoreCase(Constantes.DEMRESIDUAL);
		if (!getValsCompGeneral().get(Constantes.COMPFALLA).equalsIgnoreCase(Constantes.FALLASINESTADO) && residual) {

			int forzados;

			double[] muestra;

			if (!demanda.getSimPaso().isSimulando()) {
				muestra = demanda.getDemanda().getUltimoMuestreoOptim()[sorteo];
				forzados = f.getCantEscForzadosOptim().getEstadoDespuesDeCDE().intValue();
			} else {
				forzados = f.getCantEscForzados().getEstado().intValue();
				muestra = demanda.getDemanda().getUltimoMuestreo();
			}

			for (int i = 0; i < muestra.length; i++) {
				double potEsc = 0;
				for (int ef = 0; ef < forzados; ef++) {
					Pair<Double, Double> par = f.getEscalones().get(ef);
					Double poruno = par.first / 100;
					Double potActiva = muestra[i];
					potEsc += poruno * potActiva;

				}
				resultado.add(-potEsc);
			}
		}
		return resultado;
	}

	public FallaCompDesp getFcd() {
		return fcd;
	}

	public void setFcd(FallaCompDesp fcd) {
		this.fcd = fcd;
	}

	public Falla getFalla() {
		return falla;
	}

	public void setFalla(Falla falla) {
		this.falla = falla;
	}

	public FallaComp getFcg() {
		return fcg;
	}

	public void setFcg(FallaComp fcg) {
		this.fcg = fcg;
	}

	public FallaCompSim(Falla falla) {
		super();
		this.setParticipante(falla);

	}

	public FallaCompSim(Falla falla2, FallaCompDesp compD, FallaComp compG) {
		this.falla = falla2;
		this.fcd = compD;
		this.fcg = compG;
	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN ////////////////

	@Override
	public void actualizarOtrosDatosIniciales() {
		// Deliberadamente en blanco.
	}

	/**
	 * Se calcula el costo de la falla, incluso el costo de la decisión de la
	 * variable de control DE
	 * 
	 * @param salidaUltimaIter
	 * @return
	 */
	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		String[][] nPotFalla = fcd.getNpotpe(); // primer óndice poste, segundo poste escalón
		double costoF = 0;
		boolean optim = falla.getOptimPaso().isOptimizando();
		int cantEscForzados = 0;
		if (this.getValsCompGeneral().get(Constantes.COMPFALLA).equalsIgnoreCase(Constantes.FALLASINESTADO)) {
			falla.setCantEscProgram(0);
		}
		if (optim) {
			if (!this.getValsCompGeneral().get(Constantes.COMPFALLA).equalsIgnoreCase(Constantes.FALLASINESTADO))
				cantEscForzados = falla.getCantEscForzadosOptim().getEstadoDespuesDeCDE().intValue();				
		} else {
			if (!this.getValsCompGeneral().get(Constantes.COMPFALLA).equalsIgnoreCase(Constantes.FALLASINESTADO))
				cantEscForzados = falla.getCantEscForzados().getEstadoDespuesDeCDE().intValue();
		}

		for (int ipos = 0; ipos < falla.getCantPostes(); ipos++) {
			for (int e = 0; e < cantEscForzados; e++) {
				double pot = salidaUltimaIter.getSolucion().get(nPotFalla[ipos][e]);
				double costoUnit = falla.dameCostoUnitarioPosteEscalon(ipos, e);
				costoF += pot * costoUnit * falla.getDuracionPostes(ipos) / Constantes.SEGUNDOSXHORA;
			}
			for (int e = falla.getCantEscProgram(); e < falla.getCantEscalones(); e++) {
				double pot = salidaUltimaIter.getSolucion().get(nPotFalla[ipos][e]);
				double costoUnit = falla.dameCostoUnitarioPosteEscalon(ipos, e);
				costoF += pot * costoUnit * falla.getDuracionPostes(ipos) / Constantes.SEGUNDOSXHORA;
			}
		}

//		if(!this.getValsCompGeneral().get(Constantes.COMPFALLA).equalsIgnoreCase(Constantes.FALLASINESTADO)){
//			int instanteIniPaso = falla.getLt().getInstInicPasoCorriente();
//			VariableControlDE vc = falla.getCantEscAForzar();
//			costoF += vc.devuelveCostoControl(instanteIniPaso, vc.getCodigoControl());
//		}
	//	System.out.println("costo paso " + this.getFalla().getNombre() + " : " + costoF);
		return costoF;
	}

	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
		/**
		 * Deliberadamente en blanco. La cantidad de escalones forzados ya estó cargada
		 * La cantidad de periodos restantes se cambió en contribuirAS0fint()
		 */
	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);
	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		fcd.setNxe(new String[falla.getCantPostes()]);
		fcd.setNpotpe(new String[falla.getCantPostes()][]);
		fcd.setCostosArranque(new double[falla.getCantPostes()]);
		if (!getValsCompGeneral().get(Constantes.COMPFALLA).equalsIgnoreCase(Constantes.FALLASINESTADO)) {
			fcd.setCantEscForzados(falla.getCantEscForzadosOptim().getEstadoDespuesDeCDE().intValue());
		}

	}

	public void contribuirAS0fint() {
		String valCompGen = getValsCompGeneral().get(Constantes.COMPFALLA);
		double est = 0;
		double per = 0;
		if (!valCompGen.equalsIgnoreCase(Constantes.FALLASINESTADO)) {
			est = this.falla.getCantEscForzados().getEstadoDespuesDeCDE();
			this.falla.getCantEscForzados().setEstadoS0fint(est);
		}
		if (valCompGen.equalsIgnoreCase(Constantes.FALLA_CONESTADO_CONDUR)) {
			per = this.falla.getPerForzadosRestantes().getEstadoDespuesDeCDE();
			if (per > 0)
				this.falla.getPerForzadosRestantes().setEstadoS0fint((double) (per - 1));
		}
	}

	public void contribuirAS0fintOptim() {
		String valCompGen = getValsCompGeneral().get(Constantes.COMPFALLA);
		double est = 0;
		double per = 0;
		if (!valCompGen.equalsIgnoreCase(Constantes.FALLASINESTADO)) {
			est = this.falla.getCantEscForzadosOptim().getEstadoDespuesDeCDE();
			this.falla.getCantEscForzadosOptim().setEstadoS0fint(est);
		}
		if (valCompGen.equalsIgnoreCase(Constantes.FALLA_CONESTADO_CONDUR)) {
			per = this.falla.getPerForzadosRestantesOptim().getEstadoDespuesDeCDE();
			if (per > 0)
				this.falla.getPerForzadosRestantesOptim().setEstadoS0fint((double) (per - 1));
		}

	}
}
