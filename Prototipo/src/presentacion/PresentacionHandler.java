/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PresentacionHandler is part of MOP.
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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import logica.CorridaHandler;
import optimizacion.ResOptim;
import parque.Corrida;
import persistencia.EscritorResumenSimulacionParalelo;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.EvolucionPeriodica;
import tiempo.EvolucionPorInstantes;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.Utilitarios;

import java.util.*;

import datatypes.DatosAcumuladorCorrida;
import datatypes.DatosCicloCombinadoCorrida;
import datatypes.DatosCorrida;
import datatypes.DatosEolicoCorrida;
import datatypes.DatosEspecificacionReporte;
import datatypes.DatosFotovoltaicoCorrida;
import datatypes.DatosHidraulicoCorrida;
import datatypes.DatosImpoExpoCorrida;
import datatypes.DatosMaquinaLT;
import datatypes.DatosReporteGUI;
import datatypes.DatosTermicoCorrida;
import datatypes.DatosUsoMaquinaLT;
import interfaz.*;

/**
 * Clase encargada de manejar la capa de presentación
 * 
 * @author ut602614
 *
 */
public class PresentacionHandler {

	private static PresentacionHandler instance;

	//private CorridaHandler ch;

	private PresentacionHandler() {
		//ch = CorridaHandler.getInstance();
	}

	/** Función del singleton que devuelve siempre la misma instancia */
	public static PresentacionHandler getInstance() {
		if (instance == null)
			instance = new PresentacionHandler();
		return instance;
	}

	public static void deleteInstance() {
		instance = null;
	}
	public DatosCorrida cargarCorridaCliente(String ruta) {
//		DirectoriosYArchivos.creaDirectorio("d:/", "salidasModeloOp");
//		DirectoriosYArchivos.creaDirectorio("d:/salidasModeloOp/","logparalelismo");
//		DirectoriosYArchivos.creaDirectorio("d:/salidasModeloOp/","lp");
//		DirectoriosYArchivos.creaDirectorio("d:/salidasModeloOp/","serializados");
		return CorridaHandler.getInstance().cargarCorridaCliente(ruta);
	}

	public DatosCorrida cargarCorridaServidor() {
		return CorridaHandler.getInstance().cargarCorridaServidor();
	}

	public DatosCorrida cargarCorrida(String ruta, boolean resoptimExterno, boolean grabaCarpetas) {
		DatosCorrida dc = CorridaHandler.getInstance().cargarCorrida(ruta, resoptimExterno, false, false, grabaCarpetas);
		if(dc == null){
			return null;
		}
		DirectoriosYArchivos.crearDirsRuta(dc.getRutaSals());
		DirectoriosYArchivos.creaDirectorio(dc.getRutaSals(),"logparalelismo");
		DirectoriosYArchivos.creaDirectorio(dc.getRutaSals(),"lp");
		DirectoriosYArchivos.creaDirectorio(dc.getRutaSals(),"serializados");
		System.out.println("Se cargó corrida");
		return dc;
	}

	public ArrayList<DatosMaquinaLT> dameDatosGraficoLineaTiempo(DatosCorrida dc) {
		ArrayList<DatosMaquinaLT> maquinas = new ArrayList<>();
		GregorianCalendar inicioTiempo = Utilitarios.stringToGregorianCalendar(dc.getLineaTiempo().getTiempoInicial(), "dd MM yyyy");

		long instanteInicial = dc.getLineaTiempo().getInstanteInicial();

		Hashtable<String, DatosHidraulicoCorrida> hidraulicos = dc.getHidraulicos().getHidraulicos();
		Hashtable<String, DatosTermicoCorrida> termicos = dc.getTermicos().getTermicos();
		Hashtable<String, DatosCicloCombinadoCorrida> cicloscombinados = dc.getCcombinados().getCcombinados();
		Hashtable<String, DatosEolicoCorrida> eolicos = dc.getEolicos().getEolicos();
		Hashtable<String, DatosFotovoltaicoCorrida> fotovoltaicos = dc.getFotovoltaicos().getFotovoltaicos();
		Hashtable<String, DatosAcumuladorCorrida> acumuladores = dc.getAcumuladores().getAcumuladores();
		Hashtable<String, DatosImpoExpoCorrida> impoexpos = dc.getImpoExpos().getImpoExpos();

		maquinas.addAll(generarDatosMaquinasH(hidraulicos, inicioTiempo, instanteInicial));
		maquinas.addAll(generarDatosMaquinasT(termicos, inicioTiempo, instanteInicial));
		maquinas.addAll(generarDatosMaquinasCC(cicloscombinados, inicioTiempo, instanteInicial));
		maquinas.addAll(generarDatosMaquinasE(eolicos, inicioTiempo, instanteInicial));
		maquinas.addAll(generarDatosMaquinasF(fotovoltaicos, inicioTiempo, instanteInicial));
		maquinas.addAll(generarDatosMaquinasA(acumuladores, inicioTiempo, instanteInicial));
		maquinas.addAll(generarDatosMaquinasIE(impoexpos,inicioTiempo,instanteInicial));

		return maquinas;
	}

