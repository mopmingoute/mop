/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorPEDemandaEscenarios is part of MOP.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;

import datatypesProcEstocasticos.DatosPEDemandaAnioBase;
import datatypesProcEstocasticos.DatosPEDemandaEscenarios;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesTiempo.DatosTiposDeDia;
import persistencia.EscritorEntradaParaCargadorProcEscenarios;
import procesosEstocasticos.ProcesoDemandaEscenarios;
import tiempo.LineaTiempo;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LectorDireccionArchivoDirectorio;
import utilitarios.LeerDatosArchivo;
import utilitarios.Par;

/**
 * Levanta potencias por escenario en bruto, QUE DEBEN SER PARA AÑOS ENTEROS y construye los archivos para
 * un ProcesoEscenarios para la demanda.
 * @author ut469262
 *
 */

public class CargadorPEDemandaEscenarios {
	
	
	/**
	 * COL_ESC_1ANIO  cada columna es un escenario y cada fila es una variable aleatoria por paso, la primera fila y columna no se toman, son comentarios
	 * varias filas sucesivas de un paso contienen las variables aleatorias del paso en el escenario.
	 * Se lee un único año, con potencias por hora, que se repite rotando los escenarios en el ProcesoEscenarios.
	 */
	private static final String COL_ESC_1ANIO = "COL_ESC_1ANIO";  
	
	
	/**
	 * Si el paso en lugar de horas (60 minutos) fuera por ejemplo de media hora
	 * habría que cambiar las constantes anteriores poner el doble. Horas son en realidad los intervalos de muestreo de la potencia.
	 */
	private static final int CANT_HORAS_ANIO = 8760;
	private static final int CANT_HORAS_ANIO_BISIESTO = 8784;
	private static final int DUR1HORA_EN_SEGS = 3600; 
	
	private static int cantVA;

	private static ArrayList<String> nombresVariables;
	private static boolean sumaVariables;
	private static String nombreVarSuma;
	private static String nombrePaso;
	private static String formatoLectura;
	private static int anioBaseInicial;   // año del primer paso de los escenarios con datos base, ejemplo: 2016.
	private static int anioBaseFinal;   //  id. final
	private static int anioSimInicial;   // primer año para el que se simularán datos
	private static int anioSimFinal;   // último año para el que se simularán datos
	private static int cantAnios;
	private static boolean bisiesto; // si el año inicial es bisiesto


	// paso del año del primer paso del PE, ejemplo paso 10, si arranca en la semana décima empezando de cero del 2016
	// los pasos son 0, 1, 2, .....
	private static int cantMaxPasos;   // cantidad máxima de pasos que puede tener el proceso en un año
	
	/**
	 * clave: nombre de la VA
	 * 
	 * Valor:
	 * primer índice año a partir de anioBaseInicial
	 * segundo índice escenario 
	 * tercer índice recorre el ordinal del paso en el año, por ejemplo las semanas, horas, etc.
	 * 
	 */
	private static Hashtable<String, double[][][]>  potenciasMW;  // potencias en MW
	
	
	/**
	 * clave:  String: nombre de la V.A + String(año)
	 * valor: Energía promedio anuales de los escenarios de potencias en el año
	 */
	private static Hashtable<String, Double> energiasEspDatosGWh; 
	 
	/**
	 * clave: String: nombre de la V.A + String(año)
	 * valor: Energía promedio anuales de los escenarios de potencias en el año
	 */
	private static Hashtable<String, Double> energiasGWh; // Energías esperadas en GWh de los años para los que se producen realizaciones
	
//	/**
//	 * valores de las variables de estado, asociados a cada dato.
//	 * tercer índice recorre las VE
//	 */
//	private double[][][][]  valoresVE;
	
	private static String estimacionVE;   // identificación de la estimación de las VE que se empleó	

	private static int[][] etiquetaCron;     // da una etiqueta de crónica a cada escenario para cada año; por ejemplo "1909", "1910", etc.
	
	
	private static int[] cantEscTot;      // cantidad de escenarios totales de cada VA
	private static int[] cantEscUsados;  // se usan los cantEsc primeros escenarios de cada VA
	
