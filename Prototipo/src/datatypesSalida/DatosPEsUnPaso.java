/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEsUnPaso is part of MOP.
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
import java.util.Hashtable;

public class DatosPEsUnPaso {

	private int numPaso;

	/**
	 * Valores de las variables aleatorias Clave: nombre del proceso estocasticos +
	 * "-" + nombre de la variable aleatoria La clave se construye con el método
	 * ProcesadorSimulacionPEs.clavePEVA y contiene los nombres del PE y la VA.
	 * Array: primer índice sobre el ArrayList recorre los estados de la
	 * optimización en el orden en que los genera el OptimizadorPaso segundo índice
	 * sorteo de Montecarlo tercer índice intervalo de muestreo, si el PE no es
	 * muestreado hay un solo valor
	 */
	private Hashtable<String, ArrayList<double[][]>> valores;

	private ArrayList<int[]> estados; // lista de códigos de estado

	public DatosPEsUnPaso(int numPaso, ArrayList<String> nombresPEyVA) {
		valores = new Hashtable<String, ArrayList<double[][]>>();
		estados = new ArrayList<int[]>();
		for (String npv : nombresPEyVA) {
			ArrayList<double[][]> aux1 = new ArrayList<double[][]>();
			valores.put(npv, aux1);
		}
	}

	public int getNumPaso() {
		return numPaso;
	}

	public void setNumPaso(int numPaso) {
		this.numPaso = numPaso;
	}

	public Hashtable<String, ArrayList<double[][]>> getValores() {
		return valores;
	}

	public void setValores(Hashtable<String, ArrayList<double[][]>> valores) {
		this.valores = valores;
	}

	public ArrayList<int[]> getEstados() {
		return estados;
	}

	public void setEstados(ArrayList<int[]> estados) {
		this.estados = estados;
	}

}
