/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosGeneralesPE is part of MOP.
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

import java.util.ArrayList;


public class DatosGeneralesPE {
	
	private String nombre;
	private String nombreEstimacion;
	private int cantVariables;
	private boolean usoSimulacion;
	private boolean usoOptimizacion;
	private String nombrePEAsociadoEnOptim;
	private boolean usaTransformaciones;  
	private ArrayList<String> nombresVariables;
	private ArrayList<String> nombresVarsEstado;
	private boolean usaVarsEstadoEnOptim;
	private boolean discretoExhaustivo;
	private int prioridadSorteos;
	private String nombrePaso;  // los de utilitarios.Constantes: PASOSEMANA, .....
	private boolean tieneVAExogenas; 
	private boolean tieneVEContinuas;
	private ArrayList<String> nombresVAExogenas;  // nombres de las VA ex�genas
	private ArrayList<String> nombresProcesosExogenas; // nombres respectivos de los procesos de las VA Ex�genas
	private DatosAgregadorLineal datAgregadorEstados;
	private DatosTransformaciones datTransformaciones;
	private DatosDiscretizacionesVEPE datDiscVEPE;
	

	private boolean muestreado;  // este dato se carga a partir del xml y no del cargador
	
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	
	public boolean isMuestreado() {
		return muestreado;
	}
	public void setMuestreado(boolean muestreado) {
		this.muestreado = muestreado;
	}
	public String getNombrePEAsociadoEnOptim() {
		return nombrePEAsociadoEnOptim;
	}
	public void setNombrePEAsociadoEnOptim(String nombrePEAsociadoEnOptim) {
		this.nombrePEAsociadoEnOptim = nombrePEAsociadoEnOptim;
	}
	public String getNombreEstimacion() {
		return nombreEstimacion;
	}
	public void setNombreEstimacion(String nombreEtimacion) {
		this.nombreEstimacion = nombreEtimacion;
	}
	public int getCantVariables() {
		return cantVariables;
	}
	public void setCantVariables(int cantVariables) {
		this.cantVariables = cantVariables;
	}
	public boolean isUsaTransformaciones() {
		return usaTransformaciones;
	}
	public void setUsaTransformaciones(boolean usaTransformaciones) {
		this.usaTransformaciones = usaTransformaciones;
	}
	public ArrayList<String> getNombresVariables() {
		return nombresVariables;
	}
	public void setNombresVariables(ArrayList<String> nombresVariables) {
		this.nombresVariables = nombresVariables;
	}
	public ArrayList<String> getNombresVarsEstado() {
		return nombresVarsEstado;
	}
	public void setNombresVarsEstado(ArrayList<String> nombresVarsEstado) {
		this.nombresVarsEstado = nombresVarsEstado;
	}
	public boolean isUsaVarsEstadoEnOptim() {
		return usaVarsEstadoEnOptim;
	}
	public void setUsaVarsEstadoEnOptim(boolean usaVarsEstadoEnOptim) {
		this.usaVarsEstadoEnOptim = usaVarsEstadoEnOptim;
	}
	public boolean isUsoSimulacion() {
		return usoSimulacion;
	}
	public void setUsoSimulacion(boolean usoSimulacion) {
		this.usoSimulacion = usoSimulacion;
	}
	public boolean isUsoOptimizacion() {
		return usoOptimizacion;
	}
	public void setUsoOptimizacion(boolean usoOptimizacion) {
		this.usoOptimizacion = usoOptimizacion;
	}
	public boolean isDiscretoExhaustivo() {
		return discretoExhaustivo;
	}
	public void setDiscretoExhaustivo(boolean discretoExhaustivo) {
		this.discretoExhaustivo = discretoExhaustivo;
	}
	public int getPrioridadSorteos() {
		return prioridadSorteos;
	}
	public void setPrioridadSorteos(int prioridadSorteos) {
		this.prioridadSorteos = prioridadSorteos;
	}
	public String getNombrePaso() {
		return nombrePaso;
	}
	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}
	public boolean isTieneVAExogenas() {
		return tieneVAExogenas;
	}
	public void setTieneVAExogenas(boolean tieneVAExogenas) {
		this.tieneVAExogenas = tieneVAExogenas;
	}
	public ArrayList<String> getNombresVAExogenas() {
		return nombresVAExogenas;
	}
	public void setNombresVAExogenas(ArrayList<String> nombresVAExogenas) {
		this.nombresVAExogenas = nombresVAExogenas;
	}
	public ArrayList<String> getNombresProcesosExogenas() {
		return nombresProcesosExogenas;
	}
	public void setNombresProcesosExogenas(ArrayList<String> nombresProcesosExogenas) {
		this.nombresProcesosExogenas = nombresProcesosExogenas;
	}
	public boolean isTieneVEContinuas() {
		return tieneVEContinuas;
	}
	public void setTieneVEContinuas(boolean tieneVEContinuas) {
		this.tieneVEContinuas = tieneVEContinuas;
	}
	public DatosAgregadorLineal getDatAgregadorEstados() {
		return datAgregadorEstados;
	}
	public void setDatAgregadorEstados(DatosAgregadorLineal datAgregadorEstados) {
		this.datAgregadorEstados = datAgregadorEstados;
	}
	public DatosTransformaciones getDatTransformaciones() {
		return datTransformaciones;
	}
	public void setDatTransformaciones(DatosTransformaciones datTransformaciones) {
		this.datTransformaciones = datTransformaciones;
	}
	public DatosDiscretizacionesVEPE getDatDiscVEPE() {
		return datDiscVEPE;
	}
	public void setDatDiscVEPE(DatosDiscretizacionesVEPE datDiscVEPE) {
		this.datDiscVEPE = datDiscVEPE;
	}
	
	
	
	
	
}
