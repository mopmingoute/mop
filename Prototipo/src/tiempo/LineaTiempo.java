/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LineaTiempo is part of MOP.
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

package tiempo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;



import datatypes.DatosPostizacion;
import datatypesSalida.DatosPaso;
import datatypesTiempo.DatosLineaTiempo;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import utilitarios.Constantes;
import utilitarios.Utilitarios;

/**
 * Clase que representa la línea de tiempo asociada a la simulación o a la
 * optimización
 * 
 * @author ut602614
 *
 */

public class LineaTiempo {
	private SentidoTiempo sentidoTiempo;
	private ArrayList<PasoTiempo> linea;
	private ArrayList<BloqueTiempo> bloques;

	private GregorianCalendar inicioTiempo;
	private GregorianCalendar inicioTiempoInamovible;
	private GregorianCalendar finTiempo;

	/*
	 * Toma el valor entero representado por las constantes Calendar.MONTH,
	 * Calendar.YEAR, etc. Representa el período para el cuál se desea tener una
	 * vista
	 */
	private int periodoDeIntegracion;
	private boolean usarPeriodoIntegracion;

	private int inicial; /*
							 * tiempo inicial de la linea de tiempo expresado en segundos respecto al inicio
							 * de tiempo
							 */
	private int numPaso;
	private int bloqueActual;
	private int cantidadPasos;
	private int maxDurPaso;

	/**
	 * Instantes de inicio de cada uno de los aóos en los que la corrida se
	 * extiende. Ej. si la corrida empieza el marzo de 2020 y termina en el instante
	 * final de 2022, la corrida se extiende por 3 años
	 */

	private long[] instInicioAnio;

	/**
	 * Instantes de inicio de cada uno de los años en los que la corrida se extienda
	 * Clave año ( ejemplo 2020) Valor instante de inicio del aóo 2020
	 */
	private Hashtable<Integer, Long> instInicioAnioHT;

	/**
	 * Duración de cada año en segundos
	 */
	private Hashtable<Integer, Integer> durAnio;

	private int anioInic; // primer aóo de la corrida
	private int anioFin; // óltimo aóo de la corrida

