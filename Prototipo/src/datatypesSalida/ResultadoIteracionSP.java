/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ResultadoIteracionSP is part of MOP.
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

import java.util.Hashtable;

public class ResultadoIteracionSP {

	private int iteracion;	

	// Clave: nombre del participante que inyecta o extrae potencia de la red;
	// valor: potencia por poste
	private Hashtable<String, double[]> resultadoPotencias;

	// Clave: nombre de la barra; valor: costo marginal por poste
	private Hashtable<String, double[]> resultadoCMg;

	public ResultadoIteracionSP() {
		resultadoPotencias = new Hashtable<String, double[]>();
		resultadoCMg = new Hashtable<String, double[]>();
	}

	public int getIteracion() {
		return iteracion;
	}

	public void setIteracion(int iteracion) {
		this.iteracion = iteracion;
	}

	public Hashtable<String, double[]> getResultadoPotencias() {
		return resultadoPotencias;
	}

	public void setResultadoPotencias(Hashtable<String, double[]> resultadoPotencias) {
		this.resultadoPotencias = resultadoPotencias;
	}

	public Hashtable<String, double[]> getResultadoCMg() {
		return resultadoCMg;
	}

	public void setResultadoCMg(Hashtable<String, double[]> resultadoCMg) {
		this.resultadoCMg = resultadoCMg;
	}

}
