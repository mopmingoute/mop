/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorXML is part of MOP.
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

package persistencia;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import datatypesProcEstocasticos.*;
import interfacesParticipantes.AportanteEstado;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import interfaz.Text;
import logica.CorridaHandler;
import parque.Corrida;
import pizarron.PizarronRedis;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import procesosEstocasticos.*;

import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.EvolucionPeriodica;
import tiempo.EvolucionPorCaso;
import tiempo.EvolucionPorInstantes;
import utilitarios.Constantes;
import utilitarios.Utilitarios;
import datatypes.DatosAcumuladorCorrida;
import datatypes.DatosBarraCombCorrida;
import datatypes.DatosBarraCorrida;
import datatypes.DatosCicloCombParte;
import datatypes.DatosCicloCombinadoCorrida;
import datatypes.DatosCombustibleCorrida;
import datatypes.DatosContratoCombustibleCorrida;
import datatypes.DatosContratoEnergiaCorrida;
import datatypes.DatosCorrida;
import datatypes.DatosDemandaCorrida;
import datatypes.DatosDiscretizacion;
import datatypes.DatosDuctoCombCorrida;
import datatypes.DatosEolicoCorrida;
import datatypes.DatosFallaEscalonadaCorrida;
import datatypes.DatosFotovoltaicoCorrida;
import datatypes.DatosHidraulicoCorrida;
import datatypes.DatosImpactoCorrida;
import datatypes.DatosImpoExpoCorrida;
import datatypes.DatosIteracionesCorrida;
import datatypes.DatosPolinomio;
import datatypes.DatosPronostico;
import datatypes.DatosRamaCorrida;
import datatypes.DatosTanqueCombustibleCorrida;
import datatypes.DatosTermicoCorrida;
import datatypes.DatosVariableAleatoria;
import datatypes.DatosVariableControlDE;
import datatypes.DatosVariableEstado;
import datatypes.Pair;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesSalida.DatosParamSalida;
import datatypesSalida.DatosParamSalidaOpt;
import datatypesSalida.DatosParamSalidaSim;
import datatypesTiempo.DatosLineaTiempo;

/**
 * Clase que efectÃºa la carga de un documento XML
 *
 * @author ut602614
 *
 */

public class CargadorXML {

	private Document dom;
	private DatosCorrida datosCorrida;

	private void cargarXML(String ruta) {
		try {
			File fXmlFile = new File(ruta);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dom = dBuilder.parse(fXmlFile);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * ES ESENCIAL QUE LOS PROCESOS ESTOCÃóSTICOS SE CONSTRUYAN CON SUS VARIABLES
	 * ALEATORIAS ANTES QUE LOS PARTICIPANTES QUE USAN LAS VARIABLES ALEATORIAS DE
	 * ESOS PROCESOS
	 *
	 * @param ruta
	 * @return
	 */
	public DatosCorrida cargarCorrida(String ruta) {
		cargarXML(ruta);
		DatosCorrida corrida = new DatosCorrida();
		datosCorrida = corrida;
		corrida.setRuta(ruta);

		NodeList listaParticipantes = dom.getElementsByTagName("participantes");
		NodeList listaParametros = dom.getElementsByTagName("parametrosGenerales");
		NodeList listaProcesosEstocasticos = dom.getElementsByTagName("procesosEstocasticos");
		NodeList listaUTE = dom.getElementsByTagName("datosUTE");
		
		Node parametros = listaParametros.item(0);
		if(!cargarParametrosGenerales(corrida, parametros)){
			return null;
		}
		
		Node procesos = listaProcesosEstocasticos.item(0);
		try{
			if(!cargarProcesosEstocasticos(corrida, procesos)){
				System.out.println("Error al cargar Proceso Estocastico");
				return null;
			}
		}catch (Exception e){
			System.out.println("Error al cargar Proceso Estocastico");
			return null;
		}
		
		Node uteDatos = listaUTE.item(0);


		Node participantes = listaParticipantes.item(0);
		cargarParticipantes(corrida, participantes);

		return corrida;
	}

	private boolean cargarProcesosEstocasticos(DatosCorrida corrida, Node procesos) {
		NodeList tipos = procesos.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < tipos.getLength(); temp++) {
			node = tipos.item(temp);

			if (node instanceof Element) {
				String etiqueta = node.getNodeName();

				if (etiqueta.equalsIgnoreCase("pEstocastico"))
					cargarProcesoEstocastico(corrida, node);

			}

		}
		return true;

	}

	private void cargarProcesoEstocastico(DatosCorrida corrida, Node node) {

		Element eElement = (Element) node;
		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		String tipo = eElement.getElementsByTagName("tipo").item(0).getTextContent();
		Evolucion<Double> va = null;
		if (tipo.equalsIgnoreCase("porEvolucion")) {
			va = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
							eElement.getElementsByTagName("va").item(0)).getElementsByTagName("ev"), "double",
					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		}

		String tipoSoporte = eElement.getElementsByTagName("tipoSoporte").item(0).getTextContent();
		Boolean discretoExhaustivo = Boolean
				.parseBoolean(eElement.getElementsByTagName("discretoExhaustivo").item(0).getTextContent());

		Boolean muestreado = Boolean.parseBoolean(eElement.getElementsByTagName("muestreado").item(0).getTextContent());

		ArrayList<Pair<String, Double>> estadosIniciales = new ArrayList<Pair<String, Double>>();
		if (eElement.getElementsByTagName("estadosIniciales").item(0) != null) {
			estadosIniciales = generarListaParesStringDouble(
					eElement.getElementsByTagName("estadosIniciales").item(0).getTextContent());
		}

		Hashtable<String, Double> hestadosIniciales = new Hashtable<String, Double>();

		for (Pair<String, Double> p : estadosIniciales) {
			hestadosIniciales.put(p.first, p.second);
		}


		Hashtable<String, DatosPronostico> hpronosticos = new Hashtable<String, DatosPronostico>(); 
		if (eElement.getElementsByTagName("pronosticos").item(0) != null) {
			NodeList pronosticos = eElement.getElementsByTagName("pronosticos").item(0).getChildNodes();
			for (int temp = 0; temp < pronosticos.getLength(); temp++) {
				node = pronosticos.item(temp);
				DatosPronostico dp = null;
				if (node instanceof Element) {
					String etiqueta = node.getNodeName();
	
					if (etiqueta.equalsIgnoreCase("pronostico")) {
						dp = generarPronostico(corrida, node);
					}
					hpronosticos.put(dp.getNombreVA(), dp);	
				}

			}
		}

		DatosProcesoEstocastico nuevo = new DatosProcesoEstocastico(nombre, tipo, tipoSoporte, null, discretoExhaustivo,
				muestreado, hestadosIniciales,hpronosticos);

		nuevo.setVa(va);

		corrida.getProcesosEstocasticos().put(nombre, nuevo);

	}

	private DatosPronostico generarPronostico(DatosCorrida corrida, Node nodo) {

		Element eElement = (Element) nodo;
		String nombre = eElement.getElementsByTagName("nombreVA").item(0).getTextContent();

		
		Evolucion<Double> pesos = (Evolucion<Double>) cargarNodoEvolucion(
				(Element) ((Element) eElement.getElementsByTagName("pesoPronostico").item(0)).getElementsByTagName("ev")
						.item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		
		
		Evolucion<Double> valores = (Evolucion<Double>) cargarNodoEvolucion(
				(Element)(
						(Element) eElement.getElementsByTagName("valores").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		DatosPronostico dp = new DatosPronostico(nombre, pesos, valores);

		return dp;

	}

	private void cargarParticipantes(DatosCorrida corrida, Node participantes) {
		NodeList tipos = participantes.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < tipos.getLength(); temp++) {
			node = tipos.item(temp);

			if (node instanceof Element) {
				String etiqueta = node.getNodeName();

				if (etiqueta.equalsIgnoreCase("generadores"))
					cargarGeneradores(corrida, node, corrida.getLineaTiempo());
				if (etiqueta.equalsIgnoreCase("comercioEnergia"))
					cargarComercioEnergia(corrida, node, corrida.getLineaTiempo());
				if (etiqueta.equalsIgnoreCase("demandas"))
					cargarDemandas(corrida, node);
				if (etiqueta.equalsIgnoreCase("fallas"))
					cargarFallas(corrida, node, corrida.getLineaTiempo());
				if (etiqueta.equalsIgnoreCase("impactosAmbientales"))
					cargarImpactos(corrida, node, corrida.getLineaTiempo());
			
				if (etiqueta.equalsIgnoreCase("contratos"))
					cargarContratosEnergia(corrida, node, corrida.getLineaTiempo());
				if (etiqueta.equalsIgnoreCase("redElectrica"))
					cargarRed(corrida, node);
				if (etiqueta.equalsIgnoreCase("combustibles"))
					cargarCombustibles(corrida, node);
			}

		}
	}

	private void cargarRed(DatosCorrida corrida, Node nodoPadre) {
		NodeList listaHijos = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("compsGenerales")) {

					Element e = (Element)((Element) eElement.getElementsByTagName(Constantes.COMPRED).item(0)).getElementsByTagName("ev").item(0);
					Evolucion<String> usoRed = (Evolucion<String>) cargarNodoEvolucion( e, "string",
							corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

					corrida.getRed().getValoresComportamiento().put("compUsoRed", usoRed);
				}
				if (etiqueta.equalsIgnoreCase("barras")) {
					cargarBarras(corrida, eElement);
				}
				if (etiqueta.equalsIgnoreCase("flotante")) {
					String flot = eElement.getTextContent();
//					String flot = eElement.getElementsByTagName("barra1").item(0);
					corrida.getRed().setFlotante(flot);
				}

				if (etiqueta.equalsIgnoreCase("ramas")) {
					cargarRamas(corrida, eElement);
				}
			}
		}
	}




