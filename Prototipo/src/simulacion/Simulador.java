/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Simulador is part of MOP.
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

package simulacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import datatypesResOptim.DatosResOptimIncrementos;
import datatypesSalida.DatosEPPResumen;
import datatypesTiempo.DatosLineaTiempo;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import logica.CorridaHandler;
import optimizacion.ResOptim;
import parque.Azar;
import persistencia.CargadorResOptimEDF;
import pizarron.ClienteHandler;
import pizarron.PaqueteEscenarios;
import pizarron.ServidorHandler;
import procesosEstocasticos.ProcesadorSimulacionPEs;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.ProfilerBasicoTiempo;
import utilitarios.Utilitarios;

/**
 * Clase encargada de la lógica de la simulación
 * 
 * @author ut602614
 *
 */

//TENER EN CUENTA QUE EL EXTENDS SE USA COMO IMPLEMENTS PORQUE JAVA MANEJA ESO ASí

public class Simulador<T extends Simulable> {
	private LineaTiempo ltiempo;

	private int cantEscenarios;
	private long instanteInicial;
	private long instanteFinal;
	private long instanteInicialEstudio;
	private boolean encadenado;
	private int numpaso;
	private boolean resoptimExterno;
	private String dirSalidasSim; // directorio de las salidas de la simulación
	private boolean enDisco;

	private ResOptim resoptim;
	// Cuando se haga la paralelización es probable que se tenga una colección de
	// simulables
	private Simulable simulable;

	private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
	private final ReadOnlyBooleanWrapper progresEscritor = new ReadOnlyBooleanWrapper();

	public ReadOnlyDoubleProperty progressProperty() {
		return progress;
	}

	public ReadOnlyBooleanProperty progresEscritorProperty() {
		return progresEscritor;
	}
	/*
	 * ATENCIóN MANOLO Estructura para almacenar datos de salida de los escenenarios
	 * 
	 */
	DatosEPPResumen datosResumen;

	Hashtable<String, ProcesoEstocastico> tablaPEs;

	private ClienteHandler clienteH;

	private ServidorHandler servidorH;

	private boolean cancelarSimulacion = false;

	/**
	 * El comportamiento en un paso corresponde al vólido en el instante inicial de
	 * ese paso segón las evoluciones de los comportamientos. En el instante inicial
	 * del paso ya estó hecha la reducción o ampliación del conjunto de variables de
	 * estado.
	 * 
	 * @param resoptim2
	 */
	public Simulador() {

	}

	public void inicializarSimulador(LineaTiempo lt, ResOptim resoptim2, int cantEscenarios, T simulable,
			boolean encadenado, boolean resoptimExterno, String rutaXml, String rutaSals, boolean enDisco,
			boolean servidor) {

		CorridaHandler ch = CorridaHandler.getInstance();
		boolean paralelo = ch.isParalelo();
		if(paralelo){
			this.clienteH = ClienteHandler.getInstance();
			this.servidorH = ServidorHandler.getInstance();
		}

		this.simulable = simulable;
		this.cantEscenarios = cantEscenarios;
		this.instanteInicial = lt.getInicial();
		this.instanteFinal = lt.getLinea().get(lt.getCantidadPasos() - 1).getInstanteFinal();
		this.ltiempo = lt;
		this.resoptimExterno = resoptimExterno;
		this.enDisco = enDisco;
		if (resoptimExterno) {
			DatosResOptimIncrementos droi = new DatosResOptimIncrementos();
			droi.setNombre("Prueba de cargador EDF");
			droi.setRuta("G:/PLA/Pla_datos/Archivos/ModeloOp/ValaguaEDF");
			droi.setTipoSoporte(" ");
			this.resoptim = CargadorResOptimEDF.devuelveResOptimIncrementos(droi);

		} else {
			this.resoptim = resoptim2;
		}
		this.simulable.setResOptim(resoptim);
		ltiempo.reiniciar();

		inicializarSalidasSim(rutaXml, rutaSals, servidor);

	}

	public void simular(int[] escenarios) {

		if (encadenado)
			CorridaHandler.getInstance().getCorridaActual().setSimulacionEncadenada(encadenado);

		simularEnHaz(escenarios);

		System.out.println("TERMINÓ SIMULACIÓN");
	}

	public void canceloSimulacion(){
		cancelarSimulacion = true;
	}

