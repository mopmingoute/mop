/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ImpoExpoCompSim is part of MOP.
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

import compdespacho.ImpoExpoCompDesp;
import compgeneral.ImpoExpoComp;
import datatypesProblema.DatosSalidaProblemaLineal;
import logica.CorridaHandler;
import parque.Corrida;
import parque.ImpoExpo;
import simulacion.ValPostizador;
import utilitarios.Constantes;

public class ImpoExpoCompSim extends CompSimulacion {

	private ImpoExpoCompDesp compD;
	private ImpoExpo ie;
	private ImpoExpoComp compG;

	public ImpoExpoCompSim(ImpoExpoCompDesp compD, ImpoExpo ie, ImpoExpoComp compG) {
		super();
		this.compD = compD;
		this.ie = ie;
		this.compG = compG;
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// Deliberadamente en blanco

	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String, String> compsGlobales) {
		// Deliberadamente en blanco

	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		Corrida corrida = CorridaHandler.getInstance().getCorridaActual();		
		if(corrida.getFase().equalsIgnoreCase(utilitarios.Constantes.FASE_SIM) &&
		   corrida.isDespSinExp()){
			boolean pais = corrida.getPaisesACortar().contains(ie.getPais());
			// Si se está en un país destino que anula corrida y en la iteración sin exportación, crear potencias máxima nulas
			if(iter == corrida.getIteracionSinExp() && pais) compD.setNpotbp(new String[ie.getCantBloques()][ie.getCantPostes()]);
		}
	}