	private void cargarRamas(DatosCorrida corrida, Node eElement) {
		String valor;
		NodeList listahijos = eElement.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listahijos.getLength(); temp++) {
			node = listahijos.item(temp);
			if (node instanceof Element) {
				Element el = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = el.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						corrida.getRed().getListaRamasUtilizadas().add(s);
				}if (etiqueta.equalsIgnoreCase("compsPorDefecto")) {
					// Se deja para mas adelante cargar este atributo ya que el diseño va a cambiar
				}if (etiqueta.equalsIgnoreCase("listaRamas")) {
					NodeList listabarras = node.getChildNodes();
					Node rama = null;
					for (int i = 0; i < listabarras.getLength(); i++) {
						rama = listabarras.item(i);
						etiqueta = rama.getNodeName();
						if (etiqueta.equalsIgnoreCase("rama")) {
							cargarRama(corrida, rama);
						}
					}
				}

			}
		}

	}

	private void cargarRama(DatosCorrida corrida, Node barra) {
		Element eElement = (Element) barra;
		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		Evolucion<String> compRama = new EvolucionConstante<String>(
				eElement.getElementsByTagName("compRama").item(0).getTextContent(),
				corrida.getLineaTiempo().getSentido());
		Hashtable<String, Evolucion<String>> valsComp = new Hashtable<String, Evolucion<String>>();
		valsComp.put("compRama", compRama);
		String barra1 = eElement.getElementsByTagName("barra1").item(0).getTextContent();
		String barra2 = eElement.getElementsByTagName("barra2").item(0).getTextContent();
		Evolucion<Double> peaje12 = (Evolucion<Double>)  cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("peaje12").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());
		Evolucion<Double> peaje21 = (Evolucion<Double>)  cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("peaje21").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());
		Evolucion<Double> potMax12 = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("potMax12").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> potMax21 = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("potMax21").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());
		Evolucion<Double> perdidas12 = (Evolucion<Double>)  cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("perdidas12").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> perdidas21 = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("perdidas21").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> X = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eElement.getElementsByTagName("X").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> R = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eElement.getElementsByTagName("R").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Integer cantModIni = Integer.parseInt(eElement.getElementsByTagName("cantModIni").item(0).getTextContent());
		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		DatosRamaCorrida nuevo = new DatosRamaCorrida(nombre, barra1, barra2, peaje12, peaje21, potMax12, potMax21,
				perdidas12, perdidas21, X, R, cantModInst, cantModIni, compRama, dispMedia, tMedioArreglo, valsComp, salDet,
				mantProgramado, costoFijo);

		corrida.getRed().getRamas().put(nombre, nuevo);
	}

	private void cargarBarras(DatosCorrida corrida, Node eElement) {
		String valor;
		NodeList listahijos = eElement.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listahijos.getLength(); temp++) {
			node = listahijos.item(temp);
			if (node instanceof Element) {
				Element el = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = el.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						corrida.getRed().getListaBarrasUtilizadas().add(s);
				}

				if (etiqueta.equalsIgnoreCase("listaBarras")) {
					NodeList listabarras = node.getChildNodes();
					Node barra = null;
					for (int i = 0; i < listabarras.getLength(); i++) {
						barra = listabarras.item(i);
						etiqueta = barra.getNodeName();
						if (etiqueta.equalsIgnoreCase("barra")) {
							cargarBarra(corrida, barra);
						}
					}
				}

			}
		}

	}

	private void cargarBarra(DatosCorrida corrida, Node barra) {
		Element eElement = (Element) barra;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		DatosBarraCorrida nuevo = new DatosBarraCorrida(nombre, salDet);
		corrida.getRed().getBarras().put(nombre, nuevo);

	}

	private void cargarCombustibles(DatosCorrida corrida, Node nodoPadre) {
		NodeList listaHijos = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaCombustibles")) {
					cargarListaCombustibles(corrida, eElement, corrida.getLineaTiempo());
				}
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					String valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						corrida.getCombustibles().getListaUtilizados().add(s);
				}
			}
		}
	}

	// private void cargarListaConvertidores(DatosCorrida corrida, Element
	// eElement) {
	// NodeList listaconvertidores = eElement.getChildNodes();
	// Node convertidor = null;
	// String etiqueta;
	// for (int i = 0; i < listaconvertidores.getLength(); i++) {
	// convertidor = listaconvertidores.item(i);
	// if (convertidor instanceof Element) {
	// etiqueta = convertidor.getNodeName();
	// if (etiqueta.equalsIgnoreCase("convertidorCombustible")) {
	// cargarConvertidor(corrida, convertidor);
	// }
	// }
	// }
	// }

	// private void cargarConvertidor(DatosCorrida corrida, Node convertidor) {
	// Element eElement = (Element) convertidor;
	//
	// String nombre =
	// eElement.getElementsByTagName("nombre").item(0).getTextContent();
	// String cantModulos =
	// eElement.getElementsByTagName("cantModulos").item(0).getTextContent();
	// String cantModulosDisponibles =
	// eElement.getElementsByTagName("cantModulosDisponibles").item(0).getTextContent();
	// String combustibleOrigen =
	// eElement.getElementsByTagName("combustibleOrigen").item(0).getTextContent();
	// String combustibleTransformado =
	// eElement.getElementsByTagName("combustibleTransformado").item(0).getTextContent();
	// String barraOrigen =
	// eElement.getElementsByTagName("barraOrigen").item(0).getTextContent();
	// String barraDestino =
	// eElement.getElementsByTagName("barraDestino").item(0).getTextContent();
	// String flujoMaxOrigen =
	// eElement.getElementsByTagName("flujoMaxOrigen").item(0).getTextContent();
	// String flujoMaxConvertido =
	// eElement.getElementsByTagName("flujoMaxConvertido").item(0).getTextContent();
	// String relacion =
	// eElement.getElementsByTagName("relacion").item(0).getTextContent();
	//
	//
	//
	// DatosConvertidorCombustibleSimpleCorrida nuevo = new
	// DatosConvertidorCombustibleSimpleCorrida(nombre,
	// Integer.parseInt(cantModulos), Integer.parseInt(cantModulosDisponibles),
	// combustibleOrigen,
	// combustibleTransformado, barraOrigen, barraDestino,
	// Double.parseDouble(flujoMaxOrigen),
	// Double.parseDouble(flujoMaxConvertido), Double.parseDouble(relacion));
	// corrida.getConvertidores().getConvertidores().put(nombre,nuevo);
	// }

	private void cargarListaCombustibles(DatosCorrida corrida, Element eElement, DatosLineaTiempo lt) {
		NodeList listacombustibles = eElement.getChildNodes();
		Node combustible = null;
		String etiqueta;
		ArrayList<String> ordenCargaXML = new ArrayList<>();
		for (int i = 0; i < listacombustibles.getLength(); i++) {
			combustible = listacombustibles.item(i);
			if (combustible instanceof Element) {
				etiqueta = combustible.getNodeName();
				if (etiqueta.equalsIgnoreCase("combustible")) {
					String nombre = cargarCombustible(corrida, combustible, lt, false);
					if(nombre != null){
						ordenCargaXML.add(nombre);
					}

				}
			}
		}
		corrida.getCombustibles().setOrdenCargaXML(ordenCargaXML);

	}

	public String cargarCombustible(DatosCorrida corrida, Node combustible, DatosLineaTiempo lt, boolean copiadoPortapapeles) {
		NodeList listaHijos = combustible.getChildNodes();
		Node node = null;
		String nomcomb = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				Element eElement = (Element) node;

				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("datosCombustible")) {
					cargarDatosCombustible(corrida, eElement, lt);
					nomcomb = eElement.getElementsByTagName("nombre").item(0).getTextContent();
					if(!copiadoPortapapeles && !corrida.getCombustibles().getListaUtilizados().contains(nomcomb)){
						return null;
					}
				}
				if (etiqueta.equalsIgnoreCase("red")) {
					if(nomcomb != null){
					cargarRedCombustible(corrida.getCombustibles().getCombustibles().get(nomcomb), eElement, lt);}
				}
			}
		}
		return nomcomb;
	}

	private void cargarRedCombustible(DatosCombustibleCorrida combcorrida, Element eElement, DatosLineaTiempo lt) {
		NodeList listaHijos = eElement.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				Element eEl = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("compsGenerales")) {
					Evolucion<String> usoRed = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
//							eElement.getElementsByTagName("usoRed").item(0), "string", lt.getTiempoInicial(), lt);
									eElement.getElementsByTagName(Constantes.COMPRED).item(0)).getElementsByTagName("ev").item(0), "string", lt.getTiempoInicial(),
							lt);
					combcorrida.getRed().getValoresComportamiento().put("compUsoRed", usoRed);
				}
				if (etiqueta.equalsIgnoreCase("listaBarrasComb")) {
					NodeList listabarras = node.getChildNodes();
					Node barra = null;

					for (int i = 0; i < listabarras.getLength(); i++) {
						barra = listabarras.item(i);
						if (barra instanceof Element) {
							etiqueta = barra.getNodeName();
							if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
								String valor = barra.getTextContent();
								ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
								for (String s : utilizados)
									combcorrida.getRed().getBarrasUtilizadas().add(s);
							}
							if (etiqueta.equalsIgnoreCase("barraComb")) {
								cargarBarraCombustible(combcorrida, (Element) barra);
							}
						}
					}

				}
				if (etiqueta.equalsIgnoreCase("listaDuctosComb")) {
					NodeList listaductos = node.getChildNodes();
					Node ducto = null;

					for (int i = 0; i < listaductos.getLength(); i++) {
						ducto = listaductos.item(i);
						if (ducto instanceof Element) {
							etiqueta = ducto.getNodeName();
							if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
								String valor = ducto.getTextContent();
								ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
								for (String s : utilizados)
									combcorrida.getRed().getRamasUtilizadas().add(s);
							}
							if (etiqueta.equalsIgnoreCase("ductoComb")) {
								cargarDuctoCombustible(combcorrida, (Element) listaductos, lt);
							}
						}
					}
				}
				if (etiqueta.equalsIgnoreCase("listaTanques")) {
					cargarTanquesCombustible(combcorrida, eEl);
				}
				if (etiqueta.equalsIgnoreCase("listaContratosCombustibleCanioSimple")) {
					cargarContratosCombustible(combcorrida, eEl, lt);
				}

			}
		}

	}

	private void cargarContratosCombustible(DatosCombustibleCorrida combCorrida, Element eEl, DatosLineaTiempo lt) {
		NodeList listacontratos = eEl.getChildNodes();
		Node contrato = null;
		String etiqueta;
		for (int i = 0; i < listacontratos.getLength(); i++) {
			contrato = listacontratos.item(i);
			if (contrato instanceof Element) {
				etiqueta = contrato.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					String valor = contrato.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						combCorrida.getRed().getContratosUtilizados().add(s);
				}
				if (etiqueta.equalsIgnoreCase("contratoCanioSimple")) {
					cargarContratoCombustible(combCorrida, (Element) contrato, lt);
				}
			}
		}

	}

	/**
	 * @param combCorrida
	 * @param eEl
	 */
	private void cargarContratoCombustible(DatosCombustibleCorrida combCorrida, Element eEl, DatosLineaTiempo lt) {
		String nombre = eEl.getElementsByTagName("nombre").item(0).getTextContent();

		String barra = eEl.getElementsByTagName("barra").item(0).getTextContent();
		String comb = eEl.getElementsByTagName("combustible").item(0).getTextContent();

		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int", lt.getTiempoInicial(), lt);

		Integer cantModIni = Integer.parseInt(eEl.getElementsByTagName("cantModIni").item(0).getTextContent());
		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eEl.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0),
				"double", lt.getTiempoInicial(), lt);
		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);
		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0), "int", lt.getTiempoInicial(), lt);
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eEl.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0),
				"double", lt.getTiempoInicial(), lt);

		Evolucion<Double> caudalMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eEl.getElementsByTagName("caudalMax").item(0)).getElementsByTagName("ev").item(0),
				"double", lt.getTiempoInicial(), lt);
		Evolucion<Double> precioComb = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("precioComb").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		boolean salDet = Boolean.parseBoolean(eEl.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		DatosContratoCombustibleCorrida nuevo = new DatosContratoCombustibleCorrida(nombre, barra, comb, cantModInst,
				cantModIni, dispMedia, tMedioArreglo, caudalMax, precioComb, salDet, mantProgramado, costoFijo);

		combCorrida.getRed().getContratos().add(nuevo);
	}

	private void cargarTanquesCombustible(DatosCombustibleCorrida combCorrida, Element eEl) {
		NodeList listatanques = eEl.getChildNodes();
		Node tanque = null;
		String etiqueta;
		for (int i = 0; i < listatanques.getLength(); i++) {
			tanque = listatanques.item(i);
			if (tanque instanceof Element) {
				etiqueta = tanque.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					String valor = tanque.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						combCorrida.getRed().getTanquesUtilizados().add(s);
				}
				if (etiqueta.equalsIgnoreCase("tanque")) {
					cargarTanque(combCorrida, (Element) tanque);
				}
			}
		}

	}

	private void cargarTanque(DatosCombustibleCorrida combCorrida, Element eEl) {
		String nombre = eEl.getElementsByTagName("nombre").item(0).getTextContent();
		String cantMod = eEl.getElementsByTagName("cantModulos").item(0).getTextContent();
		String cantModDisponibles = eEl.getElementsByTagName("cantModulosDisponibles").item(0).getTextContent();
		String cantIni = eEl.getElementsByTagName("cantIni").item(0).getTextContent();
		String barra = eEl.getElementsByTagName("barra").item(0).getTextContent();
		String capacidad = eEl.getElementsByTagName("capacidad").item(0).getTextContent();
		String valComb = eEl.getElementsByTagName("valComb").item(0).getTextContent();

		DatosTanqueCombustibleCorrida nuevo = new DatosTanqueCombustibleCorrida(nombre, Integer.parseInt(cantMod),
				Integer.parseInt(cantModDisponibles), Double.parseDouble(cantIni), barra, Double.parseDouble(capacidad),
				Double.parseDouble(valComb));
		combCorrida.getRed().getTanques().add(nuevo);
	}

	private void cargarDuctoCombustible(DatosCombustibleCorrida combCorrida, Element eEl, DatosLineaTiempo lt) {
		String nombre = eEl.getElementsByTagName("nombre").item(0).getTextContent();
		String barra1 = eEl.getElementsByTagName("barra1").item(0).getTextContent();
		String barra2 = eEl.getElementsByTagName("barra2").item(0).getTextContent();
		Evolucion<Double> capacidad12 = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("capacidad12").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		Evolucion<Double> capacidad21 = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("capacidad21").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		Evolucion<Double> perdidas12 = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("perdidas12").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);
		Evolucion<Double> perdidas21 = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("perdidas21").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int", lt.getTiempoInicial(), lt);

		Integer cantModIni = Integer.parseInt(eEl.getElementsByTagName("cantModIni").item(0).getTextContent());

		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eEl.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0),
				"double", lt.getTiempoInicial(), lt);
		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0) , "double", lt.getTiempoInicial(), lt);
		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
				eEl.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0) , "int", lt.getTiempoInicial(), lt);
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eEl.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0),
				"double", lt.getTiempoInicial(), lt);

		boolean salDet = Boolean.parseBoolean(eEl.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		DatosDuctoCombCorrida nuevo = new DatosDuctoCombCorrida(nombre, cantModInst, barra1, barra2, capacidad12,
				capacidad21, perdidas12, perdidas21, cantModIni, dispMedia, tMedioArreglo, salDet, mantProgramado,
				costoFijo);
		combCorrida.getRed().getRamas().add(nuevo);

	}

	private void cargarBarraCombustible(DatosCombustibleCorrida combCorrida, Element eEl) {
		String nombre = eEl.getElementsByTagName("nombre").item(0).getTextContent();

		DatosBarraCombCorrida nuevo = new DatosBarraCombCorrida(nombre);
		combCorrida.getRed().getBarras().add(nuevo);

	}

	private void cargarDatosCombustible(DatosCorrida corrida, Element eElement, DatosLineaTiempo lt) {
		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		Double pci = Double.parseDouble(eElement.getElementsByTagName("pci").item(0).getTextContent());
		Double densidad = Double.parseDouble(eElement.getElementsByTagName("densidad").item(0).getTextContent());
		String unidad = eElement.getElementsByTagName("unidad").item(0).getTextContent();
		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		DatosCombustibleCorrida nuevo = new DatosCombustibleCorrida(nombre, unidad, pci, densidad, salDet);
		corrida.getCombustibles().getCombustibles().put(nombre, nuevo);

	}

	private void cargarFallas(DatosCorrida corrida, Node nodoPadre, DatosLineaTiempo lt) {
		NodeList listaHijos = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String valor;
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("compsPorDefecto")) {
					Evolucion<String> cargarEvolucion = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
									eElement.getElementsByTagName("compFalla").item(0)).getElementsByTagName("ev").item(0), "string",
							corrida.getLineaTiempo().getTiempoInicial(), lt);
					Evolucion<String> compFalla = cargarEvolucion;
					corrida.getFallas().getValoresComportamiento().put("compFalla", compFalla);

				}
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						corrida.getFallas().getListaUtilizados().add(s);
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getFallas().setAtributosDetallados(atributos);
				}

				if (etiqueta.equalsIgnoreCase("listaFallas")) {
					NodeList listafallas = node.getChildNodes();
					Node falla = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listafallas.getLength(); i++) {
						falla = listafallas.item(i);
						etiqueta = falla.getNodeName();
						if (etiqueta.equalsIgnoreCase("falla")) {
							String nombre = cargarFalla(corrida, falla, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}

						}
					}
					corrida.getFallas().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}

	}

	public String cargarFalla(DatosCorrida corrida, Node falla, boolean copiadoPortapapeles) {
		Element eElement = (Element) falla;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getFallas().getListaUtilizados().contains(nombre)){
			return null;
		}

		Evolucion<String> cargarEvolucion = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("compFalla").item(0)).getElementsByTagName("ev").item(0), "string",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<String> compFalla = cargarEvolucion;
		Hashtable<String, Evolucion<String>> valsComps = new Hashtable<String, Evolucion<String>>();
		valsComps.put("compFalla", compFalla);
		// Integer cantEscProgram =
		// Integer.parseInt(eElement.getElementsByTagName("cantEscProgram").item(0).getTextContent());

		/*String durMinForzamientos = eElement.getElementsByTagName("durMinForzamientos").item(0).getTextContent();
		int[] durMinForzDias = generarListaIntConSeparador(durMinForzamientos, ",");*/
//		Evolucion<Integer> cantEscProgram =  (Evolucion<Integer>) cargarEvolucion(
//                eElement.getElementsByTagName("cantEscProgram").item(0), "int",
//                corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		int cantEscProgram = Integer.parseInt(eElement.getElementsByTagName("cantEscProgram").item(0).getTextContent());
		String demanda = eElement.getElementsByTagName("demanda").item(0).getTextContent();
		Node escalonesPorciento = eElement.getElementsByTagName("escalonesPorciento").item(0);
		Element elEvol = (Element) escalonesPorciento;
//		escalonesPorciento = elEvol.getElementsByTagName("ev").item(0);
//		ArrayList<Pair<Double, Double>> escalones = generarListaPares(escalonesPorciento);
		ArrayList<Pair<Double, Double>> escalones = generarListaPares(elEvol);
		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		Hashtable<String, DatosVariableEstado> varsEstado = null;
		if (eElement.getElementsByTagName("variablesEstado").item(0) != null) {
			varsEstado = cargarVariablesEstado(corrida, eElement.getElementsByTagName("variablesEstado").item(0));
		}

		Hashtable<String, DatosVariableControlDE> varsControlDE = null;
		if (eElement.getElementsByTagName("variablesControlDE").item(0) != null) {
			varsControlDE = cargarVariablesControlDE(corrida,
					eElement.getElementsByTagName("variablesControlDE").item(0));
		}

		DatosFallaEscalonadaCorrida nuevo = new DatosFallaEscalonadaCorrida(nombre, compFalla, valsComps, demanda,
				escalones, cantEscProgram, null, varsEstado, varsControlDE, salDet);

		corrida.getFallas().getFallas().put(nuevo.getNombre(), nuevo);
		return nombre;

	}

	private void cargarImpactos(DatosCorrida corrida, Node nodoPadre, DatosLineaTiempo lt) {
		NodeList listaHijos = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String valor;
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						corrida.getImpactos().getListaUtilizados().add(s);
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getImpactos().setAtributosDetallados(atributos);
				}

				if (etiqueta.equalsIgnoreCase("listaImpactos")) {
					NodeList listaimpactos = node.getChildNodes();
					Node impacto = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listaimpactos.getLength(); i++) {
						impacto = listaimpactos.item(i);
						etiqueta = impacto.getNodeName();
						if (etiqueta.equalsIgnoreCase("impacto")) {
							String nombre = cargarImpacto(corrida, impacto, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}

						}
					}
					corrida.getImpactos().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}

	}

	public String cargarImpacto(DatosCorrida corrida, Node impacto, boolean copiadoPortapapeles) {
		Element eElement = (Element) impacto;
		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getImpactos().getListaUtilizados().contains(nombre)){
			return null;
		}
		Evolucion<Boolean> activo = (Evolucion<Boolean>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("activo").item(0)).getElementsByTagName("ev").item(0), "boolean", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());

		Evolucion<Double> costo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eElement.getElementsByTagName("costo").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> limite = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("limite").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		boolean porPoste = Boolean.parseBoolean(eElement.getElementsByTagName("porPoste").item(0).getTextContent());
		ArrayList<String> involucrados = generarListaStringConSeparador(
				eElement.getElementsByTagName("participantesInvolucrados").item(0).getTextContent(), ",");

		String tipo = eElement.getElementsByTagName("tipoImpacto").item(0).getTextContent();
		int tipoImpacto = -1;

		if (tipo.equalsIgnoreCase(Text.STRING_HIDRO_INUN_AGUAS_ARRIBA)) {
			tipoImpacto = Constantes.HIDRO_INUN_AGUAS_ARRIBA;
		}
		if (tipo.equalsIgnoreCase(Text.STRING_HIDRO_INUN_AGUAS_ABAJO)) {
			tipoImpacto = Constantes.HIDRO_INUN_AGUAS_ABAJO;
		}
		if (tipo.equalsIgnoreCase(Text.STRING_TER_EMISIONES_CO2)) {
			tipoImpacto = Constantes.TER_EMISIONES_CO2;
		}
		if (tipo.equalsIgnoreCase(Text.STRING_HIDRO_PROD_MAQUINA)) {
			tipoImpacto = Constantes.HIDRO_PROD_MAQUINA;
		}
		if (tipo.equalsIgnoreCase(Text.STRING_HIDRO_CAUDAL_ECOLOGICO)) {

			tipoImpacto = Constantes.HIDRO_CAUDAL_ECOLOGICO;
		}
		if (tipo.equalsIgnoreCase(Text.STRING_HIDRO_VERTIMIENTO_EXTERNO)) {

			tipoImpacto = Constantes.HIDRO_VERTIMIENTO_EXTERNO;
		}

		boolean porUnidadTiempo = Boolean
				.parseBoolean(eElement.getElementsByTagName("porUnidadDeTiempo").item(0).getTextContent());

		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());
		DatosImpactoCorrida nuevo = new DatosImpactoCorrida(nombre, activo, costo, limite, porPoste, involucrados,
				tipoImpacto, porUnidadTiempo, salDet);

		corrida.getImpactos().getImpactos().put(nombre, nuevo);
		
		return nombre;

	}

	private void cargarContratosEnergia(DatosCorrida corrida, Node nodoPadre, DatosLineaTiempo lt) {
		NodeList listaHijos = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String valor;
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						corrida.getContratosEnergia().getListaUtilizados().add(s);
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getContratosEnergia().setAtributosDetallados(atributos);
				}

				if (etiqueta.equalsIgnoreCase("listaContratos")) {
					NodeList listaContratos = node.getChildNodes();
					Node contrato = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listaContratos.getLength(); i++) {
						contrato = listaContratos.item(i);
						etiqueta = contrato.getNodeName();
						if (etiqueta.equalsIgnoreCase("contrato")) {
							String nombre = cargarContratoEnergia(corrida, contrato, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}

						}
					}
					corrida.getContratosEnergia().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}

	}

	public String cargarContratoEnergia(DatosCorrida corrida, Node impacto, boolean copiadoPortapapeles) {
		Element eElement = (Element) impacto;
		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getContratosEnergia().getListaUtilizados().contains(nombre)){
			return null;
		}
		ArrayList<String> involucrados = generarListaStringConSeparador(
				eElement.getElementsByTagName("participantesInvolucrados").item(0).getTextContent(), ",");
		Evolucion<Double> precioBase = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("precioBase").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> energiaBase = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("energiaBase").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		String fechaInicial = eElement.getElementsByTagName("fechaInicial").item(0).getTextContent();

		Integer cantAnios = Integer.parseInt(eElement.getElementsByTagName("cantAnios").item(0).getTextContent());

		Double energiaInicial = Double
				.parseDouble(eElement.getElementsByTagName("energiaInicial").item(0).getTextContent());

		Evolucion<Double> cotaInf = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cotaInf").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());

		Evolucion<Double> cotaSup = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cotaSup").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());

		String tipo = eElement.getElementsByTagName("tipoContrato").item(0).getTextContent();

		if (tipo.equalsIgnoreCase(Constantes.LIM_ENERGIA_ANUAL)) {
			tipo = Constantes.LIM_ENERGIA_ANUAL;
		} else {
			System.out.println("Error en tipo de contratoEnergia, tipo = " + tipo);
		}

		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());
		DatosContratoEnergiaCorrida nuevo = new DatosContratoEnergiaCorrida(nombre, involucrados, precioBase,
				energiaBase, fechaInicial, cantAnios, energiaInicial, tipo, cotaInf, cotaSup, salDet);

