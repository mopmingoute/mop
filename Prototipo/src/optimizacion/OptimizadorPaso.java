/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * OptimizadorPaso is part of MOP.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import compgeneral.CompGeneral;
import control.VariableControl;
import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosPEsUnPaso;
import estado.VariableEstado;
import futuro.ClaveDiscreta;
import futuro.Hiperplano;
import futuro.InformacionValorPunto;
import futuro.TablaHiperplanos;
import futuro.TablaHiperplanosMemoria;
import futuro.TablaVByValRecursos;
import futuro.TablaVByValRecursosMemoria;
import futuro.TablaVByValRecursosRedis;
import interfacesParticipantes.AportanteControlDE;
import interfacesParticipantes.AportanteEstado;
import interfacesParticipantes.AportantePost;
import logica.CorridaHandler;
import logica.Despachador;
import parque.Azar;
import parque.Corrida;
import parque.GeneradorHidraulico;
import parque.Participante;
import pizarron.ClienteHandler;
import pizarron.Paquete;
import pizarron.PizarronRedis;
import pizarron.ServidorHandler;
import procesosEstocasticos.DiscretoExhaustivo;
import procesosEstocasticos.ProcesadorSorteosOptimPEs;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import simulacion.PostizacionPaso;
import simulacion.Postizador;
import simulacion.ValPostizador;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.EnumeradorLexicografico;

public class OptimizadorPaso implements Optimizable {

	private Corrida corrida;
	private int numpaso;
	private OptimizadorEstado opEst;
	private PasoTiempo pasoActual;
	private Postizador postizador;
	private PostizacionPaso postPaso;
	private ValPostizador valPostizador;
	private String dirSalidas;
	private String nombreArchInfactible;
	private String nombreArchEntradasPL;
	private String nombreArchSalidasPL;

	private boolean salidaVB; // true si se quiere que se imprima la tabla de valores de Bellman

	// Lista de VE de las que se piden tablas de salida
	private ArrayList<VariableEstado> varsSalidasRecursos;

	private Integer cantidadPostes;
	/** Cantidad de postes a considerar en cada paso */
	private Integer duracionPaso;
	/** DuraciÃ³n en segundos del paso */
	private int[] duracionPostes;
	/** DuraciÃ³n en segundos de cada poste del paso */

	// private AFIncrementos aproxFuturaOpt;
	// private AFHiperplanos aproxFuturaHiperplanos;

	/**
	 * Incluye todas las variables de estado activas en el paso (las variables de
	 * estado de PE tambiÃ³n)
	 */
	private ArrayList<VariableEstado> varsEstadoOptimizacion;

	private long[] instantesMuestreo;

	/**
	 * VARIABLES DE COMPORTAMIENTO GLOBAL
	 */
	private Hashtable<String, String> valsCompGlobal;

	/**
	 * Referencias al resoptim casteadas segÃ³n el comportamiento global
	 */
	private ResOptimIncrementos roptimI;
	private ResOptimHiperplanos roptimH;

	private ClienteHandler clienteH;
	private ServidorHandler servidorH;

	/**
	 * Debido a que el conjunto de variables de estado se recalcula al fin de un
	 * paso y se mantiene vigente para todo el paso actual la dimensiÃ³n de las
	 * colecciones estadoPostSorteo y estadoPreSorteo coinciden
	 */

	// private ArrayList<VariableEstado> varsEstadoPEDE;
	// private ArrayList<VariableEstado> varsEstadoPENODE;
	private ArrayList<VariableControl> varsControlActivas; // Incluye las DE
	private ArrayList<VariableControlDE> varsControlDEActivas;
	private int cantVarsControlDE;

	private Azar azar;
	private Despachador despachador;
	private DatosSalidaProblemaLineal salidaUltimaIter;
	private double tasaDescuentoAnual;
	private int cantSortMontecarlo; // Cantidad de sorteos Montecarlo de cada
									// transiciÃ³n
	private ResOptim resoptim;

	/**
	 * Estructura provisoria usada en el comportamiento Incrementos y Derivadas
	 * parciales que guarda sÃ³lo para el paso corriente los valores de Bellman al
	 * inicio del paso t, despuÃ³s del salto de la VE de los procesos estocÃ³sticos
	 * DE entre fin t-1 e inicio t. Es una TablaVByValRecursos de un solo paso de
	 * tamaÃ³o
	 */
	private TablaVByValRecursos vBellmanIniT;

	/**
	 * Estructura provisoria usada en el comportamiento Hiperplanos que guarda sÃ³lo
	 * para el paso corriente los hiperplanos al inicio del paso t, despuÃ³s del
	 * salto de la VE. Es una TablaHiperplanos de un solo paso de tamaÃ³o.
	 */
	private TablaHiperplanos hipersIniT;

	/**
	 * Estructura provisoria usada en el comportamiento Hiperplanos que guarda sÃ³lo
	 * para el paso corriente los hiperplanos al fin del paso t-1, antes del salto
	 * de la VE. Es una TablaHiperplanos de un solo paso de tamaÃ³o. A partir de
	 * esta tabla, proyectando sobre las VE de PE DE, se obtienen los conjuntos de
	 * hiperplanos para cada clave de las VE PE DE.
	 */
	// private TablaHiperplanos hipersFinTmenos1;

	/**
	 * Variables de estado activas en el paso (las variables de estado de PE DE
	 * tambiÃ³n)
	 */

	/**
	 * Variables de estado activas en el paso posterior en el tiempo
	 */
	private ArrayList<VariableEstado> varsEstadoPasoPosterior;

	/**
	 * Dado el nombre de una variable de estado devuelve el ordinal en
	 * varsEstadoOptimizacion de esa variable de estado
	 */
	private Hashtable<String, Integer> ordinalVEEnVarsEstado;

	private Hashtable<String, Integer> ordinalVEPasoPosterior;

	/**
	 * Common random numbers: es la tÃ³cnica de reducciÃ³n de varianza por la que se
	 * usan los mismos nÃ³meros aleatorios para estimar diferencias de una VA al
	 * variar un parÃ³metro del sistema. Se usa acÃ³ para estimar las diferencias de
	 * valores de Bellman.
	 * 
	 * Los procesos estocÃ³sticos se dividen en:
	 * 
	 * - Los PE discretos exhaustivos, que por lo tanto no tienen sorteos Montecarlo
	 * 
	 * - Los PE no discretos exhaustivos que por lo tanto tienen sorteos Montecarlo
	 * y que no aportan VE a la optimizaciÃ³n (si bien pueden tener sus propias VE).
	 * peMontNoEstadoOptim Para las VA de estos procesos se sortean valores comunes
	 * a todas las realizaciones Montecarlo.
	 * 
	 * - Los PE no discretos exhaustivos que por lo tanto tienen sorteos Montecarlo
	 * y que SI APORTAN VE a la optimizaciÃ³n. Para las VA de estos procesos se
	 * toman innovaciones aleatorias comunes peMontEstadoOptim
	 * 
	 * 
	 */

	/**
	 * Lista de los PE discretos exhaustivos, cada uno tiene un Ã³nica VE
	 */
	private ArrayList<DiscretoExhaustivo> procesosEstDE;

	/**
	 * Lista de PE que tienen sorteos Montecarlo y que no aportan estado en la
	 * optimizaciÃ³n, o no tienen estado en absoluto. En los montecarlos las VA
	 * aleatorias asociadas pueden sortearse por afuera del loop en la grilla de
	 * estados
	 */
	private ArrayList<ProcesoEstocastico> peMontNoEstadoOptim;

	/**
	 * Lista de los PE que tienen sorteos Montecarlo y que tienen estado en la
	 * optimizaciÃ³n, es decir no son discretos exhaustivos. En los montecarlos las
	 * VA debe generarse dentro del loop en la grilla de estados, teniendo en cuenta
	 * el valor inicial de la VE del proceso.
	 */
	private ArrayList<ProcesoEstocastico> peMontEstadoOptim;

	/**
	 * Tabla que almacena el ordinal empezando de cero de la primera innovaciÃ³n
	 * correspondiente a un PE en el vector de innovaciones a usar en el sorteo de
	 * los PE La clave es el nombre del PE El valor es ordinal empezando de cero en
	 * el vector de innovaciones
	 */
	private Hashtable<String, Integer> ordinalInnovacionesPE;

	/**
	 * Tabla la cantidad de innovaciones de cada PE
	 */
	private Hashtable<String, Integer> cantInnovacionesPE;

	private double[][] innovaciones;

	private long instIniPaso;
	private long instFinPaso;

	private boolean optimizando; // es true solo cuando se estÃ³ en la optimizaciÃ³n clÃ³sica
	public Hashtable<Integer, PostizacionPaso> postizacionesPaso;

	public OptimizadorPaso(Corrida corridaActual) {
		this.optimizando = false;
		this.numpaso = 0;
		this.corrida = corridaActual;
		this.ordinalVEEnVarsEstado = new Hashtable<String, Integer>();
		this.postizador = new Postizador(corridaActual.getCantidadPasos(), corridaActual.isPostizacionExterna());
		this.valPostizador = new ValPostizador(corrida.dameAzar().getSemillaGeneral(),
				corrida.dameAzar().getInicioSorteos(), corrida.dameAzar().getInicioCorrida());
		if (!corridaActual.isValPostizacionExterna())
			valPostizador.setExterna(false);
		this.despachador = new Despachador();
		this.tasaDescuentoAnual = this.corrida.getTasa();
		this.varsControlActivas = new ArrayList<VariableControl>();
		this.varsControlDEActivas = new ArrayList<VariableControlDE>();
		this.peMontNoEstadoOptim = new ArrayList<ProcesoEstocastico>();
		this.peMontEstadoOptim = new ArrayList<ProcesoEstocastico>();
		this.procesosEstDE = new ArrayList<DiscretoExhaustivo>();
		this.ordinalVEPasoPosterior = new Hashtable<String, Integer>();
		this.varsEstadoPasoPosterior = new ArrayList<VariableEstado>();
		this.varsSalidasRecursos = new ArrayList<VariableEstado>();
		this.varsEstadoOptimizacion = new ArrayList<VariableEstado>();
//		this.clienteH = ClienteHandler.getInstance();
//		this.servidorH = ServidorHandler.getInstance();
		CorridaHandler ch = CorridaHandler.getInstance();
		boolean paralelo = ch.isParalelo();
		if (paralelo) {
			this.clienteH = ClienteHandler.getInstance();
			this.servidorH = ServidorHandler.getInstance();
		}
		this.postizacionesPaso = new Hashtable<Integer, PostizacionPaso>();
	}

