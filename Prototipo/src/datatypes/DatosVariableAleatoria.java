/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosVariableAleatoria is part of MOP.
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

public class DatosVariableAleatoria implements Serializable{
	private static final long serialVersionUID = 1L;
	String procOptimizacion;
	String procSimulacion;
	String nombre;
	
	public DatosVariableAleatoria(String procOptimizacion, String procSimulacion, String nombre) {
		super();
		this.procOptimizacion = procOptimizacion;
		this.procSimulacion = procSimulacion;
		this.nombre = nombre;
	}
	
	public String getProcOptimizacion() {
		return procOptimizacion;
	}
	public void setProcOptimizacion(String procOptimizacion) {
		this.procOptimizacion = procOptimizacion;
	}
	public String getProcSimulacion() {
		return procSimulacion;
	}
	public void setProcSimulacion(String procSimulacion) {
		this.procSimulacion = procSimulacion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();
		if(procOptimizacion != null && procOptimizacion.trim().equals("")) errores.add("DatosVariableAleatoria: procOptimizacion vacío.");
		if(procSimulacion != null && procSimulacion.trim().equals("")) errores.add("DatosVariableAleatoria: procSimulacion vacío.");
		if(nombre != null && nombre.trim().equals("")) errores.add("Termico: DatosVariableAleatoria vacío.");
		return errores;
	}

}
