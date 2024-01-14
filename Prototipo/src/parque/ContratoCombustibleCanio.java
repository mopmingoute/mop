/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombustibleCanio is part of MOP.
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

import tiempo.Evolucion;

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.ContratoCombCanioCompDesp;
import compdespacho.ContratoCombTopPasoFijoCompDesp;
import compgeneral.ContratoCombCanioComp;
import compsimulacion.ContratoCombCanioCompSim;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.ContratoCombCanioCompDespPE;
import datatypes.DatosContratoCombustibleCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraCombSP;
import datatypesSalida.DatosContratoCombSP;
import datatypesSalida.DatosSalidaPaso;
import logica.CorridaHandler;
import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;

/**
 * Clase que representa el contrato asociado a un combustible cuando es tipo ca�o
 * @author ut602614
 *
 */
public class ContratoCombustibleCanio extends ContratoCombustible{
	private Evolucion<Double> precio;
	private RedCombustible redAsociada;
	private ContratoCombCanioCompDesp compD;
	private ContratoCombCanioComp compG;
	private ContratoCombCanioCompSim compS;
	
	public ContratoCombustibleCanio(DatosContratoCombustibleCorrida datos,
			Combustible comb, BarraCombustible ba) {
		super(datos, comb, ba);
				
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		
		this.setPrecio(datos.getPrecioComb());
			
		this.redAsociada = ba.getRedAsociada();
		compD = new ContratoCombCanioCompDesp(this);
		
		compG = new ContratoCombCanioComp(this,compD,compS);
		compS = new ContratoCombCanioCompSim(this, compD, compG);	
		compG.setCompSimulacion(compS);
		
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
		
		this.setCantModInst(datos.getCantModInst());
		
		this.setDispMedia(datos.getDispMedia());
		this.settMedioArreglo(datos.gettMedioArreglo());
		this.setCostoFijo(datos.getCostoFijo());
		this.setMantProgramado(datos.getMantProgramado());
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		compD.setPrecioComb(datos.getPrecioComb().getValor(instanteActual));
		compD.setCaudalMax(datos.getCaudalMax().getValor(instanteActual));
		String nombreD = "Disp-" + this.getNombre();
		PEDisponibilidadGeometrica pedg = new PEDisponibilidadGeometrica(nombreD, this.getNombre(), this.getDispMedia(),this.gettMedioArreglo(),this.getCantModInst(), this.getMantProgramado(), null, this.getCantModInst().getValor(instanteActual), datos.getCantModIni(), actual.getInstanteInicialPPaso(), actual.getInstanteFinalPPaso());

		int cantDispIni = Math.min(datos.getCantModIni(), datos.getCantModInst().getValor(instanteActual));
		
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
		
		
		/**
		 *TODO: OJO CANTIDAD DE INNOVACIONES VARIABLES
		 */
		pedg.setCantidadInnovaciones(this.getCantModInst().getValor(instanteActual));		 
		this.getCantModDisp().setPe(pedg);
		this.setCostoFijo(datos.getCostoFijo());
		this.setMantProgramado(datos.getMantProgramado());
		
//		this.agregarEvolucionAColeccion(precio);
	}


	
	
	public ContratoCombCanioCompDesp getCompD() {
		return compD;
	}


	public void setCompD(ContratoCombCanioCompDesp compD) {
		this.compD = compD;
	}


	public ContratoCombCanioComp getCompG() {
		return compG;
	}


	public void setCompG(ContratoCombCanioComp compG) {
		this.compG = compG;
	}


	public ContratoCombCanioCompSim getCompS() {
		return compS;
	}


	public void setCompS(ContratoCombCanioCompSim compS) {
		this.compS = compS;
	}


	public void cargarDatosSimulacion() {}


	public Evolucion<Double> getPrecio() {
		return precio;
	}


	public void setPrecio(Evolucion<Double> precio) {
		this.precio = precio;
	}


	public RedCombustible getRedAsociada() {
		return redAsociada;
	}


	public void setRedAsociada(RedCombustible redAsociada) {
		this.redAsociada = redAsociada;
	}


	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {		
		double caudal = salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("caudalComb"));
		DatosContratoCombSP dcc = new DatosContratoCombSP(this.getNombre(),this.getCombustible().getNombre(),
				this.getPrecio().getValor(instante),this.getCaudalMaximo().getValor(instante),caudal, this.getCantModDisp().getValor().intValue(), calculaCostoPaso(salidaUltimaIter));		
		String nombarra = this.getBarra().getNombre(); 		
		for (DatosBarraCombSP dbsp: resultadoPaso.getRedComb(this.getRedAsociada().getNombre()).getBarras()) {
			if (nombarra.equalsIgnoreCase(dbsp.getNombre()) || this.getBarra().getRedAsociada().getCompD().isUninodal()){
				dbsp.agregarContrato(dcc);
				break;
			}
		}		
		
	}
	
	
	public String getNCaudalComb() {   // escribirlo abajo y que sobreescriba esto
			ContratoCombCanioCompDesp cccc = (ContratoCombCanioCompDesp) this.getCompDesp();
			return cccc.getNCaudalComb();
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
	public double costoMedio(long instante) {
		// En este caso el costo medio es el precio
		return precio.getValor(instante);
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
		ContratoCombCanioCompDespPE compDespPE = new ContratoCombCanioCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}
}
