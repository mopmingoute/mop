/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Optimizador is part of MOP.
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

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;

import datatypesSalida.DatosPEsUnPaso;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import logica.CorridaHandler;
import parque.Azar;
import parque.Corrida;
import parque.Participante;
import pizarron.ClienteHandler;
import pizarron.PizarronRedis;
import pizarron.ServidorHandler;
import procesosEstocasticos.ProcesadorSorteosOptimPEs;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.ProfilerBasicoTiempo;

/**
 * Clase encargada hacer la optimización
 * 
 * @author ut602614
 *
 */

public class Optimizador<T extends Optimizable> {

	private LineaTiempo ltiempo;
	private int numpaso;
	private Optimizable optimizable;
	private ClienteHandler clienteH;
	private ServidorHandler servidorH;

	private boolean canceloOptimizacion;
	/**
	 * Common random numbers: es la tócnica de reducción de varianza por la que se
	 * usan los mismos nómeros aleatorios para estimar diferencias de una VA al
	 * variar un parómetro del sistema. Se usa acó para estimar las diferencias de
	 * valores de Bellman.
	 *
	 * Los procesos estocósticos se dividen en:
	 *
	 * - Los PE discretos exhaustivos, que por lo tanto no tienen sorteos Montecarlo
	 *
	 * - Los PE no discretos exhaustivos que por lo tanto tienen sorteos Montecarlo
	 * y que no aportan VE a la optimización (si bien pueden tener sus propias VE).
	 * peMontNoEstadoOptim Para las VA de estos procesos se sortean valores comunes
	 * a todas las realizaciones Montecarlo.
	 *
	 * - Los PE no discretos exhaustivos que por lo tanto tienen sorteos Montecarlo
	 * y que SI APORTAN VE a la optimización. Para las VA de estos procesos se toman
	 * innovaciones aleatorias comunes peMontEstadoOptim
	 *
	 *
	 */

	public static ProfilerBasicoTiempo prof = ProfilerBasicoTiempo.getInstance();

	private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();

	public ReadOnlyDoubleProperty progressProperty() {
		return progress;
	}

	public Optimizador() {
	}

	public void inicializarOptimizador(LineaTiempo ltiempo, Optimizable optimizable, String ruta, String dirSal, boolean servidor) {
		if (CorridaHandler.getInstance().isParalelo()) {
			this.clienteH = ClienteHandler.getInstance();
			this.servidorH = ServidorHandler.getInstance();
		}
		this.ltiempo = ltiempo;
		this.optimizable = optimizable;

		if (dirSal != null) {
			inicializarSalidasOpt(ruta, dirSal, servidor);
		}
	}

	public void cancelarOptimizacion(){
		canceloOptimizacion = true;
	}

