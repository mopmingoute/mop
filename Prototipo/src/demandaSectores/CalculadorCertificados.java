/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CalculadorCertificados is part of MOP.
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

package demandaSectores;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import persistencia.LectorArchivosSalidaSimulDiaria;
import procesosEstocasticos.Serie;
import tiempo.LineaTiempo;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LectorDireccionArchivoDirectorio;

/**
 * Lee y almacena datos de curvas de carga de sectores dados por -coeficientes
 * fijos semanales, por tipo de día y horarios dentro del día -el nombre del
 * proceso estocástico de residuos
 * 
 * -el nombre de variables aleatorias y sus procesos estocásticos que afectan la
 * demanda -los parámetros de la función que a partir de esas VA afecta la
 * demanda estos dos últimos no se consideran ahora
 * 
 * @author ut469262
 *
 */
public class CalculadorCertificados {

	private DatosGenSectores datGenSec;
	private int anioGen;
	private int anioFac;
	private int cantDias;
	private int cantCron;
	private int cantHoras;
	private int cantIntMuestreo = utilitarios.Constantes.CANT_HORAS_DIA; // cantidad de intervalos de muestreo por día
																			// normalmante = 24
	private Hashtable<String, CoefSectorFact> coefSectores; // clave nombre del sector
	private String[] nombresSectores;
	private Hashtable<String, Double> propVerdeRecursos; // clave nombre del recurso, valor proporción de energía limpia
	private ArrayList<String> nombresRecursos;
	private String nombreDemanda; // nombre de la demanda total
	private String directorioDatos; // directorio dado por el usuario del que se leeerán los datos generales
	private String directorioCorrida; // directorio de los datos de salida de la simulación diaria

	/**
	 * Para cada hora del año elegido proporcion limpia de la generación cuando la
	 * energía limpia se atribuye con prioridad a la demanda local, calculado
	 * crónica a crónica. primer índice promedio y crónicas segundo índice hora del
	 * año elegido
	 * 
	 */
	private double[][] propVerdeGenPrioridadLocal;

	/**
	 * Para cada hora del año elegido proporcion limpia de la generación cuando el
	 * reparto de energía limpia entre demanda local y exportación es en la
	 * proporción de las energías, calculado crónica a crónica primer índice
	 * promedio y crónicas segundo índice hora del año elegido
	 */
	private double[][] propVerdeGenProporcional;

	/**
	 * Para cada hora la potencia de cada sector clave nombre del sector
	 * 
	 * valor double[] potencia para cada hora del año elegido
	 * 
	 */
	private Hashtable<String, double[]> potenciasSectores;

	/**
	 * Para cada hora del año elegido la potencia generada por cada recurso, en cada
	 * crónica y la potencia de la demanda. clave nombre del sector, "demanda_total"
	 * o "generacion_total" valor double[][] potencia promedio y en cada crónica
	 * para cada hora del año elegido primer índice crónica, con el indice cero para
	 * el promedio segundo índice hora del año
	 */
	private Hashtable<String, double[][]> potenciasRecursos;

	/**
	 * Para cada sector la proporción en el año de energía verde con el criterio de
	 * prioridad verde local y con el criterio de proporcionalidad clave nombre del
	 * sector o de la demanda total valor double[] la proporción verde en el
	 * promedio de las crónicas y en cada crónica
	 */
	private Hashtable<String, double[]> propVerdeSecPrioLocal;
	private Hashtable<String, double[]> propVerdeSecProporcional;

	/**
	 * Para cada sector, crónica y hora la proporción de energía verde con el
	 * criterio de prioridad verde local y con el criterio de proporcionalidad clave
	 * nombre del sector o de la demanda total valor double[][] primer índice
	 * crónica promedio de las crónicas y en cada crónica por hora segundo índice
	 * hora
	 */
	private Hashtable<String, double[][]> propVerdeSecPrioLocalHora;
	private Hashtable<String, double[][]> propVerdeSecProporcionalHora;

