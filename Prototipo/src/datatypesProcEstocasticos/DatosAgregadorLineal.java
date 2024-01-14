/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosAgregadorLineal is part of MOP.
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

public class DatosAgregadorLineal {	
	/**
	 * Nombre del proceso de simulaci�n para el que sirve este agregador, 
	 * y en cuyo directorio de datos se leen los datos del agregador
	 */
	private String nombrePESimul; 
	private String nombrePEOptim; // nombre del proceso de optimizacion para el que sirve este agregador	
	private ArrayList<String> nombresVESimul; // Lista de nombres de variables de estado del proceso de simulación
	private ArrayList<String> nombresVExo;  // Lista de nombres de variables de estado del proceso de optimización
	private ArrayList<String> nombresVEOptim;  // Lista de nombres de variables de estado del proceso de optimización	
	private int cantVESimul;
	private int cantVExo;
	private int cantVEOptim;
	/**
	 * Tiene tantas filas como VE de optimizaci�n se quieran obtener y tantas columnas como VE de simulaci�n se tienen.	
	 */
	private double[][] matrizAgregacion;

	
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

	public double[][] getMatrizAgregacion() {
		return matrizAgregacion;
	}

	public void setMatrizAgregacion(double[][] matrizAgregacion) {
		this.matrizAgregacion = matrizAgregacion;
	}

	public int getCantVESimul() {
		return cantVESimul;
	}

	public void setCantVESimul(int cantVESimul) {
		this.cantVESimul = cantVESimul;
	}

	public int getCantVEOptim() {
		return cantVEOptim;
	}

	public void setCantVEOptim(int cantVEOptim) {
		this.cantVEOptim = cantVEOptim;
	}

	public ArrayList<String> getNombresVExo() {
		return nombresVExo;
	}

	public void setNombresVExo(ArrayList<String> nombresVExo) {
		this.nombresVExo = nombresVExo;
	}

	public int getCantVExo() {
		return cantVExo;
	}

	public void setCantVExo(int cantVExo) {
		this.cantVExo = cantVExo;
	}  
	
	
	

}
