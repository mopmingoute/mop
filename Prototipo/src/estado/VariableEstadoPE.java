/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableEstadoPE is part of MOP.
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

package estado;

import procesosEstocasticos.ProcesoEstocastico;

public class VariableEstadoPE extends VariableEstado{
	
	private ProcesoEstocastico pe;
	
	public VariableEstadoPE(String nombre) {
		super(nombre);
		// TODO Auto-generated constructor stub
	}
	
	public VariableEstadoPE(String nombre, Discretizacion discretizacion) {
		super(nombre);
		// TODO Auto-generated constructor stub
	}

	public ProcesoEstocastico getPe() {
		return pe;
	}

	public void setPe(ProcesoEstocastico pe) {
		this.pe = pe;
	}
}