	/**
     *  Para cada año en los que existen pasos del escenario, ordinal (columna) de dicho primer
     *  paso en el escenario. La clave es el año y el valor es el ordinal.
     *  Ejemplo: un proceso semanal cuyos escenarios empiezan en la semana 26 del año 2016 tiene en ordinalPrimerPasoAnio
     *  las entradas: (2016, 1), (2017,27) , (2018, 27+52=79), .....
	 */	
	private Hashtable<Integer,Integer> ordinalPrimerPasoAnio; 

	private static ArrayList<ArrayList<String>> texDatosBrutos;
	
	private static boolean corrigeTiposDia;  // Si es false no se utiliza el tipo de día y feriados para producir realizaciones
	
	private static DatosTiposDeDia tiposDeDia;
	
	public static void liberaMemoria() {
		potenciasMW = null;
		energiasEspDatosGWh = null;
		energiasGWh = null;		
	}
	
	
	public static DatosPEDemandaEscenarios devuelveDatosPEDemandaEscenarios(DatosProcesoEstocastico dpe){
		
		DatosPEDemandaEscenarios dpdem = new DatosPEDemandaEscenarios();
		String ruta = "./resources/" + dpe.getNombre();
		dpdem.setDatGen(CargadorDatosGeneralesPE.devuelveDatosGeneralesPE(ruta, dpe.getNombre()));
				
		// Lee el archivo datos.txt
		String dirArch = ruta;

		cargaDatos(dirArch);
		
		// Lee los archivos potenciasBase-nombreVA.txt de cada VA del proceso
		dirArch = ruta;
		cargaPotenciasBase(dirArch);
				
		// Lee el archivo energias.txt
		dirArch = ruta;
		cargaEnergias(dirArch);
		
		cantAnios = anioBaseFinal - anioBaseInicial + 1;
		
		// Calcula energía esperada de los años de potencias en los escenarios usados
		int cantHoras;
		energiasEspDatosGWh = new Hashtable<String, Double>();
		for(int ian=0; ian<cantAnios; ian++) {
			int anio = anioBaseInicial + ian;
			boolean bisiesto = LineaTiempo.bisiesto(anio);
			cantHoras = CANT_HORAS_ANIO;
			if(bisiesto) cantHoras = CANT_HORAS_ANIO_BISIESTO;
			for(int iv=0; iv< cantVA; iv++) {
				String nombreVA = nombresVariables.get(iv);
				double ener = 0.0;
				for(int iesc=0; iesc<cantEscUsados[iv]; iesc++) {
					double ener1esc = 0.0;
					for(int ih=0; ih<cantHoras;ih++) {
						ener1esc += potenciasMW.get(nombreVA)[ian][iesc][ih]*(DUR1HORA_EN_SEGS/utilitarios.Constantes.SEGUNDOSXHORA);
					}
					ener += ener1esc/(cantEscUsados[iv]*utilitarios.Constantes.MWHXGWH);		
				}
				String clave = ProcesoDemandaEscenarios.claveEnergias(nombreVA, anio);
				energiasEspDatosGWh.put(clave, ener);
			}
		}
		
		dpdem.setAnioBaseInicial(anioBaseInicial);
		dpdem.setAnioBaseFinal(anioBaseFinal);
		dpdem.setAnioSimInicial(anioSimInicial);
		dpdem.setAnioSimFinal(anioSimFinal);
		dpdem.setNombresVA(nombresVariables);
		dpdem.setCantVA(nombresVariables.size());
		dpdem.setCantVarLeidas(nombresVariables.size());
		if(sumaVariables) dpdem.setCantVA(1);
		dpdem.setSumaVar(sumaVariables);
		dpdem.setNombre_var_suma(nombreVarSuma);
		dpdem.setPotencias(potenciasMW);
		dpdem.setEnergias(energiasGWh);
		dpdem.setEnergiaEsperadaDatos(energiasEspDatosGWh);
		dpdem.setCantEsc(cantEscUsados);
		dpdem.setDiscretoExhaustivo(dpe.getDiscretoExhaustivo());
		dpdem.setMuestreado(dpe.getMuestreado());
		dpdem.setCorrigeTiposDia(corrigeTiposDia);	
		dpdem.setTiposDeDia(tiposDeDia);
		liberaMemoria();
		return dpdem;
	}
	

	
//	public static void cargaTodoBruto(String dirEntradas) {		
//		cargaDatos(dirEntradas, true);		
//		cargaPotenciasBaseBrutas(dirEntradas);	
//		tiposDeDia = cargaTiposDeDia(dirEntradas, anioBaseInicial, anioBaseFinal);
//		
//	}
//	
	
