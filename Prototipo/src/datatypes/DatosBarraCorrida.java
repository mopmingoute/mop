/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosBarraCorrida is part of MOP.
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

/**
 * 
 * @author ut602614 Datatype representando la barra de la red elóctrica
 */

public class DatosBarraCorrida implements Serializable {

	private String nombre;
	/** Nombre de la barra */
	private boolean flotante;
	private boolean salDetallada;

	public DatosBarraCorrida(String nombre, boolean salDetallada) {
		super();
		this.nombre = nombre;
		this.flotante = false;
		this.setSalDetallada(salDetallada);
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public boolean isFlotante() {
		return flotante;
	}

	public void setFlotante(boolean flotante) {
		this.flotante = flotante;
	}

	public boolean isSalDetallada() {
		return salDetallada;
	}

	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if (nombre.trim().equals("")) {
			errores.add("DatosBarraCorrida: Nombre vacío.");
		}
		return errores;
	}

}
