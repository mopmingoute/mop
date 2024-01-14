/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosUsoMaquinaLT is part of MOP.
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

import java.util.Calendar;
import java.util.GregorianCalendar;
 
public class DatosUsoMaquinaLT {
	private GregorianCalendar fechaIni;
	private GregorianCalendar fechaFin;
	private int cantModInst;
	private double potInst;
	
	public DatosUsoMaquinaLT(GregorianCalendar fechaIni, GregorianCalendar fechaFin, int cantModInst, double potInst) {
		super();
		this.fechaIni = fechaIni;
		this.fechaFin = fechaFin;
		this.cantModInst = cantModInst;
		this.potInst = potInst;
	}
	public GregorianCalendar getFechaIni() {
		return fechaIni;
	}
	public void setFechaIni(GregorianCalendar fechaIni) {
		this.fechaIni = fechaIni;
	}
	public GregorianCalendar getFechaFin() {
		return fechaFin;
	}
	public void setFechaFin(GregorianCalendar fechaFin) {
		this.fechaFin = fechaFin;
	}
	public int getCantModInst() {
		return cantModInst;
	}
	public void setCantModInst(int cantModInst) {
		this.cantModInst = cantModInst;
	}
	public double getPotInst() {
		return potInst;
	}
	public void setPotInst(double potInst) {
		this.potInst = potInst;
	}

	public String toString(){
		String res = "";
		res += fechaIni.get(Calendar.MONTH)+1+"/"+fechaIni.get(Calendar.YEAR);
		res += "|";
		res += fechaFin == null ? "FIN_TIEMPO" : fechaFin.get(Calendar.MONTH)+1+"/"+fechaFin.get(Calendar.YEAR);
		res += "|";
		res += String.valueOf(cantModInst);
		res += "|";
		res += String.valueOf(potInst);
		return res;
	}
	
}