	/**
	 * Constructor de la LineaTiempo cuando la postización es interna
	 * 
	 * @param datos
	 * @param tiempoInicialCorrida
	 */
	public LineaTiempo(DatosLineaTiempo datos, GregorianCalendar tiempoInicialCorrida,
			GregorianCalendar tiempoFinalCorrida) {
		super();
		this.sentidoTiempo = datos.getSentido();
		this.linea = new ArrayList<PasoTiempo>();
		this.bloques = new ArrayList<BloqueTiempo>();
		this.cantidadPasos = 0;
		this.inicial = datos.getInstanteInicial();

		this.inicioTiempo = Utilitarios.stringToGregorianCalendar(datos.getTiempoInicial(), "dd MM yyyy");
		this.inicioTiempoInamovible = Utilitarios.stringToGregorianCalendar(datos.getTiempoInicialEvoluciones(), "dd MM yyyy");

		long instanteInicialCorrida = dameInstante(tiempoInicialCorrida);
		ArrayList<Integer> pasosPorBloque = datos.getPasosPorBloque();
		ArrayList<Integer> periodoPorBloque = datos.getPeriodoPasoPorBloque();
		int durpaso;
		this.numPaso = 0;
		this.setBloqueActual(0);
		this.periodoDeIntegracion = datos.getPeriodoIntegracion();
		this.usarPeriodoIntegracion = datos.isUsarPeriodoIntegracion();

		maxDurPaso = 0;
		if (!usarPeriodoIntegracion) {
			for (int cbloques = 0; cbloques < datos.getCantBloques(); cbloques++) {
				durpaso = datos.getDuracionPasoPorBloque().get(cbloques);
				if (durpaso > maxDurPaso)
					maxDurPaso = durpaso;
				// if(instanteInicialCorrida > dameInstante(tiempoFinalCorrida)) break; //
				// Emiliano
				this.bloques.add(new BloqueTiempo(cbloques, this.linea.size(), pasosPorBloque.get(cbloques), durpaso,
						datos.getIntMuestreoPorBloque().get(cbloques), datos.getDurPostesPorBloque().get(cbloques),
						datos.getCronologicos().get(cbloques)));
				for (int j = 0; j < pasosPorBloque.get(cbloques); j++) {
					this.linea.add(new PasoTiempo(instanteInicialCorrida, durpaso, periodoPorBloque.get(cbloques), this,
							this.bloques.get(cbloques)));
					this.cantidadPasos++;
					instanteInicialCorrida += durpaso;
					// if(instanteInicialCorrida > dameInstante(tiempoFinalCorrida)) break; //
					// Emiliano
				}

			}
		} else {
			GregorianCalendar finIntegracion = (GregorianCalendar) inicioTiempo.clone();
			finIntegracion.add(periodoDeIntegracion, 1);
			GregorianCalendar referencia = (GregorianCalendar) inicioTiempo.clone();

			for (int cbloques = 0; cbloques < datos.getCantBloques(); cbloques++) {
				durpaso = datos.getDuracionPasoPorBloque().get(cbloques);
				// if(dameInstante(referencia) > dameInstante(tiempoFinalCorrida)) break;
				// //Emiliano
				referencia.add(Calendar.SECOND, durpaso);
				if (referencia.compareTo(finIntegracion) > 0) {
					referencia.add(Calendar.SECOND, -durpaso);
					this.bloques.add(new BloqueTiempo(cbloques, this.linea.size(), pasosPorBloque.get(cbloques) - 1,
							durpaso, datos.getIntMuestreoPorBloque().get(cbloques),
							datos.getDurPostesPorBloque().get(cbloques), datos.getCronologicos().get(cbloques)));
					this.bloques.add(new BloqueTiempo(cbloques, this.linea.size() + pasosPorBloque.get(cbloques), 1,
							(int)(durpaso + restarFechas(finIntegracion, referencia)),
							datos.getIntMuestreoPorBloque().get(cbloques), datos.getDurPostesPorBloque().get(cbloques),
							datos.getCronologicos().get(cbloques)));

				}

				for (int j = 0; j < pasosPorBloque.get(cbloques) - 1; j++) {
					this.linea.add(new PasoTiempo(instanteInicialCorrida, durpaso, periodoPorBloque.get(cbloques), this,
							this.bloques.get(bloqueActual)));
					this.cantidadPasos++;
					instanteInicialCorrida += durpaso;
				}

				referencia.add(Calendar.SECOND, durpaso);
				if (referencia.compareTo(finIntegracion) > 0) {
					referencia.add(Calendar.SECOND, -durpaso);
					durpaso = (int)(durpaso + restarFechas(finIntegracion, referencia));
					this.linea.add(new PasoTiempo(instanteInicialCorrida, durpaso, periodoPorBloque.get(cbloques), this,
							this.bloques.get(bloqueActual)));
					this.cantidadPasos++;
					instanteInicialCorrida += durpaso;
				}
				referencia = dameTiempo(instanteInicialCorrida);
			}
		}

		this.finTiempo = dameTiempo(this.linea.get(linea.size() - 1).getInstanteFinal());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(finTiempo.getTime());
		calendar.add(Calendar.HOUR_OF_DAY, -1); // numero de días a añadir, o restar en caso de días<0
		if (tiempoFinalCorrida.before(calendar)) {

			System.out.println("EL FIN CORRIDA DEBE SER IGUAL O SUPERIOR AL FINAL DE LA LINEA DE TIEMPO");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(0);
		} else {
			calendar.setTime(finTiempo.getTime());
			calendar.add(Calendar.HOUR_OF_DAY, 1); // numero de dóas a aóadir, o restar en caso de dóas<0
			if (tiempoFinalCorrida.after(calendar)) {
				System.out.println(
						"ADVERTENCIA: EL FIN CORRIDA ES POSTERIOR AL FIN DE LINEA TIEMPO, LA CORRIDA TERMINA EN EL FIN LINEA TIEMPO");
			}
		}
		int cantAnios = this.finTiempo.get(Calendar.YEAR) - this.inicioTiempo.get(Calendar.YEAR) + 1;
		instInicioAnio = new long[cantAnios + 1];
		instInicioAnioHT = new Hashtable<Integer, Long>();
		durAnio = new Hashtable<Integer, Integer>();
		GregorianCalendar ref = new GregorianCalendar(this.inicioTiempo.get(Calendar.YEAR), Calendar.JANUARY, 1);
		int j = 0;
		do {
			instInicioAnio[j] = dameInstante(ref);
			instInicioAnioHT.put(ref.get(Calendar.YEAR), dameInstante(ref));
			ref.add(Calendar.YEAR, 1);
			j++;
		} while (ref.before(finTiempo));

		instInicioAnio[j] = dameInstante(ref);
		this.anioInic = this.inicioTiempo.get(Calendar.YEAR);
		this.anioFin = this.finTiempo.get(Calendar.YEAR);
		instInicioAnioHT.put(ref.get(Calendar.YEAR), dameInstante(ref));
		for (int ian = anioInic; ian <= anioFin; ian++) {
			if (instInicioAnioHT.get(ian + 1) != null) {
				durAnio.put(ian, (int)(instInicioAnioHT.get(ian + 1) - instInicioAnioHT.get(ian)));
			}
		}
		terminaSiHayErrorDuraciones();
		int npaso = 0;
		for(PasoTiempo p: linea) {
			p.setNumpaso(npaso);
			npaso ++;
		}
	}
	
	
	
	public void terminaSiHayErrorDuraciones() {
		for(BloqueTiempo b: bloques) {
			if(!b.chequeaDurPostes()) {
				System.out.println("ERROR EN DURACIONES : Las duraciones de postes nos son multiplos del intervalo de muestreo");
				System.exit(1);
			}
		}
	}

