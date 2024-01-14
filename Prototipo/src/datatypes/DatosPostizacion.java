/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPostizacion is part of MOP.
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

package datatypes;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class DatosPostizacion {
	/**
	 * PRIMER INDICE: PASO
	 * SEGUNDO INDICE: INTERVALO DE MUESTREO
	 */
	private GregorianCalendar fechaIni;
	private ArrayList<ArrayList<Integer>> colnumpos;
	private int cantPasos;

	public DatosPostizacion() {
		fechaIni = new GregorianCalendar();
		colnumpos = new ArrayList<ArrayList<Integer>>();
	}
	
	public GregorianCalendar getFechaIni() {
		return fechaIni;
	}

	public void setFechaIni(GregorianCalendar fechaIni) {
		this.fechaIni = fechaIni;
	}

	public ArrayList<ArrayList<Integer>> getColnumpos() {
		return colnumpos;
	}

	public void setColnumpos(ArrayList<ArrayList<Integer>> colnumpos) {
		this.colnumpos = colnumpos;
	}

	public void agregarNumpos(ArrayList<Integer> unNumpos) {
		colnumpos.add(unNumpos);
		
	}

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}
	
}
