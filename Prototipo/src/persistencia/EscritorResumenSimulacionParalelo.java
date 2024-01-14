/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorResumenSimulacionParalelo is part of MOP.
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import datatypesSalida.DatosCosMargSP;
import datatypesSalida.DatosCurvaOferta;
import datatypesSalida.DatosEPPResumen;
import datatypesSalida.DatosEPPResumenSP;
import datatypesSalida.DatosEPPUnEscenario;
import datatypesSalida.DatosParamSalida;
import datatypesSalida.DatosSalidaAtributosDetallados;
import datatypesTiempo.DatosNumpos;
import logica.CorridaHandler;
import parque.Acumulador;
import parque.Barra;
import parque.CicloCombinado;
import parque.ContratoEnergia;
import parque.Corrida;
import parque.Demanda;
import parque.Falla;
import parque.Generador;
import parque.GeneradorEolico;
import parque.GeneradorFotovoltaico;
import parque.GeneradorHidraulico;
import parque.GeneradorTermico;
import parque.Impacto;
import parque.ImpoExpo;
import parque.Participante;
import parque.Recurso;
import pizarron.PizarronRedis;
import simulacion.PostizacionPaso;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.ManejaObjetosEnDisco;
import utilitarios.ParOrdenador;
import utilitarios.UtilArrays;

public class EscritorResumenSimulacionParalelo {

	/**
	 * Genera archivos con los resultados resumidos
	 * 
	 * @param directorio   es el directorio donde se graban los archivos
	 * @param param        es un array de enteros que indica las salidas que se
	 *                     desean
	 * 
	 * 
	 *                     param es int[]; 0 indica que no se produce la salida, 1
	 *                     indica que sí.
	 * 
	 *                     nombre archivo param[0] ener_resumen_GWh energía anual
	 *                     promedio en los escenarios; filas recurso; columnas aóo
	 *                     param[1] ener_cron energía por año y escenario para todos
	 *                     los recursos: filas aóo,escenario; columnas recurso
	 *                     param[2] pot para recursos en particular, un archivo por
	 *                     poste, filas paso, columnas poste param[3] lista de
	 *                     enteros int[] con los indicadores de los recursos para
	 *                     los que se va a sacar el archivo de potencias por paso,
	 *                     escenario y poste
	 * 
	 *                     param[4] costo_resumen costo anual promedio en los
	 *                     escenarios; filas recurso; columnas aóo param[5]
	 *                     costo_cron costo por aóo y escenario para todos los
	 *                     recursos: filas (aóo,escenario); columnas recurso
	 *                     param[6] no se utiliza param[7] lista de enteros int[]
	 *                     con los indicadores de los recursos para los que se va a
	 *                     sacar el archivo de costo por paso, escenario y poste
	 *
	 *                     param[8] cosmar_resumen filas paso; columnas poste; (los
	 *                     promedios segón cantidad de horas = curva plana) param[9]
	 *                     cosmar_cron para barras en particular, un archivo por
	 *                     poste, filas paso, columnas crónicas param[10] lista de
	 *                     enterios int[] con los indices de las barras para los que
	 *                     se va a sacar los costos marginales detallados
	 * 
	 *                     param[11] Si es =1 genera un directorio cantMod, con un
	 *                     archivo de disponibilidades para cada recurso En esos
	 *                     archivos las filas son pasos y las columnas son
	 *                     escenarios (crónicas)
	 * 
	 *                     param[12] lista de enteros, uno por cada recurso, que
	 *                     indica con 1 si deben sacarse las salidas detalladas del
	 *                     recurso. param[13] Si es =1 genera el archivo de salidas
	 *                     detalladas por cada paso SalidaDetalladaSP param[14] Si
	 *                     es =1 genera el archivo de costo por paso y por crónica
	 * 
	 * 
	 * @param datosResumen es el DatosEPPResumen que contiene los resultados de la
	 *                     simulación
	 */

	// Estructuras creadas en Agosto 2020
	private ArrayList<String> listaRec; // lista con los nombres de los recursos

	public ArrayList<String> getListaTiposRec() {
		return listaTiposRec;
	}

	private ArrayList<String> listaTiposRec; // lista de los tipos de recursos
	private Hashtable<String, Integer> indiceDeRecurso; // dado el nombre del recurso devuelve el índice en las tablas
														// de [][][]
	private Hashtable<String, Participante> participanteDeRecurso; // dado el nombre del recurso devuelve el
																	// Participante asociado
	private Hashtable<Integer, Participante> participanteDeIndice; // dado el índice en datos devuelve el Participante
	private Hashtable<String, String> tipoDeRecurso; // dado el nombre del recurso devuelve el tipo (ej: "HID")

	private Hashtable<String, Integer> indiceBarraDeRecurso;

	/**
	 * Acumulan las energía y costos esperados por tipo de participante y paso
	 */
	private Hashtable<String, double[]> energiaDeTipoPorPasoGWh;
	private Hashtable<String, double[]> costoDeTipoPorPasoMUSD;
	private Hashtable<String, double[]> energiaDeTipoPorAnioGWh;
	private Hashtable<String, double[]> costoDeTipoPorAnioMUSD;

	/**
	 * COMIENZAN ATRIBUTOS PARA LAS SALIDAS GRAFICAS DE RODRIGO-BRUNO
	 */

	/**
	 * ClaveDatosAtributosDetallados contiene (nombre del recurso, nombre del
	 * atributo) Valor contiene los resultados del atributo para el recurso
	 */
	private Hashtable<ClaveDatosAtributosDetallados, DatosSalidaAtributosDetallados> salidaAtributosDetallados;

	/**
	 * ClaveDatosAtributosDetallados contiene (nombre del tipo de recurso, nombre
	 * del atributo) Valor contiene los resultados de la suma del atributo para el
	 * conjunto de los recurso del tipo Esta suma puede no tener sentido para
	 * algunos atributos pero se hace igual.
	 */
//	private Hashtable<ClaveDatosAtributosDetallados, DatosSalidaAtributosDetallados> salidaAtributosDetalladosPorTipo;

	/**
	 * Clave el nombre del atributo Valor el nombre de la unidad en la que se
	 * expresan en las salidas
	 */
	private Hashtable<String, String> nombresUnidadesAtributos;

	// Las energías y costos de los ContratoEnergia están en el caso general de
	// energías y costos

	/**
	 * FIN ATRIBUTOS PARA LAS SALIDAS GRAFICAS
	 */

	/**
	 * COMIENZAN ATRIBUTOS DE ACUMULADORES PARA LAS SALIDAS EN TEXTO
	 */

	// Un paso se carga al año de su instante inicial
	double[][] ener_resumen_GWh; // primer índice recurso, segundo índice año
	double[][] cos_resumen_MUSD; // primer índice recurso, segundo índice año
	double[][][] gradGestion_resumen; // gradiente de gestión en MUSD/MW, primer índice recurso, segundo año, tercero
										// combustible si corresponde
	double[][] ingmar_resumen_USD; // ingresos a costo marginal primer índice recurso, segundo índice
									// año
	// ojo que todavía no se está leyendo el tope spot sino que es una constante
	double[][] ingmar_resumen_tope_USD; // ingresos a costo marginal primer índice recurso, segundo índice
	// año
	double[] cos_tot_medio; // índice año
	double[][] ener_Paso_MWh; // primer índice recurso, segundo índice paso
	double[][][] ener_esc; // primer índice recurso, segundo índice año, tercer índice escenario
	double[][][] costo_esc; // primer índice recurso, segundo índice año, tercer índice escenario
	double[][][] ingmar_esc; // ingresos a costo marginal por escenario, índices recurso, año, escenario
	double[][] cosmar_prom_anio; // primer índice barra, segundo índice año
	double[][] spot_prom_anio; // primer índice barra, segundo índice año
	double[][][] cosmar_prom_anio_auxEsc; // primer índice barra, segundo índice año, tercer índice escenario
	double[][][] spot_prom_anio_auxEsc; // primer índice barra, segundo índice año, tercer índice escenario
	double[][][] cosmar_prom_paso; // primer índice barra, segundo índice paso, tercer índice poste
	double[][][] cosmar_prom_pasoEsc; // primer índice barra, segundo índice paso
	double[][][][] cosmar_cron; // índices barra, paso, escenario, poste
	double[][][][] cosmar_cron_im; // índices barra, paso, escenario, intervalo de muestreo
	double[][] cos_paso_cron; // indices paso, escenario
	int[][][] cant_mod_disp; // indices recurso, paso, escenario

	String archNumpos; // nombre del archivo donde se escribe el numpos de ser necesario

	String sufijo; // sufijo a agregar a los nombres de archivos

	/**
	 * Respectivamente Energía eléctrica generada por combustible en MWh de un
	 * escenario Volúmenes de combustible en unidades de combustible Costos
	 * variables por combustible, en el paso 
	 * Clave nombre generador térmico 
	 * Valor, primer índice año, segundo índice combustible
	 */
	private Hashtable<String, double[][]> energiasComb_resumen;
	private Hashtable<String, double[][]> volumenesComb_resumen;
	private Hashtable<String, double[][]> costosComb_resumen;

	/**
	 * Las mismas variables por escenario Clave nombre del generador térmico o ciclo
	 * combinado, Valor: primer indice año, segundo índice escenario, tercer índice
	 * combustible
	 */
	private Hashtable<String, double[][][]> enerComb_esc;
	private Hashtable<String, double[][][]> volComb_esc;
	private Hashtable<String, double[][][]> cosComb_esc;

	/**
	 * clave: nombre de una central hidráulica, valor: turbinado o vertido por año en
	 * hm3
	 */
	Hashtable<String, double[]> turbinadoAnualHm3;
	Hashtable<String, double[]> vertidoAnualHm3;
	Hashtable<String, double[]> enerSemGWh;

	/**
	 * clave: nombre de un ciclo combinado, valores: las energías generadas por TGs
	 * CVs por turbinas en ciclo abierto por turbinas combinadas incluso su parte de
	 * ciclos de vapor
	 */
	private Hashtable<String, double[]> energiaTGPorAnioGWh;
	private Hashtable<String, double[]> energiaCVPorAnioGWh;
	private Hashtable<String, double[]> energiaAbPorAnioGWh;
	private Hashtable<String, double[]> energiaCombPorAnioGWh;
	
	/**
	 * clave: nombre de un contrato interrumpible
	 * valores: los ingresos por ventas, la energía no suministrada y las multas por año en cada escenario
	 *       primer índice año, segundo índice escenario 
	 */
	private Hashtable<String, double[][]> ingVentasCIPorAnioEscMUSD;
	private Hashtable<String, double[][]> enerNoEntregadaGWh;
	private Hashtable<String, double[][]> multaCIPorAnioEscMUSD;
	

	/**
	 * clave: nombre del recurso Falla 
	 * valor: horas de falla, primer índice año, segundo
	 * índice escenario
	 */
	Hashtable<String, double[][]> horasFalla;

	/**
	 * Estructura que guarda las horas de falla de cada tipo, por año y escenario.
	 * El tipo es la combinación de fallas que hay presentes, ejemplo falla 1 y 4,
	 * pero no hay falla 2 y 3.
	 *
	 * -clave nombre de la demanda "-" nombre de la falla "-" String con los número
	 * de escalón en orden separados por guión - ejemplo
	 * demTotal-fallaDemTotal-1-2-4 quiere decir que hubo en el paso de tiempo falla
	 * 1, falla2 y falla 4 en la fallaDemaTotal de la demanda demTotal Esa clave se
	 * construye con el método creaClaveTipoFalla de DatosEPPUnEscenario
	 * 
	 * -en el int[] primer índice año, segundo índice escenario
	 */
	private Hashtable<String, double[][]> horasFallaPorTipo;
	
	
	/**
	 * ATRIBUTOS PARA EL CALCULO DE ARRANQUES
	 *
	 * Acumulador de arranques por escenario de térmicos y ciclos combinados según tiempo de parada previo.
	 * 
	 * Para los participantes térmicos en cada paso se computa el cociente de módulos arrancados dividido módulos instalados.
	 * Para los ciclos combinados se toma el promedio de los arranques como turbina combinada, del 
	 * conjunto de las turbinas a gas instaladas del ciclo.
	 * En la primera semana de un escenario no se cuenta el eventual arranque respecto al estado previo.
	 * Se publican los acumulados al fin de cada año.
	 * 
	 * Acumula las cantidades de arranques para cada tiempo de parada.
	 * Clave: nombre del recurso, térmico o ciclo combinado. Para el ciclo combinado hay dos entradas: 
	 * nombreCC + "-" + "TG" y nombreCC "-" + "CV", para las turbinas de gas y los ciclos de vapor respectivamente
	 * 
	 * ATENCIÓN QUE ESTOS SUFIJOS SON DIFERENTES DE LOS EMPLEADOS EN DatosCicloCombinadoSP y DatosEPPUnEscenario.
	 * PORQUE REPRESENTAN COSAS DISTINTAS
	 * 
	 * No se tiene en cuenta la posibilidad de arrancar como abierta y pasar a combinada. 
	 *  
	 * Clave: nombre del recurso + "-"  + escenario
	 * Valor: un array con la cantidad acumulada de arranques en el escenario para distintos valores de cantidad de días
	 * entre la parada y al arranque: 1, 2, 3,........, cantDiasMaxArranque o más días.
	 */
	private Hashtable<String, double[]> cantArranquesPorEsc;
	
	private static final int CANT_DIAS_MAX_ARRANQUE = 10; // CANTIDAD MÁXIMA DE DÍAS EN LA CONTABILIDAD DE DURACIONES DE ARRANQUES
	private static final int CANT_MAX_MOD = 10; // CANTIDAD MÁXIMA DE MÓDULOS QUE PUEDE TENER UN TÉRMICO O UN CC
	
	/**
	 * Ultimo día de funcionamiento de n módulos cuando se recorre un escenario,
	 * contado a partir del inicio de la corrida; el primer día de la corrida es el cero
	 * Clave: nombre del recurso, térmico o ciclo combinado. Si es ciclo combinado lleva sufijo "-TG" o "-CV".
	 * Valor:último día de la corrida de funcionamiento del recurso, siendo el primer día de la corrida el cero, para
	 * cada uno de n valores n=1,..CantModInst.
	 * Ejemplo: El vectos 6, 4, 1, quiere decir el último día que funcionaron 3 módulos fue el día 1,
	 * el último día que funcionaron 2 fue el día 4, el último día que funcionó 1 módulo fue el día 6.  
	 */
	private Hashtable<String, int[]> ultimoDiaFunc;
	
	// Para cada recurso la cantidad de módulos despachadas el último intervalo de muestreo del paso anterior
	private Hashtable<String, Integer> cantModDespUltimoIMPasoAnterior;  
	
//	/**
//	 * Cantidad de módulos que funcionaron n módulos en el último intervalo de muestreo del último día de funcionamiento,
//	 * con la misma clave
//	 */
//	private Hashtable<String, Integer[]> cantFuncUltimoIM;
	
	/**
	 * FIN DE ATRIBUTOS PARA EL CALCULO DE ARRANQUES
	 */
	 

	/**
	 * FIN ATRIBUTOS DE ACUMULADORES PARA LAS SALIDAS EN TEXTO
	 */

	DatosEPPResumen datosResumen;
	int cantRec;
	int cantAnios;
	int cantEsc;
	int cantBarras;
	int cantPasos;
	int[][] param;
	int anioIni;
	int[] durAnios; // duración en segundos de cada año en el resumen

	long[] instInicAnios;
	String directorio; // el directorio general de salida
	ArrayList<Barra> barras;
	LineaTiempo lt;

	/**
	 * datosDetallados guarda los valores de los datos detallados de los recursos
	 * que se seleccionaron. En el ArrayList el índice es el escenario En el
	 * Hashtable: clave es el tipo_nombre del recurso valor es un double[][][]
	 * primer índice paso, segundo índice atributo con datos detallados, tercer
	 * índice ordinal dentro de los valores del atributo en el paso, típicamente
	 * recorre los postes de un paso
	 */
	ArrayList<Hashtable<String, double[][][]>> datosDetalladosTodos;

	private int indiceEscenario = 1; // Recordar que los escenarios van de 1 en adelante

	private static int CANT_LINEAS_CABEZAL = 5;

	ArrayList<StringBuilder> resUnico; // Texto que junta los textos de archivos de salida resumen.xlt resuTer.xlt y
										// gradientes.xlt

	private String rutaSalidas;
	
	private String dirSerial; // directorio donde van las salidas serializadas de la simulación

