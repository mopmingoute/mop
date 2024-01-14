/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorTermico is part of MOP.
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

import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import procesosEstocasticos.VariableAleatoriaEntera;
import tiempo.Evolucion;
import utilitarios.Constantes;
import compdespacho.BarraCompDesp;
import compdespacho.TermicoCompDesp;
import compgeneral.TermicoComp;
import compsimulacion.TermicoCompSim;
import control.VariableControlDE;
import cp_compdespProgEst.AcumuladorCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import cp_compdespProgEst.GeneradorCompDespPE;
import cp_compdespProgEst.TermicoCompDespPE;
import datatypes.DatosTermicoCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosCurvaOferta;
import datatypesSalida.DatosEPPUnEscenario;
import datatypesSalida.DatosSalidaPaso;
import datatypesSalida.DatosTermicoSP;
import datatypesSalida.DatosTermicoSPAuxComb;
import datatypesSalida.ResultadoIteracionSP;
import estado.VariableEstado;
import estado.VariableEstadoPar;
import futuro.AFIncrementos;

/**
 * Clase que representa el generador tórmico
 * 
 * @author ut602614
 *
 */

public class GeneradorTermico extends Generador implements AportanteEstado {
	private ArrayList<String> listaCombustibles; // Lista de nombres de los combustibles
	private Hashtable<String, Combustible> combustibles;
	/** Lista de combustibles asociados al generador tórmico */
	private Hashtable<String, BarraCombustible> barrasCombustible;
	/**
	 * Barras correspondientes a cada uno de los combustibles de la lista anterior
	 */
	private VariableEstadoPar cantModIni;
	private VariableEstadoPar cantModIniOpt;
	private String flexibilidad;
	private TermicoCompDesp compD;
	private TermicoCompSim compS;
	private TermicoComp compG;
	private static ArrayList<String> atributosDetallados;
	private Hashtable<String, Evolucion<Double>> rendsPotMin;
	private Hashtable<String, Evolucion<Double>> rendsPotMax;

	/**
	 * Constructor del generador tórmico a partir de sus datos, sus combustibles y
	 * sus barras asociadas
	 */
	public GeneradorTermico(DatosTermicoCorrida dt, Barra barra, Hashtable<String, Combustible> combs,
			Hashtable<String, BarraCombustible> barrasCombs) {
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		this.setNombre(dt.getNombre());
		this.setBarra(barra);
		this.setCantModInst(dt.getCantModInst());
		this.setPropietario(dt.getPropietario());
		this.settMedioArreglo(dt.gettMedioArreglo());
		this.setDispMedia(dt.getDispMedia());
		this.combustibles = combs;
		this.listaCombustibles = dt.getListaCombustibles();
		this.barrasCombustible = barrasCombs;
		this.setPotenciaMaxima(dt.getPotMax());
		this.setMinimoTecnico(dt.getPotMin());
		/*
		 * this.setRendMax(dt.getRendPotMax()); this.setRendMin(dt.getRendPotMin());
		 */
		this.setFlexibilidad(dt.getFlexibilidadMin());
		this.setMantProgramado(dt.getMantProgramado());
		this.setCostoFijo(dt.getCostoFijo());
		this.setCostoVariable(dt.getCostoVariable());
		this.setRendsPotMax(dt.getRendimientosPotMax());
		this.setRendsPotMin(dt.getRendimientosPotMin());

		compD = new TermicoCompDesp(this);
		compG = new TermicoComp(this, compD, compS);
		compS = new TermicoCompSim(this, compD, compG);

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

		compG.setEvolucionComportamientos(dt.getValoresComportamientos());
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		String nombreD = "Disp-" + this.getNombre();
		PEDisponibilidadGeometrica pedg = new PEDisponibilidadGeometrica(nombreD, this.getNombre(), this.getDispMedia(),
				this.gettMedioArreglo(), this.getCantModInst(), this.getMantProgramado(), null,
				this.getCantModInst().getValor(instanteActual), dt.getCantModIni(), actual.getInstanteInicialPPaso(),
				actual.getInstanteFinalPPaso());
		int cantDispIni = Math.min(dt.getCantModIni(), dt.getCantModInst().getValor(instanteActual));
		this.setCantModDisp(new VariableAleatoria());
		getCantModDisp().setValor((double) cantDispIni);
		pedg.setCantDisponibles(this.getCantModDisp());
		for (int i = 0; i < getCantModInst().getValor(actual.getInstanteInicial()); i++) {
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
		pedg.setCantidadInnovaciones(this.getCantModInst().getValor(actual.getInstanteInicial()));
		this.getCantModDisp().setPe(pedg);

	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub

	}

	public String getFlexibilidad() {
		return flexibilidad;
	}

	public void setFlexibilidad(String flexibilidad) {
		this.flexibilidad = flexibilidad;
	}

	@Override
	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
		return compG.getVarsEstadoSimulacion();
	}