	public void simularEnHaz(int[] escenarios) {
		cancelarSimulacion = false;
		ProfilerBasicoTiempo pbt = ProfilerBasicoTiempo.getInstance();
		pbt.iniciarContador("TotalSimulacion");
//		pbt.crearContador("interior");	
//		pbt.crearContador("exterior");
//		pbt.crearContador("construirpljava");
//		pbt.crearContador("cargarpasoeiter");	
//		pbt.crearContador("pedirsalidalpsolve");
//		pbt.crearContador("postdespachopaso");
//		pbt.crearContador("llamadasalpsolveconst");
//		pbt.crearContador("resolverc");
//		pbt.crearContador("guardarresultado");
//		pbt.crearContador("construirdatossalidapl");		
//		pbt.crearContador("ResolverGLPK::crearProblema");
//		pbt.crearContador("ResolverGLPK::llamarSolver");
//		pbt.crearContador("ResolverGLPK::obtenerSolucion");

		simulable.inicializarSimulable();

		ltiempo.setSentidoTiempo(1);
//		pbt.continuarContador("exterior");
		// Los escenarios empiezan en 1 !!!!
		// i es un óndice en un vector de nómeros de escenarios

		// UI - UPDATE PROGRESS//
		int totalEscenarios = escenarios.length;
		double escenariosProcesados = 0;
		progresEscritor.set(false);
		// UI - UPDATE PROGRESS//

		for (int i = 0; i < escenarios.length && !cancelarSimulacion; ++i) {

			/**
			 * ATENCIóN MANOLO Para simulacion encadenadas La lónea de tiempo tiene que
			 * empezar el 1 de enero de un aóo
			 */

			ltiempo.reiniciar();

			simulable.inicializarEscenario(escenarios[i]);
			simulable.inicializarAzarParaUnEscenario(escenarios[i]);
			PasoTiempo paso = ltiempo.devuelvePasoActual();
			numpaso = ltiempo.getNumPaso();
			System.out.println("Simulando escenario: " + escenarios[i]);
			while (paso != null) {
//				System.out.println(" PASO " + numpaso);
//				if (numpaso == 20) {
//					int pp = 0;
//				}
				simulable.cargarPasoCorriente(numpaso, paso);
				simulable.inicializarPEPaso();
				simulable.inicializarParaPasoSimul();
				simulable.sortearProcEstDE(paso.getInstanteInicial() + Constantes.EPSILONSALTOTIEMPO); // htDE toma el

				/**
				 * ESTOS SON LOS MONTECARLOS, EVENTUALMENTE PODRóAMOS HACERLO DENTRO DEL
				 * SIMULADOR PASO PARA TENER LA POSIBILIDAD DE QUE LOS CONTROLES DE INCIDAN EN
				 * LOS SORTEOS *
				 */
				simulable.sortearProcEstNODE(paso.getInstanteInicial() + Constantes.EPSILONSALTOTIEMPO);

//				pbt.pausarContador("exterior");
//				pbt.continuarContador("interior");
				simulable.simularPaso(CorridaHandler.getInstance().isParalelo());
//				pbt.pausarContador("interior");
//				pbt.continuarContador("exterior");

//				pbt.continuarContador("guardarresultado");
				simulable.guardarResultadoPaso();
//				pbt.pausarContador("guardarresultado");

				simulable.actualizarParaProximoPaso();

				ltiempo.avanzarPaso();
				paso = ltiempo.devuelvePasoActual();
				numpaso++;

			}

			simulable.finalizarEscenario(enDisco);

			// UI - UPDATE PROGRESS//
			progress.set(++escenariosProcesados / totalEscenarios);
			// UI - UPDATE PROGRESS//
		}
		if(cancelarSimulacion){
			cancelarSimulacion = false;
			ltiempo.reiniciar();
			return;
		}

//		pbt.terminarContador("exterior");
//		pbt.imprimirTiempo("exterior");
//		pbt.terminarContador("interior");
//		pbt.imprimirTiempo("interior");
//		pbt.terminarContador("cargarpasoeiter");
//		pbt.imprimirTiempo("cargarpasoeiter");	
//		pbt.terminarContador("construirpljava");
//		pbt.imprimirTiempo("construirpljava");
//		pbt.terminarContador("llamadasalpsolveconst");
//		pbt.imprimirTiempo("llamadasalpsolveconst");
//		pbt.terminarContador("resolverc");
//		pbt.imprimirTiempo("resolverc");
//		pbt.terminarContador("pedirsalidalpsolve");
//		pbt.imprimirTiempo("pedirsalidalpsolve");
//		pbt.terminarContador("guardarresultado");
//		pbt.imprimirTiempo("guardarresultado");
//		pbt.terminarContador("construirdatossalidapl");
//		pbt.imprimirTiempo("construirdatossalidapl");
//		pbt.terminarContador("postdespachopaso");
//		pbt.imprimirTiempo("postdespachopaso");
//
//		pbt.terminarContador("ResolverGLPK::crearProblema");
//		pbt.imprimirTiempo("ResolverGLPK::crearProblema");
//		pbt.terminarContador("ResolverGLPK::llamarSolver");
//		pbt.imprimirTiempo("ResolverGLPK::llamarSolver");
//		pbt.terminarContador("ResolverGLPK::obtenerSolucion");
//		pbt.imprimirTiempo("ResolverGLPK::obtenerSolucion");
//		

		progresEscritor.set(true);
		simulable.finalizarSimulable();
		ltiempo.reiniciar();
        progresEscritor.set(false);
//		if (!CorridaHandler.getInstance().getCorridaActual().isEscenariosSerializados()) {
//			borrarSalidasEscenarios();
//		}
		pbt.pausarContador("TotalSimulacion");
		pbt.imprimirTiempos(this.dirSalidasSim);
		System.out.println("Terminó simulación");
	}