//		public DatosContratoEnergiaCorrida(String nombre, ArrayList<String> nombresInvolucrados,
//				Evolucion<Double> precioBase, Evolucion<Double> energiaBase, String fechaInicial, int cantAnios,
//				double energiaInicial, String tipo, Evolucion<Double> cotaInf, Evolucion<Double> cotaSup,
//				boolean salidaDetallada) {

		corrida.getContratosEnergia().getContratosEnergia().put(nombre, nuevo);
		return nombre;
	}

	

	

	public String cargarContratoInterrumpible(DatosCorrida corrida, Node interrumpible) {
		Element eElement = (Element) interrumpible;
		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		String barra = eElement.getElementsByTagName("barra").item(0).getTextContent();

		String tipo = eElement.getElementsByTagName("tipoContrato").item(0).getTextContent();

		Evolucion<Double> precioBase = (Evolucion<Double>) cargarNodoEvolucion(
				(Element) ((Element) eElement.getElementsByTagName("precioBase").item(0)).getElementsByTagName("ev")
						.item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		
		String fechaInicial = eElement.getElementsByTagName("fechaInicial").item(0).getTextContent();
		Integer cantAnios = Integer.parseInt(eElement.getElementsByTagName("cantAnios").item(0).getTextContent());

		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		if (tipo.equalsIgnoreCase(Constantes.INT_TOMA_SIEMPRE)) {

			Evolucion<Double> probToma = (Evolucion<Double>) cargarNodoEvolucion(
					(Element) ((Element) eElement.getElementsByTagName("probToma").item(0)).getElementsByTagName("ev")
							.item(0),
					"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

			ArrayList<Double> multaMWh = generarListaDoubles(
					eElement.getElementsByTagName("multaMWh").item(0).getTextContent(), ",");

			ArrayList<Double> porcentajeEnergiaObligatoriaGWh = generarListaDoubles(
					eElement.getElementsByTagName("porcentajeEnergiaObligatoria").item(0).getTextContent(), ",");

			Integer cantPuntosDiscrectizacion = Integer.parseInt(eElement.getElementsByTagName("cantPuntosDiscretizacion").item(0).getTextContent());

			
			Evolucion<Double> protMaxContrato = (Evolucion<Double>) cargarNodoEvolucion(
					(Element) ((Element) eElement.getElementsByTagName("potMaxContrato").item(0))
							.getElementsByTagName("ev").item(0),
					"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

			Hashtable<String, DatosVariableEstado> varsEstado = null;
			// if (eElement.getElementsByTagName("variablesEstado").item(0) != null) {
			varsEstado = cargarVariablesEstado(corrida, eElement.getElementsByTagName("variablesEstado").item(0));
			ArrayList<DatosVariableEstado> arr = new ArrayList<DatosVariableEstado>(varsEstado.values());

//			}

		

		} else {
			System.out.println("Error en tipo de contratoEnergia, tipo = " + tipo);
		}

//		public DatosContratoEnergiaCorrida(String nombre, ArrayList<String> nombresInvolucrados,
//				Evolucion<Double> precioBase, Evolucion<Double> energiaBase, String fechaInicial, int cantAnios,
//				double energiaInicial, String tipo, Evolucion<Double> cotaInf, Evolucion<Double> cotaSup,
//				boolean salidaDetallada) {

		return nombre;
	}
	
	
	public static Pair<Double, Double> generarPar(String string) {
		String aux = string.substring(1, string.length() - 1);
		String[] par = aux.split(";");

		return new Pair<Double, Double>(Double.parseDouble(par[0]), Double.parseDouble(par[1]));
	}

	public static Pair<String, String> generarParString(String string) {
		String aux = string.substring(1, string.length() - 1);
		String[] par = aux.split(";");

		return new Pair<String, String>(par[0], par[1]);
	}

	private Pair<Double, Double> generarParDoubleDouble(String string) {
		String aux = string.substring(1, string.length() - 1);
		String[] par = aux.split(";");

		return new Pair<Double, Double>(Double.parseDouble(par[0]), Double.parseDouble(par[1]));
	}

	private Pair<String, Double> generarParStringDouble(String string) {
		String aux = string.substring(1, string.length() - 1);
		String[] par = aux.split(";");

		return new Pair<String, Double>(par[0], Double.parseDouble(par[1]));
	}

	private void cargarDemandas(DatosCorrida corrida, Node nodoPadre) {
		NodeList listaHijos = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String valor;
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						corrida.getDemandas().getListaUtilizados().add(s);
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getDemandas().setAtributosDetallados(atributos);
				}

				if (etiqueta.equalsIgnoreCase("listaDemanda")) {
					NodeList listademandas = node.getChildNodes();
					Node demanda = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listademandas.getLength(); i++) {
						demanda = listademandas.item(i);
						etiqueta = demanda.getNodeName();
						if (etiqueta.equalsIgnoreCase("demanda")) {
							String nombre = cargarDemanda(corrida, demanda, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}

						}
					}
					corrida.getDemandas().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}

	}

	public String cargarDemanda(DatosCorrida corrida, Node node, boolean copiadoPortapapeles) {
		Element eElement = (Element) node;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getDemandas().getListaUtilizados().contains(nombre)){
			return null;
		}
		String barra = eElement.getElementsByTagName("barra").item(0).getTextContent();

		DatosVariableAleatoria potA = cargarVariableAleatoria(eElement.getElementsByTagName("potActiva").item(0));

		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		DatosDemandaCorrida nuevo = new DatosDemandaCorrida(nombre, barra, potA, salDet);
		corrida.getDemandas().getDemandas().put(nuevo.getNombre(), nuevo);
		return nombre;
	}

	public ArrayList<String> generarListaStringConSeparador(String textContent, String separador) {
		String[] nros = textContent.split(separador);
		ArrayList<String> resultado = new ArrayList<String>();
		if (nros.length == 1 && nros[0].equalsIgnoreCase(""))
			return resultado;

		for (int x = 0; x < nros.length; x++) {
			resultado.add(nros[x].trim());
		}
		return resultado;
	}

	// private ArrayList<Double> generarArrayLDoubleConSeparador(
	// String textContent, String separador) {
	// String[] nros = textContent.split(separador);
	// ArrayList<Double> resultado = new ArrayList<Double>();
	// for (int x = 0; x < nros.length; x++) {
	// resultado.add(Double.parseDouble(nros[x].trim()));
	// }
	// return resultado;
	// }

	private ArrayList<Integer> generarArrayLIntegerConSeparador(String textContent, String separador) {
		String[] nros = textContent.split(separador);
		ArrayList<Integer> resultado = new ArrayList<Integer>();
		for (int x = 0; x < nros.length; x++) {
			resultado.add(Integer.parseInt(nros[x].trim()));
		}
		return resultado;
	}

	private double[] generarListaDoubleConSeparador(String textContent, String separador) {

		String[] nros = textContent.split(separador);
		double[] resultado = new double[nros.length];
		for (int x = 0; x < nros.length; x++) {
			resultado[x] = Double.parseDouble(nros[x].trim());
		}
		return resultado;
	}

	private Integer[] generarListaIntegerConSeparador(String textContent, String separador) {

		String[] nros = textContent.split(separador);
		Integer[] resultado = new Integer[nros.length];
		for (int x = 0; x < nros.length; x++) {
			resultado[x] = Integer.parseInt(nros[x].trim());
		}
		return resultado;
	}

	private int[] generarListaIntConSeparador(String textContent, String separador) {

		String[] nros = textContent.split(separador);
		int[] resultado = new int[nros.length];
		if (nros[0].equals(""))
			return resultado;
		for (int x = 0; x < nros.length; x++) {

			resultado[x] = Integer.parseInt(nros[x].trim());
		}
		return resultado;
	}

	private void cargarGeneradores(DatosCorrida corrida, Node nodoPadre, DatosLineaTiempo lt) {
		NodeList generadores = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < generadores.getLength(); temp++) {
			node = generadores.item(temp);
			if (node instanceof Element) {
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("termicos"))
					cargarTermicos(corrida, node);
				if (etiqueta.equalsIgnoreCase("ciclosCombinados"))
					cargarCiclosCombinados(corrida, node);
				if (etiqueta.equalsIgnoreCase("hidraulicos"))
					cargarHidraulicos(corrida, node, lt);
				if (etiqueta.equalsIgnoreCase("eolicos"))
					cargarEolicos(corrida, node);
				if (etiqueta.equalsIgnoreCase("fotovoltaicos"))
					cargarFotovoltaicos(corrida, node);
				if (etiqueta.equalsIgnoreCase("centralesAcumulacion"))
					cargarCentralesAcumulacion(corrida, node, lt);
			}
		}

	}

	private void cargarComercioEnergia(DatosCorrida corrida, Node nodoPadre, DatosLineaTiempo lt) {
		NodeList comercioEnergia = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < comercioEnergia.getLength(); temp++) {
			node = comercioEnergia.item(temp);
			if (node instanceof Element) {
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("impoExpos"))
					cargarImpoExpos(corrida, node, lt);
			}
		}

	}

	private void cargarCentralesAcumulacion(DatosCorrida corrida, Node nodoPadre, DatosLineaTiempo lt) {
		NodeList listaHijos = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String valor;
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("compsPorDefecto")) {
					Evolucion<String> compPaso = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
									eElement.getElementsByTagName("compPaso").item(0)).getElementsByTagName("ev").item(0), "string",
							corrida.getLineaTiempo().getTiempoInicial(), lt);
					corrida.getAcumuladores().getValoresComportamiento().put("compPaso", compPaso);

				}
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados)
						corrida.getAcumuladores().getListaUtilizados().add(s);
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getAcumuladores().setAtributosDetallados(atributos);
				}
				if (etiqueta.equalsIgnoreCase("listaCentralesAcumulacion")) {
					NodeList listacum = node.getChildNodes();
					Node acum = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listacum.getLength(); i++) {
						acum = listacum.item(i);
						etiqueta = acum.getNodeName();
						if (etiqueta.equalsIgnoreCase("acumulador")) {
							String nombre = cargarAcumulador(corrida, acum, lt, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}

						}
					}
					corrida.getAcumuladores().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}

	}

	public String cargarAcumulador(DatosCorrida corrida, Node acum, DatosLineaTiempo lt, boolean copiadoPortapapeles) {
		Element eElement = (Element) acum;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getAcumuladores().getListaUtilizados().contains(nombre)){
			return null;
		}
		String propietario = eElement.getElementsByTagName("propietario").item(0).getTextContent();
		String barra = eElement.getElementsByTagName("barra").item(0).getTextContent();
		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<String> compPaso = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("compPaso").item(0)).getElementsByTagName("ev").item(0), "string",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> potMin = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMin").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> factorUso = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("factorUso").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> potMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMax").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> potAlmacMin = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("potAlmacMin").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> potAlmacMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("potAlmacMax").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> energAlmacMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("energAlmacMax").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> rendIny = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("rendIny").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());
		Evolucion<Double> rendAlmac = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("rendAlmac").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		// Element fRend = (Element)
		// eElement.getElementsByTagName("fRendimiento").item(0);
		// DatosPolinomio fRendimiento =
		// cargarPolinomio(fRend.getElementsByTagName("funcion").item(0));

		Integer cantModIni = Integer.parseInt(eElement.getElementsByTagName("cantModIni").item(0).getTextContent());
		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> energIniPaso = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("energIniPaso").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Hashtable<String, DatosVariableEstado> varsEstado = null;
		if (eElement.getElementsByTagName("variablesEstado").item(0) != null) {
			varsEstado = cargarVariablesEstado(corrida, eElement.getElementsByTagName("variablesEstado").item(0));
		}

		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		Evolucion<Boolean> hayPotOblig = (Evolucion<Boolean>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("hayPotObligatoria").item(0)).getElementsByTagName("ev").item(0), "boolean", lt.getTiempoInicial(), lt);

		Evolucion<Double> costoFallaPotOblig = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("CostoFallaPObligatoria").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		String potOblig = eElement.getElementsByTagName("PotObligatoria").item(0).getTextContent();
		double[] potObli = generarListaDoubleConSeparador(potOblig, ",");

		Evolucion<Double> costoVariable = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("costoVariable").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		DatosAcumuladorCorrida nuevo = new DatosAcumuladorCorrida(nombre, propietario, barra, cantModInst, potMin, potMax,
				potAlmacMin, potAlmacMax, energAlmacMax, rendIny, rendAlmac, cantModIni, dispMedia, tMedioArreglo,
				false, mantProgramado, costoFijo, costoVariable, varsEstado, factorUso, energIniPaso, salDet,
				hayPotOblig, costoFallaPotOblig, potObli);

		nuevo.getValoresComportamientos().put("compPaso", compPaso);
		corrida.getAcumuladores().getAcumuladores().put(nuevo.getNombre(), nuevo);
		return nombre;
	}

	private void cargarHidraulicos(DatosCorrida corrida, Node nodoPadre, DatosLineaTiempo lt) {
		NodeList listaHijos = nodoPadre.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String valor;
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados) {
						corrida.getHidraulicos().getListaUtilizados().add(s);
					}
				}
				if (etiqueta.equalsIgnoreCase("compsPorDefecto")) {
					Evolucion<String> compLago = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
									eElement.getElementsByTagName("compLago").item(0)).getElementsByTagName("ev").item(0), "string",
							corrida.getLineaTiempo().getTiempoInicial(), lt);
					Evolucion<String> compCoefEnergetico = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
									eElement.getElementsByTagName("compCoefEnergetico").item(0)).getElementsByTagName("ev").item(0), "string",
							corrida.getLineaTiempo().getTiempoInicial(), lt);
					corrida.getHidraulicos().getValoresComportamiento().put("compLago", compLago);
					corrida.getHidraulicos().getValoresComportamiento().put("compCoefEnergetico", compCoefEnergetico);
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getHidraulicos().setAtribtosDetallados(atributos);
				}
				if (etiqueta.equalsIgnoreCase("listaGHidraulico")) {
					NodeList listahidros = node.getChildNodes();
					Node hidro = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listahidros.getLength(); i++) {
						hidro = listahidros.item(i);
						etiqueta = hidro.getNodeName();
						if (etiqueta.equalsIgnoreCase("gHidraulico")) {
							String nombre = cargarHidraulico(corrida, hidro, lt, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}
						}
					}
					corrida.getHidraulicos().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}
	}

	public String cargarHidraulico(DatosCorrida corrida, Node hidro, DatosLineaTiempo lt, boolean copiadoPortapapeles) {
		Element eElement = (Element) hidro;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getHidraulicos().getListaUtilizados().contains(nombre)){
			return null;
		}
		String propietario = eElement.getElementsByTagName("propietario").item(0).getTextContent();
		String barra = eElement.getElementsByTagName("barra").item(0).getTextContent();
		String rutaPQ = "./resources";
		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> factorCompartir = new EvolucionConstante<Double>(
				Double.parseDouble(eElement.getElementsByTagName("factorCompartir").item(0).getTextContent()),
				corrida.getLineaTiempo().getSentido());

		Double espCaudalErogadoIter = new Double(
				eElement.getElementsByTagName("epsilonCaudalErogadoIteracion").item(0).getTextContent());
		ArrayList<String> gAguasArriba = generarListaStringConSeparador(
				eElement.getElementsByTagName("listaGHAguasArriba").item(0).getTextContent(), ",");
		String generadorAAbajo = eElement.getElementsByTagName("generadorAguasAbajo").item(0).getTextContent();
		Evolucion<Double> potMin = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMin").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> potMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMax").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> rendPotMin = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("rendMin").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());
		Evolucion<Double> rendPotMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("rendMax").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());

		// Element fRend = (Element)
		// eElement.getElementsByTagName("fRendimiento").item(0);
		// DatosPolinomio fRendimiento =
		// cargarPolinomio(fRend.getElementsByTagName("funcion").item(0));

		Evolucion<Double> volFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("volFijo").item(0)).getElementsByTagName("ev").item(0), "double", corrida.getLineaTiempo().getTiempoInicial(),
				corrida.getLineaTiempo());
		Evolucion<String> compLago = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("compLago").item(0)).getElementsByTagName("ev").item(0), "string",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<String> compCoefEnerg = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("compCoefEnergetico").item(0)).getElementsByTagName("ev").item(0), "string",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> qTur1Max = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("qTur1Max").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		DatosVariableAleatoria aporte = cargarVariableAleatoria(eElement.getElementsByTagName("aporte").item(0));

		Integer cantModIni = Integer.parseInt(eElement.getElementsByTagName("cantModIni").item(0).getTextContent());
		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		//
		// Element fCE =
		// (Element)eElement.getElementsByTagName("fCoefEnerg").item(0);
		// Evolucion<DatosPolinomio> fCoefEnerg = new
		// EvolucionConstante<DatosPolinomio>(cargarPolinomio(fCE.getElementsByTagName("funcion").item(0)));

		Element fCoAA = (Element) eElement.getElementsByTagName("fCotaAguasAbajo").item(0);
		Evolucion<DatosPolinomio> fCotaAA = new EvolucionConstante<DatosPolinomio>(
				cargarPolinomio(fCoAA.getElementsByTagName("funcion").item(0)), lt.getSentido());

		Double saltoMin = Double.parseDouble(eElement.getElementsByTagName("saltoMinimo").item(0).getTextContent());

		Double cotaInundacionAguasAbajo = Double
				.parseDouble(eElement.getElementsByTagName("cotaInundacionAguasAbajo").item(0).getTextContent());

		Double cotaInundacionAguasArriba = Double
				.parseDouble(eElement.getElementsByTagName("cotaInundacionAguasArriba").item(0).getTextContent());

		Element fQEM = (Element) eElement.getElementsByTagName("fQEroMin").item(0);
		Evolucion<DatosPolinomio> fQEroMin = new EvolucionConstante<DatosPolinomio>(
				cargarPolinomio(fQEM.getElementsByTagName("funcion").item(0)), lt.getSentido());

		Element fCV = (Element) eElement.getElementsByTagName("fCoVo").item(0);
		Evolucion<DatosPolinomio> fCoVo = new EvolucionConstante<DatosPolinomio>(
				cargarPolinomio(fCV.getElementsByTagName("funcion").item(0)), lt.getSentido());

		Element fVC = (Element) eElement.getElementsByTagName("fVoCo").item(0);
		Evolucion<DatosPolinomio> fVoCo = new EvolucionConstante<DatosPolinomio>(
				cargarPolinomio(fVC.getElementsByTagName("funcion").item(0)), lt.getSentido());

		Element fEv = (Element) eElement.getElementsByTagName("fEvaporacion").item(0);
		Evolucion<DatosPolinomio> fEvaporacion = new EvolucionConstante<DatosPolinomio>(
				cargarPolinomio(fEv.getElementsByTagName("funcion").item(0)), lt.getSentido());

		Element coEv = (Element) ((Element) eElement.getElementsByTagName("coefEvaporacion").item(0)).getElementsByTagName("ev").item(0);
		// Element evCoef = coEv.getElementsByTagName("ev");
		Evolucion<Double> coefEvaporacion = (Evolucion<Double>) cargarNodoEvolucion(coEv, "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Element fFilt = (Element) eElement.getElementsByTagName("fFiltracion").item(0);
		Evolucion<DatosPolinomio> fFiltracion = new EvolucionConstante<DatosPolinomio>(
				cargarPolinomio(fFilt.getElementsByTagName("funcion").item(0)), lt.getSentido());

		Element fQv = (Element) eElement.getElementsByTagName("fQVerMax").item(0);
		Evolucion<DatosPolinomio> fQVerM = new EvolucionConstante<DatosPolinomio>(
				cargarPolinomio(fQv.getElementsByTagName("funcion").item(0)), lt.getSentido());

		Hashtable<String, DatosVariableEstado> varsEstado = null;
		if (eElement.getElementsByTagName("variablesEstado").item(0) != null) {
			varsEstado = cargarVariablesEstado(corrida, eElement.getElementsByTagName("variablesEstado").item(0));
		}

		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		Evolucion<Double> costoVariable = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("costoVariable").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		Evolucion<Double> volReservaEstrategica = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("volumenReservaEstrategica").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(),
				lt);
		Evolucion<Double> valorMinReserva = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("valorMinReserva").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);
		boolean valAplicaOptim = Boolean
				.parseBoolean(eElement.getElementsByTagName("valorAplicaEnOptim").item(0).getTextContent().trim());
		boolean hayReservaEstrategica = Boolean
				.parseBoolean(eElement.getElementsByTagName("hayReservaEstrategica").item(0).getTextContent().trim());

		/**
		 * CONTROL DE COTAS NUEVO
		 */
		boolean hayControldeCotasMinimas = Boolean
				.parseBoolean(eElement.getElementsByTagName("hayControldeCotasMinimas").item(0).getTextContent().trim());

		Evolucion<Double> volumenControlMinimo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("volumenControlMinimo").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);
		Evolucion<Double> penalidadControlMinimo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("penalidadControlMinimo").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		boolean hayControldeCotasMaximas = Boolean
				.parseBoolean(eElement.getElementsByTagName("hayControldeCotasMaximas").item(0).getTextContent().trim());

		Evolucion<Double> volumenControlMaximo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("volumenControlMaximo").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);
		Evolucion<Double> penalidadControlMaximo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
				eElement.getElementsByTagName("penalidadControlMaximo").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(), lt);

		boolean vertimientoCte = Boolean
				.parseBoolean(eElement.getElementsByTagName("vertimientoConstante").item(0).getTextContent().trim());
		boolean hayVolObjVert = Boolean
				.parseBoolean(eElement.getElementsByTagName("hayVolumenObjetivoVertimiento").item(0).getTextContent().trim());
		Evolucion<Double> volObjVert = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("volumenObjetivoVertimiento").item(0)).getElementsByTagName("ev").item(0), "double", lt.getTiempoInicial(),
				lt);

		DatosHidraulicoCorrida nuevo = new DatosHidraulicoCorrida(nombre,propietario, barra, rutaPQ, cantModInst, factorCompartir,
				gAguasArriba, generadorAAbajo, potMin, potMax, rendPotMin, rendPotMax, volFijo, qTur1Max, aporte,
				cantModIni, dispMedia, tMedioArreglo, null, fCotaAA, saltoMin, cotaInundacionAguasAbajo,
				cotaInundacionAguasArriba, fQEroMin, fCoVo, fVoCo, fEvaporacion, coefEvaporacion, fFiltracion, fQVerM,
				varsEstado, espCaudalErogadoIter, salDet, mantProgramado, costoFijo, costoVariable,
				volReservaEstrategica, valorMinReserva, valAplicaOptim, hayReservaEstrategica, vertimientoCte,
				hayVolObjVert, volObjVert, hayControldeCotasMinimas, volumenControlMinimo, penalidadControlMinimo,
				hayControldeCotasMaximas, volumenControlMaximo, penalidadControlMaximo);
		
		nuevo.getValoresComportamientos().put("compLago", compLago);
		nuevo.getValoresComportamientos().put("compCoefEnergetico", compCoefEnerg);
		corrida.getHidraulicos().getHidraulicos().put(nuevo.getNombre(), nuevo);
		return nombre;
	}

	private Hashtable<String, DatosVariableEstado> cargarVariablesEstado(DatosCorrida corrida, Node arriba) {
		NodeList listaHijos = arriba.getChildNodes();
		Node node = null;
		DatosVariableEstado aux = null;
		Hashtable<String, DatosVariableEstado> resultado = new Hashtable<String, DatosVariableEstado>();
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("variableEstado")) {
					aux = cargarVariableEstado(corrida, node);
					resultado.put(aux.getNombre(), aux);
				}

			}
		}
		return resultado;
	}

	private DatosVariableEstado cargarVariableEstado(DatosCorrida corrida, Node node) {
		Element var = ((Element) node);

		DatosVariableEstado resultado = new DatosVariableEstado();
		resultado.setNombre(var.getElementsByTagName("nombre").item(0).getTextContent());

		Node estIni = var.getElementsByTagName("estadoInicial").item(0);
		resultado.setEstadoInicial( Double.parseDouble(estIni.getTextContent()));
		resultado.setEstadoInicialUnidad( ((Element) estIni).getAttributeNode("unidad").getTextContent());

		resultado.setHayValorInferior(var.getElementsByTagName("valorRecursoInferior").item(0) != null);
		resultado.setHayValorSuperior(var.getElementsByTagName("valorRecursoSuperior").item(0) != null);

		if (resultado.isHayValorInferior()) {
			Evolucion<Double> valRecInf = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
							var.getElementsByTagName("valorRecursoInferior").item(0)).getElementsByTagName("ev").item(0), "double",
					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
			resultado.setValorRecursoInferior(valRecInf);
		}

		if (resultado.isHayValorSuperior()) {
			Evolucion<Double> valRecSup = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
							var.getElementsByTagName("valorRecursoSuperior").item(0)).getElementsByTagName("ev").item(0), "double",
					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
			resultado.setValorRecursoSuperior(valRecSup);
		}

		resultado.setDiscreta(Boolean.parseBoolean(var.getElementsByTagName("discreta").item(0).getTextContent()));
		resultado.setOrdinal(Boolean.parseBoolean(var.getElementsByTagName("ordinal").item(0).getTextContent()));
		resultado.setDiscretaIncremental(
				Boolean.parseBoolean(var.getElementsByTagName("discretaIncremental").item(0).getTextContent()));
		// resultado.setValorRecursoInferior(var.getElementsByTagName("nombre").item(0).getTextContent());
		resultado.setEstadoInicial(
				Double.parseDouble(var.getElementsByTagName("estadoInicial").item(0).getTextContent()));

		if (var.getElementsByTagName("discretizacion").item(0) != null) {
			resultado.setDiscretizacion(new EvolucionConstante<DatosDiscretizacion>(
					cargarDiscretizacion(corrida, var.getElementsByTagName("discretizacion").item(0)),
					corrida.getLineaTiempo().getSentido()));
		}
		return resultado;
	}

	private Hashtable<String, DatosVariableControlDE> cargarVariablesControlDE(DatosCorrida corrida, Node arriba) {
		NodeList listaHijos = arriba.getChildNodes();
		Node node = null;
		DatosVariableControlDE aux = null;
		Hashtable<String, DatosVariableControlDE> resultado = new Hashtable<String, DatosVariableControlDE>();
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("variableControlDE")) {
					aux = cargarVariableControlDE(corrida, node);
					resultado.put(aux.getNombre(), aux);
				}

			}
		}
		return resultado;
	}

	private DatosVariableControlDE cargarVariableControlDE(DatosCorrida corrida, Node node) {
		Element var = ((Element) node);

		DatosVariableControlDE resultado = new DatosVariableControlDE();

		resultado.setNombre(var.getElementsByTagName("nombre").item(0).getTextContent());

		Integer periodo = Integer.parseInt(var.getElementsByTagName("periodo").item(0).getTextContent());

		resultado.setPeriodo(periodo);

//		String costoDeCont = var.getElementsByTagName("costoDeControl").item(0).getTextContent();
//		double[] costoDeControl = generarListaDoubleConSeparador(costoDeCont, ",");
//		resultado.setCostoDeControl(costoDeControl);
		Evolucion<Double[]> costoDeControl = (Evolucion<Double[]>) cargarNodoEvolucion((Element) ((Element)
						var.getElementsByTagName("costoDeControl").item(0)).getElementsByTagName("ev").item(0), "arraydouble",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		resultado.setCostoDeControl(costoDeControl);

		// resultado.setDiscretizacion(new EvolucionConstante<DatosDiscretizacion>(
//				cargarDiscretizacion(corrida, var.getElementsByTagName("discretizacion").item(0)),
//				corrida.getLineaTiempo().getSentido()));

//			Evolucion<Integer> periodo =
//					var.getElementsByTagName("valorRecursoInferior").item(0), "double",
//					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
//			resultado.setValorRecursoInferior(valRecInf);
//		}
//
//		if (resultado.isHayValorSuperior()) {
//			Evolucion<Double> valRecSup = (Evolucion<Double>) cargarEvolucion(
//					var.getElementsByTagName("valorRecursoSuperior").item(0), "double",
//					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
//			resultado.setValorRecursoSuperior(valRecSup);
//		}
//
//		resultado.setDiscreta(Boolean.parseBoolean(var.getElementsByTagName("discreta").item(0).getTextContent()));
//		resultado.setOrdinal(Boolean.parseBoolean(var.getElementsByTagName("ordinal").item(0).getTextContent()));
//		resultado.setDiscretaIncremental(
//				Boolean.parseBoolean(var.getElementsByTagName("discretaIncremental").item(0).getTextContent()));
//		// resultado.setValorRecursoInferior(var.getElementsByTagName("nombre").item(0).getTextContent());
//		resultado.setEstadoInicial(
//				Double.parseDouble(var.getElementsByTagName("estadoInicial").item(0).getTextContent()));
//
//		resultado.setDiscretizacion(new EvolucionConstante<DatosDiscretizacion>(
//				cargarDiscretizacion(corrida, var.getElementsByTagName("discretizacion").item(0)),
//				corrida.getLineaTiempo().getSentido()));
//
		return resultado;

	}

	private DatosDiscretizacion cargarDiscretizacion(DatosCorrida corrida, Node d) {
		Element dis = (Element) d;
		String tipo = dis.getAttribute("tipo");
		DatosDiscretizacion dd = new DatosDiscretizacion();
		if (tipo.equalsIgnoreCase("equiespaciada")) {
			Double minimo = Double.parseDouble(dis.getElementsByTagName("minimo").item(0).getTextContent());
			Double maximo = Double.parseDouble(dis.getElementsByTagName("maximo").item(0).getTextContent());
			int cantPuntos = Integer.parseInt(dis.getElementsByTagName("cantidadPuntos").item(0).getTextContent());
			dd.setMinimo(minimo);
			dd.setMaximo(maximo);
			double[] part = new double[cantPuntos];
			double salto = (maximo - minimo) / (cantPuntos - 1);
			for (int i = 0; i < cantPuntos; i++) {
				part[i] = minimo + i * salto;
			}
			dd.setParticion(part);
//		}else{
//			Element caudalMinE = (Element) dis.getElementsByTagName("puntos").item(0);
//			Evolucion<Double[]> puntos= (Evolucion<Double[]>) cargarEvolucion(
//					dis.getElementsByTagName("puntos").item(0), "arraydouble", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
//			dd.setParticion(puntos);
		}
		return dd;
	}

	private DatosPolinomio cargarPolinomio(Node func) {
		Element efunc = ((Element) func);
		String tipo = efunc.getAttribute("tipo");
		DatosPolinomio resultado = new DatosPolinomio();
		resultado.setTipo(tipo);

		if (tipo.equalsIgnoreCase("poliConCotas")) {
			resultado.setXmax(Double.parseDouble(efunc.getElementsByTagName("xmax").item(0).getTextContent()));
			resultado.setXmin(Double.parseDouble(efunc.getElementsByTagName("xmin").item(0).getTextContent()));
			resultado.setValmax(Double.parseDouble(efunc.getElementsByTagName("valmax").item(0).getTextContent()));
			resultado.setValmin(Double.parseDouble(efunc.getElementsByTagName("valmin").item(0).getTextContent()));
			resultado.setCoefs(
					generarListaDoubleConSeparador(efunc.getElementsByTagName("coefs").item(0).getTextContent(), ","));
		} else if (tipo.equalsIgnoreCase("poli")) {
			String separar = func.getTextContent();
			resultado.setCoefs(generarListaDoubleConSeparador(separar, ","));
		} else if (tipo.equalsIgnoreCase("poliMulti")) {
			NodeList listaHijos = efunc.getChildNodes();
			Node node = null;
			DatosPolinomio aux = null;

			for (int temp = 0; temp < listaHijos.getLength(); temp++) {
				node = listaHijos.item(temp);
				if (node instanceof Element) {
					String etiqueta = node.getNodeName();
					if (etiqueta.equalsIgnoreCase("funcion")) {
						aux = cargarPolinomio(node);
						String nomvar = ((Element) node).getAttribute("var");
						resultado.getPols().put(nomvar, aux);
					}

				}
			}
		} else if (tipo.equalsIgnoreCase("porRangos")) {
			NodeList listaHijos = efunc.getChildNodes();
			Node node = null;
			ArrayList<DatosPolinomio> polsrangos = new ArrayList<DatosPolinomio>();
			DatosPolinomio fueraRango = null;
			ArrayList<Pair<Double, Double>> rangos = new ArrayList<Pair<Double, Double>>();

			for (int temp = 0; temp < listaHijos.getLength(); temp++) {
				node = listaHijos.item(temp);
				if (node instanceof Element) {
					String etiqueta = node.getNodeName();
					if (etiqueta.equalsIgnoreCase("fueraRango")) {
						NodeList listaHijos2 = node.getChildNodes();
						for (int tempdos = 0; tempdos < listaHijos2.getLength(); tempdos++) {
							Node node2 = listaHijos2.item(tempdos);
							if (node2 instanceof Element) {
								String et = node2.getNodeName();
								if (et.equalsIgnoreCase("funcion")) {
									fueraRango = cargarPolinomio(node2);
								}

							}
						}
					} else if (etiqueta.equalsIgnoreCase("rangos")) {
						rangos = generarListaPares(node);
					} else if (etiqueta.equalsIgnoreCase("funcion")) {
						polsrangos.add(cargarPolinomio(node));
					}

				}
			}
			resultado.setFueraRango(fueraRango);
			resultado.setPolsrangos(polsrangos);
			resultado.setRangos(rangos);

		}
		if (tipo.equalsIgnoreCase("porSegmentos")) {
			NodeList listaHijos = efunc.getChildNodes();
			Node node = null;
			ArrayList<Pair<Double, Double>> segmentos = new ArrayList<Pair<Double, Double>>();

			for (int temp = 0; temp < listaHijos.getLength(); temp++) {
				node = listaHijos.item(temp);
				if (node instanceof Element) {
					String etiqueta = node.getNodeName();
					if (etiqueta.equalsIgnoreCase("segmentos")) {
						segmentos = generarListaPares(node);
					}

				}
			}
			resultado.setSegmentos(segmentos);
		}

		return resultado;
	}

	private DatosVariableAleatoria cargarVariableAleatoria(Node nomvar) {
		Element eElement = (Element) nomvar;
		Element var = (Element) eElement.getElementsByTagName("variableAleat").item(0);
		NodeList listaHijos = var.getChildNodes();
		Node node = null;
		String valor;
		String procOptimizacion = null;
		String procSimulacion = null;
		String nombre = null;

		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String etiqueta = node.getNodeName();
				valor = node.getTextContent();
				if (etiqueta.equalsIgnoreCase("procOptimizacion")) {
					procOptimizacion = valor;
				}
				if (etiqueta.equalsIgnoreCase("procSimulacion")) {
					procSimulacion = valor;
				}
				if (etiqueta.equalsIgnoreCase("nombre")) {
					nombre = valor;
				}
			}
		}

		if (procOptimizacion == null || procSimulacion == null || nombre == null) {
			System.out.println("La variable aleatoria de un nodo " + nomvar.getNodeName() + " no estÃ¡ completa");
			if (CorridaHandler.getInstance().isParalelo()) {
				//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
			}
			System.exit(1);
		}

		DatosVariableAleatoria resultado = new DatosVariableAleatoria(procOptimizacion, procSimulacion, nombre);
		return resultado;
	}

	private ArrayList<Pair<Double, Double>> generarListaPares(Node lista) {
		Element eElement = (Element) lista;
		String[] pares = eElement.getTextContent().split(",");

		ArrayList<Pair<Double, Double>> resultado = new ArrayList<Pair<Double, Double>>();

		for (int j = 0; j < pares.length; j++) {
			resultado.add(generarPar(pares[j]));
		}

		return resultado;
	}

	private void cargarEolicos(DatosCorrida corrida, Node eolicos) {
		NodeList listaHijos = eolicos.getChildNodes();
		Node eolico = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			eolico = listaHijos.item(temp);
			if (eolico instanceof Element) {
				String valor;
				Element eElement = (Element) eolico;
				String etiqueta = eolico.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados) {
						corrida.getEolicos().getListaUtilizados().add(s);
					}
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getEolicos().setAtributosDetallados(atributos);
				}
				if (etiqueta.equalsIgnoreCase("listaGEolico")) {
					NodeList listaeolos = eolico.getChildNodes();
					Node eolo = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listaeolos.getLength(); i++) {
						eolo = listaeolos.item(i);
						etiqueta = eolo.getNodeName();
						if (etiqueta.equalsIgnoreCase("gEolico")) {
							String nombre = cargarEolico(corrida, eolo, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}
						}
					}
					corrida.getEolicos().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}

	}

	public String cargarEolico(DatosCorrida corrida, Node eolo, boolean copiadoPortapapeles) {
		Element eElement = (Element) eolo;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getEolicos().getListaUtilizados().contains(nombre)){
			return null;
		}
		String propietario = eElement.getElementsByTagName("propietario").item(0).getTextContent();
		String barra = eElement.getElementsByTagName("barra").item(0).getTextContent();
		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> potMin = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMin").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> potMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element) eElement.getElementsByTagName("potMax").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		DatosVariableAleatoria factor = cargarVariableAleatoria(eElement.getElementsByTagName("factor").item(0));

		Integer cantModIni = Integer.parseInt(eElement.getElementsByTagName("cantModIni").item(0).getTextContent());
		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		Evolucion<Double> costoVariable = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoVariable").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		DatosEolicoCorrida nuevo = new DatosEolicoCorrida(nombre, propietario, barra, cantModInst, potMin, potMax, factor,
				cantModIni, dispMedia, tMedioArreglo, salDet, mantProgramado, costoFijo, costoVariable);
		corrida.getEolicos().getEolicos().put(nuevo.getNombre(), nuevo);
		return nombre;
	}

	private void cargarFotovoltaicos(DatosCorrida corrida, Node fotovoltaicos) {
		NodeList listaHijos = fotovoltaicos.getChildNodes();
		Node fotovolt = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			fotovolt = listaHijos.item(temp);
			if (fotovolt instanceof Element) {
				String valor;
				Element eElement = (Element) fotovolt;
				String etiqueta = fotovolt.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados) {
						corrida.getFotovoltaicos().getListaUtilizados().add(s);
					}
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getFotovoltaicos().setAtributosDetallados(atributos);
				}
				if (etiqueta.equalsIgnoreCase("listaGFotovoltaico")) {
					NodeList listaeolos = fotovolt.getChildNodes();
					Node fotovol = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listaeolos.getLength(); i++) {
						fotovol = listaeolos.item(i);
						etiqueta = fotovol.getNodeName();
						if (etiqueta.equalsIgnoreCase("gFotovoltaico")) {
							String nombre = cargarFotovoltaico(corrida, fotovol, false);
							if(nombre != null) {
								ordenCargaXML.add(nombre);
							}
						}
					}
					corrida.getFotovoltaicos().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}

	}

	public String cargarFotovoltaico(DatosCorrida corrida, Node fotov, boolean copiadoPortapapeles) {
		Element eElement = (Element) fotov;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getFotovoltaicos().getListaUtilizados().contains(nombre)){
			return null;
		}
		String propietario = eElement.getElementsByTagName("propietario").item(0).getTextContent();
		String barra = eElement.getElementsByTagName("barra").item(0).getTextContent();
		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> potMin = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMin").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> potMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMax").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		DatosVariableAleatoria factor = cargarVariableAleatoria(eElement.getElementsByTagName("factor").item(0));

		Integer cantModIni = Integer.parseInt(eElement.getElementsByTagName("cantModIni").item(0).getTextContent());
		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		Evolucion<Double> costoVariable = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoVariable").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		DatosFotovoltaicoCorrida nuevo = new DatosFotovoltaicoCorrida(nombre, propietario, barra,cantModInst, potMin, potMax,
				factor, cantModIni, dispMedia, tMedioArreglo, salDet, mantProgramado, costoFijo, costoVariable);
		corrida.getFotovoltaicos().getFotovoltaicos().put(nuevo.getNombre(), nuevo);
		return nombre;
	}

	private void cargarImpoExpos(DatosCorrida corrida, Node impoExpos, DatosLineaTiempo lt) {
		NodeList listaHijos = impoExpos.getChildNodes();
		Node impoExpo = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			impoExpo = listaHijos.item(temp);
			if (impoExpo instanceof Element) {
				String valor;
				Element eElement = (Element) impoExpo;
				String etiqueta = impoExpo.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados) {
						corrida.getImpoExpos().getListaUtilizados().add(s);
					}
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getImpoExpos().setAtributosDetallados(atributos);
				}
				if (etiqueta.equalsIgnoreCase("listaImpoExpos")) {
					NodeList listaIE = impoExpo.getChildNodes();
					Node ie = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listaIE.getLength(); i++) {
						ie = listaIE.item(i);
						etiqueta = ie.getNodeName();
						if (etiqueta.equalsIgnoreCase("impoExpo")) {
							String nombre = cargarImpoExpo(corrida, ie, lt, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}

						}
					}
					corrida.getImpoExpos().setOrdenCargaXML(ordenCargaXML);
				}

			}
		}

	}

	public String cargarImpoExpo(DatosCorrida corrida, Node impoExpo, DatosLineaTiempo lt, boolean copiadoPortapapeles) {
		Element eElement = (Element) impoExpo;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getImpoExpos().getListaUtilizados().contains(nombre)){
			return null;
		}
		String propietario = eElement.getElementsByTagName("propietario").item(0).getTextContent();
		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		String barra = eElement.getElementsByTagName("barra").item(0).getTextContent();
		String pais = eElement.getElementsByTagName("pais").item(0).getTextContent();
		String tipoImpoExpo = eElement.getElementsByTagName("tipoImpoExpo").item(0).getTextContent();
		String opCompraVenta = eElement.getElementsByTagName("opCompraVenta").item(0).getTextContent();
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		DatosVariableAleatoria cmg = null;
		if(tipoImpoExpo.equals(Constantes.IEALEATFORMUL)){
			cmg = cargarVariableAleatoria(eElement.getElementsByTagName("cmg").item(0));
		}

		// OJOJOJOJJOJO
