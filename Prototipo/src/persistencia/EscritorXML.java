/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorXML is part of MOP.
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import datatypes.*;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesTiempo.DatosLineaTiempo;
import interfaz.Text;
import logica.CorridaHandler;
import parque.Corrida;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.EvolucionPeriodica;
import tiempo.EvolucionPorInstantes;
import tiempo.LineaTiempo;
import utilitarios.Constantes;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utilitarios.UtilStrings;

public class EscritorXML {
	private Document dom;
	private Element last_root;
	private DatosCorrida corrida;
	private LineaTiempo lt;

	public EscritorXML(LineaTiempo lt) {
		super();
		this.lt = lt;
	}

	public boolean guardarCorrida(DatosCorrida corrida, String ruta) {
		boolean ret = false;
		try {
			File fXmlFile = new File(ruta);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dom = dBuilder.newDocument();
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer trans = transFactory.newTransformer();
			this.setCorrida(corrida);
			escribirCorrida(corrida);

			StreamResult sr = new StreamResult(new File(ruta));
			DOMSource source = new DOMSource(dom);
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "no");
			trans.transform(source, sr);
			System.out.println("Guardado");
			ret = true;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			return ret;
		}
	}

	private void escribirCorrida(DatosCorrida corrida) {
		Element nuevo = dom.createElement("corrida");
		nuevo.appendChild(escribirParametrosGenerales(corrida));
		nuevo.appendChild(escribirProcesosEstocasticos(corrida.getProcesosEstocasticos()));
		nuevo.appendChild(escribirParticipantes(corrida));
	

		dom.appendChild(nuevo);
	}

	private Element escribirParticipantes(DatosCorrida corrida) {

		Element nuevo = dom.createElement("participantes");

		nuevo.appendChild(escribirGeneradores(corrida));
		nuevo.appendChild(escribirComercioEnergia(corrida.getImpoExpos()));
		nuevo.appendChild(escribirDemandas(corrida.getDemandas()));
		nuevo.appendChild(escribirFallas(corrida.getFallas()));
		nuevo.appendChild(escribirImpactos(corrida.getImpactos()));
		nuevo.appendChild(escribirRedElectrica(corrida.getRed()));
		nuevo.appendChild(escribirCombustibles(corrida.getCombustibles()));
		nuevo.appendChild(escribirContratosEnergia(corrida.getContratosEnergia()));

		return nuevo;
	}

	private Element escribirImpactos(DatosImpactosCorrida impactos) {
		Element nuevo = dom.createElement("impactosAmbientales");

		nuevo.appendChild(escribirListaUtilizados(impactos.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(impactos.getAtributosDetallados()));
		nuevo.appendChild(escribirListaImpactos(impactos.getImpactos()));

		return nuevo;
	}
	private Element escribirListaImpactos(Hashtable<String, DatosImpactoCorrida> impactos) {
		Element nuevo = dom.createElement("listaImpactos");

		Set<String> keys = impactos.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirImpacto(impactos.get(it.next())));
		}

		return nuevo;
	}

	private Element escribirImpacto(DatosImpactoCorrida di) {
		Element nuevo = dom.createElement("impacto");

		nuevo.appendChild(escribirDatosImpacto(di));


		return nuevo;

	}

	private Element escribirDatosImpacto(DatosImpactoCorrida di) {
		Element nuevo = dom.createElement("datosImpacto");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(di.getNombre()));
		nuevo.appendChild(nombre);

		Element tcme = crearElemento("activo");
		tcme.appendChild(escribirEvolucion(di.getActivo(), "boolean"));
		nuevo.appendChild(tcme);

		Element costo = crearElemento("costo");
		costo.appendChild(escribirEvolucion(di.getCostoUnit(), "double"));
		nuevo.appendChild(costo);

		Element lim= crearElemento("limite");
		lim.appendChild(escribirEvolucion(di.getLimite(), "double"));
		nuevo.appendChild(lim);


		Element pp = crearElemento("porPoste");
		pp.appendChild(dom.createTextNode(Boolean.toString(di.isPorPoste())));
		nuevo.appendChild(pp);


		Element involucrados = crearElemento("participantesInvolucrados");
		involucrados.appendChild(dom.createTextNode(arrayStringXML(di.getInvolucrados())));
		nuevo.appendChild(involucrados);

		Element tipo = crearElemento("tipoImpacto");
		String tipoString = "";
		int tipoEntero = di.getTipoImpacto();
		if (tipoEntero==Constantes.HIDRO_INUN_AGUAS_ABAJO) {
			tipoString = Text.STRING_HIDRO_INUN_AGUAS_ABAJO;
		} else if (tipoEntero==Constantes.HIDRO_INUN_AGUAS_ARRIBA) {
			tipoString = Text.STRING_HIDRO_INUN_AGUAS_ARRIBA;
		} else if (tipoEntero==Constantes.HIDRO_PROD_MAQUINA) {
			tipoString = Text.STRING_HIDRO_PROD_MAQUINA;
		} else if (tipoEntero==Constantes.TER_EMISIONES_CO2) {
			tipoString = Text.STRING_TER_EMISIONES_CO2;
		} else if (tipoEntero==Constantes.HIDRO_CAUDAL_ECOLOGICO) {
			tipoString = Text.STRING_HIDRO_CAUDAL_ECOLOGICO;
		} else if (tipoEntero==Constantes.HIDRO_VERTIMIENTO_EXTERNO) {
			tipoString = Text.STRING_HIDRO_VERTIMIENTO_EXTERNO;
		}
		tipo.appendChild(dom.createTextNode(tipoString));
		nuevo.appendChild(tipo);

		Element put = crearElemento("porUnidadDeTiempo");
		put.appendChild(dom.createTextNode(Boolean.toString(di.isPorUnidadTiempo())));
		nuevo.appendChild(put);


		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(di.isSalDetallada())));
		nuevo.appendChild(sd);

		return nuevo;
	}

	private Element escribirContratosEnergia(DatosContratosEnergiaCorrida contratos) {
		Element nuevo = dom.createElement("contratos");

		nuevo.appendChild(escribirListaUtilizados(contratos.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(contratos.getAtributosDetallados()));
		nuevo.appendChild(escribirListaContratos(contratos.getContratosEnergia()));


		return nuevo;
	}
	private Element escribirListaContratos(Hashtable<String, DatosContratoEnergiaCorrida> contratos) {
		Element nuevo = dom.createElement("listaContratos");

		Set<String> keys = contratos.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirContratos(contratos.get(it.next())));
		}

		return nuevo;
	}
	
	private Element escribirContratos(DatosContratoEnergiaCorrida di) {
		Element nuevo = dom.createElement("contrato");

		nuevo.appendChild(escribirDatosContratoEnergia(di));
		

		return nuevo;

	}
	
	private Element escribirDatosContratoEnergia(DatosContratoEnergiaCorrida dc) {
		Element nuevo = dom.createElement("datosImpacto");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(dc.getNombre()));
		nuevo.appendChild(nombre);
		
		Element involucrados = crearElemento("participantesInvolucrados");

		involucrados.appendChild(dom.createTextNode(arrayStringXML(dc.getInvolucrados())));
		nuevo.appendChild(involucrados);
		
		Element pb= crearElemento("precioBase");
		pb.appendChild(escribirEvolucion(dc.getPrecioBase(), "double"));
		nuevo.appendChild(pb);
		
		Element eb = crearElemento("energiaBase");
		eb.appendChild(escribirEvolucion(dc.getEnergiaBase(), "double"));
		nuevo.appendChild(eb);
		
		Element fi = crearElemento("fechaInicial");
		fi.appendChild(dom.createTextNode(dc.getFechaInicial()));
		nuevo.appendChild(fi);
		
		
		
		Element canta = crearElemento("cantAnios");
		canta.appendChild(dom.createTextNode(Integer.toString(dc.getCantAnios())));
		nuevo.appendChild(canta);
		
		Element ei = crearElemento("energiaInicial");
		ei.appendChild(dom.createTextNode(Double.toString(dc.getEnergiaInicial())));
		nuevo.appendChild(ei);
		
		Element ci = crearElemento("cotaInf");
		ci.appendChild(escribirEvolucion(dc.getEnergiaBase(), "double"));
		nuevo.appendChild(ci);
		
		Element cs = crearElemento("cotaSup");
		cs.appendChild(escribirEvolucion(dc.getEnergiaBase(), "double"));
		nuevo.appendChild(cs);

		

		Element tipo = crearElemento("tipoContrato");
		String tipoString = "";
		String tipoEntero = dc.getTipo();
		if (tipoEntero.equalsIgnoreCase(Constantes.LIM_ENERGIA_ANUAL)) {
			tipoString = Constantes.LIM_ENERGIA_ANUAL;		
		} 
		
		tipo.appendChild(dom.createTextNode(tipoString));
		nuevo.appendChild(tipo);
		
		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(dc.isSalDetallada())));

		nuevo.appendChild(sd);

		return nuevo;

	}
	
	private Element escribirCombustibles(DatosCombustiblesCorrida combustibles) {
		Element nuevo = dom.createElement("combustibles");

		nuevo.appendChild(escribirListaUtilizados(combustibles.getListaUtilizados()));
		nuevo.appendChild(escribirListaCombustibles(combustibles.getCombustibles()));

		return nuevo;
	}

	private Element escribirListaCombustibles(Hashtable<String, DatosCombustibleCorrida> combustibles) {
		Element nuevo = dom.createElement("listaCombustibles");

		Set<String> keys = combustibles.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirCombustible(combustibles.get(it.next())));
		}

		return nuevo;
	}

	private Element escribirCombustible(DatosCombustibleCorrida dc) {
		Element nuevo = dom.createElement("combustible");

		nuevo.appendChild(escribirDatosCombustible(dc));
		nuevo.appendChild(escribirRedCombustible(dc));

		return nuevo;

	}

	private Node escribirRedCombustible(DatosCombustibleCorrida dc) {
		Element nuevo = dom.createElement("red");
		nuevo.appendChild(escribirCompsGenerales(dc.getRed().getValoresComportamiento()));

		nuevo.appendChild(escribirBarrasComb(dc.getRed()));
		nuevo.appendChild(escribirRamasComb(dc.getRed()));
		nuevo.appendChild(escribirContratos(dc.getRed()));

		return nuevo;
	}

	private Element escribirContratos(DatosRedCombustibleCorrida red) {
		Element node = dom.createElement("listaContratosCombustibleCanioSimple");

		for (DatosContratoCombustibleCorrida cc : red.getContratos()) {
			node.appendChild(escribirContrato(cc));
		}

		return node;
	}

	private Element escribirContrato(DatosContratoCombustibleCorrida cc) {
		Element nuevo = dom.createElement("contratoCanioSimple");

		nuevo.appendChild(escribirDatosContrato(cc));

		return nuevo;
	}

	private Node escribirDatosContrato(DatosContratoCombustibleCorrida cc) {
		Element nuevo = dom.createElement("datosContratoCanioSimple");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(cc.getNombre()));
		nuevo.appendChild(nombre);

		Element b1 = crearElemento("barra");
		b1.appendChild(dom.createTextNode(cc.getBarra()));
		nuevo.appendChild(b1);

		Element comb1 = crearElemento("combustible");
		comb1.appendChild(dom.createTextNode(cc.getComb()));
		nuevo.appendChild(comb1);

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(cc.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);

		nuevo.appendChild(escribirDispModulos(cc.getCantModIni(), cc.getDispMedia(), cc.gettMedioArreglo()));

		Element mprog = crearElemento("mantProgramado");
		mprog.appendChild(escribirEvolucion(cc.getMantProgramado(), "int"));
		nuevo.appendChild(mprog);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(cc.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element cM = crearElemento("caudalMax", "unidad", "m3/h");
		cM.appendChild(escribirEvolucion(cc.getCaudalMax(), "double"));
		nuevo.appendChild(cM);

		Element pC = crearElemento("precioComb", "unidad", "USD/m3");
		pC.appendChild(escribirEvolucion(cc.getPrecioComb(), "double"));
		nuevo.appendChild(pC);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(cc.isSalDetallada())));
		nuevo.appendChild(sd);
		return nuevo;
	}

	private Element escribirRamasComb(DatosRedCombustibleCorrida datosRedCombustibleCorrida) {
		Element node = dom.createElement("listaDuctosComb");

		for (DatosDuctoCombCorrida dr : datosRedCombustibleCorrida.getRamas()) {
			node.appendChild(escribirRamaComb(dr));
		}

		return node;
	}

	private Element escribirRamaComb(DatosDuctoCombCorrida dr) {
		Element nuevo = dom.createElement("ductoComb");

		nuevo.appendChild(escribirDatosRamaComb(dr));
		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(dr.isSalDetallada())));
		nuevo.appendChild(sd);

		return nuevo;
	}

	private Element escribirDatosRamaComb(DatosDuctoCombCorrida dr) {
		Element nuevo = dom.createElement("datosDuctoComb");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(dr.getNombre()));
		nuevo.appendChild(nombre);

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(dr.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);

		nuevo.appendChild(escribirDispModulos(dr.getCantModIni(), dr.getDispMedia(), dr.gettMedioArreglo()));

		Element mprog = crearElemento("mantProgramado");
		mprog.appendChild(escribirEvolucion(dr.getMantProgramado(), "int"));
		nuevo.appendChild(mprog);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(dr.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element b1 = crearElemento("barra1");
		b1.appendChild(dom.createTextNode(dr.getBarra1()));
		nuevo.appendChild(b1);

		Element b2 = crearElemento("barra2");
		b2.appendChild(dom.createTextNode(dr.getBarra2()));
		nuevo.appendChild(b2);

		Element c12 = crearElemento("capacidad12");
		c12.appendChild(escribirEvolucion(dr.getCapacidad12(), "double"));
		nuevo.appendChild(c12);

		Element c21 = crearElemento("capacidad21");
		c21.appendChild(escribirEvolucion(dr.getCapacidad21(), "double"));
		nuevo.appendChild(c21);

		Element p12 = crearElemento("perdidas12");
		p12.appendChild(escribirEvolucion(dr.getPerdidas12(), "double"));
		nuevo.appendChild(p12);

		Element p21 = crearElemento("perdidas21");
		p21.appendChild(escribirEvolucion(dr.getPerdidas21(), "double"));
		nuevo.appendChild(p21);


		return nuevo;
	}

	private Element escribirBarrasComb(DatosRedCombustibleCorrida datosRedCombustibleCorrida) {
		Element node = dom.createElement("listaBarrasComb");

		for (DatosBarraCombCorrida dr : datosRedCombustibleCorrida.getBarras()) {
			node.appendChild(escribirBarraComb(dr));
		}

		return node;
	}

	private Element escribirBarraComb(DatosBarraCombCorrida dr) {
		Element nuevo = dom.createElement("barraComb");
		nuevo.appendChild(escribirDatosBarra(dr));
		return nuevo;
	}

	private Element escribirDatosBarra(DatosBarraCombCorrida dr) {
		Element nuevo = dom.createElement("datosBarra");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(dr.getNombre()));
		nuevo.appendChild(nombre);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode("true"));
		nuevo.appendChild(sd);

		return nuevo;
	}

	private Element escribirDatosCombustible(DatosCombustibleCorrida dc) {
		Element nuevo = dom.createElement("datosCombustible");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(dc.getNombre()));
		nuevo.appendChild(nombre);

		Element pci = crearElemento("pci", "unidad", "MWh/m3");
		pci.appendChild(dom.createTextNode(dc.getPciPorUnidad().toString()));
		nuevo.appendChild(pci);

		Element undiad = crearElemento("unidad");
		undiad.appendChild(dom.createTextNode(dc.getUnidad()));
		nuevo.appendChild(undiad);

		Element den = crearElemento("densidad", "unidad", "kg/m3");
		den.appendChild(dom.createTextNode((dc.getDensidad().toString())));
		nuevo.appendChild(den);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(dc.isSalDetallada())));
		nuevo.appendChild(sd);

		return nuevo;
	}

	private Element escribirRedElectrica(DatosRedElectricaCorrida red) {
		Element nuevo = dom.createElement("redElectrica");
		nuevo.appendChild(escribirCompsGenerales(red.getValoresComportamiento()));
		

		Element flotante = dom.createElement("flotante");
		flotante.appendChild(dom.createTextNode(red.getFlotante()));
		nuevo.appendChild(flotante);

		
		nuevo.appendChild(escribirBarras(red));
		nuevo.appendChild(escribirRamas(red));
		return nuevo;
	}

	private Element escribirRamas(DatosRedElectricaCorrida red) {
		Element node = dom.createElement("ramas");

		node.appendChild(escribirListaUtilizados(red.getListaRamasUtilizadas()));
		//node.appendChild(escribirCompsPorDefecto(red.getValoresComportamiento()));
		/// Se hardcodea el comportamiento por defecto de las ramas ya que la estructura va a cambiar y este atributo no se esta guardando en el modelo
		Element nuevo = dom.createElement("compsPorDefecto");
		Element comp = dom.createElement("compRama");
		Evolucion<String> compRama = new EvolucionConstante<String>("simple", corrida.getLineaTiempo().getSentido());
		comp.appendChild(escribirEvolucion(compRama, "string"));
		nuevo.appendChild(comp);
		node.appendChild(nuevo);


//		node.appendChild(escribirRamas(red.getRamas()));
		node.appendChild(escribirListaRamas(red.getRamas()));

		return node;
	}

	private Node escribirRamas(Hashtable<String, DatosRamaCorrida> ramas) {
		Element nuevo = dom.createElement("listaRamas");

		nuevo.appendChild(escribirListaRamas(ramas));

		return nuevo;
	}

	private Element escribirListaRamas(Hashtable<String, DatosRamaCorrida> ramas) {
		Element nuevo = dom.createElement("listaRamas");

		Set<String> keys = ramas.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirRama(ramas.get(it.next())));
		}

		return nuevo;
	}

	private Node escribirRama(DatosRamaCorrida datosRamaCorrida) {
		Element nuevo = dom.createElement("rama");

		nuevo.appendChild(escribirCompsGenerales(datosRamaCorrida.getValoresComportamientos()));
		nuevo.appendChild(escribirDatosRama(datosRamaCorrida));

		return nuevo;
	}

	private Element escribirDatosRama(DatosRamaCorrida datosRamaCorrida) {
		Element nuevo = dom.createElement("datosRama");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(datosRamaCorrida.getNombre()));
		nuevo.appendChild(nombre);

		Element barra1 = crearElemento("barra1");
		barra1.appendChild(dom.createTextNode(datosRamaCorrida.getBarra1()));
		nuevo.appendChild(barra1);

		Element barra2 = crearElemento("barra2");
		barra2.appendChild(dom.createTextNode(datosRamaCorrida.getBarra2()));
		nuevo.appendChild(barra2);

		Element peaje12 = crearElemento("peaje12");
		peaje12.appendChild(escribirEvolucion(datosRamaCorrida.getPeaje21(), "double"));
		nuevo.appendChild(peaje12);

		Element peaje21 = crearElemento("peaje21");
		peaje21.appendChild(escribirEvolucion(datosRamaCorrida.getPeaje12(), "double"));
		nuevo.appendChild(peaje21);

		Element pMax12 = crearElemento("potMax12");
		pMax12.appendChild(escribirEvolucion(datosRamaCorrida.getPotMax12(), "double"));
		nuevo.appendChild(pMax12);

		Element pMax21 = crearElemento("potMax21");
		pMax21.appendChild(escribirEvolucion(datosRamaCorrida.getPotMax21(), "double"));
		nuevo.appendChild(pMax21);

		Element perdidas12 = crearElemento("perdidas12");
		perdidas12.appendChild(escribirEvolucion(datosRamaCorrida.getPerdidas12(), "double"));
		nuevo.appendChild(perdidas12);

		Element perdidas21 = crearElemento("perdidas21");
		perdidas21.appendChild(escribirEvolucion(datosRamaCorrida.getPerdidas21(), "double"));
		nuevo.appendChild(perdidas21);

		Element X = crearElemento("X");
		X.appendChild(escribirEvolucion(datosRamaCorrida.getX(), "double"));
		nuevo.appendChild(X);

		Element R = crearElemento("R");
		R.appendChild(escribirEvolucion(datosRamaCorrida.getR(), "double"));
		nuevo.appendChild(R);

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(datosRamaCorrida.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);

		nuevo.appendChild(escribirDispModulos(datosRamaCorrida.getCantModIni(), datosRamaCorrida.getDispMedia(),
				datosRamaCorrida.gettMedioArreglo()));

		Element mprog = crearElemento("mantProgramado");
		mprog.appendChild(escribirEvolucion(datosRamaCorrida.getMantProgramado(), "int"));
		nuevo.appendChild(mprog);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(datosRamaCorrida.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(datosRamaCorrida.isSalDetallada())));
		nuevo.appendChild(sd);

		return nuevo;
	}

	private Element escribirBarras(DatosRedElectricaCorrida red) {
		Element nuevo = dom.createElement("barras");

		nuevo.appendChild(escribirListaUtilizados(red.getListaBarrasUtilizadas()));
		nuevo.appendChild(escribirListaBarras(red.getBarras()));

		return nuevo;
	}

	private Node escribirListaBarras(Hashtable<String, DatosBarraCorrida> barras) {
		Element nuevo = dom.createElement("listaBarras");

		Set<String> keys = barras.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirBarra(barras.get(it.next())));
		}

		return nuevo;
	}

	private Element escribirBarra(DatosBarraCorrida datosBarraCorrida) {
		Element nuevo = dom.createElement("barra");

		nuevo.appendChild(escribirDatosBarra(datosBarraCorrida));

		return nuevo;
	}

	private Element escribirDatosBarra(DatosBarraCorrida datosBarraCorrida) {
		Element nuevo = dom.createElement("datosBarra");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(datosBarraCorrida.getNombre()));
		nuevo.appendChild(nombre);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString((datosBarraCorrida.isSalDetallada()))));
		nuevo.appendChild(sd);

		return nuevo;
	}

	private Node escribirFallas(DatosFallasEscalonadasCorrida fallas) {
		Element nuevo = dom.createElement("fallas");

		nuevo.appendChild(escribirCompsPorDefecto(fallas.getValoresComportamiento()));
		nuevo.appendChild(escribirListaUtilizados(fallas.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(fallas.getAtributosDetallados()));
		nuevo.appendChild(escribirListaFallas(fallas.getFallas()));

		return nuevo;

	}

	private Node escribirListaFallas(Hashtable<String, DatosFallaEscalonadaCorrida> fallas) {
		Element nuevo = dom.createElement("listaFallas");

		Set<String> keys = fallas.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirFalla(fallas.get(it.next())));

		}

		return nuevo;
	}

	private Element escribirFalla(DatosFallaEscalonadaCorrida df) {
		Element nuevo = dom.createElement("falla");

		nuevo.appendChild(escribirCompsGenerales(df.getValsComps()));
		nuevo.appendChild(escribirDatosFalla(df));
		if (df.getVarsEstado() == null)
			return nuevo;
		if (!df.getVarsEstado().isEmpty())
			nuevo.appendChild(escribirVariablesEstado(df.getVarsEstado()));
		if (df.getVarsControlDE() != null && !df.getVarsControlDE().isEmpty()){
			nuevo.appendChild(escribirVariablesControlDE(df.getVarsControlDE()));
		}

		return nuevo;

	}

	private Element escribirDatosFalla(DatosFallaEscalonadaCorrida df) {
		Element nuevo = dom.createElement("datosFalla");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(df.getNombre()));
		nuevo.appendChild(nombre);

		Element cep = crearElemento("cantEscProgram");
		cep.appendChild(dom.createTextNode(Integer.toString(df.getCantEscProgram())));
		nuevo.appendChild(cep);

		/*Element dmf = crearElemento("durMinForzamientos", "unidad", "dias");
		*//*int[] durMinFozDias = new int[df.getDurMinForzSeg().length];
		for(int i = 0; i<durMinFozDias.length; i++){
			durMinFozDias[i] = df.getDurMinForzSeg()[i]/Constantes.SEGUNDOSXDIA;
		}*//*
		dmf.appendChild(dom.createTextNode(arrayStringXML(Arrays.toString(durMinFozDias))));
		nuevo.appendChild(dmf);*/

		Element esc = crearElemento("escalonesPorciento", "unidad1", "%", "unidad2", "USD/MWh");
		StringBuilder escStr = new StringBuilder();
		for(Pair<Double,Double> par : df.getEscalones()){
			escStr.append("(").append(par.first).append(";").append(par.second).append(")").append(",");
		}
		esc.appendChild(dom.createTextNode(escStr.toString().substring(0,escStr.toString().length()-1)));
		nuevo.appendChild(esc);

		Element demanda = crearElemento("demanda");
		demanda.appendChild(dom.createTextNode((df.getDemanda())));
		nuevo.appendChild(demanda);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString((df.isSalDetallada()))));
		nuevo.appendChild(sd);

		return nuevo;
	}

	private Element crearElemento(String nombre, String atributo, String val_at, String atributo2, String val_at2) {
		Element resultado = dom.createElement(nombre);
		Attr a = null;
		if (atributo != null) {
			a = dom.createAttribute(atributo);
			a.setValue(val_at);
			resultado.setAttributeNode(a);
		}

		Attr a2 = null;
		if (atributo != null) {
			a2 = dom.createAttribute(atributo2);
			a2.setValue(val_at2);
			resultado.setAttributeNode(a2);
		}

		return resultado;
	}

	private Element escribirDemandas(DatosDemandasCorrida demandas) {
		Element nuevo = dom.createElement("demandas");

		nuevo.appendChild(escribirListaUtilizados(demandas.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(demandas.getAtributosDetallados()));
		nuevo.appendChild(escribirListaDemandas(demandas.getDemandas()));

		return nuevo;
	}

	private Element escribirListaDemandas(Hashtable<String, DatosDemandaCorrida> demandas) {
		Element nuevo = dom.createElement("listaDemanda");

		Set<String> keys = demandas.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirDemanda(demandas.get(it.next())));

		}

		return nuevo;
	}

	private Element escribirDemanda(DatosDemandaCorrida d) {
		Element nuevo = crearElemento("demanda");

		nuevo.appendChild(escribirDatosDemanda(d));

		return nuevo;

	}

	private Element escribirDatosDemanda(DatosDemandaCorrida d) {
		Element nuevo = dom.createElement("datosDemanda");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(d.getNombre()));
		nuevo.appendChild(nombre);

		Element barra = crearElemento("barra");
		barra.appendChild(dom.createTextNode(d.getBarra()));
		nuevo.appendChild(barra);

		Element potActiva = crearElemento("potActiva", "unidad", "MW");
		potActiva.appendChild(escribirVA(d.getPotActiva()));
		nuevo.appendChild(potActiva);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(d.isSalDetallada())));
		nuevo.appendChild(sd);
		return nuevo;
	}

	private Element escribirComercioEnergia(DatosImpoExposCorrida impoExpos) {
		Element nuevo = dom.createElement("comercioEnergia");
		Element impoExposElem = dom.createElement("impoExpos");
		impoExposElem.appendChild(escribirListaUtilizados(impoExpos.getListaUtilizados()));
		impoExposElem.appendChild(escribirListaAtributos(impoExpos.getAtributosDetallados()));
		impoExposElem.appendChild(escribirListaImposExpos(impoExpos.getImpoExpos()));
		nuevo.appendChild(impoExposElem);
		return nuevo;

	}

	private Node escribirListaImposExpos(Hashtable<String, DatosImpoExpoCorrida> impoExpos) {
		Element nuevo = dom.createElement("listaImpoExpos");

		Set<String> keys = impoExpos.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirImpoExpo((impoExpos.get(it.next()))));

		}

		return nuevo;

	}

	private Element escribirImpoExpo(DatosImpoExpoCorrida impo) {
		Element nuevo = dom.createElement("impoExpo");
		nuevo.appendChild(escribirDatosImpoExpo(impo));
		return nuevo;
	}

	private Element escribirDatosImpoExpo(DatosImpoExpoCorrida impo) {
		Element nuevo = dom.createElement("datosParticipante");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(impo.getNombre()));
		nuevo.appendChild(nombre);

		Element propietario = crearElemento("propietario");
		propietario.appendChild(dom.createTextNode(impo.getPropietario()));
		nuevo.appendChild(propietario);

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(impo.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);

		Element barra = crearElemento("barra");
		barra.appendChild(dom.createTextNode(impo.getBarra()));
		nuevo.appendChild(barra);

		Element pais = crearElemento("pais");
		pais.appendChild(dom.createTextNode(impo.getPais()));
		nuevo.appendChild(pais);

		Element tipoImpoExpo = crearElemento("tipoImpoExpo");
		tipoImpoExpo.appendChild(dom.createTextNode(impo.getTipoImpoExpo()));
		nuevo.appendChild(tipoImpoExpo);

		Element op = crearElemento("opCompraVenta");
		op.appendChild(dom.createTextNode(impo.getOperacionCompraVenta()));
		nuevo.appendChild(op);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(impo.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString((impo.isSalDetallada()))));
		nuevo.appendChild(sd);

		Element hmt = crearElemento("hayMinimoTecnico");
		hmt.appendChild(dom.createTextNode(Boolean.toString((impo.isHayMinTec()))));
		nuevo.appendChild(hmt);

		Element mt = crearElemento("minimoTecnico");
		mt.appendChild(escribirEvolucion(impo.getMinTec(), "double"));
		nuevo.appendChild(mt);

		if(impo.getTipoImpoExpo().equals(Constantes.IEALEATFORMUL)){
			Element cmg = crearElemento("cmg");
			cmg.appendChild(escribirVA(impo.getDatCMg()));
			nuevo.appendChild(cmg);
		}

		Element fe = crearElemento("factorEscalamiento");
		fe.appendChild(escribirEvolucion(impo.getFactorEscalamiento(), "double"));
		nuevo.appendChild(fe);

		Element uniforme = crearElemento("uniforme");
		uniforme.appendChild(escribirVA(impo.getDatUniforme()));
		nuevo.appendChild(uniforme);

		Element cb = crearElemento("cantBloques");
		cb.appendChild(dom.createTextNode(Integer.toString(impo.getCantBloques())));
		nuevo.appendChild(cb);

		nuevo.appendChild(escribirListaBloquesImpoExpo(impo));

		return nuevo;
	}

	private Element escribirListaBloquesImpoExpo(DatosImpoExpoCorrida impo) {
		Element nuevo = crearElemento("listaBloques");
		for (int i = 0; i < impo.getCantBloques(); i++) {
			nuevo.appendChild(escribirBloque(impo, i, impo.getTipoImpoExpo()));

		}
		return nuevo;
	}

	private Element escribirBloque(DatosImpoExpoCorrida impo, int i, String tipoImpoExpo) {
		Element nuevo = crearElemento("bloque");

		if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATPRPOT)){
			Element precio = crearElemento("precio");
			precio.appendChild(escribirVA(impo.getDatPrecio().get(i)));
			nuevo.appendChild(precio);

			Element potencia = crearElemento("potencia");
			potencia.appendChild(escribirVA(impo.getDatPotencia().get(i)));
			nuevo.appendChild(potencia);
		}
		if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATFORMUL)){
			Element fpre = crearElemento("fPre");
			fpre.appendChild(escribirEvolucion(impo.getPoliPre().get(i), "polinomio"));
			nuevo.appendChild(fpre);

			Element fpot = crearElemento("fPot");
			fpot.appendChild(escribirEvolucion(impo.getPoliPot().get(i), "polinomio"));
			nuevo.appendChild(fpot);

			Element fdisp = crearElemento("fDisp");
			fdisp.appendChild(escribirEvolucion(impo.getPoliDisp().get(i), "polinomio"));
			nuevo.appendChild(fdisp);
		}
		if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEEVOL)){
			Element potEvol = crearElemento("potEvol");
			potEvol.appendChild(escribirEvolucion(impo.getPotEvol().get(i), "listadouble"));
			nuevo.appendChild(potEvol);

			Element preEvol = crearElemento("preEvol");
			preEvol.appendChild(escribirEvolucion(impo.getPreEvol().get(i), "listadouble"));
			nuevo.appendChild(preEvol);

			Element dispEvol = crearElemento("dispEvol");
			dispEvol.appendChild(escribirEvolucion(impo.getDispEvol().get(i), "listadouble"));
			nuevo.appendChild(dispEvol);
		}

		return nuevo;
	}

	private Element escribirGeneradores(DatosCorrida corrida) {
		Element nuevo = dom.createElement("generadores");
		nuevo.appendChild(escribirTermicos(corrida.getTermicos()));
		nuevo.appendChild(escribirCiclosCombinados(corrida.getCcombinados()));
		nuevo.appendChild(escribirHidraulicos(corrida.getHidraulicos()));
		nuevo.appendChild(escribirEolicos(corrida.getEolicos()));
		nuevo.appendChild(escribirFotovoltaicos(corrida.getFotovoltaicos()));
		nuevo.appendChild(escribirCentralesAcumulacion(corrida.getAcumuladores()));
		return nuevo;
	}

	private Element escribirHidraulicos(DatosHidraulicosCorrida hidraulicos) {
		Element nuevo = dom.createElement("hidraulicos");
		nuevo.appendChild(escribirCompsPorDefecto(hidraulicos.getValoresComportamiento()));
		nuevo.appendChild(escribirListaUtilizados(hidraulicos.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(hidraulicos.getAtribtosDetallados()));
		nuevo.appendChild(escribirListaHidraulicos(hidraulicos.getHidraulicos(), hidraulicos.getOrdenCargaXML()));
		return nuevo;
	}

	private Element escribirListaHidraulicos(Hashtable<String, DatosHidraulicoCorrida> hidraulicos, ArrayList<String> ordenHidraulico) {
		Element nuevo = dom.createElement("listaGHidraulico");

		for (String nombreHidraulico: ordenHidraulico) {
			nuevo.appendChild(escribirHidraulico(hidraulicos.get(nombreHidraulico)));
		}

		return nuevo;
	}

	private Element escribirHidraulico(DatosHidraulicoCorrida datosHidraulicoCorrida) {
		Element nuevo = dom.createElement("gHidraulico");

		nuevo.appendChild(escribirCompsGenerales(datosHidraulicoCorrida.getValoresComportamientos()));
		nuevo.appendChild(escribirDatosHidraulico(datosHidraulicoCorrida));
		if (datosHidraulicoCorrida.getVarsEstado() == null)
			return nuevo;
		if (!datosHidraulicoCorrida.getVarsEstado().isEmpty())
			nuevo.appendChild(escribirVariablesEstado(datosHidraulicoCorrida.getVarsEstado()));
		return nuevo;
	}

	private Element escribirVariablesEstado(Hashtable<String, DatosVariableEstado> varsEstado) {
		Element nuevo = crearElemento("variablesEstado");
		Set<String> keys = varsEstado.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			nuevo.appendChild(escribirVariableEstado(varsEstado.get(it.next())));
		}

		return nuevo;
	}

	private Element escribirVariableEstado(DatosVariableEstado datosVariableEstado) {
		Element nuevo = crearElemento("variableEstado");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(datosVariableEstado.getNombre()));
		nuevo.appendChild(nombre);


		Element ei = crearElemento("estadoInicial", "unidad", datosVariableEstado.getEstadoInicialUnidad());
		ei.appendChild(dom.createTextNode(Double.toString(datosVariableEstado.getEstadoInicial())));
		nuevo.appendChild(ei);

		nuevo.appendChild(escribirEvolucion(datosVariableEstado.getDiscretizacion(), "discretizacion"));

		Element vrs = crearElemento("valorRecursoSuperior");
		vrs.appendChild(escribirEvolucion(datosVariableEstado.getValorRecursoSuperior(), "double"));
		nuevo.appendChild(vrs);

		if (datosVariableEstado.getValorRecursoInferior() != null) {
			Element vri = crearElemento("valorRecursoInferior");
			vri.appendChild(escribirEvolucion(datosVariableEstado.getValorRecursoInferior(), "double"));
			nuevo.appendChild(vri);
		}

		Element discreta = crearElemento("discreta");
		discreta.appendChild(dom.createTextNode((Boolean.toString(datosVariableEstado.isDiscreta()))));
		nuevo.appendChild(discreta);

		Element ordinal = crearElemento("ordinal");
		ordinal.appendChild(dom.createTextNode((Boolean.toString(datosVariableEstado.isOrdinal()))));
		nuevo.appendChild(ordinal);

		Element discretaIncremental = crearElemento("discretaIncremental");
		discretaIncremental.appendChild(dom.createTextNode((Boolean.toString(datosVariableEstado.isDiscretaIncremental()))));
		nuevo.appendChild(discretaIncremental);

		return nuevo;
	}

	private Element escribirVariablesControlDE(Hashtable<String, DatosVariableControlDE> varsControlDE) {
		Element nuevo = crearElemento("variablesControlDE");

		for(String key : varsControlDE.keySet()) {
			nuevo.appendChild(escribirVariableControlDE(varsControlDE.get(key)));
		}

		return nuevo;
	}

	private Element escribirVariableControlDE(DatosVariableControlDE datosVariableControlDE) {
		Element nuevo = crearElemento("variableControlDE");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(datosVariableControlDE.getNombre()));
		nuevo.appendChild(nombre);

		Element periodo = crearElemento("periodo", "undiad", "horas");
		periodo.appendChild(dom.createTextNode(String.valueOf(datosVariableControlDE.getPeriodo())));
		nuevo.appendChild(periodo);

		Element cDC = crearElemento("costoDeControl", "unidad", "USD");
