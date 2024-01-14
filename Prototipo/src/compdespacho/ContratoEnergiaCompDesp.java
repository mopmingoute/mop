/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoEnergiaCompDesp is part of MOP.
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

import java.util.Hashtable;

import parque.ContratoEnergia;

public class ContratoEnergiaCompDesp extends CompDespacho {
	
	private ContratoEnergia contratoEnergia;

	
	public ContratoEnergiaCompDesp(ContratoEnergia ce) {
		super();
		this.setParticipante(ce);
		this.setContratoEnergia(ce);
		this.parametros = new Hashtable<String, String>();		

	}
	
	
	public ContratoEnergia getContratoEnergia() {
		return contratoEnergia;
	}

	public void setContratoEnergia(ContratoEnergia contratoEnergia) {
		this.contratoEnergia = contratoEnergia;
	}

	@Override
	public void crearVariablesControl() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarRestricciones() {
		// TODO Auto-generated method stub

	}

	@Override
	public void contribuirObjetivo() {
		// TODO Auto-generated method stub

	}

}