	public void optimizarPaso() {
		Optimizador.prof.iniciarContador("MONO: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
		String compGlobalBellman = corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());
		numpaso = corrida.getLineaTiempo().getNumPaso();
		long instanteIniPaso = pasoActual.getInstanteInicial();
		valsCompGlobal = corrida.dameValsCompGlobal(pasoActual.getInstanteInicial());
		CompGeneral.setCompsGlobales(valsCompGlobal);
		int cantPostes = pasoActual.getBloque().getCantPostes();
		valPostizador.sortearUnifPostes(cantPostes);

		/**
		 * Actualiza la foto de las variables de comportamiento general que se
		 * encuentran en el Comportamiento SimulaciÃ³n
		 */
		for (Participante p : corrida.getParticipantes())
			p.actualizarVarsCompGeneral(pasoActual.getInstanteInicial());

		Hashtable<String, String> cglob = new Hashtable<String, String>();
		Set<String> keyset = corrida.getCompGlobales().keySet();
		Iterator<String> it = keyset.iterator();

		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();

		while (it.hasNext()) {
			String clave = it.next();
			cglob.put(clave, corrida.getCompGlobales().get(clave).getValor(instanteActual));
		}
		for (Participante p : corrida.getParticipantes())
			p.actualizarVariablesCompGlobal(cglob);

		if (corrida.getLineaTiempo().esPasoFinal()) {

			/**
			 * Cada participante contribuye con sus variables de estado a la colecciÃ³n que
			 * estÃ³ en ComportamientoGeneral
			 */

			// Actualiza en el optimizable las variables de estado que rigen en el
			// paso t-1
			actualizarVarEstado();

			// Actualiza variables de control DE que estÃ³n activas dado el comportamiento
			// general y el perÃ³odo de la variable
			actualizarVarsControlDE(instanteIniPaso, pasoActual.getDuracionPaso());
			cantVarsControlDE = varsControlDEActivas.size();

			/**
			 * Se actualiza la informaciÃ³n sobre variables de estado del ResOptim para
			 * usarlo en un paso. TODO: ESTO SE DEBERÃ³ HACER SÃ³LO CUANDO CAMBIAN LOS
			 * COMPORTAMIENTOS O LOS PARTICIPANTES
			 */
			resoptim.inicializaResOptimParaNuevoPaso(numpaso, pasoActual.getInstanteInicial(), varsEstadoOptimizacion,
					varsControlDEActivas);

			/**
			 * Lo siguiente sÃ³lo puede hacerse despuÃ³s de haber inicializado el resoptim
			 * para las variables de estado corrientes, de otro modo no se conocerÃ³an los
			 * estados.
			 */
			if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				roptimI = (ResOptimIncrementos) resoptim;
				roptimI.cargaVBFinales();
				roptimI.calculaDerivadasParcEIncrementos(numpaso);
			} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
				roptimH = (ResOptimHiperplanos) resoptim;
				roptimH.cargaHiperplanosFinales();
				String titulo = "HIPERPLANOS AL FIN DEL PASO ";
				String hips = roptimH.publicaHiperplanosPorVEDiscretas(numpaso, numpaso, titulo,
						roptimH.getTablaHiperplanos());
				String dirHips = dirSalidas + "/HiperplanosFinT.xlt";
				DirectoriosYArchivos.agregaTexto(dirHips, hips);
			}
		}

		// TODO:Suponemos inicalmente que las variables de estado sÃ³lo pueden
		// desaparecer al avanzar el tiempo (menor detalle)

		// vBellmanIniT es una tabla auxiliar de un paso para procesar el salto de las
		// VE PE DE en el
		// comportamiento Incrementos y DerivadasParciales

		if (CorridaHandler.getInstance().isParalelo()) {
			vBellmanIniT = new TablaVByValRecursosRedis(1);
		} else {
			vBellmanIniT = new TablaVByValRecursosMemoria(1);
		}

		// hipersIniT e hipersFinT son tabla auxiliares de un paso para procesar el
		// salto de las VE PE DE
		// en el comportamiento Hiperplanos
		hipersIniT = new TablaHiperplanosMemoria(1);
		// hipersFinTmenos1 = new TablaHiperplanosMemoria(1);

		resoptim.getEnumLexEstados().inicializaEnum();
		int[] codigoEstado = resoptim.devuelveCodigoEstadoDeLaGrilla();
		Optimizador.prof.pausarContador("MONO: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
		while (codigoEstado != null) {
			Optimizador.prof.iniciarContador("MONO: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
			if (Constantes.NIVEL_CONSOLA > 1) {
				StringBuilder sb = new StringBuilder();
				sb.append("Paso " + numpaso);
				sb.append(" - Comienza estado ");
				for (int icod = 0; icod < codigoEstado.length; icod++) {
					sb.append(codigoEstado[icod]);
					sb.append("-");
				}
				System.out.println(sb.toString());
			}

			// carga los valores de las VE corrientes para buscar segÃ³n el
			// cÃ³digo de enteros

			actualizarValoresVEDiscretizacionesVariables(this.pasoActual.getInstanteInicial());

			cargarValoresVEParaUnPaso(codigoEstado);

			/**
			 * Se crea cada vez el OptimizadorEstado anticipando la paralelizaciÃ³n
			 */

			OptimizadorEstado opEst = new OptimizadorEstado(this);

			// // Crear el enumerador de controles discretos exhaustivos si existen esos
			// controles
			// if(cantVarsControlDE>0){
			// EnumeradorLexicografico enumCDE = creaEnumeradorControlesDE(instanteIniPaso);
			// opEst.setEnumCDE(enumCDE);
			// }

			for (Participante p : this.getCorrida().getParticipantes()) {
				p.setOptimEstado(opEst);
			}

			/**
			 * Calcula una transiciÃ³n de la programaciÃ³n dinÃ³mica Obtiene del
			 * OptimizadorEstado el hiperplano en el punto del estado discreto, que incluye
			 * el valor de Bellman al inicio de t en el estado definido por codigoEstado Si
			 * el comportamiento global es Hiperplanos, tambiÃ³n se cargan los coeficientes
			 * del hiperplano que son las derivadas parciales del VB respecto a las
			 * variables de estado continuas.
			 * 
			 * ATENCIÃ³N: clave es una clave discreta de TODAS LAS VARIABLES DE ESTADO. EN
			 * hipersIniT HAY UN SOLO HIPERPLANO POR clave, EL HIPERPLANO Ã³PTIMO EN ESE
			 * PUNTO DEL ESPACIO DE ESTADOS TOTAL.
			 */
			Optimizador.prof.pausarContador("MONO: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
			Optimizador.prof.iniciarContador("MONO: TOTAL OPTIMIZAR ESTADO");
			Hiperplano hipOpt = opEst.optimizarEstado(codigoEstado);
//			System.out.println("NUMPOS: " + this.getPostPaso().getNumpos());
			Optimizador.prof.pausarContador("MONO: TOTAL OPTIMIZAR ESTADO");
			Optimizador.prof.iniciarContador("MONO: OPTIMIZAR PASO DESPUES OPTIMIZAR ESTADO");
			double valorEstado = hipOpt.getvBellman();
			// System.out.println("VBELLMAN AUX ESTADO: " + Arrays.toString(codigoEstado) +
			// " VALOR: " + valorEstado);

			// Almacena el valor en la tabla provisoria vBellmanIniT o hipersIniT segÃ³n el
			// comportamiento
			ClaveDiscreta clave = new ClaveDiscreta(codigoEstado);

			if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				InformacionValorPunto ivp = new InformacionValorPunto();
				ivp.setValorVB(valorEstado);
				vBellmanIniT.cargaInfoValoresPunto(0, clave, ivp); // la tabla tiene un sÃ³lo paso,el 0
			} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
				/**
				 * ATENCIÃ³N: clave ES LA CLAVE TOTAL DE ESTADOS DISCRETOS Y CONTINUOS
				 */
				ClaveDiscreta claveVEDis = roptimH.claveVEDiscretasDeClaveTotal(clave);
				hipersIniT.cargaHiperplano(0, claveVEDis, clave, hipOpt);
			}

			// continÃ³a la iteraciÃ³n en los estados
			codigoEstado = resoptim.devuelveCodigoEstadoDeLaGrilla();
			Optimizador.prof.pausarContador("MONO: OPTIMIZAR PASO DESPUES OPTIMIZAR ESTADO");
		}

	}

	private void actualizarValoresVEDiscretizacionesVariables(long instante) {
		for (AportanteEstado p : corrida.getAportantesEstado()) {
			p.actualizaValoresVEDiscretizacionesVariables(instante);
		}

	}

	/**
	 * Es empleado sÃ³lo en las pruebas de los procesos estocÃ¡sticos, para producir
	 * los sorteos de las VA que se usarian en la optimizaciÃ³n real
	 */
	public DatosPEsUnPaso sortearPEsOptimPaso(ProcesadorSorteosOptimPEs procSorteos) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		String compGlobalBellman = corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());
		numpaso = corrida.getLineaTiempo().getNumPaso();
		long instanteIniPaso = pasoActual.getInstanteInicial();
		valsCompGlobal = corrida.dameValsCompGlobal(pasoActual.getInstanteInicial());
		CompGeneral.setCompsGlobales(valsCompGlobal);
		int cantPostes = pasoActual.getBloque().getCantPostes();
		valPostizador.sortearUnifPostes(cantPostes);

		/**
		 * Actualiza la foto de las variables de comportamiento general que se
		 * encuentran en el Comportamiento SimulaciÃ³n
		 */
		for (Participante p : corrida.getParticipantes())
			p.actualizarVarsCompGeneral(pasoActual.getInstanteInicial());

		Hashtable<String, String> cglob = new Hashtable<String, String>();
		Set<String> keyset = corrida.getCompGlobales().keySet();
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String clave = it.next();
			cglob.put(clave, corrida.getCompGlobales().get(clave).getValor(instanteActual));
		}
		for (Participante p : corrida.getParticipantes())
			p.actualizarVariablesCompGlobal(cglob);

		if (corrida.getLineaTiempo().esPasoFinal()) {

			/**
			 * Cada participante contribuye con sus variables de estado a la colecciÃ³n que
			 * estÃ³ en ComportamientoGeneral
			 */

			// Actualiza en el optimizable las variables de estado que rigen en el
			// paso t-1
			actualizarVarEstado();

			// Actualiza variables de control DE que estÃ³n activas dado el comportamiento
			// general y el perÃ³odo de la variable
			actualizarVarsControlDE(instanteIniPaso, pasoActual.getDuracionPaso());
			cantVarsControlDE = varsControlDEActivas.size();

			/**
			 * Se actualiza la informaciÃ³n sobre variables de estado del ResOptim para
			 * usarlo en un paso. TODO: ESTO SE DEBERÃ³ HACER SÃ³LO CUANDO CAMBIAN LOS
			 * COMPORTAMIENTOS O LOS PARTICIPANTES
			 */
			resoptim.inicializaResOptimParaNuevoPaso(numpaso, pasoActual.getInstanteInicial(), varsEstadoOptimizacion,
					varsControlDEActivas);

		}

		Azar azar = this.getAzar();

		ArrayList<ProcesoEstocastico> listaPEs = procSorteos.getListaPEs();
		ArrayList<String> nombresPEyVA = new ArrayList<String>();

		for (ProcesoEstocastico pe : listaPEs) {
			for (VariableAleatoria va : pe.getVariablesAleatorias()) {
				String nom = ProcesadorSorteosOptimPEs.clavePEVA(pe, va);
				nombresPEyVA.add(nom);
			}
		}

		DatosPEsUnPaso resultPaso = new DatosPEsUnPaso(numpaso, nombresPEyVA);

		// Limita la creaciÃ³n de estados en el enumerador lexicogrÃ¡fico
		// a las variaciones de las VE de los procesos de listaPEs
		// De las otras VE se toma el estado de indice 0
		int ive = 0;
		for (VariableEstado ve : resoptim.getVarsEstadoCorrientes()) {
			boolean esta = false;
			for (ProcesoEstocastico pe : listaPEs) {
				for (VariableEstado ve2 : pe.getVarsEstado()) {
					if (ve == ve2)
						esta = true;
				}
			}
			if (!esta)
				resoptim.getEnumLexEstados().getCotasSuperiores()[ive] = resoptim.getEnumLexEstados()
						.getCotasInferiores()[ive];
			ive++;
		}

		resoptim.getEnumLexEstados().inicializaEnum();
		int[] codigoEstado = resoptim.devuelveCodigoEstadoDeLaGrilla();
		while (codigoEstado != null) {

			cargarValoresVEParaUnPaso(codigoEstado);

			OptimizadorEstado opEst = new OptimizadorEstado(this);

			for (Participante p : this.getCorrida().getParticipantes()) {
				p.setOptimEstado(opEst);
			}

			// valor: primer Ã­ndice sorteo, segundo indice intervalo de muestreo
			Hashtable<String, double[][]> resultadosPEsUnEstado = opEst.sortearPEsOptimEstado(codigoEstado, listaPEs);

			// Agrega los resultados del estado a los del paso
			for (ProcesoEstocastico pe : listaPEs) {
				for (VariableAleatoria va : pe.getVariablesAleatorias()) {
					String clave = ProcesadorSorteosOptimPEs.clavePEVA(pe, va);
					resultPaso.getValores().get(clave).add(resultadosPEsUnEstado.get(clave));
				}
			}

			// Agrega el estado procesado
			resultPaso.getEstados().add(codigoEstado);

			codigoEstado = resoptim.devuelveCodigoEstadoDeLaGrilla();
		}

		return resultPaso;

	}

	public void optimizarPasoServidor() {
		Optimizador.prof.iniciarContador("SERV: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
		String compGlobalBellman = corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());

		numpaso = corrida.getLineaTiempo().getNumPaso();
		long instanteIniPaso = pasoActual.getInstanteInicial();
		valsCompGlobal = corrida.dameValsCompGlobal(pasoActual.getInstanteInicial());
		CompGeneral.setCompsGlobales(valsCompGlobal);
		int cantPostes = pasoActual.getBloque().getCantPostes();
		valPostizador.sortearUnifPostes(cantPostes);

		/**
		 * Actualiza la foto de las variables de comportamiento general que se
		 * encuentran en el Comportamiento SimulaciÃ³n
		 */
		for (Participante p : corrida.getParticipantes())
			p.actualizarVarsCompGeneral(pasoActual.getInstanteInicial());
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		Hashtable<String, String> cglob = new Hashtable<String, String>();
		Set<String> keyset = corrida.getCompGlobales().keySet();
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String clave = it.next();
			cglob.put(clave, corrida.getCompGlobales().get(clave).getValor(instanteActual));
		}
		for (Participante p : corrida.getParticipantes())
			p.actualizarVariablesCompGlobal(cglob);

		if (corrida.getLineaTiempo().esPasoFinal()) {

			/**
			 * Cada participante contribuye con sus variables de estado a la colecciÃ³n que
			 * estÃ³ en ComportamientoGeneral
			 */

			// Actualiza en el optimizable las variables de estado que rigen en el
			// paso t-1
			actualizarVarEstado();

			// Actualiza variables de control DE
			actualizarVarsControlDE(instanteIniPaso, duracionPaso);
			cantVarsControlDE = varsControlDEActivas.size();

			/**
			 * Se actualiza la informaciÃ³n sobre variables de estado del ResOptim para
			 * usarlo en un paso. TODO: ESTO SE DEBERÃ³ HACER SÃ³LO CUANDO CAMBIAN LOS
			 * COMPORTAMIENTOS O LOS PARTICIPANTES
			 */
			resoptim.inicializaResOptimParaNuevoPaso(numpaso, pasoActual.getInstanteInicial(), varsEstadoOptimizacion,
					varsControlDEActivas);

			if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				roptimI = (ResOptimIncrementos) resoptim;
				roptimI.cargaVBFinales();
				roptimI.calculaDerivadasParcEIncrementos(numpaso);
			} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
				roptimH = (ResOptimHiperplanos) resoptim;
				roptimH.cargaHiperplanosFinales();
				String hips = roptimH.publicaHiperplanosPorVEDiscretas(numpaso, numpaso, "HIP",
						roptimH.getTablaHiperplanos());
				String dirHips = dirSalidas + "/HiperplanosFinT.xlt";
				DirectoriosYArchivos.agregaTexto(dirHips, hips);
			}

		} else {
			if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				roptimI = (ResOptimIncrementos) resoptim;
				roptimI.obtenerTabla(numpaso);
			//	roptimI.getTablaControlesDE().devuelveTablaEnteraDE(numpaso);
				roptimI.calculaDerivadasParcEIncrementos(numpaso);
			}
		}

		// TODO:Suponemos inicalmente que las variables de estado sÃ³lo pueden
		// desaparecer al avanzar el tiempo (menor detalle)

		// vBellmanIniT es una tabla auxiliar de un paso para procesar el salto de las
		// VE PE DE en el
		// comportamiento Incrementos y DerivadasParciales

		// hipersIniT e hipersFinT son tabla auxiliares de un paso para procesar el
		// salto de las VE PE DE
		// en el comportamiento Hiperplanos
		hipersIniT = new TablaHiperplanosMemoria(1);
		// hipersFinTmenos1 = new TablaHiperplanosMemoria(1);
		OptimizadorEstado opEst = new OptimizadorEstado(this);
		resoptim.getEnumLexEstados().inicializaEnum();

		boolean termine = false;
		Optimizador.prof.pausarContador("SERV: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
		boolean cambioPaso = false;

		String nroMaquina = System.getenv().get("COMPUTERNAME");

		while (!termine && numpaso == servidorH.obtenerPasoOptim()) {
			Optimizador.prof.iniciarContador("SERV: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
			// try {
			// Thread.sleep(1);
			termine = servidorH.hayQueFinalizarOptimizacion();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			if (termine)
				break;
			Paquete paq = servidorH.obtenerPaqueteAResolver(numpaso);

			if (paq == null) {

				// DirectoriosYArchivos.agregaTexto(Constantes.ruta_log_paralelismo +
				// "logServidor"
				// + ManagementFactory.getRuntimeMXBean().getName() + ".txt", "PAQUETE NULO ");

				continue;
			}

			paq.setNroMaquina(nroMaquina);

			paq.setInstanteTiempoRecibido(System.currentTimeMillis());// cargar tiempo en que recibo el paquete

			servidorH.pasarPaqueteAEnResolucion(paq);

			if (CorridaHandler.getInstance().isParalelo()) {
				vBellmanIniT = new TablaVByValRecursosRedis(1);
			} else {
				vBellmanIniT = new TablaVByValRecursosMemoria(1);
			}

			Optimizador.prof.pausarContador("SERV: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
			for (int estado = paq.getEstadoIni(); estado < paq.getEstadoFin(); estado++) {
				Optimizador.prof.iniciarContador("SERV: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
				int[] codigoEstado = resoptim.getEnumLexEstados().devuelveVectorDeOrdinal(estado);

				cargarValoresVEParaUnPaso(codigoEstado);

				if (cantVarsControlDE > 0) {
					EnumeradorLexicografico enumCDE = creaEnumeradorControlesDE(instanteIniPaso);
					opEst.setEnumCDE(enumCDE);
				}

				for (Participante p : this.getCorrida().getParticipantes()) {
					p.setOptimEstado(opEst);
				}
				Optimizador.prof.pausarContador("SERV: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
				Optimizador.prof.iniciarContador("SERV: TOTAL OPTIMIZAR ESTADO");
				Hiperplano hipOpt = opEst.optimizarEstado(codigoEstado);
				Optimizador.prof.pausarContador("SERV: TOTAL OPTIMIZAR ESTADO");
				Optimizador.prof.iniciarContador("SERV: OPTIMIZAR PASO DESPUÃ‰S OPTIMIZAR ESTADO");
				double valorEstado = hipOpt.getvBellman();

				ClaveDiscreta clave = new ClaveDiscreta(codigoEstado);

				if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
					InformacionValorPunto ivp = new InformacionValorPunto();
					ivp.setValorVB(valorEstado);

					// Optimizador.prof.iniciarContador("escrituraVB");
					vBellmanIniT.cargaInfoValoresPuntoAuxiliar(0, clave, ivp);
					// Optimizador.prof.pausarContador("escrituraVB");
				} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
					/**
					 * 
					 * HIPERPLANOS NO IMPLEMENTADO EN PARALELO
					 * 
					 */
					ClaveDiscreta claveVEDis = roptimH.claveVEDiscretasDeClaveTotal(clave);
					hipersIniT.cargaHiperplano(0, claveVEDis, clave, hipOpt);
				}
				// DirectoriosYArchivos
				// .agregaTexto(
				// Constantes.ruta_log_paralelismo + "logServidor"
				// + ManagementFactory.getRuntimeMXBean().getName() + ".txt",
				// "PASO: " + numpaso + "PAQUETE RESUELTO: " + paq.getClave());
				Optimizador.prof.pausarContador("SERV: OPTIMIZAR PASO DESPUÃ‰S OPTIMIZAR ESTADO");
			}
			Optimizador.prof.iniciarContador("SERV: OPTIMIZAR PASO DESPUÃ‰S OPTIMIZAR ESTADO");
			if (vBellmanIniT.compatiblePaquete(paq, resoptim)) {
				vBellmanIniT.cargaTablaAuxiliar(numpaso);
			//	resoptim.getTablaControlesDE().cargaTabla(numpaso);
				paq.setTiempoResolusionServidor(System.currentTimeMillis() - paq.getInstanteTiempoRecibido()); // tiempo
																												// en
																												// que
																												// termina
																												// el
																												// paquete.
				servidorH.pasarPaqueteAResuelto(paq);
			} else {
				System.out.println("SERV: TABLA MAL ARMADA");
				if (CorridaHandler.getInstance().isParalelo()) {
					// PizarronRedis pp = new PizarronRedis();
					// pp.matarServidores();
				}
				System.exit(1);
			}

			Optimizador.prof.pausarContador("SERV: OPTIMIZAR PASO DESPUÃ‰S OPTIMIZAR ESTADO");

		}

	}

	public void optimizarPasoCliente() {
		Optimizador.prof.iniciarContador("CLI: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
		String compGlobalBellman = corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());
		numpaso = corrida.getLineaTiempo().getNumPaso();
		long instanteIniPaso = pasoActual.getInstanteInicial();
		valsCompGlobal = corrida.dameValsCompGlobal(pasoActual.getInstanteInicial());
		CompGeneral.setCompsGlobales(valsCompGlobal);
		int cantPostes = pasoActual.getBloque().getCantPostes();
		valPostizador.sortearUnifPostes(cantPostes);

		/**
		 * Actualiza la foto de las variables de comportamiento general que se
		 * encuentran en el Comportamiento SimulaciÃ³n
		 */
		for (Participante p : corrida.getParticipantes())
			p.actualizarVarsCompGeneral(pasoActual.getInstanteInicial());
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		Hashtable<String, String> cglob = new Hashtable<String, String>();
		Set<String> keyset = corrida.getCompGlobales().keySet();
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String clave = it.next();
			cglob.put(clave, corrida.getCompGlobales().get(clave).getValor(instanteActual));
		}
		for (Participante p : corrida.getParticipantes())
			p.actualizarVariablesCompGlobal(cglob);

		if (corrida.getLineaTiempo().esPasoFinal()) {

			/**
			 * Cada participante contribuye con sus variables de estado a la colecciÃ³n que
			 * estÃ³ en ComportamientoGeneral
			 */

			// Actualiza en el optimizable las variables de estado que rigen en el
			// paso t-1
			actualizarVarEstado();

			// Actualiza variables de control DE
			actualizarVarsControlDE(instanteIniPaso, duracionPaso);
			cantVarsControlDE = varsControlDEActivas.size();

			/**
			 * Se actualiza la informaciÃ³n sobre variables de estado del ResOptim para
			 * usarlo en un paso. TODO: ESTO SE DEBERÃ³ HACER SÃ³LO CUANDO CAMBIAN LOS
			 * COMPORTAMIENTOS O LOS PARTICIPANTES
			 */
			resoptim.inicializaResOptimParaNuevoPaso(numpaso, pasoActual.getInstanteInicial(), varsEstadoOptimizacion,
					varsControlDEActivas);

			/**
			 * Lo siguiente sÃ³lo puede hacerse despuÃ³s de haber inicializado el resoptim
			 * para las variables de estado corrientes, de otro modo no se conocerÃ³an los
			 * estados.
			 */
			if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				roptimI = (ResOptimIncrementos) resoptim;
				roptimI.cargaVBFinales();
				roptimI.calculaDerivadasParcEIncrementos(numpaso);
				roptimI.actualizarTabla(numpaso);
				//roptimI.actualizarTablaControles(numpaso);
			} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
				roptimH = (ResOptimHiperplanos) resoptim;
				roptimH.cargaHiperplanosFinales();
				String hips = roptimH.publicaHiperplanosPorVEDiscretas(numpaso, numpaso, "HIP",
						roptimH.getTablaHiperplanos());
				String dirHips = dirSalidas + "/HiperplanosFinT.xlt";
				DirectoriosYArchivos.agregaTexto(dirHips, hips);
			}
		}

		if (CorridaHandler.getInstance().isParalelo()) {
			vBellmanIniT = new TablaVByValRecursosRedis(1);
		} else {
			vBellmanIniT = new TablaVByValRecursosMemoria(1);
		}

		resoptim.getEnumLexEstados().inicializaEnum();

		int totalEstados = resoptim.getEnumLexEstados().getCantTotalVectores();
		String aimprimir = "Total estados: " + totalEstados;
