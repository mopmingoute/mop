/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * IniciadorSemilla is part of MOP.
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

package procesosEstocasticos;

import java.util.GregorianCalendar;

public class IniciadorSemilla {
	private Semilla general;
	private String nombre;
	private GregorianCalendar inicioSorteos;
	private int escenario;
	private int nroinnovacion;
	
	public IniciadorSemilla(Semilla general, String nombre,
			GregorianCalendar inicioSorteos, int escenario, int nroinnovacion) {
		super();
		
	//	System.out.println(general.valor+nombre+inicioSorteos.getTimeInMillis()+escenario);
		this.general = general;
		this.nombre = nombre;
		this.inicioSorteos = inicioSorteos;
		this.escenario = escenario;
		this.nroinnovacion = nroinnovacion;
	}

	public Semilla getGeneral() {
		return general;
	}

	public void setGeneral(Semilla general) {
		this.general = general;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public GregorianCalendar getInicioSorteos() {
		return inicioSorteos;
	}

	public void setInicioSorteos(GregorianCalendar inicioSorteos) {
		this.inicioSorteos = inicioSorteos;
	}

	public int getEscenario() {
		return escenario;
	}

	public void setEscenario(int escenario) {
		this.escenario = escenario;
	}

	public int getNroinnovacion() {
		return nroinnovacion; 
	}

	public void setNroinnovacion(int nroinnovacion) {
		this.nroinnovacion = nroinnovacion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + escenario;
		result = prime * result + ((general == null) ? 0 : general.hashCode());
		result = prime * result
				+ ((inicioSorteos == null) ? 0 : inicioSorteos.hashCode());
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
		result = prime * result + nroinnovacion;
		return result;
	}


	
	
}
