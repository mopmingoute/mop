/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PaqueteEscenarios is part of MOP.
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

package pizarron;

import java.io.Serializable;

import utilitarios.Constantes;

public class PaqueteEscenarios implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int clave;
	public int escenarioIni; //ordinal inicial en el enumerador de estados
	public int escenarioFin; //ordinal final en el enumerador de estados
	public int estado; // el estado del paquete: ENESPERA, ENRESOLUCION,TERMINADO
	public long instanteTiempoEnvio;
	
	public PaqueteEscenarios(int clave, int escenarioIni, int escenarioFin) {
		super();
		this.clave = clave;
		this.escenarioIni = escenarioIni;
		this.escenarioFin = escenarioFin;
		this.estado = Constantes.ENESPERA;
		
	}
	
	public int getClave() {
		return clave;
	}
	public void setClave(int clave) {
		this.clave = clave;
	}
	public int getEscenarioIni() {
		return escenarioIni;
	}
	public void setEscenarioIni(int escenarioIni) {
		this.escenarioIni = escenarioIni;
	}
	public int getEscenarioFin() {
		return escenarioFin;
	}
	public void setEscenarioFin(int escenarioFin) {
		this.escenarioFin = escenarioFin;
	}
	public int getEstado() {
		return estado;
	}
	public void setEstado(int estado) {
		this.estado = estado;
	}
	public long getInstanteTiempoEnvio() {
		return instanteTiempoEnvio;
	}
	public void setInstanteTiempoEnvio(long instanteTiempoEnvio) {
		this.instanteTiempoEnvio = instanteTiempoEnvio;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void imprimirPaquete() {
		System.out.println("Nro. Paquete: " + clave +  " NroEscenarioIni: " + escenarioIni + " Estado: " + estado);
		
	}

	

}
