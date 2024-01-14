/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosGraficaGUI is part of MOP.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;


public class DatosGraficaGUI implements Serializable{
	private static final long serialVersionUID = 1L;
	/*
	 * Constantes
	 */
	public static int GRAF_LINEAS =0;
	public static int GRAF_AREAS =1;
	
	private String titulo;
	private String unidadX;
	private String tituloX;
	private String unidadY;
	private String tituloY;
	private Hashtable<String, ArrayList<Pair<Integer, Double>>> series; // el integer es el paso
	private int tipo;
	
		
	public DatosGraficaGUI(String titulo, String unidadX, String tituloX, String unidadY, String tituloY,
			Hashtable<String, ArrayList<Pair<Integer, Double>>> series, int tipo) {
		super();
		this.titulo = titulo;
		this.unidadX = unidadX;
		this.tituloX = tituloX;
		this.unidadY = unidadY;
		this.tituloY = tituloY;
		this.series = series;
		this.tipo = tipo;
	}
	
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getUnidadX() {
		return unidadX;
	}
	public void setUnidadX(String unidadX) {
		this.unidadX = unidadX;
	}
	public String getTituloX() {
		return tituloX;
	}
	public void setTituloX(String tituloX) {
		this.tituloX = tituloX;
	}
	public String getUnidadY() {
		return unidadY;
	}
	public void setUnidadY(String unidadY) {
		this.unidadY = unidadY;
	}
	public String getTituloY() {
		return tituloY;
	}
	public void setTituloY(String tituloY) {
		this.tituloY = tituloY;
	}
	public Hashtable<String, ArrayList<Pair<Integer, Double>>> getSeries() {
		return series;
	}
	public void setSeries(Hashtable<String, ArrayList<Pair<Integer, Double>>> series) {
		this.series = series;
	}
	public int getTipo() {
		return tipo;
	}
	public void setTipo(int tipo) {
		this.tipo = tipo;
	}
	
}
