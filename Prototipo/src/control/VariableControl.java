/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableControl is part of MOP.
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

package control;

import estado.Variable;

/**
 * Datatype que representa los datos de una variable de control asociada al
 * problema lineal
 * 
 * @author ut602614
 *
 */
public class VariableControl extends Variable {

	private Integer dominio;
	/** POSITIVA, LIBRE, SEMICONTINUA */
	private Double control;
	private Double controlAnterior;

	public Integer getDominio() {
		return dominio;
	}

	public void setDominio(Integer dominio) {
		this.dominio = dominio;
	}

	public Double getControl() {
		return control;
	}

	public void setControl(Double control) {
		this.control = control;
	}

	public Double getControlAnterior() {
		return controlAnterior;
	}

	public void setControlAnterior(Double controlAnterior) {
		this.controlAnterior = controlAnterior;
	}

}