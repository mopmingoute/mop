/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Servidor is part of MOP.
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

package presentacion;

import logica.CorridaHandler;
import optimizacion.ResOptim;
import pizarron.PizarronRedis;
import utilitarios.Constantes;

public class Servidor {
	
	public PresentacionHandler ph;

	public Servidor() {
		setPh(PresentacionHandler.getInstance());		

	}

	public static void main(String[] args) {		
		boolean nuevaAcargar = false;
		boolean cargada = false;
		boolean esperandoOperacion = false;
		Servidor serv = new Servidor();
		System.out.println("SERVIDOR EJECUTANDO, VERSIóN MOP PARELELO " + Constantes.VERSION_ET);
		ResOptim roptim;
		serv.ph.registrarMaquina();
		System.out.println(Integer.toString(serv.ph.damecantServidores()));
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
			nuevaAcargar = serv.ph.hayNuevaCorrida();
			if (nuevaAcargar && !cargada) {
				serv.ph.cargarCorridaServidor();
				cargada = true;
				esperandoOperacion = true;
				System.out.println("CARGUó CORRIDA SERVIDOR");
			} else if (cargada) {
				while (esperandoOperacion) {
					if (serv.ph.dameOperacion()==Constantes.OPTIMIZAR) {
						roptim = serv.ph.optimizarServidor();
					}
					if (serv.ph.dameOperacion()==Constantes.SIMULAR) {
						CorridaHandler ch = CorridaHandler.getInstance();
						ch.recargarSimulable();
						serv.ph.simularServidor();
					}
					if (serv.ph.dameOperacion()==Constantes.CERRARSERVIDOR) {	
						if (CorridaHandler.getInstance().isParalelo()){
							PizarronRedis pp = PizarronRedis.getInstance();
							pp.matarServidores();
						}
						System.exit(0);
					}
					
				}
				
			}
				
		}
	}

	public PresentacionHandler getPh() {
		return ph;
	}
	public void setPh(PresentacionHandler ph) {
		this.ph = ph;
	}
}
