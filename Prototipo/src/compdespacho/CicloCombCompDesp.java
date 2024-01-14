/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CicloCombCompDesp is part of MOP.
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
import java.util.Iterator;
import java.util.Set;

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import logica.CorridaHandler;
import parque.CicloCombinado;
import parque.GeneradorTermico;
import utilitarios.Constantes;

/**
 * ATENCIÓN: EL COMPORTAMIENTO DESPACHO DEL CICLO COMBINADO HEREDA DE GENERADOR,
 * MIENTRAS QUE EL PARTICIPANTE CiclocCombinado HEREDA DE LA CLASE PADRE
 * Participante
 * 
 * @author UT469262
 * 
 * 
 *         DE ENTRADA SE VA A PROGRAMAR SOLO EL COMPORTAMIENTO CCSINESTADO O TAL
 *         VEZ CON ESTADO SOLO EN SIMULACIÓN
 *
 */
public class CicloCombCompDesp extends GeneradorCompDesp {

	private static String compCC; // comportamiento general del CC

	private CicloCombinado cc;

	private GeneradorTermico tGs;

	private GeneradorTermico tVs;

	private int cantModCCsIni; // cantidad de modulos de TG combinados al inicio del paso

	private int cantModDispTGs; // cantidad de modulos de TG disponibles

	private int cantModDispTVs; // cantidad de modulos de ciclo de vapor disponibles

	private double cosArranque1TG; // costos de Arranque por módulo TG en ciclo abierto en dólares
	private double cosArranque1CC; // costos de Arranque por módulo TG en ciclo combinado en dólares

	private ArrayList<Double> listaIncrementosVB; // TODO: SE USARA EN EL COMPORTAMIETNO CON ESTADO

	private double potTerMinTecTG; // potencia térmica en MW para el mínimo técnico de un módulo en ciclo abierto

	private double potTerMinTecCC; // potencia térmica en MW para el mínimo técnico de un módulo en ciclo cerrado
	// puede no coincidir con el anterior porque la potencia de mínimo de la TG no
	// sea igual

	private double potTerPropTG; // potencia tórmica en MW por cada MW eléctrico por encima del mínimo de TG en
									// ciclo abierto
	private double potTerPropCC; // potencia tórmica en MW por cada MW eléctrico por encima del mínimo de TG en
									// ciclo combinado

	private double potMax1CC; // potencia eléctrica máxima por módulo de TG combinada, incluso la parte de
								// ciclo de vapor
	private double potMax1TG; // potencia eléctrica máxima por módulo de TG en ciclo abierto
	private double potMax1CV; // potencia eléctrica máxima generable por módulo de ciclo de vapor
	private double relPot; // cociente potMax1CC/poteMax1TG
	private double potMin1TG; // potencia eléctrica mínima por módulo de TG en ciclo abierto
	private double potMin1CC; // potencia eléctrica mínima por módulo de TG en ciclo combinado

	private String[] nnmodTGp; // nombres de las variables enteras cantidad de
								// módulos de TG en paralelo por poste
	private String nnmodCC; // nombre de la variable cant. entera de TGs en ciclo combinado, constante en el
							// paso

	private String[] npotTGp; // nombre de la variable potencia en MW de las TGs en ciclo abierto

	private String[] npotCCp; // nombre de la variable potencia en MW de las TGs en ciclo combinado incluso la
								// constribución del ciclo de vapor

	// Las variables con los nombres de las variables de control de la potencia
	// total son las npotp del generador array npotp

	private String[][] nenerTpc; // la energía térmica en MWh por poste y combustible, primer índice poste,
									// segundo combustible

	private String flexibilidadTGs;

	private String flexibilidadTV;

	private Hashtable<Integer, String> indNomCombustible;
	private Hashtable<String, Double> coefsEnerTer;

	public CicloCombCompDesp() {
		super();
		cc = (CicloCombinado) this.participante;
		indNomCombustible = new Hashtable<Integer, String>();
		coefsEnerTer = new Hashtable<String, Double>();

	}

	public CicloCombCompDesp(CicloCombinado cce) {
		super();
		cc = cce;
		indNomCombustible = new Hashtable<Integer, String>();
	}

	/**
	 * PARA ACCEDER AL COMPORTAMIENTO GENERAL String compMinTec =
	 * parametros.get(Constantes.COMPMINTEC);
	 */

