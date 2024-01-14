/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoDemandaEscenarios is part of MOP.
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import datatypesProcEstocasticos.DatosPEDemandaEscenarios;
import datatypesTiempo.DatosTiposDeDia;
import estado.VariableEstado;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import tiempo.AsignadorDiasEnAnioBase;
import tiempo.LineaTiempo;
import utilitarios.DirectoriosYArchivos;
import utilitarios.Par;

public class ProcesoDemandaEscenarios extends ProcesoEstocastico {
	
	private int[] cantEscTot;  // cantidad de escenarios totales
	private int[] cantEsc;   // Se usan los primeros cantEsc escenarios de los totales
	private int cantEscMax; // máxima cantidad entre las de cantEsc
	private int anioBaseInicial;   // año del primer paso de los escenarios, ejemplo: 2016.
	private int anioBaseFinal;   // año del primer paso de los escenarios, ejemplo: 2016.
	private int cantAnios;
	private int cantVarLeidas;   // cantidad de variables con datos, eventualmente se usa su suma
	private ArrayList<String> nombresVarsLeidas;
	private boolean sumaVariables;   // si es true, el proceso solo crea una V.A suma de las leídas

	private int anioSimInicial;
	private int anioSimFinal;
	
	/**
	 * Los números de escenarios que quedan sorteados al comienzo del paso, que se numeran
	 * a partir de 1 !!!
	 */
	private int[] escenariosSorteados;  
	private int isort; // indice de sorteos correlativo con el empleado en los Montecarlos
	
	private static final int CANT_HORAS_ANIO = 8760;
	private static final int CANT_HORAS_ANIO_BISIESTO = 8784;
	private static final int DUR1HORA_EN_SEGS = 3600; 	
	
	private static int cantHorasDia;
	private int iFila = 0;
	private int iCol = 0;
	private int iVA = 0;
	private int iVE = 0;
	
	static {
		cantHorasDia = utilitarios.Constantes.SEGUNDOSXDIA/DUR1HORA_EN_SEGS;
	}
	
	/**
	 * clave: nombre de la VA
	 * 
	 * Valor:
	 * primer índice año a partir de anioBaseInicial
	 * segundo índice escenario 
	 * tercer índice recorre el ordinal del paso en el año, por ejemplo las semanas, horas, etc.
	 * 
	 */
	private Hashtable<String, double[][][]>  potencias;  // potencias en MW
	
	
	/**
	 * Para cada año con potencias base, la energía esperada de la demanda en el año.
	 * clave:  String: nombre de la V.A + String(año)
	 * valor: energía esperada.
	 */	
	private Hashtable<String, Double> energiaEsperadaBase; 
	

	private Hashtable<String, Double> energias; // las energias esperadas de años a generar
	
//	private boolean corrigeTiposDia; // False: elige el dia de igual ordinal; True: elige el día del mismo tipo más cercano en ordinal
	
	
	/**
	 * Clave: construida a partir de anioBase y anioSim con el método claveEnAniosBase
	 * Valor: para cada día del anioSim el ordinal del anioBase asociado
	 */
	private Hashtable<String, int[]> ordinalesEnAniosBase; 
	
