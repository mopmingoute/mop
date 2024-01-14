/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCorrida is part of MOP.
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

package datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import tiempo.Evolucion;
import tiempo.EvolucionPorCaso;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesSalida.DatosParamSalida;
import datatypesSalida.DatosParamSalidaOpt;
import datatypesSalida.DatosParamSalidaSim;
import datatypesTiempo.DatosLineaTiempo;

/**
 * Datatype que representa todos los datos de una corrida
 * @author ut602614
 *
 */

/**
 * @author ut601781
 * Se modifica para que implemente Serializable
 *
 */
public class DatosCorrida implements Serializable{
	private static final long serialVersionUID = 1L;
	private String nombre;											/**Nombre de la corrida*/
	
	private String descripcion;										/**Descripción de la corrida*/
	private String postizacion; 									/**En el caso de externa es la ruta y en caso de ser interna el tipo (monótona o cronológica)*/
	private String tipoPostizacion;									/**Puede ser externa o interna*/
	private String tipoSimulacion;
	private String valPostizacion;									/**En el caso de externa es la ruta*/
	private String tipoValpostizacion;								/**Puede ser externa o interna*/
	private String ruta;											/**Ruta XML*/
	private String inicioCorrida;
	private String finCorrida;
	private Double semilla;
	private Double tasa;
	private double topeSpot; // en USD/MWh
	private boolean despSinExp;  // si es true una de las iteraciones de la simulación se usará para obtener un despacho sin exportación
	private int iteracionSinExp; // si despSinExp = true, es la iteración, empezando de 1, que se usa para estimar el despacho sin exportación
	private ArrayList<String> paisesACortar; // Los destinos a los que se anulan las exportaciones en la corrida sin exportación 
	private String rutaSals;
	private Integer cantEscenarios;
	private Evolucion<Integer> cantSorteosMont;									/**Cantidad de sorteos para la optimización (montecarlitos)**/
	private Hashtable<String,Evolucion<String>> valoresComportamientoGlobal;			/**Lista de valores de comportamiento para cada variable de comportamiento*/
	private DatosIteracionesCorrida datosIteraciones;				/**Datos de las iteraciones asociadas a cada paso de la corrida*/
	private DatosTermicosCorrida termicos;							/**Datos de los tórmicos*/
	private DatosCiclosCombinadosCorrida ccombinados;							/**Datos de los tórmicos*/
	private DatosHidraulicosCorrida hidraulicos;					/**Datos de los hidróulicos*/
	private DatosEolicosCorrida eolicos;							/**Datos de los eólicos*/
	private DatosFotovoltaicosCorrida fotovoltaicos;			    /**Datos de los fotovoltaicos*/
	private DatosImpoExposCorrida impoExpos;
	private DatosDemandasCorrida demandas;							/**Datos de las demandas*/
	private DatosFallasEscalonadasCorrida fallas;					/**Datos de las fallas*/
	private DatosImpactosCorrida impactos;
	private DatosContratosEnergiaCorrida contratosEnergia;
	
	private DatosRedElectricaCorrida red;							/**Datos de la red elóctrica*/
	private DatosCombustiblesCorrida combustibles;					/**Datos de las redes de combustibles*/
	private DatosAcumuladoresCorrida acumuladores;
	private DatosConvertidoresCorrida convertidores;
	private DatosLineaTiempo lineaTiempo;
	private Hashtable<String,DatosProcesoEstocastico> procesosEstocasticos;
	private DatosParamSalida datosParamSalida;
	private DatosParamSalidaOpt datosParamSalidaOpt;
	private DatosParamSalidaSim datosParamSalidaSim;
	private Boolean escenariosSerializados;
	private Boolean costosVariables = true;



	/**
	 * Esta lista le da visibilidad de las EvolucionPorCaso a la corrida base del Estudio, en el caso de que
	 * se quiera emplear un estudio para generar muchas corridas a partir de una corrida base.
	 */
	private ArrayList<EvolucionPorCaso> evolucionesPorCaso;
	private Integer clusters;



