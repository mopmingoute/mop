/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EolicoCompDesp is part of MOP.
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

import parque.GeneradorEolico;
import parque.Impacto;
import parque.Participante;
import utilitarios.Constantes;
import java.util.ArrayList;
import java.util.Hashtable;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import datatypesSalida.DatosCurvaOferta;
import datatypesSalida.DatosEPPUnEscenario;
import logica.CorridaHandler;

/**
 * Clase encargada de modelar el comportamiento del generador eólico en el
 * problema de despacho
 * 
 * @author ut602614
 *
 */

public class EolicoCompDesp extends GeneradorCompDesp {

	private GeneradorEolico ge;
	private double potenciaMaxima;
	private double[] listaPotenciaPorPoste;
	private Integer cantModDisp;

	public EolicoCompDesp() {
		super();
		setGe((GeneradorEolico) this.getParticipante());

	}

	public EolicoCompDesp(GeneradorEolico ge) {
		super();
		this.setGe(ge);

	}

	@Override
	public void crearVariablesControl() {
		// Crear potp
		// no requiere cota superior porque se cargan las potencias como
		// restricciones de menor o igual

		for (int p = 0; p < participante.getCantPostes(); p++) {
			this.npotp[p] = generarNombre("potp", Integer.toString(p));

			// COMO VIENE DADA DE AFUERA LA POTENCIA
			// NO SE CONSIDERAN LAS COTAS POR TEMAS NUMÉRICOS Y
			// POSIBLES INFACTIBILIDADES
			// ESTO HACE QUE EN DEFINITIVA NO SE ESTÉ MODELANDO ROTURAS EN
			// ESTE CASO.
			DatosVariableControl nv = new DatosVariableControl(this.npotp[p], Constantes.VCCONTINUA,
					Constantes.VCPOSITIVA, null, null);
			this.variablesControl.put(nv.getNombre(), nv);
		}

	}

	@Override
	public void cargarRestricciones() {

		for (int p = 0; p < participante.getCantPostes(); p++) {
			this.npotp[p] = generarNombre("potp", Integer.toString(p));
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(this.npotp[p], 1.0);
			nr.setSegundoMiembro(Math.max(0, this.listaPotenciaPorPoste[p]));
			nr.setNombre(this.npotp[p]);
			nr.setTipo(Constantes.RESTMENOROIGUAL);
			this.restricciones.put(nr.getNombre(), nr);
		}

	}

	@Override
	public void contribuirObjetivo() {
		boolean variables = this.ge.getSimPaso().getCorrida().isCostosVariables();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		ArrayList<Double> variablesPoste = null;
		ArrayList<Double> potsDisp = null;
		if (this.ge.getSimPaso().isSimulando() && variables) {
			variablesPoste = new ArrayList<Double>();
			potsDisp = new ArrayList<Double>();
		}

		DatosObjetivo costo = new DatosObjetivo();
		for (int p = 0; p < ge.getCantPostes(); p++) {
			costo.agregarTermino(this.npotp[p], ge.getCostoVariable().getValor(instanteActual) * ge.getDuracionPostes(p)
					/ Constantes.SEGUNDOSXHORA);
			if (this.ge.getSimPaso().isSimulando() && variables) {
				variablesPoste.add(ge.getCostoVariable().getValor(instanteActual) * ge.getDuracionPostes(p)
						/ Constantes.SEGUNDOSXHORA);
				potsDisp.add(this.listaPotenciaPorPoste[p]);
			}
		}
		costo.setTerminoIndependiente(ge.getCostoFijo().getValor(instanteActual));
		this.objetivo.contribuir(costo);
		if (this.ge.getSimPaso().isSimulando() && variables) {
			DatosEPPUnEscenario des = this.ge.getSimPaso().getDatosEscenario();
			DatosCurvaOferta dco = des.getCurvOfertas().get(ge.getSimPaso().getPaso());
			dco.agregarVariablesMaquinaPaso(ge.getNombre(), variablesPoste);
			dco.agregarPotsDispMaquinaPaso(ge.getNombre(), potsDisp);
		}

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
			ImpactoCompDesp icd = (ImpactoCompDesp) gh.getCompDesp();

			double limite_caudal = gh.getLimite().getValor(instanteActual); // limite de caudal por debajo del cuál se
																			// penaliza, por
			// poste
			String[] nFaltante = icd.getnExcesop(); // variables de control por poste con el faltante ecológico
			String[] nPotp = this.ge.getCompD().getNpotp(); // variable de control por poste con las potencias

			DatosRestriccion dr;

			/**
			 * para cada poste p: pot_p+faltante(p)*potMax/limite <= potMax
			 */

			for (int p = 0; p < this.ge.getCantPostes(); p++) {
				dr = new DatosRestriccion();
				dr.setNombre(generarNombre("topePot", Integer.toString(p)));
				double tope = ge.getPotenciaMaxima().getValor(instanteActual);

				dr.agregarTermino(nPotp[p], 1.0);
				dr.agregarTermino(nFaltante[p], tope / limite_caudal);
				dr.setSegundoMiembro(tope);
				dr.setTipo(Constantes.RESTMENOROIGUAL);
				rest.put(dr.getNombre(), dr);
			}

		}
		return rest;

	}

	public double[] getListaPotenciaPorPoste() {
		return listaPotenciaPorPoste;
	}

	public void setListaPotenciaPorPoste(double[] listaPotenciaPorPoste) {
		this.listaPotenciaPorPoste = listaPotenciaPorPoste;
	}

	public double getPotenciaMaxima() {
		return potenciaMaxima;
	}

	public void setPotenciaMaxima(double potenciaMaxima) {
		this.potenciaMaxima = potenciaMaxima;
	}

	public GeneradorEolico getGe() {
		return ge;
	}

	public void setGe(GeneradorEolico ge) {
		this.ge = ge;
	}

	public void setCantModDisp(Integer cantModDisp) {
		this.cantModDisp = cantModDisp;

	}

	public Integer getCantModDisp() {
		return cantModDisp;
	}
}
