/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DespachoProgEstocastica is part of MOP.
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

package cp_despacho;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import cp_compdespProgEst.CompDespPE;
import cp_compdespProgEst.ConstHiperCompDespPE;
import cp_compdespProgEst.HidraulicoCompDespPE;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosGrafoEscCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_nuevosParticipantesCP.ConstructorHiperplanosPE;
import cp_nuevosParticipantesCP.ContratoIntSist;
import optimizacion.ResOptim;
import parque.Barra;
import parque.Combustible;
import parque.Corrida;
import parque.GeneradorHidraulico;
import parque.Impacto;
import parque.Participante;
import problema.ProblemaHandler;
import tiempo.LineaTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.ManejaObjetosEnDisco;
import cp_persistencia.CargadorDatosGenerales;
import cp_persistencia.CargadorDatosPartCP;
import cp_persistencia.CargadorEscParaSalidas;
import cp_persistencia.CargadorGrafoEsc;
import cp_persistencia.EscritorProbLpSolve;
import cp_persistencia.ParamTextoSalidaUninodal;
import cp_salidas.BuscadorResolvedoresMOP;
import cp_salidas.BuscadorResult;
import cp_salidas.ResultadoTot1V;
import cp_salidas.ResultadosDePE;
import datatypesProblema.DatosEntradaProblemaLineal;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProblema.DatosVariableControl;


public class DespachoProgEstocastica implements DespachableCP{
	
	private DespachadorCortoPlazo despachador;
	
	private Corrida corrida;
	
	private ArrayList<Participante> participantes;
	
	
	private ResOptim resoptim;

	private String dirEntrada;
	
	private String dirSalida;
	
	private DatosGeneralesCP datGen;
	
	private ArrayList<int[]> escenarios;  // lista de escenarios para los que se generan salidas de resultados
	
	/**
	 * Clave: nombreParticipante
	 * Valor: DatosPartCP (clase padre, de la que heredan los datos de cada participante)
	 * 
	 */
	private Hashtable<String, DatosPartCP> datosPartCP;
	
	/**
	 * Clave: tipoParticipante-nombreParticipante-nombreVariable
	 * Valor: los valores rezagados para postes: -k, -k+1,...,-1 
	 * o días según el caso
	 * 
	 */
	private Hashtable<String, double[]> valoresRezagados;
	
	private GrafoEscenarios grafo;
	
	
	private DatosEntradaProblemaLineal entrada;
	private DatosSalidaProblemaLineal salida;
	
	private BuscadorResult buscador;
	
	private DatosSalidaProblemaLineal salidaPLMOP; // se usa si se emplean los resolvedores del MOP
	
	
	/**
	 * clave: el nombre de un contrato interrumpible
	 * valor: el objeto ContratoInsSist que lo sustituye en el despacho CP
	 */
	private Hashtable<String, ContratoIntSist> contratosSistemaDeCI;
	
	protected static final String NOMBRE_CONST_HIP_PE = "constructorHiperplanosPE";
	
	protected static ConstructorHiperplanosPE constHipPE;
	
		
	public ResOptim getResoptim() {
		return resoptim;
	}


