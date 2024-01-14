/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Acumulador is part of MOP.
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

import compdespacho.AcumuladorCompDesp;
import compdespacho.BarraCompDesp;
import compgeneral.AcumuladorComp;
import compsimulacion.AcumuladorCompSim;

import control.VariableControlDE;
import cp_compdespProgEst.AcumuladorCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import cp_compdespProgEst.EolicoCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import datatypes.DatosAcumuladorCorrida;
import datatypes.DatosVariableEstado;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosAcumuladorSP;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosFotovoltaicoSP;
import datatypesSalida.DatosSalidaPaso;
import datatypesTiempo.DatosLineaTiempo;
import estado.VariableEstado;
import estado.VariableEstadoPar;
import futuro.AFIncrementos;
// import interfacesParticipantes.Actualizable;
import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;
import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import tiempo.Evolucion;
import utilitarios.Constantes;

// public class Acumulador extends Generador implements AportanteEstado, Actualizable {
public class Acumulador extends Generador implements AportanteEstado {
	private static ArrayList<String> atributosDetallados;
	private AcumuladorCompDesp compD;
	private AcumuladorCompSim compS;
	private AcumuladorComp compG;
	

	private String nombre;
	/** Nombre del acumulador */
	private Barra barra;
	/** Barra asociada al acumulador */
	private Evolucion<Integer> cantModInst;
	/** Cantidad de módulos del acumulador */
	
	private String propietario;

	private Evolucion<Double> potMin;
	/** Potencia mónima asociada al acumulador (inyección) */

	private Evolucion<Double> potAlmMin;
	/** Potencia mónima asociada al acumulador (almacenamiento) */
	private Evolucion<Double> potAlmMax;
	/** Potencia móxima asociada al acumulador (almacenamiento) */

	private Evolucion<Double> energAlmacMax;
	private Evolucion<Double> energIniPoste;
	private Evolucion<Double> factorUso;

	private Evolucion<Double> rendIny;
	private Evolucion<Double> rendAlmac;

	private Integer cantModIni;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	private boolean salDetallada;
	private Evolucion<Boolean> hayPotObligatoria; 
	private Evolucion<Double> costoFallaPotOblig;
	private double[] PotOblig;
	private Evolucion<Integer> mantProgramado;
	private Evolucion<Double> costoFijo;
	private Evolucion<Double> costoVariable;
	private VariableEstadoPar energAcumulada;
	private VariableEstadoPar energAcumuladaOpt;


	public Acumulador(DatosAcumuladorCorrida datosAcumuladorCorrida, DatosLineaTiempo lineaTiempo2) {

		this.setNombre(datosAcumuladorCorrida.getNombre());
		this.setBarra(CorridaHandler.getInstance().getBarra(datosAcumuladorCorrida.getBarra()));
		this.setCantModInst(datosAcumuladorCorrida.getCantModInst());
		this.setPropietario(datosAcumuladorCorrida.getPropietario());
		this.setDispMedia(datosAcumuladorCorrida.getDispMedia());
		this.settMedioArreglo(datosAcumuladorCorrida.gettMedioArreglo());
		this.setPotenciaMaxima(datosAcumuladorCorrida.getPotMax());
		this.setPotMin(datosAcumuladorCorrida.getPotMin());
		this.setPotAlmMin(datosAcumuladorCorrida.getPotAlmacenadaMin());
		this.setPotAlmMax(datosAcumuladorCorrida.getPotAlmacenadaMax());
		this.setEnergAlmacMax(datosAcumuladorCorrida.getEnergAlmacMax());
		this.setRendIny(datosAcumuladorCorrida.getRendIny());
		this.setRendAlmac(datosAcumuladorCorrida.getRendAlmac());
		this.setFactorUso(datosAcumuladorCorrida.getFactorUso());
		this.setMantProgramado(datosAcumuladorCorrida.getMantProgramado());
		this.setCostoFijo(datosAcumuladorCorrida.getCostoFijo());
		this.setCostoVariable(datosAcumuladorCorrida.getCostoVariable());
		this.setSalDetallada(datosAcumuladorCorrida.isSalDetallada());
		this.setEnergIniPoste(datosAcumuladorCorrida.getEnergIniPaso());
		this.setHayPotObligatoria(datosAcumuladorCorrida.getHayPotObligatoria());
		this.setCostoFallaPotOblig(datosAcumuladorCorrida.getCostoFallaPotOblig());
		this.setPotOblig(datosAcumuladorCorrida.getPotOblig());

		compD = new AcumuladorCompDesp(this);
		compG = new AcumuladorComp(this, compD, compS);
		compS = new AcumuladorCompSim(this, compD, compG);
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

		compG.setEvolucionComportamientos(datosAcumuladorCorrida.getValoresComportamientos());

		if (datosAcumuladorCorrida.getVarsEstado() != null) {
			DatosVariableEstado vol = datosAcumuladorCorrida.getVarsEstado().get("energAcumulada");
			if (vol != null) {
				this.energAcumulada = new VariableEstadoPar(
						datosAcumuladorCorrida.getVarsEstado().get("energAcumulada"), lineaTiempo2, getNombre());
				this.energAcumulada.setParticipante(this);
				this.energAcumuladaOpt = new VariableEstadoPar(
						datosAcumuladorCorrida.getVarsEstado().get("energAcumulada"), lineaTiempo2, getNombre());
				this.energAcumuladaOpt.setParticipante(this);
			}
		}
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();

		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		String nombreD = "Disp-" + this.getNombre();
		PEDisponibilidadGeometrica pedg = new PEDisponibilidadGeometrica(nombreD, this.getNombre(), this.getDispMedia(),
				this.gettMedioArreglo(), this.getCantModInst(), this.getMantProgramado(), null,
				this.getCantModInst().getValor(instanteActual), datosAcumuladorCorrida.getCantModIni(),
				actual.getInstanteInicialPPaso(), actual.getInstanteFinalPPaso());
		//pedg.setAzar(this.getSimPaso().getAzar());
		int cantDispIni = Math.min(datosAcumuladorCorrida.getCantModIni(), datosAcumuladorCorrida.getCantModInst().getValor(instanteActual));
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
	}

