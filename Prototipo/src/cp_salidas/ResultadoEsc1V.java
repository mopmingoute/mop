/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ResultadoEsc1V is part of MOP.
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

package cp_salidas;


import java.util.ArrayList;
import java.util.Hashtable;

import cp_despacho.GrafoEscenarios;

/**
 * Dado un GrafoEscenarios, contiene los resultados de operación 
 * de una variable en un escenario
 * @author ut469262
 *
 */
public class ResultadoEsc1V {
	
	private String nomVar;  // nombre de la BaseVar o la BaseRest (para las duales)
	
	private String nomPar;
	
	private int[] vecEsc;  // vector con el escenario desde el origen
	
	private boolean esPoste;  // true si la variable tiene un valor por poste y false si tiene un valor por día.
	
	private GrafoEscenarios ge;

	private Double[] valores;  // los valores por poste o por día según corresponda  

	public ResultadoEsc1V(String nomVar, String nomPar, int[] vecEsc, boolean esPoste, GrafoEscenarios ge,
			Double[] valores) {
		super();
		this.nomVar = nomVar;
		this.nomPar = nomPar;
		this.vecEsc = vecEsc;
		this.esPoste = esPoste;
		this.ge = ge;
		this.valores = valores;
	}

	public String getNomVar() {
		return nomVar;
	}

	public void setNomVar(String nomVar) {
		this.nomVar = nomVar;
	}

	public String getNomPar() {
		return nomPar;
	}

	public void setNomPar(String nomPar) {
		this.nomPar = nomPar;
	}

	public int[] getVecEsc() {
		return vecEsc;
	}

	public void setVecEsc(int[] vecEsc) {
		this.vecEsc = vecEsc;
	}

	public boolean isEsPoste() {
		return esPoste;
	}

	public void setEsPoste(boolean esPoste) {
		this.esPoste = esPoste;
	}

	public GrafoEscenarios getGe() {
		return ge;
	}

	public void setGe(GrafoEscenarios ge) {
		this.ge = ge;
	}

	public Double[] getValores() {
		return valores;
	}

	public void setValores(Double[] valores) {
		this.valores = valores;
	}





}
