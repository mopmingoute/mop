/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosHidroSalida is part of MOP.
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

/**
 * Datatype que representa el turbinado y vertido asociado a un generador hidróulico. Representa una tabla del esquema de base de datos
 * 
 * @author ut602614
 *
 */

public class DatosHidroSalida {
	private Double turbinado;
	private Double vertido;
	public Double getTurbinado() {
		return turbinado;
	}
	public void setTurbinado(Double turbinado) {
		this.turbinado = turbinado;
	}
	public Double getVertido() {
		return vertido;
	}
	public void setVertido(Double vertido) {
		this.vertido = vertido;
	}
	
}