	@Override
	public void construirComportamientosCP() {
		participantes = new ArrayList<Participante>();
		contratosSistemaDeCI = new Hashtable<String, ContratoIntSist>();
		participantes.addAll(corrida.getParticipantes());
		BaseRest.setGe(grafo);
		BaseVar.setGe(grafo);
		BaseObj.setGe(grafo);
		BaseSM.setGe(grafo);
	
		Iterator ite = participantes.iterator();
		
		
		// Crea el participante ConstructorHiperplanosPE
		
		constHipPE = new ConstructorHiperplanosPE(ConCP.NOMBRE_CONST_HIP_PE, ConCP.NOMBRE_CONST_HIP_PE);
		participantes.add(constHipPE);
		CompDespPE.setConstHip(constHipPE);
		
		cargaAtributosEstaticosACompDespPE();
		
		
		
		for(Participante p: participantes){			
			if(p!=null) {
				p.crearCompDespPE();  // En este método entre otras cosas al CompDespPE se le carga el atributo participante
				if(p.getTipo()!=null) p.getCompDespPE().setTipo(p.getTipo());
				if(p.getCompDespPE()!=null) {
					p.getCompDespPE().setNomPar(p.getNombre());
					p.getCompDespPE().setCorrida(corrida);
					p.getCompDespPE().setParticipantes(participantes);
					p.getCompDespPE().setGrafo(grafo);
					p.getCompDespPE().setdGCP(datGen);
					if(p.getNombre()!=null && datosPartCP.get(p.getNombre())!=null)
					//  Carga y castea al tipo de la clase hija en cada clase hija 
						p.getCompDespPE().cargaDPCPYPart(datosPartCP.get(p.getNombre()));  
					p.getCompDespPE().completaConstruccion();
				}			
			}
		}
		
		datGen.setUsaHip(CompDespPE.isUsoHip());
		
		/**
		 * Todos los participantes cargan en el ConstructorHiperplanosCP las parejas
		 * (nombreBase de variable del problema lineal , nombre de la variable en la entrada de los hiperplanos)
		 */
		for(Participante p: participantes){		
			CompDespPE cd = p.getCompDespPE();
			if(cd!=null) cd.cargaVarsEstadoHiperplanos();
		}
		
		
		// Elimina los participantes que no tienen CompDespPE, en principio los combustibles
		ite = participantes.iterator();
		while(ite.hasNext()) {
			Participante p = (Participante)ite.next();
			if(p.getCompDespPE()==null) ite.remove();			
		}
		
		
		System.out.println("Termina la construcción de comportamientosCP");
		
		for(Participante p: participantes) {
			p.getCompDespPE().cargarValoresRezagados();
		}
		
	}
	

	public void cargaAtributosEstaticosACompDespPE(){
		CompDespPE.setCantDias(datGen.getCantDias());
		CompDespPE.setCantPos(datGen.getCantPostes());
		CompDespPE.setDur1Pos(datGen.getDur1Pos());
		CompDespPE.setDurHorCP(datGen.getCantPostes()*datGen.getDur1Pos());
		CompDespPE.setInstIniCP(datGen.getInstIniCP());

	}


	public void despacharCP() {	
		int paso = 0;
		int escenario = 0;
		salidaPLMOP = despacharPE(corrida, paso, escenario, dirSalida, dirEntrada, dirSalida);	
	}
	
	
	
	public DatosSalidaProblemaLineal despacharPE(Corrida corridaActual, int paso, int escenario, String dirInfactible, String dirEntradasPL, String dirSalidasPL) {
			 
		entrada = new DatosEntradaProblemaLineal();

		for(Participante p : participantes) {
				CompDespPE c = p.getCompDespPE();
				if (c != null) {
					/**
					 * Carga la colección static basesVar de la clase padre y basesVarDelParticipante del CompDespPE del Participante 
					 * Expande las BaseVar del participante y carga variablesControl del participante
					 * Carga las variablesControl a entrada
					 */					
					c.aportarVarAEntrada(entrada);					
				}						
		}
		System.out.println("Termina la creación de variables de control del problema lineal");
		
			
		for(Participante p : participantes) {
				
				CompDespPE c = p.getCompDespPE();
				if (c != null) {
					/**
					 * Crea la colección basesRestDelParticipante en su CompDesp
					 * Crea la coleccion restricciones del participante en su compDesp
					 * Carga las restricciones a entrada
					 */
					c.aportarRestAEntrada(entrada);
					
					/**
					 * Crea el baseObj del participante p en su CompDesp
					 * Crea el objetivo del participante p en su CompDesp
					 * Carga el objetivo a entrada
					 */
					c.aportarObjAEntrada(entrada); 										
				}
				System.out.println("Termina construcción de restricciones y aportes al objetivo de " + p.getNombre());
		}
		
//		// Agrega las variables y restricciones que permiten calcular los costos por escenario.
//		// en el atributo this.entrada
		
		
		ArrayList<DatosVariableControl> varesc = BaseObj.creaVarsCostoPorEscenario();
		entrada.agregarVariables(varesc);

		Hashtable<String, DatosRestriccion> restesc = BaseObj.creaRestCostoPorEscenario();	
		entrada.agregarRestricciones(restesc);
				
		
		System.out.println("Termina carga de variables y restricciones de costo para cada escenario");
		
		System.out.println("Comienza a generar salidas de verificación de la formulación del problema: variables, restricciones, objetivo");
		
		imprimeVerificacionVariables();
		
		imprimeVerificacionRestricciones();
		
		imprimeVerificacionObjetivo();
		
		String archEntradasPL = dirEntradasPL + "/EntradaProbLineal.lp";
		entrada.imprimirEntradaPLNueva(archEntradasPL);
		
		System.out.println("Termina la impresión de entradas");

//		System.exit(1);
		
		System.out.println("Empieza la resolución del problema lineal");
		int itCambioRes=0;
		do {
			ProblemaHandler ph = ProblemaHandler.getInstance();
			salida = ph.resolver(entrada, escenario, paso, dirInfactible, dirSalidasPL);
			for (Participante p: participantes) {
				p.setUltimoInfactible(salida.isInfactible());
			} 

			if (salida.isInfactible()) { 
				if(itCambioRes>0){
					ph.cambiarResolvedor();
					itCambioRes=0;
				}
				itCambioRes++;
			}

		} while (salida.isInfactible());
			
		ProblemaHandler ph = ProblemaHandler.getInstance();
		ph.resetearResolvedor();
		
		
		EscritorProbLpSolve escLpSolve = new EscritorProbLpSolve(dirSalida, "prueba", entrada);
		escLpSolve.escribeProb();
		
		System.out.println("Termina la resolución del problema lineal y es factible");
		return salida;
		
	}