	private ArrayList<DatosMaquinaLT> generarDatosMaquinasIE(Hashtable<String, DatosImpoExpoCorrida> impoExpos, GregorianCalendar inicioT, long instanteInicial) {
		ArrayList<DatosMaquinaLT> maqs = new ArrayList<>();

		for(var entry : impoExpos.entrySet()){
			DatosMaquinaLT dmlt = new DatosMaquinaLT(entry.getKey()+"(IE)", Text.TIPO_IMPOEXPO);
			int cantModInst = 1;
			if(entry.getValue().getTipoImpoExpo().equals(Constantes.IEEVOL)) {
				Evolucion<ArrayList<Double>> potEvol = entry.getValue().getPotEvol().get(0);
				DatosUsoMaquinaLT dum;
				ArrayList<Long> instantesOrdenados;
				Hashtable<Long, ArrayList<Double>> valorizador;
				if (potEvol instanceof EvolucionConstante) {
					dum = new DatosUsoMaquinaLT(inicioT, null, cantModInst, cantModInst * potEvol.getValor(0).get(0));
					dmlt.getUsos().add(dum);
				} else if (potEvol instanceof EvolucionPorInstantes) {
					instantesOrdenados = ((EvolucionPorInstantes<ArrayList<Double>>) potEvol).getInstantesOrdenados();
					valorizador = ((EvolucionPorInstantes<ArrayList<Double>>) potEvol).getValorizador();
					if (instantesOrdenados.size() == 1) {
						GregorianCalendar fechaInstanteActual = dameTiempo(instantesOrdenados.get(0), inicioT, instanteInicial);
						if(fechaInstanteActual.compareTo( inicioT) < 0){
							fechaInstanteActual = inicioT;
						}
						dum = new DatosUsoMaquinaLT(fechaInstanteActual, null, cantModInst, cantModInst * valorizador.get(instantesOrdenados.get(0)).get(0));
						dmlt.getUsos().add(dum);
					} else {
						for (int i = 1; i <= instantesOrdenados.size() - 1; i++) {
							GregorianCalendar fechaInstanteActual = dameTiempo(instantesOrdenados.get(i - 1), inicioT, instanteInicial);
							GregorianCalendar fechaInstanteSiguiente = dameTiempo(instantesOrdenados.get(i), inicioT, instanteInicial);
							if(fechaInstanteActual.compareTo( inicioT) < 0){
								if(fechaInstanteSiguiente.compareTo(inicioT) < 0){
									continue;
								}else {
									fechaInstanteActual = inicioT;
								}
							}
							if (valorizador.get(instantesOrdenados.get(i - 1)).get(0) > 0) {
								GregorianCalendar fechaFin = (GregorianCalendar) fechaInstanteSiguiente.clone();
								if (fechaFin.get(Calendar.MONTH) == fechaInstanteActual.get(Calendar.MONTH) && fechaFin.get(Calendar.YEAR) == fechaInstanteActual.get(Calendar.YEAR)) {
									fechaFin.set(Calendar.MONTH, fechaInstanteActual.get(Calendar.MONTH));
								} else {
									fechaFin.set(Calendar.DAY_OF_MONTH, 1);
									fechaFin.add(Calendar.DAY_OF_MONTH, -1);
								}
								dum = new DatosUsoMaquinaLT(fechaInstanteActual, fechaFin, cantModInst, cantModInst * valorizador.get(instantesOrdenados.get(i - 1)).get(0));
								if (dum.getPotInst() > 0) {
									dmlt.getUsos().add(dum);
								}
							}
						}
						if (valorizador.get(instantesOrdenados.get(instantesOrdenados.size() - 1)).get(0) > 0) {
							GregorianCalendar fechaInstanteActual = dameTiempo(instantesOrdenados.get(instantesOrdenados.size() - 1), inicioT, instanteInicial);
							dum = new DatosUsoMaquinaLT(fechaInstanteActual, null, cantModInst, cantModInst * valorizador.get(instantesOrdenados.get(instantesOrdenados.size() - 1)).get(0));
							if (dum.getPotInst() > 0) {
								dmlt.getUsos().add(dum);
							}
						}
					}
				}
			} else if(entry.getValue().getTipoImpoExpo().equals(Constantes.IEALEATFORMUL)) {
				//TODO: calcu
			} else if(entry.getValue().getTipoImpoExpo().equals(Constantes.IEALEATPRPOT)) {
				//TODO: calcu
			}

			maqs.add(dmlt);
		}
		return maqs;
	}

