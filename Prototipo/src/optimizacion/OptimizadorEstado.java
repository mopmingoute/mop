/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * OptimizadorEstado is part of MOP.
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

package optimizacion;

import java.nio.file.DirectoryIteratorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import compdespacho.ConstructorHiperplanosCompDesp;
import compdespacho.HidraulicoCompDesp;
import compgeneral.CompGeneral;
import compgeneral.ImpactoComp;
import control.VariableControl;
import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosParamSalidaOpt;
import datatypesSalida.DatosPaso;
import datatypesSalida.DatosSalidaPaso;
import estado.VariableEstado;
import futuro.AFHiperplanos;
import futuro.AFIncrementos;
import futuro.Hiperplano;
import interfacesParticipantes.AportanteControlDE;
import futuro.InformacionValorPunto;
import interfacesParticipantes.AportanteEstado;
import interfacesParticipantes.AportantePost;
import logica.CorridaHandler;
import logica.Despachador;
import parque.Azar;
import parque.ConstructorHiperplanos;
import parque.Corrida;
import parque.GeneradorHidraulico;
import parque.Impacto;
import parque.Participante;
import persistencia.EscritorTablaSalidaPaso;
import procesosEstocasticos.ProcesadorSorteosOptimPEs;
import procesosEstocasticos.ProcesoEscenarios;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import simulacion.PostizacionPaso;
import simulacion.Postizador;
import simulacion.ValPostizador;
import tiempo.BloqueTiempo;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.EnumeradorLexicografico;
import utilitarios.ProfilerBasicoTiempo;

/**
 * Es el equivalente al SimuladorPaso, en el sentido de que tiene el
 * conocimiento sobre el problema concreto que le permite implementar la
 * interfase Optimizable
 * 
 * 
 * @author ut469262
 *
 */

public class OptimizadorEstado {

	private OptimizadorPaso opPaso;
	private Despachador despachador;
	private double factorDescuentoPaso; // factor de descuento para un intervalo
										// de un paso
	private AFIncrementos aproxFuturaInc;

	private String compGlobalBellman;

	private EscritorTablaSalidaPaso escritor;
	private EnumeradorLexicografico enumCDE; // enumerador de controles
												// discretos exhaustivos

	/**
	 * primer índice recorre las variables de control DE activas segundo índice
	 * recorre los ordinales en la discretización de esas variables que
	 * corresponden a controles posibles en el paso actual.
	 */
	private int[][] ordinalesControlesDEFactibles;

	private int isort; // ordinal del último sorteo Montecarlo muestreado en los PE

//	/**
//	 * Referencias al resoptim casteadas segÃ³n el comportamiento global
//	 */
//	private ResOptimIncrementos roptimI;
//	private ResOptimHiperplanos roptimH;	
//	

	public Despachador getDespachador() {
		return despachador;
	}

	public void setDespachador(Despachador despachador) {
		this.despachador = despachador;
	}

	public double getFactorDescuentoPaso() {
		return factorDescuentoPaso;
	}

	public void setFactorDescuentoPaso(double factorDescuentoPaso) {
		this.factorDescuentoPaso = factorDescuentoPaso;
	}

	public int getIsort() {
		return isort;
	}

	public void setIsort(int isort) {
		this.isort = isort;
	}

	public EscritorTablaSalidaPaso getEscritor() {
		return escritor;
	}

	public void setEscritor(EscritorTablaSalidaPaso escritor) {
		this.escritor = escritor;
	}

	public EnumeradorLexicografico getEnumCDE() {
		return enumCDE;
	}

	public void setEnumCDE(EnumeradorLexicografico enumCDE) {
		this.enumCDE = enumCDE;
	}

	public int getPasoIniSal() {
		return pasoIniSal;
	}

	public void setPasoIniSal(int pasoIniSal) {
		this.pasoIniSal = pasoIniSal;
	}

	public int getPasoFinSal() {
		return pasoFinSal;
	}

	public void setPasoFinSal(int pasoFinSal) {
		this.pasoFinSal = pasoFinSal;
	}

	public int[] getEstIniSal() {
		return estIniSal;
	}

	public void setEstIniSal(int[] estIniSal) {
		this.estIniSal = estIniSal;
	}

	public int[] getEstFinSal() {
		return estFinSal;
	}

	public void setEstFinSal(int[] estFinSal) {
		this.estFinSal = estFinSal;
	}

	public int getSortIniSal() {
		return sortIniSal;
	}

	public void setSortIniSal(int sortIniSal) {
		this.sortIniSal = sortIniSal;
	}

	public int getSortFinSal() {
		return sortFinSal;
	}

	public void setSortFinSal(int sortFinSal) {
		this.sortFinSal = sortFinSal;
	}


	int pasoIniSal = 0;
	int pasoFinSal = 500;

