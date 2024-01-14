/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosReporteGUI is part of MOP.
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


import java.util.HashMap;

public class DatosReporteGUI {
	private String titulo;
	private Integer tipoReporte;
	private HashMap<Integer,DatosResumenGUI> resumenes;
	private HashMap<Integer,DatosGraficaGUI> graficas;
	
	public DatosReporteGUI(String titulo, Integer tipoReporte, HashMap<Integer,DatosResumenGUI> resumenes, HashMap<Integer,DatosGraficaGUI> graficas) {
		super();
		this.titulo = titulo;
		this.tipoReporte = tipoReporte;
		this.resumenes = resumenes;
		this.graficas = graficas;
	}
	
	public DatosReporteGUI() {
		super();
		this.titulo = "Inicial";
		this.tipoReporte = DatosEspecificacionReporte.REP_RES_ENER;
		this.resumenes = new HashMap<Integer, DatosResumenGUI>();
		this.graficas = new HashMap<Integer, DatosGraficaGUI>();
	}
	
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public Integer getTipoReporte() {
		return tipoReporte;
	}
	public void setTipoReporte(Integer tipoReporte) {
		this.tipoReporte = tipoReporte;
	}
	public HashMap<Integer,DatosResumenGUI> getResumenes() {
		return resumenes;
	}
	public void setResumenes(HashMap<Integer,DatosResumenGUI> resumenes) {
		this.resumenes = resumenes;
	}
	public HashMap<Integer,DatosGraficaGUI> getGraficas() {
		return graficas;
	}
	public void setGraficas(HashMap<Integer,DatosGraficaGUI> graficas) {
		this.graficas = graficas;
	} 

}
