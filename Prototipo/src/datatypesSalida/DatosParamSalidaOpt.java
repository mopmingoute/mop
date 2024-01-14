/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosParamSalidaOpt is part of MOP.
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

public class DatosParamSalidaOpt implements Serializable {

	private static final long serialVersionUID = 1L;
	private int[] estadoIni;
	private int[] estadoFin;
	private int pasoIni;
	private int pasoFin;
	private int sortIni;
	private int sortFin;
	private boolean salOpt;

	public DatosParamSalidaOpt() {
		super();
	}

	public DatosParamSalidaOpt(int[] estadoIni, int[] estadoFin, int pasoIni, int pasoFin, int sortIni, int sortFin,
			boolean salOpt) {
		super();
		this.estadoIni = estadoIni;
		this.estadoFin = estadoFin;
		this.pasoIni = pasoIni;
		this.pasoFin = pasoFin;
		this.sortIni = sortIni;
		this.sortFin = sortFin;
		this.salOpt = salOpt;
	}

	public int[] getEstadoIni() {
		return estadoIni;
	}

	public void setEstadoIni(int[] estadoIni) {
		this.estadoIni = estadoIni;
	}

	public int[] getEstadoFin() {
		return estadoFin;
	}

	public void setEstadoFin(int[] estadoFin) {
		this.estadoFin = estadoFin;
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

	public int getSortIni() {
		return sortIni;
	}

	public void setSortIni(int sortIni) {
		this.sortIni = sortIni;
	}

	public int getSortFin() {
		return sortFin;
	}

	public void setSortFin(int sortFin) {
		this.sortFin = sortFin;
	}

	public boolean isSalOpt() {
		return salOpt;
	}

	public void setSalOpt(boolean salOpt) {
		this.salOpt = salOpt;
	}

}
