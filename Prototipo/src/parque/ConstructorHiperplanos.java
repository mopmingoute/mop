/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConstructorHiperplanos is part of MOP.
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

import compdespacho.ConstructorHiperplanosCompDesp;
import compdespacho.HidraulicoCompDesp;
import compdespacho.ImpoExpoCompDesp;
import compgeneral.ConstructorHiperplanosComp;
import compgeneral.HidraulicoComp;
import compgeneral.ImpoExpoComp;
import compsimulacion.ConstructorHiperplanosCompSim;
import compsimulacion.HidraulicoCompSim;
import compsimulacion.ImpoExpoCompSim;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.ConstHiperCompDespPE;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesResOptim.DatosHiperplano;
import datatypesSalida.DatosConstructorHiperplanosSP;
import datatypesSalida.DatosSalidaPaso;
import futuro.AFHiperplanos;
import futuro.Hiperplano;
import logica.CorridaHandler;
import optimizacion.ResOptimHiperplanos;
import procesosEstocasticos.ProcesoEstocastico;

public class ConstructorHiperplanos extends Participante{
	
	
	AFHiperplanos afhiperplanos;
	
	private ConstructorHiperplanosCompDesp compD;
	private ConstructorHiperplanosCompSim compS;
	private ConstructorHiperplanosComp compG;
	

	public ConstructorHiperplanos(){
		
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		
		this.setNombre("Constructor Hiperplanos");


		compD = new ConstructorHiperplanosCompDesp(this);
		compG = new ConstructorHiperplanosComp(this,compD,compS);
		compS = new ConstructorHiperplanosCompSim(compD, this,  compG);	
		
		compS.setCompG(compG);
		compS.setCompD(compD);
//		compG.setCompS(compS);
//		compG.setCompD(compD);
		this.setCompDesp(compD);
		this.setCompG(compG);
		this.setCompS(compS);
		this.setCompDesp(compD);
		this.setCompSimulacion(compS);
		compD.setParticipante(this);
		compS.setParticipante(this);
//		compG.setParticipante(this);		
		
		
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
	public void inicializarParaEscenario() {
		// Deliberadamente en blanco
		
	}


	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {
		// Se sacan los datos de los hiperplanos que estón activos en el óptimo y sus variables duales 		
		ArrayList<DatosHiperplano> datosHipsActivos = new ArrayList<DatosHiperplano>();
		ArrayList<String> nombresHipActivos = new ArrayList<String>(); 
		Hashtable<String, Double> duales = salidaUltimaIter.getDuales();
		ArrayList<String> nombresRestH = compD.getNombresRestHiperplanos();
		int indH = 0;
		for(String nr: nombresRestH){
			double dual = duales.get(nr);
			DatosHiperplano dh = afhiperplanos.getHiperplanos().get(indH).creaDataType();
			dh.setVdual(dual);
			datosHipsActivos.add(dh);				
			nombresHipActivos.add(nr);			
			indH++;
		}
		DatosConstructorHiperplanosSP consHip = new DatosConstructorHiperplanosSP(nombresHipActivos, datosHipsActivos);
		resultadoPaso.setConsHip(consHip);
	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// Deliberadamente en blanco
		return null;
	}

	public AFHiperplanos getAfhiperplanos() {
		return afhiperplanos;
	}

	public void setAfhiperplanos(AFHiperplanos afhiperplanos) {
		this.afhiperplanos = afhiperplanos;
	}

	public ConstructorHiperplanosCompDesp getCompD() {
		return compD;
	}

	public void setCompD(ConstructorHiperplanosCompDesp compD) {
		this.compD = compD;
	}

	public ConstructorHiperplanosCompSim getCompS() {
		return compS;
	}

	public void setCompS(ConstructorHiperplanosCompSim compS) {
		this.compS = compS;
	}

	public ConstructorHiperplanosComp getCompG() {
		return compG;
	}

	public void setCompG(ConstructorHiperplanosComp compG) {
		this.compG = compG;
	}


	public void preparaParaPaso(int numpaso, ResOptimHiperplanos roptimH) {
		compS.preparaParaPaso(numpaso, roptimH);
		
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
		// DELIBERADAMENTE EN BLANCO
	}
	

}