	private DatosTiposDeDia tiposDeDia;
	
	
	public ProcesoDemandaEscenarios(DatosPEDemandaEscenarios dat) {
		super(dat.getDatGen());
		this.setMuestreado(dat.isMuestreado());
		this.setDiscretoExhaustivo(dat.isDiscretoExhaustivo());	
		this.anioBaseInicial = dat.getAnioBaseInicial();
		this.anioBaseFinal = dat.getAnioBaseFinal();
		this.anioSimInicial = dat.getAnioSimInicial();
		this.anioSimFinal = dat.getAnioSimFinal();
		this.cantEsc = dat.getCantEsc();
		cantEscMax = utilitarios.UtilArrays.maximo(this.cantEsc);
		this.potencias = dat.getPotencias();
		this.energias = dat.getEnergias();
		this.energiaEsperadaBase = dat.getEnergiaEsperadaDatos();
		this.tiposDeDia = dat.getTiposDeDia();
//		this.corrigeTiposDia = dat.isCorrigeTiposDia();
		this.setSumaVariables(dat.isSumaVar());
		this.setCantVarLeidas(dat.getCantVarLeidas());
		this.setCantidadInnovaciones(1);
		
		AsignadorDiasEnAnioBase asig = new AsignadorDiasEnAnioBase(anioBaseInicial, anioBaseFinal, anioSimInicial, anioSimFinal, tiposDeDia);
		asig.cargaOrdinalesEnAniosBase("D:/salidasModeloOp/DEMANDA-ESCENARIOS/ordinalesPrueba.xlt", false);
		this.setOrdinalesEnAniosBase(asig.getOrdinalesEnAniosBase());
		this.setNombresVarsLeidas(dat.getNombresVA());
		if(!dat.isSumaVar()) {
			this.setNombresVarsAleatorias(dat.getNombresVA());			
			this.setCantVA(dat.getCantVA());		
		}else {
			ArrayList<String> aux = new ArrayList<String>();
			aux.add(dat.getNombre_var_suma());
			this.setNombresVarsAleatorias(aux);
			this.setCantVA(1);
		}
		completaConstruccion();
	}
	
	
	
	/**
	 * Devuelve un String que sirve de clave para encontrar las listas de ordinales en un anioBase
	 * de un año anioSim de simulación del proceso
	 * @param anioBase
	 * @param anioSim
	 * @return
	 */
	public static String claveEnAniosBase(int anioBase, int anioSim) {
		return Integer.toString(anioBase) + "-" + Integer.toString(anioSim);
	}
	
	
	
