/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorSalidasCP is part of MOP.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import cp_compdespProgEst.BarraCombCompDespPE;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.CompDespPE;
import cp_compdespProgEst.ConstHiperCompDespPE;
import cp_compdespProgEst.ContratoIntCompDespPE;
import cp_compdespProgEst.DemandaCompDespPE;
import cp_compdespProgEst.HidraulicoCompDespPE;
import cp_compdespProgEst.ImpoExpoCompDespPE;
import cp_compdespProgEst.TermicoCompDespPE;
import cp_datatypesEntradas.DatosConstHipCP;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosHiperplanoCP;
import cp_nuevosParticipantesCP.ConstructorHiperplanosPE;
import cp_nuevosParticipantesCP.ContratoIntSist;
import cp_persistencia.ParamTextoSalidaUninodal;
import cp_salidas.BuscadorResult;
import cp_salidas.ResultadoEsc1V;
import cp_salidas.ResultadosDePE;
import datatypesResOptim.DatosHiperplano;
import parque.Barra;
import parque.BarraCombustible;
import parque.CicloCombinado;
import parque.Combustible;
import parque.Corrida;
import parque.Demanda;
import parque.Falla;
import parque.Generador;
import parque.GeneradorHidraulico;
import parque.GeneradorTermico;
import parque.ImpoExpo;
import parque.Participante;
import parque.RedCombustible;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.EnumeradorLexicografico;
import utilitarios.UtilArrays;

public class EscritorSalidasCP {
	
//	private static int filasMaxDeFuentes = 15; // Cantidad de fuentes previstas para el gráfico. Si la cantidad es menor se dejan filas vacías
	private static int filasMaxDeFuentes = 25; // Cantidad de fuentes previstas para el gráfico. Si la cantidad es menor se dejan filas vacías

	private int cantFuentes; // La cantidad real de fuentes
	private Corrida corrida;
	private ArrayList<Barra> barras;
	private DatosGeneralesCP datGen;
	private BuscadorResult buscador;
	
	private static final ArrayList<Double> PERCENTILES = new ArrayList<> (Arrays.asList(0.01, 0.10, 0.25, 0.5, 0.75, 0.9, 0.99));
	
	
	private ArrayList<Participante> participantes;
	
	private ResultadosDePE resultadosPE;  // Resultados generales de la programación estocástica
	
	private String archSalEsc;
	private GrafoEscenarios grafo;
	private int cantPostes;
	private int fila; // Contador de filas en la salida SOLO UNINODAL. La primera es la fila 1.
	private ParamTextoSalidaUninodal param;  // guarda los parámetros de salida para leer en Excel
	private boolean primerEsc;  // indica si se está en el primer escenario
	private StringBuilder sb;
	
	public EscritorSalidasCP(Corrida corrida, ArrayList<Participante> participantes, ArrayList<Barra> barras, GrafoEscenarios grafo, DatosGeneralesCP datGen, BuscadorResult buscador, String archSalEsc) {
		super();
		this.corrida = corrida;
		this.participantes = participantes;
		this.barras = barras;
		this.datGen = datGen;
		this.buscador = buscador;
		this.archSalEsc = archSalEsc;
		this.grafo = grafo;
		cantPostes = datGen.getCantPostes();
		fila = 1;	
		primerEsc = true;
		resultadosPE = new ResultadosDePE(grafo);
	}


	public void salidaParam(String dirSalida) {
		String archParam = dirSalida + "/ParametrosTextoSalida.xlt";
		DirectoriosYArchivos.siExisteElimina(archParam);
		DirectoriosYArchivos.agregaTexto(archParam, param.toString());
	}


