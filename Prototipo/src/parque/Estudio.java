/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Estudio is part of MOP.
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

package parque;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import datatypes.DatosCorrida;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import logica.CorridaHandler;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.EvolucionPorCaso;
import utilitarios.DirectoriosYArchivos;
import utilitarios.EnumeradorLexicografico;

/**
 * Clase que representa el estudio
 * 
 * @author ut602614
 * 
 * 
 *         El estudio permite generar diferentes corridas, modificando un unico
 *         objeto Corrida. Cada corrida que se ejecuta corresponde a un
 *         escenario formado por un conjunto de casos, uno para cada
 *         tipoDeDatos, o lo que es equivalente un caso en cada
 *         EvolucionPorCaso. Las corridas abarcan el producto cartesiano de los
 *         conjuntos de casos de cada EvolucionPorPaso
 * 
 *         Para que el Estudio tenga sentido, el objeto Corrida debe leerse y
 *         contener algún dato que sigue una EvolucionPorCaso. Cada objeto
 *         EvolucionPorCaso da lugar a una Evolucion según el valor de una
 *         clave.
 * 
 *         Por ejemplo, los tipos de datos son: "cantidad de TGs", que tiene
 *         casos "1" y "2", que corresponden a dos evoluciones constantes
 *         "potencia solar", que tiene casos "bajo", "medio" y "alto", que
 *         corresponden a tres evoluciones por instantes. Se generan corridas
 *         con los 2x3=6 escenarios: (1, bajo), (1, medio),........(2, alto)
 * 
 *         Las salidas de las corridas se dirigen adirectorios formados con el
 *         nombre de la corridaBase y sufijos construidos con los nombres de los
 *         casos de cada escenario Ejemplo: "nombre de la corrida base" +
 *         "_2_medio"
 * 
 */
public class Estudio {
	private String nombre;

	/**
	 * Directorio de salidas leído con el xml de la corridaBase. Las salidas de las
	 * corridas, u na para cada
	 */
	private String nombreCorrBase;

	private ArrayList<String> tiposDeDatos; // nombres de los tipos de datos Ej "modulos-TG" o "Pot-Solar"

	private int cantTipos;

	private DatosCorrida datosCorridaBase;

	private Corrida corrida; // Es la corrida que contiene las EvolucionPorCaso

	/**
	 * Para cada tipo de datos contiene el caso a emplear en la corrida Clave:
	 * nombre del tipo de datos Valor: caso corriente para ese tipo de datos
	 */
	private Hashtable<String, String> casosCorrientes;

	/**
	 * Clave: nombre del tipo de datos Valor: EvolucionPorCaso leída en la corrida
	 * base
	 */
	private Hashtable<String, EvolucionPorCaso> generadorEscenarios;

	private EnumeradorLexicografico enumEsc; // El enumerador que va produciendo los escenarios por la combinación de
												// casos

//	
	private Azar azar; // OJOJOJOJOJOJOJOJOJOJOJOJO

	private String rutaEstudio;

//	private int instanteInicial;

	public Estudio(String nombre, DatosCorrida datosCorridaBase) {
		super();
		this.nombre = nombre;
		this.datosCorridaBase = datosCorridaBase;
		casosCorrientes = new Hashtable<String, String>();
		tiposDeDatos = new ArrayList<String>();
		generadorEscenarios = new Hashtable<String, EvolucionPorCaso>();

		for (EvolucionPorCaso ev : datosCorridaBase.getEvolucionesPorCaso()) {
			tiposDeDatos.add(ev.getTipoDato());
			generadorEscenarios.put(ev.getTipoDato(), ev);
			ev.setCasosCorrientes(casosCorrientes);
		}

		nombreCorrBase = datosCorridaBase.getNombre();

		azar = new Azar(null, null, null, null, null);
		/**
		 * TODO: Cargar el azar
		 * 
		 */
		// azar = new Azar();

		String rutaEstudio = "";

		Calendar fecha = Calendar.getInstance();
		int anio = fecha.get(Calendar.YEAR);
		int mes = fecha.get(Calendar.MONTH) + 1;
		int dia = fecha.get(Calendar.DAY_OF_MONTH);
		int hora = fecha.get(Calendar.HOUR_OF_DAY);
		int minuto = fecha.get(Calendar.MINUTE);
		int segundo = fecha.get(Calendar.SECOND);
		this.rutaEstudio = "Estudio-" + anio + "-" + mes + "-" + dia + "-" + hora + "-" + minuto + "-" + segundo;

	}

	public Estudio(String nombre) {
		super();
		this.nombre = nombre;
		azar = new Azar(null, null, null, null, null);
		/**
		 * TODO: Cargar el azar
		 * 
		 */
		// azar = new Azar();
	}