//		cDC.appendChild(escribirEvolucion(datosVariableControlDE.getCostoDeControl(), "arraydouble"));//TODO: descomentar cuando se arregle el bug del cargadorXML
		cDC.appendChild(escribirEvolucion(datosVariableControlDE.getCostoDeControl(), "arraydouble"));//TODO: descomentar cuando se arregle el bug del cargadorXML
		nuevo.appendChild(cDC);

		return nuevo;
	}

	private Element escribirDatosHidraulico(DatosHidraulicoCorrida dh) {
		Element nuevo = dom.createElement("datosParticipante");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(dh.getNombre()));
		nuevo.appendChild(nombre);

		Element propietario = crearElemento("propietario");
		propietario.appendChild(dom.createTextNode(dh.getPropietario()));
		nuevo.appendChild(propietario);

		Element barra = crearElemento("barra");
		barra.appendChild(dom.createTextNode(dh.getBarra()));
		nuevo.appendChild(barra);
 
		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(dh.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		Element factorCompartir = crearElemento("factorCompartir");
		factorCompartir.appendChild(dom.createTextNode(Double.toString(dh.getFactorCompartir().getValor(instanteActual))));
		nuevo.appendChild(factorCompartir);

		Element epsilonCaudalErogadoIteracion = crearElemento("epsilonCaudalErogadoIteracion", "unidad", "m3/s");
		epsilonCaudalErogadoIteracion
				.appendChild(dom.createTextNode(Double.toString(dh.getEpsilonCaudalErogadoIteracion())));
		nuevo.appendChild(epsilonCaudalErogadoIteracion);

		Element lgaa = crearElemento("listaGHAguasArriba");
		lgaa.appendChild(dom.createTextNode(arrayStringXML(dh.getHidraulicosAguasArriba())));
		nuevo.appendChild(lgaa);

		Element gaa = crearElemento("generadorAguasAbajo");
		gaa.appendChild(dom.createTextNode(dh.getHidraulicoAguasAbajo()));
		nuevo.appendChild(gaa);

		Element pMin = crearElemento("potMin");
		pMin.appendChild(escribirEvolucion(dh.getPotMin(), "double"));
		nuevo.appendChild(pMin);

		Element pMax = crearElemento("potMax");
		pMax.appendChild(escribirEvolucion(dh.getPotMax(), "double"));
		nuevo.appendChild(pMax);

		Element rendMin = crearElemento("rendMin");
		rendMin.appendChild(escribirEvolucion(dh.getRendPotMin(), "double"));
		nuevo.appendChild(rendMin);

		Element rendMax = crearElemento("rendMax");
		rendMax.appendChild(escribirEvolucion(dh.getRendPotMax(), "double"));
		nuevo.appendChild(rendMax);

//		Element funcionesPQ = crearElemento("funcionesPQ");
//		funcionesPQ.appendChild(dom.createTextNode(dh.getRutaPQ()));
//		nuevo.appendChild(funcionesPQ);

		nuevo.appendChild(escribirFCotaAguasAbajo(dh.getfCoAA()));

		Element volf = crearElemento("volFijo");
		volf.appendChild(escribirEvolucion(dh.getVolFijo(), "double"));
		nuevo.appendChild(volf);

		Element qm = crearElemento("qTur1Max");
		qm.appendChild(escribirEvolucion(dh.getqTur1Max(), "double"));
		nuevo.appendChild(qm);

		Element smin = crearElemento("saltoMinimo");
		smin.appendChild(dom.createTextNode(Double.toString(dh.getSaltoMin())));
		nuevo.appendChild(smin);

		Element ciaa = crearElemento("cotaInundacionAguasAbajo");
		ciaa.appendChild(dom.createTextNode(Double.toString(dh.getCotaInundacionAguasAbajo())));
		nuevo.appendChild(ciaa);

		Element ciaar = crearElemento("cotaInundacionAguasArriba");
		ciaar.appendChild(dom.createTextNode(Double.toString(dh.getCotaInundacionAguasArriba())));
		nuevo.appendChild(ciaar);

		nuevo.appendChild(escribirFCovo(dh.getfCoVo()));
		nuevo.appendChild(escribirFVoco(dh.getfVoCo()));
		nuevo.appendChild(escribirQEromin(dh.getfQEroMin()));

		Element cefEv = crearElemento("coefEvaporacion");
		cefEv.appendChild(escribirEvolucion(dh.getCoefEvaporacion(), "double"));
		nuevo.appendChild(cefEv);

		nuevo.appendChild(escribirFEvaporacion(dh.getfEvaporacion()));
		nuevo.appendChild(escribirFFiltracion(dh.getfFiltracion()));
		nuevo.appendChild(escribirFQverMax(dh.getfQVerM()));

		Element aporte = crearElemento("aporte", "unidad", "m3/s");
		aporte.appendChild(escribirVA(dh.getAporte()));
		nuevo.appendChild(aporte);

		nuevo.appendChild(escribirDispModulos(dh.getCantModIni(), dh.getDispMedia(), dh.gettMedioArreglo()));

		Element mp = crearElemento("mantProgramado");
		mp.appendChild(escribirEvolucion(dh.getMantProgramado(), "int"));
		nuevo.appendChild(mp);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(dh.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element cv = crearElemento("costoVariable");
		cv.appendChild(escribirEvolucion(dh.getCostoVariable(), "double"));
		nuevo.appendChild(cv);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(dh.isSalDetallada())));
		nuevo.appendChild(sd);

		Element hRE = crearElemento("hayReservaEstrategica");
		hRE.appendChild(dom.createTextNode(Boolean.toString((dh.isHayReservaEstrategica()))));
		nuevo.appendChild(hRE);

		Element vRE = crearElemento("volumenReservaEstrategica");
		vRE.appendChild(escribirEvolucion(dh.getVolReservaEstrategica(), "double"));
		nuevo.appendChild(vRE);

		Element vmRE = crearElemento("valorMinReserva");
		vmRE.appendChild(escribirEvolucion(dh.getValorMinReserva(), "double"));
		nuevo.appendChild(vmRE);

		Element vaeo = crearElemento("valorAplicaEnOptim");
		vaeo.appendChild(dom.createTextNode(Boolean.toString(((dh.isValorAplicaOptim())))));
		nuevo.appendChild(vaeo);



		Element hccmi = crearElemento("hayControldeCotasMinimas");
		hccmi.appendChild(dom.createTextNode(Boolean.toString(((dh.isHayControldeCotasMinimas())))));
		nuevo.appendChild(hccmi);

		Element vcmi = crearElemento("volumenControlMinimo");
		vcmi.appendChild(escribirEvolucion(dh.getVolumenControlMinimo(), "double"));
		nuevo.appendChild(vcmi);

		Element pcmi = crearElemento("penalidadControlMinimo");
		pcmi.appendChild(escribirEvolucion(dh.getPenalidadControlMinimo(), "double"));
		nuevo.appendChild(pcmi);


		Element hccma = crearElemento("hayControldeCotasMaximas");
		hccma.appendChild(dom.createTextNode(Boolean.toString(((dh.isHayControldeCotasMaximas())))));
		nuevo.appendChild(hccma);

		Element vcma = crearElemento("volumenControlMaximo");
		vcma.appendChild(escribirEvolucion(dh.getVolumenControlMaximo(), "double"));
		nuevo.appendChild(vcma);

		Element pcma = crearElemento("penalidadControlMaximo");
		pcma.appendChild(escribirEvolucion(dh.getPenalidadControlMaximo(), "double"));
		nuevo.appendChild(pcma);


		Element vcte = crearElemento("vertimientoConstante");
		vcte.appendChild(dom.createTextNode(Boolean.toString(dh.isVertimientoConstante())));
		nuevo.appendChild(vcte);

		Element hvov = crearElemento("hayVolumenObjetivoVertimiento");
		hvov.appendChild(dom.createTextNode(Boolean.toString(dh.isHayVolObjVert())));
		nuevo.appendChild(hvov);

		Element vov = crearElemento("volumenObjetivoVertimiento");
		vov.appendChild(escribirEvolucion(dh.getVolObjVert(), "double"));
		nuevo.appendChild(vov);

		return nuevo;

	}

	private Element escribirVA(DatosVariableAleatoria va) {
		Element nuevo = crearElemento("variableAleat");

		Element procSimulacion = crearElemento("procSimulacion");
		procSimulacion.appendChild(dom.createTextNode(va.getProcSimulacion()));
		nuevo.appendChild(procSimulacion);

		Element procOptimizacion = crearElemento("procOptimizacion");
		procOptimizacion.appendChild(dom.createTextNode(va.getProcOptimizacion()));
		nuevo.appendChild(procOptimizacion);

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(va.getNombre()));
		nuevo.appendChild(nombre);

		return nuevo;
	}

	private Element escribirFQverMax(Evolucion<DatosPolinomio> getfQVerM) {
		Element nuevo = crearElemento("fQVerMax", "unidad", "m3/s");
		nuevo.appendChild(escribirEvolucion(getfQVerM, "polinomio"));
		return nuevo;
	}

	private Element escribirFFiltracion(Evolucion<DatosPolinomio> getfFiltracion) {
		Element nuevo = crearElemento("fFiltracion");
		nuevo.appendChild(escribirEvolucion(getfFiltracion, "polinomio"));
		return nuevo;
	}

	private Element escribirFEvaporacion(Evolucion<DatosPolinomio> getfEvaporacion) {
		Element nuevo = crearElemento("fEvaporacion");
		nuevo.appendChild(escribirEvolucion(getfEvaporacion, "polinomio"));
		return nuevo;
		
	}

	private Element escribirQEromin(Evolucion<DatosPolinomio> getfQEroMin) {
		Element nuevo = dom.createElement("fQEroMin");
		nuevo.appendChild(escribirEvolucion(getfQEroMin, "polinomio"));
		return nuevo;
	}

	private Element escribirFVoco(Evolucion<DatosPolinomio> getfVoCo) {
		Element nuevo = dom.createElement("fVoCo");
		nuevo.appendChild(escribirEvolucion(getfVoCo, "polinomio"));
		return nuevo;
	}

	private Element escribirFCovo(Evolucion<DatosPolinomio> getfCoVo) {
		Element nuevo = dom.createElement("fCoVo");
		nuevo.appendChild(escribirEvolucion(getfCoVo, "polinomio"));
		return nuevo;
	}

	private Element escribirFCotaAguasAbajo(Evolucion<DatosPolinomio> getfCoAA) {
		Element nuevo = dom.createElement("fCotaAguasAbajo");
		nuevo.appendChild(escribirEvolucion(getfCoAA, "polinomio"));
		return nuevo;
	}

	private Element escribirCentralesAcumulacion(DatosAcumuladoresCorrida acumuladores) {
		Element nuevo = dom.createElement("centralesAcumulacion");
		nuevo.appendChild(escribirCompsPorDefecto(acumuladores.getValoresComportamiento()));
		nuevo.appendChild(escribirListaUtilizados(acumuladores.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(acumuladores.getAtributosDetallados()));
		nuevo.appendChild(escribirListaAcumuladores(acumuladores.getAcumuladores(), acumuladores.getOrdenCargaXML()));
		return nuevo;

	}

	private Node escribirListaAcumuladores(Hashtable<String, DatosAcumuladorCorrida> acumuladores, ArrayList<String> ordenAcumuladores) {
		Element nuevo = dom.createElement("listaCentralesAcumulacion");

		for (String nombreAcumulador: ordenAcumuladores){
			nuevo.appendChild(escribirAcumulador(acumuladores.get(nombreAcumulador)));

		}

		return nuevo;
	}

	private Element escribirAcumulador(DatosAcumuladorCorrida da) {
		Element nuevo = crearElemento("acumulador");

		nuevo.appendChild(escribirCompsGenerales(da.getValoresComportamientos()));
		nuevo.appendChild(escribirDatosAcumulador(da));

		return nuevo;

	}

	private Node escribirDatosAcumulador(DatosAcumuladorCorrida da) {
		Element nuevo = dom.createElement("datosParticipante");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(da.getNombre()));
		nuevo.appendChild(nombre);

		Element barra = crearElemento("barra");
		barra.appendChild(dom.createTextNode(da.getBarra()));
		nuevo.appendChild(barra);

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(da.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);

		Element factorUso = crearElemento("factorUso");
		factorUso.appendChild(escribirEvolucion(da.getFactorUso(), "double"));
		nuevo.appendChild(factorUso);

		Element pMin = crearElemento("potMin");
		pMin.appendChild(escribirEvolucion(da.getPotMin(), "double"));
		nuevo.appendChild(pMin);

		Element pMax = crearElemento("potMax");
		pMax.appendChild(escribirEvolucion(da.getPotMax(), "double"));
		nuevo.appendChild(pMax);

		Element pAlMin = crearElemento("potAlmacMin");
		pAlMin.appendChild(escribirEvolucion(da.getPotAlmacenadaMin(), "double"));
		nuevo.appendChild(pAlMin);

		Element pAlMax = crearElemento("potAlmacMax");
		pAlMax.appendChild(escribirEvolucion(da.getPotAlmacenadaMax(), "double"));
		nuevo.appendChild(pAlMax);

		Element eAlMax = crearElemento("energAlmacMax");
		eAlMax.appendChild(escribirEvolucion(da.getEnergAlmacMax(), "double"));
		nuevo.appendChild(eAlMax);

		Element rendIny = crearElemento("rendIny");
		rendIny.appendChild(escribirEvolucion(da.getRendIny(), "double"));
		nuevo.appendChild(rendIny);

		Element rendAlmac = crearElemento("rendAlmac");
		rendAlmac.appendChild(escribirEvolucion(da.getRendAlmac(), "double"));
		nuevo.appendChild(rendAlmac);

		Element eIniPaso = crearElemento("energIniPaso");
		eIniPaso.appendChild(escribirEvolucion(da.getEnergIniPaso(), "double"));
		nuevo.appendChild(eIniPaso);

		nuevo.appendChild(escribirDispModulos(da.getCantModIni(), da.getDispMedia(), da.gettMedioArreglo()));

		Element mprog = crearElemento("mantProgramado");
		mprog.appendChild(escribirEvolucion(da.getMantProgramado(), "int"));
		nuevo.appendChild(mprog);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(da.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element cv = crearElemento("costoVariable");
		cv.appendChild(escribirEvolucion(da.getCostoVariable(), "double"));
		nuevo.appendChild(cv);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(da.isSalDetallada())));
		nuevo.appendChild(sd);

		Element hpotobli = crearElemento("hayPotObligatoria");
		hpotobli.appendChild(escribirEvolucion(da.getHayPotObligatoria(), "boolean"));
		nuevo.appendChild(hpotobli);

		Element potObli = crearElemento("PotObligatoria");
		potObli.appendChild(dom.createTextNode(arrayStringXML(Arrays.toString((da.getPotOblig())))));
		nuevo.appendChild(potObli);

		Element cfpo = crearElemento("CostoFallaPObligatoria");
		cfpo.appendChild(escribirEvolucion(da.getCostoFallaPotOblig(), "double"));
		nuevo.appendChild(cfpo);

		if (da.getVarsEstado() == null)
			return nuevo;
		if (!da.getVarsEstado().isEmpty())
			nuevo.appendChild(escribirVariablesEstado(da.getVarsEstado()));

		return nuevo;
	}

	private Element escribirFotovoltaicos(DatosFotovoltaicosCorrida fotovoltaicos) {
		Element nuevo = dom.createElement("fotovoltaicos");
		nuevo.appendChild(escribirListaUtilizados(fotovoltaicos.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(fotovoltaicos.getAtributosDetallados()));
		nuevo.appendChild(escribirListaFotovoltaicos(fotovoltaicos.getFotovoltaicos(), fotovoltaicos.getOrdenCargaXML()));
		return nuevo;
	}

	private Element escribirListaFotovoltaicos(Hashtable<String, DatosFotovoltaicoCorrida> fotovoltaicos, ArrayList<String> ordenFotovoltaicos) {
		Element nuevo = dom.createElement("listaGFotovoltaico");

		for (String nombreFotovoltaico: ordenFotovoltaicos) {
			nuevo.appendChild(escribirFotovoltaico(fotovoltaicos.get(nombreFotovoltaico)));
		}

		return nuevo;
	}

	private Element escribirFotovoltaico(DatosFotovoltaicoCorrida df) {
		Element nuevo = crearElemento("gFotovoltaico");

		nuevo.appendChild(escribirDatosFotovoltaico(df));

		return nuevo;
	}

	private Element escribirDatosFotovoltaico(DatosFotovoltaicoCorrida df) {
		Element nuevo = dom.createElement("datosParticipante");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(df.getNombre()));
		nuevo.appendChild(nombre);

		Element propietario = crearElemento("propietario");
		propietario.appendChild(dom.createTextNode(df.getPropietario()));
		nuevo.appendChild(propietario);

		Element barra = crearElemento("barra");
		barra.appendChild(dom.createTextNode(df.getBarra()));
		nuevo.appendChild(barra);

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(df.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);

		Element pMin = crearElemento("potMin");
		pMin.appendChild(escribirEvolucion(df.getPotMin(), "double"));
		nuevo.appendChild(pMin);

		Element pMax = crearElemento("potMax");
		pMax.appendChild(escribirEvolucion(df.getPotMax(), "double"));
		nuevo.appendChild(pMax);

		nuevo.appendChild(escribirFactor(df.getFactor()));

		nuevo.appendChild(escribirDispModulos(df.getCantModIni(), df.getDispMedia(), df.gettMedioArreglo()));

		Element mprog = crearElemento("mantProgramado");
		mprog.appendChild(escribirEvolucion(df.getMantProgramado(), "int"));
		nuevo.appendChild(mprog);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(df.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element cv = crearElemento("costoVariable");
		cv.appendChild(escribirEvolucion(df.getCostoVariable(), "double"));
		nuevo.appendChild(cv);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(df.isSalDetallada())));
		nuevo.appendChild(sd);
		return nuevo;
	}

	private Element escribirEolicos(DatosEolicosCorrida eolicos) {
		Element nuevo = dom.createElement("eolicos");
		nuevo.appendChild(escribirListaUtilizados(eolicos.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(eolicos.getAtributosDetallados()));
		nuevo.appendChild(escribirListaEolicos(eolicos.getEolicos(), eolicos.getOrdenCargaXML()));
		return nuevo;
	}

	private Node escribirListaEolicos(Hashtable<String, DatosEolicoCorrida> eolicos, ArrayList<String> ordenEolicos) {
		Element nuevo = dom.createElement("listaGEolico");

		for (String nombreEolico: ordenEolicos) {
			nuevo.appendChild(escribirEolico(eolicos.get(nombreEolico)));
		}
		return nuevo;
	}

	private Element escribirEolico(DatosEolicoCorrida de) {
		Element nuevo = crearElemento("gEolico");

		nuevo.appendChild(escribirDatosEolico(de));

		return nuevo;
	}

	private Element escribirDatosEolico(DatosEolicoCorrida de) {
		Element nuevo = dom.createElement("datosParticipante");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(de.getNombre()));
		nuevo.appendChild(nombre);

		Element propietario = crearElemento("propietario");
		propietario.appendChild(dom.createTextNode(de.getPropietario()));
		nuevo.appendChild(propietario);

		Element barra = crearElemento("barra");
		barra.appendChild(dom.createTextNode(de.getBarra()));
		nuevo.appendChild(barra);

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(de.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);

		Element pMin = crearElemento("potMin");
		pMin.appendChild(escribirEvolucion(de.getPotMin(), "double"));
		nuevo.appendChild(pMin);

		Element pMax = crearElemento("potMax");
		pMax.appendChild(escribirEvolucion(de.getPotMax(), "double"));
		nuevo.appendChild(pMax);

		nuevo.appendChild(escribirFactor(de.getFactor()));

		nuevo.appendChild(escribirDispModulos(de.getCantModIni(), de.getDispMedia(), de.gettMedioArreglo()));

		Element mprog = crearElemento("mantProgramado");
		mprog.appendChild(escribirEvolucion(de.getMantProgramado(), "int"));
		nuevo.appendChild(mprog);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(de.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element cv = crearElemento("costoVariable");
		cv.appendChild(escribirEvolucion(de.getCostoVariable(), "double"));
		nuevo.appendChild(cv);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(de.isSalDetallada())));
		nuevo.appendChild(sd);
		return nuevo;
	}

	private Node escribirFactor(DatosVariableAleatoria factor) {
		Element nuevo = crearElemento("factor");
		nuevo.appendChild(escribirVA(factor));
		return nuevo;
	}

	private Element escribirTermicos(DatosTermicosCorrida termicos) {
		Element nuevo = dom.createElement("termicos");

		nuevo.appendChild(escribirCompsPorDefecto(termicos.getValoresComportamiento()));
		nuevo.appendChild(escribirListaUtilizados(termicos.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(termicos.getAtribtosDetallados()));
		nuevo.appendChild(escribirListaTermicos(termicos.getTermicos(), termicos.getOrdenCargaXML()));

		return nuevo;

	}

	private Element escribirListaTermicos(Hashtable<String, DatosTermicoCorrida> termicos, ArrayList<String> ordenTermicos) {
		Element nuevo = dom.createElement("listaGTermico");

		Set<String> keys = termicos.keySet();
		Iterator<String> it = keys.iterator();


		for (String nombreTermico: ordenTermicos) {
			nuevo.appendChild(escribirTermico(termicos.get(nombreTermico)));
		}
		return nuevo;
	}

	private Element escribirListaAtributos(ArrayList<String> atrs) {
		Element nuevo = dom.createElement("listaAtributos");

		nuevo.appendChild(dom.createTextNode(arrayStringXML(atrs)));

		return nuevo;

	}

	private Element escribirListaUtilizados(ArrayList<String> listaUtilizados) {
		Element nuevo = dom.createElement("listaUtilizados");
		nuevo.appendChild(dom.createTextNode(arrayStringXML(listaUtilizados)));

		return nuevo;
	}

	private Element escribirCompsPorDefecto(Hashtable<String, Evolucion<String>> valoresComportamientos) {
		Element nuevo = dom.createElement("compsPorDefecto");

		Set<String> keys = valoresComportamientos.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			String clave = it.next();
			Element comp = dom.createElement(clave);
			comp.appendChild(escribirEvolucion(valoresComportamientos.get(clave), "string"));
			nuevo.appendChild(comp);
		}

		return nuevo;
	}

	private Element escribirTermico(DatosTermicoCorrida dt) {
		Element nuevo = dom.createElement("gTermico");

		nuevo.appendChild(escribirCompsGenerales(dt.getValoresComportamientos()));
		nuevo.appendChild(escribirDatosTermico(dt));

		return nuevo;
	}

	private Node escribirDatosTermico(DatosTermicoCorrida dt) {
		Element nuevo = dom.createElement("datosParticipante");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(dt.getNombre()));
		nuevo.appendChild(nombre);

		Element propietario = crearElemento("propietario");
		propietario.appendChild(dom.createTextNode(dt.getPropietario()));
		nuevo.appendChild(propietario);

		Element barra = crearElemento("barra");
		barra.appendChild(dom.createTextNode(dt.getBarra()));
		nuevo.appendChild(barra);

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(dt.getCantModInst(), "int"));
		nuevo.appendChild(cantModInst);

		Pair<ArrayList<String>, ArrayList<String>> barrcombs = dameListasStringHash(dt.getCombustiblesBarras());

		Element lCom = crearElemento("listaCombustibles");
		lCom.appendChild(dom.createTextNode(arrayStringXML(barrcombs.first)));
		nuevo.appendChild(lCom);

		Element lBarr = crearElemento("listaBarrasComb");
		lBarr.appendChild(dom.createTextNode(arrayStringXML(barrcombs.second)));
		nuevo.appendChild(lBarr);

		Element pMin = crearElemento("potMin");
		pMin.appendChild(escribirEvolucion(dt.getPotMin(), "double"));
		nuevo.appendChild(pMin);

		Element pMax = crearElemento("potMax");
		pMax.appendChild(escribirEvolucion(dt.getPotMax(), "double"));
		nuevo.appendChild(pMax);

		Element rendPotMin = crearElemento("rendPotMin");
		for (Element e :dameListaEvoluciones(dt.getRendimientosPotMin())) {
			rendPotMin.appendChild(e);
		}
		nuevo.appendChild(rendPotMin);

		Element rendPotMax = crearElemento("rendPotMax");
		for (Element e :dameListaEvoluciones(dt.getRendimientosPotMax())) {
			rendPotMax.appendChild(e);
		}
		nuevo.appendChild(rendPotMax);

		Element flexibilidadMin = crearElemento("flexibilidadMin");
		flexibilidadMin.appendChild(dom.createTextNode(dt.getFlexibilidadMin()));
		nuevo.appendChild(flexibilidadMin);

		nuevo.appendChild(escribirDispModulos(dt.getCantModIni(), dt.getDispMedia(), dt.gettMedioArreglo()));

		Element mprog = crearElemento("mantProgramado");
		mprog.appendChild(escribirEvolucion(dt.getMantProgramado(), "int"));
		nuevo.appendChild(mprog);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(dt.getCostoFijo(), "double"));
		nuevo.appendChild(cf);

		Element cv = crearElemento("costoVariable");
		cv.appendChild(escribirEvolucion(dt.getCostoVariable(), "double"));
		nuevo.appendChild(cv);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(dt.isSalDetallada())));
		nuevo.appendChild(sd);

		return nuevo;
	}

	private Element escribirCiclosCombinados(DatosCiclosCombinadosCorrida datosCiclosCombinadosCorrida) {
		Element nuevo = dom.createElement("ciclosCombinados");

		nuevo.appendChild(escribirCompsPorDefecto(datosCiclosCombinadosCorrida.getValoresComportamiento()));
		nuevo.appendChild(escribirListaUtilizados(datosCiclosCombinadosCorrida.getListaUtilizados()));
		nuevo.appendChild(escribirListaAtributos(datosCiclosCombinadosCorrida.getAtribtosDetallados()));
		nuevo.appendChild(escribirListaCiclosCombinados(datosCiclosCombinadosCorrida.getCcombinados(), datosCiclosCombinadosCorrida.getOrdenCargaXML()));

		return nuevo;

	}

	private Element escribirListaCiclosCombinados(Hashtable<String, DatosCicloCombinadoCorrida> ccombinados, ArrayList<String> ordenCiclosCombinados) {
		Element nuevo = dom.createElement("listaCCombinado");

		for (String nombreCComb : ordenCiclosCombinados){
			nuevo.appendChild(escribirCicloCombinado(ccombinados.get(nombreCComb)));

		}


		return nuevo;
	}

	private Element escribirCicloCombinado(DatosCicloCombinadoCorrida dt) {
		Element nuevo = dom.createElement("cicloCombinado");

		nuevo.appendChild(escribirCompsGenerales(dt.getValoresComportamientos()));
		nuevo.appendChild(escribirDatosCicloCombinado(dt));

		return nuevo;
	}

	private Node escribirDatosCicloCombinado(DatosCicloCombinadoCorrida dcc) {
		Element nuevo = dom.createElement("datosParticipante");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(dcc.getNombre()));
		nuevo.appendChild(nombre);

		Element propietario = crearElemento("propietario");
		propietario.appendChild(dom.createTextNode(dcc.getPropietario()));
		nuevo.appendChild(propietario);

		Element barra = crearElemento("barra");
		barra.appendChild(dom.createTextNode(dcc.getBarra()));
		nuevo.appendChild(barra);

		Element costoACA = crearElemento("costoArranque1TGCicloAbierto");
		costoACA.appendChild(escribirEvolucion(dcc.getCostoArranque1TGCicloAbierto(), "double"));
		nuevo.appendChild(costoACA);

		Element costoACC = crearElemento("costoArranque1TGCicloCombinado");
		costoACC.appendChild(escribirEvolucion(dcc.getCostoArranque1TGCicloCombinado(), "double"));
		nuevo.appendChild(costoACC);

		Element pMax1CC = crearElemento("potMax1CV");
		pMax1CC.appendChild(escribirEvolucion(dcc.getPotMax1CV(), "double"));
		nuevo.appendChild(pMax1CC);

		Pair<ArrayList<String>, ArrayList<String>> barrcombs = dameListasStringHash(dcc.getBarrasCombustible());

		Element lCom = crearElemento("listaCombustibles");
		lCom.appendChild(dom.createTextNode(arrayStringXML(barrcombs.first)));
		nuevo.appendChild(lCom);

		Element lBarr = crearElemento("listaBarrasComb");
		lBarr.appendChild(dom.createTextNode(arrayStringXML(barrcombs.second)));
		nuevo.appendChild(lBarr);

		Element posiblesArr = crearElemento("posiblesArranques");
		StringBuilder escStr = new StringBuilder();
		for(Pair<Double,Double> par : dcc.getPosiblesArranques()){
			escStr.append("(").append(par.first).append(";").append(par.second).append(")").append(",");
		}
		posiblesArr.appendChild(dom.createTextNode(escStr.substring(0,escStr.toString().length()-1)));
		nuevo.appendChild(posiblesArr);

		Element potRampa1CC = crearElemento("potRampa1CC");
		escStr = new StringBuilder();
		for(Double num : dcc.getPotRampaArranque()){
			escStr.append(num).append(",");
		}
		potRampa1CC.appendChild(dom.createTextNode(escStr.substring(0,escStr.toString().length()-1)));
		nuevo.appendChild(potRampa1CC);

		Element posiblesP = crearElemento("posiblesParadas");
		escStr = new StringBuilder();
		for(Double num: dcc.getPosiblesParadas()){
			escStr.append(num).append(",");
		}
		posiblesP.appendChild(dom.createTextNode(escStr.substring(0,escStr.toString().length()-1)));
		nuevo.appendChild(posiblesP);

		Element sd = crearElemento("salidaDetallada");
		sd.appendChild(dom.createTextNode(Boolean.toString(dcc.isSalDetallada())));
		nuevo.appendChild(sd);


		//Turbina a Gas
		Element turbinaG = crearElemento("turbinasAGas");

		Element cantModInst = crearElemento("cantModInst");
		cantModInst.appendChild(escribirEvolucion(dcc.getDatosTGs().getCantModInst(), "int"));
		turbinaG.appendChild(cantModInst);

		Element pMin = crearElemento("potMin");
		pMin.appendChild(escribirEvolucion(dcc.getDatosTGs().getPotMin(), "double"));
		turbinaG.appendChild(pMin);

		Element pMax = crearElemento("potMax");
		pMax.appendChild(escribirEvolucion(dcc.getDatosTGs().getPotMax(), "double"));
		turbinaG.appendChild(pMax);


		Element rendPotMinTG = crearElemento("rendPotMin");
		for (Element e :dameListaEvoluciones(dcc.getDatosTGs().getRendimientosPotMin())) {
			rendPotMinTG.appendChild(e);
		}
		turbinaG.appendChild(rendPotMinTG);

		Element rendPotMaxTG = crearElemento("rendPotMax");
		for (Element e :dameListaEvoluciones(dcc.getDatosTGs().getRendimientosPotMax())) {
			rendPotMaxTG.appendChild(e);
		}
		turbinaG.appendChild(rendPotMaxTG);

		turbinaG.appendChild(escribirDispModulos(dcc.getDatosTGs().getCantModIni(), dcc.getDatosTGs().getDispMedia(), dcc.getDatosTGs().gettMedioArreglo()));

		Element mprog = crearElemento("mantProgramado");
		mprog.appendChild(escribirEvolucion(dcc.getDatosTGs().getMantProgramado(), "int"));
		turbinaG.appendChild(mprog);

		Element cf = crearElemento("costoFijo");
		cf.appendChild(escribirEvolucion(dcc.getDatosTGs().getCostoFijo(), "double"));
		turbinaG.appendChild(cf);

		Element cv = crearElemento("costoVariable");
		cv.appendChild(escribirEvolucion(dcc.getDatosTGs().getCostoVariable(), "double"));
		turbinaG.appendChild(cv);

		nuevo.appendChild(turbinaG);

		//Ciclo de vapor
		Element cicloV = crearElemento("ciclosVapor");

		Element cantModInstCV = crearElemento("cantModInst");
		cantModInstCV.appendChild(escribirEvolucion(dcc.getDatosCCs().getCantModInst(), "int"));
		cicloV.appendChild(cantModInstCV);


		Element pMinCV = crearElemento("potMin");
		pMinCV.appendChild(escribirEvolucion(dcc.getDatosCCs().getPotMin(), "double"));
		cicloV.appendChild(pMinCV);

		Element pMaxCV = crearElemento("potMax");
		pMaxCV.appendChild(escribirEvolucion(dcc.getDatosCCs().getPotMax(), "double"));
		cicloV.appendChild(pMaxCV);

		Element rendPotMinCC = crearElemento("rendPotMin");
		for (Element e :dameListaEvoluciones(dcc.getDatosCCs().getRendimientosPotMin())) {
			rendPotMinCC.appendChild(e);
		}
		cicloV.appendChild(rendPotMinCC);

		Element rendPotMaxCC = crearElemento("rendPotMax");
		for (Element e :dameListaEvoluciones(dcc.getDatosCCs().getRendimientosPotMax())) {
			rendPotMaxCC.appendChild(e);
		}
		cicloV.appendChild(rendPotMaxCC);

		cicloV.appendChild(escribirDispModulos(dcc.getDatosCCs().getCantModIni(), dcc.getDatosCCs().getDispMedia(), dcc.getDatosCCs().gettMedioArreglo()));

		Element mprogCV = crearElemento("mantProgramado");
		mprogCV.appendChild(escribirEvolucion(dcc.getDatosCCs().getMantProgramado(), "int"));
		cicloV.appendChild(mprogCV);

		Element cfCV = crearElemento("costoFijo");
		cfCV.appendChild(escribirEvolucion(dcc.getDatosCCs().getCostoFijo(), "double"));
		cicloV.appendChild(cfCV);

		Element cvCV = crearElemento("costoVariable");
		cvCV.appendChild(escribirEvolucion(dcc.getDatosCCs().getCostoVariable(), "double"));
		cicloV.appendChild(cvCV);

		nuevo.appendChild(cicloV);

		return nuevo;
	}

	private Node escribirDispModulos(Integer cantModIni, Evolucion<Double> dispMedia,
			Evolucion<Double> gettMedioArreglo) {
		Element nuevo = crearElemento("dispModulos", "tipo", "exponencial");

		Element cmi = crearElemento("cantModIni");
		cmi.appendChild(dom.createTextNode(Integer.toString(cantModIni)));
		nuevo.appendChild(cmi);

		Element dm = crearElemento("dispMedia");
		dm.appendChild(escribirEvolucion(dispMedia, "double"));
		nuevo.appendChild(dm);

		Element tma = crearElemento("tMedioArreglo");
		tma.appendChild(escribirEvolucion(gettMedioArreglo, "double"));
		tma.setAttribute("unidad", "dias");
		nuevo.appendChild(tma);
		return nuevo;
	}

	private Pair<ArrayList<String>, ArrayList<String>> dameListasStringHash(Hashtable<String, String> mapa) {
		Iterator<String> it = mapa.keySet().iterator();
		ArrayList<String> claves = new ArrayList<String>();
		ArrayList<String> valores = new ArrayList<String>();
		String clave = "";
		while (it.hasNext()) {
			clave = it.next();
			claves.add(clave);
			valores.add(mapa.get(clave));

		}
		return new Pair<ArrayList<String>, ArrayList<String>>(claves, valores);
	}

	private ArrayList<Element> dameListaEvoluciones(Hashtable<String, Evolucion<Double>> mapa) {
		Iterator<String> it = mapa.keySet().iterator();
		ArrayList<Element> valores = new ArrayList<Element>();
		String clave = "";
		while (it.hasNext()) {
			clave = it.next();
			valores.add(escribirEvolucion(mapa.get(clave), "double" ));

		}
		return valores;
	}
	private String arrayStringXML(ArrayList<String> lista) {
		return arrayStringXML(lista.toString());
	}

	public static String arrayDoubleXML(ArrayList<Double> lista) {
		String resu = "";
		for (Double d : lista) {
//			resu += d.toString() + ",";
			resu += d.toString() + " ";
		}

		return resu.substring(0, resu.length() - 1);
	}

	private Element escribirCompsGenerales(Hashtable<String, Evolucion<String>> valoresComportamientos) {
		Element nuevo = dom.createElement("compsGenerales");

		Set<String> keys = valoresComportamientos.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			String clave = it.next();
			Element comp = dom.createElement(clave);
			comp.appendChild(escribirEvolucion(valoresComportamientos.get(clave), "string"));
			nuevo.appendChild(comp);
		}

		return nuevo;
	}

	private Element escribirProcesosEstocasticos(Hashtable<String, DatosProcesoEstocastico> procesosEstocasticos) {
		// TODO Auto-generated method stub
		Element nuevo = dom.createElement("procesosEstocasticos");

		Set<String> keys = procesosEstocasticos.keySet();
		Iterator<String> keyit = keys.iterator();

		while (keyit.hasNext()) {
			nuevo.appendChild(escribirProcesoEstocastico(procesosEstocasticos.get(keyit.next())));
		}

		return nuevo;
	}

	private Element escribirProcesoEstocastico(DatosProcesoEstocastico dp) {
		Element nuevo = dom.createElement("pEstocastico");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(dp.getNombre()));
		nuevo.appendChild(nombre);

		Element tipo = crearElemento("tipo");
		tipo.appendChild(dom.createTextNode(dp.getTipo()));
		nuevo.appendChild(tipo);

		Element tipoSoporte = crearElemento("tipoSoporte");
		tipoSoporte.appendChild(dom.createTextNode(dp.getTipoSoporte()));
		nuevo.appendChild(tipoSoporte);