	private ArrayList<DatosMaquinaLT> generarDatosMaquinasA(Hashtable<String, DatosAcumuladorCorrida> acumuladores, GregorianCalendar inicioT, long instanteInicial) {
		ArrayList<DatosMaquinaLT> maqs = new ArrayList<>();

		for(var entry : acumuladores.entrySet()){
			DatosMaquinaLT dmlt = new DatosMaquinaLT(entry.getKey()+"(A)", Text.TIPO_ACUMULADOR);
			Evolucion<Integer> cantModInstEV = entry.getValue().getCantModInst();
			Evolucion<Double> potMaxEV = entry.getValue().getPotMax();

			getUsosMaquina(cantModInstEV, potMaxEV, inicioT, instanteInicial, dmlt);
			maqs.add(dmlt);
		}

		return maqs;
	}

	private ArrayList<DatosMaquinaLT> generarDatosMaquinasT(Hashtable<String, DatosTermicoCorrida> termicos, GregorianCalendar inicioT, long instanteInicial) {
		ArrayList<DatosMaquinaLT> maqs = new ArrayList<>();

		for(var entry : termicos.entrySet()){
			DatosMaquinaLT dmlt = new DatosMaquinaLT(entry.getKey()+"(T)", Text.TIPO_TERMICO);
			Evolucion<Integer> cantModInstEV = entry.getValue().getCantModInst();
			Evolucion<Double> potMaxEV = entry.getValue().getPotMax();

			getUsosMaquina(cantModInstEV, potMaxEV, inicioT, instanteInicial, dmlt);
			maqs.add(dmlt);
		}

		return maqs;
	}

	private ArrayList<DatosMaquinaLT> generarDatosMaquinasCC(Hashtable<String, DatosCicloCombinadoCorrida> ciclosCombinados, GregorianCalendar inicioT, long instanteInicial) {
		ArrayList<DatosMaquinaLT> maqs = new ArrayList<>();

		for(var entry : ciclosCombinados.entrySet()){
			DatosMaquinaLT dmltTG = new DatosMaquinaLT(entry.getKey()+"(CC-TG)", Text.TIPO_CICLO_COMBINADO);
			Evolucion<Integer> cantModInstEV = entry.getValue().getDatosTGs().getCantModInst();
			Evolucion<Double> potMaxEV = entry.getValue().getDatosTGs().getPotMax();

			getUsosMaquina(cantModInstEV, potMaxEV, inicioT, instanteInicial, dmltTG);
			maqs.add(dmltTG);


			DatosMaquinaLT dmltCV = new DatosMaquinaLT(entry.getKey()+"(CC-CV)", Text.TIPO_CICLO_COMBINADO);
			Evolucion<Integer> cantModInstEV2 = entry.getValue().getDatosCCs().getCantModInst();
			Evolucion<Double> potMaxEV2 = entry.getValue().getDatosCCs().getPotMax();

			getUsosMaquina(cantModInstEV2, potMaxEV2, inicioT, instanteInicial, dmltCV);
			maqs.add(dmltCV);




		}

		return maqs;
	}

