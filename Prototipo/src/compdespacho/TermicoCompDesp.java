/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TermicoCompDesp is part of MOP.
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

import parque.GeneradorTermico;
import utilitarios.Constantes;

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import datatypesSalida.DatosCurvaOferta;
import datatypesSalida.DatosEPPUnEscenario;
import logica.CorridaHandler;

/**
 * Clase encargada de modelar el comportamiento de un generador tórmico en el
 * problema de despacho
 * 
 * @author ut602614
 *
 */
public class TermicoCompDesp extends GeneradorCompDesp {
	private GeneradorTermico gt;

	private static String compMinimosTecnicosPorDefecto;

	private Integer cantModIni; // cantidad de modulos en paralelo al inicio del
								// paso
	private Integer cantModDisp; // cantidad de modulos disponibles

	// cantModForzadop es la cantidad de
	// módulos forzados cuando el
	// comportamiento es dosPasos
	private int[] cantModForzadop;
	private Double cosArranque; // costos de Arranque por módulo en dólares
	private Double cosParada; // costos de Parada por módulo en dólares
	private ArrayList<Double> listaIncrementosVB; // TODO: CARGAR LUEGO DE
													// PRIMERA VERSIóN

	private double potTerMinTec; // potencia tórmica en MW para el mínimo
									// técnico de un módulo
	private double potEspTerProp; // potencia tórmica en MW por cada MW
									// elóctrico generado por un módulo por
									// encima de su mínimo
	private double potEspTerPropMax; // potencia tórmica en MW por cada MW
										// elóctrico generado por un módulo en
										// el móximo
	private double potMax; // potencia elóctrica móxima por módulo
	private double minTec; // potencia elóctrica mónima por módulo

	private String[] nnmodp; // nombres de las variables enteras cantidad de
								// módulos en paralelo por poste
	private String nnmod0; // nombre de la variable entera cantidad de módulos
							// en paralelo en el paso
	private String narranquesmod; // nombre de la variable entera cantidad de
									// arranques de módulos en el paso
	private String nparadasmod; // nombre de la variable entera cantidad de
								// paradas de módulos en el paso
	private String nmodfin; // nombre de la variable entera cantidad de módulos
							// al fin del paso
	private String[][] nenerTpc; // la energía térmica en MWh por poste y combustible, primer índice poste, segundo combustible

	private String[] nxe; // nombre de las variables binarias auxiliares para

	private Hashtable<Integer, String> indNomCombustible = new Hashtable<Integer,String>();
	private Hashtable<String, Double> coefsEnerTer;

	// forzamiento con arranque

	private String compMinTec;
	private String flexibilidadMin;


	public TermicoCompDesp() {
		super();
		gt = (GeneradorTermico) this.participante;

	}

	public TermicoCompDesp(GeneradorTermico gte) {
		super();
		gt = gte;

	}

	/**
	 * 
	 * @param potMin
	 *            Potencia en el mínimo técnico por módulo en MW
	 * @param rendMin
	 *            Rendimiento en por uno en el mínimo técnico
	 * @return Potencia tórmica en MW para el mínimo técnico de un módulo
	 */
	public double calcPotTerMinTec(double potMin, double rendMin) {
		return potMin / rendMin;

	}

	/**
	 * 
	 * @param potMin
	 *            Potencia en el mínimo técnico por módulo en MW
	 * @param potMax
	 *            Potencia móxima por módulo en MW
	 * @param rendMin
	 *            Rendimiento en por uno en el mínimo técnico
	 * @param rendMax
	 *            Rendimiento móximo en por uno
	 * @return potencia tórmica en MW por cada MW elóctrico generado por encima
	 *         del mínimo técnico
	 */
	public double calcPotEspTerProp(double potMin, double potMax, double rendMin, double rendMax) {		
		return (potMax * rendMin - potMin * rendMax) / (rendMax * rendMin * (potMax - potMin));
	}

	// TODO: REVISAR
	public double calcPotEspTerPropMax(double rendMax) {
		return 1 / rendMax;
	}

