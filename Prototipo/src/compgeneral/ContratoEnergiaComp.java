/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoEnergiaComp is part of MOP.
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

package compgeneral;

import compdespacho.ContratoEnergiaCompDesp;
import compsimulacion.ContratoEnergiaCompSim;
import parque.ContratoEnergia;

public class ContratoEnergiaComp extends CompGeneral {

	private ContratoEnergiaCompSim compS;
	private ContratoEnergiaCompDesp compD;
	private ContratoEnergia contratoEnergia;

	public ContratoEnergiaComp(ContratoEnergia ce) {
		this.setParticipante(ce);
		compS = (ContratoEnergiaCompSim) ce.getCompSimulacion();
		compD = (ContratoEnergiaCompDesp) ce.getCompDesp();
		this.contratoEnergia = ce;
	}

	public ContratoEnergiaComp(ContratoEnergia ce, ContratoEnergiaCompDesp acd, ContratoEnergiaCompSim acs) {
		super();
		this.contratoEnergia = ce;
		compS = acs;
		compD = acd;
	}

	public ContratoEnergiaCompSim getCompS() {
		return compS;
	}

	public void setCompS(ContratoEnergiaCompSim compS) {
		this.compS = compS;
	}

	public ContratoEnergiaCompDesp getCompD() {
		return compD;
	}

	public void setCompD(ContratoEnergiaCompDesp compD) {
		this.compD = compD;
	}

	public ContratoEnergia getContratoEnergia() {
		return contratoEnergia;
	}

	public void setContratoEnergia(ContratoEnergia contratoEnergia) {
		this.contratoEnergia = contratoEnergia;
	}

}