	public void simularPEs(int[] escenarios) {

		simulable.inicializarSimulable();
		Azar azar = simulable.devuelveAzar();

		// Recibe del usuario los nombres de los procesos estocásticos a simular
		// como una lista de nombres de procesos separada por blancos
		int ancho = 500;
		int alto = 100;

//		String nombres = utilsVentanas.VentanaEntradaString.leerTexto("Entre nombres de PE separados por blancos. De lo contrario procesa todos", 
//				ancho, alto);
//		ancho = 200;
//		int anini = Integer.parseInt(utilsVentanas.VentanaEntradaString.leerTexto("Entre el primer año a simular, a partir del inicio del año", ancho, alto));
//		int cantAnios = Integer.parseInt(utilsVentanas.VentanaEntradaString.leerTexto("Entre la cantidad de años a simular", ancho, alto));
//		String tipoPaso = utilsVentanas.VentanaEntradaString.leerTexto("Entre: S por paso semanal, D paso diario, H paso horario", ancho, alto);
//		int cantEsc = Integer.parseInt(utilsVentanas.VentanaEntradaString.leerTexto("Entre la cantidad de escenarios ", ancho, alto));

		String nombres = "eoloSolBootstrap_serie5anos";
		int anini = 2021;
		int cantAnios = 1100;
		String tipoPaso = "D"; // es el paso de la simulación no del PE
		int cantEsc = escenarios.length;
		String dirArchChan = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\PRUEBAS\\AYUDA-PRUEBAS-ANA-SET2021\\PedidoEoloYSolar26Oct2021\\Salidas/observPorPaso.xlt";
		String dirLT = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\PRUEBAS\\AYUDA-PRUEBAS-ANA-SET2021\\PedidoEoloYSolar26Oct2021\\Salidas/lineaDeTiempo.xlt";

		DirectoriosYArchivos.siExisteElimina(dirArchChan);
		Hashtable<String, double[]> pondRachas = new Hashtable<String, double[]>();
		double[] pond = new double[3];
		/**
		 * Con los ponderadores siguientes y la opción suma en devuelvePromOSuma se
		 * obtiene la energía hidro entrante anual en las series de aportes.
		 */
		pond[0] = (53.4 + 33.96 + 70.36) * (3600 * 24 * 7) / 1E9;
		pond[1] = 70.36 * (3600 * 24 * 7) / 1E9;
		pond[2] = 32.2 * (3600 * 24 * 7) / 1E9;
		pondRachas.put("varAportes", pond);
//		pond[0] = (53.4+33.96+70.36)*(3600*24*7)/1E9;
//		pond[1] = 70.36 *(3600*24*7)/1E9;
//		pond[2] = 32.2 *(3600*24*7)/1E9; 
		pond[0] = 1500.0;
		pond[1] = 300.0;
		pondRachas.put("mdpBootstrap", pond);

		String[] nombresPESalida = nombres.split(" ");
		String nompaso = null;
		if (tipoPaso.equalsIgnoreCase("S")) {
			nompaso = utilitarios.Constantes.PASO_SEMANAL;
		} else if (tipoPaso.equalsIgnoreCase("D")) {
			nompaso = utilitarios.Constantes.PASO_DIARIO;
		} else if (tipoPaso.equalsIgnoreCase("H")) {
			nompaso = utilitarios.Constantes.PASO_HORARIO;
		}

		String tipoSimulPE = utilitarios.Constantes.ESC_UNICO;
		if (cantEsc > 1)
			tipoSimulPE = utilitarios.Constantes.ESC_MULT;
		int anfin = anini + cantAnios - 1;
		DatosLineaTiempo dlt = interfaz.GeneradorDatosLineaDeTiempo.parsearCadenaBloques(anini, anfin, nompaso);
		String stiempoInicial = "01 01 " + anini + " 00:00:00";
		String stiempoFinal = "31 12 " + anfin + " 23:59:59";
		GregorianCalendar tiempoInicialCorrida = Utilitarios.stringToGregorianCalendar(stiempoInicial, "dd MM yyyy");
		GregorianCalendar tiempoFinalCorrida = Utilitarios.stringToGregorianCalendar(stiempoFinal, "dd MM yyyy");

		LineaTiempo lt = new LineaTiempo(dlt, tiempoInicialCorrida, tiempoFinalCorrida);

		DirectoriosYArchivos.siExisteElimina(dirLT);
		DirectoriosYArchivos.agregaTexto(dirLT, lt.toString());

		this.setLtiempo(lt);
		((SimuladorPaso) this.getSimulable()).getCorrida().setLineaTiempo(lt);

		ArrayList<ProcesoEstocastico> listaPEs = new ArrayList<ProcesoEstocastico>();
		if (nombresPESalida.length == 0) {
			listaPEs.addAll(simulable.devuelvePEsSimulacion());
		} else {
			for (String npe : nombresPESalida) {
				listaPEs.add(azar.devuelveProcesoDeNombre(npe));
			}
		}

		int cantPasos = ltiempo.getCantidadPasos();
		ProcesadorSimulacionPEs procSPE = new ProcesadorSimulacionPEs(ltiempo, listaPEs, cantPasos, tipoSimulPE,
				cantEscenarios, pondRachas);

		ltiempo.setSentidoTiempo(1);
		int totalEscenarios = escenarios.length;
		double escenariosProcesados = 0;

		for (int i = 0; i < escenarios.length; ++i) {

			ltiempo.reiniciar();

			System.out.println("COMIENZA ESCENARIO " + i);
			simulable.inicializarEscenario(escenarios[i]);
			procSPE.inicializarEscenario(escenarios[i]);
			simulable.inicializarAzarParaUnEscenario(escenarios[i]);
			PasoTiempo paso = ltiempo.devuelvePasoActual();
			numpaso = ltiempo.getNumPaso();
			System.out.println("Simulando escenario: " + escenarios[i]);

			while (paso != null) {
				System.out.println("COMIENZA PASO " + numpaso);
				simulable.cargarPasoCorriente(numpaso, paso);

				while (paso != null) {
					if (numpaso % 1000 == 0)
						System.out.println("COMIENZA PASO " + numpaso);
					simulable.cargarPasoCorriente(numpaso, paso);
					simulable.inicializarPEPaso();

					// Sortea los procesos DE, transición y valores de las VA
					simulable.sortearProcEstDE(paso.getInstanteInicial() + Constantes.EPSILONSALTOTIEMPO); // htDE toma
																											// el
					// Sortea los procesos que no son DE, y que no son muestreados.
					simulable.sortearProcEstNODE(paso.getInstanteInicial() + Constantes.EPSILONSALTOTIEMPO);

					// Sortea los procesos que no son DE y que son muestreados
					long[] instantesMuestreo = simulable.devuelveInstantesMuestreo();
					for (ProcesoEstocastico p : azar.getProcesos()) {
						p.muestrearVariablesAleats(instantesMuestreo);
					}
					for (long lo : instantesMuestreo) {
						procSPE.getInstantesDeMuestreoSimulacion().add(lo);
					}

					procSPE.guardarResultadoPasoPE(numpaso, instantesMuestreo);
//				simulable.actualizarParaProximoPaso();

					ltiempo.avanzarPaso();
					paso = ltiempo.devuelvePasoActual();
					numpaso++;

				}
				procSPE.finalizarEscenario();
				simulable.finalizarEscenario(enDisco);

				// UI - UPDATE PROGRESS//
				progress.set(++escenariosProcesados / totalEscenarios);
				// UI - UPDATE PROGRESS//
			}

		}
		String dirSalidas = simulable.dameDirSalidas();
		procSPE.finalizarSimulacionPEs(dirSalidas);
	}

