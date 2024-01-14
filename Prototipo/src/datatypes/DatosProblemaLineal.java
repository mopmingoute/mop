/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosProblemaLineal is part of MOP.
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

import datatypesProblema.DatosEntradaProblemaLineal;
import datatypesProblema.DatosSalidaProblemaLineal;

/**
 * Datatype que contiene los datos de entrada y salida asociados a un problema lineal
 * @author ut602614
 *
 */

public class DatosProblemaLineal {
	private DatosEntradaProblemaLineal entrada;
	private DatosSalidaProblemaLineal salida;

	public DatosSalidaProblemaLineal getSalida() {
		return salida;
	}

	public void setSalida(DatosSalidaProblemaLineal salida) {
		this.salida = salida;
	}

	public DatosEntradaProblemaLineal getEntrada() {
		return entrada;
	}

	public void setEntrada(DatosEntradaProblemaLineal entrada) {
		this.entrada = entrada;
	}
	
}
