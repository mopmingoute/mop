/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosDesagregadorEstDiaHora is part of MOP.
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

package datatypesProcEstocasticos;

import java.util.ArrayList;
import java.util.Hashtable;

import utilitarios.ParReales;

public class DatosDesagregadorEstDiaHora {
	
	/**
	 * Primer Hashtable
	 * Clave: nombre de la barra
	 * Valor: datos de potencia de la barra
	 * 
	 * Segundo Hashtable
	 * Clave: año (ej: 2025)
	 * Valor: para cada hora del año la potencia de la barra
	 */
	private Hashtable<String, Hashtable<Integer, double[]>> potenciasBarras;
	
	
	/**
	 * Clave: año
	 * Valor: para cada hora del año la potencia total
	 */
	private Hashtable<Integer, double[]> potenciaTotal;
	
	private ArrayList<String> nombresBarras;
	
	/**
	 * Clave: nombre de la barra
	 * Valor: los coeficientes a, b de la recta que da la evolución de la demanda de energía anual en MWh:  a0 + a1 (año)  año por ejemplo 2025.
	 */
	private Hashtable<String, ParReales> coefCrecimiento;

	public Hashtable<String, Hashtable<Integer, double[]>> getPotenciasBarras() {
		return potenciasBarras;
	}

	public void setPotenciasBarras(Hashtable<String, Hashtable<Integer, double[]>> potenciasBarras) {
		this.potenciasBarras = potenciasBarras;
	}

	public Hashtable<Integer, double[]> getPotenciaTotal() {
		return potenciaTotal;
	}

	public void setPotenciaTotal(Hashtable<Integer, double[]> potenciaTotal) {
		this.potenciaTotal = potenciaTotal;
	}

	public ArrayList<String> getNombresBarras() {
		return nombresBarras;
	}

	public void setNombresBarras(ArrayList<String> nombresBarras) {
		this.nombresBarras = nombresBarras;
	}

	public Hashtable<String, ParReales> getCoefCrecimiento() {
		return coefCrecimiento;
	}

	public void setCoefCrecimiento(Hashtable<String, ParReales> coefCrecimiento) {
		this.coefCrecimiento = coefCrecimiento;
	}
	
	
	
	

}
