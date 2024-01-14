/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombCanioCompDespPE is part of MOP.
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

import compdespacho.ContratoCombCanioCompDesp;
import compdespacho.TermicoCompDesp;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import parque.ContratoCombustibleCanio;
import parque.GeneradorTermico;
import utilitarios.Constantes;

public class ContratoCombCanioCompDespPE extends CompDespPE{
	
	private ContratoCombCanioCompDesp compDesp;
	
	private ContratoCombustibleCanio ccc;
	
	private String nomCom;
	private String nomPar;
	
	private Double precioComb;  // USD por unidades de combustible
	private Double caudalMax; // en unidades de combustible por hora
	

	@Override
	public void completaConstruccion() {
		ccc = (ContratoCombustibleCanio)participante;
		precioComb = ccc.getPrecio().getValor(instIniCP);
		caudalMax = ccc.getCaudalMaximo().getValor(instIniCP);
		nomCom = ccc.getCombustible().getNombre();
		nomPar = ccc.getNombre();
	}
	

	@Override
	public void crearBasesVar() {
		for(int p=0; p<cantPos; p++) {														
			BaseVar bV = new BaseVar(ConCP.CAUDAL + "_" + nomCom, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, caudalMax*dur1Pos/Constantes.SEGUNDOSXHORA, null, true);
			cargaBaseVar(bV, ConCP.CAUDAL + "_" + nomCom, nomPar, p);		
		}		
	}

	@Override
	public void crearBasesRest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearBaseObj() {
		double coef = precioComb*dur1Pos/Constantes.SEGUNDOSXHORA;
		for(int p=0; p<cantPos; p++) {				
			BaseTermino bt = new BaseTermino(ConCP.CAUDAL+ "_" + nomCom, nomPar, p, coef, null, grafo);
			agrega1BTalObj(bt);
		}
		
	}
	


	public ContratoCombCanioCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(ContratoCombCanioCompDesp compDesp) {
		this.compDesp = compDesp;
	}



	public ContratoCombustibleCanio getCcc() {
		return ccc;
	}

	public void setCcc(ContratoCombustibleCanio ccc) {
		this.ccc = ccc;
	}


	public Double getPrecioComb() {
		return precioComb;
	}


	public void setPrecioComb(Double precioComb) {
		this.precioComb = precioComb;
	}

	public Double getCaudalMax() {
		return caudalMax;
	}


	public void setCaudalMax(Double caudalMax) {
		this.caudalMax = caudalMax;
	}

	


}