	public void calculaParticipaciones() {

		directorioDatos = LectorDireccionArchivoDirectorio.direccionLeida(true,
				"G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\CURVAS DE CARGA Y SECTORES\\Pruebas",
				"ENTRAR DIRECTORIO DE DATOS DE SECTORES");
		LectorDatosSectoresYRecursos lec1 = new LectorDatosSectoresYRecursos(directorioDatos);
		datGenSec = lec1.leerDatosGenSectores();
		nombresSectores = datGenSec.getNombresSectores();
		anioGen = datGenSec.getAnioGen();
		cantCron = datGenSec.getCantCronicas();

		cantHoras = 8760;
		cantDias = 365;
		if (LineaTiempo.bisiesto(anioGen)) {
			cantHoras = 8784;
			cantDias = 366;
		}

		// Las proporciones hora por hora de generación verde en el total
		// de la demanda local con los dos criterios
		propVerdeGenPrioridadLocal = new double[cantCron + 1][cantHoras];
		propVerdeGenProporcional = new double[cantCron + 1][cantHoras];

		potenciasSectores = new Hashtable<String, double[]>();
		potenciasRecursos = new Hashtable<String, double[][]>();
		propVerdeSecPrioLocal = new Hashtable<String, double[]>();
		propVerdeSecProporcional = new Hashtable<String, double[]>();
		propVerdeSecPrioLocalHora = new Hashtable<String, double[][]>();
		propVerdeSecProporcionalHora = new Hashtable<String, double[][]>();

		// Lee los datos generales de recursos y demanda
		coefSectores = lec1.leerCoefYEnerMes(datGenSec);
		propVerdeRecursos = lec1.leerRecursosYDemanda();
		Set setRec = propVerdeRecursos.keySet();

		nombresRecursos = lec1.getNombresRecursos();
		nombreDemanda = lec1.getNombreDemanda();
		directorioCorrida = lec1.getDirectorioCorrida();

		System.out.println("Termina lecturas de datos generales");

		/**
		 * Calcula las potencias demandadas por los sectores en cada hora del año
		 * elegido y las carga en potenciasSectores
		 */
		for (String ns : nombresSectores) {
			double[] aux = calculaPotHorSector(ns);
			potenciasSectores.put(ns, aux);
		}

		System.out.println("Termina calculo de potencias de sectores");

		/**
		 * Lee los datos de recursos de los archivos de resultados diarios calcula las
		 * generaciones por recurso promedio y por crónica en cada hora del año elegido
		 * y carga los resultados en potenciasRecursos
		 */
		calculaGenYDemTotal();

		System.out.println("Termina calculo de potencias de recursos");

		/**
		 * Calcula las propociones verdes de la generación total para la demanda en cada
		 * hora por crónica
		 */
		calculaPropsVerdesGenHora();
		System.out.println("Termina calculo de proporciones verdes para la generación total");

		/**
		 * Calcula las proporciones verdes de cada sector por crónica
		 */
		calculaPropsVerdesSectoresHora();
		System.out.println("Termina calculo de potencias de proporciones verdes por sector");

		/**
		 * Graba los resultados en un archivo en un subdirectori /salida del directorio
		 * especificado para los datos
		 */
		grabaResult();

		System.out.println("Termina cálculos e impresión");

	}

	/**
	 * Calcula las potencias por hora en el año elegido del sector nombreSector
	 * 
	 * @param nombreSector
	 * @return potHor potencias por hora del año
	 */
	public double[] calculaPotHorSector(String nombreSector) {
		EstimaCoefs ec = new EstimaCoefs();
		double[] enerMes = coefSectores.get(nombreSector).getEnerMesFacMWh();
		double[] enerDiaMes = ec.estimaEnerDiariaCert(enerMes, anioFac);
		double[] potHor = new double[cantHoras];
		int[] estacionDeMes = datGenSec.getDefEstacMeses();
		for (int ih = 1; ih <= cantHoras; ih++) {
			int horaDelDia = ih % 24;
			int diaAnio = (ih - 1) / 24 + 1; // dia del año empezando en 1
			int mes = Serie.devuelveMes(anioGen, diaAnio);
			int estac = estacionDeMes[mes - 1];
			double[] coefHor = coefSectores.get(nombreSector).getCoefHora()[estac - 1][0];
			potHor[ih - 1] = enerDiaMes[mes - 1] * coefHor[horaDelDia];
		}
		return potHor;
	}

	/**
	 * Calcula hora por hora la potencia de generación total de todos los recursos y
	 * de la demanda total en el año elegido
	 */
	public void calculaGenYDemTotal() {
		for (String nr : nombresRecursos) {
			double[][] aux = calculaPotRecurso(nr);
			potenciasRecursos.put(nr, aux);
		}
		double[][] aux = calculaPotRecurso(this.nombreDemanda);
		potenciasRecursos.put(this.nombreDemanda, aux);
	}

