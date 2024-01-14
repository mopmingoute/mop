/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosVariableControlDE is part of MOP.
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

import tiempo.Evolucion;

public class DatosVariableControlDE implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;
	private int periodo;
	private Evolucion<Double[]> costoDeControl;
	
	public DatosVariableControlDE() {
		super();
	}
	
	public DatosVariableControlDE(String nombre) {
		super();
		this.nombre = nombre;
	}
	
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getPeriodo() {
		return periodo;
	}

	public void setPeriodo(int periodo) {
		this.periodo = periodo;
	}

	public Evolucion<Double[]> getCostoDeControl() {
		return costoDeControl;
	}

	public void setCostoDeControl(Evolucion<Double[]> costoDeControl) {
		this.costoDeControl = costoDeControl;
	}



	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(nombre.trim().equals("")) { errores.add("VariableControlDE: Nombre vacío."); }
		if(periodo == 0) { errores.add("VariableControlDE" + nombre + ": periodo vacío."); }
		if(costoDeControl == null) { errores.add("VariableControlDE" + nombre + ": costoDeControl vacío."); }
		else if(costoDeControl.controlDatosCompletos().size() > 0 ) { errores.add("VariableControlDE" + nombre + ": costoDeControl vacío."); }

		return errores;
	}
	
	

}
