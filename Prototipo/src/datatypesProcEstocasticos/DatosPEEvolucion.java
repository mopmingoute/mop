/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEEvolucion is part of MOP.
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

public class DatosPEEvolucion {

	private DatosGeneralesPE datGen;
	private String nombre;

	private boolean discretoExhaustivo;
	private boolean muestreado;
	private String estimacionVEUsada; // identificaci�n de la estimaci�n de las VE que se emple�
	private String tipo;
	private String ruta;

	private ArrayList<String> nombresVA; // nombres de las VA
	private ArrayList<String> nombresVE; // nombres de las VE
	private int cantVA; // cantidad de variables aleatorias
	private int cantVE; // cantidad de variables de estado
	private int[] cantValoresVE; // cantidad de valores que puede tomar cada VE

	public DatosPEEvolucion() {
		super();

	}

	public DatosGeneralesPE getDatGen() {
		return datGen;
	}

	public void setDatGen(DatosGeneralesPE datGen) {
		this.datGen = datGen;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public boolean isDiscretoExhaustivo() {
		return discretoExhaustivo;
	}

	public void setDiscretoExhaustivo(boolean discretoExhaustivo) {
		this.discretoExhaustivo = discretoExhaustivo;
	}

	public boolean isMuestreado() {
		return muestreado;
	}

	public void setMuestreado(boolean muestreado) {
		this.muestreado = muestreado;
	}

	public String getEstimacionVEUsada() {
		return estimacionVEUsada;
	}

	public void setEstimacionVEUsada(String estimacionVEUsada) {
		this.estimacionVEUsada = estimacionVEUsada;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public ArrayList<String> getNombresVA() {
		return nombresVA;
	}

	public void setNombresVA(ArrayList<String> nombresVA) {
		this.nombresVA = nombresVA;
	}

	public ArrayList<String> getNombresVE() {
		return nombresVE;
	}

	public void setNombresVE(ArrayList<String> nombresVE) {
		this.nombresVE = nombresVE;
	}

	public int getCantVA() {
		return cantVA;
	}

	public void setCantVA(int cantVA) {
		this.cantVA = cantVA;
	}

	public int getCantVE() {
		return cantVE;
	}

	public void setCantVE(int cantVE) {
		this.cantVE = cantVE;
	}

	public int[] getCantValoresVE() {
		return cantValoresVE;
	}

	public void setCantValoresVE(int[] cantValoresVE) {
		this.cantValoresVE = cantValoresVE;
	}
}