	public void salidaUnEscSal(String dirSal, int[] vecEsc, ArrayList<Participante> participantes) {
		int[] vecEsc2;		
		if(datGen.getCantEtapas()==vecEsc.length) {
			vecEsc2 = vecEsc;
		}else {
			// los escenarios de entrada tienen más etapas que las del grafo, se acotan a las etapas del grafo
			vecEsc2 = utilitarios.UtilArrays.truncaNFinalesI(vecEsc, vecEsc.length-datGen.getCantEtapas());
		}	
		for(Barra b: barras) {
			System.out.println("Carga potencias por poste, escenario " + vecEsc.toString() + "barra " + b.getNombre());
			cargaPotPorPoste(vecEsc2, participantes, b);
			System.out.println("Carga otras variables, escenario " + vecEsc.toString() + "barra " + b.getNombre());
			cargaOtrasVarDeParticipantes(vecEsc2, participantes, b);
			System.out.println("Imprime potencias por poste, escenario " + vecEsc.toString() + "barra " + b.getNombre());
			imprimePotPorPoste(dirSal, vecEsc2, participantes, b);	
			System.out.println("Imprime otras variables, escenario " + vecEsc.toString() + "barra " + b.getNombre());
			imprimeOtrasVarDeParticipantes(dirSal, vecEsc2, participantes, b);
			
		}
		primerEsc = false;
	}	
	
	
	/**
	 * Carga los costos por escenario y las potencias por poste
	 * @param vecEsc
	 * @param participantes
	 * @param b
	 */
	public void cargaPotPorPoste(int[] vecEsc, ArrayList<Participante> participantes, Barra b) {

		int cantpos = datGen.getCantPostes();
		cantFuentes = 0;		

		double costoTot = buscador.dameObjetivo();
		String nomVB = BaseVar.generaNomVar(ConCP.ZVB, ConCP.NOMBRE_CONST_HIP_PE, cantpos-1, vecEsc); 
		double costoFut = 0;
		if(datGen.isUsaHip()) costoFut = buscador.dame1ValorVariable(nomVB);
		
		
		
		String nomCostoEsc = BaseVar.generaNomVar(ConCP.COSTOESC, ConCP.DESPACHO, cantpos-1, vecEsc);
		double costoEsc = buscador.dame1ValorVariable(nomCostoEsc);
		
		double costoPaso = costoEsc - costoFut;
				
				
		resultadosPE.cargaRes1V1Esc(ConCP.COSTOTOT, ConCP.DESPACHO, vecEsc, ConCP.UNICO, new Double[]{costoTot} );
		resultadosPE.cargaRes1V1Esc(ConCP.COSTOESC, ConCP.DESPACHO, vecEsc, ConCP.UNICO, new Double[]{costoEsc} );
		resultadosPE.cargaRes1V1Esc(ConCP.COSTOPASO, ConCP.DESPACHO, vecEsc, ConCP.UNICO, new Double[]{costoPaso} );
		resultadosPE.cargaRes1V1Esc(ConCP.COSTOFUT, ConCP.DESPACHO, vecEsc, ConCP.UNICO, new Double[]{costoFut} );
		
		ArrayList<String> parts = new ArrayList<String>();

		for(Generador g: b.getGeneradores()) {
			parts.add(g.getNombre());
		}
		
		for(ImpoExpo ie: b.getImpoExpos()) {
			ImpoExpoCompDespPE iePE = (ImpoExpoCompDespPE)ie.getCompDespPE();
			for(String s: iePE.getListaOf()) {
				parts.add(s);
			}
		}
		


		for(String s: parts) {
			Double[] pot = buscador.dameVariable(ConCP.POT, s, vecEsc).getValores();
			if(pot!=null){
				resultadosPE.cargaRes1V1Esc(ConCP.POT, s, vecEsc, ConCP.POSTE, pot);
			}
		}
		
		for(ImpoExpo ie: b.getImpoExpos()) {
			ImpoExpoCompDespPE iePE = (ImpoExpoCompDespPE)ie.getCompDespPE();
			Double[] pot = buscador.dameVariable(ConCP.POTIETOTENT, ie.getNombre(), vecEsc).getValores();
			resultadosPE.cargaRes1V1Esc(ConCP.POTIETOTENT, ie.getNombre(), vecEsc, ConCP.POSTE, pot);
		}
		
		for (Demanda d : b.getDemandas()) {
			Falla f = d.getFalla();
			String par = f.getNombre();
			for(int e=0; e<ConCP.FALLAPOTESC.size();e++) {
				Double[] pot = buscador.dameVariable(ConCP.FALLAPOTESC.get(e), par, vecEsc).getValores();
				if(pot!=null){
					resultadosPE.cargaRes1V1Esc(ConCP.FALLAPOTESC.get(e), par, vecEsc, ConCP.POSTE, pot);
				}
			}
		}
		
		for(Participante p: participantes) {
			if(p instanceof ContratoIntSist) {
				Double[] pot = buscador.dameVariable(ConCP.POT, p.getNombre(), vecEsc).getValores();
				resultadosPE.cargaRes1V1Esc(ConCP.POT, p.getNombre(), vecEsc, ConCP.POSTE, pot);
			}
		}
		
	}
	
	
	public void imprimePotPorPoste(String archSal, int[] vecEsc, ArrayList<Participante> participantes, Barra b) {		
		int cphora = datGen.getCantPosHora();  // cantidad de postes por hora
		int cpdia = datGen.getCantPosDia();
		int posIni = datGen.getPosteIniDia();
		int cantDias = datGen.getCantDias();
		int cantpos = datGen.getCantPostes();
		cantFuentes = 0;		
		sb = new StringBuilder();
		sb.append("SALIDAS DEL ESCENARIO  \t");
		for(int e=0; e<vecEsc.length; e++) {
			sb.append(vecEsc[e] + "\t");
		}
		double costoTot = buscador.dameObjetivo();
		String nomCostoEsc = BaseVar.generaNomVar(ConCP.COSTOESC, ConCP.DESPACHO, cantpos-1, vecEsc);
		double costoEsc = buscador.dame1ValorVariable(nomCostoEsc);
		String nomVB = BaseVar.generaNomVar(ConCP.ZVB, ConCP.NOMBRE_CONST_HIP_PE, cantpos-1, vecEsc); 
		double costoFut = 0; 
		if(datGen.isUsaHip()) costoFut = buscador.dame1ValorVariable(nomVB);
		double costoPaso = costoEsc - costoFut;
		sb.append("CostoTotMUSD\t" + costoTot/1E6 + "\tCostoEscMUSD:\t" + costoEsc/1E6 +  "\tCostoPasoMUSD\t" + costoPaso/1E6 + "\tCostoFutMUSD\t" + costoFut/1E6);				
		sb.append("\n");
		fila++;
		sb.append("BARRA " + b.getNombre());
		sb.append("\n");
		fila++;
		sb.append("POTENCIAS POR PARTICIPANTE POR POSTE EN MW\n");
		fila++;
		sb.append("Dia\t");
		for(int p=0; p<cantpos; p++) {
			sb.append(grafo.diaDePoste(p) + "\t");
		}
		sb.append("\n");
		fila++;
		sb.append("Poste en el horizonte\t");
		for(int p=0; p<cantpos; p++) {
			sb.append(p + "\t");
		}
		sb.append("\n");
		fila++;
		int pos = posIni;  // pos es el poste dentro del día
		String[] diaposte = new String[cantpos];
 		for(int id=0; id<cantDias; id++) {
			if(id>0) pos = 0;
			for(int p=pos; p<cpdia; p++) {
				int hora = p/cphora;
				int poshora = p%cphora;
				if(cphora>1) {
					diaposte[id*cpdia + p] = "d" + id + "-h" + hora + "-" + poshora;
				}else {
					diaposte[id*cpdia + p] = "d" + id + "-h" + hora;
				}	
			}
		}
		if(primerEsc) param.setFilaEncabezado(fila);
		sb.append("\n");
		sb.append("FUENTES\t");
		for(int p=0; p<cantpos; p++) {
			sb.append(diaposte[p] + "\t");
		}
		sb.append("\n");
		fila = fila + 2;
		int filaIniF = 0;
		if(primerEsc) {
			param.setfIniFuentes(fila);
			filaIniF = fila;
		}
		ArrayList<String> parts = new ArrayList<String>();
		for(Generador g: b.getGeneradores()) {
			parts.add(g.getNombre());
			cantFuentes++;
		}
		for(ImpoExpo ie: b.getImpoExpos()) {
			ImpoExpoCompDespPE iePE = (ImpoExpoCompDespPE)ie.getCompDespPE();		
			for(String s: iePE.getListaOf()) {
				if(iePE.getdCP().getOperacion().get(s).equalsIgnoreCase(Constantes.COMPRA)) 
				parts.add(s);
				cantFuentes++;
			}
		}
		for(String s: parts) {
			Double[] pot = buscador.dameVariable(ConCP.POT, s, vecEsc).getValores();
			if(pot!=null){
				sb.append(s + "\t");					
				for(int ip=0; ip<pot.length; ip++) {
					sb.append(pot[ip] + "\t");									
				}						
			}
			sb.append("\n");
			fila++;
		}
		
		for (Demanda d : b.getDemandas()) {
			Falla f = d.getFalla();
			String par = f.getNombre();
			for(int e=0; e<ConCP.FALLAPOTESC.size();e++) {
				Double[] pot = buscador.dameVariable(ConCP.FALLAPOTESC.get(e), par, vecEsc).getValores();
				if(pot!=null){
					sb.append(ConCP.FALLAPOTESC.get(e)+"_"+par + "\t");					
					for(int ip=0; ip<pot.length; ip++) {
						sb.append(pot[ip] + "\t");									
					}
					cantFuentes++;
				}
				sb.append("\n");
				fila++;
			}
		}
		
		for(int ifil=0; ifil< filasMaxDeFuentes - cantFuentes; ifil++) {
			sb.append("serie-" + ifil + "\t");
			for(int ip=0; ip<cantPostes; ip++) {
				sb.append(0-0 + "\t");									
			}
			sb.append("\n");
			fila++;
		}
		
		if(primerEsc) param.setLargoFuentes(fila - filaIniF);
		parts = new ArrayList<String>();
		
		sb.append("\nUSOS\t");
		for(int p=0; p<cantpos; p++) {
			sb.append(diaposte[p] + "\t");
		}
		sb.append("\n");
		fila = fila + 2;
		int filaIniU = 0;
		if(primerEsc) {
			param.setfIniUsos(fila);
			filaIniU = fila;
		}
		for (Demanda d : b.getDemandas()) {
			DemandaCompDespPE dc = (DemandaCompDespPE)d.getCompDespPE();
			String nomVA = dc.getNomVADem();
			String par = d.getNombre();
			sb.append(par + "\t");
			for(int p=0; p<cantpos; p++) {
				int e = grafo.etapaDePoste(p);
				double pot1 = grafo.valorVA(nomVA, p, vecEsc[e]);
				sb.append(pot1 + "\t");		
			}
			sb.append("\n");
			fila++;
		}
		
		for(ImpoExpo ie: b.getImpoExpos()) {
			ImpoExpoCompDespPE iePE = (ImpoExpoCompDespPE)ie.getCompDespPE();		
			for(String s: iePE.getListaOf()) {
				if(iePE.getdCP().getOperacion().get(s).equalsIgnoreCase(Constantes.VENTA)) 
				parts.add(s);
			}
		}
		
		
		
		for(Participante p: participantes) {
			if(p instanceof ContratoIntSist) {
				parts.add(p.getNombre());
			}
		}
		
		
		for(String s: parts) {
			Double[] pot = buscador.dameVariable(ConCP.POT, s, vecEsc).getValores();
			if(pot!=null){
				sb.append(s + "\t");					
				for(int ip=0; ip<pot.length; ip++) {
					sb.append(pot[ip] + "\t");									
				}						
			}
			sb.append("\n");
			fila++;
		}
		param.setLargoUsos(fila - filaIniU);
		DirectoriosYArchivos.agregaTexto(archSal, sb.toString());
		fila++;
	}
	