	/**
	 * @param paralelo     true si se está en procesamiento paralelo y los
	 *                     resultados de los escenarios no se han cargado en el
	 *                     DatosEPPResumen datosResumen sino que se serializaron
	 * @param directorio   directorio donde se imprimen las salidas
	 * @param dirResSim    directorio desde donde se obtienen los resultados de la
	 *                     simulación
	 * @param dtParam
	 * @param datosResumen NO CONTENDRA LOS RESULTADOS DE LA SIMULACIóN SINO SOLO
	 *                     LOS DATOS GENERALES
	 */
	public void escribeResumenesSimulacion(boolean paralelo, String directorioArg, String dirResSim,
			DatosParamSalida dtParam, DatosEPPResumen datosResumenArg) {
		this.rutaSalidas = directorioArg;
		directorio = directorioArg;
		datosResumen = datosResumenArg;
		param = dtParam.getParam();
		lt = datosResumen.getCorrida().getLineaTiempo();
		cantPasos = lt.getCantidadPasos();
		listaRec = datosResumen.getListaRecursos();
		listaTiposRec = datosResumen.getListaTiposRec();

		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();
		sufijo = "_" + corrida.getNombre().substring(0, 3) + "_" + corrida.getFechaEjecucion() + "_"
				+ corrida.getHoraEjecucion();

		indiceDeRecurso = datosResumen.getIndiceDeRecurso();
		participanteDeRecurso = datosResumen.getParticipanteDeRecurso();
		participanteDeIndice = datosResumen.getParticipanteDeIndice();
		tipoDeRecurso = datosResumen.getTipoDeRecurso();
		indiceBarraDeRecurso = datosResumen.getIndiceBarraDeRecurso();

		boolean residual = (datosResumen.getCorrida().getCompDemanda()
				.equalsIgnoreCase(utilitarios.Constantes.DEMRESIDUAL));
		archNumpos = directorio + "/numpos.xlt"; // en este archivo se imprime numpos si la demanda es residual

		barras = datosResumen.getCorrida().getRed().getBarrasActivas();

		/**
		 * listaRec tiene los String tipo_nombre del recurso ordenados alfabéticamente
		 * 
		 * En datosResumen.getIndiceRecurso() Dada la clave String tipo_nombre del
		 * recurso, devuelve el �ndice en los arrays potencias, costos, etc. del
		 * tipo_nombre asociado El tipo_nombre se forma concatenando el tipo y el nombre
		 * del recurso.
		 */

		instInicAnios = lt.getInstInicioAnio();
		anioIni = lt.getAnioInic();
		cantRec = datosResumen.getCantRec();
		cantBarras = datosResumen.getCorrida().getRed().getBarrasActivas().size();
		cantEsc = CorridaHandler.getInstance().getCorridaActual().getCantEscenarios();
		cantAnios = lt.getInstInicioAnio().length - 1; // OJOJOJO no se si va el menos 1
		
		creaEstructuras();

		datosDetalladosTodos = new ArrayList<Hashtable<String, double[][][]>>();
		for (int iesc = 0; iesc < cantEsc; iesc++) {
			datosDetalladosTodos.add(null);
		}

		// RECORRE LOS RESULTADOS Y CARGA ESTRUCTURAS PARA SALIDAS
		
		DatosEPPUnEscenario d1esc = devuelveResUnEscenario(datosResumen, dirResSim, paralelo);
		while (d1esc != null) {

			// El poste de cada intervalo de muestreo del escenario, abarcando todos los
			// pasos
			ArrayList<Integer> numpos1Esc = new ArrayList<Integer>();

			durAnios = new int[cantAnios];
			int iesc = d1esc.getNumeroEsc() - 1;
			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("Procesa salidas escenario= " + d1esc.getNumeroEsc());
			lt.reiniciar();
			PasoTiempo pt = lt.devuelvePasoActual();
			long instInicAnio = lt.getInicial();
			int anioCorr = anioIni;
			int indAnio = anioCorr - anioIni;
			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("indAnio= " + indAnio);
			long instFin = 0;

			while (pt != null) {

				int durPaso = pt.getDuracionPaso();
				int numPaso = lt.getNumPaso();

				int[] numpos = UtilArrays.dameArrayI(d1esc.getNumposPasos().get(numPaso));

				//
				numpos1Esc.addAll(d1esc.getNumposPasos().get(numPaso));

				long instInicPaso = pt.getInstanteInicial();
				long instFinPaso = instInicPaso + durPaso;
				if (instInicPaso >= instInicAnios[indAnio + 1])
					indAnio++;
				anioCorr++;
				long instFinAnio = instInicAnios[indAnio + 1];

				/**
				 * propAnio y propAnioMasUno son las proporciones de tiempo del paso en el anio
				 * indAnio e indAnio +1 respectivamente. Las energías y costos de un paso a
				 * caballo entre dos años se reparten en esas proporciones.
				 */
				double propAnio = 1.0;
				double propAnioMas1 = 0.0;
				boolean pasoPartido = false;
				if (instFinPaso > instFinAnio) {
					propAnio = (instFinAnio - instInicPaso) / durPaso;
					propAnioMas1 = 1 - propAnio;
					pasoPartido = true;

				}

				durAnios[indAnio] += durPaso * propAnio;
				if (pasoPartido)
					durAnios[indAnio] += durPaso * propAnioMas1;

				// TODO
				if (Constantes.NIVEL_CONSOLA > 1)
					System.out.println(numPaso);

				int cantPostes = pt.getBloque().getCantPostes();
				double[] auxCosmar = new double[cantPostes];
				if (Constantes.NIVEL_CONSOLA > 1)
					System.out.println("Procesa salidas indAnio= " + indAnio + " ---- numpaso = " + numPaso);

				int[] durPos = d1esc.getDurPos()[numPaso];
				int indRecEnLista = 0;

				// Carga potencias, energías e ingresos al marginal y horas de falla
				for (String nombreRec : listaRec) {

					int indiceRec = datosResumen.getIndiceDeRecurso().get(nombreRec);

					ener_Paso_MWh[indRecEnLista][numPaso] += d1esc.getEnergias()[indiceRec][numPaso] / cantEsc;
					String tipo = tipoDeRecurso.get(nombreRec);
					double signo = 1.0;
					if (tipo == utilitarios.Constantes.IMPOEXPO) {
						ImpoExpo ie = (ImpoExpo) participanteDeIndice.get(indiceRec);
						if (ie.getOperacionCompraVenta().equalsIgnoreCase(utilitarios.Constantes.VENTA))
							signo = -1.0;
					}

					energiaDeTipoPorPasoGWh.get(tipo)[numPaso] += signo * d1esc.getEnergias()[indiceRec][numPaso]
							/ (cantEsc * Constantes.MWHXGWH);
					// ATENCION: el costo ya viene negativo, por eso no se cambia de signo
					costoDeTipoPorPasoMUSD.get(tipo)[numPaso] += d1esc.getCostos()[indiceRec][numPaso]
							/ (cantEsc * Constantes.USDXMUSD);

					cos_paso_cron[numPaso][iesc] += d1esc.getCostos()[indiceRec][numPaso] / Constantes.USDXkUSD;
					indRecEnLista++;
					if (tipo.equalsIgnoreCase(utilitarios.Constantes.TER)
							|| tipo.equalsIgnoreCase(utilitarios.Constantes.CC))
						calculaArranquesPaso(nombreRec, indiceRec, d1esc, numpos, pt);
				}

				// Carga costos marginales
				int indBarra = 0;
				for (Barra b : barras) {
					for (int ip = 0; ip < cantPostes; ip++) {
						// Se suman costos por duraciones, al final de todo hay que dividir entre la
						// duración del aóo
						// cosmar_prom_anio[indBarra][indAnio] +=
						// d1esc.getCostosMarg()[indBarra][numPaso][ip]*durPos[ip]/cantEsc;
						cosmar_prom_anio_auxEsc[indBarra][indAnio][iesc] += d1esc.getCostosMarg()[indBarra][numPaso][ip]
								* durPos[ip] / cantEsc;
						spot_prom_anio_auxEsc[indBarra][indAnio][iesc] += Math
								.min(d1esc.getCostosMarg()[indBarra][numPaso][ip], utilitarios.Constantes.TOPE_SPOT)
								* durPos[ip] / cantEsc;
						// Se suman costos
						auxCosmar[ip] += d1esc.getCostosMarg()[indBarra][numPaso][ip];
					}
					double v = 1.0 / cantEsc;
					cosmar_prom_pasoEsc[indBarra][numPaso] = UtilArrays.prodNumero(auxCosmar, v);
					cosmar_cron[indBarra][numPaso][iesc] = d1esc.getCostosMarg()[indBarra][numPaso];
					cosmar_cron_im[indBarra][numPaso][iesc] = antiPostiza(auxCosmar, numpos);
					indBarra++;
				}

				/**
				 * Carga los datos del paso que se reparten entre años en caso de ser necesario
				 * con el método cargaDatosDeUnPaso TODO DEBERÍA INCLUIRSE EN ESE MÉTODO TAMBIEN
				 * LOS COSTOS MARGINALES MEDIOS POR AÑO
				 */
				int ia = indAnio;
				double prop = propAnio;
				cargaDatosDeUnPaso(numPaso, pt, ia, prop, d1esc);
				if (pasoPartido) {
					ia = indAnio + 1;
					prop = propAnioMas1;
					cargaDatosDeUnPaso(numPaso, pt, ia, prop, d1esc);
				}

				// Carga cantidad de módulos disponibles
				for (String nombreRec : listaRec) {
					int indRec = datosResumen.getIndiceDeRecurso().get(nombreRec);
					cant_mod_disp[indRec][numPaso][iesc] = d1esc.getCantModDisp()[indRec][numPaso];
				}

				// Carga atributos detallados

				datosDetalladosTodos.set(iesc, d1esc.getDatosDetallados());

				// Si termina el año calcula promedios por año de costos marginales
				// y se aumenta indAnio
				lt.avanzarPaso();
				pt = lt.devuelvePasoActual();

				if (pt != null) {
					long instInic = pt.getInstanteInicial();
					instFin = instInic + pt.getDuracionPaso();
					if (instInic >= instInicAnios[indAnio + 1]) {
						// el nuevo paso se inicia despuós del inicio del próximo aóo
						// se calcula el promedio del costo marginal
						for (indBarra = 0; indBarra < cantBarras; indBarra++) {
							int durAnioQueTermina = (int) (instInicAnios[indAnio + 1] - instInicAnio);
							// NO ESTA ACUMULANDO EN LOS ESCENARIOS
							// cosmar_prom_anio[indBarra][indAnio] =
							// cosmar_prom_anio[indBarra][indAnio]/durAnioQueTermina ;
							cosmar_prom_anio_auxEsc[indBarra][indAnio][iesc] = cosmar_prom_anio_auxEsc[indBarra][indAnio][iesc]
									/ durAnioQueTermina;
							spot_prom_anio_auxEsc[indBarra][indAnio][iesc] = spot_prom_anio_auxEsc[indBarra][indAnio][iesc]
									/ durAnioQueTermina;
						}
						// se aumenta indAnio
						indAnio++;
						instInicAnio = instInicAnios[indAnio];
						if (Constantes.NIVEL_CONSOLA > 1)
							System.out.println("Instante inicial nuevo aóo=" + instInicAnios[indAnio]);
					}
				} else {
					// se terminó el escenario
					for (indBarra = 0; indBarra < cantBarras; indBarra++) {
						int durAnioQueTermina = (int) (instFin - instInicAnio);
						// cosmar_prom_anio[indBarra][indAnio] =
						// cosmar_prom_anio[indBarra][indAnio]/durAnioQueTermina ;
						cosmar_prom_anio_auxEsc[indBarra][indAnio][iesc] = cosmar_prom_anio_auxEsc[indBarra][indAnio][iesc]
								/ durAnioQueTermina;
						spot_prom_anio_auxEsc[indBarra][indAnio][iesc] = spot_prom_anio_auxEsc[indBarra][indAnio][iesc]
								/ durAnioQueTermina;
					}

				}
				// Costo marginal medio por Paso
				for (indBarra = 0; indBarra < cantBarras; indBarra++) {
					if (iesc == 0)
						cosmar_prom_paso[indBarra][numPaso] = new double[cantPostes];
					for (int ip = 0; ip < cantPostes; ip++) {
						cosmar_prom_paso[indBarra][numPaso][ip] += cosmar_prom_pasoEsc[indBarra][numPaso][ip];
					}
				}
			}

			// Calcula el costo marginal promedio por año
			for (int indBarra = 0; indBarra < cantBarras; indBarra++) {
				for (int iAnio = 0; iAnio < cantAnios; iAnio++) {
					cosmar_prom_anio[indBarra][iAnio] += cosmar_prom_anio_auxEsc[indBarra][iAnio][iesc];
					spot_prom_anio[indBarra][iAnio] += spot_prom_anio_auxEsc[indBarra][iAnio][iesc];
				}
			}

			// Escribe los postes de cada intervalo de muestreo del escenario
			if (param[Constantes.PARAMSAL_COSMAR_RESUMEN][0] == 1 && residual) {
				Corrida c = datosResumen.getCorrida();
				String arch = directorio + "/numpos" + sufijo + ".txt";
				imprimeLineaNumpos(c, arch, iesc, numpos1Esc);
			}
			d1esc = null;
			d1esc = devuelveResUnEscenario(datosResumen, dirResSim, paralelo);
		}

		// TERMINÓ LA RECORRIDA DE LOS PASOS DE TIEMPO PARA CARGAR LOS DATOS

		calculaCostoTotalAnualPromedio();

		Corrida c = datosResumen.getCorrida();
		String dirArch = directorio + "/modYPot" + sufijo + ".xlt";
		imprimeModulosYPotencias(c, dirArch);

		dirArch = directorio + "/resumen" + sufijo + ".xlt";
		imprimeResumen(dirArch);

		dirArch = directorio + "/resuTer" + sufijo + ".xlt";
		imprimeResuTerm(dirArch);

		dirArch = directorio + "/resuCC" + sufijo + ".xlt";
		imprimeResuCC(dirArch);

		dirArch = directorio + "/resumIng" + sufijo + ".xlt";
		imprimeResumIng(dirArch);

		dirArch = directorio + "/gradientes" + sufijo + ".xlt";
		imprimeGradientes(dirArch);

		dirArch = directorio + "/enercron" + sufijo + ".xlt";
		imprimeEnercron(dirArch);

		dirArch = directorio + "/horasFalla" + sufijo + ".xlt";
		imprimeHorasFallaYLOLE(dirArch);

		dirArch = directorio + "/costocron" + sufijo + ".xlt";
		imprimeCostocron(dirArch);

		dirArch = directorio + "/costoPasoCron" + sufijo + ".xlt";
		imprimeCostoPasoCron(dirArch);

		imprimeCombCron(directorio);

		String dirCosmar = directorio + "/cosmar";
		imprimeCostosMarginales(dirCosmar);

		imprimeCostosMarginalesIntMuestreo(dirCosmar);

		String dirCantModDisp = directorio + "/cantModDisp";
		imprimeCantModDisp(dirCantModDisp);

//		String dirArranques = directorio + "/arranques" + sufijo + ".xlt";
//		imprimeArranques(dirArranques);

		dirArch = directorio + "/resUnico" + sufijo + ".xlt";
		imprimeResUnico(dirArch);

//		imprimeDatosDetallados();
		
		boolean unPasoPorFila = true;
		imprimeDatosDetallados2(unPasoPorFila);
		
		calculaAtributosPercentilesYMedia();

		dirArch = directorio + "/enerPaso" + sufijo + ".xlt";
		imprimeEnerPaso(dirArch);
		
		serializaDatosResumenYCMg(directorio);

		System.out.println("Terminó la escritura de salidas");

		// pruebaSalidaAtributosDetallados(); // se usa solo durante las pruebas
	}