	@Override
	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
		return compG.getVarsEstadoSimulacion();
	}

	@Override
	public ArrayList<VariableEstado> aportarEstadoOptimizacion() {
		return compG.getVarsEstadoOptimizacion();
	}

//	@Override
//	public void actualizarParaSiguientePaso(DatosSalidaProblemaLineal resultado) {
//		this.getCompSimulacion().actualizarParaProximoPaso(resultado);
//
//	}

	@Override
	public void inicializarParaEscenario() {
		this.getCompSimulacion().inicializarParaEscenario();
	}

	@Override
	public void actualizarVarsEstadoSimulacion() {
		compG.actualizarVarsEstadoSimulacion();

	}

	@Override
	public void actualizarVarsEstadoOptimizacion() {
		compG.actualizarVarsEstadoOptimizacion();

	}

	@Override
	public void contribuirAS0fint() {
		AcumuladorCompSim acs = (AcumuladorCompSim) this.getCompSimulacion();
		acs.contribuirAS0fint();
	}

	public void contribuirAS0fintOptim() {
		AcumuladorCompSim acs = (AcumuladorCompSim) this.getCompSimulacion();
		acs.contribuirAS0fintOptim();
	}

	@Override
	public void cargarValVEOptimizacion() {
		compG.cargarValVEOptimizacion();
	}

	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos af) {
		if (energAcumuladaOpt!=null) {
			String nomVarEs = this.energAcumuladaOpt.getNombre();
			this.energAcumuladaOpt.setValorRecurso(af.getIncrementosYDerivadasParciales().get(nomVarEs));
		}
	}

	@Override
	public void cargarValRecursoVESimulacion() {
		if (energAcumulada!=null) {
			this.energAcumulada.setValorRecurso(this.energAcumuladaOpt.getValorRecurso());
			this.energAcumulada.setEstado(this.energAcumuladaOpt.getEstado());
		}
	}

	@Override
	public void asignaVAOptim() {
	}

	@Override
	public void asignaVASimul() {

	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();
		ret.add(this.getCantModDisp().getPe());

		return ret;
	}

	/**
	 * Como los controles DE no afectan las variables de estado en este
	 * participante se carga el valor de estado en estadoDespuesDeCE
	 * 
	 * @param instInicioPaso
	 * @param varsControlDE
	 */
	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		ArrayList<VariableEstado> varsEst = aportarEstadoSimulacion();
		for (VariableEstado ve : varsEst) {
			ve.setEstadoDespuesDeCDE(ve.getEstado());
		}
	}

	/**
	 * Como los controles DE no afectan las variables de estado en este
	 * participante se carga el valor de estado en estadoDespuesDeCE
	 * 
	 * @param instInicioPaso
	 * @param varsControlDE
	 */
	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		ArrayList<VariableEstado> varsEst = aportarEstadoOptimizacion();
		for (VariableEstado ve : varsEst) {
			ve.setEstadoDespuesDeCDE(ve.getEstado());
		}
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String fase, long instante) {
		double[] potencia = new double[this.getCantPostes()]; //inyectada en la red
		double[] potenciaAlmac = new double[this.getCantPostes()]; //almacenada en el acumulador 
		double[] potenciaBalance= new double[this.getCantPostes()]; //positivo inyectado a la red
		double[] energAcum = new double[this.getCantPostes()]; //energía acumulada en el acumulador, sólo en el balanceCronologico, al inicio de poste

		/**
		 * En la optimización no se usa la VE de simulación energiaAcumulada,
		 * para usarla en la salida se la carga
		 */
		if (fase.equalsIgnoreCase(Constantes.FASE_OPT)) {
			this.energAcumulada = this.getEnergAcumuladaOpt();
		}

		double energini = 0;
		if (compG.getFotoComportamientos(instante).get("compPaso").equalsIgnoreCase(Constantes.ACUMULTIPASO)) {
			energini = this.getEnergAcumulada().getEstadoDespuesDeCDE();
		} else {
			energini = this.getEnergIniPoste().getValor(instante);
		}
		energAcum[0] = energini;
		
		
	
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

			potencia[p] = salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("pot", Integer.toString(p)));
			potenciaAlmac[p] = salidaUltimaIter.getSolucion()
					.get(this.getCompD().generarNombre("potAlmac", Integer.toString(p)));
			potenciaBalance[p] = potencia[p]-potenciaAlmac[p];
			energiaMWhp = potenciaBalance[p]*this.getDuracionPostes(p)/utilitarios.Constantes.SEGUNDOSXHORA;
			energiaMWh += energiaMWhp;
			if (p>0) {
				energAcum[p]=energAcum[p-1]-potencia[p-1]*this.getDuracionPostes(p-1)/this.getRendIny().getValor(instante)/Constantes.SEGUNDOSXHORA
					+potenciaAlmac[p-1]*this.getDuracionPostes(p-1)* this.getRendAlmac().getValor(instante) / Constantes.SEGUNDOSXHORA;
			}
			valorEnergAlMarginal += energiaMWhp*costosMarginales[p];  // valor de la energóa en USD		
		}
		
		double costoVarOyMPaso = this.compS.calculaCostoPaso(salidaUltimaIter); // Se agrega costo O&M
		int cantModDisp = this.getCantModDisp().getValor().intValue();
		double gradGestion = Math.max((valorEnergAlMarginal - costoVarOyMPaso)/(cantModDisp*this.getPotenciaMaxima().getValor(instante)),0);

	
		String nombarra = this.getBarra().getNombre();
		
			
		DatosAcumuladorSP acu = new DatosAcumuladorSP(this.getNombre(), nombarra,
				this.getPotenciaMaxima().getValor(instante), this.getPotAlmMax().getValor(instante),
				this.getCantModDisp().getValor().intValue(), energini, this.getCompD().getValEnerg(), 
				potenciaBalance, potenciaAlmac,potenciaBalance, energAcum,calculaCostoPaso(salidaUltimaIter),gradGestion);
		for (DatosBarraSP dbsp : resultadoPaso.getRed().getBarras()) {
			if (nombarra.equalsIgnoreCase(dbsp.getNombre())
					|| this.getBarra().getRedAsociada().getCompD().isUninodal()) {
				dbsp.agregarAcumulador(acu);
				break;
			}
		}
	
	
	}

	public AcumuladorCompDesp getCompD() {
		return compD;
	}

	public void setCompD(AcumuladorCompDesp compD) {
		this.compD = compD;
	}

	public AcumuladorCompSim getCompS() {
		return compS;
	}

	public void setCompS(AcumuladorCompSim compS) {
		this.compS = compS;
	}

	public AcumuladorComp getCompG() {
		return compG;
	}

	public void setCompG(AcumuladorComp compG) {
		this.compG = compG;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Barra getBarra() {
		return barra;
	}

	public void setBarra(Barra barra) {
		this.barra = barra;
	}

	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}

	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
	}

	public Evolucion<Double> getPotMin() {
		return potMin;
	}

	public void setPotMin(Evolucion<Double> potMin) {
		this.potMin = potMin;
	}

	public Evolucion<Double> getPotAlmMin() {
		return potAlmMin;
	}

	public void setPotAlmMin(Evolucion<Double> potAlmacenadaMin) {
		this.potAlmMin = potAlmacenadaMin;
	}

	public Evolucion<Double> getPotAlmMax() {
		return potAlmMax;
	}

	public void setPotAlmMax(Evolucion<Double> potAlmacenadaMax) {
		this.potAlmMax = potAlmacenadaMax;
	}

	public Evolucion<Double> getRendIny() {
		return rendIny;
	}

	public void setRendIny(Evolucion<Double> rendIny) {
		this.rendIny = rendIny;
	}

	public Evolucion<Double> getRendAlmac() {
		return rendAlmac;
	}

	public void setRendAlmac(Evolucion<Double> rendAlmac) {
		this.rendAlmac = rendAlmac;
	}

	public Integer getCantModIni() {
		return cantModIni;
	}

	public void setCantModIni(Integer cantModIni) {
		this.cantModIni = cantModIni;
	}

	public Evolucion<Double> getDispMedia() {
		return dispMedia;
	}

	public void setDispMedia(Evolucion<Double> dispMedia) {
		this.dispMedia = dispMedia;
	}

	public Evolucion<Double> gettMedioArreglo() {
		return tMedioArreglo;
	}

	public void settMedioArreglo(Evolucion<Double> tMedioArreglo) {
		this.tMedioArreglo = tMedioArreglo;
	}

	public boolean isSalDetallada() {
		return salDetallada;
	}

	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}

	public Evolucion<Integer> getMantProgramado() {
		return mantProgramado;
	}

	public void setMantProgramado(Evolucion<Integer> mantProgramado) {
		this.mantProgramado = mantProgramado;
	}

	public Evolucion<Double> getCostoFijo() {
		return costoFijo;
	}

	public void setCostoFijo(Evolucion<Double> costoFijo) {
		this.costoFijo = costoFijo;
	}

	public Evolucion<Double> getCostoVariable() {
		return costoVariable;
	}

	public void setCostoVariable(Evolucion<Double> costoVariable) {
		this.costoVariable = costoVariable;
	}

	public VariableEstadoPar getEnergAcumulada() {
		return energAcumulada;
	}

	public void setEnergAcumulada(VariableEstadoPar energAcumulada) {
		this.energAcumulada = energAcumulada;
	}

	public VariableEstadoPar getEnergAcumuladaOpt() {
		return energAcumuladaOpt;
	}

	public void setEnergAcumuladaOpt(VariableEstadoPar energAcumuladaOpt) {
		this.energAcumuladaOpt = energAcumuladaOpt;
	}

	public Evolucion<Double> getEnergAlmacMax() {
		return energAlmacMax;
	}
	
	public Evolucion<Double> getEnergIniPoste() {
		return energIniPoste;
	}

	public void setEnergIniPoste(Evolucion<Double> energIniPoste) {
		this.energIniPoste = energIniPoste;
	}

	public void setEnergAlmacMax(Evolucion<Double> energAlmacMax) {
		this.energAlmacMax = energAlmacMax;
	}

	public Evolucion<Double> getFactorUso() {
		return factorUso;
	}

	public void setFactorUso(Evolucion<Double> factorUso) {
		this.factorUso = factorUso;
	}

	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		Acumulador.atributosDetallados = atributosDetallados;
	}


	public Evolucion<Boolean> getHayPotObligatoria() {
		return hayPotObligatoria;
	}

	public void setHayPotObligatoria(Evolucion<Boolean> hayPotObligatoria) {
		this.hayPotObligatoria = hayPotObligatoria;
	}

	public Evolucion<Double> getCostoFallaPotOblig() {
		return costoFallaPotOblig;
	}

	public void setCostoFallaPotOblig(Evolucion<Double> costoFallaPotOblig) {
		this.costoFallaPotOblig = costoFallaPotOblig;
	}

	public double[] getPotOblig() {
		return PotOblig;
	}

	public void setPotOblig(double[] potOblig) {
		PotOblig = potOblig;
	}
	
	

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}

	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		return this.getCompS().devuelveVarDualVEContinua(vec, resultado);
	}

	@Override
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla) {
// TODO:  ATENCIóN ESTO HAY QUE IMPLEMENTARLO PARA QUE FUNCIONE HIPERPLANOS
//		String compGlobalBellman = this.devuelveCompValorBellman();
//		if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
//			tabla.put(energAcumuladaOpt.getNombre(), compD.getNombreVEVolEnDespacho());
//		}	
		
	}

	@Override
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		this.getCompS().cargarVEfinPasoOptim(resultado);
		
	}

	@Override
	public ArrayList<VariableEstado> getVarsEstado() {
		ArrayList<VariableEstado> ret = new ArrayList<VariableEstado>();
		ret.add(energAcumulada);		
		return ret;
	}  

	public ArrayList<VariableEstado> getVarsEstadoOptim() {
		ArrayList<VariableEstado> ret = new ArrayList<VariableEstado>();
		ret.add(energAcumuladaOpt);		
		return ret;
		
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
	public void actualizaValoresVEDiscretizacionesVariables(long instante) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void crearCompDespPE() {
		AcumuladorCompDespPE compDespPE = new AcumuladorCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}

	@Override
	public GeneradorCompDespPE devuelveCompDespPE() {
		return (AcumuladorCompDespPE)getCompDespPE();
	}



}