	public void imprimeOtrasVarDeParticipantes(String archSal, int[] vecEsc, ArrayList<Participante> participantes, Barra b) {
		sb = new StringBuilder();		
		sb.append("OTRAS VARIABLES DE PARTICIPANTES POR POSTE \n\n");	
		fila = fila + 2;
		if(primerEsc) param.setFilaCosmar(fila);
		sb.append(dameLinea("CostoMarginal", ResultadosDePE.devuelveRes1V1Esc(ConCP.CMG, b.getNombre(), vecEsc)));
		sb.append("\n\n");
		fila = fila + 2;
		ArrayList<String> nomVar;	
		for(Generador g: b.getGeneradores()) {
			int filaIniG = 0;
			if(g instanceof CicloCombinado) {
				imprimeCicloCombinado(g, vecEsc);
			}else if(g instanceof GeneradorHidraulico) {
				imprimeGeneradorHidraulico(g, vecEsc);
				
			}else if(g instanceof GeneradorTermico) {
				imprimeGeneradorTermico(g, vecEsc);
			}			
		}
		
		for(ImpoExpo ie: b.getImpoExpos()) {
			imprimeImpoExpo(ie, vecEsc);
		}			
		if(datGen.isUsaHip())  imprimeConstHiperplanos(vecEsc);		
		DirectoriosYArchivos.agregaTexto(archSal, sb.toString());			
		sb = null;  // libera la memoria del sb
		
	}
	
	
	public void imprimeCicloCombinado(Generador g, int[] vecEsc) {
		ArrayList<String> nomVar = new ArrayList<String>();	
		sb.append(g.getNombre()+ "\n");
		fila++;
		int filaIniG = 0;
		if(primerEsc) {
			filaIniG = fila;		
			param.getFilaIniOtrosDatos().put(g.getNombre(), filaIniG);
		}
		CicloCombCompDespPE cd = (CicloCombCompDespPE)g.getCompDespPE();
		nomVar.add(ConCP.NMODCC); 
		nomVar.add(ConCP.NMODCCINI);
		nomVar.add(ConCP.POTCC); 
		nomVar.add(ConCP.POTAINI); 
		if(!cd.getdCP().getListaASim().isEmpty()) nomVar.add(ConCP.POTASIM);
		if(!cd.getdCP().getListaPar().isEmpty()) nomVar.add(ConCP.POTPAR);	
		nomVar.add(ConCP.POTTGAB);
		for(String s: cd.getdCP().getListaAIni()) {
			nomVar.add(ConCP.XAINI + "_" + s); 
		}				
		for(String s: cd.getdCP().getListaASim()) {
			nomVar.add(ConCP.XASIM + "_" + s); 
		}								
		for(String s: cd.getdCP().getListaPar()) {
			nomVar.add(ConCP.YPAR + "_" + s); 
		}					
		for(String s: cd.getdCP().getListaAIni()) {
			if(cd.getdCP().getHorasMax().containsKey(s))
			nomVar.add(ConCP.ZINI + "_" + s); 
		}	
		nomVar.add(ConCP.NMODTG); 
		nomVar.add(ConCP.ARRTGAB); 
		sb.append(textoDeVariables(nomVar, g.getNombre(), vecEsc));
		if(primerEsc) param.getLargoOtrosDatos().put(g.getNombre(), fila-filaIniG + 1);
	}
	
	
	public void imprimeGeneradorHidraulico(Generador g, int[] vecEsc) {
		ArrayList<String>nomVar = new ArrayList<String>();
		sb.append(g.getNombre() + "-------------\n");
		fila++;
		int filaIniG = 0;
		if(primerEsc) {
			filaIniG = fila;
			param.getFilaIniOtrosDatos().put(g.getNombre(), filaIniG);	
		}
		GeneradorHidraulico gh = (GeneradorHidraulico)g;
		HidraulicoCompDespPE cd = (HidraulicoCompDespPE)g.getCompDespPE();
		
		Double[] cotas = dameCotas(cd, vecEsc);
		sb.append("CotaAlInicioDelPoste\t");
		for(int p=0; p<cantPostes; p++) {
			sb.append(cotas[p] + "\t");
		}
		sb.append("\n");
		fila++;
		
		sb.append(dameStringAportes(cd, vecEsc));
		fila = fila + 2;
		
		nomVar.add(ConCP.QTUR); 
		nomVar.add(ConCP.QVER);
		nomVar.add(ConCP.QERO);
		sb.append(textoDeVariables(nomVar, g.getNombre(), vecEsc));
		
		sb.append(dameStringVolEro(cd, vecEsc));
		sb.append(dameStringVolEvaFilt(cd, vecEsc));
					
		nomVar = new ArrayList<String>();	
		nomVar.add(ConCP.VOLINI); 
		nomVar.add(ConCP.VOLFIN); 
		if(gh.isHayControldeCotasMaximas()) {
			nomVar.add(ConCP.VOLEXCED);
		}else {
			nomVar.add(null);
		}
		if(gh.isHayControldeCotasMinimas()) {
			nomVar.add(ConCP.VOLFALT);
		}else {
			nomVar.add(null);
		}
		if(cd.isImpactoQmin()) {
			nomVar.add(ConCP.QFALT);
		}else {
			nomVar.add(null);
		}
		sb.append(textoDeVariables(nomVar, g.getNombre(), vecEsc));				
		sb.append(dameStringValAguaYEnerg(cd, vecEsc));
		if(primerEsc) param.getLargoOtrosDatos().put(g.getNombre(), fila-filaIniG+1);		
	}
	
	
	public void imprimeGeneradorTermico(Generador g, int[] vecEsc) {
		
		ArrayList<String> nomVar = new ArrayList<String>();	
		sb.append(g.getNombre()+ "\n");
		fila++;
		int filaIniG = fila;
		if(primerEsc) param.getFilaIniOtrosDatos().put(g.getNombre(), filaIniG);
		TermicoCompDespPE tcd = (TermicoCompDespPE)g.getCompDespPE();

		nomVar.add(ConCP.NMOD);  
		sb.append(textoDeVariables(nomVar, g.getNombre(), vecEsc));
		sb.append(dameStringCMg1Termico(tcd, vecEsc));
		if(primerEsc) param.getLargoOtrosDatos().put(g.getNombre(), fila-filaIniG+1);
		
	}
	
