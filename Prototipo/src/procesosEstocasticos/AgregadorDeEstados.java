/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AgregadorDeEstados is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;

public abstract class AgregadorDeEstados {

	protected String nombrePESimul; // nombre del proceso de simulaci�n para el que sirve este agregador		
	protected String nombrePEOptim; // nombre del proceso de optimizaci�n para el que sirve este agregador	
	protected ArrayList<String> nombresVESimul; // Lista de nombres de variables de estado del proceso de simulaci�n
	protected ArrayList<String> nombresVAExo; // Lista de nombres de las variables aleatorias ex�genas del proceso de simulaci�n
	protected ArrayList<String> nombresVEOptim;  // Lista de nombres de variables de estado del proceso de optimizaci�n

	
	/**
	 * Devuelve los valores de las variables de estado de la optimización, 
	 * si los valores de las VE de simulación son valoresVESimul, 
	 * asociados uno a uno a los nombresVESimul
	 * 
	 * @param valoresVESimul
	 * @return
	 */
	public abstract double[] devuelveEstadoOptim(double[]  valoresVESimul, double[]  valoresVarExo);

	
	public String getNombrePESimul() {
		return nombrePESimul;
	}


	public void setNombrePESimul(String nombrePESimul) {
		this.nombrePESimul = nombrePESimul;
	}


	public String getNombrePEOptim() {
		return nombrePEOptim;
	}


	public void setNombrePEOptim(String nombrePEOptim) {
		this.nombrePEOptim = nombrePEOptim;
	}


	public ArrayList<String> getNombresVESimul() {
		return nombresVESimul;
	}


	public void setNombresVESimul(ArrayList<String> nombresVESimul) {
		this.nombresVESimul = nombresVESimul;
	}


	public ArrayList<String> getNombresVEOptim() {
		return nombresVEOptim;
	}


	public void setNombresVEOptim(ArrayList<String> nombresVEOptim) {
		this.nombresVEOptim = nombresVEOptim;
	}


	public ArrayList<String> getNombresVAExo() {
		return nombresVAExo;
	}


	public void setNombresVAExo(ArrayList<String> nombresVAExo) {
		this.nombresVAExo = nombresVAExo;
	}  
	
	
	
	
}
