/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BloqueTiempo is part of MOP.
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
import java.util.ArrayList;

import utilitarios.Constantes;

/**
 * Clase que representa un bloque de tiempo, todo bloque tiene un conjunto de pasos de igual duración
 * @author ut602614
 *
 */
public class BloqueTiempo implements Serializable{
	private int primerPaso;
	private int cantidadPasos;
	private int duracionPaso;
	private int ordinalBloque; // ordinal del bloque empezando en 0 a partir del comienzo de la lónea de tiempo
	/**
	 * ATENCIóN: EL PASO DE TIEMPO DE TODOS LOS PROCESOS ESTOCóSTICOS MUESTREADOS DEBE
	 * COINCIDIR CON EL INTERVALO DE MUESTREO
	 */
	private int intervaloMuestreo; //en segundos, la duración del paso y de los postes debe ser móltiplo de este valor
	private int instantesDesplazados;
	private int[] duracionPostes; //en segundos, la suma de todas la duraciones de los postes debe ser la duracion del paso
	private int cantPostes;
	private boolean cronologico; 
	private int cantIm;  // cantidad de intervalos de muestreo en cada paso del bloque
	private int[] cantImPostes; // la cantidad de intervales de muestreo por poste

	public BloqueTiempo(){}

	public BloqueTiempo(int ordinalBloque, int primerPaso, int pasosPorBloque, int durpaso, int intervaloMuestreo, ArrayList<Integer> durPostes, Boolean cronologico) {
		this.ordinalBloque = ordinalBloque;
		this.primerPaso = primerPaso;
		this.cantidadPasos = pasosPorBloque;
		this.duracionPaso = durpaso;
		this.intervaloMuestreo = intervaloMuestreo; 
		this.cronologico = cronologico;
		this.cantPostes = durPostes.size();
		duracionPostes = new int[cantPostes];
		cantImPostes = new int[cantPostes];
		for (int i = 0; i < cantPostes; i++) {
			duracionPostes[i] = durPostes.get(i);
			cantImPostes[i] = durPostes.get(i)/intervaloMuestreo;
		}
		cantIm = duracionPaso/intervaloMuestreo;
		setInstantesDesplazados((int)(Constantes.DESLIZAMIENTOMUESTREO*intervaloMuestreo));
	}
	
	public BloqueTiempo(int ordinalBloque, int primerPaso, int pasosPorBloque, int durpaso, int intervaloMuestreo, int[] durPostes, boolean cronologico) {
		this.ordinalBloque = ordinalBloque;		
		this.primerPaso = primerPaso;
		this.cantidadPasos = pasosPorBloque;
		this.duracionPaso = durpaso;
		this.intervaloMuestreo = intervaloMuestreo; 
		this.cronologico = cronologico;
		this.cantPostes = durPostes.length;
		duracionPostes = durPostes;
		
		setInstantesDesplazados((int)(Constantes.DESLIZAMIENTOMUESTREO*intervaloMuestreo));
	}
	
	
	
	/**
	 * Chequea si la duración de los postes es múltiplo del intervalo de muestreo
	 * @return true si todas las duraciones de postes son múltimplos del intervalo de muestreo y false en otro caso
	 */
	public boolean chequeaDurPostes() {
		boolean mult = true;
		for(int ip=0; ip<cantPostes;ip++) {
			if(duracionPostes[ip]%intervaloMuestreo != 0) {
				System.out.println("En el bloque ordinal " + ordinalBloque + " no es múltiplo del int.muestreo la duración " + duracionPostes[ip]);
				return false;
			}
		}
		return true;
	}
	
	
	public int getPrimerPaso() {
		return primerPaso;
	}
	public void setPrimerPaso(int primerPaso) {
		this.primerPaso = primerPaso;
	}
	public int getCantidadPasos() {
		return cantidadPasos;
	}
	public void setCantidadPasos(int cantidadPasos) {
		this.cantidadPasos = cantidadPasos;
	}
	public int getDuracionPaso() {
		return duracionPaso;
	}
	public void setDuracionPaso(int duracionPaso) {
		this.duracionPaso = duracionPaso;
	}

	public int getOrdinalBloque() {
		return ordinalBloque;
	}

	public void setOrdinalBloque(int ordinalBloque) {
		this.ordinalBloque = ordinalBloque;
	}

	public int getIntervaloMuestreo() {
		return intervaloMuestreo;
	}
	public void setIntervaloMuestreo(int intervaloMuestreo) {
		this.intervaloMuestreo = intervaloMuestreo;
	}
	public int [] getDuracionPostes() {
		return duracionPostes;
	}
	public void setDuracionPostes(int [] duracionPostes) {
		this.duracionPostes = duracionPostes;
	}
	public int getCantPostes() {
		return cantPostes;
	}
	public void setCantPostes(int cantPostes) {
		this.cantPostes = cantPostes;
	}
	public boolean isCronologico() {
		return cronologico;
	}
	public void setCronologico(boolean cronologico) {
		this.cronologico = cronologico;
	}

	public int getInstantesDesplazados() {
		return instantesDesplazados;
	}

	public void setInstantesDesplazados(int instantesDesplazados) {
		this.instantesDesplazados = instantesDesplazados;
	}

	public int[] getCantImPostes() {
		return cantImPostes;
	}

	public void setCantImPostes(int[] cantImPostes) {
		this.cantImPostes = cantImPostes;
	}

	public int getCantIm() {
		return cantIm;
	}

	public void setCantIm(int cantIm) {
		this.cantIm = cantIm;
	}

	public ArrayList<String> controlDatosCompletos(){
		ArrayList<String> ret = new ArrayList<>();


		if(duracionPaso == 0){
			ret.add("Duración Paso erronea");
		}
		if(duracionPostes != null){
			boolean vacio = false;
			for(int i = 0; i < duracionPostes.length; i++){
				if(duracionPostes[i] == 0){ vacio = true; }
			}
			if(vacio) { ret.add("Duración Postes erronea");}
		}
		if(cantidadPasos == 0){
			ret.add("Cantidad Paso erronea");
		}


		return ret;
	}
	

}