	public void imprimeImpoExpo(ImpoExpo ie, int[] vecEsc) {
		int filaIniG = fila+2;
		if(primerEsc) param.getFilaIniOtrosDatos().put(ie.getNombre(), filaIniG);
		sb.append("\n");
		sb.append(ie.getNombre()+ "\n");
		fila = fila +2;
		ArrayList<String> nomVar = new ArrayList<String>();
		if(ie.isMinTec()) {				
			nomVar.add(ConCP.XIMPOEXPENT);  
			nomVar.add(ConCP.XIMPOEXPSAL);					
		}
		nomVar.add(ConCP.POTIETOTENT);
		sb.append(textoDeVariables(nomVar, ie.getNombre(), vecEsc));  			
		ImpoExpoCompDespPE iePE = (ImpoExpoCompDespPE)ie.getCompDespPE();
		for(String s: iePE.getListaOf()) {	
			if(!iePE.getdCP().getTipoOf().get(s).equalsIgnoreCase(ConCP.OALEAT)) {
				nomVar = new ArrayList<String>();
				sb.append(s + "\n");
				fila++;
				nomVar.add(ConCP.XINID);
				sb.append(textoDeVariables(nomVar, s, vecEsc));
			}				
		}	
		if(primerEsc) param.getLargoOtrosDatos().put(ie.getNombre(), fila-filaIniG+1);
		
		
	}
	
	
	
	public void imprimeConstHiperplanos(int[] vecEsc) {
		
		ConstHiperCompDespPE cH = (ConstHiperCompDespPE)CompDespPE.getConstHip().getCompDespPE();
		DatosConstHipCP dat = cH.getdCP();
		String[] nombresVE = dat.getNombresVE();  // nombres de las VE en el hiperplano
		int cantVE = nombresVE.length;
		String[] nombresVEVC = new String[nombresVE.length];  // nombres de las variables de control en el problema lineal en el escenario
		for(int ive=0; ive<cantVE; ive++) {
			NombreBaseVar nb = CompDespPE.getConstHip().getNombresVEParticipantes().get(nombresVE[ive]);
			nombresVEVC[ive] = nb.devuelveNombreVariableControl(vecEsc);
		}

		ArrayList<Integer> numHiperMax = new ArrayList<Integer>();  // numeros de los hiperplanos que dan el máximo valor de Bellman en el estado final
		double valMax = Double.MIN_VALUE;
		int ihMax = 0;
		int ih = 0;
		for(DatosHiperplanoCP dh: dat.getHiperplanos()) {
			double vh = dameValorHiperplano(dh, nombresVEVC);
			if(vh>= valMax) {
				ihMax = ih;
				valMax = vh;
			}
			ih++;
		}
		numHiperMax.add(ihMax);
		ih = 0;
		for(DatosHiperplanoCP dh: dat.getHiperplanos()) {
			double vh = dameValorHiperplano(dh, nombresVEVC);
			if(valMax - vh < ConCP.EPSIHIPERPLANOS_USD) {
				numHiperMax.add(ih);
			}
			ih++;
		}		
		sb.append("\n\n");
		fila = fila + 2;
		int filaIniH = fila;
		if(primerEsc) param.getFilaIniOtrosDatos().put(cH.getNomPar(), filaIniH);
		
		sb.append("HIPERPLANOS ACTIVOS " + "\t");
		for(Integer ihip: numHiperMax) {
			sb.append(ihip + "\t");
		}
		sb.append("\n");
		fila++;
		sb.append("ValorBellmanFinal\t"  +  valMax + "\n");
		fila++;
		sb.append("\t");
		for(int ive=0; ive<cantVE; ive++) {
			sb.append(nombresVE[ive] + "\t");
		}
		sb.append("\n");
		fila++;
		sb.append("EstadoFinal\t");
		for(int ive=0; ive<cantVE; ive++) {
			sb.append(buscador.dame1ValorVariable(nombresVEVC[ive]) + "\t");
		}	
		sb.append("\n");
		fila++;
		sb.append("ValoresDeRecursos\n");
		fila++;
		for(Integer ihip: numHiperMax) {
			sb.append("HIP_" + ihip + "\t");
			for(int ive=0; ive<cantVE; ive++) {
				sb.append( dat.getHiperplanos().get(ihip).getCoeficientes()[ive+1]+ "\t");
			}
			sb.append("\n");
			fila++;
		}
		if(primerEsc) param.getLargoOtrosDatos().put(cH.getNomPar(), fila-filaIniH+1);
		

	}
	
	
	
	public void cargaOtrasVarDeParticipantes(int[] vecEsc, ArrayList<Participante> participantes, Barra b) {

		cargaCostosMarginales(b, vecEsc);
		
		ArrayList<String> nomVar;	
		for(Generador g: b.getGeneradores()) {
			if(g instanceof CicloCombinado) {
				cargaCicloCombinado(g, vecEsc);
			}else if(g instanceof GeneradorHidraulico) {
				cargaGeneradorHidraulico(g, vecEsc);
				
			}else if(g instanceof GeneradorTermico) {
				cargaGeneradorTermico(g, vecEsc);
			}			
		}
		
		for(ImpoExpo ie: b.getImpoExpos()) {
			cargaImpoExpo(ie, vecEsc);
		}			
		if(datGen.isUsaHip())  cargaConstHiperplanos(vecEsc);		
		
	}
	
	/**
	 * Devuelve el costo marginal por poste en la barra b, dado que en cada poste es conocido 
	 * el escenario. 
	 * Por esto la variable dual respecto a la restricción de demanda en el poste y escenario se
	 * divide por la probabilidad del escenario en el poste. 
	 * @param b
	 * @param vecEsc
	 */
	public void cargaCostosMarginales(Barra b, int[] vecEsc) {
		System.out.println("Carga costos marginales ");
		Double[] cmg = buscador.dameDuales(ConCP.RBALBARRA, b.getNombre(), vecEsc).getValores();
		double[] probs = grafo.devuelveProbsPostes(vecEsc);
		Double[] cmgD = new Double[cantPostes];
		for(int p=0; p<cantPostes; p++) {
			cmgD[p] = cmg[p]/probs[p];
		}
		ResultadosDePE.cargaRes1V1Esc(ConCP.CMG, b.getNombre(), vecEsc, ConCP.POSTE, cmgD);
	}
	
	
	
	public void cargaCicloCombinado(Generador g, int[] vecEsc) {
		System.out.println("Carga resultados " + g.getNombre());
		ArrayList<String> nomVar = new ArrayList<String>();	
		CicloCombCompDespPE cd = (CicloCombCompDespPE)g.getCompDespPE();
		nomVar.add(ConCP.NMODCC); 
		nomVar.add(ConCP.NMODCCINI);
		nomVar.add(ConCP.POTCC); 
		nomVar.add(ConCP.POTAINI); 
		if(!cd.getdCP().getListaASim().isEmpty()) nomVar.add(ConCP.POTASIM);
		if(!cd.getdCP().getListaPar().isEmpty()) nomVar.add(ConCP.POTPAR);	
		nomVar.add(ConCP.POTTGAB);
		for(String s: cd.getdCP().getListaAIni()) {
			nomVar.add(ConCP.XAINI + "_" + s); 
		}				
		for(String s: cd.getdCP().getListaASim()) {
			nomVar.add(ConCP.XASIM + "_" + s); 
		}								
		for(String s: cd.getdCP().getListaPar()) {
			nomVar.add(ConCP.YPAR + "_" + s); 
		}					
		for(String s: cd.getdCP().getListaAIni()) {
			if(cd.getdCP().getHorasMax().containsKey(s))
			nomVar.add(ConCP.ZINI + "_" + s); 
		}	
		nomVar.add(ConCP.NMODTG); 
		nomVar.add(ConCP.ARRTGAB); 
		cargaDeVariables(nomVar, g.getNombre(), vecEsc);

	}
	