	public void crearVariablesControl() {
		GeneradorTermico generador = (GeneradorTermico) this.participante;
		String compMinTec = parametros.get(Constantes.COMPMINTEC);
		String flexibilidadMin = parametros.get(Constantes.COMPFLEXIBILIDAD);
		this.setCompMinTec(compMinTec);
		this.setFlexibilidadMin(flexibilidadMin);
		String compBellman = parametros.get(Constantes.COMPVALORESBELLMAN);
		for (int ip = 0; ip < participante.getCantPostes(); ip++) {
			// crear potp
			String nombre = generarNombre("pot", Integer.toString(ip));

			DatosVariableControl nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA,
					null, Constantes.INFNUESTRO);

			Double cotaMax = null;
	
			if (compMinTec.equalsIgnoreCase(Constantes.TERSINMINTEC)) {
				cotaMax = this.cantModDisp * this.potMax;

			} else if (compMinTec.equalsIgnoreCase(Constantes.TERMINTECFORZADO)) {
				cotaMax = this.cantModForzadop[ip] * this.potMax;
			}

			nv.setCotaSuperior(cotaMax);
			/**
			 * SOLO PARA TERMINTECFORZADO Atención: Si se despachan finalmente
			 * más módulos que los forzados en el postprocesamiento para hallar
			 * los costos deben tenerse en cuenta
			 *
			 */

			if (compMinTec.equalsIgnoreCase(Constantes.TERMINTECFORZADO)) {
				Double cotaMin = this.cantModForzadop[ip] * this.minTec;
				nv.setCotaInferior(cotaMin);
			}

			this.variablesControl.put(nombre, nv);
			npotp[ip] = nombre;

			// crear enerTp,c matriz
			Set<String> set = generador.getCombustibles().keySet();
			Iterator<String> itr = set.iterator();
			String clave;

			int ic = 0;
			nenerTpc[ip] = new String[set.size()];
			while (itr.hasNext()) {
				clave = itr.next();
				this.indNomCombustible.put(ic,clave);
				nombre = generarNombre("enerTPC", ip + "_" + clave);
				nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
						Constantes.INFNUESTRO);
				this.variablesControl.put(nombre, nv);
				nenerTpc[ip][ic] = nombre;
				ic++;
			}

		}

		if (compMinTec.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			// crear arranquesMod (VARIABLE ENTERA)
			narranquesmod = generarNombre("arranques");
			DatosVariableControl nv = new DatosVariableControl(narranquesmod, Constantes.VCENTERA,
					Constantes.VCPOSITIVA, null, (double) (this.cantModDisp - this.cantModIni));
			this.variablesControl.put(narranquesmod, nv);

			// crear paradasMod (VARIABLE ENTERA)
			nparadasmod = generarNombre("paradas");
			nv = new DatosVariableControl(nparadasmod, Constantes.VCENTERA, Constantes.VCPOSITIVA, null,
					(double) this.cantModIni);
			this.variablesControl.put(nparadasmod, nv);

			// crear nModFin (VARIABLE ENTERA)
			nmodfin = generarNombre("modfin");
			nv = new DatosVariableControl(nmodfin, Constantes.VCENTERA, Constantes.VCPOSITIVA, null,
					(double) this.cantModDisp);
			this.variablesControl.put(nmodfin, nv);

			if (compBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				for (int e = 0; e < this.cantModDisp; e++) {
					this.nxe[e] = generarNombre("nxe", Integer.toString(e));
					nv = new DatosVariableControl(this.nxe[e], Constantes.VCBINARIA, null, null, Constantes.INFNUESTRO);
					this.variablesControl.put(nv.getNombre(), nv);
				}
			}

		} else if (compMinTec.equalsIgnoreCase(Constantes.TERVARENTERAS)) {
			if (flexibilidadMin.equalsIgnoreCase(Constantes.TERFLEXSEMANAL)) {
				// crear nMod0 (VARIABLE ENTERA)
				nnmod0 = generarNombre("nmod0");
				DatosVariableControl nv = new DatosVariableControl(nnmod0, Constantes.VCENTERA, Constantes.VCPOSITIVA,
						null, (double) this.cantModDisp);
				this.variablesControl.put(nnmod0, nv);

			} else if ((flexibilidadMin.equalsIgnoreCase(Constantes.TERFLEXHORARIO))) {
				// crear nModp (VARIABLE ENTERA)
			
				nnmodp = new String[participante.getCantPostes()];
				for (int ip = 0; ip < participante.getCantPostes(); ip++) {
					nnmodp[ip] = generarNombre("nmod", Integer.toString(ip));
					DatosVariableControl nv = new DatosVariableControl(nnmodp[ip], Constantes.VCENTERA,
							Constantes.VCPOSITIVA, null, (double) this.cantModDisp);
					this.variablesControl.put(nnmodp[ip], nv);
				}
			}
		}
	} 

	public GeneradorTermico getGt() {
		return gt;
	}

	public void setGt(GeneradorTermico gt) {
		this.gt = gt;
	}

	/**
	 * Crea restricciones de mínimo técnico
	 * 
	 * @param multiPoste
	 *            indica si el mínimo técnico es diferente por poste
	 * @return devuelve la restricción
	 */
	private Hashtable<String, DatosRestriccion> cargarMinTec(boolean multiPoste) {
		Hashtable<String, DatosRestriccion> nrs = new Hashtable<String, DatosRestriccion>();

		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(npotp[p], 1.0);
			if (multiPoste)
				nr.agregarTermino(nnmodp[p], -this.minTec);
			if (!multiPoste)
				nr.agregarTermino(nnmod0, -this.minTec);
			nr.setNombre(generarNombre("potMin", Integer.toString(p)));
			nr.setSegundoMiembro(0.0);
			nr.setTipo(Constantes.RESTMAYOROIGUAL);
			nrs.put(nr.getNombre(), nr);
		}

		return nrs;
	}

	private Hashtable<String, DatosRestriccion> cargarPotMax(boolean multiPoste) {
		Hashtable<String, DatosRestriccion> nuevas = new Hashtable<String, DatosRestriccion>();

		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			nr.agregarTermino(npotp[p], -1.0);
			if (multiPoste)
				nr.agregarTermino(nnmodp[p], this.potMax);
			if (!multiPoste)
				nr.agregarTermino(nnmod0, this.potMax);

			nr.setNombre(generarNombre("potMax", Integer.toString(p)));
			nr.setSegundoMiembro(0.0);
			nr.setTipo(Constantes.RESTMAYOROIGUAL);
			nuevas.put(nr.getNombre(), nr);
		}

		return nuevas;
	}

	private Hashtable<String, DatosRestriccion> cargarEnergiaTermica(boolean multiPoste, boolean esSINMINTEC) {
		GeneradorTermico generador = (GeneradorTermico) this.participante;
		Hashtable<String, DatosRestriccion> nuevas = new Hashtable<String, DatosRestriccion>();

		double potEsp = (esSINMINTEC) ? this.getPotEspTerPropMax() : this.getPotEspTerProp();

		for (int p = 0; p < participante.getCantPostes(); p++) {
			DatosRestriccion nr = new DatosRestriccion();
			for (int ic = 0; ic < generador.getCombustibles().size(); ic++) {
				nr.agregarTermino(nenerTpc[p][ic], this.coefsEnerTer.get(this.indNomCombustible.get(ic)));
			}

			nr.agregarTermino(npotp[p], -potEsp * gt.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
			if (multiPoste && !esSINMINTEC)
				nr.agregarTermino(nnmodp[p], -gt.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA
						* (this.getPotTerMinTec() - potEsp * this.minTec));

			if (!multiPoste && !esSINMINTEC) {
				nr.agregarTermino(nnmod0, -gt.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA
						* (this.getPotTerMinTec() - potEsp * this.minTec));
			}
			nr.setNombre(generarNombre("energiaTermica", Integer.toString(p)));
			nr.setSegundoMiembro(0.0);
			nr.setTipo(Constantes.RESTMAYOROIGUAL);
			nuevas.put(nr.getNombre(), nr);
		}

		return nuevas;
	}

	public void cargarRestricciones() {

		String compMinTec = parametros.get(Constantes.COMPMINTEC);
		String flexibilidadMin = parametros.get(Constantes.COMPFLEXIBILIDAD);
		String compBellman = parametros.get(Constantes.COMPVALORESBELLMAN);
		DatosRestriccion nr = new DatosRestriccion();

		boolean multiPoste = flexibilidadMin.equalsIgnoreCase(Constantes.TERFLEXHORARIO);
		if (compMinTec.equalsIgnoreCase(Constantes.TERVARENTERAS)
				|| compMinTec.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			// si es varenteras o varenterasyestado
			// Crear 2.2.4.1 a)

			this.restricciones.putAll(cargarMinTec(multiPoste));

			// Crear 2.2.4.1 b)

			this.restricciones.putAll(cargarPotMax(multiPoste));

			// Crear 2.2.4.2
			this.restricciones.putAll(cargarEnergiaTermica(multiPoste, false));

		} else if (compMinTec.equalsIgnoreCase(Constantes.TERSINMINTEC)
				|| compMinTec.equalsIgnoreCase(Constantes.TERMINTECFORZADO)) {
			// NO EXISTE 2.2.4.1 a)
			// 2.2.4.1 b) se pasó a restricción de cota
			// Crear 2.2.4.2 simplificada

			this.restricciones.putAll(cargarEnergiaTermica(multiPoste, true));
		}
		// } else if
		// (compMinTec.equalsIgnoreCase(Constantes.TERMINTECFORZADO)) {
		// // 2.2.4.1 a) se pasó a restricción de cota
		// // 2.2.4.1 b) simplificada se pasó a restricción de cota
		// // Crear 2.2.4.2 simplificada con cantidad de módulos forzados
		// nr = new DatosRestriccion();
		// nr = cargarEnergiaTermicaForzado(multiPoste);
		// this.restricciones.put(nr.getNombre(), nr);
		// }

		if (compMinTec.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			// nmodfin <= cantModDisponibles, Se agregó en cargar variables
			// como restricción de cota
			// Crear nModFin = cantModIni + arranquesMod - paradasMod
			nr = new DatosRestriccion();
			nr.agregarTermino(nmodfin, 1.0);
			nr.agregarTermino(narranquesmod, -1.0);
			nr.agregarTermino(nparadasmod, 1.0);
			nr.setTipo(Constantes.RESTIGUAL);
			nr.setSegundoMiembro((double) this.cantModIni);

			// restricción de incrementos de Valor de Bellman
			if (compBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				nr = new DatosRestriccion();
				for (int e = 0; e < this.cantModDisp; e++) {
					nr.agregarTermino(this.nxe[e], 1.0);
				}
				nr.agregarTermino(this.nmodfin, -1.0);
				nr.setSegundoMiembro(0.0);
				nr.setTipo(Constantes.RESTIGUAL);
				nr.setNombre("incrementosVB");
				this.restricciones.put(nr.getNombre(), nr);
			}
		}

	}

	public void contribuirObjetivo() {		
		long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
		String compMinTec = parametros.get(Constantes.COMPMINTEC);
		// String flexibilidadMin = parametros.get(Constantes.COMPFLEXIBILIDAD);
		String compBellman = parametros.get(Constantes.COMPVALORESBELLMAN);
		DatosObjetivo costo = new DatosObjetivo();

		// El costo de combustible es tomado en cuenta en los combustibles
		// asociados al generador
		if (compMinTec.equalsIgnoreCase(Constantes.TERVARENTERASYVARESTADO)) {
			// sumar al objetivo arranquesMod*cosArranque +
			// paradasMod*cosParada
			DatosObjetivo objetivo = new DatosObjetivo();
			objetivo.agregarTermino(narranquesmod, this.cosArranque);
			objetivo.agregarTermino(nparadasmod, this.cosParada);
			this.objetivo.contribuir(objetivo);

			// sumar incrementos de Valor de Bellman
			// LOS MóDULOS ENCENDIDOS SE CONSIDERAN UN RECURSO
			if (compBellman.equalsIgnoreCase(Constantes.PROBINCREMENTOS)) {
				for (int e = 0; e < this.cantModDisp; e++) {
					this.objetivo.agregarTermino(this.nxe[e], -this.listaIncrementosVB.get(e));
				}

			}
		}

		for (int p = 0; p < gt.getCantPostes(); p++) {
			costo.agregarTermino(this.npotp[p],
					gt.getCostoVariable().getValor(instanteActual) * gt.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
		}

		costo.setTerminoIndependiente(gt.getCostoFijo().getValor(instanteActual));
		this.objetivo.contribuir(costo);

	}

	public Integer getCantModIni() {
		return cantModIni;
	}

	public void setCantModIni(Integer cantModIni) {
		this.cantModIni = cantModIni;
	}

	public Double getCosArranque() {
		return cosArranque;
	}

	public void setCosArranque(Double cosArranque) {
		this.cosArranque = cosArranque;
	}

	public Double getCosParada() {
		return cosParada;
	}

	public void setCosParada(Double cosParada) {
		this.cosParada = cosParada;
	}

	public static String getCompMinimosTecnicosPorDefecto() {
		return compMinimosTecnicosPorDefecto;
	}

	public static void setCompMinimosTecnicosPorDefecto(String compMinimosTecnicosPorDefecto) {
		TermicoCompDesp.compMinimosTecnicosPorDefecto = compMinimosTecnicosPorDefecto;
	}

	public String[] getNnmodp() {
		return nnmodp;
	}

	public void setNnmodp(String[] nnmodp) {
		this.nnmodp = nnmodp;
	}

	public String getNnmod0() {
		return nnmod0;
	}

	public void setNnmod0(String nnmod0) {
		this.nnmod0 = nnmod0;
	}

	public String[][] getNenerTpc() {
		return nenerTpc;
	}

	public void setNenerTpc(String[][] nenerTpc) {
		this.nenerTpc = nenerTpc;
	}

	public String getNarranquesmod() {
		return narranquesmod;
	}

	public void setNarranquesmod(String narranquesmod) {
		this.narranquesmod = narranquesmod;
	}

	public String getNparadasmod() {
		return nparadasmod;
	}

	public void setNparadasmod(String nparadasmod) {
		this.nparadasmod = nparadasmod;
	}

	public int[] getCantModForzadop() {
		return cantModForzadop;
	}

	public void setCantModForzadop(int[] cantModForzadop) {
		this.cantModForzadop = cantModForzadop;
	}

	public String getNenerTpc(int p, String nombre) {
		GeneradorTermico generador = (GeneradorTermico) this.participante;
		Set<String> set = generador.getCombustibles().keySet();
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

	public String[] getNxe() {
		return nxe;
	}

	public void setNxe(String[] nxe) {
		this.nxe = nxe;
	}

	public double getPotTerMinTec() {
		return potTerMinTec;
	}

	public void setPotTerMinTec(double potTerMinTec) {
		this.potTerMinTec = potTerMinTec;
	}

	public double getPotEspTerProp() {
		return potEspTerProp;
	}

	public void setPotEspTerProp(double potEspTerProp) {
		this.potEspTerProp = potEspTerProp;
	}

	public double getPotEspTerPropMax() {
		return potEspTerPropMax;
	}

	public void setPotEspTerPropMax(double potEspTerPropMax) {
		this.potEspTerPropMax = potEspTerPropMax;
	}

	public Integer getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(Integer cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public ArrayList<Double> getListaIncrementosVB() {
		return listaIncrementosVB;
	}

	public void setListaIncrementosVB(ArrayList<Double> listaIncrementosVB) {
		this.listaIncrementosVB = listaIncrementosVB;
	}

	public double getPotMax() {
		return potMax;
	}

	public void setPotMax(double potMax) {
		this.potMax = potMax;
	}

	public double getMinTec() {
		return minTec;
	}

	public void setMinTec(double minTec) {
		this.minTec = minTec;
	}

	public String getNmodfin() {
		return nmodfin;
	}

	public void setNmodfin(String nmodfin) {
		this.nmodfin = nmodfin;
	}

	public String getCompMinTec() {
		return compMinTec;
	}

	public void setCompMinTec(String compMinTec) {
		this.compMinTec = compMinTec;
	}

	public String getFlexibilidadMin() {
		return flexibilidadMin;
	}

	public void setFlexibilidadMin(String flexibilidadMin) {
		this.flexibilidadMin = flexibilidadMin;
	}

	public void setCoefsEnerTer(Hashtable<String, Double> coefsEnerTer) {
		this.coefsEnerTer = coefsEnerTer;
	}

	public Hashtable<String, Double> getCoefsEnerTer() {
		return coefsEnerTer;
	}
}
