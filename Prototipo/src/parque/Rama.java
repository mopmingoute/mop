/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Rama is part of MOP.
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

import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import procesosEstocasticos.VariableAleatoriaEntera;
import tiempo.Evolucion;

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.RamaCompDesp;
import compgeneral.RamaComp;
import compsimulacion.RamaCompSim;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.RamaCompDespPE;
import datatypes.DatosRamaCorrida;
import datatypes.Peaje;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import logica.CorridaHandler;

/**
 * Clase que representa la rama elóctrica
 * @author ut602614
 *
 */
public class Rama extends Recurso{
	private Barra barra1;				/**Barra elóctrica extremo de la rama*/
	private Barra barra2;				/**Barra elóctrica extremo de la rama*/
	
	private Peaje peaje12;				/**Peaje de la barra1 a la barra2*/
	private Peaje peaje21;				/**Peaje de la barra2 a la barra1*/
	
	private Evolucion<Double> potenciaMaxima12; 	/**Potencia móxima en MW que puede salir de 1 hacia 2, medida en 1*/
	private Evolucion<Double> potenciaMaxima21; 	/**Potencia móxima en MW que puede salir de 2 hacia 1, medida en 2*/
	
	private Evolucion<Double> perdidas12;  		/**Perdidas en x1 de la cantidad que sale de 1*/
	private Evolucion<Double> perdidas21;  
	
	//reactancia serie en por uno
	private Evolucion<Double> X;
	//resistencia serie en por uno
	private Evolucion<Double> R;
	
	private RamaCompDesp comportamiento;
	
	private RamaCompDesp compD;
	private RamaComp compG;
	private RamaCompSim  compS;	
	
	public Rama(DatosRamaCorrida datos, Barra b1, Barra b2) {
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		this.setNombre(datos.getNombre());
		this.barra1 = b1;
		this.barra2 = b2;
		this.peaje12 = new Peaje();
		this.peaje21 = new Peaje();
		this.potenciaMaxima12 = datos.getPotMax12();
		this.potenciaMaxima21 = datos.getPotMax21();		
		this.perdidas12 = datos.getPerdidas12();
		this.perdidas21 = datos.getPerdidas21();
		
		
		this.X = datos.getX();
		this.setR(datos.getR());
		this.setDispMedia(datos.getDispMedia());
		this.settMedioArreglo(datos.gettMedioArreglo());
		this.setCantModInst(datos.getCantModInst());
		this.setPropietario(datos.getPropietario());
		this.setMantProgramado(datos.getMantProgramado());
		this.setCostoFijo(datos.getCostoFijo());
		
		compD = new RamaCompDesp(this);
		compG= new RamaComp();
		compS = new RamaCompSim();	
		
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
		
		compG.setEvolucionComportamientos(datos.getValoresComportamientos());
		
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		String nombreD = "Disp-" + this.getNombre();
		PEDisponibilidadGeometrica pedg = new PEDisponibilidadGeometrica(nombreD, this.getNombre(), this.getDispMedia(),this.gettMedioArreglo(),this.getCantModInst(), 
				this.getMantProgramado(), null,this.getCantModInst().getValor(instanteActual).intValue(),datos.getCantModIni(), actual.getInstanteInicialPPaso(),
				actual.getInstanteFinalPPaso());
		//pedg.setAzar(this.getSimPaso().getAzar());
		int cantDispIni = Math.min(datos.getCantModIni(), datos.getCantModInst().getValor(instanteActual));
		
		this.setCantModDisp(new VariableAleatoria());
		getCantModDisp().setValor((double)cantDispIni);
		pedg.setCantDisponibles(this.getCantModDisp());
		for (int i = 0; i < getCantModInst().getValor(instanteActual)-getMantProgramado().getValor(instanteActual); i++) {
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
	
	}
		
	public Barra getBarra1() {
		return barra1;
	}
	public void setBarra1(Barra barra1) {
		this.barra1 = barra1;
	}
	public Barra getBarra2() {
		return barra2;
	}
	public void setBarra2(Barra barra2) {
		this.barra2 = barra2;
	}
	public Peaje getPeaje12() {
		return peaje12;
	}
	public void setPeaje12(Peaje peaje12) {
		this.peaje12 = peaje12;
	}
	public Peaje getPeaje21() {
		return peaje21;
	}
	public void setPeaje21(Peaje peaje21) {
		this.peaje21 = peaje21;
	}
	
	
	public Evolucion<Double> getX() {
		return X;
	}

	public void setX(Evolucion<Double> x) {
		X = x;
	}

	public Evolucion<Double> getR() {
		return R;
	}

	public void setR(Evolucion<Double> r) {
		R = r;
	}

	public RamaCompDesp getComportamiento() {
		return comportamiento;
	}

	public void setComportamiento(RamaCompDesp comportamiento) {
		this.comportamiento = comportamiento;
	}

	public DatosRestriccion dameAporteABarra(Barra barra, int poste) {
		return this.comportamiento.dameAporteABarra(barra, poste);
	}

	public Barra dameLaOtraBarra(Barra barra) {		
		return (this.barra1 == barra) ? this.barra2 : this.barra1;
	}

	
	public Evolucion<Double> getPotenciaMaxima12() {
		return potenciaMaxima12;
	}

	public void setPotenciaMaxima12(Evolucion<Double> potenciaMaxima12) {
		this.potenciaMaxima12 = potenciaMaxima12;
	}

	public Evolucion<Double> getPotenciaMaxima21() {
		return potenciaMaxima21;
	}

	public void setPotenciaMaxima21(Evolucion<Double> potenciaMaxima21) {
		this.potenciaMaxima21 = potenciaMaxima21;
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
		RamaCompDespPE compDespPE = new RamaCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}
	
}


