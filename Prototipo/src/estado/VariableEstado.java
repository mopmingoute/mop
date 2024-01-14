/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableEstado is part of MOP.
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

import java.util.ArrayList;

import datatypes.DatosVariableEstado;
import tiempo.Evolucion;



/**
 * Clase abstracta que representa una variable de estado
 * @author ut602614
 *
 */ 
public class VariableEstado extends Variable{

	protected Double estadoAnterior;
	protected Double estado;
		
	/**
	 * Estado despuós que al atributo estado con que se entra en el paso de tiempo se le aplican los resultados
	 * de las variables de control discretas exhaustivas. 
	 */
	protected Double estadoDespuesDeCDE; 
	
	/*
	 * Estado que se presume para buscar valores de la variable de estado en el resoptim
	 */
	protected Double estadoS0fint;
	
	/*
	 * Estado final que se estima luego de la optimización de la operación
	 * En particular se usa en la optimización
	 */
	protected Double estadoFinalOptim; 

	
	public Double getEstadoDespuesDeCDE() {
		return estadoDespuesDeCDE;
	}

	public void setEstadoDespuesDeCDE(Double estadoDespuesDeCDE) {
		this.estadoDespuesDeCDE = estadoDespuesDeCDE;
	}
	
	
	
	public String imprimir() {
		return this.getNombre()+ " : " + "ES=" +this.estado +" ESs0FINT=" +this.estadoS0fint; 
	}
	/*
	 * Es la derivada parcial o lista de incrementos de valor.
	 * Normalmente se aplica a variables de estado de participantes.
	 */
	protected ArrayList<Double> valorRecurso; 
	
	
	/*
	 * En la simulación es el valor que toma la variable en el inicio del paso en que empieza a
	 * usarse la variable (puede no ocurrir al principio del escenario) 
	 */
	protected Double valorInicial; 	
	
	
	public VariableEstado(String nombre, boolean discreta, boolean discretaExhaustiva, boolean ordinal) {
		super(nombre, discreta, discretaExhaustiva, ordinal);
	}
	
	public VariableEstado(DatosVariableEstado datos) {
		super(datos.getNombre(),datos.isDiscreta(),datos.isDiscretaIncremental(), datos.isOrdinal());
	}
	
	
    public void actualizarEstado(double d) {
        estadoAnterior= estado;
        estado=d;
        
    }


	public Double getEstadoAnterior() {
		return estadoAnterior;
	}

	public void setEstadoAnterior(Double estadoAnterior) {
		this.estadoAnterior = estadoAnterior;
	}

	public Double getEstado() {
		return estado;
	}

	public void setEstado(Double estado) {
		this.estado = estado;
	}

	public boolean isAgregada() {
		return agregada;
	}

	public void setAgregada(boolean agregada) {
		this.agregada = agregada;
	}

	protected boolean agregada;
		
	public boolean isAgregado() {
		return agregada;
	}

	public void setAgregado(boolean agregado) {
		this.agregada = agregado;
	}

	public VariableEstado() {
		super();
	}
		
	public VariableEstado(String nombre) {
		super(nombre);			
		
	}

	public VariableEstado(String nombre, Evolucion<Discretizacion> evolDiscretizacion) {
		super(nombre);			
		this.evolDiscretizacion = evolDiscretizacion;
		
	}
	

	
	public ArrayList<Double> getValorRecurso() {
		return valorRecurso;
	}

	public void setValorRecurso(ArrayList<Double> valorRecurso) {
		this.valorRecurso = valorRecurso;
	}
	
	public void cargarValorInicial() {
		this.estado = this.valorInicial;
		
	}

	public Double getEstadoS0fint() {
		return estadoS0fint;
	}

	
	
	
	public void setEstadoS0fint(Double estadoS0fint) {
		this.estadoS0fint = estadoS0fint;
	}

	public Double getValorInicial() {
		return valorInicial;
	}

	public void setValorInicial(Double valorInicial) {
		this.valorInicial = valorInicial;
	}

	public Double getEstadoFinalOptim() {
		return estadoFinalOptim;
	}

	public void setEstadoFinalOptim(Double estadoFinalOptim) {
		this.estadoFinalOptim = estadoFinalOptim;
	}

	/**
	 * Se emplea cuando en el paso t-1 o existe una VE y só existe en el paso t
	 * Devuelve el estado que estó en el medio del rango en el instante  
	 * @return
	 */
	public int devuelveOrdinalEstadoAlAparecerVE(long instante) {
		int estado = this.getEvolDiscretizacion().getValor(instante).getCantValores()/2;
		return estado;
	}
	
	
	

}
