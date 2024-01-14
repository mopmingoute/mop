/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CorridaHandler is part of MOP.
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

package logica;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import javax.swing.JFrame;

import javafx.beans.property.ReadOnlyBooleanProperty;
import cp_despacho.DespachableCP;
import cp_despacho.DespachadorCortoPlazo;
import cp_despacho.DespachoProgEstocastica;
import javafx.beans.property.ReadOnlyDoubleProperty;
import optimizacion.Optimizable;
import optimizacion.Optimizador;
import optimizacion.OptimizadorPaso;
import optimizacion.ResOptim;
import optimizacion.ResOptimHiperplanos;
import optimizacion.ResOptimIncrementos;
import paralelismo.GestorParalelismo;
import datatypes.DatosCorrida;
import datatypes.DatosEspecificacionReporte;
import datatypes.DatosPostizacion;
import datatypes.DatosReporteGUI;
import datatypesProblema.DatosCorridaSalida;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosCosMargSP;
import datatypesSalida.DatosEPPResumen;
import datatypesSalida.DatosEPPUnEscenario;
import datatypesSalida.DatosSalidaAtributosDetallados;
import datatypesTiempo.DatosNumpos;
import datatypesTiempo.DatosTiposDeDia;
import parque.Azar;
import parque.Barra;
import parque.ConstructorHiperplanos;
import parque.Corrida;
import parque.Estudio;
import parque.Impacto;
import parque.Participante;
import persistencia.EscritorResumenSimulacionParalelo;
import persistencia.PersistenciaHandler;
import persistencia.Reprocesable;
import pizarron.ServidorHandler;
import pizarron.ClienteHandler;
import pizarron.ICliente;
import pizarron.IServidor;
import pizarron.PizarronRedis;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.Semilla;
import simulacion.Postizador;
import simulacion.SimuladorPaso;
import simulacion.Simulable;
import simulacion.Simulador;
import tiempo.Evolucion;
import tiempo.LineaTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.ManejaObjetosEnDisco;
import utilitarios.Utilitarios;

/**
 * Manejador de la lógica del sistema asociado a la corrida
 * @author ut602614
 *
 */

public class CorridaHandler {

	/** Instancia estótica que implementa el patrón Singleton */
	private static CorridaHandler instance;

	/** Colección de corridas cargadas en el sistema */
	private Hashtable<String, Corrida> corridas;
	
	/** Despachador asociado */
	private Despachador despachador;
		
	/**Corrida Actual**/ 
	private Corrida corridaActual;
	
	/**Optimizador asociado*/
	private Optimizador<OptimizadorPaso> optimizador;
	
	/**Optimizable  Asociado*/
	private Optimizable optimizable;	
	
	private ResOptim resoptim;
	
	/**Simulador Asociado*/
	private Simulador<SimuladorPaso> simulador;
	
	/**Simulable Asociado*/
	private Simulable simulable;
	
	
	/** 
	 * ATRIBUTOS PARA LA OPTIMIZACIÓN DE CORTO PLAZO
	 * 
	 */
	private DespachadorCortoPlazo<DespachoProgEstocastica> despachadorCP;
	
	private DespachableCP despachable;
	
	
	
	private String dirSals;
	
	/** El valor es el id-fuente en la base de datos general de corridas 
	 * asociada a la clave nombre de participante
	 * TODO hay que crearlo al principio ojo que las fallas tienen un string
	 * compuesto de nombre falla - numero de escalon
	 */
	private Hashtable<String, Integer> tablaIdFuentes;
	private ICliente cliente;
	private IServidor servidor;

	private boolean resoptimExterno;

	private GestorParalelismo gestorParalelismo;

	private boolean paralelo;

	private Estudio estudioActual;

	
	private boolean estudio;

	/** Corrida activa */
	private CorridaHandler() {
		despachador = new Despachador();
		corridas = new Hashtable<String, Corrida>();
		this.simulador = new Simulador<SimuladorPaso>();
		this.optimizador = new Optimizador<OptimizadorPaso>();
		this.estudio = false;
	}