	int[] estIniSal = { 0, 0 };
	int[] estFinSal = { 0, 0 };
	int sortIniSal = 0; // Empezando en 0
	int sortFinSal = 0;

	public OptimizadorEstado(OptimizadorPaso opPaso) {
		super();
		this.opPaso = opPaso;
		this.opPaso.setOpEst(this);
		this.despachador = new Despachador();
		double durPaso = (double) opPaso.getPasoActual().getDuracionPaso();
		double pasosPorAnio = Constantes.SEGUNDOSXANIO / durPaso;
		double tasaAnual = opPaso.getTasaDescuentoAnual();
		double factorAnual = 1 / (1 + tasaAnual);
		this.factorDescuentoPaso = Math.pow(factorAnual, 1 / pasosPorAnio);

		this.escritor = new EscritorTablaSalidaPaso();
	}

	/**
	 * Para un estado de la grilla calcula el VBinit valor de Bellman al inicio y en
	 * el comportamiento Hiperplanos, también calcula las derivadas parciales del
	 * valor de Bellman, del paso t, después del salto de las VE de los PE discretos
	 * exhustivos.
	 * 
	 * Tiene en cuenta la tasa de descuento.
	 * 
	 * Debe hacer la prueba exhaustiva para determinar los controles DE óptimos
	 * 
	 * @param codigoEstado se pasa como parámetro solamente para determinar si se
	 *                     saca la salida detallada, ya que las VE ya llegan
	 *                     cargadas con el valor del estado asociado al código.
	 */
	/**
	 * @param codigoEstado
	 * @return
	 */
	/**
	 * @param codigoEstado
	 * @return
	 */
	public Hiperplano optimizarEstado(int[] codigoEstado) {

//		Optimizador.prof.iniciarContador("dentro_opt_estado_fuera_loop_sorteos");
		PasoTiempo pasoActual = opPaso.getPasoActual();
		int cantSortMontecarlo = opPaso.getCantSortMontecarlo();
		long[] instantesMuestreo = opPaso.getInstantesMuestreo();
		int cantVCDE = opPaso.getCantVarsControlDE();
		boolean hayControlesDE = opPaso.getCantVarsControlDE() > 0;
		Azar azar = opPaso.getAzar();
		long instanteIniPaso = opPaso.getInstIniPaso();
		Corrida corrida = opPaso.getCorrida();
		ResOptim resoptim = opPaso.getResoptim();
		int numpaso = opPaso.getNumpaso();
		double escalar = 1 / ((double) cantSortMontecarlo);
		int cantVEContinuas = opPaso.getResoptim().getCantVECont();

		compGlobalBellman = opPaso.getCorrida().getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());
//		boolean esHiperplanos = compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS);
		
		DatosSalidaProblemaLineal salidaUltimaIter;

		/**
		 * Prueba todos los valores posibles de las variables de control DE y elige el
		 * VB menor y los carga en la tabla de controles DE
		 */
		double objetivo = Double.MAX_VALUE;
		int ordinalOpt = 0;
		int[] codigoOpt;
		int ordinalCDE = 0;
		int[] codCDE;
		ordinalesControlesDEFactibles = new int[cantVCDE][];
		if (hayControlesDE) {
			int[] cotasSuperiores = new int[cantVCDE];

			for (AportanteControlDE acde : opPaso.getCorrida().getAportantesControlDE()) {
				acde.cargaControlesDEFactibles();
			}
			ArrayList<VariableControlDE> varsControlDE = opPaso.getVarsControlDEActivas();
			int iv = 0;
			for (VariableControlDE vcde : varsControlDE) {
				ordinalesControlesDEFactibles[iv] = vcde.devuelveControlesFactibles();
				cotasSuperiores[iv] = ordinalesControlesDEFactibles[iv].length - 1;
				iv++;
			}
			int[] cotasInferiores = new int[cantVCDE];
			enumCDE = new EnumeradorLexicografico(cantVCDE, cotasInferiores, cotasSuperiores);
			enumCDE.inicializaEnum();
			enumCDE.creaTablaYListaOrdinales();
			codCDE = enumCDE.devuelveVector();
		} else {
			codCDE = new int[] { Integer.MIN_VALUE };
		}

		Hiperplano hipControlDEOptimo = new Hiperplano(cantVEContinuas, numpaso, 0); // Almacena el hiperplano asociado
																						// al control DEOptimo

