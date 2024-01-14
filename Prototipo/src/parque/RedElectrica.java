/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedElectrica is part of MOP.
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

import compdespacho.BarraCompDesp;
import compdespacho.RedCompDesp;
import compgeneral.RedComp;
import compsimulacion.RedCompSim;
import cp_compdespProgEst.BarraCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import cp_compdespProgEst.EolicoCompDespPE;
import cp_compdespProgEst.RedCompDespPE;
import utilitarios.Constantes;
import logica.CorridaHandler;
import procesosEstocasticos.ProcesoEstocastico;
import datatypes.DatosBarraCorrida;
import datatypes.DatosRamaCorrida;
import datatypes.DatosRedElectricaCorrida;
import datatypes.Pair;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosRedSP;
import datatypesSalida.DatosSalidaPaso;


/**
 * Clase que representa la red elóctrica
 * @author ut602614
 *
 */
public class RedElectrica extends Participante{
	
	private Hashtable<String, Barra> barras;			/**Colección de barras asociadas a la red elóctrica*/
	private Hashtable<String, Rama> ramas;				/**Colección de ramas asociadas a la red elóctrica*/

	private RedComp compG;
	private RedCompSim compS;
	private RedCompDesp compD;
	
	private Barra barraUnica;
	private ArrayList<Barra> barrasActivas;
	private ArrayList<Generador> generadoresBarraUnica;	
	private ArrayList<ImpoExpo> comercioEnerBarraUnica;

	private ArrayList<Demanda> demandasBarraUnica;
	

	private Hashtable<Pair<String,String>, Double> BPrima;
	
	public RedElectrica(DatosRedElectricaCorrida redelectrica) {
		this.setNombre(Constantes.RED_ELECTRICA);
		CorridaHandler ch = CorridaHandler.getInstance();
		setGeneradoresBarraUnica(new ArrayList<Generador>());
		setDemandasBarraUnica(new ArrayList<Demanda>());
		generadoresBarraUnica = new ArrayList<Generador>();
		comercioEnerBarraUnica = new ArrayList<ImpoExpo>();
		demandasBarraUnica = new ArrayList<Demanda>();
	

		ArrayList<String> barrasU = redelectrica.getListaBarrasUtilizadas();
		ArrayList<String> ramasU = redelectrica.getListaRamasUtilizadas();
				
		
		compD = new RedCompDesp(this);
		compG = new RedComp(this,compD,compS);
		compS = new RedCompSim(this, compD, compG);	
		
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
		
		compG.setEvolucionComportamientos(redelectrica.getValoresComportamiento());
		
		
		
		this.barras = new Hashtable<String, Barra>();
		this.barrasActivas = new ArrayList<Barra>();
		this.ramas = new Hashtable<String, Rama>();

		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		
		this.compD.setUninodal(redelectrica.getValoresComportamiento().get(Constantes.COMPRED).getValor(instanteActual).equalsIgnoreCase(Constantes.UNINODAL));
		
		if (barrasU != null)
			for (String clave: barrasU) {		
		    	DatosBarraCorrida datos = redelectrica.getBarras().get(clave);
		    	Barra nueva = new Barra(datos);
		    	nueva.setRedAsociada(this);
		    	this.barras.put(clave, nueva);		    	
		    	if (!this.compD.isUninodal()) {
		    		this.barrasActivas.add(nueva);
		    		ch.agregarParticipante(nueva);
		    		ch.agregarParticipanteDirecto(nueva);
		    	}
		    	
		    }
		

		
	    this.BPrima = new Hashtable<Pair<String,String>,Double>();		
	    		
	    if (ramasU != null)
		    for (String clave: ramasU) {
		    	DatosRamaCorrida datos = redelectrica.getRamas().get(clave);
		    	Barra barra1 = barras.get(datos.getBarra1());
		    	Barra barra2 = barras.get(datos.getBarra2());
		    	Rama nueva = new Rama(datos, barra1, barra2);
		    	this.ramas.put(clave, nueva);
		    	if (!this.compD.isUninodal()) {		    		
		    		ch.agregarParticipante(nueva);
		    		ch.agregarParticipanteDirecto(nueva);
		    	}
		    	barra1.getRamas().add(nueva);
		    	barra2.getRamas().add(nueva);	
		    }
		    
	    this.cargarBPrima();
	    
	    this.barras.get(redelectrica.getFlotante()).setFlotante(true);
		
	}
	
	public void construirBarraUnica() {
		CorridaHandler ch = CorridaHandler.getInstance();
		this.barraUnica = new Barra();
		barraUnica.setNombre(Constantes.NOMBREBARRAUNICA);
		barraUnica.setDemandas(demandasBarraUnica);
		barraUnica.setGeneradores(generadoresBarraUnica);

		barraUnica.setImpoExpos(comercioEnerBarraUnica);
		barraUnica.setUnica(true);
		barraUnica.setCompDesp(new BarraCompDesp(barraUnica));
		barraUnica.setRedAsociada(this);
		this.barras.put(barraUnica.getNombre(), this.barraUnica);
		if (this.compD.isUninodal()) {
			ch.agregarParticipante(this.barraUnica);
			this.barrasActivas.add(this.barraUnica);
		}
	}
	
