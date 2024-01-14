/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CompDespPE is part of MOP.
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

package cp_compdespProgEst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import compdespacho.CompDespacho;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseObj;
import cp_despacho.BaseRest;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import cp_despacho.GrafoEscenarios;
import cp_nuevosParticipantesCP.ConstructorHiperplanosPE;
import datatypesProblema.DatosEntradaProblemaLineal;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProblema.DatosVariableControl;
import parque.Corrida;
import parque.Impacto;
import parque.Participante;
import utilitarios.Constantes;

public abstract class CompDespPE {
	
	
	protected static DatosSalidaProblemaLineal ultimoDespacho;
	
	protected static ConstructorHiperplanosPE constHip;
	
	protected Corrida  corrida;
	protected ArrayList<Participante> participantes;  // lista general de participantes del problema CP
	protected ArrayList<Impacto> impactosQueLoInvolucran;  // lista de Impactos que involucran al participante de this
	protected GrafoEscenarios grafo;
	protected DatosGeneralesCP dGCP;  // datos generales del corto plazo
//	protected DatosPartCP dPCP;    // datos del participantes del corto plazo
	protected static int POSXH;   // postes por hora
	
	protected static boolean usoHip;  // true si se usan hiperplanos
	
	protected static int cantDias;  // días del horizonte. El primero puede no estar entero
	protected static int cantPos;   // cantidad de postes del horizonte
	protected static int dur1Pos;    // duración de un poste en segundos
	protected static int durHorCP; // duración del horizonte del CP en segundos	
	protected static long instIniCP; // inicio del horizonte CP
	
	protected Participante participante;
	protected String nomPar;  // nombre del participante
	protected String tipo;   // tipo del participante
	protected CompDespacho compDespacho; // el comportamiento despacho del participante en optimización y simulación

	
	protected Hashtable<String, DatosVariableControl> variablesControl;
	protected Hashtable<String, DatosRestriccion> restricciones;
	protected DatosObjetivo objetivo;
	
	/**
	 * Conjunto de BaseVar del total de participantes
	 * clave: nombre de la baseVar obtenido con el método generaNomBVar de this
	 */
	protected static Hashtable<String, BaseVar> basesVarGeneral;	
	static {
		basesVarGeneral = new Hashtable<String, BaseVar>();
	}
	
	/**
	 * Atributos no estáticos, propios de cada participante
	 * Clave: nombre de la variable
	 * Valor: la BaseVar o BaseRest asociada entre las del participante
	 */
	private Hashtable<String, BaseVar> basesVar;	
	private Hashtable<String, BaseRest> basesRest;	
	private BaseObj baseObj;
	


	protected static final double cero = 0;
	protected static final double uno = 1;
	
	public CompDespPE() {
		variablesControl = new Hashtable<String, DatosVariableControl>();
		restricciones = new Hashtable<String, DatosRestriccion>();
		objetivo = new DatosObjetivo();
		basesVar = new Hashtable<String, BaseVar>();
		basesRest = new Hashtable<String, BaseRest>();
		baseObj = new BaseObj();
		basesVar = new Hashtable<String, BaseVar>();
		impactosQueLoInvolucran = new ArrayList<Impacto>();

	}
	

	/**
	 * Carga en el objetivo de this la lista de BaseTermino por la contribución al costo variable de OyM por MWh de energía
	 * sumando las contribuciones de los postes del 0 al cantPos-1
	 * @param cvar   costo variable de OyM en USD/MWh
	 * @return
	 */
	public void contObjCVarOyMEnergia(double cvar, String nomVar, String nomPar) {
		ArrayList<BaseTermino> cont = new ArrayList<BaseTermino>();
		for(int p=0; p<cantPos-1; p++) {
			BaseTermino bt = new BaseTermino(nomVar, nomPar, p, dur1Pos/Constantes.SEGUNDOSXHORA*cvar, null, grafo);
			cont.add(bt);
		}
		for(BaseTermino bt: cont) {
			agrega1BTalObj(bt);		
		}		
	}

	
	
	/**
	 * LOS SIGUIENTES TRES MÉTODOS EN CADA PARTICIPANTE DEBEN CREAR
	 * LAS BASEVAR, BASEREST Y BASEOBJ QUE DEBEN SER EXPANDIDOS EN LOS ESCENARIOS
	 */
	public abstract void crearBasesVar();	// carga la tabla static basesVar con elementos BaseVar
	public abstract void crearBasesRest();	// carga la tabla static basesRest con elementos RestBase
	public abstract void crearBaseObj();    // carga el static baseObj
	