	/** Función del singleton que devuelve siempre la misma instancia */
	public static CorridaHandler getInstance() {
		if (instance == null)
			instance = new CorridaHandler();
		return instance;
	}
	
	public static void deleteInstance() {
		instance = null;			
	}
	
	/**
	 * Función que carga la corrida especificada en el archivo de entrada (de tipo xml)
	 * referenciado por la ruta que recibe de parámetro. 
	 */

	public void recargarSimulable() {
		SimuladorPaso simulableCorrida = new SimuladorPaso(this.corridaActual);
		simulable = simulableCorrida;		
		OptimizadorPaso optimizadorPaso = new OptimizadorPaso(this.corridaActual);
		optimizable = optimizadorPaso;
		Postizador postizador = new Postizador();
		simulableCorrida.setPostizador(postizador);
		for (Participante p: corridaActual.getParticipantes()) {
			p.setSimPaso(simulableCorrida);
			p.setOptimPaso(optimizadorPaso);
			corridaActual.getEstudio().getAzar().agregarPEsOptim(p.getProcesosOptim());
		}

		Azar az = corridaActual.dameAzar();
		az.inicializarAzarParaSimulacion();
		for (ProcesoEstocastico p: corridaActual.dameAzar().getProcesos()) {			
			p.setSimuladorPaso(simulableCorrida);
			p.setOptimizadorPaso(optimizadorPaso);
		}
		// el directorio de salidas es null porque ya estó creado en CorridaHandler.cargarCorrida
		simulador.inicializarSimulador(this.corridaActual.getLineaTiempo(), resoptim, corridaActual.getCantEscenarios(), simulableCorrida, this.corridaActual.isSimulacionEncadenada(), resoptimExterno, null, null,true, false);
		optimizador.inicializarOptimizador(this.corridaActual.getLineaTiempo(), optimizadorPaso, null, null, false);  


	}
	
	
	public void simularDesdeDirectorio(String ruta) {

		simulable.finalizarSimulableDirectorio(ruta);

	}
	
	public boolean hayNuevaCorrida() {
		this.servidor = (IServidor)ServidorHandler.getInstance();
		return servidor.hayNuevaCorrida();		
	}

	
	public DatosCorrida cargarCorridaCliente(String ruta) {
		DatosCorrida datos = cargarCorrida(ruta, false, true, false, true);
		this.cliente = (ICliente)ClienteHandler.getInstance();
		this.servidor = (IServidor)ServidorHandler.getInstance();
		cliente.cargarCorrida("corrida", ruta);
		return datos;
	}
	
	public DatosCorrida cargarCorridaServidor(){
		this.cliente = (ICliente)ClienteHandler.getInstance();
		this.servidor = (IServidor)ServidorHandler.getInstance();
		String ruta = servidor.dameRutaNueva();
		return cargarCorrida(ruta, false, true, true, true);
	}
	
	public DatosCorrida cargarCorrida(String ruta, boolean resoptimExterno, boolean paralelo, boolean servidor, boolean generaCarpetas) {
		System.out.println(ruta);
		this.resoptimExterno = resoptimExterno;
		this.setParalelo(paralelo);

		PersistenciaHandler ph = PersistenciaHandler.getInstance();
		DatosCorrida datosXML = ph.cargarCorrida(ruta);
		if(datosXML ==null){
			return null;
		}
		this.generarCorrida(datosXML);
		SimuladorPaso simulableCorrida = new SimuladorPaso(this.corridaActual);
		simulable = simulableCorrida;		
		OptimizadorPaso optimizadorPaso = new OptimizadorPaso(this.corridaActual);
		optimizable = optimizadorPaso;

		for (Participante p: corridaActual.getParticipantes()) {
			p.setSimPaso(simulableCorrida);
			p.setOptimPaso(optimizadorPaso);
			corridaActual.getEstudio().getAzar().agregarPEsOptim(p.getProcesosOptim());
		}

		for (ProcesoEstocastico p: corridaActual.dameAzar().getProcesos()) {
			p.setSimuladorPaso(simulableCorrida);
			p.setOptimizadorPaso(optimizadorPaso);
		}
		
		
		
	
		//DirectoriosYArchivos.creaDirectorio("d:/", "salidasModeloOp");
		if(!servidor && generaCarpetas) {
			DirectoriosYArchivos.crearDirsRuta(datosXML.getRutaSals());
			DirectoriosYArchivos.creaDirectorio(datosXML.getRutaSals(), corridaActual.getNombre());
		}
		dirSals = datosXML.getRutaSals() +"/" +corridaActual.getNombre();

		if(generaCarpetas) {
			simulador.inicializarSimulador(this.corridaActual.getLineaTiempo(), resoptim, corridaActual.getCantEscenarios(), simulableCorrida, this.corridaActual.isSimulacionEncadenada(), resoptimExterno, ruta, dirSals, true, servidor);
			optimizador.inicializarOptimizador(this.corridaActual.getLineaTiempo(), optimizadorPaso, ruta, dirSals, servidor);
		}

		if (this.paralelo) {
			this.gestorParalelismo = new GestorParalelismo(this.corridaActual.getCantEscenarios(), Runtime.getRuntime().availableProcessors(), Constantes.FACTOR_CARGA_NUCLEOS);
		}
		return datosXML;
	}