	private void cargarBPrima(){
//		Collection<Barra> barrasCol = this.barras.values();
//		
//		Iterator<Barra> itr = barrasCol.iterator();
//		Double Bequivalente = 0.0;
//		while (itr.hasNext()) {
//			Barra actual = itr.next();
//			ArrayList<Barra> adyacentesDC = this.listaBarrasConectadasDC(actual);
//			for (Barra vecina: adyacentesDC) {
//				ArrayList<Rama> ramasUnion = vecina.dameRamasConectadasBarra(actual);						
//				for (Rama r: ramasUnion) {
//					Bequivalente += 1.0/r.getX();
//				}						
//				Pair<String, String> nuevo = new Pair<String,String>(actual.getNombre(),vecina.getNombre()); 
//				if (!this.BPrima.containsKey(nuevo.espejo())) {
//					this.BPrima.put(nuevo, Bequivalente);
//				}
//			}
//			// Llena la diagonal de la matriz
//			Bequivalente = 0.0;
//			for (Rama r: actual.getRamas()) {
//				Bequivalente -= 1.0/r.getX();
//			}
//			this.BPrima.put(new Pair<String,String>(actual.getNombre(), actual.getNombre()), Bequivalente);
//		}
//		
	}
	
	public Hashtable<String, Barra> getBarras() {
		return barras;
	}

	public void setBarras(Hashtable<String, Barra> barras) {
		this.barras = barras;
	}

	public Hashtable<String, Rama> getRamas() {
		return ramas;
	}

	public void setRamas(Hashtable<String, Rama> ramas) {
		this.ramas = ramas;
	}

	public Barra getBarra(String barra) {
		return barras.get(barra);
	}

	public Barra getBarraUnica() {
		return barraUnica;
	}

	public void setBarraUnica(Barra barraUnica) {
		this.barraUnica = barraUnica;
	}

	public ArrayList<Barra> listaBarrasConectadasDC(Barra barra) {
		ArrayList<Barra> lista = new ArrayList<Barra>();
		for (Rama r: barra.getRamas()) {
			if (r.getComportamiento().isDC()) {
				lista.add(r.dameLaOtraBarra(barra));
			}
		}
		
		return lista;
	}

	public Hashtable<Pair<String,String>, Double> getBPrima() {
		return BPrima;
	}

	public void setBPrima(Hashtable<Pair<String,String>, Double> bPrima) {
		BPrima = bPrima;
	}

	public Double getCoeficienteBPrima(Barra barra, Barra b) {
		Pair<String, String> clave = new Pair<String, String>(barra.getNombre(), b.getNombre());
		Double coeficiente =this.BPrima.get(clave);
		return (coeficiente==null)?this.BPrima.get(clave.espejo()):coeficiente;		
	}

	public ArrayList<Generador> getGeneradoresBarraUnica() {
		return generadoresBarraUnica;
	}

	public void setGeneradoresBarraUnica(ArrayList<Generador> generadoresBarraUnica) {
		this.generadoresBarraUnica = generadoresBarraUnica;
	}

	public ArrayList<Demanda> getDemandasBarraUnica() {
		return demandasBarraUnica;
	}

	public void setDemandasBarraUnica(ArrayList<Demanda> demandasBarraUnica) {
		this.demandasBarraUnica = demandasBarraUnica;
	}



	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {
		DatosRedSP dred = new DatosRedSP();
		resultadoPaso.setRed(dred);
	
		for (Barra b: barrasActivas) {
		
			b.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
			
		}
	
		Set<String> claves;
		Iterator<String> it;
		claves = ramas.keySet();
		it = claves.iterator();
		while (it.hasNext()) {
			ramas.get(it.next()).guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
			
		}
	}

	public RedComp getCompG() {
		return compG;
	}

	public void setCompG(RedComp compG) {
		this.compG = compG;
	}

	public RedCompSim getCompS() {
		return compS;
	}

	public void setCompS(RedCompSim compS) {
		this.compS = compS;
	}

	public RedCompDesp getCompD() {
		return compD;
	}

	public void setCompD(RedCompDesp compD) {
		this.compD = compD;
	}

	public ArrayList<Barra> getBarrasActivas() {
		return barrasActivas;
	}

	public void setBarrasActivas(ArrayList<Barra> barrasActivas) {
		this.barrasActivas = barrasActivas;
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
	public ArrayList<ImpoExpo> getComercioEnerBarraUnica() {
		return comercioEnerBarraUnica;
	}

	public void setComercioEnerBarraUnica(ArrayList<ImpoExpo> comercioEnerBarraUnica) {
		this.comercioEnerBarraUnica = comercioEnerBarraUnica;
	}

	public boolean isRedUninodal(long instante) {		
		return this.getCompGeneral().getFotoComportamientos(instante).get(Constantes.COMPRED).equalsIgnoreCase(Constantes.UNINODAL);
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
		RedCompDespPE compDespPE = new RedCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		compDespPE.setRed(this);
		if(isRedUninodal(devuelveCorrida().getInstanteInicial())) {
			compDespPE.setUninodal(true);
			BarraCompDespPE bcd = new BarraCompDespPE();
			barraUnica.setCompDespPE(bcd);
			bcd.setUninodal(true);
			bcd.setNomPar("barraUnica");
			bcd.setParticipante(barraUnica);
		}
	}



	
	
	
}