	public void inicializarEstudio() {

		/**
		 * ATENCION SE SUPONE QUE AL CONSTRUIR LA CORRIDA BASE SE HA CARGADO
		 * generadorEscenarios
		 */
		tiposDeDatos = new ArrayList<String>(generadorEscenarios.keySet());
		cantTipos = tiposDeDatos.size();

		int[] cotasInf = new int[cantTipos];
		int[] cotasSup = new int[cantTipos];

		int itip = 0;
		for (String tip : tiposDeDatos) {
			EvolucionPorCaso ev = generadorEscenarios.get(tip);
			cotasSup[itip] = ev.getNombresCasos().size() - 1;
			itip++;
		}

		// Crea el EnumeradorLexicografico

		enumEsc = new EnumeradorLexicografico(cantTipos, cotasInf, cotasSup);

		// Carga el objeto ArrayList que contendrá los casos corrientes en cada
		// EvolucionPorCaso

	}

	/**
	 * Recorre el producto cartesiano de los conjuntos de casos posibles de los
	 * tiposDeDatos. Devuelve un código de enteros con el ordinal empezando en cero
	 * de cada caso dentro de su respectiva lista del tipoDeDatos o null cuando se
	 * terminaron los escenarios. Carga en casosCorrientes los nombres de los casos
	 * asociados al código de enteros.
	 */
	public int[] generaUnEscenario() {
		int[] codigo = enumEsc.devuelveVector();
		if (codigo == null) {
			return null;
		} else {
			casosCorrientes.clear();
			for (int it = 0; it < cantTipos; it++) {
				String nombreTipo = tiposDeDatos.get(it);
				ArrayList<String> al = generadorEscenarios.get(nombreTipo).getNombresCasos();
				String nombreCaso = al.get(codigo[it]);
				casosCorrientes.put(nombreTipo, nombreCaso);
			}
			return codigo;
		}
	}

	/**
	 * Lanza las corridas resultantes del producto cartesiano, con nombres que
	 * resultan del nombre de la corridaBase y sufijos que se generan de acuerdo al
	 * caso
	 */
	public void ejecutarEstudio() {

		CorridaHandler ch = CorridaHandler.getInstance();

		inicializarEstudio();

		int[] codigo = generaUnEscenario(); // el método carga los valores de casos en casosCorrientes
		while (codigo != null) {
			String sufijo = "";
			for (int it = 0; it < cantTipos; it++) {
				String nombreTipo = tiposDeDatos.get(it);

				sufijo += "-" + this.casosCorrientes.get(nombreTipo);

			}
			datosCorridaBase.setNombre(nombreCorrBase + sufijo);
			ch.setEstudio(true);
			ch.cargarCorrida(datosCorridaBase, datosCorridaBase.getRuta(), false, false);

			ch.optimizar();
			ch.recargarSimulable();
			ch.simular();
			codigo = generaUnEscenario();
			System.out.println("TERMINO LA EJECUCION DEL ESTUDIO");
		}

	}

	public Azar getAzar() {
		return azar;
	}

	public void setAzar(Azar azar) {
		this.azar = azar;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

//	public int getInstanteInicial() {
//		return instanteInicial;
//	}
//	
//	
//	public void setInstanteInicial(int instanteInicial) {
//		this.instanteInicial = instanteInicial;
//	}
//	

	public ProcesoEstocastico dameProcesoEstocastico(String nombre2) {
		return azar.damePEstocastico(nombre2);
	}

	public String getNombreCorrBase() {
		return nombreCorrBase;
	}

	public void setNombreCorrBase(String nombreCorrBase) {
		this.nombreCorrBase = nombreCorrBase;
	}

	public ArrayList<String> getTiposDeDatos() {
		return tiposDeDatos;
	}

	public void setTiposDeDatos(ArrayList<String> tiposDeDatos) {
		this.tiposDeDatos = tiposDeDatos;
	}

	public int getCantTipos() {
		return cantTipos;
	}

	public void setCantTipos(int cantTipos) {
		this.cantTipos = cantTipos;
	}

	public DatosCorrida getDatosCorridaBase() {
		return datosCorridaBase;
	}

	public void setDatosCorridaBase(DatosCorrida datosCorridaBase) {
		this.datosCorridaBase = datosCorridaBase;
	}

	public Corrida getCorrida() {
		return corrida;
	}

	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}

	public Hashtable<String, String> getCasosCorrientes() {
		return casosCorrientes;
	}

	public void setCasosCorrientes(Hashtable<String, String> casosCorrientes) {
		this.casosCorrientes = casosCorrientes;
	}

	public Hashtable<String, EvolucionPorCaso> getGeneradorEscenarios() {
		return generadorEscenarios;
	}

	public void setGeneradorEscenarios(Hashtable<String, EvolucionPorCaso> generadorEscenarios) {
		this.generadorEscenarios = generadorEscenarios;
	}

	public EnumeradorLexicografico getEnumEsc() {
		return enumEsc;
	}

	public void setEnumEsc(EnumeradorLexicografico enumEsc) {
		this.enumEsc = enumEsc;
	}

	public String getRutaEstudio() {
		return rutaEstudio;
	}

	public void setRutaEstudio(String rutaEstudio) {
		this.rutaEstudio = rutaEstudio;
	}

}
