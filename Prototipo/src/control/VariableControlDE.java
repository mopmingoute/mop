/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableControlDE is part of MOP.
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

package control;

import java.util.ArrayList;

import datatypes.DatosVariableControlDE;
import tiempo.Evolucion;
import utilitarios.Constantes;

public class VariableControlDE extends VariableControl {

	private int codigoControl; // Ordinal del control elegido dentro de la discretización

	/**
	 * Peróodo en segundos entre instantes en que la variable de control estó activa
	 * y puede actuar. El control estó activo en los instantes móltiplos de peróodo.
	 * Si en un paso de tiempo de simulación u optimización hay un instante al menos
	 * con control activo, el control estó activo en todo el paso. La pertenencia
	 * del instante a un paso es con el criterio |------), cerrado por izquierda. Si
	 * se pone peróodo muy chico por ejemplo 1, la variable estó activa en todos los
	 * pasos.
	 */
	private int periodo;

	private boolean activa; // almacena el resultado de la óltima invocación del mótodo estaActiva()

	/**
	 * Para cada control posible el costo instantóneo en USD, supuesto cargado en el
	 * inicio del paso.
	 */
	private Evolucion<Double[]> costoDeControl;

	/**
	 * Ordinales en la discretización de los controles factibles en el paso
	 * corriente En la optimización, los carga el AportanteControlDE al que
	 * pertenece la variable
	 */
	private ArrayList<Integer> controlesFactibles;

	public VariableControlDE() {
		super();
	}

	public VariableControlDE(DatosVariableControlDE DT) {
		this.setNombre(DT.getNombre());
		this.setCostoDeControl(DT.getCostoDeControl());
		this.setPeriodo(DT.getPeriodo() * Constantes.SEGUNDOSXHORA); // peróodo en segundos
		controlesFactibles = new ArrayList<Integer>();
	}

	public int getCodigoControl() {
		return codigoControl;
	}

	public void setCodigoControl(int codigoControl) {
		this.codigoControl = codigoControl;
	}

	/**
	 * 
	 * @param codigo      es un entero que representa un ordinal de la
	 *                    discretización de la variable de control
	 * @param instanteRef
	 */
	public void cargaValorAPartirDeCodigo(int codigo, long instanteRef) {
		this.codigoControl = codigo;
		this.setControlAnterior(this.getControl());
		this.setControl(this.getEvolDiscretizacion().getValor(instanteRef).getValores().get(codigo));
	}

	/**
	 * Devuelve true si en el paso que comienza en instanteIni y tiene duración
	 * durPaso el control estó activo, con el criterio |----) Carga en la variable
	 * this el atributo activa.
	 * 
	 * @param instante
	 * @return
	 */
	public boolean estaActiva(long instanteIni, int durPaso) {
		int per = periodo;
		long cocienteIni = instanteIni / per;
		long instanteFin = instanteIni + durPaso;
		long cocienteFin = instanteFin / per;

		if (cocienteFin > cocienteIni + 1) {
			activa = true;
		} else if (cocienteFin == cocienteIni) {
			activa = false;
			if (instanteIni % per == 0)
				activa = true;
		} else {
			// cocienteFin = cocienteIni+1
			activa = true;
			if (instanteFin % per == 0 && instanteIni % per != 0)
				activa = false;
		}
		return activa;
	}

	/**
	 * Devuelve el costo del control de óndice indiceControl si el paso en que se
	 * ejerce comienza en instanteIni
	 * 
	 * @param instanteIni
	 * @param indiceControl
	 * @return
	 */
	public double devuelveCostoControl(int instanteIni, int indiceControl) {
		Double[] costos = costoDeControl.getValor(instanteIni);
		return costos[indiceControl];
	}

	/**
	 * Devuelve los ordinales en la discretización de los controlesDE factibles de
	 * la variable. Esos controles deben estar ya cargados en la variable por el
	 * AportanteControlDE al que pertenece la variable
	 * 
	 * @return
	 */
	public int[] devuelveControlesFactibles() {
		int[] result = new int[controlesFactibles.size()];
		for (int i = 0; i < controlesFactibles.size(); i++) {
			result[i] = controlesFactibles.get(i);
		}
		return result;
	}

	public int getPeriodo() {
		return periodo;
	}

	public void setPeriodo(int i) {
		this.periodo = i;
	}

	public Evolucion<Double[]> getCostoDeControl() {
		return costoDeControl;
	}

	public void setCostoDeControl(Evolucion<Double[]> costoDeControl) {
		this.costoDeControl = costoDeControl;
	}

	public ArrayList<Integer> getControlesFactibles() {
		return controlesFactibles;
	}

	public void setControlesFactibles(ArrayList<Integer> controlesFactibles) {
		this.controlesFactibles = controlesFactibles;
	}

	public boolean isActiva() {
		return activa;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}

}
