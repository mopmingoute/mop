/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoIntCompDespPE is part of MOP.
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

import compdespacho.EolicoCompDesp;
import cp_datatypesEntradas.DatosContIntCP;
import cp_datatypesEntradas.DatosDemandaCP;
import cp_datatypesEntradas.DatosEolicoCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import cp_nuevosParticipantesCP.ContratoIntSist;
import parque.Demanda;
import parque.GeneradorEolico;
import utilitarios.Constantes;

public class ContratoIntCompDespPE extends CompDespPE {
	
	private String nomPar;
	private ContratoIntSist ci;
	private DatosContIntCP dCP;
	private String nomVAFactor;
	
	
	@Override
	public void completaConstruccion() {
		nomPar = participante.getNombre();
		ci = (ContratoIntSist)participante;

	}

	
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosContIntCP)dpcp;		
	}
	
	@Override
	public void crearBasesVar() {
		for(int p=0; p<cantPos; p++) {													
			BaseVar bV = new BaseVar(ConCP.POT, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, dCP.getPotencia(), null, true);
			cargaBaseVar(bV, ConCP.POT, nomPar, p);				
		}				
	}

	@Override
	public void crearBasesRest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void crearBaseObj() {
		for(int p=0; p<cantPos; p++) {			
			double coef = -(double)dur1Pos*dCP.getValorTotalDesp()/(double)Constantes.SEGUNDOSXHORA;		
			BaseTermino bt = new BaseTermino(ConCP.POT, dCP.getNombrePart(), p, coef, null, grafo);
			agrega1BTalObj(bt);
		}
	}


	public DatosContIntCP getdCP() {
		return dCP;
	}


	public void setdCP(DatosContIntCP dCP) {
		this.dCP = dCP;
	}


	
}