	/**
	 * 
	 * @param potMin  Potencia en el mónimo tócnico por módulo en MW
	 * @param potMax  Potencia móxima por módulo en MW
	 * @param rendMin Rendimiento en por uno en el mónimo tócnico
	 * @param rendMax Rendimiento móximo en por uno
	 * @return potencia tórmica en MW por cada MW elóctrico generado por encima del
	 *         mónimo tócnico
	 */
	public double calcPotEspTerProp(double potMin, double potMax, double rendMin, double rendMax) {
		return (potMax * rendMin - potMin * rendMax) / (rendMax * rendMin * (potMax - potMin));
	}

	/**
	 * 
	 * @param potMin  Potencia en el mónimo tócnico por módulo en MW
	 * @param rendMin Rendimiento en por uno en el mónimo tócnico
	 * @return Potencia tórmica en MW para el mónimo tócnico de un módulo
	 */
	public double calcPotTerMinTec(double potMin, double rendMin) {
		return potMin / rendMin;
	}

//	/**
//	 * 
//	 * @param potMin Potencia en el mónimo tócnico por módulo en MW          
//	 * @param potMax Potencia móxima por módulo en MW           
//	 * @param rendMin Rendimiento en por uno en el mónimo tócnico            
//	 * @param rendMax Rendimiento móximo en por uno
//	 *            
//	 * @return potencia tórmica en MW por cada MW elóctrico generado por encima
//	 *         del mónimo tócnico
//	 */
//	public double calcPotTerProp(double potMin, double potMax, double rendMin, double rendMax) {
//		// return (potMax / rendMax - potMin / rendMin) / (potMax - potMin);
//		return (potMax * rendMin - potMin * rendMax) / (rendMax * rendMin * (potMax - potMin));
//	}

	// TODO: REVISAR
	public double calcPotTerPropMax(double rendMax) {
		return 1 / rendMax;
	}

	public void crearVariablesControl() {

		GeneradorTermico generadorTG = ((CicloCombinado) this.participante).getTGs();

		int cantPostes = participante.getCantPostes();
		String nombre;

		for (int ip = 0; ip < cantPostes; ip++) {

			// crea potTG por poste
			nombre = generarNombre("potTG", Integer.toString(ip));
			DatosVariableControl pTG = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA,
					null, Constantes.INFNUESTRO);
			this.variablesControl.put(nombre, pTG);
			npotTGp[ip] = nombre;

			// crea potCC por poste
			nombre = generarNombre("potCC", Integer.toString(ip));
			DatosVariableControl pCC = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA,
					null, Constantes.INFNUESTRO);
			this.variablesControl.put(nombre, pCC);
			npotCCp[ip] = nombre;

