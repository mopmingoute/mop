/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * SentidoTiempo is part of MOP.
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

/**
 * Para las evoluciones indica si el tiempo va para adelante o para atrós
 * @author ut469262
 *
 */
public class SentidoTiempo implements Serializable{
	private int sentido; // 1 si avanza y -1 si retrocede

	
	
	
	public SentidoTiempo(int sentido) {
		this.sentido = sentido;
	}

	public int getSentido() {
		return sentido;
	}

	public void setSentido(int sentido) {
		this.sentido = sentido;
	}
		

}

