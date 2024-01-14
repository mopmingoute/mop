/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedCombustible is part of MOP.
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
import java.util.Iterator;
import java.util.Set;

import compdespacho.BarraCombustibleCompDesp;
import compdespacho.RedCombCompDesp;
import compgeneral.BarraCombustibleComp;
import compgeneral.RedCombComp;
import compsimulacion.BarraCombustibleCompSim;
import compsimulacion.RedCombCompSim;
import cp_compdespProgEst.BarraCombCompDespPE;
import cp_compdespProgEst.BarraCompDespPE;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.RedCombCompDespPE;
import cp_compdespProgEst.RedCompDespPE;
import utilitarios.Constantes;
import logica.CorridaHandler;
import procesosEstocasticos.ProcesoEstocastico;
import datatypes.DatosBarraCombCorrida;
import datatypes.DatosContratoCombustibleCorrida;
import datatypes.DatosDuctoCombCorrida;
import datatypes.DatosRedCombustibleCorrida;
import datatypes.DatosTanqueCombustibleCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosCombustibleSP;
import datatypesSalida.DatosRedCombustibleSP;
import datatypesSalida.DatosSalidaPaso;


/**
 * Clase que representa la red de combustible
 * @author ut602614
 *
 */
public class RedCombustible extends Participante{
																				
																				/**Nombre asociado a la red de combustible*/
	private Combustible combustible;																	/**Combustible asociado a la red*/
	private Hashtable<String,BarraCombustible> barras;		/**Colección de barras de la red*/
	private Hashtable<String,DuctoCombustible> ductos;		/**Colección de ductos de la red*/
	private Hashtable<String,TanqueCombustible> tanques;	/**Colección de tanques asociados a la red*/

	private Hashtable<String,ContratoCombustible> contratos;
	private BarraCombustible barraunica; 		/**Representa la barra ónica, se crea siempre con todo menos los ductos*/
	
	private RedCombCompDesp compD;
	
	private RedCombCompSim compS;
	private RedCombComp compG;
	private boolean uninodal;
	
	/**
	 * Subconjuntos de los postes para los que existen variables de la red y para los que se hace el 
	 * balance de barra
	 */
	private ArrayList<ArrayList<Integer>> subConjuntosPostes; 
	
	
	/**Constructor de la red de combustible a partir de sus datos y el combustible asociado*/
	public RedCombustible(DatosRedCombustibleCorrida red, Combustible comb) {
		CorridaHandler ch = CorridaHandler.getInstance();
		barras = new Hashtable<String,BarraCombustible>();		
		ductos = new Hashtable<String,DuctoCombustible>();		
		tanques = new Hashtable<String,TanqueCombustible>();
		contratos = new Hashtable<String, ContratoCombustible>();
		this.setNombre(red.getNombre());
		this.setCombustible(comb);
		
		compD = new RedCombCompDesp(this);
		compG = new RedCombComp(this,compD,compS);
		compS = new RedCombCompSim(this, compD, compG);	
		
		compG.setCompS(compS);
		
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
		
		compG.setEvolucionComportamientos(red.getValoresComportamiento());
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
			
		uninodal =compG.getEvolucionComportamientos().get(Constantes.COMPRED).getValor(instanteActual).equalsIgnoreCase(Constantes.UNINODAL);
		
		for (DatosBarraCombCorrida dB:  red.getBarras()) {
			BarraCombustible nueva = new BarraCombustible(dB, comb);
			
			nueva.setRedAsociada(this);
			barras.put(dB.getNombre(),nueva);			
			if (!uninodal) ch.agregarParticipante(barras.get(dB.getNombre()));
		}
		
		for (DatosDuctoCombCorrida dD:  red.getRamas()) {
			BarraCombustible barra1 = this.barras.get(dD.getBarra1());
			BarraCombustible barra2 = this.barras.get(dD.getBarra2());
			DuctoCombustible nuevo =new DuctoCombustible(dD, comb, barra1, barra2);
			ductos.put(dD.getNombre(), nuevo);
			if (!uninodal) ch.agregarParticipante(ductos.get(dD.getNombre()));
			barra1.getDuctosSalientes().add(nuevo);
			barra2.getDuctosEntrantes().add(nuevo);
		}
		
		for (DatosTanqueCombustibleCorrida dT: red.getTanques()) {
			BarraCombustible barra = this.barras.get(dT.getBarra());
			tanques.put(dT.getNombre(), new TanqueCombustible(dT, comb, barra));
			barra.getTanques().add(tanques.get(dT.getNombre()));
			ch.agregarParticipante(tanques.get(dT.getNombre()));
		}		
		
		for (DatosContratoCombustibleCorrida dccc: red.getContratos()) {
			BarraCombustible barra = this.barras.get(dccc.getBarra());
			contratos.put(dccc.getNombre(), new ContratoCombustibleCanio(dccc, comb, barra));
			barra.getContratos().add(contratos.get(dccc.getNombre()));
			ch.agregarParticipante(contratos.get(dccc.getNombre()));
		}
			
	}
	