	/**
	 * Salida de verificación de BasesVar
	 */
	public void imprimeVerificacionVariables() {
		
		String archBasesVar = dirSalida + "/BasesVar.xlt";
		DirectoriosYArchivos.siExisteElimina(archBasesVar);
		boolean inicio = true;
		for(Participante p: participantes) {
			CompDespPE c = p.getCompDespPE();
			ArrayList<String> basesVarAl = new ArrayList<String>();
			basesVarAl.addAll(c.getBasesVar().keySet());
			Collections.sort(basesVarAl);		
			StringBuilder sb = new StringBuilder("");
			for(String s: basesVarAl) {
				sb.append(c.getBasesVar().get(s).toString() + "\n");
			}	
			if(!inicio)sb.append("\n\n");			
			if(!sb.toString().equalsIgnoreCase(""))DirectoriosYArchivos.agregaTexto(archBasesVar, sb.toString());	
		}
		
		ArrayList<DatosVariableControl> variables = entrada.getVariables();
		Collections.sort(variables);	
		StringBuilder sb = new StringBuilder();
		for(DatosVariableControl dvc: variables) {
			sb.append(dvc.toString() + "\n");
		}
		String archVar = dirSalida + "/Variables.xlt";
		DirectoriosYArchivos.siExisteElimina(archVar);
		DirectoriosYArchivos.agregaTexto(archVar, sb.toString());		
		sb = new StringBuilder("");
		for(DatosVariableControl dvc: BaseObj.getVarsCostoEsc()) {
			sb.append(dvc.toString() + "\n");
		}	
		DirectoriosYArchivos.agregaTexto(archVar, sb.toString());		
	}
	
	
	/**
	 * Salida de verificación de BasesRest 
	 */
	public void imprimeVerificacionRestricciones() {
		String archBasesRest = dirSalida + "/BasesRest.xlt";
		DirectoriosYArchivos.siExisteElimina(archBasesRest);
		String archRest = dirSalida + "/Restricciones.xlt";
		for(Participante p: participantes) {
			CompDespPE c = p.getCompDespPE();
			ArrayList<String> basesRestAl = new ArrayList<String>();
			basesRestAl.addAll(c.getBasesRest().keySet());
			Collections.sort(basesRestAl);		
			StringBuilder sb = new StringBuilder();
			for(String s: basesRestAl) {
				sb.append(c.getBasesRest().get(s).toString() + "\n");
			}	
			if(basesRestAl.size()>0)sb.append("\n\n");
			DirectoriosYArchivos.agregaTexto(archBasesRest, sb.toString());	
		
			ArrayList<DatosRestriccion> rest = entrada.getRestricciones();
			Collections.sort(rest);
			sb = new StringBuilder();
			int r=0;
			for(DatosRestriccion dr: rest) {
				sb.append(dr.creaSalida() + "\n");
				r++;
			}
			sb.append("\n\n\n");
			DirectoriosYArchivos.siExisteElimina(archRest);
			DirectoriosYArchivos.agregaTexto(archRest, sb.toString());		
		}
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n\n");
		
		Hashtable<String, DatosRestriccion> aux = BaseObj.getRestsCostoEsc();
		for(String s: aux.keySet()) {
			sb.append(aux.get(s).creaSalida());
		}
		DirectoriosYArchivos.agregaTexto(archRest, sb.toString());		
		
	}
	
	
	
