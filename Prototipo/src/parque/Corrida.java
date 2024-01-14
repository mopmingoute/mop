/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Corrida is part of MOP.
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

import tiempo.Evolucion;
import tiempo.EvolucionPorCaso;
import tiempo.LineaTiempo;
import utilitarios.Constantes;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import compdespacho.AcumuladorCompDesp;
import compdespacho.CicloCombCompDesp;
import compdespacho.FallaCompDesp;
import compdespacho.HidraulicoCompDesp;
import compdespacho.TermicoCompDesp;
import interfacesPE.AportanteMuestreo;
import interfacesParticipantes.AportanteControlDE;
import interfacesParticipantes.AportanteEstado;
import interfacesParticipantes.AportantePost;
import control.VariableControl;
import optimizacion.ResOptimIncrementos;
import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import datatypes.DatosAcumuladorCorrida;
import datatypes.DatosAcumuladoresCorrida;
import datatypes.DatosCicloCombinadoCorrida;
import datatypes.DatosCiclosCombinadosCorrida;
import datatypes.DatosCombustibleCorrida;
import datatypes.DatosCombustiblesCorrida;
import datatypes.DatosContratoEnergiaCorrida;
import datatypes.DatosContratosEnergiaCorrida;
import datatypes.DatosConvertidorCombustibleSimpleCorrida;
import datatypes.DatosConvertidoresCorrida;
import datatypes.DatosDemandaCorrida;
import datatypes.DatosDemandasCorrida;
import datatypes.DatosEolicoCorrida;
import datatypes.DatosEolicosCorrida;
import datatypes.DatosEspecificacionReporte;
import datatypes.DatosFallaEscalonadaCorrida;
import datatypes.DatosFallasEscalonadasCorrida;
import datatypes.DatosFotovoltaicoCorrida;
import datatypes.DatosFotovoltaicosCorrida;
import datatypes.DatosHidraulicoCorrida;
import datatypes.DatosHidraulicosCorrida;
import datatypes.DatosImpactoCorrida;
import datatypes.DatosImpactosCorrida;
import datatypes.DatosImpoExpoCorrida;
import datatypes.DatosImpoExposCorrida;
import datatypes.DatosPostizacion;
import datatypes.DatosRedElectricaCorrida;
import datatypes.DatosReporteGUI;
import datatypes.DatosTermicoCorrida;
import datatypes.DatosTermicosCorrida;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesSalida.DatosParamSalida;
import datatypesSalida.DatosParamSalidaOpt;
import datatypesSalida.DatosParamSalidaSim;
import datatypesTiempo.DatosLineaTiempo;
import datatypesTiempo.DatosTiposDeDia;
import estado.VariableEstado;

/**
 * Clase que representa la corrida
 * 
 * @author ut602614
 *
 */
public class Corrida {
	private String nombre;
	
	
	/** Nombre asociado */
	private String fase; // fase del proceso en que está: simulación, optimización, etc.
	private String descripcion;
	private String nombreCorto;
	/** Descripción de la corrida */
	private Hashtable<String, Evolucion<String>> compGlobales;
	
	private DatosTiposDeDia tiposDeDia;

	private boolean corridaMultipleEstudio;

	private String ruta;
	private String fechaEjecucion;
	private String horaEjecucion;
	/** Comportamientos Globales **/
	private Integer maximoIteraciones;
	/** Cantidad móxima de iteraciones a realizarse por paso */
	private Integer numeroIteraciones;
	private String criterioParada;
	/**
	 * Criterio de parada que se utiliza para dejar de iterar dentro de un paso
	 */
	private boolean postizacionExterna;
	private boolean valPostizacionExterna;
	private boolean simulacionEncadenada;
	private String rutaPostizacion;
	private String rutaSals;
	private Double tasa;
	
	private double topeSpot; // en USD/MWh
	private boolean despSinExp;  // si es true una de las iteraciones de la simulación se usará para obtener un despacho sin exportación
	private int iteracionSinExp; // si despSinExp = true, es la iteración, empezando de 1, que se usa para estimar el despacho sin exportación
	private ArrayList<String> paisesACortar; // Los destinos a los que se anulan las exportaciones en la corrida sin exportación 

	private ArrayList<AportantePost> aportantesPostizacion;
	private ArrayList<AportanteEstado> aportantesEstado;
	private ArrayList<AportanteControlDE> aportantesControlDE;

	private ArrayList<AportanteMuestreo> aportantesMuestreo;

	// Colecciones de los distintos tipos de participantes
	private Hashtable<String, GeneradorTermico> termicos;
	private Hashtable<String, GeneradorHidraulico> hidraulicos;
	private Hashtable<String, Acumulador> acumuladores;
	private Hashtable<String, GeneradorEolico> eolicos;
	private Hashtable<String, GeneradorFotovoltaico> fotovoltaicos;
	private Hashtable<String, ImpoExpo> impoExpos;
	private Hashtable<String, Demanda> demandas;
	private Hashtable<String, Falla> fallas;
	private Hashtable<String, Impacto> impactos;
	private Hashtable<String, ContratoEnergia> contratosEnergia;
	private Hashtable<String, Combustible> combustibles;
	private Hashtable<String, RedCombustible> redesCombustible;
	private Hashtable<String, ContratoCombustible> contratosCombustible;
	private Hashtable<String, ConvertidorCombustibleSimple> convertidores;
	private Hashtable<String, CicloCombinado> ciclosCombinados;

	// Lista de nombres de participantes en el orden en que aparecen en el xml

	private ArrayList<String> ltermicos;
	private ArrayList<String> lhidraulicos;
	private ArrayList<String> lacumuladores;
	private ArrayList<String> leolicos;
	private ArrayList<String> lfotovoltaicos;
	private ArrayList<String> limpoExpos;
	private ArrayList<String> ldemandas;
	private ArrayList<String> lfallas;
	private ArrayList<String> limpactos;
	private ArrayList<String> lcontratosEnergia;
	private ArrayList<String> lcontratosInterrumpibles;
	private ArrayList<String> lcombustibles;
	private ArrayList<String> lredesCombustible;
	private ArrayList<String> lcontratosCombustible;
	private ArrayList<String> lconvertidores;
	private ArrayList<String> lciclosCombinados;

	/** Red eléctrica asociada al problema */
	private RedElectrica red;

	private ConstructorHiperplanos constructorHiperplanos;

	private ArrayList<Participante> redes; // Contiene la red eléctrica y las redes de combustible

	private ArrayList<Participante> participantes;
	private ArrayList<Participante> participantesDirectos;

	private ArrayList<Participante> partsConSalidaDetallada;

	private Estudio estudio;
	private int cantEscenarios;
	private long instanteInicial;
	private long instanteFinal;	
	private ResOptimIncrementos resoptim;
	private int cantidadPasos;
	private LineaTiempo lineaTiempo;
	private DatosParamSalida datosParamSalida;
	private DatosParamSalidaOpt datosParamSalidaOpt;
	private DatosParamSalidaSim datosParamSalidaSim;
	private Evolucion<Integer> cantSorteosMont;
	private boolean cronologica;
	private String compBellman;
	private String compDemanda;
//	private boolean escenariosSerializados;
	private boolean costosVariables = false;
	
	
	/**
	 * false si se hace el despacho determinístico habitual
	 * true si se usa programación lineal estocastica para el problema de corto plazo o por otra razón
	 */
	private boolean despachoLinealEstocastico; 
	

	/**
	 * Esta lista le da visibilidad de las EvolucionPorCaso a la corrida base del
	 * Estudio, en el caso de que se quiera emplear un estudio para generar muchas
	 * corridas a partir de una corrida base.
	 */
	private ArrayList<EvolucionPorCaso> evolucionesPorCaso;
	private String postizacion;
	private int clusters;

