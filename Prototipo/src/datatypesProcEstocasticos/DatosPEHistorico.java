/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEHistorico is part of MOP.
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

public class DatosPEHistorico {

	private DatosGeneralesPE datGen;
	private String nombre;

	private boolean discretoExhaustivo;
	private boolean muestreado;
	private String estimacionVEUsada; // identificaci�n de la estimaci�n de las VE que se emple�
	private String tipo;
	private String ruta;

	private double[][][] datos; // datos de las VA
	private double[][][] valoresVE;
	private ArrayList<String> nombresVA; // nombres de las VA
	private ArrayList<String> nombresVE; // nombres de las VE
	private int cantVA; // cantidad de variables aleatorias
	private int cantVE; // cantidad de variables de estado
	private int[] cantValoresVE; // cantidad de valores que puede tomar cada VE
	private int cantCron; // es la cantidad de cr�nicas anuales disponibles
	private int cronIni;
	private int cronFin;
	private int[] cronicas; // da una etiqueta a cada cr�nica; por ejemplo "1909"
	private int[] cantPasosCron; // cantidad de pasos que entran en cada cr�nica de los datos hist�ricos
	private int cantPasosMax; // m�xima cantidad de pasos que puede tener una cr�nica; ejemplo 366 si el paso
								// es diario
	private String nombrePaso;
	private int saltoAtrasAlRepetir;
	private int cantDatos;

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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public double[][][] getDatos() {
		return datos;
	}

	public void setDatos(double[][][] datos) {
		this.datos = datos;
	}

	public double[][][] getValoresVE() {
		return valoresVE;
	}

	public void setValoresVE(double[][][] valoresVE) {
		this.valoresVE = valoresVE;
	}

	public int[] getCantValoresVE() {
		return cantValoresVE;
	}

	public void setCantValoresVE(int[] cantValoresVE) {
		this.cantValoresVE = cantValoresVE;
	}

	public int getCantVA() {
		return cantVA;
	}

	public void setCantVA(int cantVA) {
		this.cantVA = cantVA;
	}

	public int getCantCron() {
		return cantCron;
	}

	public void setCantCron(int cantCron) {
		this.cantCron = cantCron;
	}

	public int getCronIni() {
		return cronIni;
	}

	public void setCronIni(int cronIni) {
		this.cronIni = cronIni;
	}

	public int getCronFin() {
		return cronFin;
	}

	public void setCronFin(int cronFin) {
		this.cronFin = cronFin;
	}

	public int[] getCronicas() {
		return cronicas;
	}

	public void setCronicas(int[] cronicas) {
		this.cronicas = cronicas;
	}

	public int[] getCantPasosCron() {
		return cantPasosCron;
	}

	public void setCantPasosCron(int[] cantPasosCron) {
		this.cantPasosCron = cantPasosCron;
	}

	public String getEstimacionVEUsada() {
		return estimacionVEUsada;
	}

	public void setEstimacionVEUsada(String estimacionVEUsada) {
		this.estimacionVEUsada = estimacionVEUsada;
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

	public int getCantPasosMax() {
		return cantPasosMax;
	}

	public String getNombrePaso() {
		return nombrePaso;
	}

	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}

	public int getSaltoAtrasAlRepetir() {
		return saltoAtrasAlRepetir;
	}

	public void setSaltoAtrasAlRepetir(int saltoAtrasAlRepetir) {
		this.saltoAtrasAlRepetir = saltoAtrasAlRepetir;
	}

	public void setCantPasosMax(int cantPasosMax) {
		this.cantPasosMax = cantPasosMax;
	}

	public int getCantVE() {
		return cantVE;
	}

	public void setCantVE(int cantVE) {
		this.cantVE = cantVE;
	}

	public void setRuta(String ruta) {

		this.ruta = ruta;
	}

	public String getRuta() {
		return ruta;
	}

	public boolean isDiscretoExhaustivo() {
		return discretoExhaustivo;
	}

	public void setDiscretoExhaustivo(boolean discretoExhaustivo) {
		this.discretoExhaustivo = discretoExhaustivo;
	}

	public int getCantDatos() {
		return cantDatos;
	}

	public void setCantDatos(int cantDatos) {
		this.cantDatos = cantDatos;
	}

	public boolean isMuestreado() {
		return muestreado;
	}

	public void setMuestreado(boolean muestreado) {
		this.muestreado = muestreado;
	}

}
