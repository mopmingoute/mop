/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosGrafoEscCP is part of MOP.
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

package cp_datatypesEntradas;

import java.util.ArrayList;
import java.util.Hashtable;

import procesosEstocasticos.MatPTrans;

public class DatosGrafoEscCP {
	
	ArrayList<String>  nombresVA;
	int[] cantEscEtapa;
	
	/**
	 * Clave: nombreVA + "-" + etapa + "-" + escenario
	 *  etapa entero de 0 hasta cantEtapas-1
	 *  escenario entero de 0 hasta cantEscEtapa[etapa]-1
	 * Valor: double[] con los valores de la variable aleatoria nombreVA por poste en la etapa y el escenario
	 */
	private Hashtable<String, double[]> valoresVA;
	
	private ArrayList<MatPTrans> matTransicion;
	
	

	public ArrayList<String> getNombresVA() {
		return nombresVA;
	}

	public void setNombresVA(ArrayList<String> nombresVA) {
		this.nombresVA = nombresVA;
	}

	public int[] getCantEscEtapa() {
		return cantEscEtapa;
	}

	public void setCantEscEtapa(int[] cantEscEtapa) {
		this.cantEscEtapa = cantEscEtapa;
	}

	public Hashtable<String, double[]> getValoresVA() {
		return valoresVA;
	}

	public void setValoresVA(Hashtable<String, double[]> valoresVA) {
		this.valoresVA = valoresVA;
	}

	public ArrayList<MatPTrans> getMatTransicion() {
		return matTransicion;
	}

	public void setMatTransicion(ArrayList<MatPTrans> matTransicion) {
		this.matTransicion = matTransicion;
	}
	
	
	

}