	public void construirBarraUnica() {
		CorridaHandler ch = CorridaHandler.getInstance();
		DatosBarraCombCorrida dc = new DatosBarraCombCorrida(Constantes.NOMBREBARRAUNICA);
		
		barraunica = new BarraCombustible(dc,this.getCombustible());
		barraunica.setRedAsociada(this);
		
		
		Set<String> claves = this.barras.keySet();
		
		Iterator<String> it = claves.iterator();
		
		while (it.hasNext()) {
			BarraCombustible b = this.barras.get(it.next());
			barraunica.getContratos().addAll(b.getContratos());
			barraunica.setComb(this.combustible);
			barraunica.getConvertidoresEntrantes().addAll(b.getConvertidoresEntrantes());
			barraunica.getConvertidoresSalientes().addAll(b.getConvertidoresSalientes());
			barraunica.getTanques().addAll(b.getTanques());
			barraunica.getGeneradoresConectados().addAll(b.getGeneradoresConectados());		
			barraunica.getCiclosCombConectados().addAll(b.getCiclosCombConectados());
		}	
		barraunica.setCompDesp(new BarraCombustibleCompDesp(barraunica)); 
		
		ch.agregarParticipante(barraunica);		
	}
	
	
	
	public Hashtable<String,BarraCombustible> getBarras() {
		return barras;
	}
	public void setBarras(Hashtable<String,BarraCombustible> barras) {
		this.barras = barras;
	}
	public Hashtable<String, DuctoCombustible> getDuctos() {
		return ductos;
	}
	public void setDuctos(Hashtable<String, DuctoCombustible> ductos) {
		this.ductos = ductos;
	}
	public Hashtable<String,TanqueCombustible> getTanques() {
		return tanques;
	}
	public void setTanques(Hashtable<String,TanqueCombustible> tanques) {
		this.tanques = tanques;
	}

	public Combustible getCombustible() {
		return combustible;
	}
	public void setCombustible(Combustible combustible) {
		this.combustible = combustible;
	}


	public BarraCombustible getBarraunica() {
		return barraunica;
	}

	public void setBarraunica(BarraCombustible barraunica) {
		this.barraunica = barraunica;
	}

	public Hashtable<String,ContratoCombustible> getContratos() {
		return contratos;
	}

	public void setContratos(Hashtable<String,ContratoCombustible> contratos) {
		this.contratos = contratos;
	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}
	public RedCombCompDesp getCompD() {
		return compD;
	}

	public void setCompD(RedCombCompDesp compD) {
		this.compD = compD;
	}

	public RedCombCompSim getCompS() {
		return compS;
	}

	public void setCompS(RedCombCompSim compS) {
		this.compS = compS;
	}

	public RedCombComp getCompG() {
		return compG;
	}

	public void setCompG(RedCombComp compG) {
		this.compG = compG;
	}

	
	
	public boolean isUninodal() {
		return uninodal;
	}

	public void setUninodal(boolean uninodal) {
		this.uninodal = uninodal;
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,	DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {
		DatosCombustibleSP dc = new DatosCombustibleSP();
		dc.setNombre(this.getCombustible().getNombre());
		dc.setPCI(this.getCombustible().getPci());
		dc.setUnidad(this.getCombustible().getUnidad());
		
		DatosRedCombustibleSP dred = new DatosRedCombustibleSP(this.getNombre(),dc);
		dred.setNombre(this.getNombre());
		resultadoPaso.agregarRedCombustible(dred);

		Set<String> claves = barras.keySet();
		Iterator<String> it = claves.iterator();
		while (it.hasNext()) {
			barras.get(it.next()).guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);			
		}
		claves = ductos.keySet();
		it = claves.iterator();
		while (it.hasNext()) {
			ductos.get(it.next()).guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
			
		}
		
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
		RedCombCompDespPE compDespPE = new RedCombCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		compDespPE.setRed(this);
		if(isUninodal()) {
			compDespPE.setUninodal(true);
			BarraCombCompDespPE bcd = new BarraCombCompDespPE();
			barraunica.setCompDespPE(bcd);
			bcd.setUninodal(true);
			bcd.setNomPar("barraunica");
			bcd.setParticipante(barraunica);
		}
	}

}
