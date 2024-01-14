/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Barra is part of MOP.
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
import compgeneral.BarraComp;
import compsimulacion.BarraCompSim;
import cp_compdespProgEst.BarraCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import datatypes.DatosBarraCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosSalidaPaso;
import logica.CorridaHandler;
import procesosEstocasticos.ProcesoEstocastico;
import utilitarios.Constantes;


/**
 * Clase que representa la barra elóctrica
 * @author ut602614
 *
 */
public class Barra extends Participante{

	private RedElectrica redAsociada;
	private ArrayList<Generador> generadores;
	private ArrayList<Demanda> demandas;

	private ArrayList<ImpoExpo> impoExpos;
	private ArrayList<Rama> ramas; 	/**Colección de ramas asociadas a la barra*/
	
	
	private boolean unica;
	private boolean flotante;
	
	BarraCompDesp compD;
	BarraComp compG;
	BarraCompSim compS;	
	


	public Barra(DatosBarraCorrida datos) {
		this.generadores = new ArrayList<Generador>(); 
		this.demandas = new ArrayList<Demanda>();
		this.ramas = new ArrayList<Rama>();

		this.impoExpos = new ArrayList<ImpoExpo>();
		this.setNombre(datos.getNombre());	
		this.unica = false;
		this.setFlotante(false);
		
		compD = new BarraCompDesp(this);
		compG= new BarraComp();
		compS = new BarraCompSim(this, compD);	
		
		compS.setCompgeneral(compG);
		compS.setCompdespacho(compD);
		compG.setCompSimulacion(compS);
		compG.setCompDespacho(compD);
		this.setCompDesp(compD);
		this.setCompGeneral(compG);
		this.setCompSimulacion(compS);
		compS.setParticipante(this);
		compG.setParticipante(this);
	
		
		compD.setBarra(this);
		compD.setFlotante(datos.isFlotante());
		
	}
	
	
	public Barra() {
		
		this.generadores = new ArrayList<Generador>();
		this.demandas = new ArrayList<Demanda>();
		this.ramas = new ArrayList<Rama>();
			
		this.unica = false;
		
		this.setFlotante(false);
		this.setCompDesp(new BarraCompDesp(this));
	}

	

	public ArrayList<Rama> getRamas() {
		return ramas;
	}

	public void setRamas(ArrayList<Rama> ramas) {
		this.ramas = ramas;
	}


	public ArrayList<Generador> getGeneradores() {
		return generadores;
	}


	public void setGeneradores(ArrayList<Generador> generadores) {
		this.generadores = generadores;
	}


	public ArrayList<Demanda> getDemandas() {
		return demandas;
	}


	public void setDemandas(ArrayList<Demanda> demandas) {
		this.demandas = demandas;
	}

	
	

	public ArrayList<ImpoExpo> getImpoExpos() {
		return impoExpos;
	}


	public void setImpoExpos(ArrayList<ImpoExpo> impoExpos) {
		this.impoExpos = impoExpos;
	}


	public boolean isUnica() {
		return unica;
	}


	public void setUnica(boolean unica) {
		this.unica = unica;
	}


	public RedElectrica getRedAsociada() {
		return redAsociada;
	}


	public void setRedAsociada(RedElectrica redAsociada) {
		this.redAsociada = redAsociada;
	}

	/**
	 * Devuelve la coleccion de ramas que conectan la barra this con la otra en cualquiera de los dos sentidos	 
	 */
	public ArrayList<Rama> dameRamasConectadasBarra(Barra laotra) {
		ArrayList<Rama> resultado = new ArrayList<Rama>();
		for (Rama r: this.ramas) {
			if ((r.getBarra1() == this && r.getBarra2() == laotra) || (r.getBarra2() == this && r.getBarra1() == laotra)) {
				resultado.add(r);
			}
		}
		return resultado;
	}



	public boolean isFlotante() {
		return flotante;
	}


	public void setFlotante(boolean flotante) {
		this.flotante = flotante;
	}



	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}
	
	
	public double[] dameCostosMarginales(DatosSalidaProblemaLineal salidaUltimaIter) {
		double [] costosMarginales = new double[this.getCantPostes()];
		for (int p = 0; p < this.getCantPostes(); p++) {
			costosMarginales[p] = salidaUltimaIter.getDuales().get(this.getCompDesp().generarNombre("demandaPoste", Integer.toString(p)))*Constantes.SEGUNDOSXHORA/this.getDuracionPostes(p);
		}
		return costosMarginales;
	}
	

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso,
			DatosSalidaProblemaLineal salidaUltimaIter, String proceso, long instante) {
		
		DatosBarraSP db = new DatosBarraSP();
		double [] costosMarginales = dameCostosMarginales(salidaUltimaIter);
		
		
		
		db.setCostoMarginal(costosMarginales);
		db.setNombre(this.getNombre());
		resultadoPaso.getRed().getBarras().add(db);
		
		for (Demanda d: this.demandas) {
			d.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
		}
			
		for (ImpoExpo p: this.impoExpos) {
			p.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
		}
		
		for (Generador g: this.generadores) {
			g.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
		}
		
		
		for (Rama r: this.ramas) {
			r.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, instante);
		}				
		
	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		return null;
	}

	@Override
	public void asignaVAOptim() {
		// Deliberadamente en blanco
		
	}

	@Override
	public void asignaVASimul() {
		// // Deliberadamente en blanco
		
	}


	public boolean isRedUninodal(long instante) {
		return this.getRedAsociada().isRedUninodal(instante);
	}


	public Barra dameBarraUnica() {
		// TODO Auto-generated method stub
		return this.getRedAsociada().getBarraUnica();
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
		BarraCompDespPE compDespPE = new BarraCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		compDespPE.setUninodal(isRedUninodal(devuelveCorrida().getInstanteInicial()));		
	}
	
}
