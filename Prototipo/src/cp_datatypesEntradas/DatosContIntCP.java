/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosContIntCP is part of MOP.
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

package cp_datatypesEntradas;

public class DatosContIntCP extends DatosPartCP {

	private String barra;
	private double potencia;
	private double valorTotalDesp;
	
	public DatosContIntCP(String nombrePart, String tipoPart, String barra, double potencia, double valorTotalDesp) {
		super(nombrePart, tipoPart);
		this.barra = barra;
		this.potencia = potencia;
		this.valorTotalDesp = valorTotalDesp;
	}

	public double getValorTotalDesp() {
		return valorTotalDesp;
	}

	public void setValorTotalDesp(double valorTotalDesp) {
		this.valorTotalDesp = valorTotalDesp;
	}

	public String getBarra() {
		return barra;
	}

	public void setBarra(String barra) {
		this.barra = barra;
	}

	public double getPotencia() {
		return potencia;
	}

	public void setPotencia(double potencia) {
		this.potencia = potencia;
	}

	
	

}
