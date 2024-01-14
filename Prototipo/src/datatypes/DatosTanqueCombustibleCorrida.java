/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTanqueCombustibleCorrida is part of MOP.
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
 * Datatype que representa los datos de un tanque de combustible
 * @author ut602614
 *
 */
public class DatosTanqueCombustibleCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private Integer cantModulos; 
	private Integer cantModDisponibles;
	private String nombre;
	private Double capacidad;
	private String barra;
	private Double cantIni;
	private Double valComb;
	

	public DatosTanqueCombustibleCorrida(String nombre, Integer cantModIni,
			Integer cantModDisponibles,  Double capacidad,
			String barra, Double cantIni, Double valComb) {
		super();
		this.cantModulos = cantModIni;
		this.cantModDisponibles = cantModDisponibles;
		this.nombre = nombre;
		this.capacidad = capacidad;
		this.barra = barra;
		this.cantIni = cantIni;
		this.valComb = valComb;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Double getCapacidad() {
		return capacidad;
	}
	public void setCapacidad(Double capacidad) {
		this.capacidad = capacidad;
	}
	public String getBarra() {
		return barra;
	}
	public void setBarra(String barra) {
		this.barra = barra;
	}
	public Integer getCantModIni() {
		return cantModulos;
	}
	public void setCantModIni(Integer cantModIni) {
		this.cantModulos = cantModIni;
	}
	public Double getValComb() {
		return valComb;
	}
	public void setValComb(Double valComb) {
		this.valComb = valComb;
	}
	public Double getCantIni() {
		return cantIni;
	}
	public void setCantIni(Double cantIni) {
		this.cantIni = cantIni;
	}
	public Integer getCantModDisponibles() {
		return cantModDisponibles;
	}
	public void setCantModDisponibles(Integer cantModDisponibles) {
		this.cantModDisponibles = cantModDisponibles;
	}
 
}
