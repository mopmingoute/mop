/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosResumenGUI is part of MOP.
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
import java.util.HashMap;


public class DatosResumenGUI implements Serializable{
	private static final long serialVersionUID = 1L;
	private String titulo;
	private HashMap<String, HashMap<String,Double>> valores; // hash con valores por columna para cada fila
	
	
	public DatosResumenGUI(String titulo, HashMap<String, HashMap<String,Double>> valores) {
		this.titulo = titulo;
		this.valores = valores;
	}
	
	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public HashMap<String, HashMap<String, Double>> getValores() {
		return valores;
	}

	public void setValores(HashMap<String, HashMap<String, Double>> valores) {
		this.valores = valores;
	}
}