	@Override
	public ArrayList<VariableEstado> aportarEstadoOptimizacion() {
		return compG.getVarsEstadoOptimizacion();
	}

	@Override
	public void actualizarVarsEstadoSimulacion() {
		compG.actualizarVarsEstadoSimulacion();
	}

	@Override
	public void actualizarVarsEstadoOptimizacion() {
		// TODO Auto-generated method stub

	}

	@Override
	public void contribuirAS0fint() {
		compS.contribuirAS0fint();
	}

	@Override
	public void contribuirAS0fintOptim() {
		compS.contribuirAS0fintOptim();
	}

	@Override
	public void cargarValVEOptimizacion() {
		compG.cargarValVEOptimizacion();

	}

	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos af) {
		String valCompMinTec = compS.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valCompMinTec.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			String nomVarEs = this.cantModIniOpt.getNombre();
			this.cantModIniOpt.setValorRecurso(af.getIncrementosYDerivadasParciales().get(nomVarEs));
		}

	}

	@Override
	public void cargarValRecursoVESimulacion() {
		String valCompMinTec = compS.getValsCompGeneral().get(Constantes.COMPMINTEC);
		if (valCompMinTec.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			this.cantModIni.setValorRecurso(this.cantModIniOpt.getValorRecurso());
		}

	}

	public TermicoCompDesp getCompD() {
		return compD;
	}

	public void setCompD(TermicoCompDesp compD) {
		this.compD = compD;
	}

	public TermicoCompSim getCompS() {
		return compS;
	}

	public void setCompS(TermicoCompSim compS) {
		this.compS = compS;
	}

	public TermicoComp getCompG() {
		return compG;
	}

	public void setCompG(TermicoComp compG) {
		this.compG = compG;
	}

	public VariableEstadoPar getCantModIniOpt() {
		return cantModIniOpt;
	}

	public void setCantModIniOpt(VariableEstadoPar cantModIniOpt) {
		this.cantModIniOpt = cantModIniOpt;
	}

	public Hashtable<String, BarraCombustible> getBarrasCombustible() {
		return barrasCombustible;
	}

	public void setBarrasCombustible(Hashtable<String, BarraCombustible> barrasCombustible) {
		this.barrasCombustible = barrasCombustible;
	}

	public ArrayList<String> getListaCombustibles() {
		return listaCombustibles;
	}

	public void setListaCombustibles(ArrayList<String> listaCombustibles) {
		this.listaCombustibles = listaCombustibles;
	}

	public Hashtable<String, Combustible> getCombustibles() {
		return combustibles;
	}

	public void setCombustibles(Hashtable<String, Combustible> combustibles) {
		this.combustibles = combustibles;
	}

	public String getNEnerT(int p, String nombre) {
		TermicoCompDesp tc = (TermicoCompDesp) this.getCompDesp();
		return tc.getNenerTpc(p, nombre);
	}

	public VariableEstadoPar getCantModIni() {
		return cantModIni;
	}

	public void setCantModIni(VariableEstadoPar cantModIni) {
		this.cantModIni = cantModIni;
	}

	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		GeneradorTermico.atributosDetallados = atributosDetallados;
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {
		double[] potencia = new double[this.getCantPostes()];
		double[] costosMarginales = new double[this.getCantPostes()];
		Barra b = this.getBarra();
		boolean uninodal = this.getBarra().isRedUninodal(instante);
		if (uninodal) {
			b = this.getBarra().dameBarraUnica();
		}
		BarraCompDesp bcd = (BarraCompDesp) b.getCompDesp();
		for (int p = 0; p < this.getCantPostes(); p++) {
			String nombreRest = bcd.nombreRestPoste(p);
			 // costo marginal por poste en USD/MWh
			costosMarginales[p] = salidaUltimaIter.getDuales().get(nombreRest) * Constantes.SEGUNDOSXHORA
					/ this.getDuracionPostes(p);
		}
		int cantPostes = this.getCantPostes();
		int cantFuncUltimoIM = 0;
//		double[] valor1MWAlMarginal = new double[cantPostes]; 
		for (int p = 0; p < cantPostes; p++) {
			potencia[p] = salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("pot", Integer.toString(p)));
		}
		String nombarra = this.getBarra().getNombre();
		int[] cantModDesp = new int[this.getCantPostes()];
		
		if(this.getCompD().getCompMinTec().equalsIgnoreCase(Constantes.TERVARENTERAS)) {
			if(this.getCompD().getFlexibilidadMin().equalsIgnoreCase(Constantes.TERFLEXHORARIO)){
				for (int p = 0; p < cantPostes; p++) {
					cantModDesp[p] = (salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("nmod", Integer.toString(p)))).intValue();
				}
				cantFuncUltimoIM = cantModDesp[cantPostes-1];
			}else {
				for (int p = 0; p < cantPostes; p++) {
					cantModDesp[p] = (salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("nmod0"))).intValue();
				}				
				cantFuncUltimoIM = cantModDesp[0];
			}
		}

		boolean variables = this.getSimPaso().getCorrida().isCostosVariables();
		ArrayList<Double> variablesPoste = null;
		ArrayList<Double> potsPoste = null;
		if (this.getSimPaso().isSimulando() && variables) {
			variablesPoste = new ArrayList<Double>();
			potsPoste = new ArrayList<Double>();
		}