	/**
	 * Calcula la potencia de un recurso promedio y por crónica en cada hora del año
	 * elegido, tomando los valores de un archivo de salidas de simulación de paso
	 * diario
	 * 
	 * @param nomRec
	 * @return result primer índice recorre promedio y crónicas segundo índice
	 *         recorre horas
	 */
	public double[][] calculaPotRecurso(String nomRec) {
		System.out.println("Comienza cálculo de potencias de recurso " + nomRec);

		String dirArchivo = directorioCorrida + "/" + nomRec + "/potencias" + "_"
				+ nomRec.substring(nomRec.indexOf("_") + 1) + ".xlt";
		LectorArchivosSalidaSimulDiaria lecDiaria = new LectorArchivosSalidaSimulDiaria(dirArchivo, cantIntMuestreo,
				cantCron);
		lecDiaria.leePotenciasPorDiaYCronica();
		double[][] result = lecDiaria.calculaValCronHora(anioGen);
		return result;
	}

	/**
	 * Para cada crónica y hora del año elegido calcula la proporción limpia de la
	 * demanda local con los dos repartos y los carga en double[][]
	 * propVerdeGenProporcional double[][] propVerdeGenPrioridadLocal
	 * 
	 * ATENCION: En el CASO PROPORCIONAL la proporcion verde promediando en los
	 * escenarios no es la proporción para el escenario con el promedio de los
	 * despachos. Ejemplo sencillo: Sea Vi, Gi, Di la generación verde, la
	 * generación total y la demanda en el escenario i, i=1,2 Sea pi la proporción
	 * verde en el escenario i, i=1,2 Promedio de las proporciones es p1=V1/G1,
	 * p2=V2/G2, (p1+p2)/2 = (1/2)(V1/G1+V2/G2) La proporción con el promedio de los
	 * despachos es distinta: pm = [(V1+V2)/2]/[(G1+G2)/2] = (V1+V2)/(G1+G2)
	 * 
	 * En el CASO PRIORIDAD LOCAL la proporcion verde promediando en los escenarios
	 * no es la proporción para el escenario con el promedio de los despachos.
	 * porque la proporción verde no es lineal. p1=Min(V1/D1, 1) p2=Min(V2/D2, 1) La
	 * proporción con el promedio de los despachos es: pm=Min[(V1+V2)/(D1+D2), 1]
	 */
	public void calculaPropsVerdesGenHora() {
		double[][] genTotalCron = new double[cantCron + 1][cantHoras];
		double[][] genVerdeCron = new double[cantCron + 1][cantHoras];
		double[][] demanda = potenciasRecursos.get(nombreDemanda);
		for (String nr : nombresRecursos) {
			double[][] aux1R = potenciasRecursos.get(nr);
			for (int ic = 0; ic <= cantCron; ic++) {
				for (int ih = 0; ih < cantHoras; ih++) {
					genTotalCron[ic][ih] += aux1R[ic][ih];
					genVerdeCron[ic][ih] += aux1R[ic][ih] * propVerdeRecursos.get(nr);
					propVerdeGenProporcional[ic][ih] = genVerdeCron[ic][ih] / genTotalCron[ic][ih];
					propVerdeGenPrioridadLocal[ic][ih] = Math.min(genVerdeCron[ic][ih] / demanda[ic][ih], 1.0);
				}

			}
		}
	}

