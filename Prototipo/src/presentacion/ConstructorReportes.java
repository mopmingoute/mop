/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConstructorReportes is part of MOP.
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

package presentacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import datatypes.DatosEspecificacionReporte;
import datatypes.DatosGraficaGUI;
import datatypes.DatosReporteGUI;
import datatypes.DatosResumenGUI;
import datatypes.DatosTotalReportes;
import datatypes.Pair;
import datatypesSalida.DatosEPPUnEscenario;
import logica.CorridaHandler;
import parque.Corrida;
import persistencia.EscritorResumenSimulacionParalelo;
import tiempo.LineaTiempo;
import utilitarios.Constantes;

public class ConstructorReportes {

	private ArrayList<DatosEPPUnEscenario> datosEscenarios;

	private ArrayList<Integer> cronicasOrdenadasPorEshy1;

	private static ConstructorReportes instance;

	private DatosTotalReportes datosReportes;

	private EscritorResumenSimulacionParalelo eResumen;

	private int[] aniosCorrida;

	private Hashtable<Integer, ArrayList<Integer>> pasosAnio;

	private Corrida corrida;

	private ConstructorReportes() {
		datosReportes = new DatosTotalReportes(datosEscenarios);

	}
	
	public static void deleteInstance() {
		instance = null;			
	}
	/** Función del singleton que devuelve siempre la misma instancia */
	public static ConstructorReportes getInstance() {
		if (instance == null)
			instance = new ConstructorReportes();

		return instance;
	}

	public ArrayList<Integer> getCronicasOrdenadasPorEshy1() {
		return cronicasOrdenadasPorEshy1;
	}

	public void setCronicasOrdenadasPorEshy1(ArrayList<Integer> cronicasOrdenadasPorEshy1) {
		this.cronicasOrdenadasPorEshy1 = cronicasOrdenadasPorEshy1;
	}

