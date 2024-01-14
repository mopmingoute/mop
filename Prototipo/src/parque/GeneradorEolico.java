/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorEolico is part of MOP.
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
import utilitarios.Constantes;

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.BarraCompDesp;
import compdespacho.EolicoCompDesp;
import compgeneral.EolicoComp;
import compsimulacion.EolicoCompSim;
import cp_compdespProgEst.AcumuladorCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import cp_compdespProgEst.EolicoCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import logica.CorridaHandler;
import datatypes.DatosEolicoCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosEolicoSP;
import datatypesSalida.DatosSalidaPaso;

/**
 * Clase que representa el generador eólico
 * @author ut602614
 *
 */

public class GeneradorEolico extends Generador {
	
	private VariableAleatoria factor;  
	private EolicoCompDesp compD;
	private EolicoComp compG;
	private EolicoCompSim compS;
	private String nombreVA;
	private static ArrayList<String> atributosDetallados;
	
	/**Constructor del Generador eólico a partir de sus datos*/
	public GeneradorEolico(DatosEolicoCorrida datosEolicoCorrida) {
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		
		this.setNombre(datosEolicoCorrida.getNombre());
		this.setBarra(CorridaHandler.getInstance().getBarra(datosEolicoCorrida.getBarra()));
		this.setCantModInst(datosEolicoCorrida.getCantModInst());
		this.setPropietario(datosEolicoCorrida.getPropietario());
		this.setPropietario(datosEolicoCorrida.getPropietario());
		// TODO: OJO NO ENTIENDO LA SENTENCIA SIGUIENTE
		this.setCompDesp(new EolicoCompDesp());
		this.setPotenciaMaxima(datosEolicoCorrida.getPotMax());
		this.setMinimoTecnico(datosEolicoCorrida.getPotMin());
		this.setDispMedia(datosEolicoCorrida.getDispMedia());
		this.settMedioArreglo(datosEolicoCorrida.gettMedioArreglo());
		this.setMantProgramado(datosEolicoCorrida.getMantProgramado());
		this.setCostoFijo(datosEolicoCorrida.getCostoFijo());
		this.setCostoVariable(datosEolicoCorrida.getCostoVariable());
		

		compD = new EolicoCompDesp(this);
		compG = new EolicoComp(this,compD,compS); 
		compS = new EolicoCompSim(this, compD, compG);	
		
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

		String nombreD = "Disp-" + this.getNombre();
		PEDisponibilidadGeometrica pedg = new PEDisponibilidadGeometrica(nombreD, this.getNombre(), this.getDispMedia(),this.gettMedioArreglo(),this.getCantModInst(),this.getMantProgramado(), null, this.getCantModInst().getValor(instanteActual),datosEolicoCorrida.getCantModIni(), actual.getInstanteInicialPPaso(), actual.getInstanteFinalPPaso());
		int cantDispIni = Math.min(datosEolicoCorrida.getCantModIni(), datosEolicoCorrida.getCantModInst().getValor(instanteActual));		
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
		
		/**
		 * VARIABLE ALEATORIA FACTOR
		 */
		ProcesoEstocastico proc = actual.dameProcesoEstocastico(datosEolicoCorrida.getFactor().getProcSimulacion());
		ProcesoEstocastico procOptim = actual.dameProcesoEstocastico(datosEolicoCorrida.getFactor().getProcOptimizacion());
				
		nombreVA = datosEolicoCorrida.getFactor().getNombre();

		this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_OPT, procOptim);
		this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_SIM, proc);	
		this.chequearProcesosConAsociadoEnOptim(proc, procOptim);		
	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}

	public VariableAleatoria getFactor() {
		return factor;
	}

	public void setFactor(VariableAleatoria factor) {
		this.factor = factor;
	}

	public EolicoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(EolicoCompDesp compD) {
		this.compD = compD;
	}

	public EolicoComp getCompG() {
		return compG;
	}

	public void setCompG(EolicoComp compG) {
		this.compG = compG;
	}

	public EolicoCompSim getCompS() {
		return compS;
	}

	public void setCompS(EolicoCompSim compS) {
		this.compS = compS;
	}
	

	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		GeneradorEolico.atributosDetallados = atributosDetallados;
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,
			DatosSalidaProblemaLineal salidaUltimaIter, String proceso, long instante) {
		double [] potencia = new double [this.getCantPostes()];
		double energiaMWh = 0.0;
		double energiaMWhp = 0.0;	
		double valorEnergAlMarginal = 0.0;
		
		double[] costosMarginales = new double[this.getCantPostes()];
		Barra b = this.getBarra();
		boolean uninodal = this.getBarra().isRedUninodal(instante);		
		if (uninodal) {
			b = this.getBarra().dameBarraUnica();
		}		
		BarraCompDesp bcd = (BarraCompDesp)b.getCompDesp();
		for (int p = 0; p < this.getCantPostes(); p++) {
			String nombreRest = bcd.nombreRestPoste(p);
			costosMarginales[p] = salidaUltimaIter.getDuales().get(nombreRest)*Constantes.SEGUNDOSXHORA/this.getDuracionPostes(p);
		}	
		
		for (int p = 0; p < this.getCantPostes(); p++) {
			potencia[p] = salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("potp", Integer.toString(p)));
			if(this.getSimPaso().getEscenario()==1 && this.getSimPaso().getNumpaso()==68) {
			 int pp=0;
			}
			energiaMWhp = potencia[p]*this.getDuracionPostes(p)/utilitarios.Constantes.SEGUNDOSXHORA;
			energiaMWh += energiaMWhp;
			valorEnergAlMarginal += energiaMWhp*costosMarginales[p];  // valor de la energóa en USD			
		}
		
		double costoVarOyMPaso = this.compS.calculaCostoPaso(salidaUltimaIter); // Se agrega costo O&M
		int cantModDisp = this.getCantModDisp().getValor().intValue();
		// Cólculo del gradiente de gestión para disponibilidad 1 en USD/MW
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		double gradGestion = Math.max((valorEnergAlMarginal - costoVarOyMPaso)/(cantModDisp*this.getPotenciaMaxima().getValor(instante)),0);
	
		double costoPaso=this.compS.calculaCostoPaso(salidaUltimaIter);
		DatosEolicoSP eolo = new DatosEolicoSP(this.getNombre(), this.getBarra().getNombre(),
				potencia, this.getCantModDisp().getValor().intValue(), this.getPotenciaMaxima().getValor(instante), costoPaso, gradGestion);
		String nombarra = this.getBarra().getNombre();
		for (DatosBarraSP dbsp: resultadoPaso.getRed().getBarras()) {
			if (nombarra.equalsIgnoreCase(dbsp.getNombre()) || this.getBarra().getRedAsociada().getCompD().isUninodal()){
				dbsp.agregarEolico(eolo);
				break;
			}
		}
	
	}
	
	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
	ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();		
		ProcesoEstocastico peOptim ;
		peOptim = this.devuelveProceso(nombreVA, Constantes.FASE_OPT);
		ret.add(peOptim);
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
				VariableAleatoria vaFactor = peOptim.devuelveVADeNombre(nombreAux);			
				factor = vaFactor;
				
			}
		}
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
				VariableAleatoria vaFactor = peSim.devuelveVADeNombre(nombreAux);			
				factor = vaFactor;
				
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
	public void aportarImpacto(Impacto i,DatosObjetivo costo ) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return 0.0;
	}
	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// TODO Auto-generated method stub
		return this.compD.cargarRestriccionesImpacto(impacto);
	}

	@Override
	public void crearCompDespPE() {
		EolicoCompDespPE compDespPE = new EolicoCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}
	
	@Override
	public GeneradorCompDespPE devuelveCompDespPE() {
		return (EolicoCompDespPE) getCompDespPE();
	}
	
}

