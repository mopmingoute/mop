/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProfilerBasicoTiempo is part of MOP.
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

package utilitarios;

import java.util.Hashtable;
import java.util.Set;

public class ProfilerBasicoTiempo {
	private static ProfilerBasicoTiempo instance;
	private boolean imprimir;
	private Hashtable<String, ContadorTiempo> contadores;

	private ProfilerBasicoTiempo() {
		setContadores(new Hashtable<String, ContadorTiempo>());
		imprimir = Constantes.IMPRIMIR_TIEMPOS;
	}

	/** Función del singleton que devuelve siempre la misma instancia */
	public static ProfilerBasicoTiempo getInstance() {
		if (instance == null)
			instance = new ProfilerBasicoTiempo();

		return instance;
	}
	public static void deleteInstance() {
		instance = null;			
	}
	public void reset() {
		setContadores(new Hashtable<String, ContadorTiempo>());
	}

	public void crearContador(String nombre) {
		ContadorTiempo nuevo = new ContadorTiempo();
		contadores.put(nombre, nuevo);
	}

	public void iniciarContador(String nombre) {
		if (contadores.get(nombre) == null) {
			ContadorTiempo nuevo = new ContadorTiempo();
			contadores.put(nombre, nuevo);
		}
		contadores.get(nombre).continuarContador();
	}

	public void pausarContador(String nombre) {
		contadores.get(nombre).pausarContador();
	}

	public long getMilisegundosAcumulados(String nombre) {
		return contadores.get(nombre).getMilisegundosAcumulados();
	}

	public void imprimirTiempo(String nombre, String ruta_tiempos) {
		DirectoriosYArchivos.agregaTexto(ruta_tiempos+"/tiempos_optimizacion.txt", "El tiempo acumulado por el contador " + nombre + " es de: "
				+ contadores.get(nombre).getMilisegundosAcumulados() / 1000 + " seg");
	}

	public Hashtable<String, ContadorTiempo> getContadores() {
		return contadores;
	}

	public void setContadores(Hashtable<String, ContadorTiempo> contadores) {
		this.contadores = contadores;
	}

	public void imprimirTiempos(String ruta_tiempos) {

		Set<String> claves = contadores.keySet();
		for (String c : claves) {
			if (imprimir) {
				System.out.println("El tiempo acumulado por el contador " + c + " es de: "
						+ contadores.get(c).getMilisegundosAcumulados() + " ms");
			}
			imprimirTiempo(c, ruta_tiempos);
		}
	}
}