	public void simularEnHazServidor(int[] escenarios) {
		System.out.println("Se resuelven escenarios: " + Arrays.toString(escenarios));
		ltiempo.setSentidoTiempo(1);
		// Los escenarios empiezan en 1 !!!!
		// i es un óndice en un vector de nómeros de escenarios

		for (int i = 0; i < escenarios.length; ++i) {

			/**
			 * ATENCIóN MANOLO Para simulacion encadenadas La lónea de tiempo tiene que
			 * empezar el 1 de enero de un aóo
			 */

			ltiempo.reiniciar();

			/*
			 * ATENCION MANOLO Los dos mótodos siguientes hay que agregarle parómetro
			 * ENCADENADO
			 * 
			 */
			simulable.inicializarEscenario(escenarios[i]);
			simulable.inicializarAzarParaUnEscenario(escenarios[i]);
			PasoTiempo paso = ltiempo.devuelvePasoActual();
			numpaso = ltiempo.getNumPaso();
			System.out.println("Simulando escenario: " + escenarios[i]);
			while (paso != null) {

				simulable.cargarPasoCorriente(numpaso, paso);

				simulable.inicializarPEPaso();
				// simulable.sortearProcEstDE(paso.getInstanteInicial()+Constantes.EPSILONSALTOTIEMPO);
				// //htDE toma el valor luego del sorteo, lo guarda el simulable

				/**
				 * ESTOS SON LOS MONTECARLITOS, EVENTUALMENTE PODRóAMOS HACERLO DENTRO DEL
				 * SIMULADOR PASO PARA TENER LA POSIBILIDAD DE QUE LOS CONTROLES DE INCIDAN EN
				 * LOS SORTEOS *
				 */
				simulable.sortearProcEstNODE(paso.getInstanteInicial() + Constantes.EPSILONSALTOTIEMPO);

				simulable.simularPaso(CorridaHandler.getInstance().isParalelo());
				simulable.guardarResultadoPaso();

				simulable.actualizarParaProximoPaso();

				ltiempo.avanzarPaso();
				paso = ltiempo.devuelvePasoActual();
				numpaso++;

			}

			simulable.finalizarEscenarioServidor();
		}

	}

