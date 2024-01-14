/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCorridaSalida is part of MOP.
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

package datatypesProblema;

import java.util.ArrayList;
import datatypes.DatosLocalizadorSalida;

/**
 * Datatype que representa la salida establecida en el esquema de Base de datos
 * . Solamente se definen los datos relevantes para el prototipo
 * 
 * @author ut602614
 * 
 */
public class DatosCorridaSalida {
	private String nombre;
	private String directorio;
	private Double probabilidad;
	private ArrayList<DatosLocalizadorSalida> localizadores;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDirectorio() {
		return directorio;
	}

	public void setDirectorio(String directorio) {
		this.directorio = directorio;
	}

	public Double getProbabilidad() {
		return probabilidad;
	}

	public void setProbabilidad(Double probabilidad) {
		this.probabilidad = probabilidad;
	}

	public ArrayList<DatosLocalizadorSalida> getLocalizadores() {
		return localizadores;
	}

	public void setLocalizadores(ArrayList<DatosLocalizadorSalida> localizadores) {
		this.localizadores = localizadores;
	}

}
