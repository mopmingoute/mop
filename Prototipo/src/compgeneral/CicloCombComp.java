/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CicloCombComp is part of MOP.
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

import compdespacho.CicloCombCompDesp;
import compsimulacion.CicloCombCompSim;
import parque.CicloCombinado;

public class CicloCombComp extends CompGeneral {

	private CicloCombCompSim compS;
	private CicloCombCompDesp compD;
	private CicloCombinado cc;

	public CicloCombComp() {

	}

	public CicloCombComp(CicloCombinado ccomb) {
		this.setParticipante(ccomb);
		compS = ccomb.getCompS();
		compD = ccomb.getCompD();
		setCc(ccomb);
	}

	public CicloCombComp(CicloCombinado ccomb, CicloCombCompDesp tcd, CicloCombCompSim tcs) {
		this.setParticipante(ccomb);
		compS = tcs;
		compD = tcd;
		cc = ccomb;
	}

	public CicloCombCompSim getCompS() {
		return compS;
	}

	public CicloCombCompDesp getCompD() {
		return compD;
	}

	public CicloCombinado getCc() {
		return cc;
	}

	public void setCompS(CicloCombCompSim compS) {
		this.compS = compS;
	}

	public void setCompD(CicloCombCompDesp compD) {
		this.compD = compD;
	}

	public void setCc(CicloCombinado cc) {
		this.cc = cc;
	}

	public void actualizarVarsEstadoSimulacion() {
//		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPMINTEC);
//		getVarsEstadoSimulacion().clear();
//		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
//			getVarsEstadoSimulacion().add(gt.getCantModIni());
//		}
//		
	}

	public void actualizarVarsEstadoOptimizacion() {
//		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPMINTEC);
//		getVarsEstadoSimulacion().clear();
//		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
//			getVarsEstadoSimulacion().add(gt.getCantModIniOpt());
//		}		
	}

	public void cargarValVEOptimizacion() {
//		String valCompGen = compS.getValsCompGeneral().get(Constantes.COMPMINTEC);
//		if (valCompGen.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {			
//			gt.getCantModIniOpt().setEstadoS0fint(gt.getCantModIni().getEstadoS0fint());
//			gt.getCantModIniOpt().setEstado(gt.getCantModIni().getEstado());
//			gt.getCantModIniOpt().setEstadoDespuesDeCDE(gt.getCantModIni().getEstadoDespuesDeCDE());			
//		}		

	}

}
