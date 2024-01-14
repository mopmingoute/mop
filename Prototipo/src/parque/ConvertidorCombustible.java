/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConvertidorCombustible is part of MOP.
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

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.ConvertCombSimpleCompDesp;
import cp_compdespProgEst.CicloCombCompDespPE;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import procesosEstocasticos.ProcesoEstocastico;

/**
 * Clase que representa el convertidor de combustible 
 * @author ut602614
 *
 */

public class ConvertidorCombustible extends Recurso{
	private Combustible combustibleOrigen;
	private Combustible combustibleTransformado;
	private BarraCombustible barraOrigen;
	private BarraCombustible barraDestino;
	private Double flujoMaxOrigen; 					//en unidades de combustible por unidades de tiempo
	private Double flujoMaxConvertido; 				//en unidades de combustible por unidades de tiempo
	private Double relacion; 						/**Cantidad de unidades de combustibleTransformado que se generan a partir de 	
													una unidad de combustible origen antes de las perdidas*/
	
	
	public Combustible getCombustibleOrigen() {
		return combustibleOrigen;
	}
	public void setCombustibleOrigen(Combustible combustibleOrigen) {
		this.combustibleOrigen = combustibleOrigen;
	}
	public Combustible getCombustibleTransformado() {
		return combustibleTransformado;
	}
	public void setCombustibleTransformado(Combustible combustibleTransformado) {
		this.combustibleTransformado = combustibleTransformado;
	}
	public BarraCombustible getBarraOrigen() {
		return barraOrigen;
	}
	public void setBarraOrigen(BarraCombustible barraOrigen) {
		this.barraOrigen = barraOrigen;
	}
	public BarraCombustible getBarraDestino() {
		return barraDestino;
	}
	public void setBarraDestino(BarraCombustible barraDestino) {
		this.barraDestino = barraDestino;
	}
	public Double getRelacion() {
		return relacion;
	}
	public void setRelacion(Double relacion) {
		this.relacion = relacion;
	}
	public Double getFlujoMaxOrigen() {
		return flujoMaxOrigen;
	}
	public void setFlujoMaxOrigen(Double flujoMaxOrigen) {
		this.flujoMaxOrigen = flujoMaxOrigen;
	}
	public Double getFlujoMaxConvertido() {
		return flujoMaxConvertido;
	}
	public void setFlujoMaxConvertido(Double flujoMaxConvertido) {
		this.flujoMaxConvertido = flujoMaxConvertido;
	}
	public String getNFlujoConv() {
		ConvertCombSimpleCompDesp ccsc = (ConvertCombSimpleCompDesp)this.getCompDesp(); 
		return ccsc.getNflujoConvertido();
	}
	public String getNFlujoOrigen() {
		ConvertCombSimpleCompDesp ccsc = (ConvertCombSimpleCompDesp)this.getCompDesp(); 
		return ccsc.getNflujoOrigen();
	}
	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,
			DatosSalidaProblemaLineal salidaUltimaIter, String proceso, long instante) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void asignaVAOptim() {
		// Deliberadamente en blanco
		
	}
	@Override
	public void asignaVASimul() {
		// Deliberadamente en blanco
		
	}
//	@Override
//	public void inicializarEvolucionesParticipante() {
//		// Deliberadamente en blanco
//		
//	}
	@Override
	public void aportarImpacto(Impacto i,DatosObjetivo costo ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void crearCompDespPE() {
		// Deliberadamente en blanco
	}
}