	public LineaTiempo getLtiempo() {
		return ltiempo;
	}

	public void setLtiempo(LineaTiempo ltiempo) {
		this.ltiempo = ltiempo;
	}

	public int getCantEscenarios() {
		return cantEscenarios;
	}

	public void setCantEscenarios(int cantEscenarios) {
		this.cantEscenarios = cantEscenarios;
	}

	public long getInstanteInicial() {
		return instanteInicial;
	}

	public void setInstanteInicial(long instanteInicial) {
		this.instanteInicial = instanteInicial;
	}

	public long getInstanteFinal() {
		return instanteFinal;
	}

	public void setInstanteFinal(long instanteFinal) {
		this.instanteFinal = instanteFinal;
	}

	public long getInstanteInicialEstudio() {
		return instanteInicialEstudio;
	}

	public void setInstanteInicialEstudio(int instanteInicialEstudio) {
		this.instanteInicialEstudio = instanteInicialEstudio;
	}

	public boolean isEncadenado() {
		return encadenado;
	}

	public void setEncadenado(boolean encadenado) {
		this.encadenado = encadenado;
	}

	public ResOptim getResoptim() {
		return resoptim;
	}

	public void setResoptim(ResOptim resoptim) {
		this.resoptim = resoptim;
		this.simulable.setResOptim(resoptim);
	}

	public Simulable getSimulable() {
		return simulable;
	}

	public void setSimulable(Simulable simulable) {
		this.simulable = simulable;
	}

	public boolean isResoptimExterno() {
		return resoptimExterno;
	}

