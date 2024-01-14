/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConjuntoDeSeries is part of MOP.
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

package procesosEstocasticos;

import java.util.Hashtable;

public class ConjuntoDeSeries {
	private String nombre;
	private String[] nombresSeries;
	private Hashtable<String, Serie> series;
	private String nombrePaso;
	private int cantCron;
	
	
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String[] getNombresSeries() {
		return nombresSeries;
	}
	public void setNombresSeries(String[] nombresSeries) {
		this.nombresSeries = nombresSeries;
	}
	public Hashtable<String, Serie> getSeries() {
		return series;
	}
	public void setSeries(Hashtable<String, Serie> series) {
		this.series = series;
	}
	public String getNombrePaso() {
		return nombrePaso;
	}
	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}
	
	public int getCantCron() {
		return cantCron;
	}
	public void setCantCron(int cantCron) {
		this.cantCron = cantCron;
	}
	public ConjuntoDeSeries(String nombre, String[] nombresSeries, Hashtable<String, Serie> series, String nombrePaso, int cantCron) {
		super();
		this.nombre = nombre;
		this.nombresSeries = nombresSeries;
		this.series = series;
		this.nombrePaso = nombrePaso;
		this.cantCron = cantCron;
	}
	
	

}
