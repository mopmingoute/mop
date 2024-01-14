/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AcumuladorCompDespPE is part of MOP.
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

import compdespacho.AcumuladorCompDesp;
import compdespacho.EolicoCompDesp;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosPartCP;
import parque.Acumulador;
import parque.GeneradorEolico;

public class AcumuladorCompDespPE extends GeneradorCompDespPE {
	

	
	private AcumuladorCompDesp compDesp;
	private Acumulador ac;
	
	
	public AcumuladorCompDesp getCompDesp() {
		return compDesp;
	}
	public void setCompDesp(AcumuladorCompDesp compDesp) {
		this.compDesp = compDesp;
	}
	public Acumulador getAc() {
		return ac;
	}
	public void setAc(Acumulador ac) {
		this.ac = ac;
	}


	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		// TODO Auto-generated method stub
		
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