	public void setResoptimExterno(boolean resoptimExterno) {
		this.resoptimExterno = resoptimExterno;
	}

	public String getDirSalidasSim() {
		return dirSalidasSim;
	}

	public void setDirSalidasSim(String dirSalidasSim) {
		this.dirSalidasSim = dirSalidasSim;
	}

	/**
	 * Reinicializa los archivos de salida salidaDetalladaSP.txt - tabla con el
	 * despacho por paso y poste en formato tabular salidaAvisosPaso.txt - avisos de
	 * resultados raros por paso, por ejemplo destrucción de agua
	 * modeloInfactible.lp - salida del resolvedor lineal LPsolve u otro con el
	 * problema infactible para investigar
	 * 
	 * @param rutaXml
	 * @param rutaSals: si es null, se emplea el directorio de salidas ya existente
	 *                  en el atributo dirSalidasSim
	 */
	public void inicializarSalidasSim(String rutaXml, String rutaSals, boolean servidor) {

		// Fecha y hora actual
		Calendar fecha = Calendar.getInstance();
		int anio = fecha.get(Calendar.YEAR);
		int mes = fecha.get(Calendar.MONTH) + 1;
		int dia = fecha.get(Calendar.DAY_OF_MONTH);
		int hora = fecha.get(Calendar.HOUR_OF_DAY);
		int minuto = fecha.get(Calendar.MINUTE);
		int segundo = fecha.get(Calendar.SECOND);
		String dirCompleto = null;
		String dirRaiz = null;
		String dirNuevo = null;
		String fechaParaCorrida = anio + "-" + mes + "-" + dia;
		String horaParaCorrida = hora + "-" + minuto + "-" + segundo;
		CorridaHandler ch = CorridaHandler.getInstance();
		ch.getCorridaActual().setFechaEjecucion(fechaParaCorrida);
		ch.getCorridaActual().setHoraEjecucion(horaParaCorrida);
		if (rutaSals != null) {
			// Se crea el directorio de Salidas
			dirRaiz = rutaSals;
			dirNuevo = anio + "-" + mes + "-" + dia + "-" + hora + "-" + minuto + "-" + segundo + "-SIM";
			if (CorridaHandler.getInstance().isParalelo())
				dirNuevo += "-PAR";
			dirCompleto = dirRaiz + "/" + dirNuevo;
			dirSalidasSim = dirCompleto;
			this.simulable.setDirSalidas(dirCompleto);
			ClienteHandler.getInstance().setSimulable(simulable);

			if (!servidor) {
				DirectoriosYArchivos.creaDirectorio(dirRaiz, dirNuevo);
				DirectoriosYArchivos.creaDirectorio(dirSalidasSim, utilitarios.Constantes.DIR_SERIALIZADOS);
			}
			String[] partes = rutaXml.split("\\\\");
			String nombreXml = partes[partes.length - 1];
			try {
				if (!servidor) {
					DirectoriosYArchivos.copy2(rutaXml, dirCompleto + "/" + nombreXml);
					String lt = ltiempo.toString();
					DirectoriosYArchivos.grabaTexto(dirCompleto + "/lineaTiempo.xlt", lt);
				}
			} catch (Exception E) {
				if (CorridaHandler.getInstance().isParalelo()) {
					// PizarronRedis pp = new PizarronRedis();
					// pp.matarServidores();
				}
				System.exit(1);
			}
		} else {
			// Se toma el directorio de Salidas ya existente
			dirCompleto = dirSalidasSim;
		}

		this.simulable.setDirSalidas(dirCompleto);

		String nombreArchivoSalida = dirCompleto + "salidaDetalladaSP.xlt";
		boolean existe = DirectoriosYArchivos.existeArchivo(nombreArchivoSalida);
		if (existe)
			DirectoriosYArchivos.eliminaArchivo(nombreArchivoSalida);

		String nombreArchivoPL = dirCompleto + "salidaAvisosPaso.txt";
		existe = DirectoriosYArchivos.existeArchivo(nombreArchivoPL);
		if (existe)
			DirectoriosYArchivos.eliminaArchivo(nombreArchivoPL);

		String nombreArchInfactible = dirCompleto + "\\";
//		existe = DirectoriosYArchivos.existeArchivo(nombreArchInfactible);
//		if (existe) DirectoriosYArchivos.eliminaArchivo(nombreArchInfactible);			
//		

	}

