/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoEnergia is part of MOP.
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
import java.util.GregorianCalendar;
import java.util.Hashtable;

import compdespacho.ContratoEnergiaCompDesp;
import compdespacho.ImpactoCompDesp;
import compgeneral.ContratoEnergiaComp;
import compgeneral.ImpactoComp;
import compsimulacion.ContratoEnergiaCompSim;
import compsimulacion.ImpactoCompSim;
import cp_compdespProgEst.CicloCombCompDespPE;
import datatypes.DatosContratoEnergiaCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosContratoEnergiaSP;
import datatypesSalida.DatosSalidaPaso;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.Evolucion;
import tiempo.LineaTiempo;
import utilitarios.Utilitarios;

public class ContratoEnergia extends Participante {
	
	private ArrayList<Participante> involucrados; /* Lista de participantes que contribuyen al suministro del contrato */
	private Evolucion<Double> precioBase;  // En USD/MWh
	private Evolucion<Double> energiaBase;  // En MWh, se lee en GWh y se convierte a MWH para los c�lculos.
	private GregorianCalendar fechaInicial; // Fecha y hora incial del contrato.
	private GregorianCalendar fechaFinal; 
	private long instFechaInicialContrato;   // instante en segundos de la fecha inicial.
	private long instFechaFinalContrato;  // idem fecha final sumando cantAnios.
	private int cantAnios; // cantidad de a�os a partir de la fecha inicial
	private double energiaInicial; // energ�a acumulada en el per�odo anual vigente al inicio de la corrida
	private String tipoContratoEnergia;    
	/**
	 * Por ahora el �nico es LIM_ENERGIA_ANUAL = "contratoLimEnergia"
	 * El precio del contrato es el precio base mientras la energ�a anual se menor que
	 * la energiaBase y cuando se excede esa cantidad es el costo marginal con cotas
	 * inferior y superior.
	 * Los per�odos anuales comienzan en la fechaInicial y se repiten por cantAnios
	 * 
	 */
	private Evolucion<Double> cotaInf;  // En USD/MWh
	private Evolucion<Double> cotaSup;  // En USD/MWh
	private static ArrayList<String> atributosDetallados;
	
	/**
	 * En la simulaci�n, a�o en el que comienza el per�odo anual corriente. Por ejemplo, si se est�
	 * en la semana 5 de 2025 y el contrato empez� el 1 de octubre de 2020, anioCorriente es
	 * 2024.
	 */
	private int anioCorriente; 
	
	/**
	 * En la simulaci�n, cantidad de energ�a ya consumida en el per�odo anual corriente
	 * expresada en GWh
	 */
	private double enerAcumAnioCorrienteGWh;  
	
	private ContratoEnergiaCompDesp compD;
	private ContratoEnergiaCompSim compS;
	private ContratoEnergiaComp compG;
	

	public ContratoEnergia(DatosContratoEnergiaCorrida dat, ArrayList<Participante> part){
		
		super();
		this.setNombre(dat.getNombre());
		this.setEnergiaBase(dat.getEnergiaBase());  // En GWh
		this.setPrecioBase(dat.getPrecioBase());
		this.setEnergiaInicial(dat.getEnergiaInicial());
		this.setCotaInf(dat.getCotaInf());
		this.setCotaSup(dat.getCotaSup());
		this.setFechaInicial(Utilitarios.stringToGregorianCalendar(dat.getFechaInicial(),"dd MM yyyy HH:mm:ss")); 		
		this.setCantAnios(dat.getCantAnios());
		this.setTipoContratoEnergia(dat.getTipo());
		this.setInvolucrados(part);
		

		compD = new ContratoEnergiaCompDesp(this);
		compG = new ContratoEnergiaComp(this, compD, compS);
		compS = new ContratoEnergiaCompSim(this, compD, compG);
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
		
		
		
		
		
		
		
		
	}
	

//	<tipoContrato>contratoLimEnergiaAnual</tipoContrato>
//	<salidaDetallada>true</salidaDetallada>	

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
		this.getCompSimulacion().inicializarParaEscenario();
		/**
		 * Se calculan algunas constantes del participante que
		 * en realidad deber�an inicializarse al comienzo de la simulaci�n
		 */
		LineaTiempo lt = this.getSimPaso().getCorrida().getLineaTiempo();
		fechaFinal = (GregorianCalendar)fechaInicial.clone();
		fechaFinal.add(GregorianCalendar.YEAR, cantAnios);
		instFechaInicialContrato = lt.dameInstante(fechaInicial);
		instFechaFinalContrato = lt.dameInstante(fechaFinal);
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {
		ArrayList<Participante> pars = this.getInvolucrados(); 		
		if(tipoContratoEnergia.equalsIgnoreCase(utilitarios.Constantes.LIM_ENERGIA_ANUAL)){				
			long instIniPaso = this.getSimPaso().getPasoActual().getInstanteInicial();
			DatosContratoEnergiaSP datCE;
			if(instIniPaso < this.getInstFechaFinalContrato() && instIniPaso >= this.getInstFechaInicialContrato()){			
				double enerPasoGWh = ContratoEnergiaCompSim.calculaEnergiaPasoMWh(pars, salidaUltimaIter)/utilitarios.Constantes.MWHXGWH;
				double valor = this.compS.calculaValorPasoContratoUSD(salidaUltimaIter);
				/**
				 * enerAcumAnioCorriente no se actualiz� a�n porque esto se hace en actualizarParaProximoPaso
				 * que se invoca despu�s en el SimuladorPaso
				 */
				double enerAcumGWh = enerAcumAnioCorrienteGWh + enerPasoGWh;  // es la energ�a acumulada del a�o al fin del paso corriente
				double[] potencias = ContratoEnergiaCompSim.devuelvePotenciasPasoMW(pars, salidaUltimaIter);
				datCE = new DatosContratoEnergiaSP(this.getNombre(), enerPasoGWh, potencias, enerAcumGWh, valor);							
			}else{
				double[] potencias = new double[this.getCantPostes()];
				datCE = new DatosContratoEnergiaSP(this.getNombre(), 0.0, potencias, 0.0, 0.0);
			}
			resultadoPaso.agregarContratoEnergia(datCE);
		}else{
			System.out.println("Se pidi� un tipo de contrato no programado: " + tipoContratoEnergia);
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
			
		}
	
	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// Deliberadamente en blanco
		return null;
	}

