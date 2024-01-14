/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DuctoCombCompDespPE is part of MOP.
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

package cp_compdespProgEst;

import compdespacho.DuctoCombCompDesp;
import compdespacho.TermicoCompDesp;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import parque.BarraCombustible;
import parque.DuctoCombustible;
import parque.GeneradorTermico;
import parque.ImpoExpo;
import utilitarios.Constantes;

public class DuctoCombCompDespPE extends CompDespPE{
	
	private DuctoCombCompDesp compDesp;
	
	private DuctoCombustible dc;
	
	private double capacidad12;
	private double capacidad21;
	private BarraCombustible barra1;
	private BarraCombustible barra2;
	private int cantModDisp;
	private String nomCom;
	
	
	@Override
	public void completaConstruccion() {
		long instIniCP = dGCP.getInstIniCP();
		dc = (DuctoCombustible) this.getParticipante();
		nomPar = dc.getNombre();
		nomCom = barra1.getComb().getNombre();
		
		capacidad12 = dc.getCapacidad12().getValor(instIniCP);
		capacidad21 = dc.getCapacidad21().getValor(instIniCP);
		barra1 = dc.getBarra1();
		barra2 = dc.getBarra2();
		// TODO NO SE TOMA EN CUENTA LA DISPONIBILIDAD DE DUCTOS
		
	}
	
	
	@Override
	public void crearBasesVar() {
		for(int p=0; p<cantPos; p++) {														
			BaseVar bV = new BaseVar(ConCP.FLUJO , nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCLIBRE, false, 
					-capacidad21, null, capacidad12, null, true);
			cargaBaseVar(bV, ConCP.FLUJO, nomPar, p);		
		}
	}

	@Override
	public void crearBasesRest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearBaseObj() {
		// TODO Auto-generated method stub
		
	}
	
	
	public DuctoCombCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(DuctoCombCompDesp compDesp) {
		this.compDesp = compDesp;
	}

	public DuctoCombustible getDc() {
		return dc;
	}

	public void setDc(DuctoCombustible dc) {
		this.dc = dc;
	}


	public double getCapacidad12() {
		return capacidad12;
	}


	public void setCapacidad12(double capacidad12) {
		this.capacidad12 = capacidad12;
	}


	public double getCapacidad21() {
		return capacidad21;
	}


	public void setCapacidad21(double capacidad21) {
		this.capacidad21 = capacidad21;
	}


	public BarraCombustible getBarra1() {
		return barra1;
	}


	public void setBarra1(BarraCombustible barra1) {
		this.barra1 = barra1;
	}


	public BarraCombustible getBarra2() {
		return barra2;
	}


	public void setBarra2(BarraCombustible barra2) {
		this.barra2 = barra2;
	}


	public int getCantModDisp() {
		return cantModDisp;
	}


	public void setCantModDisp(int cantModDisp) {
		this.cantModDisp = cantModDisp;
	}








	
	

}
