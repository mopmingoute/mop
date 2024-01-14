/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEEscenarios is part of MOP.
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

public class DatosPEEscenarios {

	private DatosGeneralesPE datGen;
	private String nombre;
	private boolean discretoExhaustivo;
	private boolean muestreado;
	private String estimacionVEUsada; // identificación de la estimación de las VE que se empleo
	private String tipo;
	private String ruta;
	private String nombrePaso;

	private int cantVA; // cantidad de variables aleatorias
	private int cantVE; // cantidad de variables de estado
	private int cantEsc; // cantidad de escenarios de todas las VA y VE
	private int instInicEscenarios;

	private ArrayList<String> nombresVA; // nombres de las VA
	private ArrayList<String> nombresVE; // nombres de las VE

	/*
	 * 
	 */
	private int anioInicialPE; // a�o del primer paso del PE de los escenarios, ejemplo: 2016.
	private int pasoInicialPE; // paso del a�o en el que se inician los datos en el a�o inicial
	private int anioFinalPE; // a�o del �ltimo paso del PE de los escenarios, ejemplo: 2040.
	// paso del a�o del primer paso del PE, ejemplo paso 10, si arranca en la semana
	// 10 del 2016
	// los pasos son 1, 2, .....
	private int pasoFinalPE; // paso del �ltimo a�o

	private int cantMaxPasos;

	/**
	 * datos de las VA: primer �ndice "fila" es el escenario (ya no es una cr�nica
	 * hist�rica sino un �ndice de escenario generado segundo �ndice "columna"
	 * recorre el ordinal del paso en el a�o, por ejemplo las semanas tercer �ndice
	 * recorre las VA
	 */
	private double[][][][] datos;

	/**
	 * valores de las variables de estado, asociados a cada dato. tercer �ndice
	 * recorre las VE
	 */
	private double[][][][] valoresVE;

	private String estimacionVE; // identificaci�n de la estimaci�n de las VE que se emple�

	private int[][] etiquetaCron; // da una etiqueta a cada escenario en cada a�o; por ejemplo "1909", "1910",
									// etc.

	/**
	 * Para cada a�o en los que existen pasos del escenario, ordinal ("columna") de
	 * dicho primer paso en el escenario. La clave es el a�o y el valor es el
	 * ordinal. Ejemplo: un proceso semanal cuyos escenarios empiezan en la semana
	 * 26 del a�o 2016 tiene en ordinalPrimerPasoA�o las entradas: (2016, 1),
	 * (2017,27) , (2018, 27+52=79), .....
	 */
	private Hashtable<Integer, Integer> ordinalPrimerPasoAnio;

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

	public int getInstInicEscenarios() {
		return instInicEscenarios;
	}

	public void setInstInicEscenarios(int instInicEscenarios) {
		this.instInicEscenarios = instInicEscenarios;
	}

	public int getAnioInicialPE() {
		return anioInicialPE;
	}

	public void setAnioInicialPE(int anioInicialPE) {
		this.anioInicialPE = anioInicialPE;
	}

	public int getPasoInicialPE() {
		return pasoInicialPE;
	}

	public void setPasoInicialPE(int pasoInicialPE) {
		this.pasoInicialPE = pasoInicialPE;
	}

	public double[][][][] getDatos() {
		return datos;
	}

	public void setDatos(double[][][][] datos) {
		this.datos = datos;
	}

	public double[][][][] getValoresVE() {
		return valoresVE;
	}

	public void setValoresVE(double[][][][] valoresVE) {
		this.valoresVE = valoresVE;
	}

	public String getEstimacionVE() {
		return estimacionVE;
	}

	public void setEstimacionVE(String estimacionVE) {
		this.estimacionVE = estimacionVE;
	}

	public int[][] getEtiquetaCron() {
		return etiquetaCron;
	}

	public void setEtiquetaCron(int[][] etiquetaCron) {
		this.etiquetaCron = etiquetaCron;
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

	public int getCantEsc() {
		return cantEsc;
	}

	public void setCantEsc(int cantEsc) {
		this.cantEsc = cantEsc;
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

	public int getCantMaxPasos() {
		return cantMaxPasos;
	}

	public void setCantMaxPasos(int cantMaxPasos) {
		this.cantMaxPasos = cantMaxPasos;
	}

	public int getAnioFinalPE() {
		return anioFinalPE;
	}

	public void setAnioFinalPE(int anioFinalPE) {
		this.anioFinalPE = anioFinalPE;
	}

	public String getNombrePaso() {
		return nombrePaso;
	}

	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}

	public int getPasoFinalPE() {
		return pasoFinalPE;
	}

	public void setPasoFinalPE(int pasoFinalPE) {
		this.pasoFinalPE = pasoFinalPE;
	}

	public boolean isMuestreado() {
		return muestreado;
	}

	public void setMuestreado(boolean muestreado) {
		this.muestreado = muestreado;
	}

}