		while (codCDE != null) {

			// Actualiza las variables iniciales de los participantes y de sus
			// ComportamientoSimulacion que no son variables de estado
			actualizarOtrosDatosIniciales();

			// Carga los valores en las variables de control DE los participantes y acumula
			// el costo de los controles
			int ic = 0;
			for (VariableControlDE vc : opPaso.getVarsControlDEActivas()) {
				vc.cargaValorAPartirDeCodigo(codCDE[ic], instanteIniPaso);
				ic++;
			}

			/*
			 * Actualiza las variables de estado de los participantes que aportan estado
			 * como resultado de los cambios introducidos por las variables de control DE El
			 * valor actualizado se carga en el atributo estadoDespuesDeCDE
			 */
			for (AportanteEstado p : corrida.getAportantesEstado()) {
				p.actualizaVEOptPorControlesDE(instanteIniPaso, opPaso.getVarsControlDEActivas());
			}

			// vBPromedio e hipPromedio son las estructuras para promediar resultados de los
			// sorteos Montecarlo
			double vBPromedio = 0.0;

			Hiperplano hipPromedio = new Hiperplano(cantVEContinuas, numpaso, 0);
			int i = 0;
			for (VariableEstado vec : resoptim.getVarsEstadoContinuas()) {
				// TODO: OJO ESTADO ANTES O DESPUÃ³S DEL CONTROL DISCRETO
				hipPromedio.getPunto()[i] = vec.getEstado();
				i++;
			}

			// Loop en los sorteos Montecarlo

			for (isort = 0; isort < cantSortMontecarlo; isort++) {

				/**
				 * Carga los valores de las VA -en el atributo valor para los PE no muestreado
				 * -en el atributo ultimoMuestreo para los PE muestreados
				 * 
				 */
				for (ProcesoEstocastico pe : azar.getProcesosOptim()) {
					pe.cargarVAOptim(isort, instantesMuestreo);
				}

				for (ProcesoEstocastico pe : opPaso.devuelveAzar().getProcesosOptim()) {
					pe.preparaUnSorteoMontecarlo();
				}

				if (Constantes.NIVEL_CONSOLA > 1) {
					StringBuilder sb = new StringBuilder();
					if (hayControlesDE) {
						sb.append("Código CDE: ");
						for (int is = 0; is < codCDE.length; is++) {
							sb.append(codCDE[is] + " ");
						}
					}
					sb.append("sorteo " + isort);
					System.out.println(sb.toString());
				}

				/**
				 * PREPARACION DE DATOS DEL POSTIZADOR ASOCIADOS A CUALQUIER TIPO DE POSTIZACION
				 */
				opPaso.getPostizador().setPtiempo(opPaso.getPasoActual());
				opPaso.getPostizador().setNumPaso(opPaso.getNumpaso());
				opPaso.getPostizador()
						.setClusterizar(this.getOpPaso().getCorrida().getPostizacion().equalsIgnoreCase("clustering"));
				opPaso.getPostizador().setKmax(this.getOpPaso().getCorrida().getClusters());
				BloqueTiempo bloque = pasoActual.getBloque();

				if (!corrida.isPostizacionExterna()) {
					/**
					 * PREPARACION DE LA POSTIZACION INTERNA
					 */
					/**
					 * TODO: ALGUNAS COSAS SOLO HAY QUE HACERLAS CUANDO CAMBIA EL BLOQUE
					 */
					ArrayList<Double> referencia = opPaso.obtenerRefPostInterna(isort);
					opPaso.getPostizador().setReferencia(referencia);

					int cantPostes = bloque.getCantPostes();
					int[] durPos = bloque.getDuracionPostes();
					opPaso.getPostizador().setCantPos(cantPostes);
					int[] intPorPoste = new int[cantPostes];
					for (i = 0; i < cantPostes; ++i) {
						intPorPoste[i] = durPos[i] / bloque.getIntervaloMuestreo();
					}
					opPaso.getPostizador().setInterPorPoste(intPorPoste);
					opPaso.getPostizador().setDurPos(bloque.getDuracionPostes());

				}

				/**
				 * En lugar de pasar como parametros de la funcion se setearon previamente los
				 * atributos.
				 */
				opPaso.setPostPaso(opPaso.getPostizador().postizar());

				opPaso.postizacionesPaso.put(numpaso, opPaso.getPostPaso());

				opPaso.getValPostizador().setPostPaso(opPaso.getPostPaso());
				opPaso.setDuracionPaso(opPaso.getPostPaso().getDurPaso());
				opPaso.setCantidadPostes(opPaso.getPostPaso().getCantPos());
				opPaso.setDuracionPostes(opPaso.getPostPaso().getDurPos());

				/**
				 * Se trae la aproximacion futura desde ResOptim
				 */
				if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
					/**
					 * Estima por heurÃ³stica el s0fint; se usa una estimaciÃ³n del s0fint propia de
					 * la optimizaciÃ³n
					 */
					for (AportanteEstado p : corrida.getAportantesEstado()) {
						if (p instanceof ProcesoEstocastico) {
							ProcesoEstocastico pe = (ProcesoEstocastico) p;
							if (pe.isUsoOptimizacion())
								p.contribuirAS0fintOptim();
						} else {
							p.actualizaValoresVEDiscretizacionesVariables(this.opPaso.getInstFinPaso());
							p.contribuirAS0fintOptim();
						}
					}
					// Se trae la aproximación local para el S0fint que
					// está cargado en las VE
					aproxFuturaInc = ((ResOptimIncrementos) resoptim).devuelveAproxS0fint(numpaso, false);
					// Carga en las variables de estado de optimización el valor al fin del paso
					// en el estado S0fint de los recursos
					for (AportanteEstado ap : corrida.getAportantesEstado()) {
						ap.cargarValRecursoVEOptimizacion(aproxFuturaInc);
					}
				} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
					ConstructorHiperplanos constH = corrida.getConstructorHiperplanos();
					constH.preparaParaPaso(numpaso, (ResOptimHiperplanos) resoptim);
				}