	/**
	 * CREA LAS PAREJAS (VARIABLE DE CONTROL , VARIABLE EN LOS HIPERPLANOS) y las carga en el ConstructorHiperplanosCP
	 * Debe ser sobre escrito por los CompDespPE de los Participantes que tienen variables de estado en los hiperplanos finales
	 */
	public void cargaVarsEstadoHiperplanos() {
		// DELIBERADAMENTE EN BLANCO
	}
	
	
	/**
	 * A partir del DatosPartCP carga y castea el DatosPart de las clases hijas
	 * Castea el Participante en el atributo con el participante hijo
	 * DEBE SER SOBRE ESCRITO PARA LOS PARTICIPANTES QUE TENGAN DATOS DE CORTO
	 * PLAZO QUE SE LEEN
	 */
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		// DELIBERADAMENTE EN BLANCO
	}
	
	
	/**
	 * Usando los DatosPartCP casteados y otra información de la entrada de CP,
	 * completa la construcción de las clases hijas
	 * 
	 */
	public abstract void completaConstruccion();
	
	
	public void aportarVarAEntrada(DatosEntradaProblemaLineal entrada) {
		crearBasesVar();
		crearVariablesControl();
		entrada.agregarVariables(this.getVariablesControlArray());			
	}


	
	public void aportarRestAEntrada(DatosEntradaProblemaLineal entrada) {
		crearBasesRest();
		crearRestricciones();
		entrada.agregarRestricciones(restricciones);
	}
	
	
	public void aportarObjAEntrada(DatosEntradaProblemaLineal entrada) {
		crearBaseObj();
		crearObjetivo();
		entrada.contribuirObjetivo(objetivo);		
	}
	
	
	public void crearVariablesControl() {
		for(String s: basesVar.keySet()) {
			ArrayList<DatosVariableControl> al = basesVar.get(s).expandeBaseVar();
			for(DatosVariableControl dvc: al) {
				variablesControl.put(dvc.getNombre(), dvc);
			}
		}
	}

	public void crearRestricciones() {
		for(String s: basesRest.keySet()) {
			ArrayList<DatosRestriccion> al = basesRest.get(s).expandeBaseRest(grafo);
			for(DatosRestriccion dr: al) {
				if(dr.getNombre().contains(ConCP.RPOTPAR)) {
					int pp = 0;
				}
				restricciones.put(dr.getNombre(), dr);
			}
		}
	}
		
		
	public void crearObjetivo() {
		objetivo = baseObj.expandeBaseObj();
	}
	

	
	

	public static DatosSalidaProblemaLineal getUltimoDespacho() {
		return ultimoDespacho;
	}

	public static void setUltimoDespacho(DatosSalidaProblemaLineal ultimoDespacho) {
		CompDespPE.ultimoDespacho = ultimoDespacho;
	}
	
	/**
	 * Crea el nombre base de una variable con el nombre
	 * de la variable el nombre del participante, el  y el poste. 
	 * Este nombre luego se expande en los escenarios
	 * 
	 * @param nomVar nombre de la variable, ejemplo "vertimiento"
	 * @param nomPar nombre del participante, ejemplo "bonete"
	 * @param entero poste o día
	 * @return
	 */
	public static String generaNomBVar(String nomVar, String nomPar, Integer entero) {
		String nom = nomVar;
		if(nomPar!=null) nom += "_" + nomPar;
		if(entero!=null) nom += "_" + entero;
		return nom;
	}
	
	/**
	 * Devuelve la BaseVar con el nombre de variable, de participante y poste o día requerido
	 * y si no existe corta la ejecución
	 * @param nomVar
	 * @param nomPar
	 * @param entero poste o día
	 * @return
	 */
	public static BaseVar dameBaseVar(String nomVar,String nomPar, int entero) {
		String nom =  generaNomBVar(nomVar, nomPar, entero);
		BaseVar result =  basesVarGeneral.get(nom);
//		if(result==null) {
//			System.out.println("La BaseVAR " + nom + " no existe");
//			System.exit(1);
//		}
		return result;
	}
	
	
	/**
	 * Devuelve la BaseVar de la variable de potencia del participante en el poste
	 * SOLO PUEDE INVOCARSE POR PARTICIPANTES QUE TENGAN POTENCIA POR POSTE
	 */
	public static BaseVar dameBaseVarPot(String nomPar, int poste) {
		String nom = generaNomBVar(ConCP.POT, nomPar, poste);
		return  basesVarGeneral.get(nom);
	}

	
	/**
	 * Carga una BaseVar con el nombre de variable, de participante y poste o día requerido,
	 * que forman una clave para guardar la BaseVar.
	 * Además lo carga en la tabla de BasesVar del propio participante hijo
	 * @param nomVar
	 * @param nomPar
	 * @param entero poste o día
	 * @return
	 */	
	public void cargaBaseVar(BaseVar bV, String nomVar, String nomPar, Integer entero) {
		String nom =  generaNomBVar(nomVar, nomPar, entero);
		basesVar.put(nom, bV);		
		basesVarGeneral.put(nom, bV);
	}
	
	
	/**
	 * Genera un nombre de restricción tomando un nombre que debe ser extraído del paquete ConCP,
	 * un nombre de participante y un entero día o poste.
	 * Si el String nomPar es null no lo agrega
	 * Si el Integer es null no agrega un entero
	 * @param constDeConCP
	 * @param nomPar
	 * @param entero
	 * @return
	 */
	public static String generaNomBRest(String constDeConCP, String nomPar, Integer entero) {
		String nom = constDeConCP;
		if(nomPar!= null) nom = nom + "_" + nomPar;
		if(entero!= null) nom = nom + "_" + entero;
		return nom;
	}
	
	
	public static String generaNomVarPar(String nomVar, String nomPar) {
		return nomVar + "_" + nomPar;
	}
	

	/**
	 * Carga en la tabla valoresRezagados los valores de variables de control de
	 * pasos anteriores al inicio del horizonte, que ya están determinados.
	 * 
	 * ESTE METODO DEBE SER SOBRE ESCRITO POR LOS PARTICIPANTES QUE TENGAN VALORES REZAGADOS
	 * ATENCIÓN: EN MUCHOS CASOS EN LUGAR DE CARGAR VALORES REZAGADOS DE VARIABLE DE 
	 * CONTROL SE CREAN RESTRICCIONES QUE ESPECIFICAN EL VALOR DE ESAS VARIABLES.
	 */
	public void cargarValoresRezagados() {
		// Deliberadamente en blanco
	}
	
	/**
	 * Si la BaseRest no es idénticamente nula la carga a basesRest
	 * @param nombreBase
	 * @param br
	 */
	public void agrega1BR(String nombreBase, BaseRest br) {
		if(!br.isIdNula()) {
			basesRest.put(nombreBase, br);
		}	
	}
	
	/**
	 * Agrega un BaseTermino al baseObj del Participante
	 * @param bt
	 */
	public void agrega1BTalObj(BaseTermino bt) {
		baseObj.getTerminosBase().add(bt);
		BaseObj.getBasesTerCostoEsc().add(bt);
	}
	
	
	
	/**
	 * Devuelve los enteros que pertenecen a listaIndices 
	 * y que son mayores o iguales a posteIni y menores o iguales a posteFin
	 * @param listaIndices
	 * @param posteIni
	 * @param posteFin
	 * @return
	 */
	public static ArrayList<Integer> postesEnLista(ArrayList<Integer> listaIndices, 
			int posteIni, int posteFin){
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int p=posteIni; p<= posteFin; p++) {
			if(listaIndices.contains(p)) result.add(p);
		}
		return result;
	}
	
	
	
	
	public GrafoEscenarios getGrafo() {
		return grafo;
	}
	public void setGrafo(GrafoEscenarios grafo) {
		this.grafo = grafo;
	}
	public static Hashtable<String, BaseVar> getBasesVarGeneral() {
		return basesVarGeneral;
	}


	public Hashtable<String, DatosVariableControl> getVariablesControl() {
		return variablesControl;
	}
	
	public ArrayList<DatosVariableControl> getVariablesControlArray() {
		ArrayList<DatosVariableControl> result = new ArrayList<DatosVariableControl>();
		for(String s : variablesControl.keySet()) {
			DatosVariableControl dvc = variablesControl.get(s);
			result.add(dvc);
		}
		return result;
	}

	public void setVariablesControl(Hashtable<String, DatosVariableControl> variablesControl) {
		this.variablesControl = variablesControl;
	}

	public Hashtable<String, DatosRestriccion> getRestricciones() {
		return restricciones;
	}

	public void setRestricciones(Hashtable<String, DatosRestriccion> restricciones) {
		this.restricciones = restricciones;
	}

	public DatosObjetivo getObjetivo() {
		return objetivo;
	}

	public void setObjetivo(DatosObjetivo objetivo) {
		this.objetivo = objetivo;
	}

	public Participante getParticipante() {
		return participante;
	}

	public void setParticipante(Participante participante) {
		this.participante = participante;
	}
	
	
	

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public ArrayList<Participante> getParticipantes() {
		return participantes;
	}

	public void setParticipantes(ArrayList<Participante> participantes) {
		this.participantes = participantes;
	}

	public CompDespacho getCompDespacho() {
		return compDespacho;
	}

	public void setCompDespacho(CompDespacho compDespacho) {
		this.compDespacho = compDespacho;
	}

	public static int getPOSXH() {
		return POSXH;
	}
	public static void setPOSXH(int pOSXH) {
		POSXH = pOSXH;
	}

	public DatosGeneralesCP getdGCP() {
		return dGCP;
	}
	public void setdGCP(DatosGeneralesCP dGCP) {
		this.dGCP = dGCP;
	}
	public String getNomPar() {
		return nomPar;
	}
	public void setNomPar(String nomPar) {
		this.nomPar = nomPar;
	}
	public Corrida getCorrida() {
		return corrida;
	}
	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}
	public static int getCantDias() {
		return cantDias;
	}
	public static void setCantDias(int cantDias) {
		CompDespPE.cantDias = cantDias;
	}
	public static int getCantPos() {
		return cantPos;
	}
	public static void setCantPos(int cantPos) {
		CompDespPE.cantPos = cantPos;
	}
	public static int getDur1Pos() {
		return dur1Pos;
	}
	public static void setDur1Pos(int dur1Pos) {
		CompDespPE.dur1Pos = dur1Pos;
	}
	public static double getCero() {
		return cero;
	}
	public static double getUno() {
		return uno;
	}
	public void setBasesVar(Hashtable<String, BaseVar> basesVar) {
		basesVar = basesVar;
	}
	public void setBasesRest(Hashtable<String, BaseRest> basesRest) {
		basesRest = basesRest;
	}
	public void setBaseObj(BaseObj baseObj) {
		baseObj = baseObj;
	}

	public Hashtable<String, BaseVar> getBasesVar() {
		return basesVar;
	}

	public Hashtable<String, BaseRest> getBasesRest() {
		return basesRest;
	}

	public BaseObj getBaseObj() {
		return baseObj;
	}

	public static void setBasesVarGeneral(Hashtable<String, BaseVar> basesVarGeneral) {
		CompDespPE.basesVarGeneral = basesVarGeneral;
	}
	
	
	
	public static int getDurHorCP() {
		return durHorCP;
	}



	public static void setDurHorCP(int durHorCP) {
		CompDespPE.durHorCP = durHorCP;
	}



	public static long getInstIniCP() {
		return instIniCP;
	}



	public static void setInstIniCP(long instIniCP) {
		CompDespPE.instIniCP = instIniCP;
	}


	

	public ArrayList<Impacto> getImpactosQueLoInvolucran() {
		return impactosQueLoInvolucran;
	}


	public void setImpactosQueLoInvolucran(ArrayList<Impacto> impactosQueLoInvolucran) {
		this.impactosQueLoInvolucran = impactosQueLoInvolucran;
	}


	public static void main(String[] args) {
		ArrayList<Integer> lista = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,9,11,14,19));
		ArrayList<Integer> result = postesEnLista(lista, 7, 15);
		System.out.println(result);
		
	}


	public static ConstructorHiperplanosPE getConstHip() {
		return constHip;
	}


	public static void setConstHip(ConstructorHiperplanosPE constHip) {
		CompDespPE.constHip = constHip;
	}


	public static boolean isUsoHip() {
		return usoHip;
	}


	public static void setUsoHip(boolean usoHip) {
		CompDespPE.usoHip = usoHip;
	}
	
	
	
	

}
