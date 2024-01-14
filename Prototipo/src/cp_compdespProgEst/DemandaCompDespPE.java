/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DemandaCompDespPE is part of MOP.
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

import compdespacho.DemandaCompDesp;
import cp_datatypesEntradas.DatosDemandaCP;
import cp_datatypesEntradas.DatosImpoExpoCP;
import cp_datatypesEntradas.DatosPartCP;
import parque.Demanda;
import parque.ImpoExpo;

public class DemandaCompDespPE extends CompDespPE{
	private DemandaCompDesp compDesp;
	private Demanda dem;
	private DatosDemandaCP dCP;
	
	private String nomPar; // nombre del participante
	
	private String nomVADem;
	
	
	@Override
	public void completaConstruccion() {
		nomVADem = dCP.getNomVADem();		
	}


	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosDemandaCP)dpcp;
		dem = (Demanda)this.participante;
	}

	public DemandaCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(DemandaCompDesp compDesp) {
		this.compDesp = compDesp;
	}

	public Demanda getDem() {
		return dem;
	}

	public void setDem(Demanda dem) {
		this.dem = dem;
	}





	public String getNomPar() {
		return nomPar;
	}

	public void setNomPar(String nomPar) {
		this.nomPar = nomPar;
	}

	public String getNomVADem() {
		return nomVADem;
	}

	public void setNomVADem(String nomVADem) {
		this.nomVADem = nomVADem;
	}

	@Override
	public void crearBasesVar() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearBasesRest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearBaseObj() {
		// TODO Auto-generated method stub
		
	}




	

}
