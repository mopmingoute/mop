/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Estimacion is part of MOP.
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

package procesosEstocasticos;


/**
 * Contiene la informacion que identifica cuando fue estimado o generado el proceso,
 * como estan asociados los procesos de optimizacion y simulacion, y que agregador se
 * usa para los estados
 * @author ut469262
 *
 */
public class Estimacion {
	
	private String nombre;
	private ProcesoEstocastico procSim;
	private ProcesoEstocastico procOptim;
	private AgregadorDeEstados agregador;
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public ProcesoEstocastico getProcSim() {
		return procSim;
	}
	public void setProcSim(ProcesoEstocastico procSim) {
		this.procSim = procSim;
	}
	public ProcesoEstocastico getProcOptim() {
		return procOptim;
	}
	public void setProcOptim(ProcesoEstocastico procOptim) {
		this.procOptim = procOptim;
	}
	public AgregadorDeEstados getAgregador() {
		return agregador;
	}
	public void setAgregador(AgregadorDeEstados agregador) {
		this.agregador = agregador;
	}
	

}