	public void imprimeVerificacionObjetivo() {
		
		String archBasesObj = dirSalida + "/BasesObj.xlt";
		DirectoriosYArchivos.siExisteElimina(archBasesObj);		
		for(Participante p: participantes) {
			CompDespPE c = p.getCompDespPE();		
			BaseObj bo = c.getBaseObj();
			if(!bo.toString().equalsIgnoreCase(""))
				DirectoriosYArchivos.agregaTexto(archBasesObj, bo.toString()+"\n\n");	
			
			StringBuilder sb = new StringBuilder();		
			DatosObjetivo dobj = entrada.getObjetivo();
			Hashtable<String, Double> ht = dobj.getTerminos();
			ArrayList<String> ls = new ArrayList<String>();
			ls.addAll(ht.keySet());
			Collections.sort(ls);
			int il=0;
			for(String s: ls) {
				if(ht.get(s)>0) sb.append("+");
				sb.append(ht.get(s) + s + "\t");
				if(il%10==0 && il>0) sb.append("\n");
				il++;
			}
			String archObj = dirSalida + "/Objetivo.xlt";
			DirectoriosYArchivos.siExisteElimina(archObj);
			DirectoriosYArchivos.agregaTexto(archObj, sb.toString());			
		}
	}		
		
	
	@Override
	public void setDirEntrada(String dirEntrada) {
		this.dirEntrada = dirEntrada;
		
	}

	@Override
	public void setDirSalida(String dirSalida) {
		this.dirSalida = dirSalida;
		
	}

	@Override
	public void leerdatosGenerales() {
		
		datGen = cp_persistencia.CargadorDatosGenerales.devuelveDatosGeneralesCP(dirEntrada);	
		LineaTiempo lt = corrida.getLineaTiempo();
		String fecha = datGen.getInstIniCPS();
		long instIniLeido = lt.dameInstanteDeFecha(fecha);
		int segDiaIniLeido = lt.horaDeFecha(fecha)*Constantes.SEGUNDOSXHORA + 
				lt.minDeFecha(fecha)*Constantes.SEGUNDOSXMINUTO +
				lt.segDeFecha(fecha);   // segundo del día del instante leído
		int posteIni = Math.round(segDiaIniLeido/datGen.getDur1Pos());
		int segPosteIniCP = posteIni*datGen.getDur1Pos();
		int difSeg = segPosteIniCP-segDiaIniLeido;
		long instIniCP = instIniLeido + difSeg;
		datGen.setInstIniCP(instIniCP);
		datGen.setPosteIniDia(posteIni);
	}


	@Override
	public void leerDatosParticipantesCP() {		
		datosPartCP = CargadorDatosPartCP.devuelveDatosPartCP(dirEntrada);
	}
	
	
	

	@Override
	public void leerProcEstocasticos() {
		String arch = dirEntrada;
		int cantEtapas = datGen.getCantEtapas();
		int[] cantPostesEtapas = datGen.getCantPostesEtapas();
		DatosGrafoEscCP dge = CargadorGrafoEsc.devuelveGrafoEsc(dirEntrada, cantEtapas, cantPostesEtapas);
		
		grafo = new GrafoEscenarios(dge, datGen, corrida);	
		
		if(datGen.isUsaSoloEsc0()==true) {
			for(int e=0; e<datGen.getCantEtapas(); e++) {
				grafo.getCantEscEtapas()[e] = 1;
			}
		}
		
	}