	@Override
	public boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter) {
		return true;
	}

	@Override
	public void cargarDatosCompDespacho(long instante) {
		ValPostizador valP = ie.getSimPaso().getValPostizador();
		cargarDatosCompDespachoAuxiliar(instante, valP);
	}

	@Override
	public void cargarDatosCompDespachoOptim(long instante) {
		ValPostizador valP = ie.getOptimPaso().getValPostizador();
		cargarDatosCompDespachoAuxiliar(instante, valP);
	}

	public void cargarDatosCompDespachoAuxiliar(long instante, ValPostizador valP) {
		// TODO: la creación de abajo no deberóa hacerse siempre
		compD.setNpotbp(new String[ie.getCantBloques()][ie.getCantPostes()]);
		compD.setOperacionCompraVenta(ie.getOperacionCompraVenta());

		compD.setMinTec(ie.getMinTec().getValor(instante));
		/**
		 * Aquó se hace una discusión segón la postización sea interna o externa y segón
		 * el tipo de ImpoExpo
		 */
		// DATOS QUE VIENEN DE EVOLUCIONES
		long[] instantesMuestreo;
		int cantBloques = ie.getCantBloques();
		int cantPostes = ie.getCantPostes();
		double[][] auxPot = new double[cantBloques][cantPostes];
		double[][] auxPre = new double[cantBloques][cantPostes];
		int cantModInst = this.ie.getCantModInst().getValor(instante);
		// Obtiene el valor de la VA uniforme para determinar la disponibilidad
		double valorUnifDisp = 0.0;

		if (!ie.getTipoImpoExpo().equalsIgnoreCase(Constantes.IEALEATPRPOT)) {
			valorUnifDisp = ie.getVaUniforme().getValor();
		}

		if (ie.getTipoImpoExpo().equalsIgnoreCase(Constantes.IEEVOL)) {
			// Impoexpo a partir de Evoluciones
			if (valP.isExterna()) {
				// la valpostización es externa y entonces las evoluciones dan valores por poste
				// externo
				for (int ib = 0; ib < cantBloques; ib++) {
					for (int ip = 0; ip < cantPostes; ip++) {
						if (ie.getDispEvol().get(ib).getValor(instante).get(ip) > valorUnifDisp) {
							auxPot[ib][ip] = ie.getPotEvol().get(ib).getValor(instante).get(ip);
						} else {
							auxPot[ib][ip] = 0;
						}
						auxPre[ib][ip] = ie.getPreEvol().get(ib).getValor(instante).get(ip);
					}
				}
			} else {
				// La valpostización es interna, hay que evaluar la evolución en los instantes
				// de muestreo
				// y valpostizar para cada bloque
				// Hay una ónica Evolucion por bloque
				instantesMuestreo = ie.getInstantesMuestreo();
				int cantInst = instantesMuestreo.length;
				for (int ib = 0; ib < cantBloques; ib++) {
					double[] auxPre1 = new double[cantInst];
					for (int im = 0; im < cantInst; im++) {
						long instIm = instantesMuestreo[im];
						auxPre1[im] = ie.getPreEvol().get(ib).getValor(instIm).get(0);
					}
					auxPre[ib] = valP.valPostizar(auxPre1, Constantes.VALPPROMEDIO, 0.0);
				}
				for (int ib = 0; ib < cantBloques; ib++) {
					double[] auxPot1 = new double[cantInst];
					if (ie.getDispEvol().get(ib).getValor(instante).get(0) > valorUnifDisp) {
						for (int im = 0; im < cantInst; im++) {
							long instIm = instantesMuestreo[im];

							auxPot1[im] = ie.getPotEvol().get(ib).getValor(instIm).get(0);
						}
						auxPot[ib] = valP.valPostizar(auxPot1, Constantes.VALPPROMEDIO, 0.0);
					}
				}
			}
		} else if (ie.getTipoImpoExpo().equalsIgnoreCase(Constantes.IEALEATFORMUL)) {
			// ImpoExpo a partir de una variable aleatoria
			double[] cmg;
			// Obtiene los valores del costo marginal en cada poste
			if (valP.isExterna()) {
				// Para cada bloque hay un Polinomio de potencia, precio y disponibilidad
				cmg = valP.valPostizar(ie.getVaCMg().getPe(), ie.getNombreVACMg());
			} else {
				// La valpostización es interna, hay que muestrear el CMg en
				// los instantes de muestreo y aplicarle un polinomio distinto por bloque
				// para obtener precios y potencias
				// Luego hay que valpostizar, empleando los valores en los intervalos de
				// muestreo de potencia y precio

				cmg = valP.valPostizar(ie.getVaCMg().getUltimoMuestreo(), Constantes.VALPALEAT,
						ie.getVaCMg().getValor());
				// Se multiplican los valores por el factor de escalamiento
				// la evolución podróa variar a lo largo de los instantes de muestreo
				for (int i = 0; i < cmg.length; i++) {
					cmg[i] = cmg[i] * ie.getFactorEscalamiento().getValor(ie.getInstantesMuestreo()[i]);
				}
			}
			for (int ib = 0; ib < cantBloques; ib++) {
				for (int ip = 0; ip < cantPostes; ip++) {
					if (!valP.isExterna()) {
					} else {
					}
					if (ie.getPoliDisp().get(ib).dameValor(cmg[ip]) > valorUnifDisp) {
						auxPot[ib][ip] = ie.getPoliPot().get(ib).dameValor(cmg[ip]);
					} else {
						auxPot[ib][ip] = 0;
					}
					auxPre[ib][ip] = ie.getPoliPre().get(ib).dameValor(cmg[ip]);				
				}
			}
		} else {
			// La ImpoExpo es de tipo IEALEATPRPOT: la potencia y el precio son variables
			// aleatorias diferentes en cada bloque
			for (int ib = 0; ib < cantBloques; ib++) {
				if (valP.isExterna()) {
					auxPot[ib] = valP.valPostizar(ie.getVaPotencia().get(ib).getPe(),
							ie.getNombresVAPotencia().get(ib));
					auxPre[ib] = valP.valPostizar(ie.getVaPrecio().get(ib).getPe(), ie.getNombresVAPrecio().get(ib));
				} else {
					auxPot[ib] = valP.valPostizar(ie.getVaPotencia().get(ib).getUltimoMuestreo(), Constantes.VALPALEAT,
							ie.getVaPotencia().get(ib).getValor());
					auxPre[ib] = valP.valPostizar(ie.getVaPrecio().get(ib).getUltimoMuestreo(), Constantes.VALPALEAT,
							ie.getVaPrecio().get(ib).getValor());
				}

			}
		}

		for (int ib = 0; ib < cantBloques; ib++) {
			for (int ip = 0; ip < cantPostes; ip++) {
				auxPot[ib][ip] = Math.max(0.00001,auxPot[ib][ip]*cantModInst);
			}
		}

		compD.setPotenciabp(auxPot);
		compD.setPreciobp(auxPre);
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// Deliberadamente en blanco

	}

	@Override
	public void inicializarParaEscenario() {
		// Deliberadamente en blanco

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
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		String[][] nomP = compD.getNpotbp(); // nombres de las variables de potencia
		double costo = 0.0;
		double[][] precio = compD.getPreciobp();
		double signo = 1;
		if (ie.getOperacionCompraVenta().equalsIgnoreCase(Constantes.PROVVENTA)) {
			signo = -1;
		} else if (ie.getOperacionCompraVenta().equalsIgnoreCase(Constantes.PROVCOMPRA)) {
			signo = 1;
		}
		for (int ib = 0; ib < ie.getCantBloques(); ib++) {
			for (int ip = 0; ip < ie.getCantPostes(); ip++) {
				double pot = salidaUltimaIter.getSolucion().get(nomP[ib][ip]);
				costo += signo * pot * precio[ib][ip] * ie.getDuracionPostes(ip) / Constantes.SEGUNDOSXHORA;
			}
		}
		return costo;
	}

}