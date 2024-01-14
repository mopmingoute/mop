/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * HidraulicoCompDesp is part of MOP.
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

package compdespacho;

import java.util.ArrayList;
import java.util.Hashtable;
import parque.GeneradorHidraulico;
import parque.Impacto;
import utilitarios.Constantes;
import utilitarios.Polinomio;
import utilitarios.Recta;
import datatypes.Pair;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProblema.DatosVariableControl;
import datatypesSalida.DatosCurvaOferta;
import datatypesSalida.DatosEPPUnEscenario;
import logica.CorridaHandler;

/**
 * Clase encargada de modelar el comportamiento del generador hidróulico en el
 * problema de despacho
 * 
 * @author ut602614
 * 
 */

public class HidraulicoCompDesp extends GeneradorCompDesp {
	private GeneradorHidraulico gh;

	private static String compLagoPorDefecto;
	private static String compCoefEnergeticoDefecto;
	private String compLago;
	private String compCoefEnergetico;
	private boolean comportamientoEspecifico; // si es true usa algón
												// comportamiento especófico del
												// participante

	private Double potMax;
	private Double aporte;
	private double coefEnergetico; // coeficiente energótico por poste en
									// MW/(m3/s)
	private Double volIni; // volumen inicial en hm3
	private Double qTur1Max;
	private Double factorCompartir;

	private Double volEroMin; // en m3
	private double volEroMax; // en m3
	private Double qverMax;
	private double valAgua;
	private Integer cantModDisp;
	private ArrayList<Recta> funcionPQ; // se usa solo en el comportamiento potencia caudal

//	private boolean tieneCaudalMinEcol;
	// private double[] caudalMinEcol; // caudal mónimo ecológico m3/s aplicable a
	// todos los postes
	// private double penalizacionFaltanteCaudal; // USD/hm3 de penalización

	// Nombres de las variables del problema de despacho
	private String[] nqturp; // turbinados por poste
	private String[] nqerop; // erogados por poste
	private String[] nqFaltEco; // caudal faltante para completar el caudal ecológico por poste
	private String nqeropAlto; // erogados móximos por poste de centrales sin Lago
	private String nqeropBajo; // erogados mónimos por poste de cemtrales sin Lago
	private String nqver; // vertimiento si es ónico
	private String[] nqverp; // vertimiento si es ónico
	private String nvolfin; // volumen final del lago
	private String nVolPenMin; // volumen por debajo del cual se penaliza
	private String nVolPenMax; // volumen por encima del cual se penaliza
	private String nombreRestriccionBalanceLago; // nombre de las restricción del balance
	// private String nombreVEVolEnDespacho; // nombre de la variable de estado
	// volumen final

	private boolean controlApagado; // Si es true, se habilita el apagado por pórdida del salto
	private boolean apagarTurbinado; // Si es true, el turbinado quedó apagado para las siguienes iteraciones
	private double cotaAguasArribaDeCentralAguasAbajo;
	private double cotaAguasArriba;
	private double volfilthm3; // Filtración en hm3 en el paso
	private double volevaphm3; // Evaporación en hm3 en el paso

	private double cotaMax;
	private double[] coefParaRestriccion;

	private double caudalErogadoAnteriorPaso;

	private double caudalErogadoMedioIterAnterior;

	private boolean vertConstante;

	public HidraulicoCompDesp(GeneradorHidraulico gen) {
		super();
		this.setParticipante(gen);
		gh = gen;
		this.parametros = new Hashtable<String, String>();
		nombreRestriccionBalanceLago = generarNombre("BalanceLago");
		nvolfin = generarNombre("volFinal");
		vertConstante = false;
	}

	/**
	 * Se crean las variables de control del hidróulico
	 * 
	 */
	public void crearVariablesControl() {

		String compLago = parametros.get(Constantes.COMPLAGO);
		String compCoefEnerg = parametros.get(Constantes.COMPCOEFENERGETICO);

		this.compLago = compLago;
		this.compCoefEnergetico = parametros.get(Constantes.COMPCOEFENERGETICO);

		coefParaRestriccion = new double[participante.getCantPostes()];

		if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)
				&& (gh.isHayControldeCotasMaximas() || gh.isHayControldeCotasMinimas())) {
			if (this.gh.isHayControldeCotasMinimas()) {
				this.nVolPenMin = generarNombre("volPenMin");

				DatosVariableControl nv = new DatosVariableControl(this.nVolPenMin, Constantes.VCCONTINUA,
						Constantes.VCPOSITIVA, null, null);
				this.variablesControl.put(this.nVolPenMin, nv);
			}
			if (this.gh.isHayControldeCotasMaximas()) {
				this.nVolPenMax = generarNombre("volPenMax");

				DatosVariableControl nv = new DatosVariableControl(this.nVolPenMax, Constantes.VCCONTINUA,
						Constantes.VCPOSITIVA, null, null);
				this.variablesControl.put(this.nVolPenMax, nv);
			}
		}

		// crear potp
		for (int ip = 0; ip < participante.getCantPostes(); ip++) {
			coefParaRestriccion[ip] = coefEnergetico;
			String nombre = generarNombre("pot", Integer.toString(ip));
			// Restricción de cota móxima para cada poste 2.11.3.1
			cotaMax = 0.0;
			if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
				cotaMax = cantModDisp * Math.min(potMax, qTur1Max * coefEnergetico) * this.getFactorCompartir();
			} else {
				cotaMax = cantModDisp * potMax * this.getFactorCompartir();
			}
			if (cotaMax < 0)
				cotaMax = 0;

			DatosVariableControl nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA,
					null, cotaMax);
			this.variablesControl.put(nombre, nv);
			getNpotp()[ip] = nombre;
		}

		if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {

			DatosVariableControl nv = null;
			String nombreAlto = generarNombre("qeroAlto");
			nv = new DatosVariableControl(nombreAlto, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
					Constantes.INFNUESTRO);
			this.variablesControl.put(nombreAlto, nv);
			nqeropAlto = nombreAlto;

			String nombreBajo = generarNombre("qeroBajo");
			nv = new DatosVariableControl(nombreBajo, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
					Constantes.INFNUESTRO);
			this.variablesControl.put(nombreBajo, nv);
			nqeropBajo = nombreBajo;

			for (int ip = 0; ip < participante.getCantPostes(); ip++) {
				// crear qerop
				// Restricción de erogado móximo del generador con lago por poste 2.12.3.1
				String nombre = generarNombre("qero", Integer.toString(ip));
				if (compLago.equalsIgnoreCase(Constantes.HIDROSINLAGO)) {
					nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
							Constantes.INFNUESTRO);
				} else if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
					nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
							qTur1Max * cantModDisp + qverMax);
				}
				this.variablesControl.put(nombre, nv);
				nqerop[ip] = nombre;

			}

		} else if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
			// crear qver
			// Restricción de vertimiento móximo qVerMax en centrales con lago 2.11.3.6
			String nombre = generarNombre("qver");
			nqver = nombre;
			DatosVariableControl nv = null;
			if (compLago.equalsIgnoreCase(Constantes.HIDROSINLAGO)) {
				nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null, null);

			} else if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
				if (qverMax == 0) {
					nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
							Constantes.EPSILONCOEF);
				} else {
					nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null, qverMax);
				}
			}
//			nVertExterno=generarNombre("vertExterno");
//			
//			DatosVariableControl nve = new DatosVariableControl(nVertExterno, Constantes.VCCONTINUA, Constantes.VCLIBRE, null, null);
//			
//			
//			this.variablesControl.put(nVertExterno, nve);
			this.variablesControl.put(nombre, nv);
			setNqver(nombre);

		}