	public DatosReporteGUI devolverReporte(DatosEspecificacionReporte especificacion) {

		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_RES_ENER) {
			return devolverReporteEnergias(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_RES_COSTO) {
			return devolverReporteCostos(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_COSTOS_MARG) {
			return devolverReporteCostosMarginales(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_FALLAS) {
			return devolverReporteCostosFallas(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_HIDROS) {
			return devolverReporteHidros(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_TERS) {
			return devolverReporteTermicos(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_OPTIMIZACION) {
			return devolverReporteOptimizacion(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_VAL_RENOVABLES) {
			return devolverReporteRenovables(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_AMBIENTAL) {
			return devolverReporteAmbiental(especificacion);
		}
		if (especificacion.getTipoReporte() == DatosEspecificacionReporte.REP_CONTRATOS) {
			return devolverReporteContratos(especificacion);
		}

		return null;
	}

	private DatosReporteGUI devolverReporteContratos(DatosEspecificacionReporte especificacion) {
		// TODO Auto-generated method stub
		return null;
	}

	private DatosReporteGUI devolverReporteAmbiental(DatosEspecificacionReporte especificacion) {
		// TODO Auto-generated method stub
		return null;
	}

	private DatosReporteGUI devolverReporteRenovables(DatosEspecificacionReporte especificacion) {
		// TODO Auto-generated method stub
		return null;
	}

	private DatosReporteGUI devolverReporteOptimizacion(DatosEspecificacionReporte especificacion) {
		// TODO Auto-generated method stub
		return null;
	}

	private DatosReporteGUI devolverReporteTermicos(DatosEspecificacionReporte especificacion) {
		// TODO Auto-generated method stub
		return null;
	}

	private DatosReporteGUI devolverReporteHidros(DatosEspecificacionReporte especificacion) {
		// TODO Auto-generated method stub
		return null;
	}

	private DatosReporteGUI devolverReporteCostosFallas(DatosEspecificacionReporte especificacion) {
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();
		if (corrida == null)
			return null;
		LineaTiempo lt = corrida.getLineaTiempo();

		if (especificacion.isPorDefecto()) {
			especificacion.setAnioInicio(lt.getAnioInic());
			especificacion.setAnioFin(lt.getAnioFin());
			especificacion.setPorTecnologia(false);
		}

		int anioInicio = especificacion.getAnioInicio();
		int anioFin = especificacion.getAnioFin();
		boolean porTecnologia = especificacion.isPorTecnologia();

		HashMap<Integer, DatosResumenGUI> resumenes = new HashMap<Integer, DatosResumenGUI>();
		HashMap<Integer, DatosGraficaGUI> graficas = new HashMap<Integer, DatosGraficaGUI>();
		DatosGraficaGUI graficoCM = dameGraficoFallas(anioInicio, anioFin, porTecnologia);
		graficas.put(0, graficoCM);

		DatosReporteGUI dr = new DatosReporteGUI("Reporte de fallas", especificacion.getTipoReporte(),
				resumenes, graficas);

		return dr;
	}

	private DatosReporteGUI devolverReporteCostosMarginales(DatosEspecificacionReporte especificacion) {
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();
		if (corrida == null)
			return null;
		LineaTiempo lt = corrida.getLineaTiempo();

		if (especificacion.isPorDefecto()) {
			especificacion.setAnioInicio(lt.getAnioInic());
			especificacion.setAnioFin(lt.getAnioFin());
			especificacion.setPorTecnologia(false);
		}

		int anioInicio = especificacion.getAnioInicio();
		int anioFin = especificacion.getAnioFin();
		boolean porTecnologia = especificacion.isPorTecnologia();

		HashMap<Integer, DatosResumenGUI> resumenes = new HashMap<Integer, DatosResumenGUI>();
		HashMap<Integer, DatosGraficaGUI> graficas = new HashMap<Integer, DatosGraficaGUI>();
		DatosGraficaGUI graficoCM = dameGraficoCM(anioInicio, anioFin, porTecnologia);
		graficas.put(0, graficoCM);

		DatosReporteGUI dr = new DatosReporteGUI("Resúmen de Costos Marginales", especificacion.getTipoReporte(),
				resumenes, graficas);

		return dr;
	}

	private DatosGraficaGUI dameGraficoCM(int anioInicio, int anioFin, boolean porTecnologia) {
		double[][] costos = this.eResumen.getCosmar_prom_paso()[0];

		Hashtable<String, ArrayList<Pair<Integer, Double>>> datosGrafica = new Hashtable<String, ArrayList<Pair<Integer, Double>>>();

		LineaTiempo lt = corrida.getLineaTiempo();
		int anioIniCorr = lt.getAnioInic();
		int anioFinCorr = lt.getAnioFin();
 		
		int pasoInicio = corrida.dameNumeroPasoInicioAnio(anioInicio);
		int pasoFin = corrida.dameNumeroPasoFinAnio(anioFin);
		for (int poste = 0; poste < costos[0].length; poste++) {
			String nombreSerie = "Poste" + poste;
			ArrayList<Pair<Integer, Double>> serie = new ArrayList<Pair<Integer, Double>>();

			for (int paso = pasoInicio ; paso < pasoFin; paso++) {				
				serie.add(new Pair<Integer, Double>(paso, costos[paso][poste]));
			}
			datosGrafica.put(nombreSerie,serie);

		}

		DatosGraficaGUI res = new DatosGraficaGUI("Resumen de Costos Marginales", "Paso", "Tiempo", "USD/MWh", "CMg",
				datosGrafica, DatosGraficaGUI.GRAF_LINEAS);

		return res;
	}

	
	private DatosGraficaGUI dameGraficoFallas(int anioInicio, int anioFin, boolean porTecnologia) {
	//	double[][] costos = this.eResumen.getCosto_esc()
//
//		Hashtable<String, ArrayList<Pair<Integer, Double>>> datosGrafica = new Hashtable<String, ArrayList<Pair<Integer, Double>>>();
//
//		
//		for (int poste = 0; poste < costos[0].length; poste++) {
//			String nombreSerie = "Poste" + poste;
//			ArrayList<Pair<Integer, Double>> serie = new ArrayList<Pair<Integer, Double>>();
//
//			for (int paso = 0; paso < costos.length; paso++) {				
//				serie.add(new Pair<Integer, Double>(paso, costos[paso][poste]));
//			}
//			datosGrafica.put(nombreSerie,serie);
//
//		}
//
//		DatosGraficaGUI res = new DatosGraficaGUI("Resumen de Costos Marginales", "Paso", "USD/MWh", "", "CMg",
//				datosGrafica, DatosGraficaGUI.GRAF_LINEAS);
//
//		return res;
		return null;
	}
	
	private DatosReporteGUI devolverReporteCostos(DatosEspecificacionReporte especificacion) {
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();
		if (corrida == null)
			return null;
		LineaTiempo lt = corrida.getLineaTiempo();

		if (especificacion.isPorDefecto()) {
			especificacion.setAnioInicio(lt.getAnioInic());
			especificacion.setAnioFin(lt.getAnioFin());
			especificacion.setPorTecnologia(true);
		}

		int anioInicio = especificacion.getAnioInicio();
		int anioFin = especificacion.getAnioFin();
		boolean porTecnologia = especificacion.isPorTecnologia();

		HashMap<Integer, DatosResumenGUI> resumenes = new HashMap<Integer, DatosResumenGUI>();
		DatosResumenGUI reporteEnergias = dameResumenCostos(anioInicio, anioFin, porTecnologia);
		resumenes.put(1, reporteEnergias);
		HashMap<Integer, DatosGraficaGUI> graficas = new HashMap<Integer, DatosGraficaGUI>();
		DatosGraficaGUI graficoEnergias = dameGraficoCostos(anioInicio, anioFin, porTecnologia);
		graficas.put(0, graficoEnergias);

		DatosReporteGUI dr = new DatosReporteGUI("Resúmen de Costos", especificacion.getTipoReporte(), resumenes,
				graficas);

		return dr;
	}

	private DatosResumenGUI dameResumenCostos(int anioInicio, int anioFin, boolean porTecnologia) {

		HashMap<String, HashMap<String, Double>> valores = cargarCostos(anioInicio, anioFin, porTecnologia);

		DatosResumenGUI drg = new DatosResumenGUI("Costos", valores);

		return drg;
	}

	private HashMap<String, HashMap<String, Double>> cargarCostos(int anioInicio, int anioFin, boolean porTecnologia) {
		double[][] costos = this.eResumen.getCos_resumen_MUSD();

		return generarValores(costos, anioInicio, anioFin, porTecnologia, true);

	}

	private DatosReporteGUI devolverReporteEnergias(DatosEspecificacionReporte especificacion) {

		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();
		if (corrida == null)
			return null;
		LineaTiempo lt = corrida.getLineaTiempo();

		if (especificacion.isPorDefecto()) {
			especificacion.setAnioInicio(lt.getAnioInic());
			especificacion.setAnioFin(lt.getAnioFin());
			especificacion.setPorTecnologia(false);
		}

		int anioInicio = especificacion.getAnioInicio();
		int anioFin = especificacion.getAnioFin();
		boolean porTecnologia = especificacion.isPorTecnologia();

		HashMap<Integer, DatosResumenGUI> resumenes = new HashMap<Integer, DatosResumenGUI>();
		DatosResumenGUI reporteEnergias = dameResumenEnergias(anioInicio, anioFin, porTecnologia);
		resumenes.put(1, reporteEnergias);
		HashMap<Integer, DatosGraficaGUI> graficas = new HashMap<Integer, DatosGraficaGUI>();
		DatosGraficaGUI graficoEnergias = dameGraficoEnergias(anioInicio, anioFin);
		graficas.put(0, graficoEnergias);

		DatosReporteGUI dr = new DatosReporteGUI("Resúmen de Energías", especificacion.getTipoReporte(), resumenes,
				graficas);

		return dr;
	}

	private DatosResumenGUI dameResumenEnergias(int anioInicio, int anioFin, boolean porTecnologia) {

		HashMap<String, HashMap<String, Double>> valores = cargarEnergias(anioInicio, anioFin, porTecnologia);

		DatosResumenGUI drg = new DatosResumenGUI("Energías", valores);

		return drg;
	}

	private HashMap<String, HashMap<String, Double>> cargarEnergias(int anioInicio, int anioFin,
			boolean porTecnologia) {
		double[][] energias = this.eResumen.getEner_resumen();

		return generarValores(energias, anioInicio, anioFin, porTecnologia, true);

	}

	private HashMap<String, HashMap<String, Double>> generarValores(double[][] valores, int anioIni, int anioFin,
			boolean porTecnologia, boolean resumido) {

		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();
		int pasoIni = corrida.dameNumeroPasoInicioAnio(anioIni);
		int pasoFin = corrida.dameNumeroPasoFinAnio(anioFin);
		ArrayList<Integer> cambiosAnio = corrida.dameCambiosDeAnio(anioIni, anioFin);

		HashMap<String, HashMap<String, Double>> resultado = new HashMap<String, HashMap<String, Double>>();
//		if (porTecnologia) {
//
//		} else
		if (!resumido) {
			for (int i = 0; i < valores.length; i++) {
				String nombre = dameStringRecurso(i);
				HashMap<String, Double> vals = new HashMap<String, Double>();
				double sumando = 0;
				int anio = anioIni;
				int cambiador = 0;
				for (int j = pasoIni; j <= pasoFin; j++) {
					if (cambiosAnio.size() > 0) {
						if (j == cambiosAnio.get(cambiador)) {
							cambiador += 1;
							vals.put(Integer.toString(anio), sumando);
							sumando = 0;
							anio += 1;
						}
					}
					sumando += valores[i][j];
				}
				vals.put(Integer.toString(anio), sumando);
				resultado.put(nombre, vals);
			}
		} else if (resumido) {
			for (int i = 0; i < valores.length; i++) {
				HashMap<String, Double> vals = new HashMap<String, Double>();
				for (int j = 0; j < valores[i].length - 1; j++) {
					vals.put(Integer.toString(this.aniosCorrida[j]), valores[i][j]);
				}
				resultado.put(this.eResumen.nombreIndice(i), vals);
			}
		}

		return resultado;
	}

	private HashMap<String, HashMap<String, Double>> generarValoresPorPaso(double[][] valores, int anioIni, int anioFin,
			boolean porTecnologia) {

		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();
		int pasoIni = corrida.dameNumeroPasoInicioAnio(anioIni);
		int pasoFin = corrida.dameNumeroPasoFinAnio(anioFin);

		HashMap<String, HashMap<String, Double>> resultado = new HashMap<String, HashMap<String, Double>>();
//		if (porTecnologia) {
//
//		} else {
		for (int i = 0; i < valores.length; i++) {
			String nombre = dameStringRecurso(i);
			HashMap<String, Double> vals = new HashMap<String, Double>();

			for (int j = pasoIni; j <= pasoFin; j++) {

				vals.put(Integer.toString(j), valores[i][j]);
			}

			resultado.put(nombre, vals);
		}
		// }

		return resultado;
	}

	private String dameStringRecurso(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	private DatosGraficaGUI dameGraficoEnergias(int anioInicio, int anioFin) {
		// double[][] energias = this.eResumen.getEner_resumen();
		Hashtable<String, double[]> enerTPaso = this.eResumen.getEnergiaDeTipoPorPasoGWh();

		Hashtable<String, double[]> data = new Hashtable<>();
		for (Entry<String, double[]> entry : enerTPaso.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(Constantes.TER) || entry.getKey().equalsIgnoreCase(Constantes.HID)
					|| entry.getKey().equalsIgnoreCase(Constantes.FALLA)
					|| entry.getKey().equalsIgnoreCase(Constantes.EOLO)
					|| entry.getKey().equalsIgnoreCase(Constantes.FOTOV)
					|| entry.getKey().equalsIgnoreCase(Constantes.IMPOEXPO)) {
				data.put(entry.getKey(), entry.getValue());
			}
		}

		HashMap<String, HashMap<String, Double>> datos = generarValoresPorTipo(data, anioInicio, anioFin);
		Hashtable<String, ArrayList<Pair<Integer, Double>>> datosGrafica = new Hashtable<String, ArrayList<Pair<Integer, Double>>>();

		DatosGraficaGUI res = new DatosGraficaGUI("Resumen de Energías Anuales", "Año", "Años", "MWh", "Energía",
				datosGrafica, DatosGraficaGUI.GRAF_AREAS);

		Set<String> keys = datos.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			String nombreSerie = it.next();

			ArrayList<Pair<Integer, Double>> serie = new ArrayList<Pair<Integer, Double>>();

			Set<String> keysserie = ((HashMap<String, Double>) (datos.get(nombreSerie))).keySet();
			Iterator<String> itserie = keysserie.iterator();

			while (itserie.hasNext()) {
				String clave = itserie.next();

				Integer indice = Integer.valueOf(clave);
				Double val = datos.get(nombreSerie).get(clave);
				serie.add(new Pair<Integer, Double>(indice, val));
			}
			datosGrafica.put(nombreSerie, serie);
		}

		return res;
	}

	private HashMap<String, HashMap<String, Double>> generarValoresPorTipo(Hashtable<String, double[]> valores,
			int anioIni, int anioFin) {

		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();

		ArrayList<Integer> cambiosAnio = corrida.dameCambiosDeAnio(anioIni, anioFin);
		int pasoInicio = corrida.dameNumeroPasoInicioAnio(anioIni);
		int pasoFin = corrida.dameNumeroPasoFinAnio(anioFin);

		HashMap<String, HashMap<String, Double>> resultado = new HashMap<String, HashMap<String, Double>>();

		ArrayList<String> anios = new ArrayList<String>(valores.keySet());
		Collections.sort(anios);

		for (String clave : anios) {
			int indCambio = 0;
			double[] valsAnio = valores.get(clave);
			double sumaEner = 0;
			HashMap<String, Double> sumaPorAnio = new HashMap<String, Double>();
			int anio = anioIni;
//				for (int i = 0; i < valsAnio.length; i++) {
			for (int i = pasoInicio; i <= pasoFin; i++) {
				sumaEner += valsAnio[i];

				if (indCambio < cambiosAnio.size() && cambiosAnio.size() > 0 && i == cambiosAnio.get(indCambio)) {
					sumaPorAnio.put(Integer.toString(anio), sumaEner);
					anio++;
					indCambio++;
					sumaEner = 0;
				}
			}
			sumaPorAnio.put(Integer.toString(anio), sumaEner);
			resultado.put(clave, sumaPorAnio);
		}
		return resultado;

	}

	private DatosGraficaGUI dameGraficoCostos(int anioInicio, int anioFin, boolean porTecnologia) {
		Hashtable<String, double[]> costos = this.eResumen.getCostoDeTipoPorPasoMUSD();

		Hashtable<String, double[]> data = new Hashtable<>();
		for (Entry<String, double[]> entry : costos.entrySet()) {
			if (!entry.getKey().equalsIgnoreCase(Constantes.DEM)) {
				data.put(entry.getKey(), entry.getValue());
			}
		}

		HashMap<String, HashMap<String, Double>> datos = generarValoresPorTipo(data, anioInicio, anioFin);
		Hashtable<String, ArrayList<Pair<Integer, Double>>> datosGrafica = new Hashtable<String, ArrayList<Pair<Integer, Double>>>();

		DatosGraficaGUI res = new DatosGraficaGUI("Resumen de Costos Anuales", "Año", "Años", "MUSD", "Costos",
				datosGrafica, DatosGraficaGUI.GRAF_AREAS);

		Set<String> keys = datos.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			String nombreSerie = it.next();

			ArrayList<Pair<Integer, Double>> serie = new ArrayList<Pair<Integer, Double>>();

			Set<String> keysserie = ((HashMap<String, Double>) (datos.get(nombreSerie))).keySet();
			Iterator<String> itserie = keysserie.iterator();

			while (itserie.hasNext()) {
				String clave = itserie.next();

				Integer indice = Integer.valueOf(clave);
				Double val = datos.get(nombreSerie).get(clave);
				serie.add(new Pair<Integer, Double>(indice, val));
			}
			datosGrafica.put(nombreSerie, serie);
		}

		return res;
	}

	public ArrayList<DatosEPPUnEscenario> getDatosEscenarios() {
		return datosEscenarios;
	}

	public void setDatosEscenarios(ArrayList<DatosEPPUnEscenario> datosEscenarios) {
		this.datosEscenarios = datosEscenarios;
	}


	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
		this.aniosCorrida = corrida.getLineaTiempo().getAniosCorrida();
		this.pasosAnio = corrida.getLineaTiempo().getPasosAnio();
	}

	public DatosTotalReportes getDatosReportes() {
		return datosReportes;
	}

	public void setDatosReportes(DatosTotalReportes datosReportes) {
		this.datosReportes = datosReportes;
	}

	public EscritorResumenSimulacionParalelo geteResumen() {
		return eResumen;
	}

	public void seteResumen(EscritorResumenSimulacionParalelo eResumen) {
		this.eResumen = eResumen;
	}

	public int[] getAniosCorrida() {
		return aniosCorrida;
	}

	public void setAniosCorrida(int[] aniosCorrida) {
		this.aniosCorrida = aniosCorrida;
	}

	public Hashtable<Integer, ArrayList<Integer>> getPasosAnio() {
		return pasosAnio;
	}

	public void setPasosAnio(Hashtable<Integer, ArrayList<Integer>> pasosAnio) {
		this.pasosAnio = pasosAnio;
	}

	public Corrida getCorrida() {
		return corrida;
	}

	public static void setInstance(ConstructorReportes instance) {
		ConstructorReportes.instance = instance;
	}

}
