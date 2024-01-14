/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DemandaCompDesp is part of MOP.
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

import parque.Demanda;

/**
 * Clase encargada de modelar el comportamiento de la demanda en el problema de
 * despacho
 * 
 * @author ut602614
 *
 */

public class DemandaCompDesp extends CompDespacho {

	private Double potenciaActiva;
	/** Potencia activa asociada a la demanda */
	private Double potenciaReactiva;
	/** Potencia reactiva asociada a la demanda */
	private double[] potActivaPorPoste;
	private Demanda demanda;

	public DemandaCompDesp(Demanda demanda) {
		super();
		this.setDemanda(demanda);
//		
	}

	@Override
	public void crearVariablesControl() {

	}

	@Override
	public void cargarRestricciones() {
	}

	@Override
	public void contribuirObjetivo() {
	}

	public Double getPotenciaActiva() {
		return potenciaActiva;
	}

	public void setPotenciaActiva(Double potenciaActiva) {
		this.potenciaActiva = potenciaActiva;
	}

	public Double getPotenciaReactiva() {
		return potenciaReactiva;
	}

	public void setPotenciaReactiva(Double potenciaReactiva) {
		this.potenciaReactiva = potenciaReactiva;
	}

	public double[] getPotActivaPorPoste() {
		return potActivaPorPoste;
	}

	public void setPotActivaPorPoste(double[] potActivaPorPoste) {
		this.potActivaPorPoste = potActivaPorPoste;
	}

	public double getPotActivaPosteP(int p) {
		return this.potActivaPorPoste[p];
	}

	public Demanda getDemanda() {
		return demanda;
	}

	public void setDemanda(Demanda demanda) {
		this.demanda = demanda;
	}

}