	private DatosPaso determinarDurPosCantPosInterPos(DatosPostizacion post, int paso, int intervalo) {
		DatosPaso retorno = new DatosPaso();
		ArrayList<Integer> numpos = post.getColnumpos().get(paso);

		int cantPos = 0;

		for (Integer i : numpos) {
			if (i > cantPos) {
				cantPos = i;
			}
		}
		int[] interPorPoste = new int[cantPos];
		for (Integer i : numpos) {
			interPorPoste[i - 1] += 1;
		}

		int[] durPos = new int[cantPos];
		for (int i = 0; i < cantPos; i++) {
			durPos[i] = interPorPoste[i] * intervalo;
		}
		retorno.setCantPostes(cantPos);
		retorno.setCantIntervMuestreo(numpos.size());
		retorno.setDurPostes(durPos);
		retorno.setNumPaso(paso);
		retorno.setDurpaso(numpos.size() * intervalo);
		return retorno;
	}

	/**
	 * Constructor de la LineaTiempo cuando la postización es externa
	 *
	 */
	public LineaTiempo(DatosLineaTiempo datos, DatosPostizacion postizacion, GregorianCalendar tiempoInicialCorrida,
			GregorianCalendar tiempoFinalCorrida) {
		super();
		this.sentidoTiempo = datos.getSentido();
		this.linea = new ArrayList<PasoTiempo>();
		this.bloques = new ArrayList<BloqueTiempo>();
		this.cantidadPasos = 0;
		this.inicial = datos.getInstanteInicial();

		this.inicioTiempo = Utilitarios.stringToGregorianCalendar(datos.getTiempoInicial(), "dd MM yyyy");

		long instanteInicialCorrida = dameInstante(tiempoInicialCorrida);
		long instanteFinalCorrida = dameInstante(tiempoFinalCorrida);

		this.numPaso = 0;
		this.setBloqueActual(0);
		this.periodoDeIntegracion = datos.getPeriodoIntegracion();
		this.usarPeriodoIntegracion = datos.isUsarPeriodoIntegracion();

		boolean masPasos = true;

		maxDurPaso = 0;
		if (!usarPeriodoIntegracion) {
			/**
			 * acó hay que construir bloques lineas pasos maxdurpaso a partir de la
			 * postizacion
			 */
			long instanteInicialPaso = instanteInicialCorrida;
			ArrayList<ArrayList<Integer>> colnumpos = postizacion.getColnumpos();
			this.cantidadPasos = colnumpos.size();
			int intervalo = datos.getIntMuestreoPorBloque().get(0);
			boolean nuevoBloque = true;
			BloqueTiempo bloque = null;
			int cbloques = 0;
			int i = 0;
			while (i < cantidadPasos && masPasos) {
				DatosPaso dp = determinarDurPosCantPosInterPos(postizacion, i, intervalo);
				if (i != 0) {
					nuevoBloque = !(dp.getDurpaso() == bloques.get(bloques.size() - 1).getDuracionPaso()
							&& dp.getCantPostes() == bloques.get(bloques.size() - 1).getCantPostes()
							&& Arrays.equals(dp.getDurPostes(), bloques.get(bloques.size() - 1).getDuracionPostes()));
				}
				if (nuevoBloque) {
					bloque = new BloqueTiempo(cbloques, i, 1, dp.getDurpaso(), intervalo, dp.getDurPostes(),
							datos.getCronologicos().get(0));
					cbloques++;
					bloques.add(bloque);
				} else {
					bloque = bloques.get(bloques.size() - 1);
					bloque.setCantidadPasos(bloque.getCantidadPasos() + 1);
				}
				PasoTiempo p = new PasoTiempo(instanteInicialPaso, dp.getDurpaso(), 0, this, bloque);
				this.linea.add(p);
				instanteInicialPaso += dp.getDurpaso();
				i++;
				masPasos = instanteInicialPaso < instanteFinalCorrida;
				if (!masPasos)
					this.cantidadPasos = i;
			}

		}
		this.finTiempo = dameTiempo(this.linea.get(linea.size() - 1).getInstanteFinal());
		GregorianCalendar tiempoAnterior = dameTiempo(this.linea.get(linea.size() - 2).getInstanteFinal());
		int ultanio = (tiempoAnterior.get(Calendar.YEAR) != this.finTiempo.get(Calendar.YEAR))
				? this.finTiempo.get(Calendar.YEAR)
				: tiempoAnterior.get(Calendar.YEAR);
		int cantAnios = ultanio - this.inicioTiempo.get(Calendar.YEAR) + 1;
		instInicioAnio = new long[cantAnios + 1];
		instInicioAnioHT = new Hashtable<Integer, Long>();
		GregorianCalendar ref = new GregorianCalendar(this.inicioTiempo.get(Calendar.YEAR), Calendar.JANUARY, 1);
		int j = 0;
		for (j = 0; j < cantAnios + 1; j++) {
			instInicioAnio[j] = dameInstante(ref);
			instInicioAnioHT.put(ref.get(Calendar.YEAR), dameInstante(ref));
			ref.add(Calendar.YEAR, 1);
		}
		this.anioInic = this.inicioTiempo.get(Calendar.YEAR);
		this.anioFin = this.finTiempo.get(Calendar.YEAR);
		instInicioAnioHT.put(ref.get(Calendar.YEAR), dameInstante(ref));
		durAnio = new Hashtable<Integer, Integer>();
		for (int ian = anioInic; ian <= anioFin; ian++) {
			if (instInicioAnioHT.get(ian + 1) != null) {
				durAnio.put(ian, (int)(instInicioAnioHT.get(ian + 1) - instInicioAnioHT.get(ian)));
			}
		}
		terminaSiHayErrorDuraciones();
		int npaso = 0;
		for(PasoTiempo p: linea) {
			p.setNumpaso(npaso);
			npaso ++;
		}
	}

