/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstudioHandler is part of MOP.
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

package logica;

import java.util.Hashtable;

import parque.Estudio;


/**
 * Clase encargada del manejo de la lógica del estudio
 * @author ut602614
 *
 */

public class EstudioHandler {
	private static EstudioHandler instance;
	/** Instancia estatica que implementa el patrón Singleton */

	private Hashtable<String, Estudio> estudios;
	/** Colección de estudios cargados en el sistema */
	
	private Estudio estudioActual;
	
	
	/** Función del singleton que devuelve siempre la misma instancia */
	public static EstudioHandler getInstance() {
		if (instance == null)
			instance = new EstudioHandler();
		return instance;
	}
	
	public static void deleteInstance() {
		instance = null;			
	}
	
	private CorridaHandler cHandler;
	/** Corrida Handler*/

	private EstudioHandler() {
		
		estudios = new Hashtable<String, Estudio>();
		setcHandler(CorridaHandler.getInstance());
		
		
	}



	public Hashtable<String, Estudio> getEstudios() {
		return estudios;
	}



	public void setEstudios(Hashtable<String, Estudio> estudios) {
		this.estudios = estudios;
	}



	public CorridaHandler getcHandler() {
		return cHandler;
	}



	public void setcHandler(CorridaHandler cHandler) {
		this.cHandler = cHandler;
	}



	public Estudio getEstudioActual() {
		return estudioActual;
	}



	public void setEstudioActual(Estudio estudioActual) {
		this.estudioActual = estudioActual;
	}

}