//		Element ruta = crearElemento("ruta");
//		ruta.appendChild(dom.createTextNode(dp.getRuta()));
//		nuevo.appendChild(ruta);

		Element discretoExhaustivo = crearElemento("discretoExhaustivo");
		discretoExhaustivo.appendChild(dom.createTextNode(dp.getDiscretoExhaustivo().toString()));
		nuevo.appendChild(discretoExhaustivo);

		Element muestreado = crearElemento("muestreado");
		muestreado.appendChild(dom.createTextNode(dp.getMuestreado().toString()));
		nuevo.appendChild(muestreado);

		Element estadosIniciales = crearElemento("estadosIniciales");
		Hashtable<String, Double> est = dp.getEstadosIniciales();
		Set<String> keys = est.keySet();
		Iterator<String> it = keys.iterator();
		String estadosI = "";
		boolean primero = true;
		while (it.hasNext()) {
			String pref = ",(";
			if (primero) {
				pref = "(";
				primero = false;
			}
			String clave = it.next();
			estadosI += pref + clave + ";" + est.get(clave) + ")";
		}
		if (!estadosI.equalsIgnoreCase("")) {
			estadosIniciales.appendChild(dom.createTextNode(estadosI));
			nuevo.appendChild(estadosIniciales);
		}

		return nuevo;
	}

	private Element escribirParametrosGenerales(DatosCorrida corrida) {
		Element nuevo = dom.createElement("parametrosGenerales");

		Element nombre = crearElemento("nombre");
		nombre.appendChild(dom.createTextNode(corrida.getNombre()));
		nuevo.appendChild(nombre);

		Element descripcion = crearElemento("descripcion");
		descripcion.appendChild(dom.createTextNode(corrida.getDescripcion()));
		nuevo.appendChild(descripcion);

		Element postizacion = crearElemento("postizacion", "tipo", corrida.getTipoPostizacion());
		postizacion.appendChild(dom.createTextNode(""));
		nuevo.appendChild(postizacion);

		Element valpostizacion = crearElemento("valpostizacion", "tipo", corrida.getTipoValpostizacion());
		valpostizacion.appendChild(dom.createTextNode(corrida.getValPostizacion()));
		nuevo.appendChild(valpostizacion);

		Element tipoSimulacion = crearElemento("tipoSimulacion");
		tipoSimulacion.appendChild(dom.createTextNode(corrida.getTipoSimulacion()));
		nuevo.appendChild(tipoSimulacion);

		Element inicioCorrida = crearElemento("inicioCorrida");
		inicioCorrida.appendChild(dom.createTextNode(corrida.getInicioCorrida()));
		nuevo.appendChild(inicioCorrida);

		Element finCorrida = crearElemento("finCorrida");
		finCorrida.appendChild(dom.createTextNode(corrida.getFinCorrida()));
		nuevo.appendChild(finCorrida);

		Element cantEscenarios = crearElemento("cantEscenarios");
		cantEscenarios.appendChild(dom.createTextNode(corrida.getCantEscenarios().toString()));
		nuevo.appendChild(cantEscenarios);

		Element tasaAnual = crearElemento("tasaAnual");
		tasaAnual.appendChild(dom.createTextNode(corrida.getTasa().toString()));
		nuevo.appendChild(tasaAnual);

		Element topeSpot = crearElemento("topeSpot");
		topeSpot.appendChild(dom.createTextNode(String.valueOf(corrida.getTopeSpot())));
		nuevo.appendChild(topeSpot);

		Element despachoSinExportacion = crearElemento("despachoSinExportacion");

		Element despSinExp = crearElemento("despSinExp");
		despSinExp.appendChild(dom.createTextNode(String.valueOf(corrida.isDespSinExp())));

		Element iteracionSinExp = crearElemento("iteracionSinExp");
		iteracionSinExp.appendChild(dom.createTextNode(String.valueOf(corrida.getIteracionSinExp())));

		Element paisesACortar = crearElemento("paisesACortar");
		paisesACortar.appendChild(dom.createTextNode(UtilStrings.arrayStringAtexto(corrida.getPaisesACortar(), ",")));

		despachoSinExportacion.appendChild(despSinExp);
		despachoSinExportacion.appendChild(iteracionSinExp);
		despachoSinExportacion.appendChild(paisesACortar);

		nuevo.appendChild(despachoSinExportacion);


		nuevo.appendChild(escribirLineaDeTiempo(corrida.getLineaTiempo()));

		nuevo.appendChild(escribirCompsGlobales(corrida.getValoresComportamientoGlobal()));

		Element rutasSalidas = crearElemento("rutaSalidas");
		rutasSalidas.appendChild(dom.createTextNode(corrida.getRutaSals()));
		nuevo.appendChild(rutasSalidas);

		nuevo.appendChild(escribirIteraciones(corrida.getDatosIteraciones()));

		Element semilla = crearElemento("semilla");
		semilla.appendChild(dom.createTextNode(Integer.toString(corrida.getSemilla().intValue())));
		nuevo.appendChild(semilla);

		nuevo.appendChild(escribirCantSorteosMont(corrida));

		nuevo.appendChild(escribirSalidaTexto(corrida));
		nuevo.appendChild(escribirSalidaSimDet(corrida));
		nuevo.appendChild(escribirSalidaOpt(corrida));
		return nuevo;

	}

	private Element escribirSalidaOpt(DatosCorrida corrida) {
		Element nuevo = dom.createElement("salidaOpt");

		String salOpt = Boolean.toString(corrida.getDatosParamSalidaOpt().isSalOpt());
		String pasoIni = Integer.toString(corrida.getDatosParamSalidaOpt().getPasoIni());
		String pasoFin = Integer.toString(corrida.getDatosParamSalidaOpt().getPasoFin());
		String estadoIni = arrayStringXML(Arrays.toString(corrida.getDatosParamSalidaOpt().getEstadoIni()));
		String estadoFin = arrayStringXML(Arrays.toString(corrida.getDatosParamSalidaOpt().getEstadoFin()));
		String sortIni = Integer.toString(corrida.getDatosParamSalidaOpt().getSortIni());
		String sortFin = Integer.toString(corrida.getDatosParamSalidaOpt().getSortFin());

		Element er = crearElemento("salOpt");
		er.appendChild(dom.createTextNode(salOpt));
		nuevo.appendChild(er);

		Element ec = crearElemento("pasoIni");
		ec.appendChild(dom.createTextNode(pasoIni));
		nuevo.appendChild(ec);

		Element pp = crearElemento("pasoFin");
		pp.appendChild(dom.createTextNode(pasoFin));
		nuevo.appendChild(pp);

		Element ip = crearElemento("estadoIni");
		ip.appendChild(dom.createTextNode(estadoIni));
		nuevo.appendChild(ip);

		Element cr = crearElemento("estadoFin");
		cr.appendChild(dom.createTextNode(estadoFin));
		nuevo.appendChild(cr);

		Element sf = crearElemento("sortIni");
		sf.appendChild(dom.createTextNode(sortIni));
		nuevo.appendChild(sf);

		Element sfss = crearElemento("sortFin");
		sfss.appendChild(dom.createTextNode(sortFin));
		nuevo.appendChild(sfss);
		return nuevo;

	}

	private Element escribirSalidaTexto(DatosCorrida corrida) {
		Element nuevo = dom.createElement("salidaTexto");

		int[][] param = corrida.getDatosParamSalida().getParam();
		String enerResumen = intBoolString(param[Constantes.PARAMSAL_RESUMEN]);
		String enercron = intBoolString(param[Constantes.PARAMSAL_ENERCRON]);
		String potPoste = intBoolString(param[Constantes.PARAMSAL_POT]);
		//String indPot = intBoolString(param[Constantes.PARAMSAL_IND_POT]);
		String costoResumen = intBoolString(param[Constantes.PARAMSAL_COSTO_RESUMEN]);
		String costocron = intBoolString(param[Constantes.PARAMSAL_COSTO_CRON]);
		String costoPoste = intBoolString(param[Constantes.PARAMSAL_COSTO_POSTE]);
		//String indCostoPoste = intBoolString(param[Constantes.PARAMSAL_IND_COSTO_POSTE]);
		String cosmarResumen = intBoolString(param[Constantes.PARAMSAL_COSMAR_RESUMEN]);
		String cosmarCron = intBoolString(param[Constantes.PARAMSAL_COSMAR_CRON]);
		//String indCosmarCron = intBoolString(param[Constantes.PARAMSAL_IND_COSMAR_CRON]);
		String cantMod = intBoolString(param[Constantes.PARAMSAL_CANTMOD]);
		//String indAtriDetallado = intBoolString(param[Constantes.PARAMSAL_IND_ATR_DET]);
		String salidaDetalladaPaso = intBoolString(param[Constantes.PARAMSAL_SALIDA_DET_PASO]);
		String costoPasoCron = intBoolString(param[Constantes.PARAMSAL_COSTO_PASO_CRON]);

		Element er = crearElemento("enerResumen");
		er.appendChild(dom.createTextNode(enerResumen));
		nuevo.appendChild(er);

		Element ec = crearElemento("enercron");
		ec.appendChild(dom.createTextNode(enercron));
		nuevo.appendChild(ec);

		Element pp = crearElemento("potPoste");
		pp.appendChild(dom.createTextNode(potPoste));
		nuevo.appendChild(pp);

//		Element ip = crearElemento("indPot");
//		ip.appendChild(dom.createTextNode(indPot));
//		nuevo.appendChild(ip);

		Element cr = crearElemento("costoResumen");
		cr.appendChild(dom.createTextNode(costoResumen));
		nuevo.appendChild(cr);

		Element cc = crearElemento("costocron");
		cc.appendChild(dom.createTextNode(costocron));
		nuevo.appendChild(cc);

		Element cp = crearElemento("costoPoste");
		cp.appendChild(dom.createTextNode(costoPoste));
		nuevo.appendChild(cp);
//
//		Element icp = crearElemento("indCostoPoste");
//		icp.appendChild(dom.createTextNode(indCostoPoste));
//		nuevo.appendChild(icp);

		Element cres = crearElemento("cosmarResumen");
		cres.appendChild(dom.createTextNode(cosmarResumen));
		nuevo.appendChild(cres);

		Element cmc = crearElemento("cosmarCron");
		cmc.appendChild(dom.createTextNode(cosmarCron));
		nuevo.appendChild(cmc);

//		Element icc = crearElemento("indCosmarCron");
//		icc.appendChild(dom.createTextNode(indCosmarCron));
//		nuevo.appendChild(icc);

		Element cm = crearElemento("cantMod");
		cm.appendChild(dom.createTextNode(cantMod));
		nuevo.appendChild(cm);

//		Element iad = crearElemento("indAtriDetallado");
//		iad.appendChild(dom.createTextNode(indAtriDetallado));
//		nuevo.appendChild(iad);

		Element sdp = crearElemento("salidaDetalladaPaso");
		sdp.appendChild(dom.createTextNode(salidaDetalladaPaso));
		nuevo.appendChild(sdp);

		Element cpc = crearElemento("costoPasoCron");
		cpc.appendChild(dom.createTextNode(costoPasoCron));
		nuevo.appendChild(cpc);
		return nuevo;

	}

	private Element escribirSalidaSimDet(DatosCorrida corrida) {
		Element nuevo = dom.createElement("salidaSimDet");

		String salSim = Boolean.toString(corrida.getDatosParamSalidaSim().isSalSim());
		String pasoIni = Integer.toString(corrida.getDatosParamSalidaSim().getPasoIni());
		String pasoFin = Integer.toString(corrida.getDatosParamSalidaSim().getPasoFin());
		String escIni = Integer.toString(corrida.getDatosParamSalidaSim().getEscIni());
		String escFin = Integer.toString(corrida.getDatosParamSalidaSim().getEscFin());

		Element er = crearElemento("salSim");
		er.appendChild(dom.createTextNode(salSim));
		nuevo.appendChild(er);

		Element ec = crearElemento("pasoIni");
		ec.appendChild(dom.createTextNode(pasoIni));
		nuevo.appendChild(ec);

		Element pp = crearElemento("pasoFin");
		pp.appendChild(dom.createTextNode(pasoFin));
		nuevo.appendChild(pp);

		Element ip = crearElemento("escIni");
		ip.appendChild(dom.createTextNode(escIni));
		nuevo.appendChild(ip);

		Element cr = crearElemento("escFin");
		cr.appendChild(dom.createTextNode(escFin));
		nuevo.appendChild(cr);
		return nuevo;

	}

	private String intBoolString(int[] is) {
		if (is == null)
			return "FALSE";
		return is[0] == 0 ? "FALSE" : "TRUE";
	}

	private Element escribirCantSorteosMont(DatosCorrida corrida) {
		Element nuevo = dom.createElement("cantSorteosMont");

		nuevo.appendChild(escribirEvolucion(corrida.getCantSorteosMont(), "int"));

		return nuevo;

	}

	public Element escribirEvolucion(Evolucion<?> ev, String tipoDato) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		Element nuevo = dom.createElement("ev");
		String tipo = "";
		String valor = ev.getValor(instanteActual).toString();
		String periodo = "";
		String cantPeriodo = "";
		if (ev instanceof EvolucionConstante) {
			tipo = "const";
			nuevo.setAttribute("tipo", tipo);
			if (tipoDato.equalsIgnoreCase("polinomio")) {
				EvolucionConstante<DatosPolinomio> dp = (EvolucionConstante<DatosPolinomio>)ev;
//				((DatosPolinomio)dp.getValor()).toString();


				nuevo.appendChild(dp.getValor(instanteActual).toXML(dom, null));
				return nuevo;

			} else if(tipoDato.equalsIgnoreCase("listadouble")){
				EvolucionConstante<ArrayList<Double>> listaDouble = (EvolucionConstante<ArrayList<Double>>)ev;
				nuevo.appendChild(dom.createTextNode(listaDouble.getValor(instanteActual).toString().substring(1,listaDouble.getValor(instanteActual).toString().length()-1)));
				return nuevo;
			} else if(tipoDato.equalsIgnoreCase("arraydouble")){
				EvolucionConstante<Double[]> arrayDouble = (EvolucionConstante<Double[]>)ev;
//				nuevo.appendChild(dom.createTextNode(arrayDouble.getValor().toString().substring(1,arrayDouble.getValor().toString().length()-1)));
				String arrayValue = "";
				arrayValue += arrayDouble.getValor(instanteActual)[0];
				for(int i=1;i<arrayDouble.getValor(instanteActual).length;i++){
					arrayValue += ",";
					arrayValue += arrayDouble.getValor(instanteActual)[i];
				}
				nuevo.appendChild(dom.createTextNode(arrayValue));
				return nuevo;
			} else if(tipoDato.equalsIgnoreCase("discretizacion")){
				EvolucionConstante<DatosDiscretizacion> discretizacion = (EvolucionConstante<DatosDiscretizacion>)ev;
				nuevo.appendChild(discretizacion.getValor(instanteActual).toXML(dom));
				return nuevo;
			}

		} else if (ev instanceof EvolucionPorInstantes) {
			tipo = "porInstantes";

			
			if (tipoDato.equalsIgnoreCase("string")) {
				EvolucionPorInstantes<String> evs = (EvolucionPorInstantes<String>) ev;
				Hashtable<Long, String> valorizador = (Hashtable<Long, String>) evs.getValorizador();
				valor = "";

				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {

					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					String val = valorizador.get(p);
					valor += fechaest + ";" + val + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				EvolucionPorInstantes<Double> evs = (EvolucionPorInstantes<Double>) ev;
				Hashtable<Long, Double> valorizador = (Hashtable<Long, Double>) evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Double val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}

			}
			if (tipoDato.equalsIgnoreCase("int")) {
				EvolucionPorInstantes<Integer> evs = (EvolucionPorInstantes<Integer>) ev;
				Hashtable<Long, Integer> valorizador = (Hashtable<Long, Integer>) evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Integer val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				EvolucionPorInstantes<Boolean> evs = (EvolucionPorInstantes<Boolean>) ev;
				Hashtable<Long, Boolean> valorizador = (Hashtable<Long, Boolean>) evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Boolean val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				EvolucionPorInstantes<ArrayList<Pair<Double, Double>>> evs = (EvolucionPorInstantes<ArrayList<Pair<Double, Double>>>) ev;
				Hashtable<Long, ArrayList<Pair<Double, Double>>> valorizador = (Hashtable<Long, ArrayList<Pair<Double, Double>>>) evs
						.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					ArrayList<Pair<Double, Double>> val = valorizador.get(p);
					valor += fechaest + ";" + Arrays.deepToString(val.toArray()) + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				EvolucionPorInstantes<ArrayList<Double>> evs = (EvolucionPorInstantes<ArrayList<Double>>) ev;
				Hashtable<Long, ArrayList<Double>> valorizador = (Hashtable<Long, ArrayList<Double>>) evs
						.getValorizador();
				valor = "";

				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					ArrayList<Double> val = valorizador.get(p);
					valor += fechaest + ";" + arrayDoubleXML(val) + ")";
				}
			}
		} else if (ev instanceof EvolucionPeriodica) {
			tipo = "periodica";
			if (tipoDato.equalsIgnoreCase("string")) {
				EvolucionPeriodica<String> evs = (EvolucionPeriodica<String>) ev;
				Hashtable<Long, String> valorizador = (Hashtable<Long, String>) evs.getDefinicionPeriodo()
						.getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {

					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					String val = valorizador.get(p);
					valor += fechaest + ";" + val + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				EvolucionPeriodica<Double> evs = (EvolucionPeriodica<Double>) ev;
				Hashtable<Long, Double> valorizador = (Hashtable<Long, Double>) evs.getDefinicionPeriodo()
						.getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Double val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}

			}
			if (tipoDato.equalsIgnoreCase("int")) {
				EvolucionPeriodica<Integer> evs = (EvolucionPeriodica<Integer>) ev;
				Hashtable<Long, Integer> valorizador = (Hashtable<Long, Integer>) evs.getDefinicionPeriodo()
						.getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Integer val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				EvolucionPeriodica<Boolean> evs = (EvolucionPeriodica<Boolean>) ev;
				Hashtable<Long, Boolean> valorizador = (Hashtable<Long, Boolean>) evs.getDeterminante()
						.getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p,lt);
					Boolean val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				EvolucionPeriodica<ArrayList<Pair<Double, Double>>> evs = (EvolucionPeriodica<ArrayList<Pair<Double, Double>>>) ev;
				Hashtable<Long, ArrayList<Pair<Double, Double>>> valorizador = (Hashtable<Long, ArrayList<Pair<Double, Double>>>) evs
						.getDefinicionPeriodo().getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					ArrayList<Pair<Double, Double>> val = valorizador.get(p);
					valor += fechaest + ";" + Arrays.deepToString(val.toArray()) + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				EvolucionPeriodica<ArrayList<Double>> evs = (EvolucionPeriodica<ArrayList<Double>>) ev;
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				Hashtable<Long, ArrayList<Double>> valorizador = (Hashtable<Long, ArrayList<Double>>) evs
						.getDefinicionPeriodo().getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					ArrayList<Double> val = valorizador.get(p);
					valor += fechaest + ";" + arrayDoubleXML(val) + ")";
				}
			}
			nuevo.setAttribute("tipo", tipo);
			Element defPeriodoElem = dom.createElement("definicionPeriodo");
			defPeriodoElem.appendChild(dom.createTextNode(valor));
			nuevo.appendChild(defPeriodoElem);
