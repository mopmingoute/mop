/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AcumuladorCompSim is part of MOP.
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

import java.util.Hashtable;

import compdespacho.AcumuladorCompDesp;
import compgeneral.AcumuladorComp;
import datatypesProblema.DatosSalidaProblemaLineal;
import estado.VariableEstado;
import logica.CorridaHandler;
import parque.Acumulador;
import utilitarios.Constantes;

public class AcumuladorCompSim extends CompSimulacion {

	private Acumulador ac;
	private AcumuladorCompDesp acd;
	private AcumuladorComp acg;

	public AcumuladorCompSim(Acumulador gen, AcumuladorCompDesp acd, AcumuladorComp acg) {
		super();
		this.setParticipante(gen);
		this.setCompdespacho(acd);
		this.setCompgeneral(acg);
		ac = (Acumulador) this.getParticipante();

		this.acd = (AcumuladorCompDesp) this.getCompdespacho();
		this.acg = (AcumuladorComp) this.getCompgeneral();

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
		// hcd.setCoefEnergetico(new double[gh.getCantPostes()]);

		// DATOS QUE VIENEN DE EVOLUCIONES
		acd.setPotMax(ac.getPotenciaMaxima().getValor(instante));
		acd.setPotMaxAlmac(ac.getPotAlmMax().getValor(instante));
		acd.setEnergMax(ac.getEnergAlmacMax().getValor(instante));
		acd.setEnergAlmacIniPoste(ac.getEnergIniPoste().getValor(instante));
		acd.setEnergAlmacFinPaso(ac.getEnergIniPoste().getValor(instante + this.ac.getDuracionPaso()));
		acd.setNpotp(new String[ac.getCantPostes()]);
		acd.setNpotAlmacp(new String[ac.getCantPostes()]);
		acd.setNpotAux(new String[ac.getCantPostes()]);
		acd.setnEnerAlmacp(new String[ac.getCantPostes()]);
		acd.setHayPotObligatoria(ac.getHayPotObligatoria().getValor(instante));
		acd.setCostoFallaPOblig(ac.getCostoFallaPotOblig().getValor(instante));
		acd.setPotObligatoria(ac.getPotOblig());

		// DATOS QUE VIENEN DE VARIABLES DE ESTADO
		double energini;
		String valCompGen = getValsCompGeneral().get(Constantes.COMPPASO);
		if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {
			if (optim) {
				energini = ac.getEnergAcumuladaOpt().getEstadoDespuesDeCDE();
			} else {
				energini = ac.getEnergAcumulada().getEstadoDespuesDeCDE();
			}
		} else
			energini = 0.0;

		acd.setEnergAlmacIni(energini);
		acd.setFactorUso(ac.getFactorUso().getValor(instante));
		// DATOS QUE VIENEN DE VARIABLES ALEATORIAS
		Integer cantModDisp = ac.getCantModDisp().getValor().intValue();
		acd.setCantModDisp(cantModDisp);

		// Resoptim
		String compGlobalBellman = acd.getParametros().get(Constantes.COMPVALORESBELLMAN);
		if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
			if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {
				if (optim) {
//					AFutura aprox = ac.getOptimEstado().getAproxFuturaInc();
//					if (aprox instanceof AFIncrementos) {
					acd.setValEnerg(ac.getEnergAcumuladaOpt().getValorRecurso().get(0));
//					}
				} else {
//					AFutura aprox = ac.getSimPaso().getAproxFuturaActual();
//					if (aprox instanceof AFIncrementos) {
					acd.setValEnerg(ac.getEnergAcumulada().getValorRecurso().get(0));
//					}
				}
			}
		}
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		String valCompPaso = this.getValsCompGeneral().get(Constantes.COMPPASO);
		if (valCompPaso.equalsIgnoreCase(Constantes.ACUCIERRAPASO))
			this.getCompdespacho().getParametros().put(Constantes.COMPPASO, Constantes.ACUCIERRAPASO);
		else if (valCompPaso.equalsIgnoreCase(Constantes.ACUMULTIPASO))
			this.getCompdespacho().getParametros().put(Constantes.COMPPASO, Constantes.ACUMULTIPASO);
		else if (valCompPaso.equalsIgnoreCase(Constantes.ACUBALANCECRONOLOGICO))
			this.getCompdespacho().getParametros().put(Constantes.COMPPASO, Constantes.ACUBALANCECRONOLOGICO);

	}

	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		String valCompPaso = this.getValsCompGeneral().get(Constantes.COMPPASO);
		if (valCompPaso.equalsIgnoreCase(Constantes.ACUCIERRAPASO))
			this.getCompdespacho().getParametros().put(Constantes.COMPPASO, Constantes.ACUCIERRAPASO);
		else if (valCompPaso.equalsIgnoreCase(Constantes.ACUMULTIPASO))
			this.getCompdespacho().getParametros().put(Constantes.COMPPASO, Constantes.ACUMULTIPASO);
		else if (valCompPaso.equalsIgnoreCase(Constantes.ACUBALANCECRONOLOGICO))
			this.getCompdespacho().getParametros().put(Constantes.COMPPASO, Constantes.ACUBALANCECRONOLOGICO);
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal resultado) {
		// actualizarCaudalesYVolumen(resultado);
		String valCompGen = getValsCompGeneral().get(Constantes.COMPPASO);
		acd.setUltimoResultado(resultado);

		if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {

			double energini = ac.getEnergAcumulada().getEstadoDespuesDeCDE();

			double energincr = acd.calcularEnergIncremental();

			ac.getEnergAcumulada().actualizarEstado(energini + energincr);
		}
	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {

	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		return true;
	}

	@Override

	public void inicializarParaEscenario() {
		if (ac.getEnergAcumulada() != null)
			ac.getEnergAcumulada().cargarValorInicial();

	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> comps) {
		this.getCompdespacho().getParametros().putAll(comps);
	}

	public void contribuirAS0fint() {
		String valCompGen = getValsCompGeneral().get(Constantes.COMPPASO);
		if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {
			double energInicial = ac.getEnergAcumulada().getEstadoDespuesDeCDE();
			double energIncr = acd.calcularEnergIncremental();
			double estadoS0fint = energInicial + energIncr;

			ac.getEnergAcumulada().setEstadoS0fint(estadoS0fint < 0 ? 0 : estadoS0fint);
		}
	}

	public void contribuirAS0fintOptim() {
		String valCompGen = getValsCompGeneral().get(Constantes.COMPPASO);
		if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {

			double energInicial = ac.getEnergAcumuladaOpt().getEstadoDespuesDeCDE();

			double estadoS0fint = energInicial;

			ac.getEnergAcumuladaOpt().setEstadoS0fint(estadoS0fint < 0 ? 0 : estadoS0fint);
		}

	}

	@Override
	public void actualizarOtrosDatosIniciales() {
		// TODO Auto-generated method stub

	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		Double costo = 0.0;
		for (int p = 0; p < ac.getCantPostes(); p++) {
			costo += (salidaUltimaIter.getSolucion().get(acd.getNpotp()[p])
					+ salidaUltimaIter.getSolucion().get(acd.getNpotAlmacp()[p])) * ac.getDuracionPostes(p)
					/ Constantes.SEGUNDOSXHORA * ac.getCostoVariable().getValor(instanteActual);
		}
		return costo;
	}

	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
		String valCompGen = getValsCompGeneral().get(Constantes.COMPPASO);
		acd.setUltimoResultado(salidaUltimaIter);

		if (valCompGen.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {

			double energini = ac.getEnergAcumuladaOpt().getEstadoDespuesDeCDE();
			double energincr = acd.calcularEnergIncremental();

			ac.getEnergAcumuladaOpt().setEstadoFinalOptim(energini + energincr);
		}
	}

	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// Como hay una sola variable de estado continua no se emplea el argumento vec
		String nombreRBalance = ac.getCompD().getNombreRestriccionBalEnergia();
		return resultado.getDuales().get(nombreRBalance);
	}

}