	public DatosCorrida cargarCorrida(DatosCorrida datosCorrida, String ruta, boolean resoptimExterno, boolean paralelo) {

		this.resoptimExterno = resoptimExterno;
		this.setParalelo(paralelo);

		this.generarCorrida(datosCorrida);
		SimuladorPaso simulableCorrida = new SimuladorPaso(this.corridaActual);
		simulable = simulableCorrida;
		OptimizadorPaso optimizadorPaso = new OptimizadorPaso(this.corridaActual);
		optimizable = optimizadorPaso;

		for (Participante p: corridaActual.getParticipantes()) {
			p.setSimPaso(simulableCorrida);
			p.setOptimPaso(optimizadorPaso);
			corridaActual.getEstudio().getAzar().agregarPEsOptim(p.getProcesosOptim());
		}

		for (ProcesoEstocastico p: corridaActual.dameAzar().getProcesos()) {
			p.setSimuladorPaso(simulableCorrida);
			p.setOptimizadorPaso(optimizadorPaso);
		}

		/***
		CODIGO PROVISORIO
		**/
		DirectoriosYArchivos.crearDirsRuta(corridaActual.getRutaSals());
	//	DirectoriosYArchivos.creaDirectorio("d:/", "salidasModeloOp");
		/***
		FIN CODIGO PROVISORIO
		**/
		
		String rutaEstudio = "";
		
		if (this.estudio) {				
			rutaEstudio = this.corridaActual.getEstudio().getRutaEstudio();
			DirectoriosYArchivos.creaDirectorio(datosCorrida.getRutaSals(), rutaEstudio);
			rutaEstudio = datosCorrida.getRutaSals() + "/" + rutaEstudio;			
		}
		String rutaSals = this.estudio?rutaEstudio:datosCorrida.getRutaSals(); 
		DirectoriosYArchivos.creaDirectorio(rutaSals, corridaActual.getNombre());
		dirSals = rutaSals +"/" +corridaActual.getNombre();

		simulador.inicializarSimulador(this.corridaActual.getLineaTiempo(), resoptim, corridaActual.getCantEscenarios(), simulableCorrida, this.corridaActual.isSimulacionEncadenada(), resoptimExterno, ruta, dirSals,true, false);
		optimizador.inicializarOptimizador(this.corridaActual.getLineaTiempo(), optimizadorPaso, ruta, dirSals, false);

		if (this.paralelo) {
			this.gestorParalelismo = new GestorParalelismo(this.corridaActual.getCantEscenarios(), Runtime.getRuntime().availableProcessors(), Constantes.FACTOR_CARGA_NUCLEOS);
		}
		

		return datosCorrida;
	}


	public void simular() {
		// se chequea si ya está construido
		if (resoptim != null)
			simulador.setResoptim(resoptim);
		int [] escenarios = new int[this.corridaActual.getCantEscenarios()];
        for (int i = 0; i<escenarios.length; i++) {
               escenarios[i] = i+1;
        }
        simulador.simular(escenarios);

	}