//	&& (compValoresBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)
		// || (gh.isHayControldeCotasMaximas() || gh.isHayControldeCotasMinimas()))
		if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			// crear volfinal
			String nombre = nvolfin;

			DatosVariableControl nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA,
					null, Constantes.INFNUESTRO);
			this.variablesControl.put(nombre, nv);
			this.setNvolfin(nombre);
		}
	}

	// A continuación se escriben los mótodos que cargan las restricciones del
	// hidróulico
	/**
	 * Restricción de volumen erogado mónimo en el paso 2.11.3.3
	 * 
	 * @param divisorEscalamiento
	 */
	private void cargarRestEroMin(double divisorEscalamiento) {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		DatosRestriccion nr = new DatosRestriccion();
		for (int p = 0; p < gh.getCantPostes(); p++) {
			nr.agregarTermino(getNpotp()[p], gh.getDuracionPostes(p) / coefEnergetico
					/ gh.getFactorCompartir().getValor(instanteActual) / divisorEscalamiento);
		}
		nr.agregarTermino(nqver, (double) gh.getDuracionPaso() / divisorEscalamiento);
		nr.setNombre(generarNombre("eroMin"));
//		nr.setSegundoMiembro(volEroMin / divisorEscalamiento + Constantes.EPSILONCOEF);
		nr.setSegundoMiembro(volEroMin / divisorEscalamiento);
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	/**
	 * Restricción de caudal erogado mónimo para cada poste 2.11.3.4
	 * 
	 * @param divisorEscalamiento
	 * @param poste
	 */
	private void cargarRestEroMinPoste(double divisorEscalamiento, int poste) {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		DatosRestriccion nr = new DatosRestriccion();
		nr.agregarTermino(getNpotp()[poste],
				1.0 / coefEnergetico / gh.getFactorCompartir().getValor(instanteActual) / divisorEscalamiento);
		nr.agregarTermino(nqver, 1.0 / divisorEscalamiento);

		nr.setNombre(generarNombre("eroMin_" + poste));
		nr.setSegundoMiembro(volEroMin / divisorEscalamiento / gh.getDuracionPaso());
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	/**
	 * Restricción de caudal ecológico para cada poste 2.11.3.5 La variable
	 * qFaltEcol se utiliza para contabilizar y penalizar el incumplimiento del
	 * caudal ecológico.
	 * 
	 * @param divisorEscalamiento
	 * @param poste
	 */
//	private void cargarRestCaudalEcologico(double divisorEscalamiento, int poste) {
//		DatosRestriccion nr = new DatosRestriccion();
//		nr.agregarTermino(getNpotp()[poste],
//				1.0 / coefEnergetico / gh.getFactorCompartir().getValor(instanteActual) / divisorEscalamiento);
//		nr.agregarTermino(nqver, 1.0 / divisorEscalamiento);
//		nr.agregarTermino(getNqFaltEco()[poste], 1.0 / divisorEscalamiento);
//		nr.setNombre(generarNombre("caudalEcologico_" + poste));
//		nr.setSegundoMiembro(caudalMinEcol[poste] / divisorEscalamiento);
//		nr.setTipo(Constantes.RESTMAYOROIGUAL);
//		this.restricciones.put(nr.getNombre(), nr);
//	}

	/**
	 * Restricción de caudal ecológico para cada poste 2.12.2.5 La variable
	 * qFaltEcol se utiliza para contabilizar y penalizar el incumplimiento del
	 * caudal ecológico.
	 * 
	 * @param divisorEscalamiento
	 * @param poste
	 */
//	private void cargarRestCaudalEcologicoPQ(double divisorEscalamiento, int poste) {
//		DatosRestriccion nr = new DatosRestriccion();
//		nr.agregarTermino(nqerop[poste], 1.0 / divisorEscalamiento);
//		nr.agregarTermino(getNqFaltEco()[poste], 1.0 / divisorEscalamiento);
//		nr.setNombre(generarNombre("caudalEcologicoPQ_" + poste));
//		nr.setSegundoMiembro(caudalMinEcol[poste] / divisorEscalamiento);
//		nr.setTipo(Constantes.RESTMAYOROIGUAL);
//		this.restricciones.put(nr.getNombre(), nr);
//	}

//	/**
//	 * Restricción de volumen erogado mónimo 
//	 * No se utiliza
//	 */
//	private void cargarRestEroMinPQ() {
//		DatosRestriccion nr = new DatosRestriccion();
//		for (int p = 0; p < gh.getCantPostes(); p++) {
//			nr.agregarTermino(nqerop[p], (double) gh.getDuracionPostes(p));
//		}
//		nr.setNombre(generarNombre("eroMin"));
//		nr.setSegundoMiembro(volEroMin);
//		nr.setTipo(Constantes.RESTMAYOROIGUAL);
//		this.restricciones.put(nr.getNombre(), nr);
//	}

	/**
	 * Crea la restricción de volumen móximo erogable de centrales con lago y el
	 * balance de centrales sin lago 2.11.2.2
	 * 
	 * @param operador
	 * @param voldisp             (es el volumen inicial neto de filtración y
	 *                            evaporación estimada) en hm3
	 * @param divisorEscalamiento
	 * @return
	 */
	private DatosRestriccion cargarBalanceLago(int operador, double voldisp, double divisorEscalamiento) {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		double durPaso = (double) gh.getDuracionPaso();
		DatosRestriccion nr = new DatosRestriccion();
		for (int p = 0; p < gh.getCantPostes(); p++) {
			nr.agregarTermino(getNpotp()[p],
					gh.getDuracionPostes(p) / coefEnergetico / this.getFactorCompartir() / divisorEscalamiento);
		}
		nr.agregarTermino(nqver, durPaso / divisorEscalamiento);
		for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
			HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
			for (int p = 0; p < gh.getCantPostes(); p++) {
				nr.agregarTermino(compg.getNpotp()[p], -gh.getDuracionPostes(p) / compg.getCoefEnergetico()
						/ gh.getFactorCompartir().getValor(instanteActual) / divisorEscalamiento);
			}
			nr.agregarTermino(compg.getNqver(), -durPaso / divisorEscalamiento);
		}
		String nombreR = nombreRestriccionBalanceLago;
		nr.setNombre(nombreR);
		// AGREGA CONSTANTE ESPSILONVOLAGUA QUE RELAJA EL BALANCE
		// SE PUEDE FABRICAR HASTA EPSILONVOLAGUA m3
		/*
		 * double volauxm3 = Math.max(0, (voldisp - (volfilthm3 +
		 * volevaphm3))*Constantes.M3XHM3 + aporte*durPaso);
		 * if(gh.isUltimoInfactible()){ nr.setSegundoMiembro((volauxm3 +
		 * Constantes.EPSILONVOLAGUA)/divisorEscalamiento); }else{
		 * nr.setSegundoMiembro(volauxm3/divisorEscalamiento); }
		 */
		
		if (gh.isUltimoInfactible()) {
			if (!(operador == Constantes.RESTIGUAL)) {
				nr.setSegundoMiembro((voldisp * Constantes.M3XHM3 + aporte * durPaso + Constantes.EPSILONVOLAGUA)
						/ divisorEscalamiento);
			}
		} else {
			nr.setSegundoMiembro((voldisp * Constantes.M3XHM3 + aporte * durPaso) / divisorEscalamiento);
		}

		nr.setTipo(operador);
		return nr;
	}

	/**
	 * Restricción de erogado móximo en centrales con lago que tengan centrales
	 * aguas abajo 2.11.2.7 Evita inundar las centrales con lago aguas abajo
	 * 
	 * @param divisorEscalamiento
	 */
	private void cargarErogadoMaximo(double divisorEscalamiento) {
		double durPaso = (double) gh.getDuracionPaso();
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("ErogadoMaximo"));

		for (int p = 0; p < gh.getCantPostes(); p++) {
			nr.agregarTermino(getNpotp()[p],
					gh.getDuracionPostes(p) / coefEnergetico / this.getFactorCompartir() / divisorEscalamiento);
		}
		nr.agregarTermino(nqver, durPaso / divisorEscalamiento);
		nr.setTipo(Constantes.RESTMENOROIGUAL);
		nr.setSegundoMiembro((volEroMax) / divisorEscalamiento);
		this.restricciones.put(nr.getNombre(), nr);
	}

