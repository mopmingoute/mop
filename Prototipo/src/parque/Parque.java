/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Parque is part of MOP.
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

package parque;

import java.util.Hashtable;

/**
 * Clase que representa el parque elóctrico
 * @author ut602614
 *
 */
public class Parque {
	/**TODO: LUEGO DEL PROTOTIPO*/
	String id;
	
	Hashtable<String,Participante> participantes;
	
	
	public Parque(String id) {
		this.id = id;
		participantes = new Hashtable<String, Participante>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	

	public Hashtable<String, Participante> getParticipantes() {
		return participantes;
	}

	public void setParticipantes(Hashtable<String, Participante> participantes) {
		this.participantes = participantes;
	}
	

}
