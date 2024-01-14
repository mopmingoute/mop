/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosFallaCP is part of MOP.
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

public class DatosFallaCP extends DatosPartCP{
	
	
	private int dProg;   // período entre las decisiones de programación de escalones 
	private ArrayList<Integer> escProgramables; // escalones que no son de despapacho libre y se deciden cada fD días y hasta el fin del horizonte CP
	private boolean hayEscForzados;   // false si no hay escalones forzados al inicio del horizonte CP
	private boolean hayEscProgramables; 
	private ArrayList<Integer> escForzados;  // lista de los escalones forzados al inicio del período, numerados desde cero.
	
	public DatosFallaCP(String nombrePart, String tipoPart, boolean hayEscProgramables, 
			int dProg, ArrayList<Integer> escProgramables, boolean hayEscForzados, ArrayList<Integer> escForzados) {
		super(nombrePart, tipoPart);
		this.hayEscForzados = hayEscForzados;
		this.hayEscProgramables = hayEscProgramables;
		this.dProg = dProg;
		this.escProgramables = escProgramables;
		this.escForzados = escForzados;
	}

	public boolean isHayEscForzados() {
		return hayEscForzados;
	}

	public void setHayEscForzados(boolean hayEscForzados) {
		this.hayEscForzados = hayEscForzados;
	}

	public int getdProg() {
		return dProg;
	}

	public void setdProg(int dProg) {
		this.dProg = dProg;
	}

	public ArrayList<Integer> getEscProgramables() {
		return escProgramables;
	}

	public void setEscProgramables(ArrayList<Integer> escProgramables) {
		this.escProgramables = escProgramables;
	}

	public ArrayList<Integer> getEscForzados() {
		return escForzados;
	}

	public void setEscForzados(ArrayList<Integer> escForzados) {
		this.escForzados = escForzados;
	}

	public boolean isHayEscProgramables() {
		return hayEscProgramables;
	}

	public void setHayEscProgramables(boolean hayEscProgramables) {
		this.hayEscProgramables = hayEscProgramables;
	}

}
