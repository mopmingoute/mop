
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPaso is part of MOP.
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

package datatypesSalida;

import java.util.ArrayList;

/**
 * Contiene los datos generales del paso
 * 
 * @author ut469262
 *
 */
public class DatosPaso {
	private int escenario;
	private int numPaso;
	private int durpaso; // duración en segundos
	private int cantPostes;
	private int cantIntervMuestreo; // cantidad de intervalos de muestreo
	private ArrayList<Integer> numpos; // poste al que pertenece cada intervalo de muestreo
	private int[] durPostes; // duración de los postes en horas
	private String fYHInicial; // fecha y hora instante inicial del paso
	private String fyHFinal;

	public DatosPaso(int escenario, int numPaso, int durpaso, int cantPostes, int cantIntervMuestreo,
			ArrayList<Integer> arrayList, int[] durPostes, String fYHInicial, String fyHFinal) {
		super();
		this.escenario = escenario;
		this.numPaso = numPaso;
		this.durpaso = durpaso;
		this.cantPostes = cantPostes;
		this.cantIntervMuestreo = cantIntervMuestreo;
		this.numpos = arrayList;
		this.durPostes = durPostes;
		this.fYHInicial = fYHInicial;
		this.fyHFinal = fyHFinal;
	}

	public DatosPaso() {
		// TODO Auto-generated constructor stub
	}

	public int getEscenario() {
		return escenario;
	}

	public void setEscenario(int escenario) {
		this.escenario = escenario;
	}

	public int getNumPaso() {
		return numPaso;
	}

	public void setNumPaso(int numPaso) {
		this.numPaso = numPaso;
	}

	public int getDurpaso() {
		return durpaso;
	}

	public void setDurpaso(int durpaso) {
		this.durpaso = durpaso;
	}

	public int getCantPostes() {
		return cantPostes;
	}

	public void setCantPostes(int cantPostes) {
		this.cantPostes = cantPostes;
	}

	public int getCantIntervMuestreo() {
		return cantIntervMuestreo;
	}

	public void setCantIntervMuestreo(int cantIntervMuestreo) {
		this.cantIntervMuestreo = cantIntervMuestreo;
	}

	public ArrayList<Integer> getNumpos() {
		return numpos;
	}

	public void setNumpos(ArrayList<Integer> numpos) {
		this.numpos = numpos;
	}

	public int[] getDurPostes() {
		return durPostes;
	}

	public void setDurPostes(int[] durPostes) {
		this.durPostes = durPostes;
	}

	public String getfYHInicial() {
		return fYHInicial;
	}

	public void setfYHInicial(String fYHInicial) {
		this.fYHInicial = fYHInicial;
	}

	public String getFyHFinal() {
		return fyHFinal;
	}

	public void setFyHFinal(String fyHFinal) {
		this.fyHFinal = fyHFinal;
	}

}
