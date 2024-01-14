/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoDemandaAnioBase is part of MOP.
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
import java.util.HashSet;
import java.util.Hashtable;

import control.VariableControlDE;
import datatypes.DatosPronostico;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProcEstocasticos.DatosPEBootstrapDiscreto;
import datatypesProcEstocasticos.DatosPEDemandaAnioBase;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesTiempo.DatosTiposDeDia;
import estado.VariableEstado;
import futuro.AFIncrementos;
import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;
import parque.Azar;
import persistencia.CargadorPEBootstrapDiscreto;
import persistencia.CargadorPEDemandaAnioBase;
import persistencia.CargadorTiposDeDia;
import pizarron.PizarronRedis;
import tiempo.LineaTiempo;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LeerDatosArchivo;
import utilitarios.Par;

/**
 * Implementa una demanda que toma como base las potencias de un año base,
 * en un día equivalente: feriado, día especial y si no simplemente un día de la semana (domingo a sábado)
 * y la incrementa en un factor dado por el crecimiento de unas "energías" anuales
 * 
 * Si el días es especial cuya fecha varía con el año (como viernes santo) se toma como base igual día
 * especial del año base.
 * Si es feriado cuya fecha es fija (como 1o. de mayo) se toma el mismo feriado del año base
 * Si no pasa ninguna de las anteriores, se toma el día del mismo tipo más cercano en el orden de los días
 * del año base. Los tipos se atribuyen a los días DOM, LUN, MAR, MIE, JUE, VIE, SAB. Por ejemplo 3 1 1 1 1 1 2.
 * 
 * 
 * ATENCIÓN QUE LA CLASE GregorianCalendar de Java CUENTA LOS MESES A PARTIR DE CERO !!!!!!
 * Y LOS DIAS DE LA SEMANA DE 1 A 7 EMPEZANDO EN DOMINGO=1. 
 * @author ut469262
 *
 */
public class ProcesoDemandaAnioBase extends ProcesoEstocastico {
	
	private int cantVarLeidas;  // es la cantidad de variables leídas, que luego pueden mantenerse o reducirse a una en la opción suma de variables de demanda
	private boolean sumaVariables;   // si es true, el proceso solo crea una V.A suma de las leídas
	
	private int anioHorizonteIni; // primer año del horizonte en el que se invocarán instantes, ej. 2019
	private int anioHorizonteFin; // último año del horizonte en el que se invocarán instantes, ej. 2040
	
	/**
	 * 
	 * Potencias de los anos base
	 * Clave del hashtable el año base, por ejemplo 2016.
	 * Valor:
	 * primer índice recorre días del año base
	 * segundo índice recorre "horas" del ano base (podrían ser medias horas, etc.)
	 * tercer índice recorre variables de potencia (ej. sitios distintos)
	 */
	private Hashtable<Integer, double[][][]> potenciasAniosBase;
	private double[][][] potenciasAnioBase;  // un elemento del hashtable anterior
	
	/**
	 * Para cada día de un año del horizonte, da el ordinal del día del año base elegido
	 * que le corresponde.
	 * 
	 * Primer índice recorre años del horizonte desde anioHorizonteIni hasta anioHorizonteFin
	 * Segundo índice recorre días del año horizonte de 0 a 365.
	 * Se carga al construir el proceso.
	 */
	private int[][] diaAnioBaseAsociado;
	
	
	/**
	 * El paso del proceso estocástico es la "hora".
	 * En adelante las llamamos simplemente horas.
	 * Los días son siempre los días del año.
	 */
	private int cantHoras; // cantidad de horas de un día.
	

	private DatosTiposDeDia tiposDeDia;

	/**
	 * Lista de los años base, ejemplo: 2012, 2015, 2016
	 */
	private ArrayList<Integer> aniosBase;
	
	private int anioBase;  // Año base elegido
	
	
	/**
	 * Energías relativas leídas de los distintos años que sirven para 
	 * incrementar la potencia del año base en el cociente, para cada una de 
	 * las variables de demanda, en el factor
	 * (energía(año t, demanda k)/energía(año base, demanda k))
	 */
	private Hashtable<Integer, double[]> energias;
	