	public ArrayList<PasoTiempo> getLinea() {
		return linea;
	}

	public void setLinea(ArrayList<PasoTiempo> linea) {
		this.linea = linea;
	}

	public Integer getInicial() {
		return inicial;
	}

	public void setInicial(Integer inicial) {
		this.inicial = inicial;
	}

	public PasoTiempo pasoActual() {
		return linea.get(numPaso);
	}

	public GregorianCalendar getInicioTiempo() {
		return inicioTiempo;
	}

	public void setInicioTiempo(GregorianCalendar inicioTiempo) {
		this.inicioTiempo = inicioTiempo;
	}

	public int getNumPaso() {
		return numPaso;
	}

	public void setNumPaso(int numPaso) {
		this.numPaso = numPaso;
	}

	public PasoTiempo devuelvePasoActual() {
		if (numPaso < linea.size())
			return linea.get(numPaso);
		return null;
	}

	public void retrocederPaso() {
		numPaso--;
		bloqueActual = linea.get(numPaso).getBloque().getOrdinalBloque();

	}

	public void avanzarPaso() {
		numPaso++;
		if (numPaso < linea.size())
			bloqueActual = linea.get(numPaso).getBloque().getOrdinalBloque();
	}

	/**
	 * Devuelve el tiempo transcurrido entre dos fechas en segundos
	 * 
	 * @param fFin
	 * @param fIni
	 * @return
	 */
	public long restarFechas(GregorianCalendar fFin, GregorianCalendar fIni) {
		return (long) ((fFin.getTimeInMillis() - fIni.getTimeInMillis()) / 1000);
	}

	/**
	 * Devuelve el paso al que pertenece el instante (abierto al final del paso)
	 */

	public PasoTiempo damePaso(GregorianCalendar instanteTiempo) {
		long segundosDesdeInicio = restarFechas(instanteTiempo, this.getTiempoInicial());
		int primerPasoPosible = (int)(segundosDesdeInicio / maxDurPaso);
		int i = primerPasoPosible;
		while (i < this.linea.size()) {
			if (segundosDesdeInicio < this.linea.get(i).getInstanteFinal()) {
				return this.linea.get(i);
			}
			i++;
		}
		return null; // Estó fuera de la lónea de tiempo

	}
	


	/**
	 * Ajusta la linea de tiempo a peróodo de integración "estirando" el óltimo paso
	 */
	public void ajustarAPeriodoIntegracion() {

	}

	public ArrayList<BloqueTiempo> getBloques() {
		return bloques;
	}

	public void setBloques(ArrayList<BloqueTiempo> bloques) {
		this.bloques = bloques;
	}

	public int getBloqueActual() {
		return bloqueActual;
	}

	public void setBloqueActual(int bloqueActual) {
		this.bloqueActual = bloqueActual;
	}

	public GregorianCalendar getTiempoInicial() {
		return inicioTiempo;
	}

	public void setTiempoInicial(GregorianCalendar tiempoInicial) {
		this.inicioTiempo = tiempoInicial;
	}

	public int getPeriodoDeIntegracion() {
		return periodoDeIntegracion;
	}

	public void setPeriodoDeIntegracion(int periodoDeIntegracion) {
		this.periodoDeIntegracion = periodoDeIntegracion;
	}

	public int getCantidadPasos() {
		return cantidadPasos;
	}

	public void setCantidadPasos(int cantidadPasos) {
		this.cantidadPasos = cantidadPasos;
	}

	public int getMaxDurPaso() {
		return maxDurPaso;
	}

	public void setMaxDurPaso(int maxDurPaso) {
		this.maxDurPaso = maxDurPaso;
	}

	public void setInicial(int inicial) {
		this.inicial = inicial;
	}

//	public GregorianCalendar dameTiempo(int instante) {
//		int segundosTranscurridos = instante - this.inicial;
//		GregorianCalendar resultado = (GregorianCalendar) this.inicioTiempo.clone();
//		resultado.setTime(this.inicioTiempo.getTime());
//		resultado.add(Calendar.SECOND, segundosTranscurridos);
//		return resultado;
//
//	}
	
	
	public GregorianCalendar dameTiempo(long instante) {
		long segundosTranscurridos = instante - this.inicial;
		int diasAd = (int)(segundosTranscurridos/utilitarios.Constantes.SEGUNDOSXDIA);
		int segsAd = (int)(segundosTranscurridos%utilitarios.Constantes.SEGUNDOSXDIA);
		GregorianCalendar resultado = (GregorianCalendar) this.inicioTiempo.clone();
		resultado.setTime(this.inicioTiempo.getTime());
		resultado.add(Calendar.DAY_OF_YEAR, diasAd);
		resultado.add(Calendar.SECOND, segsAd);
		return resultado;
	}