	/**
	 * Devuelve un String que sirve de clave para encontrar la energía de una variable aleatoria en un año dado
	 * para buscar en los Hastable energias y energiaEsperadaBase
	 * @return
	 */
	public static String claveEnergias(String nombreVA, int anio) {
		String clave = nombreVA + Integer.toString(anio);
		return clave;
	}
	
	
	public static int claveDiasEsp(int anioE, int mesE, int diaE) {
		int clave = anioE*10000+mesE*100+diaE;
		return clave;
	}

	
//	/**
//	 * Carga la tabla 	ordinalesEnAniosBase
//	 * Clave: construida a partir de anioBase y anioSim con el método claveEnAniosBase
//	 * Valor: para cada día del anioSim el ordinal del anioBase asociado
//	 *
//	 * @param archOrdinales
//	 * @param imprime
//	 */
//	public void cargaOrdinalesEnAniosBase(String archOrdinales, boolean imprime){
//		ordinalesEnAniosBase = new Hashtable<String, int[]>();
//		if(imprime){
//			boolean existe = DirectoriosYArchivos.existeArchivo(archOrdinales);
//	        if(existe) DirectoriosYArchivos.eliminaArchivo(archOrdinales);
//	        DirectoriosYArchivos.agregaTexto(archOrdinales, "ORDINALES DE DIAS ASOCIADOS EN EL ANIO BASE ");
//		// si se está creando el proceso desde el MOP se sobreescriben los parámetros anioHorizonteIni y cantAnios
//		}
//		
//		// Recorre todos los años base posibles y todos los anioSim posibles
//		for(int anioBase=anioBaseInicial; anioBase<=anioBaseFinal; anioBase++) {
//			for(int anioSim = anioSimInicial; anioSim<=anioSimFinal; anioSim++) {
//				if(anioSim==anioBase || (anioBase==anioBaseFinal && anioSim>anioBaseFinal) || (anioSim<anioBaseInicial && anioBase==anioBaseInicial)) {
//			        DirectoriosYArchivos.agregaTexto(archOrdinales, "ANIO SIMULADO\t" + anioSim + "\tANIO BASE\t" + anioBase);
//					int[] diaAnioBaseAsociado = new int[366];
//					StringBuilder sb = new StringBuilder();
//					String clave = claveEnAniosBase(anioBase, anioSim);
//					// Recorre todos los días del año simulado anioSim	
//
//					GregorianCalendar cal = new GregorianCalendar(anioSim, Calendar.JANUARY, 1);
//					int cantDias = 365;
//					if(cal.isLeapYear(anioSim)) cantDias = 366;
//					for(int idia=0; idia<cantDias; idia++){	
//						int mes = cal.get(Calendar.MONTH)+1;
//						int diaMes = cal.get(Calendar.DAY_OF_MONTH);
//						int diaDelAnio = cal.get(Calendar.DAY_OF_YEAR);
//						int ordinal = -1;						
//						if(anioBase==anioBaseFinal && anioSim>anioBaseFinal) {
//							int claveDia = claveDiasEsp(anioSim, mes, cal.get(Calendar.DAY_OF_MONTH));
//							String nombreDiaEspecial = tiposDeDia.getDiasEspecialesHorizonte().get(clave);						
//							if(nombreDiaEspecial!=null){
//								// El día es una fecha especial del horizonte
//								ordinal = tiposDeDia.getOrdinalDiasEspecialesEnAnioBase().get(anioBase+nombreDiaEspecial);
//							}else{
//								Par par = new Par(mes, diaMes);
//								if(tiposDeDia.getFeriadosComunes().contains(par)){
//									GregorianCalendar diaFer = new GregorianCalendar(anioBase, mes-1, diaMes);
//									ordinal = diaFer.get(Calendar.DAY_OF_YEAR)-1;
//								}
//								// si el día es un feriado ya queda determinado su ordinal
//								if(ordinal<0){
//									// El día no es un feriado porque no se encuentra en la tabla
//									// Es un día común	
//									// Se encuentra el código de día de lunes a viernes del nuevo día
//									// salteando los días especiales
//									int codigoDia = cal.get(Calendar.DAY_OF_WEEK)-1; 
//									int tipoDia = tiposDeDia.getTiposDiasSemana()[codigoDia];
//									// Busca el día más cercano de igual ordinal o posterior en el año base, del mismo tipo	
//									// a menos que se termine el año, en cuyo caso busca en ordinales anteriores en el año base
//									int claveDiaEsp = claveDiasEsp(anioBase, mes, cal.get(Calendar.DAY_OF_MONTH));
//									GregorianCalendar calBase = new GregorianCalendar(anioBase, mes-1, diaMes);
//									int desplazamiento = 1;   
//									while(tiposDeDia.getDiasEspecialesHorizonte().get(claveDiaEsp)!=null 
//											|| tiposDeDia.getTiposDiasSemana()[calBase.get(Calendar.DAY_OF_WEEK)-1]!=tipoDia
//											|| tiposDeDia.getFeriadosComunes().contains(new Par(calBase.get(Calendar.MONTH)+1, calBase.get(Calendar.DAY_OF_MONTH)))){
//										// sigue iterando porque el dia de calBase es especial o no es del tipoDia adecuado o es un feriado
//										if(calBase.get(Calendar.DAY_OF_YEAR)==cantDias) desplazamiento = -1; // si se le acaba el año base empieza a retroceder
//										calBase.add(Calendar.DAY_OF_YEAR, desplazamiento);
//										int mesB = calBase.get(Calendar.MONTH)+1;
//										int diaMesB = calBase.get(Calendar.DAY_OF_MONTH);
//										claveDiaEsp = claveDiasEsp(anioBase, mesB, diaMesB);
//									}
//									ordinal = calBase.get(Calendar.DAY_OF_YEAR)-1;						
//								}
//							}
//						diaAnioBaseAsociado[idia] = ordinal;
//						}else if(anioSim==anioBase) {
//							diaAnioBaseAsociado[idia] = idia;
//						}				
//						
//						cal.add(Calendar.DAY_OF_YEAR, 1);
//						ordinalesEnAniosBase.put(clave, diaAnioBaseAsociado);
//						if(imprime){						
//							sb.append(anioSim);
//							sb.append("\t");
//							sb.append(mes);
//							sb.append("\t");
//							sb.append(diaMes);
//							sb.append("\tOrdinal\t");
//							sb.append(idia);				
//							sb.append("\tOrdinalEnAnioBase\t");
//							sb.append(diaAnioBaseAsociado[idia]);
//							sb.append("\n");
//						}						
//					}
//					DirectoriosYArchivos.agregaTexto(archOrdinales, sb.toString());
//				}
//			}
//		}
//	}

	
	