	@Override
	public void producirSalidasCP() {
		
		if(ConCP.LPUSADO.equalsIgnoreCase(ConCP.LPMOP)){
			buscador = new  BuscadorResolvedoresMOP(null, salidaPLMOP, BaseVar.getCatalogoVarParPos(), corrida, grafo, datGen);
		}
		
		ArrayList<Barra> barras = corrida.getBarras();
		// Si la red es uninodal elimina las barras distintas de la barraUnica
		// si no elimina la barra única
		if(corrida.getRed().isRedUninodal(datGen.getInstIniCP())) {
			Iterator ite = barras.iterator();
			while(ite.hasNext()) {
				Participante p = (Participante)ite.next();
				if(p instanceof Barra && !(p.getNombre().equalsIgnoreCase(Constantes.NOMBREBARRAUNICA))) ite.remove();				
			}
		}else {
			// la red no es uninodal
			Iterator ite = barras.iterator();
			while(ite.hasNext()) {
				Participante p = (Participante)ite.next();
				if(p instanceof Barra && (p.getNombre().equalsIgnoreCase(Constantes.NOMBREBARRAUNICA))) ite.remove();				
			}
		}
		EscritorSalidasCP escritor = new EscritorSalidasCP(corrida, participantes, barras, grafo, datGen, buscador, dirSalida);
		
		String archParam = dirSalida + "/Parametros.xlt"; 	
		String archEscenarios = dirSalida + "/SalidaEscenarios.xlt";		
		
		DirectoriosYArchivos.siExisteElimina(archEscenarios);
		
		escenarios = CargadorEscParaSalidas.devuelveEscenariosSalidaCP(dirEntrada, grafo);
		int cantESal = escenarios.size();
		
		escritor.setParam(new ParamTextoSalidaUninodal(cantESal, datGen.getCantPostes()));
		
		for(int esc=0; esc<cantESal; esc++) {
			System.out.println("Escribe salida escenario " + esc);
			escritor.salidaUnEscSal(archEscenarios, escenarios.get(esc), participantes);
			if(esc==0) escritor.getParam().setCantFilas1Esc(escritor.getFila());
		}
		escritor.salidaParam(dirSalida);   // crea el archivo de los parámetros de salida para ser leídos en Excel
		System.out.println("Termina la salida de resultados para escenarios seleccionados");
	
		escritor.salidaDistribucionesPoste(dirSalida);  // genera distribuciones de probabilidad en los escenarios
		
		System.out.println("TERMINA LA SALIDA DE RESULTADOS");
		System.exit(0);
		
	}
	
	
	

	


	@Override
	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
		
	}


	@Override
	public void cargarDespachoCortoPlazo(DespachadorCortoPlazo despachador) {
		this.despachador = despachador;
		
	}


	public DespachadorCortoPlazo getDespachador() {
		return despachador;
	}


	public void setDespachador(DespachadorCortoPlazo despachador) {
		this.despachador = despachador;
	}


	public Hashtable<String, double[]> getValoresRezagados() {
		return valoresRezagados;
	}


	public void setValoresRezagados(Hashtable<String, double[]> valoresRezagados) {
		this.valoresRezagados = valoresRezagados;
	}


	public Corrida getCorrida() {
		return corrida;
	}


	public String getDirEntrada() {
		return dirEntrada;
	}


	public String getDirSalida() {
		return dirSalida;
	}


	public void setResoptim(ResOptim resoptim) {
		this.resoptim = resoptim;
	}


	public ArrayList<Participante> getParticipantes() {
		return participantes;
	}


	public void setParticipantes(ArrayList<Participante> participantes) {
		this.participantes = participantes;
	}


	public DatosGeneralesCP getDatGen() {
		return datGen;
	}


	public void setDatGen(DatosGeneralesCP datGen) {
		this.datGen = datGen;
	}


	public ArrayList<int[]> getEscenarios() {
		return escenarios;
	}


	public void setEscenarios(ArrayList<int[]> escenarios) {
		this.escenarios = escenarios;
	}


	public Hashtable<String, DatosPartCP> getDatosPartCP() {
		return datosPartCP;
	}


	public void setDatosPartCP(Hashtable<String, DatosPartCP> datosPartCP) {
		this.datosPartCP = datosPartCP;
	}


	public GrafoEscenarios getGrafo() {
		return grafo;
	}


	public void setGrafo(GrafoEscenarios grafo) {
		this.grafo = grafo;
	}


	public DatosEntradaProblemaLineal getEntrada() {
		return entrada;
	}


	public void setEntrada(DatosEntradaProblemaLineal entrada) {
		this.entrada = entrada;
	}


	public DatosSalidaProblemaLineal getSalida() {
		return salida;
	}


	public void setSalida(DatosSalidaProblemaLineal salida) {
		this.salida = salida;
	}


	public BuscadorResult getBuscador() {
		return buscador;
	}


	public void setBuscador(BuscadorResult buscador) {
		this.buscador = buscador;
	}


	public DatosSalidaProblemaLineal getSalidaPLMOP() {
		return salidaPLMOP;
	}


	public void setSalidaPLMOP(DatosSalidaProblemaLineal salidaPLMOP) {
		this.salidaPLMOP = salidaPLMOP;
	}


	public Hashtable<String, ContratoIntSist> getContratosSistemaDeCI() {
		return contratosSistemaDeCI;
	}


	public void setContratosSistemaDeCI(Hashtable<String, ContratoIntSist> contratosSistemaDeCI) {
		this.contratosSistemaDeCI = contratosSistemaDeCI;
	}


	
	
	

}