	public GregorianCalendar dameTiempoParaEscibirEvolucion(long instante) {
		long segundosTranscurridos = instante - this.inicial;
		int diasAd = (int)(segundosTranscurridos/utilitarios.Constantes.SEGUNDOSXDIA);
		int segsAd = (int)(segundosTranscurridos%utilitarios.Constantes.SEGUNDOSXDIA);
		GregorianCalendar resultado = (GregorianCalendar) this.inicioTiempoInamovible.clone();
		resultado.setTime(this.inicioTiempoInamovible.getTime());
		resultado.add(Calendar.DAY_OF_YEAR, diasAd);
		resultado.add(Calendar.SECOND, segsAd);
		return resultado;
	}

	/**
	 * Devuelve 1 si la fecha y hora del instante inst1 es posterior a la fecha y hora de inst2. 
	 * Es decir se prescinde del año para comparar los instantes. Devuelve 0 si coinciden fecha y hora.
	 * 
	 * Por ejemplo si inst1 es el inicio de 10 de marzo de 2030 y inst2 es el inicio
	 * del 20 de noviembre de 2024, comparaFechasDeInstantes(int inst1, int inst2)
	 * devueltve -1
	 * 
	 * @param inst1
	 * @param inst2
	 * @return
	 */
	public int comparaFechasDeInstantes(long inst1, long inst2) {
		GregorianCalendar g1 = dameTiempo(inst1);
		GregorianCalendar g2 = dameTiempo(inst2);
		g1.set(GregorianCalendar.YEAR, 2000);
		g2.set(GregorianCalendar.YEAR, 2000);
		double c = g1.compareTo(g2);
		if (c > 0)
			return 1;
		if (c < 0)
			return -1;
		return 0;
	}


	/**
	 * Devuelve la diferencia en segundos entre las fechas de los instantes inst1 e inst2
	 * 
	 * Si la fecha y hora del instante inst1 es posterior a la fecha y hora de inst2 la cantidad de segundos
	 * es positiva.
	 * Es decir se prescinde del año para hallar la diferencia en segundos entre los instantes. 
	 * Devuelve 0 si coinciden las fechas y horas de ambos instantes.
	 * 
	 * @param inst1
	 * @param inst2
	 * @return
	 */
	public long difEnSegundosDeFechas(long inst1, long inst2) {
		GregorianCalendar g1 = dameTiempo(inst1);
		GregorianCalendar g2 = dameTiempo(inst2);
		g1.set(GregorianCalendar.YEAR, 2000);
		g2.set(GregorianCalendar.YEAR, 2000);
		long s1 = dameInstante(g1);
		long s2 = dameInstante(g2);
		return s1-s2;
	}	
	
	/**
	 * Dado el instante devuelve la fecha y hora en formato aaaa/mm/dd/hh:mm:ss
	 * Si cualquiera de las magnitudes tiene un solo dígito aparece con un solo dígito
	 * los meses empiezan en 1.
	 * @param instante
	 * @return
	 */
	public String fechaYHoraDeInstante(long instante) {
		StringBuilder sb = new StringBuilder();
		GregorianCalendar gc = dameTiempo(instante);
		sb.append(gc.get(Calendar.YEAR) + "/");
		sb.append((gc.get(Calendar.MONTH) + 1) + "/");
		sb.append(gc.get(Calendar.DAY_OF_MONTH) + "/");
		sb.append(gc.get(Calendar.HOUR) + ":");
		sb.append(gc.get(Calendar.MINUTE) + ":");
		sb.append(gc.get(Calendar.SECOND));
		return sb.toString();
	}
	

