/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEMarkov is part of MOP.
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

import procesosEstocasticos.MatPTrans;

public class DatosPEMarkov {

	private DatosGeneralesPE datGen;
	private String nombre;
	private boolean discretoExhaustivo;
	private boolean muestreado;
	private String tipo;
	private String ruta;
	private String nombrePaso;
	private int cantMaxPasos;
	private String estimacionVE; // identificaci�n de la estimaci�n de las VE que se emple�
	private ArrayList<String> nombresVA; // nombres de las VA
	private ArrayList<String> nombresVE; // nombres de las VE
	private ArrayList<String> nombresVEOptim; // nombres de las VE de optimizaci�n
	private int cantVA; // cantidad de variables aleatorias
	private int cantVE; // cantidad de variables de estado
	private int cantVEOptim; // cantidad de variables de estado de optimizaci�n

	private int[] cantCla; // cantidad de clases de cada VE
	private int cantEstac; // cantidad de estaciones en el a�o (estaci�n: conjunto de pasos con el mismo
							// modelo)
	private int[] estDelPaso; // es la estaci�n a la que pertenece cada paso
	private int cantCron; // cantidad de cr�nicas usadas en la estimaci�n

	/**
	 * Hash map de matrices de probabilidades de transici�n La clave es el par de
	 * estaciones: (estaci�n inicial, estaci�n final) El orden de filas y columnas
	 * en las matrices de transici�n es el de defEstadosComp.
	 */
	private Hashtable<Integer, MatPTrans> matrices;

	/**
	 * Estructura que almacena los valores de las series representativos para cada
	 * estaci�n (paso dentro del a�o) y estado compuesto.
	 * 
	 * Primer �ndice del array: estaci�n (paso dentro del a�o) Segundo �ndice del
	 * array: estado compuesto
	 * 
	 * El ArrayList tiene Double[] con los datos de cada serie.
	 */
	private Object[][] observaciones;

	/**
	 * Definici�n de estados compuestos Estado compuesto: resulta de las ordenaci�n
	 * lexicogr�fica de los estados de cada variable de estado. Ejemplo para dos
	 * variables de estado: (1,1),(1,2), ....;(2,1),(2,2),(2,3),...
	 */
	private ArrayList<int[]> defEstadosComp;

	private int cantEstadosComp; // cantidad de estados compuestos; es la dimensi�n de las matrices de transici�n

	public DatosGeneralesPE getDatGen() {
		return datGen;
	}

	public void setDatGen(DatosGeneralesPE datGen) {
		this.datGen = datGen;
	}

	public Hashtable<Integer, MatPTrans> getMatrices() {
		return matrices;
	}

	public void setMatrices(Hashtable<Integer, MatPTrans> matrices) {
		this.matrices = matrices;
	}

	public Object[][] getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(Object[][] observaciones) {
		this.observaciones = observaciones;
	}

	public ArrayList<int[]> getDefEstadosComp() {
		return defEstadosComp;
	}

	public void setDefEstadosComp(ArrayList<int[]> defEstadosComp) {
		this.defEstadosComp = defEstadosComp;
	}

	public int getCantCron() {
		return cantCron;
	}

	public void setCantCron(int cantCron) {
		this.cantCron = cantCron;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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

	public String getEstimacionVE() {
		return estimacionVE;
	}

	public void setEstimacionVE(String estimacionVE) {
		this.estimacionVE = estimacionVE;
	}

	public boolean isDiscretoExhaustivo() {
		return discretoExhaustivo;
	}

	public void setDiscretoExhaustivo(boolean discretoExhaustivo) {
		this.discretoExhaustivo = discretoExhaustivo;
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

	public String getNombrePaso() {
		return nombrePaso;
	}

	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}

	public int[] getCantCla() {
		return cantCla;
	}

	public void setCantCla(int[] cantCla) {
		this.cantCla = cantCla;
	}

	public int getCantEstadosComp() {
		return cantEstadosComp;
	}

	public void setCantEstadosComp(int cantEstadosComp) {
		this.cantEstadosComp = cantEstadosComp;
	}

	public int getCantEstac() {
		return cantEstac;
	}

	public void setCantEstac(int cantEstac) {
		this.cantEstac = cantEstac;
	}

	public int[] getEstDelPaso() {
		return estDelPaso;
	}

	public void setEstDelPaso(int[] estDelPaso) {
		this.estDelPaso = estDelPaso;
	}

	public int getCantMaxPasos() {
		return cantMaxPasos;
	}

	public void setCantMaxPasos(int cantMaxPasos) {
		this.cantMaxPasos = cantMaxPasos;
	}

	public boolean isMuestreado() {
		return muestreado;
	}

	public void setMuestreado(boolean muestreado) {
		this.muestreado = muestreado;
	}

	public void setNombresVEOptim(ArrayList<String> nombresVEOptim) {
		this.nombresVEOptim = nombresVEOptim;
	}

	public ArrayList<String> getNombresVEOptim() {
		return nombresVEOptim;
	}

	public void setCantVEOptim(int cantVEOptim) {
		this.cantVEOptim = cantVEOptim;

	}

	public int getCantVEOptim() {
		return cantVEOptim;
	}

}
