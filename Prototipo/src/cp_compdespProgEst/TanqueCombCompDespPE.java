/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TanqueCombCompDespPE is part of MOP.
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

import compdespacho.RamaCompDesp;
import compdespacho.TanqueCombCompDesp;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import parque.Rama;
import parque.TanqueCombustible;
import utilitarios.Constantes;

public class TanqueCombCompDespPE extends CompDespPE{
	
	private TanqueCombCompDesp compDesp;
	
	private TanqueCombustible ta;
	
	private Double cantIni;
	/** Cantidad inicial en unidad de combustible del tanque */
	private Double valComb;
	/** Valor del combustible */

	private String nvarvol;
	private String ncantfin;
	private double capacidad;
	
	@Override
	public void completaConstruccion() {
		capacidad = ta.getCapacidad();
		
	}
	
	
	@Override
	public void crearBasesVar() {
		for(int p=0; p<cantPos; p++) {														
			BaseVar bV = new BaseVar(ConCP.VARVOL, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCLIBRE, false, 
					-capacidad, null, capacidad, null, true);
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



	public TanqueCombCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(TanqueCombCompDesp compDesp) {
		this.compDesp = compDesp;
	}

	public TanqueCombustible getTa() {
		return ta;
	}

	public void setTa(TanqueCombustible ta) {
		this.ta = ta;
	}

	public Double getCantIni() {
		return cantIni;
	}

	public void setCantIni(Double cantIni) {
		this.cantIni = cantIni;
	}

	public Double getValComb() {
		return valComb;
	}

	public void setValComb(Double valComb) {
		this.valComb = valComb;
	}

	public String getNvarvol() {
		return nvarvol;
	}

	public void setNvarvol(String nvarvol) {
		this.nvarvol = nvarvol;
	}

	public String getNcantfin() {
		return ncantfin;
	}

	public void setNcantfin(String ncantfin) {
		this.ncantfin = ncantfin;
	}





	
	


}