	public ResOptim optimizar() {
		prof.iniciarContador("MONO: TOTAL OPTIMIZAR");
		prof.iniciarContador("MONO: OPTIMIZAR ANTES OPTIMIZAR PASO");
		System.out.println("Comienza Optimización");
		ltiempo.prepararParaOptimizar();

		optimizable.inicializarOptimizable();

		optimizable.inicializarAzarParaOptimizacion();

		ResOptim resoptim = optimizable.devuelveResOptim();

		PasoTiempo paso = ltiempo.devuelvePasoActual();

		numpaso = ltiempo.getNumPaso();

		// UI - UPDATE PROGRESS//
		int totalPasos = numpaso;
		double pasosProcesados = 0;
		// UI - UPDATE PROGRESS//
		prof.pausarContador("MONO: OPTIMIZAR ANTES OPTIMIZAR PASO");
		while (numpaso >= 0 && !canceloOptimizacion) {
			prof.iniciarContador("MONO: OPTIMIZAR ANTES OPTIMIZAR PASO");

			System.out.println("Optimizar paso: " + numpaso);
			
			/**
			 * TODO Escritura para detener optimización en un paso
			 */
//			if(numpaso == 90) {
//				System.out.println("Llegó a paso 90");
//			}
			
			

			paso = ltiempo.devuelvePasoActual();

			optimizable.cargarPasoCorriente(numpaso, paso);

			optimizable.determinarInstantesMuestreo();

			/**
			 * Prepara los PE para los procesos Carga instante inicial del paso de la
			 * optimización Carga indAnio del paso
			 */
			optimizable.inicializarPEPasoOptim();

			optimizable.sortearInnovPEDE(); // sortea innovaciones para los PE con VE discretas exhaustivas
											// ATENCION: las innovaciones se usan para sortear valores de las VA
											// no para sortear transiciones, ya que son exhaustivas.

			optimizable.sortearInnovMontPEVE(); // sortea innovaciones para PE con VE en la optimización

			optimizable.sortearVAMontPENoVE(); // sortea valores VA para PE sin VE en la optimización

			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("Llamada a optimizable paso " + numpaso);

			prof.pausarContador("MONO: OPTIMIZAR ANTES OPTIMIZAR PASO");
			Optimizador.prof.iniciarContador("MONO:TOTAL OPTIMIZAR PASO");
			optimizable.optimizarPaso();
			Optimizador.prof.pausarContador("MONO:TOTAL OPTIMIZAR PASO");
			prof.iniciarContador("MONO: OPTIMIZAR DESPUÉS OPTIMIZAR PASO");

			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("Terminó llamada a optimizable paso " + numpaso);
			if (numpaso > 0) {
				optimizable.actualizarParaPasoAnterior();
				numpaso = ltiempo.getNumPaso();
				// NO HAY QUE LLAMAR retrocederPaso porque eso ya lo hace
				// actualizarParaPasoAnterior()
			} else {
				optimizable.finalizarOptimizacion();
				numpaso = -1;
			}

			// UI - UPDATE PROGRESS//
			progress.set(++pasosProcesados / totalPasos);
			// UI - UPDATE PROGRESS//
			prof.pausarContador("MONO: OPTIMIZAR DESPUÉS OPTIMIZAR PASO");
		}
		if(canceloOptimizacion){
			canceloOptimizacion = false;
			return null;
		}
		prof.iniciarContador("MONO: OPTIMIZAR DESPUÉS OPTIMIZAR PASO");

		/**
		 * Se serializan las tablas de valores de Bellman y de control en disco si estón
		 * en memoria
		 */

		optimizable.guardarTablasResOptimEnDisco();
		if (Constantes.NIVEL_CONSOLA > 1)
			System.out.println("Se generó resoptim serializado en disco");

		System.out.println("TERMINO LA OPTIMIZACIóN");
		prof.pausarContador("MONO: OPTIMIZAR DESPUÉS OPTIMIZAR PASO");
		prof.pausarContador("MONO: TOTAL OPTIMIZAR");
		OptimizadorPaso op = (OptimizadorPaso) optimizable;
		prof.imprimirTiempos(op.getDirSalidas());

		return resoptim;
	}
	
	
	
	
	public void sortearPEOptim() {	
				
//		int ancho = 500;
//		int alto = 100;
//		
//		String nombres = utilsVentanas.VentanaEntradaString.leerTexto("Entre nombres de PE separados por blancos. De lo contrario procesa todos", 
//				ancho, alto);
		
		String nombres = "varAportesOptim";
		String dirArchChan = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/PRUEBAS/observPorPaso.xlt";
		String dirLT = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/PRUEBAS/lineaDeTiempo.xlt";
		DirectoriosYArchivos.siExisteElimina(dirArchChan);
		
		
		String[] nombresPESalida = nombres.split(" ");

		ltiempo.prepararParaOptimizar();			
		optimizable.inicializarOptimizable(); 			
		optimizable.inicializarAzarParaOptimizacion();
			
		Azar azar = optimizable.devuelveAzar();
		Corrida corrida = azar.getCorrida();  // ESTO ROMPE EL ENCAPSULAMIENTO
		ArrayList<ProcesoEstocastico> listaPEs = new ArrayList<ProcesoEstocastico>();
		if(nombresPESalida.length==0){
			listaPEs.addAll(azar.getProcesosOptim());
		}else{
			for(String npe: nombresPESalida){
				listaPEs.add(azar.devuelveProcesoDeNombre(npe));
			}		
		}

		ProcesadorSorteosOptimPEs procSorteos = new ProcesadorSorteosOptimPEs(ltiempo, listaPEs);		
						
	//	ResOptim resoptim = optimizable.devuelveResOptim();	
		
		PasoTiempo paso = ltiempo.devuelvePasoActual();

		numpaso = ltiempo.getNumPaso();


		
		while (numpaso>=0) {	
			
			System.out.println("Sortea procesos del paso: " + numpaso);
			
			paso = ltiempo.devuelvePasoActual();
			
			optimizable.cargarPasoCorriente(numpaso, paso);
			
			optimizable.determinarInstantesMuestreo();
			
			/**
			 * ESTO SUSTITUYE AL METODO DE OPTIMIZABLE inicializarPEPasoOptim() 
			 * Prepara los PE para los procesos
			 * Carga instante inicial del paso de la optimización
			 * Carga indAnio del paso
			 */
			int cantSortMontecarlo = azar.getCorrida().getCantSorteosMont().getValor(paso.getInstanteInicial());
			long instIniPasoOp = paso.getInstanteInicial();
			for (ProcesoEstocastico pe : listaPEs) {
				pe.setInstIniPasoOptim(instIniPasoOp);
				pe.prepararPasoOptim(cantSortMontecarlo);
				int pasoPEInstanteInicial = pe.pasoDelAnio(instIniPasoOp); // Los pasos empiezan en cero;
				long instanteInicialAnio = pe.instanteInicialAnioDeInstante(instIniPasoOp);
				pe.setInstanteCorrienteFinal(instanteInicialAnio + pasoPEInstanteInicial * pe.getDurPaso());
				pe.setInstanteCorrienteInicial(pe.getInstanteCorrienteFinal() - pe.getDurPaso());
			}
					
			
			optimizable.sortearInnovPEDE();   	// sortea innovaciones para los PE con VE discretas exhaustivas
												// ATENCION: las innovaciones se usan para sortear valores de las VA
												// no para sortear transiciones, ya que son exhaustivas.
			
			optimizable.sortearInnovMontPEVE();   // sortea innovaciones para PE con VE en la optimización		
			
			optimizable.sortearVAMontPENoVE();  // sortea valores VA para PE sin VE en la optimización						
			
			if (Constantes.NIVEL_CONSOLA > 1) System.out.println("Llamada a optimizable paso " + numpaso);
			
			DatosPEsUnPaso dat1P = optimizable.sortearPEsOptimPaso(procSorteos); 
			
			procSorteos.getResultados().add(dat1P);
			procSorteos.getNumerosPasos().add(numpaso);
				
			if (Constantes.NIVEL_CONSOLA > 1) System.out.println("Terminó llamada a optimizable paso " + numpaso);		
			if(numpaso>0){											
//				optimizable.actualizarParaPasoAnterior();
				ltiempo.retrocederPaso();
				paso = ltiempo.devuelvePasoActual();

				numpaso = ltiempo.getNumPaso();
				
				// Actualiza en el optimizable las variables de comportamiento que rigen
				// en el paso t-1
				// Actualiza la foto de las variables de comportamiento general que se
				// encuentran en el Comportamiento Simulación
				for (Participante p : corrida.getParticipantes())
					p.actualizarVarsCompGeneral(paso.getInstanteInicial());
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
				
				
				numpaso = ltiempo.getNumPaso();
				// NO HAY QUE LLAMAR retrocederPaso porque eso ya lo hace actualizarParaPasoAnterior()							
			}else{
				numpaso = -1;
			}
		}
		
		OptimizadorPaso op = (OptimizadorPaso)optimizable;  // ROMPE EL ENCAPSULAMIENTO
		procSorteos.imprimirSorteosOptim(op.getDirSalidas());

		System.out.println("TERMINO EL SORTEO DE PE DE OPTIMIZACION");

		
	}
	
	