	/** Constructor por parómetros primitivos */
	public Corrida(String nombre, String descripcion) {

		// CARGAR ESTUDIO
		// TODO: CARGAR EVOLUCION CANTSORTEOSMONT

		this.setNombre(nombre);
		this.setDescripcion(descripcion);
		this.setNombreCorto(nombre.substring(0, 2) + "-" + descripcion.substring(0, 2));
		// Construye colecciones de participantes y sus nombres
		termicos = new Hashtable<String, GeneradorTermico>();
		ciclosCombinados = new Hashtable<String, CicloCombinado>();
		hidraulicos = new Hashtable<String, GeneradorHidraulico>();
		eolicos = new Hashtable<String, GeneradorEolico>();
		fotovoltaicos = new Hashtable<String, GeneradorFotovoltaico>();
		impoExpos = new Hashtable<String, ImpoExpo>();
		demandas = new Hashtable<String, Demanda>();
		fallas = new Hashtable<String, Falla>();
		impactos = new Hashtable<String, Impacto>();
		contratosEnergia = new Hashtable<String, ContratoEnergia>();		
		combustibles = new Hashtable<String, Combustible>();
		redesCombustible = new Hashtable<String, RedCombustible>();
		contratosCombustible = new Hashtable<String, ContratoCombustible>();
		convertidores = new Hashtable<String, ConvertidorCombustibleSimple>();
		evolucionesPorCaso = new ArrayList<EvolucionPorCaso>();
		lciclosCombinados = new ArrayList<String>();
		ltermicos = new ArrayList<String>();
		lhidraulicos = new ArrayList<String>();
		leolicos = new ArrayList<String>();
		lfotovoltaicos = new ArrayList<String>();
		limpoExpos = new ArrayList<String>();
		ldemandas = new ArrayList<String>();
		lfallas = new ArrayList<String>();
		limpactos = new ArrayList<String>();
		lcontratosEnergia = new ArrayList<String>();
		lcombustibles = new ArrayList<String>();
		lredesCombustible = new ArrayList<String>();
		lcontratosCombustible = new ArrayList<String>();
		lconvertidores = new ArrayList<String>();
		participantes = new ArrayList<Participante>();
		participantesDirectos = new ArrayList<Participante>();
		aportantesEstado = new ArrayList<AportanteEstado>();
		aportantesPostizacion = new ArrayList<AportantePost>();
		aportantesControlDE = new ArrayList<AportanteControlDE>();
		estudio = new Estudio(nombre);
		redes = new ArrayList<Participante>();
		aportantesControlDE = new ArrayList<AportanteControlDE>();
		acumuladores = new Hashtable<String, Acumulador>();
		partsConSalidaDetallada = new ArrayList<Participante>();

	}

//	public void setEscenariosSerializados(boolean escenariosSerializados) {
//		this.escenariosSerializados = escenariosSerializados;
//	}

	public ProcesoEstocastico dameProcesoEstocastico(String nombre) {
		return this.estudio.dameProcesoEstocastico(nombre);
	}