	public void cargaGeneradorHidraulico(Generador g, int[] vecEsc) {
		System.out.println("Carga resultados " + g.getNombre());
		ArrayList<String>nomVar = new ArrayList<String>();
	
		GeneradorHidraulico gh = (GeneradorHidraulico)g;
		HidraulicoCompDespPE cd = (HidraulicoCompDespPE)g.getCompDespPE();
		String nomPar = cd.getNomPar();
		
		Double[] cotas = dameCotas(cd, vecEsc);
		resultadosPE.cargaRes1V1Esc(ConCP.COTA, nomPar, vecEsc, ConCP.POSTE, cotas);
		
		Double[] aportes = dameAportesM3Seg(cd, vecEsc);
		resultadosPE.cargaRes1V1Esc(ConCP.APORTE, nomPar, vecEsc, ConCP.POSTE, aportes);
		
		nomVar.add(ConCP.QTUR); 
		nomVar.add(ConCP.QVER);
		nomVar.add(ConCP.QERO);
		cargaDeVariables(nomVar, g.getNombre(), vecEsc);
		
		cargaVolEro(cd,vecEsc);
		cargaVolEvaFilt(cd, vecEsc);
		
					
		nomVar = new ArrayList<String>();	
		nomVar.add(ConCP.VOLINI); 
		nomVar.add(ConCP.VOLFIN); 
		if(gh.isHayControldeCotasMaximas()) {
			nomVar.add(ConCP.VOLEXCED);
		}else {
			nomVar.add(null);
		}
		if(gh.isHayControldeCotasMinimas()) {
			nomVar.add(ConCP.VOLFALT);
		}else {
			nomVar.add(null);
		}
		if(cd.isImpactoQmin()) {
			nomVar.add(ConCP.QFALT);
		}else {
			nomVar.add(null);
		}
		cargaDeVariables(nomVar, g.getNombre(), vecEsc);				
		cargaValAguaYEnerg(cd, vecEsc);
		
	}
	
	
	/**
	 * Carga los erogados en hm3 por poste en una línea
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public void cargaVolEro(HidraulicoCompDespPE cd, int[] vecEsc) {
		Double[] qero = buscador.dameVariable(ConCP.QERO, cd.getNomPar(), vecEsc).getValores();
		Double[] vero = new Double[cantPostes];
		for(int p=0; p<cantPostes; p++) {
			vero[p]= qero[p]*datGen.getDur1Pos()/Constantes.M3XHM3;
		}
		resultadosPE.cargaRes1V1Esc(ConCP.QERO, cd.getNomPar(), vecEsc, ConCP.POSTE, vero);
	}
	
	
	public void cargaVolEvaFilt(HidraulicoCompDespPE cd, int[] vecEsc) {
		double ef = cd.getEvafilPosHm3();
		Double[] evafil = new Double[cantPostes];
		for(int p=0; p<datGen.getCantPostes(); p++) {
			evafil[p] = ef;
		}
		resultadosPE.cargaRes1V1Esc(ConCP.EVAFILHM3, cd.getNomPar(), vecEsc, ConCP.POSTE, evafil);
	}
	
	
	public void cargaValAguaYEnerg(HidraulicoCompDespPE cd, int[] vecEsc) {
		GeneradorHidraulico gh = cd.getGh();
		Double[] volAb;
		Double[] dual = new Double[cantPostes];
		Double[] dualAb = null;  // valor del agua de la primera central con lago aguas abajo, que se resta para el cálculo del costo de la energía
		Double[] vol;
		Double[] qero;
		GeneradorHidraulico gi;
		GeneradorHidraulico gabi;
		HidraulicoCompDespPE cdAbi = null;
		HidraulicoCompDespPE cdi = cd;
		double[] coef = new double[cantPostes];
		dualAb = new Double[cantPostes];
		volAb = new Double[cantPostes];
		for(int p=0; p<cantPostes; p++) {
			volAb[p] = 0.0;
			dualAb[p]=0.0;
		}		
		String nombres = "";  // Se van acumulando los nombres de las centrales que suman al coeficiente energético
		gabi = gh;
		boolean ini = true;
		if(cd.isConLago()) {
			dual = buscador.dameDuales(ConCP.RBALLAGO, cd.getNomPar(), vecEsc).getValores();
			do {
				gi = gabi;
				cdi = (HidraulicoCompDespPE) gi.getCompDespPE();
				 // USD/hm3
				vol = buscador.dameVariable(ConCP.VOLINI, cdi.getNomPar(), vecEsc).getValores();
				qero = buscador.dameVariable(ConCP.QERO, cdi.getNomPar(), vecEsc).getValores();
				gabi = gi.getGeneradorAbajo();
				if(gabi!=null) {
					cdAbi = (HidraulicoCompDespPE)gabi.getCompDespPE();			
					volAb =  buscador.dameVariable(ConCP.VOLINI, cdAbi.getNomPar(), vecEsc).getValores(); 
					if(cdAbi.isConLago()) dualAb = buscador.dameDuales(ConCP.RBALLAGO, cdAbi.getNomPar(), vecEsc).getValores();
				}else {
					volAb = new Double[cantPostes];
					for(int p=0; p<cantPostes; p++) {
						volAb[p] = 0.0;
					}	
				}
				for(int p=0; p<cantPostes; p++) {
					coef[p] += cdi.dameCoefEnergMargMWhHm3(vol[p], qero[p], volAb[p])*cdi.getFactorCompartir();
				}
				if(!ini) nombres += "+";
				nombres += cdi.getNomPar();		
				ini = false;										
			}while(gabi!=null && !cdAbi.isConLago());
		}
		Double[] valagua = new Double[cantPostes];
		Double[] valenerg = new Double[cantPostes]; 
		double cv = 0;
		double[] probs = grafo.devuelveProbsPostes(vecEsc);
		Double[] cmgD = new Double[cantPostes];
		if(cd.isConLago()) {	
			for(int p=1; p<cantPostes; p++) {
				valagua[p] = -dual[p]/probs[p];
				valenerg[p] = (-dual[p] + dualAb[p])/(coef[p]*probs[p]);
			}
		}
		ResultadosDePE.cargaRes1V1Esc(ConCP.VALAGUA, cd.getNomPar(), vecEsc, ConCP.POSTE, valagua);
		ResultadosDePE.cargaRes1V1Esc(ConCP.VALENERG, cd.getNomPar(), vecEsc, ConCP.POSTE, valenerg);	
	}
	
	
	
	
	
	public void cargaGeneradorTermico(Generador g, int[] vecEsc) {
		System.out.println("Carga resultados " + g.getNombre());
		ArrayList<String> nomVar = new ArrayList<String>();	
		TermicoCompDespPE tcd = (TermicoCompDespPE)g.getCompDespPE();
		nomVar.add(ConCP.NMOD);  
		cargaDeVariables(nomVar, g.getNombre(), vecEsc);
		cargaCMg1Termico(tcd, vecEsc);		
	}
	
	
	public void cargaImpoExpo(ImpoExpo ie, int[] vecEsc) {
		System.out.println("Carga resultados " + ie.getNombre());
		ArrayList<String> nomVar = new ArrayList<String>();
		if(ie.isMinTec()) {				
			nomVar.add(ConCP.XIMPOEXPENT);  
			nomVar.add(ConCP.XIMPOEXPSAL);					
		}
		nomVar.add(ConCP.POTIETOTENT);
		cargaDeVariables(nomVar, ie.getNombre(), vecEsc);  			
		ImpoExpoCompDespPE iePE = (ImpoExpoCompDespPE)ie.getCompDespPE();
		for(String s: iePE.getListaOf()) {	
			if(!iePE.getdCP().getTipoOf().get(s).equalsIgnoreCase(ConCP.OALEAT)) {
				nomVar = new ArrayList<String>();
				nomVar.add(ConCP.XINID);
				cargaDeVariables(nomVar, s, vecEsc);
			}				
		}	
	}
	
	
	public void cargaConstHiperplanos(int[] vecEsc) {
		System.out.println("carga resultados constructor hiperplanos");
		ConstHiperCompDespPE cH = (ConstHiperCompDespPE)CompDespPE.getConstHip().getCompDespPE();
		DatosConstHipCP dat = cH.getdCP();
		String[] nombresVE = dat.getNombresVE();  // nombres de las VE en el hiperplano
		int cantVE = nombresVE.length;
		String[] nombresVEVC = new String[nombresVE.length];  // nombres de las variables de control en el problema lineal en el escenario
		for(int ive=0; ive<cantVE; ive++) {
			NombreBaseVar nb = CompDespPE.getConstHip().getNombresVEParticipantes().get(nombresVE[ive]);
			nombresVEVC[ive] = nb.devuelveNombreVariableControl(vecEsc);
		}

		ArrayList<Integer> numHiperMax = new ArrayList<Integer>();  // numeros de los hiperplanos que dan el máximo valor de Bellman en el estado final
		double valMax = Double.MIN_VALUE;
		int ihMax = 0;
		int ih = 0;
		for(DatosHiperplanoCP dh: dat.getHiperplanos()) {
			double vh = dameValorHiperplano(dh, nombresVEVC);
			if(vh>= valMax) {
				ihMax = ih;
				valMax = vh;
			}
			ih++;
		}
		resultadosPE.cargaRes1V1Esc(ConCP.COSTOFUT, ConCP.DESPACHO, vecEsc, ConCP.UNICO, new Double[] {valMax});

		for(int ive=0; ive<cantVE; ive++) {
			double valVE = buscador.dame1ValorVariable(nombresVEVC[ive]);
			resultadosPE.cargaRes1V1Esc(nombresVE[ive], ConCP.DESPACHO, vecEsc, ConCP.UNICO, new Double[] {valMax});
		}	
	}
	

	
	
	
	/**
	 * Devuelve el valor de un hiperplano al final del horizonte en un escenario
	 * @param dh datatype de un hiperplano
	 * @param nombresVEVC los nombres de las variables de control del problema lineal asociados
	 * a las variables del hiperplano en un escenario en particular
	 */
	public double dameValorHiperplano(DatosHiperplanoCP dh, String[] nombresVEVC) {
		double[] coefs = dh.getCoeficientes();
		double result = coefs[0];
		for(int ic=1; ic<coefs.length; ic++) {
			double val = buscador.dame1ValorVariable(nombresVEVC[ic-1]);
 			result += coefs[ic]*val;
		}
		return result;
	}
	
	
	
	
	/**
	 * Devuelve el texto para la salida de un conjunto de variables cuyos nombres
	 * están en nomVar, para el participante nomPar, cuando el escenario desde el origen
	 * hasta el final es vecEsc
	 * @param nomVar
	 * @param nomPar
	 * @param vecEsc
	 * @param rango: POSTE, DIA O UNICO
	 * @return
	 */
	public String textoDeVariables(ArrayList<String> nomVar, String nomPar, int[] vecEsc) {
		StringBuilder sb = new StringBuilder();
		for(int iv=0; iv<nomVar.size(); iv++) {
			String nv = nomVar.get(iv);
			if(nv!=null) {
				sb.append(nv + "\t");
				Double[] val = buscador.dameVariable(nv, nomPar, vecEsc).getValores();
				if(BaseVar.esPorPoste(nv, nomPar)) {
					for(int j=0; j<cantPostes; j++) {
						if(val[j]!=null) {
							sb.append(val[j] + "\t");
						}else {
							sb.append(0 + "\t");
						}
					}
				}else {
					for(int j=0; j<datGen.getCantDias(); j++) {
						if(val[j]!=null) {
							sb.append(val[grafo.diaDePoste(j)] + "\t");
						}else {
							sb.append(0 + "\t");
						}
					}				
				}
			}
			sb.append("\n");
			fila++;
		}
		return sb.toString();
	}
	
	
	