				for (Participante p : corrida.getParticipantes())
					p.cargarDatosCompDespachoOptim(pasoActual.getInstanteInicial());

				int iter = 1; // Inicializa el contador de iteraciones
				boolean sigoIterando = true;
				int maxiter = corrida.getMaximoIteraciones();
				int numiter = corrida.getNumeroIteraciones();
				salidaUltimaIter = null;

				while (sigoIterando && iter <= maxiter) {

					for (Participante p : corrida.getParticipantes()) {
						p.actualizarVariablesCompDespachoOptim(iter);
					}
					for (Participante p : corrida.getParticipantes()) {
						p.cargarDatosParaUnaIteracion(iter, salidaUltimaIter);
					}

					String archEntradaPL = null;
					String archSalidaPL = null;

//
//					if(numpaso==186 & codigoEstado[0]==2 & codigoEstado[1]==9 & (codigoEstado[2]==1 || codigoEstado[2]==2) & (isort==0 || isort == 1)) {
//						int pp = 0;
//					}
					int codigoLlamado = 0;
					int mult = 100;
					for (int ie = 0; ie < codigoEstado.length; ie++) {
						codigoLlamado = codigoLlamado * mult + codigoEstado[ie];
					}
					codigoLlamado = codigoLlamado * mult + isort;
					codigoLlamado = codigoLlamado * mult + iter;

				
					
					salidaUltimaIter = despachador.despachar(corrida, numpaso, codigoLlamado,
							opPaso.getNombreArchInfactible(), archEntradaPL, archSalidaPL);

					ArrayList<GeneradorHidraulico> hidros = new ArrayList<GeneradorHidraulico>(
							opPaso.getCorrida().getHidraulicos().values());
					boolean cierranTodos = true;
					boolean cierra1h = true;
					for (GeneradorHidraulico gh : hidros) {
						HidraulicoCompDesp hcd = (HidraulicoCompDesp) gh.getCompD();
						if (hcd.getCompLago().equalsIgnoreCase(Constantes.HIDROSINLAGO))
							cierra1h = gh.getCompS().verificaBalanceHidro(salidaUltimaIter, numpaso, codigoEstado,
									isort, iter);
						cierranTodos = cierranTodos && cierra1h;
					}
					if (!cierranTodos) {
//						archEntradaPL = opPaso.getNombreArchEntradasPL();
//						despachador.getEntrada().guardar(archEntradaPL);
//						System.out.println("NO CIERRA BALANCE");
						// System.exit(1);

					}

				

					if (corrida.getCriterioParada().equalsIgnoreCase(Constantes.PORUNANIMIDADPARTICIPANTES)) {
						sigoIterando = false;
						for (Participante p : corrida.getParticipantes()) {
							if (!p.aceptaDetenerIteracion(iter, salidaUltimaIter)) {
								sigoIterando = true;
								break;
							}
						}
					} else if (corrida.getCriterioParada().equalsIgnoreCase(Constantes.PORNUMEROITERACIONES)) {
						if (iter == numiter)
							sigoIterando = false;
					}
					// Acá se termina una iteración
					iter++;
				}
			
				
				double cp = calculaCostoPaso(salidaUltimaIter);
				
//				cp = utilitarios.Metodos.round(cp, 4);
				
			//	System.out.println("Costo Estado Paso : " + cp + " - " + Arrays.toString(codigoEstado) + " paso : " + numpaso + " sorteo : " + isort);
//				

				double vbfp  = calculaVBFinal(salidaUltimaIter);
				
//				System.out.println("VB FInal : " + vbfp + " - " + Arrays.toString(codigoEstado) + " paso : "
//						+ numpaso + " sorteo : " + isort);
//				

//				vbfp = utilitarios.Metodos.round(vbfp, 4);

				double vBIniUnMontecarlo = cp * Math.pow(factorDescuentoPaso, 0.5) + vbfp * factorDescuentoPaso;