	/**
	 * Procedimiento que crea y carga los generadores tórmicos a partir de los datos
	 */
	public void cargarTermicos(DatosTermicosCorrida termis) {
		Hashtable<String, DatosTermicoCorrida> datosTermicos = termis.getTermicos();
		Hashtable<String, Evolucion<String>> variablesComportamiento = termis.getValoresComportamiento();
		ArrayList<String> atributosDetallados = termis.getAtribtosDetallados();

		// Se establece el comportamiento a nivel de clase para el generador
		// tórmico
		if (variablesComportamiento.size() != 0) {
			TermicoCompDesp.setCompMinimosTecnicosPorDefecto(
					variablesComportamiento.get(Constantes.COMPMINTEC).getValor(instanteInicial));

		}
		if (atributosDetallados.size() != 0) {
			GeneradorTermico.setAtributosDetallados(atributosDetallados);
		}
		ArrayList<String> set = termis.getListaUtilizados();
		ltermicos = set;

		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				DatosTermicoCorrida dt = datosTermicos.get(clave);
				Barra barra = red.getBarras().get(dt.getBarra());
				Hashtable<String, Combustible> combustiblesUsados = dameCombustibles(
						dt.getCombustiblesBarras().keySet());
				Hashtable<String, BarraCombustible> barrasUsadas = dameBarras(dt.getCombustiblesBarras());

				// Se agrega en nuevo generador con su barra elóctrica y la
				// colección de combustibles y barras de combustible asociadas
				GeneradorTermico nuevo = new GeneradorTermico(dt, barra, combustiblesUsados, barrasUsadas);
				nuevo.setTipo(utilitarios.Constantes.TER);
				termicos.put(clave, nuevo);
				participantes.add(nuevo);
				participantesDirectos.add(nuevo);
				if (dt.isSalDetallada())
					partsConSalidaDetallada.add(nuevo);
				barra.getGeneradores().add(nuevo);
				this.red.getGeneradoresBarraUnica().add(nuevo);
				Hashtable<String, String> combustiblesBarras = dt.getCombustiblesBarras();
				Set<String> clavesCombs = combustiblesBarras.keySet();

				// Se agrega a las barras de combustible el link al generador
				Iterator<String> it = clavesCombs.iterator();
				while (it.hasNext()) {
					String clave2 = it.next();
					RedCombustible rc = this.redesCombustible.get(clave2);
					BarraCombustible bc = rc.getBarras().get(combustiblesBarras.get(clave2));
					bc.getGeneradoresConectados().add(nuevo);

				}
			}
		}
	}

	public void cargarCiclosCombinados(DatosCiclosCombinadosCorrida ccombinados) {

		Hashtable<String, DatosCicloCombinadoCorrida> datosCco = ccombinados.getCcombinados();
		Hashtable<String, Evolucion<String>> variablesComportamiento = ccombinados.getValoresComportamiento();
		ArrayList<String> atributosDetallados = ccombinados.getAtribtosDetallados();

		if (variablesComportamiento.size() != 0) {
			CicloCombCompDesp.setCompCC(variablesComportamiento.get(Constantes.COMPCC).getValor(instanteInicial));

		}
		if (atributosDetallados.size() != 0) {
			CicloCombinado.setAtributosDetallados(atributosDetallados);
		}
		ArrayList<String> set = ccombinados.getListaUtilizados();
		setLciclosCombinados(set);

		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				DatosCicloCombinadoCorrida dcc = datosCco.get(clave);
				Barra barra = red.getBarras().get(dcc.getBarra());

				Hashtable<String, Combustible> combustiblesUsados = dameCombustibles(
						dcc.getBarrasCombustible().keySet());
				Hashtable<String, BarraCombustible> barrasUsadas = dameBarras(dcc.getBarrasCombustible());

				// Se agrega en nuevo generador con su barra elóctrica y la
				// colección de combustibles y barras de combustible asociadas

				CicloCombinado nuevo = new CicloCombinado(dcc, barra, combustiblesUsados, barrasUsadas);
				nuevo.setTipo(utilitarios.Constantes.CC);
				ciclosCombinados.put(clave, nuevo);
				participantes.add(nuevo);
				participantesDirectos.add(nuevo);
				if (dcc.isSalDetallada())
					partsConSalidaDetallada.add(nuevo);
				barra.getGeneradores().add(nuevo);
				this.red.getGeneradoresBarraUnica().add(nuevo);

				Hashtable<String, String> combustiblesBarras = dcc.getBarrasCombustible();
				Set<String> clavesCombs = combustiblesBarras.keySet();

				// Se agrega a las barras de combustible el link al generador
				Iterator<String> it = clavesCombs.iterator();
				while (it.hasNext()) {
					String clave2 = it.next();
					RedCombustible rc = this.redesCombustible.get(clave2);
					BarraCombustible bc = rc.getBarras().get(combustiblesBarras.get(clave2));
					bc.getCiclosCombConectados().add(nuevo);
				}
			}
		}
	}

	/**
	 * Procedimiento que crea y carga los generadores eólicos a partir de los datos
	 */
	public void cargarEolicos(DatosEolicosCorrida eolos) {
		Hashtable<String, DatosEolicoCorrida> datosEolicos = eolos.getEolicos();
		ArrayList<String> atributosDetallados = eolos.getAtributosDetallados();

		if (atributosDetallados.size() != 0) {
			GeneradorEolico.setAtributosDetallados(atributosDetallados);
		}
		ArrayList<String> set = eolos.getListaUtilizados();
		leolicos = set;
		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				GeneradorEolico ge = new GeneradorEolico(datosEolicos.get(clave));
				ge.setTipo(utilitarios.Constantes.EOLO);
				eolicos.put(clave, ge);
				participantes.add(ge);
				participantesDirectos.add(ge);
				if (datosEolicos.get(clave).isSalDetallada())
					partsConSalidaDetallada.add(ge);
				this.red.getGeneradoresBarraUnica().add(eolicos.get(clave));
				this.aportantesPostizacion.add(ge.getCompS());
			}
		}
	}

	/**
	 * Procedimiento que crea y carga los impoExpos a partir de los datos
	 */
	public void cargarImpoExpos(DatosImpoExposCorrida datIExpos) {
		Hashtable<String, DatosImpoExpoCorrida> datosIE = datIExpos.getImpoExpos();
		ArrayList<String> atributosDetallados = datIExpos.getAtributosDetallados();

		if (atributosDetallados.size() != 0) {
			ImpoExpo.setAtributosDetallados(atributosDetallados);
		}
		ArrayList<String> set = datIExpos.getListaUtilizados();
		limpoExpos = set;
		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				ImpoExpo ie = new ImpoExpo(datosIE.get(clave));
				ie.setTipo(utilitarios.Constantes.IMPOEXPO);
				impoExpos.put(clave, ie);
				participantes.add(ie);
				participantesDirectos.add(ie);
				if (datosIE.get(clave).isSalDetallada())
					partsConSalidaDetallada.add(ie);
				this.red.getComercioEnerBarraUnica().add(impoExpos.get(clave));
			}
		}
	}

	/**
	 * Procedimiento que crea y carga los generadores fotovoltaicos a partir de los
	 * datos
	 */
	public void cargarFotovoltaicos(DatosFotovoltaicosCorrida fotos) {
		Hashtable<String, DatosFotovoltaicoCorrida> datosFotovoltaicos = fotos.getFotovoltaicos();
		ArrayList<String> atributosDetallados = fotos.getAtributosDetallados();
		if (atributosDetallados.size() != 0) {
			GeneradorFotovoltaico.setAtributosDetallados(atributosDetallados);
		}

		ArrayList<String> set = fotos.getListaUtilizados();
		lfotovoltaicos = set;

		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				GeneradorFotovoltaico ge = new GeneradorFotovoltaico(datosFotovoltaicos.get(clave));
				ge.setTipo(utilitarios.Constantes.FOTOV);
				fotovoltaicos.put(clave, ge);
				participantes.add(ge);
				participantesDirectos.add(ge);
				if (datosFotovoltaicos.get(clave).isSalDetallada())
					partsConSalidaDetallada.add(ge);
				this.red.getGeneradoresBarraUnica().add(fotovoltaicos.get(clave));
				this.aportantesPostizacion.add(ge.getCompS());
			}
		}
	}

	/**
	 * Devuelve la colección de barras de combustible extraóda de la colección de
	 * identificadores de combustibles con identificadores de barras
	 */
	public Hashtable<String, BarraCombustible> dameBarras(Hashtable<String, String> combustiblesBarras) {
		Hashtable<String, BarraCombustible> resultado = new Hashtable<String, BarraCombustible>();
		Iterator<String> itr = combustiblesBarras.keySet().iterator();

		while (itr.hasNext()) {
			String clave = itr.next();
			RedCombustible redcomb = dameRedCombustible(clave);
			resultado.put(clave, redcomb.getBarras().get(combustiblesBarras.get(clave)));
		}
		return resultado;
	}

	/** Procedimiento que crea y carga los combustibles a partir de los datos */
	public void cargarCombustibles(DatosCombustiblesCorrida combustibles) {
		Hashtable<String, DatosCombustibleCorrida> datosCombustibles = combustibles.getCombustibles();
		ArrayList<String> set = combustibles.getListaUtilizados();
		lcombustibles = set;
		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				DatosCombustibleCorrida datos = datosCombustibles.get(clave);
				Combustible nuevo = new Combustible(datos);
				this.combustibles.put(clave, nuevo);
				this.participantes.add(nuevo);
				this.redesCombustible.put(datos.getNombre(), new RedCombustible(datos.getRed(), nuevo));
//				this.redesCombustible.get(datos.getNombre()).construirBarraUnica();
				this.participantes.add(this.redesCombustible.get(datos.getNombre()));
				this.participantesDirectos.add(this.redesCombustible.get(datos.getNombre()));
				if (datos.isSalDetallada())
					partsConSalidaDetallada.add(nuevo);
				this.redes.add(this.redesCombustible.get(datos.getNombre()));
				this.contratosCombustible.putAll(this.redesCombustible.get(datos.getNombre()).getContratos());
			}
		}

	}

	/** Procedimiento que crea y carga la red elóctrica a partir de los datos */
	public void cargarRed(DatosRedElectricaCorrida redelectrica) {
		this.red = new RedElectrica(redelectrica);
		this.participantesDirectos.add(red);
		this.participantes.add(red);
		this.redes.add(red);

//		Hashtable<String, DatosProveedorElectricoSimpleCorrida> redelect = redelectrica.getProveedores()
//				.getProveedores();
//		ArrayList<String> set = redelectrica.getListaProveedoresUtilizados();
//		ArrayList<String> atributosDetallados = redelectrica.getProveedores().getAtributosDetallados();
//		if (atributosDetallados.size() != 0) {
//			ProveedorElecSimple.setAtributosDetallados(atributosDetallados);
//		}
//		if (set != null) {
//			Iterator<String> itr = set.iterator();
//			while (itr.hasNext()) {
//				String clave = itr.next();
//				DatosProveedorElectricoSimpleCorrida datos = redelect.get(clave);
//				ProveedorElecSimple nuevo = new ProveedorElecSimple(datos);
////				this.participantes.add(nuevo);
////				this.participantesDirectos.add(nuevo);
//				if (datos.isSalDetallada())
//					partsConSalidaDetallada.add(nuevo);
//			}
//		}

	}

	/** Procedimiento que crea y carga las demandas a partir de los datos */
	public void cargarDemandas(DatosDemandasCorrida demandas2) {
		Hashtable<String, DatosDemandaCorrida> datosDemandas = demandas2.getDemandas();
		ArrayList<String> set = demandas2.getListaUtilizados();
		ldemandas = set;
		ArrayList<String> atributosDetallados = demandas2.getAtributosDetallados();
		if (atributosDetallados.size() != 0) {
			Demanda.setAtributosDetallados(atributosDetallados);
		}

		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				DatosDemandaCorrida datos = datosDemandas.get(clave);
				Barra barra = red.getBarras().get(datos.getBarra());
				Demanda nuevo = new Demanda(datos, barra);
				nuevo.setTipo(utilitarios.Constantes.DEM);
				barra.getDemandas().add(nuevo);
				this.demandas.put(clave, nuevo);
				this.participantes.add(nuevo);
				if (datos.isSalDetallada())
					partsConSalidaDetallada.add(nuevo);
				this.red.getDemandasBarraUnica().add(nuevo);
				this.aportantesPostizacion.add(nuevo.getCompS());
			}
		}
	}

	public Participante devuelveParticipante(String nombre) {
		for (Participante p : participantes) {
			if (p.getNombre() != null)
				if (p.getNombre().equalsIgnoreCase(nombre))
					return p;
		}
		return null;
	}
	
	
	/**
	 * Devuelve el participantes con el tipo y nombre de los argumentos o null
	 * si no lo encontró
	 * @param tipo
	 * @param nombre
	 * @return
	 */
	public Participante devuelveParticipante(String tipo, String nombre) {
		
		for(Participante p: participantes) {
			if(p.getTipo()!=null && p.getNombre()!=null) {
				if(p.getNombre().equalsIgnoreCase(nombre) && p.getTipo().equalsIgnoreCase(tipo)) 
					return p;			
			}
		}
		return null;
	}

	public void cargarImpactos(DatosImpactosCorrida impactos, DatosLineaTiempo lineaTiempo2) {
		Hashtable<String, DatosImpactoCorrida> datosImpactos = impactos.getImpactos();
		ArrayList<String> set = impactos.getListaUtilizados();
		limpactos = set;
		ArrayList<String> atributosDetallados = impactos.getAtributosDetallados();
		if (atributosDetallados.size() != 0) {
			Impacto.setAtributosDetallados(atributosDetallados);
		}

		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				DatosImpactoCorrida datos = datosImpactos.get(clave);
				ArrayList<String> involucrados = datos.getInvolucrados();
				ArrayList<Participante> partinv = new ArrayList<Participante>();
				for (String nom : involucrados) {
					partinv.add(devuelveParticipante(nom));
				}

				Impacto nuevo = new Impacto(datos.getNombre(), datos.getActivo(), datos.getCostoUnit(),
						datos.getLimite(), datos.isPorPoste(), partinv, datos.getTipoImpacto(),
						datos.isPorUnidadTiempo());
				nuevo.setTipo(utilitarios.Constantes.IMPACTO);
				this.impactos.put(nuevo.getNombre(), nuevo);
				this.participantes.add(nuevo);
				this.participantesDirectos.add(nuevo);
				if (datos.isSalDetallada())
					partsConSalidaDetallada.add(nuevo);
			}
		}

	}

	public void cargarContratosEnergia(DatosContratosEnergiaCorrida contratosEnergia) {
		Hashtable<String, DatosContratoEnergiaCorrida> datosContratosEnergia = contratosEnergia.getContratosEnergia();
		ArrayList<String> set = contratosEnergia.getListaUtilizados();
		lcontratosEnergia = set;
		ArrayList<String> atributosDetallados = contratosEnergia.getAtributosDetallados();
		if (atributosDetallados.size() != 0) {
			ContratoEnergia.setAtributosDetallados(atributosDetallados);
		}

		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				DatosContratoEnergiaCorrida dat = datosContratosEnergia.get(clave);
				if (dat.getTipo().equalsIgnoreCase(utilitarios.Constantes.INT_TOMA_SIEMPRE)) continue;
				if(dat==null) continue;
				ArrayList<String> nombresInvolucrados = null;
				ArrayList<Participante> partinv = new ArrayList<Participante>();
				if (dat != null) {
					nombresInvolucrados = dat.getInvolucrados();

					for (String nom : nombresInvolucrados) {
						partinv.add(devuelveParticipante(nom));
					}

					ContratoEnergia nuevo = new ContratoEnergia(dat, partinv);
					this.contratosEnergia.put(nuevo.getNombre(), nuevo);
					this.participantes.add(nuevo);
//				this.participantesDirectos.add(nuevo);
					if (dat.isSalidaDetallada())
						partsConSalidaDetallada.add(nuevo);
				}
			}
		}
	}

	/**
	 * Procedimiento que crea y carga los generadores hidróulicos a partir de los
	 * datos
	 */
	public void cargarHidraulicos(DatosHidraulicosCorrida hidros, DatosLineaTiempo lt) {
		Hashtable<String, DatosHidraulicoCorrida> datosHidraulicos = hidros.getHidraulicos();
		Hashtable<String, Evolucion<String>> variablesComportamiento = hidros.getValoresComportamiento();
		ArrayList<String> atributosDetallados = hidros.getAtribtosDetallados();
		if (atributosDetallados.size() != 0) {
			GeneradorHidraulico.setAtributosDetallados(atributosDetallados);
		}

		// Se establece el comportamiento a nivel de clase para el generador
		// hidróulico
		if (variablesComportamiento.size() != 0) {
			HidraulicoCompDesp
					.setCompLagoPorDefecto(variablesComportamiento.get(Constantes.COMPLAGO).getValor(instanteInicial));
			HidraulicoCompDesp.setCompCoefEnergeticoDefecto(
					variablesComportamiento.get(Constantes.COMPCOEFENERGETICO).getValor(instanteInicial));
		}

		ArrayList<String> set = hidros.getListaUtilizados();
		lhidraulicos = set;
		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();

				GeneradorHidraulico gh = new GeneradorHidraulico(datosHidraulicos.get(clave), lt);
				gh.setTipo(utilitarios.Constantes.HID);
				hidraulicos.put(clave, gh);
				participantes.add(gh);
				participantesDirectos.add(gh);
				if (datosHidraulicos.get(clave).isSalDetallada())
					partsConSalidaDetallada.add(gh);
				aportantesEstado.add(gh);

				this.red.getGeneradoresBarraUnica().add(hidraulicos.get(clave));
			}
		}
	}

	public void cargarAcumuladores(DatosAcumuladoresCorrida acumuladores, DatosLineaTiempo lineaTiempo2) {
		Hashtable<String, DatosAcumuladorCorrida> datosAcumuladores = acumuladores.getAcumuladores();
		Hashtable<String, Evolucion<String>> variablesComportamiento = acumuladores.getValoresComportamiento();
		ArrayList<String> atributosDetallados = acumuladores.getAtributosDetallados();
		if (atributosDetallados.size() != 0) {
			Acumulador.setAtributosDetallados(atributosDetallados);
		}

		if (variablesComportamiento.size() != 0) {
			// Se establece el comportamiento a nivel de clase para el
			// acumuladores
			AcumuladorCompDesp
					.setCompPasoPorDefecto(variablesComportamiento.get(Constantes.COMPPASO).getValor(instanteInicial));
		}

		ArrayList<String> set = acumuladores.getListaUtilizados();
		lacumuladores = set;

		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();

				Acumulador ac = new Acumulador(datosAcumuladores.get(clave), lineaTiempo2);
				ac.setTipo(utilitarios.Constantes.ACUM);
				this.acumuladores.put(clave, ac);
				participantes.add(ac);
				participantesDirectos.add(ac);
				if (datosAcumuladores.get(clave).isSalDetallada())
					partsConSalidaDetallada.add(ac);
				aportantesEstado.add(ac);

				this.red.getGeneradoresBarraUnica().add(this.acumuladores.get(clave));
			}
		}

	}

	public void cargarConvertidores(DatosConvertidoresCorrida convertidores) {
		Hashtable<String, DatosConvertidorCombustibleSimpleCorrida> datosconvs = convertidores.getConvertidores();

		ArrayList<String> set = convertidores.getListaUtilizados();
		lconvertidores = set;

		if (set != null) {

			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				DatosConvertidorCombustibleSimpleCorrida dccs = datosconvs.get(clave);
				Combustible combOrigen = this.combustibles.get(dccs.getCombustibleOrigen());
				Combustible combDestino = this.combustibles.get(dccs.getCombustibleTransformado());
				RedCombustible redasociadaOrigen = this.redesCombustible.get(dccs.getCombustibleOrigen());
				RedCombustible redasociadaDestino = this.redesCombustible.get(dccs.getCombustibleTransformado());
				BarraCombustible bcorigen = redasociadaOrigen.getBarras().get(dccs.getBarraOrigen());
				BarraCombustible bcdestino = redasociadaDestino.getBarras().get(dccs.getBarraDestino());
				ConvertidorCombustibleSimple cccs = new ConvertidorCombustibleSimple(datosconvs.get(clave), combOrigen,
						combDestino, bcorigen, bcdestino);
				this.convertidores.put(clave, cccs);
				this.participantes.add(cccs);
				bcorigen.getConvertidoresSalientes().add(this.convertidores.get(clave));
				bcdestino.getConvertidoresEntrantes().add(this.convertidores.get(clave));
			}
		}

	}

	public void cargarAguasArriba(DatosHidraulicosCorrida hidros) {
		Hashtable<String, DatosHidraulicoCorrida> datos = hidros.getHidraulicos();
		ArrayList<String> set = hidros.getListaUtilizados();

		Iterator<String> itr = set.iterator();
		while (itr.hasNext()) {
			String clave = itr.next();
			GeneradorHidraulico gh = hidraulicos.get(clave);
			GeneradorHidraulico aguasAbajo = null;
			if (set.contains(datos.get(clave).getHidraulicoAguasAbajo())) {
				aguasAbajo = hidraulicos.get(datos.get(clave).getHidraulicoAguasAbajo());
			}
			ArrayList<GeneradorHidraulico> aguasArriba = new ArrayList<GeneradorHidraulico>();
			for (String gen : datos.get(clave).getHidraulicosAguasArriba()) {
				if (set.contains(gen))
					aguasArriba.add(hidraulicos.get(gen));
			}
			gh.actualizarAguasArribaAbajo(aguasArriba, aguasAbajo);
		}
		while (itr.hasNext()) {
			ArrayList<GeneradorHidraulico> todosAbajo = new ArrayList<GeneradorHidraulico>();
			String clave = itr.next();
			GeneradorHidraulico gh = hidraulicos.get(clave);
			GeneradorHidraulico gaux = gh;
			while (gaux.getGeneradorAbajo() != null) {
				todosAbajo.add(gaux.getGeneradorAbajo());
				gaux = gaux.getGeneradorAbajo();
			}
			gh.setTodosAbajo(todosAbajo);
		}

	}

	/** Procedimiento que crea y carga las fallas a partir de los datos */
	public void cargarFallas(DatosFallasEscalonadasCorrida falls, DatosLineaTiempo lt) {
		Hashtable<String, DatosFallaEscalonadaCorrida> datosFallas = falls.getFallas();
		Hashtable<String, Evolucion<String>> variablesComportamiento = falls.getValoresComportamiento();
		ArrayList<String> atributosDetallados = falls.getAtributosDetallados();
		if (atributosDetallados.size() != 0) {
			Falla.setAtributosDetallados(atributosDetallados);
		}

		// Se establece el comportamiento a nivel de clase para la falla
		FallaCompDesp
				.setCompFallaPorDefecto(variablesComportamiento.get(Constantes.COMPFALLA).getValor(instanteInicial));

		ArrayList<String> set = falls.getListaUtilizados();
		lfallas = set;
		if (set != null) {
			Iterator<String> itr = set.iterator();
			while (itr.hasNext()) {
				String clave = itr.next();
				Demanda d = demandas.get(datosFallas.get(clave).getDemanda());
				Falla f = new Falla(datosFallas.get(clave), d, lt);
				f.setTipo(utilitarios.Constantes.FALLA);
				fallas.put(clave, f);
				participantes.add(fallas.get(clave));
				participantesDirectos.add(fallas.get(clave));
				if (datosFallas.get(clave).isSalDetallada())
					partsConSalidaDetallada.add(fallas.get(clave));
				aportantesEstado.add(f);
				aportantesControlDE.add(f);
				if (!datosFallas.get(clave).getValsComps().get(Constantes.COMPFALLA).getValor(instanteInicial)
						.equalsIgnoreCase(Constantes.FALLASINESTADO))
					this.aportantesPostizacion.add(f.getCompS());
				d.setFalla(fallas.get(clave));
			}
		}

	}

	// /**Procedimiento que crea y carga los contratos de combustibles*/
	// public void cargarContratos( Hashtable<String,
	// DatosContratoCombustibleCorrida> contratos) {
	//
	// Set<String> set = contratos.keySet();
	//
	// Iterator<String> itr = set.iterator();
	// while (itr.hasNext()) {
	// String clave = itr.next();
	// DatosContratoCombustibleCorrida datos = contratos.get(clave);
	// Combustible comb = combustibles.get(datos.getCombustible());
	// BarraCombustible barra =
	// redesCombustible.get(comb.getNombre()).getBarras().get(datos.getBarra());
	// ContratoCombustible nuevo = new ContratoCombustible(datos, comb, barra);
	// this.contratos.put(datos.getNombre(),nuevo);
	// participantes.add(nuevo);
	// }
	//
	// }

	/** Devuelve la red de combustible asociada al combustible parómetro */
	private RedCombustible dameRedCombustible(String combustible) {
		return this.redesCombustible.get(combustible);
	}

	/**
	 * Devuelve la colección de combustibles extraóda de la colección de
	 * identificadores de combustible con identificadores de barras
	 */
	public Hashtable<String, Combustible> dameCombustibles(Set<String> combs) {
		Hashtable<String, Combustible> combustiblesListados = new Hashtable<String, Combustible>();

		Iterator<String> itr = combs.iterator();
		while (itr.hasNext()) {
			String clave = itr.next();
			combustiblesListados.put(clave, this.combustibles.get(clave));
		}
		return combustiblesListados;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Hashtable<String, Combustible> getCombustibles() {
		return combustibles;
	}

	public void setCombustibles(Hashtable<String, Combustible> combustibles) {
		this.combustibles = combustibles;
	}

	public Hashtable<String, RedCombustible> getRedesCombustible() {
		return redesCombustible;
	}

	public void setRedesCombustible(Hashtable<String, RedCombustible> redesCombustible) {
		this.redesCombustible = redesCombustible;
	}

	public Integer getMaximoIteraciones() {
		return maximoIteraciones;
	}

	public void setMaximoIteraciones(Integer maximoIteraciones) {
		this.maximoIteraciones = maximoIteraciones;
	}

	public String getCriterioParada() {
		return criterioParada;
	}

	public void setCriterioParada(String criterioParada) {
		this.criterioParada = criterioParada;
	}

	public Hashtable<String, GeneradorTermico> getTermicos() {
		return termicos;
	}

	public void setTermicos(Hashtable<String, GeneradorTermico> termicos) {
		this.termicos = termicos;
	}

	public Hashtable<String, GeneradorHidraulico> getHidraulicos() {
		return hidraulicos;
	}

	public void setHidraulicos(Hashtable<String, GeneradorHidraulico> hidraulicos) {
		this.hidraulicos = hidraulicos;
	}

	public Hashtable<String, GeneradorEolico> getEolicos() {
		return eolicos;
	}

	public void setEolicos(Hashtable<String, GeneradorEolico> eolicos) {
		this.eolicos = eolicos;
	}

	public Hashtable<String, GeneradorFotovoltaico> getFotovoltaicos() {
		return fotovoltaicos;
	}

	public void setFotovoltaicos(Hashtable<String, GeneradorFotovoltaico> fotovoltaicos) {
		this.fotovoltaicos = fotovoltaicos;
	}

	public Hashtable<String, ImpoExpo> getImpoExpos() {
		return impoExpos;
	}

	public void setImpoExpos(Hashtable<String, ImpoExpo> impoExpos) {
		this.impoExpos = impoExpos;
	}

	public Hashtable<String, Demanda> getDemandas() {
		return demandas;
	}

	public void setDemandas(Hashtable<String, Demanda> demandas) {
		this.demandas = demandas;
	}

	public Hashtable<String, Falla> getFallas() {
		return fallas;
	}

	public void setFallas(Hashtable<String, Falla> fallas) {
		this.fallas = fallas;
	}

	public Double getTasa() {
		return tasa;
	}

	public void setTasa(Double tasa) {
		this.tasa = tasa;
	}

	public RedElectrica getRed() {
		return red;
	}

	public void setRed(RedElectrica red) {
		this.red = red;
	}

	public Corrida(String id) {
		nombre = id;
	}

	public Barra getBarra(String barra) {
		return this.red.getBarra(barra);
	}

	// Devuelve la colección de barras
	public ArrayList<Barra> getBarras() {
		ArrayList<Barra> lista = new ArrayList<Barra>();
		Collection<Barra> col = this.red.getBarras().values();
		Iterator<Barra> it = col.iterator();
		while (it.hasNext()) {
			lista.add(it.next());
		}
		return lista;
	}

	public Collection<Participante> getParticipantes() {
		return participantes;
	}

	public void setParticipantes(ArrayList<Participante> participantes) {
		this.participantes = participantes;
	}

	public Hashtable<String, ConvertidorCombustibleSimple> getConvertidores() {
		return convertidores;
	}

	public void setConvertidores(Hashtable<String, ConvertidorCombustibleSimple> convertidores) {
		this.convertidores = convertidores;
	}

	public Integer getNumeroIteraciones() {
		return numeroIteraciones;
	}

	public void setNumeroIteraciones(Integer numeroIteraciones) {
		this.numeroIteraciones = numeroIteraciones;
	}

	public ArrayList<Generador> getGeneradoresBarra(String nombreBarra) {
		ArrayList<Generador> lista = new ArrayList<Generador>();
		Collection<Generador> col = this.red.getBarras().get(nombreBarra).getGeneradores();
		Iterator<Generador> it = col.iterator();
		while (it.hasNext()) {
			lista.add(it.next());
		}
		return lista;
	}

	public ArrayList<Demanda> getDemandasBarra(String nombreBarra) {
		ArrayList<Demanda> lista = new ArrayList<Demanda>();
		Collection<Demanda> col = this.red.getBarras().get(nombreBarra).getDemandas();
		Iterator<Demanda> it = col.iterator();
		while (it.hasNext()) {
			lista.add(it.next());
		}
		return lista;
	}

	public ArrayList<Falla> getFallasBarra(String nombreBarra) {
		ArrayList<Falla> lista = new ArrayList<Falla>();
		ArrayList<Demanda> dems = this.getDemandasBarra(nombreBarra);
		for (Demanda d : dems) {
			lista.add(d.getFalla());
		}
		return lista;
	}

	public void construirBarrasUnicasRedesCombustibles() {
		Set<String> claves = this.redesCombustible.keySet();
		Iterator<String> it = claves.iterator();
		while (it.hasNext()) {
			this.redesCombustible.get(it.next()).construirBarraUnica();
		}

	}

	public Azar dameAzar() {

		return this.estudio.getAzar();
	}

	public int getCantEscenarios() {

		return this.cantEscenarios;
	}

	public long getInstanteInicial() {
		return instanteInicial;
	}

	public void setInstanteInicial(int instanteInicial) {
		this.instanteInicial = instanteInicial;
	}

	public long getInstanteFinal() {
		return instanteFinal;
	}

	public void setInstanteFinal(int instanteFinal) {
		this.instanteFinal = instanteFinal;
	}

//	public int getInstanteInicialEstudio() {
//		return this.estudio.getInstanteInicial();
//	}

	public ResOptimIncrementos getResoptim() {
		return resoptim;
	}

	public void setResoptim(ResOptimIncrementos resoptim) {
		this.resoptim = resoptim;
	}

	public int getCantidadPasos() {
		return cantidadPasos;
	}

	public void setCantidadPasos(int cantidadPasos) {
		this.cantidadPasos = cantidadPasos;
	}

	public LineaTiempo getLineaTiempo() {
		return lineaTiempo;
	}

	public void setLineaTiempo(LineaTiempo lineaTiempo) {
		this.lineaTiempo = lineaTiempo;
	}

	public ArrayList<VariableEstado> getVarsEstadoParque() {
		ArrayList<VariableEstado> resultado = new ArrayList<VariableEstado>();

		for (AportanteEstado ap : aportantesEstado) {

			resultado.addAll(ap.aportarEstadoSimulacion());
		}
		return resultado;
	}

	public int dameDuracionPaso(int instante) {
		return this.getLineaTiempo().damePaso(instante).getDuracionPaso();
	}

	public ArrayList<VariableControl> getVarsControlParque() {
		ArrayList<VariableControl> resultado = new ArrayList<VariableControl>();
		for (Participante p : participantes) {
			resultado.addAll(p.getVarsControl());
		}
		return resultado;
	}

	// public ArrayList<VariableControl> getVarsControlDEParque() {
	// ArrayList<VariableControl> resultado = new ArrayList<VariableControl>();
	// for(Participante p: participantes) {
	// resultado.addAll(p.getVarsControlDE());
	// }
	// return resultado;
	// }

	public Hashtable<String, Evolucion<String>> getCompGlobales() {
		return compGlobales;
	}

	public void setCompGlobales(Hashtable<String, Evolucion<String>> compGlobales) {
		this.compGlobales = compGlobales;

	}

	public Integer getCantidadPostes() {
		return this.lineaTiempo.getCantidadPostes();
	}

	public Integer getDuracionPaso() {
		return this.lineaTiempo.getDuracionPaso();
	}

	public Hashtable<String, String> dameValsCompGlobal(long instanteInicial) {
		Hashtable<String, String> resultado = new Hashtable<String, String>();
		Set<String> keyset = compGlobales.keySet();
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String clave = (String) it.next();
			resultado.put(clave, compGlobales.get(clave).getValor(instanteInicial));
		}
		return resultado;
	}

	public Hashtable<String, ContratoCombustible> getContratosCombustible() {
		return contratosCombustible;
	}

	public void setContratosCombustible(Hashtable<String, ContratoCombustible> contratosCombustible) {
		this.contratosCombustible = contratosCombustible;
	}

	public Hashtable<String, ContratoEnergia> getContratosEnergia() {
		return contratosEnergia;
	}

	public void setContratosEnergia(Hashtable<String, ContratoEnergia> contratosEnergia) {
		this.contratosEnergia = contratosEnergia;
	}

	public Estudio getEstudio() {
		return estudio;
	}

	public void setEstudio(Estudio estudio) {
		this.estudio = estudio;
	}

	public void setCantEscenarios(int cantEscenarios) {
		this.cantEscenarios = cantEscenarios;
	}

	public boolean isPostizacionExterna() {
		return postizacionExterna;
	}

	public void setPostizacionExterna(boolean postizacionExterna) {
		this.postizacionExterna = postizacionExterna;
	}

	public ArrayList<AportantePost> getAportantesPostizacion() {
		return aportantesPostizacion;
	}

	public void setAportantesPostizacion(ArrayList<AportantePost> aportantesPostizacion) {
		this.aportantesPostizacion = aportantesPostizacion;
	}

	public boolean isValPostizacionExterna() {
		return valPostizacionExterna;
	}

	public void setValPostizacionExterna(boolean valPostizacionExterna) {
		this.valPostizacionExterna = valPostizacionExterna;
	}

	public String getRutaPostizacion() {
		return rutaPostizacion;
	}

	public void setRutaPostizacion(String rutaPostizacion) {
		this.rutaPostizacion = rutaPostizacion;
	}

	public boolean isSimulacionEncadenada() {
		return simulacionEncadenada;
	}

	public void setSimulacionEncadenada(boolean simulacionEncadenada) {
		this.simulacionEncadenada = simulacionEncadenada;
	}

	public boolean isCronologica() {
		return cronologica;
	}

	public void setCronologica(boolean cronologica) {
		this.cronologica = cronologica;
	}

	public ArrayList<AportanteEstado> getAportantesEstado() {
		return aportantesEstado;
	}

	public void setAportantesEstado(ArrayList<AportanteEstado> aportantesEstado) {
		this.aportantesEstado = aportantesEstado;
	}

	public ArrayList<AportanteControlDE> getAportantesControlDE() {
		return aportantesControlDE;
	}

	public void setAportantesControlDE(ArrayList<AportanteControlDE> aportantesControlDE) {
		this.aportantesControlDE = aportantesControlDE;
	}

	public ArrayList<Participante> getPartsConSalidaDetallada() {
		return partsConSalidaDetallada;
	}

	public void setPartsConSalidaDetallada(ArrayList<Participante> partsConSalidaDetallada) {
		this.partsConSalidaDetallada = partsConSalidaDetallada;
	}

	public void cargarLineaTiempo(DatosLineaTiempo lineaTiempo2, GregorianCalendar tiempoInicialCorrida,
			GregorianCalendar tiempoFinalCorrida, boolean externa, DatosPostizacion post) {
		if (!externa)
			lineaTiempo = new LineaTiempo(lineaTiempo2, tiempoInicialCorrida, tiempoFinalCorrida);
		else
			lineaTiempo = new LineaTiempo(lineaTiempo2, post, tiempoInicialCorrida, tiempoFinalCorrida);
	}

	public void cargarPEstocasticos(Hashtable<String, DatosProcesoEstocastico> procesosEstocasticos,
			Evolucion<Integer> cantSortMont) {
		this.estudio.getAzar().cargarPEstocasticos(procesosEstocasticos, cantSortMont);

	}

	public void vincularAsocOptimYExogenos() {
		this.estudio.getAzar().vincularAsocOptimYExogenos();
	}

	public ArrayList<AportanteMuestreo> getAportantesMuestreo() {
		return aportantesMuestreo;
	}

	public void setAportantesMuestreo(ArrayList<AportanteMuestreo> aportantesMuestreo) {
		this.aportantesMuestreo = aportantesMuestreo;
	}

	public ArrayList<Participante> getParticipantesDirectos() {
		return participantesDirectos;
	}

	public void setParticipantesDirectos(ArrayList<Participante> participantesDirectos) {
		this.participantesDirectos = participantesDirectos;
	}

	public ArrayList<Participante> getRedes() {
		return redes;
	}

	public void setRedes(ArrayList<Participante> redes) {
		this.redes = redes;
	}

	public void agregarPE(ProcesoEstocastico pe) {
		this.getEstudio().getAzar().agregarPE(pe);

	}

	public String getRutaSals() {
		return rutaSals;
	}

	/*
	 * param[0] ener_resumen energóa anual promedio en los escenarios; filas
	 * recurso; columnas aóo param[1] ener_cron energóa por aóo y escenario para
	 * todos los recursos: filas aóo,escenario; columnas recurso param[2] pot_poste
	 * para recursos en particular, un archivo por poste, filas paso, columnas poste
	 * param[3] lista de enteros int[] con los indicadores de los recursos para los
	 * que se va a sacar el archivo de pot
	 * 
	 * param[4] costo_resumen costo anual promedio en los escenarios; filas recurso;
	 * columnas aóo param[5] costo_cron costo por aóo y escenario para todos los
	 * recursos: filas (aóo,escenario); columnas recurso param[6] costo_poste para
	 * recursos en particular, un archivo por poste, filas paso, columnas poste
	 * param[7] lista de enteros int[] con los indicadores de los recursos para los
	 * que se va a sacar el archivo de costo_poste
	 *
	 * param[8] cosmar_resumen filas paso; columnas poste; (los promedios segón
	 * cantidad de horas = curva plana) param[9] cosmar_cron para barras en
	 * particular, un archivo por poste, filas paso, columnas crónicas param[10]
	 * lista de enterios int[] con los indices de las barras para los que se va a
	 * sacar los costos marginales detallados
	 * 
	 * param[11] Si es =1 genera un directorio cantMod, con un archivo de
	 * disponibilidades para cada recurso En esos archivos las filas son pasos y las
	 * columnas son escenarios (crónicas)
	 * 
	 * param[12] lista de enteros, uno por cada recurso, que indica con 1 si deben
	 * sacarse las salidas detalladas del recurso.
	 */
	public void cargarParamSalida(DatosParamSalida datosParamSalida) {
		this.datosParamSalida = datosParamSalida;
	}

	public void cargarParamSalidaOpt(DatosParamSalidaOpt datosParamSalidaOpt) {
		this.datosParamSalidaOpt = datosParamSalidaOpt;
	}

	public void cargarParamSalidaSim(DatosParamSalidaSim datosParamSalidaSim) {
		this.datosParamSalidaSim = datosParamSalidaSim;
	}

	public DatosParamSalida getDatosParamSalida() {
		return datosParamSalida;
	}

	public void setDatosParamSalida(DatosParamSalida datosParamSalida) {
		this.datosParamSalida = datosParamSalida;
	}

	public DatosParamSalidaOpt getDatosParamSalidaOpt() {
		return datosParamSalidaOpt;
	}

	public void setDatosParamSalidaOpt(DatosParamSalidaOpt datosParamSalidaOpt) {
		this.datosParamSalidaOpt = datosParamSalidaOpt;
	}

	public DatosParamSalidaSim getDatosParamSalidaSim() {
		return datosParamSalidaSim;
	}

	public void setDatosParamSalidaSim(DatosParamSalidaSim datosParamSalidaSim) {
		this.datosParamSalidaSim = datosParamSalidaSim;
	}

	public int dameCantSorteos(long instIniPaso) {

		return this.cantSorteosMont.getValor(instIniPaso);

	}

	public void setRutaSals(String rutaSals) {
		this.rutaSals = rutaSals;
	}

	public ArrayList<Participante> getPartsConSalidaDetallda() {
		return partsConSalidaDetallada;
	}

	public void setPartsConSalidaDetallda(ArrayList<Participante> partsConSalidaDetallda) {
		this.partsConSalidaDetallada = partsConSalidaDetallda;
	}

	public Evolucion<Integer> getCantSorteosMont() {
		return cantSorteosMont;
	}

	public void setCantSorteosMont(Evolucion<Integer> cantSorteosMont) {
		this.cantSorteosMont = cantSorteosMont;
	}

	public String getFase() {
		return fase;
	}

	public void setFase(String fase) {
		this.fase = fase;
	}

	public long getInstanteInicialPPaso() {

		return this.getLineaTiempo().getLinea().get(0).getInstanteInicial();
	}

	public long getInstanteFinalPPaso() {
		return this.getLineaTiempo().getLinea().get(0).getInstanteFinal();
	}

	public Hashtable<String, Acumulador> getAcumuladores() {
		return acumuladores;
	}

	public void setAcumuladores(Hashtable<String, Acumulador> acumuladores) {
		this.acumuladores = acumuladores;
	}

	public ConstructorHiperplanos getConstructorHiperplanos() {
		return constructorHiperplanos;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}
	
	
	

	public boolean isDespachoLinealEstocastico() {
		return despachoLinealEstocastico;
	}

	public void setDespachoLinealEstocastico(boolean despachoLinealEstocastico) {
		this.despachoLinealEstocastico = despachoLinealEstocastico;
	}

	public void setConstructorHiperplanos(ConstructorHiperplanos constructorHiperplanos) {
		this.constructorHiperplanos = constructorHiperplanos;
	}

	public String getCompBellman() {
		return compBellman;
	}

	public void setCompBellman(String compBellman) {
		this.compBellman = compBellman;
	}

	public Hashtable<String, Impacto> getImpactos() {
		return impactos;
	}

	public void setImpactos(Hashtable<String, Impacto> impactos) {
		this.impactos = impactos;
	}

	public String getCompDemanda() {
		return compDemanda;
	}

	public void setCompDemanda(String compDemanda) {
		this.compDemanda = compDemanda;
	}

	public int dameNumeroPasoInicioAnio(int anio) {

		return lineaTiempo.dameNumeroPasoInicioAnio(anio);
	}

	public int dameNumeroPasoFinAnio(int anio) {

		return lineaTiempo.dameNumeroPasoFinAnio(anio);
	}

	public ArrayList<Integer> dameCambiosDeAnio(int anioIni, int anioFin) {

		return lineaTiempo.dameCambiosAnio(anioIni, anioFin);
	}

	public ArrayList<String> getLtermicos() {
		return ltermicos;
	}

	public void setLtermicos(ArrayList<String> ltermicos) {
		this.ltermicos = ltermicos;
	}

	public ArrayList<String> getLhidraulicos() {
		return lhidraulicos;
	}

	public void setLhidraulicos(ArrayList<String> lhidraulicos) {
		this.lhidraulicos = lhidraulicos;
	}

	public ArrayList<String> getLacumuladores() {
		return lacumuladores;
	}

	public void setLacumuladores(ArrayList<String> lacumuladores) {
		this.lacumuladores = lacumuladores;
	}

	public ArrayList<String> getLeolicos() {
		return leolicos;
	}

	public void setLeolicos(ArrayList<String> leolicos) {
		this.leolicos = leolicos;
	}

	public ArrayList<String> getLfotovoltaicos() {
		return lfotovoltaicos;
	}

	public void setLfotovoltaicos(ArrayList<String> lfotovoltaicos) {
		this.lfotovoltaicos = lfotovoltaicos;
	}

	public ArrayList<String> getLimpoExpos() {
		return limpoExpos;
	}

	public void setLimpoExpos(ArrayList<String> limpoExpos) {
		this.limpoExpos = limpoExpos;
	}

	public ArrayList<String> getLdemandas() {
		return ldemandas;
	}

	public void setLdemandas(ArrayList<String> ldemandas) {
		this.ldemandas = ldemandas;
	}

	public ArrayList<String> getLfallas() {
		return lfallas;
	}

	public void setLfallas(ArrayList<String> lfallas) {
		this.lfallas = lfallas;
	}

	public ArrayList<String> getLimpactos() {
		return limpactos;
	}

	public void setLimpactos(ArrayList<String> limpactos) {
		this.limpactos = limpactos;
	}

	public ArrayList<String> getLcontratosEnergia() {
		return lcontratosEnergia;
	}

	public void setLcontratosEnergia(ArrayList<String> lcontratosEnergia) {
		this.lcontratosEnergia = lcontratosEnergia;
	}

	public ArrayList<String> getLcombustibles() {
		return lcombustibles;
	}

	public void setLcombustibles(ArrayList<String> lcombustibles) {
		this.lcombustibles = lcombustibles;
	}

	public ArrayList<String> getLredesCombustible() {
		return lredesCombustible;
	}

	public void setLredesCombustible(ArrayList<String> lredesCombustible) {
		this.lredesCombustible = lredesCombustible;
	}

	public ArrayList<String> getLcontratosCombustible() {
		return lcontratosCombustible;
	}

	public void setLcontratosCombustible(ArrayList<String> lcontratosCombustible) {
		this.lcontratosCombustible = lcontratosCombustible;
	}

	public ArrayList<String> getLconvertidores() {
		return lconvertidores;
	}

	public void setLconvertidores(ArrayList<String> lconvertidores) {
		this.lconvertidores = lconvertidores;
	}