	@Override
	public void aportarImpacto(Impacto i, DatosObjetivo costo) {
		// Deliberadamente en blanco
		
	}

	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// Deliberadamente en blanco
		return null;
	}

	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// Deliberadamente en blanco
		return null;
	}


	public ArrayList<Participante> getInvolucrados() {
		return involucrados;
	}


	public void setInvolucrados(ArrayList<Participante> involucrados) {
		this.involucrados = involucrados;
	}


	public Evolucion<Double> getPrecioBase() {
		return precioBase;
	}


	public void setPrecioBase(Evolucion<Double> precioBase) {
		this.precioBase = precioBase;
	}


	public Evolucion<Double> getEnergiaBase() {
		return energiaBase;
	}


	public void setEnergiaBase(Evolucion<Double> energiaBase) {
		this.energiaBase = energiaBase;
	}




	public GregorianCalendar getFechaInicial() {
		return fechaInicial;
	}


	public void setFechaInicial(GregorianCalendar fechaInicial) {
		this.fechaInicial = fechaInicial;
	}


	
	
	public GregorianCalendar getFechaFinal() {
		return fechaFinal;
	}


	public void setFechaFinal(GregorianCalendar fechaFinal) {
		this.fechaFinal = fechaFinal;
	}




	public long getInstFechaInicialContrato() {
		return instFechaInicialContrato;
	}


	public void setInstFechaInicialContrato(long instFechaInicialContrato) {
		this.instFechaInicialContrato = instFechaInicialContrato;
	}


	public long getInstFechaFinalContrato() {
		return instFechaFinalContrato;
	}


	public void setInstFechaFinalContrato(long instFechaFinalContrato) {
		this.instFechaFinalContrato = instFechaFinalContrato;
	}


	public int getAnioCorriente() {
		return anioCorriente;
	}


	public void setAnioCorriente(int anioCorriente) {
		this.anioCorriente = anioCorriente;
	}


	public int getCantAnios() {
		return cantAnios;
	}


	public void setCantAnios(int cantAnios) {
		this.cantAnios = cantAnios;
	}


	public double getEnergiaInicial() {
		return energiaInicial;
	}


	public void setEnergiaInicial(double energiaInicial) {
		this.energiaInicial = energiaInicial;
	}


	public String getTipoContratoEnergia() {
		return tipoContratoEnergia;
	}


	public void setTipoContratoEnergia(String tipoContratoEnergia) {
		this.tipoContratoEnergia = tipoContratoEnergia;
	}


	public Evolucion<Double> getCotaInf() {
		return cotaInf;
	}


	public void setCotaInf(Evolucion<Double> cotaInf) {
		this.cotaInf = cotaInf;
	}


	public Evolucion<Double> getCotaSup() {
		return cotaSup;
	}


	public void setCotaSup(Evolucion<Double> cotaSup) {
		this.cotaSup = cotaSup;
	}



	public double getEnerAcumAnioCorrienteGWh() {
		return enerAcumAnioCorrienteGWh;
	}


	public void setEnerAcumAnioCorrienteGWh(double enerAcumAnioCorrienteGWh) {
		this.enerAcumAnioCorrienteGWh = enerAcumAnioCorrienteGWh;
	}


	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}


	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		ContratoEnergia.atributosDetallados = atributosDetallados;
	}


	public ContratoEnergiaCompDesp getCompD() {
		return compD;
	}


	public void setCompD(ContratoEnergiaCompDesp compD) {
		this.compD = compD;
	}


	public ContratoEnergiaCompSim getCompS() {
		return compS;
	}


	public void setCompS(ContratoEnergiaCompSim compS) {
		this.compS = compS;
	}


	public ContratoEnergiaComp getCompG() {
		return compG;
	}


	public void setCompG(ContratoEnergiaComp compG) {
		this.compG = compG;
	}



	@Override
	public void crearCompDespPE() {
		// Deliberadamente en blanco
		
	}
	
	
	
	
	

}
