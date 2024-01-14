/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EvolucionAleatoria is part of MOP.
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
import java.util.ArrayList;

import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;

/**
 * Clase que modela una evolución aleatoria
 * @author ut602614
 * @param
 *
 */
public class EvolucionAleatoria extends Evolucion<Double> implements Serializable{
	
	


	private String nombrePE; // nombre del proceso estocástico que contiene la variable aleatoria
	private String nombreVA; // nombre de la variable aleatoria dentro del proceso pe
	
	private ProcesoEstocastico pe; // proceso al que pertenece la variable aleatoria va
	private VariableAleatoria va; // variable aleatoria de pe que sigue la evolución
	
	public EvolucionAleatoria(SentidoTiempo sentido) {
		super(sentido);
		// TODO Auto-generated constructor stub
	}

	

	@Override
	public Double getValor(long instante) {
		pe.producirRealizacion(instante);
		return va.getValor();
	}

	@Override
	public void inicializarParaSimulacion() {

		// DELIBERADAMENTE VACIO
		
	}



	public String getNombrePE() {
		return nombrePE;
	}



	public void setNombrePE(String nombrePE) {
		this.nombrePE = nombrePE;
	}



	public String getNombreVA() {
		return nombreVA;
	}



	public void setNombreVA(String nombreVA) {
		this.nombreVA = nombreVA;
	}



	public ProcesoEstocastico getPe() {
		return pe;
	}



	public void setPe(ProcesoEstocastico pe) {
		this.pe = pe;
	}



	public VariableAleatoria getVa() {
		return va;
	}



	public void setVa(VariableAleatoria va) {
		this.va = va;
	}

	@Override
	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();
		if(nombrePE.trim().equals("")) { errores.add("Evolucion Aleatorioa: NombrePE vacío."); }
		if(nombreVA.trim().equals("")) { errores.add("Evolucion Aleatorioa: NombreVA vacío."); }
		if(pe == null )  { errores.add("Evolucion Aleatorioa: ProcesoEstocastico vacío."); }
		if(va == null )  { errores.add("Evolucion Aleatorioa: VariableAleatoria vacío."); }

		return errores;
	}

	@Override
	public ArrayList<String> controlRango(Double min, Double max, ArrayList<String> err) {
		//TODO control Rango en Evolucion Aleatoria
		return null;
	}

	@Override
	public ArrayList<String> controlRango(Integer min, Integer max, ArrayList<String> err) {
		//TODO control Rango en Evolucion Aleatoria
		return null;
	}

}
