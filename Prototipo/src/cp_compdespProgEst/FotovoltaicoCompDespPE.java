/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FotovoltaicoCompDespPE is part of MOP.
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

import compdespacho.FotovoltaicoCompDesp;
import compdespacho.TermicoCompDesp;
import parque.GeneradorFotovoltaico;
import parque.GeneradorTermico;

public class FotovoltaicoCompDespPE extends GeneradorCompDespPE{
	
	private FotovoltaicoCompDesp compDesp;
	
	private GeneradorFotovoltaico gf;

	public FotovoltaicoCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(FotovoltaicoCompDesp compDesp) {
		this.compDesp = compDesp;
	}

	public GeneradorFotovoltaico getGf() {
		return gf;
	}

	public void setGf(GeneradorFotovoltaico gf) {
		this.gf = gf;
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