				vBPromedio += vBIniUnMontecarlo * escalar; // escalar = 1/cantSorteos
				if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) { 
					Hiperplano hipUnMontecarlo = construyeHiperplanoOptimo(corrida.getAportantesEstado(), resoptim,
							salidaUltimaIter, vBIniUnMontecarlo);
					hipPromedio.sumaHiperplanoPorEscalar(hipUnMontecarlo, escalar);
				}

				
				if (!hayControlesDE)
					codCDE = null;
				if (opPaso.getCorrida().getDatosParamSalidaOpt().isSalOpt()) {
					guardarResultadoEstadoOpt(numpaso, codigoEstado, codCDE, isort, vbfp, cp, vBIniUnMontecarlo,
							salidaUltimaIter, opPaso.getCorrida().getFase(),
							opPaso.getCorrida().getDatosParamSalidaOpt());
				}
			}

//			if (codigoEstado[0] == 1 && codigoEstado[1] == 4) {
//				int pp = 0;
//				System.out.println("Costo Estado Paso : " + vBPromedio + " - " + Arrays.toString(codigoEstado)
//						+ " paso : " + numpaso + " sorteo : " + isort);
//			}

			if (vBPromedio < objetivo) {
				// el control DE corriente es el que da el menor objetivo hasta el momento
				ordinalOpt = ordinalCDE;
				objetivo = vBPromedio;
				if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS))
					hipPromedio.setvBellman(vBPromedio);

				hipControlDEOptimo = hipPromedio;
			}
			if (hayControlesDE) {
				codCDE = enumCDE.devuelveVector();
				ordinalCDE++;
			} else {				
				codCDE = null;
			}
		}
		if (hayControlesDE) {
			codigoOpt = enumCDE.devuelveVectorDeOrdinal(ordinalOpt);
			resoptim.cargaCodigoControlesDEOptimos(numpaso, codigoEstado, codigoOpt);
		}
		return hipControlDEOptimo;
	}

	/**
	 * Es empleado solo en las pruebas de los procesos estocásticos, para producir
	 * los sorteos de las VA que se usarian en la optimización real
	 * 
	 * @param codigoEstado
	 * 
	 * @return Hashtable<String, double[][]> resultadosPEsUnEstado Valores de las
	 *         variables aleatorias para el estado Clave: nombre del proceso
	 *         estocasticos + "-" + nombre de la variable aleatoria La clave se
	 *         construye con el método ProcesadorSorteosOptimPEs.clavePEVA y
	 *         contiene los nombres del PE y la VA. Valor, un array: primer índice
	 *         sorteo de Montecarlo segundo índice intervalo de muestreo, si el PE
	 *         no es muestreado hay un solo valor
	 */

	public Hashtable<String, double[][]> sortearPEsOptimEstado(int[] codigoEstado,
			ArrayList<ProcesoEstocastico> listaPEs) {

		PasoTiempo pasoActual = opPaso.getPasoActual();
		int cantSortMontecarlo = opPaso.getCantSortMontecarlo();
		long[] instantesMuestreo = opPaso.getInstantesMuestreo();
		Azar azar = opPaso.getAzar();

		Hashtable<String, double[][]> resultadosPEsUnEstado = new Hashtable<String, double[][]>();

		for (ProcesoEstocastico pe : listaPEs) {
			for (VariableAleatoria va : pe.getVariablesAleatorias()) {
				double[][] aux = new double[cantSortMontecarlo][];
				String clave = ProcesadorSorteosOptimPEs.clavePEVA(pe, va);
				resultadosPEsUnEstado.put(clave, aux);
			}
		}

		long instanteIniPaso = opPaso.getInstIniPaso();
		Corrida corrida = opPaso.getCorrida();
		ResOptim resoptim = opPaso.getResoptim();
		int numpaso = opPaso.getNumpaso();
		double escalar = 1 / ((double) cantSortMontecarlo);
		int cantVEContinuas = opPaso.getResoptim().getCantVECont();

		compGlobalBellman = opPaso.getCorrida().getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());
