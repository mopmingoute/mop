/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosParamSalidaSim is part of MOP.
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

package datatypesSalida;

import java.io.Serializable;

public class DatosParamSalidaSim implements Serializable{
	
	

	private static final long serialVersionUID = 1L;
	private int escIni;
	private int escFin;
	private int pasoIni;
	private int pasoFin;
	private boolean salSim;
	
	public DatosParamSalidaSim() {
		super();
	}
	

	public DatosParamSalidaSim(int escIni, int escFin, int pasoIni, int pasoFin, boolean sal) {
		super();
		this.escIni = escIni;
		this.escFin = escFin;
		this.pasoIni = pasoIni;
		this.pasoFin = pasoFin;
		this.salSim = sal;
	}


	
	public int getEscIni() {
		return escIni;
	}


	public void setEscIni(int escIni) {
		this.escIni = escIni;
	}


	public int getEscFin() {
		return escFin;
	}


	public void setEscFin(int escFin) {
		this.escFin = escFin;
	}


	public int getPasoIni() {
		return pasoIni;
	}


	public void setPasoIni(int pasoIni) {
		this.pasoIni = pasoIni;
	}


	public int getPasoFin() {
		return pasoFin;
	}


	public void setPasoFin(int pasoFin) {
		this.pasoFin = pasoFin;
	}


	public boolean isSalSim() {
		return salSim;
	}


	public void setSalSim(boolean salSim) {
		this.salSim = salSim;
	}


	

	
	
	
	
	

}
