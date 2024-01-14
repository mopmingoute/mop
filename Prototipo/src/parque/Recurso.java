/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Recurso is part of MOP.
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

import procesosEstocasticos.VariableAleatoria;
import procesosEstocasticos.VariableAleatoriaEntera;
import tiempo.Evolucion;

/**
 * Clase que representa un recurso genórico (tiene varios módulos idónticos)
 * @author ut602614
 *
 */

public abstract class Recurso extends Participante {
	

	private Evolucion<Integer> cantModInst;				/**Cantidad de módulos del recurso*/
	private Evolucion<Double> dispMedia; 
	private Evolucion<Double> tMedioArreglo;
	private VariableAleatoria cantModDisp;
	private Evolucion<Integer> mantProgramado;
	private Evolucion<Double> costoFijo;    	//  TODO  ATENCIÓN QUE ESTO NO SE DICE SI ES POR MÓDULO
	private String propietario;      // NOMBRE DE LA EMPRESA PROPIETARIA

	public Recurso(){
	
	}
	
	public Evolucion<Double> getDispMedia() {
		return dispMedia;
	}

	public void setDispMedia(Evolucion<Double> dispMedia) {
		this.dispMedia = dispMedia;
	}

	public Evolucion<Double> gettMedioArreglo() {
		return tMedioArreglo;
	}

	public void settMedioArreglo(Evolucion<Double> tMedioArreglo) {
		this.tMedioArreglo = tMedioArreglo;
	}

	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}

	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
	}
	public VariableAleatoria getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(VariableAleatoria cantModDisp) {
		this.cantModDisp = cantModDisp;
	}


	@Override
	public abstract void inicializarParaEscenario();

	public Evolucion<Integer> getMantProgramado() {
		return mantProgramado;
	}

	public void setMantProgramado(Evolucion<Integer> mantProgramado) {
		this.mantProgramado = mantProgramado;
	}

	public Evolucion<Double> getCostoFijo() {
		return costoFijo;
	}

	public void setCostoFijo(Evolucion<Double> costoFijo) {
		this.costoFijo = costoFijo;
	}

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}


}
