/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Paquete is part of MOP.
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

public class Paquete implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int paso;
	public int clave;
	public int estadoIni; //ordinal inicial en el enumerador de estados
	public int estadoFin; //ordinal final en el enumerador de estados
	public int estado; // el estado del paquete: ENESPERA, ENRESOLUCION,TERMINADO
	public long instanteTiempoEnvio;
	public long tiempoResolucionCliente;
	public long instanteTiempoRecibido;
	public long tiempoResolucion;
	public String nroMaquina;
	int penalizadorTiempo;


	public Paquete(int clave, int estadoIni, int estadoFin, int paso) {
		super();
		this.clave = clave;
		this.estadoIni = estadoIni;
		this.estadoFin = estadoFin;
		this.estado = Constantes.ENESPERA;
		this.paso = paso;
		penalizadorTiempo = 0;
	}

	public int getPenalizadorTiempo() {
		return penalizadorTiempo;
	}
	
	public void setPenalizadorTiempo(int pen) {
		this.penalizadorTiempo = pen;
	}
	
	public long getTiempoResolucionCliente() {
		return tiempoResolucionCliente;
	}

	public void setTiempoResolucionCliente(long tiempoResolucionCliente) {
		this.tiempoResolucionCliente = tiempoResolucionCliente;
	}

	
	public long getInstanteTiempoRecibido() {
		return instanteTiempoRecibido;
	}

	public void setInstanteTiempoRecibido(long instanteTiempoRecibido) {
		this.instanteTiempoRecibido = instanteTiempoRecibido;
	}
	
	public long getTiempoResolucionServidor() {
		return tiempoResolucion;
	}

	public void setTiempoResolusionServidor(long tiempoResolucion) {
		this.tiempoResolucion = tiempoResolucion;
	}


	public String getNroMaquina() {
		return nroMaquina;
	}

	public void setNroMaquina(String nroMaquina) {
		this.nroMaquina = nroMaquina;
	}
	
	public int getClave() {
		return clave;
	}
	public void setClave(int clave) {
		this.clave = clave;
	}
	public int getEstadoIni() {
		return estadoIni;
	}
	public void setEstadoIni(int estadoIni) {
		this.estadoIni = estadoIni;
	}
	public int getEstadoFin() {
		return estadoFin;
	}
	public void setEstadoFin(int estadoFin) {
		this.estadoFin = estadoFin;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
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
	public int getPaso() {
		return paso;
	}

	public void setPaso(int paso) {
		this.paso = paso;
	}

	public void imprimirPaquete() {
		System.out.println("Nro. Paquete: " + clave + " Paso: " +paso + " NroEstadoIni: " + estadoIni + " Estado: " + estado);
	}
	
}
