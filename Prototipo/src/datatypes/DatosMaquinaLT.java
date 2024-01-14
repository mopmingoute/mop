/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosMaquinaLT is part of MOP.
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


public class DatosMaquinaLT implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;	
	private int tipo;	
	private ArrayList<DatosUsoMaquinaLT> usos;
	
	public DatosMaquinaLT(String nombre, int tipo) {
		super();
		this.nombre = nombre;
		this.tipo = tipo;
		this.usos = new ArrayList<>();
	}
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public ArrayList<DatosUsoMaquinaLT> getUsos() {
		return usos;
	}

	public void setUsos(ArrayList<DatosUsoMaquinaLT> usos) {
		this.usos = usos;
	}

}