	/**
	 * Serializa las estructuras que permiten private ArrayList<String> listaRec; //
	 * lista con los nombres de los recursos private ArrayList<String>
	 * listaTiposRec; // lista de los tipos de recursos private Hashtable<String,
	 * Integer> indiceDeRecurso; // dado el nombre del recurso devuelve el índice en
	 * las tablas // de [][][] private Hashtable<String, Participante>
	 * participanteDeRecurso; // dado el nombre del recurso devuelve el //
	 * Participante asociado private Hashtable<Integer, Participante>
	 * participanteDeIndice; // dado el índice en datos devuelve el Participante
	 * private Hashtable<String, String> tipoDeRecurso; // dado el nombre del
	 * recurso devuelve el tipo (ej: "HID") private Hashtable<String, Integer>
	 * indiceBarraDeRecurso;
	 * 
	 * @param dirSalidas es el directorio de salidas de la corrida. Los serializados
	 *                   se almacenan en el subdirectorio
	 * 
	 */
	public void serializaDatosResumenYCMg(String dirSalidas) {
		String dirSerial = dirSalidas + "/" + utilitarios.Constantes.DIR_SERIALIZADOS;
		DatosCosMargSP dcmg = new DatosCosMargSP(cosmar_cron);
		DatosEPPResumenSP drsp = new DatosEPPResumenSP(datosResumen);
		try {
			ManejaObjetosEnDisco.guardarEnDisco(dirSerial, "datosResumenSP", drsp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ManejaObjetosEnDisco.guardarEnDisco(dirSerial, "cosmar_cron", dcmg);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Levanta a memoria el DatosEPPResumen serializado en el método
	 * serializaDatosResumenYCMg
	 * 
	 * @param dirSalidas el directorio general de salidas de la simulación de la
	 *                   corrida
	 */
	public static DatosEPPResumenSP levantaDatosResumen(String dirSalidas) {
		String dirSerial = dirSalidas + "/" + utilitarios.Constantes.DIR_SERIALIZADOS;
		DatosEPPResumenSP datosResumenSP = (DatosEPPResumenSP) ManejaObjetosEnDisco.traerDeDisco(dirSerial,
				"datosResumenSP");
		DatosCosMargSP dcmg = (DatosCosMargSP) ManejaObjetosEnDisco.traerDeDisco(dirSerial, "cosmar_cron");
		return datosResumenSP;
	}

	/**
	 * Levanta a memoria el DatosCosMargSP serializado en el método
	 * serializaDatosResumenYCMg
	 * 
	 * @param dirSalidas el directorio general de salidas de la simulación de la
	 *                   corrida
	 */
	public static DatosCosMargSP levantaDatosCosMarg(String dirSalidas) {
		String dirSerial = dirSalidas + "/" + utilitarios.Constantes.DIR_SERIALIZADOS;
		DatosCosMargSP dcmg = (DatosCosMargSP) ManejaObjetosEnDisco.traerDeDisco(dirSerial, "cosmar_cron");
		return dcmg;
	}

	/**
	 * Carga los resultados del PasoTiempo pt en los acumuladores anuales del año
	 * indAnio en la proporción prop. Cuando un paso está a caballo entre dos años
	 * se invoca dos veces para el mismo paso
	 * 
	 * @param indAnio Indice del año en el que se cargan los valores del paso
	 * @param prop    Proporción de los valores del año que se cargan al año indAnio
	 * @param d1esc
	 * @param numPaso
	 */
	public void cargaDatosDeUnPaso(int numPaso, PasoTiempo pt, int indAnio, double prop, DatosEPPUnEscenario d1esc) {
		// Carga potencias, energías e ingresos, y horas de falla, del paso del año
		// siguiente.
		// GRADIENTE
		int indRecEnLista = 0;
		int cantPostes = pt.getBloque().getCantPostes();

		int[] durPos = d1esc.getDurPos()[numPaso];
		int iesc = d1esc.getNumeroEsc() - 1;

		for (String nombreRec : listaRec) {

			int indiceRec = datosResumen.getIndiceDeRecurso().get(nombreRec);

			String tipo = tipoDeRecurso.get(nombreRec);
			double signo = 1.0;
			if (tipo == utilitarios.Constantes.IMPOEXPO) {
				ImpoExpo ie = (ImpoExpo) participanteDeIndice.get(indiceRec);
				if (ie.getOperacionCompraVenta().equalsIgnoreCase(utilitarios.Constantes.VENTA))
					signo = -1.0;
			}
			energiaDeTipoPorAnioGWh.get(tipo)[indAnio] += signo * d1esc.getEnergias()[indiceRec][numPaso] * prop
					/ (cantEsc * Constantes.MWHXGWH);
			costoDeTipoPorAnioMUSD.get(tipo)[indAnio] += d1esc.getCostos()[indiceRec][numPaso] * prop
					/ (cantEsc * Constantes.USDXMUSD);

			ener_resumen_GWh[indRecEnLista][indAnio] += d1esc.getEnergias()[indiceRec][numPaso] * prop
					/ (cantEsc * Constantes.MWHXGWH);
			cos_resumen_MUSD[indRecEnLista][indAnio] += d1esc.getCostos()[indiceRec][numPaso] * prop
					/ (cantEsc * Constantes.USDXMUSD); // USD Pasa a MUSD

			// Ingresos al marginal
			int indBarra = indiceBarraDeRecurso.get(nombreRec);
			if (indBarra >= 0) {
				if (datosResumen.getCorrida().getRed().isRedUninodal(pt.getInstanteInicial()))
					indBarra = 0;
				for (int ip = 0; ip < cantPostes; ip++) {
					// ingresos a costo marginal con y sin tope spot primer índice recurso, segundo
					// índice
					// año
					ingmar_resumen_USD[indRecEnLista][indAnio] += d1esc.getPotencias()[indiceRec][numPaso][ip]
							* d1esc.getCostosMarg()[indBarra][numPaso][ip] * durPos[ip] * prop
							/ (cantEsc * Constantes.SEGUNDOSXHORA);
					ingmar_resumen_tope_USD[indRecEnLista][indAnio] += d1esc.getPotencias()[indiceRec][numPaso][ip]
							* Math.min(d1esc.getCostosMarg()[indBarra][numPaso][ip], utilitarios.Constantes.TOPE_SPOT)
							* durPos[ip] * prop / (cantEsc * Constantes.SEGUNDOSXHORA);
					// Se suman costos
				}
			}

			ener_esc[indRecEnLista][indAnio][iesc] += d1esc.getEnergias()[indiceRec][numPaso] * prop
					/ Constantes.MWHXGWH;
			costo_esc[indRecEnLista][indAnio][iesc] += d1esc.getCostos()[indiceRec][numPaso] * prop
					/ Constantes.USDXkUSD;

			if (tipoDeRecurso.get(nombreRec).equals(utilitarios.Constantes.HID)) {
				turbinadoAnualHm3.get(nombreRec)[indAnio] += prop * d1esc.getTurhm3().get(nombreRec)[numPaso] / cantEsc;
				vertidoAnualHm3.get(nombreRec)[indAnio] += prop * d1esc.getVerhm3().get(nombreRec)[numPaso] / cantEsc;
			}

			if (tipoDeRecurso.get(nombreRec).equals(utilitarios.Constantes.CC)) {
				energiaTGPorAnioGWh.get(nombreRec)[indAnio] += prop
						* d1esc.getEnergiaTGPorPasoGWh().get(nombreRec)[numPaso] / cantEsc;
				energiaCVPorAnioGWh.get(nombreRec)[indAnio] += prop
						* d1esc.getEnergiaCVPorPasoGWh().get(nombreRec)[numPaso] / cantEsc;
				energiaAbPorAnioGWh.get(nombreRec)[indAnio] += prop
						* d1esc.getEnergiaAbPorPasoGWh().get(nombreRec)[numPaso] / cantEsc;
				energiaCombPorAnioGWh.get(nombreRec)[indAnio] += prop
						* d1esc.getEnergiaCombPorPasoGWh().get(nombreRec)[numPaso] / cantEsc;
			}

			// Gradiente de gestión

			if (d1esc.getGradGestion()[indiceRec][indAnio] != null) {
				int cantGrad = d1esc.getGradGestion()[indiceRec][numPaso].length;
				if (gradGestion_resumen[indRecEnLista][indAnio] == null) {
					gradGestion_resumen[indRecEnLista][indAnio] = new double[cantGrad];
				}
				for (int ig = 0; ig < cantGrad; ig++) {
					// pasa el gradiente a USD/kW instalado
					gradGestion_resumen[indRecEnLista][indAnio][ig] += d1esc.getGradGestion()[indiceRec][numPaso][ig]
							* prop / (cantEsc * Constantes.KWXMW);
				}
			}

			// carga estructuras para salidas de uso de combustibles
			Participante p = participanteDeIndice.get(indRecEnLista);
			String nomt = "";
			if (p instanceof GeneradorTermico || p instanceof CicloCombinado) {
				if (p instanceof GeneradorTermico) {
					GeneradorTermico gt = (GeneradorTermico) p;
					nomt = gt.getNombre();
				} else if (p instanceof CicloCombinado) {
					CicloCombinado gt = (CicloCombinado) p;
					nomt = gt.getNombre();
				}
				double[] aux = d1esc.getEnergiasComb().get(nomt)[numPaso];
				int cantC = aux.length;
				for (int ic = 0; ic < cantC; ic++) {
					energiasComb_resumen.get(nomt)[indAnio][ic] += d1esc.getEnergiasComb().get(nomt)[numPaso][ic] * prop
							/ cantEsc;
					enerComb_esc.get(nomt)[indAnio][iesc][ic] += d1esc.getEnergiasComb().get(nomt)[numPaso][ic] * prop;

				}
				aux = d1esc.getCostosComb().get(nomt)[numPaso];
				for (int ic = 0; ic < cantC; ic++) {
					costosComb_resumen.get(nomt)[indAnio][ic] += d1esc.getCostosComb().get(nomt)[numPaso][ic] * prop
							/ cantEsc;
					cosComb_esc.get(nomt)[indAnio][iesc][ic] += d1esc.getCostosComb().get(nomt)[numPaso][ic] * prop;
				}
				aux = d1esc.getVolumenesComb().get(nomt)[numPaso];
				for (int ic = 0; ic < cantC; ic++) {
					volumenesComb_resumen.get(nomt)[indAnio][ic] += d1esc.getVolumenesComb().get(nomt)[numPaso][ic]
							* prop / cantEsc;
					volComb_esc.get(nomt)[indAnio][iesc][ic] += d1esc.getVolumenesComb().get(nomt)[numPaso][ic] * prop;

				}
			}
			
			// Carga acumuladores de contratos interrumpibles
			if (tipoDeRecurso.get(nombreRec).equals(utilitarios.Constantes.CONTRATOINTERRUMPIBLE)) {
				String nom = p.getNombre();
				ingVentasCIPorAnioEscMUSD.get(nom)[indAnio][iesc] += d1esc.getIngVentasCIMUSD().get(nom)[numPaso];
				multaCIPorAnioEscMUSD.get(nom)[indAnio][iesc] += d1esc.getMultaCIMUSD().get(nom)[numPaso];	
				enerNoEntregadaGWh.get(nom)[indAnio][iesc] += d1esc.getEnerNoEntGWh().get(nom)[numPaso];
								
			}

			// Carga acumuladores de horas de falla
			if (p instanceof Falla) {
				double[][] falla = horasFalla.get(nombreRec);
				for (int ip = 0; ip < cantPostes; ip++) {
					if (d1esc.getPotencias()[indiceRec][numPaso][ip] > 0) {
						falla[indAnio][iesc] += durPos[ip] * prop / utilitarios.Constantes.SEGUNDOSXHORA;
					}
				}
			}
			

			indRecEnLista++;
		}

		Hashtable<String, int[]> hfpaso = d1esc.getHorasFallaPorTipo();
		for (String clave : hfpaso.keySet()) {
			if (horasFallaPorTipo.containsKey(clave)) {
				// ya está registrado el tipo de falla
				horasFallaPorTipo.get(clave)[indAnio][iesc] += hfpaso.get(clave)[numPaso] * prop;
			} else {
				// Se crea el double[][] por año y escenario de un tipo de falla no registrado
				// todavía
				double[][] auxd = new double[cantAnios][cantEsc];
				auxd[indAnio][iesc] = hfpaso.get(clave)[numPaso] * prop;
				horasFallaPorTipo.put(clave, auxd);

			}
		}

	}
	

	
	

	/**
	 * Escribe el cabezal si el escenario iesc es cero y escribe una línea con los
	 * numpos de todos los intervalos de muestreo del escenario.
	 * 
	 * @param c
	 * @param archNumpos
	 * @param iesc
	 * @param numpos1Esc
	 */
	public static void imprimeLineaNumpos(Corrida c, String archNumpos, int iesc, ArrayList<Integer> numpos1Esc) {

		if (iesc == 0) {
			StringBuilder sb = new StringBuilder();
			imprimeCabezal(sb, c);
			sb.append("NUMERO DE POSTE PARA CADA INTERVALO DE MUESTREO DE LOS ESCENARIOS" + "\n");
			LineaTiempo lt = c.getLineaTiempo();
			lt.reiniciar();
			PasoTiempo pt = lt.devuelvePasoActual();
			while (pt != null) {
				String fecha = lt.fechaDeInstante(pt.getInstanteInicial());
				int cantIM = pt.getDuracionPaso() / (pt.getBloque().getIntervaloMuestreo());
				for (int im = 0; im < cantIM; im++) {
					sb.append(fecha + "-IM=" + im + "\t");
				}
				lt.avanzarPaso();
				pt = lt.devuelvePasoActual();
			}
			DirectoriosYArchivos.agregaTexto(archNumpos, sb.toString());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("ESCENARIO " + iesc + "\t");
		for (Integer ip : numpos1Esc) {
			sb.append(ip);
			sb.append("\t");
		}
		DirectoriosYArchivos.agregaTexto(archNumpos, sb.toString());
	}

//	public static ArrayList<Integer> leeLineaNumpos(String archNumpos, int iesc) {
//		if(iesc==0) {
//			StringBuilder sb = new StringBuilder();
//			imprimeCabezal(sb, c);
//			sb.append("NUMERO DE POSTE PARA CADA INTERVALO DE MUESTREO DE LOS ESCENARIOS" + "\n");
//			DirectoriosYArchivos.agregaTexto(archNumpos, sb.toString());
//		}
//		StringBuilder sb = new StringBuilder();
//		sb.append("ESCENARIO " + iesc + "\t");
//		for(Integer ip: numpos1Esc) {
//			sb.append(ip);
//			sb.append("\t");
//		}
//		DirectoriosYArchivos.agregaTexto(archNumpos, sb.toString());
//	}	

	/**
	 * Dado un array con un valor por poste de un paso y el numpos de los intervalos
	 * de muestreo del paso devuelve otro array con un valor por intervalo de
	 * muestreo
	 */
	public double[] antiPostiza(double[] valPorPoste, int[] numpos) {
		int cantIm = numpos.length; // cantidad de intervalos de muestreo del paso
		double[] valPorIM = new double[cantIm];
		for (int im = 0; im < cantIm; im++) {
			valPorIM[im] = valPorPoste[numpos[im] - 1];
		}
		return valPorIM;
	}

	private void creaEstructuras() {
		ener_resumen_GWh = new double[cantRec][cantAnios];
		ener_Paso_MWh = new double[cantRec][cantPasos];
		cos_resumen_MUSD = new double[cantRec][cantAnios];
		ingmar_resumen_USD = new double[cantRec][cantAnios];
		ingmar_resumen_tope_USD = new double[cantRec][cantAnios];

		energiasComb_resumen = new Hashtable<String, double[][]>();
		volumenesComb_resumen = new Hashtable<String, double[][]>();
		costosComb_resumen = new Hashtable<String, double[][]>();

		/**
		 * Las mismas variables por escenario Clave nombre del generador térmico Valor
		 * primer indice año, segundo índice escenario, tercer índice combustible
		 */
		enerComb_esc = new Hashtable<String, double[][][]>();
		volComb_esc = new Hashtable<String, double[][][]>();
		cosComb_esc = new Hashtable<String, double[][][]>();

		gradGestion_resumen = new double[cantRec][cantAnios][]; // gradiente de gestión en MUSD/MW
		cos_tot_medio = new double[cantAnios];
		ener_esc = new double[cantRec][cantAnios][cantEsc];
		costo_esc = new double[cantRec][cantAnios][cantEsc];
		ingmar_esc = new double[cantRec][cantAnios][cantEsc];
		cosmar_prom_anio = new double[cantBarras][cantAnios];
		spot_prom_anio = new double[cantBarras][cantAnios];
		cosmar_prom_anio_auxEsc = new double[cantBarras][cantAnios][cantEsc];
		spot_prom_anio_auxEsc = new double[cantBarras][cantAnios][cantEsc];
		cosmar_prom_paso = new double[cantBarras][cantPasos][]; // primer índice barra, segundo índice paso, tercer
																// índice poste

		cosmar_prom_pasoEsc = new double[cantBarras][cantPasos][];
		cosmar_cron = new double[cantBarras][cantPasos][cantEsc][];
		cosmar_cron_im = new double[cantBarras][cantPasos][cantEsc][];

		resUnico = new ArrayList<StringBuilder>();

		cos_paso_cron = new double[cantPasos][cantEsc];
		cant_mod_disp = new int[cantRec][cantPasos][cantEsc];

		turbinadoAnualHm3 = new Hashtable<String, double[]>();
		vertidoAnualHm3 = new Hashtable<String, double[]>();

		energiaTGPorAnioGWh = new Hashtable<String, double[]>();
		energiaCVPorAnioGWh = new Hashtable<String, double[]>();
		energiaAbPorAnioGWh = new Hashtable<String, double[]>();
		energiaCombPorAnioGWh = new Hashtable<String, double[]>();
		
		horasFalla = new Hashtable<String, double[][]>();
		
		ingVentasCIPorAnioEscMUSD = new Hashtable<String, double[][]>();
		multaCIPorAnioEscMUSD = new Hashtable<String, double[][]>() ;
		enerNoEntregadaGWh = new Hashtable<String, double[][]>() ;
		
		cantArranquesPorEsc = new Hashtable<String, double[]>();
		ultimoDiaFunc = new Hashtable<String, int[]>();
		cantModDespUltimoIMPasoAnterior = new Hashtable<String, Integer>();
		
//		cantFuncUltimoIM = new Hashtable<String, Integer>();

		for (String rec : listaRec) {
			if (tipoDeRecurso.get(rec).equals(utilitarios.Constantes.HID)) {
				double[] turAux = new double[cantAnios];
				double[] verAux = new double[cantAnios];
				turbinadoAnualHm3.put(rec, turAux);
				vertidoAnualHm3.put(rec, verAux);
			}
			if (tipoDeRecurso.get(rec).equals(utilitarios.Constantes.TER)) {
				GeneradorTermico gt = (GeneradorTermico) participanteDeRecurso.get(rec);
				int cantC = gt.getListaCombustibles().size();
				String nom = gt.getNombre();
				double[][] auxE = new double[cantAnios][cantC];
				double[][] auxV = new double[cantAnios][cantC];
				double[][] auxC = new double[cantAnios][cantC];
				energiasComb_resumen.put(nom, auxE);
				volumenesComb_resumen.put(nom, auxV);
				costosComb_resumen.put(nom, auxC);
				double[][][] aux2E = new double[cantAnios][cantEsc][cantC];
				double[][][] aux2V = new double[cantAnios][cantEsc][cantC];
				double[][][] aux2C = new double[cantAnios][cantEsc][cantC];
				enerComb_esc.put(nom, aux2E);
				volComb_esc.put(nom, aux2V);
				cosComb_esc.put(nom, aux2C);

				for(int iesc=0; iesc<cantEsc; iesc++) {
					cantArranquesPorEsc.put(rec+"-"+iesc, new double[CANT_DIAS_MAX_ARRANQUE]);
				}
				int[] aux = new int[CANT_MAX_MOD];
				
				for(int imod=0; imod<CANT_MAX_MOD; imod++) {
					aux[imod] = -2;   // Para que al inicio de la corrida se compute un arranque
				}
				ultimoDiaFunc.put(rec, aux);
				cantModDespUltimoIMPasoAnterior.put(rec, 0);
//				cantFuncUltimoIM.put(rec, 0);
			}	
			
			/**
			 * Cantidad de módulos que funcionaron el último intervalo de muestreo del último día de funcionamiento.
			 */
//			cantFuncUltimoIM = new Hashtable<String, Integer>() ;
			
			
			if (tipoDeRecurso.get(rec).equals(utilitarios.Constantes.CC)) {
				CicloCombinado cc = (CicloCombinado) participanteDeRecurso.get(rec);
				int cantC = cc.getListaCombustibles().size();
				String nom = cc.getNombre();
				double[][] auxE = new double[cantAnios][cantC];
				double[][] auxV = new double[cantAnios][cantC];
				double[][] auxC = new double[cantAnios][cantC];
				energiasComb_resumen.put(nom, auxE);
				volumenesComb_resumen.put(nom, auxV);
				costosComb_resumen.put(nom, auxC);
				double[][][] aux2E = new double[cantAnios][cantEsc][cantC];
				double[][][] aux2V = new double[cantAnios][cantEsc][cantC];
				double[][][] aux2C = new double[cantAnios][cantEsc][cantC];
				enerComb_esc.put(nom, aux2E);
				volComb_esc.put(nom, aux2V);
				cosComb_esc.put(nom, aux2C);

				double[] enerAux = new double[cantAnios];
				energiaTGPorAnioGWh.put(rec, enerAux);
				enerAux = new double[cantAnios];
				energiaCVPorAnioGWh.put(rec, enerAux);
				enerAux = new double[cantAnios];
				energiaAbPorAnioGWh.put(rec, enerAux);
				enerAux = new double[cantAnios];
				energiaCombPorAnioGWh.put(rec, enerAux);

				for(int iesc=0; iesc<cantEsc; iesc++) {
					cantArranquesPorEsc.put(rec + "-" + "TG" +"-"+ iesc, new double[CANT_DIAS_MAX_ARRANQUE]);
					cantArranquesPorEsc.put(rec + "-" + "CV" +"-"+ iesc, new double[CANT_DIAS_MAX_ARRANQUE]);
				}
				int[] aux1 = new int[CANT_MAX_MOD];
				int[] aux2 = new int[CANT_MAX_MOD];
				for(int imod=0; imod<CANT_MAX_MOD; imod++) {
					aux1[imod] = -2;   // Para que al inicio de la corrida se compute un arranque
					aux2[imod] = -2;
				}
				ultimoDiaFunc.put(rec + "-TG", aux1);  // para que se cuente un arranque en el día 0 inicial de la corrida.
				ultimoDiaFunc.put(rec + "-CV", aux2);
				cantModDespUltimoIMPasoAnterior.put(rec + "-TG", 0);
				cantModDespUltimoIMPasoAnterior.put(rec + "-CV", 0);
//				cantFuncUltimoIM.put(rec + "-TG", 0);
//				cantFuncUltimoIM.put(rec + "-CV", 0);
				
			}
			
			if (tipoDeRecurso.get(rec).equalsIgnoreCase(utilitarios.Constantes.FALLA)) {
				Participante p = participanteDeRecurso.get(rec);
				Falla f = (Falla) p;
				int cantEscalones = f.getCantEscalones();
				double[][] aux = new double[cantAnios][cantEsc];
				horasFalla.put(rec, aux);
			}
			if (tipoDeRecurso.get(rec).equals(utilitarios.Constantes.CONTRATOINTERRUMPIBLE)) {
				Participante p = participanteDeRecurso.get(rec);
				String nom = p.getNombre();
				ingVentasCIPorAnioEscMUSD.put(nom, new double[cantAnios][cantEsc]);
				multaCIPorAnioEscMUSD.put(nom, new double[cantAnios][cantEsc]) ;	
				enerNoEntregadaGWh.put(nom, new double[cantAnios][cantEsc]) ;	
			}
		}

		horasFallaPorTipo = new Hashtable<String, double[][]>();

		// Crea estructuras para las salidas gráficas de Rodrigo-Bruno
		salidaAtributosDetallados = new Hashtable<ClaveDatosAtributosDetallados, DatosSalidaAtributosDetallados>();
//		salidaAtributosDetalladosPorTipo = new Hashtable<ClaveDatosAtributosDetallados, DatosSalidaAtributosDetallados>();
		nombresUnidadesAtributos = utilitarios.Constantes.getUnidadesAtributos();

		energiaDeTipoPorPasoGWh = new Hashtable<String, double[]>();
		costoDeTipoPorPasoMUSD = new Hashtable<String, double[]>();

		energiaDeTipoPorAnioGWh = new Hashtable<String, double[]>();
		costoDeTipoPorAnioMUSD = new Hashtable<String, double[]>();

		for (String tipo : utilitarios.Constantes.getTiposDeRecursos()) {
			double[] enerAux = new double[cantPasos];
			double[] costAux = new double[cantPasos];
			energiaDeTipoPorPasoGWh.put(tipo, enerAux);
			costoDeTipoPorPasoMUSD.put(tipo, costAux);
			enerAux = new double[cantAnios];
			costAux = new double[cantAnios];
			energiaDeTipoPorAnioGWh.put(tipo, enerAux);
			costoDeTipoPorAnioMUSD.put(tipo, costAux);

		}
	}

	
	/**
	 * Calcula el costo total por año promedio en los escenarios sin incluir el
	 * costo de los Impactos ni ContratosEnergia
	 */
	private void calculaCostoTotalAnualPromedio() {

		int indAnio = 0;
		for (indAnio = 0; indAnio < cantAnios; indAnio++) {
			double auxcos = 0;
			for (int indRec = 0; indRec < cantRec; indRec++) {
				Participante par = datosResumen.getParticipanteDeIndice().get(indRec);
				if (!(par instanceof Impacto) && !(par instanceof ContratoEnergia)
						)
					auxcos += cos_resumen_MUSD[indRec][indAnio];
			}
			cos_tot_medio[indAnio] = auxcos;
		}
	}
	
	
	/**
	 * Calcula la cantidad de arranques de un térmico o ciclo combinado en un paso.
	 * Se supone que el paso de la corrida es mayor o igual que un día.
	 */
	private void calculaArranquesPaso(String nombreRec, int indiceRec, DatosEPPUnEscenario d1esc, int[] numpos,
			PasoTiempo pt) {
		long instIniPaso = pt.getInstanteInicial();
		boolean esCC = false; // true si el recurso es un ciclo combinado
		boolean esCV = false; // true si se está calculando los arranques de ciclos de vapor
		if (tipoDeRecurso.get(nombreRec).equalsIgnoreCase(utilitarios.Constantes.TER)) {
			arranquesDelPaso(nombreRec, esCC, esCV, indiceRec, d1esc, numpos, pt);
		} else if (tipoDeRecurso.get(nombreRec).equalsIgnoreCase(utilitarios.Constantes.CC)) {
			CicloCombinado cc = (CicloCombinado) participanteDeIndice.get(indiceRec);
			if (cc.getCompG().getEvolucionComportamientos().get(utilitarios.Constantes.COMPCC).getValor(instIniPaso)
					.equalsIgnoreCase(utilitarios.Constantes.CCSINESTADO)) {
				esCC = true;
				arranquesDelPaso(nombreRec, esCC, esCV, indiceRec, d1esc, numpos, pt);
				esCV = true;
				arranquesDelPaso(nombreRec, esCC, esCV, indiceRec, d1esc, numpos, pt);
			}
		} else {
			System.out.println("El comportamiento del ciclo combinado " + nombreRec + " no permite calcular arranques");
			System.exit(1);
		}				
			
	}		
			
	
	
	/**
	 * Actualiza todas las variables de arranque del recurso incluso la cantidad de arranque
	 * 
	 * @param nombreRec  nombre del recurso sin sufijos
	 * @param esCC  true si se trata de un ciclo combinado y false si es un térmico.
	 * @param esCV true si se va a considerar los arranques de los ciclos de vapor y false las turbinas a gas.
	 * 
	 */
	private void arranquesDelPaso(String nombreRec, boolean esCC, boolean esCV, int indiceRec, DatosEPPUnEscenario d1esc, int[] numpos, PasoTiempo pt){
		Participante p = participanteDeIndice.get(indiceRec);
		int numPaso = pt.getNumpaso();
		int cantIm = pt.getBloque().getCantIm();
		int durIM = pt.getBloque().getIntervaloMuestreo();
		long instante = pt.getInstanteInicial();
		int ordinalDiaCorr = 0;
		int cantModInst = 0;
		double modTGporCV = 0; // para el ciclo combinado, el cociente entre la cantidad de TGs y la cantidad
								// de CVs del ciclo.

		String nombre = nombreRec;
		if (esCC) {
			CicloCombinado cc = (CicloCombinado) p;
			if (esCV) {
				nombre = nombreRec + "-CV";
				cantModInst = cc.getCCs().getCantModInst().getValor(instante);
				if (cantModInst != 0) {
					modTGporCV = cc.getTGs().getCantModInst().getValor(instante) / cantModInst;
				}else {
					modTGporCV = 0;
				}
			} else {
				nombre = nombreRec + "-TG";
				cantModInst = cc.getTGs().getCantModInst().getValor(instante);
			}
		} else {
			cantModInst = ((Recurso) p).getCantModInst().getValor(instante);
		}
		

		/**
		 * Si el IM es el primero del paso y funcionan k módulos, y
		 * cantModDespUltimoIMPasoAnterior = k-a con a>0, se cuentan a arranques. Para
		 * cada j, tal que k >= j > k-a, se calcula la duración de la parada previa al
		 * arranque como:
		 * 
		 * ordinal del día corriente - ultimoDiaFunc[j] - 1
		 *  
		 * Si el IM no es el primero del paso y funcionan k módulos y en el IM anterior del paso funcionaron k-a, con a>0, se cuentan a arranques.
		 * Para cada j, tal que k >= j > k-a, se calcula la duración del arranque como:
		 * 
		 * Si el IM no es el primero del paso y funcionan k módulos y en el IM anterior
		 * del paso funcionaron k-a, con a>0, se cuentan a arranques. Para cada j, tal
		 * que k >= j > k-a, se calcula la duración del arranque como:
		 * 
		 * ordinal del día corriente - ultimoDiaFunc[j] - 1
		 */

		int[] ordUltimoDiaFunc = ultimoDiaFunc.get(nombre);
		int encendidos = 0;
		int[] contador = new int[CANT_DIAS_MAX_ARRANQUE];

		int encendidosAnt = cantModDespUltimoIMPasoAnterior.get(nombre);
		int durParada = 0;
		if (cantModInst != 0) {
			for (int im = 0; im < cantIm; im++) {
				ordinalDiaCorr = lt.diaDelaCorrida(instante);
				int iposte = numpos[im] - 1;
				if (!esCC) {
					encendidos = d1esc.getCantModDesp()[indiceRec][numPaso][iposte];
				} else {
					CicloCombinado cc = (CicloCombinado) participanteDeIndice.get(indiceRec);
					if (cc.getCompG().getEvolucionComportamientos().get(utilitarios.Constantes.COMPCC)
							.getValor(instante).equalsIgnoreCase(utilitarios.Constantes.CCSINESTADO)) {
						if (esCV) {
							if (modTGporCV != 0)
								encendidos = (int) Math
										.floor((d1esc.getCantModTGCombdesp().get(nombreRec)[numPaso][iposte] + 1)
												/ modTGporCV);
						} else {
							encendidos = d1esc.getCantModTGCombdesp().get(nombreRec)[numPaso][iposte]
									+ d1esc.getCantModTGAbdesp().get(nombreRec)[numPaso][iposte];
						}

					} else {
						System.out.println("El comportamiento del CC" + nombreRec + "no permite calcular arranques");
						System.exit(1);
					}
				}
				for (int j = encendidosAnt + 1; j <= encendidos; j++) {
					durParada = Math.min(ordinalDiaCorr - ordUltimoDiaFunc[j - 1] - 1, CANT_DIAS_MAX_ARRANQUE - 1);
					durParada = Math.max(durParada, 0);
					contador[durParada]++;
				}
				for (int j = 0; j < encendidos; j++) {
					ultimoDiaFunc.get(nombre)[j] = ordinalDiaCorr;
				}
				if (im == cantIm - 1) {
					cantModDespUltimoIMPasoAnterior.put(nombre, encendidos);
				}
				encendidosAnt = encendidos;
				instante += durIM;
			}
			for (int a = 0; a < CANT_DIAS_MAX_ARRANQUE; a++) {
				int iesc = d1esc.getNumeroEsc() - 1;
				cantArranquesPorEsc.get(nombre + "-" + iesc)[a] += (contador[a] / cantModInst);
			}

		}

	}
	


	private static void imprimeCabezal(StringBuilder sb, Corrida c) {
		sb.append("Nombre de la corrida: " + c.getNombre());
		sb.append("\n");
		sb.append("Descripcion: " + c.getDescripcion());
		sb.append("\n");
		sb.append("Fecha-hora de ejecucion: " + c.getFechaEjecucion() + " - " + c.getHoraEjecucion());
		sb.append("\n");
//		sb.append("Fecha de inicio de corrida: " + dameStringFecha(c.getLineaTiempo().getInicioTiempo()));
//		sb.append("\n");
//		sb.append("Fecha de fin de corrida: " + dameStringFecha(c.getLineaTiempo().getFinTiempo()));
//		sb.append("\n");
		sb.append("Cantidad_escenarios: " + "\t" + c.getCantEscenarios());
		sb.append("\n");
		sb.append("Cantidad_pasos_total: " + "\t" + c.getCantidadPasos());
		sb.append("\n");
	}

	private void imprimeModulosYPotencias(Corrida c, String dirArch) {
		StringBuilder sbc = new StringBuilder();
		imprimeCabezal(sbc, c);
		LineaTiempo lt = c.getLineaTiempo();
		int indAnio;
		long[] instIni = lt.getInstInicioAnio();
		if (param[Constantes.PARAMSAL_RESUMEN][0] == 1) {

			if (DirectoriosYArchivos.existeArchivo(dirArch))
				DirectoriosYArchivos.eliminaArchivo(dirArch);
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			for (indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append(indAnio + anioIni);
				sb.append("\t");
			}
			sb.append("\n");
			sb.append("CANTIDAD DE MODULOS DE PARTICIPANTES AL COMIENZO DEL AÑO \n");
			for (int indRec = 0; indRec < cantRec; indRec++) {
				String nombre = listaRec.get(indRec);
				Participante par = datosResumen.getParticipanteDeIndice().get(indRec);
				if (par instanceof Recurso) {
					Recurso rec = (Recurso) par;
					sb.append(nombre + "\t");
					if (rec.getCantModInst() != null) {
						for (indAnio = 0; indAnio < cantAnios; indAnio++) {
							if (!(par instanceof CicloCombinado))
								sb.append(rec.getCantModInst().getValor(instIni[indAnio]));
							sb.append("\t");
						}
						sb.append("\n");
					} else {
						if (par instanceof CicloCombinado) {
							for (indAnio = 0; indAnio < cantAnios; indAnio++) {
								sb.append(1);
								sb.append("\t");
							}

						}
						sb.append("\n");
					}
				}
			}
			sb.append("\n");
			sb.append("POTENCIA TOTAL DE GENERADORES(MW) \n");
			for (int indRec = 0; indRec < cantRec; indRec++) {
				String nombre = listaRec.get(indRec);
				Participante par = datosResumen.getParticipanteDeIndice().get(indRec);
				if (par instanceof GeneradorHidraulico) {
					Generador g = (Generador) par;
					sb.append(nombre + "\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						double pot = g.getPotenciaMaxima().getValor(instIni[indAnio])
								* g.getCantModInst().getValor(instIni[indAnio]);
						sb.append(pot + "\t");
					}
					sb.append("\n");
				}
				if (par instanceof GeneradorTermico) {
					Generador g = (Generador) par;
					sb.append(nombre + "\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						double pot = g.getPotenciaMaxima().getValor(instIni[indAnio])
								* g.getCantModInst().getValor(instIni[indAnio]);
						sb.append(pot + "\t");
					}
					sb.append("\n");
				}
				if (par instanceof CicloCombinado) {
					CicloCombinado g = (CicloCombinado) par;
					sb.append(nombre + "\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						double pot = g.getCCs().getPotenciaMaxima().getValor(instIni[indAnio])
								* g.getTGs().getCantModInst().getValor(instIni[indAnio]);
						sb.append(pot + "\t");
					}
					sb.append("\n");
				}
				if (par instanceof GeneradorEolico) {
					GeneradorEolico g = (GeneradorEolico) par;
					sb.append(nombre + "\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						double pot = g.getPotenciaMaxima().getValor(instIni[indAnio])
								* g.getCantModInst().getValor(instIni[indAnio]);
						sb.append(pot + "\t");
					}
					sb.append("\n");
				}
				
				if (par instanceof GeneradorFotovoltaico) {
					GeneradorFotovoltaico g = (GeneradorFotovoltaico) par;
					sb.append(nombre + "\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						double pot = g.getPotenciaMaxima().getValor(instIni[indAnio])
								* g.getCantModInst().getValor(instIni[indAnio]);
						sb.append(pot + "\t");
					}
					sb.append("\n");
				}
				
				if (par instanceof Acumulador) {
					Acumulador g = (Acumulador) par;
					sb.append(nombre + "\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						double pot = g.getPotenciaMaxima().getValor(instIni[indAnio])
								* g.getCantModInst().getValor(instIni[indAnio]);
						sb.append(pot + "\t");
					}
					sb.append("\n");
				}				
			}
			DirectoriosYArchivos.grabaTexto(dirArch, sbc.toString() + sb.toString());
			resUnico.add(sb);

		}

	}

	private void imprimeResumen(String dirArch) {
		// IMPRIME RESUMEN
		Corrida c = datosResumen.getCorrida();
		StringBuilder sbc = new StringBuilder();
		imprimeCabezal(sbc, c);
		int indAnio;
		if (param[Constantes.PARAMSAL_RESUMEN][0] == 1) {

			if (DirectoriosYArchivos.existeArchivo(dirArch))
				DirectoriosYArchivos.eliminaArchivo(dirArch);
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			for (indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append(indAnio + anioIni);
				sb.append("\t");
			}
			sb.append("\n");
			sb.append("ENERGIAS ESPERADAS POR TIPO DE RECURSO EN GWh \n");

			for (String st : listaTiposRec) {
				sb.append(st);
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(energiaDeTipoPorAnioGWh.get(st)[indAnio]);
					sb.append("\t");
				}
				sb.append("\n");
			}
			sb.append("\n");

			sb.append("COSTOS ESPERADOS POR TIPO DE RECURSO EN MUSD \n");

			for (String st : listaTiposRec) {
				sb.append(st);
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(costoDeTipoPorAnioMUSD.get(st)[indAnio]);
					sb.append("\t");
				}
				sb.append("\n");
			}
			sb.append("\n");

			sb.append("\t");
			for (indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append(indAnio + anioIni);
				sb.append("\t");
			}
			sb.append("\n");
			sb.append("horas\t");
			for (indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append(durAnios[indAnio] / utilitarios.Constantes.SEGUNDOSXHORA);
				sb.append("\t");
			}
			sb.append("\n");
			sb.append("ENERGIAS ESPERADAS POR RECURSO EN GWh \n");
			for (int indRec = 0; indRec < cantRec; indRec++) {
				if (tipoDeRecurso.get(listaRec.get(indRec)) == Constantes.IMPACTO)
					continue;
				double signo = 1.0;
				String nombreRec = listaRec.get(indRec);
				String tipo = tipoDeRecurso.get(nombreRec);
				if (tipo == utilitarios.Constantes.IMPOEXPO) {
					ImpoExpo ie = (ImpoExpo) participanteDeIndice.get(indRec);
					if (ie.getOperacionCompraVenta().equalsIgnoreCase(utilitarios.Constantes.VENTA))
						signo = -1.0;
				}
				sb.append(tipo);
				sb.append("-");
				sb.append(listaRec.get(indRec));
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(signo * ener_resumen_GWh[indRec][indAnio]);
					sb.append("\t");
				}
				sb.append("\n");
			}
			sb.append("\n");

			sb.append("COSTOS ESPERADOS EN MUSD \n");
			// Participantes excepto Impactos y contratosEnergia
			for (int indRec = 0; indRec < cantRec; indRec++) {
				Participante par = datosResumen.getParticipanteDeIndice().get(indRec);
				if (!(par instanceof Impacto) && !(par instanceof ContratoEnergia)
						) {
					sb.append(listaRec.get(indRec));
					sb.append("\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						sb.append(cos_resumen_MUSD[indRec][indAnio]);
						sb.append("\t");
					}
					sb.append("\n");
				}
			}
			sb.append("\n");
			sb.append("COSTO_TOTAL SIN CONTRATOS ENERGIA NI IMPACTOS");
			sb.append("\t");
			for (indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append(cos_tot_medio[indAnio]);
				sb.append("\t");
			}
			sb.append("\n");

			// Impactos y contratosEnergia
			sb.append("COSTO DE IMPACTOS Y DE CONTRATOS DE ENERGIA \n");
			double[] cosImpYCE = new double[cantAnios];
			for (int indRec = 0; indRec < cantRec; indRec++) {
				Participante par = datosResumen.getParticipanteDeIndice().get(indRec);
				if (par instanceof Impacto || par instanceof ContratoEnergia ) {
					sb.append(listaRec.get(indRec));
					sb.append("\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						sb.append(cos_resumen_MUSD[indRec][indAnio]);
						sb.append("\t");
						cosImpYCE[indAnio] += cos_resumen_MUSD[indRec][indAnio];
					}
					sb.append("\n");
				}
			}
			sb.append("\n");
			
			sb.append("COSTO TOTAL INCLUSO IMPACTOS Y CONTRATOS DE ENERGIA (MUSD)\t");
			for(indAnio=0; indAnio<cantAnios; indAnio++) {
				sb.append(cos_tot_medio[indAnio]+cosImpYCE[indAnio]);
				sb.append("\t");
			}
			
			sb.append("\n");
			sb.append("COSTO MARGINAL PROMEDIO CURVA PLANA (USD/MWh) \n");
			int indBarra = 0;
			for (Barra b : barras) {
				sb.append(b.getNombre());
				sb.append("\t");
				for (int ian = 0; ian < cantAnios; ian++) {
					sb.append(cosmar_prom_anio[indBarra][ian]);
					sb.append("\t");
				}
				indBarra++;
			}
			sb.append("\n");

			sb.append("\n");
			sb.append("SPOT (CON TOPE " + utilitarios.Constantes.TOPE_SPOT + ") PROMEDIO CURVA PLANA (USD/MWh) \n");
			indBarra = 0;
			for (Barra b : barras) {
				sb.append(b.getNombre());
				sb.append("\t");
				for (int ian = 0; ian < cantAnios; ian++) {
					sb.append(spot_prom_anio[indBarra][ian]);
					sb.append("\t");
				}
				indBarra++;
			}
			sb.append("\n");

			// Volúmenes de hidráulicas
			sb.append("\n");
			sb.append("VOLUMENES EN HM3 Y CAUDALES MEDIOS EN m3/s DE HIDRAULICOS Y COEFICIENTES ENERGETICOS");
			sb.append("\n");
			sb.append("\n");
			for (int indRec = 0; indRec < cantRec; indRec++) {
				String nombre = listaRec.get(indRec);
				Participante par = datosResumen.getParticipanteDeIndice().get(indRec);
				if (par instanceof GeneradorHidraulico) {
					sb.append(nombre);
					sb.append("\n");
					sb.append(" Turb.hm3");
					sb.append("\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						sb.append(turbinadoAnualHm3.get(nombre)[indAnio]);
						sb.append("\t");
					}
					sb.append("\n");
					sb.append("Vert.hm3");
					sb.append("\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						sb.append(vertidoAnualHm3.get(nombre)[indAnio]);
						sb.append("\t");
					}
					sb.append("\n");
					sb.append("Coef.energ[MWh/hm3]");
					sb.append("\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						sb.append(ener_resumen_GWh[indRec][indAnio] * utilitarios.Constantes.MWHXGWH
								/ turbinadoAnualHm3.get(nombre)[indAnio]);
						sb.append("\t");
					}
					sb.append("\n");
					sb.append("Coef.energ[MW/(m3/s)]");
					sb.append("\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						double eMWmed = ener_resumen_GWh[indRec][indAnio] * utilitarios.Constantes.MWHXGWH
								/ (durAnios[indAnio] / utilitarios.Constantes.SEGUNDOSXHORA);
						double qm3smed = turbinadoAnualHm3.get(nombre)[indAnio] * utilitarios.Constantes.M3XHM3
								/ durAnios[indAnio];
						sb.append(eMWmed / qm3smed);
						sb.append("\t");
					}
					sb.append("\n");
					sb.append("Turb.medio m3/s");
					sb.append("\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						sb.append(turbinadoAnualHm3.get(nombre)[indAnio] * utilitarios.Constantes.M3XHM3
								/ durAnios[indAnio]);
						sb.append("\t");
					}
					sb.append("\n");
					sb.append("Vert.medio m3/s");
					sb.append("\t");
					for (indAnio = 0; indAnio < cantAnios; indAnio++) {
						sb.append(vertidoAnualHm3.get(nombre)[indAnio] * utilitarios.Constantes.M3XHM3
								/ durAnios[indAnio]);
						sb.append("\t");
					}
					sb.append("\n");
				}
			}

			// Resultados de combustibles
			sb.append("\n");
			sb.append("\n");
			sb.append("VOLUMENES DE COMBUSTIBLE Y COSTOS DE COMBUSTIBLE DE TERMICOS Y CC");
			sb.append("\n");
			for (int indRec = 0; indRec < cantRec; indRec++) {
				String nombre = listaRec.get(indRec);
				Participante par = datosResumen.getParticipanteDeIndice().get(indRec);
				if (par instanceof GeneradorTermico) {
					sb.append(nombre);
					sb.append("\t");
					sb.append(" Energias por combustible (GWh)\n");
					GeneradorTermico gt = (GeneradorTermico) par;
					int cantC = gt.getListaCombustibles().size();
					for (int ic = 0; ic < cantC; ic++) {
						String nomC = gt.getListaCombustibles().get(ic);
						sb.append(nomC + "\t");
						for (indAnio = 0; indAnio < cantAnios; indAnio++) {
							sb.append(energiasComb_resumen.get(nombre)[indAnio][ic] / 1000);
							sb.append("\t");
						}
						sb.append("\n");
					}
					sb.append(nombre);
					sb.append("\t");
					sb.append(" Volumenes de combustible (Munidades)\n");
					for (int ic = 0; ic < cantC; ic++) {
						String nomC = gt.getListaCombustibles().get(ic);
						sb.append(nomC + "\t");
						for (indAnio = 0; indAnio < cantAnios; indAnio++) {
							sb.append(volumenesComb_resumen.get(nombre)[indAnio][ic] / 1.0E6);
							sb.append("\t");
						}
						sb.append("\n");
					}
					sb.append(nombre);
					sb.append("\t");
					sb.append(" Costos por combustible (MUSD)\n");
					for (int ic = 0; ic < cantC; ic++) {
						String nomC = gt.getListaCombustibles().get(ic);
						sb.append(nomC + "\t");
						for (indAnio = 0; indAnio < cantAnios; indAnio++) {
							sb.append(costosComb_resumen.get(nombre)[indAnio][ic] / 1.0E6);
							sb.append("\t");
						}
						sb.append("\n");
					}
					sb.append("\n");
				}
			}

			for (int indRec = 0; indRec < cantRec; indRec++) {
				String nombre = listaRec.get(indRec);
				Participante par = datosResumen.getParticipanteDeIndice().get(indRec);
				if (par instanceof CicloCombinado) {
					sb.append(nombre);
					sb.append("\t");
					sb.append(" Energias por combustible (GWh)\n");
					CicloCombinado cc = (CicloCombinado) par;
					int cantC = cc.getListaCombustibles().size();
					for (int ic = 0; ic < cantC; ic++) {
						String nomC = cc.getListaCombustibles().get(ic);
						sb.append(nomC + "\t");
						for (indAnio = 0; indAnio < cantAnios; indAnio++) {
							sb.append(energiasComb_resumen.get(nombre)[indAnio][ic] / 1000);
							sb.append("\t");
						}
						sb.append("\n");
					}
					sb.append(nombre);
					sb.append("\t");
					sb.append(" Volumenes de combustible (Munidades)\n");
					for (int ic = 0; ic < cantC; ic++) {
						String nomC = cc.getListaCombustibles().get(ic);
						sb.append(nomC + "\t");
						for (indAnio = 0; indAnio < cantAnios; indAnio++) {
							sb.append(volumenesComb_resumen.get(nombre)[indAnio][ic] / 1.0E6);
							sb.append("\t");
						}
						sb.append("\n");
					}
					sb.append(nombre);
					sb.append("\t");
					sb.append(" Costos por combustible (MUSD)\n");
					for (int ic = 0; ic < cantC; ic++) {
						String nomC = cc.getListaCombustibles().get(ic);
						sb.append(nomC + "\t");
						for (indAnio = 0; indAnio < cantAnios; indAnio++) {
							sb.append(costosComb_resumen.get(nombre)[indAnio][ic] / 1.0E6);
							sb.append("\t");
						}
						sb.append("\n");
					}
					sb.append("\n");
				}
			}
			sb.append("\n");
			
		
			
			DirectoriosYArchivos.grabaTexto(dirArch, sbc.toString() + sb.toString());
			resUnico.add(sb);
		}
		
		

	}

	/**
	 * Imprime los costos variables medios, cociente entre costo total esperado y
	 * energía generada esperada para las centrales térmicas (sin separar por
	 * combustibles) y los factores de planta esperados
	 * 
	 * @param dirArch
	 */
	public void imprimeResuTerm(String dirArch) {
		Corrida c = datosResumen.getCorrida();
		StringBuilder sb = new StringBuilder();
		imprimeCabezal(sb, c);

		LineaTiempo lt = c.getLineaTiempo();
		// Se usan las estructuras
		// double[][] ener_resumen_GWh; // primer índice recurso, segundo índice año
		// double[][] cos_resumen_MUSD; // primer índice recurso, segundo índice año
		int indAnio;

		sb.append(
				"Costo variable medio de centrales termicas suma de combustible y no combustible (no discrimina distintos combs.) (USD/MWh)");
		sb.append("\n\t");
		for (indAnio = 0; indAnio < cantAnios; indAnio++) {
			sb.append(indAnio + anioIni);
			sb.append("\t");
		}
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			String nomRec = listaRec.get(indRec);
			if (tipoDeRecurso.get(nomRec).equalsIgnoreCase(utilitarios.Constantes.TER)
					|| tipoDeRecurso.get(nomRec).equalsIgnoreCase(utilitarios.Constantes.CC)) {
				sb.append(listaRec.get(indRec));
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(cos_resumen_MUSD[indRec][indAnio] * utilitarios.Constantes.USDXMUSD
							/ (ener_resumen_GWh[indRec][indAnio] * utilitarios.Constantes.MWHXGWH));
					sb.append("\t");
				}
				sb.append("\n");
			}
		}
		sb.append("\n");

		sb.append("Factor de planta esperado relativo a la potencia al comienzo del anio (en porciento)");
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			String nomRec = listaRec.get(indRec);
			if (tipoDeRecurso.get(nomRec).equalsIgnoreCase(utilitarios.Constantes.TER)) {
				sb.append(nomRec);
				GeneradorTermico gt = (GeneradorTermico) participanteDeRecurso.get(nomRec);
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					long instIni = lt.getInstInicioAnio()[indAnio];
					double pot = gt.getPotenciaMaxima().getValor(instIni); // potencia máxima en MW
					int mod = gt.getCantModInst().getValor(instIni).intValue();
					int durHorasAnio = durAnios[indAnio] / utilitarios.Constantes.SEGUNDOSXHORA;
					sb.append((ener_resumen_GWh[indRec][indAnio] * utilitarios.Constantes.MWHXGWH * 100)
							/ (pot * mod * durHorasAnio));
					sb.append("\t");
				}
				sb.append("\n");
			} else if (tipoDeRecurso.get(nomRec).equalsIgnoreCase(utilitarios.Constantes.CC)) {
				sb.append(nomRec);
				CicloCombinado cc = (CicloCombinado) participanteDeRecurso.get(nomRec);
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					long instIni = lt.getInstInicioAnio()[indAnio];
					double pot = cc.getCCs().getPotenciaMaxima().getValor(instIni); // potencia máxima en MW
					int mod = cc.getTGs().getCantModInst().getValor(instIni);
					int durHorasAnio = durAnios[indAnio] / utilitarios.Constantes.SEGUNDOSXHORA;
					sb.append((ener_resumen_GWh[indRec][indAnio] * utilitarios.Constantes.MWHXGWH * 100)
							/ (pot * mod * durHorasAnio));
					sb.append("\t");
				}
				sb.append("\n");
			}
		}
		DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());

	}

	/**
	 * Imprime las energías por TG y CV y de turbinas en ciclo abierto y cerrado,
	 * los costos variables medios, cociente entre costo total esperado y energía
	 * generada esperada (sin separar por combustibles) y los factores de planta
	 * esperados
	 * 
	 * @param dirArch
	 */
	public void imprimeResuCC(String dirArch) {
		Corrida c = datosResumen.getCorrida();
		StringBuilder sb = new StringBuilder();
		imprimeCabezal(sb, c);

		LineaTiempo lt = c.getLineaTiempo();
		// Se usan las estructuras
		// double[][] ener_resumen_GWh; // primer índice recurso, segundo índice año
		// double[][] cos_resumen_MUSD; // primer índice recurso, segundo índice año
		int indAnio;

		sb.append("\t");
		for (indAnio = 0; indAnio < cantAnios; indAnio++) {
			sb.append(indAnio + anioIni);
			sb.append("\t");
		}
		sb.append("Energias de TGs y CVs y de turbinas en ciclo abierto y cerrado, de los ciclos combinados");
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			String nomRec = listaRec.get(indRec);
			if (tipoDeRecurso.get(nomRec).equalsIgnoreCase(utilitarios.Constantes.CC)) {
				sb.append(nomRec);
				sb.append("\n");
				sb.append("\n");
				sb.append("Energia de TGs (GWh)");
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(energiaTGPorAnioGWh.get(nomRec)[indAnio]);
					sb.append("\t");
				}
				sb.append("\n");
				sb.append("Energia de ciclos de vapor (GWh)");
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(energiaCVPorAnioGWh.get(nomRec)[indAnio]);
					sb.append("\t");
				}
				sb.append("\n");
				sb.append("Energia de turbinas en ciclo abierto (GWh)");
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(energiaAbPorAnioGWh.get(nomRec)[indAnio]);
					sb.append("\t");
				}
				sb.append("\n");
				sb.append("Energia de turbinas en ciclo combinado (GWh)");
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(energiaCombPorAnioGWh.get(nomRec)[indAnio]);
					sb.append("\t");
				}
				sb.append("\n");
			}
		}

		sb.append(
				"Costo variable medio de ciclos combinados suma de combustible y no combustible (no discrimina distintos combs.) (USD/MWh)");
		sb.append("\n\t");
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			String nomRec = listaRec.get(indRec);
			if (tipoDeRecurso.get(nomRec).equalsIgnoreCase(utilitarios.Constantes.CC)) {
				sb.append(listaRec.get(indRec));
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					sb.append(cos_resumen_MUSD[indRec][indAnio] * utilitarios.Constantes.USDXMUSD
							/ (ener_resumen_GWh[indRec][indAnio] * utilitarios.Constantes.MWHXGWH));
					sb.append("\t");
				}
				sb.append("\n");
			}
		}
		sb.append("\n");

		sb.append("Factor de planta esperado relativo a la potencia al comienzo del anio (en porciento)");
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			String nomRec = listaRec.get(indRec);
			if (tipoDeRecurso.get(nomRec).equalsIgnoreCase(utilitarios.Constantes.CC)) {
				sb.append(nomRec);
				CicloCombinado cc = (CicloCombinado) participanteDeRecurso.get(nomRec);
				sb.append("\t");
				for (indAnio = 0; indAnio < cantAnios; indAnio++) {
					long instIni = lt.getInstInicioAnio()[indAnio];
					double pot = cc.getCCs().getPotenciaMaxima().getValor(instIni); // potencia máxima en MW
					int mod = cc.getTGs().getCantModInst().getValor(instIni).intValue();
					int durHorasAnio = durAnios[indAnio] / utilitarios.Constantes.SEGUNDOSXHORA;
					sb.append((ener_resumen_GWh[indRec][indAnio] * utilitarios.Constantes.MWHXGWH * 100)
							/ (pot * mod * durHorasAnio));
					sb.append("\t");
				}
				sb.append("\n");
			}
		}

		DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());

	}

	/**
	 * Imprime los ingresos de centrales a costo marginal con tope
	 * 
	 * @param dirArch
	 */
	public void imprimeResumIng(String dirArch) {
		Corrida c = datosResumen.getCorrida();
		StringBuilder sb = new StringBuilder();
		imprimeCabezal(sb, c);

		sb.append("VALOR DE LA ENERGIA AL COSTO MARGINAL SIN TOPE (MUSD) \n\t");

		for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
			sb.append(indAnio + anioIni);
			sb.append("\t");
		}
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			sb.append(listaRec.get(indRec));
			sb.append("\t");
			for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append(ingmar_resumen_USD[indRec][indAnio] / 1E6);
				sb.append("\t");
			}
			sb.append("\n");
		}
		sb.append("\n");

		sb.append("VALOR DE LA ENERGIA AL COSTO MARGINAL CON TOPE (MUSD) \n\t");

		for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
			sb.append(indAnio + anioIni);
			sb.append("\t");
		}
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			sb.append(listaRec.get(indRec));
			sb.append("\t");
			for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append(ingmar_resumen_tope_USD[indRec][indAnio] / 1E6);
				sb.append("\t");
			}
			sb.append("\n");
		}
		sb.append("\n");

		DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());

	}

	public void imprimeGradientes(String dirArch) {

		Corrida c = datosResumen.getCorrida();
		StringBuilder sbc = new StringBuilder();
		imprimeCabezal(sbc, c);
		StringBuilder sb = new StringBuilder();
		sb.append("GRADIENTE DE GESTION ANUAL ESPERADO PARA DISPONIBILIDAD ESPERADA SIN TOPE (USD/kW) \n\t");
		for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
			sb.append(indAnio + anioIni);
			sb.append("\t");
		}
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			if (gradGestion_resumen[indRec][0] != null) {
				Participante p = getParticipanteDeIndice().get(indRec);

				sb.append(listaRec.get(indRec));
				sb.append("\n");
				for (int ic = 0; ic < gradGestion_resumen[indRec][0].length; ic++) {
					if (p instanceof GeneradorTermico) {
						GeneradorTermico gt = (GeneradorTermico) p;
						sb.append(gt.getListaCombustibles().get(ic) + "\t");
					} else if (p instanceof CicloCombinado) {
						CicloCombinado cc = (CicloCombinado) p;
						sb.append(cc.getListaCombustibles().get(ic) + "\t");

					} else {
						sb.append("\t");
					}
					for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
						if (gradGestion_resumen[indRec][indAnio] != null) {
							sb.append(gradGestion_resumen[indRec][indAnio][ic]);
							sb.append("\t");
						}
					}
					sb.append("\n");
				}
			}
		}

		sb.append("\n");

		sb.append("VALOR MEDIO AL COSTO MARGINAL SIN TOPE(USD/MWh)  \n\t");
		for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
			sb.append(indAnio + anioIni);
			sb.append("\t");
		}
		sb.append("\n");
		for (int indRec = 0; indRec < cantRec; indRec++) {
			sb.append(listaRec.get(indRec));
			sb.append("\t");
			for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append(ingmar_resumen_USD[indRec][indAnio]
						/ (ener_resumen_GWh[indRec][indAnio] * utilitarios.Constantes.MWHXGWH));
				sb.append("\t");
			}
			sb.append("\n");
		}

		DirectoriosYArchivos.grabaTexto(dirArch, sbc.toString() + sb.toString());
		resUnico.add(sb);

	}

	/**
	 * Imprime resultados de consumo de combustibles por crónica y año si se pidió
	 * salida de enercron. Produce -para cada térmico y ciclo combinado - un archivo
	 * con todos los GWh, unidades y costos de cada combustible por año y crónica
	 * -para cada combustible un archivo con todos GWh, unidades y costos de cada
	 * generador, por año y crónica.
	 * 
	 */
	public void imprimeCombCron(String directorio) {

		/**
		 * Volumen en unidades y energía de cada térmica y ciclo combinado por año y
		 * crónica Clave nombre del combustible Valor: primer índice año, segundo índice
		 * generador, tercer índice escenario
		 * 
		 */

		if (param[Constantes.PARAMSAL_ENERCRON][0] == 1) {
			Set<String> listaTodosCombsTer = new HashSet<String>(); // lista de nombres de combustibles
			Set<String> listaTodosCombsCC = new HashSet<String>(); // lista de nombres de combustibles
			/**
			 * Clave: nombre del combustible Valor: lista generadores termicos o ciclos
			 * combinados que usan el combustible
			 */
			Hashtable<String, ArrayList<GeneradorTermico>> termicasDeComb = new Hashtable<String, ArrayList<GeneradorTermico>>();
			Hashtable<String, ArrayList<CicloCombinado>> ciclosDeComb = new Hashtable<String, ArrayList<CicloCombinado>>();

			String dirComb = directorio + "/COMB-CRON";
			if (!DirectoriosYArchivos.existeArchivo(dirComb)) {
				DirectoriosYArchivos.creaDirectorio(directorio, "/COMB-CRON");
			}
			for (String nombreRec : listaRec) {
				Participante par = participanteDeRecurso.get(nombreRec);
				if (par instanceof GeneradorTermico || par instanceof CicloCombinado) {
					GeneradorTermico gt;
					CicloCombinado cc;
					String nomTer = "";
					ArrayList<String> listaComb = null;
					int cantC = 0;
					if (par instanceof GeneradorTermico) {
						gt = (GeneradorTermico) par;
						listaComb = gt.getListaCombustibles();
						for (String sc : listaComb) {
							if (!listaTodosCombsTer.contains(sc)) {
								listaTodosCombsTer.add(sc);
								termicasDeComb.put(sc, new ArrayList<GeneradorTermico>());
							}
							termicasDeComb.get(sc).add(gt);
						}
						nomTer = gt.getNombre();
						cantC = gt.getListaCombustibles().size();
					}
					if (par instanceof CicloCombinado) {
						cc = (CicloCombinado) par;
						listaComb = cc.getListaCombustibles();
						for (String sc : listaComb) {
							if (!listaTodosCombsCC.contains(sc)) {
								listaTodosCombsCC.add(sc);
								ciclosDeComb.put(sc, new ArrayList<CicloCombinado>());
							}
							ciclosDeComb.get(sc).add(cc);
						}
						nomTer = cc.getNombre();
						cantC = cc.getListaCombustibles().size();
					}

					double[][][] datos = new double[cantAnios][cantEsc][3 * cantC];
					double[][][] dener = enerComb_esc.get(nomTer);
					double[][][] dvol = volComb_esc.get(nomTer);
					double[][][] dcos = cosComb_esc.get(nomTer);
					String[] nombres = new String[3 * cantC];
					for (int ian = 0; ian < cantAnios; ian++) {
						for (int iesc = 0; iesc < cantEsc; iesc++) {
							int ii = 0;
							for (int ic = 0; ic < cantC; ic++) {
								datos[ian][iesc][ii] = dener[ian][iesc][ic] / 1000;
								nombres[ii] = listaComb.get(ic) + "-GWh";
								ii++;
							}
							for (int ic = 0; ic < cantC; ic++) {
								datos[ian][iesc][ii] = dvol[ian][iesc][ic] / 1E6;
								nombres[ii] = listaComb.get(ic) + "-Munidades";
								ii++;
							}
							for (int ic = 0; ic < cantC; ic++) {
								datos[ian][iesc][ii] = dcos[ian][iesc][ic] / 1E6;
								nombres[ii] = listaComb.get(ic) + "-MUSD";
								ii++;
							}
						}
					}
					String nomArch = dirComb + "/" + nomTer + sufijo + ".xlt";
					imprimeCron(datos, nombres, nomArch, "COMBUSTIBLES-" + nomTer);
				}
			}

			ArrayList<String> listaTodosCombs = new ArrayList<String>();
			for (String nc : listaTodosCombsTer) {
				if (!listaTodosCombs.contains(nc))
					listaTodosCombsTer.add(nc);
			}
			for (String nc : listaTodosCombsCC) {
				if (!listaTodosCombs.contains(nc))
					listaTodosCombsCC.add(nc);
			}

			for (String sc : listaTodosCombs) {
				// Se imprimen para cada combustible los volúmenes de cada térmica o CC
				ArrayList<GeneradorTermico> listaTer = termicasDeComb.get(sc);
				ArrayList<CicloCombinado> listaCC = ciclosDeComb.get(sc);
				int cantTerYCC = 0;
				if (listaTer != null) {
					cantTerYCC = listaTer.size();
				}
				if (listaCC != null) {
					cantTerYCC += listaCC.size();
				}
				double[][][] datos = new double[cantAnios][cantEsc][cantTerYCC];
				String[] nombresTer = new String[cantTerYCC];
				int it = 0;
				if (listaTer != null) {
					for (GeneradorTermico gt : listaTer) {
						nombresTer[it] = gt.getNombre();
						int ic = gt.getListaCombustibles().indexOf(sc);
						for (int ian = 0; ian < cantAnios; ian++) {
							for (int iesc = 0; iesc < cantEsc; iesc++) {
								datos[ian][iesc][it] = volComb_esc.get(nombresTer[it])[ian][iesc][ic] / 1E6;
							}
						}
						it++;
					}
				}
				if (listaCC != null) {
					for (CicloCombinado cc : listaCC) {
						nombresTer[it] = cc.getNombre();
						int ic = cc.getListaCombustibles().indexOf(sc);
						for (int ian = 0; ian < cantAnios; ian++) {
							for (int iesc = 0; iesc < cantEsc; iesc++) {
								datos[ian][iesc][it] = volComb_esc.get(nombresTer[it])[ian][iesc][ic] / 1E6;
							}
						}
						it++;
					}
				}
				String dirArch = dirComb + "/" + sc + sufijo + ".xlt";
				String titulo = "VOLUMENES DE " + sc + " EN MUnidades";
				imprimeCron(datos, nombresTer, dirArch, titulo);
			}
		}
	}

	public void imprimeResUnico(String dirArch) {
		Corrida c = datosResumen.getCorrida();
		StringBuilder agrega = new StringBuilder();
		imprimeCabezal(agrega, c);
		for (StringBuilder sb : resUnico) {
			agrega.append(sb.toString());
			agrega.append("\n\n");
		}
		DirectoriosYArchivos.grabaTexto(dirArch, agrega.toString());
	}
	

	/**
	 * Dado un double[][][] con primer índice año, segundo índice crónica y tercer
	 * índice ítems a imprimir escribe un archivo con el formato del enercron y
	 * costocron
	 * 
	 * @param datos   double[][][] primer índice año, segundo índice crónica y
	 *                tercer índice ítems a imprimir
	 * @param nombres String[] con los nombres de los ítems y sus unidades en el
	 *                mismo orden de datos
	 * @param dirArch nombre del archivo con su extension.
	 * @param titulo  que encabeza el archivo.
	 */
	public void imprimeCron(double[][][] datos, String[] nombres, String dirArch, String titulo) {

		if (DirectoriosYArchivos.existeArchivo(dirArch))
			DirectoriosYArchivos.eliminaArchivo(dirArch);
		StringBuilder sb = new StringBuilder();
		Corrida c = datosResumen.getCorrida();
		imprimeCabezal(sb, c);
		sb.append(titulo + "\n");
		for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
			sb.append("Año ," + (indAnio + anioIni));
			sb.append("\n");
			sb.append("\t");
			for (String nom : nombres) {
				sb.append(nom);
				sb.append("\t");
			}
			sb.append("\n");
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				sb.append(iesc);
				sb.append("\t");
				int ind = 0;
				for (String nom : nombres) {
					sb.append(datos[indAnio][iesc][ind]);
					sb.append("\t");
					ind++;
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
		DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
	}

	/**
	 * Imprime el archivo enercron de energías por crónica y año
	 * 
	 * @param dirArch
	 */
	public void imprimeEnercron(String dirArch) {

		// IMPRIME ENERCRON
		if (param[Constantes.PARAMSAL_ENERCRON][0] == 1) {

			if (DirectoriosYArchivos.existeArchivo(dirArch))
				DirectoriosYArchivos.eliminaArchivo(dirArch);
			StringBuilder sb = new StringBuilder();
			Corrida c = datosResumen.getCorrida();
			imprimeCabezal(sb, c);
			sb.append("ENERGIAS ANUALES POR CRONICAS EN GWh \n");
			for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append("Año ," + (indAnio + anioIni));
				sb.append("\n");
				sb.append("\t");
				for (String nombreRec : listaRec) {
					sb.append(tipoDeRecurso.get(nombreRec));
					sb.append("-");
					sb.append(nombreRec);
					sb.append("\t");
				}
				sb.append("\n");
				for (int iesc = 0; iesc < cantEsc; iesc++) {
					sb.append(iesc);
					sb.append("\t");
					int indiceRec = 0;
					for (String nombreRec : listaRec) {
						sb.append(ener_esc[indiceRec][indAnio][iesc]);
						sb.append("\t");
						indiceRec++;
					}
					sb.append("\n");
				}
				sb.append("\n");
			}
			DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
		}

	}
	
	/**
	 * Imprime el archivo dirArch con las cantidades de arranques promedio y por escenario
	 * de térmicos y ciclos combinados
	 * @param dirArch
	 */
	public void imprimeArranques(String dirArch) {
		if (DirectoriosYArchivos.existeArchivo(dirArch))
			DirectoriosYArchivos.eliminaArchivo(dirArch);
				
		Corrida c = datosResumen.getCorrida();
		StringBuilder sbc = new StringBuilder();
		imprimeCabezal(sbc, c);
		StringBuilder sb = new StringBuilder();
		sb.append("CANTIDAD DE ARRANQUES DE TERMICOS Y CICLOS COMBINADOS (turbinas y ciclos de vapor) \n");
		
		for (String nombreRec : listaRec) {
			Participante p = participanteDeRecurso.get(nombreRec);
			if (p instanceof GeneradorTermico) {
				sb.append("GeneradorTermico\n");
				generaArr1Recurso(sb, nombreRec);							
			}
			if (p instanceof CicloCombinado) {
				sb.append("Ciclo combinado: turbinas a gas\n");
				generaArr1Recurso(sb, nombreRec + "-TG");				
				sb.append("Ciclo combinado: ciclos de vapor\n");
				generaArr1Recurso(sb, nombreRec + "-CV");
				DirectoriosYArchivos.agregaTexto(dirArch, sb.toString());
			}			
		}	
	}
	

	private void generaArr1Recurso(StringBuilder sb, String nombreRec) {


		ArrayList<ParOrdenador> todosEsc = new ArrayList<ParOrdenador>();
		double[][] arranques = new double[cantEsc][CANT_DIAS_MAX_ARRANQUE+1]; // en el último lugar tiene la suma de todas las duraciones
		double[] prom = new double[CANT_DIAS_MAX_ARRANQUE+1];
		double cant = 0;		
		for(int iesc=0; iesc<cantEsc; iesc++) {
			double suma = 0;
			for(int durParada = 0; durParada<CANT_DIAS_MAX_ARRANQUE; durParada++) {
				cant = cantArranquesPorEsc.get(nombreRec + "-" + iesc)[durParada];
				arranques[iesc][durParada] = cant;
				suma += cant;
				prom[durParada] += cant/cantEsc;
			}
			arranques[iesc][CANT_DIAS_MAX_ARRANQUE] = suma;
			prom[CANT_DIAS_MAX_ARRANQUE] += suma/cantEsc;
			ParOrdenador par = new ParOrdenador(suma, arranques[iesc]);
			todosEsc.add(par);
		}
		
		sb.append(nombreRec + "\n\tPromedio\t");
		for(int iesc=0; iesc<cantEsc; iesc++) {
			sb.append("ESC" + iesc + "\t");
		}
		sb.append("\n");
		for(int durParada = 0; durParada<CANT_DIAS_MAX_ARRANQUE; durParada++) {
			if(durParada<CANT_DIAS_MAX_ARRANQUE-1) {
				sb.append("Duracion de parada " + durParada + "\t");
			}else {
				sb.append("Duracion de parada " + durParada + " o mas\t");
			}
			sb.append(prom[durParada]);
			sb.append("\t");
			for(int iesc=0; iesc<cantEsc; iesc++) {
				sb.append(arranques[iesc][durParada] + "\t");
			}
			sb.append("\n");
		}
		sb.append("Todos\t");
		sb.append(prom[CANT_DIAS_MAX_ARRANQUE]);
		sb.append("\t");
		for(int iesc=0; iesc<cantEsc; iesc++) {
			sb.append(arranques[iesc][CANT_DIAS_MAX_ARRANQUE] + "\t");
		}
		
		Collections.sort(todosEsc);

		sb.append(
				"\n\tPromedio\tPor escenarios ordenado en orden decreciente de arranques totales del escenario en la corrida\n");

		for (int durParada = 0; durParada < CANT_DIAS_MAX_ARRANQUE; durParada++) {
			if (durParada < CANT_DIAS_MAX_ARRANQUE - 1) {
				sb.append("Duracion de parada " + durParada + "\t");
			}else {
				sb.append("Duracion de parada " + durParada + " o mas\t");
			}
			sb.append(prom[durParada]);
			sb.append("\t");
			for(int iesc=0; iesc<cantEsc; iesc++) {	
				double[] dd = (double[])todosEsc.get(cantEsc - 1 - iesc).getObjeto();
				sb.append(dd[durParada]);
				sb.append("\t");
			}
			sb.append("\n");
		}
		sb.append("Todos\t");
		sb.append(prom[CANT_DIAS_MAX_ARRANQUE]);
		sb.append("\t");
		for(int iesc=0; iesc<cantEsc; iesc++) {
			double[] dd = (double[])todosEsc.get(cantEsc - 1 - iesc).getObjeto();
			sb.append(dd[CANT_DIAS_MAX_ARRANQUE]);
			sb.append("\t");
		}
		sb.append("\n\n\n");
	}





	/**
	 * Imprime el archivo dirArch con las horas de falla por escenario y la LOLE de
	 * cada falla y con las horas por tipo de falla (ej: ocurre falla 1 y 4 y no
	 * ocurre falla 2 y 3)
	 * 
	 * @param dirArch nombre del archivo de salida con su extensión
	 */

	public void imprimeHorasFallaYLOLE(String dirArch) {

		/**
		 * Clave concatenación de nombre de la falla + "-" + (el percentil o "promedio")
		 * Valor un double[] con un valor por año de horas de falla
		 */
		Hashtable<String, double[]> resumen = new Hashtable<String, double[]>();

		/**
		 * Clave nombre del tipo (combinación) de la falla Valor un double[] con un
		 * valor por año de horas promedio del tipo de falla
		 */
		Hashtable<String, double[]> resumenTipos = new Hashtable<String, double[]>();

		double[] percents = utilitarios.Constantes.PERCENTILES_LOLE;
		int cantPercents = percents.length;

		if (DirectoriosYArchivos.existeArchivo(dirArch))
			DirectoriosYArchivos.eliminaArchivo(dirArch);
		StringBuilder sb = new StringBuilder();

		ArrayList<String> nombresFallas = new ArrayList<String>(); // los nombres de las fallas que incluyen el escalón

		ArrayList<String> tiposFalla = new ArrayList<String>(); // los nombres de los tipos de falla que incluyen la
																// combinación de escalones
		tiposFalla.addAll(horasFallaPorTipo.keySet());
		Collections.sort(tiposFalla);

		int cantFallas = 0;
		for (String nombreRec : listaRec) {
			Participante p = participanteDeRecurso.get(nombreRec);
			if (p instanceof Falla) {
				cantFallas++;
				nombresFallas.add(nombreRec);
			}
			double[] aux = new double[cantAnios];
			String clave = nombreRec + "-promedio";
			resumen.put(clave, aux);
			for (int iper = 0; iper < cantPercents; iper++) {
				clave = nombreRec + "-" + percents[iper];
				aux = new double[cantAnios];
				resumen.put(clave, aux);
			}
		}

		for (String ntipo : tiposFalla) {
			double[] aux = new double[cantAnios];
			resumenTipos.put(ntipo, aux);
		}

		Corrida c = datosResumen.getCorrida();
		imprimeCabezal(sb, c);
		sb.append("HORAS DE FALLA POR ESCENARIO \n");
		for (int indAnio = 0; indAnio < cantAnios; indAnio++) {

//			ArrayList<Double> auxD = new ArrayList<Double>(); 

			sb.append("Anio \t" + (indAnio + anioIni));
			sb.append("\n");
			sb.append("escenario");
			sb.append("\t");

			// clave nombre del recurso, que es de falla, valor horas de falla promedio en
			// los escenarios en el año
			Hashtable<String, Double> horasFallaProm = new Hashtable<String, Double>();

			// Clave nombre del tipo de falla, valor horas de ese tipo de falla promedio en
			// los escenarios en el año
			Hashtable<String, Double> horasTipoFallaProm = new Hashtable<String, Double>();

			for (String nombreRec : nombresFallas) {
				sb.append(nombreRec);
				sb.append("\t");
				horasFallaProm.put(nombreRec, 0.0);
			}
			sb.append("\t");
			for (String ntipo : tiposFalla) {
				sb.append(ntipo);
				sb.append("\t");
				horasTipoFallaProm.put(ntipo, 0.0);
			}
			sb.append("\n");
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				sb.append(iesc);
				sb.append("\t");
				for (String nombreRec : nombresFallas) {
					sb.append(horasFalla.get(nombreRec)[indAnio][iesc]);
					sb.append("\t");
					horasFallaProm.put(nombreRec,
							horasFallaProm.get(nombreRec) + horasFalla.get(nombreRec)[indAnio][iesc] / cantEsc);
				}
				sb.append("\t");

				for (String ntipo : tiposFalla) {
					sb.append(horasFallaPorTipo.get(ntipo)[indAnio][iesc]);
					sb.append("\t");
					horasTipoFallaProm.put(ntipo,
							horasTipoFallaProm.get(ntipo) + horasFallaPorTipo.get(ntipo)[indAnio][iesc] / cantEsc);
				}
				sb.append("\n");
			}
			sb.append("HorasPromedioFalla\t");

			for (String nombreRec : nombresFallas) {
				sb.append(horasFallaProm.get(nombreRec));
				sb.append("\t");
//				auxD.add(horasFallaProm.get(nombreRec));
				String clave = nombreRec + "-promedio";
				double[] aux = resumen.get(clave);
				aux[indAnio] = horasFallaProm.get(nombreRec);
			}
			sb.append("\t");
			for (String ntipo : tiposFalla) {
				sb.append(horasTipoFallaProm.get(ntipo));
				sb.append("\t");
				double[] auxt = resumenTipos.get(ntipo);
				auxt[indAnio] = horasTipoFallaProm.get(ntipo);
			}

			sb.append("\n");

			for (int iper = 0; iper < percents.length; iper++) {
				double per1 = percents[iper];
				ArrayList<Double> horas = new ArrayList<Double>();
				for (String nombreRec : nombresFallas) {
					ArrayList<Double> fallas = new ArrayList<Double>();
					for (int iesc = 0; iesc < cantEsc; iesc++) {
						fallas.add(horasFalla.get(nombreRec)[indAnio][iesc]);
					}
					Collections.sort(fallas); // se ordena en orden ascendente las horas de falla por escenario
					int ordinal = (int) (Math.floor(fallas.size() * (1 - per1))); // se trunca al pasar a entero
					double lole = 0.0;
					for (int i = 0; i <= ordinal; i++) {
						lole += fallas.get(i);
					}
					horas.add(lole / ordinal);
					String clave = nombreRec + "-" + per1;
					double[] aux = resumen.get(clave);
					aux[indAnio] = lole / ordinal;
				}
				sb.append("Horas sin percent. " + per1 + " mayor\t");
				int j = 0;
				for (String nombre : nombresFallas) {
					sb.append(horas.get(j));
					sb.append("\t");
					j++;
				}
				sb.append("\n");
			}

			sb.append("\n");
			DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
		}

		// crea StringBuilder para resumen
		sb = new StringBuilder();
		sb.append("RESUMEN DE HORAS DE FALLA\t");
		for (int ian = 0; ian < cantAnios; ian++) {
			sb.append(ian + anioIni);
			sb.append("\t");
		}
		sb.append("\n");
		for (String nombre : nombresFallas) {
			String clave = nombre + "-promedio";
			double[] aux = resumen.get(clave);
			sb.append(clave + "\t");
			for (int ian = 0; ian < cantAnios; ian++) {
				sb.append(aux[ian]);
				sb.append("\t");
			}
			sb.append("\n");
			for (int iper = 0; iper < cantPercents; iper++) {
				clave = nombre + "-" + percents[iper];
				aux = resumen.get(clave);
				sb.append(clave + "\t");
				for (int ian = 0; ian < cantAnios; ian++) {
					sb.append(aux[ian]);
					sb.append("\t");
				}
				sb.append("\n");
			}
		}

		sb.append("\n");
		sb.append("RESUMEN DE HORAS POR COMBINACIONES (TIPOS) DE FALLA\t");
		for (int ian = 0; ian < cantAnios; ian++) {
			sb.append(ian + anioIni);
			sb.append("\t");
		}
		sb.append("\n");
		for (String ntipo : tiposFalla) {
			double[] auxt = resumenTipos.get(ntipo);
			sb.append(ntipo + "\t");
			for (int ian = 0; ian < cantAnios; ian++) {
				sb.append(auxt[ian]);
				sb.append("\t");
			}
			sb.append("\n");
		}
		resUnico.add(sb);
	}

	/**
	 * Imprime el archivo costoCron con costos por crónica y año por participante
	 * 
	 * @param dirArch
	 */
	private void imprimeCostocron(String dirArch) {
		if (param[Constantes.PARAMSAL_COSTO_CRON][0] == 1) {

			if (DirectoriosYArchivos.existeArchivo(dirArch))
				DirectoriosYArchivos.eliminaArchivo(dirArch);
			StringBuilder sb = new StringBuilder();
			Corrida c = datosResumen.getCorrida();
			imprimeCabezal(sb, c);
			sb.append("COSTOS ANUALES POR CRONICA EN kUSD \n");
			for (int indAnio = 0; indAnio < cantAnios; indAnio++) {
				sb.append("Año ," + (indAnio + anioIni));
				sb.append("\n");
				sb.append("\t");
				for (String nombreRec : listaRec) {
					sb.append(tipoDeRecurso.get(nombreRec));
					sb.append("-");
					sb.append(nombreRec);
					sb.append("\t");
				}
				sb.append("\n");
				for (int iesc = 0; iesc < cantEsc; iesc++) {
					sb.append(iesc);
					sb.append("\t");
					int indiceRec = 0;
					for (String nombreRec : listaRec) {
						sb.append(costo_esc[indiceRec][indAnio][iesc]);
						sb.append("\t");
						indiceRec++;
					}
					sb.append("\n");
				}
				sb.append("\n");
			}
			DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
		}

	}

	/**
	 * Imprime las energías promeio por paso de tiempo para todos los recursos
	 * 
	 * @param archivo
	 */
	private void imprimeEnerPaso(String dirArch) {

		if (DirectoriosYArchivos.existeArchivo(dirArch))
			DirectoriosYArchivos.eliminaArchivo(dirArch);
		StringBuilder sb = new StringBuilder();
		Corrida c = datosResumen.getCorrida();
		imprimeCabezal(sb, c);
		lt.reiniciar();
		cantPasos = lt.getCantidadPasos();

		sb.append("ENERGIAS PROMEDIO POR PASO Y POR RECURSO GWh \n");
		sb.append("\t\t");
		for (String nr : listaRec) {
			sb.append(nr + "\t");

		}
		sb.append("\n");
		for (int indPaso = 0; indPaso < cantPasos; indPaso++) {
			sb.append(indPaso);
			sb.append("\t");
			long instanteIni = lt.devuelvePasoActual().getInstanteInicial();
			sb.append(lt.fechaYHoraDeInstante(instanteIni));
			sb.append("\t");

			for (int ir = 0; ir < cantRec; ir++) {
				sb.append(ener_Paso_MWh[ir][indPaso] + "\t");
			}
			sb.append("\n");

			lt.avanzarPaso();
		}
		DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
	}

	/**
	 * Imprime los costos totales por paso y crónica
	 * 
	 * @param dirArch
	 */
	private void imprimeCostoPasoCron(String dirArch) {
		// IMPRIME COSTOS TOTALES POR PASO Y CRóNICA
		if (param[Constantes.PARAMSAL_COSTO_PASO_CRON][0] == 1) {

			if (DirectoriosYArchivos.existeArchivo(dirArch))
				DirectoriosYArchivos.eliminaArchivo(dirArch);
			StringBuilder sb = new StringBuilder();
			Corrida c = datosResumen.getCorrida();
			imprimeCabezal(sb, c);
			sb.append("COSTOS TOTALES POR PASO Y POR CRONICA EN kUSD \n");
			sb.append("Paso\\Escenario");
			sb.append("\t");
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				sb.append(iesc);
				sb.append("\t");
			}
			sb.append("\n");
			for (int indPaso = 0; indPaso < cantPasos; indPaso++) {
				sb.append(indPaso);
				sb.append("\t");
				for (int iesc = 0; iesc < cantEsc; iesc++) {
					sb.append(cos_paso_cron[indPaso][iesc]);
					sb.append("\t");
				}
				sb.append("\n");
			}
			DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
		}
	}

	/**
	 * Imprime en el directorio dirCosmar varios archivos con costos marginales
	 * 
	 * @param dirCosmar
	 */
	private void imprimeCostosMarginales(String dirCosmar) {

		// IMPRIME COSTOS MARGINALES PROMEDIO EN LOS ESCENARIOS
		if (param[Constantes.PARAMSAL_COSMAR_RESUMEN][0] == 1) {

			if (!DirectoriosYArchivos.existeArchivo(dirCosmar)) {
				DirectoriosYArchivos.creaDirectorio(directorio, "cosmar");
			}

			String dirArch = dirCosmar + "/cosmar_resumen" + sufijo + ".xlt";
			String dirArchCMmedio = dirCosmar + "/cosmar_medio" + sufijo + ".xlt";
			String dirArchCMpasoProm = dirCosmar + "/cosmar_medio_paso" + sufijo + ".xlt";
			;
			if (DirectoriosYArchivos.existeArchivo(dirArch))
				DirectoriosYArchivos.eliminaArchivo(dirArch);
			if (DirectoriosYArchivos.existeArchivo(dirArchCMmedio))
				DirectoriosYArchivos.eliminaArchivo(dirArchCMmedio);
			if (DirectoriosYArchivos.existeArchivo(dirArchCMpasoProm))
				DirectoriosYArchivos.eliminaArchivo(dirArchCMpasoProm);

			Corrida c = datosResumen.getCorrida();
			StringBuilder sb = new StringBuilder();
			imprimeCabezal(sb, c);
			StringBuilder sbCmM = new StringBuilder();
			imprimeCabezal(sbCmM, c);
			StringBuilder sbCmPasoCron = new StringBuilder();
			imprimeCabezal(sbCmPasoCron, c);

			// Crea la tabla de costos marginales promedio por barra y año

			sb.append("Costos marginales promedio en las horas (curva plana) por año\n");
			sbCmM.append("Costos marginales medios\n");
			sbCmPasoCron.append("Costos marginales promedio en el paso por crónica (curva plana) \n");
			sb.append("\t");
			sbCmM.append("\t");
			sbCmPasoCron.append("\t");
			for (int ian = 0; ian < cantAnios; ian++) {
				sb.append(anioIni + ian);
				sb.append("\t");
			}
			sb.append("\n");
			int indBarra = 0;
			for (Barra b : barras) {
				sb.append(b.getNombre());
				sb.append("\t");
				for (int ian = 0; ian < cantAnios; ian++) {
					sb.append(cosmar_prom_anio[indBarra][ian]);
					sb.append("\t");
				}
				indBarra++;
			}

			// Se escribe en el mismo archivo a continuación la tabla de costos marginales
			// promedio por barra, paso y poste.

			sb.append("\n");
			sb.append("\n");
			sb.append("Costos marginales promedio en las horas (curva plana) por paso y poste\n");

			int cantPostes = 0;
			int ipaso = 0;

			lt.reiniciar();
			PasoTiempo pt = lt.devuelvePasoActual();
			while (pt != null) {
				int[] durpos = pt.getBloque().getDuracionPostes();
				int durpaso = pt.getBloque().getDuracionPaso();
				if (pt.getBloque().getCantPostes() != cantPostes) {
					cantPostes = pt.getBloque().getCantPostes();
					sb.append("\t");
					for (Barra b : barras) {
						for (int ip = 0; ip < cantPostes; ip++) {
							sb.append(b.getNombre());
							sb.append("P");
							sb.append(ip);
							sb.append("\t");
						}
					}
					sb.append("\n");
					sbCmM.append("\n");
				}
				sb.append("Paso ");
				sb.append(ipaso);
				sb.append("\t");
				sbCmM.append("Paso ");
				sbCmM.append(ipaso);
				sbCmM.append("\t");
				sbCmPasoCron.append("Paso ");
				sbCmPasoCron.append(ipaso);
				sbCmPasoCron.append("\t");

				cantPostes = pt.getBloque().getCantPostes();
				indBarra = 0;
				for (Barra b : barras) {
					double cMgMedioDelPaso = 0.0;
					for (int ip = 0; ip < cantPostes; ip++) {
						sb.append(cosmar_prom_paso[indBarra][ipaso][ip]);
						sb.append("\t");
						cMgMedioDelPaso += cosmar_prom_paso[indBarra][ipaso][ip] * durpos[ip] / durpaso;
					}
					sbCmM.append(cMgMedioDelPaso);
					indBarra++;
				}
				lt.avanzarPaso();
				sb.append("\n");
				sbCmM.append("\n");
				pt = lt.devuelvePasoActual();
				ipaso++;
			}
			DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
			DirectoriosYArchivos.grabaTexto(dirArchCMmedio, sbCmM.toString());
			DirectoriosYArchivos.grabaTexto(dirArchCMpasoProm, sbCmPasoCron.toString());

		}

		// IMPRIME COSMAR_CRON
		if (param[Constantes.PARAMSAL_COSMAR_CRON][0] == 1) {
			dirCosmar = directorio + "/cosmar";
			if (!DirectoriosYArchivos.existeArchivo(dirCosmar)) {
				DirectoriosYArchivos.creaDirectorio(directorio, "cosmar");
			}
			int cantBloques = lt.getBloques().size();
			for (int iBarra = 0; iBarra < cantBarras; iBarra++) {
				String dirArch = dirCosmar + "/cosmarcron_" + iBarra + ".xlt";
				if (DirectoriosYArchivos.existeArchivo(dirArch))
					DirectoriosYArchivos.eliminaArchivo(dirArch);
				int cantPostesVieja = 0;
				lt.reiniciar();
				cantPasos = lt.getCantidadPasos();
				double[] promEsc = null;
				double[][][] salida1Atr = new double[cantPasos][][]; // base para construir el
																		// DatosSalidaAtributosDetallados de un atributo
				String nombreBarra = barras.get(iBarra).getNombre();
				ClaveDatosAtributosDetallados claveAtr = new ClaveDatosAtributosDetallados(nombreBarra,
						utilitarios.Constantes.COSTOMARGINAL);
				StringBuilder cab = new StringBuilder();
				Corrida c = datosResumen.getCorrida();
				imprimeCabezal(cab, c);
				DirectoriosYArchivos.agregaTexto(dirArch, cab.toString());
				for (int ipaso = 0; ipaso < cantPasos; ipaso++) {
					int cantPostes = lt.devuelvePasoActual().getBloque().getCantPostes();
					promEsc = new double[cantPostes]; // almacena promedio en los escenarios por poste
					if (cantPostes != cantPostesVieja) {
						// crea encabezamiento de escenario y poste
						StringBuilder sb = new StringBuilder();
						sb.append("Paso");
						sb.append("\t");
						sb.append("\t");
						for (int iposte = 0; iposte < cantPostes; iposte++) {
							sb.append("PROMEDIO_ESC");
							if (cantPostes > 1)
								sb.append("-POSTE" + iposte + "\t");
						}
						sb.append("\t");
						for (int iesc = 0; iesc < cantEsc; iesc++) {
							for (int iposte = 0; iposte < cantPostes; iposte++) {
								sb.append("ESC" + iesc);
								if (cantPostes > 1)
									sb.append("-POSTE" + iposte + "\t");
							}
							sb.append("\t");
						}
						DirectoriosYArchivos.agregaTexto(dirArch, sb.toString());
						cantPostesVieja = cantPostes;
					}

					// crea linea de costos marginales por escenario y poste
					StringBuilder sb = new StringBuilder();
					StringBuilder sp = new StringBuilder();
					sp.append(ipaso);
					sp.append("\t");
					long instanteIni = lt.devuelvePasoActual().getInstanteInicial();
					sp.append(lt.fechaYHoraDeInstante(instanteIni));
					sp.append("\t");
					double[][] salida1AtrEscPos = new double[cantEsc][cantPostes];
					for (int iesc = 0; iesc < cantEsc; iesc++) {
						for (int iposte = 0; iposte < cantPostes; iposte++) {
							sb.append(cosmar_cron[iBarra][ipaso][iesc][iposte]);
							if (cantPostes > 1)
								sb.append("\t");
							promEsc[iposte] += cosmar_cron[iBarra][ipaso][iesc][iposte] / cantEsc;
							salida1AtrEscPos[iesc][iposte] = cosmar_cron[iBarra][ipaso][iesc][iposte];
						}
						sb.append("\t");
					}
					salida1Atr[ipaso] = salida1AtrEscPos;

					for (int iposte = 0; iposte < cantPostes; iposte++) {
						sp.append(promEsc[iposte]);
						if (cantPostes > 1)
							sp.append("\t");
					}
					sp.append("\t");
					sp.append(sb.toString());
					DirectoriosYArchivos.agregaTexto(dirArch, sp.toString());
					lt.avanzarPaso();
				}
				DatosSalidaAtributosDetallados datSal1Atr = new DatosSalidaAtributosDetallados();
				datSal1Atr.setAtributos(salida1Atr);
				salidaAtributosDetallados.put(claveAtr, datSal1Atr);
			}
		}
	}

	/**
	 * Escribe los costos marginales por intervalo de muestreo y escenario
	 * 
	 * @param dirCosmar
	 */
	public void imprimeCostosMarginalesIntMuestreo(String dirCosmar) {

		// IMPRIME COSTOS MARGINALES PROMEDIO EN LOS ESCENARIOS
		if (param[Constantes.PARAMSAL_COSMAR_RESUMEN][0] == 1) {

			if (!DirectoriosYArchivos.existeArchivo(dirCosmar)) {
				DirectoriosYArchivos.creaDirectorio(directorio, "cosmar");
			}

			String dirArchIM = dirCosmar + "/cosmar_esc_im.xlt";

			if (DirectoriosYArchivos.existeArchivo(dirArchIM))
				DirectoriosYArchivos.eliminaArchivo(dirArchIM);

			StringBuilder sb = new StringBuilder();
			Corrida c = datosResumen.getCorrida();
			imprimeCabezal(sb, c);

			int indBarra = 0;

			for (Barra b : barras) {

				sb.append("COSTOS MARGINALES POR INTERVALO DE MUESTREO, BARRA " + b.getNombre() + "\n");
				sb.append("FechaInicialPaso\tInt.MuestreoDelPaso\tPROMEDIO\t");
				for (int ie = 0; ie < cantEsc; ie++) {
					sb.append(ie + "\t");
				}
				lt.reiniciar();
				PasoTiempo pt = lt.devuelvePasoActual();
				sb.append("\n");
				while (pt != null) {
					int numPaso = lt.getNumPaso();
					// double[][][][] cosmar_cron_im; // índices barra, paso, escenario, intervalo
					// de muestreo
					int cantIm = cosmar_cron_im[indBarra][numPaso][0].length;
					for (int im = 0; im < cantIm; im++) {
						double prom = 0.0;
						for (int iesc = 0; iesc < cantEsc; iesc++) {
							prom += cosmar_cron_im[indBarra][numPaso][iesc][im] / cantEsc;
						}
						sb.append(lt.fechaDeInstante(pt.getInstanteInicial()) + "\t");
						sb.append(im + "\t");
						sb.append(prom + "\t");
						for (int iesc = 0; iesc < cantEsc; iesc++) {
							sb.append(cosmar_cron_im[indBarra][numPaso][iesc][im] + "\t");
						}
						if(im < cantIm-1) sb.append("\n");
					}
					lt.avanzarPaso();
					pt = lt.devuelvePasoActual();
					DirectoriosYArchivos.agregaTexto(dirArchIM, sb.toString());
					sb = new StringBuilder();
				}
				indBarra++;
			}

		}
	}

	/**
	 * Imprime un archivo con los datos por intervalo de muestreo y escenario a
	 * partir del DatosSalidaAtributosDetallados datosPoste
	 * 
	 * El nombre (incluso directorios) del archivo de salida archIM se forma
	 * agregando el sufijo -IM al nombre del archivo rutaArchPoste
	 * 
	 * @param c             Corrida que debe estar cargada
	 * @param rutaArchPoste el archivo de origen de los datos por poste y escenario
	 * @parem rutaNumpos el archivo que contiene los números de poste, ATENCIÓN, EN
	 *        ESE ARCHIVO LOS POSTES SE NUMERAN A PARTIR DE UNO
	 */
	public static void imprimeDatosDoublePorIntMuestreo(Corrida c, String rutaArchPoste, String rutaNumpos, String rutaArchIM) {

		DatosNumpos dn = DatosNumpos.leeDatosNumpos(rutaNumpos);

		DatosSalidaAtributosDetallados datosPoste = CargadorAtrDetPoste.devuelveDatosAtrDetPoste1LineaPorPoste(rutaArchPoste);

		// primer índice paso, segundo índice escenario, tercer índice poste,
		// eventualmente el único.
		double[][][] dPos = datosPoste.getAtributos();
		int cantEscen = dPos[0].length;

		// primer índice escenario, segundo índice intervalo de muestreo
		ArrayList<ArrayList<Integer>> numpos = dn.getNumpos();

		LineaTiempo lt = c.getLineaTiempo();
		if (DirectoriosYArchivos.existeArchivo(rutaArchIM))
			DirectoriosYArchivos.eliminaArchivo(rutaArchIM);

		StringBuilder sb = new StringBuilder();

		imprimeCabezal(sb, c);
		sb.append(datosPoste.getTitulo());
		DirectoriosYArchivos.agregaTexto(rutaArchIM, sb.toString());
		System.out.println("Comienza impresión de archivo por intervalo de muestreo");
		int iposte;
		sb = new StringBuilder();
		int cantPostesAnt = -1;
		lt.reiniciar();
		PasoTiempo pt = lt.devuelvePasoActual();

		while (pt != null) {
			int cantPostes = pt.getBloque().getCantPostes();
			if(cantPostes!=cantPostesAnt) {
				cantPostesAnt = cantPostes;
				sb.append("CANT_POSTES\t" + cantPostes);
				sb.append("\n");
				sb.append("paso\tFechaInicialPaso\tInt.MuestreoDelPaso\tPROMEDIO\t");
				for (int ie = 0; ie < cantEscen; ie++) {
					sb.append(ie + "\t");
				}	
				sb.append("\n");
			}
			int numPaso = lt.getNumPaso();
			if (numPaso % 50 == 0)
				System.out.println("paso " + numPaso);
			int cantIm = pt.getDuracionPaso() / pt.getIntervaloMuestreo();
			for (int im = 0; im < cantIm; im++) {
				sb.append(numPaso + "\t");
				sb.append(lt.fechaDeInstante(pt.getInstanteInicial()) + "\t");
				sb.append(im + "\t");
				double prom = 0.0;
				for (int iesc = 0; iesc < cantEscen; iesc++) {
					iposte = numpos.get(iesc).get(im);
					double val = dPos[numPaso][iesc][iposte - 1];
					prom += val / cantEscen;
				}
				sb.append(prom + "\t");
				for (int iesc = 0; iesc < cantEscen; iesc++) {
					iposte = numpos.get(iesc).get(im);
					double val = dPos[numPaso][iesc][iposte - 1];
					sb.append(val + "\t");
				}
				if (im < cantIm - 1)
					sb.append("\n");
			}
			lt.avanzarPaso();
			pt = lt.devuelvePasoActual();
			
		}
		DirectoriosYArchivos.agregaTexto(rutaArchIM, sb.toString());
		System.out.println("Termina impresión por intervalo de muestreo");
	}

	/**
	 * Imprime un archivo con la cantidad de módulos disponibles de los Recursos por
	 * paso y escenario
	 * 
	 * @param dirCantModDisp
	 */
	private void imprimeCantModDisp(String dirCantModDisp) {
		// CANTMOD
		if (param[Constantes.PARAMSAL_CANTMOD][0] == 1) {
			/**
			 * Crea un archivo de cantidad de módulos disponibles para cada recurso El
			 * archivo de un recurso tiene una fila por cada paso y una columna por cada
			 * escenario Todos los archivos van al directorio cantMod
			 */

			// Calcula las disponibilidades medias intempestivas de cada recurso en todos
			// los pasos y escenarios
			double[] dispMedia = new double[cantRec];
			double[] tmedioArreglo = new double[cantRec];
			int ipaso;
			int d = 0;
			boolean roto = false;
			for (String s : datosResumen.getListaRecursos()) {
				int indRec = datosResumen.getIndiceDeRecurso().get(s);
				Participante part = participanteDeIndice.get(indRec);
				if ((part instanceof Recurso)) {
					Recurso rec = (Recurso) part;
					int sumaDisp = 0;
					int sumaInstNoProg = 0; // suma de módulos instalados que no están en mantenimiento programado
					for (int iesc = 0; iesc < cantEsc; iesc++) {
						lt.reiniciar();
						ipaso = 0;
						PasoTiempo pt = lt.devuelvePasoActual();
						while (pt != null) {
							long instIni = pt.getInstanteInicial();
							if (rec instanceof CicloCombinado) {
								rec = ((CicloCombinado) rec).getTGs();
							}
							int insNoProg = 0;
							if (rec instanceof ImpoExpo) {
								insNoProg = rec.getCantModInst().getValor(instIni);
							} else {
								insNoProg = rec.getCantModInst().getValor(instIni)
										- rec.getMantProgramado().getValor(instIni);
							}
							sumaDisp += cant_mod_disp[indRec][ipaso][iesc];
							sumaInstNoProg += insNoProg;
							lt.avanzarPaso();
							pt = lt.devuelvePasoActual();
							ipaso++;
						}
					}
					dispMedia[indRec] = (double) sumaDisp / (double) sumaInstNoProg;
				}
			}

			// Crea el directorio para los archivos de disponibilidades

			if (!DirectoriosYArchivos.existeArchivo(dirCantModDisp)) {
				DirectoriosYArchivos.creaDirectorio(directorio, "cantModDisp");
			}

			/*
			 * Crea los archivos y sus encabezamientos
			 */

			// Crea el encabezamiento
			ArrayList<String> archivos = new ArrayList<String>();
			StringBuilder sbEnc = new StringBuilder("Escenario\t");
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				sbEnc.append(iesc);
				sbEnc.append("\t");
			}
			// Crea el archivo y le carga el encabezamiento
			for (String s : datosResumen.getListaRecursos()) {
				/**
				 * Crea un archivo de texto por recurso tórmico e hidróulico (inicialmente,
				 * luego habró que agredar los otros recursos con disponibilidad)
				 */
				String sufijoCC = "";
				if (participanteDeRecurso.get(s) instanceof CicloCombinado)
					sufijoCC = "-turbinas a gas";
				String nomArchDisp = dirCantModDisp + "/" + s + sufijoCC + sufijo + ".xlt";

				archivos.add(nomArchDisp);
				if (DirectoriosYArchivos.existeArchivo(nomArchDisp))
					DirectoriosYArchivos.eliminaArchivo(nomArchDisp);
				DirectoriosYArchivos.agregaTexto(nomArchDisp, "CANTIDAD DE MODULOS DISPONIBLES " + s + "\n");
				int indRec = datosResumen.getIndiceDeRecurso().get(s);

				Participante part = participanteDeIndice.get(indRec);
				String oracion = "";
				if (part instanceof CicloCombinado) {
					Recurso rec = (Recurso) part;
					oracion = "DE LAS TURBINAS A GAS";
				}

				DirectoriosYArchivos.agregaTexto(nomArchDisp,
						"DISPONIBILIDAD MEDIA INTEMPESTIVA RESULTANTE " + oracion + "\t" + dispMedia[indRec] + "\n");
				DirectoriosYArchivos.agregaTexto(nomArchDisp, sbEnc.toString() + "\n");
			}

			int cantPostes = 0;
			ipaso = 0;
			lt.reiniciar();
			PasoTiempo pt = lt.devuelvePasoActual();

			// Recorre los pasos de tiempo
			while (pt != null) {
				if (pt.getBloque().getCantPostes() != cantPostes) {
					cantPostes = pt.getBloque().getCantPostes();
				}
				// Recorre los recursos dado un paso de tiempo
				int indLista = 0;
				for (String s : datosResumen.getListaRecursos()) {
					int indRec = datosResumen.getIndiceDeRecurso().get(s);
					StringBuilder sb = new StringBuilder();
					// Recorre los escenarios dado un paso de tiempo y recurso
					int iesc = 0;
					sb.append("Paso ");
					sb.append(ipaso);
					sb.append("\t");
					for (iesc = 0; iesc < cantEsc; iesc++) {
						sb.append(cant_mod_disp[indRec][ipaso][iesc]);
						sb.append("\t");
					}
					DirectoriosYArchivos.agregaTexto(archivos.get(indLista), sb.toString());
					indLista++;
				}
				lt.avanzarPaso();
				pt = lt.devuelvePasoActual();
				ipaso++;
			}

		}

	}

	/**
	 * Imprime el texto de salida de atributos detallados y la estructura
	 * salidaAtributosDetallados para las salidas gráficas de Rodrigo y Bruno
	 */
	private void imprimeDatosDetallados() {
		int[] indRecDet = param[Constantes.PARAMSAL_IND_ATR_DET];
		NumberFormat formatter = new DecimalFormat("#0.00");

		for (int ip = 0; ip < indRecDet.length; ip++) {
			if (indRecDet[ip] == 1) {

				// Se crea el directorio asociado al recurso

				String nombre_rec = datosResumen.getListaRecursos().get(ip);
				if (nombre_rec.contains("int45")) {
					int pp = 0;
					pp++;
				}
				String tipo = datosResumen.getTipoDeRecurso().get(nombre_rec);
				String tipo_nombre_rec = tipo + "_" + nombre_rec;
				String dirRecSalDet = directorio + "/" + tipo_nombre_rec;
				if (!DirectoriosYArchivos.existeArchivo(dirRecSalDet)) {
					DirectoriosYArchivos.creaDirectorio(directorio, tipo_nombre_rec);
				}
				CorridaHandler ch = CorridaHandler.getInstance();
				Corrida corrida = ch.getCorridaActual();
				// Se imprimen los archivos
				String[] listaNombreAtr = datosResumen.getNombresAtributos().get(nombre_rec);
				int cantAtr = 0;
				if (listaNombreAtr == null)
					cantAtr = listaNombreAtr.length;

				for (int iatr = 0; iatr < cantAtr; iatr++) {
					// Se determina si el atributo fue cargado o si está en null
					// eso puede pasar normalmente si un térmico no tiene combustible alternativo
					Hashtable<String, double[][][]> datosDetalladosPrueba = datosDetalladosTodos.get(0);
					if (nombre_rec.contains("Falla"))
						nombre_rec = tipo_nombre_rec;
					double[][][] dprueba = datosDetalladosPrueba.get(nombre_rec);
					if (dprueba[0][iatr] != null) {

						String nombreAtr = listaNombreAtr[iatr];
						// String nombreRec = datosResumen.getClaveNombreRecurso().get(tipo_nombre_rec);
						String dirArchivo = dirRecSalDet + "/" + nombreAtr + "_" + nombre_rec + "_"
								+ corrida.getNombre() + "_" + corrida.getFechaEjecucion() + "_"
								+ corrida.getHoraEjecucion() + ".xlt";
						StringBuilder cab = new StringBuilder();
						Corrida c = datosResumen.getCorrida();
						imprimeCabezal(cab, c);
						DirectoriosYArchivos.agregaTexto(dirArchivo, cab.toString());
						String nombreUnidad = nombresUnidadesAtributos.get(nombreAtr);
						DirectoriosYArchivos.agregaTexto(dirArchivo,
								nombre_rec + " " + nombreAtr + " [" + nombreUnidad + "] por poste y escenario");
						int cantPostesVieja = 0;
						lt.reiniciar();
						cantPasos = lt.getCantidadPasos();
						double[] promEsc = null;
						int cantSal = 0;
						double[][][] salida1Atr = new double[cantPasos][][]; // base para construir el
																				// DatosSalidaAtributosDetallados de un
																				// atributo
						ClaveDatosAtributosDetallados claveAtr = new ClaveDatosAtributosDetallados(nombre_rec,
								nombreAtr);
						for (int ipaso = 0; ipaso < cantPasos; ipaso++) {
							int cantPostes = lt.devuelvePasoActual().getBloque().getCantPostes();
							promEsc = new double[cantPostes]; // almacena promedio en los escenarios por poste
							if (cantPostes != cantPostesVieja) {
								// crea encabezamiento de escenario y poste
								StringBuilder sb = new StringBuilder();
								sb.append("CANT_POSTES");
								sb.append("\t");
								sb.append(cantPostes);
								sb.append("\t");
								// Se determina cantSal la cantidad de salidas por paso
								Hashtable<String, double[][][]> datosDetallados = datosDetalladosTodos.get(0);
								if (nombre_rec.contains("Falla"))
									nombre_rec = tipo_nombre_rec;
								double[][][] dat = datosDetallados.get(nombre_rec);
								double[] dat2 = dat[ipaso][iatr];
								cantSal = dat2.length;
								for (int iposte = 0; iposte < cantSal; iposte++) {
									sb.append("PROMEDIO_ESC");
									if (cantSal > 1)
										sb.append("-POSTE" + iposte + "\t");
								}
								sb.append("\t");
								for (int iesc = 0; iesc < cantEsc; iesc++) {
									for (int iposte = 0; iposte < cantSal; iposte++) {
										sb.append("ESC" + iesc);
										if (cantSal > 1)
											sb.append("-POSTE" + iposte + "\t");
									}
									sb.append("\t");
								}
								DirectoriosYArchivos.agregaTexto(dirArchivo, sb.toString());
								cantPostesVieja = cantPostes;
							}
							StringBuilder sb = new StringBuilder();
							StringBuilder sp = new StringBuilder();
							sp.append(ipaso);
							sp.append("\t");
							long instanteIni = lt.devuelvePasoActual().getInstanteInicial();
							sp.append(lt.fechaYHoraDeInstante(instanteIni));
							sp.append("\t");
							double[][] salida1AtrEscPos = new double[cantEsc][cantSal];
							for (int iesc = 0; iesc < cantEsc; iesc++) {
								Hashtable<String, double[][][]> d1ht = datosDetalladosTodos.get(iesc);
								double[][][] dat = d1ht.get(nombre_rec);
								double[] dat2 = dat[ipaso][iatr];
								for (int iposte = 0; iposte < dat2.length; iposte++) {
									sb.append(dat2[iposte]);
									if (dat2.length > 1)
										sb.append("\t");
									promEsc[iposte] += dat2[iposte] / cantEsc;
									salida1AtrEscPos[iesc][iposte] = dat2[iposte];
								}
								sb.append("\t");
							}
							salida1Atr[ipaso] = salida1AtrEscPos;
							for (int iposte = 0; iposte < cantSal; iposte++) {
//								sp.append(System.out.println(formatter.format(4.0));
								sp.append(promEsc[iposte]);
								if (cantSal > 1)
									sp.append("\t");
							}
							sp.append("\t");
							sp.append(sb.toString());
							DirectoriosYArchivos.agregaTexto(dirArchivo, sp.toString());
							lt.avanzarPaso();
						}
						DatosSalidaAtributosDetallados datSal1Atr = new DatosSalidaAtributosDetallados();
						datSal1Atr.setAtributos(salida1Atr);
						salidaAtributosDetallados.put(claveAtr, datSal1Atr);
					}
				}
			}
		}
	}

	/**
	 * Imprime el texto de salida de atributos detallados y la estructura
	 * salidaAtributosDetallados para las salidas gráficas de Rodrigo y Bruno
	 */
	private void imprimeDatosDetallados2(boolean unPostePorFila) {
		int[] indRecDet = param[Constantes.PARAMSAL_IND_ATR_DET];
//		NumberFormat formatter = new DecimalFormat("#0.00");     

		for (int ip = 0; ip < indRecDet.length; ip++) {
			if (indRecDet[ip] == 1) {

				// Se crea el directorio asociado al recurso

				String nombre_rec = datosResumen.getListaRecursos().get(ip);
				String tipo = datosResumen.getTipoDeRecurso().get(nombre_rec);
				String tipo_nombre_rec = tipo + "_" + nombre_rec;
				String dirRecSalDet = this.rutaSalidas + "/" + tipo_nombre_rec;
				if (!DirectoriosYArchivos.existeArchivo(dirRecSalDet)) {
					DirectoriosYArchivos.creaDirectorio(this.rutaSalidas, tipo_nombre_rec);
				}
				CorridaHandler ch = CorridaHandler.getInstance();
				Corrida corrida = ch.getCorridaActual();
				// Se imprimen los archivos
				String[] listaNombreAtr = datosResumen.getNombresAtributos().get(nombre_rec);
				int cantAtr;
				if (listaNombreAtr == null)
					cantAtr = 0;
				else
					cantAtr = listaNombreAtr.length;
				for (int iatr = 0; iatr < cantAtr; iatr++) {

					// Se determina si el atributo fue cargado o si está en null
					// eso puede pasar normalmente si un térmico no tiene combustible alternativo
					Hashtable<String, double[][][]> datosDetalladosPrueba = datosDetalladosTodos.get(0);
					if (nombre_rec.contains("Falla"))
						nombre_rec = tipo_nombre_rec;
					double[][][] dprueba = datosDetalladosPrueba.get(nombre_rec);
					if (dprueba[0][iatr] != null) {

						String nombreAtr = listaNombreAtr[iatr];
						// String nombreRec = datosResumen.getClaveNombreRecurso().get(tipo_nombre_rec);
						String dirArchivo = dirRecSalDet + "/" + nombreAtr + "_" + nombre_rec + "_"
								+ corrida.getNombre() + "_" + corrida.getFechaEjecucion() + "_"
								+ corrida.getHoraEjecucion() + ".xlt";
						StringBuilder cab = new StringBuilder();
						Corrida c = datosResumen.getCorrida();
						imprimeCabezal(cab, c);
						DirectoriosYArchivos.agregaTexto(dirArchivo, cab.toString());
						String nombreUnidad = nombresUnidadesAtributos.get(nombreAtr);
						DirectoriosYArchivos.agregaTexto(dirArchivo,
								nombre_rec + " " + nombreAtr + " [" + nombreUnidad + "] por poste y escenario");
						int cantPostesVieja = 0;
						lt.reiniciar();
						cantPasos = lt.getCantidadPasos();

						int cantSal = 0;
						double[][][] salida1Atr = new double[cantPasos][][];// base para construir el
																			// DatosSalidaAtributosDetallados de un
																			// atributo
						ClaveDatosAtributosDetallados claveAtr = new ClaveDatosAtributosDetallados(nombre_rec,
								nombreAtr);
						for (int ipaso = 0; ipaso < cantPasos; ipaso++) {
							StringBuilder sb = new StringBuilder();
							Hashtable<String, double[][][]> datosDetallados = datosDetalladosTodos.get(0);
							if (nombre_rec.contains("Falla"))
								nombre_rec = tipo_nombre_rec;
							double[][][] dat = datosDetallados.get(nombre_rec);
							double[] dat2 = dat[ipaso][iatr];
							cantSal = dat2.length;
							int cantPostes = lt.devuelvePasoActual().getBloque().getCantPostes();
							if (unPostePorFila) {
								creaEncabezadoPoste(dirArchivo, cantPostes, cantPostesVieja, ipaso, iatr, nombre_rec,
										tipo_nombre_rec);
								creaUnPasoDatosDetalladosPoste(dirArchivo, cantSal, ipaso, iatr, nombre_rec,
										tipo_nombre_rec, salida1Atr);
							} else {
								creaEncabezadoPaso(dirArchivo, cantPostes, cantPostesVieja, ipaso, iatr, nombre_rec,
										tipo_nombre_rec);
								creaUnPasoDatosDetalladosPaso(dirArchivo, ipaso, iatr, nombre_rec, tipo_nombre_rec,
										salida1Atr);
							}
							cantPostesVieja = cantPostes;
							double[][] salida1AtrEscPos = new double[cantEsc][cantSal];
							for (int iesc = 0; iesc < cantEsc; iesc++) {
								Hashtable<String, double[][][]> d1ht = datosDetalladosTodos.get(iesc);
								dat = d1ht.get(nombre_rec);
								dat2 = dat[ipaso][iatr];
								for (int iposte = 0; iposte < dat2.length; iposte++) {
									salida1AtrEscPos[iesc][iposte] = dat2[iposte];
								}
							}
							salida1Atr[ipaso] = salida1AtrEscPos;
							lt.avanzarPaso();
						}
						DatosSalidaAtributosDetallados datSal1Atr = new DatosSalidaAtributosDetallados();
						datSal1Atr.setAtributos(salida1Atr);
						salidaAtributosDetallados.put(claveAtr, datSal1Atr);
					}
				}
			}
		}
	}

	public void creaEncabezadoPaso(String dirArchivo, int cantPostes, int cantPostesVieja, int ipaso, int iatr,
			String nombre_rec, String tipo_nombre_rec) {
		StringBuilder sb = new StringBuilder();
		if (cantPostes != cantPostesVieja) {
			// crea encabezamiento de escenario y poste
			sb = new StringBuilder();
			sb.append("CANT_POSTES");
			sb.append("\t");
			sb.append(cantPostes);
			sb.append("\n");
			// Se determina cantSal la cantidad de salidas por paso
			Hashtable<String, double[][][]> datosDetallados = datosDetalladosTodos.get(0);
			if (nombre_rec.contains("Falla"))
				nombre_rec = tipo_nombre_rec;
			double[][][] dat = datosDetallados.get(nombre_rec);
			double[] dat2 = dat[ipaso][iatr];
			int cantSal = dat2.length;
			sb.append("paso\tfecha inicial paso\t");
			for (int iposte = 0; iposte < cantSal; iposte++) {
				sb.append("PROMEDIO_ESC");
				if (cantSal > 1)
					sb.append("-POSTE" + iposte + "\t");
			}
			sb.append("\t");
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				for (int iposte = 0; iposte < cantSal; iposte++) {
					sb.append("ESC" + iesc);
					if (cantSal > 1)
						sb.append("-POSTE" + iposte + "\t");
				}
				sb.append("\t");
			}
			DirectoriosYArchivos.agregaTexto(dirArchivo, sb.toString());
		}
	}

	public void creaEncabezadoPoste(String dirArchivo, int cantPostes, int cantPostesVieja, int ipaso, int iatr,
			String nombre_rec, String tipo_nombre_rec) {
		StringBuilder sb = new StringBuilder();
		if (cantPostes != cantPostesVieja) {
			// crea encabezamiento de escenario y poste
			sb = new StringBuilder();
			sb.append("CANT_POSTES");
			sb.append("\t");
			sb.append(cantPostes);
			sb.append("\n");
			// Se determina cantSal la cantidad de salidas por paso
			Hashtable<String, double[][][]> datosDetallados = datosDetalladosTodos.get(0);
			if (nombre_rec.contains("Falla"))
				nombre_rec = tipo_nombre_rec;
			double[][][] dat = datosDetallados.get(nombre_rec);
			double[] dat2 = dat[ipaso][iatr];
			sb.append("paso");
			sb.append("\t\t");
			sb.append("poste\t");
			sb.append("PROMEDIO_ESC");
			sb.append("\t");
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				sb.append("ESC" + iesc);
				sb.append("\t");
			}
			DirectoriosYArchivos.agregaTexto(dirArchivo, sb.toString());
		}
	}

	/**
	 * Graba en dirArchivo un paso de datos detallados con el formato de un POSTE
	 * por fila y un encabezado si corresponde y devuelve información para
	 * 
	 * @param dirArchivo
	 * @param cantPostes
	 * @param cantPostesVieja
	 * @param ipaso
	 * @param iatr
	 * @param nombre_rec
	 * @param tipo_nombre_rec
	 * @param salida1Atr
	 * @return
	 */
	public void creaUnPasoDatosDetalladosPoste(String dirArchivo, int cantPostes, int ipaso, int iatr,
			String nombre_rec, String tipo_nombre_rec, double[][][] salida1Atr) {

		if (nombre_rec.contains("Falla"))
			nombre_rec = tipo_nombre_rec;
		/**
		 * datosDetalladosTodos guarda los valores de los datos detallados de los
		 * recursos que se seleccionaron. En el ArrayList el índice es el escenario. En
		 * el Hashtable: clave es el tipo_nombre del recurso valor es un double[][][]
		 * primer índice paso, segundo índice atributo con datos detallados, tercer
		 * índice ordinal dentro de los valores del atributo en el paso, típicamente
		 * recorre los postes de un paso
		 */
		StringBuilder sp = new StringBuilder();
		for (int iposte = 0; iposte < cantPostes; iposte++) {
			double[] valores = new double[cantEsc];
			double prom = 0.0;
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				Hashtable<String, double[][][]> datosDetallados = datosDetalladosTodos.get(iesc);
				double valor = datosDetallados.get(nombre_rec)[ipaso][iatr][iposte];
				valores[iesc] = valor;
				prom += valor;
			}
			prom = prom / cantEsc;
			sp.append(ipaso);
			sp.append("\t");
			long instanteIni = lt.devuelvePasoActual().getInstanteInicial();
			sp.append(lt.fechaYHoraDeInstante(instanteIni));
			sp.append("\t");
			if (cantPostes != 1)
				sp.append(iposte);
			sp.append("\t");
			sp.append(prom);
			sp.append("\t");
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				sp.append(valores[iesc]);
				sp.append("\t");
			}
			if (iposte < cantPostes - 1)
				sp.append("\n");
		}
		DirectoriosYArchivos.agregaTexto(dirArchivo, sp.toString());
	}

	/**
	 * Graba en dirArchivo un paso de datos detallados con el formato de un PASO por
	 * fila y un encabezado si corresponde y devuelve información para
	 * 
	 * @param dirArchivo
	 * @param cantPostes
	 * @param cantPostesVieja
	 * @param ipaso
	 * @param iatr
	 * @param nombre_rec
	 * @param tipo_nombre_rec
	 * @param salida1Atr
	 * @return
	 */
	public void creaUnPasoDatosDetalladosPaso(String dirArchivo, int ipaso, int iatr, String nombre_rec,
			String tipo_nombre_rec, double[][][] salida1Atr) {
		Hashtable<String, double[][][]> datosDetallados = datosDetalladosTodos.get(0);
		if (nombre_rec.contains("Falla"))
			nombre_rec = tipo_nombre_rec;
		double[][][] dat = datosDetallados.get(nombre_rec);
		double[] dat2 = dat[ipaso][iatr];
		int cantSal = dat2.length;
		StringBuilder sb = new StringBuilder();
		StringBuilder sp = new StringBuilder();
		sp.append(ipaso);
		sp.append("\t");
		long instanteIni = lt.devuelvePasoActual().getInstanteInicial();
		sp.append(lt.fechaYHoraDeInstante(instanteIni));
		sp.append("\t");
		double[] promEsc = new double[cantSal];
		for (int iesc = 0; iesc < cantEsc; iesc++) {
			Hashtable<String, double[][][]> d1ht = datosDetalladosTodos.get(iesc);
			dat = d1ht.get(nombre_rec);
			dat2 = dat[ipaso][iatr];
			for (int iposte = 0; iposte < cantSal; iposte++) {
				sb.append(dat2[iposte]);
				if (dat2.length > 1)
					sb.append("\t");
				promEsc[iposte] += dat2[iposte] / cantEsc;
			}
			sb.append("\t");
		}
		for (int iposte = 0; iposte < cantSal; iposte++) {
			sp.append(promEsc[iposte]);
			if (cantSal > 1)
				sp.append("\t");
		}
		sp.append("\t");
		sp.append(sb.toString());
		DirectoriosYArchivos.agregaTexto(dirArchivo, sp.toString());
	}

	/**
	 * Recorre los DatosSalidaAtributosDetallados de salidasAtributosDetallados y
	 * los completa agregando el array ordenado por valores del índice de escenarios
	 */
	public void calculaAtributosPercentilesYMedia() {
		System.out.println("Comienza ordenación para percentiles y cálculo de medias");
		ArrayList<DatosSalidaAtributosDetallados> arr = new ArrayList<DatosSalidaAtributosDetallados>(
				salidaAtributosDetallados.values());
		for (DatosSalidaAtributosDetallados dat : arr) {
			double[][][] at = dat.getAtributos();
			double[][][] atPerc = DatosSalidaAtributosDetallados.ordenaValorCrecienteD2(at);
			dat.setAtributosPerc(atPerc);
			double[][] medias = DatosSalidaAtributosDetallados.calculaMediaD2(at);
			dat.setMedias(medias);
		}
		System.out.println("Terminó ordenación para percentiles y cálculo de medias");
	}

	/**
	 * Encuentra en dirResSim un archivo con las serialización de los resultados de
	 * un escenario Si no hay ninguno devuelve null
	 * 
	 * @param dirResSim
	 * @return
	 */

	private DatosEPPUnEscenario devuelveResUnEscenario(DatosEPPResumen datosResumen, String dirResSim,
			boolean paralelo) {
		CorridaHandler ch = CorridaHandler.getInstance();

		if (!paralelo) {
			if (indiceEscenario == ch.getCorridaActual().getCantEscenarios() + 1) {
				return null;
			} else {
				File directorios = new File(dirResSim + "/" + utilitarios.Constantes.DIR_SERIALIZADOS);
				if (!directorios.exists()) {
					if (directorios.mkdirs()) {

					} else {
						System.out.println("Error al crear directorios");

					}
				}
				Object o = ManejaObjetosEnDisco.traerDeDisco(dirResSim + "/" + utilitarios.Constantes.DIR_SERIALIZADOS
						+ "/escenario" + Integer.toString(indiceEscenario));

				while (o == null) {
					o = ManejaObjetosEnDisco.traerDeDisco(dirResSim + "/" + utilitarios.Constantes.DIR_SERIALIZADOS
							+ "/escenario" + Integer.toString(indiceEscenario));
				}
				indiceEscenario++;
				return (DatosEPPUnEscenario) o;
			}
		} else {
			DatosEPPUnEscenario desc = null;
			if (indiceEscenario == ch.getCorridaActual().getCantEscenarios() + 1) {
				return null;
			} else {

				PizarronRedis pp = PizarronRedis.getInstance();
				desc = (DatosEPPUnEscenario) pp.levantarEscenario(indiceEscenario);

//				Object o = ManejaObjetosEnDisco.traerDeDisco(
//						dirResSim + "\\escenario" + Integer.toString(indiceEscenario));
//				while (o == null) {
//					o = ManejaObjetosEnDisco.traerDeDisco(
//							dirResSim + "\\escenario" + Integer.toString(indiceEscenario));
//				}
				// desc = (DatosEPPUnEscenario) o;
				if (Constantes.NIVEL_CONSOLA > 1)
					System.out.println(desc.getNumeroEsc());
				indiceEscenario++;
			}
			return desc; // indiceEscenario va de 1 a cantidad de escenarios
		}
	}

	public void pruebaSalidaAtributosDetallados() {
		String nombreRecurso = "barraUnica";
		String nombreAtributo = utilitarios.Constantes.COSTOMARGINAL;
		ClaveDatosAtributosDetallados clave = new ClaveDatosAtributosDetallados(nombreRecurso, nombreAtributo);
		int ipaso = 20;
		DatosSalidaAtributosDetallados dat = salidaAtributosDetallados.get(clave);
		double[] medias = dat.getMedias()[ipaso];
		System.out.println(medias[0] + "\t" + medias[1] + "\t" + medias[2] + "\t" + medias[3] + "\t");
		double sumaEner = 0;
		double sumaCost = 0;
		String tipo = utilitarios.Constantes.HID;
		double[] energias = energiaDeTipoPorPasoGWh.get(tipo);
		double[] costos = costoDeTipoPorPasoMUSD.get(tipo);
		for (int paso = 0; paso < cantPasos; paso++) {
			sumaEner += energias[paso];
			sumaCost += costos[paso];
		}
		System.out.println("tipo = " + tipo + " energia=" + sumaEner);
		System.out.println("tipo = " + tipo + " costo=" + sumaCost);
	}

	public DatosEPPUnEscenario dameEscenarioSerializado(int esc) {

		Object o = ManejaObjetosEnDisco.traerDeDisco(this.getRutaSalidas() + "/"
				+ utilitarios.Constantes.DIR_SERIALIZADOS + "/" + Integer.toString(indiceEscenario));
		while (o == null) {
			o = ManejaObjetosEnDisco
					.traerDeDisco(this.getRutaSalidas() + "\\escenario" + Integer.toString(indiceEscenario));
		}
		return (DatosEPPUnEscenario) o;

	}

	public void imprimeVariables(String dirArch) {
		if (this.getDatosResumen().getCorrida().isCostosVariables()) {
			if (DirectoriosYArchivos.existeArchivo(dirArch))
				DirectoriosYArchivos.eliminaArchivo(dirArch);
			StringBuilder sb = new StringBuilder();
			Corrida c = datosResumen.getCorrida();
			imprimeCabezal(sb, c);
			lt.reiniciar();
			cantPasos = lt.getCantidadPasos();
			sb.append("COSTOS VARIABLES POR ESCENARIO EN USD/MWh \n");
			DatosEPPUnEscenario deppunesc = this.dameEscenarioSerializado(0);
			DatosCurvaOferta dco = deppunesc.getCurvOfertas().get(0);
			ArrayList<String> recursos = new ArrayList<String>(dco.getVariables().keySet());
			Collections.sort(recursos);
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				sb.append("\t");
				sb.append("E" + Integer.toString(iesc));
				sb.append("\t");
				for (String nombreRec : recursos) {
					sb.append(nombreRec);
					sb.append("\t");
				}
				sb.append("\t");
			}

			for (int indPaso = 1; indPaso < cantPasos + 1; indPaso++) {
				for (int ipos = 0; ipos < this.getDatosResumen().getCorrida().getCantidadPostes(); ipos++) {
					for (int iesc = 0; iesc < cantEsc; iesc++) {
						sb.append(indPaso);
						sb.append("\t");
						long instanteIni = lt.devuelvePasoActual().getInstanteInicial();
						sb.append(lt.fechaYHoraDeInstante(instanteIni));
						sb.append("\t");
						deppunesc = this.dameEscenarioSerializado(iesc);
						dco = deppunesc.getCurvOfertas().get(indPaso - 1);
						for (String nombreRec : recursos) {
							if (dco.getVariables() != null) {
								if (dco.getVariables().containsKey(nombreRec)) {
									sb.append(dco.getVariables().get(nombreRec).get(ipos));
								} else {
									sb.append(0.0);
								}
							}
							sb.append("\t");
						}
						sb.append("\t");
					}
					sb.append("\n");
				}
				lt.avanzarPaso();
			}
			DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
		}

	}

	public void imprimePotsDisp(String dirArch) {
		if (this.getDatosResumen().getCorrida().isCostosVariables()) {
			if (DirectoriosYArchivos.existeArchivo(dirArch))
				DirectoriosYArchivos.eliminaArchivo(dirArch);
			StringBuilder sb = new StringBuilder();
			Corrida c = datosResumen.getCorrida();
			imprimeCabezal(sb, c);
			lt.reiniciar();
			cantPasos = lt.getCantidadPasos();
			sb.append("POTENCIAS DISPONIBLES POR ESCENARIO EN MW \n");
			DatosEPPUnEscenario deppunesc = this.dameEscenarioSerializado(0);
			DatosCurvaOferta dco = deppunesc.getCurvOfertas().get(0);
			ArrayList<String> recursos = new ArrayList<String>(dco.getVariables().keySet());
			Collections.sort(recursos);
			for (int iesc = 0; iesc < cantEsc; iesc++) {
				sb.append("\t");
				sb.append("E" + Integer.toString(iesc));
				sb.append("\t");
				for (String nombreRec : recursos) {
					sb.append(nombreRec);
					sb.append("\t");
				}
				sb.append("\t");
			}

			for (int indPaso = 1; indPaso < cantPasos + 1; indPaso++) {
				for (int ipos = 0; ipos < this.getDatosResumen().getCorrida().getCantidadPostes(); ipos++) {
					for (int iesc = 0; iesc < cantEsc; iesc++) {
						sb.append(indPaso);
						sb.append("\t");
						long instanteIni = lt.devuelvePasoActual().getInstanteInicial();
						sb.append(lt.fechaYHoraDeInstante(instanteIni));
						sb.append("\t");
						// DatosEPPUnEscenario d1esc = devuelveResUnEscenario(datosResumen, dirResSim,
						// paralelo);
						deppunesc = this.dameEscenarioSerializado(iesc);
						dco = deppunesc.getCurvOfertas().get(indPaso - 1);
						for (String nombreRec : recursos) {
							if (dco.getVariables() != null) {
								if (dco.getPotenciasDisp().containsKey(nombreRec)) {
									sb.append(dco.getPotenciasDisp().get(nombreRec).get(ipos));
								} else {
									sb.append(0.0);
								}
							}

							sb.append("\t");
						}
						sb.append("\t");
					}
					sb.append("\n");
				}
				lt.avanzarPaso();
			}
			DirectoriosYArchivos.grabaTexto(dirArch, sb.toString());
		}

	}

	public ArrayList<String> getListaRec() {
		return listaRec;
	}

	public void setListaRec(ArrayList<String> listaRec) {
		this.listaRec = listaRec;
	}

	public Hashtable<String, Integer> getIndiceDeRecurso() {
		return indiceDeRecurso;
	}

	public void setIndiceDeRecurso(Hashtable<String, Integer> indiceDeRecurso) {
		this.indiceDeRecurso = indiceDeRecurso;
	}

	public Hashtable<String, Participante> getParticipanteDeRecurso() {
		return participanteDeRecurso;
	}

	public void setParticipanteDeRecurso(Hashtable<String, Participante> participanteDeRecurso) {
		this.participanteDeRecurso = participanteDeRecurso;
	}

	public Hashtable<Integer, Participante> getParticipanteDeIndice() {
		return participanteDeIndice;
	}

	public void setParticipanteDeIndice(Hashtable<Integer, Participante> participanteDeIndice) {
		this.participanteDeIndice = participanteDeIndice;
	}

	public Hashtable<String, String> getTipoDeRecurso() {
		return tipoDeRecurso;
	}

	public void setTipoDeRecurso(Hashtable<String, String> tipoDeRecurso) {
		this.tipoDeRecurso = tipoDeRecurso;
	}

	public Hashtable<String, double[]> getEnergiaDeTipoPorPasoGWh() {
		return energiaDeTipoPorPasoGWh;
	}

	public void setEnergiaDeTipoPorPasoGWh(Hashtable<String, double[]> energiaDeTipoPorPasoGWh) {
		this.energiaDeTipoPorPasoGWh = energiaDeTipoPorPasoGWh;
	}

	public Hashtable<String, double[]> getCostoDeTipoPorPasoMUSD() {
		return costoDeTipoPorPasoMUSD;
	}

	public void setCostoDeTipoPorPasoMUSD(Hashtable<String, double[]> costoDeTipoPorPasoMUSD) {
		this.costoDeTipoPorPasoMUSD = costoDeTipoPorPasoMUSD;
	}

	public Hashtable<ClaveDatosAtributosDetallados, DatosSalidaAtributosDetallados> getSalidaAtributosDetallados() {
		return salidaAtributosDetallados;
	}

	public void setSalidaAtributosDetallados(
			Hashtable<ClaveDatosAtributosDetallados, DatosSalidaAtributosDetallados> salidaAtributosDetallados) {
		this.salidaAtributosDetallados = salidaAtributosDetallados;
	}

	public Hashtable<String, String> getNombresUnidadesAtributos() {
		return nombresUnidadesAtributos;
	}

	public void setNombresUnidadesAtributos(Hashtable<String, String> nombresUnidadesAtributos) {
		this.nombresUnidadesAtributos = nombresUnidadesAtributos;
	}

	public double[][] getEner_resumen() {
		return ener_resumen_GWh;
	}

	public void setEner_resumen(double[][] ener_resumen_GWh) {
		this.ener_resumen_GWh = ener_resumen_GWh;
	}

	public double[][] getCos_resumen_MUSD() {
		return cos_resumen_MUSD;
	}

	public void setCos_resumen(double[][] cos_resumen_MUSD) {
		this.cos_resumen_MUSD = cos_resumen_MUSD;
	}

	public double[][][] getGradGestion_resumen() {
		return gradGestion_resumen;
	}

	public void setGradGestion_resumen(double[][][] gradGestion_resumen) {
		this.gradGestion_resumen = gradGestion_resumen;
	}

	public double[] getCos_tot_medio() {
		return cos_tot_medio;
	}

	public void setCos_tot_medio(double[] cos_tot_medio) {
		this.cos_tot_medio = cos_tot_medio;
	}

	public double[][][] getEner_esc() {
		return ener_esc;
	}

	public void setEner_esc(double[][][] ener_esc) {
		this.ener_esc = ener_esc;
	}

	public double[][][] getCosto_esc() {
		return costo_esc;
	}

	public void setCosto_esc(double[][][] costo_esc) {
		this.costo_esc = costo_esc;
	}

	public double[][] getCosmar_prom_anio() {
		return cosmar_prom_anio;
	}

	public void setCosmar_prom_anio(double[][] cosmar_prom_anio) {
		this.cosmar_prom_anio = cosmar_prom_anio;
	}

	public double[][][] getCosmar_prom_anio_auxEsc() {
		return cosmar_prom_anio_auxEsc;
	}

	public void setCosmar_prom_anio_auxEsc(double[][][] cosmar_prom_anio_auxEsc) {
		this.cosmar_prom_anio_auxEsc = cosmar_prom_anio_auxEsc;
	}

	public double[][][] getCosmar_prom_paso() {
		return cosmar_prom_paso;
	}

	public void setCosmar_prom_paso(double[][][] cosmar_prom_paso) {
		this.cosmar_prom_paso = cosmar_prom_paso;
	}

	public double[][][] getCosmar_prom_pasoEsc() {
		return cosmar_prom_pasoEsc;
	}

	public void setCosmar_prom_pasoEsc(double[][][] cosmar_prom_pasoEsc) {
		this.cosmar_prom_pasoEsc = cosmar_prom_pasoEsc;
	}

	public double[][][][] getCosmar_cron() {
		return cosmar_cron;
	}

	public void setCosmar_cron(double[][][][] cosmar_cron) {
		this.cosmar_cron = cosmar_cron;
	}

	public double[][] getCos_paso_cron() {
		return cos_paso_cron;
	}

	public void setCos_paso_cron(double[][] cos_paso_cron) {
		this.cos_paso_cron = cos_paso_cron;
	}

	public int[][][] getCant_mod_disp() {
		return cant_mod_disp;
	}

	public void setCant_mod_disp(int[][][] cant_mod_disp) {
		this.cant_mod_disp = cant_mod_disp;
	}

	public Hashtable<String, double[]> getTurbinadoAnualHm3() {
		return turbinadoAnualHm3;
	}

	public void setTurbinadoAnualHm3(Hashtable<String, double[]> turbinadoAnualHm3) {
		this.turbinadoAnualHm3 = turbinadoAnualHm3;
	}

	public Hashtable<String, double[]> getVertidoAnualHm3() {
		return vertidoAnualHm3;
	}

	public void setVertidoAnualHm3(Hashtable<String, double[]> vertidoAnualHm3) {
		this.vertidoAnualHm3 = vertidoAnualHm3;
	}

	public DatosEPPResumen getDatosResumen() {
		return datosResumen;
	}

	public void setDatosResumen(DatosEPPResumen datosResumen) {
		this.datosResumen = datosResumen;
	}

	public int getCantRec() {
		return cantRec;
	}

	public void setCantRec(int cantRec) {
		this.cantRec = cantRec;
	}

	public int getCantAnios() {
		return cantAnios;
	}

	public void setCantAnios(int cantAnios) {
		this.cantAnios = cantAnios;
	}

	public int getCantEsc() {
		return cantEsc;
	}

	public void setCantEsc(int cantEsc) {
		this.cantEsc = cantEsc;
	}

	public int getCantBarras() {
		return cantBarras;
	}

	public void setCantBarras(int cantBarras) {
		this.cantBarras = cantBarras;
	}

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

	public int[][] getParam() {
		return param;
	}

	public void setParam(int[][] param) {
		this.param = param;
	}

	public int getAnioIni() {
		return anioIni;
	}

	public void setAnioIni(int anioIni) {
		this.anioIni = anioIni;
	}

	public int[] getDurAnios() {
		return durAnios;
	}

	public void setDurAnios(int[] durAnios) {
		this.durAnios = durAnios;
	}

	public long[] getInstInicAnios() {
		return instInicAnios;
	}

	public void setInstInicAnios(long[] instInicAnios) {
		this.instInicAnios = instInicAnios;
	}

	public String getDirectorio() {
		return directorio;
	}

	public void setDirectorio(String directorio) {
		this.directorio = directorio;
	}

	public ArrayList<Barra> getBarras() {
		return barras;
	}

	public void setBarras(ArrayList<Barra> barras) {
		this.barras = barras;
	}

	public LineaTiempo getLt() {
		return lt;
	}

	public void setLt(LineaTiempo lt) {
		this.lt = lt;
	}

	public ArrayList<Hashtable<String, double[][][]>> getDatosDetalladosTodos() {
		return datosDetalladosTodos;
	}

	public void setDatosDetalladosTodos(ArrayList<Hashtable<String, double[][][]>> datosDetalladosTodos) {
		this.datosDetalladosTodos = datosDetalladosTodos;
	}

	public int getIndiceEscenario() {
		return indiceEscenario;
	}

	public void setIndiceEscenario(int indiceEscenario) {
		this.indiceEscenario = indiceEscenario;
	}

	public String tipoNombre(String nombre) {
		return this.tipoDeRecurso.get(nombre);
	}

	public int indNombre(String nombre) {
		return this.indiceDeRecurso.get(nombre);
	}

	public String tipoIndice(int indice) {
		return tipoNombre(nombreIndice(indice));
	}

	public String nombreIndice(int ind) {
		return this.listaRec.get(ind);
	}

	public static int getCantLineasCabezal() {
		return CANT_LINEAS_CABEZAL;
	}

	public void setListaTiposRec(ArrayList<String> listaTiposRec) {
		this.listaTiposRec = listaTiposRec;
	}

	public Hashtable<String, Integer> getIndiceBarraDeRecurso() {
		return indiceBarraDeRecurso;
	}

	public void setIndiceBarraDeRecurso(Hashtable<String, Integer> indiceBarraDeRecurso) {
		this.indiceBarraDeRecurso = indiceBarraDeRecurso;
	}

	public Hashtable<String, double[]> getEnergiaDeTipoPorAnioGWh() {
		return energiaDeTipoPorAnioGWh;
	}

	public void setEnergiaDeTipoPorAnioGWh(Hashtable<String, double[]> energiaDeTipoPorAnioGWh) {
		this.energiaDeTipoPorAnioGWh = energiaDeTipoPorAnioGWh;
	}

	public Hashtable<String, double[]> getCostoDeTipoPorAnioMUSD() {
		return costoDeTipoPorAnioMUSD;
	}

	public void setCostoDeTipoPorAnioMUSD(Hashtable<String, double[]> costoDeTipoPorAnioMUSD) {
		this.costoDeTipoPorAnioMUSD = costoDeTipoPorAnioMUSD;
	}

	public double[][] getEner_resumen_GWh() {
		return ener_resumen_GWh;
	}

	public void setEner_resumen_GWh(double[][] ener_resumen_GWh) {
		this.ener_resumen_GWh = ener_resumen_GWh;
	}

	public double[][] getIngmar_resumen_USD() {
		return ingmar_resumen_USD;
	}

	public void setIngmar_resumen_USD(double[][] ingmar_resumen_USD) {
		this.ingmar_resumen_USD = ingmar_resumen_USD;
	}

	public double[][] getIngmar_resumen_tope_USD() {
		return ingmar_resumen_tope_USD;
	}

	public void setIngmar_resumen_tope_USD(double[][] ingmar_resumen_tope_USD) {
		this.ingmar_resumen_tope_USD = ingmar_resumen_tope_USD;
	}

	public double[][] getEner_Paso_MWh() {
		return ener_Paso_MWh;
	}

	public void setEner_Paso_MWh(double[][] ener_Paso_MWh) {
		this.ener_Paso_MWh = ener_Paso_MWh;
	}

	public double[][][] getIngmar_esc() {
		return ingmar_esc;
	}

	public void setIngmar_esc(double[][][] ingmar_esc) {
		this.ingmar_esc = ingmar_esc;
	}

	public double[][] getSpot_prom_anio() {
		return spot_prom_anio;
	}

	public void setSpot_prom_anio(double[][] spot_prom_anio) {
		this.spot_prom_anio = spot_prom_anio;
	}

	public double[][][] getSpot_prom_anio_auxEsc() {
		return spot_prom_anio_auxEsc;
	}

	public void setSpot_prom_anio_auxEsc(double[][][] spot_prom_anio_auxEsc) {
		this.spot_prom_anio_auxEsc = spot_prom_anio_auxEsc;
	}

	public double[][][][] getCosmar_cron_im() {
		return cosmar_cron_im;
	}

	public void setCosmar_cron_im(double[][][][] cosmar_cron_im) {
		this.cosmar_cron_im = cosmar_cron_im;
	}

	public String getArchNumpos() {
		return archNumpos;
	}

	public void setArchNumpos(String archNumpos) {
		this.archNumpos = archNumpos;
	}

	public String getSufijo() {
		return sufijo;
	}

	public void setSufijo(String sufijo) {
		this.sufijo = sufijo;
	}

	public Hashtable<String, double[][]> getEnergiasComb_resumen() {
		return energiasComb_resumen;
	}

	public void setEnergiasComb_resumen(Hashtable<String, double[][]> energiasComb_resumen) {
		this.energiasComb_resumen = energiasComb_resumen;
	}

	public Hashtable<String, double[][]> getVolumenesComb_resumen() {
		return volumenesComb_resumen;
	}

	public void setVolumenesComb_resumen(Hashtable<String, double[][]> volumenesComb_resumen) {
		this.volumenesComb_resumen = volumenesComb_resumen;
	}

	public Hashtable<String, double[][]> getCostosComb_resumen() {
		return costosComb_resumen;
	}

	public void setCostosComb_resumen(Hashtable<String, double[][]> costosComb_resumen) {
		this.costosComb_resumen = costosComb_resumen;
	}

	public Hashtable<String, double[][][]> getEnerComb_esc() {
		return enerComb_esc;
	}

	public void setEnerComb_esc(Hashtable<String, double[][][]> enerComb_esc) {
		this.enerComb_esc = enerComb_esc;
	}

	public Hashtable<String, double[][][]> getVolComb_esc() {
		return volComb_esc;
	}

	public void setVolComb_esc(Hashtable<String, double[][][]> volComb_esc) {
		this.volComb_esc = volComb_esc;
	}

	public Hashtable<String, double[][][]> getCosComb_esc() {
		return cosComb_esc;
	}

	public void setCosComb_esc(Hashtable<String, double[][][]> cosComb_esc) {
		this.cosComb_esc = cosComb_esc;
	}

	public Hashtable<String, double[]> getEnerSemGWh() {
		return enerSemGWh;
	}

	public void setEnerSemGWh(Hashtable<String, double[]> enerSemGWh) {
		this.enerSemGWh = enerSemGWh;
	}

	public Hashtable<String, double[]> getEnergiaTGPorAnioGWh() {
		return energiaTGPorAnioGWh;
	}

	public void setEnergiaTGPorAnioGWh(Hashtable<String, double[]> energiaTGPorAnioGWh) {
		this.energiaTGPorAnioGWh = energiaTGPorAnioGWh;
	}

	public Hashtable<String, double[]> getEnergiaCVPorAnioGWh() {
		return energiaCVPorAnioGWh;
	}

	public void setEnergiaCVPorAnioGWh(Hashtable<String, double[]> energiaCVPorAnioGWh) {
		this.energiaCVPorAnioGWh = energiaCVPorAnioGWh;
	}

	public Hashtable<String, double[]> getEnergiaAbPorAnioGWh() {
		return energiaAbPorAnioGWh;
	}

	public void setEnergiaAbPorAnioGWh(Hashtable<String, double[]> energiaAbPorAnioGWh) {
		this.energiaAbPorAnioGWh = energiaAbPorAnioGWh;
	}

	public Hashtable<String, double[]> getEnergiaCombPorAnioGWh() {
		return energiaCombPorAnioGWh;
	}

	public void setEnergiaCombPorAnioGWh(Hashtable<String, double[]> energiaCombPorAnioGWh) {
		this.energiaCombPorAnioGWh = energiaCombPorAnioGWh;
	}

	public Hashtable<String, double[][]> getHorasFalla() {
		return horasFalla;
	}

	public void setHorasFalla(Hashtable<String, double[][]> horasFalla) {
		this.horasFalla = horasFalla;
	}

	public Hashtable<String, double[][]> getHorasFallaPorTipo() {
		return horasFallaPorTipo;
	}

	public void setHorasFallaPorTipo(Hashtable<String, double[][]> horasFallaPorTipo) {
		this.horasFallaPorTipo = horasFallaPorTipo;
	}

	public static int getCANT_LINEAS_CABEZAL() {
		return CANT_LINEAS_CABEZAL;
	}

	public static void setCANT_LINEAS_CABEZAL(int cANT_LINEAS_CABEZAL) {
		CANT_LINEAS_CABEZAL = cANT_LINEAS_CABEZAL;
	}

	public ArrayList<StringBuilder> getResUnico() {
		return resUnico;
	}

	public void setResUnico(ArrayList<StringBuilder> resUnico) {
		this.resUnico = resUnico;
	}

	public String getRutaSalidas() {
		return rutaSalidas;
	}

	public void setRutaSalidas(String rutaSalidas) {
		this.rutaSalidas = rutaSalidas;
	}

	public void setCos_resumen_MUSD(double[][] cos_resumen_MUSD) {
		this.cos_resumen_MUSD = cos_resumen_MUSD;
	}

}
