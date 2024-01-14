/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosVariableEstado is part of MOP.
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

import tiempo.Evolucion;
import tiempo.EvolucionConstante;

public class DatosVariableEstado implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;
	private Double estadoInicial;

	private String estadoInicialUnidad;
	private EvolucionConstante<DatosDiscretizacion> discretizacion;
	private Evolucion<Double> valorRecursoInferior;
	private Evolucion<Double> valorRecursoSuperior;
	private boolean hayValorInferior;  
	private boolean hayValorSuperior; 
	private boolean discreta;   
	private boolean ordinal; 
	private boolean discretaIncremental; 

	public DatosVariableEstado(String nombre, Double estadoInicial,  String estadoInicialUnidad,
			EvolucionConstante<DatosDiscretizacion> discretizacion) {
		super();
		this.nombre = nombre;
		this.estadoInicial = estadoInicial;
		this.estadoInicialUnidad = estadoInicialUnidad;
		this.setDiscretizacion(discretizacion);
	}
	
	public DatosVariableEstado(String nombre, Double estadoInicial, String estadoInicialUnidad,
			EvolucionConstante<DatosDiscretizacion> discretizacion,
			Evolucion<Double> valorRecursoInferior,
			Evolucion<Double> valorRecursoSuperior, boolean hayValorInferior,
			boolean hayValorSuperior, boolean discreta, boolean ordinal,
			boolean discretaIncremental) {
		super();
		this.nombre = nombre;
		this.estadoInicial = estadoInicial;
		this.estadoInicialUnidad = estadoInicialUnidad;
		this.discretizacion = discretizacion;
		this.valorRecursoInferior = valorRecursoInferior;
		this.valorRecursoSuperior = valorRecursoSuperior;
		this.hayValorInferior = hayValorInferior;
		this.hayValorSuperior = hayValorSuperior;
		this.discreta = discreta;
		this.ordinal = ordinal;
		this.discretaIncremental = discretaIncremental;
	}



	public DatosVariableEstado() {
		// TODO Auto-generated constructor stub
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Double getEstadoInicial() {
		return estadoInicial;
	}

	public void setEstadoInicial(Double estadoInicial) {
		this.estadoInicial = estadoInicial;
	}

	public String getEstadoInicialUnidad() {   return estadoInicialUnidad;   }

	public void setEstadoInicialUnidad(String estadoInicialUnidad) {		this.estadoInicialUnidad = estadoInicialUnidad;   }

	public EvolucionConstante<DatosDiscretizacion> getDiscretizacion() {
		return discretizacion;
	}

	public void setDiscretizacion(EvolucionConstante<DatosDiscretizacion> discretizacion) {
		this.discretizacion = discretizacion;
	}

	
	public Evolucion<Double> getValorRecursoInferior() {
		return valorRecursoInferior;
	}

	public void setValorRecursoInferior(Evolucion<Double> valorRecursoInferior) {
		this.valorRecursoInferior = valorRecursoInferior;
	}

	public Evolucion<Double> getValorRecursoSuperior() {
		return valorRecursoSuperior;
	}

	public void setValorRecursoSuperior(Evolucion<Double> valorRecursoSuperior) {
		this.valorRecursoSuperior = valorRecursoSuperior;
	}

	public boolean isHayValorInferior() {
		return hayValorInferior;
	}

	public void setHayValorInferior(boolean hayValorInferior) {
		this.hayValorInferior = hayValorInferior;
	}

	public boolean isHayValorSuperior() {
		return hayValorSuperior;
	}

	public void setHayValorSuperior(boolean hayValorSuperior) {
		this.hayValorSuperior = hayValorSuperior;
	}

	public boolean isDiscreta() {
		return discreta;
	}

	public void setDiscreta(boolean discreta) {
		this.discreta = discreta;
	}

	public boolean isOrdinal() {
		return ordinal;
	}

	public void setOrdinal(boolean ordinal) {
		this.ordinal = ordinal;
	}

	public boolean isDiscretaIncremental() {
		return discretaIncremental;
	}

	public void setDiscretaIncremental(boolean discretaIncremental) {
		this.discretaIncremental = discretaIncremental;
	}


	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();

		if(nombre.trim().equals("")) { errores.add("DatosVariableEstado: Nombre vacío."); }
		if(estadoInicial == null) { errores.add("DatosVariableEstado: estadoInicial vacío."); }
		if(!nombre.trim().equals("cantEscForzados")){
			if(discretizacion == null ) { errores.add("DatosVariableEstado: discretizacion vacío."); }
			else if(discretizacion.controlDatosCompletos().size() > 0 ) { errores.add("DatosVariableEstado" + nombre + ": discretizacion vacío."); }

			if(hayValorInferior && valorRecursoInferior == null ) { errores.add("DatosVariableEstado: valorRecursoInferior vacío."); }
			if(hayValorSuperior && valorRecursoSuperior == null ) { errores.add("DatosVariableEstado: valorRecursoSuperior vacío."); }
		}





			return errores;
		}


}