//	private void cargarErogadoMaximoPQ(double divisorEscalamiento){
//		DatosRestriccion nr = new DatosRestriccion();
//		for (int p = 0; p < gh.getCantPostes(); p++) {
//			nr.agregarTermino(nqerop[p], (double) gh.getDuracionPostes(p)/ divisorEscalamiento);
//		}
//		nr.setNombre(generarNombre("ErogadoMaximo"));
//		nr.setSegundoMiembro((volEroMax/ divisorEscalamiento));
//		nr.setTipo(Constantes.RESTMENOROIGUAL);
//		this.restricciones.put(nr.getNombre(), nr);
//	}
	/**
	 * Crea la restricción de volumen móximo erogable de centrales con lago y el
	 * balance de centrales sin lago 2.12.3.2
	 * 
	 * @param operador
	 * @param volini
	 */
	private DatosRestriccion cargarBalanceLagoPQ(int operador, double volini) {
		double durPaso = (double) gh.getDuracionPaso();
		DatosRestriccion nr = new DatosRestriccion();
		for (int p = 0; p < gh.getCantPostes(); p++) {
			nr.agregarTermino(nqerop[p], (double) gh.getDuracionPostes(p));
		}
		for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
			HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
			for (int p = 0; p < gh.getCantPostes(); p++) {
				nr.agregarTermino(compg.getNqerop()[p], (double) -gh.getDuracionPostes(p));

			}
		}
		nr.setNombre(nombreRestriccionBalanceLago);
		nr.setSegundoMiembro(durPaso * aporte + volini * Constantes.M3XHM3);
		nr.setTipo(operador);

		return nr;
	}

	/**
	 * Restricción de caudal erogado mónimo para cada poste 2.12.3.3
	 * 
	 * @param poste
	 */
	private void cargarRestEroMinPQPoste(int poste) {
		DatosRestriccion nr = new DatosRestriccion();
		nr.agregarTermino(nqerop[poste], 1.0);
		nr.setNombre(generarNombre("eroMin_" + poste));
		nr.setSegundoMiembro(volEroMin / (double) gh.getDuracionPaso());
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	/**
	 * Restricción de caudal erogado mìnimo por paso PQ 2.12.3.X
	 * 
	 * @param poste
	 */
	private void cargarRestEroMinPQPaso() {
		DatosRestriccion nr = new DatosRestriccion();
		for (int p = 0; p < gh.getCantPostes(); p++) {
			nr.agregarTermino(nqerop[p], (double) gh.getDuracionPostes(p));
		}
		nr.setNombre(generarNombre("eroMin_Paso"));
		nr.setSegundoMiembro(volEroMin);
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	/**
	 * Restricción de erogado móximo 2.12.3.4
	 * 
	 * @param divisorEscalamiento
	 * @param p
	 */
	private void cargarErogadoMaximoPQPoste(double divisorEscalamiento, int p) {
		DatosRestriccion nr = new DatosRestriccion();
		nr.agregarTermino(nqerop[p], 1.0 / divisorEscalamiento);
		nr.setNombre(generarNombre("ErogadoMaximo_" + p));
		nr.setSegundoMiembro((volEroMax / divisorEscalamiento / gh.getDuracionPaso()));
		nr.setTipo(Constantes.RESTMENOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	/**
	 * Restricción de erogado móximo 2.12.3.X
	 * 
	 * @param divisorEscalamiento
	 * @param p
	 */
	private void cargarErogadoMaximoPQPaso() {
		DatosRestriccion nr = new DatosRestriccion();
		for (int p = 0; p < gh.getCantPostes(); p++) {
			nr.agregarTermino(nqerop[p], (double) gh.getDuracionPostes(p));
		}
		nr.setNombre(generarNombre("ErogadoMaximo_Paso"));
		nr.setSegundoMiembro(volEroMax);
		nr.setTipo(Constantes.RESTMENOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	/**
	 * Restricción de eragado alto para centrales sin lago 2.12.3.5
	 * 
	 * @param p
	 */
	private void cargarErogAltoPosteSinLagoPQ(int p) {
		DatosRestriccion nr = new DatosRestriccion();
		nr.agregarTermino(nqerop[p], 1.0);
		nr.agregarTermino(nqeropAlto, -1.0);
		nr.setNombre(generarNombre("ErogAltoSinLago" + p));
		nr.setSegundoMiembro(0.0);
		nr.setTipo(Constantes.RESTMENOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	/**
	 * Restricción de eragado bajo para centrales sin lago 2.12.3.6
	 * 
	 * @param p
	 */
	private void cargarErogBajoPosteSinLagoPQ(int p) {
		DatosRestriccion nr = new DatosRestriccion();
		nr.agregarTermino(nqerop[p], 1.0);
		nr.agregarTermino(nqeropBajo, -1.0);
		nr.setNombre(generarNombre("ErogBajoSinLago" + p));
		nr.setSegundoMiembro(0.0);
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	/**
	 * Restricción de diferencia entre variables erogado alto y bajo fija 2.12.3.7
	 */
	private void cargarVariacionErogPosteSinLagoPQ() {
		DatosRestriccion nr = new DatosRestriccion();
		nr.agregarTermino(nqeropAlto, 1.0);
		nr.agregarTermino(nqeropBajo, -1.0);
		nr.setNombre(generarNombre("ErogDeltaSinLago"));
		nr.setSegundoMiembro(qTur1Max * cantModDisp * Constantes.VARIA_EROGADO_ENTRE_POSTES);
		nr.setTipo(Constantes.RESTMENOROIGUAL);
		this.restricciones.put(nr.getNombre(), nr);
	}

	public void cargarRestricciones() {		 
		/**
		 * TODO: HAY UN LóO PORQUE EL COMPORTAMIENTO CON LAGO NO ES EQUIVALENTE EN TODO
		 * CASO A LA EXISTENCIA DE VARIABLE DE ESTADO ACA SE ESTó COMETIENDO ESE ERROR
		 */

		GeneradorHidraulico gh = (GeneradorHidraulico) this.participante;

		String compLago = parametros.get(Constantes.COMPLAGO);
		String compCoefEnerg = parametros.get(Constantes.COMPCOEFENERGETICO);
		String compValoresBellman = parametros.get(Constantes.COMPVALORESBELLMAN);

		if (controlApagado && calcularSalto() <= gh.getSaltoMin()) {
			apagarTurbinado = true;
			String textoCatastrofe = "La central " + gh.getNombre() + " tiene cota aguas arriba: "
					+ this.getCotaAguasArriba() + " y cota aguas abajo: "
					+ this.dameCotaAguasAbajo(caudalErogadoMedioIterAnterior, cotaAguasArribaDeCentralAguasAbajo)
					+ " en el escenario: " + gh.getSimPaso().getEscenario() + " con aporte: "
					+ gh.getAporte().getValor() + " y erogado : " + caudalErogadoMedioIterAnterior + "m3/s en el paso: "
					+ gh.getSimPaso().getPaso();

			if (Constantes.NIVEL_CONSOLA > 1) {
				System.out.println("APAGA TURBINADO " + gh.getNombre()
						+ "****************************************************!!!!!!!!!!!!!!!!!!!");
				System.out.println(textoCatastrofe);
			}
		}

		if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
			// COEFICIENTE ENERGETICO CONSTANTE
			if (!(apagarTurbinado && qverMax <= Constantes.EPSILONCAUDALAGUA)) {
				// La central puede turbinar o verter una cantidad no despreciable
				if (compLago.equalsIgnoreCase(Constantes.HIDROSINLAGO)) {
					// La central no tiene lago
					// cargar 2.11.3.2 con igual y volini 0
					DatosRestriccion nr = cargarBalanceLago(Constantes.RESTIGUAL, 0.0, 1);
					this.restricciones.put(nr.getNombre(), nr);

				} else {
					// La central tiene lago
//					DatosRestriccion nr = cargarBalanceLago(Constantes.RESTMENOROIGUAL, volIni-volevaphm3-volfilthm3, 1);
					// cargar 2.11.3.2 tal cual
					DatosRestriccion nr = null;
					if (compValoresBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
						nr = cargarBalanceLago(Constantes.RESTIGUAL, Math.max(0.0, volIni - volevaphm3 - volfilthm3),
								1);
						nr.agregarTermino(nvolfin, 1.0);
					} else if (compValoresBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
						nr = cargarBalanceLago(Constantes.RESTMENOROIGUAL,
								Math.max(0.0, volIni - volevaphm3 - volfilthm3), 1);
						nr.agregarTermino(nvolfin, 1.0);
					}

					this.restricciones.put(nr.getNombre(), nr);

					cargarRestEroMin(1.0);

				}
			}

			if (apagarTurbinado) {
				for (int p = 0; p < gh.getCantPostes(); p++) {
					DatosRestriccion nr = new DatosRestriccion();
					nr.agregarTermino(npotp[p], 1.0);
					nr.setNombre(generarNombre("apagarTurbinado", Integer.toString(p)));
					nr.setSegundoMiembro(0.0);
					nr.setTipo(Constantes.RESTIGUAL);
					this.restricciones.put(nr.getNombre(), nr);
				}
			}
			// Restricción de volumen erogado móximo en centrales con lago que tengan
			// centrales aguas abajo 2.11.3.7
			if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO) && gh.getGeneradorAbajo() != null)
				cargarErogadoMaximo(1.0);
			// Restricción de caudal ecológico 2.11.3.5

//			DatosRestriccion vertexterno = new DatosRestriccion();
//			vertexterno.setNombre(generarNombre("vex"));
//			vertexterno.agregarTermino(this.nVertExterno, 1.0);
//			vertexterno.agregarTermino(this.nqver, -1.0);
//			vertexterno.setSegundoMiembro(0.0);
//			vertexterno.setTipo(Constantes.RESTIGUAL);			
//			this.restricciones.put(vertexterno.getNombre(),vertexterno);
////			
//			DatosRestriccion paraDual = new DatosRestriccion();
//			paraDual.setNombre(generarNombre("vexCaja"));
//			paraDual.agregarTermino(this.nVertExterno, 1.0);
//			paraDual.setSegundoMiembro(0.0);
//			paraDual.setTipo(Constantes.RESTMAYOROIGUAL);			
//			this.restricciones.put(paraDual.getNombre(),paraDual);
//			DatosRestriccion vertexterno = new DatosRestriccion();
//			vertexterno.setNombre(generarNombre("vex"));
//			vertexterno.agregarTermino(this.nqver, 1.0);
//			double cotaInferior = 0.0;
//			double maximoARecibir = 0.0;
//			double vertObligatorio = 0.0;
//			if (gh.getNombre().equalsIgnoreCase("baygorria")) {
//				double volumenArriba = 0.0;
//				if (gh.getSimPaso().isSimulando()) {
//					volumenArriba = gh.getGeneradoresArriba().get(0).getVolumen().getEstado();
//				} else {
//					volumenArriba = gh.getGeneradoresArriba().get(0).getVolumenOpt().getEstado();
//				}
//				
//				 maximoARecibir = volumenArriba*1000000/168/3600;
//				 vertObligatorio = 0.0;
//				 
//			}
//			vertexterno.setSegundoMiembro(Math.min(vertObligatorio, maximoARecibir));
//			vertexterno.setTipo(Constantes.RESTMAYOROIGUAL);			
//			this.restricciones.put(vertexterno.getNombre(),vertexterno);

		} else if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
			// Modelado de centrales hidróulica mediante rectas P(Q)
			if (isVertConstante()) {
				// Si iter>1 (estoy en la segunda iteraciòn)
				// Si en salida anterior Sumatoria qero.durposte > Qturb*Ndisp*durPaso
				// Esas condiciones la verifico en comportamiento simulaciòn
				// La informaciòn la trae la bandera isVertConstante
				for (int p = 0; p < gh.getCantPostes() - 1; p++) {
					DatosRestriccion nr = new DatosRestriccion();
					nr.agregarTermino(getNqerop()[p], 1.0);
					nr.agregarTermino(getNqerop()[p + 1], -1.0);
					nr.setSegundoMiembro(0.0);
					nr.setTipo(Constantes.RESTIGUAL);
					String nombreR = generarNombre("erogadoPoste", Integer.toString(p));
					nr.setNombre(nombreR);
					this.restricciones.put(nombreR, nr);
				}

			}
			if (!(apagarTurbinado && qverMax <= Constantes.EPSILONCAUDALAGUA)) {
				if (compLago.equalsIgnoreCase(Constantes.HIDROSINLAGO)) {
					// SIN LAGO
					// cargar 2.12.3.2 con igual y volini 0
					DatosRestriccion nr = cargarBalanceLagoPQ(Constantes.RESTIGUAL, 0);
					this.restricciones.put(nr.getNombre(), nr);
					// Restricciones de erogado alto, bajo y diferencia entre las variables qeroAlto
					// y qeroBajo: 2.12.3.5-6-7 respectivamente
					for (int p = 0; p < gh.getCantPostes(); p++) {
						cargarErogAltoPosteSinLagoPQ(p);
						cargarErogBajoPosteSinLagoPQ(p);
					}
					cargarVariacionErogPosteSinLagoPQ();
				} else {
					// La central tiene lago
					// vER SI APLICA:
					for (int p = 0; p < gh.getCantPostes(); p++) {
						cargarErogAltoPosteSinLagoPQ(p);
						cargarErogBajoPosteSinLagoPQ(p);
					}
					cargarVariacionErogPosteSinLagoPQ();
					// Restricción de volumen erogado móximo 2.12.3.2
					DatosRestriccion nr = cargarBalanceLagoPQ(Constantes.RESTMENOROIGUAL, volIni);
					if (compValoresBellman.equalsIgnoreCase(Constantes.PROBHIPERPLANOS)) {
						nr.setNombre(generarNombre("volFinal"));
						nr.agregarTermino(nvolfin, 1.0);
					}
					this.restricciones.put(nr.getNombre(), nr);
					// 2.12.3.3 Restricción de caudal erogado mónimo por poste (EroMinPQPoste)
					// for (int p = 0; p < gh.getCantPostes(); p++) {
					// cargarRestEroMinPQPoste(p);
					// }
					cargarRestEroMinPQPaso();
				}
				// cargar 2.12.3.8 Restricción de potencia mecónica segón el caudal erogado por
				// poste (rectas P(Q))
				int r = 0;
				for (Recta recta : this.funcionPQ) {
					for (int p = 0; p < gh.getCantPostes(); p++) {
						this.restricciones.put(generarNombre("PQposte:" + p + "r:" + r),
								armarRectaPQ(recta, getNpotp()[p], nqerop[p], p, r));
					}
					r++;
				}

			}
			if (apagarTurbinado) {
				for (int p = 0; p < gh.getCantPostes(); p++) {
					DatosRestriccion nr = new DatosRestriccion();
					nr.agregarTermino(npotp[p], 1.0);
					nr.setNombre(generarNombre("apagarTurbinado", Integer.toString(p)));
					nr.setSegundoMiembro(0.0);
					nr.setTipo(Constantes.RESTIGUAL);
					this.restricciones.put(nr.getNombre(), nr);
				}
			}
			// Restricción de caudal erogado móximo en centrales con lago que tengan
			// centrales aguas abajo 2.12.3.4
			if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO) && gh.getGeneradorAbajo() != null) {
				// for (int p = 0; p < gh.getCantPostes(); p++) {
				// cargarErogadoMaximoPQPoste(1.0, p);
				// }
				cargarErogadoMaximoPQPaso();
			}

		}

		// && (gh.isHayControldeCotasMaximas() || gh.isHayControldeCotasMinimas())
		if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			// cargarRestriccionVolumenFinal();

			if (gh.isHayControldeCotasMaximas()) {
				cargarRestriccionesControlCotasMaximas();
			}
			if (gh.isHayControldeCotasMinimas()) {
				cargarRestriccionesControlCotasMinimas();
			}
		}
	}

	private void cargarRestriccionesControlCotasMinimas() {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		DatosRestriccion nr = new DatosRestriccion();
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		nr.agregarTermino(this.nvolfin, 1.0 / Constantes.CONHM3AM3);
		nr.agregarTermino(this.nVolPenMin, 1.0);
		nr.setSegundoMiembro((double) gh.getVolumenControlMinimo().getValor(instanteActual));
		nr.setNombre(generarNombre("volumenInferiorPenalizado"));

		this.restricciones.put(nr.getNombre(), nr);

	}

	private void cargarRestriccionesControlCotasMaximas() {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		DatosRestriccion nr = new DatosRestriccion();
		nr.setTipo(Constantes.RESTMENOROIGUAL);
		nr.agregarTermino(this.nvolfin, 1.0 / Constantes.CONHM3AM3);
		nr.agregarTermino(this.nVolPenMax, -1.0);
		nr.setSegundoMiembro((double) gh.getVolumenControlMaximo().getValor(instanteActual));
		nr.setNombre(generarNombre("volumenSuperiorPenalizado"));

		this.restricciones.put(nr.getNombre(), nr);

	}

	private void cargarRestriccionVolumenFinal() {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		

		String compCoefEnerg = parametros.get(Constantes.COMPCOEFENERGETICO);
		DatosRestriccion nr = new DatosRestriccion();
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		nr.agregarTermino(nvolfin, 1.0);
		nr.setSegundoMiembro(volIni + aporte * gh.getDuracionPaso());
		nr.setNombre(generarNombre("volumenFinal"));
		if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
			for (int p = 0; p < gh.getCantPostes(); p++) {
				nr.agregarTermino(getNpotp()[p], gh.getDuracionPostes(p) / coefEnergetico / this.getFactorCompartir());
			}
			nr.agregarTermino(nqver, (double) gh.getDuracionPaso());
			for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
				HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
				for (int p = 0; p < gh.getCantPostes(); p++) {
					nr.agregarTermino(compg.getNpotp()[p],
							-gh.getDuracionPostes(p) / compg.getCoefEnergetico() / gh.getFactorCompartir().getValor(instanteActual));
				}
				nr.agregarTermino(compg.getNqver(), -(double) gh.getDuracionPaso());
			}
		} else if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
			for (int p = 0; p < gh.getCantPostes(); p++) {
				nr.agregarTermino(nqerop[p], (double) gh.getDuracionPostes(p));
			}
			for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
				HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
				for (int p = 0; p < gh.getCantPostes(); p++) {
					nr.agregarTermino(compg.getNqerop()[p], (double) -gh.getDuracionPostes(p));

				}
			}
		}
		this.restricciones.put(nr.getNombre(), nr);
	}

	private boolean catastrofe() {
		return (dameCotaAguasAbajo(caudalErogadoMedioIterAnterior, cotaAguasArribaDeCentralAguasAbajo) > gh
				.getCotaInundacionAguasAbajo()) || (cotaAguasArriba > gh.getCotaInundacionAguasArriba());
	}

	private DatosObjetivo cargarObjetivoValAgua() {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		double pasosPorAnio = Constantes.SEGUNDOSXANIO / this.gh.getDuracionPaso();
		double tasaAnual = gh.devuelveTasaDescuento();
		double factorAnual = 1 / (1 + tasaAnual);
		double factorDescuentoPaso = Math.pow(factorAnual, 1 / pasosPorAnio);
		double raizFac = Math.pow(factorDescuentoPaso, 0.5);
		double valAgua = raizFac * this.valAgua;
		boolean variables = this.gh.getSimPaso().getCorrida().isCostosVariables();
		double[] variable = null;
		
		ArrayList<Double> variablesPoste = null;
		ArrayList<Double> potsPoste = null;
		if (this.gh.getSimPaso().isSimulando() && variables) {
			variablesPoste = new ArrayList<Double>();
			variable = new double[gh.getCantPostes()];
			potsPoste = new ArrayList<Double>();
		}
		// Valorización del agua erogada
		GeneradorHidraulico gh = (GeneradorHidraulico) this.participante;
		double durPaso = (double) gh.getDuracionPaso();
		DatosObjetivo no = new DatosObjetivo();

		double vari= dameVariable();
		
		for (int p = 0; p < gh.getCantPostes(); p++) {
			if (getNpotp()[p] != null) {
				no.agregarTermino(getNpotp()[p],
						gh.getDuracionPostes(p) / coefEnergetico / gh.getFactorCompartir().getValor(instanteActual) * valAgua);
				if (this.gh.getSimPaso().isSimulando() && variables) {
					variable[p] = vari;
					potsPoste.add(cotaMax);
				}
			}
		}
		no.agregarTermino(nqver, durPaso * valAgua);

		// Valorización del agua recibida de centrales aguas arriba
		for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
			HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
			for (int p = 0; p < gh.getCantPostes(); p++) {
				no.agregarTermino(compg.getNpotp()[p], -gh.getDuracionPostes(p) / compg.getCoefEnergetico()
						/ g.getFactorCompartir().getValor(instanteActual) * valAgua);
//				if (this.gh.getSimPaso().isSimulando() && variables) {
//					variable[p] += (-gh.getDuracionPostes(p) / compg.getCoefEnergetico()
//							/ g.getFactorCompartir().getValor(instanteActual)-gh.getDuracionPostes(p)) * valAgua;
//				}
			}
			no.agregarTermino(compg.getNqver(), -durPaso * valAgua);

		}
		if (this.gh.getSimPaso().isSimulando() && variables) {
			for (int p = 0; p<gh.getCantPostes(); p++) variablesPoste.add(variable[p]);
			DatosEPPUnEscenario des = this.gh.getSimPaso().getDatosEscenario();
			DatosCurvaOferta dco = des.getCurvOfertas().get(gh.getSimPaso().getPaso());
			dco.agregarVariablesMaquinaPaso(gh.getNombre(), variablesPoste);
			dco.agregarPotsDispMaquinaPaso(gh.getNombre(), potsPoste);
		}
		return no;
	}

	private double dameSumaCoefEnergHastaLago() {
		boolean sinLago = true;
		GeneradorHidraulico aux = gh;
		double suma = gh.getCompD().getCoefEnergetico();
		while(sinLago) {
			if (aux.getGeneradorAbajo()==null) return suma;
			if (!aux.getGeneradorAbajo().tengoLago()) {	
				suma+=aux.getGeneradorAbajo().getCompD().getCoefEnergetico();
			} else {
				sinLago=false;
			}
			aux = aux.getGeneradorAbajo();
			
		}
		return suma;
	}
	
	private double dameVariable() {		
		double vagua = gh.getCompD().getValAgua();
	//	System.out.println("VARIABLE DE : " + gh.getNombre());
		double sumaCoefs = dameSumaCoefEnergHastaLago();
		double cenerg = gh.getCompD().getCoefEnergetico();
		double pondera = cenerg/sumaCoefs;
		
		double vaguaMasAbajo = dameValAguaSiguienteAguasAbajo();
		
		if (gh.tengoLago()) {
			return (vagua-vaguaMasAbajo)/(cenerg/Constantes.SEGUNDOSXHORA)*pondera;  
		} else {
			GeneradorHidraulico aux = gh;			
			while(!aux.tengoLago()&&aux.getGeneradoresArriba().size()==1) {				
				aux=aux.getGeneradoresArriba().get(0);
			}		
			if (aux.tengoLago()) {
				return aux.getCompD().dameVariable()/aux.getCompD().getCoefEnergetico()*cenerg;
			}
			if (aux.getGeneradoresArriba().size()==0) {
				return 0;
			} 
		}
		return 0;
		
	
	}

	private double dameValAguaSiguienteAguasAbajo() {
		
		GeneradorHidraulico aux = gh;
			
		while (aux.getGeneradorAbajo()!=null) {
			if (aux.getGeneradorAbajo().tengoLago()) {				
				return aux.getGeneradorAbajo().getCompD().getValAgua();
			}
			aux = aux.getGeneradorAbajo();
		}
	
		return 0;
	}

	/*
	 * private DatosObjetivo cargarObjetivoVolEcol(){ DatosObjetivo no = new
	 * DatosObjetivo(); no.agregarTermino(getNVolFaltEco()[p],
	 * penalizacionFaltanteCaudal/Constantes.M3XHM3); //USD return no; }
	 */

	private DatosObjetivo cargarObjetivoValAguaPQ() {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		double pasosPorAnio = Constantes.SEGUNDOSXANIO / this.gh.getDuracionPaso();
		double tasaAnual = gh.devuelveTasaDescuento();
		double factorAnual = 1 / (1 + tasaAnual);
		double factorDescuentoPaso = Math.pow(factorAnual, 1 / pasosPorAnio);
		double raizFac = Math.pow(factorDescuentoPaso, 0.5);
		double valAgua = raizFac * this.valAgua;
		// Valorización del agua erogada
		GeneradorHidraulico gh = (GeneradorHidraulico) this.participante;

		DatosObjetivo no = new DatosObjetivo();

		for (int p = 0; p < gh.getCantPostes(); p++) {
			no.agregarTermino(nqerop[p], gh.getDuracionPostes(p) / gh.getFactorCompartir().getValor(instanteActual) * valAgua);
		}

		// Valorización del agua recibida de centrales aguas arriba
		for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
			HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
			for (int p = 0; p < gh.getCantPostes(); p++) {
				no.agregarTermino(compg.getNqerop()[p],
						-gh.getDuracionPostes(p) / gh.getFactorCompartir().getValor(instanteActual) * valAgua);
			}
		}
		return no;
	}

	public void contribuirObjetivo() {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		String compLago = parametros.get(Constantes.COMPLAGO);
		String compCoefEnerg = parametros.get(Constantes.COMPCOEFENERGETICO);
		String compValoresBellman = parametros.get(Constantes.COMPVALORESBELLMAN);

		if (compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
				if (compValoresBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
					// creamos la contribución 2.11.4
					this.objetivo.contribuir(cargarObjetivoValAgua());
					this.objetivo.contribuir(cargarObjetivoControlCota());
				}
			} else if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
				if (compValoresBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
					// creamos la contribución 2.12.4
					this.objetivo.contribuir(cargarObjetivoValAguaPQ());
					this.objetivo.contribuir(cargarObjetivoControlCota());
				}
			}
		} else if (compLago.equalsIgnoreCase(Constantes.HIDROSINLAGO)) {
			if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
				boolean variables = this.gh.getSimPaso().getCorrida().isCostosVariables();
				double[] variable = null;
				
				ArrayList<Double> variablesPoste = null;
				ArrayList<Double> potsPoste = null;
				if (this.gh.getSimPaso().isSimulando() && variables) {
					variablesPoste = new ArrayList<Double>();
					variable = new double[gh.getCantPostes()];
					potsPoste = new ArrayList<Double>();
				}
				// Valorización del agua erogada
				GeneradorHidraulico gh = (GeneradorHidraulico) this.participante;
			
				
				double vari = dameVariable();
				for (int p = 0; p < gh.getCantPostes(); p++) {
					if (getNpotp()[p] != null) {
						if (this.gh.getSimPaso().isSimulando() && variables) {
							variable[p] = vari;		
							potsPoste.add(cotaMax);
						}
					
					}
				}
				
				if (this.gh.getSimPaso().isSimulando() && variables) {
					for (int p = 0; p<gh.getCantPostes(); p++) variablesPoste.add(variable[p]);
					DatosEPPUnEscenario des = this.gh.getSimPaso().getDatosEscenario();
					DatosCurvaOferta dco = des.getCurvOfertas().get(gh.getSimPaso().getPaso());
					dco.agregarVariablesMaquinaPaso(gh.getNombre(), variablesPoste);
					dco.agregarPotsDispMaquinaPaso(gh.getNombre(), potsPoste);
				}				
			} else if (compCoefEnerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
				// this.objetivo.contribuir(cargarPenalizacionAguaDestruidaPQ());
			}
		}

		DatosObjetivo costo = new DatosObjetivo();
		for (int p = 0; p < gh.getCantPostes(); p++) {
			costo.agregarTermino(this.npotp[p],
					gh.getCostoVariable().getValor(instanteActual) * gh.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
		}
		costo.setTerminoIndependiente(gh.getCostoFijo().getValor(instanteActual));

		this.objetivo.contribuir(costo);
	}

	private DatosObjetivo cargarObjetivoControlCota() {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		DatosObjetivo dob = new DatosObjetivo();
		if (gh.isHayControldeCotasMinimas()) {
			double cotaControlMin = gh.getfCovo().dameValor(gh.getVolumenControlMinimo().getValor(instanteActual));
			double dVdCenCotaControlInf = calcularDerivadaIncremental(cotaControlMin, gh.getfVoco());
			double fraccionDia = gh.getDuracionPaso() / Constantes.SEGUNDOSXDIA;
			double cvpenInf = gh.getPenalidadControlMinimo().getValor(instanteActual) * Constantes.CONHM3AM3 * fraccionDia
					/ dVdCenCotaControlInf;
			dob.agregarTermino(this.nVolPenMin, cvpenInf);

		}
		if (gh.isHayControldeCotasMaximas()) {
			double cotaControlMax = gh.getfCovo().dameValor(gh.getVolumenControlMaximo().getValor(instanteActual));
			double dVdCenCotaControlSup = calcularDerivadaIncremental(cotaControlMax, gh.getfVoco());
			double fraccionDia = gh.getDuracionPaso() / Constantes.SEGUNDOSXDIA;
			double cvpenSup = gh.getPenalidadControlMaximo().getValor(instanteActual) * Constantes.CONHM3AM3 * fraccionDia
					/ dVdCenCotaControlSup;
			dob.agregarTermino(this.nVolPenMax, cvpenSup);
		}
		return dob;
	}

	public double calcularDerivadaIncremental(double cota, Polinomio voco) {
		double y1 = voco.dameValor(cota + 0.1);
		double y = voco.dameValor(cota);

		return (y1 - y) / 0.1;
	}

	private DatosObjetivo cargarPenalizacionAguaDestruida() {
		DatosObjetivo no = new DatosObjetivo();

		double durPaso = (double) gh.getDuracionPaso();

		for (int p = 0; p < gh.getCantPostes(); p++) {
			no.agregarTermino(getNpotp()[p], -Constantes.PENALIZACION_AGUA_DESTRUIDA * gh.getDuracionPostes(p)
					/ coefEnergetico / this.getFactorCompartir());
		}
		no.agregarTermino(nqver, -Constantes.PENALIZACION_AGUA_DESTRUIDA * durPaso);
		for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
			HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
			for (int p = 0; p < gh.getCantPostes(); p++) {
				// cargar 2.10.3.1 como cota superior
				if (compg.getNpotp()[p] != null) {
					no.agregarTermino(compg.getNpotp()[p], Constantes.PENALIZACION_AGUA_DESTRUIDA
							* (gh.getDuracionPostes(p)) / compg.getCoefEnergetico() / this.getFactorCompartir());
				}

			}
			no.agregarTermino(compg.getNqver(), Constantes.PENALIZACION_AGUA_DESTRUIDA * durPaso);
		}

		return no;
	}

	private DatosObjetivo cargarPenalizacionAguaDestruidaPQ() {
		DatosObjetivo no = new DatosObjetivo();

		double durPaso = (double) gh.getDuracionPaso();

		for (int p = 0; p < gh.getCantPostes(); p++) {
			no.agregarTermino(nqerop[p], -Constantes.PENALIZACION_AGUA_DESTRUIDA * gh.getDuracionPostes(p));
		}
		for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
			HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
			for (int p = 0; p < gh.getCantPostes(); p++) {
				// cargar 2.10.3.1 como cota superior
				if (compg.getNqerop()[p] != null) {
					no.agregarTermino(compg.getNqerop()[p],
							Constantes.PENALIZACION_AGUA_DESTRUIDA * (gh.getDuracionPostes(p)));
				}

			}
		}

		return no;
	}

	/**
	 * 
	 * @param recta a*x + b
	 * @param var1  variable acotada
	 * @param var2  variable
	 * @return
	 */
	private DatosRestriccion armarRectaPQ(Recta recta, String var1, String var2, int p, int r) {
		DatosRestriccion nr = new DatosRestriccion();
		// las rectas descendentes deben multiplicarse por cantModDisp
		if (recta.getA() <= 0) {
			recta = recta.multiplicar(recta, cantModDisp);
		}
		nr.agregarTermino(var2, -recta.getA());
		nr.agregarTermino(var1, 1.0 / factorCompartir);
		nr.setSegundoMiembro(recta.getB());
		nr.setTipo(Constantes.RESTMENOROIGUAL);
		nr.setNombre(generarNombre("PQ_recta_" + r + "_poste_" + p));
		return nr;

	}

	/**
	 * Devuelve el resultado de vertimiento del poste p en m3/s dada una salida del
	 * problema lineal
	 * 
	 * @return
	 */
	public double getQVer(DatosSalidaProblemaLineal resultados, int p) {
		Hashtable<String, Double> solucion = resultados.getSolucion();
		if (compCoefEnergetico.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
			return solucion.get((this.nqver));
		} else {
			if (apagarTurbinado) {
				return solucion.get((this.nqerop[p]));
			} else {
				if (resultados.getSolucion().get(this.nqerop[p]) < cantModDisp * qTur1Max) {
					return 0.0;
				} else {
					return solucion.get((this.nqerop[p])) - cantModDisp * qTur1Max;
				}
			}
		}
	}

	public String getnVolPenMin() {
		return nVolPenMin;
	}

	public void setnVolPenMin(String nVolPenMin) {
		this.nVolPenMin = nVolPenMin;
	}

	public String getnVolPenMax() {
		return nVolPenMax;
	}

	public void setnVolPenMax(String nVolPenMax) {
		this.nVolPenMax = nVolPenMax;
	}

	public double getCotaMax() {
		return cotaMax;
	}

	public void setCotaMax(double cotaMax) {
		this.cotaMax = cotaMax;
	}

	public double[] getCoefParaRestriccion() {
		return coefParaRestriccion;
	}

	public void setCoefParaRestriccion(double[] coefParaRestriccion) {
		this.coefParaRestriccion = coefParaRestriccion;
	}

	/**
	 * Devuelve el resultado de turbinado del poste p en m3/s dada una salida del
	 * problema lineal
	 * 
	 * @return
	 */
	public double getQTur(DatosSalidaProblemaLineal resultados, int p) {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		if (compCoefEnergetico.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
			if (this.npotp[p] == null)
				return 0;

			return resultados.getSolucion().get(this.npotp[p]) / this.coefEnergetico
					/ gh.getFactorCompartir().getValor(instanteActual);
		} else {
			if (apagarTurbinado) {
				return 0;
			} else {
				if (resultados.getSolucion().get(this.nqerop[p]) < cantModDisp * qTur1Max) {
					return resultados.getSolucion().get(this.nqerop[p]);
				} else {
					return cantModDisp * qTur1Max;
				}
			}
		}
	}

	public double getErogadoPaso(DatosSalidaProblemaLineal res) {
		double erogadoPaso = 0;

		for (int p = 0; p < participante.getCantPostes(); p++) {
			erogadoPaso += getQVer(res, p) + getQTur(res, p);
		}
		return erogadoPaso;

	}

	public double getCoefEnergetico() {
		return coefEnergetico;
	}

	public void setCoefEnergetico(double coefEnergetico) {
		this.coefEnergetico = coefEnergetico;
	}

	public Double getqTur1Max() {
		return qTur1Max;
	}

	public void setqTur1Max(Double qTur1Max) {
		this.qTur1Max = qTur1Max;
	}

	public Double getQverMax() {
		return qverMax;
	}

	public void setQverMax(Double qverMax) {
		this.qverMax = qverMax;
	}

	public Double getValAgua() {
		return valAgua;
	}

	public void setValAgua(Double valAgua) {
		this.valAgua = valAgua;
	}

	public static String getCompLagoPorDefecto() {
		return compLagoPorDefecto;
	}

	public static void setCompLagoPorDefecto(String compLagoPorDefecto) {
		HidraulicoCompDesp.compLagoPorDefecto = compLagoPorDefecto;
	}

	public static String getCompCoefEnergeticoDefecto() {
		return compCoefEnergeticoDefecto;
	}

	public static void setCompCoefEnergeticoDefecto(String compCoefEnergeticoDefecto) {
		HidraulicoCompDesp.compCoefEnergeticoDefecto = compCoefEnergeticoDefecto;
	}

	public String getCompLago() {
		return compLago;
	}

	public void setCompLago(String compLago) {
		this.compLago = compLago;
	}

	public String getCompCoefEnergetico() {
		return compCoefEnergetico;
	}

	public void setCompCoefEnergetico(String compCoefEnergetico) {
		this.compCoefEnergetico = compCoefEnergetico;
	}

	public Double getAporte() {
		return aporte;
	}

	public void setAporte(Double aporte) {
		this.aporte = aporte;
	}

	public Double getVolIni() {
		return volIni;
	}

	public void setVolIni(Double volIni) {
		this.volIni = volIni;
	}

	public boolean isComportamientoEspecifico() {
		return comportamientoEspecifico;
	}

	public void setComportamientoEspecifico(boolean comportamientoEspecifico) {
		this.comportamientoEspecifico = comportamientoEspecifico;
	}

	public void setVariablesControl(Hashtable<String, DatosVariableControl> variablesControl) {
		this.variablesControl = variablesControl;
	}

	public String[] getNqerop() {
		return nqerop;
	}

	public void setNqerop(String[] nqerop) {
		this.nqerop = nqerop;
	}

	public String getNqeropAlto() {
		return nqeropAlto;
	}

	public void setNqeropAlto(String nqeropAlto) {
		this.nqeropAlto = nqeropAlto;
	}

	public String getNqeropBajo() {
		return nqeropBajo;
	}

	public void setNqeropBajo(String nqeropBajo) {
		this.nqeropBajo = nqeropBajo;
	}

	public String getNqver() {
		return nqver;
	}

	public void setNqver(String nqver) {
		this.nqver = nqver;
	}

	public void setRestricciones(Hashtable<String, DatosRestriccion> restricciones) {
		this.restricciones = restricciones;
	}

	public String getNvolfin() {
		return nvolfin;
	}

	public void setNvolfin(String nvolfin) {
		this.nvolfin = nvolfin;
	}

	public ArrayList<Recta> getFuncionPQ() {
		return funcionPQ;
	}

	public void setFuncionPQ(ArrayList<Recta> funcionPQ) {
		this.funcionPQ = funcionPQ;
	}

	public Double getPotMax() {
		return potMax;
	}

	public void setPotMax(Double potMax) {
		this.potMax = potMax;
	}

	public Double getVolEroMin() {
		return volEroMin;
	}

	public void setVolEroMin(Double volEroMin) {
		this.volEroMin = volEroMin;
	}

	public double getVolEroMax() {
		return volEroMax;
	}

	public void setVolEroMax(double volEroMax) {
		this.volEroMax = volEroMax;
	}

	public Integer getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(Integer cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public String[] getNpotp() {
		return npotp;
	}

	public void setNpotp(String[] npotp) {
		this.npotp = npotp;
	}

	public String[] getNqturp() {
		return nqturp;
	}

	public void setNqturp(String[] nqturp) {
		this.nqturp = nqturp;
	}

	public String getNombreRestriccionBalanceLago() {
		return nombreRestriccionBalanceLago;
	}

	public void setNombreRestriccionBalanceLago(String nombreRestriccionBalanceLago) {
		this.nombreRestriccionBalanceLago = nombreRestriccionBalanceLago;
	}

	public Hashtable<String, DatosVariableControl> getVariablesControl() {
		return variablesControl;
	}

	public double getCotaAguasArribaDeCentralAguasAbajo() {
		return cotaAguasArribaDeCentralAguasAbajo;
	}

	public void setCotaAguasArribaDeCentralAguasAbajo(double cotaAguasArribaDeCentralAguasAbajo) {
		this.cotaAguasArribaDeCentralAguasAbajo = cotaAguasArribaDeCentralAguasAbajo;
	}

	public double getCotaAguasArriba() {
		return cotaAguasArriba;
	}

	public void setCotaAguasArriba(double cotaAguasArriba) {
		this.cotaAguasArriba = cotaAguasArriba;
	}

	/**
	 * @param caudalErogado            en m3/s
	 * @param cotaAArribaCentralAAbajo
	 * @return
	 */
	public double dameCotaAguasAbajo(double caudalErogado, double cotaAArribaCentralAAbajo) {
		Polinomio fCotaAA = gh.getfCotaAguasAbajo();
		double cotaAguasAbajo;
		if (fCotaAA.getTipo().equalsIgnoreCase("poliMulti") || ((fCotaAA.getTipo().equalsIgnoreCase("porRangos")
				&& fCotaAA.getFueraRango().getTipo().equalsIgnoreCase("poliMulti")))) {
			cotaAguasAbajo = fCotaAA.dameValor("QErogado","CotaAguasAbajo",caudalErogado, cotaAArribaCentralAAbajo);
		} else {
			cotaAguasAbajo = fCotaAA.dameValor(caudalErogado);
		}
		return cotaAguasAbajo;

	}

	public double calcularSalto() {		
		return cotaAguasArriba - dameCotaAguasAbajo(caudalErogadoMedioIterAnterior, cotaAguasArribaDeCentralAguasAbajo);
	}

	public void actualizarCoeficienteEnergetico() {

		coefEnergetico = gh.coefEnergetico(cotaAguasArriba,
				dameCotaAguasAbajo(caudalErogadoMedioIterAnterior, cotaAguasArribaDeCentralAguasAbajo));

	}

	public double getCaudalErogadoAnteriorPaso() {
		return caudalErogadoAnteriorPaso;
	}

	public void actualizarFuncionesPQ() {
		// Calcular el ArrayList<Rectas> para las cotasAguasArriba y
		// cotasAguasArriabaDeCentralAguasAbajo
		Hashtable<Pair<Double, Double>, ArrayList<Recta>> fPQ = gh.getFuncionesPQ();
		// conseguir el ArrayList del par
		// cotaAguasArriba-cotasAguasArriabaDeCentralAguasAbajo interpolando en la clave
		// del Hashtable
		// Ptos a interpolar

		/*
		 * for(Pair<Double,Double> clave : fPQ.keySet()){ if(clave.first >
		 * cotaAguasArriba){
		 * 
		 * } }
		 * 
		 * 
		 * List<Pair<Double,Double>> sortedKeys=new ArrayList(fPQ.keySet());
		 * Collections.sort(sortedKeys);
		 * 
		 */

		Pair<Double, Double> p1 = new Pair(Math.floor(cotaAguasArriba), Math.floor(cotaAguasArribaDeCentralAguasAbajo));
		Pair<Double, Double> p2 = new Pair(Math.floor(cotaAguasArriba), Math.ceil(cotaAguasArribaDeCentralAguasAbajo));
		Pair<Double, Double> p3 = new Pair(Math.ceil(cotaAguasArriba), Math.floor(cotaAguasArribaDeCentralAguasAbajo));
		Pair<Double, Double> p4 = new Pair(Math.ceil(cotaAguasArriba), Math.ceil(cotaAguasArribaDeCentralAguasAbajo));
		Pair<Double, Double> pto = new Pair(cotaAguasArriba, cotaAguasArribaDeCentralAguasAbajo);
		// System.out.println("Cota Lago: "+cotaAguasArriba + " Cota Lago Central AA:
		// "+cotaAguasArribaDeCentralAguasAbajo );
		ArrayList<Pair<Double, Double>> grilla = new ArrayList<Pair<Double, Double>>();
		grilla.add(p1);
		grilla.add(p2);
		grilla.add(p3);
		grilla.add(p4);
		ArrayList<ArrayList<Recta>> valsGrilla = new ArrayList<ArrayList<Recta>>();
		valsGrilla.add(fPQ.get(p1));
		valsGrilla.add(fPQ.get(p2));
		valsGrilla.add(fPQ.get(p3));
		valsGrilla.add(fPQ.get(p4));
		Recta aux = new Recta(0, 0);
		ArrayList<Recta> valInterpolado = new ArrayList<Recta>();
		int cantRectas = fPQ.get(p1).size();
		// Interpolar el conjunto de todas las rectas
		for (int r = 0; r < cantRectas; r++) {
			ArrayList<Recta> Recta_i_interpolar = new ArrayList<Recta>();
			for (int p = 0; p < grilla.size(); p++) {
				Recta_i_interpolar.add(valsGrilla.get(p).get(r));
			}
			valInterpolado.add(aux.interpolarPorDistancia(grilla, pto, Recta_i_interpolar));
		}

		this.funcionPQ = valInterpolado;
	}

	public Double getFactorCompartir() {
		return factorCompartir;
	}

	public void setFactorCompartir(Double factorCompartir) {
		this.factorCompartir = factorCompartir;
	}

	public GeneradorHidraulico getGh() {
		return gh;
	}

	public void setGh(GeneradorHidraulico gh) {
		this.gh = gh;
	}

	public boolean isControlApagado() {
		return controlApagado;
	}

	public void setControlApagado(boolean controlApagado) {
		this.controlApagado = controlApagado;
	}

	public boolean isApagarTurbinado() {
		return apagarTurbinado;
	}

	public void setApagarTurbinado(boolean apagarTurbinado) {
		this.apagarTurbinado = apagarTurbinado;
	}

	public void setValAgua(double valAgua) {
		this.valAgua = valAgua;
	}

	public double getVolfilthm3() {
		return volfilthm3;
	}

	public void setVolfilthm3(double volfilthm3) {
		this.volfilthm3 = volfilthm3;
	}

	public double getVolevaphm3() {
		return volevaphm3;
	}

	public void setVolevaphm3(double volevaphm3) {
		this.volevaphm3 = volevaphm3;
	}


	public String[] getNqFaltEco() {
		return nqFaltEco;
	}

	public void setNqFaltEco(String[] nqFaltEco) {
		this.nqFaltEco = nqFaltEco;
	}

	

	public void setCaudalErogadoAnteriorPaso(double d) {
		this.caudalErogadoAnteriorPaso = d;

	}

	public double getCaudalErogadoMedioIterAnterior() {
		return caudalErogadoMedioIterAnterior;
	}

	public void setCaudalErogadoMedioIterAnterior(double caudalErogadoMedioIterAnterior) {
		this.caudalErogadoMedioIterAnterior = caudalErogadoMedioIterAnterior;
	}

	public String[] getNqverp() {
		return nqverp;
	}

	public void setNqverp(String[] nqverp) {
		this.nqverp = nqverp;
	}

	public boolean isVertConstante() {
		return vertConstante;
	}

	public void setVertConstante(boolean vertConstante) {
		this.vertConstante = vertConstante;
	}
	
	
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto i) {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		Hashtable<String, DatosRestriccion> restricciones = new Hashtable<String, DatosRestriccion>();
		String compLago = parametros.get(Constantes.COMPLAGO);
		String compCoefenerg = parametros.get(Constantes.COMPCOEFENERGETICO);
		double limite = i.getLimite().getValor(instanteActual);

		ImpactoCompDesp icd = (ImpactoCompDesp) i.getCompDesp();
		String nExceso = icd.getnExceso();
		String[] nExcesop = icd.getnExcesop();

		if (i.getTipoImpacto() == Constantes.HIDRO_INUN_AGUAS_ABAJO) {
			/**
			 * (suma(caudalerogadoposte(i)*durposte(i)) - exceso )<= lim >>>>>>>
			 * refs/remotes/adm_origin/devImpContratv0.4.3
			 */

			if (compCoefenerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
				double multiplicador = 1.0;
				if (i.isPorPoste()) {
					for (int p = 0; p < gh.getCantPostes(); p++) {
						if (i.isPorUnidadTiempo())
							multiplicador = gh.getDuracionPostes(p);
						DatosRestriccion dr = new DatosRestriccion();
						dr.setNombre(generarNombre("inundAguasAbajo", Integer.toString(p)));
						dr.agregarTermino(this.getNqerop()[p], (double) multiplicador);
						dr.agregarTermino(nExcesop[p], -multiplicador);
						dr.setSegundoMiembro(limite * multiplicador);
						dr.setTipo(Constantes.RESTMENOROIGUAL);
						restricciones.put(dr.getNombre(), dr);
					}
				} else {
					DatosRestriccion dr = new DatosRestriccion();
					if (i.isPorUnidadTiempo())
						multiplicador = gh.getDuracionPaso();
					dr.setNombre(generarNombre("inundAguasAbajo"));
					for (int p = 0; p < gh.getCantPostes(); p++) {
						dr.agregarTermino(this.getNqerop()[p], (double) multiplicador);
					}
					dr.agregarTermino(nExceso, -multiplicador);
					dr.setSegundoMiembro(limite * multiplicador);
					dr.setTipo(Constantes.RESTMENOROIGUAL);
					restricciones.put(dr.getNombre(), dr);
				}
			} else if (compCoefenerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
				/**
				 * suma(caudaltur(i)*durposte(i))+caudalverpaso*durpaso -exceso <= lim
				 */
				double multiplicador = 1.0;
				if (i.isPorPoste()) {
					for (int p = 0; p < gh.getCantPostes(); p++) {
						if (i.isPorUnidadTiempo())
							multiplicador = gh.getDuracionPostes(p);
						DatosRestriccion dr = new DatosRestriccion();
						dr.setNombre(generarNombre("inundAguasAbajo", Integer.toString(p)));
						dr.agregarTermino(this.getNpotp()[p],
								multiplicador / this.coefEnergetico / gh.getFactorCompartir().getValor(instanteActual));
						dr.agregarTermino(this.getNqver(), gh.getDuracionPostes(p) / 1.0);
						dr.agregarTermino(nExcesop[p], -multiplicador);
						dr.setSegundoMiembro(limite * multiplicador);
						dr.setTipo(Constantes.RESTMENOROIGUAL);
						restricciones.put(dr.getNombre(), dr);
					}
				} else {
					if (i.isPorUnidadTiempo())
						multiplicador = gh.getDuracionPaso();
					DatosRestriccion dr = new DatosRestriccion();
					dr.setNombre(generarNombre("inundAguasAbajo"));
					for (int p = 0; p < gh.getCantPostes(); p++) {
						dr.agregarTermino(this.getNpotp()[p],
								gh.getDuracionPostes(p) / this.coefEnergetico / gh.getFactorCompartir().getValor(instanteActual));
					}
					dr.agregarTermino(this.getNqver(), multiplicador);
					dr.agregarTermino(nExceso, -multiplicador);
					dr.setSegundoMiembro(limite * multiplicador);
					dr.setTipo(Constantes.RESTMENOROIGUAL);
					restricciones.put(dr.getNombre(), dr);
				}
			}

		}
		if (i.getTipoImpacto() == Constantes.HIDRO_INUN_AGUAS_ARRIBA
				&& compLago.equalsIgnoreCase(Constantes.HIDROCONLAGO)) {
			/**
			 * erogado + exceso - aarriba<= volini+aportes-lim
			 */
			// double multiplicador = 1.0;
			limite = limite * Constantes.M3XHM3;

			if (compCoefenerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
				double durPaso = (double) gh.getDuracionPaso();
				if (i.isPorPoste()) {
					for (int p = 0; p < gh.getCantPostes(); p++) {
						DatosRestriccion dr = new DatosRestriccion();
						dr.setNombre(generarNombre("inundAguasArriba", Integer.toString(p)));
						dr.agregarTermino(nqerop[p], (double) gh.getDuracionPostes(p));

						dr.agregarTermino(icd.getnExcesop()[p], 1.0);

						/*-aguasArriba*/
						for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
							HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
							dr.agregarTermino(compg.getNqerop()[p], (double) -gh.getDuracionPostes(p));

						}
						dr.setSegundoMiembro(
								gh.getDuracionPostes(p) * aporte + this.volIni * Constantes.M3XHM3 - limite);
						dr.setTipo(Constantes.RESTMAYOROIGUAL);
						restricciones.put(dr.getNombre(), dr);
					}

				} else {
					DatosRestriccion dr = new DatosRestriccion();
					dr.setNombre(generarNombre("inundAguasArriba"));
					/* erogado */
					for (int p = 0; p < gh.getCantPostes(); p++) {
						dr.agregarTermino(nqerop[p], (double) gh.getDuracionPostes(p));
						if (i.isPorPoste())
							dr.agregarTermino(nExcesop[p], 1.0);
					}
					/*-aguasArriba*/
					for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
						HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
						for (int p = 0; p < gh.getCantPostes(); p++) {
							dr.agregarTermino(compg.getNqerop()[p], (double) -gh.getDuracionPostes(p));
						}
					}
					if (!i.isPorPoste())
						dr.agregarTermino(nExceso, 1.0);
					dr.setSegundoMiembro(durPaso * aporte + this.volIni * Constantes.M3XHM3 - limite);
					dr.setTipo(Constantes.RESTMAYOROIGUAL);
					restricciones.put(dr.getNombre(), dr);
				}

			} else if (compCoefenerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
				/**
				 * erogado + exceso - aarriba>= volini+aportes-lim
				 */

				double durPaso = (double) gh.getDuracionPaso();
				if (i.isPorPoste()) {

					/* erogado */
					for (int p = 0; p < gh.getCantPostes(); p++) {
						DatosRestriccion dr = new DatosRestriccion();
						dr.setNombre(generarNombre("inundAguasArriba", Integer.toString(p)));
						dr.agregarTermino(this.getNpotp()[p], (double) gh.getDuracionPostes(p) / this.coefEnergetico
								/ gh.getFactorCompartir().getValor(instanteActual));
						dr.agregarTermino(this.getNqver(), (double) gh.getDuracionPostes(p));
						dr.agregarTermino(icd.getnExcesop()[p], 1.0);

						/*-aguasArriba*/
						for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
							HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
							dr.agregarTermino(compg.getNpotp()[p], (double) -gh.getDuracionPostes(p)
									/ this.coefEnergetico / gh.getFactorCompartir().getValor(instanteActual));
							dr.agregarTermino(compg.getNqver(), (double) -gh.getDuracionPostes(p));

						}
						dr.setSegundoMiembro(
								gh.getDuracionPostes(p) * aporte + this.volIni * Constantes.M3XHM3 - limite);
						dr.setTipo(Constantes.RESTMAYOROIGUAL);
						restricciones.put(dr.getNombre(), dr);
					}

				} else {
					DatosRestriccion dr = new DatosRestriccion();
					dr.setNombre(generarNombre("inundAguasArriba"));
					/* erogado */
					for (int p = 0; p < gh.getCantPostes(); p++) {
						dr.agregarTermino(this.getNpotp()[p], (double) gh.getDuracionPostes(p) / this.coefEnergetico
								/ gh.getFactorCompartir().getValor(instanteActual));
					}
					/*-aguasArriba*/
					for (GeneradorHidraulico g : gh.getGeneradoresArriba()) {
						HidraulicoCompDesp compg = (HidraulicoCompDesp) g.getCompDesp();
						for (int p = 0; p < gh.getCantPostes(); p++) {
							dr.agregarTermino(compg.getNpotp()[p], (double) -gh.getDuracionPostes(p)
									/ this.coefEnergetico / gh.getFactorCompartir().getValor(instanteActual));
							dr.agregarTermino(compg.getNqver(), (double) -gh.getDuracionPostes(p));
						}
					}
					dr.agregarTermino(nExceso, 1.0);
					dr.setSegundoMiembro(durPaso * aporte + this.volIni * Constantes.M3XHM3 - limite);
					dr.setTipo(Constantes.RESTMAYOROIGUAL);
					restricciones.put(dr.getNombre(), dr);
				}
			}
		}
		if (i.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO) {
			if (compCoefenerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
				double multiplicador = 1.0;
				if (i.isPorPoste()) {
					for (int p = 0; p < gh.getCantPostes(); p++) {
						if (i.isPorUnidadTiempo()) {
							multiplicador = gh.getDuracionPostes(p);
						}

						DatosRestriccion nr = new DatosRestriccion();
						nr.agregarTermino(nqerop[p], multiplicador);
						nr.agregarTermino(icd.getnExcesop()[p], multiplicador);
						nr.setNombre(generarNombre("caudalEcologicoPQ_" + p));
						nr.setSegundoMiembro(limite * multiplicador);
						nr.setTipo(Constantes.RESTMAYOROIGUAL);
						restricciones.put(nr.getNombre(), nr);
					}
				} else {
					DatosRestriccion nr = new DatosRestriccion();

					for (int p = 0; p < gh.getCantPostes(); p++) {
						if (i.isPorUnidadTiempo()) {
							multiplicador = gh.getDuracionPostes(p);
						}
						nr.setNombre(generarNombre("caudalEcologicoPQ_" + p));
						nr.agregarTermino(getNqerop()[p],
								(double) multiplicador / this.coefEnergetico / gh.getFactorCompartir().getValor(instanteActual));
					}
					if (i.isPorUnidadTiempo()) {
						multiplicador = gh.getDuracionPaso();
					}
					nr.agregarTermino(icd.getnExceso(), multiplicador);
					nr.setSegundoMiembro(limite * multiplicador);
					nr.setTipo(Constantes.RESTMAYOROIGUAL);
				}
			} else if (compCoefenerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
				double multiplicador = 1.0;
				if (i.isPorPoste()) {
					for (int p = 0; p < gh.getCantPostes(); p++) {
						if (i.isPorUnidadTiempo()) {
							multiplicador = gh.getDuracionPostes(p);
						}
						DatosRestriccion nr = new DatosRestriccion();
						nr.agregarTermino(getNpotp()[p],
								multiplicador / coefEnergetico / gh.getFactorCompartir().getValor(instanteActual));
						nr.agregarTermino(nqver, multiplicador);
						nr.agregarTermino(icd.getnExcesop()[p], multiplicador);
						nr.setNombre(generarNombre("caudalEcologico_" + p));
						// System.out.println("RESTRICCION: LIM: " + limite + " DUR: " + multiplicador);
						nr.setSegundoMiembro(limite * multiplicador);
						nr.setTipo(Constantes.RESTMAYOROIGUAL);
						restricciones.put(nr.getNombre(), nr);
					}
				} else {

					DatosRestriccion nr = new DatosRestriccion();
					for (int p = 0; p < gh.getCantPostes(); p++) {
						if (i.isPorUnidadTiempo()) {
							multiplicador = gh.getDuracionPostes(p);
						}
						nr.agregarTermino(getNpotp()[p],
								multiplicador / coefEnergetico / gh.getFactorCompartir().getValor(instanteActual));

					}
					if (i.isPorUnidadTiempo()) {
						multiplicador = gh.getDuracionPaso();
					}
					nr.agregarTermino(nqver, multiplicador);
					nr.agregarTermino(icd.getnExceso(), multiplicador);
					nr.setNombre(generarNombre("caudalEcologico"));
					nr.setSegundoMiembro(limite * multiplicador);
					nr.setTipo(Constantes.RESTMAYOROIGUAL);
					restricciones.put(nr.getNombre(), nr);
				}
			}

		}
		if (i.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO) {
			cargarRestriccionImpactoVertimientoExterno(i, restricciones, compCoefenerg, limite, icd);
		}

		return restricciones;
	}

	private void cargarRestriccionImpactoVertimientoExterno(Impacto i,
			Hashtable<String, DatosRestriccion> restricciones, String compCoefenerg, double limite,
			ImpactoCompDesp icd) {
		
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		if (compCoefenerg.equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)) {
			double multiplicador = 1.0;
			if (i.isPorPoste()) {
				for (int p = 0; p < gh.getCantPostes(); p++) {
					if (i.isPorUnidadTiempo()) {
						multiplicador = gh.getDuracionPostes(p);
					}

					DatosRestriccion nr = new DatosRestriccion();
					nr.agregarTermino(nqerop[p], multiplicador);
					nr.agregarTermino(icd.getnExcesop()[p], multiplicador);
					nr.setNombre(generarNombre("caudalVertimientoExterno" + p));
					nr.setSegundoMiembro(limite * multiplicador);
					nr.setTipo(Constantes.RESTMAYOROIGUAL);
					restricciones.put(nr.getNombre(), nr);
				}
			} else {
				DatosRestriccion nr = new DatosRestriccion();

				for (int p = 0; p < gh.getCantPostes(); p++) {
					if (i.isPorUnidadTiempo()) {
						multiplicador = gh.getDuracionPostes(p);
					}
					nr.setNombre(generarNombre("caudalVertimientoExternoPoste" + p));
					nr.agregarTermino(getNqerop()[p],
							(double) multiplicador / this.coefEnergetico / gh.getFactorCompartir().getValor(instanteActual));
				}
				if (i.isPorUnidadTiempo()) {
					multiplicador = gh.getDuracionPaso();
				}
				nr.agregarTermino(icd.getnExceso(), multiplicador);
				nr.setSegundoMiembro(limite * multiplicador);
				nr.setTipo(Constantes.RESTMAYOROIGUAL);
			}
		} else if (compCoefenerg.equalsIgnoreCase(Constantes.HIDROCOEFENERGCONSTANTES)) {
			double multiplicador = 1.0;
			if (i.isPorPoste()) {
				for (int p = 0; p < gh.getCantPostes(); p++) {
					if (i.isPorUnidadTiempo()) {
						multiplicador = gh.getDuracionPostes(p);
					}
					DatosRestriccion nr = new DatosRestriccion();
					nr.agregarTermino(nqver, multiplicador);
					nr.agregarTermino(icd.getnExcesop()[p], multiplicador);
					nr.setNombre(generarNombre("caudalVertimientoExternoPoste" + p));
					// System.out.println("RESTRICCION: LIM: " + limite + " DUR: " + multiplicador);
					nr.setSegundoMiembro(limite * multiplicador);
					nr.setTipo(Constantes.RESTMAYOROIGUAL);
					restricciones.put(nr.getNombre(), nr);
				}
			} else {

				DatosRestriccion nr = new DatosRestriccion();
				for (int p = 0; p < gh.getCantPostes(); p++) {
					if (i.isPorUnidadTiempo()) {
						multiplicador = gh.getDuracionPostes(p);
					}
				}
				if (i.isPorUnidadTiempo()) {
					multiplicador = gh.getDuracionPaso();
				}
				nr.agregarTermino(nqver, multiplicador);
				nr.agregarTermino(icd.getnExceso(), multiplicador);
				nr.setNombre(generarNombre("caudalVertimientoExterno"));
				nr.setSegundoMiembro(limite * multiplicador);
				nr.setTipo(Constantes.RESTMAYOROIGUAL);
				restricciones.put(nr.getNombre(), nr);
			}
		}
	}

	public void aportarImpacto(Impacto i, DatosObjetivo obj) {
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		double costoUnit = i.getCostoUnit().getValor(instanteActual);
		if (i.getTipoImpacto() == Constantes.HIDRO_INUN_AGUAS_ARRIBA
				|| i.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO)
			costoUnit /= Constantes.M3XHM3;
		ImpactoCompDesp icd = (ImpactoCompDesp) i.getCompDesp();
		double multiplicador = 1.0;

		if (!i.isPorPoste()) {
			String nExceso = icd.getnExceso();
			/*
			 * el exceso se obtiene en m3 y costo
			 */
			if (i.isPorUnidadTiempo() || i.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO
					|| i.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO)
				multiplicador = gh.getDuracionPaso();
			obj.agregarTermino(nExceso, costoUnit * multiplicador);
		} else {
			for (int p = 0; p < i.getCantPostes(); p++) {
				if (i.isPorUnidadTiempo() || i.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO
						|| i.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO)
					multiplicador = gh.getDuracionPostes(p);
			
				obj.agregarTermino(icd.getnExcesop()[p], costoUnit * multiplicador);
			}
		}
	}


}