//		double [][] enerTC = new double[this.getCantPostes()][this.getCombustibles().size()];
//		double [] costoPC = new double[this.getCombustibles().size()];
//		costoPC = this.compS.calculaCostoDeCombustiblesPaso(salidaUltimaIter, instante);
		DatosTermicoSPAuxComb dcomb = this.compS.calculaResultadosDeCombustiblesPaso(salidaUltimaIter, instante);
		String clave;
		String nombre;
		double cvarNoComb = this.getCostoVariable().getValor(instante); // costo variable no combustible en USD/MWh
		double costoVarMintec = -1;
		int cantComb = listaCombustibles.size();
		double[] valoresComb = new double[cantComb];
		double[] costoVarProp = new double[cantComb]; // costo variable proporcional USD/MWh por encima del mónimo
														// tócnico con cada combustible
		double[] costoVarMax = new double[cantComb]; // costo variable medio USD/MWh a plena carga con cada combustible
		int ic = 0;
		double cvMin = Double.MAX_VALUE;
		for (String sc : listaCombustibles) {
			BarraCombustible bc = barrasCombustible.get(sc);
			RedCombustible redc = bc.getRedAsociada();
			if (redc.isUninodal())
				bc = redc.getBarraunica();
			Combustible comb = combustibles.get(sc);
			// Se usa cualquier entero para el poste porque es irrelevante
			double valorComb = bc.devuelveVarDualBalance(salidaUltimaIter, -1);
			double pci = comb.getPci(); // PCI en MWh por unidad
			double potEspEncimaMin = compD.getPotEspTerProp(); // MW tórmicos por MW elóctrico por encima del mónimo
			double potEspMax = 1 / this.getRendsPotMax().get(sc).getValor(instante); // MW tórmicos por MW elóctrico a
																						// plena carga
			costoVarProp[ic] = valorComb / pci * potEspEncimaMin;
			costoVarMax[ic] = valorComb / pci * potEspMax; // costo variable medio a potencia móxima en USD/MWh
			ic++;
		}
		DatosCurvaOferta dco = null;
		if (this.getSimPaso().isSimulando() && variables) {
			DatosEPPUnEscenario des = this.getSimPaso().getDatosEscenario();
			dco = des.getCurvOfertas().get(this.getSimPaso().getPaso());
		}

		double costoTotPaso = 0;
		if (this.getSimPaso().isSimulando() && variables) {
			for (int p = 0; p < this.getCantPostes(); p++) {
				ic = 0;
				if (this.getSimPaso().isSimulando() && variables) {
					if (this.getCantModDisp().getValor().intValue() > 0) {
						double var = cvarNoComb;
						for (int ic2 = 0; ic2 < cantComb; ic2++) {
							var += costoVarMax[ic2];
						}
						variablesPoste.add(var);
					} else {
						variablesPoste.add(0.0);
					}
				}
				potsPoste.add(this.compD.getCantModDisp() * this.getPotenciaMaxima().getValor(instante));
			}
		}
		if (this.getSimPaso().isSimulando() && variables) {
			dco.agregarVariablesMaquinaPaso(this.getNombre(), variablesPoste);
			dco.agregarPotsDispMaquinaPaso(this.getNombre(), potsPoste);
		}

		Integer cantModini;
		if (this.getCompD().getCompMinTec().equalsIgnoreCase(Constantes.TERSINMINTEC)
				|| this.getCompD().getCompMinTec().equalsIgnoreCase(Constantes.TERMINTECFORZADO)
				|| this.getCompD().getCompMinTec().equalsIgnoreCase(Constantes.TERVARENTERAS)) {
			cantModini = -1;
		} else {
			cantModini = this.getCantModIni().getEstado().intValue();

		}
		for (int i = 0; i < dcomb.getCostoC().length; i++) {
			costoTotPaso += dcomb.getCostoC()[i];
		}

		costoTotPaso += this.compS.calculaCostoPaso(salidaUltimaIter); // Se agrega costo O&M
		int cantModDisp = this.getCantModDisp().getValor().intValue();
		double disp = this.getDispMedia().getValor(instante);

		// Cólculo del gradiente de gestión para disponibilidad media en USD/MW
		double[] gradGestion = new double[cantComb];
		for (int ic2 = 0; ic2 < cantComb; ic2++) {
			for (int p = 0; p < this.getCantPostes(); p++) {
				gradGestion[ic2] += Math.max(0, costosMarginales[p] - costoVarMax[ic2] - cvarNoComb)
						* (this.getDuracionPostes(p) / utilitarios.Constantes.SEGUNDOSXHORA) * disp;
//				System.out.println(this.getNombre() + "  " + costosMarginales[p] + "  " + costoVarMax[ic2]);
//				ojo falta el variable no combustible
			}
		}
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
	
		DatosTermicoSP ter = new DatosTermicoSP(this.getNombre(), nombarra, this.getPotenciaMaxima().getValor(instanteActual),
				this.getMinimoTecnico().getValor(instanteActual), cantModini, cantFuncUltimoIM, this.getCantModDisp().getValor().intValue(),
				cantModDesp, potencia, this.getListaCombustibles(), dcomb.getVolC(), dcomb.getEnerTC(),
				dcomb.getEnerTPC(), dcomb.getEnerEC(), dcomb.getCostoC(), dcomb.getVolPC(), dcomb.getPotEPC(),
				costoVarMintec, costoVarProp, costoTotPaso, gradGestion);

