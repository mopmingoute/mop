/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEPorPeriodos is part of MOP.
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

public class DatosPEPorPeriodos {
	/**
	 * 
	 * ATENCIóN: En esta clase se emplea la palabra hora como el intervalo de
	 * muestreo en un paso para el que hay un valor. Si se muestreasen dóas con 48
	 * medias horas, la "hora" seróa la media hora
	 */

	private String nombre;
	private boolean discretoExhaustivo;
	private boolean muestreado;
	private String tipo;

	private String nombrePasoPE; // es el nombre del paso en el lenguaje de la clase ProcesoEstocastico

	private String estimacionVE; // identificación de la estimación de las VE que se empleó
	private String nombreVA; // nombre de la VA del ProcesoPorPeriodos
	// Las VE del ProcesoPorPeriodos son las del proceso base

	private String nombreProcesoBase; // nombre del proceso estocóstico base asociado

	private int cantVE; // cantidad de variables de estado
	private int cantVEOptim; // cantidad de variables de estado de optimización

	private int cantHoras; // las "horas" se enumeran de 0 a cantHoras-1
	private int durHora; // duración de la "hora" en segundos
	private int cantPeriodos; // los peróodos dentro del paso se enumeran de 0 a cantPeriodos-1
	private int[] periodoDeHoras; // periodoDeHoras[i] indica a quó peróodo pertenece la hora i.
	private String ruta; // directorio donde se leen los datos del proceso this

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getNombrePasoPE() {
		return nombrePasoPE;
	}

	public void setNombrePasoPE(String nombrePasoPE) {
		this.nombrePasoPE = nombrePasoPE;
	}

	public String getEstimacionVE() {
		return estimacionVE;
	}

	public void setEstimacionVE(String estimacionVE) {
		this.estimacionVE = estimacionVE;
	}

	public String getNombreVA() {
		return nombreVA;
	}

	public void setNombreVA(String nombreVA) {
		this.nombreVA = nombreVA;
	}

	public String getNombreProcesoBase() {
		return nombreProcesoBase;
	}

	public void setNombreProcesoBase(String nombreProcesoBase) {
		this.nombreProcesoBase = nombreProcesoBase;
	}

	public int getCantVE() {
		return cantVE;
	}

	public void setCantVE(int cantVE) {
		this.cantVE = cantVE;
	}

	public int getCantVEOptim() {
		return cantVEOptim;
	}

	public void setCantVEOptim(int cantVEOptim) {
		this.cantVEOptim = cantVEOptim;
	}

	public int getCantHoras() {
		return cantHoras;
	}

	public void setCantHoras(int cantHoras) {
		this.cantHoras = cantHoras;
	}

	public int getDurHora() {
		return durHora;
	}

	public void setDurHora(int durHora) {
		this.durHora = durHora;
	}

	public int getCantPeriodos() {
		return cantPeriodos;
	}

	public void setCantPeriodos(int cantPeriodos) {
		this.cantPeriodos = cantPeriodos;
	}

	public int[] getPeriodoDeHoras() {
		return periodoDeHoras;
	}

	public void setPeriodoDeHoras(int[] periodoDeHoras) {
		this.periodoDeHoras = periodoDeHoras;
	}

}