	/**
	 * Factores de incremento de las potencias para pasar del año base elegido
	 * al año del horizonte
	 * clave: el año del horizonte 
	 * valor: un factor para cada variable aleatoria.
	 * El factor de incremento incluye el crecimiento de la demanda de energía y el 
	 * posible ajuste para que cierre la energía anual en el año horizonte.
	 */
	private Hashtable<Integer, double[]> factores;
	
	
	/**
	 * Potencia demandada en cada "hora" del horizonte de años
	 * luego de realizada la corrección por energía si se pidió
	 * De acá se saca la realización del proceso.
	 * Primer índice año del horizonte entre el inicial y el final
	 * Segundo índice día del año
	 * Tercer índice "hora" del año, puede no ser una hora de 3600 segundos
	 * Cuarto índice recorre las variables aleatorias de demanda si hay más de una 
	 */
	private double[][][][] potenciaDemandada;
	

	private Integer ordinalDiaAnioBase; // ordinal del día del año base referencia en la anterior invocación
	
	
	
	/**
	 * Si el proceso es creado fuera del MOP emplea los parámetros anioHorizonteIni y cantAnios, 
	 * de lo contrario serán ignorados en la creación y los valores tomados de la línea de tiempo
	 * 
	 * @param datos
	 * @param azar
	 */
	public ProcesoDemandaAnioBase(DatosPEDemandaAnioBase datos, Azar azar, int anioHorizonteIni, int cantAnios) {
//		kk ojo que si es para la simulaci�n debe estar cargado el instantecorriente inicial
//		kk y el estado inicial
		//
		super(datos.getDatGen());
		this.setAzar(azar);
		this.setNombre(datos.getNombre());
		this.setDiscretoExhaustivo(datos.isDiscretoExhaustivo());
		this.setMuestreado(datos.isMuestreado());
//		this.setEstimacionVE(datos.getEstimacionVE());
		this.setRuta(datos.getRuta());
		this.setNombrePaso(datos.getNombrePasoPE());
		this.setCantHoras(utilitarios.Constantes.SEGUNDOSXDIA/ProcesoEstocastico.durPasoDeNombreDur(this.getNombrePaso()));
		this.setCantidadInnovaciones(1);
		this.setCantVarLeidas(datos.getCantVA());
		this.setSumaVariables(datos.isSumaVar());
		if(!datos.isSumaVar()) {
			this.setNombresVarsAleatorias(datos.getNombresVA());
			this.setCantVA(datos.getCantVA());		
		}else {
			ArrayList<String> aux = new ArrayList<String>();
			aux.add(datos.getNombre_var_suma());
			this.setNombresVarsAleatorias(aux);
			this.setCantVA(1);
		}
		this.setNombresVarsEstado(new ArrayList<String>()); // no hay VE queda vac�o
	
		this.setCantVE(datos.getCantVE());
//		this.setCantVEOptim(0);
		
		this.completaConstruccion();
		
		this.setAnioHorizonteIni(datos.getAnioInicialHorizonte());
		this.setAnioHorizonteFin(datos.getAnioFinalHorizonte());
		this.setAnioBase(datos.getAnioBaseElegido());
		
		diaAnioBaseAsociado = new int[anioHorizonteFin-anioHorizonteIni+1][366];

		this.setEnergias(datos.getEnergias());
		this.setTiposDeDia(datos.getTiposDeDia());
				
		// Crea la tabla de ordinales asociada a cada día del horizonte
		String archOrdinales = datos.getRuta()+"/ordinalesEnAnioBase.txt";
		cargaOrdinalDiaAnioBaseAsociado(archOrdinales, false, anioHorizonteIni, cantAnios);
				
		// Lee las potencias de los años base para las todas las variables
		String archPot = datos.getArchPotencias();
		leePotenciasBase(archPot);
		
		// Calcula factores de crecimiento
		calculaFactores();
		
	}
	
	

