/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BarraCombustible is part of MOP.
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

import compdespacho.BarraCombustibleCompDesp;
import compgeneral.BarraCombustibleComp;
import compsimulacion.BarraCombustibleCompSim;
import cp_compdespProgEst.BarraCombCompDespPE;
import cp_compdespProgEst.BarraCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import datatypes.DatosBarraCombCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraCombSP;
import datatypesSalida.DatosSalidaPaso;
import procesosEstocasticos.ProcesoEstocastico;
import utilitarios.Constantes;

/**
 * Clase que representa la barra de combustible
 * @author ut602614
 *
 */
public class BarraCombustible extends Participante{
	
	private BarraCombustibleCompDesp compD; 
	private BarraCombustibleComp compG;
	private BarraCombustibleCompSim compS;
	
	private Combustible comb;									/**Combustible asociado a la barra*/
	private ArrayList<DuctoCombustible> ductosEntrantes;
	private ArrayList<DuctoCombustible> ductosSalientes;
	private ArrayList<TanqueCombustible> tanques;
	private ArrayList<GeneradorTermico> generadoresConectados;
	private ArrayList<CicloCombinado> ciclosCombConectados;
	private ArrayList<ContratoCombustible> contratos;
	private ArrayList<ConvertidorCombustible> convertidoresEntrantes;
	private ArrayList<ConvertidorCombustible> convertidoresSalientes;
	private RedCombustible redAsociada;
	

	public BarraCombustible(DatosBarraCombCorrida dB, Combustible comb2) {
		this.setNombre(dB.getNombre());
		ductosEntrantes = new ArrayList<DuctoCombustible>();
		ductosSalientes = new ArrayList<DuctoCombustible>();
		tanques = new ArrayList<TanqueCombustible>();
		generadoresConectados = new ArrayList<GeneradorTermico>();
		ciclosCombConectados = new ArrayList<CicloCombinado>();
		contratos =  new ArrayList<ContratoCombustible>();
		convertidoresEntrantes = new ArrayList<ConvertidorCombustible> ();
		convertidoresSalientes = new ArrayList<ConvertidorCombustible>();
		this.comb = comb2;
		
		compD = new BarraCombustibleCompDesp(this);  
		compG= new BarraCombustibleComp();
		compS = new BarraCombustibleCompSim(this, compD, compG);	
		
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
		compD.setBarra(this);
	}

	public BarraCombustible() {
		ductosEntrantes = new ArrayList<DuctoCombustible>();
		ductosSalientes = new ArrayList<DuctoCombustible>();
		tanques = new ArrayList<TanqueCombustible>();
		generadoresConectados = new ArrayList<GeneradorTermico>();
		ciclosCombConectados = new ArrayList<CicloCombinado>();
		contratos =  new ArrayList<ContratoCombustible>();
		convertidoresEntrantes = new ArrayList<ConvertidorCombustible> ();
		convertidoresSalientes = new ArrayList<ConvertidorCombustible>();
		
	}

	
	public Combustible getComb() {
		return comb;
	}

	public void setComb(Combustible comb) {
		this.comb = comb;
	}
	
	public ArrayList<DuctoCombustible> getDuctosEntrantes() {
		return ductosEntrantes;
	}

	public void setDuctosEntrantes(ArrayList<DuctoCombustible> ductosEntrantes) {
		this.ductosEntrantes = ductosEntrantes;
	}

	public ArrayList<DuctoCombustible> getDuctosSalientes() {
		return ductosSalientes;
	}

	public void setDuctosSalientes(ArrayList<DuctoCombustible> ductosSalientes) {
		this.ductosSalientes = ductosSalientes;
	}

	public ArrayList<TanqueCombustible> getTanques() {
		return tanques;
	}

	public void setTanques(ArrayList<TanqueCombustible> tanques) {
		this.tanques = tanques;
	}

	public ArrayList<GeneradorTermico> getGeneradoresConectados() {
		return generadoresConectados;
	}

	public void setGeneradoresConectados(ArrayList<GeneradorTermico> generadoresConectados) {
		this.generadoresConectados = generadoresConectados;
	}

	public ArrayList<ContratoCombustible> getContratos() {
		return contratos;
	}

	public void setContratos(ArrayList<ContratoCombustible> contratos) {
		this.contratos = contratos;
	}

	public ArrayList<ConvertidorCombustible> getConvertidoresEntrantes() {
		return convertidoresEntrantes;
	}

	public void setConvertidoresEntrantes(ArrayList<ConvertidorCombustible> convertidoresEntrantes) {
		this.convertidoresEntrantes = convertidoresEntrantes;
	}

	public ArrayList<ConvertidorCombustible> getConvertidoresSalientes() {
		return convertidoresSalientes;
	}

	public void setConvertidoresSalientes(ArrayList<ConvertidorCombustible> convertidoresSalientes) {
		this.convertidoresSalientes = convertidoresSalientes;
	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}

	public RedCombustible getRedAsociada() {
		return redAsociada;
	}

	public void setRedAsociada(RedCombustible redAsociada) {
		this.redAsociada = redAsociada;
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,
			DatosSalidaProblemaLineal salidaUltimaIter, String proceso, long instante) {
		DatosBarraCombSP db = new DatosBarraCombSP();
		if(this.redAsociada.isUninodal() && this.getNombre().equalsIgnoreCase(Constantes.NOMBREBARRAUNICA)
				|| !this.redAsociada.isUninodal()){
			db.setNombre(this.getNombre());
			// el poste 0 es por compatibilidad con la función, hay un balance ónico por paso
			double costoMargComb = devuelveVarDualBalance(salidaUltimaIter, 0);
			db.setCostoMarg(costoMargComb);
			resultadoPaso.getRedComb(this.getRedAsociada().getNombre()).getBarras().add(db);
			
			for (ContratoCombustible c: this.contratos) {
				c.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
			}
			
			for (TanqueCombustible t: this.tanques) {
				t.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
			}
		}
			
	}
	
	
	
	/**
	 * Devuelve la variable dual del valor del combustible en la barra en USD/unidad del combustible
	 * En el caso mós simple es el precio variable
	 * @param salidaUltimaIter
	 * @param poste EN ESTA IMPLEMENTACIóN INICIAL NO SE USA PERO PODRóA EN EL FUTURO
	 * @return
	 */
	public double devuelveVarDualBalance(DatosSalidaProblemaLineal salidaUltimaIter, int poste){
		return this.getCompS().devuelveVarDualBalance(salidaUltimaIter);		
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

	public BarraCombustibleCompDesp getCompD() {
		return compD;
	}

	public void setCompD(BarraCombustibleCompDesp compD) {
		this.compD = compD;
	}

	public BarraCombustibleComp getCompG() {
		return compG;
	}

	public void setCompG(BarraCombustibleComp compG) {
		this.compG = compG;
	}

	public BarraCombustibleCompSim getCompS() {
		return compS;
	}

	public void setCompS(BarraCombustibleCompSim compS) {
		this.compS = compS;
	}
	

	public ArrayList<CicloCombinado> getCiclosCombConectados() {
		return ciclosCombConectados;
	}

	public void setCiclosCombConectados(ArrayList<CicloCombinado> ciclosCombConectados) {
		this.ciclosCombConectados = ciclosCombConectados;
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
		BarraCombCompDespPE compDespPE = new BarraCombCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}

}