	public static void cargaDatos(String dirEntradas) {		
		
		String nomArch = dirEntradas+"/datos.txt";
        texDatosBrutos = LeerDatosArchivo.getDatos(nomArch);
        AsistenteLectorEscritorTextos al = new AsistenteLectorEscritorTextos(texDatosBrutos, dirEntradas);
		int i=0;
		String etiqueta;
		
		etiqueta = "NOMBRES_VARIABLES";
		nombresVariables = al.cargaLista(i, etiqueta);
		i++;		
		
		cantVA = nombresVariables.size();
		
		etiqueta = "SUMA_VARIABLES";    // SI - El proceso tiene una única V.A. que es la suma de las variables definidas, NO - Cada variable genera una V.A diferente  
		sumaVariables = al.cargaBooleano(i, etiqueta);
		i++;		
		
		etiqueta = "NOMBRE_VAR_SUMA";
		nombreVarSuma = al.cargaPalabra(i, etiqueta);
		i++;
		
		etiqueta = "CANTIDAD_ESCENARIOS_TOTAL";
		cantEscTot = utilitarios.UtilArrays.dameArrayI(al.cargaListaEnteros(i, etiqueta));
		i++;
		etiqueta = "CANTIDAD_ESCENARIOS_USADOS";
		cantEscUsados = utilitarios.UtilArrays.dameArrayI(al.cargaListaEnteros(i, etiqueta));
		i++;
		etiqueta = "NOMBRE_PASO";
		nombrePaso = al.cargaPalabra(i, etiqueta);
		i++;
		etiqueta = "ANIO_BASE_INI";
		anioBaseInicial = al.cargaEntero(i, etiqueta);
		i++;
		etiqueta = "ANIO_BASE_FIN";
		anioBaseFinal = al.cargaEntero(i, etiqueta);
		i++;
		etiqueta = "ANIO_SIM_INI";
		anioSimInicial = al.cargaEntero(i, etiqueta);
		i++;
		etiqueta = "ANIO_SIM_FIN";
		anioSimFinal = al.cargaEntero(i, etiqueta);
		i++;				
		etiqueta = "CANT_MAXIMA_PASOS";
		cantMaxPasos = al.cargaEntero(i, etiqueta);			
		cantAnios = anioBaseFinal-anioBaseInicial+1;
		i++;
		etiqueta = "CORRIGE_TIPOS_DIA";
		corrigeTiposDia = al.cargaBooleano(i, etiqueta);
		
		String rutaTiposDia = "./resources/" + "tiposDeDia.txt";
		tiposDeDia = persistencia.CargadorTiposDeDia.cargaTiposDeDia(rutaTiposDia, anioBaseInicial, anioBaseFinal);
		
	}
	
	
	public static void cargaPotenciasBase(String dirEntradas) {
		
		cantAnios = anioBaseFinal-anioBaseInicial+1;
		potenciasMW = new Hashtable<String, double[][][]>();
		for(int iv=0; iv<cantVA; iv++) {
			String nombreVA = nombresVariables.get(iv);
			potenciasMW.put(nombresVariables.get(iv), new double[cantAnios][cantEscTot[iv]][cantMaxPasos]);
			ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(dirEntradas+	"/potenciasBase-"+ nombreVA +".prn");				
			int cantHoras = CANT_HORAS_ANIO;
			bisiesto = LineaTiempo.bisiesto(anioBaseInicial);
			if(bisiesto) cantHoras = CANT_HORAS_ANIO_BISIESTO;
			int fila = 0;
			for(int ian=0; ian<cantAnios; ian++) {
				for(int ih=0; ih<cantHoras; ih++) {
					for(int iesc=0; iesc<cantEscTot[iv]; iesc++) {					
						potenciasMW.get(nombreVA)[ian][iesc][ih] = Double.parseDouble(texto.get(fila).get(iesc+2));
					}
					fila++;
				}
			}
		}
	}
	


	
	
	
//	/**
//	 */
//	public static void cargaPotenciasBase(String dirEntradas) {
//		potenciasMW = new Hashtable<String, double[][][]>();
//		for(int iv=0; iv<cantVA; iv++) {
//			String nombreVA = nombresVariables.get(iv);
//			potenciasMW.put(nombresVariables.get(iv), new double[cantAnios][cantEscTot[iv]][cantMaxPasos]);
//			ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(dirEntradas+	"/potenciasBaseBrutas/"+ nombreVA +".txt");						
//			String arch = dirEntradas+	"/potenciasBase-" + nombreVA + ".txt";			
//			cantAnios = anioBaseFinal-anioBaseInicial+1;
//			bisiesto = LineaTiempo.bisiesto(anioBaseInicial);
//			int fila = 0;
//			for(int ian=0; ian<cantAnios; ian++) {
//				int anio = anioBaseInicial + ian;
//				int cantHoras = CANT_HORAS_ANIO;
//				if(Integer.parseInt(texto.get(fila).get(0))!=anio) {
//					System.out.println("Error en el año en fila " + fila + " archivo " + arch);
//					System.exit(1);
//				}
//				if(bisiesto) cantHoras = CANT_HORAS_ANIO_BISIESTO;
//				for(int iesc=0; iesc<cantEscTot[iv]; iesc++) {		
//					for(int ih=0; ih<cantHoras; ih++) {													
//						potenciasMW.get(nombreVA)[ian][iesc][ih] = Double.parseDouble(texto.get(fila).get(ih+2));
//					}
//					fila++;				
//				}
//			}
//		
//		}
//	}
	
	

	
	