	private ArrayList<DatosMaquinaLT> generarDatosMaquinasE(Hashtable<String, DatosEolicoCorrida> eolos, GregorianCalendar inicioT, long instanteInicial) {
		ArrayList<DatosMaquinaLT> maqs = new ArrayList<>();

		for(var entry : eolos.entrySet()){
			DatosMaquinaLT dmlt = new DatosMaquinaLT(entry.getKey()+"(E)", Text.TIPO_EOLICO);
			Evolucion<Integer> cantModInstEV = entry.getValue().getCantModInst();
			Evolucion<Double> potMaxEV = entry.getValue().getPotMax();

			getUsosMaquina(cantModInstEV, potMaxEV, inicioT, instanteInicial, dmlt);
			maqs.add(dmlt);
		}

		return maqs;
	}

	private ArrayList<DatosMaquinaLT> generarDatosMaquinasF(Hashtable<String, DatosFotovoltaicoCorrida> fotovoltaicos, GregorianCalendar inicioT, long instanteInicial) {
		ArrayList<DatosMaquinaLT> maqs = new ArrayList<>();

		for(var entry : fotovoltaicos.entrySet()){
			DatosMaquinaLT dmlt = new DatosMaquinaLT(entry.getKey()+"(F)", Text.TIPO_SOLAR);
			Evolucion<Integer> cantModInstEV = entry.getValue().getCantModInst();
			Evolucion<Double> potMaxEV = entry.getValue().getPotMax();

			getUsosMaquina(cantModInstEV, potMaxEV, inicioT, instanteInicial, dmlt);
			maqs.add(dmlt);
		}

		return maqs;
	}

	private ArrayList<DatosMaquinaLT> generarDatosMaquinasH(Hashtable<String, DatosHidraulicoCorrida> hidros, GregorianCalendar inicioT, long instanteInicial) {
		ArrayList<DatosMaquinaLT> maqs = new ArrayList<>();

		for(var entry : hidros.entrySet()){
			DatosMaquinaLT dmlt = new DatosMaquinaLT(entry.getKey()+"(H)", Text.TIPO_HIDRAULICO);
			Evolucion<Integer> cantModInstEV = entry.getValue().getCantModInst();
			Evolucion<Double> potMaxEV = entry.getValue().getPotMax();

			getUsosMaquina(cantModInstEV, potMaxEV, inicioT, instanteInicial, dmlt);
			maqs.add(dmlt);
		}

		return maqs;
	}