//			nuevo.appendChild(dom.createElement("definicionPeriodo").appendChild(dom.createTextNode(valor)));
			Element periodoElem = dom.createElement("periodo");
			periodoElem.appendChild(dom.createTextNode(periodo));
			nuevo.appendChild(periodoElem);
//			nuevo.appendChild(dom.createElement("periodo").appendChild(dom.createTextNode(periodo)));
			Element cantPeriodosElem = dom.createElement("cantPeriodo");
			cantPeriodosElem.appendChild(dom.createTextNode(cantPeriodo));
			nuevo.appendChild(cantPeriodosElem);
//			nuevo.appendChild(dom.createElement("cantPeriodos").appendChild(dom.createTextNode(cantPeriodo)));
			return nuevo;
		}

		nuevo.setAttribute("tipo", tipo);
		nuevo.appendChild(dom.createTextNode(valor));
		return nuevo;
	}

	public Element escribirEvolucion(Evolucion<?> ev, String tipoDato, Document dom) {
		LineaTiempo lt = CorridaHandler.getInstance().getCorridaActual().getLineaTiempo();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		Element nuevo = dom.createElement("ev");
		String tipo = "";
		String valor = ev.getValor(instanteActual).toString();
		String periodo = "";
		String cantPeriodo = "";
		if (ev instanceof EvolucionConstante) {
			tipo = "const";
			nuevo.setAttribute("tipo", tipo);
			if (tipoDato.equalsIgnoreCase("polinomio")) {
				EvolucionConstante<DatosPolinomio> dp = (EvolucionConstante<DatosPolinomio>)ev;
//				((DatosPolinomio)dp.getValor()).toString();


				nuevo.appendChild(dp.getValor(instanteActual).toXML(dom, null));
				return nuevo;

			} else if(tipoDato.equalsIgnoreCase("listadouble")){
				EvolucionConstante<ArrayList<Double>> listaDouble = (EvolucionConstante<ArrayList<Double>>)ev;
				nuevo.appendChild(dom.createTextNode(listaDouble.getValor(instanteActual).toString().substring(1,listaDouble.getValor(instanteActual).toString().length()-1)));
				return nuevo;
			} else if(tipoDato.equalsIgnoreCase("arraydouble")){
				EvolucionConstante<Double[]> arrayDouble = (EvolucionConstante<Double[]>)ev;
//				nuevo.appendChild(dom.createTextNode(arrayDouble.getValor().toString().substring(1,arrayDouble.getValor().toString().length()-1)));
				String arrayValue = "";
				arrayValue += arrayDouble.getValor(instanteActual)[0];
				for(int i=1;i<arrayDouble.getValor(instanteActual).length;i++){
					arrayValue += ",";
					arrayValue += arrayDouble.getValor(instanteActual)[i];
				}
				nuevo.appendChild(dom.createTextNode(arrayValue));
				return nuevo;
			} else if(tipoDato.equalsIgnoreCase("discretizacion")){
				EvolucionConstante<DatosDiscretizacion> discretizacion = (EvolucionConstante<DatosDiscretizacion>)ev;
				nuevo.appendChild(discretizacion.getValor(instanteActual).toXML(dom));
				return nuevo;
			}

		} else if (ev instanceof EvolucionPorInstantes) {
			tipo = "porInstantes";


			if (tipoDato.equalsIgnoreCase("string")) {
				EvolucionPorInstantes<String> evs = (EvolucionPorInstantes<String>) ev;
				Hashtable<Long, String> valorizador = (Hashtable<Long, String>) evs.getValorizador();
				valor = "";

				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {

					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					String val = valorizador.get(p);
					valor += fechaest + ";" + val + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				EvolucionPorInstantes<Double> evs = (EvolucionPorInstantes<Double>) ev;
				Hashtable<Long, Double> valorizador = (Hashtable<Long, Double>) evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Double val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}

			}
			if (tipoDato.equalsIgnoreCase("int")) {
				EvolucionPorInstantes<Integer> evs = (EvolucionPorInstantes<Integer>) ev;
				Hashtable<Long, Integer> valorizador = (Hashtable<Long, Integer>) evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Integer val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				EvolucionPorInstantes<Boolean> evs = (EvolucionPorInstantes<Boolean>) ev;
				Hashtable<Long, Boolean> valorizador = (Hashtable<Long, Boolean>) evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Boolean val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				EvolucionPorInstantes<ArrayList<Pair<Double, Double>>> evs = (EvolucionPorInstantes<ArrayList<Pair<Double, Double>>>) ev;
				Hashtable<Long, ArrayList<Pair<Double, Double>>> valorizador = (Hashtable<Long, ArrayList<Pair<Double, Double>>>) evs
						.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					ArrayList<Pair<Double, Double>> val = valorizador.get(p);
					valor += fechaest + ";" + Arrays.deepToString(val.toArray()) + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				EvolucionPorInstantes<ArrayList<Double>> evs = (EvolucionPorInstantes<ArrayList<Double>>) ev;
				Hashtable<Long, ArrayList<Double>> valorizador = (Hashtable<Long, ArrayList<Double>>) evs
						.getValorizador();
				valor = "";

				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					ArrayList<Double> val = valorizador.get(p);
					valor += fechaest + ";" + arrayDoubleXML(val) + ")";
				}
			}
		} else if (ev instanceof EvolucionPeriodica) {
			tipo = "periodica";
			if (tipoDato.equalsIgnoreCase("string")) {
				EvolucionPeriodica<String> evs = (EvolucionPeriodica<String>) ev;
				Hashtable<Long, String> valorizador = (Hashtable<Long, String>) evs.getDefinicionPeriodo()
						.getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {

					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					String val = valorizador.get(p);
					valor += fechaest + ";" + val + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("double")) {
				EvolucionPeriodica<Double> evs = (EvolucionPeriodica<Double>) ev;
				Hashtable<Long, Double> valorizador = (Hashtable<Long, Double>) evs.getDefinicionPeriodo()
						.getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Double val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}

			}
			if (tipoDato.equalsIgnoreCase("int")) {
				EvolucionPeriodica<Integer> evs = (EvolucionPeriodica<Integer>) ev;
				Hashtable<Long, Integer> valorizador = (Hashtable<Long, Integer>) evs.getDefinicionPeriodo()
						.getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					Integer val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("boolean")) {
				EvolucionPeriodica<Boolean> evs = (EvolucionPeriodica<Boolean>) ev;
				Hashtable<Long, Boolean> valorizador = (Hashtable<Long, Boolean>) evs.getDeterminante()
						.getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p,lt);
					Boolean val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("listaParesDouble")) {
				EvolucionPeriodica<ArrayList<Pair<Double, Double>>> evs = (EvolucionPeriodica<ArrayList<Pair<Double, Double>>>) ev;
				Hashtable<Long, ArrayList<Pair<Double, Double>>> valorizador = (Hashtable<Long, ArrayList<Pair<Double, Double>>>) evs
						.getDefinicionPeriodo().getValorizador();
				valor = "";
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					ArrayList<Pair<Double, Double>> val = valorizador.get(p);
					valor += fechaest + ";" + Arrays.deepToString(val.toArray()) + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase("listadouble")) {
				EvolucionPeriodica<ArrayList<Double>> evs = (EvolucionPeriodica<ArrayList<Double>>) ev;
				periodo = dameStringPeriodo(evs.getPeriodo());
				cantPeriodo = Integer.toString(evs.getCantPeriodos());
				Hashtable<Long, ArrayList<Double>> valorizador = (Hashtable<Long, ArrayList<Double>>) evs
						.getDefinicionPeriodo().getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = convertirAFecha(p, lt);
					ArrayList<Double> val = valorizador.get(p);
					valor += fechaest + ";" + arrayDoubleXML(val) + ")";
				}
			}
			nuevo.setAttribute("tipo", tipo);
			Element defPeriodoElem = dom.createElement("definicionPeriodo");
			defPeriodoElem.appendChild(dom.createTextNode(valor));
			nuevo.appendChild(defPeriodoElem);
