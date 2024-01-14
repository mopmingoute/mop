/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosIteracionesCorrida is part of MOP.
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
 * Datatype que representa los datos asociados a las iteraciones por paso de una corrida
 * @author ut602614
 *
 */

public class DatosIteracionesCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private Integer maximoIteraciones;
	private Integer numIteraciones;
	private String criterioParada;
	
	
	public DatosIteracionesCorrida(Integer maximoIteraciones, Integer numIteraciones,
			String criterioParada) {
		
		this.maximoIteraciones = maximoIteraciones;
		this.criterioParada = criterioParada;
		this.numIteraciones = numIteraciones;
	}
	public DatosIteracionesCorrida() {
		// TODO Auto-generated constructor stub
	}
	public Integer getMaximoIteraciones() {
		return maximoIteraciones;
	}
	public void setMaximoIteraciones(Integer maximoIteraciones) {
		this.maximoIteraciones = maximoIteraciones;
	}
	public String getCriterioParada() {
		return criterioParada;
	}
	public void setCriterioParada(String criterioParada) {
		this.criterioParada = criterioParada;
	}
	public Integer getNumIteraciones() {
		return numIteraciones;
	}
	public void setNumIteraciones(Integer numIteraciones) {
		this.numIteraciones = numIteraciones;
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(criterioParada.trim().equals("")) errores.add("Datos Iteraciones: criterioParada vacío.");
		if(maximoIteraciones == null || maximoIteraciones == 0) errores.add("Datos Iteraciones: maximoIteraciones vacío.");
		if(numIteraciones == null || numIteraciones == 0) errores.add("Datos Iteraciones: numIteraciones vacío.");
		return errores;
	}
}