	/**
	 * Carga en ResultadosPE un conjunto de variables cuyos nombres
	 * están en nomVar, para el participante nomPar, cuando el escenario desde el origen
	 * hasta el final es vecEsc
	 * @param nomVar
	 * @param nomPar
	 * @param vecEsc
	 * @return
	 */
	public void cargaDeVariables(ArrayList<String> nomVar, String nomPar, int[] vecEsc) {
		for(int iv=0; iv<nomVar.size(); iv++) {
			String nv = nomVar.get(iv);
			if(nv!=null) {
				String rango = ConCP.POSTE;
				if (BaseVar.esPorPoste(nv, nomPar)== false) rango = ConCP.DIA;
				Double[] val = buscador.dameVariable(nv, nomPar, vecEsc).getValores();
				if(val.length == 1)	rango = ConCP.UNICO;
				resultadosPE.cargaRes1V1Esc(nv, nomPar, vecEsc, rango, val);
			}
		}		
	}
	
	
	
	/**
	 * Devuelve las cotas de un hidráulico a lo largo del horizonte CP
	 * @param cd  CompDespPE del hidráulico
	 * @param vecEsc
	 * @return
	 */
//	public double[] dameCotas(HidraulicoCompDespPE cd, int[] vecEsc) {		
//		Double[] vols = buscador.dameVariable(ConCP.VOLINI, cd.getNomPar(), vecEsc).getValores();
//		double[] cotas = new double[datGen.getCantPostes()];
//		for(int j=0; j<datGen.getCantPostes(); j++) {
//			cotas[j] = ((GeneradorHidraulico)cd.getParticipante()).getfCovo().dameValor(vols[j]);
//		}
//		return cotas;
//	}
	