//		boolean esHiperplanos = compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS);
		DatosSalidaProblemaLineal salidaUltimaIter;

		/**
		 * Prueba todos los valores posibles de las variables de control DE y elige el
		 * VB menor y los carga en la tabla de controles DE
		 */
		double objetivo = Double.MAX_VALUE;
		int ordinalOpt = 0;
		int[] codigoOpt;
		int ordinalCDE = 0;
		int[] codCDE;

		// Actualiza las variables iniciales de los participantes y de sus
		// ComportamientoSimulacion que no son variables de estado
		
		actualizarOtrosDatosIniciales();

		// Loop en los sorteos Montecarlo
		for (isort = 0; isort < cantSortMontecarlo; isort++) {

			for (ProcesoEstocastico pe : listaPEs) {
				pe.preparaUnSorteoMontecarlo();
			}

			/**
			 * Almacena resultados de un sorteo del estado clave: nombre del proceso
			 * estocasticos + "-" + nombre de la variable aleatoria valor: double[] con el
			 * valor de la VA (en ese casto tiene dimension 1) o los valores muestreados
			 */
			Hashtable<String, double[]> resultados1Sort = new Hashtable<String, double[]>();

			/**
			 * PREPARACION DE DATOS DEL POSTIZADOR ASOCIADOS A CUALQUIER TIPO DE POSTIZACION
			 */
			opPaso.getPostizador().setPtiempo(opPaso.getPasoActual());
			opPaso.getPostizador().setNumPaso(opPaso.getNumpaso());

			BloqueTiempo bloque = pasoActual.getBloque();

			if (!corrida.isPostizacionExterna()) {
				/**
				 * PREPARACION DE LA POSTIZACION INTERNA
				 */
				/**
				 * TODO: ALGUNAS COSAS SOLO HAY QUE HACERLAS CUANDO CAMBIA EL BLOQUE
				 */
				ArrayList<Double> referencia = opPaso.obtenerRefPostInterna(isort);
				opPaso.getPostizador().setReferencia(referencia);

				int cantPostes = bloque.getCantPostes();
				int[] durPos = bloque.getDuracionPostes();
				opPaso.getPostizador().setCantPos(cantPostes);
				int[] intPorPoste = new int[cantPostes];
				for (int i = 0; i < cantPostes; ++i) {
					intPorPoste[i] = durPos[i] / bloque.getIntervaloMuestreo();
				}
				opPaso.getPostizador().setInterPorPoste(intPorPoste);
				opPaso.getPostizador().setDurPos(bloque.getDuracionPostes());

			}

			/**
			 * En lugar de pasar como parametros de la funcion se setearon previamente los
			 * atributos.
			 */
			opPaso.setPostPaso(opPaso.getPostizador().postizar());
			opPaso.postizacionesPaso.put(numpaso, opPaso.getPostPaso());

			opPaso.getValPostizador().setPostPaso(opPaso.getPostPaso());
			opPaso.setDuracionPaso(opPaso.getPostPaso().getDurPaso());
			opPaso.setCantidadPostes(opPaso.getPostPaso().getCantPos());
			opPaso.setDuracionPostes(opPaso.getPostPaso().getDurPos());

			/**
			 * Carga los valores de las VA -desde el atributo valor para los PE no
			 * muestreado -desde el atributo ultimoMuestreo para los PE muestreados
			 */
			for (ProcesoEstocastico pe : listaPEs) {
				pe.cargarVAOptim(isort, instantesMuestreo);
				boolean muestreado = pe.isMuestreado();
				for (VariableAleatoria va : pe.getVariablesAleatorias()) {
					String clave = ProcesadorSorteosOptimPEs.clavePEVA(pe, va);
					double[] valores;
					if (pe.isMuestreado()) {
						valores = va.getUltimoMuestreo();
					} else {
						valores = new double[1];
						valores[0] = va.getValor();
					}
					double[][] aux = resultadosPEsUnEstado.get(clave);
					aux[isort] = valores;
				}
			}
		}
		return resultadosPEsUnEstado;
	}

	/**
	 * A partir del resultado de un problema lineal de despacho crea un hiperplano
	 * tangente a la funciÃ³n de VB EN EL INICIO DEL PASO, en el óptimo del
	 * problema lineal.
	 * 
	 * TODO: ATENCIóN, POR EL MOMENTO SóLO PUEDE INVOCARSE EN EL COMPORTAMIENTO
	 * GLOBAL HIPERPLANOS
	 * 
	 * TODO: ATENCIóN, SOLO PUEDE INVOCARSE SI EL PROBLEMA LINEAL SE HA RESUELTO EN
	 * EL AMBIENTE DE LA OPTIMIZACIóN, PORQUE USA METODOS QUE SOLO FUNCIONAN PARA
	 * LA OPTIMIZACIóN.
	 * 
	 * Se supone que las VE tienen los valores del estado inicial con los que se
	 * hizo el despacho.
	 * 
	 * @param salidaPL         resultado del problema lineal
	 * @param aportantesEstado lista de los participantes y PE que aportan VE al
	 *                         problema.
	 * @param resoptim
	 * @param valorBellman     es el valor de Bellman en el punto óptimo
	 * @param numeroHip        es el número con que se crea el hiperplano
	 * @return
	 */
	private Hiperplano construyeHiperplanoOptimo(ArrayList<AportanteEstado> aportantesEstado, ResOptim resoptim,
			DatosSalidaProblemaLineal salidaPL, double valorBellman) {
		int paso = resoptim.getPasoCorriente();
		ResOptimHiperplanos roptimH = (ResOptimHiperplanos) resoptim;
		Hiperplano hip = new Hiperplano(resoptim.getCantVECont(), paso, roptimH.getContadorHiperplanos()[paso] + 1);
		for (int i = 0; i < resoptim.getCantVECont(); i++) {
			hip.getPunto()[i] = resoptim.getVarsEstadoContinuas().get(i).getEstado();
		}
		roptimH.getContadorHiperplanos()[paso]++;
		hip.setvBellman(valorBellman);
		// El hiperplano es de la forma: valor Bellman = Suma en i (coef[i] * xC[i]) +
		// tind
		// donde xC[i] es el valor en el Ã³ptimo de la i-Ã³sima variable continua
		double sumaCoefX = 0.0;
		for (AportanteEstado apE : aportantesEstado) {
			ArrayList<VariableEstado> varsEstado = apE.aportarEstadoOptimizacion();
			for (VariableEstado ve : varsEstado) {
				if (!ve.isDiscreta()) {
					// La VE es continua
					// Se descuenta desde el instante medio del paso, en el que se evaluÃ³ la
					// variable dual en el problema lineal
					// hasta el inicio del paso
					double coefHiperplano = apE.devuelveVarDualVEContinua(ve, salidaPL)
							* Math.pow(factorDescuentoPaso, 0.5);
					int ordinal = resoptim.devuelveOrdinalDeUnaVEContinua(ve.getNombre());
					hip.getCoefs()[ordinal] = coefHiperplano;
					//
					// POR EL CONTROL DISCRETO EXHAUSTIVO.
					hip.getPunto()[ordinal] = ve.getEstado();
					sumaCoefX += coefHiperplano * ve.getEstado() * Constantes.M3XHM3;					
				}
			}
			hip.setTind(valorBellman - sumaCoefX);
		}
		return hip;
	}

	/**
	 * Actualiza las variables iniciales de los participantes y sus
	 * ComportamientoSimulacion que no son variables de estado, por ejemplo caudales
	 * del paso anterior de hidrÃ³ulicos y otros datos que son necesarios para las
	 * heurÃ³sticas Cada participante tiene acceso a los valores de sus variables de
	 * estado para ser usados en las heurísticas
	 * 
	 */
	private void actualizarOtrosDatosIniciales() {		
		for (Participante p : opPaso.getCorrida().getParticipantesDirectos()) {
			p.actualizarOtrosDatosIniciales();
		}
	}

	/**
	 * Suma los aportes de todos los participantes al costo del paso corriente.
	 * 
	 * @param salidaUltimaIter
	 * @return
	 */
	private double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		double cp = 0.0;		
		for (Participante p : opPaso.getCorrida().getParticipantesDirectos()) {
			double costo = p.calculaCostoPaso(salidaUltimaIter);
			if (costo==0) continue;
			//System.out.println("costo paso " + p.getNombre() +" : " + costo);		
			cp += costo;
		}
		return cp;
	}

	/**
	 * Calcula el valor de Bellman al final de la transición en forma exacta dado
	 * el estado final de la transición. En comportamiento global INCREMENTOS
	 * obtiene el VB con una nueva interpolación dado el estado final En
	 * HIPERPLANOS trae el resultado de la variable valor del Bellman del problema
	 * lineal.
	 * 
	 * @param salidaUltimaIter
	 * @return
	 */
	private double calculaVBFinal(DatosSalidaProblemaLineal salidaUltimaIter) {
		if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
			// Carga el atributo estadoFinalOptim para los participantes que son
			// aportantes de estado
			for (AportanteEstado ae : opPaso.getCorrida().getAportantesEstado())
				ae.cargarVEfinPasoOptim(salidaUltimaIter);
			AFIncrementos aproxFuturaInc = ((ResOptimIncrementos) opPaso.getResoptim())
					.devuelveAproxEstadoFinal(opPaso.getNumpaso(), true);
			return aproxFuturaInc.getValorBellman();
		} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
			String nombreVBEnProblemaLineal = ((ConstructorHiperplanosCompDesp) opPaso.getCorrida()
					.getConstructorHiperplanos().getCompDesp()).getnVBellman();
			return salidaUltimaIter.getSolucion().get(nombreVBEnProblemaLineal);
		}
		// por acá no debe salir nunca
		return 0.0;
	}

	public OptimizadorPaso getOpPaso() {
		return opPaso;
	}

	public void setOpPaso(OptimizadorPaso opPaso) {
		this.opPaso = opPaso;
	}

	public AFIncrementos getAproxFuturaInc() {
		return aproxFuturaInc;
	}

	public void setAproxFuturaOpt(AFIncrementos aproxFuturaInc) {
		this.aproxFuturaInc = aproxFuturaInc;
	}

	/**
	 * Si el paso, estado y sorteo estÃ³n dentro del rango estipulado imprime la
	 * salida detallada de la transiciÃ³n
	 * 
	 * @param numpasoT   número de paso
	 * @param codEstado  código de enteros del estado
	 * @param isort      número de sorteo
	 * @param vBFinT
	 * @param costoPasoT costo del paso en USD, sin imputación del valor de los
	 *                   recursos
	 * @param vBIniT     valor de Bellman al inicio del paso que se obtiene en la
	 *                   transición
	 * @param proceso    La fase optimización o simulación en la que se está
	 *                   sacando la salida.
	 * 
	 */
	public void guardarResultadoEstadoOpt(int numpasoT, int[] codEstado, int[] codControlDE, int isort, double vBFinT,
			double costoPasoT, double vBIniT, DatosSalidaProblemaLineal salidaUltimaIter, String proceso,
			DatosParamSalidaOpt paramSalOpt) {

		int pasoIniSal = paramSalOpt.getPasoIni();
		int pasoFinSal = paramSalOpt.getPasoFin();
		int sortIniSal = paramSalOpt.getSortIni();
		int sortFinSal = paramSalOpt.getSortFin();
		int[] estIniSal = paramSalOpt.getEstadoIni();
		int[] estFinSal = paramSalOpt.getEstadoFin();

		boolean guarda = numpasoT >= pasoIniSal & numpasoT <= pasoFinSal & isort >= sortIniSal & isort <= sortFinSal;
		int ive = 0;

		while (guarda == true & ive < codEstado.length) {
			guarda = guarda & (codEstado[ive] >= estIniSal[ive]) & (codEstado[ive] <= estFinSal[ive]);
			ive++;
		}
		if (guarda) {
			PasoTiempo pt = opPaso.getPasoActual();

			LineaTiempo lt = opPaso.getCorrida().getLineaTiempo();
			long instIni = opPaso.getPasoActual().getInstanteInicial();
			long instFin = opPaso.getPasoActual().getInstanteFinal();
			String fechaIni = lt.fechaYHoraDeInstante(instIni);
			String fechaFin = lt.fechaYHoraDeInstante(instFin);

			DatosPaso dpaso = new DatosPaso(-1, numpasoT, opPaso.getDuracionPaso(), opPaso.getCantidadPostes(),
					opPaso.getInstantesMuestreo().length, opPaso.getPostPaso().getNumpos(), opPaso.getDuracionPostes(),
					fechaIni, fechaFin);
			DatosSalidaPaso resultadoPaso = new DatosSalidaPaso();
			resultadoPaso.setPaso(dpaso);
			ArrayList<Participante> redes = opPaso.getCorrida().getRedes();

			for (Participante p : redes) {
				p.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, pt.getInstanteInicial());
			}

			Enumeration<Impacto> enumeration = opPaso.getCorrida().getImpactos().elements();
			while (enumeration.hasMoreElements()) {
				Impacto i = enumeration.nextElement();
				i.guardarResultadoPaso(resultadoPaso, salidaUltimaIter, proceso, pt.getInstanteInicial());

			}

			if (opPaso.getCorrida().getConstructorHiperplanos() != null)
				opPaso.getCorrida().getConstructorHiperplanos().guardarResultadoPaso(resultadoPaso, salidaUltimaIter,
						proceso, pt.getInstanteInicial());

			/**
			 * ATENCION: SE COMENTA O DESCOMENTA LA SALIDA DEL ARCHIVO DETALLADO POR
			 * PASO
			 */
			StringBuilder sb = new StringBuilder();
			sb.append("CODIGO ESTADO ");
			for (ive = 0; ive < codEstado.length; ive++) {
				sb.append(codEstado[ive] + " ");
			}
			sb.append("\n");
			ArrayList<VariableEstado> varsEst = opPaso.getResoptim().getVarsEstadoCorrientes();
			sb.append("Estado inicial\t");
			for (VariableEstado ve : varsEst) {
				sb.append(ve.getEstado() + "\t");
			}
			sb.append("\n");
			sb.append("Estado final\t");
			for (VariableEstado ve : varsEst) {
				sb.append(ve.getEstadoFinalOptim() + "\t");
			}
			sb.append("\n");
			sb.append("CODIGO CONTROL ");
			if (codControlDE != null) {
				for (int ic = 0; ic < codControlDE.length; ic++) {
					sb.append(codControlDE[ic] + " ");
				}
			} else {
				sb.append("NO HAY VC ACTIVAS");
			}
			sb.append("\n");
			sb.append("Sorteo \t" + isort);
			sb.append("\n");
			sb.append("V.Bellman al fin del paso t\t" + vBFinT);
			sb.append("\n");
			sb.append("Costo del paso t\t" + costoPasoT);
			sb.append("\n");
			sb.append("V.Bellman al inicio del paso t\t" + vBIniT);
			sb.append("\n");

			DirectoriosYArchivos.agregaTexto(opPaso.getDirSalidas() + "/SalidaDetalladaOPT.xlt", sb.toString());
			DirectoriosYArchivos.agregaTexto(opPaso.getDirSalidas() + "/SalidaDetalladaOPT.xlt",
					escritor.escribeSalidaPaso(resultadoPaso));

		}
	}

	public String getCompGlobalBellman() {
		return compGlobalBellman;
	}

	public void setCompGlobalBellman(String compGlobalBellman) {
		this.compGlobalBellman = compGlobalBellman;
	}

}
