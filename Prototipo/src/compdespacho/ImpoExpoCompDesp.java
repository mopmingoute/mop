/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ImpoExpoCompDesp is part of MOP.
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

package compdespacho;

import java.util.ArrayList;
import java.util.Hashtable;

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import datatypesSalida.DatosCurvaOferta;
import datatypesSalida.DatosEPPUnEscenario;
import logica.CorridaHandler;
import parque.GeneradorHidraulico;
import parque.Impacto;
import parque.ImpoExpo;
import parque.Participante;
import utilitarios.Constantes;

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;

public class ImpoExpoCompDesp extends CompDespacho {

	private ImpoExpo ie;
	private String npotbp[][]; // primer óndice bloque, segundo índice poste
	private String operacionCompraVenta;
	private double[][] preciobp; // precio en USD/MWh por bloque y poste
	private double[][] potenciabp; // potencia máxima en MW por bloque y poste
	private double minTec;
	private String[] nnmodp; // nombres de las variables enteras cantidad de
	// módulos en paralelo por poste (0 o 1)

	public ImpoExpoCompDesp(ImpoExpo ie) {
		super();
		this.ie = ie;

	}

	@Override
	public void crearVariablesControl() {
		nnmodp = new String[participante.getCantPostes()];
		for (int ib = 0; ib < ie.getCantBloques(); ib++) {
			for (int ip = 0; ip < ie.getCantPostes(); ip++) {
				this.npotbp[ib][ip] = generarNombre("pot", Integer.toString(ib), Integer.toString(ip));
				Double cotaMax = potenciabp[ib][ip];
				if (ie.isMinTec()) {
					cotaMax = null;
				}

				DatosVariableControl nv = new DatosVariableControl(this.npotbp[ib][ip], Constantes.VCCONTINUA,
						Constantes.VCPOSITIVA, null, cotaMax);
				this.variablesControl.put(this.npotbp[ib][ip], nv);
				if (this.ie.isMinTec()) {
					nnmodp[ip] = generarNombre("nmod", Integer.toString(ip));
					nv = new DatosVariableControl(nnmodp[ip], Constantes.VCENTERA, Constantes.VCPOSITIVA, null,
							(double) 1.0);
					this.variablesControl.put(nnmodp[ip], nv);

				}
			}
		}
	}

	@Override
	public void cargarRestricciones() {

		Hashtable<String, DatosRestriccion> nrs = new Hashtable<String, DatosRestriccion>();

		for (int ib = 0; ib < ie.getCantBloques(); ib++) {
			if (this.ie.isMinTec()) {
				for (int p = 0; p < participante.getCantPostes(); p++) {
					DatosRestriccion nr = new DatosRestriccion();
					nr.agregarTermino(npotbp[ib][p], 1.0);
					nr.agregarTermino(nnmodp[p], -this.minTec);
					nr.setNombre(generarNombre("potMin", Integer.toString(p)));
					nr.setSegundoMiembro(0.0);
					nr.setTipo(Constantes.RESTMAYOROIGUAL);
					nrs.put(nr.getNombre(), nr);

					nr = new DatosRestriccion();
					nr.agregarTermino(npotbp[ib][p], -1.0);
					nr.agregarTermino(nnmodp[p], potenciabp[ib][p]);

					nr.setNombre(generarNombre("potMax", Integer.toString(p)));
					nr.setSegundoMiembro(0.0);
					nr.setTipo(Constantes.RESTMAYOROIGUAL);
					nrs.put(nr.getNombre(), nr);
				}

			}
		}
		this.restricciones.putAll(nrs);
	}