	public void cancelarSimulacion(){
		simulador.canceloSimulacion();
	}
	
	public void simularProcesosEstocasticos(){
		
		int [] escenarios = new int[this.corridaActual.getCantEscenarios()];
		for (int i = 0; i<escenarios.length; i++) {
			escenarios[i] = i+1;
		}
		simulador.simularPEs(escenarios);		

	}
	
	
	public void despacharCP(String dirEntradas, String dirSalidas) {
		
		corridaActual.setDespachoLinealEstocastico(true);
		DespachoProgEstocastica despachoProgEstocastica = new DespachoProgEstocastica();
		DespachadorCortoPlazo despachador = new DespachadorCortoPlazo(despachoProgEstocastica, corridaActual, dirEntradas, dirSalidas);
//		despachador.frame.setVisible(false);
		despachador.despacharCP();
	}
	

	/**
	 * Debe estar cargado el xml de la corrida previa cuyos resultados de escenarios se
	 * quiere reprocesar y construída la Corrida.
	 * Los escenarios serializados resultantes de la corrida previa se van a 
	 * levantar del directorio rutaDirEscPrevia.
	 * Las salidas aparecerán en subdirectorios del directorio de salidas
	 * de la corrida que se ha levantado.
	 * 
	 * 	
	 * PROVISORIAMENTE SE CONSTRUYE POR PROGRAMA SIEMPRE EL REPROCESABLE QUE CALCULA LA POTENCIA FIRME
	 * Y EL QUE CALCULA LOS COSTOS NETOS DE UTE
	 * 
	 * EN ESTA IMPLEMENTACIÓN SE PRIVILEGIA EL MENOR USO DE MEMORIA
	 */
	public void reprocesarEscenariosCorrida(String rutaEscenariosCorrPrevia, String dirOtrosDatos) {
		
	}
	
	
	/**
	 * Devuelve los archivos de atributos por intervalo de muestreo en un subdirectorio
	 * IM del directorio rutaDirPoste donde están los archivos de atributos por poste
	 * 
	 * @param rutaDirPoste directorio donde se encuentran los archivos de salidas de 
	 * atributos detallados por poste Y SOLO ELLOS
	 * @param rutaNumpos archivo numpos de la corrida que produjo los atributos detallados
	 */
	public void resultadosPorIM(String rutaDirPoste, String rutaNumpos) {
		String dirSalidasPorIM = rutaDirPoste + "/IM";
		DirectoriosYArchivos.creaDirectorio(rutaDirPoste, "/IM");
		Corrida c = getCorridaActual();
		ArrayList<File> archivos = DirectoriosYArchivos.dameNombresArchivosDeDir(rutaDirPoste);	
		ArrayList<String> archConError = new ArrayList<String>();
		for(File f: archivos) {			
			String dirArchPos = f.getAbsolutePath();			
			String nom = DirectoriosYArchivos.devuelveNombreSinExtArch(dirArchPos);
			String ext = DirectoriosYArchivos.devuelveExtArch(dirArchPos);
			String dirArchIM = rutaDirPoste + "/IM/" + nom + "-IM." + ext;
			System.out.println("Se está escribiendo el archivo " + dirArchIM);
			try {
				EscritorResumenSimulacionParalelo.imprimeDatosDoublePorIntMuestreo(c, dirArchPos, rutaNumpos, dirArchIM);	
			}
			catch(Exception ex) {
				archConError.add(dirArchPos);
			}			
		}	
		for(String s: archConError) {
			System.out.println("ATENCIÓN: No se pudo pasar a intervalo de muestreo el archivo " + s + ". Verifique si es un archivo por poste de la corrida");
		}
	}

	
	/** Recibe los datos de una corrida y genera los objetos de la capa lógica */
	public void generarCorrida(DatosCorrida datosCorrida) {
		Corrida nueva = new Corrida(datosCorrida.getNombre(),
				datosCorrida.getDescripcion());
		
		this.setCorridaActual(nueva);
		this.corridas.put(nueva.getNombre(), nueva);		
			
		Hashtable<String, Evolucion<String>> compsGlobales = datosCorrida.getValoresComportamientoGlobal();
		
		GregorianCalendar iniCorrida = Utilitarios.stringToGregorianCalendar(datosCorrida.getInicioCorrida(),"dd MM yyyy HH:mm:ss");		
		GregorianCalendar iniSorteos = Utilitarios.stringToGregorianCalendar(datosCorrida.getInicioCorrida(),"dd MM yyyy HH:mm:ss");
		
		GregorianCalendar finCorrida = Utilitarios.stringToGregorianCalendar(datosCorrida.getFinCorrida(),"dd MM yyyy HH:mm:ss");		;
		
		DateFormat df = new SimpleDateFormat("dd MM yyyy HH:mm:ss"); // Se parsea un string con este formato a GregorianCalendar
		Date date = null;
		try {
			date = df.parse(datosCorrida.getInicioCorrida());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		iniCorrida.setTime(date);
		
		iniSorteos.setTime(date);

		try {
			date = df.parse(datosCorrida.getFinCorrida());
		} catch (ParseException e) {
			e.printStackTrace();
		}
//		GregorianCalendar finSorteos = new GregorianCalendar();
//		finSorteos.setTime(date);

		try {
			date = df.parse(datosCorrida.getFinCorrida());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		finCorrida.setTime(date);
		
		Azar az = new Azar(new Semilla(datosCorrida.getSemilla().intValue()), iniSorteos, iniCorrida, new ArrayList<ProcesoEstocastico>(),corridaActual);
		Estudio nuevo = null;
		if(datosCorrida.getEvolucionesPorCaso().size()==0) {
			nuevo = new Estudio("total");

		} else {
			nuevo = this.estudioActual;
		}
		nuevo.setAzar(az);
		nueva.setEstudio(nuevo);
		nueva.setCantSorteosMont(datosCorrida.getCantSorteosMont());
		nueva.setRutaSals(datosCorrida.getRutaSals());
		nueva.setTasa(datosCorrida.getTasa());
		nueva.setTopeSpot(datosCorrida.getTopeSpot());
		nueva.setDespSinExp(datosCorrida.isDespSinExp());
		nueva.setIteracionSinExp(datosCorrida.getIteracionSinExp());
		nueva.setPaisesACortar(datosCorrida.getPaisesACortar());
		nueva.setCantEscenarios(datosCorrida.getCantEscenarios());
		boolean externa = datosCorrida.getTipoPostizacion().equalsIgnoreCase("externa");
		datosCorrida.getLineaTiempo().setTiempoInicial(datosCorrida.getInicioCorrida());
		datosCorrida.getLineaTiempo().setTiempoFinal(datosCorrida.getFinCorrida());
		if (!externa) {
			nueva.cargarLineaTiempo(datosCorrida.getLineaTiempo(),iniCorrida, finCorrida, externa, null);
			nueva.setPostizacion(datosCorrida.getPostizacion());
			nueva.setClusters(datosCorrida.getClusters());
		} else {
			PersistenciaHandler ph = PersistenciaHandler.getInstance();
			DatosPostizacion datosP = ph.leerPostizacionExterna(datosCorrida.getPostizacion(),iniCorrida);
			nueva.cargarLineaTiempo(datosCorrida.getLineaTiempo(), iniCorrida, finCorrida, externa, datosP);
			
		}
		nueva.setCompGlobales(compsGlobales);
		
		nueva.setMaximoIteraciones(datosCorrida.getDatosIteraciones().getMaximoIteraciones());
		nueva.setNumeroIteraciones(datosCorrida.getDatosIteraciones().getNumIteraciones());
		nueva.setCriterioParada(datosCorrida.getDatosIteraciones().getCriterioParada());
		nueva.setPostizacionExterna(datosCorrida.getTipoPostizacion().equalsIgnoreCase("externa"));
		if (datosCorrida.getTipoValpostizacion().equalsIgnoreCase("externa")) {
			nueva.setValPostizacionExterna(true);
		} else {
			nueva.setValPostizacionExterna(false);
		}
		if (datosCorrida.getTipoSimulacion().equalsIgnoreCase("encadenada")) {
			nueva.setSimulacionEncadenada(true);
		} else {
			nueva.setSimulacionEncadenada(false);
		}		
		nueva.setRutaPostizacion(datosCorrida.getPostizacion());
		nueva.cargarPEstocasticos(datosCorrida.getProcesosEstocasticos(),datosCorrida.getCantSorteosMont());
		nueva.vincularAsocOptimYExogenos();
		nueva.cargarRed(datosCorrida.getRed());
		nueva.cargarCombustibles(datosCorrida.getCombustibles());
		nueva.cargarConvertidores(datosCorrida.getConvertidores());
		nueva.cargarHidraulicos(datosCorrida.getHidraulicos(), datosCorrida.getLineaTiempo());
		nueva.cargarAcumuladores(datosCorrida.getAcumuladores(),datosCorrida.getLineaTiempo());
		nueva.cargarAguasArriba(datosCorrida.getHidraulicos()); //segunda pasada para linkear los hidróulicos
		nueva.cargarEolicos(datosCorrida.getEolicos());
		nueva.cargarFotovoltaicos(datosCorrida.getFotovoltaicos());
		nueva.cargarImpoExpos(datosCorrida.getImpoExpos());
		nueva.cargarTermicos(datosCorrida.getTermicos());
		nueva.cargarCiclosCombinados(datosCorrida.getCcombinados());
		nueva.cargarDemandas(datosCorrida.getDemandas());
		nueva.cargarFallas(datosCorrida.getFallas(), datosCorrida.getLineaTiempo());
		nueva.cargarContratosEnergia(datosCorrida.getContratosEnergia());		
		nueva.cargarImpactos(datosCorrida.getImpactos(), datosCorrida.getLineaTiempo());
		nueva.getRed().construirBarraUnica();
	//	nueva.setEscenariosSerializados(datosCorrida.getEscenariosSerializados());
		nueva.construirBarrasUnicasRedesCombustibles();
		nueva.cargarParamSalida(datosCorrida.getDatosParamSalida());
		nueva.cargarParamSalidaOpt(datosCorrida.getDatosParamSalidaOpt());
		nueva.cargarParamSalidaSim(datosCorrida.getDatosParamSalidaSim());
		nueva.setCantidadPasos(nueva.getLineaTiempo().getCantidadPasos());
		
		String compGlobalBellman = nueva.getCompGlobales().get(Constantes.COMPVALORESBELLMAN).getValor(this.getCorridaActual().getInstanteInicial());
		String compGlobalDemanda = nueva.getCompGlobales().get(Constantes.COMPDEMANDA).getValor(this.getCorridaActual().getInstanteInicial());
		if(compGlobalBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)){
			ConstructorHiperplanos cH = new ConstructorHiperplanos();
			nueva.setConstructorHiperplanos(cH);
			nueva.getParticipantes().add(cH);
			nueva.getParticipantesDirectos().add(cH);
			nueva.setCompBellman(compGlobalBellman);
		}
		
		nueva.setCompDemanda(compGlobalDemanda); 
		nueva.setEvolucionesPorCaso(datosCorrida.getEvolucionesPorCaso());
		
		// Carga los tipos de dia desde resources
		String archTD = "./resources/" + "tiposDeDia.txt";
		DatosTiposDeDia datTD = persistencia.CargadorTiposDeDia.cargaTiposDeDia(archTD, iniCorrida.get(Calendar.YEAR), finCorrida.get(Calendar.YEAR));
		nueva.setTiposDeDia(datTD);
		
		// Verifica nombres de participantes repetidos y crea colección de Impactos involucrados
		ArrayList<String> nombresParticipantes = new ArrayList<String>();
		for(Participante p: nueva.getParticipantes()){
			p.setImpactosQueLoInvolucran(new ArrayList<Impacto>());	
			if(nombresParticipantes.contains(p.getNombre())){
				System.out.println("Se repite el nombre de participante " + p.getNombre());
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
				System.exit(1);
			}
		}
		
		// Asocia a cada participante los Impactos involucrados
		for(Participante p: nueva.getParticipantes()){						
			if(p instanceof Impacto) {
				Impacto imp = (Impacto)p;
				ArrayList<Participante> pinvolucrados = imp.getInvolucrados();
				for(Participante pi: pinvolucrados) {
					pi.getImpactosQueLoInvolucran().add(imp);
				}
			}								
		}
		
	}

	
	public void simular(String ruta) {
		if (!resoptimExterno || resoptim==null) {
			if(corridaActual.getCompGlobales().get(Constantes.COMPVALORESBELLMAN).getValor(this.getCorridaActual().getInstanteInicial()).equalsIgnoreCase(Constantes.PROBINCREMENTOS)){
				resoptim = new ResOptimIncrementos();	
			}else if(corridaActual.getCompGlobales().get(Constantes.COMPVALORESBELLMAN).getValor(this.getCorridaActual().getInstanteInicial()).equalsIgnoreCase(Constantes.PROBINCREMENTOS)){
				resoptim = new ResOptimHiperplanos();	
			}
	    }
		this.simulable.levantarTablasResOptimDeDisco(ruta, this.resoptim);
		int [] escenarios = new int[this.corridaActual.getCantEscenarios()];
        for (int i = 0; i<escenarios.length; i++) {
               escenarios[i] = i+1;
        }
        simulador.simular(escenarios);
	}
	
	public void simularCliente() {			
        simulador.simularCliente(corridaActual.getCantEscenarios());
	}

	public void simularServidor() {
		simulador.simularServidor();		
	}

	
	public void optimizarCliente() {
		this.setResoptim(optimizador.optimizarCliente());	
	
	}
	
	public ResOptim optimizarServidor() {
		this.setResoptim(optimizador.optimizarServidor());
		return this.resoptim;
	}

	public void optimizar() {
		this.setResoptim(optimizador.optimizar());			
	}

	public void cancelarOptimizacion(){

		optimizador.cancelarOptimizacion();
		resoptim = null;
	}
	
	public void sortearPEOptim(){	
		optimizador.sortearPEOptim();
	}
	
	
	
	/** Agrega una corrida a la colección de corridas del Handler */
	public void agregarCorrida(Corrida c) {
		corridas.put(c.getNombre(), c);
	}

	/** Selecciona como corrida actual a la representada por id */
	public void seleccionarCorridaActual(String id) {
		setCorridaActual(corridas.get(id));
	}

	public Corrida getCorridaActual() {
		return corridaActual;
	}

	public void setCorridaActual(Corrida corridaActual) {
		this.corridaActual = corridaActual;
	}
	
	

	public Hashtable<String, Integer> getTablaIdFuentes() {
		return tablaIdFuentes;
	}

	public void setTablaIdFuentes(Hashtable<String, Integer> tablaIdFuentes) {
		this.tablaIdFuentes = tablaIdFuentes;
	}

	/**
	 * Devuelve la barra que se recibe como parómetro extraóda de la ónica red
	 * electrica de la corrida
	 */
	public Barra getBarra(String barra) {
		return this.corridaActual.getBarra(barra);
	}

	public Despachador getDespachador() {
		return despachador;
	}

	public void setDespachador(Despachador despachador) {
		this.despachador = despachador;
	}

	public void agregarParticipante(Participante nueva) {
		this.corridaActual.getParticipantes().add(nueva);
	}
	
	public void quitarBarras() {
		for (Participante p: this.corridaActual.getParticipantes()) {
			if (p instanceof Barra) {
				this.corridaActual.getParticipantes().remove(p);
			}
		}
	}

	public Simulador<SimuladorPaso> getSimulador() {
		return simulador;
	}

	public void setSimulador(Simulador<SimuladorPaso> simulador) {
		this.simulador = simulador;
	}

	public Simulable getSimulable() {
		return simulable;
	}

	public void setSimulable(Simulable simulable) {
		this.simulable = simulable;
	}

	public ResOptim getResoptim() {
		return resoptim;
	}

	public void setResoptim(ResOptim resoptim) {
		this.resoptim = resoptim;
	}

	public void quitarParticipante(Barra barraUnica) {
		this.corridaActual.getParticipantes().remove(barraUnica);		
	}

	public void agregarBarras(Collection<Barra> values) {
		this.corridaActual.getParticipantes().addAll(values);
		
	}

	public void agregarParticipanteDirecto(Participante nuevo) {
		this.corridaActual.getParticipantesDirectos().add(nuevo);		
	}

	public Optimizable getOptimizable() {
		return optimizable;
	}

	public void setOptimizable(Optimizable optimizable) {
		this.optimizable = optimizable;
	}

	public boolean hayResoptim() {
		return resoptim!=null;
	}


	public GestorParalelismo getGestorParalelismo() {
		return gestorParalelismo;
	}

	public void setGestorParalelismo(GestorParalelismo gestorParalelismo) {
		this.gestorParalelismo = gestorParalelismo;
	}

	public boolean isParalelo() {
		return paralelo;
	}

	public void setParalelo(boolean paralelo) {
		this.paralelo = paralelo;
	}

	public int dameOperacion() {
		return this.servidor.dameOperacion();		
	}


	public ReadOnlyDoubleProperty optProgressProperty() {
		return optimizador.progressProperty();
	}

	public ReadOnlyDoubleProperty simProgressProperty() {
		return simulador.progressProperty();
	}

	public ReadOnlyBooleanProperty escritorProgressProperty() {
		return simulador.progresEscritorProperty();
	}

	public void registrarMaquina() {
		PizarronRedis pp = PizarronRedis.getInstance();
		pp.registrarMaquina();		
	}

	public int damecantServidores() {
		PizarronRedis pp = PizarronRedis.getInstance();
		return pp.obtenercantServidores();		
	}

	public void simularClienteDesdeDirectorio(String ruta) {
		simulador.simularClienteDesdeDirectorio(corridaActual.getCantEscenarios(), ruta);
		
	}

	public void cerrarServidores() {		
		PizarronRedis pp = PizarronRedis.getInstance();
			pp.matarServidores();
	}

	public void cargarEstudio(String ruta) {

		System.out.println(ruta);
		
		this.setParalelo(paralelo);

		PersistenciaHandler ph = PersistenciaHandler.getInstance();
		DatosCorrida datosXML = ph.cargarCorrida(ruta);
		
		this.generarEstudio(datosXML);	
		
		System.out.println("SE CARGÓ EL ESTUDIO");
	}

	private void generarEstudio(DatosCorrida datosXML) {
		
		
		this.estudioActual = new Estudio(datosXML.getNombre(), datosXML);		
		this.estudioActual.inicializarEstudio();
		//this.generarCorrida(datosXML);
		
	}

	public void ejecutarEstudio() {
		
		this.estudioActual.ejecutarEstudio();
	}

	public Hashtable<String, Corrida> getCorridas() {
		return corridas;
	}

	public void setCorridas(Hashtable<String, Corrida> corridas) {
		this.corridas = corridas;
	}

	public Optimizador<OptimizadorPaso> getOptimizador() {
		return optimizador;
	}

	public void setOptimizador(Optimizador<OptimizadorPaso> optimizador) {
		this.optimizador = optimizador;
	}

	public String getDirSals() {
		return dirSals;
	}

	public void setDirSals(String dirSals) {
		this.dirSals = dirSals;
	}

	public ICliente getCliente() {
		return cliente;
	}

	public void setCliente(ICliente cliente) {
		this.cliente = cliente;
	}

	public IServidor getServidor() {
		return servidor;
	}

	public void setServidor(IServidor servidor) {
		this.servidor = servidor;
	}

	public boolean isResoptimExterno() {
		return resoptimExterno;
	}

	public void setResoptimExterno(boolean resoptimExterno) {
		this.resoptimExterno = resoptimExterno;
	}

	public Estudio getEstudioActual() {
		return estudioActual;
	}

	public void setEstudioActual(Estudio estudioActual) {
		this.estudioActual = estudioActual;
	}

	public boolean isEstudio() {
		return estudio;
	}

	public void setEstudio(boolean estudio) {
		this.estudio = estudio;
	}

	public static void setInstance(CorridaHandler instance) {
		CorridaHandler.instance = instance;
	}
	
	public long dameInstanteActual() {
		return this.corridaActual.getLineaTiempo().getInstInicPasoCorriente();
	}

}

