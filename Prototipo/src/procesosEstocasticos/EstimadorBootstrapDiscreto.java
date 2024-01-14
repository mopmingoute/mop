/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimadorBootstrapDiscreto is part of MOP.
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

package procesosEstocasticos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import logica.CorridaHandler;
import persistencia.EscritorTextosGeneralesPE;
import pizarron.PizarronRedis;
import utilitarios.*;

import static persistencia.EscritorTextosGeneralesPE.escribeDatosGeneralesPE;

/**
 * Estima procesos BootstrapDiscretos
 * 
 */
public class EstimadorBootstrapDiscreto {

	/**
	 * ATENCION: En esta clase se emplea la palabra hora como el intervalo para el
	 * que hay un valor y la palabra día como el conjunto de horas que se muestrean
	 * todas juntas sucesivamente. Supongamos que se muestrean días de 24 horas, con
	 * un valor por hora. En el lenguaje de la clase ProcesoEstocastico, el paso del
	 * PE sería la hora.
	 * 
	 * Si se muestreasen días con 48 medias horas, la "hora" sería la media hora
	 */

	private String nombreProceso;
	private String archDatos; // dirección del archivo con los datos
	private int cantVar; // cantidad de variables de datos
	private int cantHoras; // cantidad de horas por día
	private int durHora; // duración de la hora en segundos (atención puede ser 1800 si la "hora" es
							// media hora)
	private String nombrePasoPE; // nombre del paso del Proceso estoc�stico: típicamente "HORA"
	private int cantMaxDias; // cantidad máxima de días que puede tener un año
	private int anchoVentana; // cantidad de dias hacia adelante y hacia atr�s que se incluyen en la población
								// de un paso dado
	private int cantDatos; // cantidad de horas leidas con datos
	private int cantDiasDatos; // cantidad de días en los datos hist�ricos le�dos
	private ArrayList<String> nombresVar; // nombres de las variables de datos

	/**
	 * Nombre de la variable de estado compuesto del PE a crear La variable de
	 * estado compuesto resulta del producto cartesiano de las variables de estado
	 * parciales asociadas a cada variable de datos.
	 */
	private String nombreVE;
	private boolean varEstadoEnOptim; // True si se aplicará la VE en la optimización

	/**
	 * Probabilidades de cada clase para variable de estado parcial definida. Primer
	 * indice variable de estado, segundo índice clase. Las clases se numeran del 0
	 * en adelante. Puede haber diferente cantidad de clases en cada serie.
	 */
	private double[][] probCla;

	/**
	 * Cantidad de clases en la variable de estado continua para cada variable de
	 * datos;
	 */
	private int[] cantCla;

	/**
	 * Ponderadores para construir las variable de estado parciales. primer indice
	 * variable de estado parcial segundo índice hora dentro del dia. Las variables
	 * de estado se numeran del 0 en adelante.
	 * 
	 */
	private double[][] ponderadores;

	/**
	 * Datos de las variables aleatorias observados históricamente Primer índice:
	 * ordinal del día, de cada dato histórico. Ejemplo el día 560 de los datos
	 * Segundo índice: hora dentro del día. Tercer índice: variable aleatoria, por
	 * ejemplo factor eólico, factor solar, etc.
	 */
	private double[][][] datosHistoricos;

	/**
	 * Un valor para cada valor del primer índice de datosHistoricos. Indica a que
	 * día del año (por ejemplo de 0 a 365) corresponde cada observación. Si
	 * diasLeidos comienza con {20, 21, 22, .......} quiere decir que el primer dia
	 * de datosHistoricos es el dia 20 del año, etc.
	 */
	private int[] diasLeidos;

	/**
	 * L�mites superiores de las clases de las variables de estado continuas Si hay
	 * primer índice: ordinal de variable de estado segundo índice: ordinal de clase
	 * de la variable de estado continua
	 */
	private double[][] limitesSupClases;

	/**
	 * Valores de las variables de estado continuas primer índice: ordinal en
	 * datosHistoricos segundo índice: variable de datos;
	 */
	private double[][] varsEstadoContinuas;

	/**
	 * Clase discreta a la que pertenece cada observación historica en cada variable
	 * primer índice: ordinal de la observación en datosHistoricos segundo índice:
	 * ordinal de la variable de datos
	 */
	private int[][] claseDisc;

	/**
	 * Límites superiores de las clase de las variables continuas primer índice:
	 * paso del año, por ejemplo di�s del año de 0 a 365 segundo índice: ordinal de
	 * variable tercer índice: ordinal de clase
	 */
	private double[][][] limSupVECont;

	/**
	 * Estado discreto compuesto de cada observación hist�rica
	 */
	private int[] estadoCompuesto;

	/**
	 * Para cada "día" del año (ejemplo de 0 a 365) las observaciones de la
	 * población de variables continuas El objeto será
	 * ArrayList<ObservacionVarsContinuas>
	 */
	private Object[] poblacionesVarsContinuas;

	/**
	 * Para cada "día" del año (ejemplo de 0 a 365) las probabilidades de los
	 * estados compuestos, resultantes de las clases de todas las variables primer
	 * índice día del año segundo índice estado compuesto
	 */
	private double[][] probabilidadesEstadosCompuestos;

	/**
	 * Población de la que se extraen los sucesores en el bootstrap
	 * 
	 * Primer índice: dia del año del que se consideran los sucesores posibles
	 * Segundo índice: estado discreto compuesto
	 * 
	 * El object es un ArrayList<Integer> con los ordinales de los días sucesores en
	 * datos hist�ricos
	 * 
	 * A PARTIR DE ESTE ATRIBUTO SE CREAN LAS REALIZACIONES DEL PE SE TOMA EL
	 * Integer que apunta a datosHistoricos
	 * 
	 */
	private Object[][] poblacionesSucesores;

