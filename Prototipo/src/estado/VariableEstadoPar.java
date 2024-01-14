/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableEstadoPar is part of MOP.
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

package estado;

import datatypes.DatosDiscretizacion;
import datatypes.DatosVariableEstado;
import datatypesTiempo.DatosLineaTiempo;
import logica.CorridaHandler;
import parque.Participante;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;

public class VariableEstadoPar extends VariableEstado{

	private Participante participante;
	
	
	
	/*
	 * Los siguientes son los valores de los recursos cuando el valor de la variable de estado estó:
	 * - por debajo del primer punto de discretización (valorRecursoInferior)
	 * - por encima del primer punto de discretización (valorRecursoSuperior)
	 * Por ejemplo por encima de cota 82 metros el valor del agua de Bonete es nulo.
	 * Sólo se aplica a las variables continuas con derivada parcial
	 * 
	 * TODO: hay que cargar en el xml estas evoluciones.
	 */
	private Evolucion<Double> valorRecursoInferior;
	private Evolucion<Double> valorRecursoSuperior;
	
	/*
	 * Si hayValorInferior es true, cuando se calcula la derivada parcial respecto a esta variable,
	 * si la misma estó por debajo del mónimo valor de discretización, la derivada se toma igual a valorRecursoInferior,
	 * de lo contrario se extrapola.	
	 */
	private boolean hayValorInferior;  
	private boolean hayValorSuperior;  // si true se aplica valorRecursoSuperior en las , de lo contrario se extrapola
	
	public VariableEstadoPar() {
		super();
	}
	public VariableEstadoPar(String nombre) {
		super(nombre);
		// TODO Auto-generated constructor stub
	}
	
	public VariableEstadoPar(String nombre, Discretizacion discretizacion) {
		super(nombre);
		// TODO Auto-generated constructor stub
	}

	public VariableEstadoPar(DatosVariableEstado datosVariableEstado, DatosLineaTiempo lt, String nomParticipante) {
		this.setNombre(datosVariableEstado.getNombre()+"_"+ nomParticipante);
		Evolucion<Discretizacion> discretizacion = crearEvolucionDiscretizacion(datosVariableEstado.getDiscretizacion(),lt);
		this.setEvolDiscretizacion(discretizacion);
		this.setDiscreta(datosVariableEstado.isDiscreta());
		this.setDiscretaIncremental(datosVariableEstado.isDiscretaIncremental());
		this.valorInicial = datosVariableEstado.getEstadoInicial();
		this.setEstado(datosVariableEstado.getEstadoInicial());		
		this.valorRecursoInferior = datosVariableEstado.getValorRecursoInferior();
		this.valorRecursoSuperior = datosVariableEstado.getValorRecursoSuperior();
		this.hayValorInferior = datosVariableEstado.isHayValorInferior();
		this.hayValorSuperior = datosVariableEstado.isHayValorSuperior();
		
		
	}
	
	private Evolucion<Discretizacion> crearEvolucionDiscretizacion(
			EvolucionConstante<DatosDiscretizacion> discretizacion, DatosLineaTiempo lt) {
		EvolucionConstante<Discretizacion> retorno = null;
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		if (discretizacion!=null) {
			retorno = new EvolucionConstante<Discretizacion>(new Discretizacion(discretizacion.getValor(instanteActual)),lt.getSentido());
			retorno.getValor(instanteActual).setVarAsoc(this);
		}
		return retorno;		
	}
	
	public Participante getParticipante() {
		return participante;
	}

	public void setParticipante(Participante participante) {
		this.participante = participante;
	}
	public Double getEstadoS0fint() {
		return estadoS0fint;
	}

	public void setEstadoS0fint(Double estadoS0fint) {
		this.estadoS0fint = estadoS0fint;
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
	
	
	
	
}
