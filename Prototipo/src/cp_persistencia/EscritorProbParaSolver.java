/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorProbParaSolver is part of MOP.
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

package cp_persistencia;

import datatypesProblema.DatosEntradaProblemaLineal;
import datatypesProblema.DatosObjetivo;

public abstract class EscritorProbParaSolver {
	
	protected String dirSalida;  // nombre del directorio de salida
	protected String nomArchSinExt;  // nombre del archivo sin extenxión
	protected DatosEntradaProblemaLineal prob;
	
	


	public EscritorProbParaSolver(String dirSalida, String nomArchSinExt, DatosEntradaProblemaLineal prob) {
		super();
		this.dirSalida = dirSalida;
		this.nomArchSinExt = nomArchSinExt;
		this.prob = prob;
	}


	public abstract void escribeProb();


	public String getDirSalida() {
		return dirSalida;
	}


	public void setDirSalida(String dirSalida) {
		this.dirSalida = dirSalida;
	}


	public String getNomArchSinExt() {
		return nomArchSinExt;
	}


	public void setNomArchSinExt(String nomArchSinExt) {
		this.nomArchSinExt = nomArchSinExt;
	}


	public DatosEntradaProblemaLineal getProb() {
		return prob;
	}




	public void setProb(DatosEntradaProblemaLineal prob) {
		this.prob = prob;
	}
	
	
	

}