	/**
	 * Población de ordinales en datosHistoricos para cada dia del año y estado
	 * compuesto Primer índice: dia del año Segundo índice: estado discreto
	 * compuesto
	 */
	private Object[][] poblacionesEstadosCompuestos;

	/**
	 * Enumerador lexicográfico de los estados compuestos constru�dos a partir de
	 * las clases discretas de las variables de los datos
	 */
	private EnumeradorLexicografico enumEstadosCompuestos;

	private int cantEstadosCompuestos; // cantidad de estados conpuestos

	public static boolean estimar(String dirEntradas, String nombre) {
		EstimadorBootstrapDiscreto eB = new EstimadorBootstrapDiscreto();

		eB.cargaTodo(dirEntradas);
		String dirSalidas = "";
		String dirResources = "";
		String raiz = "";

		LectorPropiedades lprop = new LectorPropiedades(".\\resources\\mop.conf");
		try {
			dirResources = lprop.getProp("rutaEntradas") + "\\resources\\";
			raiz = lprop.getProp("rutaEntradas") + "\\resources";
			dirSalidas= dirResources+ nombre ;


		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		System.out.println("Termina lectura de datos");

		// Copia el archivo de datos en el directorio de salida
		String origen = dirEntradas + "/datos.txt";
		String destino = dirSalidas + "/datos.txt";

		if (!DirectoriosYArchivos.existeDirectorio(dirSalidas))
			DirectoriosYArchivos.creaDirectorio(raiz, nombre);

		String dirVerif = dirSalidas + "/SalidasVerificacion";
		if (!DirectoriosYArchivos.existeDirectorio(dirVerif))
			DirectoriosYArchivos.creaDirectorio(dirSalidas, "SalidasVerificacion");


		// Por compatibilidad con versiones viejas se elimina la columna inicial de
		// datos con el anio.
		eB.copiaEliminaAnio(origen, destino);





		// Estima proceso Bootstrap discreto
		eB.estimaBootstrapDiscreto(dirSalidas);

		// Crea textos legibles por CargadorPEBootstrapDiscreto
		eB.imprimeTextosParaDatatypes(dirSalidas);

		System.out.println("TERMINO TODO EL PROCESO DE ESTIMACION");

		return true;

	}


	/**
	 * Clase auxiliar que conserva el vínculo entre el ordinal de los datos
	 * hist�ricos y los valores de las variables de estado continuas.
	 * 
	 * @author ut469262
	 *
	 */
	private class ObservacionVarsContinuas {

		private int ordinalEnDatosHistoricos; // ordinal en datos hist�ricos
		private double[] valoresVarsContinuas; // valores de las variables de estado continuas
		private int[] valorClaseDiscreta; // valores de las clases discretas de cada variable de estado

		private int valorEstadoCompuesto; // valor del estado único que sale de la enumeración 
		                                  // lexicográfica de las clases discretas de las variables
		
		
		public double[] getValoresVarsContinuas() {
			return valoresVarsContinuas;
		}

		public ObservacionVarsContinuas(int ordinalEnDatosHistoricos, double[] valoresVarsContinuas) {
			super();
			this.ordinalEnDatosHistoricos = ordinalEnDatosHistoricos;
			this.valoresVarsContinuas = valoresVarsContinuas;
		}

		public int getOrdinalEnDatosHistoricos() {
			return ordinalEnDatosHistoricos;
		}

		public void setOrdinalEnDatosHistoricos(int ordinalEnDatosHistoricos) {
			this.ordinalEnDatosHistoricos = ordinalEnDatosHistoricos;
		}

		public void setValoresVarsContinuas(double[] valoresVarsContinuas) {
			this.valoresVarsContinuas = valoresVarsContinuas;
		}

		public int[] getValorClaseDiscreta() {
			return valorClaseDiscreta;
		}

		public void setValorClaseDiscreta(int[] valorClaseDiscreta) {
			this.valorClaseDiscreta = valorClaseDiscreta;
		}

		public int getValorEstadoCompuesto() {
			return valorEstadoCompuesto;
		}

		public void setValorEstadoCompuesto(int valorEstadoCompuesto) {
			this.valorEstadoCompuesto = valorEstadoCompuesto;
		}

	}

	//////////////////////////////////////////////////////////
	//
	// METODOS DE LECTURA DE DATOS Y PAR�METROS
	//
	//////////////////////////////////////////////////////////

	public String getNombreProceso() {
		return nombreProceso;
	}

	public void setNombreProceso(String nombreProceso) {
		this.nombreProceso = nombreProceso;
	}

	public int getCantVar() {
		return cantVar;
	}

	public void setCantVar(int cantVar) {
		this.cantVar = cantVar;
	}

	public int getCantHoras() {
		return cantHoras;
	}

	public void setCantHoras(int cantHoras) {
		this.cantHoras = cantHoras;
	}

	public int getDurHora() {
		return durHora;
	}

	public void setDurHora(int durHora) {
		this.durHora = durHora;
	}

	public String getNombrePasoPE() {
		return nombrePasoPE;
	}

	public void setNombrePasoPE(String nombrePasoPE) {
		this.nombrePasoPE = nombrePasoPE;
	}

	public int getCantMaxDias() {
		return cantMaxDias;
	}

	public void setCantMaxDias(int cantMaxDias) {
		this.cantMaxDias = cantMaxDias;
	}

	public int getAnchoVentana() {
		return anchoVentana;
	}

	public void setAnchoVentana(int anchoVentana) {
		this.anchoVentana = anchoVentana;
	}

	public int getCantDatos() {
		return cantDatos;
	}

	public void setCantDatos(int cantDatos) {
		this.cantDatos = cantDatos;
	}

	public int getCantDiasDatos() {
		return cantDiasDatos;
	}

	public void setCantDiasDatos(int cantDiasDatos) {
		this.cantDiasDatos = cantDiasDatos;
	}

	public ArrayList<String> getNombresVar() {
		return nombresVar;
	}

	public void setNombresVar(ArrayList<String> nombresVar) {
		this.nombresVar = nombresVar;
	}

	public String getNombreVE() {
		return nombreVE;
	}

	public void setNombreVE(String nombreVE) {
		this.nombreVE = nombreVE;
	}

	public double[][] getProbCla() {
		return probCla;
	}

	public void setProbCla(double[][] probCla) {
		this.probCla = probCla;
	}

	public int[] getCantCla() {
		return cantCla;
	}

	public void setCantCla(int[] cantCla) {
		this.cantCla = cantCla;
	}

	public double[][] getPonderadores() {
		return ponderadores;
	}

	public void setPonderadores(double[][] ponderadores) {
		this.ponderadores = ponderadores;
	}

	public double[][][] getDatosHistoricos() {
		return datosHistoricos;
	}

	public void setDatosHistoricos(double[][][] datosHistoricos) {
		this.datosHistoricos = datosHistoricos;
	}

	public int[] getDiasLeidos() {
		return diasLeidos;
	}

	public void setDiasLeidos(int[] diasLeidos) {
		this.diasLeidos = diasLeidos;
	}

	public double[][] getLimitesSupClases() {
		return limitesSupClases;
	}

	public void setLimitesSupClases(double[][] limitesSupClases) {
		this.limitesSupClases = limitesSupClases;
	}

	public double[][] getVarsEstadoContinuas() {
		return varsEstadoContinuas;
	}

	public void setVarsEstadoContinuas(double[][] varsEstadoContinuas) {
		this.varsEstadoContinuas = varsEstadoContinuas;
	}

	public int[][] getClaseDisc() {
		return claseDisc;
	}

	public void setClaseDisc(int[][] claseDisc) {
		this.claseDisc = claseDisc;
	}

	public double[][][] getLimSupVECont() {
		return limSupVECont;
	}

	public void setLimSupVECont(double[][][] limSupVECont) {
		this.limSupVECont = limSupVECont;
	}

	public int[] getEstadoCompuesto() {
		return estadoCompuesto;
	}

	public void setEstadoCompuesto(int[] estadoCompuesto) {
		this.estadoCompuesto = estadoCompuesto;
	}

	public Object[] getPoblacionesVarsContinuas() {
		return poblacionesVarsContinuas;
	}

	public void setPoblacionesVarsContinuas(Object[] poblacionesVarsContinuas) {
		this.poblacionesVarsContinuas = poblacionesVarsContinuas;
	}

	public Object[][] getPoblacionesSucesores() {
		return poblacionesSucesores;
	}

	public void setPoblacionesSucesores(Object[][] poblacionesSucesores) {
		this.poblacionesSucesores = poblacionesSucesores;
	}

	public Object[][] getPoblacionesEstadosCompuestos() {
		return poblacionesEstadosCompuestos;
	}

	public void setPoblacionesEstadosCompuestos(Object[][] poblacionesEstadosCompuestos) {
		this.poblacionesEstadosCompuestos = poblacionesEstadosCompuestos;
	}

	public EnumeradorLexicografico getEnumEstadosCompuestos() {
		return enumEstadosCompuestos;
	}

	public void setEnumEstadosCompuestos(EnumeradorLexicografico enumEstadosCompuestos) {
		this.enumEstadosCompuestos = enumEstadosCompuestos;
	}

	public int getCantEstadosCompuestos() {
		return cantEstadosCompuestos;
	}

	public void setCantEstadosCompuestos(int cantEstadosCompuestos) {
		this.cantEstadosCompuestos = cantEstadosCompuestos;
	}

	public String getArchDatos() {
		return archDatos;
	}

	public void setArchDatos(String archDatos) {
		this.archDatos = archDatos;
	}

	public boolean isVarEstadoEnOptim() {
		return varEstadoEnOptim;
	}

	public void setVarEstadoEnOptim(boolean varEstadoEnOptim) {
		this.varEstadoEnOptim = varEstadoEnOptim;
	}

	/**
	 * Lee todos los datos
	 */
	public void cargaTodo(String dir) {
		// Se debe ejecutar antes la carga de par�metros
		cargaParam(dir + "/param.txt");
		archDatos = dir + "/datos.txt";
		cargaDatos(archDatos);
	}

	public void cargaParam(String dirArchivo) {
		ArrayList<ArrayList<String>> texDatos;
		texDatos = LeerDatosArchivo.getDatos(dirArchivo);
		int i = 0;
		nombreProceso = texDatos.get(i).get(1);
		i++;
//		archDatos = texDatos.get(i).get(1);
//		i++;		
		cantVar = Integer.parseInt(texDatos.get(i).get(1));
		i++;
		nombrePasoPE = texDatos.get(i).get(1);
		i++;
		durHora = Integer.parseInt(texDatos.get(i).get(1));
		i++;
		cantHoras = Integer.parseInt(texDatos.get(i).get(1));
		i++;
		cantMaxDias = Integer.parseInt(texDatos.get(i).get(1));
		i++;
		anchoVentana = Integer.parseInt(texDatos.get(i).get(1));
		i++;
		nombresVar = new ArrayList<String>();
		for (int j = 0; j < cantVar; j++) {
			nombresVar.add(texDatos.get(i).get(j + 1));
		}
		i++;
		nombreVE = texDatos.get(i).get(1);
		i++;
		varEstadoEnOptim = false;
		if (texDatos.get(i).get(1).equalsIgnoreCase("SI")) {
			varEstadoEnOptim = true;
		}
		i++;
		// Lee probabilidades de las clases de cada variable de estado parcial
		probCla = new double[cantVar][];
		cantCla = new int[cantVar];
		for (int k = 0; k < cantVar; k++) {
			cantCla[k] = texDatos.get(i).size() - 2;
			double[] aux = new double[cantCla[k]];
			if (texDatos.get(i).get(0).equalsIgnoreCase("PROBABILIDADES_CLASES")
					&& texDatos.get(i).get(1).equalsIgnoreCase(nombresVar.get(k))) {
				for (int j = 0; j < cantCla[k]; j++) {
					aux[j] = Double.parseDouble(texDatos.get(i).get(j + 2));
				}
				probCla[k] = aux;
			} else {
				System.out
						.println("Error en la lectura de probabilidades de clase de la variable " + nombresVar.get(k));
				if (CorridaHandler.getInstance().isParalelo()) {
					//PizarronRedis pp = new PizarronRedis();
					//pp.matarServidores();
				}
				System.exit(1);
			}
			i++;
		}

		// Lee los ponderadores de los valores en los intervalos para crear las clases
		// de cada variable de estado parcial
		ponderadores = new double[cantVar][cantHoras];
		for (int k = 0; k < cantVar; k++) {
			if (texDatos.get(i).get(0).equalsIgnoreCase("PONDERADORES_INTERVALOS")
					&& texDatos.get(i).get(1).equalsIgnoreCase(nombresVar.get(k))) {
				for (int j = 0; j < cantHoras; j++) {
//					nombresVar.add(texDatos.get(i).get(j + 1));
					ponderadores[k][j] = Double.parseDouble(texDatos.get(i).get(j + 2));
				}
			} else {
				System.out.println("Error en la lectura de ponderadores para la variable" + nombresVar.get(k));
				if (CorridaHandler.getInstance().isParalelo()) {
					//PizarronRedis pp = new PizarronRedis();
					//pp.matarServidores();
				}
				System.exit(1);
			}
			i++;
		}

	}

	public void cargaDatos(String dirArchivo) {

		ArrayList<ArrayList<String>> texDatos;
		texDatos = LeerDatosArchivo.getDatos(dirArchivo);
		cantDatos = texDatos.size() - 1;
		cantDiasDatos = cantDatos / cantHoras;
		datosHistoricos = new double[cantDiasDatos][cantHoras][cantVar];
		diasLeidos = new int[cantDiasDatos];
		int i = 0;
		// Lee la l�nea inicial con nombre del paso y de variables para verificar
		String nombrePasoLeido = texDatos.get(i).get(1);
		if (!nombrePasoLeido.equals(nombrePasoPE)) {
			System.out.println("Error en nombre duración del paso");
			System.exit(0);
		}

		if (ProcesoEstocastico.verificaNombreDurPaso(nombrePasoLeido)) {
			int durSegunNombre = ProcesoEstocastico.durPasoDeNombreDur(nombrePasoLeido);
			if (durSegunNombre != durHora) {
				System.out.println("Error en duración del intervalo o cantidad de intervalos respecto al paso");
				if (CorridaHandler.getInstance().isParalelo()) {
					//PizarronRedis pp = new PizarronRedis();
					//pp.matarServidores();
				}
				System.exit(0);
			}
		} else {
			System.out.println("Error en nombre duración del paso");
			if (CorridaHandler.getInstance().isParalelo()) {
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(0);
		}

		// Carga los datos
		int idia = 0;
		int ihora = 0;
		for (i = 1; i < texDatos.size(); i++) {
			diasLeidos[idia] = Integer.parseInt(texDatos.get(i).get(1));
 			for (int j = 0; j < cantVar; j++) {
				datosHistoricos[idia][ihora][j] = Double.parseDouble(texDatos.get(i).get(j + 3));
			}
			ihora++;
			if (ihora == cantHoras) {
				ihora = 0;
				idia++;
			}
			if (idia == cantDiasDatos)
				break;
		}

	}

	//////////////////////////////////////////////////////////
	//
	// METODO QUE CREA TEXTOS LEGIBLES POR CargadorPEBootstrapDiscreto
	//
	//////////////////////////////////////////////////////////

	//

	public void imprimeTextosParaDatatypes(String dirSal) {

		String archTextoDataType = dirSal + "/TextoParaCargador.txt";

		// Imprime par�metros generales
		StringBuilder sb = new StringBuilder();
		sb.append("NOMBRE_PROCESO " + nombreProceso);
		sb.append("\n");
//      Ahora los datos se leen del mismo directorio dirSal
//    	sb.append("RUTA_DATOS " + archDatos );
//    	sb.append("\n");     	
		sb.append("CANT_VAR " + cantVar);
		sb.append("\n");
		sb.append("NOMBRE_PASO_PE " + nombrePasoPE);
		sb.append("\n");
		sb.append("DUR_HORA " + durHora + "// duración de la hora en segundos, puede no ser 3600");
		sb.append("\n");
		sb.append("CANT_HORAS " + cantHoras);
		sb.append("\n");
		sb.append("CANT_MAX_DIAS_ANIO " + cantMaxDias);
		sb.append("\n");
		sb.append("INTERVALO_VENTANA_MOVIL " + anchoVentana);
		sb.append("\n");
		sb.append("CANT_DIAS_DATOS " + cantDiasDatos);
		sb.append("\n");
		sb.append("NOMBRES_VAR ");
		for (int iv = 0; iv < cantVar; iv++) {
			sb.append(nombresVar.get(iv));
			sb.append(" ");
		}
		sb.append("\n");
		sb.append("NOMBRE_VE " + nombreVE);
		sb.append("\n");
		sb.append("VE_EN_OPTIM " + varEstadoEnOptim);
		sb.append("\n");
		for (int iv = 0; iv < cantVar; iv++) {
			sb.append("PROBABILIDADES_CLASES " + nombresVar.get(iv) + " ");
			for (int ic = 0; ic < cantCla[iv]; ic++) {
				sb.append(probCla[iv][ic]);
				sb.append(" ");
			}
			sb.append("\n");
		}
		sb.append("CANT_ESTADOS_COMPUESTOS " + cantEstadosCompuestos);
		sb.append("\n");
		for (int iv = 0; iv < cantVar; iv++) {
			sb.append("PONDERADORES_INTERVALOS " + nombresVar.get(iv) + " ");
			for (int iint = 0; iint < ponderadores[iv].length; iint++) {
				sb.append(ponderadores[iv][iint]);
				sb.append(" ");
			}
			sb.append("\n");
		}

		sb.append("// LIMITES SUPERIORES DE LAS CLASES");
		sb.append("\n");
		for (int ip = 0; ip < cantMaxDias; ip++) {
			for (int iv = 0; iv < cantVar; iv++) {
				sb.append("día " + ip + " variable " + nombresVar.get(iv) + " ");
				for (int ic = 0; ic < cantCla[iv]; ic++) {
					sb.append(limSupVECont[ip][iv][ic]);
					sb.append(" ");
				}
				sb.append("\n");
			}
		}

// ESTO EST� MAL PORQUE UN DIA DE DATOS HISTORICOS CORRESPONDE A UN ESTADO COMPUESTO 
// DISTINTO SEG�N EL DIA DEL A�O 
//    	
//    	sb.append("ESTADO COMPUESTO DE CADA D�A DE LOS DATOS HIST�RICOS" );
//    	sb.append("\n");
//    	for(int id=0; id<cantDiasDatos; id++){
//    		sb.append("ORDINAL_DATO_HISTORICO " + id);
//    		sb.append(" ");
//    		sb.append("DIA_DEL_A�O " + diasLeidos[id]);
//    		sb.append(" ");
//    		sb.append("ESTADO_COMPUESTO " + estadoCompuesto[id]);
//        	sb.append("\n");
//    	}

		// Imprime ordinales de días sucesores en datosHistoricos para cada día del año
		// y estado compuesto

		sb.append("// ORDINALES EN DATOS HISTORICOS DE SUCESORES PARA CADA DIA Y ESTADO COMPUESTO DISCRETO");
		sb.append("\n");
		for (int ip = 0; ip < cantMaxDias; ip++) {
			for (int iec = 0; iec < cantEstadosCompuestos; iec++) {
				sb.append("día " + ip + " " + "indice_estado_compuesto " + iec + " ordinales_sucesores ");
				ArrayList<Integer> aux = (ArrayList<Integer>) poblacionesSucesores[ip][iec];
				for (Integer inte : aux) {
					sb.append(inte);
					sb.append(" ");
				}
				sb.append("\n");
			}
		}

		// Imprime probabilidades de los estados compuestos para cada día del año
		sb.append("// PROBABILIDADES DE LOS ESTADOS COMPUESTOS");
		sb.append("\n");
		for (int ip = 0; ip < cantMaxDias; ip++) {
			sb.append("día " + ip + " ");
			for (int iec = 0; iec < cantEstadosCompuestos; iec++) {
				sb.append(probabilidadesEstadosCompuestos[ip][iec] + " ");
			}
			sb.append("\n");
		}

		if (DirectoriosYArchivos.existeArchivo(archTextoDataType))
			DirectoriosYArchivos.eliminaArchivo(archTextoDataType);
		DirectoriosYArchivos.agregaTexto(archTextoDataType, sb.toString());
		System.out.println("Terminó la impresión de texto para cargar datatype");
		
		
		// Imprime archivo de datos generales del proceso
		EscritorTextosGeneralesPE esc = new EscritorTextosGeneralesPE();
		String directorio = dirSal;
	    String nombreEstimacion = nombreProceso;
	    boolean usoSim = true;
	    boolean usoOpt = true;
	    String procAsociadoOptim = "";
	    boolean usoTransformaciones = false;
	    String[] nombresVariables = UtilArrays.dameArrayS(nombresVar);
	    String[] nombresVE = new String[] {nombreVE};
	    boolean usaVEenOptim = false;
	    boolean discretoExaustivo = false;
	    boolean tieneVEContinuas = false;
	    int prioridadSorteos = 1;
	    String nombrePaso = nombrePasoPE;
	    boolean tieneVarsExo = false;
	    String[] nombresVarExo = new String[] {""};
	    String[] nombresProcesosExo = new String[] {""};
		esc.escribeDatosGeneralesPE(directorio, nombreEstimacion, usoSim, usoOpt, procAsociadoOptim, usoTransformaciones, nombresVariables, nombresVE, usaVEenOptim, discretoExaustivo, tieneVEContinuas, prioridadSorteos, nombrePaso, tieneVarsExo, nombresVarExo, nombresProcesosExo);

	}

	//////////////////////////////////////////////////////////
	//
	// PROGRAMA PRINCIPAL
	//
	//////////////////////////////////////////////////////////

	/**
	 * 
	 * M�todo principal que hace la estimación invocando a otros m�todos
	 *
	 * @param dirSalidas directorio donde se crean los textos de resultados de
	 *                   estimación
	 */
	public boolean estimaBootstrapDiscreto(String dirSalidas) {
		creaVarsEstadoContinuas();
		cargaPoblacionesVarsContinuas(dirSalidas);
		creaVarEstadoConjuntoDiscreta(dirSalidas);

		String[] nombresVarAux = UtilArrays.dameArrayS(nombresVar);
		String[] nombreVEAux = new String[1];
		nombreVEAux[0] = nombreVE;

		escribeDatosGeneralesPE( dirSalidas, "", true,  true,  "",  false,
				nombresVarAux,  nombreVEAux,  true,  true,
				false,  1,  Constantes.SEMANA,  false, new String[0],  new String[0]);
		 return true;

	}

	/**
	 * Carga los valores de las variables de estado parciales que son continuas. Hay
	 * un valor para cada variable de datos y para cada dato hist�rico La variable
	 * de estado continua se calcula como el promedio ponderado de las observaciones
	 * de los intervalos por los ponderadores de los intervalos divida por la suma
	 * de los ponderadores
	 */
	public void creaVarsEstadoContinuas() {
		varsEstadoContinuas = new double[cantDiasDatos][cantVar];
		/**
		 * i recorre datos hist�ricos k recorre variables de datos j recorre intervalos
		 * de un paso
		 */
		double[] sumapond = new double[cantVar];
		for (int k = 0; k < cantVar; k++) {
			sumapond[k] = 0.0;
			for (int j = 0; j < ponderadores[k].length; j++) {
				sumapond[k] += ponderadores[k][j];
			}
		}
		for (int i = 0; i < cantDiasDatos; i++) {
			if(i%100==0)System.out.println("Crea vars. estado continuas día " + i);
			for (int k = 0; k < cantVar; k++) {
				double varCont = 0;
				for (int j = 0; j < ponderadores[k].length; j++) {
					varCont += datosHistoricos[i][j][k] * ponderadores[k][j];
				}
				if(sumapond[k]!=0) {
					varsEstadoContinuas[i][k] = varCont / sumapond[k];
				}else {
					varsEstadoContinuas[i][k] = 0.0;
				}
			}
		}

	}

	/**
	 * Crea para cada "día" del año la población de variables de estado continuas
	 * asociadas y crea el archivo de texto
	 */
	public void cargaPoblacionesVarsContinuas(String dirSalidas) {
		String salidaOrdinalesContinuas = dirSalidas + "/SalidasVerificacion/ordinalesPoblacionesContinuas.xlt";
		if (DirectoriosYArchivos.existeArchivo(salidaOrdinalesContinuas))
			DirectoriosYArchivos.eliminaArchivo(salidaOrdinalesContinuas);
		poblacionesVarsContinuas = new Object[cantMaxDias];
		for (int ip = 0; ip < cantMaxDias; ip++) {
			poblacionesVarsContinuas[ip] = new ArrayList<ObservacionVarsContinuas>();
		}
		/**
		 * ip recorre pasos del año ih recorre datosHistóricos
		 */
		for (int ip = 0; ip < cantMaxDias; ip++) {
			ArrayList<ObservacionVarsContinuas> al = (ArrayList<ObservacionVarsContinuas>) poblacionesVarsContinuas[ip];
			for (int ih = 0; ih < cantDiasDatos; ih++) {
				if (Math.abs(diasLeidos[ih] - ip) <= anchoVentana
						|| Math.abs(diasLeidos[ih] - (ip + cantMaxDias)) <= anchoVentana
						|| Math.abs(diasLeidos[ih] + cantMaxDias - ip) <= anchoVentana) {
					ObservacionVarsContinuas ovc = new ObservacionVarsContinuas(ih, varsEstadoContinuas[ih]);
					al.add(ovc);
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("Paso del año,");
			sb.append(ip + ",");
			for (ObservacionVarsContinuas o2 : al) {
				sb.append(o2.getOrdinalEnDatosHistoricos());
				sb.append(",");
			}
			if(ip%100==0)System.out.println("Carga poblaciones vars. continuas dia " + ip);
			DirectoriosYArchivos.agregaTexto(salidaOrdinalesContinuas, sb.toString());
		}
		System.out.println("Termina carga de poblaciones de vars. estado continuas");
	}

	/**
	 * Crea para cada "día" del año el estado conjunto discreto
	 * 
	 * @param
	 */
	public void creaVarEstadoConjuntoDiscreta(String dirSalidas) {
		String salidaVEContinuas = dirSalidas + "/SalidasVerificacion/poblacionesVEContinuas.xlt";
		estadoCompuesto = new int[cantDiasDatos];

		if (DirectoriosYArchivos.existeArchivo(salidaVEContinuas))
			DirectoriosYArchivos.eliminaArchivo(salidaVEContinuas);

		limSupVECont = new double[cantMaxDias][cantVar][];
		int[] cotasInferiores = new int[cantVar];
		int[] cotasSuperiores = new int[cantVar];
		for (int iv = 0; iv < cantVar; iv++) {
			cotasInferiores[iv] = 0;
			cotasSuperiores[iv] = cantCla[iv] - 1;
		}
		enumEstadosCompuestos = new EnumeradorLexicografico(cantVar, cotasInferiores, cotasSuperiores);
		enumEstadosCompuestos.creaTablaYListaOrdinales();
		cantEstadosCompuestos = enumEstadosCompuestos.getCantTotalVectores();
//		Object[][] poblacionesSucesores = new Object[cantMaxPasos][cantEstadosCompuestos];
//		Object[][] poblacionesEstadosCompuestos = new Object[cantMaxPasos][cantEstadosCompuestos]; 
		poblacionesSucesores = new Object[cantMaxDias][cantEstadosCompuestos];
		poblacionesEstadosCompuestos = new Object[cantMaxDias][cantEstadosCompuestos];
		probabilidadesEstadosCompuestos = new double[cantMaxDias][cantEstadosCompuestos];
		for (int ip = 0; ip < cantMaxDias; ip++) {
			if(ip%100==0) System.out.println("Se carga poblaciones sucesores y est. compuestos dia " + ip);
			for (int iec = 0; iec < cantEstadosCompuestos; iec++) {
				poblacionesSucesores[ip][iec] = new ArrayList<Integer>();
				poblacionesEstadosCompuestos[ip][iec] = new ArrayList<Integer>();
			}
		}
		for (int dia = 0; dia < cantMaxDias; dia++) {
			// Crea para un dia del año los límites superiores de las VE continuas
			System.out.println("Carga límites superiores VE continuas dia " + dia);
			for (int ive = 0; ive < cantVar; ive++) {
				limSupVECont[dia][ive] = new double[cantCla[ive]];
				ArrayList<ObservacionVarsContinuas> obs1 = ordenarPorVEContinua(ive, dia);
				double percentAcum = 0.0;
				for (int iclas = 0; iclas < cantCla[ive]; iclas++) {
					percentAcum += probCla[ive][iclas];
					if (iclas < cantCla[ive] - 1) {
						int N = obs1.size();
						int indice = (int) Math.round(percentAcum * N + Constantes.EPSILONCOEF);
						if (indice > N)
							indice = N;
						if (indice == 0)
							indice = 1;
						indice--;
						limSupVECont[dia][ive][iclas] = obs1.get(indice).getValoresVarsContinuas()[ive];
					} else {
						// se carga el high-value como límite superior de la última clase
						limSupVECont[dia][ive][cantCla[ive] - 1] = Double.POSITIVE_INFINITY;
					}
				}
				if (Math.abs(percentAcum - 1.0) > 0.00001) {
					System.out.print("Error en definición de probabilidades");
					if (CorridaHandler.getInstance().isParalelo()) {
						//PizarronRedis pp = new PizarronRedis();
					//	pp.matarServidores();
					}
					System.exit(1);
				}
				// Carga los valores de las VE discretas de cada variable
				for (ObservacionVarsContinuas ovc : obs1) {
					// Si se est� procesando la VE de ordinal 0 se crea el vector de datos para
					// valorClaseDiscreta
					if (ive == 0)
						ovc.setValorClaseDiscreta(new int[cantVar]);
					boolean hallo = false;
					for (int iclas = 0; iclas < cantCla[ive]; iclas++) {
						if (ovc.getValoresVarsContinuas()[ive] <= limSupVECont[dia][ive][iclas]) {
							ovc.valorClaseDiscreta[ive] = iclas;
							hallo = true;
							break;
						}
					}
				}
				// Imprime las poblaciones de cada variable de estado continua del paso
				StringBuilder sb = new StringBuilder();
				sb.append("dia " + dia + "," + "variable " + ive + "\n");
				sb.append("OBSERVACIONES,");
				for (ObservacionVarsContinuas ovc : obs1) {
					sb.append(ovc.getValoresVarsContinuas()[ive]);
					sb.append(",");
				}
				sb.append("\n");

				// Imprime los valores de clase de las observaciones
				sb.append("CLASE DISCRETA,");
				for (ObservacionVarsContinuas ovc : obs1) {
					sb.append(ovc.getValorClaseDiscreta()[ive]);
					sb.append(",");
				}
				sb.append("\n");
				// Imprime el ordinal en datosHistoricos de las observaciones
				sb.append("ORDINAL EN DATOS HIST�RICOS,");
				for (ObservacionVarsContinuas ovc : obs1) {
					sb.append(ovc.getOrdinalEnDatosHistoricos());
					sb.append(",");
				}
				sb.append("\n");
				// Imprime límites de clases
				sb.append("LIMITES DE CLASES,");
				for (int iclas = 0; iclas < cantCla[ive]; iclas++) {
					sb.append(limSupVECont[dia][ive][iclas]);
					sb.append(",");
				}
				sb.append("\n");
				DirectoriosYArchivos.agregaTexto(salidaVEContinuas, sb.toString());

			}
			// Calcula el estado compuesto discreto y carga la población de sucesores
			ArrayList<ObservacionVarsContinuas> listaObs = (ArrayList<ObservacionVarsContinuas>) poblacionesVarsContinuas[dia];
			for (ObservacionVarsContinuas ovc : listaObs) {
				int estadoCompuesto = enumEstadosCompuestos.devuelveOrdinalDeVector(ovc.valorClaseDiscreta);
				ovc.setValorEstadoCompuesto(estadoCompuesto);
				ArrayList<Integer> auxEc = (ArrayList<Integer>) poblacionesEstadosCompuestos[dia][estadoCompuesto];
				auxEc.add(ovc.getOrdinalEnDatosHistoricos());
				int ordinalSucesor = ovc.getOrdinalEnDatosHistoricos() + 1;
				if (ordinalSucesor < cantDiasDatos) {
					// el ultimo dia de los datos no tiene sucesor
					ArrayList<Integer> auxSuc = (ArrayList<Integer>) poblacionesSucesores[dia][estadoCompuesto];
					auxSuc.add(ordinalSucesor);
				}
			}

			// Imprime la población de ordinales en datosHistoricos de cada estado compuesto

			StringBuilder sb = new StringBuilder();
			sb.append("ORDINALES EN DatosHistoricos DE OBSERVACIONES PARA CADA ESTADO COMPUESTO \n");
			for (int iec = 0; iec < cantEstadosCompuestos; iec++) {
				sb.append("dia " + dia + "," + "indice estado compuesto, " + iec + ",");
				sb.append("ordinales en datosHistoricos \n");
				ArrayList<Integer> auxEc = (ArrayList<Integer>) poblacionesEstadosCompuestos[dia][iec];
				for (Integer inte : auxEc) {
					sb.append(inte);
					sb.append(",");
				}
				sb.append("\n");
			}
			DirectoriosYArchivos.agregaTexto(salidaVEContinuas, sb.toString());

			/**
			 * Calcula e imprime las probabilidades de los estados compuestos para el "día"
			 * del año
			 */
			int cantObsDia = 0;
			for (int iec = 0; iec < cantEstadosCompuestos; iec++) {
				ArrayList<Integer> auxEc = (ArrayList<Integer>) poblacionesEstadosCompuestos[dia][iec];
				probabilidadesEstadosCompuestos[dia][iec] = auxEc.size();
				cantObsDia += auxEc.size();
			}
			sb.append("PROBABILIDADES EN EL DIA DE LOS ESTADOS COMPUESTOS \n");
			for (int iec = 0; iec < cantEstadosCompuestos; iec++) {
				probabilidadesEstadosCompuestos[dia][iec] = probabilidadesEstadosCompuestos[dia][iec] / cantObsDia;
				sb.append(probabilidadesEstadosCompuestos[dia][iec] + ",");
			}
			sb.append("\n");

			// Imprime la población de ordinales de sucesores en datosHistoricos para cada
			// estadoCompuesto del paso
			sb = new StringBuilder("ORDINALES DE SUCESORES EN DATOS HIST�RICOS PARA CADA ESTADO COMPUESTO \n");
			for (int iec = 0; iec < cantEstadosCompuestos; iec++) {
				sb.append("dia " + dia + "," + "indice estado compuesto, " + iec + ",");
				sb.append("ordinales de sucesores en datosHistoricos \n");
				ArrayList<Integer> aux = (ArrayList<Integer>) poblacionesSucesores[dia][iec];
				for (Integer inte : aux) {
					sb.append(inte);
					sb.append(",");
				}
				sb.append("\n");
			}
			DirectoriosYArchivos.agregaTexto(salidaVEContinuas, sb.toString());
		}
		System.out.println("Termin� la estimación de vars. de estado discretas y sucesores");
	}

	/**
	 * Devuelve las observaciones de un dia de poblacionesVarsContinuas ordenadas
	 * por la variable de estado continua de ordinal indiceVE
	 * 
	 * @param indiceVE ordinal de la variable de estado continua
	 * @param          dia: dia del año (ejemplo día entre 0 y 365)
	 * @return
	 */
	public ArrayList<ObservacionVarsContinuas> ordenarPorVEContinua(int indiceVE, int dia) {

		ArrayList<ComparadorObjetos> listaComparadores = new ArrayList<ComparadorObjetos>();
		ArrayList<ObservacionVarsContinuas> listaObs = (ArrayList<ObservacionVarsContinuas>) poblacionesVarsContinuas[dia];
		for (ObservacionVarsContinuas ovc : listaObs) {
			ComparadorObjetos co = new ComparadorObjetos(ovc.getValoresVarsContinuas()[indiceVE], ovc);
			listaComparadores.add(co);
		}
		Collections.sort(listaComparadores);
		ArrayList<ObservacionVarsContinuas> listaObsOrdenadas = new ArrayList<ObservacionVarsContinuas>();
		for (ComparadorObjetos co : listaComparadores) {
			listaObsOrdenadas.add((ObservacionVarsContinuas) co.getObjeto());
		}
		return listaObsOrdenadas;
	}

	/**
	 * Crea el archivo de datos en destino eliminando la primera columna con el año
	 * por compatibilidad con datos anteriores
	 * 
	 * @param origen
	 * @param destino
	 */
	public void copiaEliminaAnio(String origen, String destino) {

		ArrayList<ArrayList<String>> entrada = LeerDatosArchivo.getDatos(origen);
		int nfil = entrada.size();
		int ncol = entrada.get(1).size() - 1;
		String[][] salida = new String[nfil][ncol];
		for (int i = 0; i < entrada.size(); i++) {
			if(i%100==0)System.out.println("Se elimina anio de datos del dia " + i);
			if (i == 0) {
				for (int j = 0; j < entrada.get(0).size(); j++) {
					salida[i][j] = entrada.get(i).get(j);
				}
			} else {
				for (int j = 0; j < ncol; j++) {
					salida[i][j] = entrada.get(i).get(j + 1);
				}
			}
		}
		System.out.println("Está escribiendo nuevos archivos de datos: puede demorar algunos minutos, espere");
		String sep = " ";
		DirectoriosYArchivos.siExisteElimina(destino);
		DirectoriosYArchivos.creaTexto(salida, destino, sep);
	}

	//////////////////////////////////////////////////////////
	//
	// METODO MAIN PARA PROBAR LA CLASE
	//
	//////////////////////////////////////////////////////////

	/**
	 * El programa - lee los datos de un conjunto de series y la definición de
	 * variables de estado - estima el proceso bootstrap - crea los archivos para
	 * cargar los datatypes, legibles por CargadorPRBootstrapDiscreto
	 */

	public static void main(String[] args) {

		EstimadorBootstrapDiscreto eB = new EstimadorBootstrapDiscreto();
		String dirArchConf = "resources/ESTIMADORES.conf";
		String nombreProp = "rutaBootstrap";
		// Elige directorios de entradas y salidas
		boolean soloDirectorio = true;
		String titulo1 = "ELIJA EL DIRECTORIO DE ENTRADA DE DATOS DONDE LEER LOS ARCHIVOS param.txt y datos.txt";
		String dirEntradas = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo1, dirArchConf,
				nombreProp);

		String titulo2 = "ELIJA EL DIRECTORIO DE SALIDA DE LA ESTIMACION, ARCHIVOS: TextoParaCargador.txt y otros de verificación";
		String dirSalidas = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo2, dirArchConf,
				nombreProp);
		String dirVerif = dirSalidas + "/SalidasVerificacion";
		if (!DirectoriosYArchivos.existeDirectorio(dirVerif))
			DirectoriosYArchivos.creaDirectorio(dirSalidas, "SalidasVerificacion");

		// Lee datos de series

		eB.cargaTodo(dirEntradas);
		System.out.println("Termina lectura de datos");

		// Copia el archivo de datos en el directorio de salida
		String origen = dirEntradas + "/datos.txt";
		String destino = dirSalidas + "/datos.txt";
//        try{
//        	DirectoriosYArchivos.copy2(origen, destino);
//        }catch(Exception e){
//        	System.out.println("Error al copiar el archivo " + origen);
//        	System.exit(1);
//        }

		// Por compatibilidad con versiones viejas se elimina la columna inicial de
		// datos con el anio.
		eB.copiaEliminaAnio(origen, destino);

		// Estima proceso Bootstrap discreto
		eB.estimaBootstrapDiscreto(dirSalidas);

		// Crea textos legibles por CargadorPEBootstrapDiscreto
		eB.imprimeTextosParaDatatypes(dirSalidas);

		System.out.println("TERMINO TODO EL PROCESO DE ESTIMACION");
	}

}