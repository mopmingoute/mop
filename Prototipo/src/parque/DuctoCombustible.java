/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DuctoCombustible is part of MOP.
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

import logica.CorridaHandler;
import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import procesosEstocasticos.VariableAleatoriaEntera;
import tiempo.Evolucion;

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.DuctoCombCompDesp;
import compgeneral.DuctoCombComp;
import compsimulacion.DuctoCombCompSim;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.DuctoCombCompDespPE;
import datatypes.DatosDuctoCombCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
/**
 * Clase que representa el ducto de combustible
 * @author ut602614
 *
 */

public class DuctoCombustible extends Recurso{
				
	
	private Combustible combustible;
	private BarraCombustible barra1;	/**Barra extremo del ducto de combustible*/
	private BarraCombustible barra2;	/**Barra extremo del ducto de combustible*/
	private Evolucion<Double> capacidad12; 		/**Capacidad de transferencia de combustible desde la barra 1 a la barra 2 expresada en unidad de combustible por hora*/
	private Evolucion<Double> capacidad21; 		/**Capacidad de transferencia de combustible desde la barra 2 a la barra 1 expresada en unidad de combustible por hora*/
	private Evolucion<Double> perdidas12;  		/**Perdidas por unidad de la cantidad que sale de la barra 1*/
	private Evolucion<Double> perdidas21;  		/**Perdidas por unidad de la cantidad que sale de la barra 2*/
	

	/**Constructor de ducto a partir de sus datos, el combustible asociado y ambas barras*/
	public DuctoCombustible(DatosDuctoCombCorrida dD, Combustible comb, BarraCombustible barra1, BarraCombustible barra2) {
		this.setNombre(dD.getNombre());
		this.combustible = comb;
		this.setCapacidad12(dD.getCapacidad12());
		this.setCapacidad21(dD.getCapacidad21());
		this.setPerdidas12(dD.getPerdidas12());
		this.setPerdidas21(dD.getPerdidas21());
		this.barra1 = barra1;
		this.barra2 = barra2;
		this.setDispMedia(dD.getDispMedia());
		this.settMedioArreglo(dD.gettMedioArreglo());
		this.setCantModInst(dD.getCantModInst());
		this.setMantProgramado(dD.getMantProgramado());
		this.setCostoFijo(dD.getCostoFijo());
		DuctoCombCompDesp compD = new DuctoCombCompDesp(this);
		DuctoCombComp compG= new DuctoCombComp();
		DuctoCombCompSim  compS = new DuctoCombCompSim();	
		
		compS.setCompgeneral(compG);
		compS.setCompdespacho(compD);
		compG.setCompSimulacion(compS);
		compG.setCompDespacho(compD);
		this.setCompDesp(compD);
		this.setCompGeneral(compG);
		this.setCompSimulacion(compS);
		compD.setParticipante(this);
		compS.setParticipante(this);
		compG.setParticipante(this);
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		String nombreD = "Disp-" + this.getNombre();
		PEDisponibilidadGeometrica pedg = new PEDisponibilidadGeometrica(nombreD, this.getNombre(), this.getDispMedia(),this.gettMedioArreglo(),this.getCantModInst(), this.getMantProgramado(), null,this.getCantModInst().getValor(instanteActual), dD.getCantModIni(), actual.getInstanteInicialPPaso(), actual.getInstanteFinalPPaso());
		int cantDispIni = Math.min(dD.getCantModIni(), dD.getCantModInst().getValor(instanteActual));
		this.setCantModDisp(new VariableAleatoria());
		getCantModDisp().setValor((double)cantDispIni);
		pedg.setCantDisponibles(this.getCantModDisp());
		for (int i = 0; i < getCantModInst().getValor(instanteActual); i++) {
			VariableAleatoria modI = new VariableAleatoria();
			modI.setNombre(this.getNombre() + "modDisp_" + i);
			modI.setMuestreada(false);
			if (cantDispIni > 0) {
				modI.setValor(1.0);
			} else {
				modI.setValor(0.0);
			}			
			pedg.getVariablesAleatorias().add(modI);
			pedg.getNombresVarsAleatorias().add(modI.getNombre());
			cantDispIni--;	
		}
		
			
		actual.agregarPE(pedg);
		pedg.setCantidadInnovaciones(this.getCantModInst().getValor(instanteActual));		 
		this.getCantModDisp().setPe(pedg);
		
		
//		// carga las evoluciones del participante en la colección
//		this.agregarEvolucionAColeccion(capacidad12);
//		this.agregarEvolucionAColeccion(capacidad21);
//		this.agregarEvolucionAColeccion(perdidas12);
//		this.agregarEvolucionAColeccion(perdidas21);
		
	}
	
	public Combustible getCombustible() {
		return combustible;
	}
	public void setCombustible(Combustible combustible) {
		this.combustible = combustible;
	}
	public BarraCombustible getBarra1() {
		return barra1;
	}
	public void setBarra1(BarraCombustible barra1) {
		this.barra1 = barra1;
	}
	public BarraCombustible getBarra2() {
		return barra2;
	}
	public void setBarra2(BarraCombustible barra2) {
		this.barra2 = barra2;
	}
	
	public String getNFlujo() {
		DuctoCombCompDesp dcc = (DuctoCombCompDesp)this.getCompDesp();
		return dcc.getNflujo();
	}



	public Evolucion<Double> getCapacidad12() {
		return capacidad12;
	}

	public void setCapacidad12(Evolucion<Double> capacidad12) {
		this.capacidad12 = capacidad12;
	}

	public Evolucion<Double> getCapacidad21() {
		return capacidad21;
	}

	public void setCapacidad21(Evolucion<Double> capacidad21) {
		this.capacidad21 = capacidad21;
	}

	public Evolucion<Double> getPerdidas12() {
		return perdidas12;
	}

	public void setPerdidas12(Evolucion<Double> perdidas12) {
		this.perdidas12 = perdidas12;
	}

	public Evolucion<Double> getPerdidas21() {
		return perdidas21;
	}

	public void setPerdidas21(Evolucion<Double> perdidas21) {
		this.perdidas21 = perdidas21;
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
		ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();
		ret.add(this.getCantModDisp().getPe());
		return ret;
	}

	@Override
	public void asignaVAOptim() {
		// Deliberadamente en blanco		
	}

	@Override
	public void asignaVASimul() {
		// Deliberadamente en blanco		
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
		// TODO 
		
	}
	
}

