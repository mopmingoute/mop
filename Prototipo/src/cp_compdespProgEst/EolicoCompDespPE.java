/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EolicoCompDespPE is part of MOP.
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

import java.util.ArrayList;

import compdespacho.EolicoCompDesp;
import compdespacho.FallaCompDesp;
import cp_datatypesEntradas.DatosDemandaCP;
import cp_datatypesEntradas.DatosEolicoCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseRest;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import parque.Demanda;
import parque.Falla;
import parque.GeneradorEolico;
import utilitarios.Constantes;

public class EolicoCompDespPE extends GeneradorCompDespPE{
	
	private EolicoCompDesp compDesp;
	private GeneradorEolico ge;
	private DatosEolicoCP dCP;
	private String nomVAFactor;
	
	
	@Override
	public void completaConstruccion() {				
		ge = (GeneradorEolico)participante;
		nomPar = ge.getNombre();
		nomVAFactor = dCP.getNomVAFactor();
	}
	
	
	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosEolicoCP)dpcp;
	}
	
	
	@Override
	public void crearBasesVar() {		
		for(int p=0; p<cantPos; p++) {														
			BaseVar bV = new BaseVar(ConCP.POT, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
					cero, null, Double.MAX_VALUE, null, true);
			cargaBaseVar(bV, ConCP.POT, nomPar, p);			
		}					
	}
	
	@Override
	public void crearBasesRest() {
		long instCorr = dGCP.getInstIniCP();
		for(int p=0; p<cantPos; p++) {		
			double pot = ge.getPotenciaMaxima().getValor(instCorr);
			BaseRest br = new BaseRest(ConCP.RPOTEOL, ge.getNombre(), p, 
					pot, null, null, dCP.getNomVAFactor() , p, Constantes.RESTIGUAL); 
			br.agSumVar(ConCP.POT, nomPar, p, 1.0, null);			
			agrega1BR(generaNomBRest(ConCP.RPOTEOL, nomPar, p), br);
			instCorr += dGCP.getDur1Pos();
		}		
	}
	
	@Override
	public void crearBaseObj() {
		double cos = ge.getCostoVariable().getValor(instIniCP);
		contObjCVarOyMEnergia(cos, ConCP.POT, nomPar);
	}


	
	
	
	public EolicoCompDesp getCompDesp() {
		return compDesp;
	}
	public void setCompDesp(EolicoCompDesp compDesp) {
		this.compDesp = compDesp;
	}
	public GeneradorEolico getGe() {
		return ge;
	}
	public void setGe(GeneradorEolico ge) {
		this.ge = ge;
	}





}