//			nuevo.appendChild(dom.createElement("definicionPeriodo").appendChild(dom.createTextNode(valor)));
			Element periodoElem = dom.createElement("periodo");
			periodoElem.appendChild(dom.createTextNode(periodo));
			nuevo.appendChild(periodoElem);
//			nuevo.appendChild(dom.createElement("periodo").appendChild(dom.createTextNode(periodo)));
			Element cantPeriodosElem = dom.createElement("cantPeriodo");
			cantPeriodosElem.appendChild(dom.createTextNode(cantPeriodo));
			nuevo.appendChild(cantPeriodosElem);
//			nuevo.appendChild(dom.createElement("cantPeriodos").appendChild(dom.createTextNode(cantPeriodo)));
			return nuevo;
		}

		nuevo.setAttribute("tipo", tipo);
		nuevo.appendChild(dom.createTextNode(valor));
		return nuevo;
	}

	public static String dameStringPeriodo(int periodo) {
		if (periodo == Calendar.YEAR)
			return "año";
		if (periodo == Calendar.MONTH)
			return "mes";
		if (periodo == Calendar.WEEK_OF_YEAR)
			return "semana";
		if (periodo == Calendar.DAY_OF_YEAR)
			return "dia";
		return null;
	}

	public static String convertirAFecha(Long p, LineaTiempo lt) {
		GregorianCalendar gc = lt.dameTiempoParaEscibirEvolucion(p);
		DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
		String strDate = dateFormat.format(gc.getTime());
		return strDate;
	}

	private Element escribirIteraciones(DatosIteracionesCorrida datosIteraciones) {
		Element nuevo = dom.createElement("iteraciones");

		Element maximoIteraciones = crearElemento("maximoIteraciones");
		maximoIteraciones.appendChild(dom.createTextNode(datosIteraciones.getMaximoIteraciones().toString()));
		nuevo.appendChild(maximoIteraciones);

		Element numeroIteraciones = crearElemento("numeroIteraciones");
		numeroIteraciones.appendChild(dom.createTextNode(datosIteraciones.getNumIteraciones().toString()));
		nuevo.appendChild(numeroIteraciones);

		Element criterioParada = crearElemento("criterioParada");
		criterioParada.appendChild(dom.createTextNode(datosIteraciones.getCriterioParada()));
		nuevo.appendChild(criterioParada);

		return nuevo;

	}

	private Element escribirCompsGlobales(Hashtable<String, Evolucion<String>> valoresComportamientoGlobal) {
		Element nuevo = dom.createElement("compsGlobales");

		Set<String> keys = valoresComportamientoGlobal.keySet();
		Iterator<String> it = keys.iterator();
		CorridaHandler.getInstance().getCorridaActual().getLineaTiempo().reiniciar();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		while (it.hasNext()) {
			String clave = it.next();
			Element comp = dom.createElement(clave);
			comp.appendChild(dom.createTextNode(valoresComportamientoGlobal.get(clave).getValor(instanteActual)));
			nuevo.appendChild(comp);
		}

		return nuevo;
	}

	private Element escribirLineaDeTiempo(DatosLineaTiempo lineaTiempo) {
		Element nuevo = dom.createElement("lineaTiempo");
		Element inicioTiempo = crearElemento("inicioTiempo");
		inicioTiempo.appendChild(dom.createTextNode(lineaTiempo.getTiempoInicial()));
		nuevo.appendChild(inicioTiempo);
		nuevo.appendChild(escribirPeriodoIntegracion(lineaTiempo));
		nuevo.appendChild(escribirBloques(lineaTiempo));

		// Element postizacion=
		// crearElemento("postizacion","tipo",corrida.getTipoPostizacion());
		//
		// last_root.appendChild(tasaAnual);

		return nuevo;
	}

	private Element escribirBloques(DatosLineaTiempo lineaTiempo) {
		Element nuevo = dom.createElement("listaBloques");
		for (int i = 0; i < lineaTiempo.getCantBloques(); i++) {
			nuevo.appendChild(escribirBloque(lineaTiempo, i));
		}

		return nuevo;

	}

	private Element escribirBloque(DatosLineaTiempo lineaTiempo, int i) {
		Element nuevo = dom.createElement("bloque");

		Element cantPasos = dom.createElement("cantPasos");
		cantPasos.appendChild(dom.createTextNode(lineaTiempo.getPasosPorBloque().get(i).toString()));
		nuevo.appendChild(cantPasos);

		Element durPaso = crearElemento("durPaso", "unidad", "segundos");
		durPaso.appendChild(dom.createTextNode(lineaTiempo.getDuracionPasoPorBloque().get(i).toString()));
		nuevo.appendChild(durPaso);

		Element intervaloMuestreo = crearElemento("intervaloMuestreo", "unidad", "segundos");
		intervaloMuestreo.appendChild(dom.createTextNode(lineaTiempo.getIntMuestreoPorBloque().get(i).toString()));
		nuevo.appendChild(intervaloMuestreo);

		Element cantPostes = dom.createElement("cantPostes");
		cantPostes.appendChild(dom.createTextNode(Integer.toString(lineaTiempo.getDurPostesPorBloque().get(i).size())));
		nuevo.appendChild(cantPostes);

		Element duracionPostes = dom.createElement("duracionPostes");
		String durPosString = lineaTiempo.getDurPostesPorBloque().get(i).toString();
		duracionPostes.appendChild(dom.createTextNode(arrayStringXML(durPosString)));
		nuevo.appendChild(duracionPostes);

		Element periodoBloque = dom.createElement("periodoBloque");
		periodoBloque.appendChild(dom.createTextNode(lineaTiempo.getPeriodoPasoPorBloque().get(i).toString()));
		nuevo.appendChild(periodoBloque);

		Element cronologico = dom.createElement("cronologico");
		cronologico.appendChild(dom.createTextNode(lineaTiempo.getCronologicos().get(i).toString()));
		nuevo.appendChild(cronologico);

		return nuevo;
	}

	private String arrayStringXML(String in) {

		in = in.substring(1, in.length() - 1);
		return in.replaceAll("\\s+", "");
	}

	private Element escribirPeriodoIntegracion(DatosLineaTiempo lineaTiempo) {
		Element nuevo = dom.createElement("periodoIntegracion");
		Element activo = crearElemento("activo");
		Element durPeriodoIntegracion = crearElemento("durPeriodoIntegracion");
		activo.appendChild(dom.createTextNode("false"));
		durPeriodoIntegracion.appendChild(dom.createTextNode(Integer.toString(lineaTiempo.getPeriodoIntegracion())));
		nuevo.appendChild(activo);
		nuevo.appendChild(durPeriodoIntegracion);
		return nuevo;

	}

	private Element crearElemento(String nombre) {
		return crearElemento(nombre, null, null);
	}

	private Element crearElemento(String nombre, String atributo, String val_at) {
		Element resultado = dom.createElement(nombre);
		Attr a = null;
		if (atributo != null) {
			a = dom.createAttribute(atributo);
			a.setValue(val_at);
			resultado.setAttributeNode(a);
		}

		return resultado;

	}

	public static void main(String[] args) {
		DatosCorrida dc = new DatosCorrida();

		CargadorXML cxml = new CargadorXML();
		dc = cxml.cargarCorrida(
				"G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\Preliminares\\Pruebas_Emiliano\\P55-COPIA-CorridaSemanalLP_externa.xml");
		CorridaHandler ch = CorridaHandler.getInstance();
		ch.cargarCorrida(
				"G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\Preliminares\\Pruebas_Emiliano\\P55-COPIA-CorridaSemanalLP_externa.xml",
				false, false, false, true);
		Corrida corrida = ch.getCorridaActual();
		LineaTiempo lt = corrida.getLineaTiempo();
		EscritorXML eml = new EscritorXML(lt);
		eml.guardarCorrida(dc, "./prueba.xml");
	}

	public Element getLast_root() {
		return last_root;
	}

	public void setLast_root(Element last_root) {
		this.last_root = last_root;
	}

	public DatosCorrida getCorrida() {
		return corrida;
	}

	public void setCorrida(DatosCorrida corrida) {
		this.corrida = corrida;
	}
}