	public void simular(int dameEscenario) {
		// TODO Auto-generated method stub

	}

	private void borrarSalidasParalelo() {
//		for (int i = 1; i <= CorridaHandler.getInstance().getCorridaActual().getCantEscenarios(); ++i) {
//			DirectoriosYArchivos
//					.eliminaArchivo(Constantes.RUTA_SALIDA_SIM_PARALELA + "\\escenario" + Integer.toString(i));
//		}

	}

	private void borrarSalidasEscenarios() {
		SimuladorPaso sp = (SimuladorPaso)this.getSimulable();
		for (int i = 1; i <= CorridaHandler.getInstance().getCorridaActual().getCantEscenarios(); ++i) {
				DirectoriosYArchivos.eliminaArchivo(sp.getDirSalidas()+"/"+ Constantes.DIR_SERIALIZADOS + "\\escenario" + Integer.toString(i));
		}
		DirectoriosYArchivos.eliminaArchivo(sp.getDirSalidas()+"/"+ Constantes.DIR_SERIALIZADOS + "\\cosmar_cron");
		DirectoriosYArchivos.eliminaArchivo(sp.getDirSalidas()+"/"+ Constantes.DIR_SERIALIZADOS + "\\datosResumenSP");
	}

	public void simularCliente(int cantEscenarios) {
		if (CorridaHandler.getInstance().isParalelo()) {
			borrarSalidasParalelo();
		}

		simulable.inicializarSimulable();
//
		ltiempo.setSentidoTiempo(1);
//				
		clienteH.cargarOperacion(Constantes.SIMULAR);
		int cantServidores = clienteH.obtenercantServidores();
		System.out.println("CANTIDAD DE SERVIDORES: " + cantServidores);
		System.out.println("TOTAL: " + cantEscenarios);

		int cantEscenariosPorPaquete = (int) Math.ceil(cantEscenarios / (double) cantServidores);

		clienteH.cargarPaquetesEscenarios(cantEscenarios, cantEscenariosPorPaquete);
		System.out.println("SE CARGARON LOS PAQUETES ESCENARIOS");

		clienteH.cargarPaquetesEscenariosAResolver();
		System.out.println("SE CARGARON LOS PAQUETES ESCENARIOS A RESOLVER");

		DirectoriosYArchivos.creaDirectorio(clienteH.getSimulable().dameDirSalidas(), "Paralelismo");

		clienteH.resolucionPaquetesEscenarios();

		simulable.finalizarSimulable();

		clienteH.finalizarSimulacion();

		if (CorridaHandler.getInstance().isParalelo()) {
			borrarSalidasParalelo();
		}

		System.out.println("TERMINO LA SIMULACIÓN");
	}

	public ServidorHandler getServidorH() {
		return servidorH;
	}

	public void setServidorH(ServidorHandler servidorH) {
		this.servidorH = servidorH;
	}

	public void simularServidor() {
		boolean termine = false;
		simulable.inicializarSimulable();
		while (!termine) {
			try {
				Thread.sleep(1);
				termine = servidorH.hayQueFinalizarSimulacion();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (termine)
				break;

			PaqueteEscenarios paq = servidorH.obtenerPaqueteEscenariosAResolver();

			if (paq == null) {
				continue;
			}

			System.out.print("Empiezo a Resolver: ");
			paq.imprimirPaquete();

			int escenarioIni = paq.getEscenarioIni();
			int escenarioFin = paq.getEscenarioFin();
			int tamanio = escenarioFin - escenarioIni;
			int[] escenarios = new int[tamanio];
			for (int i = 0; i < tamanio; ++i) {
				escenarios[i] = escenarioIni + i;
			}

			simularEnHazServidor(escenarios);

			servidorH.pasarPaqueteEscenarioAResuelto(paq);

		}

		System.out.println("Terminó la simulación");

	}

	public void simularClienteDesdeDirectorio(int cantEscenarios2, String ruta) {

		simulable.inicializarSimulable();
//
		ltiempo.setSentidoTiempo(1);

		clienteH.finalizarSimulacion();

		simulable.finalizarSimulableDirectorio(ruta);

	}
	
	public void simularDesdeDirectorio(String ruta) {

		simulable.inicializarSimulable();

		ltiempo.setSentidoTiempo(1);

		simulable.finalizarSimulableDirectorio(ruta);

	}

}
