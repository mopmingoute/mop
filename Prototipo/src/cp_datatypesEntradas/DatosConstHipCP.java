/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosConstHipCP is part of MOP.
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

package cp_datatypesEntradas;

import java.util.ArrayList;
import java.util.Hashtable;

public class DatosConstHipCP extends DatosPartCP {
	
	private boolean usoHip;  // true si se usan hiperplanos
	
	private ArrayList<DatosHiperplanoCP> hiperplanos;
	
	private String[] nombresVE; // nombres de las variables de los hiperplanos
	
	/**
	 * Forma de obtener los hiperplanos si usoHip = true
	 * EXTERNO: lee los hiperplanos de un archivo hiperplanos.txt en el directorio Entradas
	 * CORRIDA: los toma de la corrida; esto requiere que se haya completado la optimización
	 */	
	private boolean origenCorrida;   // es true si el origen es CORRIDA y false en el otro caso
	
	private boolean vbnoneg; // si es true se impone valor de Bellman no negativo además de los hiperplanos

	public DatosConstHipCP(String nombrePart, String tipoPart, boolean usoHip, boolean origenCorrida, 
			ArrayList<DatosHiperplanoCP> hiperplanos, boolean vbnoneg) {
		super(nombrePart, tipoPart);
		this.usoHip = usoHip;
		this.origenCorrida = origenCorrida;
		this.hiperplanos = hiperplanos;
		this.vbnoneg = vbnoneg;
		nombresVE = hiperplanos.get(0).getNombresVE();
	}

	public boolean isUsoHip() {
		return usoHip;
	}

	public void setUsoHip(boolean usoHip) {
		this.usoHip = usoHip;
	}

	public ArrayList<DatosHiperplanoCP> getHiperplanos() {
		return hiperplanos;
	}

	public void setHiperplanos(ArrayList<DatosHiperplanoCP> hiperplanos) {
		this.hiperplanos = hiperplanos;
	}

	public boolean isOrigenCorrida() {
		return origenCorrida;
	}

	public void setOrigenCorrida(boolean origenCorrida) {
		this.origenCorrida = origenCorrida;
	}

	public boolean isVbnoneg() {
		return vbnoneg;
	}

	public void setVbnoneg(boolean vbnoneg) {
		this.vbnoneg = vbnoneg;
	}

	public String[] getNombresVE() {
		return nombresVE;
	}

	public void setNombresVE(String[] nombresVE) {
		this.nombresVE = nombresVE;
	}
	
	

}
