/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCurvaOferta is part of MOP.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class DatosCurvaOferta implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer paso;
	private Hashtable<String, ArrayList<Double>> variables;
	private Hashtable<String, ArrayList<Double>> potenciasDisp;

	public DatosCurvaOferta() {
		variables = new Hashtable<String, ArrayList<Double>>();
		potenciasDisp = new Hashtable<String, ArrayList<Double>>();
	}

	public Integer getPaso() {
		return paso;
	}

	public void setPaso(Integer paso) {
		this.paso = paso;
	}

	public Hashtable<String, ArrayList<Double>> getVariables() {
		return variables;
	}

	public void setVariables(Hashtable<String, ArrayList<Double>> variables) {
		this.variables = variables;
	}

	public void agregarVariablesMaquinaPaso(String maquina, ArrayList<Double> variables) {
		this.variables.put(maquina, variables);
	}

	public void agregarPotsDispMaquinaPaso(String maquina, ArrayList<Double> pots) {
		this.potenciasDisp.put(maquina, pots);
	}

	public Hashtable<String, ArrayList<Double>> getPotenciasDisp() {
		return potenciasDisp;
	}

	public void setPotenciasDisp(Hashtable<String, ArrayList<Double>> potenciasDisp) {
		this.potenciasDisp = potenciasDisp;
	}

}