	private void getUsosMaquina(Evolucion<Integer> cantModInstEV, Evolucion<Double> potMaxEV, GregorianCalendar inicioT, long instanteInicial, DatosMaquinaLT dmlt){
		DatosUsoMaquinaLT dum;
		ArrayList<Long> instantesOrdenados;
		Hashtable valorizador;
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		if(cantModInstEV instanceof EvolucionConstante && potMaxEV instanceof EvolucionConstante) {
			dum = new DatosUsoMaquinaLT(inicioT, null, cantModInstEV.getValor(instanteActual), cantModInstEV.getValor(instanteActual)*potMaxEV.getValor(instanteActual));
			dmlt.getUsos().add(dum);
		}else { 
			if (cantModInstEV instanceof EvolucionConstante && potMaxEV instanceof EvolucionPorInstantes) {
				instantesOrdenados = ((EvolucionPorInstantes<Double>) potMaxEV).getInstantesOrdenados();
				valorizador = ((EvolucionPorInstantes<Double>) potMaxEV).getValorizador();
			} else if (cantModInstEV instanceof EvolucionPorInstantes && potMaxEV instanceof EvolucionConstante) {
				instantesOrdenados = ((EvolucionPorInstantes<Integer>) cantModInstEV).getInstantesOrdenados();
				valorizador = ((EvolucionPorInstantes<Integer>) cantModInstEV).getValorizador();
			} else {				
				instantesOrdenados = null;
				valorizador = null;
			}
			if(instantesOrdenados != null && instantesOrdenados.size() == 1){
				GregorianCalendar fechaInstanteActual = dameTiempo(instantesOrdenados.get(0), inicioT, instanteInicial);
				if(fechaInstanteActual.compareTo( inicioT) < 0){
					fechaInstanteActual = inicioT;
				}
				dum = new DatosUsoMaquinaLT(fechaInstanteActual, null,
						cantModInstEV instanceof EvolucionConstante ? cantModInstEV.getValor(instanteActual) : (int)valorizador.get(instantesOrdenados.get(0)),
						cantModInstEV instanceof EvolucionConstante ? cantModInstEV.getValor(instanteActual) * (double)valorizador.get(instantesOrdenados.get(0)) : potMaxEV.getValor(instanteActual) * (int)valorizador.get(instantesOrdenados.get(0)));
				dmlt.getUsos().add(dum);
			}else if(instantesOrdenados != null){
				for (int i = 1; i <= instantesOrdenados.size() - 1; i++) {
					GregorianCalendar fechaInstanteActual = dameTiempo(instantesOrdenados.get(i - 1), inicioT, instanteInicial);
					GregorianCalendar fechaInstanteSiguiente = dameTiempo(instantesOrdenados.get(i), inicioT, instanteInicial);
					if(fechaInstanteActual.compareTo( inicioT) < 0){
						if(fechaInstanteSiguiente.compareTo(inicioT) < 0){
							continue;
						}else {
							fechaInstanteActual = inicioT;
						}

					}
					if ((cantModInstEV instanceof EvolucionConstante ? (Double) valorizador.get(instantesOrdenados.get(i - 1)) : (Integer) valorizador.get(instantesOrdenados.get(i - 1))) > 0) {
						GregorianCalendar fechaFin = (GregorianCalendar) fechaInstanteSiguiente.clone();
						if (fechaFin.get(Calendar.MONTH) == fechaInstanteActual.get(Calendar.MONTH) && fechaFin.get(Calendar.YEAR) == fechaInstanteActual.get(Calendar.YEAR)) {
							fechaFin.set(Calendar.MONTH, fechaInstanteActual.get(Calendar.MONTH));
						} else {
							fechaFin.set(Calendar.DAY_OF_MONTH, 1);
							fechaFin.add(Calendar.DAY_OF_MONTH, -1);
						}
						dum = new DatosUsoMaquinaLT(fechaInstanteActual, fechaFin,
								cantModInstEV instanceof EvolucionConstante ? cantModInstEV.getValor(instanteActual) : (int) valorizador.get(instantesOrdenados.get(i - 1)),
								cantModInstEV instanceof EvolucionConstante ? cantModInstEV.getValor(instanteActual) * (double) valorizador.get(instantesOrdenados.get(i - 1)) : potMaxEV.getValor(instanteActual) * (int) valorizador.get(instantesOrdenados.get(i - 1)));
						if(dum.getPotInst() > 0) {
							dmlt.getUsos().add(dum);
						}
					}
				}
				if((cantModInstEV instanceof EvolucionConstante ?
						(Double) valorizador.get(instantesOrdenados.get(instantesOrdenados.size() - 1)) :
						(Integer) valorizador.get(instantesOrdenados.get(instantesOrdenados.size() - 1))) > 0){
					GregorianCalendar fechaInstanteActual = dameTiempo(instantesOrdenados.get(instantesOrdenados.size()-1), inicioT, instanteInicial);
					dum = new DatosUsoMaquinaLT(fechaInstanteActual, null,
							cantModInstEV instanceof EvolucionConstante ? cantModInstEV.getValor(instanteActual) :
									(int) valorizador.get(instantesOrdenados.get(instantesOrdenados.size() - 1)),
							cantModInstEV instanceof EvolucionConstante ? cantModInstEV.getValor(instanteActual) * (double) valorizador.get(instantesOrdenados.get(instantesOrdenados.size() - 1)) :
									potMaxEV.getValor(instanteActual) * (int) valorizador.get(instantesOrdenados.get(instantesOrdenados.size() - 1)));
					if(dum.getPotInst() > 0) {
						dmlt.getUsos().add(dum);
					}
				}
			}
		}
	}

	public GregorianCalendar dameTiempo(long instante, GregorianCalendar inicioTiempo, long instanteInicial) {
		long segundosTranscurridos = instante - instanteInicial;
		int diasAd = (int)(segundosTranscurridos/utilitarios.Constantes.SEGUNDOSXDIA);
		int segsAd = (int)(segundosTranscurridos%utilitarios.Constantes.SEGUNDOSXDIA);
		GregorianCalendar resultado = (GregorianCalendar) inicioTiempo.clone();
		resultado.setTime(inicioTiempo.getTime());
		resultado.add(Calendar.DAY_OF_YEAR, diasAd);
		resultado.add(Calendar.SECOND, segsAd);
		return resultado;
	}

