/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FotovoltaicoCompDesp is part of MOP.
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

import parque.GeneradorFotovoltaico;
import utilitarios.Constantes;

import java.util.ArrayList;

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

public class FotovoltaicoCompDesp extends GeneradorCompDesp {

	private GeneradorFotovoltaico ge;
	private double potenciaMaxima;
	private double[] listaPotenciaPorPoste;
	private Integer cantModDisp;

	public FotovoltaicoCompDesp() {
		super();
		setGe((GeneradorFotovoltaico) this.getParticipante());

	}

	public FotovoltaicoCompDesp(GeneradorFotovoltaico ge) {
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
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		ArrayList<Double> variablesPoste = null;
		ArrayList<Double> potsDisp = null;

		boolean variables = this.ge.getSimPaso().getCorrida().isCostosVariables();

		if (this.ge.getSimPaso().isSimulando() && variables) {
			variablesPoste = new ArrayList<Double>();
			potsDisp = new ArrayList<Double>();
		}

		DatosObjetivo costo = new DatosObjetivo();
		for (int p = 0; p < ge.getCantPostes(); p++) {
			costo.agregarTermino(this.npotp[p],
					ge.getCostoVariable().getValor(instanteActual) * ge.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
			if (this.ge.getSimPaso().isSimulando() && variables) {
				variablesPoste
						.add(ge.getCostoVariable().getValor(instanteActual) * ge.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
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

	public GeneradorFotovoltaico getGe() {
		return ge;
	}

	public void setGe(GeneradorFotovoltaico ge) {
		this.ge = ge;
	}

	public void setCantModDisp(Integer cantModDisp) {
		this.cantModDisp = cantModDisp;

	}

	public Integer getCantModDisp() {
		return cantModDisp;
	}

}