//		DatosTermicoSP(String nombre,String nombreBarra, double potMax, double potMin,
//				int cantModIni, int cantModDisp, int[] cantModDesp,
//				double[] potencias, double rendMax, double rendMin, ArrayList<String> listaCombustibles,
//				double[] volC, double[] enerTC, double[] enerEC, double[] costoC, double[][] volPC, double[][] potEPC,
//				double costoVarMintec, double costoVarProp[], double costoTotPaso,
//				double[] gradGestion) 		

		for (DatosBarraSP dbsp : resultadoPaso.getRed().getBarras()) {
			if (nombarra.equalsIgnoreCase(dbsp.getNombre())
					|| this.getBarra().getRedAsociada().getCompD().isUninodal()) {
				dbsp.agregarTermico(ter);
				break;
			}
		}
	}
	
	


	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();
		ret.add(this.getCantModDisp().getPe());
		return ret;

	}

	/**
	 * Como los controles DE no afectan las variables de estado en este participante
	 * se carga el valor de estado en estadoDespuesDeCE
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
		// TODO: FALTA ARREGLAR PARA EL COMPORTAMIENTO VARS DE ESTADO
	}

	/**
	 * Como los controles DE no afectan las variables de estado en este participante
	 * se carga el valor de estado en estadoDespuesDeCE
	 * 
	 * @param instInicioPaso
	 * @param varsControlDE
	 */
	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		ArrayList<VariableEstado> varsEst = aportarEstadoSimulacion();
		for (VariableEstado ve : varsEst) {
			ve.setEstadoDespuesDeCDE(ve.getEstado());
		}
		// TODO: FALTA ARREGLAR PARA EL COMPORTAMIENTO VARS DE ESTADO
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
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// DELIBERADAMENTE EN BLANCO PORQUE NUNCA SERó INVOCADO PORQUE
		// EL GeneradorTermico NO TIENE VE CONTINUAS;
		return 0.0;
	}

	@Override
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla) {
		// Deliberadamente en blanco

	}

	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		this.compS.cargarVEfinPasoOptim(resultado);

	}

	@Override
	public ArrayList<VariableEstado> getVarsEstado() {
		ArrayList<VariableEstado> ret = new ArrayList<VariableEstado>();
		ret.add(cantModIni);
		return ret;
	}

	public ArrayList<VariableEstado> getVarsEstadoOptim() {
		ArrayList<VariableEstado> ret = new ArrayList<VariableEstado>();
		ret.add(cantModIniOpt);
		return ret;
	}

	@Override
	public void aportarImpacto(Impacto i, DatosObjetivo costo) {
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

	public void setRendsPotMax(Hashtable<String, Evolucion<Double>> rendsPotMax) {
		this.rendsPotMax = rendsPotMax;
	}

	public void setRendsPotMin(Hashtable<String, Evolucion<Double>> rendsPotMin) {
		this.rendsPotMin = rendsPotMin;
	}

	public Hashtable<String, Evolucion<Double>> getRendsPotMax() {
		return rendsPotMax;
	}

	public Hashtable<String, Evolucion<Double>> getRendsPotMin() {
		return rendsPotMin;
	}

	@Override
	public void actualizaValoresVEDiscretizacionesVariables(long instante) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearCompDespPE() {
		TermicoCompDespPE compDespPE = new TermicoCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}

	@Override
	public GeneradorCompDespPE devuelveCompDespPE() {
		return (TermicoCompDespPE) getCompDespPE();
	}

}