	/**
	 * Crea el directorio de salidas de la optimización
	 * 
	 * @param rutaXml
	 * @param rutaSals
	 */
	public void inicializarSalidasOpt(String rutaXml, String rutaSals, boolean servidor) {

		// Fecha y hora actual
		Calendar fecha = Calendar.getInstance();
		int anio = fecha.get(Calendar.YEAR);
		int mes = fecha.get(Calendar.MONTH) + 1;
		int dia = fecha.get(Calendar.DAY_OF_MONTH);
		int hora = fecha.get(Calendar.HOUR_OF_DAY);
		int minuto = fecha.get(Calendar.MINUTE);
		int segundo = fecha.get(Calendar.SECOND);

		String dirRaiz = rutaSals;
		String dirNuevo = anio + "-" + mes + "-" + dia + "-" + hora + "-" + minuto + "-" + segundo + "-OPT";

		if (CorridaHandler.getInstance().isParalelo())
			dirNuevo += "-PAR";

		String dirCompleto = dirRaiz + "/" + dirNuevo;
		this.optimizable.setDirSalidas(dirCompleto);
		ClienteHandler.getInstance().setOptimizable(optimizable);
		
		if(!servidor)
			DirectoriosYArchivos.creaDirectorio(dirRaiz, dirNuevo);

		String[] partes = rutaXml.split("\\\\");
		String nombreXml = partes[partes.length - 1];
		try {
			if(!servidor)
				DirectoriosYArchivos.copy2(rutaXml, dirCompleto + "/" + nombreXml);
			//	DirectoriosYArchivos.grabaTexto(dirCompleto + "/lineaTiempo.xlt", lt);
		} catch (Exception E) {
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores(); 
			}
			System.exit(1);
		}
	
	}

	public ResOptim optimizarCliente() {
		
		prof.iniciarContador("CLI: TOTAL OPTIMIZAR");
		prof.iniciarContador("CLI: OPTIMIZAR ANTES OPTIMIZAR PASO");
		ltiempo.prepararParaOptimizar();
		numpaso = ltiempo.getNumPaso();

		clienteH.escribirPasoActual(numpaso);

		clienteH.cargarOperacion(Constantes.OPTIMIZAR);

		optimizable.inicializarOptimizable();

		optimizable.inicializarAzarParaOptimizacion();

		ResOptim resoptim = optimizable.devuelveResOptim();

		PasoTiempo paso = ltiempo.devuelvePasoActual();

		prof.pausarContador("CLI: OPTIMIZAR ANTES OPTIMIZAR PASO");
		
		DirectoriosYArchivos.creaDirectorio(clienteH.getOptimizable().getDirSalidas(), "Paralelismo");
		
		while (numpaso >= 0) {
			prof.iniciarContador("CLI: OPTIMIZAR ANTES OPTIMIZAR PASO");
			System.out.println("Optimización Cliente: " + numpaso);
			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("Optimizar: comienza preparación paso " + numpaso);

			paso = ltiempo.devuelvePasoActual();

			// INTERFAZ PARALELISMO

			clienteH.escribirPasoActual(numpaso);
			// FIN INTERFAZ PARALELISMO
			// System.out.println(" PASO ACTUAL CLIENTE : " + numpaso);

			optimizable.cargarPasoCorriente(numpaso, paso);

			optimizable.determinarInstantesMuestreo();

			/**
			 * Prepara los PE para los procesos Carga instante inicial del paso de la
			 * optimización Carga indAnio del paso
			 */
			optimizable.inicializarPEPasoOptim();

			optimizable.sortearInnovPEDE(); // sortea innovaciones para los PE con VE discretas exhaustivas
											// ATENCION: las innovaciones se usan para sortear valores de las VA
											// no para sortear transiciones, ya que son exhaustivas.

			optimizable.sortearInnovMontPEVE(); // sortea innovaciones para PE con VE en la optimización

			optimizable.sortearVAMontPENoVE(); // sortea valores VA para PE sin VE en la optimización

			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("Llamada a optimizable paso " + numpaso);
//			DirectoriosYArchivos
//					.agregaTexto(
//							Constantes.ruta_log_paralelismo + "logCliente"
//									+ ManagementFactory.getRuntimeMXBean().getName() + ".txt",
//							"INICIO OPTIMIZAR CLIENTE , PASO: " + numpaso);

			prof.pausarContador("CLI: OPTIMIZAR ANTES OPTIMIZAR PASO");
			prof.iniciarContador("CLI: OPTIMIZAR PASO");

			optimizable.optimizarPasoCliente();

			prof.pausarContador("CLI: OPTIMIZAR PASO");
			prof.iniciarContador("CLI: OPTIMIZAR DESPUÉS OPTIMIZAR PASO");

			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("Terminó llamada a optimizable paso " + numpaso);
			if (numpaso > 0) {
				optimizable.actualizarParaPasoAnteriorCliente();
				numpaso = ltiempo.getNumPaso();
			} else {
				optimizable.finalizarOptimizacion();
				numpaso = -1;
			}
			prof.pausarContador("CLI: OPTIMIZAR DESPUÉS OPTIMIZAR PASO");
		}
				
		prof.pausarContador("CLI: TOTAL OPTIMIZAR");
		OptimizadorPaso op = (OptimizadorPaso) optimizable;
		prof.imprimirTiempos(op.getDirSalidas());
		clienteH.finalizarOptimizacion();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("TERMINO LA OPTIMIZACIóN");
				
		return resoptim;

	}

	public ResOptim optimizarServidor() {
		prof.iniciarContador("SERV: TOTAL OPTIMIZACIÓN");
		prof.iniciarContador("SERV: OPTIMIZADOR ANTES OPRTIMIZAR PASO");
		ltiempo.prepararParaOptimizar();

		optimizable.inicializarOptimizable();

		optimizable.inicializarAzarParaOptimizacion();

		PasoTiempo paso = ltiempo.devuelvePasoActual();

		numpaso = ltiempo.getNumPaso();
		prof.pausarContador("SERV: OPTIMIZADOR ANTES OPRTIMIZAR PASO");
		while (numpaso >= 0) {
			prof.iniciarContador("SERV: OPTIMIZADOR ANTES OPRTIMIZAR PASO");
			paso = ltiempo.devuelvePasoActual();

			optimizable.cargarPasoCorriente(numpaso, paso);

			optimizable.determinarInstantesMuestreo();

			optimizable.inicializarPEPasoOptim();

			optimizable.sortearInnovPEDE();

			optimizable.sortearInnovMontPEVE(); // sortea innovaciones para PE con VE en la optimización

			optimizable.sortearVAMontPENoVE(); // sortea valores VA para PE sin VE en la optimización

			int numpasoPizarron = servidorH.obtenerPasoOptim();
			while (numpasoPizarron == -1) {
				numpasoPizarron = servidorH.obtenerPasoOptim();
			}
			if (numpaso == numpasoPizarron) {
				System.out.println("Servidor optimiza paso: " + numpaso);
				prof.pausarContador("SERV: OPTIMIZADOR ANTES OPRTIMIZAR PASO");
				prof.iniciarContador("SERV: TOTAL OPTIMIZAR PASO");
				optimizable.optimizarPasoServidor();
				prof.pausarContador("SERV: TOTAL OPTIMIZAR PASO");
				prof.iniciarContador("SERV: OPTIMIZADOR DESPUÉS DE OPTIMIZAR PASO");
			}

			if (numpaso > 0) {
				optimizable.actualizarParaPasoAnteriorServidor();
				numpaso = ltiempo.getNumPaso();
				// NO HAY QUE LLAMAR retrocederPaso porque eso ya lo hace
				// actualizarParaPasoAnterior()
			} else {
				numpaso = -1;
				
			}
			prof.pausarContador("SERV: OPTIMIZADOR DESPUÉS DE OPTIMIZAR PASO");
		}

		/**
		 * Se serializan las tablas de valores de Bellman y de control en disco si estón
		 * en memoria
		 */
		
		prof.pausarContador("SERV: TOTAL OPTIMIZACIÓN");
		System.out.println("TERMINO LA OPTIMIZACIóN");
		OptimizadorPaso op = (OptimizadorPaso) optimizable;
		if (!CorridaHandler.getInstance().isParalelo())
			prof.imprimirTiempos(op.getDirSalidas());
		
		return optimizable.devuelveResOptim();

	}
	








}
