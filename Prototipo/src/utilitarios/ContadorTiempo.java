/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContadorTiempo is part of MOP.
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

package utilitarios;

public class ContadorTiempo {
	private long milisegundosAcumulados;
	private long tiempoInicio;
	
	private boolean prendido;


	public ContadorTiempo() {
		super();
		milisegundosAcumulados = 0;
		tiempoInicio = 0;		
		prendido = false;
	}


//	public void iniciarContador() {
//		if (!prendido)	{
//			milisegundosAcumulados = 0;
//			tiempoInicio = 	System.currentTimeMillis();
//			prendido = true;
//		}
//	}

	public void pausarContador() {
		if (prendido) {
			milisegundosAcumulados += System.currentTimeMillis() - tiempoInicio;
			prendido = false;
			
		}
		
	}
	
	public void continuarContador() {
		if (!prendido) {
			tiempoInicio = 	System.currentTimeMillis();
			prendido = true;
		}
		
	}
	
	public void terminarContador(){
		pausarContador();
	}
	
	public long finalizarContador(){
		if (!prendido) {
			return milisegundosAcumulados;
		}
		return milisegundosAcumulados;
	}
	

	public long getMilisegundosAcumulados() {
		return milisegundosAcumulados;
	}


	public void setMilisegundosAcumulados(long milisegundosAcumulados) {
		this.milisegundosAcumulados = milisegundosAcumulados;
	}


	public long getTiempoInicio() {
		return tiempoInicio;
	}


	public void setTiempoInicio(long tiempoInicio) {
		this.tiempoInicio = tiempoInicio;
	}




	public boolean isPrendido() {
		return prendido;
	}


	public void setPrendido(boolean prendido) {
		this.prendido = prendido;
	}


}
