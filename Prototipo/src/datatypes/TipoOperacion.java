/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TipoOperacion is part of MOP.
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

/**
 * Datatype que representa el tipo de operación comercial 
 * @author ut602614
 *
 */
public class TipoOperacion implements Serializable{
	private static final long serialVersionUID = 1L;
	static final String VENTA = "VENTA";
	static final String COMPRA = "COMPRA";
	
	private String tipo;

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
}
