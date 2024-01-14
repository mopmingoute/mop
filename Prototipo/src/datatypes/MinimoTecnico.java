/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * MinimoTecnico is part of MOP.
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
 * Datatype que representa las especificaciones del mónimo tócnico
 * @author ut602614
 *
 */
public class MinimoTecnico implements Serializable{
	private static final long serialVersionUID = 1L;
	static final String PASO = "PASO";
	static final String POSTE = "POSTE";
	
	private String tipoMinimo;
	private Double valor;
	public String getTipoMinimo() {
		return tipoMinimo;
	}
	public void setTipoMinimo(String tipoMinimo) {
		this.tipoMinimo = tipoMinimo;
	}
	public Double getValor() {
		return valor;
	}
	public void setValor(Double valor) {
		this.valor = valor;
	}
		
}