			// crea potencia total del ciclo por poste
			nombre = generarNombre("pot", Integer.toString(ip));
			DatosVariableControl pTot = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA,
					null, Constantes.INFNUESTRO);
			this.variablesControl.put(nombre, pTot);
			npotp[ip] = nombre;

			// Crea variables de cantidad de módulos TG en ciclo abierto operativos
			nombre = generarNombre("nmodTG", Integer.toString(ip));
			DatosVariableControl nModTG = new DatosVariableControl(nombre, Constantes.VCENTERA, Constantes.VCPOSITIVA,
					null, (double) cantModDispTGs);
			this.variablesControl.put(nombre, nModTG);
			nnmodTGp[ip] = nombre;

			// crear enerTp,c matriz
			Set<String> set = generadorTG.getCombustibles().keySet();
			Iterator<String> itr = set.iterator();
			String clave;

			// TODO FALTA ARREGLAR LA VARIACIÓN DE RENDIMIENTO SEGÚN EL COMBUSTIBLE
			// TODO DONDE SE CREA EL nenerTpc??????
			int ic = 0;
			nenerTpc[ip] = new String[set.size()];
			DatosVariableControl nv;
			while (itr.hasNext()) {
				clave = itr.next();
				this.indNomCombustible.put(ic, clave);
				nombre = generarNombre("enerTPC", ip + "_" + clave);
				nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
						Constantes.INFNUESTRO);
				this.variablesControl.put(nombre, nv);
				nenerTpc[ip][ic] = nombre;
				ic++;
			}
		}

		nombre = generarNombre("nmodCC");
		DatosVariableControl nModCC = new DatosVariableControl(nombre, Constantes.VCENTERA, Constantes.VCPOSITIVA, null,
				(double) cantModDispTGs);
		this.variablesControl.put(nombre, nModCC);
		nnmodCC = nombre;

	}

	/**
	 * Crea restricciones de suma de potencia total a partir de las potencias de TGs
	 * en ciclo abierto y en ciclo combinado
	 */
	private Hashtable<String, DatosRestriccion> cargarSumaPotencias() {
		Hashtable<String, DatosRestriccion> nrs = new Hashtable<String, DatosRestriccion>();
		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(this.getNpotp()[p], 1.0);
			nr.agregarTermino(npotTGp[p], -1.0);
			nr.agregarTermino(npotCCp[p], -1.0);

			nr.setNombre(generarNombre("potTotal", Integer.toString(p)));
			nr.setSegundoMiembro(0.0);
			nr.setTipo(Constantes.RESTIGUAL);
			nrs.put(nr.getNombre(), nr);
		}
		return nrs;
	}

	/**
	 * Invoca la carga de los mínimos tecnicos de TGs en ciclo abierto y cerrado.
	 */
	private Hashtable<String, DatosRestriccion> cargarMinTecs() {

		Hashtable<String, DatosRestriccion> nuevas = new Hashtable<String, DatosRestriccion>();

		boolean multiposte = true;
		String sufijo = "TG-CA";
		nuevas.putAll(cargarMinTecAux(sufijo, multiposte, npotTGp, nnmodTGp, potMin1TG));

		multiposte = false;
		sufijo = "TG-CC";
		String[] aux = new String[1];
		aux[0] = nnmodCC;
		nuevas.putAll(cargarMinTecAux(sufijo, multiposte, npotCCp, aux, potMin1CC));

		return nuevas;

	}

	/**
	 * Crea restricciones de mínimo tócnico de TG o de CC según los parámetros con
	 * que se invoque Este método debe ser invocado para ambos
	 * 
	 * @param prefijo    TG o CC según el caso
	 * 
	 * @param multiPoste indica si el mónimo tócnico es diferente por poste
	 * 
	 * @param npots      los nombres de las variables de potencia en MW
	 * @param nmods      los nombres de las variables de cantidad de módulos. Si es
	 *                   multiposte=false en el array hay un solo valor.
	 * 
	 * @param min        la potencia mínima en MW
	 * 
	 * @return devuelve la restricción
	 */
	private Hashtable<String, DatosRestriccion> cargarMinTecAux(String sufijo, boolean multiPoste, String[] npots,
			String[] nmods, double min) {
		Hashtable<String, DatosRestriccion> nrs = new Hashtable<String, DatosRestriccion>();
		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(npots[p], 1.0);
			if (multiPoste)
				nr.agregarTermino(nmods[p], -min);
			if (!multiPoste)
				nr.agregarTermino(nmods[0], -min);
			nr.setNombre(generarNombre("potMin" + sufijo, Integer.toString(p)));
			nr.setSegundoMiembro(0.0);
			nr.setTipo(Constantes.RESTMAYOROIGUAL);
			nrs.put(nr.getNombre(), nr);
		}

		return nrs;
	}

	/**
	 * Invoca la carga de los máximos de TGs en ciclo abierto y cerrado.
	 */
	private Hashtable<String, DatosRestriccion> cargarMaximos() {
		Hashtable<String, DatosRestriccion> nuevas = new Hashtable<String, DatosRestriccion>();

		boolean multiposte = true;
		String sufijo = "TG-CA";
		nuevas.putAll(cargarMaxAux(sufijo, multiposte, npotTGp, nnmodTGp, potMax1TG));

		multiposte = false;
		sufijo = "TG-CC";
		String[] aux = new String[1];
		aux[0] = nnmodCC;
		nuevas.putAll(cargarMaxAux(sufijo, multiposte, npotCCp, aux, potMax1CC));

		return nuevas;

	}

	/**
	 * Crea restricciones de máximo de TG o de CC según los parámetros con que se
	 * invoque Este método debe ser invocado para ambos
	 * 
	 * @param multiPoste
	 * @return
	 */
	private Hashtable<String, DatosRestriccion> cargarMaxAux(String prefijo, boolean multiPoste, String[] npots,
			String[] nmods, double max) {
		Hashtable<String, DatosRestriccion> nuevas = new Hashtable<String, DatosRestriccion>();

		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(npots[p], -1.0);
			if (multiPoste)
				nr.agregarTermino(nmods[p], max);
			if (!multiPoste)
				nr.agregarTermino(nmods[0], max);

			nr.setNombre(generarNombre("potMax" + prefijo, Integer.toString(p)));
			nr.setSegundoMiembro(0.0);
			nr.setTipo(Constantes.RESTMAYOROIGUAL);
			nuevas.put(nr.getNombre(), nr);
		}

		return nuevas;
	}

	/**
	 * Restricción de cantidad de módulos máxima de TGs que pueden usarse y de
	 * cantidad máxima de módulos de TG combinados
	 */
	private Hashtable<String, DatosRestriccion> cargarMaxCantMod() {

		Hashtable<String, DatosRestriccion> nuevas = new Hashtable<String, DatosRestriccion>();

		// nModTGp + nModCC ≤ cantTGdisp para p=1, Nposte
		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(nnmodCC, 1.0);
			nr.agregarTermino(nnmodTGp[p], 1.0);
			nr.setNombre(generarNombre("CantModTGMax", Integer.toString(p)));
			nr.setSegundoMiembro((double) cantModDispTGs);
			nr.setTipo(Constantes.RESTMENOROIGUAL);
			nuevas.put(nr.getNombre(), nr);
		}

		// nModCC <= cantCVdisp * relacionTGCC
		// relacionTGCCInst cantidad de TGs máxima para cada CV disponible.
		// relacionTGCCInst = entero por exceso (cantTGinstaladas/cantCVinstalados)

		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("TVDisponibles"));
		nr.agregarTermino(nnmodCC, 1.0);
		nr.setSegundoMiembro((double) (cantModDispTVs * cc.getRelacionTGCCInst()));
		nr.setTipo(Constantes.RESTMENOROIGUAL);
		nuevas.put(nr.getNombre(), nr);

		return nuevas;
	}

	/**
	 * Carga la restricción de máxima potencia obtenible con las TGs en ciclo
	 * combinado resultante de la disponibilidad de ciclos de vapor
	 * 
	 * potCCp ≤ potMax1TG*nModCC + potMax1CV *cantCVdisp
	 * 
	 * y la restricción dada por la relación de potencia relPot de aprovechamiento
	 * de los gases de salida de TG en los ciclos de vapor (ejemplo relpot = 1.5)
	 * 
	 * potCCp ≤ potMax1TG*relpot*nModCC
	 */
	private Hashtable<String, DatosRestriccion> cargarMaxPorCicloVapor() {
		// potCCp - potMax1TG*nModCC <= potMax1CV *cantCVdisp para cada poste p
		Hashtable<String, DatosRestriccion> nuevas = new Hashtable<String, DatosRestriccion>();
		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(npotCCp[p], 1.0);
			nr.agregarTermino(nnmodCC, -potMax1TG);
			nr.setNombre(generarNombre("potMaxPorCV", Integer.toString(p)));
			nr.setSegundoMiembro(potMax1CV * cantModDispTVs);
			nr.setTipo(Constantes.RESTMENOROIGUAL);
			nuevas.put(nr.getNombre(), nr);
		}

		// potCCp - potMax1TG*relpot*nModCC ≤ 0
		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(npotCCp[p], 1.0);
			nr.agregarTermino(nnmodCC, -potMax1TG * relPot);
			nr.setNombre(generarNombre("potMaxPorRelPot", Integer.toString(p)));
			nr.setSegundoMiembro(0.0);
			nr.setTipo(Constantes.RESTMENOROIGUAL);
			nuevas.put(nr.getNombre(), nr);
		}

		return nuevas;

	}

	/**
	 * Para cada poste
	 * 
	 * suma en c (fc * enerTp,c) - potTerPropTG *durposp *potTGp - (potTerMintec1TG
	 * - potTerPropTG*potMinTG)*durposp* nModTGp - potTerPropCC *durposp *potCCp -
	 * (potTerMintec1CC - potTerPropCC*potMinCC)*durposp* nModCC
	 */

	private Hashtable<String, DatosRestriccion> cargarEnergiaTermica() {
		CicloCombinado generador = (CicloCombinado) this.participante;
		Hashtable<String, DatosRestriccion> nuevas = new Hashtable<String, DatosRestriccion>();

		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			for (int ic = 0; ic < generador.getCombustibles().size(); ic++) {
				nr.agregarTermino(nenerTpc[p][ic], this.coefsEnerTer.get(this.indNomCombustible.get(ic)));
			}

			// consumo de energía de TGs en ciclo abierto
			nr.agregarTermino(npotTGp[p], -potTerPropTG * cc.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
			nr.agregarTermino(nnmodTGp[p],
					-(potTerMinTecTG - potTerPropTG * potMin1TG) * cc.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);

			// consumo de energía de TGs en ciclo combinado
			nr.agregarTermino(npotCCp[p], -potTerPropCC * cc.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
			nr.agregarTermino(nnmodCC,
					-(potTerMinTecCC - potTerPropCC * potMin1CC) * cc.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);

			nr.setNombre(generarNombre("energiaTermica", Integer.toString(p)));
			nr.setSegundoMiembro(0.0);
			nr.setTipo(Constantes.RESTMAYOROIGUAL);
			nuevas.put(nr.getNombre(), nr);
		}

		return nuevas;
	}

	public Hashtable<Integer, String> getIndNomCombustible() {
		return indNomCombustible;
	}

	public void setIndNomCombustible(Hashtable<Integer, String> indNomCombustible) {
		this.indNomCombustible = indNomCombustible;
	}

	public Hashtable<String, Double> getCoefsEnerTer() {
		return coefsEnerTer;
	}

	public void setCoefsEnerTer(Hashtable<String, Double> coefsEnerTer) {
		this.coefsEnerTer = coefsEnerTer;
	}

	public void cargarRestricciones() {

		this.restricciones.putAll(cargarMaxCantMod());
		this.restricciones.putAll(cargarMaximos());
		this.restricciones.putAll(cargarMaxPorCicloVapor());
		this.restricciones.putAll(cargarMinTecs());
		this.restricciones.putAll(cargarEnergiaTermica());
		this.restricciones.putAll(cargarSumaPotencias());
	}

	public void contribuirObjetivo() {

		// TODO FALTA TODO LO DE COSTO DE ARRANQUE

		DatosObjetivo costo = new DatosObjetivo();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		// El costo de combustible es tomado en cuenta en los combustibles
		// asociados al generador

		// Costo variable no combustible
		for (int p = 0; p < cc.getCantPostes(); p++) {
			costo.agregarTermino(this.npotTGp[p], cc.getTGs().getCostoVariable().getValor(instanteActual)
					* cc.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
			costo.agregarTermino(this.npotCCp[p],
					((relPot - 1) * cc.getCCs().getCostoVariable().getValor(instanteActual)
							+ (1 / relPot) * cc.getTGs().getCostoVariable().getValor(instanteActual))
							* cc.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
		}

		costo.setTerminoIndependiente(cc.getTGs().getCostoFijo().getValor(instanteActual)); // TODO OJO ESTE Y TODOS LOS
																							// COSTOS FIJOS
		this.objetivo.contribuir(costo);

	}

	public String[] getNnmodTGp() {
		return nnmodTGp;
	}

	public String getNnmodCC() {
		return nnmodCC;
	}

	public String[] getNpotTGp() {
		return npotTGp;
	}

	public String[] getNpotCCp() {
		return npotCCp;
	}

	public String[][] getNenerTpc() {
		return nenerTpc;
	}

	public void setNnmodTGp(String[] nnmodTGp) {
		this.nnmodTGp = nnmodTGp;
	}

	public void setNnmodCC(String nnmodCC) {
		this.nnmodCC = nnmodCC;
	}

	public void setNpotTGp(String[] npotTGp) {
		this.npotTGp = npotTGp;
	}

	public void setNpotCCp(String[] npotCCp) {
		this.npotCCp = npotCCp;
	}

	public void setNenerTpc(String[][] nenerTpc) {
		this.nenerTpc = nenerTpc;
	}

	public String[] getNpotp() {
		return npotp;
	}

	public void setNpotp(String[] npotp) {
		this.npotp = npotp;
	}

	public CicloCombinado getCc() {
		return cc;
	}

	public GeneradorTermico gettGs() {
		return tGs;
	}

	public GeneradorTermico gettVs() {
		return tVs;
	}

	public int getCantModCCsIni() {
		return cantModCCsIni;
	}

	public int getCantModDispTGs() {
		return cantModDispTGs;
	}

	public int getCantModDispTVs() {
		return cantModDispTVs;
	}

	public double getCosArranque1TG() {
		return cosArranque1TG;
	}

	public double getCosArranque1CC() {
		return cosArranque1CC;
	}

	public double getPotTerMinTecTG() {
		return potTerMinTecTG;
	}

	public double getPotTerMinTecCC() {
		return potTerMinTecCC;
	}

	public double getPotTerPropTG() {
		return potTerPropTG;
	}

	public double getPotTerPropCC() {
		return potTerPropCC;
	}

	public double getPotMax1CC() {
		return potMax1CC;
	}

	public double getPotMax1TG() {
		return potMax1TG;
	}

	public double getPotMax1CV() {
		return potMax1CV;
	}

	public double getRelPot() {
		return relPot;
	}

	public void setRelPot(double relPot) {
		this.relPot = relPot;
	}

	public double getPotMin1TG() {
		return potMin1TG;
	}

	public double getPotMin1CC() {
		return potMin1CC;
	}

	public String getCompCC() {
		return compCC;
	}

	public String getFlexibilidadTGs() {
		return flexibilidadTGs;
	}

	public String getFlexibilidadTV() {
		return flexibilidadTV;
	}

	public void setCc(CicloCombinado cc) {
		this.cc = cc;
	}

	public void settGs(GeneradorTermico tGs) {
		this.tGs = tGs;
	}

	public void settVs(GeneradorTermico tVs) {
		this.tVs = tVs;
	}

	public void setCantModCCsIni(int cantModCCsIni) {
		this.cantModCCsIni = cantModCCsIni;
	}

	public void setCantModDispTGs(int cantModDispTGs) {
		this.cantModDispTGs = cantModDispTGs;
	}

	public void setCantModDispTVs(int cantModDispTVs) {
		this.cantModDispTVs = cantModDispTVs;
	}

	public void setCosArranque1TG(double cosArranque1TG) {
		this.cosArranque1TG = cosArranque1TG;
	}

	public void setCosArranque1CC(double cosArranque1CC) {
		this.cosArranque1CC = cosArranque1CC;
	}

	public void setPotTerMinTecTG(double potTerMinTecTG) {
		this.potTerMinTecTG = potTerMinTecTG;
	}

	public void setPotTerMinTecCC(double potTerMinTecCC) {
		this.potTerMinTecCC = potTerMinTecCC;
	}

	public void setPotTerPropTG(double potTerPropTG) {
		this.potTerPropTG = potTerPropTG;
	}

	public void setPotTerPropCC(double potTerPropCC) {
		this.potTerPropCC = potTerPropCC;
	}

	public void setPotMax1CC(double potMax1CC) {
		this.potMax1CC = potMax1CC;
	}

	public void setPotMax1TG(double potMax1TG) {
		this.potMax1TG = potMax1TG;
	}

	public void setPotMax1CV(double potMax1CV) {
		this.potMax1CV = potMax1CV;
	}

	public void setPotMin1TG(double potMin1TG) {
		this.potMin1TG = potMin1TG;
	}

	public void setPotMin1CC(double potMin1CC) {
		this.potMin1CC = potMin1CC;
	}

	public static void setCompCC(String compCC) {
		CicloCombCompDesp.compCC = compCC;
	}

	public void setFlexibilidadTGs(String flexibilidadTGs) {
		this.flexibilidadTGs = flexibilidadTGs;
	}

	public void setFlexibilidadTV(String flexibilidadTV) {
		this.flexibilidadTV = flexibilidadTV;
	}

	public String getNenerTpc(int p, String nombre) {
		Set<String> set = cc.getCombustibles().keySet();
		Iterator<String> itr = set.iterator();

		int ic = 0;
		while (itr.hasNext()) {
			if (itr.next().equalsIgnoreCase(nombre)) {
				return nenerTpc[p][ic];
			}
			ic++;
		}
		return null;
	}

	public ArrayList<Double> getListaIncrementosVB() {
		return listaIncrementosVB;
	}

	public void setListaIncrementosVB(ArrayList<Double> listaIncrementosVB) {
		this.listaIncrementosVB = listaIncrementosVB;
	}

}