	/**
	 * Lee las potencias de los años base y crea y carga 
	 * Hashtable<Integer, double[][]> potenciasAniosBase;
	 * 
	 * Clave del hashtable el año base, por ejemplo 2016.
	 * Valor:
	 * primer índice recorre días del año base
	 * segundo índice recorre "horas" del año base (podrían ser medias horas, etc.)
	 * tercer índice recorre variables de demanda
	 */
	public void leePotenciasBase(String archPot){
	    ArrayList<ArrayList<String>> texto;		
	    texto = LeerDatosArchivo.getDatos(archPot);	
	    potenciasAniosBase = new Hashtable<Integer, double[][][]>(); 
	    int anio = -1;
	    int hora = 0;
	    int i = 0;
	    double[][][] pot1Anio = null;
    	GregorianCalendar cal=null;
    	int ordinalDiaAnioBase = 0;
    	boolean inicio = true;
    	boolean leyoAnioBase = false;
	    while(i<texto.size()){
	    	int anioLeido = Integer.parseInt(texto.get(i).get(0)); 
	    	if(anioLeido!=anio){
	    		if(!inicio) potenciasAniosBase.put(anio, pot1Anio);
	    		anio = anioLeido;
	    		hora = 0;
	    		cal = new GregorianCalendar(anio, 0, 1);
	    		pot1Anio = new double[366][cantHoras][this.getCantVarLeidas()];
	    		
	    	}
	    	if(anioLeido==anioBase) leyoAnioBase = true;
	    	int mesLeido = Integer.parseInt(texto.get(i).get(1)); 
	    	int diaLeido = Integer.parseInt(texto.get(i).get(2));
	    	int horaLeida = Integer.parseInt(texto.get(i).get(3));
	    	if(mesLeido != cal.get(Calendar.MONTH) + 1   
	    	   || diaLeido != cal.get(Calendar.DAY_OF_MONTH)
	    	   || horaLeida != hora){
	    		System.out.println("Error en lectura de potencias del a�o base, fecha: año " + anioLeido + " " + mesLeido + " " + diaLeido + " " + horaLeida);
	    		if (CorridaHandler.getInstance().isParalelo()){
				//	PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
	    		System.exit(0);
	    	}
	    	ordinalDiaAnioBase = cal.get(Calendar.DAY_OF_YEAR)-1;
//	    	System.out.println("anio\t" + anio + "\tmes\t" + mesLeido + "\tdia\t" + diaLeido + "\thora\t" + horaLeida + "\tordinalDiaAnioBase\t" + ordinalDiaAnioBase);
	    	for(int iv=0; iv<this.getCantVarLeidas(); iv++){
	    		pot1Anio[ordinalDiaAnioBase][hora][iv]=Double.parseDouble(texto.get(i).get(4+iv));
	    	}
	    	
	    	hora ++;
	    	if(hora>=cantHoras){
	    		hora = 0;
	    		cal.add(Calendar.DAY_OF_YEAR, 1);
	    	}
	    	i++;
		    potenciasAniosBase.put(anio, pot1Anio);	
	    }
	    if(leyoAnioBase==false){
	    	System.out.println("No había potencias para el año base");
	    	if (CorridaHandler.getInstance().isParalelo()){
			//	PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
	    	System.exit(0);
	    }
	    potenciasAnioBase = potenciasAniosBase.get(anioBase);
    
	}
	
	

	/**
	 * Carga diaAnioBaseAsociado
	 * 	
	 * Carga el ordinal del día del año base asociado a cada día del horizonte de prediccion
	 * el ordinal en el año base EMPIEZA EN CERO.
	 * 
	 * @param anioHorizonteIni se usa si el Azar es nulo porque el proceso no se
	 * está creando desde el MOP; de lo contrario se ignora.
	 * @param cantAnios idem.
	 * @param archOrdinal es el archivo donde se guardan los ordinales en el año si el parámetro imprime es true 
	 * base de todos los días del horizonte de predicción
	 */
	public void cargaOrdinalDiaAnioBaseAsociado(String archOrdinales, boolean imprime, int anioHorizonteIni, int cantAnios){
		
		if(imprime){
			boolean existe = DirectoriosYArchivos.existeArchivo(archOrdinales);
	        if(existe) DirectoriosYArchivos.eliminaArchivo(archOrdinales);
	        DirectoriosYArchivos.agregaTexto(archOrdinales, "ORDINALES DE DÍAS ASOCIADOS EN EL AÑO BASE " + anioBase);
		}
		// si se está creando el proceso desde el MOP se sobreescriben los parámetros anioHorizonteIni y cantAnios
		if(this.getAzar()!= null){
			LineaTiempo lt = this.getAzar().getCorrida().getLineaTiempo();
			anioHorizonteIni = this.getAnioHorizonteIni();
			cantAnios = anioHorizonteFin-anioHorizonteIni+1;
		}
		diaAnioBaseAsociado = new int[anioHorizonteFin-anioHorizonteIni+1][366];
		int cantDiasB = 365;
		GregorianCalendar calBase = new GregorianCalendar(anioBase, Calendar.JANUARY, 1);
		if(calBase.isLeapYear(anioBase)) cantDiasB = 366;
		// Recorre todos los días del horizonte		
		for(int ian=0; ian<cantAnios; ian++){
			int anio = anioHorizonteIni + ian;
			GregorianCalendar cal = new GregorianCalendar(anio, Calendar.JANUARY, 1);
			int cantDias = 365;
			if(cal.isLeapYear(anio)) cantDias = 366;
			for(int idia=0; idia<cantDias; idia++){	
				int ordinal = -1;
				int mes = cal.get(Calendar.MONTH)+1;
				int diaMes = cal.get(Calendar.DAY_OF_MONTH);
				int diaDelAnio = cal.get(Calendar.DAY_OF_YEAR);
				int clave = anio*10000 + mes*100+ cal.get(Calendar.DAY_OF_MONTH);
				String nombreDiaEspecial = tiposDeDia.getDiasEspecialesHorizonte().get(clave);						
				if(nombreDiaEspecial!=null){
					// El día es una fecha especial del horizonte
					ordinal = tiposDeDia.getOrdinalDiasEspecialesEnAnioBase().get(anioBase + nombreDiaEspecial);
				}else{
					// El dia es un feriado que no cambia de fecha o un día común
					String claveFe = CargadorTiposDeDia.claveFeriadosComunes(mes, diaMes);
					Par pfe = tiposDeDia.getFeriadosComunes().get(claveFe); 
					int mesB = mes;  // mes en el año base
					int diaMesB = diaMes;  // día del mes en el año base
					if(pfe != null){
						// El dia es un feriado que no cambia de fecha
						mesB = pfe.getInt1();
						diaMesB = pfe.getInt2();
						GregorianCalendar diaFer = new GregorianCalendar(anioBase, mesB-1, diaMesB);
						ordinal = diaFer.get(Calendar.DAY_OF_YEAR)-1;				
					}else {
						// Es un día común porque no se encuentra en las tablas
						// de días especiales ni de feriados que no cambian
						int codigoDia = cal.get(Calendar.DAY_OF_WEEK)-1; 
						int tipoDia = tiposDeDia.getTiposDiasSemana()[codigoDia];
						// Busca el día más cercano de igual ordinal o posterior en el año base, que no sea feriado y tenga el mismo tipoDia						
						int claveBase = anioBase*10000 + mesB*100 + diaMesB;
						calBase = new GregorianCalendar(anioBase, mesB-1, diaMesB);
						int desplazamiento = 1;   
						while(tiposDeDia.getDiasEspecialesHorizonte().get(claveBase)!=null 
								|| tiposDeDia.getTiposDiasSemana()[calBase.get(Calendar.DAY_OF_WEEK)-1]!=tipoDia
								|| tiposDeDia.getFeriadosComunes().contains(new Par(calBase.get(Calendar.MONTH)+1, calBase.get(Calendar.DAY_OF_MONTH)))){
							// sigue iterando porque el dia de calBase es especial o no es del tipoDia adecuado o es un feriado
							if(calBase.get(Calendar.DAY_OF_YEAR)==cantDiasB) desplazamiento = -1; // si se le acaba el año base empieza a retroceder
							calBase.add(Calendar.DAY_OF_YEAR, desplazamiento);
							mesB = calBase.get(Calendar.MONTH)+1;
							diaMesB = calBase.get(Calendar.DAY_OF_MONTH);
							claveBase = anioBase*10000 +  mesB*100 + diaMesB;
						}
						ordinal = calBase.get(Calendar.DAY_OF_YEAR)-1;						
					}
				}
				diaAnioBaseAsociado[ian][idia] = ordinal;
				cal.add(Calendar.DAY_OF_YEAR, 1);
				if(imprime){
					StringBuilder sb = new StringBuilder();
					sb.append(anio);
					sb.append("\t");
					sb.append(mes);
					sb.append("\t");
					sb.append(diaMes);
					sb.append("\tOrdinal\t");
					sb.append(idia);				
					sb.append("\tOrdinalEnAnioBase\t");
					sb.append(ordinal);
			        DirectoriosYArchivos.agregaTexto(archOrdinales, sb.toString());
				}
			}
		}		
	}
	
	
	
	
	
	/**
	 * Calcula el factor para normalizar las potencias para mantener la energía anual
	 * Multiplica todas las potencias de un año para 
	 * que la energía anual cierre con la leída
	 */
	public void calculaFactores(){
		int cantAnios = anioHorizonteFin - anioHorizonteIni + 1;
		factores = new Hashtable<Integer, double[]>();
		for(int ian=0; ian<cantAnios; ian++){
			double[] auxFac = new double[this.getCantVarLeidas()];
			double[] sumaEner = new double[this.getCantVarLeidas()];  // acumula energías en GWh
			int anio = anioHorizonteIni + ian;
			GregorianCalendar cal = new GregorianCalendar(anioHorizonteIni, Calendar.JANUARY, 1);
			int cantDias = 365;
			if(cal.isLeapYear(anio)) cantDias = 366;
			for(int idia=0; idia<cantDias; idia++){
				for(int ih=0; ih<cantHoras; ih++){
					for(int iv=0; iv<this.getCantVarLeidas(); iv++){
						int diaBase = diaAnioBaseAsociado[ian][idia];
						sumaEner[iv] += potenciasAnioBase[diaBase][ih][iv]*energias.get(anio)[iv]*this.getDurPaso()
								/(energias.get(anioBase)[iv]*utilitarios.Constantes.SEGUNDOSXHORA*1000);						
					}					
				}			
			}
			
			for(int iv=0; iv<this.getCantVarLeidas(); iv++){
				if(sumaEner[iv]!=0) {
					auxFac[iv] = energias.get(anio)[iv]*energias.get(anio)[iv]/(energias.get(anioBase)[iv]*sumaEner[iv]);				
				}else {
					auxFac[iv]=0.0;
				}
			}
			factores.put(anio, auxFac);
		}
	
		
		
	}
	
	
	@Override
	public void producirRealizacionSinPronostico(long instante) {		
//		int indAnio = this.indiceDelAnioDeInstante(instante);
		int anio = this.anioDeInstante(instante);
		if(anio>anioHorizonteFin){
			System.out.println("Se pidió una realización del proceso " +
					this.getNombre() + "para el año " + anio + " posterior al año final del horizonte de ese proceso");
		}
		int horaDelAnio = this.pasoDelAnio(instante); // las horas del año empiezan en cero
		int diaDelAnio = horaDelAnio/cantHoras;  // los días del año empiezan en el cero
		int horaDelDia = horaDelAnio%cantHoras;  // las horas del día empiezan en cero
		int indAnio = anio - anioHorizonteIni;
		int ordinalBase = diaAnioBaseAsociado[indAnio][diaDelAnio];	
		double[] facAnio = factores.get(anio);
		if(!this.isSumaVariables()) {
			for(int iv=0; iv<this.getCantVA(); iv++){
				double pot = potenciasAnioBase[ordinalBase][horaDelDia][iv]*facAnio[iv];
				this.getVariablesAleatorias().get(iv).setValor(pot);
			}
		}else {
			// El proceso tiene una única V.A que es la suma de las variables leidas
			double sum = 0.0;
			for(int iv=0; iv<this.getCantVarLeidas(); iv++){
				double pot = potenciasAnioBase[ordinalBase][horaDelDia][iv]*facAnio[iv];
				sum+= pot;
			}
			this.getVariablesAleatorias().get(0).setValor(sum); // solo hay una V.A de ordinal 0 en el vector.
		}
	}


//	@Override
//	public Hashtable<String, VariableEstado> dameEstadoAgregado(ArrayList<String> nomVarEstado, String modoReduccion) {
//		// TODO Auto-generated method stub
//		return null;
//	}


	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean tieneVEOptim() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void preparaUnSorteoMontecarloPEsinVEOptim(int isort) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void prepararPasoOptim(int cantSortMontecarlo) {
		// TODO Auto-generated method stub
		
	}

	
	public boolean isSumaVariables() {
		return sumaVariables;
	}

	public void setSumaVariables(boolean sumaVariables) {
		this.sumaVariables = sumaVariables;
	}

	public int getAnioHorizonteIni() {
		return anioHorizonteIni;
	}

	public void setAnioHorizonteIni(int anioHorizonteIni) {
		this.anioHorizonteIni = anioHorizonteIni;
	}

	public int getAnioHorizonteFin() {
		return anioHorizonteFin;
	}

	public void setAnioHorizonteFin(int anioHorizonteFin) {
		this.anioHorizonteFin = anioHorizonteFin;
	}

	public Hashtable<Integer, double[][][]> getPotenciasAniosBase() {
		return potenciasAniosBase;
	}

	public void setPotenciasAniosBase(Hashtable<Integer, double[][][]> potenciasAniosBase) {
		this.potenciasAniosBase = potenciasAniosBase;
	}

	public double[][][] getPotenciasAnioBase() {
		return potenciasAnioBase;
	}

	public void setPotenciasAnioBase(double[][][] potenciasAnioBase) {
		this.potenciasAnioBase = potenciasAnioBase;
	}

	public int[][] getDiaAnioBaseAsociado() {
		return diaAnioBaseAsociado;
	}

	public void setDiaAnioBaseAsociado(int[][] diaAnioBaseAsociado) {
		this.diaAnioBaseAsociado = diaAnioBaseAsociado;
	}

	public int getCantHoras() {
		return cantHoras;
	}

	public void setCantHoras(int cantHoras) {
		this.cantHoras = cantHoras;
	}

	public ArrayList<Integer> getAniosBase() {
		return aniosBase;
	}

	public void setAniosBase(ArrayList<Integer> aniosBase) {
		this.aniosBase = aniosBase;
	}

	public int getAnioBase() {
		return anioBase;
	}

	public void setAnioBase(int anioBase) {
		this.anioBase = anioBase;
	}

	public Hashtable<Integer, double[]> getEnergias() {
		return energias;
	}

	public void setEnergias(Hashtable<Integer, double[]> energias) {
		this.energias = energias;
	}

	public Integer getOrdinalDiaAnioBase() {
		return ordinalDiaAnioBase;
	}

	public void setOrdinalDiaAnioBase(Integer ordinalDiaAnioBase) {
		this.ordinalDiaAnioBase = ordinalDiaAnioBase;
	}

	public Hashtable<Integer, double[]> getFactores() {
		return factores;
	}

	public void setFactores(Hashtable<Integer, double[]> factores) {
		this.factores = factores;
	}

	public double[][][][] getPotenciaDemandada() {
		return potenciaDemandada;
	}

	public void setPotenciaDemandada(double[][][][] potenciaDemandada) {
		this.potenciaDemandada = potenciaDemandada;
	}

	public int getCantVarLeidas() {
		return cantVarLeidas;
	}

	public void setCantVarLeidas(int cantVarLeidas) {
		this.cantVarLeidas = cantVarLeidas;
	}	

	public DatosTiposDeDia getTiposDeDia() {
		return tiposDeDia;
	}

	public void setTiposDeDia(DatosTiposDeDia tiposDeDia) {
		this.tiposDeDia = tiposDeDia;
	}


	public static void main(String[] args) {
		String ruta = "D:/Proyectos/modelopadmin/resources/anioBaseSemanaMayo23ConVE_45PF-PRUEBA-ERROR";
		
		Hashtable<String, Double> estadosIniciales = new Hashtable<String, Double>();
		Hashtable<String, DatosPronostico> pronosticos = new Hashtable<String, DatosPronostico>();
		
		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico("anioBaseSemanaMayo23ConVE_45PF-PRUEBA-ERROR","NADA", "NADA", 
				ruta, false, false, estadosIniciales, pronosticos);
				
		DatosPEDemandaAnioBase dpd = CargadorPEDemandaAnioBase.devuelveDatosPEDemandaBase(dpe);
		ProcesoDemandaAnioBase procD = new ProcesoDemandaAnioBase(dpd, null, 2020, 11);
		System.out.println("Construyó el Proceso");
		
		
	}






}