	public static void cargaEnergias(String dirEntradas) {
		energiasGWh = new Hashtable<String,Double>();
		String arch = dirEntradas+	"/energias.txt";
		ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(arch);
		AsistenteLectorEscritorTextos al = new AsistenteLectorEscritorTextos(texto, arch);
		ArrayList<String> nleidos = al.cargaLista(0,"NOMBRES_VA");
		int cantAniosHoriz = texto.size()-1;
		for(int iv=0; iv<cantVA; iv++) {
			if(!nleidos.get(iv).equalsIgnoreCase(nombresVariables.get(iv))) {
				System.out.println("Error en energía de variable " + nombresVariables.get(iv) + " archivo " + arch);
				System.exit(1);
			}
			for(int ian=0; ian<cantAniosHoriz; ian++) {
				int anio = Integer.parseInt(texto.get(ian+1).get(0));
//				if(anio!=anioBaseInicial+ian) {
//					System.out.println("Error en año del archivo energias.txt, en el directorio " + dirEntradas);
//				}
				double ener = Double.parseDouble(texto.get(ian+1).get(1+iv));
				String clave = ProcesoDemandaEscenarios.claveEnergias(nombresVariables.get(iv), anio);
				energiasGWh.put(clave, ener);
			}
		}
		
	}
	
	
//	public void grabaTextosParaDatatypes(String dirSalidas) {
//		// Graba archivo potenciasBase.txt
//		String dirArchivo = dirSalidas + "/potenciasBase.txt";
//		String titulo = "// Potencias base: filas ordenadas por año, escenario, variable aleatoria; columnas por paso del proceso (hora, semana, etc.)"; 
//		int[] cantPasosPorAnio = {CANT_HORAS_ANIO};
//		if(LineaTiempo.bisiesto(anioBaseInicial)) cantPasosPorAnio[0]=CANT_HORAS_ANIO_BISIESTO;	
//		EscritorEntradaParaCargadorProcEscenarios.escribeEntradaPEscenarios(dirArchivo, titulo, nombrePaso, anioBaseInicial, cantAnios, cantPasosPorAnio, cantEscTot, cantEscUsados, cantVA, potenciasMW);	
//		
//		// Graba archivo datos.txt
//		ArrayList<ArrayList<String>> aux = new ArrayList<ArrayList<String>>();
//		for(int i=1; i<texDatosBrutos.size(); i++) {
//			aux.add(texDatosBrutos.get(i));
//		}
//		dirArchivo = dirSalidas + "/datos.txt";
//		DirectoriosYArchivos.siExisteElimina(dirArchivo);
//		DirectoriosYArchivos.agregaTexto(dirArchivo, aux, " ");
//		
//	}
	
//	/**
//	 * Este método lee los datos en bruto de potencia de cualquier directorio, los datos de tipos de día
//	 * y los parámetros del proceso a crear y carga en el subdirectorio del proceso estocástico dentro del directorio resources
//	 * los archivos:
//	 * datos.txt
//	 * potenciasBase.txt
//	 * tiposDeDia.txt
//	 * Estos archivos son los que debe leer el método
//	 * devuelveDatosPEDemandaEscenarios(DatosProcesoEstocastico dpe)
//	 * que construye el DatosPEDemandaEscenarios que requiere el constructor de la clase ProcesoDemandaEscenarios.
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		CargadorPEDemandaEscenarios eE = new CargadorPEDemandaEscenarios();
//		String dirArchConf = "resources/ESTIMADORES.conf";
//		String nombreProp = "rutaDemEscenarios";
//		// Elige directorios de entradas y salidas
//		boolean soloDirectorio = true;
//		String titulo1 = "ELIJA EL DIRECTORIO DE ENTRADA DONDE LEER LOS ARCHIVOS datosBrutos.txt, tiposDeDia.txt y potenciasBaseBrutas.txt";
//		String dirEntradas = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo1, dirArchConf,
//				nombreProp);
//
//		String titulo2 = "ELIJA EL DIRECTORIO DONDE SE CARGA LOS ARCHIVOS potenciasBase.txt y datos.txt PARA RESOURCES DEL PROCESO ESTOCASTICO A CREAR ";
//		String dirSalidas = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo2, dirArchConf,
//				nombreProp);
//		
//
//		// Copia el archivo de tipos de dia
//		String original=dirEntradas+"/tiposDeDia.txt";
//		String copia=dirSalidas+"/tiposDeDia.txt";
//		DirectoriosYArchivos.siExisteElimina(copia);
//		try {
//			DirectoriosYArchivos.copy(original, copia);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.exit(1);
//		}
//		
//		// Lee potencias 
//
//		eE.cargaTodoBruto(dirEntradas);
//		System.out.println("Termina lectura de potencias");
//
//		eE.grabaTextosParaDatatypes(dirSalidas);
//		
//		
//
//		System.out.println("TERMINO TODO EL PROCESO DE CREACION DE ARCHIVOS DEL PROCESO");
//	}
		
