/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AFHiperplanos is part of MOP.
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

package futuro;

import java.util.ArrayList;

import parque.Participante;

/**
 * 
 * Conjunto de los hiperplanos de un paso de tiempo
 *
 */

public class AFHiperplanos extends AFutura {
	
	ArrayList<String>  nombresVECont;  // nombres de las variables de estado continuas de los hiperplanos
	ArrayList<Hiperplano> hiperplanos;


	public AFHiperplanos(ArrayList<Hiperplano> hiperplanos, ArrayList<String> nombresVECont) {
		super();
		this.hiperplanos = hiperplanos;
		this.nombresVECont = nombresVECont;
	}

	public ArrayList<String> getNombresVECont() {
		return nombresVECont;
	}

	public void setNombresVECont(ArrayList<String> nombresVECont) {
		this.nombresVECont = nombresVECont;
	}

	public ArrayList<Hiperplano> getHiperplanos() {
		return hiperplanos;
	}

	public void setHiperplanos(ArrayList<Hiperplano> hiperplanos) {
		this.hiperplanos = hiperplanos;
	}


	
	
	
	
	
	
}
