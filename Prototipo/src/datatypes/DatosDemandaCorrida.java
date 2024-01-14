
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosDemandaCorrida is part of MOP.
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
 * Datatype que representa los datos asociados a una demanda
 * @author ut602614
 *
 */
public class DatosDemandaCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;
	private String barra;
	
	private DatosVariableAleatoria potActiva;
	
	private boolean salDetallada;

	public DatosDemandaCorrida(String nombre, String barra,
			DatosVariableAleatoria potA, boolean salDetallada) {
		super();
		this.nombre = nombre;
		this.barra = barra;
		this.setPotActiva(potA);
		this.salDetallada = salDetallada;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getBarra() {
		return barra;
	}

	public void setBarra(String barra) {
		this.barra = barra;
	}

	public DatosVariableAleatoria getPotActiva() {
		return potActiva;
	}

	public void setPotActiva(DatosVariableAleatoria potActiva) {
		this.potActiva = potActiva;
	}

	public boolean isSalDetallada() {
		return salDetallada;
	}

	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}


	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(nombre.trim().equals("")) errores.add("Demanda: Nombre vacío.");
		if(barra == null) errores.add("Demanda " + nombre + ": Barra vacío.");
		if(potActiva == null){ errores.add("Demanda " + nombre + ": potencia Activa vacía."); }
		else {
			if(potActiva.controlDatosCompletos().size() >0 ){
				errores.add("Demanda " + nombre + ": potencia Activa vacía.");
			}
		}
		return errores;

    }
}

