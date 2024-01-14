/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosRedCombustibleCorrida is part of MOP.
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

package datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import tiempo.Evolucion;

/**
 * Datatype que representa los datos de una red de combustible 
 * @author ut602614
 *
 */

public class DatosRedCombustibleCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;
	private Hashtable<String,Evolucion<String>> valoresComportamiento;
	private ArrayList<String> barrasUtilizadas;
	private ArrayList<String> ramasUtilizadas;
	private ArrayList<String> tanquesUtilizados;
	private ArrayList<String> contratosUtilizados;
	private ArrayList<DatosBarraCombCorrida> barras;
	private ArrayList<DatosDuctoCombCorrida> ductos;
	private ArrayList<DatosTanqueCombustibleCorrida> tanques;
	private ArrayList<DatosContratoCombustibleCorrida> contratos;	
	
	
	public DatosRedCombustibleCorrida() {
		super();
		this.barrasUtilizadas = new ArrayList<String>();
		this.ramasUtilizadas = new ArrayList<String>();
		this.tanquesUtilizados = new ArrayList<String>();
		this.contratosUtilizados = new ArrayList<String>();
		this.barras = new ArrayList<DatosBarraCombCorrida>();
		this.ductos = new ArrayList<DatosDuctoCombCorrida>();
		this.tanques = new ArrayList<DatosTanqueCombustibleCorrida>();
		this.contratos = new ArrayList<DatosContratoCombustibleCorrida>();
		this.valoresComportamiento = new Hashtable<String, Evolucion<String>>();
	}

	public ArrayList<DatosBarraCombCorrida> getBarras() {
		return barras;
	}
	public void setBarras(ArrayList<DatosBarraCombCorrida> barras) {
		this.barras = barras;
	}
	public ArrayList<DatosDuctoCombCorrida> getRamas() {
		return ductos;
	}
	public void setRamas(ArrayList<DatosDuctoCombCorrida> ramas) {
		this.ductos = ramas;
	}
	public ArrayList<DatosTanqueCombustibleCorrida> getTanques() {
		return tanques;
	}
	public void setTanques(ArrayList<DatosTanqueCombustibleCorrida> tanques) {
		this.tanques = tanques;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Hashtable<String, Evolucion<String>> getValoresComportamiento() {
		return valoresComportamiento;
	}

	public void setValoresComportamiento(
			Hashtable<String, Evolucion<String>> valoresComportamiento) {
		this.valoresComportamiento = valoresComportamiento;
	}

	public ArrayList<DatosContratoCombustibleCorrida> getContratos() {
		return contratos;
	}
	public void setContratos(ArrayList<DatosContratoCombustibleCorrida> contratos) {
		this.contratos = contratos;
	}
	

	public ArrayList<String> getBarrasUtilizadas() {
		return barrasUtilizadas;
	}

	public void setBarrasUtilizadas(ArrayList<String> barrasUtilizadas) {
		this.barrasUtilizadas = barrasUtilizadas;
	}

	public ArrayList<DatosDuctoCombCorrida> getDuctos() {
		return ductos;
	}

	public void setDuctos(ArrayList<DatosDuctoCombCorrida> ductos) {
		this.ductos = ductos;
	}

	public ArrayList<String> getRamasUtilizadas() {
		return ramasUtilizadas;
	}
	public void setRamasUtilizadas(ArrayList<String> ramasUtilizadas) {
		this.ramasUtilizadas = ramasUtilizadas;
	}
	public ArrayList<String> getTanquesUtilizados() {
		return tanquesUtilizados;
	}
	public void setTanquesUtilizados(ArrayList<String> tanquesUtilizados) {
		this.tanquesUtilizados = tanquesUtilizados;
	}
	public ArrayList<String> getContratosUtilizados() {
		return contratosUtilizados;
	}
	public void setContratosUtilizados(ArrayList<String> contratosUtilizados) {
		this.contratosUtilizados = contratosUtilizados;
	}


	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(nombre.trim().equals("")) { errores.add("Red Combustible: Nombre vacío."); }

		if(valoresComportamiento == null) { errores.add("Red Combustible " + nombre + ": valoresComportamiento vacío."); }
		else {
			// if(valoresComportamiento.get(Constantes.COMPRED).equals(Constantes.UNINODAL));
		}

		if(barras == null) { errores.add("Red Combustible " + nombre + ": barras vacío."); }
		else{
			barras.forEach((b)-> { if(b.getNombre().trim().equals("")) errores.add("Red Combustible " + nombre + ": barras vacío."); } );
		}

		if(ductos == null) { errores.add("Red Combustible " + nombre + ": ductos vacío."); }
		else{
			//ductos.forEach((d)-> { if(d.controlDatosCompletos().size() > 0 ) errores.add("Red Combustible " + nombre + ": ductos vacío."); } );
			ductos.forEach((d)->  errores.addAll(d.controlDatosCompletos() ) );
		}

		if(contratos == null) { errores.add("Red Combustible " + nombre + ": contratos vacío."); }
		else{
			//contratos.forEach((d)-> { if(d.controlDatosCompletos().size() > 0 ) errores.add("Red Combustible " + nombre + ": contratos vacío."); } );
			contratos.forEach((d)->  errores.addAll(d.controlDatosCompletos() ) );
		}

		return errores;


	}
	
}
