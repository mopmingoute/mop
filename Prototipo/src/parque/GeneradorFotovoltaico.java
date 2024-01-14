/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorFotovoltaico is part of MOP.
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

import compdespacho.BarraCompDesp;
import compdespacho.EolicoCompDesp;
import compdespacho.FotovoltaicoCompDesp;
import compgeneral.EolicoComp;
import compgeneral.FotovoltaicoComp;
import compsimulacion.EolicoCompSim;
import compsimulacion.FotovoltaicoCompSim;
import cp_compdespProgEst.AcumuladorCompDespPE;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.FotovoltaicoCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import datatypes.DatosEolicoCorrida;
import datatypes.DatosFotovoltaicoCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosEolicoSP;
import datatypesSalida.DatosFotovoltaicoSP;
import datatypesSalida.DatosSalidaPaso;
import logica.CorridaHandler;
import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import utilitarios.Constantes;
/**
 * Clase que representa el generador fotovoltóico
 * @author ut602614
 *
 */
public class GeneradorFotovoltaico extends Generador{

	private VariableAleatoria factor;  // factor potencia instantónea / potencia nominal
	private FotovoltaicoCompDesp compD;
	private FotovoltaicoComp compG;
	private FotovoltaicoCompSim compS;
	private String nombreVA;
	private static ArrayList<String> atributosDetallados;
	
	/**Constructor del Generador fotovoltaico a partir de sus datos*/
	public GeneradorFotovoltaico(DatosFotovoltaicoCorrida datosFotovoltaicoCorrida) {
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		
		this.setNombre(datosFotovoltaicoCorrida.getNombre());
		this.setBarra(CorridaHandler.getInstance().getBarra(datosFotovoltaicoCorrida.getBarra()));
		this.setCantModInst(datosFotovoltaicoCorrida.getCantModInst());
		this.setPropietario(datosFotovoltaicoCorrida.getPropietario());
		this.setCompDesp(new FotovoltaicoCompDesp());
		this.setPotenciaMaxima(datosFotovoltaicoCorrida.getPotMax());
		this.setMinimoTecnico(datosFotovoltaicoCorrida.getPotMin());
		this.setDispMedia(datosFotovoltaicoCorrida.getDispMedia());
		this.settMedioArreglo(datosFotovoltaicoCorrida.gettMedioArreglo());
		this.setMantProgramado(datosFotovoltaicoCorrida.getMantProgramado());
		this.setCostoFijo(datosFotovoltaicoCorrida.getCostoFijo());
		this.setCostoVariable(datosFotovoltaicoCorrida.getCostoVariable());
		
		
		compD = new FotovoltaicoCompDesp(this);
		compG = new FotovoltaicoComp(this,compD,compS);
		compS = new FotovoltaicoCompSim(this, compD, compG);	
		
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
		PEDisponibilidadGeometrica pedg = new PEDisponibilidadGeometrica(nombreD, this.getNombre(), this.getDispMedia(),this.gettMedioArreglo(),this.getCantModInst(),this.getMantProgramado(), null, this.getCantModInst().getValor(instanteActual),datosFotovoltaicoCorrida.getCantModIni(), actual.getInstanteInicialPPaso(), actual.getInstanteFinalPPaso());
		int cantDispIni = Math.min(datosFotovoltaicoCorrida.getCantModIni(), datosFotovoltaicoCorrida.getCantModInst().getValor(instanteActual));		
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
		ProcesoEstocastico proc = actual.dameProcesoEstocastico(datosFotovoltaicoCorrida.getFactor().getProcSimulacion());
		ProcesoEstocastico procOptim = actual.dameProcesoEstocastico(datosFotovoltaicoCorrida.getFactor().getProcOptimizacion());
		
		nombreVA = datosFotovoltaicoCorrida.getFactor().getNombre();

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

	public FotovoltaicoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(FotovoltaicoCompDesp compD) {
		this.compD = compD;
	}

	public FotovoltaicoComp getCompG() {
		return compG;
	}

	public void setCompG(FotovoltaicoComp compG) {
		this.compG = compG;
	}

	public FotovoltaicoCompSim getCompS() {
		return compS;
	}

	public void setCompS(FotovoltaicoCompSim compS) {
		this.compS = compS;
	}
	

	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		GeneradorFotovoltaico.atributosDetallados = atributosDetallados;
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
			energiaMWhp = potencia[p]*this.getDuracionPostes(p)/utilitarios.Constantes.SEGUNDOSXHORA;
			energiaMWh += energiaMWhp;
			valorEnergAlMarginal += energiaMWhp*costosMarginales[p];  // valor de la energóa en USD						
		}
		
		double costoVarOyMPaso = this.compS.calculaCostoPaso(salidaUltimaIter); // Se agrega costo O&M
		int cantModDisp = this.getCantModDisp().getValor().intValue();
		double gradGestion = Math.max((valorEnergAlMarginal - costoVarOyMPaso)/(cantModDisp*this.getPotenciaMaxima().getValor(instante)),0);

		double costoPaso = this.compS.calculaCostoPaso(salidaUltimaIter);
		DatosFotovoltaicoSP fotov = new DatosFotovoltaicoSP(this.getNombre(), this.getBarra().getNombre(),
				potencia, this.getCantModDisp().getValor().intValue(), this.getPotenciaMaxima().getValor(instante), costoPaso, gradGestion);
		String nombarra = this.getBarra().getNombre();
		for (DatosBarraSP dbsp: resultadoPaso.getRed().getBarras()) {
			if (nombarra.equalsIgnoreCase(dbsp.getNombre()) || this.getBarra().getRedAsociada().getCompD().isUninodal()){
				dbsp.agregarFotovoltaico(fotov);
				break;
			}
		}
	
	}
	
	
		
	

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();
		ret.add(this.getCantModDisp().getPe());
		ProcesoEstocastico pfactorOpt = this.devuelveProceso(nombreVA, Constantes.FASE_OPT);
		ret.add(pfactorOpt);
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
				VariableAleatoria vaFactor = peSim.devuelveVADeNombre(nombreAux);			
				factor = vaFactor;
				
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
		FotovoltaicoCompDespPE compDespPE = new FotovoltaicoCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}

	@Override
	public GeneradorCompDespPE devuelveCompDespPE() {
		return (FotovoltaicoCompDespPE) getCompDespPE();
	}

}
