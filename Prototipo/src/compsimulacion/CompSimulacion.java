/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CompSimulacion is part of MOP.
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

package compsimulacion;

import java.util.Hashtable;

import parque.Participante;
import compdespacho.CompDespacho;
import compgeneral.CompGeneral;
import datatypesProblema.DatosSalidaProblemaLineal;

public abstract class CompSimulacion {
	private Hashtable<String, String> valsCompGeneral;
	private CompGeneral compgeneral;
	private CompDespacho compdespacho;
	private Participante participante;

	public CompSimulacion() {
		valsCompGeneral = new Hashtable<String, String>();
	}

	public Hashtable<String, String> getValsCompGeneral() {
		return valsCompGeneral;
	}

	public void setValsCompGeneral(Hashtable<String, String> valsCompGeneral) {
		this.valsCompGeneral = valsCompGeneral;
	}

	/**
	 * Actualiza el comportamiento despacho asociado de acuerdo a la iteración
	 * correspondiente incluso para la primera iteración * @param iter
	 */
	public abstract void actualizarVariablesCompDespacho(int iter);

	public abstract void actualizarVariablesCompGlobal(Hashtable<String, String> compsGlobales);

	/**
	 * Carga en comportamiento despacho los datos que cambian a lo largo de las
	 * iteraciones correspondientes al participante, incluso para la primera
	 * iteración
	 * 
	 * @param instante
	 */
	public abstract void cargarDatosParaUnaIteracion(int iter, DatosSalidaProblemaLineal salidaIter);


	/**
	 * Este metodo solo se sobreescribe por los participantes que tienen inicializaciones
	 * que no se hacen al iniciar el escenario sino en otros pasos. Por ejemplo un ContratoInterrumpible
	 * cuya vida no se inicia en el primer paso de simulación sino después.
	 */
	public void inicializarParaPasoSimul() {};
	
	public abstract boolean aceptaDetenerIteracion(int iter, DatosSalidaProblemaLineal salidaIter);

	/**
	 * Carga en comportamiento despacho los datos que no cambian a lo largo de las
	 * iteraciones (dentro de un mismo paso) correspondientes al participante,
	 * incluso los que fueron actualizados en actualizarParaElProximo que pueden
	 * estar en diversos lugares
	 * 
	 * @param instante.
	 * 
	 *                  TODO: 24 ENERO 2017. Una parte de los datos procede de
	 *                  variables de estado. Esos datos procecen de las VE de la
	 *                  simulación o de la optimización, segón se estó en una u
	 *                  otra.
	 */
	public abstract void cargarDatosCompDespacho(long instante);

	/**
	 * Carga en distintos lugares la información del participante que resulta del
	 * despacho de este paso y se usa en pasos posteriores
	 * 
	 * @param instante
	 */
	public abstract void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter);

	public abstract void inicializarParaEscenario();

	public CompGeneral getCompgeneral() {
		return compgeneral;
	}

	public void setCompgeneral(CompGeneral compgeneral) {
		this.compgeneral = compgeneral;
	}

	public CompDespacho getCompdespacho() {
		return compdespacho;
	}

	public void setCompdespacho(CompDespacho compdespacho) {
		this.compdespacho = compdespacho;
	}

	public Participante getParticipante() {
		return participante;
	}

	public void setParticipante(Participante participante) {
		this.participante = participante;
	}

	//////////// METODOS USADOS SOLO EN LA OPTIMIZACIóN ///////////////

	public abstract void actualizarOtrosDatosIniciales();

	public abstract void actualizarVariablesCompDespachoOptim(int iter);

	public abstract void cargarDatosCompDespachoOptim(long instante);

	//////////////////////////////////////////////////////////////

	/**
	 * No incluye los costos de combustible de termicos
	 * 
	 * @param salidaUltimaIter
	 * @return
	 */
	public abstract double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter);

}