//		Evolucion<Double> factorEscalamiento = null;
		Evolucion<Double> factorEscalamiento = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("factorEscalamiento").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		DatosVariableAleatoria uniforme = cargarVariableAleatoria(eElement.getElementsByTagName("uniforme").item(0));
		Integer cantBloques = Integer.parseInt(eElement.getElementsByTagName("cantBloques").item(0).getTextContent());
		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());


		boolean hayMinTec= Boolean
				.parseBoolean(eElement.getElementsByTagName("hayMinimoTecnico").item(0).getTextContent());
		Evolucion<Double> minTec = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("minimoTecnico").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		DatosImpoExpoCorrida datImpoExpo = new DatosImpoExpoCorrida(nombre, propietario,cantModInst, barra, pais, tipoImpoExpo, opCompraVenta,
				costoFijo, cantBloques, salDet, hayMinTec, minTec, cmg, factorEscalamiento, uniforme);
		corrida.getImpoExpos().getImpoExpos().put(datImpoExpo.getNombre(), datImpoExpo);

		Element listaBloq = (Element) eElement.getElementsByTagName("listaBloques").item(0);
		NodeList Bloques = listaBloq.getChildNodes();
		Node bloque = null;
		String etiqueta;
		for (int i = 0; i < Bloques.getLength(); i++) {
			bloque = Bloques.item(i);
			etiqueta = bloque.getNodeName();
			if (etiqueta.equalsIgnoreCase("bloque")) {
				cargarBloqueIE(corrida, datImpoExpo, impoExpo, lt);
			}
		}
		
		return nombre;
	}

	private void cargarBloqueIE(DatosCorrida corrida, DatosImpoExpoCorrida datImpoExpo, Node bloque,
								DatosLineaTiempo lt) {
		Element eElement = (Element) bloque;

		DatosVariableAleatoria pre = null;
		DatosVariableAleatoria pot = null;

		Evolucion<DatosPolinomio> poliPRE = null;
		Evolucion<DatosPolinomio> poliPOT = null;
		Evolucion<DatosPolinomio> poliDISP = null;

		Evolucion<ArrayList<Double>> potEv = null;
		Evolucion<ArrayList<Double>> preEv = null;
		Evolucion<ArrayList<Double>> dispEv = null;

		if(datImpoExpo.getTipoImpoExpo().equals(Constantes.IEALEATPRPOT)) {
			pre = cargarVariableAleatoria(eElement.getElementsByTagName("precio").item(0));
			pot = cargarVariableAleatoria(eElement.getElementsByTagName("potencia").item(0));

		} else if(datImpoExpo.getTipoImpoExpo().equals(Constantes.IEALEATFORMUL)) {

			Element poliPre = (Element) eElement.getElementsByTagName("fPre").item(0);
			poliPRE = new EvolucionConstante<DatosPolinomio>(
					cargarPolinomio(poliPre.getElementsByTagName("funcion").item(0)), lt.getSentido());

			Element poliPot = (Element) eElement.getElementsByTagName("fPot").item(0);
			poliPOT = new EvolucionConstante<DatosPolinomio>(
					cargarPolinomio(poliPot.getElementsByTagName("funcion").item(0)), lt.getSentido());

			Element poliDisp = (Element) eElement.getElementsByTagName("fDisp").item(0);
			poliDISP = new EvolucionConstante<DatosPolinomio>(
					cargarPolinomio(poliDisp.getElementsByTagName("funcion").item(0)), lt.getSentido());
		} else  {


			Element potEvol = (Element)( (Element) eElement.getElementsByTagName("potEvol").item(0)).getElementsByTagName("ev").item(0);
			potEv = (Evolucion<ArrayList<Double>>) cargarNodoEvolucion( potEvol, "listadouble",
					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
			Element preEvol = (Element)((Element) eElement.getElementsByTagName("preEvol").item(0)).getElementsByTagName("ev").item(0);
			preEv = (Evolucion<ArrayList<Double>>) cargarNodoEvolucion(preEvol, "listadouble",
					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
			Element dispEvol = (Element)((Element) eElement.getElementsByTagName("dispEvol").item(0)).getElementsByTagName("ev").item(0);
			dispEv = (Evolucion<ArrayList<Double>>) cargarNodoEvolucion(dispEvol, "listadouble",
					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		}

		// Cargar los datos del bloque al datatype impoExpo
		datImpoExpo.cargarBloque(pre, pot, poliPRE, poliPOT, poliDISP, potEv, preEv, dispEv);

	}

	private void cargarTermicos(DatosCorrida corrida, Node nodoArriba) {
		NodeList listaHijos = nodoArriba.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String valor;
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados) {
						corrida.getTermicos().getListaUtilizados().add(s);
					}
				}
				if (etiqueta.equalsIgnoreCase("compsPorDefecto")) {

					Evolucion<String> cargarEvolucion = (Evolucion<String>) cargarNodoEvolucion(
							(Element) ((Element)
									eElement.getElementsByTagName("compMinimosTecnicos").item(0)).getElementsByTagName("ev").item(0), "string",
							corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

					Evolucion<String> compMinTec = cargarEvolucion;
					corrida.getTermicos().getValoresComportamiento().put("compMinimosTecnicos", compMinTec);
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getTermicos().setAtribtosDetallados(atributos);
				}
				if (etiqueta.equalsIgnoreCase("listaGTermico")) {
					NodeList listatermos = node.getChildNodes();
					Node termo = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listatermos.getLength(); i++) {



						termo = listatermos.item(i);
						etiqueta = termo.getNodeName();
						if (etiqueta.equalsIgnoreCase("gTermico")) {
							DatosTermicoCorrida nvo = cargarTermico(corrida, termo, true, false);
							if(nvo != null){
								ordenCargaXML.add(nvo.getNombre());
							}
						}
					}
					corrida.getTermicos().setOrdenCargaXML(ordenCargaXML);
				}
			}
		}
	}

	private void cargarCiclosCombinados(DatosCorrida corrida, Node nodoArriba) {
		NodeList listaHijos = nodoArriba.getChildNodes();
		Node node = null;
		for (int temp = 0; temp < listaHijos.getLength(); temp++) {
			node = listaHijos.item(temp);
			if (node instanceof Element) {
				String valor;
				Element eElement = (Element) node;
				String etiqueta = node.getNodeName();
				if (etiqueta.equalsIgnoreCase("compsPorDefecto")) {
					Evolucion<String> cargarEvolucion = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
									eElement.getElementsByTagName("compCC").item(0)).getElementsByTagName("ev").item(0), "string",
							corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
					Evolucion<String> compCComb = cargarEvolucion;
					corrida.getCcombinados().getValoresComportamiento().put(Constantes.COMPCC, compCComb);
				}
				if (etiqueta.equalsIgnoreCase("listaUtilizados")) {
					valor = eElement.getTextContent();
					ArrayList<String> utilizados = generarListaStringConSeparador(valor, ",");
					for (String s : utilizados){
						corrida.getCcombinados().getListaUtilizados().add(s);
					}
				}
				if (etiqueta.equalsIgnoreCase("listaAtributos")) {
					String atri = eElement.getTextContent();
					ArrayList<String> atributos = generarListaStringConSeparador(atri, ",");
					corrida.getCcombinados().setAtribtosDetallados(atributos);
				}
				if (etiqueta.equalsIgnoreCase("listaCCombinado")) {
					NodeList listaCiclosComb = node.getChildNodes();
					Node ccomb = null;
					ArrayList<String> ordenCargaXML = new ArrayList<>();
					for (int i = 0; i < listaCiclosComb.getLength(); i++) {
						ccomb = listaCiclosComb.item(i);
						etiqueta = ccomb.getNodeName();
						if (etiqueta.equalsIgnoreCase("cicloCombinado")) {
							String nombre = cargarCicloCombinado(corrida, ccomb, false);
							if(nombre != null){
								ordenCargaXML.add(nombre);
							}
						}
					}
					corrida.getCcombinados().setOrdenCargaXML(ordenCargaXML);
				}
			}
		}
	}
	
	private DatosCicloCombParte cargarCicloCombinadoParte(DatosCorrida corrida, Node ccparte, ArrayList<String> listaCombustibles) {

		Element eElement = (Element) ccparte;

		Integer cantModIni = Integer.parseInt(eElement.getElementsByTagName("cantModIni").item(0).getTextContent());

		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
						eElement.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> potMax = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("potMax").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> potMin = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("potMin").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Hashtable<String,Evolucion<Double>> rendimientosMin = new Hashtable<>();
		if(eElement.getElementsByTagName("rendPotMin").item(0) != null ){
			NodeList listaRends = ((Element) eElement.getElementsByTagName("rendPotMin").item(0)).getElementsByTagName("ev");
			for (int i = 0; i < listaRends.getLength(); i++) {
				rendimientosMin.put(listaCombustibles.get(i),(Evolucion<Double>) cargarNodoEvolucion(
						((Element) listaRends.item(i)), "double",
						corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo()));
			}
		}

		Hashtable<String,Evolucion<Double>> rendimientosMax = new Hashtable<>();
		if(eElement.getElementsByTagName("rendPotMax").item(0) != null ) {
			NodeList listaRends = ((Element) eElement.getElementsByTagName("rendPotMax").item(0)).getElementsByTagName("ev");
			for (int i = 0; i < listaRends.getLength(); i++) {
				rendimientosMax.put(listaCombustibles.get(i),(Evolucion<Double>) cargarNodoEvolucion(
						((Element) listaRends.item(i)), "double",
						corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo()));
			}
		}

		Evolucion<Double> costoVariable = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("costoVariable").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		DatosCicloCombParte nuevo = new DatosCicloCombParte(cantModInst, potMin, potMax, rendimientosMax, rendimientosMin, 
				cantModIni, dispMedia, tMedioArreglo, mantProgramado, costoFijo, costoVariable);

		return nuevo;
		
	}

	
	public String cargarCicloCombinado(DatosCorrida corrida, Node ccomb, boolean copiadoPortapapeles) {
		Element eElement = (Element) ccomb;

		Evolucion<String> compCC = (Evolucion<String>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("compCC").item(0)).getElementsByTagName("ev").item(0), "string",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		if(!copiadoPortapapeles && !corrida.getCcombinados().getListaUtilizados().contains(nombre)){
			return null;
		}
		String propietario = eElement.getElementsByTagName("propietario").item(0).getTextContent();
		String barra = eElement.getElementsByTagName("barra").item(0).getTextContent();

		Evolucion<Double> potMax1CV = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("potMax1CV").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());		
		
		Evolucion<Double> costoArranque1TGCicloAbierto = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("costoArranque1TGCicloAbierto").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> costoArranque1TGCicloCombinado = (Evolucion<Double>) cargarNodoEvolucion((Element)((Element)
				eElement.getElementsByTagName("costoArranque1TGCicloCombinado").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		
		ArrayList<String> listaCombustibles = null;
		if(eElement.getElementsByTagName("listaCombustibles").item(0) != null ){
			listaCombustibles = generarListaStringConSeparador( eElement.getElementsByTagName("listaCombustibles").item(0).getTextContent(), ",");
		}

		ArrayList<String> listaBarrasComb = null;
		if(eElement.getElementsByTagName("listaBarrasComb").item(0) != null ){
			listaBarrasComb = generarListaStringConSeparador( eElement.getElementsByTagName("listaBarrasComb").item(0).getTextContent(), ",");
		}

		
		Hashtable<String, String> combustiblesBarras = new Hashtable<String, String>();
		for (int i = 0; i < listaCombustibles.size(); i++)
			combustiblesBarras.put(listaCombustibles.get(i), listaBarrasComb.get(i));
		
		boolean salDet = Boolean.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());

		Node posArr = eElement.getElementsByTagName("posiblesArranques").item(0);
		Element elposArr = (Element) posArr;
		ArrayList<Pair<Double, Double>> posiblesArranques = generarListaPares(elposArr);


		Node potRamp = eElement.getElementsByTagName("potRampa1CC").item(0);
		ArrayList<Double>  potRampa1CC = generarListaDoubles(potRamp.getTextContent(),",");


		Node posPar = eElement.getElementsByTagName("posiblesParadas").item(0);
		ArrayList<Double>  posiblesParadas = generarListaDoubles(posPar.getTextContent(),",");

		
		Node tgs = eElement.getElementsByTagName("turbinasAGas").item(0);
		Node tvs = eElement.getElementsByTagName("ciclosVapor").item(0);
		
		DatosCicloCombParte dptg = cargarCicloCombinadoParte(corrida, tgs,listaCombustibles);
		DatosTermicoCorrida dtg = new DatosTermicoCorrida("TurbinasAGas-" + nombre, null,dptg);
		
		DatosCicloCombParte dpcc = cargarCicloCombinadoParte(corrida, tvs,listaCombustibles);		
		DatosTermicoCorrida dcc = new DatosTermicoCorrida("CiclosDeVapor-" + nombre,null, dpcc);
		

		DatosCicloCombinadoCorrida ccombinado = new DatosCicloCombinadoCorrida(dtg, dcc, potMax1CV);
		ccombinado.setNombre(nombre);
		ccombinado.setPropietario(propietario);
		ccombinado.setBarra(barra);
		ccombinado.setPotMax1CV(potMax1CV);
		ccombinado.setListaCombustibles(listaCombustibles);
		ccombinado.setBarrasCombustible(combustiblesBarras);
		ccombinado.setCostoArranque1TGCicloAbierto(costoArranque1TGCicloAbierto);
		ccombinado.setCostoArranque1TGCicloCombinado(costoArranque1TGCicloCombinado);
		ccombinado.setSalDetallada(salDet);
		ccombinado.getValoresComportamientos().put("compCC", compCC);

		corrida.getCcombinados().getCcombinados().put(nombre, ccombinado);

		ccombinado.setPosiblesArranques(posiblesArranques);
		ccombinado.setPotRampaArranque(potRampa1CC);
		ccombinado.setPosiblesParadas(posiblesParadas);

		return nombre;

	}	/********
	 *
	 * TODO: TODO:REVISAR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !!!!!!!!!! TODO: TODO:
	 *
	 *
	 * @param nodoEv
	 * @param tipoDato
	 * @param tiempoInicial
	 * @return
	 */

	//Explicacion del cambio a cargarNodoEvolucion: en lugar de usar el nodo padre y pedir el elemento .(0) se castea a Element y se pide el tag que tiene "ev"
	//Esto permite usar los xml a los que se les aplico "prettyPrint", ya que los /n o /t no complican la obtencion del tag correcto
	@Deprecated
	private Evolucion<?> cargarEvolucion(Node nodoEv, String tipoDato, String tiempoInicial, DatosLineaTiempo lt) {

		//Element eElement = (Element) nodoEv.getChildNodes().item(0);
		Element nodoElement = (Element) nodoEv;

		Node ev = nodoElement.getElementsByTagName("ev").item(0);

		Element eElement = (Element) ev;

		//Element eElement = (Element) ((Element) nodoEv).getElementsByTagName("ev");

		String valor = eElement.getTextContent();
		Evolucion<?> retorno = null;
		if (eElement.getAttribute("tipo").equalsIgnoreCase("const")) {
			if (tipoDato.equalsIgnoreCase("string")) {
				retorno = new EvolucionConstante<String>(valor, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				retorno = new EvolucionConstante<Boolean>(Boolean.parseBoolean(valor), lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				retorno = new EvolucionConstante<Double>(Double.parseDouble(valor), lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("int")) {
				retorno = new EvolucionConstante<Integer>(Integer.parseInt(valor), lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				retorno = new EvolucionConstante<ArrayList<Pair<Double, Double>>>(generarListaParesDouble(valor),
						lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				retorno = new EvolucionConstante<ArrayList<Double>>(generarListaDoubles(valor, ","), lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("arraydouble")) {
				retorno = new EvolucionConstante<>(generarArrayDouble(valor, ","), lt.getSentido());
			}

		} else if (eElement.getAttribute("tipo").equalsIgnoreCase("porInstantes")) {
			DateFormat df = null;
			df = new SimpleDateFormat("dd MM yyyy"); // Se parsea un string con este formato a GregorianCalendar

			GregorianCalendar fechaInicial = Utilitarios.stringToGregorianCalendar(tiempoInicial, "dd MM yyyy");

			GregorianCalendar fechaReferencia = new GregorianCalendar();

			ArrayList<Pair<String, String>> listaPorInstantes = generarListaPorInstantes(valor, ",");

			ArrayList<Pair<Long, String>> listaInstantesEnteros = new ArrayList<Pair<Long, String>>();

			for (int i = 0; i < listaPorInstantes.size(); i++) {
				DateFormat dfi = null;
				if (listaPorInstantes.get(i).first.length() <= " dd MM yyyy ".length()) {
					dfi = df;
				} else {
					dfi = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
				}
				try {
					Date d = dfi.parse(listaPorInstantes.get(i).first);
					fechaReferencia.setTime(d);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				Long instante = restarFechas(fechaReferencia, fechaInicial);
				listaInstantesEnteros.add(new Pair<Long, String>(instante, listaPorInstantes.get(i).second));
			}

			if (tipoDato.equalsIgnoreCase("string")) {
				Hashtable<Long, String> vals = new Hashtable<Long, String>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, p.second);
				}
				retorno = new EvolucionPorInstantes<String>(vals, "", lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				Hashtable<Long, Double> vals = new Hashtable<Long, Double>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Double.parseDouble(p.second));
				}
				retorno = new EvolucionPorInstantes<Double>(vals, 0.0, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("int")) {
				Hashtable<Long, Integer> vals = new Hashtable<Long, Integer>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Integer.parseInt(p.second));
				}
				retorno = new EvolucionPorInstantes<Integer>(vals, 0, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				Hashtable<Long, Boolean> vals = new Hashtable<Long, Boolean>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Boolean.parseBoolean(p.second));
				}
				retorno = new EvolucionPorInstantes<Boolean>(vals, false, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				Hashtable<Long, ArrayList<Pair<Double, Double>>> vals = new Hashtable<Long, ArrayList<Pair<Double, Double>>>();
				int cantLista = 0;
				for (Pair<Long, String> p : listaInstantesEnteros) {
					ArrayList<Double> ad = generarListaDoubles(p.second, " ");
					vals.put(p.first, generarListaParesDouble(p.second));
					cantLista = ad.size();
				}
				ArrayList<Pair<Double, Double>> ini = new ArrayList<Pair<Double, Double>>();
				for (int i = 0; i < cantLista; i++) {
					ini.add(new Pair<Double, Double>(0.0, 0.0));
				}
				retorno = new EvolucionPorInstantes<ArrayList<Pair<Double, Double>>>(vals, ini, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				Hashtable<Long, ArrayList<Double>> vals = new Hashtable<Long, ArrayList<Double>>();
				int cantLista = 0;
				for (Pair<Long, String> p : listaInstantesEnteros) {
					ArrayList<Double> ad = generarListaDoubles(p.second, " ");
					vals.put(p.first, ad);
					cantLista = ad.size();
				}
				ArrayList<Double> ini = new ArrayList<Double>();
				for (int i = 0; i < cantLista; i++) {
					ini.add(0.0);
				}
				retorno = new EvolucionPorInstantes<ArrayList<Double>>(vals, ini, lt.getSentido());
			}
		} else if (eElement.getAttribute("tipo").equalsIgnoreCase("periodica")) {
			GregorianCalendar fechaInicial = Utilitarios.stringToGregorianCalendar(tiempoInicial, "dd MM yyyy");

			String defPeriodo = eElement.getElementsByTagName("definicionPeriodo").item(0).getTextContent();
			EvolucionPorInstantes evInst = null;
			ArrayList<Pair<String, String>> listaPorInstantes = generarListaPorInstantes(defPeriodo, ",");
			ArrayList<Pair<Long, String>> listaInstantesEnteros = new ArrayList<Pair<Long, String>>();
			GregorianCalendar fechaReferencia = new GregorianCalendar();
			DateFormat df = null;
			df = new SimpleDateFormat("dd MM yyyy"); // Se parsea un string con este formato a GregorianCalendar
			for (int i = 0; i < listaPorInstantes.size(); i++) {
				DateFormat dfi = null;
				if (listaPorInstantes.get(i).first.length() <= " dd MM yyyy ".length()) {
					dfi = df;
				} else {
					dfi = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
				}
				try {
					Date d = dfi.parse(listaPorInstantes.get(i).first);
					fechaReferencia.setTime(d);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				Long instante = restarFechas(fechaReferencia, fechaInicial);
				listaInstantesEnteros.add(new Pair<Long, String>(instante, listaPorInstantes.get(i).second));
			}

			if (tipoDato.equalsIgnoreCase("string")) {
				Hashtable<Long, String> vals = new Hashtable<Long, String>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, p.second);
				}
				evInst = new EvolucionPorInstantes<String>(vals, "", lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				Hashtable<Long, Double> vals = new Hashtable<Long, Double>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Double.parseDouble(p.second));
				}
				evInst = new EvolucionPorInstantes<Double>(vals, 0.0, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("int")) {
				Hashtable<Long, Integer> vals = new Hashtable<Long, Integer>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Integer.parseInt(p.second));
				}
				evInst = new EvolucionPorInstantes<Integer>(vals, 0, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				Hashtable<Long, Boolean> vals = new Hashtable<Long, Boolean>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Boolean.parseBoolean(p.second));
				}
				evInst = new EvolucionPorInstantes<Boolean>(vals, false, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				Hashtable<Long, ArrayList<Pair<Double, Double>>> vals = new Hashtable<Long, ArrayList<Pair<Double, Double>>>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, generarListaParesDouble(p.second));
				}
				evInst = new EvolucionPorInstantes<ArrayList<Pair<Double, Double>>>(vals, null, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				Hashtable<Long, ArrayList<Double>> vals = new Hashtable<Long, ArrayList<Double>>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, generarListaDoubles(p.second, " "));
				}
				evInst = new EvolucionPorInstantes<ArrayList<Double>>(vals, new ArrayList<Double>(), lt.getSentido());
			}

			String periodo = eElement.getElementsByTagName("periodo").item(0).getTextContent();
			int peri = Calendar.YEAR; // por defecto
			if (periodo.equalsIgnoreCase("año"))
				peri = Calendar.YEAR;
			if (periodo.equalsIgnoreCase("mes"))
				peri = Calendar.MONTH;
			if (periodo.equalsIgnoreCase("semana"))
				peri = Calendar.WEEK_OF_YEAR;
			if (periodo.equalsIgnoreCase("dia"))
				peri = Calendar.DAY_OF_YEAR;

			Integer cantPeriodo = Integer
					.parseInt(eElement.getElementsByTagName("cantPeriodo").item(0).getTextContent());

			retorno = new EvolucionPeriodica<Double>(fechaInicial, evInst, peri, cantPeriodo, lt.getSentido());

		} else if (eElement.getAttribute("tipo").equalsIgnoreCase("porCaso")) {
			String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
			Hashtable<String, Evolucion> evs = new Hashtable<String, Evolucion>();
			ArrayList<String> nombresCasos = new ArrayList<String>();

			NodeList listaHijos = eElement.getChildNodes();
			Node entrada = null;
			for (int temp = 0; temp < listaHijos.getLength(); temp++) {
				entrada = listaHijos.item(temp);
				if (entrada instanceof Element) {
					eElement = (Element) entrada;
					String etiqueta = entrada.getNodeName();
					if (etiqueta.equalsIgnoreCase("entrada")) {
						cargarEntrada(eElement, tipoDato, evs, nombresCasos, tiempoInicial, lt);
					}
				}
			}
			retorno = new EvolucionPorCaso(nombre, evs, lt.getSentido(), nombresCasos);
			datosCorrida.getEvolucionesPorCaso().add((EvolucionPorCaso) retorno);
		}
		return retorno;
	}

	// 28/06/2022 Refactoring eliminar el metodo cargarEvolucion y usar el cargarNodoEvolucion para que use el Nodo y no el padre,
	//  se evita el problema de los /t sea mas coherente y evite otoros problemas ej lista de nodos EV
	// USAR ESTE EN LUGAR DEL OTRO
	public Evolucion<?> cargarNodoEvolucion(Element eElement, String tipoDato, String tiempoInicial, DatosLineaTiempo lt) {


		String valor = eElement.getTextContent();
		Evolucion<?> retorno = null;
		if (eElement.getAttribute("tipo").equalsIgnoreCase("const")) {
			if (tipoDato.equalsIgnoreCase("string")) {
				retorno = new EvolucionConstante<String>(valor, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				retorno = new EvolucionConstante<Boolean>(Boolean.parseBoolean(valor), lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				retorno = new EvolucionConstante<Double>(Double.parseDouble(valor), lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("int")) {
				retorno = new EvolucionConstante<Integer>(Integer.parseInt(valor), lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				retorno = new EvolucionConstante<ArrayList<Pair<Double, Double>>>(generarListaParesDouble(valor),
						lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				retorno = new EvolucionConstante<ArrayList<Double>>(generarListaDoubles(valor, ","), lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("arraydouble")) {
				retorno = new EvolucionConstante<>(generarArrayDouble(valor, ","), lt.getSentido());
			}

		} else if (eElement.getAttribute("tipo").equalsIgnoreCase("porInstantes")) {
			DateFormat df = null;
			df = new SimpleDateFormat("dd MM yyyy"); // Se parsea un string con este formato a GregorianCalendar

			GregorianCalendar fechaInicial = Utilitarios.stringToGregorianCalendar(tiempoInicial, "dd MM yyyy");

			GregorianCalendar fechaReferencia = new GregorianCalendar();

			ArrayList<Pair<String, String>> listaPorInstantes = generarListaPorInstantes(valor, ",");

			ArrayList<Pair<Long, String>> listaInstantesEnteros = new ArrayList<Pair<Long, String>>();

			for (int i = 0; i < listaPorInstantes.size(); i++) {
				DateFormat dfi = null;
				if (listaPorInstantes.get(i).first.length() <= " dd MM yyyy ".length()) {
					dfi = df;
				} else {
					dfi = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
				}
				try {
					Date d = dfi.parse(listaPorInstantes.get(i).first);
					fechaReferencia.setTime(d);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				Long instante = restarFechas(fechaReferencia, fechaInicial);
				listaInstantesEnteros.add(new Pair<Long, String>(instante, listaPorInstantes.get(i).second));
			}

			if (tipoDato.equalsIgnoreCase("string")) {
				Hashtable<Long, String> vals = new Hashtable<Long, String>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, p.second);
				}
				retorno = new EvolucionPorInstantes<String>(vals, "", lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				Hashtable<Long, Double> vals = new Hashtable<Long, Double>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Double.parseDouble(p.second));
				}
				retorno = new EvolucionPorInstantes<Double>(vals, 0.0, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("int")) {
				Hashtable<Long, Integer> vals = new Hashtable<Long, Integer>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Integer.parseInt(p.second));
				}
				retorno = new EvolucionPorInstantes<Integer>(vals, 0, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				Hashtable<Long, Boolean> vals = new Hashtable<Long, Boolean>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Boolean.parseBoolean(p.second));
				}
				retorno = new EvolucionPorInstantes<Boolean>(vals, false, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				Hashtable<Long, ArrayList<Pair<Double, Double>>> vals = new Hashtable<Long, ArrayList<Pair<Double, Double>>>();
				int cantLista = 0;
				for (Pair<Long, String> p : listaInstantesEnteros) {
					ArrayList<Double> ad = generarListaDoubles(p.second, " ");
					vals.put(p.first, generarListaParesDouble(p.second));
					cantLista = ad.size();
				}
				ArrayList<Pair<Double, Double>> ini = new ArrayList<Pair<Double, Double>>();
				for (int i = 0; i < cantLista; i++) {
					ini.add(new Pair<Double, Double>(0.0, 0.0));
				}
				retorno = new EvolucionPorInstantes<ArrayList<Pair<Double, Double>>>(vals, ini, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				Hashtable<Long, ArrayList<Double>> vals = new Hashtable<Long, ArrayList<Double>>();
				int cantLista = 0;
				for (Pair<Long, String> p : listaInstantesEnteros) {
					ArrayList<Double> ad = generarListaDoubles(p.second, " ");
					vals.put(p.first, ad);
					cantLista = ad.size();
				}
				ArrayList<Double> ini = new ArrayList<Double>();
				for (int i = 0; i < cantLista; i++) {
					ini.add(0.0);
				}
				retorno = new EvolucionPorInstantes<ArrayList<Double>>(vals, ini, lt.getSentido());
			}
		} else if (eElement.getAttribute("tipo").equalsIgnoreCase("periodica")) {
			GregorianCalendar fechaInicial = Utilitarios.stringToGregorianCalendar(tiempoInicial, "dd MM yyyy");

			String defPeriodo = eElement.getElementsByTagName("definicionPeriodo").item(0).getTextContent();
			EvolucionPorInstantes evInst = null;
			ArrayList<Pair<String, String>> listaPorInstantes = generarListaPorInstantes(defPeriodo, ",");
			ArrayList<Pair<Long, String>> listaInstantesEnteros = new ArrayList<Pair<Long, String>>();
			GregorianCalendar fechaReferencia = new GregorianCalendar();
			DateFormat df = null;
			df = new SimpleDateFormat("dd MM yyyy"); // Se parsea un string con este formato a GregorianCalendar
			for (int i = 0; i < listaPorInstantes.size(); i++) {
				DateFormat dfi = null;
				if (listaPorInstantes.get(i).first.length() <= " dd MM yyyy ".length()) {
					dfi = df;
				} else {
					dfi = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
				}
				try {
					Date d = dfi.parse(listaPorInstantes.get(i).first);
					fechaReferencia.setTime(d);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				Long instante = restarFechas(fechaReferencia, fechaInicial);
				listaInstantesEnteros.add(new Pair<Long, String>(instante, listaPorInstantes.get(i).second));
			}

			if (tipoDato.equalsIgnoreCase("string")) {
				Hashtable<Long, String> vals = new Hashtable<Long, String>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, p.second);
				}
				evInst = new EvolucionPorInstantes<String>(vals, "", lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				Hashtable<Long, Double> vals = new Hashtable<Long, Double>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Double.parseDouble(p.second));
				}
				evInst = new EvolucionPorInstantes<Double>(vals, 0.0, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("int")) {
				Hashtable<Long, Integer> vals = new Hashtable<Long, Integer>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Integer.parseInt(p.second));
				}
				evInst = new EvolucionPorInstantes<Integer>(vals, 0, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				Hashtable<Long, Boolean> vals = new Hashtable<Long, Boolean>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Boolean.parseBoolean(p.second));
				}
				evInst = new EvolucionPorInstantes<Boolean>(vals, false, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				Hashtable<Long, ArrayList<Pair<Double, Double>>> vals = new Hashtable<Long, ArrayList<Pair<Double, Double>>>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, generarListaParesDouble(p.second));
				}
				evInst = new EvolucionPorInstantes<ArrayList<Pair<Double, Double>>>(vals, null, lt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				Hashtable<Long, ArrayList<Double>> vals = new Hashtable<Long, ArrayList<Double>>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, generarListaDoubles(p.second, " "));
				}
				evInst = new EvolucionPorInstantes<ArrayList<Double>>(vals, new ArrayList<Double>(), lt.getSentido());
			}

			String periodo = eElement.getElementsByTagName("periodo").item(0).getTextContent();
			int peri = Calendar.YEAR; // por defecto
			if (periodo.equalsIgnoreCase("año"))
				peri = Calendar.YEAR;
			if (periodo.equalsIgnoreCase("mes"))
				peri = Calendar.MONTH;
			if (periodo.equalsIgnoreCase("semana"))
				peri = Calendar.WEEK_OF_YEAR; 
