/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConstructorHiperplanosCompDespPE is part of MOP.
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

import compdespacho.ConstructorHiperplanosCompDesp;
import compdespacho.EolicoCompDesp;
import cp_datatypesEntradas.DatosConstHipCP;
import cp_datatypesEntradas.DatosHidroCP;
import cp_datatypesEntradas.DatosPartCP;
import parque.ConstructorHiperplanos;
import parque.GeneradorEolico;

public class ConstructorHiperplanosCompDespPE extends CompDespPE{
	
	private ConstructorHiperplanosCompDesp compDesp;
	private ConstructorHiperplanos ch;
	
	private DatosConstHipCP dCP;
	
	
	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosConstHipCP)dpcp;	
	}
		
	
	public ConstructorHiperplanosCompDesp getCompDesp() {
		return compDesp;
	}
	public void setCompDesp(ConstructorHiperplanosCompDesp compDesp) {
		this.compDesp = compDesp;
	}
	public ConstructorHiperplanos getCh() {
		return ch;
	}
	public void setCh(ConstructorHiperplanos ch) {
		this.ch = ch;
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
	@Override
	public void completaConstruccion() {
		// TODO Auto-generated method stub
		
	}


	
	

}