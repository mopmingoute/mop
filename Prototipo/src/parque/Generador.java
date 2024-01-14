/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Generador is part of MOP.
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

import cp_compdespProgEst.GeneradorCompDespPE;
import datatypesProblema.DatosSalidaProblemaLineal;
import tiempo.Evolucion;

/**
 * Clase que representa el generador
 * @author ut602614
 *
 */
public abstract class Generador extends Recurso {
	private Evolucion<Double> potenciaMaxima; 		/**Potencia móxima por cada módulo del generador*/
	private Evolucion<Double> minimoTecnico; 		/**Mónimo tócnico por cada módulo del generador*/
	private Barra barra;				/**Barra a la que el generador se encuentra conectado*/
	private Evolucion<Double> rendMin;
	private Evolucion<Double> rendMax;
	private Evolucion<Double> costoVariable;
	private String propietario;
	
	
	
	
	
	/**
	 * 
	 * @param potMin
	 *            Potencia en el mónimo tócnico por módulo en MW
	 * @param rendMin
	 *            Rendimiento en por uno en el mónimo tócnico
	 * @return Potencia tórmica en MW para el mónimo tócnico de un módulo
	 */
	public double calcPotTerMinTec(double potMin, double rendMin) {
		return potMin / rendMin;

	}

	/**
	 * 
	 * @param potMin
	 *            Potencia en el mónimo tócnico por módulo en MW
	 * @param potMax
	 *            Potencia móxima por módulo en MW
	 * @param rendMin
	 *            Rendimiento en por uno en el mónimo tócnico
	 * @param rendMax
	 *            Rendimiento móximo en por uno
	 * @return potencia tórmica en MW por cada MW elóctrico generado por encima
	 *         del mónimo tócnico
	 */
	public double calcPotEspTerProp(double potMin, double potMax, double rendMin, double rendMax) {
		// return (potMax / rendMax - potMin / rendMin) / (potMax - potMin);
		return (potMax * rendMin - potMin * rendMax) / (rendMax * rendMin * (potMax - potMin));
	}

	// TODO: REVISAR
	public double calcPotEspTerPropMax(double rendMax) {
		return 1 / rendMax;
	}

	
		
	public Evolucion<Double> getPotenciaMaxima() {
		return potenciaMaxima;
	}

	public void setPotenciaMaxima(Evolucion<Double> potenciaMaxima) {
		this.potenciaMaxima = potenciaMaxima;
	}

	public Evolucion<Double> getMinimoTecnico() {
		return minimoTecnico;
	}

	public void setMinimoTecnico(Evolucion<Double> minimoTecnico) {
		this.minimoTecnico = minimoTecnico;
	}

	public Barra getBarra() {
		return barra;
	}

	public void setBarra(Barra barra) {
		this.barra = barra;
	}

	public Evolucion<Double> getRendMin() {
		return rendMin;
	}

	public void setRendMin(Evolucion<Double> rendMin) {
		this.rendMin = rendMin;
	}

	public Evolucion<Double> getRendMax() {
		return rendMax;
	}

	public void setRendMax(Evolucion<Double> rendMax) {
		this.rendMax = rendMax;
	}

	public Evolucion<Double> getCostoVariable() {
		return costoVariable;
	}

	public void setCostoVariable(Evolucion<Double> costoVariable) {
		this.costoVariable = costoVariable;
	}
	
	
	
	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}

	/**
	 * Potencia instalada del conjunto de los módulos
	 * @param instante
	 * @return
	 */
	public double potInstaladaTotal(long instante) {
		return this.getCantModInst().getValor(instante)*this.getPotenciaMaxima().getValor(instante);
	}
	
	
	public abstract GeneradorCompDespPE devuelveCompDespPE();
	
	
		
}