	/**
	 * Dado el instante devuelve la fecha en formato aaaa/mm/dd
	 * Si cualquiera de las magnitudes tiene un solo dígito aparece con un solo dígito
	 * los meses empiezan en 1.
	 * @param instante
	 * @return
	 */
	public String fechaDeInstante(long instante) {
		StringBuilder sb = new StringBuilder();
		GregorianCalendar gc = dameTiempo(instante);
		sb.append(gc.get(Calendar.YEAR) + "/");
		sb.append((gc.get(Calendar.MONTH) + 1) + "/");
		sb.append(gc.get(Calendar.DAY_OF_MONTH));
		return sb.toString();
	}
	
	
	/**
	 * Devuelve el instante inicial del mes del anio.
	 * @param anio
	 * @param mes (numerado de 1 a 12)
	 * @return
	 */
	public long instanteInicialAnioMes(int anio, int mes) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(anio, mes-1, 1, 0, 0, 0);
		return dameInstante(gc);
	}
	
	
	/**
	 * Dada una fecha en el formato aaaa/mm/dd/hh:mm:ss devuelve el año
	 * @param fecha
	 * @return el año asociado a la fecha
	 */
	public static int anioDeFecha(String fecha) {
		String[] aux = fecha.split("/");
		return Integer.parseInt(aux[0]);		
	}
	
	/**
	 * Dada una fecha en el formato aaaa/mm/dd/hh:mm:ss devuelve el mes
	 * @param fecha
	 * @return el mes asociado a la fecha
	 */
	public static int mesDeFecha(String fecha) {
		String[] aux = fecha.split("/");
		return Integer.parseInt(aux[1]);		
	}	

	
	/**
	 * Dada una fecha en el formato aaaa/mm/dd/hh:mm:ss devuelve el mes
	 * @param fecha
	 * @return el dia del mes asociado a la fecha
	 */
	public static int diaMesDeFecha(String fecha) {
		String[] aux = fecha.split("/");
		return Integer.parseInt(aux[2]);		
	}	
	
	
	/**
	 * Dada una fecha en el formato aaaa/mm/dd/hh:mm:ss devuelve la hora
	 * @param fecha
	 * @return la hora hh
	 */
	public static int horaDeFecha(String fecha) {
		String[] aux = fecha.split("/");
		String hhmmdd = aux[3];
		String[] aux2 = hhmmdd.split(":");
		return Integer.parseInt(aux2[0]);
	}
	
	/**
	 * Dada una fecha en el formato aaaa/mm/dd/hh:mm:ss devuelve los minutos de mm
	 * @param fecha
	 * @return los minutos mm
	 */
	public static int minDeFecha(String fecha) {
		String[] aux = fecha.split("/");
		String hhmmdd = aux[3];
		String[] aux2 = hhmmdd.split(":");
		return Integer.parseInt(aux2[1]);
	}	
		

	/**
	 * Dada una fecha en el formato aaaa/mm/dd/hh:mm:ss devuelve los minutos de mm
	 * @param fecha
	 * @return los segundos ss
	 */
	public static int segDeFecha(String fecha) {
		String[] aux = fecha.split("/");
		String hhmmdd = aux[3];
		String[] aux2 = hhmmdd.split(":");
		return Integer.parseInt(aux2[2]);
	}	
	
	/**
	 * Devuelve el instante a partir del inicio de la corrida en segundos
	 * de la fecha expresada como String en formato aaaa/mm/dd/hh:mm:ss
	 * En fecha los meses empiezan en enero = 1.
	 * @param fecha
	 * @return
	 */	
	public long dameInstanteDeFecha(String fecha) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.YEAR, anioDeFecha(fecha));
		gc.set(Calendar.MONTH, mesDeFecha(fecha)-1);
		gc.set(Calendar.DAY_OF_MONTH, diaMesDeFecha(fecha));
		gc.set(Calendar.HOUR_OF_DAY, horaDeFecha(fecha));
		gc.set(Calendar.MINUTE, minDeFecha(fecha));
		long inst = dameInstante(gc);
		return inst;
	}

	public PasoTiempo damePaso(long instante) {
		return damePaso(dameTiempo(instante));
	}
	
	
	/**
	 * Devuelve el paso de tiempo anterior al paso corriente de la linea de tiempo
	 * o null si la línea está en el primer paso.
	 * @return
	 */
	public PasoTiempo damePasoAnterior() {
		PasoTiempo pt = devuelvePasoActual();
		if (pt.getNumpaso()>0) return this.linea.get(numPaso-1);
		return null;		
	}

	
	public long dameInstante(GregorianCalendar tiempo) {
		return restarFechas(tiempo, this.inicioTiempo);

	}
	


	public boolean isUsarPeriodoIntegracion() {
		return usarPeriodoIntegracion;
	}

	public void setUsarPeriodoIntegracion(boolean usarPeriodoIntegracion) {
		this.usarPeriodoIntegracion = usarPeriodoIntegracion;
	}
	
	
	/**
	 * Devuelve el ordinal del día dentro de la corrida. El primer día de la corrida es el cero
	 */
	public int diaDelaCorrida(long instante) {
		int dia = (int)instante/utilitarios.Constantes.SEGUNDOSXDIA;
		return dia;		
	}
	

	/**
	 * Vuelve al inicio la lónea de tiempo
	 */
	public void reiniciar() {
		this.numPaso = 0;
		this.bloqueActual = 0;
	}

	public Integer getCantidadPostes() {
		return bloques.get(bloqueActual).getCantPostes();
	}

	public Integer getDuracionPaso() {
		return pasoActual().getDuracionPaso();
	}

	public long[] getInstInicioAnio() {

		return instInicioAnio;
	}

	public Hashtable<Integer, Long> getInstInicioAnioHT() {
		return instInicioAnioHT;
	}

	public void setInstInicioAnioHT(Hashtable<Integer, Long> instInicioAnioHT) {
		this.instInicioAnioHT = instInicioAnioHT;
	}

	public Hashtable<Integer, Integer> getDurAnio() {
		return durAnio;
	}

	public void setDurAnio(Hashtable<Integer, Integer> durAnio) {
		this.durAnio = durAnio;
	}

	public void setInstInicioAnio(long[] instInicioAnio) {
		this.instInicioAnio = instInicioAnio;
	}

	public int getAnioInic() {
		return anioInic;
	}

	public void setAnioInic(int anioInic) {
		this.anioInic = anioInic;
	}

	public int getAnioFin() {
		return anioFin;
	}

	public void setAnioFin(int anioFin) {
		this.anioFin = anioFin;
	}

	public boolean primerPasoBloque() {
		return bloques.get(bloqueActual).getPrimerPaso() == numPaso;
	}

	public boolean esPasoFinal() {
		return numPaso == cantidadPasos - 1;
	}

	public void llevarAlFinal() {
		this.numPaso = this.cantidadPasos - 1;
		this.bloqueActual = this.bloques.size() - 1;

	}

	public SentidoTiempo getSentidoTiempo() {
		return sentidoTiempo;
	}

	public void setSentidoTiempo(SentidoTiempo sentidoTiempo) {
		this.sentidoTiempo = sentidoTiempo;
	}

	public GregorianCalendar getFinTiempo() {
		return finTiempo;
	}

	public void setFinTiempo(GregorianCalendar finTiempo) {
		this.finTiempo = finTiempo;
	}

	public long getInstInicPasoCorriente() {
		return this.getLinea().get(this.getNumPaso()).getInstanteInicial();
	}
	
	
	/**
	 * Devuelve el mes del año (empezando en 1) al que pertenece el instante inicial de un paso de tiempo
	 * @param pt
	 * @return
	 */
	public int mesDePaso(PasoTiempo pt) {
		long instInicialPaso = pt.getInstanteInicial();
		GregorianCalendar gc = dameTiempo(instInicialPaso);
		int mes = gc.get(Calendar.MONTH) + 1;
		return mes;		
	}
	
	
	/**
	 * Devuelve la semana del año (empezando en 1) a la que pertenece el instante inicial del paso de tiempo
	 */
	public int semanaDePaso(PasoTiempo pt) {
		long instInicialPaso = pt.getInstanteInicial();
		GregorianCalendar gc = dameTiempo(instInicialPaso);
		int semana = gc.get(Calendar.WEEK_OF_YEAR);
		return semana;		
	}
	
	/**
	 * Devuelve el entero del día de la semana (con lunes = 1 ,...., domingo = 7)
	 * del instante instante
	 */
	public int diaSemDeInstanteLun1(long instante) {
		GregorianCalendar gc = dameTiempo(instante);
		int diaCalendar = gc.get(Calendar.DAY_OF_WEEK); // Para GregorianCalendar el día 1 es el domingo.
		if(diaCalendar == 7) {
			return 1;
		}else {
			return diaCalendar-1;
		}		
	}
	
	
	
	/**
	 * Devuelve el entero del día de la semana (con domingo = 1, lunes = 2 ,...., )
	 * del instante instante
	 */
	public int diaSemDeInstanteDom1(long instante) {
		GregorianCalendar gc = dameTiempo(instante);
		int diaCalendar = gc.get(Calendar.DAY_OF_WEEK); // Para GregorianCalendar el día 1 es el domingo.
		return diaCalendar;		
	}	
	
	
	/**
	 * Devuelve el mes del año al que pertenece un instante, con enero empezando en 1.
	 */
	public int mesDeInstante(long instante) {
		GregorianCalendar gc = dameTiempo(instante);
		return gc.get(Calendar.MONTH) +1;
	}
	

	/**
	 * Devuelve la hora del día empezando en 0
	 * @param instante
	 * @return
	 */
	public int horaDeInstante(long instante) {
		GregorianCalendar gc = dameTiempo(instante);
		return gc.get(Calendar.HOUR);
	}
	
	
	/**
	 * Devuelve el intervalo de muestreo cerrado por izquierda [...) dentro del año, empezando en 0
	 * dado que el intervalo de muestreo tiene duración durInt
	 * @param instante
	 * @param durInt en segundos
	 * @return
	 */
	public int intMuestreoAnioDeInstante(long instante, int durInt) {
		GregorianCalendar gc = dameTiempo(instante);
		//new GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second)
		GregorianCalendar iniAnio = new GregorianCalendar(gc.get(Calendar.YEAR), 0, 1, 0, 0, 0);
		long difSec = restarFechas(gc, iniAnio);
		return (int) difSec%durInt;
	}
	
	
	/**
	 * Devuelve el intervalo de muestreo cerrado por izquierda [...) dentro del día, empezando en 0
	 * dado que el intervalo de muestreo tiene duración durInt
	 * @param instante
	 * @param durInt en segundos
	 * @return
	 */
	public int intMuestreoDiaDeInstante(long instante, int durInt) {
		GregorianCalendar gc = dameTiempo(instante);
		//new GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second)
		GregorianCalendar iniDia = new GregorianCalendar(gc.get(Calendar.YEAR), gc.get(Calendar.DAY_OF_MONTH), 
				gc.get(Calendar.HOUR_OF_DAY), gc.get(Calendar.MINUTE),gc.get(Calendar.SECOND));
		long difSec = restarFechas(gc, iniDia);
		return (int) difSec%durInt;
	}
	
	/**
	 * Devuelve el año al que pertenece un instante, ej. 2030.
	 */
	public int anioDeInstante(long instante) {
		GregorianCalendar gc = dameTiempo(instante);
		return gc.get(Calendar.YEAR);
	}
	
	
	/**
	 * Devuelve el ordinal del día dentro del año al que pertenece un instante,
	 * empezando con 0 el primer día del año.
	 */
	public int diaDelAnioDeInstante(long instante) {
		GregorianCalendar gc = dameTiempo(instante);
		return gc.get(Calendar.DAY_OF_YEAR)-1;  // se resta 1 porque el método empieza el año en día 1
	}	
	
	public int horaDelAnioDeInstante(long instante) {
		GregorianCalendar gc = dameTiempo(instante);
		return gc.get(Calendar.HOUR_OF_DAY);
	}
	
	
	/**
	 * Devuelve true si el anio es bisiesto y false en caso contrario
	 * @param anio
	 * @return
	 */
	public static boolean bisiesto(int anio){
	   if(anio % 4 == 0){
		   if(anio % 100 != 0 ){
			   return true;
		   }else{
			   if(anio % 400 == 0) return true;
		   }
	   }
	   return false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LINEA DE TIEMPO\n");
		sb.append("Instante inicio de corrida" + inicial + "\n\n");
		sb.append("Instantes iniciales de pasos\n");
		int numpaso = 0;
		for (PasoTiempo p : linea) {
			sb.append("Paso " + numpaso + " - Inst.Inicio " + p.getInstanteInicial() + " " + fechaYHoraDeInstante(p.getInstanteInicial())+ " - Inst.Final "
					+ p.getInstanteFinal() + " " + fechaYHoraDeInstante(p.getInstanteInicial()) + "\n");
			numpaso++;
		}
		sb.append("\nInstantes de inicio de anios\n");
		for (int ia = 0; ia < instInicioAnio.length; ia++) {
			sb.append(instInicioAnio[ia] + "\n");
		}
		return sb.toString();
	}

	public void setSentidoTiempo(int i) {
		this.getSentidoTiempo().setSentido(i);

	}

	public void prepararParaOptimizar() {
		this.setSentidoTiempo(-1);
		// Lleva la lónea de tiempo al óltimo paso
		this.llevarAlFinal();

	}

	@SuppressWarnings("deprecation")
	public int dameNumeroPasoInicioAnio(int anio) {
		int numpaso = 0;
		for (PasoTiempo p : linea) {
			if (dameTiempo(p.getInstanteInicial()).get(Calendar.YEAR)== anio) {
				return numpaso;
			}
			numpaso++;
		}
		return numpaso;
	}

	@SuppressWarnings("deprecation")
	public int dameNumeroPasoFinAnio(int anio) {
		int numpaso = 0;
		boolean ultimoAnio = false;
		for (PasoTiempo p : linea) {
			if (ultimoAnio == false) {
				if (dameTiempo(p.getInstanteInicial()).get(Calendar.YEAR)==  anio) {
					ultimoAnio = true;
				}
			} else {
				if (dameTiempo(p.getInstanteInicial()).get(Calendar.YEAR) != anio) {
					return numpaso-1;
				}
			}

			numpaso++;
		}
		return numpaso-1;
	}

	public ArrayList<Integer> dameCambiosAnio(int anioIni, int anioFin) {
		ArrayList<Integer> cambiosAnio = new ArrayList<Integer>();
		
		int numpaso = dameNumeroPasoInicioAnio(anioIni);
		int pasofin = dameNumeroPasoFinAnio(anioFin);
		
		int anio = dameTiempo(linea.get(numpaso).getInstanteInicial()).get(Calendar.YEAR);
		for (int i = numpaso; i<=pasofin; i++) {			
				if (dameTiempo(linea.get(i).getInstanteInicial()).get(Calendar.YEAR) != anio) {
					cambiosAnio.add(numpaso-1);
					anio = dameTiempo(linea.get(i).getInstanteInicial()).get(Calendar.YEAR);
				}			
			numpaso++;
		}		
		return cambiosAnio;
		
	}

	public int[] getAniosCorrida() {
		int anioFin = this.getAnioFin();
		int anioIni = this.getAnioInic();
		int[] aniosCorrida = new int[anioFin-anioIni] ;
		int anio = anioIni;
		
		for (int i = 0; i < aniosCorrida.length; i++) {
			aniosCorrida[i] = anio;
			anio++;
		}
		return aniosCorrida;
	}

	public int getAnioPaso(int paso) {
		long instante = linea.get(paso).getInstanteInicial();
		GregorianCalendar fecha= dameTiempo(instante);		
		return fecha.get(Calendar.YEAR); 
	}
	
	/**
	 * Devuelve el año del paso (ejemplo 2030)
	 * @param paso
	 * @return
	 */
	public int getAnioPaso(PasoTiempo paso) {
		long instante = paso.getInstanteInicial();
		GregorianCalendar fecha= dameTiempo(instante);		
		return fecha.get(Calendar.YEAR); 
	}
	
	
	public Hashtable<Integer, ArrayList<Integer>> getPasosAnio() {
		Hashtable<Integer, ArrayList<Integer>> aniosPaso = new Hashtable<Integer, ArrayList<Integer>>(); 
		int numpaso = 0;
		for (PasoTiempo p : this.linea) {
			int anio = getAnioPaso(p);
			if (aniosPaso.get(anio)==null) {
				ArrayList<Integer> pasos = new ArrayList<Integer>();
				aniosPaso.put(anio, pasos);
			}
			aniosPaso.get(anio).add(numpaso);
			numpaso++;
			
		}
		return aniosPaso;
	}



}