//		DirectoriosYArchivos.agregaTexto(Constantes.ruta_log_paralelismo + "logCliente"
//				+ ManagementFactory.getRuntimeMXBean().getName() + ".txt", aimprimir);

		int cantServidores = clienteH.obtenercantServidores();
		System.out.println("CANTIDAD DE SERVIDORES: " + cantServidores);
		System.out.println("TOTAL: " + totalEstados);

		int cantEstadosPorPaquete = (int) Math.ceil(totalEstados / (double) cantServidores);

		System.out.println("POR SERVIDOR: " + cantEstadosPorPaquete);

		clienteH.cargarPaquetes(totalEstados, numpaso, cantEstadosPorPaquete);
//		DirectoriosYArchivos.agregaTexto(Constantes.ruta_log_paralelismo + "logCliente"
//				+ ManagementFactory.getRuntimeMXBean().getName() + ".txt",
//				"CARGARON PAQUETES LOCALES , PASO: " + numpaso);
		Optimizador.prof.pausarContador("CLI: OPTIMIZAR PASO ANTES OPTIMIZAR ESTADO");
		Optimizador.prof.iniciarContador("CLI: CARGAR PAQUETES");
		clienteH.cargarPaquetesAResolver(numpaso);
		Optimizador.prof.pausarContador("CLI: CARGAR PAQUETES");

		Optimizador.prof.iniciarContador("CLI: RESOLVER PAQUETES");

		clienteH.resolucionPaquetes();
		Optimizador.prof.pausarContador("CLI: RESOLVER PAQUETES");

		if (Constantes.NIVEL_CONSOLA > 1)
			System.out.println("TerminÃ³ cÃ³lculo de valores al inicio del paso " + numpaso);
	}

	public EnumeradorLexicografico creaEnumeradorControlesDE(long instanteRef) {
		int cantDigitos = varsControlDEActivas.size();
		int[] cotasInferiores = new int[cantDigitos];
		int[] cotasSuperiores = new int[cantDigitos];
		int ic = 0;
		for (VariableControlDE vc : varsControlDEActivas) {
			cotasInferiores[ic] = 0;
			cotasInferiores[ic] = vc.getEvolDiscretizacion().getValor(instanteRef).getCantValores() - 1;
		}

		EnumeradorLexicografico enumControlesDE = new EnumeradorLexicografico(cantDigitos, cotasInferiores,
				cotasSuperiores);
		return enumControlesDE;
	}

	public ArrayList<Double> obtenerRefPostInterna(int sorteo) {
		ArrayList<Double> referencia = new ArrayList<Double>();
		for (int i = 0; i < instantesMuestreo.length; i++) {
			referencia.add(new Double(0));
		}

		/**
		 * TODO: EFICIENCIA: OPTIMIZAR SUMA
		 */

		for (AportantePost ap : corrida.getAportantesPostizacion()) {
			sumaArray(referencia, ap.aportaParaPost(sorteo));
		}
		return referencia;

	}

	/**
	 * TODO ATENCIÃ³N ESTE MÃ³TODO ES COPIADO TAL CUAL DE SIMULADOR PASO HABRÃ³A QUE
	 * MANDARLO PARA ARRIBA
	 * 
	 * @return
	 */
	private void sumaArray(ArrayList<Double> entradasalida, ArrayList<Double> sumando) {
		for (int i = 0; i < entradasalida.size(); i++) {
			entradasalida.set(i, entradasalida.get(i) + sumando.get(i));
		}
	}

	public void determinarInstantesMuestreo() {
		int intervaloMuestreo = pasoActual.getIntervaloMuestreo();
		this.instantesMuestreo = new long[pasoActual.getDuracionPaso() / intervaloMuestreo];

		for (int i = 0; i < instantesMuestreo.length; i++) {
			instantesMuestreo[i] = pasoActual.getInstanteInicial() + i * intervaloMuestreo
					+ pasoActual.getBloque().getInstantesDesplazados();
		}
	}

	/**
	 * Para los PE que no aportan VE a la optimizaciÃ³n sortea los valores de las VA
	 * aleatorias del PE y los almacena en atributos de las respectivas VA
	 */
	public void sortearVAMontPENoVE() {
		instIniPaso = pasoActual.getInstanteInicial();
		cantSortMontecarlo = corrida.dameCantSorteos(instIniPaso);
		long[] instInicial = new long[1]; // vector auxiliar para usar como
											// argumento cuando no hay muestreo
		instInicial[0] = instIniPaso + Constantes.EPSILONSALTOTIEMPO;

		// Procesos que no tienen VE en la optimizaciÃ³n
		// Se sortean los valores de las variables y se almacenen en las VA
		for (ProcesoEstocastico pe : peMontNoEstadoOptim) {
			if (pe.isMuestreado()) {
				pe.muestrearVariablesAleatsOptim(cantSortMontecarlo, instantesMuestreo);
			} else {
				pe.muestrearVariablesAleatsOptim(cantSortMontecarlo, instInicial);
			}
		}
	}

	@Override
	/**
	 * Para los PE que tienen VE discretas exhaustivas, sortea las innovaciones que
	 * servirÃ³n para generar la VA en cada estado y sorteo Montecarlo NO SE TRATA
	 * DE INNOVACIONES PARA SORTEAR TRANSICIONES YA QUE ESTAS SE TRATAN
	 * EXHAUSTIVAMENTE
	 */
	public void sortearInnovPEDE() {
		instIniPaso = pasoActual.getInstanteInicial();
		cantSortMontecarlo = corrida.dameCantSorteos(instIniPaso);
		long[] instInicial = new long[1]; // vector auxiliar para usar como
											// argumento cuando no hay muestreo
		instInicial[0] = instFinPaso + Constantes.EPSILONSALTOTIEMPO;
		// Procesos que tienen VE en la optimizaciÃ³n
		// Se sortean las innovaciones y se almacenan en los PE
		for (DiscretoExhaustivo de : azar.getProcesosDEOptim()) {
			ProcesoEstocastico pe = (ProcesoEstocastico) de;
			if (pe.isMuestreado()) {
				pe.sortearInnovacionesOptim(cantSortMontecarlo, instantesMuestreo);
			} else {
				pe.sortearInnovacionesOptim(cantSortMontecarlo, instInicial);
			}
		}
	}

	/**
	 * Para los PE que tienen VE en la optimizaciÃ³n y sorteo Montecarlo sortea las
	 * innovaciones que servirÃ³n para genera la VA en cada estado y sorteo
	 * Montecarlo
	 */
	public void sortearInnovMontPEVE() {
		instIniPaso = pasoActual.getInstanteInicial();
		cantSortMontecarlo = corrida.dameCantSorteos(instIniPaso);
		long[] instInicial = new long[1]; // vector auxiliar para usar como
											// argumento cuando no hay muestreo
		instInicial[0] = instIniPaso + Constantes.EPSILONSALTOTIEMPO;

		// Procesos que tienen VE en la optimizaciÃ³n
		// Se sortean las innovaciones y se almacenan en los PE
		for (ProcesoEstocastico pe : peMontEstadoOptim) {
			if (pe.isMuestreado()) {
				pe.sortearInnovacionesOptim(cantSortMontecarlo, instantesMuestreo);
			} else {
				pe.sortearInnovacionesOptim(cantSortMontecarlo, instInicial);
			}
		}
	}

	public void actualizarParaPasoAnteriorServidor() {
		int ive = 0;
		varsEstadoPasoPosterior.clear();
		ordinalVEPasoPosterior.clear();
		String compGlobalBellman = corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());
		for (VariableEstado ve : varsEstadoOptimizacion) {
			varsEstadoPasoPosterior.add(ve);
			ordinalVEPasoPosterior.put(ve.getNombre(), ive);
			ive++;
		}
		// Las VE de los PEDE son las mismas a lo largo del tiempo, puede
		// cambiar la cantidad de valores
		// Crea el enumerador para recorrer los estados de las VE PEDE en el
		// paso t
		int cantVEPEDE = procesosEstDE.size();
		int[] cotasInferiores = new int[cantVEPEDE];
		int[] cotasSuperiores = new int[cantVEPEDE];
		int ivPE = 0;
		for (DiscretoExhaustivo pe : procesosEstDE) {
			cotasInferiores[ivPE] = 0;
			// los proceso PE tienen una sola VE
			cotasSuperiores[ivPE] = pe.devuelveCantPosibles(instIniPaso + Constantes.EPSILONSALTOTIEMPO) - 1;
		}

		LineaTiempo ltiempo = corrida.getLineaTiempo();
		numpaso = ltiempo.getNumPaso();

		PasoTiempo paso = ltiempo.devuelvePasoActual();
		long instInicialPaso = paso.getInstanteInicial();

		/**
		 * Se retrocede al paso t-1, para calcular los VB o hiperplanos al fin del paso
		 * t-1, antes del salto del estado de las VA discretas exhaustivas.
		 * 
		 * Las variables de estado del resoptim se actualizan a las que rigen en el paso
		 * t-1
		 */
		ltiempo.retrocederPaso();
		paso = ltiempo.devuelvePasoActual();
		pasoActual = paso;
		numpaso = ltiempo.getNumPaso();
		instInicialPaso = paso.getInstanteInicial();
		long instFinalPaso = paso.getInstanteFinal();
		int cantVEPasoPosterior = varsEstadoPasoPosterior.size();
		ordinalVEEnVarsEstado.clear();

		// Actualiza en el optimizable las variables de comportamiento que rigen
		// en el paso t-1
		actualizarVarComportamiento(instInicialPaso);

		// Actualiza en el optimizable las variables de estado que rigen en el
		// paso t-1
		boolean cambioCantVE = actualizarVarEstado();
		resoptim.setCambioCantVE(cambioCantVE);
		;

		ive = 0;
		for (VariableEstado ve : varsEstadoOptimizacion) {
			ordinalVEEnVarsEstado.put(ve.getNombre(), ive);
			ive++;
		}

		// Actualiza en el optimizable las variables de control DE

		actualizarVarsControlDE(instInicialPaso, duracionPaso);
		cantVarsControlDE = varsControlDEActivas.size();

		// Se actualiza el resoptim para el paso t-1
		resoptim.inicializaResOptimParaNuevoPaso(numpaso, instInicialPaso, varsEstadoOptimizacion,
				varsControlDEActivas);
	}

	/**
	 * Carga las variables de estado vigentes en el paso t antes de retroceder al
	 * paso t-1 Carga la tabla de ordinales de las VE vigentes en el paso t antes de
	 * retroceder al paso t-1
	 * 
	 * Calcula la tabla de VBellman o la tabla de hiperplanos al fin del paso usando
	 * las probabilidades de transiciÃ³n de las VE de procesos estocÃ³sticos DE.
	 */
	public void actualizarParaPasoAnterior() {
		int ive = 0;
		varsEstadoPasoPosterior.clear();
		ordinalVEPasoPosterior.clear();
		String compGlobalBellman = corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());
		for (VariableEstado ve : varsEstadoOptimizacion) {
			varsEstadoPasoPosterior.add(ve);
			ordinalVEPasoPosterior.put(ve.getNombre(), ive);
			ive++;
		}

		// Las VE de los PEDE son las mismas a lo largo del tiempo, puede
		// cambiar la cantidad de valores
		// Crea el enumerador para recorrer los estados de las VE PEDE en el
		// paso t
		int cantVEPEDE = procesosEstDE.size();
		int[] cotasInferiores = new int[cantVEPEDE];
		int[] cotasSuperiores = new int[cantVEPEDE];
		int ivPE = 0;
		for (DiscretoExhaustivo pe : procesosEstDE) {
			cotasInferiores[ivPE] = 0;
			// los proceso PE tienen una sola VE
			cotasSuperiores[ivPE] = pe.devuelveCantPosibles(instIniPaso + Constantes.EPSILONSALTOTIEMPO) - 1;
			ivPE++;
		}
		EnumeradorLexicografico enumEstadosVEPEDEenT = new EnumeradorLexicografico(cantVEPEDE, cotasInferiores,
				cotasSuperiores);

		LineaTiempo ltiempo = corrida.getLineaTiempo();
		numpaso = ltiempo.getNumPaso();
		PasoTiempo paso = ltiempo.devuelvePasoActual();
		long instInicialPaso = paso.getInstanteInicial();

		// Se inicializan los parÃ³metros de salida
		salidaVB = true;
		varsSalidasRecursos = resoptim.getVarsEstadoCorrientes();
		if (salidaVB) {
			String unidad = "USD";
			String vB = resoptim.publicaValoresIniT(numpaso, instInicialPaso, this, unidad);
			String dirTabla = dirSalidas + "/ValoresDeBellmanIniT.xlt";
			DirectoriosYArchivos.agregaTexto(dirTabla, vB);
			resoptim.setCambioCantVE(false);
		}

		if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
			// Publica hiperplanos al inicio del paso t
			String titulo = "HIPERPLANOS AL INICIO DEL PASO ";
			String hips = roptimH.publicaHiperplanosPorVEDiscretas(0, numpaso, titulo, hipersIniT);
			String dirHips = dirSalidas + "/HiperplanosIniT.xlt";
			DirectoriosYArchivos.agregaTexto(dirHips, hips);
		}

		// Imprime los controles DE Ã³ptimos segÃºn el estado
		long instanteRef = pasoActual.getInstanteInicial();
		int pasoImpresion = numpaso;
		String controles = resoptim.publicaUnPasoControlesDEOpt(numpaso, pasoImpresion, instanteRef, corrida);
		String dirControlesDE = dirSalidas + "/ControlesDEIniT.xlt";
		DirectoriosYArchivos.agregaTexto(dirControlesDE, controles);

		actualizarValoresVEDiscretizacionesVariables(instanteRef);

		/**
		 * Se retrocede al paso t-1, para calcular los VB o hiperplanos al fin del paso
		 * t-1, antes del salto del estado de las VA discretas exhaustivas.
		 * 
		 * Las variables de estado del resoptim se actualizan a las que rigen en el paso
		 * t-1
		 */
		ltiempo.retrocederPaso();

		paso = ltiempo.devuelvePasoActual();
		pasoActual = paso;
		numpaso = ltiempo.getNumPaso();
		instInicialPaso = paso.getInstanteInicial();
		long instFinalPaso = paso.getInstanteFinal();
		int cantVEPasoPosterior = varsEstadoPasoPosterior.size();
		ordinalVEEnVarsEstado.clear();

		// Actualiza en el optimizable las variables de comportamiento que rigen
		// en el paso t-1
		actualizarVarComportamiento(instInicialPaso);

		// Actualiza en el optimizable las variables de estado que rigen en el
		// paso t-1
		boolean cambioCantVE = actualizarVarEstado();
		resoptim.setCambioCantVE(cambioCantVE);

		ive = 0;
		for (VariableEstado ve : varsEstadoOptimizacion) {
			ordinalVEEnVarsEstado.put(ve.getNombre(), ive);
			ive++;
		}

		// Actualiza en el optimizable las variables de control DE

		actualizarVarsControlDE(instInicialPaso, paso.getDuracionPaso());
		cantVarsControlDE = varsControlDEActivas.size();

		// Se actualiza el resoptim para el paso t-1
		resoptim.inicializaResOptimParaNuevoPaso(numpaso, instInicialPaso, varsEstadoOptimizacion,
				varsControlDEActivas);

		// El enumerador de estados del resoptim recorre todos los estados del
		// paso t-1, con la clave que sale del vector vtmenos1.
		// La cantidad de VE de PEDE es la misma en t-1 y en t
		// Recorre el producto cartesiano de estados en t-1 y transiciones de
		// las VE de PEDE hacia t

		EnumeradorLexicografico enumEstadosTmenos1 = resoptim.getEnumLexEstados();
		Hashtable<String, Integer> ordVEPEDE = resoptim.getOrdinalDePEDEEnVarsEstadoOptimizacion();
		int[] vtmenos1 = enumEstadosTmenos1.devuelveVector();
		while (vtmenos1 != null) {
			// para cada estado en t-1
			enumEstadosVEPEDEenT.inicializaEnum(); // es el enumerador de estados de las VE PE DE en t
			int cantVECont = resoptim.getCantVECont();
			double[] puntoVECont = new double[cantVECont]; // es el vector de los valores de las VE continuas
			ArrayList<VariableEstado> veCont = resoptim.getVarsEstadoContinuas();
			int i = 0;
			for (VariableEstado vc : veCont) {
				int ordEnCont = resoptim.devuelveOrdinalDeUnaVEContinua(vc.getNombre());
				int ordEnVE = resoptim.getOrdinalEnVarsEstadoDeContinuas().get(ordEnCont);
				puntoVECont[ordEnCont] = vc.getEvolDiscretizacion().getValor(instInicialPaso)
						.devuelveValorOrdinal(vtmenos1[ordEnVE]);
				i++;
			}
			double vBellmanTmenos1 = 0;
			Hiperplano hiperTmenos1 = new Hiperplano(resoptim.getCantVECont(), numpaso, 0);
			hiperTmenos1.setPunto(puntoVECont);

			// crea el vector de cÃ³digos enteros para las VE PEDE en t-1
			int[] vtPEDETmenos1 = new int[cantVEPEDE];
			int indPEDE = 0;
			for (DiscretoExhaustivo pde : procesosEstDE) {
				String nombreVE = pde.getNombreVEPEDE();
				int ordinalEnVtmenos1 = ordVEPEDE.get(nombreVE);
				vtPEDETmenos1[indPEDE] = vtmenos1[ordinalEnVtmenos1];
				indPEDE++;
			}
			int[] vtPEDEenT = enumEstadosVEPEDEenT.devuelveVector();

			/**
			 * Si no hay variables de estado PEDE se hace una pasada Ãºnica
			 */
			boolean arrancaSinVEPEDE = false;
			if (cantVEPEDE == 0)
				arrancaSinVEPEDE = true;
			while (vtPEDEenT != null || arrancaSinVEPEDE == true) {

				// Para cada estado de las VE PEDE en t
				// crea el vector de cÃ³digos enteros completo en t

				int[] vt = new int[cantVEPasoPosterior];
				int indvepp;
				int indve;
				/**
				 * Copia en lo posible desde el estado en t-1 hacia el estado en t. COMO LA
				 * COPIA ES POR NOMBRE, SI UNA VE de t-1 deja de existir en t simplemente
				 * desaparece. Si en t aparece una nueva variable de estado que no existe en t-1
				 * 
				 * Primero copia en t todas las variables, incluso las discretas no exhaustivas
				 */
				for (VariableEstado vepp : varsEstadoPasoPosterior) {
					indvepp = ordinalVEPasoPosterior.get(vepp.getNombre());
					if (ordinalVEEnVarsEstado.get(vepp.getNombre()) != null) {
						indve = ordinalVEEnVarsEstado.get(vepp.getNombre());
						vt[indvepp] = vtmenos1[indve];
					} else {
						vt[indvepp] = vepp
								.devuelveOrdinalEstadoAlAparecerVE(instFinalPaso + Constantes.EPSILONSALTOTIEMPO);
					}
				}
				// sobreescribe los valores de VE PEDE
				int ivpede = 0;
				for (DiscretoExhaustivo pede : procesosEstDE) {
					String nve = pede.getNombreVEPEDE();
					indve = ordinalVEPasoPosterior.get(nve);
					vt[indve] = vtPEDEenT[ivpede];
				}
				double probTrans = 1;
				int ipe = 0;
				for (DiscretoExhaustivo pede : procesosEstDE) {
					probTrans = probTrans * pede.devuelveProbTransicion(instInicialPaso,
							instFinalPaso + Constantes.EPSILONSALTOTIEMPO, vtPEDETmenos1[ipe], vtPEDEenT[ipe]);

					ipe++;
				}
				ClaveDiscreta clave = new ClaveDiscreta(vt);

				if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
					InformacionValorPunto ivp = vBellmanIniT.devuelveInfoValoresPuntoAuxiliar(0, clave);
					double valor1tr = ivp.getValorVB();

					// System.out.println("V1TR: " + valor1tr);
					// System.out.println("PASO pedido: " + Integer.toString(numpaso) + "VB: "
					// +Double.toString(valor1tr));
					vBellmanTmenos1 += valor1tr * probTrans;

				} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
					Hiperplano hip1tr = hipersIniT.devuelveElHiperplanoDeUnPunto(0, clave);
					hiperTmenos1.sumaHiperplanoPorEscalar(hip1tr, probTrans);
				}

				// sigue la iteraciÃ³n en los estados finales de las VE PEDE
				vtPEDEenT = enumEstadosVEPEDEenT.devuelveVector();
				arrancaSinVEPEDE = false;
			}

			if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				/**
				 * Carga el VB del estado vtmenos1 al fin de t-1, antes del salto de VE PE DE,
				 * para hacerlo crea la entrada InformacionValorPunto que tendrÃ³ tambiÃ³n las
				 * derivadas parciales e incrementos
				 */
				roptimI.creaIVPyCargaVBEnTabla(numpaso, vtmenos1, vBellmanTmenos1);

				roptimI.actualizarTabla(numpaso);
			//	roptimI.actualizarTablaControles(numpaso);
			} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
				/**
				 * Carga en la tablaHiperplanos el hiperplano por el punto vtmenos1, al fin de
				 * t-1, antes del salto de VE PE DE
				 */
				ClaveDiscreta claveTotalTMenos1 = new ClaveDiscreta(vtmenos1);
				ClaveDiscreta claveVEDiscretas = resoptim.claveVEDiscretasDeClaveTotal(claveTotalTMenos1);
				corrigeValRecursosHiper(hiperTmenos1);
				roptimH.cargaHiperplano(numpaso, claveVEDiscretas, claveTotalTMenos1, hiperTmenos1);
			}

			// sigue la iteraciÃ³n en los estados antes del salto
			vtmenos1 = enumEstadosTmenos1.devuelveVector();
		}

		salidaVB = true;

		/**
		 * Completa las tablas al fin del paso t-1
		 */
		if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
			// Calcula y carga las derivadas parciales e incrementos, luego de haber
			// completado la carga de los valores de Bellman al fin t-1
			roptimI.calculaDerivadasParcEIncrementos(numpaso);
			// Se comenta porque la correcciÃ³n de la reserva estratÃ³gica se hace en el
			// hidrÃ³ulico comportamiento simulaciÃ³n
			// corrigeValRecursosIyDP(roptimI);
		} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
			// A partir de la tabla auxiliar de hiperplanos en cada estado total
			// hipersFinTmenos1, de un paso,
			// carga la tabla tablaHiperplanos para fin del paso t-1
			// roptimH.cargaTablaHiperplanosFinTmenos1(numpaso, hipersFinTmenos1);
		}

		String unidad;
		if (salidaVB) {
			unidad = "USD";
			String vB = resoptim.publicaUnPasoValores(numpaso, numpaso, instInicialPaso, null, corrida, unidad);
			String dirTabla = dirSalidas + "/ValoresDeBellmanFinT.xlt";
			DirectoriosYArchivos.agregaTexto(dirTabla, vB);
			String vBML = resoptim.publicaUnPasoValoresParaML(numpaso, numpaso, instInicialPaso, null, corrida, unidad);
			String dirTablaML = dirSalidas + "/ValoresDeBellmanParaRN.csv";
			DirectoriosYArchivos.agregaTexto(dirTablaML, vBML);
		}
		for (VariableEstado ve : varsSalidasRecursos) {
			boolean esDiscretaNoIncremental = (ve != null) && (ve.isDiscreta() && !ve.isDiscretaIncremental());
			if (!esDiscretaNoIncremental) {
				// Si varR es discreta no incremental no se hace nada
				if (ve.isDiscretaIncremental()) {
					unidad = "USD";
				} else {
					unidad = "HAY QUE ARREGLAR ESTO AGREGANDO NOMBRE DE UNIDAD A LA VARIABLE DE ESTADO";
				}
				String vR = resoptim.publicaUnPasoValores(numpaso, numpaso, instInicialPaso, ve, corrida, unidad);
				String dirTabla = dirSalidas + "/ValRecursosVE-" + ve.getNombre() + ".xlt";
				DirectoriosYArchivos.agregaTexto(dirTabla, vR);
				vR = resoptim.publicaUnPasoValoresParaML(numpaso, numpaso, instInicialPaso, ve, corrida, unidad);
				dirTabla = dirSalidas + "/ValRecursosVEML-" + ve.getNombre() + ".csv";
				DirectoriosYArchivos.agregaTexto(dirTabla, vR);
			}
		}
		if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
			String dirHiperplanos = dirSalidas + "/HiperplanosFinT.xlt";
			// String ships = roptimH.publicaHiperplanosPorPuntos(numpaso);
			String titulo = "HIPERPLANOS AL FIN DEL PASO  ";
			String ships = roptimH.publicaHiperplanosPorVEDiscretas(numpaso, numpaso, titulo,
					roptimH.getTablaHiperplanos());
			DirectoriosYArchivos.agregaTexto(dirHiperplanos, ships);

		}
		resoptim.setInicio(false);

	}

	private void corrigeValRecursosIyDP(ResOptimIncrementos roptimI) {
		// Recorre los hidrÃ³ulicos para corregir valores del agua por reserva
		// estratÃ³gica
		for (GeneradorHidraulico gh : corrida.getHidraulicos().values()) {
			if (gh.isValorAplicaOptim() && gh.isHayReservaEstrategica()) {
				// Corrige el valor del agua

			}

		}

	}

	private void corrigeValRecursosHiper(Hiperplano hiperTmenos1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizarParaPasoAnteriorCliente() {
		ClienteHandler ch = ClienteHandler.getInstance();
		vBellmanIniT.devuelveTablaAuxiliar(this.numpaso, ch.getPaquetes().size(),
				resoptim.getEnumLexEstados().getListaVectorDeOrdinal().size());
	//	resoptim.getTablaControlesDE().devuelveTabla(numpaso, ch.getPaquetes().size(), resoptim.getEnumLexEstados().getListaVectorDeOrdinal().size());	
		actualizarParaPasoAnterior();
	}

	/**
	 * Devuelve true si cambia la cantidad de variables de estado y false si no
	 * cambian
	 * 
	 * @return
	 */
	private boolean actualizarVarEstado() {
		int cantVEAnteriores = varsEstadoOptimizacion.size();
		varsEstadoOptimizacion.clear();
		for (AportanteEstado p : corrida.getAportantesEstado()) {
			p.actualizaValoresVEDiscretizacionesVariables(this.getPasoActual().getInstanteFinal());
			p.actualizarVarsEstadoOptimizacion();
			ArrayList<VariableEstado> nuevasOptimizacion;
			nuevasOptimizacion = p.aportarEstadoOptimizacion();
			if (nuevasOptimizacion != null)
				varsEstadoOptimizacion.addAll(nuevasOptimizacion);
		}
		return (varsEstadoOptimizacion.size() != cantVEAnteriores);
	}

	/**
	 * Cada participante contribuye con sus variables de control DE a la colecciÃ³n
	 * que estÃ³ en ComportamientoGeneral. SÃ³lo se incluyen las variables que
	 * estÃ³n activas en el paso, teniendo en cuenta su perÃ³odo.
	 */
	private void actualizarVarsControlDE(long instInicialPaso, int durPaso) {
		varsControlDEActivas.clear();
		ArrayList<VariableControlDE> nuevasControlDE;
		for (AportanteControlDE p : corrida.getAportantesControlDE()) {
			p.actualizarVarsControlDE();
			nuevasControlDE = p.aportarVarsControlDE();
			if (nuevasControlDE != null) {
				for (VariableControlDE vc : nuevasControlDE) {
					if (vc==null) continue;
					if (vc.estaActiva(instInicialPaso, durPaso))
						varsControlDEActivas.add(vc);
				}
			}
		}
	}

	@Override
	public void cargarPasoCorriente(int numpaso, PasoTiempo paso) {
		this.numpaso = numpaso;
		this.pasoActual = paso;
		this.duracionPaso = paso.getDuracionPaso();
		this.duracionPostes = postizador.getDurPos();
	}

	public void actualizarVarComportamiento(long instInicialPaso) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		// Actualiza la foto de las variables de comportamiento general que se
		// encuentran en el Comportamiento SimulaciÃ³n
		for (Participante p : corrida.getParticipantes())
			p.actualizarVarsCompGeneral(pasoActual.getInstanteInicial());

		Hashtable<String, String> cglob = new Hashtable<String, String>();
		Set<String> keyset = corrida.getCompGlobales().keySet();
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String clave = it.next();
			cglob.put(clave, corrida.getCompGlobales().get(clave).getValor(instanteActual));
		}

		for (Participante p : corrida.getParticipantes())
			p.actualizarVariablesCompGlobal(cglob);

	}

	@Override
	/**
	 * Crea un ResOptim y su tabla de valores de Bellman incrementos y derivadas
	 * parciales
	 * 
	 * Asigna las VA de los participantes con las VA del proceso de optimizaciÃ³n
	 * 
	 */
	public void inicializarOptimizable() {

		corrida.setFase(Constantes.FASE_OPT);
		optimizando = true;
		pasoActual = corrida.getLineaTiempo().pasoActual();
		this.azar = corrida.dameAzar();

		if (corrida.isPostizacionExterna()) {
			postizador.setRuta(corrida.getRutaPostizacion());
			postizador.setExterna(true);
			postizador.cargarExterna(corrida.getLineaTiempo().getInicioTiempo());
		} else {
			postizador.setExterna(false);
		}

		valPostizador.inicializarParaOptimizacion();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		// Se construye el resoOptim
		if (corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN).getValor(instanteActual)
				.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
			resoptim = new ResOptimIncrementos(corrida.getCantidadPasos());
		} else if (corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN).getValor(instanteActual)
				.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
			resoptim = new ResOptimHiperplanos(corrida.getCantidadPasos());
		}
		for (AportanteEstado ae : corrida.getAportantesEstado()) {
			// en la tabla del resoptim cada aportante estado carga la pareja (nombre de VE
			// continua, nombre en el despacho de la VE)
			ae.cargaParVEContinuaVDespacho(resoptim.getVariableDespachoDeVEContinua());
		}

		/**
		 * Carga las colecciones de PE distintos tipos
		 */
		for (ProcesoEstocastico pe : azar.getProcesosOptim()) {
			if (!pe.isDiscretoExhaustivo() && !(pe.tieneVEOptim())) {
				peMontNoEstadoOptim.add(pe);
			} else if (!pe.isDiscretoExhaustivo() && (pe.tieneVEOptim())) {
				peMontEstadoOptim.add(pe);
			} else {
				// el PE es discreto exhaustivo
				DiscretoExhaustivo pede = (DiscretoExhaustivo) pe;
				procesosEstDE.add(pede);
			}
		}

		/**
		 * Asigna las VA de los participantes con las VA de sus procesos de
		 * optimizaciÃ³n Cuando las VA resultan de valpostizaciÃ³n no es relevante.
		 */
		for (Participante p : corrida.getParticipantes()) {
			p.asignaVAOptim();
		}