	/**
	 * Si el instante es anterior al inicio del primer paso de tiempo 
	 * de los escenarios, el proceso da error
	 * En la simulación el proceso repite circularmente sus propios escenarios
	 * Ejemplo: si tiene 105 escenarios del 1 al 105, el escenario 106 repite el 1, y as� sucesivamente
	 * 
	 */
	@Override
	public void producirRealizacionSinPronostico(long instante) {
		if(optim){
			// Si el método es invocado desde la optimización se carga uno de los escenarios
			// sorteados, en secuencia circular 0,.....,cantSorteos-1,0,......
			this.setEscenario(escenariosSorteados[isort]+1); // suma 1 porque los escenarios de simulación empiezan en 1			
		}
		int anioCorriente = anioDeInstante(instante);  // anio del instante pedido
		int anioBase = anioCorriente;
		int ip = pasoDelAnio(instante);   // usa el método pasoDelAnio de la clase padre ProcesoEstocastico
		int ian = anioCorriente - anioBaseInicial;
		if(ian<0){
			anioBase = anioBaseInicial;
			ian = 0;
		}
		double sum = 0.0; // Si sumaVariables == true acumulará la suma
		for(int iv=0; iv<cantVarLeidas; iv++) {
			String nombreVA = getNombresVarsLeidas().get(iv);
			int iescva = (this.getEscenario()-1)%cantEsc[iv];		
			if(anioCorriente>anioBaseFinal) {
				ian = anioBaseFinal-anioBaseInicial;
				anioBase = anioBaseFinal; 
				iescva = (this.getEscenario() -1 + anioCorriente - anioBaseFinal)%cantEsc[iv];
			}			
	

			int ordinalDiaAnioSim = ip/cantHorasDia;  // cociente entero,comenzando en 0
			int hora = ip-ordinalDiaAnioSim*cantHorasDia;
			String clave = claveEnAniosBase(anioBase, anioCorriente);
			ip = ordinalesEnAniosBase.get(clave)[ordinalDiaAnioSim]*cantHorasDia + hora;			
			
			
			if(!this.isSumaVariables()) {				
				clave = claveEnergias(nombreVA, anioCorriente);
				String claveBase = claveEnergias(nombreVA, anioBase);
				this.getVariablesAleatorias().get(iv).setValor(potencias.get(nombreVA)[ian][iescva][ip]*energias.get(clave)/energiaEsperadaBase.get(claveBase));
			
			}else {
				// El proceso tiene una única V.A que es la suma de las variables leidas
				clave = claveEnergias(this.getNombresVarsLeidas().get(iv), anioCorriente);
				String claveBase = claveEnergias(this.getNombresVarsLeidas().get(iv), anioBase);
				double pot = potencias.get(nombreVA)[ian][iescva][ip]*energias.get(clave)/energiaEsperadaBase.get(claveBase);
				sum+= pot;
//				System.out.println("sumando de 1 VA\r" + pot);
			}	
		}
		if(this.isSumaVariables())this.getVariablesAleatorias().get(0).setValor(sum); // solo hay una V.A de ordinal 0 en el vector.
//		System.out.println("potencia sumada\r" + sum);
	}
	
	
	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	/**
	 * Carga atributo optim y determina los escenarios sorteados para
	 * ser usados en la optimización
	 * @param cantSorteos
	 */
	public void prepararPasoOptim(int cantSorteos){
		optim = true;
		escenariosSorteados = new int[cantSorteos];
		for(int is=0; is<cantSorteos; is++){
			GeneradorDistUniforme gdu = this.getGeneradoresAleatorios().get(0); // hay un único generador de innovaciones
			// Se suma uno porque los escenarios van del 1 en adelante en el ProcesoEscenario
			// y devuelve ordinal va de 0 en adelante.
			escenariosSorteados[is] = gdu.devuelveOrdinal(cantEscMax)+1;
			if(escenariosSorteados[is]<0){
				System.out.println("un escenario es negativo");
			}
		} 
	}

	public void prepararPaso(){
	
	}