	/**
	 * Se debe cargar las estructuras que almacenan las proporciones de energía
	 * verde por sector.
	 * 
	 * EL PROMEDIO DE LAS PROPORCIONES EN LOS ESCENARIOS PARA UN SECTOR DE DEMANDA
	 * NO ES IGUAL A LA PROPORCION CON EL DESPACHO PROMEDIO. private
	 * Hashtable<String, double[]> propVerdeSecPrioLocal; private Hashtable<String,
	 * double[]> propVerdeSecProporcional; double[] un valor para cada crónica
	 * 
	 * private Hashtable<String, double[][]> propVerdeSecPrioLocalHora; private
	 * Hashtable<String, double[][]> propVerdeSecProporcionalHora; valor double[][]
	 * primer índice crónica promedio de las crónicas y en cada crónica por hora
	 * segundo índice hora
	 * 
	 * 
	 */
	public void calculaPropsVerdesSectoresHora() {
		for (String ns : nombresSectores) {
			double[] demSector = potenciasSectores.get(ns);
			double[] auxProp = new double[cantCron + 1];
			double[] auxPrio = new double[cantCron + 1];
			double promProp = 0;
			double promPrio = 0;
			double[] promPropHora = new double[cantHoras];
			double[] promPrioHora = new double[cantHoras];
			double[][] auxPropHora = new double[cantCron + 1][cantHoras];
			double[][] auxPrioHora = new double[cantCron + 1][cantHoras];
			for (int ic = 1; ic <= cantCron; ic++) {
				double prop = matrices.Oper.prodEscalarVectores(demSector, propVerdeGenProporcional[ic])
						/ matrices.Oper.sumaComponentesVector(demSector);
				auxProp[ic] = prop;
				auxProp[0] += prop / cantCron;
				double prio = matrices.Oper.prodEscalarVectores(demSector, propVerdeGenPrioridadLocal[ic])
						/ matrices.Oper.sumaComponentesVector(demSector);
				auxPrio[ic] = prio;
				auxPrio[0] += prio / cantCron;
				for (int ih = 0; ih < cantHoras; ih++) {
					auxPropHora[ic][ih] = propVerdeGenProporcional[ic][ih];
					auxPropHora[0][ih] += propVerdeGenProporcional[ic][ih] / cantCron;
					auxPrioHora[ic][ih] = propVerdeGenPrioridadLocal[ic][ih];
					auxPrioHora[0][ih] += propVerdeGenPrioridadLocal[ic][ih] / cantCron;
				}
			}
			propVerdeSecProporcional.put(ns, auxProp);
			propVerdeSecPrioLocal.put(ns, auxPrio);

			propVerdeSecProporcionalHora.put(ns, auxPropHora);
			propVerdeSecPrioLocalHora.put(ns, auxPrioHora);
		}

	}

	public void grabaResult() {
		String dirSalida = directorioDatos + "/salidas";
		if (!DirectoriosYArchivos.existeDirectorio(dirSalida))
			DirectoriosYArchivos.creaDirectorio(directorioDatos, "salidas");
		String archSalidaGen = dirSalida + "/resGenerales.xlt";
		String archSalidaDet = dirSalida + "/resDetallados.xlt";
		DirectoriosYArchivos.siExisteElimina(archSalidaGen);
		DirectoriosYArchivos.siExisteElimina(archSalidaDet);
		grabaResultGenerales(archSalidaGen);
		grabaResultDetallados(archSalidaDet);
		System.out.println("Terminó la impresión de resultados");
	}

	public void grabaResultGenerales(String arch) {

		StringBuilder sb = new StringBuilder("FRACCION DE ENERGIA VERDE POR SECTOR - CRITERIO PROPORCIONAL\n");
		sb.append("SECTOR\tPROMEDIO_ESCENARIOS\t");
		for (int iesc = 0; iesc < cantCron; iesc++) {
			sb.append("ESC-" + iesc + "\t");
		}
		sb.append("\n");
		for (String ns : nombresSectores) {
			sb.append(ns + "\t");
			sb.append(propVerdeSecProporcional.get(ns)[0] + "\t");
			for (int ic = 1; ic <= cantCron; ic++) {
				sb.append(propVerdeSecProporcional.get(ns)[ic] + "\t");
			}
			sb.append("\n");
		}

		sb.append("FRACCION DE ENERGIA VERDE POR SECTOR - CRITERIO PRIORIDAD VERDE PARA DEMANDA LOCAL\n");
		sb.append("SECTOR\tPROMEDIO_ESCENARIOS\t");
		for (int iesc = 0; iesc < cantCron; iesc++) {
			sb.append("ESC-" + iesc + "\t");
		}
		sb.append("\n");
		for (String ns : nombresSectores) {
			sb.append(ns + "\t");
			sb.append(propVerdeSecPrioLocal.get(ns)[0] + "\t");
			for (int ic = 1; ic <= cantCron; ic++) {
				sb.append(propVerdeSecPrioLocal.get(ns)[ic] + "\t");
			}
			sb.append("\n");
		}

		DirectoriosYArchivos.agregaTexto(arch, sb.toString());
	}

	public void grabaResultDetallados(String arch) {

	}

	public static void main(String[] args) {
		CalculadorCertificados calc = new CalculadorCertificados();
		calc.calculaParticipaciones();
	}

}