	public DatosCorrida() {
		setValoresComportamientoGlobal(new Hashtable<String,Evolucion<String>>());
		demandas = new DatosDemandasCorrida();
		datosIteraciones = new DatosIteracionesCorrida();
		termicos = new DatosTermicosCorrida();
		hidraulicos = new DatosHidraulicosCorrida();
		eolicos = new DatosEolicosCorrida();
		fotovoltaicos = new DatosFotovoltaicosCorrida();
		impoExpos = new DatosImpoExposCorrida();
		fallas = new DatosFallasEscalonadasCorrida();
		acumuladores = new DatosAcumuladoresCorrida();
		red =  new DatosRedElectricaCorrida();
		combustibles = new DatosCombustiblesCorrida();
		convertidores = new DatosConvertidoresCorrida();
		lineaTiempo = new DatosLineaTiempo();
		procesosEstocasticos = new Hashtable<String, DatosProcesoEstocastico>();
		datosParamSalida = new DatosParamSalida();
		impactos = new DatosImpactosCorrida();
		contratosEnergia = new DatosContratosEnergiaCorrida();
		evolucionesPorCaso = new ArrayList<EvolucionPorCaso>();
		ccombinados = new DatosCiclosCombinadosCorrida();
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


	public Double getTasa() {
		return tasa;
	}

	public void setTasa(Double tasa) {
		this.tasa = tasa;
	}

	public DatosIteracionesCorrida getDatosIteraciones() {
		return datosIteraciones;
	}
	public void setDatosIteraciones(DatosIteracionesCorrida datosIteraciones) {
		this.datosIteraciones = datosIteraciones;
	}
	public DatosTermicosCorrida getTermicos() {
		return termicos;
	}
	public void setTermicos(DatosTermicosCorrida termicos) {
		this.termicos = termicos;
	}
	public DatosHidraulicosCorrida getHidraulicos() {
		return hidraulicos;
	}
	public void setHidraulicos(DatosHidraulicosCorrida hidraulicos) {
		this.hidraulicos = hidraulicos;
	}
	public DatosEolicosCorrida getEolicos() {
		return eolicos;
	}
	public void setEolicos(DatosEolicosCorrida eolicos) {
		this.eolicos = eolicos;
	}
	public DatosFotovoltaicosCorrida getFotovoltaicos() {
		return fotovoltaicos;
	}
	public void setFotovoltaicos(DatosFotovoltaicosCorrida fotovoltaicos) {
		this.fotovoltaicos = fotovoltaicos;
	}

	public DatosImpoExposCorrida getImpoExpos() {
		return impoExpos;
	}

	public void setImpoExpos(DatosImpoExposCorrida impoExpos) {
		this.impoExpos = impoExpos;
	}

	public DatosDemandasCorrida getDemandas() {
		return demandas;
	}
	public void setDemandas(DatosDemandasCorrida demandas) {
		this.demandas = demandas;
	}
	public DatosFallasEscalonadasCorrida getFallas() {
		return fallas;
	}
	public void setFallas(DatosFallasEscalonadasCorrida fallas) {
		this.fallas = fallas;
	}
	public DatosRedElectricaCorrida getRed() {
		return red;
	}
	public void setRed(DatosRedElectricaCorrida red) {
		this.red = red;
	}
	public DatosCombustiblesCorrida getCombustibles() {
		return combustibles;
	}
	public void setCombustibles(DatosCombustiblesCorrida combustibles) {
		this.combustibles = combustibles;
	}
	public DatosConvertidoresCorrida getConvertidores() {
		return convertidores;
	}
	public void setConvertidores(DatosConvertidoresCorrida convertidores) {
		this.convertidores = convertidores;
	}
	public String imprimir() {
		return this.toString();

	}

	public DatosLineaTiempo getLineaTiempo() {
		return lineaTiempo;
	}

	public void setLineaTiempo(DatosLineaTiempo lineaTiempo) {
		this.lineaTiempo = lineaTiempo;
	}

	public String getPostizacion() {
		return postizacion;
	}

	public void setPostizacion(String postizacion) {
		this.postizacion = postizacion;
	}

	public String getInicioCorrida() {
		return inicioCorrida;
	}

	public void setInicioCorrida(String inicioCorrida) {
		this.inicioCorrida = inicioCorrida;
	}

	public String getFinCorrida() {
		return finCorrida;
	}

	public void setFinCorrida(String finCorrida) {
		this.finCorrida = finCorrida;
	}

	public Hashtable<String,DatosProcesoEstocastico> getProcesosEstocasticos() {
		return procesosEstocasticos;
	}

	public void setProcesosEstocasticos(Hashtable<String,DatosProcesoEstocastico> procesosEstocasticos) {
		this.procesosEstocasticos = procesosEstocasticos;
	}

	public Hashtable<String,Evolucion<String>> getValoresComportamientoGlobal() {
		return valoresComportamientoGlobal;
	}

	public void setValoresComportamientoGlobal(
			Hashtable<String,Evolucion<String>> valoresComportamientoGlobal) {
		this.valoresComportamientoGlobal = valoresComportamientoGlobal;
	}



	public String getTipoSimulacion() {
		return tipoSimulacion;
	}

	public void setTipoSimulacion(String tipoSimulacion) {
		this.tipoSimulacion = tipoSimulacion;
	}

	public String getTipoPostizacion() {
		return tipoPostizacion;
	}

	public void setTipoPostizacion(String tipoPostizacion) {
		this.tipoPostizacion = tipoPostizacion;
	}

	public String getTipoValpostizacion() {
		return tipoValpostizacion;
	}

	public void setTipoValpostizacion(String tipoValpostizacion) {
		this.tipoValpostizacion = tipoValpostizacion;
	}


	public String getValPostizacion() {
		return valPostizacion;
	}

	public void setValPostizacion(String valPostizacion) {
		this.valPostizacion = valPostizacion;
	}

	public Integer getCantEscenarios() {
		return cantEscenarios;
	}

	public void setCantEscenarios(Integer cantEscenarios) {
		this.cantEscenarios = cantEscenarios;
	}

	public Double getSemilla() {
		return semilla;
	}

	public void setSemilla(Double semilla) {
		this.semilla = semilla;
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

	public void setRutaSals(String rutaSals) {
		this.rutaSals = rutaSals;
	}

	public String getRutaSals() {
		return rutaSals;
	}

	public Evolucion<Integer> getCantSorteosMont() {
		return cantSorteosMont;
	}

	public void setCantSorteosMont(Evolucion<Integer> cantSorteosMont) {
		this.cantSorteosMont = cantSorteosMont;
	}

	public DatosAcumuladoresCorrida getAcumuladores() {
		return acumuladores;
	}

	public void setAcumuladores(DatosAcumuladoresCorrida acumuladores) {
		this.acumuladores = acumuladores;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public DatosImpactosCorrida getImpactos() {
		return impactos;
	}

	public void setImpactos(DatosImpactosCorrida impactos) {
		this.impactos = impactos;
	}

	public DatosContratosEnergiaCorrida getContratosEnergia() {
		return contratosEnergia;
	}

	public void setContratosEnergia(DatosContratosEnergiaCorrida contratosEnergia) {
		this.contratosEnergia = contratosEnergia;
	}


	public ArrayList<String> getListaParticipantes(){
		ArrayList<String> res = new ArrayList<>();

		res.addAll(termicos.getTermicos().keySet());
		res.addAll(hidraulicos.getHidraulicos().keySet());
		res.addAll(eolicos.getEolicos().keySet());
		res.addAll(fotovoltaicos.getFotovoltaicos().keySet());
		res.addAll(impoExpos.getImpoExpos().keySet());
		res.addAll(demandas.getDemandas().keySet());
		res.addAll(fallas.getFallas().keySet());
		res.addAll(impactos.getImpactos().keySet());
		res.addAll(contratosEnergia.getContratosEnergia().keySet());
//		res.addAll(red.getR);
		res.addAll(combustibles.getCombustibles().keySet());
		res.addAll(acumuladores.getAcumuladores().keySet());
		res.addAll(convertidores.getConvertidores().keySet());

		return res;
	}

//	public void setEscenariosSerializados(Boolean escenariosSerializados) {
//		this.escenariosSerializados=escenariosSerializados;
//	}
//	public Boolean getEscenariosSerializados() {
//		return escenariosSerializados;
//	}

	public ArrayList<EvolucionPorCaso> getEvolucionesPorCaso() {
		return evolucionesPorCaso;
	}

	public void setEvolucionesPorCaso(ArrayList<EvolucionPorCaso> evolucionesPorCaso) {
		this.evolucionesPorCaso = evolucionesPorCaso;
	}

	public void setClusters(Integer cantClusters) {
		this.clusters=cantClusters;

	}

	public Integer getClusters() {
		return clusters;
	}

	public DatosCiclosCombinadosCorrida getCcombinados() {
		return ccombinados;
	}

	public void setCcombinados(DatosCiclosCombinadosCorrida ccombinados) {
		this.ccombinados = ccombinados;
	}
	public Boolean getCostosVariables() {
		return costosVariables;
	}

	public void setCostosVariables(Boolean costosVariables) {
		this.costosVariables = costosVariables;
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
	
	

	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();

		if(nombre.trim().equals("")) errores.add("Corrida: Nombre vacío.");
		if(descripcion.trim().equals("")) errores.add("Corrida: Descripción vacío.");
		//if(postizacion.trim().equals("")) errores.add("Corrida: Postización vacío.");
		if(tipoPostizacion.trim().equals("")) errores.add("Corrida: Tipo postización vacío.");
		if(tipoSimulacion.trim().equals("")) errores.add("Corrida: Tipo simulación vacío.");
		//if(valPostizacion.trim().equals("")) errores.add("Corrida: Valpostización vacío.");
		if(tipoValpostizacion.trim().equals("")) errores.add("Corrida: Tipo valpostización vacío.");
		if(ruta.trim().equals("")) errores.add("Corrida: Ruta vacío.");
		if(inicioCorrida.trim().equals("")) errores.add("Corrida: Inicio corrida vacío.");
		if(finCorrida.trim().equals("")) errores.add("Corrida: Fin corrida vacío.");

		if( semilla == null) errores.add("Corrida: semilla vacía.");
		if( tasa == null) errores.add("Corrida: tasa vacía.");

		if(rutaSals.trim().equals("")) errores.add("Corrida: rutaSals  vacío.");

		if( cantEscenarios == null) errores.add("Corrida: cantEscenarios vacío.");


		if( cantSorteosMont == null) errores.add("Corrida: cantSorteosMont vacío.");
		if( valoresComportamientoGlobal == null || valoresComportamientoGlobal.size() == 0) errores.add("Corrida: valoresComportamientoGlobal vacío.");

		errores.addAll(datosIteraciones.controlDatosCompletos());
		errores.addAll(termicos.controlDatosCompletos());
		errores.addAll(ccombinados.controlDatosCompletos());
		errores.addAll(hidraulicos.controlDatosCompletos());
		errores.addAll(eolicos.controlDatosCompletos());
		errores.addAll(fotovoltaicos.controlDatosCompletos());
		errores.addAll(impoExpos.controlDatosCompletos());
		errores.addAll(demandas.controlDatosCompletos());
		errores.addAll(fallas.controlDatosCompletos());
		errores.addAll(impactos.controlDatosCompletos());
		errores.addAll(contratosEnergia.controlDatosCompletos());
		errores.addAll(red.controlDatosCompletos());
		errores.addAll(combustibles.controlDatosCompletos());
		errores.addAll(acumuladores.controlDatosCompletos());
		errores.addAll(convertidores.controlDatosCompletos());
		errores.addAll(lineaTiempo.controlDatosCompletos());

		if( procesosEstocasticos == null || procesosEstocasticos.size() == 0) errores.add("Corrida: procesosEstocasticos vacío.");

		if( datosParamSalida == null) errores.add("Corrida: semilla vacía.");
		if( datosParamSalidaOpt == null) errores.add("Corrida: semilla vacía.");
		if( datosParamSalidaSim == null) errores.add("Corrida: semilla vacía.");

		return errores;
	}

	public void setEscenariosSerializados(Boolean escenariosSerializados) {
		this.escenariosSerializados=escenariosSerializados;
		
	}

	public Boolean getEscenariosSerializados() {
		return escenariosSerializados;
	}
}
