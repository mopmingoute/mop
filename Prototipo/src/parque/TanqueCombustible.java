/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TanqueCombustible is part of MOP.
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

import compdespacho.TanqueCombCompDesp;
import cp_compdespProgEst.CicloCombCompDespPE;
import datatypes.DatosTanqueCombustibleCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import procesosEstocasticos.ProcesoEstocastico;


/**
 * Clase que representa el tanque de combustible
 * @author ut602614
 *
 */
public class TanqueCombustible extends Recurso{
							
	private Double capacidad;				/**Capacidad del tanque de combustible*/
	private Combustible combustible;		/**Combustible asociado al tanque*/
	private BarraCombustible barra;			/**Barra asociada al tanque*/
	
	
	/**Constructor de TanqueCombustible a partir de sus datos, combustible y barra*/
 
	public TanqueCombustible(DatosTanqueCombustibleCorrida dT,
			Combustible comb, BarraCombustible barra2) {
		
		//TODO: NADA HECHO, QUEDó EN PROTOTIPO FALTAN EVOLUCIONES
//		this.setNombre(dT.getNombre());
//		this.setCantModInst(dT.getCantModIni());
//		this.settMedioArreglo(dT.getT);
//		this.capacidad = dT.getCapacidad();
//		this.combustible = comb;
//		this.barra = barra2;
//		setCompDesp(new TanqueCombCompDesp(dT, this));
//		
//		compD = new HidraulicoCompDesp(this);
//		
//		compG = new HidraulicoComp(this,compD,compS);
//		compS = new HidraulicoCompSim(this, compD, compG);	
//		compG.setCompS(compS);
//		
//		compS.setCompgeneral(compG);
//		compS.setCompdespacho(compD);
//		compG.setCompSimulacion(compS);
//		compG.setCompDespacho(compD);
//		this.setCompDesp(compD);
//		this.setCompGeneral(compG);
//		this.setCompSimulacion(compS);
//		compD.setParticipante(this);
//		compS.setParticipante(this);
//		compG.setParticipante(this);
//		
//		compG.setEvolucionComportamientos(datosHidraulicoCorrida.getValoresComportamientos());
	}


	public Double getCapacidad() {
		return capacidad;
	}
	public void setCapacidad(Double capacidad) {
		this.capacidad = capacidad;
	}
	public Combustible getCombustible() {
		return combustible;
	}
	public void setCombustible(Combustible combustible) {
		this.combustible = combustible;
	}
	public BarraCombustible getBarra() {
		return barra;
	}
	public void setBarra(BarraCombustible barra) {
		this.barra = barra;
	}


	public String getNVarvol() {
		TanqueCombCompDesp tcc = (TanqueCombCompDesp)this.getCompDesp();
		return tcc.getNvarvol();
	}


	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,
			DatosSalidaProblemaLineal salidaUltimaIter, String proceso, long instante) {
		

		
	}


	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void asignaVAOptim() {
		// DELIBERADAMENTE EN BLANCO
		
	}


	@Override
	public void asignaVASimul() {
		// DELIBERADAMENTE EN BLANCO
		
	}


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
		// TODO Auto-generated method stub
		
	}
	

	
}
