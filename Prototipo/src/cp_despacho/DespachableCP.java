/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DespachableCP is part of MOP.
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

package cp_despacho;

import datatypesProblema.DatosSalidaProblemaLineal;
import optimizacion.ResOptim;
import parque.Corrida;

public interface DespachableCP {
	
	
	public abstract void setDirEntrada(String dirEntrada);
	
	public abstract void setDirSalida(String dirEntrada);
	
	public abstract void setCorrida(Corrida corrida);
	
	public abstract void leerdatosGenerales();

	public abstract void leerDatosParticipantesCP();

	public abstract void leerProcEstocasticos();
	
	
	/**
	 * Carga en el despachable el DespachadorCortoPlazo
	 * @param dcp
	 */
	public void cargarDespachoCortoPlazo(DespachadorCortoPlazo dcp);
	
	/**
	 * Se recorren los participantes de la Corrida y se crea un CompDespCP
	 * en el caso del DespachadorPE se contruyen CompDespPE. Al CompDespCP se le carga como atributo
	 * el ComportamientoDespacho de la corrida y el participante.
	 */
	public abstract void construirComportamientosCP();
	
	public void despacharCP();

	public void producirSalidasCP();

}
