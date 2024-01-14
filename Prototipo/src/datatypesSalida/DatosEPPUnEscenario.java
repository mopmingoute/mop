/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosEPPUnEscenario is part of MOP.
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

package datatypesSalida;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import parque.Acumulador;
import parque.CicloCombinado;
import parque.ContratoEnergia;
import parque.Demanda;
import parque.Falla;
import parque.GeneradorEolico;
import parque.GeneradorFotovoltaico;
import parque.GeneradorHidraulico;
import parque.GeneradorTermico;
import parque.Impacto;
import parque.ImpoExpo;

import utilitarios.Constantes;

public class DatosEPPUnEscenario implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int numeroEsc;

	private int cantPasos;

	/**
	 * Potencias en MW de un escenario; primer índice ordinal de recurso segundo
	 * índice paso tercer índice poste
	 * 
	 */
	private double[][][] potencias;

	/**
	 * Energías en MWh de un escenario; primer índice ordinal de recurso segundo
	 * índice paso
	 */
	private double[][] energias;

	/**
	 * Gradiente de gestión en USD/MW del recurso primer óndice ordinal de recurso
	 * segundo óndice paso tercer óndice combustible (sólo toma mós de un valor en
	 * tórmicos)
	 */
	private double[][][] gradGestion;

	/**
	 * Costos marginales en USD/MWh de un escenario; primer óndice barra EN EL ORDEN
	 * QUE APARECEN EN LA COLECCIóN barrasActivas de la Corrida segundo óndice paso
	 * tercer óndice poste
	 * 
	 */
	private double[][][] costosMarg;

	/**
	 * Cantidad de módulos disponibles de un escenario: primer índice ordinal de
	 * recurso segundo índice paso
	 * 
	 * Para el ciclo combinado se toma la disponibilidad de las TGs
	 */
	private int[][] cantModDisp;
	

	/**
	 * Cantidad de módulos despachados de un escenario: 
	 * primer índice ordinal de recurso
	 * segundo índice paso
	 * tercer índice poste
	 * 
	 * Para el ciclo combinado se toma la cantidad de TGs tanto en ciclo abierto como combinado
	 */
	private int[][][] cantModDesp;	


	/**
	 * Costos en USD de un escenario primer índice ordinal de recurso segundo índice
	 * paso
	 */
	private double[][] costos;

	/**
	 * Duración de los postes del paso primer índice paso segundo índice poste
	 */
	private int[][] durPos;

	/**
	 * Numpos de cada intervalo de muestreo en el escenario para cada paso 
	 * clave: ordinal del paso
	 * valor: la postización empleada en el paso
	 */
	private Hashtable<Integer, ArrayList<Integer>> numposPasos;

	/**
	 * aportes, volumenes turbinados y vertidos por paso en hm3 de los
	 * GeneradorHidraulico, clave nombre del generador hidro, segundo índice paso
	 */
	private Hashtable<String, double[]> aportem3s;
	private Hashtable<String, double[]> turhm3;
	private Hashtable<String, double[]> verhm3;

	/**
	 * Para cada paso del escenario:
	 * clave: nombre de un ciclo combinado 
	 * valores: 
	 * -las energías generadas por TGs y CVs 
	 * por turbinas en ciclo abierto y por turbinas combinadas incluso su parte de
	 * ciclos de vapor 
	 * -los módulos de TG disponibles
	 * -los módulos de CV disponibles
	 * -las TGs despachadas como combinadas 
	 */
	private Hashtable<String, double[]> energiaTGPorPasoGWh;  // índice paso
	private Hashtable<String, double[]> energiaCVPorPasoGWh;
	private Hashtable<String, double[]> energiaAbPorPasoGWh;
	private Hashtable<String, double[]> energiaCombPorPasoGWh;
	private Hashtable<String, int[]> cantModTGdisp;
	private Hashtable<String, int[]> cantModCVdisp;    // primer índice paso segundo índice poste
	private Hashtable<String, int[][]> cantModTGAbdesp;
	private Hashtable<String, int[][]> cantModTGCombdesp;
	
	/**
	 * Cantindad de módulos en funcionamiento en el último intervalo de muestreo del paso
	 * primer índice ordinal de recurso, segundo índice paso
	 */
	private int[][] cantFuncUltimoIMpaso;
	
	/**
	 * clave: nombre de un contrato interrumpible
	 * valor: ingreso por ventas (MUSD), energía no suministrada (GWh) y multa pagada (MUSD), por paso.
	 */
	private Hashtable<String, double[]> ingVentasCIMUSD;
	private Hashtable<String, double[]> multaCIMUSD;
	private Hashtable<String, double[]> enerNoEntGWh;

	/**
	 * Guarda los valores de los datos detallados de los recursos que se
	 * seleccionaron clave es el tipo_nombre del recurso valor es un double[][][]
	 * primer índice paso segundo índice atributo con datos detallados tercer indice
	 * ordinal dentro de los valores del atributo en el paso, típicamente recorre
	 * los postes de un paso
	 */
	private Hashtable<String, double[][][]> datosDetallados;

	private DatosParamSalida dtParam;

	private Hashtable<String, Integer> tablaInd;
	Hashtable<String, double[][]> energiasComb;
	Hashtable<String, double[][]> volumenesComb;
	Hashtable<String, double[][]> costosComb;

	/**
	 * Estructura que almacena la cantidad DE HORAS de cada combinación posible de
	 * fallas para cada paso de tiempo del escenario -clave nombre de la demanda "-"
	 * nombre de la falla "-" String con los número de escalón en orden separados
	 * por guión - ejemplo demTotal-fallaDemTotal-1-2-4 quiere decir que hubo en el
	 * paso de tiempo falla 1, falla2 y falla 4 en la fallaDemaTotal de la demanda
	 * demTotal -el int[] recorre los pasos de tiempo del escenario
	 */
	private Hashtable<String, int[]> horasFallaPorTipo;

	private Hashtable<Integer, DatosCurvaOferta> curvOfertas;

	/**
	 * Potencias en el despacho sin exportación en MW de un escenario; primer índice
	 * ordinal de recurso segundo índice paso tercer índice poste
	 * 
	 */
	private double[][][] potenciasSinExp;

	/**
	 * Costos marginales sin exportación en USD/MWh de un escenario; primer óndice
	 * barra EN EL ORDEN QUE APARECEN EN LA COLECCIóN barrasActivas de la Corrida
	 * segundo óndice paso tercer óndice poste
	 * 
	 */
	private double[][][] costosMargSinExp;

	public DatosEPPUnEscenario(int cantRec, int cantPasos, int cantBarras, Hashtable<String, double[][][]> datosDet,
			DatosParamSalida dtParam, Hashtable<String, Integer> tablaInd) {
		this.cantPasos = cantPasos;
		potencias = new double[cantRec][cantPasos][];
		potenciasSinExp = new double[cantRec][cantPasos][];
		costos = new double[cantRec][cantPasos];
		energias = new double[cantRec][cantPasos];
		gradGestion = new double[cantRec][cantPasos][];
		costosMarg = new double[cantBarras][cantPasos][];
		costosMargSinExp = new double[cantBarras][cantPasos][];
		cantModDisp = new int[cantRec][cantPasos];
		cantModDesp = new int[cantRec][cantPasos][];
		durPos = new int[cantPasos][];
		aportem3s = new Hashtable<String, double[]>();
		turhm3 = new Hashtable<String, double[]>();
		verhm3 = new Hashtable<String, double[]>();
		energiasComb = new Hashtable<String, double[][]>();
		volumenesComb = new Hashtable<String, double[][]>();
		costosComb = new Hashtable<String, double[][]>();
		horasFallaPorTipo = new Hashtable<String, int[]>();
		curvOfertas = new Hashtable<Integer, DatosCurvaOferta>();
		for (int p = 0; p < cantPasos; p++) {
			DatosCurvaOferta dco = new DatosCurvaOferta();
			dco.setPaso(p);
			dco.setVariables(new Hashtable<String, ArrayList<Double>>());
			curvOfertas.put(p, dco);
		}
		datosDetallados = datosDet;
		this.dtParam = dtParam;
		this.tablaInd = tablaInd;

		energiaTGPorPasoGWh = new Hashtable<String, double[]>();
		energiaCVPorPasoGWh = new Hashtable<String, double[]>();
		energiaAbPorPasoGWh = new Hashtable<String, double[]>();
		energiaCombPorPasoGWh = new Hashtable<String, double[]>();
		cantModTGdisp = new Hashtable<String, int[]>();
		cantModCVdisp = new Hashtable<String, int[]>();
		cantModTGAbdesp = new Hashtable<String, int[][]>();
		cantModTGCombdesp = new Hashtable<String, int[][]>();
		ingVentasCIMUSD = new Hashtable<String, double[]>() ;
		multaCIMUSD = new Hashtable<String, double[]>();
		enerNoEntGWh = new Hashtable<String, double[]>();
		cantFuncUltimoIMpaso = new int[cantRec][cantPasos];
	}



	/**
	 * Carga en this los resultados de un paso a partir del DatosSalidaPaso dsp
	 * 
	 * @param numPaso es el nómero de paso en la simulación
	 * @param dsp     es el DatosSalidaPaso con los resultados del paso
	 * 
	 */
	public void cargaResultUnPaso(int numPaso, DatosSalidaPaso dsp) {

		int[][] param = dtParam.getParam();
		int durPaso = dsp.getPaso().getDurpaso();
		int cantPos = dsp.getPaso().getDurPostes().length; // cantidad de postes en ese paso
		int[] durPosPaso = dsp.getPaso().getDurPostes();
		int[] auxDurpos = new int[cantPos];
		for (int ip = 0; ip < cantPos; ip++) {
			auxDurpos[ip] = durPosPaso[ip];
		}
		durPos[numPaso] = auxDurpos;

		ArrayList<DatosBarraSP> barras = dsp.getRed().getBarras();
		int indBarra = 0;
		for (DatosBarraSP db : barras) {
			ArrayList<DatosDemandaSP> dems = db.getDemandas();
			// Demandas y fallas
			for (DatosDemandaSP d : dems) {
				String nombre = d.getNombre();
				double[] pot = d.getPotencias();
				int ind = tablaInd.get(nombre);
				double enerPaso = 0.0;
				double[] aux = new double[cantPos];
				potencias[ind][numPaso] = aux;
				for (int ip = 0; ip < cantPos; ip++) {
					potencias[ind][numPaso][ip] = pot[ip];
					enerPaso = enerPaso + pot[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
				}
				energias[ind][numPaso] = enerPaso;

				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = Demanda.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS)) {
								dat[numPaso][i] = new double[cantPos];
								for (int ipos = 0; ipos < cantPos; ipos++) {
									dat[numPaso][i][ipos] = d.getPotencias()[ipos];
								}
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
								double[] e = { enerPaso };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
								double[] c = { 0 }; // d.getCostoTotalPaso agregar mótodo en DatosDemandaSP
								dat[numPaso][i] = c;
							}
						}
					}
				}

				// Fallas
				DatosFallaSP df = d.getFalla();
				nombre = Constantes.FALLA + "_" + df.getNombreDemanda();
				double[][] pot2 = df.getPotencias(); // pot2 y df.getPotencias: primer óndice poste, segundo óndice
														// escalón

				int cantEsc = pot2[0].length; // cantEsc cantidad de escalones de la falla
				String nombreE = "";
				for (int ie = 0; ie < cantEsc; ie++) {
					// Para cada escalón de falla
					nombreE = d.getNombre() + "_EscFalla" + ie;
//					nombreE = nombre + "_Esc" + ie;
					ind = tablaInd.get(nombreE);
					enerPaso = 0.0;
					aux = new double[cantPos];
					potencias[ind][numPaso] = aux;
					for (int ip = 0; ip < cantPos; ip++) {
						potencias[ind][numPaso][ip] = pot2[ip][ie];
						enerPaso = enerPaso + pot2[ip][ie] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
					}
					energias[ind][numPaso] = enerPaso;
					costos[ind][numPaso] = df.getCosto()[ie];
					// Carga datos detallados si corresponde
					if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
						ArrayList<String> atDet = Falla.getAtributosDetallados();
						if (atDet != null) {
							double[][][] dat = datosDetallados
									.get(Constantes.FALLA + "_" + df.getNombreDemanda() + "_EscFalla" + ie);
							for (int i = 0; i < atDet.size(); i++) {
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS)) {
									dat[numPaso][i] = new double[cantPos];
									for (int ipos = 0; ipos < cantPos; ipos++) {
										dat[numPaso][i][ipos] = pot2[ipos][ie];
									}
								}
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
									double[] e = { enerPaso };
									dat[numPaso][i] = e;
								}
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
									double[] c = { df.getCosto()[ie] };
									dat[numPaso][i] = c;
								}
							}
						}
					}
				}
				// carga ahora las horas por tipo de falla
				// TODO cuando haya más de una falla por demanda va a tener que arreglarse esto

				for (int ip = 0; ip < cantPos; ip++) { // recorre los postes del paso
					boolean hayFalla = false;
					boolean[] auxb = new boolean[cantEsc];
					for (int ie = 0; ie < cantEsc; ie++) { // recorre los escalones de la falla
						// Para cada escalón de falla
						nombreE = d.getNombre() + "_EscFalla" + ie;
						ind = tablaInd.get(nombreE);
						if (df.getPotencias()[ip][ie] > 0) {
							hayFalla = true;
							auxb[ie] = true;
						}
					}
					String clave = creaClaveTipoFalla(d.getNombre(), d.getFalla().getNombreFalla(), auxb);
					if (hayFalla) {
						if (horasFallaPorTipo.containsKey(clave)) { // en el escenario ya hubo ese tipo de falla
							int[] aux2 = horasFallaPorTipo.get(clave);
							aux2[numPaso] += durPos[numPaso][ip] / utilitarios.Constantes.SEGUNDOSXHORA;
						} else { // en el escenario no hubo este tipo de falla, se crea la entrada
							int[] aux3 = new int[cantPasos];
							aux3[numPaso] = durPos[numPaso][ip] / utilitarios.Constantes.SEGUNDOSXHORA;
							horasFallaPorTipo.put(clave, aux3);
						}
					}
				}
			}

			// Hidros

			ArrayList<DatosHidraulicoSP> hidro = db.getHidraulicos();
			for (DatosHidraulicoSP d : hidro) {
				String nombre = d.getNombre();
				// en el primer paso no existe la clave en el hastable, crea el par
				// (nombre,double[])
				if (turhm3.get(nombre) == null) {
					turhm3.put(nombre, new double[cantPasos]);
					verhm3.put(nombre, new double[cantPasos]);
					aportem3s.put(nombre, new double[cantPasos]);
				}
				double[] pot = d.getPotencias();
				int ind = tablaInd.get(nombre);
				cantModDisp[ind][numPaso] = d.getCantModDisp();
				double enerPaso = 0.0;
				double[] aux = new double[cantPos];
				potencias[ind][numPaso] = aux;
				for (int ip = 0; ip < pot.length; ip++) {
					potencias[ind][numPaso][ip] = pot[ip];
					enerPaso = enerPaso + pot[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
				}
				energias[ind][numPaso] = enerPaso;
				cantModDisp[ind][numPaso] = d.getCantModDisp();
				costos[ind][numPaso] = d.getCostoTotalPaso();

				double sumaT = 0;
				double sumaV = 0;
				for (int ip = 0; ip < cantPos; ip++) {
					sumaT += d.getQturb()[ip] * durPosPaso[ip] / utilitarios.Constantes.M3XHM3;
					sumaV += d.getQvert()[ip] * durPosPaso[ip] / utilitarios.Constantes.M3XHM3;
				}
				turhm3.get(nombre)[numPaso] = sumaT;
				verhm3.get(nombre)[numPaso] = sumaV;
				aportem3s.get(nombre)[numPaso] = d.getAporte();

				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = GeneradorHidraulico.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS))
								dat[numPaso][i] = d.getPotencias();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
								double[] e = { enerPaso };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.TURBINADOS))
								dat[numPaso][i] = d.getQturb();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VERTIDO)) {
								dat[numPaso][i] = d.getQvert();
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.TURB_PASO)) {
								double tprom = 0;
								for (int ip = 0; ip < cantPos; ip++) {
									tprom += d.getQturb()[ip] * durPosPaso[ip];
								}
								tprom = tprom / durPaso;
								double[] e = { tprom };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VERT_PASO)) {
								double tvert = 0;
								for (int ip = 0; ip < cantPos; ip++) {
									tvert += d.getQvert()[ip] * durPosPaso[ip];
								}
								tvert = tvert / durPaso;
								double[] e = { tvert };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.APORTE)) {
								double[] e = { d.getAporte() };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COEFENERGETICO))
								dat[numPaso][i] = d.getCoefEnergMWm3s();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VALAGUA)) {
								double[] valAgua = { d.getValAgua() };
								dat[numPaso][i] = valAgua; // en USD/hm3
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COTAAGUASARRIBA)) {
								double[] cota = { d.getCotaArribaIni() };
								dat[numPaso][i] = cota;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
								double[] costo = { d.getCostoTotalPaso() };
								dat[numPaso][i] = costo;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOSPENECO)) {
								double[] costo = { d.getCostoPenalEco() };
								dat[numPaso][i] = costo;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.DUALVERTIDO)) {
								double dualVert = d.getDualvert();
								double[] e = { dualVert };
								dat[numPaso][i] = e; // en USD/hm3
							}

							/**
							 * CONTROL DE COTAS
							 */

							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOPENCCOTASINF)) {
								double[] costo = { d.getCostoIncuplimientoInferior() };
								dat[numPaso][i] = costo;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOPENCCOTASSUP)) {
								double[] costo = { d.getCostoIncuplimientoSuperior() };
								dat[numPaso][i] = costo;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VOLPENINF)) {
								double[] volpeninf = { d.getVolumenInferiorIncumplido() };
								dat[numPaso][i] = volpeninf;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VOLPENSUP)) {
								double[] volpensup = { d.getVolumenSuperiorIncumplido() };
								dat[numPaso][i] = volpensup;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COTAPENINF)) {
								double[] cotapeninf = { d.getCotaInferiorIncumplida() };
								dat[numPaso][i] = cotapeninf;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COTAPENSUP)) {
								double[] cotapensup = { d.getCotaSuperiorIncumplida() };
								dat[numPaso][i] = cotapensup;
							}
						}
					}
				}
			}

			// Térmicos

			ArrayList<DatosTermicoSP> termo = db.getTermicos();
			for (DatosTermicoSP d : termo) {
				String nombre = d.getNombre();
				double[] pot = d.getPotencias();
				int ind = tablaInd.get(nombre);
				cantModDisp[ind][numPaso] = d.getCantModDisp();
				double enerPaso = 0.0;
				double[] aux = new double[cantPos];
				potencias[ind][numPaso] = aux;
				for (int ip = 0; ip < pot.length; ip++) {
					potencias[ind][numPaso][ip] = pot[ip];
					enerPaso = enerPaso + pot[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
				}
				cantModDesp[ind][numPaso] = d.getCantModDesp();
				energias[ind][numPaso] = enerPaso;
				gradGestion[ind][numPaso] = d.getGradGestion();
				costos[ind][numPaso] = d.getCostoTotPaso();
				cantFuncUltimoIMpaso[ind][numPaso] = d.getCantFuncUltimoIMpaso();
				d.getCantModDesp();

				// Carga resultados de combustible
				// en el primer paso no existe la clave en el hastable, crea el par
				// (nombre,double[][])
				if (energiasComb.get(nombre) == null) {
					energiasComb.put(nombre, new double[cantPasos][]);
					volumenesComb.put(nombre, new double[cantPasos][]);
					costosComb.put(nombre, new double[cantPasos][]);
				}
				energiasComb.get(nombre)[numPaso] = d.getEnerEC();
				volumenesComb.get(nombre)[numPaso] = d.getVolC();
				costosComb.get(nombre)[numPaso] = d.getCostoC();

				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = GeneradorTermico.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS))
								dat[numPaso][i] = d.getPotencias();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
								double[] e = { enerPaso };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
								double[] c = { d.getCostoTotPaso() };
								dat[numPaso][i] = c;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERCOM1)) {
								double[] ec = { d.getEnerEC()[0] };
								dat[numPaso][i] = ec;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VOLCOM1)) {
								double[] ec = { d.getVolC()[0] };
								dat[numPaso][i] = ec;
							}
							if (d.getVolC().length == 2) {
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERCOM2)) {
									double[] ec = { d.getEnerEC()[1] };
									dat[numPaso][i] = ec;
								}
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VOLCOM2)) {
									double[] ec = { d.getVolC()[1] };
									dat[numPaso][i] = ec;
								}
							}
						}
					}
				}
			}

			// Ciclos combinados

			ArrayList<DatosCicloCombSP> ciclos = db.getCiclosCombinados();
			for (DatosCicloCombSP d : ciclos) {
				String nombre = d.getNombre();
				double[] pot = d.getPotencias();
				int ind = tablaInd.get(nombre);
				cantModDisp[ind][numPaso] = d.getCantModTGDisp(); // son los de TG
				double enerPaso = 0.0;
				double[] aux = new double[cantPos];
				potencias[ind][numPaso] = aux;
				for (int ip = 0; ip < pot.length; ip++) {
					potencias[ind][numPaso][ip] = pot[ip];
					enerPaso = enerPaso + pot[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
				}
				energias[ind][numPaso] = enerPaso;
				gradGestion[ind][numPaso] = d.getGradGestion();
				costos[ind][numPaso] = d.getCostoTotPaso();

				// en el primer paso no existe la clave en el hastable, crea el par
				// (nombre,double[])
				if (energiaTGPorPasoGWh.get(nombre) == null) {
					energiaTGPorPasoGWh.put(nombre, new double[cantPasos]);
					energiaCVPorPasoGWh.put(nombre, new double[cantPasos]);
					energiaAbPorPasoGWh.put(nombre, new double[cantPasos]);
					energiaCombPorPasoGWh.put(nombre, new double[cantPasos]);
					cantModCVdisp.put(nombre, new int[cantPasos]);
					cantModTGdisp.put(nombre, new int[cantPasos]);
					cantModTGAbdesp.put(nombre, new int[cantPasos][cantPos]);
					cantModTGCombdesp.put(nombre, new int[cantPasos][cantPos]);
					
				}

				for (int ip = 0; ip < cantPos; ip++) {
					energiaTGPorPasoGWh.get(nombre)[numPaso] += d.getPotTGs()[ip] * durPosPaso[ip]
							/ (Constantes.SEGUNDOSXHORA * Constantes.MWHXGWH);
					energiaCVPorPasoGWh.get(nombre)[numPaso] += d.getPotTVs()[ip] * durPosPaso[ip]
							/ (Constantes.SEGUNDOSXHORA * Constantes.MWHXGWH);
					energiaAbPorPasoGWh.get(nombre)[numPaso] += d.getPotAb()[ip] * durPosPaso[ip]
							/ (Constantes.SEGUNDOSXHORA * Constantes.MWHXGWH);
					energiaCombPorPasoGWh.get(nombre)[numPaso] += d.getPotComb()[ip] * durPosPaso[ip]
							/ (Constantes.SEGUNDOSXHORA * Constantes.MWHXGWH);
					
				}
				
				cantModTGAbdesp.get(nombre)[numPaso] = d.getCantModTGAbDesp();	
				cantModTGCombdesp.get(nombre)[numPaso] = d.getCantModTGCombDesp();	

				// Carga resultados de combustible
				// en el primer paso no existe la clave en el hastable, crea el par
				// (nombre,double[][])
				if (energiasComb.get(nombre) == null) {
					energiasComb.put(nombre, new double[cantPasos][]);
					volumenesComb.put(nombre, new double[cantPasos][]);
					costosComb.put(nombre, new double[cantPasos][]);
				}
				energiasComb.get(nombre)[numPaso] = d.getEnerEC();
				volumenesComb.get(nombre)[numPaso] = d.getVolC();
				costosComb.get(nombre)[numPaso] = d.getCostoC();
				cantFuncUltimoIMpaso[ind][numPaso] = d.getCantFuncUltimoIMpaso();

				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = CicloCombinado.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS))
								dat[numPaso][i] = d.getPotencias();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTSCV))
								dat[numPaso][i] = d.getPotTVs();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTSTG))
								dat[numPaso][i] = d.getPotTGs();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTSAB))
								dat[numPaso][i] = d.getPotAb();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTSCOMB))
								dat[numPaso][i] = d.getPotComb();

							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.CANTMODTGDISP)) {
								double[] e = { d.getCantModTGDisp() };
								dat[numPaso][i] = e;
							}

							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.CANTMODCVDISP)) {
								double[] e = { d.getCantModCVDisp() };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
								double[] e = { enerPaso };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
								double[] c = { d.getCostoTotPaso() };
								dat[numPaso][i] = c;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERCOM1)) {
								double[] ec = { d.getEnerEC()[0] };
								dat[numPaso][i] = ec;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VOLCOM1)) {
								double[] ec = { d.getVolC()[0] };
								dat[numPaso][i] = ec;
							}
							if (d.getVolC().length == 2) {
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERCOM2)) {
									double[] ec = { d.getEnerEC()[1] };
									dat[numPaso][i] = ec;
								}
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VOLCOM2)) {
									double[] ec = { d.getVolC()[1] };
									dat[numPaso][i] = ec;
								}
							}
						}
					}
				}
			}

			// Eolos
			ArrayList<DatosEolicoSP> eolos = db.getEolicos();
			for (DatosEolicoSP d : eolos) {
				String nombre = d.getNombre();
				double[] pot = d.getPotencias();
				int ind = tablaInd.get(nombre);
				double enerPaso = 0.0;
				double[] aux = new double[cantPos];
				potencias[ind][numPaso] = aux;
				for (int ip = 0; ip < pot.length; ip++) {
					potencias[ind][numPaso][ip] = pot[ip];
					enerPaso = enerPaso + pot[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
				}
				energias[ind][numPaso] = enerPaso;
				double[] valor = new double[1];
				valor[0] = d.getGradGestion();
				gradGestion[ind][numPaso] = valor;
				costos[ind][numPaso] = d.getCostoTotalPaso();
				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = GeneradorEolico.getAtributosDetallados();
					double[][][] dat = datosDetallados.get(nombre);
					for (int i = 0; i < atDet.size(); i++) {
						if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS))
							dat[numPaso][i] = d.getPotencias();
						if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
							double[] e = { enerPaso };
							dat[numPaso][i] = e;
						}
						if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
							double[] c = { d.getCostoTotalPaso() };
							dat[numPaso][i] = c;
						}
					}
				}
			}

			// Fotovoltaicos
			ArrayList<DatosFotovoltaicoSP> fotos = db.getFotovoltaicos();
			for (DatosFotovoltaicoSP d : fotos) {
				String nombre = d.getNombre();
				double[] pot = d.getPotencias();
				int ind = tablaInd.get(nombre);
				double enerPaso = 0.0;
				double[] aux = new double[cantPos];
				potencias[ind][numPaso] = aux;
				for (int ip = 0; ip < pot.length; ip++) {
					potencias[ind][numPaso][ip] = pot[ip];
					enerPaso = enerPaso + pot[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
				}
				energias[ind][numPaso] = enerPaso;
				double[] valor = new double[1];
				valor[0] = d.getGradGestion();
				gradGestion[ind][numPaso] = valor;
				costos[ind][numPaso] = d.getCostoTotalPaso();
				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = GeneradorFotovoltaico.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS))
								dat[numPaso][i] = d.getPotencias();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
								double[] e = { enerPaso };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
								double[] c = { d.getCostoTotalPaso() };
								dat[numPaso][i] = c;
							}
						}
					}
				}
			}


			// Acumuladores
			ArrayList<DatosAcumuladorSP> acums = db.getAcumuladores();
			for (DatosAcumuladorSP d : acums) {
				String nombre = d.getNombre();
				double[] potI = d.getPotenciasIny();
				double[] potA = d.getPotenciasAlm();
				double[] energAcum = d.getEnergAcum();
				int ind = tablaInd.get(nombre);
				double enerPasoIny = 0.0;
				double enerPasoAlm = 0.0;
				double enerPasoNeta = 0.0;
				double[] aux = new double[cantPos];
				potencias[ind][numPaso] = aux;
				for (int ip = 0; ip < potI.length; ip++) {
					potencias[ind][numPaso][ip] = potI[ip];
					enerPasoIny = enerPasoIny + potI[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
					enerPasoAlm = enerPasoAlm + potA[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
					enerPasoNeta = enerPasoNeta - potA[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA
							+ potI[ip] * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
					// Neta inyectada a la red
				}
				energias[ind][numPaso] = enerPasoNeta;
				costos[ind][numPaso] = d.getCostoTotalPaso();
				double[] valor = new double[1];
				valor[0] = d.getGradGestion();
				gradGestion[ind][numPaso] = valor;
				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = Acumulador.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIASINY))
								dat[numPaso][i] = d.getPotenciasIny();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIASALM))
								dat[numPaso][i] = d.getPotenciasAlm();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIASNETAS))
								dat[numPaso][i] = d.getPotenciaNeta();
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIASINY)) {
								double[] e = { enerPasoIny };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIASALM)) {
								double[] e = { enerPasoAlm };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIASNETAS)) {
								double[] e = { enerPasoNeta };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAENACUMULADOR)) {
								dat[numPaso][i] = energAcum;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
								double[] c = { d.getCostoTotalPaso() };
								dat[numPaso][i] = c;
							}
						}
					}
				}
			}

			// ImpoExpos (resume la información sumando en todos los bloques)
			ArrayList<DatosImpoExpoSP> impoexpos = db.getImpoExpos();
			for (DatosImpoExpoSP d : impoexpos) {
				String nombre = d.getNombre();
				double[][] pot = d.getPotencias(); // potencia por bloque y poste
				int ind = tablaInd.get(nombre);
				double enerPaso = 0.0;
				double[] aux = new double[cantPos];
				potencias[ind][numPaso] = aux;
				for (int ip = 0; ip < cantPos; ip++) {
					double sumPot = 0; // auxiliar para sumar la potencia de todos los bloques
					for (int ib = 0; ib < pot.length; ib++) {
						sumPot += pot[ib][ip];
					}
					potencias[ind][numPaso][ip] = sumPot;
					enerPaso = enerPaso + sumPot * durPosPaso[ip] / Constantes.SEGUNDOSXHORA;
				}

				energias[ind][numPaso] = enerPaso;
				costos[ind][numPaso] = d.getCostoTotPaso();
				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = ImpoExpo.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS)) {
								dat[numPaso][i] = potencias[ind][numPaso];
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
								double[] e = { enerPaso };
								dat[numPaso][i] = e;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
								double[] c = { d.getCostoTotPaso() };
								dat[numPaso][i] = c;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.PRECIOMED)) {
								double[] c = { d.getPrecioMed() };
								dat[numPaso][i] = c;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.CMGPAIS)) {
								double[] c = { d.getCMgPais() };
								dat[numPaso][i] = c;
							}							
						}
					}
				}
			}

			// Impactos
			ArrayList<DatosImpactoSP> impactos = dsp.getImpactos();
			for (DatosImpactoSP im : impactos) {
				String nombre = im.getNombre();
				int ind = tablaInd.get(nombre);
				costos[ind][numPaso] = im.getCostoTotalPaso();
				boolean pp = im.getMagnitudPoste() != null;
				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = Impacto.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS)) {
								double[] c = { im.getCostoTotalPaso() };
								dat[numPaso][i] = c;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.MAGNITUDES)) {
								double[] m = { im.getMagnitudTotal() };
								dat[numPaso][i] = m;
							}
							if (pp) {
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.COSTOS))
									dat[numPaso][i] = im.getCostoPoste();
								if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.MAGNITUDES))
									dat[numPaso][i] = im.getMagnitudPoste();

							}
						}
					}
				}

			}

			// ContratosEnergia
			ArrayList<DatosContratoEnergiaSP> contratosEnergia = dsp.getContratosEnergia();
			for (DatosContratoEnergiaSP ce : contratosEnergia) {
				String nombre = ce.getNombre();
				int ind = tablaInd.get(nombre);
				costos[ind][numPaso] = ce.getValorPasoUSD();
				energias[ind][numPaso] = ce.getEnergiaPasoGWh() * utilitarios.Constantes.MWHXGWH;
				// Carga datos detallados si corresponde
				if (param[Constantes.PARAMSAL_IND_ATR_DET][ind] == 1) {
					ArrayList<String> atDet = ContratoEnergia.getAtributosDetallados();
					if (atDet != null) {
						double[][][] dat = datosDetallados.get(nombre);
						for (int i = 0; i < atDet.size(); i++) {
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VALORES)) {
								double[] c = { ce.getValorPasoUSD() };
								dat[numPaso][i] = c;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.VALORESMEDIOS)) {
								double inic = -999999;
								if (ce.getEnergiaPasoGWh() > 0)
									inic = ce.getValorPasoUSD() / ce.getEnergiaPasoGWh()
											/ utilitarios.Constantes.MWHXGWH;
								double[] c = { inic };
								dat[numPaso][i] = c;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIAS)) {
								double[] c = { ce.getEnergiaPasoGWh() };
								dat[numPaso][i] = c;
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.POTENCIAS)) {
								dat[numPaso][i] = ce.getPotencias();
							}
							if (atDet.get(i).equalsIgnoreCase(utilitarios.Constantes.ENERGIASACUM)) {
								double[] c = { ce.getEnergiaAcumAnioGWh() };
								dat[numPaso][i] = c;
							}
						}
					}
				}
			}
			
			

			// Costos marginales
			double[] auxCosmar = new double[cantPos];
			for (int ip = 0; ip < cantPos; ip++) {
				auxCosmar[ip] = db.getCostoMarginal()[ip];
			}
			costosMarg[indBarra][numPaso] = auxCosmar;
			indBarra++;
			

		}

		if (dsp.getDespSinExp() != null) {
			/**
			 * Carga en this el resultado de la iteración del despacho sin exportación
			 */
			Hashtable<String, double[]> dsinExp = dsp.getDespSinExp().getResultadoPotencias();
			Set<String> nombres = dsinExp.keySet();
			for (String nombre : nombres) {
				int ind = tablaInd.get(nombre);
				potenciasSinExp[ind][numPaso] = dsinExp.get(nombre);
			}

			indBarra = 0;
			for (DatosBarraSP db : barras) {
				String nb = db.getNombre();
				double[] cmg = dsp.getDespSinExp().getResultadoCMg().get(nb);
				costosMargSinExp[indBarra][numPaso] = cmg;
				indBarra++;
			}
		}
	}

	/**
	 * Crea una clave String para una falla de una demanda, según los escalones de
	 * falla que están activos (potencias no nulas)
	 * 
	 * @param d
	 * @param f
	 * @param potsFalla
	 * @return
	 */
	public static String creaClaveTipoFalla(String nombreDem, String nombreFalla, boolean[] hayFalla) {
		String clave = nombreDem + "-" + nombreFalla;
		for (int ie = 0; ie < hayFalla.length; ie++) {
			if (hayFalla[ie])
				clave = clave + "-" + ie;
		}
		return clave;
	}
	
	
	/**
	 * 
	 * @param nomRec nombre del recurso + escenario para arranques 
	 * @param anio año, por ejemplo 2025
	 * @return
	 */
	public String claveRecursoEsc(String nomRec, int esc) {
		return nomRec + "-" + esc;
	}
	
	

	public int getNumeroEsc() {
		return numeroEsc;
	}

	public void setNumeroEsc(int numeroEsc) {
		this.numeroEsc = numeroEsc;
	}

	/**
	 * Potencias en MW de un escenario; primer óndice ordinal de recurso segundo
	 * óndice paso tercer óndice poste
	 * 
	 */
	public double[][][] getPotencias() {
		return potencias;
	}

	public void setPotencias(double[][][] potencias) {
		this.potencias = potencias;
	}

	/**
	 * Energóas en MWh de un escenario; primer óndice ordinal de recurso segundo
	 * óndice paso
	 */
	public double[][] getEnergias() {
		return energias;
	}

	public Hashtable<String, double[][]> getEnergiasComb() {
		return energiasComb;
	}

	public void setEnergiasComb(Hashtable<String, double[][]> energiasComb) {
		this.energiasComb = energiasComb;
	}

	public Hashtable<String, double[][]> getVolumenesComb() {
		return volumenesComb;
	}

	public void setVolumenesComb(Hashtable<String, double[][]> volumenesComb) {
		this.volumenesComb = volumenesComb;
	}

	public Hashtable<String, double[][]> getCostosComb() {
		return costosComb;
	}

	public void setCostosComb(Hashtable<String, double[][]> costosComb) {
		this.costosComb = costosComb;
	}

	public Hashtable<Integer, ArrayList<Integer>> getNumposPasos() {
		return numposPasos;
	}

	public void setNumposPasos(Hashtable<Integer, ArrayList<Integer>> numposPasos) {
		this.numposPasos = numposPasos;
	}
	
	

	public Hashtable<String, double[]> getIngVentasCIMUSD() {
		return ingVentasCIMUSD;
	}

	public void setIngVentasCIMUSD(Hashtable<String, double[]> ingVentasCIMUSD) {
		this.ingVentasCIMUSD = ingVentasCIMUSD;
	}

	public Hashtable<String, double[]> getMultaCIMUSD() {
		return multaCIMUSD;
	}

	public void setMultaCIMUSD(Hashtable<String, double[]> multaCIMUSD) {
		this.multaCIMUSD = multaCIMUSD;
	}
	
	
	

	public int[][][] getCantModDesp() {
		return cantModDesp;
	}



	public void setCantModDesp(int[][][] cantModDesp) {
		this.cantModDesp = cantModDesp;
	}



	public Hashtable<String, int[]> getCantModTGdisp() {
		return cantModTGdisp;
	}



	public void setCantModTGdisp(Hashtable<String, int[]> cantModTGdisp) {
		this.cantModTGdisp = cantModTGdisp;
	}



	public Hashtable<String, int[]> getCantModCVdisp() {
		return cantModCVdisp;
	}



	public void setCantModCVdisp(Hashtable<String, int[]> cantModCVdisp) {
		this.cantModCVdisp = cantModCVdisp;
	}



	public Hashtable<String, int[][]> getCantModTGAbdesp() {
		return cantModTGAbdesp;
	}



	public void setCantModTGAbdesp(Hashtable<String, int[][]> cantModTGAbdesp) {
		this.cantModTGAbdesp = cantModTGAbdesp;
	}



	public Hashtable<String, int[][]> getCantModTGCombdesp() {
		return cantModTGCombdesp;
	}



	public void setCantModTGCombdesp(Hashtable<String, int[][]> cantModTGCombdesp) {
		this.cantModTGCombdesp = cantModTGCombdesp;
	}



	public double[][][] getPotenciasSinExp() {
		return potenciasSinExp;
	}



	public void setPotenciasSinExp(double[][][] potenciasSinExp) {
		this.potenciasSinExp = potenciasSinExp;
	}



	public double[][][] getCostosMargSinExp() {
		return costosMargSinExp;
	}



	public void setCostosMargSinExp(double[][][] costosMargSinExp) {
		this.costosMargSinExp = costosMargSinExp;
	}



	public Hashtable<String, double[]> getEnerNoEntGWh() {
		return enerNoEntGWh;
	}

	public void setEnerNoEntGWh(Hashtable<String, double[]> enerNoEntGWh) {
		this.enerNoEntGWh = enerNoEntGWh;
	}

	/**
	 * Costos marginales en USD/MWh de un escenario; primer óndice barra segundo
	 * óndice paso tercer óndice poste
	 * 
	 */
	public double[][][] getCostosMarg() {
		return costosMarg;
	}

	public void setCostosMarg(double[][][] costosMarg) {
		this.costosMarg = costosMarg;
	}

	public void setEnergias(double[][] energias) {
		this.energias = energias;
	}

	public int[][] getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(int[][] cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public double[][] getCostos() {
		return costos;
	}

	public void setCostos(double[][] costos) {
		this.costos = costos;
	}

	public Hashtable<String, int[]> getHorasFallaPorTipo() {
		return horasFallaPorTipo;
	}

	public void setHorasFallaPorTipo(Hashtable<String, int[]> horasFallaPorTipo) {
		this.horasFallaPorTipo = horasFallaPorTipo;
	}

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

	public Hashtable<String, double[]> getTurhm3() {
		return turhm3;
	}

	public void setTurhm3(Hashtable<String, double[]> turhm3) {
		this.turhm3 = turhm3;
	}

	public Hashtable<String, double[]> getVerhm3() {
		return verhm3;
	}

	public void setVerhm3(Hashtable<String, double[]> verhm3) {
		this.verhm3 = verhm3;
	}

	public Hashtable<String, double[]> getAportem3s() {
		return aportem3s;
	}

	public void setAportem3s(Hashtable<String, double[]> aportem3s) {
		this.aportem3s = aportem3s;
	}

	public int[][] getDurPos() {
		return durPos;
	}

	public void setDurPos(int[][] durPos) {
		this.durPos = durPos;
	}

	public Hashtable<String, double[][][]> getDatosDetallados() {
		return datosDetallados;
	}

	public void setDatosDetallados(Hashtable<String, double[][][]> datosDetallados) {
		this.datosDetallados = datosDetallados;
	}

	public double[][][] getGradGestion() {
		return gradGestion;
	}

	public void setGradGestion(double[][][] gradGestion) {
		this.gradGestion = gradGestion;
	}

	public DatosParamSalida getDtParam() {
		return dtParam;
	}

	public void setDtParam(DatosParamSalida dtParam) {
		this.dtParam = dtParam;
	}

	public Hashtable<String, Integer> getTablaInd() {
		return tablaInd;
	}

	public void setTablaInd(Hashtable<String, Integer> tablaInd) {
		this.tablaInd = tablaInd;
	}

	public Hashtable<String, double[]> getEnergiaTGPorPasoGWh() {
		return energiaTGPorPasoGWh;
	}

	public void setEnergiaTGPorPasoGWh(Hashtable<String, double[]> energiaTGPorPasoGWh) {
		this.energiaTGPorPasoGWh = energiaTGPorPasoGWh;
	}

	public Hashtable<String, double[]> getEnergiaCVPorPasoGWh() {
		return energiaCVPorPasoGWh;
	}

	public void setEnergiaCVPorPasoGWh(Hashtable<String, double[]> energiaCVPorPasoGWh) {
		this.energiaCVPorPasoGWh = energiaCVPorPasoGWh;
	}

	public Hashtable<String, double[]> getEnergiaAbPorPasoGWh() {
		return energiaAbPorPasoGWh;
	}

	public void setEnergiaAbPorPasoGWh(Hashtable<String, double[]> energiaAbPorPasoGWh) {
		this.energiaAbPorPasoGWh = energiaAbPorPasoGWh;
	}

	public Hashtable<String, double[]> getEnergiaCombPorPasoGWh() {
		return energiaCombPorPasoGWh;
	}

	public void setEnergiaCombPorPasoGWh(Hashtable<String, double[]> energiaCombPorPasoGWh) {
		this.energiaCombPorPasoGWh = energiaCombPorPasoGWh;
	}

	public double[] dameEnergias(String rec) {
		return energias[this.tablaInd.get(rec)];
	}

	public Hashtable<Integer, DatosCurvaOferta> getCurvOfertas() {
		return curvOfertas;
	}

	public void setCurvOfertas(Hashtable<Integer, DatosCurvaOferta> curvOfertas) {
		this.curvOfertas = curvOfertas;
	}


	public int[][] getCantFuncUltimoIMpaso() {
		return cantFuncUltimoIMpaso;
	}

	public void setCantFuncUltimoIMpaso(int[][] cantFuncUltimoIMpaso) {
		this.cantFuncUltimoIMpaso = cantFuncUltimoIMpaso;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