//		String nomArchLT = dirSalidas + "/LineaTiempo.xlt";
//		boolean existe = DirectoriosYArchivos.existeArchivo(nomArchLT);
//		if (existe)
//			DirectoriosYArchivos.eliminaArchivo(nomArchLT); 
		// String slt = corrida.getLineaTiempo().toString();
		// DirectoriosYArchivos.agregaTexto(nomArchLT, slt);
		setNombreArchInfactible(dirSalidas + "\\");
		setNombreArchEntradasPL(dirSalidas + "/entradasPL.lp");
		setNombreArchSalidasPL(dirSalidas + "/salidasPL.lp");

	}

	public void finalizarOptimizacion() {
		int paso = 0;
		int pasoImpresion = 0;
		long instanteRef = corrida.getLineaTiempo().getInstInicPasoCorriente();
		String controles = resoptim.publicaUnPasoControlesDEOpt(paso, pasoImpresion, instanteRef, corrida);
		String dirControlesDE = dirSalidas + "/ControlesDEIniT.xlt";
		DirectoriosYArchivos.agregaTexto(dirControlesDE, controles);
	}

	public void cargarValoresVEParaUnPaso(int[] codigoEstado) {
		int icod = 0;
		for (VariableEstado ve : varsEstadoOptimizacion) {
			double val = ve.devuelveDiscretizacion(instIniPaso).devuelveValorOrdinal(codigoEstado[icod]);
			ve.setEstado(val);
			icod++;
		}

	}

	/**
	 * Inicializa los PE para hacerlos participar de un paso de la optimizaciÃ³n
	 * Setea los instantes iniciales de los PE
	 * 
	 * ATENCIÃ³N: CUANDO SE HACE prepararPasoAnterior en OptimizarPaso el inicio del
	 * paso anterior puede estar en otro aÃ³o y el indAnio cambia.
	 */
	@Override
	public void inicializarPEPasoOptim() {
		cantSortMontecarlo = corrida.getCantSorteosMont().getValor(pasoActual.getInstanteInicial());
		long instIniPasoOp = pasoActual.getInstanteInicial();
		for (ProcesoEstocastico pe : azar.getProcesosOptim()) {
			pe.setInstIniPasoOptim(instIniPasoOp);
			pe.prepararPasoOptim(cantSortMontecarlo);
			int pasoPEInstanteInicial = pe.pasoDelAnio(instIniPasoOp); // Los pasos empiezan en cero;
			long instanteInicialAnio = pe.instanteInicialAnioDeInstante(instIniPasoOp);
			pe.setInstanteCorrienteFinal(instanteInicialAnio + pasoPEInstanteInicial * pe.getDurPaso());
			pe.setInstanteCorrienteInicial(pe.getInstanteCorrienteFinal() - pe.getDurPaso());
		}
	}

	@Override
	public void guardarTablasResOptimEnDisco() {
		resoptim.guardarTablasResOptimEnDisco(dirSalidas);
	}

	public boolean isSalidaVB() {
		return salidaVB;
	}

	public void setSalidaVB(boolean salidaVB) {
		this.salidaVB = salidaVB;
	}

	public ArrayList<VariableEstado> getVarsSalidasRecursos() {
		return varsSalidasRecursos;
	}

	public void setVarsSalidasRecursos(ArrayList<VariableEstado> varsSalidasRecursos) {
		this.varsSalidasRecursos = varsSalidasRecursos;
	}

	public int getCantVarsControlDE() {
		return cantVarsControlDE;
	}

	public void setCantVarsControlDE(int cantVarsControlDE) {
		this.cantVarsControlDE = cantVarsControlDE;
	}

	public Corrida getCorrida() {
		return corrida;
	}

	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}

	public int getNumpaso() {
		return numpaso;
	}

	public void setNumpaso(int numpaso) {
		this.numpaso = numpaso;
	}

	public PasoTiempo getPasoActual() {
		return pasoActual;
	}

	public void setPasoActual(PasoTiempo pasoActual) {
		this.pasoActual = pasoActual;
	}

	public Postizador getPostizador() {
		return postizador;
	}

	public void setPostizador(Postizador postizador) {
		this.postizador = postizador;
	}

	public PostizacionPaso getPostPaso() {
		return postPaso;
	}

	public void setPostPaso(PostizacionPaso postPaso) {
		this.postPaso = postPaso;
	}

	public ValPostizador getValPostizador() {
		return valPostizador;
	}

	public void setValPostizador(ValPostizador valPostizador) {
		this.valPostizador = valPostizador;
	}

	public String getDirSalidas() {
		return dirSalidas;
	}

	public void setDirSalidas(String dirSalidas) {
		this.dirSalidas = dirSalidas;
	}

	public Integer getCantidadPostes() {
		return cantidadPostes;
	}

	public void setCantidadPostes(Integer cantidadPostes) {
		this.cantidadPostes = cantidadPostes;
	}

	public Integer getDuracionPaso() {
		return duracionPaso;
	}

	public void setDuracionPaso(Integer duracionPaso) {
		this.duracionPaso = duracionPaso;
	}

	public int[] getDuracionPostes() {
		return duracionPostes;
	}

	public void setDuracionPostes(int[] duracionPostes) {
		this.duracionPostes = duracionPostes;
	}

	// public AFIncrementos getAproxFuturaOpt() {
	// return aproxFuturaOpt;
	// }
	//
	// public void setAproxFuturaOpt(AFIncrementos aproxFuturaOpt) {
	// this.aproxFuturaOpt = aproxFuturaOpt;
	// }
	//
	// public AFHiperplanos getAproxFuturaHiperplanos() {
	// return aproxFuturaHiperplanos;
	// }
	//
	// public void setAproxFuturaHiperplanos(AFHiperplanos aproxFuturaHiperplanos) {
	// this.aproxFuturaHiperplanos = aproxFuturaHiperplanos;
	// }

	public ArrayList<VariableEstado> getVarsEstadoOptimizacion() {
		return varsEstadoOptimizacion;
	}

	public void setVarsEstadoOptimizacion(ArrayList<VariableEstado> varsEstadoOptimizacion) {
		this.varsEstadoOptimizacion = varsEstadoOptimizacion;
	}

	public long[] getInstantesMuestreo() {
		return instantesMuestreo;
	}

	public void setInstantesMuestreo(long[] instantesMuestreo) {
		this.instantesMuestreo = instantesMuestreo;
	}

	public Hashtable<String, String> getValsCompGlobal() {
		return valsCompGlobal;
	}

	public void setValsCompGlobal(Hashtable<String, String> valsCompGlobal) {
		this.valsCompGlobal = valsCompGlobal;
	}

	// public ArrayList<VariableEstado> getVarsEstadoPEDE() {
	// return varsEstadoPEDE;
	// }
	//
	// public void setVarsEstadoPEDE(ArrayList<VariableEstado> varsEstadoPEDE) {
	// this.varsEstadoPEDE = varsEstadoPEDE;
	// }
	//
	// public ArrayList<VariableEstado> getVarsEstadoPENODE() {
	// return varsEstadoPENODE;
	// }
	//
	// public void setVarsEstadoPENODE(ArrayList<VariableEstado> varsEstadoPENODE) {
	// this.varsEstadoPENODE = varsEstadoPENODE;
	// }

	public ArrayList<VariableControl> getVarsControlActivas() {
		return varsControlActivas;
	}

	public void setVarsControlActivas(ArrayList<VariableControl> varsControlActivas) {
		this.varsControlActivas = varsControlActivas;
	}

	public ArrayList<VariableControlDE> getVarsControlDEActivas() {
		return varsControlDEActivas;
	}

	public void setVarsControlDEActivas(ArrayList<VariableControlDE> varsControlDEActivas) {
		this.varsControlDEActivas = varsControlDEActivas;
	}

	public Azar getAzar() {
		return azar;
	}

	public void setAzar(Azar azar) {
		this.azar = azar;
	}

	public OptimizadorEstado getOpEst() {
		return opEst;
	}

	public void setOpEst(OptimizadorEstado opEst) {
		this.opEst = opEst;
	}

	public Despachador getDespachador() {
		return despachador;
	}

	public void setDespachador(Despachador despachador) {
		this.despachador = despachador;
	}

	public DatosSalidaProblemaLineal getSalidaUltimaIter() {
		return salidaUltimaIter;
	}

	public void setSalidaUltimaIter(DatosSalidaProblemaLineal salidaUltimaIter) {
		this.salidaUltimaIter = salidaUltimaIter;
	}

	public double getTasaDescuentoAnual() {
		return tasaDescuentoAnual;
	}

	public void setTasaDescuentoAnual(double tasaDescuentoAnual) {
		this.tasaDescuentoAnual = tasaDescuentoAnual;
	}

	public int getCantSortMontecarlo() {
		return cantSortMontecarlo;
	}

	public void setCantSortMontecarlo(int cantSortMontecarlo) {
		this.cantSortMontecarlo = cantSortMontecarlo;
	}

	public ResOptim getResoptim() {
		return resoptim;
	}

	public void setResoptim(ResOptimIncrementos resoptim) {
		this.resoptim = resoptim;
	}

	public ArrayList<VariableEstado> getVarsEstadoPasoPosterior() {
		return varsEstadoPasoPosterior;
	}

	public void setVarsEstadoPasoPosterior(ArrayList<VariableEstado> varsEstadoPasoPosterior) {
		this.varsEstadoPasoPosterior = varsEstadoPasoPosterior;
	}

	public Hashtable<String, Integer> getOrdinalVEEnVarsEstado() {
		return ordinalVEEnVarsEstado;
	}

	public void setOrdinalVEEnVarsEstado(Hashtable<String, Integer> ordinalVEEnVarsEstado) {
		this.ordinalVEEnVarsEstado = ordinalVEEnVarsEstado;
	}

	public Hashtable<String, Integer> getOrdinalVEPasoPosterior() {
		return ordinalVEPasoPosterior;
	}

	public void setOrdinalVEPasoPosterior(Hashtable<String, Integer> ordinalVEPasoPosterior) {
		this.ordinalVEPasoPosterior = ordinalVEPasoPosterior;
	}

	public ArrayList<DiscretoExhaustivo> getProcesosEstDE() {
		return procesosEstDE;
	}

	public void setProcesosEstDE(ArrayList<DiscretoExhaustivo> procesosEstDE) {
		this.procesosEstDE = procesosEstDE;
	}

	public ArrayList<ProcesoEstocastico> getPeMontNoEstadoOptim() {
		return peMontNoEstadoOptim;
	}

	public void setPeMontNoEstadoOptim(ArrayList<ProcesoEstocastico> peMontNoEstadoOptim) {
		this.peMontNoEstadoOptim = peMontNoEstadoOptim;
	}

	public ArrayList<ProcesoEstocastico> getPeMontEstadoOptim() {
		return peMontEstadoOptim;
	}

	public void setPeMontEstadoOptim(ArrayList<ProcesoEstocastico> peMontEstadoOptim) {
		this.peMontEstadoOptim = peMontEstadoOptim;
	}

	public Hashtable<String, Integer> getOrdinalInnovacionesPE() {
		return ordinalInnovacionesPE;
	}

	public void setOrdinalInnovacionesPE(Hashtable<String, Integer> ordinalInnovacionesPE) {
		this.ordinalInnovacionesPE = ordinalInnovacionesPE;
	}

	public Hashtable<String, Integer> getCantInnovacionesPE() {
		return cantInnovacionesPE;
	}

	public void setCantInnovacionesPE(Hashtable<String, Integer> cantInnovacionesPE) {
		this.cantInnovacionesPE = cantInnovacionesPE;
	}

	public double[][] getInnovaciones() {
		return innovaciones;
	}

	public void setInnovaciones(double[][] innovaciones) {
		this.innovaciones = innovaciones;
	}

	public long getInstIniPaso() {
		return instIniPaso;
	}

	public void setInstIniPaso(long instIniPaso) {
		this.instIniPaso = instIniPaso;
	}

	public long getInstFinPaso() {
		return instFinPaso;
	}

	public void setInstFinPaso(long instFinPaso) {
		this.instFinPaso = instFinPaso;
	}

	public TablaVByValRecursos getvBellmanIniT() {
		return vBellmanIniT;
	}

	public void setvBellmanIniT(TablaVByValRecursos vBellmanIniT) {
		this.vBellmanIniT = vBellmanIniT;
	}

	public ResOptimIncrementos getRoptimI() {
		return roptimI;
	}

	public void setRoptimI(ResOptimIncrementos roptimI) {
		this.roptimI = roptimI;
	}

	public ResOptimHiperplanos getRoptimH() {
		return roptimH;
	}

	public void setRoptimH(ResOptimHiperplanos roptimH) {
		this.roptimH = roptimH;
	}

	public TablaHiperplanos getHipersIniT() {
		return hipersIniT;
	}

	public void setHipersIniT(TablaHiperplanos hipersIniT) {
		this.hipersIniT = hipersIniT;
	}

	// public TablaHiperplanos getHipersFinTmenos1() {
	// return hipersFinTmenos1;
	// }
	//
	// public void setHipersFinTmenos1(TablaHiperplanos hipersFinTmenos1) {
	// this.hipersFinTmenos1 = hipersFinTmenos1;
	// }

	public void setResoptim(ResOptim resoptim) {
		this.resoptim = resoptim;
	}

	@Override
	public ResOptim devuelveResOptim() {
		return resoptim;
	}

	@Override
	public void inicializarAzarParaOptimizacion() {
		this.azar.inicializarAzarParaOptimizacion();
	}

	@Override
	public Azar devuelveAzar() {
		return this.getAzar();
	}

	@Override
	public long[] devuelveInstantesMuestreo() {
		return this.getInstantesMuestreo();
	}

	public boolean isOptimizando() {
		return optimizando;
	}

	public void setOptimizando(boolean optimizando) {
		this.optimizando = optimizando;
	}

	public String getNombreArchInfactible() {
		return nombreArchInfactible;
	}

	public void setNombreArchInfactible(String nombreArchInfactible) {
		this.nombreArchInfactible = nombreArchInfactible;
	}

	public String getNombreArchEntradasPL() {
		return nombreArchEntradasPL;
	}

	public void setNombreArchEntradasPL(String nombreArchEntradasPL) {
		this.nombreArchEntradasPL = nombreArchEntradasPL;
	}

	public String getNombreArchSalidasPL() {
		return nombreArchSalidasPL;
	}

	public void setNombreArchSalidasPL(String nombreArchSalidasPL) {
		this.nombreArchSalidasPL = nombreArchSalidasPL;
	}

	public Hashtable<Integer, PostizacionPaso> getPostizacionesPaso() {
		return postizacionesPaso;
	}

	public void setPostizacionesPaso(Hashtable<Integer, PostizacionPaso> postizacionesPaso) {
		this.postizacionesPaso = postizacionesPaso;
	}

	@Override
	public void optimizarPasoAproximada() {
		String compGlobalBellman = corrida.getCompGlobales().get(Constantes.COMPVALORESBELLMAN)
				.getValor(pasoActual.getInstanteInicial());
		numpaso = corrida.getLineaTiempo().getNumPaso();
		long instanteIniPaso = pasoActual.getInstanteInicial();
		valsCompGlobal = corrida.dameValsCompGlobal(pasoActual.getInstanteInicial());
		CompGeneral.setCompsGlobales(valsCompGlobal);
		int cantPostes = pasoActual.getBloque().getCantPostes();
		valPostizador.sortearUnifPostes(cantPostes);

		/**
		 * Actualiza la foto de las variables de comportamiento general que se
		 * encuentran en el Comportamiento SimulaciÃ³n
		 */
		for (Participante p : corrida.getParticipantes())
			p.actualizarVarsCompGeneral(pasoActual.getInstanteInicial());

		Hashtable<String, String> cglob = new Hashtable<String, String>();
		Set<String> keyset = corrida.getCompGlobales().keySet();
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String clave = it.next();
			cglob.put(clave, corrida.getCompGlobales().get(clave).getValor(instanteIniPaso));
		}
		for (Participante p : corrida.getParticipantes())
			p.actualizarVariablesCompGlobal(cglob);

		if (corrida.getLineaTiempo().esPasoFinal()) {

			/**
			 * Cada participante contribuye con sus variables de estado a la colecciÃ³n que
			 * estÃ³ en ComportamientoGeneral
			 */

			// Actualiza en el optimizable las variables de estado que rigen en el
			// paso t-1
			actualizarVarEstado();

			// Actualiza variables de control DE que estÃ³n activas dado el comportamiento
			// general y el perÃ³odo de la variable
			actualizarVarsControlDE(instanteIniPaso, pasoActual.getDuracionPaso());
			cantVarsControlDE = varsControlDEActivas.size();

			/**
			 * Se actualiza la informaciÃ³n sobre variables de estado del ResOptim para
			 * usarlo en un paso. TODO: ESTO SE DEBERÃ³ HACER SÃ³LO CUANDO CAMBIAN LOS
			 * COMPORTAMIENTOS O LOS PARTICIPANTES
			 */
			resoptim.inicializaResOptimParaNuevoPaso(numpaso, pasoActual.getInstanteInicial(), varsEstadoOptimizacion,
					varsControlDEActivas);

			/**
			 * Lo siguiente sÃ³lo puede hacerse despuÃ³s de haber inicializado el resoptim
			 * para las variables de estado corrientes, de otro modo no se conocerÃ³an los
			 * estados.
			 */
			if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				roptimI = (ResOptimIncrementos) resoptim;
				roptimI.cargaVBFinales();
				roptimI.calculaDerivadasParcEIncrementos(numpaso);
			} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
				roptimH = (ResOptimHiperplanos) resoptim;
				roptimH.cargaHiperplanosFinales();
				String titulo = "HIPERPLANOS AL FIN DEL PASO ";
				String hips = roptimH.publicaHiperplanosPorVEDiscretas(numpaso, numpaso, titulo,
						roptimH.getTablaHiperplanos());
				String dirHips = dirSalidas + "/HiperplanosFinT.xlt";
				DirectoriosYArchivos.agregaTexto(dirHips, hips);
			}

		}
		if (CorridaHandler.getInstance().isParalelo()) {
			vBellmanIniT = new TablaVByValRecursosRedis(1);
		} else {
			vBellmanIniT = new TablaVByValRecursosMemoria(1);
		}

		// hipersIniT e hipersFinT son tabla auxiliares de un paso para procesar el
		// salto de las VE PE DE
		// en el comportamiento Hiperplanos
		hipersIniT = new TablaHiperplanosMemoria(1);