//			if (periodo.equalsIgnoreCase("dia"))
//				peri = Calendar.DAY_OF_YEAR;

			Integer cantPeriodo = Integer
					.parseInt(eElement.getElementsByTagName("cantPeriodo").item(0).getTextContent());

			retorno = new EvolucionPeriodica<Double>(fechaInicial, evInst, peri, cantPeriodo, lt.getSentido());

		} else if (eElement.getAttribute("tipo").equalsIgnoreCase("porCaso")) {
			String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
			Hashtable<String, Evolucion> evs = new Hashtable<String, Evolucion>();
			ArrayList<String> nombresCasos = new ArrayList<String>();

			NodeList listaHijos = eElement.getChildNodes();
			Node entrada = null;
			for (int temp = 0; temp < listaHijos.getLength(); temp++) {
				entrada = listaHijos.item(temp);
				if (entrada instanceof Element) {
					eElement = (Element) entrada;
					String etiqueta = entrada.getNodeName();
					if (etiqueta.equalsIgnoreCase("entrada")) {
						cargarEntrada(eElement, tipoDato, evs, nombresCasos, tiempoInicial, lt);
					}
				}
			}
			retorno = new EvolucionPorCaso(nombre, evs, lt.getSentido(), nombresCasos);
			datosCorrida.getEvolucionesPorCaso().add((EvolucionPorCaso) retorno);
		}
		return retorno;
	}


	private void cargarEntrada(Element eElement, String tipoValor, Hashtable<String, Evolucion> evs,
							   ArrayList<String> nombresCasos, String tiempoInicial, DatosLineaTiempo lt) {

		String caso = eElement.getElementsByTagName("caso").item(0).getTextContent();
		nombresCasos.add(caso);
		evs.put(caso, cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("valor").item(0)).getElementsByTagName("ev").item(0), tipoValor, tiempoInicial, lt));

	}



	public static long restarFechas(GregorianCalendar fFin, GregorianCalendar fIni) {
		return (long) ((fFin.getTimeInMillis() - fIni.getTimeInMillis()) / 1000);
	}

	public static ArrayList<Pair<String, String>> generarListaPorInstantes(String aParsear, String separadorExterno) {
		String[] separados = aParsear.split(separadorExterno);

		ArrayList<Pair<String, String>> retorno = new ArrayList<Pair<String, String>>();

		for (int i = 0; i < separados.length; ++i) {
			retorno.add(generarParString(separados[i]));
		}

		return retorno;

	}

	public static ArrayList<Double> generarListaDoubles(String aParsear, String separadorExterno) {
		String[] separados = aParsear.split(separadorExterno);

		ArrayList<Double> retorno = new ArrayList<Double>();

		for (int i = 0; i < separados.length; ++i) {
			retorno.add(Double.parseDouble(separados[i]));
		}

		return retorno;
	}

	//	private double[] generarArrayDouble(String aParsear, String separadorExterno){
	private Double[] generarArrayDouble(String aParsear, String separadorExterno) {
		String[] separados = aParsear.split(separadorExterno);

//		double[] retorno = new double[separados.length];
		Double[] retorno = new Double[separados.length];

		for (int i = 0; i < separados.length; ++i) {
			retorno[i] = Double.parseDouble(separados[i]);
		}

		return retorno;
	}

	public static ArrayList<Pair<Double, Double>> generarListaParesDouble(String aParsear) {

		String[] separados = aParsear.split(",");

		ArrayList<Pair<Double, Double>> retorno = new ArrayList<Pair<Double, Double>>();

		for (int i = 0; i < separados.length; ++i) {
			retorno.add(generarPar(separados[i]));
		}

		return retorno;
	}

	private ArrayList<Pair<String, Double>> generarListaParesStringDouble(String aParsear) {

		String[] separados = aParsear.split(",");

		ArrayList<Pair<String, Double>> retorno = new ArrayList<Pair<String, Double>>();

		for (int i = 0; i < separados.length; ++i) {
			retorno.add(generarParStringDouble((separados[i])));
		}

		return retorno;
	}

	public DatosTermicoCorrida cargarTermico(DatosCorrida corrida, Node termo, boolean cargarTermicoAcorrida, boolean copiadoPortapapeles) {
		Element eElement = (Element) termo;
		String nombre = "", propietario = "", barra = "", flexibilidadMin = "";
		if (eElement.getElementsByTagName("nombre").item(0) != null) {
			nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
			if(!copiadoPortapapeles && !corrida.getTermicos().getListaUtilizados().contains(nombre)){
				return null;
			}
		}

		if (eElement.getElementsByTagName("propietario").item(0) != null) {
			propietario = eElement.getElementsByTagName("propietario").item(0).getTextContent();
		}
		if (eElement.getElementsByTagName("barra").item(0) != null) {
			barra = eElement.getElementsByTagName("barra").item(0).getTextContent();
		}
		if (eElement.getElementsByTagName("flexibilidadMin").item(0) != null) {
			flexibilidadMin = eElement.getElementsByTagName("flexibilidadMin").item(0).getTextContent();
		}

		Integer cantModIni = Integer.parseInt(eElement.getElementsByTagName("cantModIni").item(0).getTextContent());
		Evolucion<Double> dispMedia = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("dispMedia").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> tMedioArreglo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("tMedioArreglo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Integer> mantProgramado = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("mantProgramado").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> costoFijo = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoFijo").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		Evolucion<Double> costoVariable = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("costoVariable").item(0)).getElementsByTagName("ev").item(0), "double",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		System.out.println(nombre);
		Evolucion<Integer> cantModInst = (Evolucion<Integer>) cargarNodoEvolucion((Element) ((Element)
						eElement.getElementsByTagName("cantModInst").item(0)).getElementsByTagName("ev").item(0), "int",
				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
		boolean salDet = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetallada").item(0).getTextContent());
		Evolucion<Double> potMin = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMin").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		Evolucion<Double> potMax = (Evolucion<Double>) cargarNodoEvolucion((Element) ((Element)eElement.getElementsByTagName("potMax").item(0)).getElementsByTagName("ev").item(0),
				"double", corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());

		ArrayList<String> listaCombustibles = null;
		if(eElement.getElementsByTagName("listaCombustibles").item(0) != null ){
			listaCombustibles = generarListaStringConSeparador( eElement.getElementsByTagName("listaCombustibles").item(0).getTextContent(), ",");
		}

		ArrayList<String> listaBarrasComb = null;
		if(eElement.getElementsByTagName("listaBarrasComb").item(0) != null ){
			listaBarrasComb = generarListaStringConSeparador( eElement.getElementsByTagName("listaBarrasComb").item(0).getTextContent(), ",");
		}

		Hashtable<String,Evolucion<Double>> rendimientosMin = new Hashtable<>();
		if(eElement.getElementsByTagName("rendPotMin").item(0) != null ){
			NodeList listaRends = ((Element) eElement.getElementsByTagName("rendPotMin").item(0)).getElementsByTagName("ev");
			for (int i = 0; i < listaRends.getLength(); i++) {
				rendimientosMin.put(listaCombustibles.get(i),(Evolucion<Double>) cargarNodoEvolucion(
						((Element) listaRends.item(i)), "double",
						corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo()));
			}
		}

		Hashtable<String,Evolucion<Double>> rendimientosMax = new Hashtable<>();
		if(eElement.getElementsByTagName("rendPotMax").item(0) != null ) {
			NodeList listaRends = ((Element) eElement.getElementsByTagName("rendPotMax").item(0)).getElementsByTagName("ev");
			for (int i = 0; i < listaRends.getLength(); i++) {
				rendimientosMax.put(listaCombustibles.get(i),(Evolucion<Double>) cargarNodoEvolucion(
						((Element) listaRends.item(i)), "double",
						corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo()));
			}
		}

		Hashtable<String, String> combustiblesBarras = new Hashtable<String, String>();

		if(listaCombustibles != null) {
			for (int i = 0; i < listaCombustibles.size(); i++)
				combustiblesBarras.put(listaCombustibles.get(i), listaBarrasComb.get(i));
		}

		DatosTermicoCorrida nuevo = new DatosTermicoCorrida(nombre, propietario, barra, cantModInst, listaCombustibles,
				combustiblesBarras, potMin, potMax, rendimientosMax, rendimientosMin, flexibilidadMin, cantModIni,
				dispMedia, tMedioArreglo, salDet, mantProgramado, costoFijo, costoVariable);

	
		if(eElement.getElementsByTagName("compMinimosTecnicos").item(0) != null ){

			Evolucion<String> compMinimosTecnicos = (Evolucion<String>) cargarNodoEvolucion((Element) ((Element)
							eElement.getElementsByTagName("compMinimosTecnicos").item(0)).getElementsByTagName("ev").item(0), "string",
					corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
			nuevo.getValoresComportamientos().put(Constantes.COMPMINTEC, compMinimosTecnicos);
		}

		if(cargarTermicoAcorrida) {
			corrida.getTermicos().getTermicos().put(nuevo.getNombre(), nuevo);
		}
		return nuevo;

	}

	private boolean cargarParametrosGenerales(DatosCorrida nuevo, Node parametros) {
		Element eElement = (Element) parametros;

		String nombre = eElement.getElementsByTagName("nombre").item(0).getTextContent();
		String descripcion = eElement.getElementsByTagName("descripcion").item(0).getTextContent();
		String tipoSimulacion = eElement.getElementsByTagName("tipoSimulacion").item(0).getTextContent();
		String postizacion = "./resources/numposlineal.txt";
		String tipoPostizacion = ((Element) (eElement.getElementsByTagName("postizacion").item(0)))
				.getAttribute("tipo");
		Integer cantClusters = 0;
		if (tipoPostizacion.equalsIgnoreCase("interna")) {
			String val= eElement.getElementsByTagName("postizacion").item(0).getTextContent();
			if (val.equalsIgnoreCase("clustering")) {
				cantClusters= Integer.parseInt(eElement.getElementsByTagName("clusters").item(0).getTextContent());
			}
			postizacion=val;
		}
		String valPostizacion = eElement.getElementsByTagName("valpostizacion").item(0).getTextContent();
		String tipoValPostizacion = ((Element) (eElement.getElementsByTagName("valpostizacion").item(0)))
				.getAttribute("tipo");

		String inicioCorrida = eElement.getElementsByTagName("inicioCorrida").item(0).getTextContent();
		String finCorrida = eElement.getElementsByTagName("finCorrida").item(0).getTextContent();

		Integer cantEscenarios = Integer
				.parseInt(eElement.getElementsByTagName("cantEscenarios").item(0).getTextContent());
		Double tasa = Double.parseDouble(eElement.getElementsByTagName("tasaAnual").item(0).getTextContent());
		double topeSpot = Double.parseDouble(eElement.getElementsByTagName("topeSpot").item(0).getTextContent());
		
		boolean despSinExp = Boolean.parseBoolean(eElement.getElementsByTagName("despSinExp").item(0).getTextContent());
		int iteracionSinExp = Integer.parseInt(eElement.getElementsByTagName("iteracionSinExp").item(0).getTextContent());
		ArrayList<String> paisesACortar = generarListaStringConSeparador(eElement.getElementsByTagName("paisesACortar").item(0).getTextContent(), ",");
		
		NodeList nodoLineaTiempo = eElement.getElementsByTagName("lineaTiempo");
		DatosLineaTiempo ltiempo = cargarLineaTiempo(nodoLineaTiempo);
		if (!inicioCorrida.equalsIgnoreCase(ltiempo.getTiempoInicial())) {
			System.err.println("ERROR AL CARGAR: INICIO TIEMPO E INICIO CORRIDA DEBEN COINCIDIR");
			if (CorridaHandler.getInstance().isParalelo()) {
				//PizarronRedis pp = new PizarronRedis();
				//		pp.matarServidores();
			}
			return false;
		}
		NodeList nodoDatosPSalida = eElement.getElementsByTagName("salidaTexto");
		DatosParamSalida datosParamSal = cargarDatosParamSalida(nodoDatosPSalida);

		NodeList nodoDatosPSalidaSim = eElement.getElementsByTagName("salidaSimDet");
		DatosParamSalidaSim datosParamSalSim = cargarDatosParamSalidaSim(nodoDatosPSalidaSim);

		NodeList nodoDatosPSalidaOpt = eElement.getElementsByTagName("salidaOpt");
		DatosParamSalidaOpt datosParamSalOpt = cargarDatosParamSalidaOpt(nodoDatosPSalidaOpt);

		String valoresBellman = eElement.getElementsByTagName("tipoResolucionValoresBellman").item(0).getTextContent();
		String tipoDemanda = eElement.getElementsByTagName("tipoDemanda").item(0).getTextContent();
		String rutaSals = eElement.getElementsByTagName("rutaSalidas").item(0).getTextContent();
		Integer maxIteraciones = Integer
				.parseInt(eElement.getElementsByTagName("maximoIteraciones").item(0).getTextContent());
		Integer numIteraciones = Integer
				.parseInt(eElement.getElementsByTagName("numeroIteraciones").item(0).getTextContent());
		String criterioParada = eElement.getElementsByTagName("criterioParada").item(0).getTextContent();
		DatosIteracionesCorrida its = new DatosIteracionesCorrida(maxIteraciones, numIteraciones, criterioParada);
		Double semilla = Double.parseDouble(eElement.getElementsByTagName("semilla").item(0).getTextContent());
//		System.out.println("=>"+eElement.getElementsByTagName("cantSorteosMont").item(0).getChildNodes().getLength()+"<=");
//		System.out.println("=>"+eElement.getElementsByTagName("cantSorteosMont").item(0).getChildNodes().item(0)+"<=");
//		System.out.println("=>"+eElement.getElementsByTagName("cantSorteosMont").item(0).getChildNodes().item(1)+"<=");
//		System.out.println("=>"+eElement.getElementsByTagName("cantSorteosMont").item(0).getChildNodes().item(2)+"<=");
//		Evolucion<Integer> cantSorteosMont = new EvolucionConstante<Integer>(
//				Integer.parseInt(eElement.getElementsByTagName("cantSorteosMont").item(0).getTextContent()),
//				nuevo.getLineaTiempo().getSentido());

		Element e = (Element)((Element )(eElement.getElementsByTagName("cantSorteosMont").item(0))).getElementsByTagName("ev").item(0);
		Evolucion<Integer> cantSorteosMont = (Evolucion<Integer>) cargarNodoEvolucion(e, "int", ltiempo.getTiempoInicial(), ltiempo);


//				corrida.getLineaTiempo().getTiempoInicial(), corrida.getLineaTiempo());
//		System.out.println("->"+cantSorteosMont.getValor()+"<-");
	//	Boolean escenariosSerializados = Boolean.parseBoolean(eElement.getElementsByTagName("escenariosSerializados").item(0).getTextContent());
		nuevo.setSemilla(semilla);
		nuevo.setCantSorteosMont(cantSorteosMont);
		nuevo.setDatosParamSalida(datosParamSal);
		nuevo.setDatosParamSalidaOpt(datosParamSalOpt);
		nuevo.setDatosParamSalidaSim(datosParamSalSim);
		nuevo.setNombre(nombre);
		nuevo.setTasa(tasa);
		nuevo.setTopeSpot(topeSpot);
		nuevo.setDespSinExp(despSinExp);
		nuevo.setIteracionSinExp(iteracionSinExp);
		nuevo.setPaisesACortar(paisesACortar);
		nuevo.setValPostizacion(valPostizacion);
		nuevo.setTipoSimulacion(tipoSimulacion);
		nuevo.setTipoPostizacion(tipoPostizacion);
		nuevo.setPostizacion(postizacion);
		nuevo.setTipoValpostizacion(tipoValPostizacion);
		nuevo.setInicioCorrida(inicioCorrida);
		nuevo.setFinCorrida(finCorrida);
		nuevo.setLineaTiempo(ltiempo);
		nuevo.setDescripcion(descripcion);
		nuevo.setRutaSals(rutaSals);
		nuevo.setDatosIteraciones(its);
		nuevo.setCantEscenarios(cantEscenarios);
		nuevo.getValoresComportamientoGlobal().put("tipoResolucionValoresBellman",
				new EvolucionConstante<String>(valoresBellman, nuevo.getLineaTiempo().getSentido()));
		nuevo.getValoresComportamientoGlobal().put("tipoDemanda",
				new EvolucionConstante<String>(tipoDemanda, nuevo.getLineaTiempo().getSentido()));
	//	nuevo.setEscenariosSerializados(escenariosSerializados);
		nuevo.setClusters(cantClusters);
		return true;
	}

	private DatosParamSalida cargarDatosParamSalida(NodeList nodoPSalida) {
		Element eElement = (Element) nodoPSalida.item(0);

		Boolean enerResumen = Boolean
				.parseBoolean(eElement.getElementsByTagName("enerResumen").item(0).getTextContent());
		Boolean enercron = Boolean.parseBoolean(eElement.getElementsByTagName("enercron").item(0).getTextContent());
		Boolean potPoste = Boolean.parseBoolean(eElement.getElementsByTagName("potPoste").item(0).getTextContent());
		// Boolean indPot =
		// Boolean.parseBoolean(eElement.getElementsByTagName("indPot").item(0).getTextContent());
		Boolean costoResumen = Boolean
				.parseBoolean(eElement.getElementsByTagName("costoResumen").item(0).getTextContent());
		Boolean costocron = Boolean.parseBoolean(eElement.getElementsByTagName("costocron").item(0).getTextContent());
		Boolean costoPoste = Boolean.parseBoolean(eElement.getElementsByTagName("costoPoste").item(0).getTextContent());
		// Boolean indCostoPoste =
		// Boolean.parseBoolean(eElement.getElementsByTagName("indCostoPoste").item(0).getTextContent());
		Boolean cosmarResumen = Boolean
				.parseBoolean(eElement.getElementsByTagName("cosmarResumen").item(0).getTextContent());
		Boolean cosmarCron = Boolean.parseBoolean(eElement.getElementsByTagName("cosmarCron").item(0).getTextContent());
		// Boolean indCosmarCron =
		// Boolean.parseBoolean(eElement.getElementsByTagName("indCosmarCron").item(0).getTextContent());
		Boolean cantMod = Boolean.parseBoolean(eElement.getElementsByTagName("cantMod").item(0).getTextContent());
		//	Boolean indAtriDetallado = Boolean.parseBoolean(eElement.getElementsByTagName("indAtriDetallado").item(0).getTextContent());
		Boolean salidaDetalladaPaso = Boolean
				.parseBoolean(eElement.getElementsByTagName("salidaDetalladaPaso").item(0).getTextContent());
		Boolean costoPasoCron = Boolean
				.parseBoolean(eElement.getElementsByTagName("costoPasoCron").item(0).getTextContent());
		/*
		 * param[0] ener_resumen energía anual promedio en los escenarios; param[1]
		 * ener_cron energía por año y escenario para todos los recursos: filas
		 * año,escenario; columnas recurso param[2] pot_poste para recursos en
		 * particular, un archivo por poste, filas paso,columnas poste param[3] lista de
		 * enteros int[] con los indicadores de los recursos para los que se va a sacar
		 * el archivo de pot param[4] costo_resumen costo anual promedio en los
		 * escenarios; filas recurso; columnas año param[5] costo_cron costo por año y
		 * escenario paratodos los recursos: filas (año,escenario); columnas recurso
		 * param[6]costo_poste para recursos en particular, un archivo por poste,
		 * filaspaso, columnas poste param[7] lista de enteros int[] con los
		 * indicadoresde los recursos para los que se va a sacar el archivo de
		 * costo_poste param[8] cosmar_resumen filas paso; columnas poste; (los
		 * promedios segÃºn cantidad de horas = curva plana) param[9] cosmar_cron para
		 * barras en particular, un archivo por poste, filas paso, columnas crónicas
		 * param[10] lista de enterios int[] con los indices de las barras para los que
		 * se va a sacar los costos marginales detallados param[11] Si es =1 genera un
		 * directorio cantMod, con un archivo de disponibilidades para cada recurso En
		 * esos archivos las filas son pasos y las columnas son escenarios (crónicas)
		 * param[12] lista de enteros, uno por cada recurso, que indica con 1 si deben
		 * sacarse las salidas detalladas del recurso. param[13] Si es =1 genera el
		 * archivo de salidas detalladas por cada paso SalidaDetalladaSP param[14] Si es
		 * =1 genera el archivo de costo por paso y por crónica
		 *
		 */
		int[][] param = new int[Constantes.PARAMSAL_CANT_PARAM][];
		int[] vEnerResumen = { enerResumen ? 1 : 0 };
		int[] vecEnercron = { enercron ? 1 : 0 };
		int[] vpotPoste = { potPoste ? 1 : 0 };
//		int[] vindPot = { indPot ? 1 : 0 };
		int[] vcostoResumen = { costoResumen ? 1 : 0 };
		int[] vcostocron = { costocron ? 1 : 0 };
		int[] vcostoPoste = { costoPoste ? 1 : 0 };
//		int[] vindcostoPoste = { indCostoPoste ? 1 : 0 };
		int[] vcosmarResumen = { cosmarResumen ? 1 : 0 };
		int[] vcosmarCron = { cosmarCron ? 1 : 0 };
//		int[] vindCosmarCron = { indCosmarCron ? 1 : 0 };
		int[] vcantMod = { cantMod ? 1 : 0 };
		//	int[] vindAtriDetallado = { indAtriDetallado ? 1 : 0 };
		int[] vsalidaDetalladaPaso = { salidaDetalladaPaso ? 1 : 0 };
		int[] vcostoPasoCron = { costoPasoCron ? 1 : 0 };

		param[Constantes.PARAMSAL_RESUMEN] = vEnerResumen;
		param[Constantes.PARAMSAL_ENERCRON] = vecEnercron;
		param[Constantes.PARAMSAL_POT] = vpotPoste;
//		param[Constantes.PARAMSAL_IND_POT] = vindPot;
		param[Constantes.PARAMSAL_COSTO_RESUMEN] = vcostoResumen;
		param[Constantes.PARAMSAL_COSTO_CRON] = vcostocron;
		param[Constantes.PARAMSAL_COSTO_POSTE] = vcostoPoste;
//		param[Constantes.PARAMSAL_IND_COSTO_POSTE] = vindcostoPoste;
		param[Constantes.PARAMSAL_COSMAR_RESUMEN] = vcosmarResumen;
		param[Constantes.PARAMSAL_COSMAR_CRON] = vcosmarCron;
//		param[Constantes.PARAMSAL_IND_COSMAR_CRON] = vindCosmarCron;
		param[Constantes.PARAMSAL_CANTMOD] = vcantMod;
		//	param[Constantes.PARAMSAL_IND_ATR_DET] = vindAtriDetallado;
		param[Constantes.PARAMSAL_SALIDA_DET_PASO] = vsalidaDetalladaPaso;
		param[Constantes.PARAMSAL_COSTO_PASO_CRON] = vcostoPasoCron;

		return new DatosParamSalida(param);
	}

	private DatosParamSalidaOpt cargarDatosParamSalidaOpt(NodeList nodoPSalidaOpt) {
		Element eElement = (Element) nodoPSalidaOpt.item(0);
		Boolean salOpt = Boolean.parseBoolean(eElement.getElementsByTagName("salOpt").item(0).getTextContent());
		int pasoIni = Integer.parseInt(eElement.getElementsByTagName("pasoIni").item(0).getTextContent());
		int pasoFin = Integer.parseInt(eElement.getElementsByTagName("pasoFin").item(0).getTextContent());
		int sortIni = Integer.parseInt(eElement.getElementsByTagName("sortIni").item(0).getTextContent());
		int sortFin = Integer.parseInt(eElement.getElementsByTagName("sortFin").item(0).getTextContent());
		int[] estadoIni = generarListaIntConSeparador(
				eElement.getElementsByTagName("estadoIni").item(0).getTextContent(), ",");
		int[] estadoFin = generarListaIntConSeparador(
				eElement.getElementsByTagName("estadoFin").item(0).getTextContent(), ",");

		return new DatosParamSalidaOpt(estadoIni, estadoFin, pasoIni, pasoFin, sortIni, sortFin, salOpt);
	}

	private DatosParamSalidaSim cargarDatosParamSalidaSim(NodeList nodoPSalidaSim) {
		Element eElement = (Element) nodoPSalidaSim.item(0);
		Boolean salOpt = Boolean.parseBoolean(eElement.getElementsByTagName("salSim").item(0).getTextContent());
		int pasoIni = Integer.parseInt(eElement.getElementsByTagName("pasoIni").item(0).getTextContent());
		int pasoFin = Integer.parseInt(eElement.getElementsByTagName("pasoFin").item(0).getTextContent());
		int escIni = Integer.parseInt(eElement.getElementsByTagName("escIni").item(0).getTextContent());
		int escFin = Integer.parseInt(eElement.getElementsByTagName("escFin").item(0).getTextContent());

		return new DatosParamSalidaSim(escIni, escFin, pasoIni, pasoFin, salOpt);
	}

	private DatosLineaTiempo cargarLineaTiempo(NodeList nodoLineaTiempo) {
		Element eElement = (Element) nodoLineaTiempo.item(0);

		String inicioTiempo = eElement.getElementsByTagName("inicioTiempo").item(0).getTextContent();
		String activo = eElement.getElementsByTagName("activo").item(0).getTextContent();
		// 0-año, 1-mes, 2-semana
		String durPeriodoIntegracion = eElement.getElementsByTagName("durPeriodoIntegracion").item(0).getTextContent();

		NodeList nBloques = eElement.getElementsByTagName("listaBloques");

		DatosLineaTiempo resultado = new DatosLineaTiempo();

		cargarBloques(nBloques.item(0).getChildNodes(), resultado);
		resultado.setTiempoInicial(inicioTiempo);
		resultado.setTiempoInicialEvoluciones(inicioTiempo);
		resultado.setUsarPeriodoIntegracion(Boolean.parseBoolean(activo));
		resultado.setPeriodoIntegracion(Integer.parseInt(durPeriodoIntegracion));
		return resultado;
	}

	private void cargarBloques(NodeList nBloques, DatosLineaTiempo resultado) {

		Node nodo;
		for (int temp = 0; temp < nBloques.getLength(); temp++) {
			nodo = nBloques.item(temp);
			if (nodo instanceof Element) {
				cargarBloqueTiempo(resultado, nodo);
			}
		}
	}

	private void cargarBloqueTiempo(DatosLineaTiempo resultado, Node bloque) {
		Element eElement = (Element) bloque;

		String cantPasos = eElement.getElementsByTagName("cantPasos").item(0).getTextContent();
		String durPaso = eElement.getElementsByTagName("durPaso").item(0).getTextContent();
		String intervaloMuestreo = eElement.getElementsByTagName("intervaloMuestreo").item(0).getTextContent();
		String cantPostes = eElement.getElementsByTagName("cantPostes").item(0).getTextContent();
		String durPos = eElement.getElementsByTagName("duracionPostes").item(0).getTextContent();
		String periodoBloque = eElement.getElementsByTagName("periodoBloque").item(0).getTextContent();
		String cronologico = eElement.getElementsByTagName("cronologico").item(0).getTextContent();

		resultado.agregarBloque(cantPasos, durPaso, intervaloMuestreo, cantPostes,
				generarArrayLIntegerConSeparador(durPos, ","), periodoBloque, cronologico);

	}

	public void guardarCorrida(DatosCorrida corrida, String ruta) {

	}

	public Document getDom() {
		return dom;
	}

	public void setDom(Document dom) {
		this.dom = dom;
	}

}
