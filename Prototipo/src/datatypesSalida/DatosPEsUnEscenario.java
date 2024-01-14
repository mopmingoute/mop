/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEsUnEscenario is part of MOP.
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

/**
 * Almacena resultados de todas las VA de todos los PE en la simulación de
 * procesos estocásticos
 * 
 * @author ut469262
 *
 */
public class DatosPEsUnEscenario {

	private int numeroEsc;

	private int cantPasos;

	/**
	 * Valores de las variables aleatorias Clave: nombre del proceso estocasticos +
	 * "-" + nombre de la variable aleatoria La clave se construye con el método
	 * ProcesadorSimulacionPEs.clavePEVA y contiene los nombres del PE y la VA.
	 * Array: primer índice paso de tiempo de la simulacion segundo índice intervalo
	 * de muestreo, si el PE no es muestreado hay un solo valor
	 */
	private Hashtable<String, double[][]> valores;

	public DatosPEsUnEscenario(int numeroEsc, int cantPasos, ArrayList<String> nombresPEyVA) {
		valores = new Hashtable<String, double[][]>();
		this.numeroEsc = numeroEsc;
		for (String npv : nombresPEyVA) {
			double[][] aux = new double[cantPasos][];
			valores.put(npv, aux);
		}

	}

	public int getNumeroEsc() {
		return numeroEsc;
	}

	public void setNumeroEsc(int numeroEsc) {
		this.numeroEsc = numeroEsc;
	}

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

	public Hashtable<String, double[][]> getValores() {
		return valores;
	}

	public void setValores(Hashtable<String, double[][]> valores) {
		this.valores = valores;
	}

}