//	public boolean isEscenariosSerializados() {
//		return this.escenariosSerializados;
//	}

	public void setFechaEjecucion(String fechaParaCorrida) {
		this.fechaEjecucion = fechaParaCorrida;

	}

	public String getHoraEjecucion() {
		return horaEjecucion;
	}

	public void setHoraEjecucion(String horaEjecucion) {
		this.horaEjecucion = horaEjecucion;
	}

	public String getFechaEjecucion() {
		return fechaEjecucion;
	}

	public String getNombreCorto() {
		return nombreCorto;
	}

	public void setNombreCorto(String nombreCorto) {
		this.nombreCorto = nombreCorto;
	}

	public ArrayList<EvolucionPorCaso> getEvolucionesPorCaso() {
		return evolucionesPorCaso;
	}

	public void setEvolucionesPorCaso(ArrayList<EvolucionPorCaso> evolucionesPorCaso) {
		this.evolucionesPorCaso = evolucionesPorCaso;
	}

	public void setPostizacion(String postizacion) {
		this.postizacion = postizacion;

	}
	
	

	public DatosTiposDeDia getTiposDeDia() {
		return tiposDeDia;
	}

	public void setTiposDeDia(DatosTiposDeDia tiposDeDia) {
		this.tiposDeDia = tiposDeDia;
	}

	public boolean isCorridaMultipleEstudio() {
		return corridaMultipleEstudio;
	}

	public void setCorridaMultipleEstudio(boolean corridaMultipleEstudio) {
		this.corridaMultipleEstudio = corridaMultipleEstudio;
	}

	public String getPostizacion() {
		return postizacion;
	}

	public int getClusters() {
		return this.clusters;
	}

	public void setClusters(int clusters) {
		this.clusters = clusters;
	}

	public Hashtable<String, CicloCombinado> getCiclosCombinados() {
		return ciclosCombinados;
	}

	public void setCiclosCombinados(Hashtable<String, CicloCombinado> ciclosCombinados) {
		this.ciclosCombinados = ciclosCombinados;
	}

	public boolean isCostosVariables() {
		return costosVariables;
	}

	public void setCostosVariables(boolean costosVariables) {
		this.costosVariables = costosVariables;
	}

	public ArrayList<String> getLciclosCombinados() {
		return lciclosCombinados;
	}

	public void setLciclosCombinados(ArrayList<String> lciclosCombinados) {
		this.lciclosCombinados = lciclosCombinados;
	}

	public double getTopeSpot() {
		return topeSpot;
	}

	public void setTopeSpot(double topeSpot) {
		this.topeSpot = topeSpot;
	}

	public boolean isDespSinExp() {
		return despSinExp;
	}

	public void setDespSinExp(boolean despSinExp) {
		this.despSinExp = despSinExp;
	}

	public int getIteracionSinExp() {
		return iteracionSinExp;
	}

	public void setIteracionSinExp(int iteracionSinExp) {
		this.iteracionSinExp = iteracionSinExp;
	}

	public ArrayList<String> getPaisesACortar() {
		return paisesACortar;
	}

	public void setPaisesACortar(ArrayList<String> paisesACortar) {
		this.paisesACortar = paisesACortar;
	}
	
	


	

	
	public ArrayList<String> getLcontratosInterrumpibles() {
		return lcontratosInterrumpibles;
	}

	public void setLcontratosInterrumpibles(ArrayList<String> lcontratosInterrumpibles) {
		this.lcontratosInterrumpibles = lcontratosInterrumpibles;
	}

//	public boolean isEscenariosSerializados() {
//		return escenariosSerializados;
//	}
//
//	public void setEscenariosSerializados(boolean escenariosSerializados) {
//		this.escenariosSerializados = escenariosSerializados;
//	}
//	
	
} 