	@Override
	public void contribuirObjetivo() {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		boolean variables = this.ie.getSimPaso().getCorrida().isCostosVariables();

		ArrayList<Double> variablesPoste = null;
		ArrayList<Double> potsPoste = null;
		if (this.ie.getSimPaso().isSimulando() && variables) {
			variablesPoste = new ArrayList<Double>();
			potsPoste = new ArrayList<Double>();
		}
		for (int ib = 0; ib < ie.getCantBloques(); ib++) {
			if (this.operacionCompraVenta.equalsIgnoreCase(Constantes.PROVVENTA)) {
				for (int ip = 0; ip < participante.getCantPostes(); ip++) {
					this.objetivo.agregarTermino(npotbp[ib][ip],
							-this.preciobp[ib][ip] * ie.getDuracionPostes(ip) / Constantes.SEGUNDOSXHORA);

					if (this.ie.getSimPaso().isSimulando() && variables) {
						variablesPoste
								.add(-this.preciobp[ib][ip] * ie.getDuracionPostes(ip) / Constantes.SEGUNDOSXHORA);
						potsPoste.add(potenciabp[ib][ip]);
					}
				}
			}
			if (this.operacionCompraVenta.equalsIgnoreCase(Constantes.PROVCOMPRA)) {
				for (int ip = 0; ip < participante.getCantPostes(); ip++) {
					this.objetivo.agregarTermino(npotbp[ib][ip],
							this.preciobp[ib][ip] * ie.getDuracionPostes(ip) / Constantes.SEGUNDOSXHORA);

					if (this.ie.getSimPaso().isSimulando() && variables) {
						variablesPoste.add(this.preciobp[ib][ip] * ie.getDuracionPostes(ip) / Constantes.SEGUNDOSXHORA);
						potsPoste.add(potenciabp[ib][ip]);
					}
				}
			}
			if (this.ie.getSimPaso().isSimulando() && variables) {
				DatosEPPUnEscenario des = this.ie.getSimPaso().getDatosEscenario();
				DatosCurvaOferta dco = des.getCurvOfertas().get(ie.getSimPaso().getPaso());
				dco.agregarVariablesMaquinaPaso(ie.getNombre() + "bloque_" + Integer.toString(ib), variablesPoste);
				dco.agregarPotsDispMaquinaPaso(ie.getNombre() + "bloque_" + Integer.toString(ib), potsPoste);

			}

		}
		DatosObjetivo costo = new DatosObjetivo();
		costo.setTerminoIndependiente(ie.getCostoFijo().getValor(instanteActual));
		this.objetivo.contribuir(costo);

	}

	public double[][] getPreciobp() {
		return preciobp;
	}

	public void setPreciobp(double[][] precio) {
		this.preciobp = precio;
	}

	public double[][] getPotenciabp() {
		return potenciabp;
	}

	public void setPotenciabp(double[][] potenciabp) {
		this.potenciabp = potenciabp;
	}

	public String getOperacionCompraVenta() {
		return operacionCompraVenta;
	}

	public void setOperacionCompraVenta(String operacionCompraVenta) {
		this.operacionCompraVenta = operacionCompraVenta;
	}

	public ImpoExpo getIe() {
		return ie;
	}

	public void setIe(ImpoExpo ie) {
		this.ie = ie;
	}

	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		Hashtable<String, DatosRestriccion> rest = new Hashtable<String, DatosRestriccion>();
		if (impacto.getTipoImpacto() == Constantes.HIDRO_PROD_MAQUINA) {
			Impacto gh = null;
			for (Participante p : impacto.getInvolucrados()) {
				if (p instanceof Impacto) {
					gh = (Impacto) p;
					break;
				}
			}

			double limite_caudal = gh.getLimite().getValor(instanteActual);
			ImpactoCompDesp icd = (ImpactoCompDesp) gh.getCompDesp();
			String[] nFaltante = icd.getnExcesop();
			String[][] nPotp = this.ie.getCompD().getNpotbp();

			DatosRestriccion dr;
			for (int b = 0; b < this.ie.getCantBloques(); b++)
				for (int p = 0; p < this.ie.getCantPostes(); p++) {
					dr = new DatosRestriccion();
					dr.setNombre(generarNombre("topePot", Integer.toString(b), Integer.toString(p)));
					double pmax = ie.getPotEvol().get(b).getValor(instanteActual).get(p);
					dr.agregarTermino(nPotp[b][p], limite_caudal);
					dr.agregarTermino(nFaltante[p], pmax);
					dr.setSegundoMiembro(pmax * limite_caudal);
					dr.setTipo(Constantes.RESTMENOROIGUAL);
					rest.put(dr.getNombre(), dr);
				}

		}
		return rest;
	}

	public String[][] getNpotbp() {
		return npotbp;
	}

	public void setNpotbp(String[][] npotbp) {
		this.npotbp = npotbp;
	}

	public double getMinTec() {
		return minTec;
	}

	public void setMinTec(double minTec) {
		this.minTec = minTec;
	}

	public String[] getNnmodp() {
		return nnmodp;
	}

	public void setNnmodp(String[] nnmodp) {
		this.nnmodp = nnmodp;
	}
}