	@Override
	public boolean tieneVEOptim() {
		// TODO Auto-generated method stub
		return false;
	}



	public int[] getCantEscTot() {
		return cantEscTot;
	}

	public void setCantEscTot(int[] cantEscTot) {
		this.cantEscTot = cantEscTot;
	}

	public int[] getCantEsc() {
		return cantEsc;
	}

	public void setCantEsc(int[] cantEsc) {
		this.cantEsc = cantEsc;
	}

	

	public void setPotencias(Hashtable<String, double[][][]> potencias) {
		this.potencias = potencias;
	}

	public int getCantAnios() {
		return cantAnios;
	}


	public void setCantAnios(int cantAnios) {
		this.cantAnios = cantAnios;
	}


	public int[] getEscenariosSorteados() {
		return escenariosSorteados;
	}


	public void setEscenariosSorteados(int[] escenariosSorteados) {
		this.escenariosSorteados = escenariosSorteados;
	}


	public int getIsort() {
		return isort;
	}


	public void setIsort(int isort) {
		this.isort = isort;
	}


	public int getiFila() {
		return iFila;
	}


	public void setiFila(int iFila) {
		this.iFila = iFila;
	}


	public int getiCol() {
		return iCol;
	}


	public void setiCol(int iCol) {
		this.iCol = iCol;
	}


	public int getiVA() {
		return iVA;
	}


	public void setiVA(int iVA) {
		this.iVA = iVA;
	}


	public int getiVE() {
		return iVE;
	}


	public void setiVE(int iVE) {
		this.iVE = iVE;
	}





	public Hashtable<String, int[]> getOrdinalesEnAniosBase() {
		return ordinalesEnAniosBase;
	}

	public void setOrdinalesEnAniosBase(Hashtable<String, int[]> ordinalesEnAniosBase) {
		this.ordinalesEnAniosBase = ordinalesEnAniosBase;
	}

	public DatosTiposDeDia getTiposDeDia() {
		return tiposDeDia;
	}

	public void setTiposDeDia(DatosTiposDeDia tiposDeDia) {
		this.tiposDeDia = tiposDeDia;
	}

	public int getAnioBaseInicial() {
		return anioBaseInicial;
	}


	public void setAnioBaseInicial(int anioBaseInicial) {
		this.anioBaseInicial = anioBaseInicial;
	}


	public int getAnioBaseFinal() {
		return anioBaseFinal;
	}


	public void setAnioBaseFinal(int anioBaseFinal) {
		this.anioBaseFinal = anioBaseFinal;
	}


	public int getAnioSimInicial() {
		return anioSimInicial;
	}


	public void setAnioSimInicial(int anioSimInicial) {
		this.anioSimInicial = anioSimInicial;
	}


	public int getAnioSimFinal() {
		return anioSimFinal;
	}


	public void setAnioSimFinal(int anioSimFinal) {
		this.anioSimFinal = anioSimFinal;
	}

	
	public Hashtable<String, double[][][]> getPotencias() {
		return potencias;
	}

	public Hashtable<String, Double> getEnergias() {
		return energias;
	}


	public void setEnergias(Hashtable<String, Double> energias) {
		this.energias = energias;
	}


	public Hashtable<String, Double> getEnergiaEsperadaBase() {
		return energiaEsperadaBase;
	}


	public void setEnergiaEsperadaBase(Hashtable<String, Double> energiaEsperadaBase) {
		this.energiaEsperadaBase = energiaEsperadaBase;
	}

	public boolean isSumaVariables() {
		return sumaVariables;
	}

	public void setSumaVariables(boolean sumaVariables) {
		this.sumaVariables = sumaVariables;
	}

	public int getCantVarLeidas() {
		return cantVarLeidas;
	}

	public void setCantVarLeidas(int cantVarLeidas) {
		this.cantVarLeidas = cantVarLeidas;
	}

	public ArrayList<String> getNombresVarsLeidas() {
		return nombresVarsLeidas;
	}

	public void setNombresVarsLeidas(ArrayList<String> nombresVarsLeidas) {
		this.nombresVarsLeidas = nombresVarsLeidas;
	}

	





	
	
	

}