	public DatosReporteGUI devolverReporte(DatosEspecificacionReporte especificacion) {

		ConstructorReportes cr = (ConstructorReportes) ConstructorReportes.getInstance();
		return cr.devolverReporte(especificacion);

//		 return ReportesContentController.dummyData();// TODO: 21/07/2020 para testeo, eliminar luego

	}

	public void simular() {
		CorridaHandler.getInstance().simular();
	}

	public void simularProcesosEstocasticos(){
		CorridaHandler.getInstance().simularProcesosEstocasticos();
	}
	
	
	public void despacharCP(String dirEntradas, String dirSalidas) {
		CorridaHandler.getInstance().despacharCP(dirEntradas, dirSalidas);
	}
	

	public void optimizar() {
		CorridaHandler.getInstance().optimizar();

	}

	public void cancelarOptimizacion(){
		CorridaHandler.getInstance().cancelarOptimizacion();
	}

	public void sortearPEOptim() {
		CorridaHandler.getInstance().sortearPEOptim();

	}

	
	public void reprocesarEscenariosCorrida(String rutaDirEscPrevia, String rutaOtrosDatos) {
		CorridaHandler.getInstance().reprocesarEscenariosCorrida(rutaDirEscPrevia, rutaOtrosDatos);
	}
	
	public void escribirSalidaDesdeSerializadoCorrida(String ruta) {
		CorridaHandler.getInstance().simularDesdeDirectorio(ruta);
	}
	

	public void optimizar(String estado, int paso) {
		// CorridaHandler.getInstance().optimizar(estado, paso);

	}

	public boolean hayResoptim() {
		return CorridaHandler.getInstance().hayResoptim();
	}

	public void simular(String ruta) {
		CorridaHandler.getInstance().simular(ruta);

	}
	public void cancelarSimulacion(){
		CorridaHandler.getInstance().cancelarSimulacion();
	}

//
//	public void simularParalelo(String ruta) {
//		CorridaHandler.getInstance().simularParalelo(ruta);
//
//	}
//

	public boolean hayNuevaCorrida() {
		return CorridaHandler.getInstance().hayNuevaCorrida();
	}

	public void optimizarCliente() {
		CorridaHandler.getInstance().optimizarCliente();

	}

	public int dameOperacion() {
		return CorridaHandler.getInstance().dameOperacion();
	}

	public ResOptim optimizarServidor() {
		return CorridaHandler.getInstance().optimizarServidor();
	}

	public void simularCliente() {
		CorridaHandler.getInstance().simularCliente();

	}

	public void simularServidor() {
		CorridaHandler.getInstance().simularServidor();
	}

	public ReadOnlyDoubleProperty optProgressProperty() {
		return CorridaHandler.getInstance().optProgressProperty();
	}

	public ReadOnlyDoubleProperty simProgressProperty() {
		return CorridaHandler.getInstance().simProgressProperty();
	}
	public ReadOnlyBooleanProperty escritorProgressProperty() {
		return CorridaHandler.getInstance().escritorProgressProperty();
	}

	public void registrarMaquina() {
		CorridaHandler.getInstance().registrarMaquina();
	}

	public int damecantServidores() {
		// TODO Auto-generated method stub
		return CorridaHandler.getInstance().damecantServidores();
	}

	public void simularConSerializados(String ruta) {
		CorridaHandler.getInstance().simularClienteDesdeDirectorio(ruta);

	}

	public void cargarEstudio(String ruta) {
		CorridaHandler.getInstance().cargarEstudio(ruta);

	}

	public void ejecutarEstudio() {
		CorridaHandler.getInstance().ejecutarEstudio();

	}

	public void resultadosPorIM(String rutaDirPoste, String rutaNumpos) {
		CorridaHandler ch = CorridaHandler.getInstance();
		CorridaHandler.getInstance().resultadosPorIM(rutaDirPoste,  rutaNumpos);

	}
}
