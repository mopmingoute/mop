/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PasoTiempo is part of MOP.
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

package tiempo;

import java.io.Serializable;

import logica.CorridaHandler;
import pizarron.PizarronRedis;

/**
 * Clase que representa un paso de tiempo en la línea de tiempo
 * @author ut602614
 *
 */
public class PasoTiempo implements Serializable{

	private long instanteInicial;
	private long instanteFinal;
	private int duracionPaso;
	//private int periodoPaso; /**Este peróodo puede ser SEGUNDO, DIA, HORA, SEMANA, toma valores de las constantes DE CALENDAR*/
	private LineaTiempo linea;
	private BloqueTiempo bloque;
	private int numpaso;
	
	public void setDuracionPaso(int duracionPaso) {
		this.duracionPaso = duracionPaso;
	}

	public BloqueTiempo getBloque() {
		return bloque;
	}

	public void setBloque(BloqueTiempo bloque) {
		this.bloque = bloque;
	}

	public PasoTiempo(long instanteInicial, int longitudPaso, int perPaso, LineaTiempo linea, BloqueTiempo bloque) {
		this.instanteInicial = instanteInicial;
		this.setInstanteFinal(instanteInicial + longitudPaso);
		if(instanteFinal<instanteInicial){
			System.out.println("Error en construcción de paso de tiempo: instante final menor que instante inicial");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		this.duracionPaso = longitudPaso;
	//	this.setPeriodoPaso(perPaso);
		this.linea = linea;
		this.bloque = bloque;
	}

	public long getInstanteInicial() {
		return instanteInicial;
	}

	public void setInstanteInicial(long instanteInicial) {
		this.instanteInicial = instanteInicial;
	}

	public int getDuracionPaso() {
		return duracionPaso;
	}

	public void setDuraciondPaso(int longitudPaso) {
		this.duracionPaso = longitudPaso;
	}

	public long getInstanteFinal() {
		return instanteFinal;
	}

	public void setInstanteFinal(long instanteFinal) {
		this.instanteFinal = instanteFinal;
	}

	public LineaTiempo getLinea() {
		return linea;
	}

	public void setLinea(LineaTiempo linea) {
		this.linea = linea;
	}
	
	public int getIntervaloMuestreo() {
		return this.bloque.getIntervaloMuestreo();
	}

	public int getNumpaso() {
		return numpaso;
	}

	public void setNumpaso(int numpaso) {
		this.numpaso = numpaso;
	}
	
	

//	public int getPeriodoPaso() {
//		return periodoPaso;
//	}
//
//	public void setPeriodoPaso(int periodoPaso) {
//		this.periodoPaso = periodoPaso;
//	}
}