//		hipersFinTmenos1 = new TablaHiperplanosMemoria(1);

		ArrayList<int[]> estadosSampleados = resoptim.getEnumLexEstados()
				.dameKEstadosAleatorios((int) Math.floor(resoptim.dameCantidadEstados() * 0.01));

		for (int[] codigoEstado : estadosSampleados) {

			cargarValoresVEParaUnPaso(codigoEstado);

			/**
			 * Se crea cada vez el OptimizadorEstado anticipando la paralelizaciÃ³n
			 */
			OptimizadorEstado opEst = new OptimizadorEstado(this);

//			// Crear el enumerador de controles discretos exhaustivos si existen esos controles
//			if(cantVarsControlDE>0){
//				EnumeradorLexicografico enumCDE = creaEnumeradorControlesDE(instanteIniPaso);
//				opEst.setEnumCDE(enumCDE);	
//			}

			for (Participante p : this.getCorrida().getParticipantes()) {
				p.setOptimEstado(opEst);
			}

			Hiperplano hipOpt = opEst.optimizarEstado(codigoEstado);
			double valorEstado = hipOpt.getvBellman();

			// Almacena el valor en la tabla provisoria vBellmanIniT o hipersIniT segÃ³n el
			// comportamiento
			ClaveDiscreta clave = new ClaveDiscreta(codigoEstado);

			if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				InformacionValorPunto ivp = new InformacionValorPunto();
				ivp.setValorVB(valorEstado);

				vBellmanIniT.cargaInfoValoresPunto(0, clave, ivp); // la tabla tiene un sólo paso,el 0
			} else if (compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
				ClaveDiscreta claveVEDis = roptimH.claveVEDiscretasDeClaveTotal(clave);
				hipersIniT.cargaHiperplano(0, claveVEDis, clave, hipOpt);
			}

		}

		System.out.println("TerminÃ³ cÃ³lculo de valores al inicio del paso " + numpaso);

	}

}