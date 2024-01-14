/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PostizacionPaso is part of MOP.
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

package simulacion;

import java.io.Serializable;
import java.util.ArrayList;
import tiempo.PasoTiempo;

public class PostizacionPaso implements Serializable{
	//PEDIR A LA POSTIZACION EXTERNA QUE CARGUE ESTAS COSAS (FECHAINICIAL, NUMPOS)
	private PasoTiempo paso;
	private int cantIntervalos;
	private ArrayList<Integer> numpos;  // ATENCIÓN: Los números de postes de numpos empiezan en 1 y no en cero.
	private int [] interPorPoste;
	private int [] durPos;
	private int durPaso;
	private int cantPos;

	
	public PostizacionPaso(PasoTiempo paso, int cantIntervalos, ArrayList<Integer> numpos,
			int[] interPorPoste, int cantPos, int[] durPos) {
		super();
		this.paso = paso;
		this.durPaso = 0;
		for (int i = 0; i < durPos.length; i++) {
			this.durPaso += durPos[i];
		}
		this.paso.setDuraciondPaso(durPaso);
		
		this.cantIntervalos = cantIntervalos;
		this.setNumpos(numpos);
		this.interPorPoste = interPorPoste;
		this.cantPos = cantPos;
		this.setDurPos(durPos);
		
	}

	public int getDurPaso() {
		return durPaso;
	}

	public void setDurPaso(int durPaso) {
		this.durPaso = durPaso;
	}

	public PasoTiempo getPaso() {
		return paso;
	}


	public void setPaso(PasoTiempo paso) {
		this.paso = paso;
	}

	public int getCantIntervalos() {
		return cantIntervalos;
	}

	public void setCantIntervalos(int cantIntervalos) {
		this.cantIntervalos = cantIntervalos;
	}

	public int getCantPos() {
		return cantPos;
	}

	public void setCantPos(int cantPos) {
		this.cantPos = cantPos;
	}

	public int [] getInterPorPoste() {
		return interPorPoste;
	}

	public void setInterPorPoste(int [] interPorPoste) {
		this.interPorPoste = interPorPoste;
	}

	public ArrayList<Integer> getNumpos() {
		return numpos;
	}

	public void setNumpos(ArrayList<Integer> numpos) {
		this.numpos = numpos;
	}

	public int [] getDurPos() {
		return durPos;
	}

	public void setDurPos(int [] durPos) {
		this.durPos = durPos;
	}
	
	
}
