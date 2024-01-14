/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCombustibleCorrida is part of MOP.
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
 * Datatype que representa el combustible
 * @author ut602614
 *
 */
public class DatosCombustibleCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;												/**Nombre del combustible*/
	private String unidad;												/**Unidad de combustible*/
	private Double pciPorUnidad;										/**Pci por unidad*/
	private Double densidad;											/**Densidad del combustible*/
	private DatosRedCombustibleCorrida red;								/**Datos de la red de combustible asociada*/	
	private boolean salDetallada;

	public DatosCombustibleCorrida(String nombre, String unidad,
			Double pciPorUnidad, Double densidad, boolean salDetallada) {
		super();
		this.nombre = nombre;
		this.unidad = unidad;
		this.pciPorUnidad = pciPorUnidad;
		this.densidad = densidad;
		this.red = new DatosRedCombustibleCorrida();
		this.red.setNombre(this.nombre);
		this.setSalDetallada(salDetallada);
		
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getUnidad() {
		return unidad;
	}
	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}
	public Double getPciPorUnidad() {
		return pciPorUnidad;
	}
	public void setPciPorUnidad(Double pciPorUnidad) {
		this.pciPorUnidad = pciPorUnidad;
	}
	public DatosRedCombustibleCorrida getRed() {
		return red;
	}
	public void setRed(DatosRedCombustibleCorrida red) {
		this.red = red;
	}
	public Double getDensidad() {
		return densidad;
	}
	public void setDensidad(Double densidad) {
		this.densidad = densidad;
	}
	public boolean isSalDetallada() {
		return salDetallada;
	}
	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(nombre.trim().equals("")) { errores.add("Combustible: Nombre vacío."); }
		if(unidad.trim().equals("")) { errores.add("Combustible " + nombre + ": Unidad vacío."); }
		if(pciPorUnidad== null || pciPorUnidad == 0 ) { errores.add("Combustible " +nombre + ": Pci por Unidad vacío."); }
		if(densidad == null || densidad == 0 ) { errores.add("Combustible " +nombre + ": Densidad vacío."); }
		if(red == null ) { errores.add("Combustible " +nombre + ": Red vacío."); }
		else {
			if(red. controlDatosCompletos().size() > 0 ) { errores.add("Combustible " +nombre + ": Red incompleto."); }
		}

		return errores;
	}
}

