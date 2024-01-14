/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosProcesoEstocastico is part of MOP.
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

package datatypesProcEstocasticos;

import persistencia.*;

import tiempo.Evolucion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import datatypes.DatosPronostico;
import utilitarios.Constantes;

public class DatosProcesoEstocastico implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nombre;
	private String tipo;
	private Boolean discretoExhaustivo;
	private String tipoSoporte;
	private String ruta;
	private Boolean muestreado;
	private ArrayList<String> nombresVa;
	private Evolucion<Double> va;
	
	/**
	 * Para cada VA su pronóstico
	 * clave nombre de la VA
	 * valor un Pronostico
	 */
	private Hashtable<String, DatosPronostico> pronosticos;

	/**
	 * Para cada nombre de VE del proceso , estado inicial al inicio de la
	 * corrida de simulación.
	 */
	private Hashtable<String, Double> estadosIniciales;

	public DatosProcesoEstocastico(String nombre,  String tipo, String tipoSoporte, String ruta,
								   Boolean discretoExhaustivo, Boolean muestreado, Hashtable<String, Double> estadosIniciales, Hashtable<String, DatosPronostico> pronosticos, String dummy) {
		
		super();
		this.nombre = nombre;
		this.tipo = tipo;
		this.ruta = ruta;
		this.tipoSoporte = tipoSoporte;
		this.setDiscretoExhaustivo(discretoExhaustivo);
		this.setMuestreado(muestreado);
		this.setEstadosIniciales(estadosIniciales);
		this.setPronosticos(pronosticos);
		this.nombresVa = new ArrayList<String>();
	}


	public DatosProcesoEstocastico(String nombre, String tipo, String tipoSoporte, String ruta,
			Boolean discretoExhaustivo, Boolean muestreado, Hashtable<String, Double> estadosIniciales,Hashtable<String, DatosPronostico> hpronosticos) {
		super();
		this.nombre = nombre;
		this.tipo = tipo;
		this.ruta = ruta;
		this.tipoSoporte = tipoSoporte;
		this.setDiscretoExhaustivo(discretoExhaustivo);
		this.setMuestreado(muestreado);
		this.setEstadosIniciales(estadosIniciales);
		this.nombresVa = new ArrayList<String>();
		this.pronosticos = hpronosticos;
		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico(nombre,tipo,tipoSoporte,ruta,discretoExhaustivo,muestreado,estadosIniciales,hpronosticos,"");

		if (this.tipo.equalsIgnoreCase(Constantes.HISTORICO)) {
			DatosPEHistorico dpeh = CargadorPEHistorico.devuelveDatosPEHistorico(dpe);
			this.nombresVa = dpeh.getNombresVA();
		}
		if (this.tipo.equalsIgnoreCase(Constantes.MARKOV)) {
			DatosPEMarkov dpeh = CargadorPEMarkov.devuelveDatosPEMarkov(dpe);
			this.nombresVa = dpeh.getNombresVA();
		}
		if (this.tipo.equalsIgnoreCase(Constantes.MARKOV_AMPLIADO)) {
			DatosPEMarkov dpeh = CargadorPEMarkovAmpliado.devuelveDatosPEMarkovAmpliado(dpe);
			this.nombresVa = dpeh.getNombresVA();
		}		
		if (this.tipo.equalsIgnoreCase(Constantes.POR_ESCENARIOS)) {
			DatosPEEscenarios dpeh = CargadorPEEscenarios.devuelveDatosPEEscenarios(dpe);
			this.nombresVa = dpeh.getNombresVA();
		}
		if (this.tipo.equalsIgnoreCase(Constantes.POR_CRONICAS)) {
			DatosPECronicas dpeh = CargadorPECronicas.devuelveDatosPECronicas(dpe);
			this.nombresVa = dpeh.getNombresVA();
		}
		if (this.tipo.equalsIgnoreCase(Constantes.BOOTSTRAP_DISCRETO)) {
			DatosPEBootstrapDiscreto dpeh = CargadorPEBootstrapDiscreto.devuelveDatosPEBootstrap(dpe);
			this.nombresVa = dpeh.getNombresVA();
		}
		if (this.tipo.equalsIgnoreCase(Constantes.DEMANDA_ANIO_BASE)) {
			DatosPEDemandaAnioBase dpeh = CargadorPEDemandaAnioBase.devuelveDatosPEDemandaBase(dpe);
			if(dpeh.isSumaVar()){
				this.nombresVa.add(dpeh.getNombre_var_suma());
			}else{
				this.nombresVa = dpeh.getNombresVA();
			}

		}
		if (this.tipo.equalsIgnoreCase(Constantes.DEMANDA_ESCENARIOS)) {
			DatosPEDemandaEscenarios dpeh = CargadorPEDemandaEscenarios.devuelveDatosPEDemandaEscenarios(dpe);
			this.nombresVa = dpeh.getNombresVA();
		}
		if (this.tipo.equalsIgnoreCase(Constantes.VAR)) {
			DatosPEVAR deph = CargadorPEVAR.devuelveDatosPEVar(dpe);
			this.nombresVa = deph.getDatGen().getNombresVariables();
		}


	}

	public ArrayList<String> getNombresVa() {
		return nombresVa;
	}

	public void setNombresVa(ArrayList<String> nombresVa) {
		this.nombresVa = nombresVa;
	}

	public String getRuta() {
		return ruta;
	}
	public void setRuta(String ruta) {
		this.ruta = ruta;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
		
	}
	public String getTipoSoporte() {
		return tipoSoporte;
	}

	public void setTipoSoporte(String tipoSoporte) {
		this.tipoSoporte = tipoSoporte;
	}

	public Boolean isDiscretoExhaustivo() {
		return discretoExhaustivo;
	}

	public void setDiscretoExhaustivo(Boolean discretoExhaustivo) {
		this.discretoExhaustivo = discretoExhaustivo;
	}

	public Boolean getMuestreado() {
		return muestreado;
	}

	public void setMuestreado(Boolean muestreado) {
		this.muestreado = muestreado;
	}

	public Hashtable<String, Double> getEstadosIniciales() {
		return estadosIniciales;
	}

	public void setEstadosIniciales(Hashtable<String, Double> estadosIniciales) {
		this.estadosIniciales = estadosIniciales;
	}

	public Boolean getDiscretoExhaustivo() {
		return discretoExhaustivo;
	}
	
	
	public Evolucion<Double> getVa() {
		return va;
	}


	public void setVa(Evolucion<Double> va) {
		this.va = va;
	}


	public Hashtable<String, DatosPronostico> getPronosticos() {
		return pronosticos;
	}


	public void setPronosticos(Hashtable<String, DatosPronostico> pronosticos) {
		this.pronosticos = pronosticos;
	}
	
	
}
