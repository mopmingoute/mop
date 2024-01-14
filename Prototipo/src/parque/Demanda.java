/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Demanda is part of MOP.
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
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import utilitarios.Constantes;

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.DemandaCompDesp;
import compgeneral.DemandaComp;
import compsimulacion.DemandaCompSim;
import cp_compdespProgEst.DemandaCompDespPE;
import datatypes.DatosDemandaCorrida;
import datatypes.Pair;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosDemandaSP;
import datatypesSalida.DatosFallaSP;
import datatypesSalida.DatosSalidaPaso;
/**
 * Clase que representa la demanda
 * @author ut602614
 *
 */
public class Demanda extends Participante {
	
	private Barra barra;							/**Barra desde donde se produce la demanda*/

	private Falla falla;				/**Conjunto de fallas asociadas a la demanda*/
	private DemandaCompDesp compD;
	private DemandaCompSim compS;
	private DemandaComp compG;
	private VariableAleatoria demanda;
	private String nombreVA;
	private static ArrayList<String> atributosDetallados;

	/**Constructor de la demanda a partir de los datos y la barra asociada*/
	public Demanda(DatosDemandaCorrida datos, Barra barra) {
		setNombre(datos.getNombre()); 
		this.barra = barra;		
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		
		compD = new DemandaCompDesp(this);
		compG = new DemandaComp(this,compD,compS);
		compS = new DemandaCompSim(this, compD, compG);	
		
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
		
		
		/**
		 * VARIABLE ALEATORIA DE DEMANDA
		 */
		ProcesoEstocastico proc = actual.dameProcesoEstocastico(datos.getPotActiva().getProcSimulacion());
		ProcesoEstocastico procOptim = actual.dameProcesoEstocastico(datos.getPotActiva().getProcOptimizacion());		
		nombreVA = datos.getPotActiva().getNombre();
		this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_OPT, procOptim);
		this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_SIM, proc);	
		if(proc!=procOptim) this.chequearProcesosConAsociadoEnOptim(proc, procOptim);
		
	}

	public Barra getBarra() {
		return barra;
	}

	public void setBarra(Barra barra) {
		this.barra = barra;
	}



	public DemandaCompDesp getCompD() {
		return compD;
	}

	public void setCompD(DemandaCompDesp compD) {
		this.compD = compD;
	}

	public DemandaCompSim getCompS() {
		return compS;
	}

	public void setCompS(DemandaCompSim compS) {
		this.compS = compS;
	}

	public DemandaComp getCompG() {
		return compG;
	}

	public void setCompG(DemandaComp compG) {
		this.compG = compG;
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}
	
	public VariableAleatoria getDemanda() {
		return demanda;
	}

	public void setDemanda(VariableAleatoria demanda) {
		this.demanda = demanda;
	}
	

	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		Demanda.atributosDetallados = atributosDetallados;
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,
			DatosSalidaProblemaLineal salidaUltimaIter, String proceso, long instante) {
		
		
		DatosDemandaSP demanda = new DatosDemandaSP();
		demanda.setNombre(this.getNombre());
		demanda.setNombreBarra(this.barra.getNombre());
		demanda.setPotencias(this.getCompD().getPotActivaPorPoste());
//		/**
//		 * IMPRESIÓN TRUCHA
//		 */
//		String salida="guardar resultados";
//		for(double d: demanda.getPotencias()) {
//			System.out.println(d);
//		}
		
		
		
		
		double [] costo = new double[this.getFalla().getCantEscalones()];
		double [] costoAux = new double[this.getFalla().getCantEscalones()];
		int e = 0;
		for (Pair<Double,Double> p: this.getFalla().getEscalones()) {
			costoAux[e] = p.second;
			e++;
		}
		double costoTotalPaso = 0.0;
	
		double [][] potencias = new double[this.getCantPostes()][this.getFalla().getCantEscalones()];
		for (int p = 0; p < this.getCantPostes(); p++) {
			for (int esc = 0; esc <this.falla.getCantEscalones(); esc++ ) {
				potencias[p][esc] = salidaUltimaIter.getSolucion().get(this.falla.getCompDesp().generarNombre("potpe_", p + "_" + esc));
				costoTotalPaso += potencias[p][esc]*costoAux[esc]*this.getDuracionPostes(p)/Constantes.SEGUNDOSXHORA;
				costo[esc] += potencias[p][esc]*costoAux[esc]*this.getDuracionPostes(p)/Constantes.SEGUNDOSXHORA;
			}
		}
		DatosFallaSP falla = new DatosFallaSP(this.getNombre(), this.getFalla().getNombre(),costo, this.falla.dameProf(this.getCompD().getPotActivaPorPoste()), potencias, costoTotalPaso);
		demanda.setFalla(falla);
		for (DatosBarraSP dbsp: resultadoPaso.getRed().getBarras()) {
			if (this.getBarra().getNombre().equalsIgnoreCase(dbsp.getNombre()) || this.getBarra().getRedAsociada().getCompD().isUninodal()){
				dbsp.agregarDemanda(demanda);
				break;
			}
		}
	}

	public Falla getFalla() {
		return falla;
	}

	public void setFalla(Falla falla) {
		this.falla = falla;
	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();		
		ProcesoEstocastico peAporteOptim ;		
		peAporteOptim = this.devuelveProceso(nombreVA, Constantes.FASE_OPT);		
		ret.add(peAporteOptim);
		return ret;
		
	}

	@Override
	public void asignaVAOptim() {
		if(this.getProcesosDelParticipante()!=null){
			ProcesoEstocastico peOptim;
			String nombreAux;
			if (this.getSimPaso().getValPostizador().isExterna()) {
				nombreAux = nombreVA+"1";
				
			} else {
				nombreAux = nombreVA;
			}
			peOptim = this.devuelveProceso(nombreVA, Constantes.FASE_OPT);
			if (peOptim != null) {			
				VariableAleatoria vaDemanda = peOptim.devuelveVADeNombre(nombreAux);			
				demanda = vaDemanda;
			}
		}		
	}

	public String getNombreVA() {
		return nombreVA;
	}

	public void setNombreVA(String nombreVA) {
		this.nombreVA = nombreVA;
	}

	@Override
	public void asignaVASimul() {
		if(this.getProcesosDelParticipante()!=null){
			ProcesoEstocastico peSim;	
			String nombreAux;
			if (this.getSimPaso().getValPostizador().isExterna()) {
				nombreAux = nombreVA+"1";
				
			} else {
				nombreAux = nombreVA;
			}
			
			peSim= this.devuelveProceso(nombreVA, Constantes.FASE_SIM);
			
			if (peSim != null) {			
				VariableAleatoria vaDemanda = peSim.devuelveVADeNombre(nombreAux);
				demanda = vaDemanda;
			}
			
		}	
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
		DemandaCompDespPE compDespPE = new DemandaCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}
	


}
