/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosValRezagadosCP is part of MOP.
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

package cp_datatypesEntradas;

import java.util.Hashtable;

public class DatosValRezagadosCP {
	
	
	public DatosValRezagadosCP() {
		valRezagados = new Hashtable<String, double[]>();
		
	}
	
	Hashtable<String, double[]> valRezagados;

	public Hashtable<String, double[]> getValRezagados() {
		return valRezagados;
	}

	public void setValRezagados(Hashtable<String, double[]> valRezagados) {
		this.valRezagados = valRezagados;
	}
	
}