	/**
	 * Este método main sirve solamente para darle formato a los datos de entrada de potencias base
	 * a partir un archivo que se recibió. NO TIENE CARÁCTER GENERAL
	 * @param args
	 */
	public static void main(String[] args) {		
		cantAnios = 1;
		int anio = 2022;
		int cantEsc = 200;
		String archEntradas = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\PRUEBAS\\DEMANDA ALEATORIA/potenciasBaseBrutas-DEMALEAT.txt";
		String archSalidas =  "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\PRUEBAS\\DEMANDA ALEATORIA/potenciasBase-DEMALEAT.prn";
		ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(archEntradas);	
		ArrayList<ArrayList<String>> salida = new ArrayList<ArrayList<String>>();
		int cantHoras = 8760;
		int fila = 0;
		for(int ian=0; ian<cantAnios; ian++) {
			for(int ih=0; ih<cantHoras; ih++) {	
				System.out.println("fila " + fila);
				salida.add(new ArrayList<String>());
				salida.get(fila).add("2022");
				salida.get(fila).addAll(texto.get(fila));			
				fila++;
			}
		}
		DirectoriosYArchivos.siExisteElimina(archSalidas);
		DirectoriosYArchivos.agregaTextoSB(archSalidas, salida, " ");
		
	}
		
	

}