	/**
	 * Devuelve las cotas de un hidráulico a lo largo del horizonte CP
	 * @param cd  CompDespPE del hidráulico
	 * @param vecEsc
	 * @return
	 */
	public Double[] dameCotas(HidraulicoCompDespPE cd, int[] vecEsc) {		
		Double[] vols = buscador.dameVariable(ConCP.VOLINI, cd.getNomPar(), vecEsc).getValores();
		Double[] cotas = new Double[datGen.getCantPostes()];
		for(int j=0; j<datGen.getCantPostes(); j++) {
			cotas[j] = ((GeneradorHidraulico)cd.getParticipante()).getfCovo().dameValor(vols[j]);
		}
		return cotas;
	}	
	
	
	/**
	 * Devuelve el texto con los aportes en m3 y en hm3 por poste en dos líneas
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public String dameStringAportes(HidraulicoCompDespPE cd, int[] vecEsc) {
		StringBuilder m = new StringBuilder(ConCP.APORTE+"-m3\t");
		StringBuilder hm = new StringBuilder(ConCP.APORTE+"-hm3\t");
		for(int p=0; p<datGen.getCantPostes(); p++) {
			String nomVAAp = cd.getNombreVAAporte();
			int esc = vecEsc[grafo.etapaDePoste(p)];
			double auxM3 = grafo.valorVA(nomVAAp, p, esc);
			double auxHm3 = auxM3*datGen.getDur1Pos()/Constantes.M3XHM3;
			m.append(auxM3 + "\t");
			hm.append(auxHm3+ "\t");			
		}
		return m.toString() + "\n" + hm.toString() + "\n";				
	}
	
	
	
	/**
	 * Devuelve el texto con los aportes en m3 y en hm3 por poste en dos líneas
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public Double[] dameAportesM3Seg(HidraulicoCompDespPE cd, int[] vecEsc) {
		Double[] aportes = new Double[cantPostes];
		String nomVAAp = cd.getNombreVAAporte();
		for(int p=0; p<cantPostes; p++) {
			int esc = vecEsc[grafo.etapaDePoste(p)];
			double auxM3 = grafo.valorVA(nomVAAp, p, esc);
			aportes[p] = auxM3;
		}
		return aportes;				
	}	
	
	/**
	 * Devuelve el texto con los erogados en hm3 por poste en una línea
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public String dameStringVolEro(HidraulicoCompDespPE cd, int[] vecEsc) {
		StringBuilder vero = new StringBuilder("volero-hm3\t");
		Double[] qero = buscador.dameVariable(ConCP.QERO, cd.getNomPar(), vecEsc).getValores();
		for(int p=0; p<datGen.getCantPostes(); p++) {
			vero.append(qero[p]*datGen.getDur1Pos()/Constantes.M3XHM3 + "\t");
		}
		vero.append("\n");
		fila++;
		return vero.toString();
	}
	
	
	
	/**
	 * Devuelve los erogados en hm3 por poste en una línea
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public Double[] dameVolEro(HidraulicoCompDespPE cd, int[] vecEsc) {
		Double[] volero = new Double[cantPostes];
		Double[] qero = buscador.dameVariable(ConCP.QERO, cd.getNomPar(), vecEsc).getValores();
		for(int p=0; p<datGen.getCantPostes(); p++) {
			volero[p] = qero[p]*datGen.getDur1Pos()/Constantes.M3XHM3;
		}
		return volero;
	}
	
	
	
	/**
	 * Devuelve el texto con los volúmenes perdidos por evaporación y filtración por
	 * poste en una línea
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public String dameStringVolEvaFilt(HidraulicoCompDespPE cd, int[] vecEsc) {
		double ef = cd.getEvafilPosHm3();
		StringBuilder filef = new StringBuilder("volEvapYFilt-hm3\t");
		for(int p=0; p<datGen.getCantPostes(); p++) {
			filef.append(ef + "\t");
		}
		filef.append("\n");
		fila++;
		return filef.toString();		
	}
	
	
	
	/**
	 * Devuelve los volúmenes perdidos por evaporación y filtración por
	 * poste en una línea
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public Double[] dameVolEvaFilt(HidraulicoCompDespPE cd, int[] vecEsc) {
		double ef = cd.getEvafilPosHm3();
		Double[] result = new Double[cantPostes];
		for(int p=0; p<datGen.getCantPostes(); p++) {
			result[p] = ef;
		}
		return result;
	}
	
	
	
	/**
	 * Devuelve una línea de texto con el encabezado titulo y los valores de valores
	 * separados por tabulaciones
	 * 
	 * @param titulo si es null no hay encabezado en la línea
	 * @param valores
	 * @return
	 */
	public String dameLinea(String titulo, Double[] valores) {
		StringBuilder sb = new StringBuilder(titulo);
		if(titulo!=null) sb.append("\t");
		if(valores.length!=cantPostes) {
			System.out.println("en EscritorSalidasCP método dameLinea hay error de largo de valores");
			System.exit(1);
		}
		for(int p=0; p<cantPostes; p++) {
			sb.append(valores[p] + "\t");
		}
		return sb.toString();		
	}
	
	/**
	 * Devuelve los valores del agua de la central y los costos variables de la central
	 * generando individualmente
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public String dameStringValAguaYEnerg(HidraulicoCompDespPE cd, int[] vecEsc) {
		GeneradorHidraulico gh = cd.getGh();
		Double[] volAb;
		Double[] dual = new Double[cantPostes];
		Double[] dualAb = null;  // valor del agua de la primera central con lago aguas abajo, que se resta para el cálculo del costo de la energía
		Double[] vol;
		Double[] qero;
		GeneradorHidraulico gi;
		GeneradorHidraulico gabi;
		HidraulicoCompDespPE cdAbi = null;
		HidraulicoCompDespPE cdi = cd;
		double[] coef = new double[cantPostes];
		dualAb = new Double[cantPostes];
		volAb = new Double[cantPostes];
		for(int p=0; p<cantPostes; p++) {
			volAb[p] = 0.0;
			dualAb[p]=0.0;
		}		
		String nombres = "";  // Se van acumulando los nombres de las centrales que suman al coeficiente energético
		gabi = gh;
		boolean ini = true;
		if(cd.isConLago()) {
			dual = buscador.dameDuales(ConCP.RBALLAGO, cd.getNomPar(), vecEsc).getValores();
			do {
				gi = gabi;
				cdi = (HidraulicoCompDespPE) gi.getCompDespPE();
				 // USD/hm3
				vol = buscador.dameVariable(ConCP.VOLINI, cdi.getNomPar(), vecEsc).getValores();
				qero = buscador.dameVariable(ConCP.QERO, cdi.getNomPar(), vecEsc).getValores();
				gabi = gi.getGeneradorAbajo();
				if(gabi!=null) {
					cdAbi = (HidraulicoCompDespPE)gabi.getCompDespPE();			
					volAb =  buscador.dameVariable(ConCP.VOLINI, cdAbi.getNomPar(), vecEsc).getValores(); 
					if(cdAbi.isConLago()) dualAb = buscador.dameDuales(ConCP.RBALLAGO, cdAbi.getNomPar(), vecEsc).getValores();
				}else {
					volAb = new Double[cantPostes];
					for(int p=0; p<cantPostes; p++) {
						volAb[p] = 0.0;
					}	
				}
				for(int p=0; p<cantPostes; p++) {
					coef[p] += cdi.dameCoefEnergMargMWhHm3(vol[p], qero[p], volAb[p])*cdi.getFactorCompartir();
				}
				if(!ini) nombres += "+";
				nombres += cdi.getNomPar();		
				ini = false;										
			}while(gabi!=null && !cdAbi.isConLago());
		}
	
		StringBuilder sb1 = new StringBuilder("ValorDelAguaUSDHm3-" +  nombres + "\t");
		StringBuilder sb2 = new StringBuilder("ValorDeLaEnergia-" + nombres + "\t");
		double cv = 0;
		double[] probs = grafo.devuelveProbsPostes(vecEsc);
		if(cd.isConLago()) {
			sb1.append("\t");
			for(int p=1; p<cantPostes; p++) {
				sb1.append(-dual[p]/probs[p] + "\t");;				
				cv = (-dual[p] + dualAb[p])/(coef[p]*probs[p]);
				sb2.append(cv + "\t");			
			}
		}
		fila = fila + 2;
		return sb1.toString() + "\n" + sb2.toString() + "\n";		
	}
	
	/**
	 * Devuelve el costo marginal de energía en USD/MWh para cada uno de
	 * los combustibles del térmico
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public String dameStringCMg1Termico(TermicoCompDespPE cd, int[] vecEsc){
		GeneradorTermico gt = cd.getGt();
		ArrayList<String> combs = gt.getListaCombustibles();
		Hashtable<String, BarraCombustible> barras = gt.getBarrasCombustible();
		TermicoCompDespPE tcd = (TermicoCompDespPE)gt.getCompDespPE();
		double potTerProp = tcd.getPotTerProp();
		String result = "";
		for(String c: combs) {
			Double[] cMg = new Double[cantPostes];
			BarraCombustible b = barras.get(c);
			RedCombustible red = b.getRedAsociada();
			if(b.getRedAsociada().isUninodal()) b = red.getBarraunica();
			Combustible comb = b.getComb();
			BarraCombCompDespPE bc = (BarraCombCompDespPE)b.getCompDespPE();
			ResultadoEsc1V cmgCombUSDPorUnidad = buscador.dameDuales(ConCP.RBALBARRACOMB + "_" + comb.getNombre(), bc.getNomPar(), vecEsc);
			double pci = comb.getPci(); // MWh/unidad
			for(int p=0; p<cantPostes; p++) {
				cMg[p] = cmgCombUSDPorUnidad.getValores()[p]*potTerProp/pci;
			}
			result = result + dameLinea("CMg-" + c, cMg) + "\n";
			fila++;
		}
		return result;		
	}


	/**
	 * Devuelve el costo marginal de energía en USD/MWh para cada uno de
	 * los combustibles del térmico
	 * @param cd
	 * @param vecEsc
	 * @return
	 */
	public void cargaCMg1Termico(TermicoCompDespPE cd, int[] vecEsc){
		GeneradorTermico gt = cd.getGt();
		String nomPar = cd.getNomPar();
		ArrayList<String> combs = gt.getListaCombustibles();
		Hashtable<String, BarraCombustible> barras = gt.getBarrasCombustible();
		TermicoCompDespPE tcd = (TermicoCompDespPE)gt.getCompDespPE();
		double potTerProp = tcd.getPotTerProp();

		for(String c: combs) {
			Double[] cMg = new Double[cantPostes];
			BarraCombustible b = barras.get(c);
			RedCombustible red = b.getRedAsociada();
			if(b.getRedAsociada().isUninodal()) b = red.getBarraunica();
			Combustible comb = b.getComb();
			BarraCombCompDespPE bc = (BarraCombCompDespPE)b.getCompDespPE();
			ResultadoEsc1V cmgCombUSDPorUnidad = buscador.dameDuales(ConCP.RBALBARRACOMB + "_" + comb.getNombre(), bc.getNomPar(), vecEsc);
			double pci = comb.getPci(); // MWh/unidad
			for(int p=0; p<cantPostes; p++) {
				cMg[p] = cmgCombUSDPorUnidad.getValores()[p]*potTerProp/pci;
				
			}
			resultadosPE.cargaRes1V1Esc(ConCP.CMG1TER, nomPar , vecEsc, ConCP.POSTE, cMg);
		}
	}
	
	
	/**
	 * Imprime las salidas de distribuciones de probabilidad de variables con un valor por poste
	 * En columnas los postes, en filas los escenarios.
	 * @param dirSalida
	 */
	public void salidaDistribucionesPoste(String dirSalida) {
		if(grafo.isEscUnico()) {
			System.out.println("SOLO HAY UN ESCENARIO, NO SALEN DISTRIBUCIONES");
			return;
		}
		String archSalida = dirSalida + "/DistribucionesPorPoste.xlt";
		int cantEtapas = datGen.getCantEtapas();
		EnumeradorLexicografico enEsc = grafo.getEnumeradores().get(cantEtapas-1);
		int cantEsc = enEsc.getCantTotalVectores();
		DirectoriosYArchivos.siExisteElimina(archSalida);
		String[] titFil = new String[cantEsc];
		int[] esc = enEsc.devuelveVector();
		int iesc = 0;		
		while(esc!=null) {
			titFil[iesc] = BaseVar.generaSufijoEscenario(esc);
			esc = enEsc.devuelveVector();
			iesc++;
		}	
		DirectoriosYArchivos.agregaTexto(archSalida, "CANT_POSTES \t" + cantPostes);
		DirectoriosYArchivos.agregaTexto(archSalida, "CANT_ESCENARIOS \t" + cantEsc);
		for(Participante p: participantes) {
			String nompar = p.getNombre();
			ArrayList<String> nombresVars = ResultadosDePE.getVariablesDePar().get(nompar);
			if(nombresVars==null) {
				System.out.println("No hay salidas por escenario de participante " + nompar);
				String st = p.getNombre();
				DirectoriosYArchivos.agregaTexto(archSalida, st + "\tNo tiene salidas por escenario");
			}else {
				StringBuilder sb = new StringBuilder(p.getNombre() + "\t");
				for(int ip=0; ip<cantPostes; ip++) {
					sb.append("===============\t");
				}
				DirectoriosYArchivos.agregaTexto(archSalida, sb.toString());
				for(String nomvar: nombresVars) {
					DirectoriosYArchivos.agregaTexto(archSalida, nompar + "_" + nomvar);
					Double[][] valores = new Double[cantEsc][cantPostes];
					String nvp = BaseVar.generaNomVarPar(nomvar, nompar);
					String cl = BaseVar.generaNomVarPar(nomvar, nompar);
					if(resultadosPE.getRangoDeVarPar().get(cl).equalsIgnoreCase(ConCP.UNICO)) {
						enEsc.inicializaEnum();
						esc = enEsc.devuelveVector();
						iesc = 0;					
						while(esc!=null) {
							String cl2 = ResultadosDePE.claveResultadosDePE(nomvar, nompar, esc);
							valores[iesc] = ResultadosDePE.getResultados().get(cl2).getValores();
							esc = enEsc.devuelveVector();
							iesc++;
						}			
					}
					String[] titCol = null;
					String sep = "\t";
//					DirectoriosYArchivos.agregaTablaReal(archSalida,titCol,titFil, valores, sep);
					double[][] vesp = new double[1][];
					if(nomvar.equalsIgnoreCase(ConCP.VALAGUA) && nompar.equalsIgnoreCase("bonete") ) {
						int pp = 0;
					}
					vesp[0] = ResultadosDePE.devuelveValorEsperado(nomvar, nompar);
					titFil = new String[] {"ValorEsperado"};
					DirectoriosYArchivos.agregaTablaReal(archSalida, titCol, titFil, vesp, sep);
					double[][] valPercent = ResultadosDePE.devuelvePercentiles(nomvar, nompar, PERCENTILES);
					titFil = new String[PERCENTILES.size()];
					for(int i=0; i<titFil.length; i++) {
						titFil[i] = "Percentil" + ((Double)(PERCENTILES.get(i)*100)).toString() + "%";
					}
					DirectoriosYArchivos.agregaTablaReal(archSalida, titCol, titFil, valPercent, sep);
				}			
			}
		}
		return;
	}
	

	public Corrida getCorrida() {
		return corrida;
	}


	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}


	public DatosGeneralesCP getDatGen() {
		return datGen;
	}


	public void setDatGen(DatosGeneralesCP datGen) {
		this.datGen = datGen;
	}


	public BuscadorResult getBuscador() {
		return buscador;
	}


	public void setBuscador(BuscadorResult buscador) {
		this.buscador = buscador;
	}


	public String getArchSalEsc() {
		return archSalEsc;
	}


	public void setArchSalEsc(String archSalEsc) {
		this.archSalEsc = archSalEsc;
	}


	public GrafoEscenarios getGrafo() {
		return grafo;
	}


	public void setGrafo(GrafoEscenarios grafo) {
		this.grafo = grafo;
	}


	public int getCantPostes() {
		return cantPostes;
	}


	public void setCantPostes(int cantPostes) {
		this.cantPostes = cantPostes;
	}


	public int getFila() {
		return fila;
	}


	public void setFila(int fila) {
		this.fila = fila;
	}


	public ParamTextoSalidaUninodal getParam() {
		return param;
	}


	public void setParam(ParamTextoSalidaUninodal param) {
		this.param = param;
	}


	public boolean isPrimerEsc() {
		return primerEsc;
	}


	public void setPrimerEsc(boolean primerEsc) {
		this.primerEsc = primerEsc;
	}	
	
	
	
	
	
	
}
