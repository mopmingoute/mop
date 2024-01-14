/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorDatosPartCP is part of MOP.
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

package cp_persistencia;

import java.util.ArrayList;
import java.util.Hashtable;

import cp_datatypesEntradas.DatosCicloCombCP;
import cp_datatypesEntradas.DatosConstHipCP;
import cp_datatypesEntradas.DatosContIntCP;
import cp_datatypesEntradas.DatosDemandaCP;
import cp_datatypesEntradas.DatosEolicoCP;
import cp_datatypesEntradas.DatosFallaCP;
import cp_datatypesEntradas.DatosHidroCP;
import cp_datatypesEntradas.DatosHiperplanoCP;
import cp_datatypesEntradas.DatosImpoExpoCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_datatypesEntradas.DatosTermicoCP;
import cp_despacho.ConCP;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.Constantes;
import utilitarios.LeerDatosArchivo;
import utilitarios.UtilArrays;

public class CargadorDatosPartCP {
	
	private static int i=0;
	private static ArrayList<ArrayList<String>> texto;
	private static AsistenteLectorEscritorTextos lector;
	private static String nomPar;
	private static String dirEntradas; // directorio donde se leen los archivos
	
	
	
	/**
	 * Devuelve una tabla con los datos de los participantes para el despacho de corto plazo
	 * 
	 * Clave: nombre del participantes
	 * Valor: un objeto de la clase padre de los datatypes de corto plazo
	 * 
	 * Cada método de lectura de un participante debe dejar el índice i en la última línea de sus datos
	 * 
	 * @param dirEntradas
	 * @return
	 */
	public static Hashtable<String, DatosPartCP> devuelveDatosPartCP(String dirEnt){
		dirEntradas = dirEnt;
		String arch = dirEntradas + "/EntradasParticipantesCP.txt";
		texto = LeerDatosArchivo.getDatos(arch);	
		lector = new AsistenteLectorEscritorTextos(texto, arch);  
		Hashtable<String, DatosPartCP> result = new Hashtable<String, DatosPartCP>();
              
		do {
			if(!texto.get(i).get(0).equalsIgnoreCase("PARTICIPANTE")) {
				System.out.println("ERROR EN EntradasParticipantesCP " + texto.get(i).get(0));
			}else {
				nomPar = texto.get(i).get(1);
				i++;
				String tipo = lector.cargaPalabra(i, "TIPO");
				i++;
				if(tipo.equalsIgnoreCase(Constantes.CC)) {
					result.put(nomPar, cargaCCCP(nomPar, tipo));
				}else if(tipo.equalsIgnoreCase(Constantes.HID)) {
					result.put(nomPar, cargaHIDCP(nomPar, tipo));
				}else if(tipo.equalsIgnoreCase(Constantes.IMPOEXPO)) {
					result.put(nomPar, cargaIECP(nomPar, tipo));
				}else if(tipo.equalsIgnoreCase(Constantes.DEM)) {
					result.put(nomPar, cargaDemCP(nomPar, tipo));
				}else if(tipo.equalsIgnoreCase(Constantes.EOLO)) {
					result.put(nomPar, cargaEolicoCP(nomPar, tipo));
				}else if(tipo.equalsIgnoreCase(Constantes.CONTRATOINTERRUMPIBLE)) {
					result.put(nomPar, cargaContInt(nomPar, tipo));
				}else if(tipo.equalsIgnoreCase(Constantes.FALLA)) {
					result.put(nomPar, cargaFallaCP(nomPar, tipo));
				}else if(tipo.equalsIgnoreCase(Constantes.CONST_HIPERPLANOS)) {
					result.put(nomPar, cargaConstHipCP(ConCP.NOMBRE_CONST_HIP_PE, tipo));
				}else if(tipo.equalsIgnoreCase(Constantes.TER)) {
					result.put(nomPar, cargaTermicoCP(nomPar, tipo));
				}
			}
			i++;
		}while(i<texto.size());
	
		return result;
			
	}
	
	
	public static DatosHidroCP cargaHIDCP(String nomPar, String tipo) {
		
		String nvaap = lector.cargaPalabra(i, "VA_APORTE");
		i++;
		String modoPot = lector.cargaPalabra(i, "MODO_POT");
		i++;
		String modoVerMax = lector.cargaPalabra(i, "MODO_VERMAX");
		i++;
		double[] qeroAbaH = null;
		lector.verificaEtiqueta(i, "ERO_ABAJO_ANTERIOR");
		if(texto.get(i).get(1).equalsIgnoreCase("SI")) {
			int cantP = texto.get(i).size()-2;
			qeroAbaH = new double[cantP];
			for(int ic=0; ic<cantP; ic++) {
				qeroAbaH[ic] = Double.parseDouble(texto.get(i).get(ic+2));								
			}
		}
		i++;
		double cotaIni = lector.cargaReal(i, "COTAINI");
		i++;
		double valAgua = lector.cargaReal(i, "VALOR_AGUA_FINAL");

		DatosHidroCP result = new DatosHidroCP(nomPar, tipo, nvaap, qeroAbaH, cotaIni, valAgua, modoPot, modoVerMax);		
		
		System.out.println("Termina lectura de datos CP de Hidro " + nomPar);
		return result;
	}
	
	
	public static DatosTermicoCP cargaTermicoCP(String nomPar, String tipo) {
		double cosArr1Mod = lector.cargaReal(i, "COSTO_ARR_1MOD");
		i++;
		int cantModIni = lector.cargaEntero(i, "CANT_MOD_INI");
		DatosTermicoCP result = new DatosTermicoCP(nomPar, tipo, cosArr1Mod, cantModIni);
		return result;		
	}
	
	
	public static DatosCicloCombCP cargaCCCP(String nomPar, String tipo) {
		
		// CARGA DATOS DE ARRANQUES INICIALES
		ArrayList<String> listaAIni = (lector.cargaLista(i, "LISTA_AINI"));
		int cantAIni = listaAIni.size();
		i++;
		ArrayList<String> horasMaxL = lector.cargaLista(i, "HORAS_MAX");
		Hashtable<String, Double> horasMax = new Hashtable<String, Double>();
		int a=0;
		for(String s: listaAIni) {
			if(!horasMaxL.get(a).equalsIgnoreCase(ConCP.SINLIMITE))
					horasMax.put(s, Double.parseDouble(horasMaxL.get(a)));			
			a++;
		}
		Hashtable<String, Integer> postesMax = new Hashtable<String, Integer>();

		i++;
		Hashtable<String, Integer>  nmodAIni = new Hashtable<String, Integer>();
		Hashtable<String, Double>  costAIni = new Hashtable<String, Double>();
		Hashtable<String, double[]> potAIni = new Hashtable<String, double[]>(); 
		Hashtable<String, Integer> durAIni = new Hashtable<String, Integer>();
		for(int il=0; il<cantAIni; il++) {			
			String s = listaAIni.get(il);
			lector.verificaEtiqueta(i, s);
			nmodAIni.put(s, Integer.parseInt(texto.get(i).get(1)));
			costAIni.put(s, Double.parseDouble(texto.get(i).get(2)));
			double[] pot = new double[texto.get(i).size()-3];
			for(int ic = 0; ic<pot.length; ic++) {
				pot[ic] = Double.parseDouble(texto.get(i).get(ic+3));
			}
			potAIni.put(s, pot);
			durAIni.put(s, pot.length);
			i++;
		}
		
		Hashtable<String, ArrayList<Integer>> postesAIniD = new Hashtable<String, ArrayList<Integer>>();
		for(int il=0; il<cantAIni; il++) {
			String s = listaAIni.get(il);
			lector.verificaEtiqueta(i, s);
			ArrayList<Integer> pos = new ArrayList<Integer>();
			int nval = texto.get(i).size()-1;
			for(int ic = 0; ic<nval; ic++) {
				pos.add(Integer.parseInt(texto.get(i).get(ic+1)));
			}
			postesAIniD.put(s, pos);
			i++;
		}		
				
		
		// CARGA DATOS DE ARRANQUES SIMPLES
		ArrayList<String> listaASim = (lector.cargaLista(i, "LISTA_ASIM"));
		int cantASim = listaASim.size();
		i++;
		Hashtable<String, Integer>  nmodASim = new Hashtable<String, Integer>();
		Hashtable<String, Double>  costASim = new Hashtable<String, Double>();
		Hashtable<String, double[]> potASim = new Hashtable<String, double[]>(); 
		Hashtable<String, Integer> durASim = new Hashtable<String, Integer>();
		for(int il=0; il<cantASim; il++) {
			String s = listaASim.get(il);
			lector.verificaEtiqueta(i, s);
			nmodASim.put(s, Integer.parseInt(texto.get(i).get(1)));
			costASim.put(s, Double.parseDouble(texto.get(i).get(2)));
			double[] pot = new double[texto.get(i).size()-3];
			for(int ic = 0; ic<pot.length; ic++) {
				pot[ic] = Double.parseDouble(texto.get(i).get(ic+3));
			}
			potASim.put(s, pot);
			durASim.put(s, pot.length);
			i++;
		}
		
		Hashtable<String, ArrayList<Integer>> postesASimD = new Hashtable<String, ArrayList<Integer>>();
		for(int il=0; il<cantASim; il++) {
			String s = listaASim.get(il);
			lector.verificaEtiqueta(i, s);
			ArrayList<Integer> pos = new ArrayList<Integer>();
			int nval = texto.get(i).size()-1;
			for(int ic = 0; ic<nval; ic++) {
				pos.add(Integer.parseInt(texto.get(i).get(ic+1)));
			}
			postesASimD.put(s, pos);
			i++;
		}		
				
		// CARGA LOS DATOS DE PARADAS
		
		ArrayList<String> listaPar = (lector.cargaLista(i, "LISTA_PAR"));
		int cantPar = listaPar.size();
		i++;
		Hashtable<String, Integer>  nmodPar = new Hashtable<String, Integer>();
		Hashtable<String, Double>  costPar = new Hashtable<String, Double>();
		Hashtable<String, double[]> potPar = new Hashtable<String, double[]>(); 
		Hashtable<String, Integer> durPar = new Hashtable<String, Integer>();
		for(int il=0; il<cantPar; il++) {
			String s = listaPar.get(il);
			lector.verificaEtiqueta(i, s);
			nmodPar.put(s, Integer.parseInt(texto.get(i).get(1)));
			costPar.put(s, Double.parseDouble(texto.get(i).get(2)));
			double[] pot = new double[texto.get(i).size()-3];
			for(int ic = 0; ic<pot.length; ic++) {
				pot[ic] = Double.parseDouble(texto.get(i).get(ic+3));
			}
			potPar.put(s, pot);
			durPar.put(s, pot.length);
			i++;
		}	
		
		
		Hashtable<String, ArrayList<Integer>> postesParD = new Hashtable<String, ArrayList<Integer>>();
		for(int il=0; il<cantPar; il++) {
			String s = listaPar.get(il);
			lector.verificaEtiqueta(i, s);
			ArrayList<Integer> pos = new ArrayList<Integer>();
			int nval = texto.get(i).size()-1;
			for(int ic = 0; ic<nval; ic++) {
				pos.add(Integer.parseInt(texto.get(i).get(ic+1)));
			}
			postesParD.put(s, pos);
			i++;
		}	
		
		// Carga horas de apagado previas del CV
		int horasApagadoCV = lector.cargaEntero(i , "HORAS_APAGADO_CV");
		i++;
		// Carga datos iniciales
		int nmodCCIni = lector.cargaEntero(i , "NMOD_TG_COMB_INI");
		i++;
		
		int nmodTGIni = lector.cargaEntero(i , "NMOD_TG_AB_INI");
		i++;
		
		// Arranques y paradas en ejecución al inicio del horizonte
		ArrayList<String> listaEnEjec = new ArrayList<String>();
		Hashtable<String, Integer> posteIniEE = new Hashtable<String, Integer>(); 
		if(!texto.get(i).get(1).equalsIgnoreCase("NO")) {
			ArrayList<String> ars = lector.cargaLista(i, "ARR_Y_PAR_EN_EJECUCION");	
			i++;
			for(String s: ars) {		
				int pa = lector.cargaEntero(i, s);
				i++;	
				if(pa>=0) {
					System.out.println("Un arranque en ejecución comienza para poste>= 0");
					System.exit(1);
				}else {
					posteIniEE.put(s, pa);
				}
				i++;
				
			}
		}else {
			i++;
		}
		// costo de arranque TG ciclo abierto
		double costArrTGAb = lector.cargaReal( i, "COSTARRTGAB");		
		i++;
		// cant máxima de arranques en el horizonte CP
		int cantMab = lector.cargaEntero(i, "CANT_ARR_TGAB_MAX");  // DE TGS EN CICLO ABIERTO
		i++;
		int cantMIni = lector.cargaEntero(i, "CANT_ARR_INI_MAX");  // DEL CV EN ARRANQUES INICIALES DE TGS EN CICLO COMBINADO
		i++;
		int cantMSim = lector.cargaEntero(i, "CANT_ARR_SIM_MAX");  // DE TGS EN ARRANQUES SIMPLES DE TGS EN CICLO COMBINADO    

		
		DatosCicloCombCP result = new DatosCicloCombCP(nomPar, tipo, listaAIni,
				horasMax,  nmodAIni,
				costAIni, potAIni,  durAIni,
				postesAIniD, listaASim, nmodASim,
				costASim, potASim, durASim,
				postesASimD, listaPar, nmodPar,
				potPar, durPar,	postesParD, 
				costArrTGAb, horasApagadoCV, nmodCCIni,nmodTGIni, listaEnEjec,
				posteIniEE, cantMab, cantMIni, cantMSim);
		
		System.out.println("Termina lectura de datos CP de CicloComb " + nomPar);
		
		return result;
	}
	
	
	public static DatosImpoExpoCP cargaIECP(String nomPar, String tipoPar) {
		
		ArrayList<String> listaOf = (lector.cargaLista(i, "LISTA_OFERTAS"));
		int cantOf = listaOf.size();
		i++;
		Hashtable<String, String> operacion = new Hashtable<String, String>(); 
		Hashtable<String, String> tipoOf = new Hashtable<String, String>(); 
		Hashtable<String, Boolean> yaConv = new Hashtable<String, Boolean>(); 
		Hashtable<String, Double>  precio = new Hashtable<String, Double>();
		Hashtable<String, Double>  potMax = new Hashtable<String, Double>();
		Hashtable<String, Double>  potMed = new Hashtable<String, Double>();
		Hashtable<String, Double>  multa = new Hashtable<String, Double>();
		Hashtable<String, Integer>  durDias = new Hashtable<String, Integer>();
		Hashtable<String, Integer>  anticipH = new Hashtable<String, Integer>();
		Hashtable<String, Integer>  primerDia = new Hashtable<String, Integer>();
		Hashtable<String, Integer>  ultimoDia = new Hashtable<String, Integer>();
		Hashtable<String, Integer>  diasYa = new Hashtable<String, Integer>();
		Hashtable<String, Double>  enerYaGWh = new Hashtable<String, Double>();
		
		Hashtable<String, String> nomVAPot = new Hashtable<String, String>(); 
		Hashtable<String, String> nomVAPre = new Hashtable<String, String>(); 
		
		for(int il=0; il<cantOf; il++) {			
			String s = listaOf.get(il);
			lector.verificaEtiqueta(i, s);
			String oper = texto.get(i).get(1);
			operacion.put(s, oper);
			String tipo = texto.get(i).get(2);
			tipoOf.put(s, tipo);
			if(tipo.equalsIgnoreCase(ConCP.OALEAT)) {
				nomVAPot.put(s, texto.get(i).get(3));
				nomVAPre.put(s, texto.get(i).get(4));

			}else {
				boolean ya = false;
				if(texto.get(i).get(3).equalsIgnoreCase("SI")) {
					ya = true;   // ya quiere decir oferta ya convocada
				}
				yaConv.put(s, ya);
				precio.put(s, Double.parseDouble(texto.get(i).get(4)));
				potMax.put(s, Double.parseDouble(texto.get(i).get(5)));
				potMed.put(s, Double.parseDouble(texto.get(i).get(6)));
				multa.put(s, Double.parseDouble(texto.get(i).get(7))); 
				durDias.put(s, Integer.parseInt(texto.get(i).get(8)));
				anticipH.put(s, Integer.parseInt(texto.get(i).get(9)));	
				
				if(ya) {
					int dias = Integer.parseInt(texto.get(i).get(12));
					primerDia.put(s, -dias);
					ultimoDia.put(s, -dias);
					diasYa.put(s, dias);
				}else {
					primerDia.put(s, Integer.parseInt(texto.get(i).get(10)));
					ultimoDia.put(s, Integer.parseInt(texto.get(i).get(11)));
					diasYa.put(s, 0);	
				}
				enerYaGWh.put(s, Double.parseDouble(texto.get(i).get(13)));				
				if(yaConv.get(s)==false && (enerYaGWh.get(s)>0 || diasYa.get(s)>0)) {
					System.out.println("Error en dato de convocatoria de oferta " + s);
				}		
			}
			if(il<cantOf-1) i++;
		}
		
		DatosImpoExpoCP result = new DatosImpoExpoCP(nomPar, tipoPar, listaOf, operacion, tipoOf,
				yaConv, precio, potMax, multa, potMed,
				durDias, anticipH, primerDia, ultimoDia, diasYa,
				enerYaGWh, nomVAPot, nomVAPre);
		
		System.out.println("Termina lectura de datos CP de ImpoExpo " + nomPar);
		
		return result;
	}
	
	
	
	public static DatosDemandaCP cargaDemCP(String nomPar, String tipo) {		
		String nomVADem = lector.cargaPalabra(i, "VA_POT");		
		return new DatosDemandaCP(nomPar, tipo, nomVADem);
	}
	
	public static DatosEolicoCP cargaEolicoCP(String nomPar, String tipo) {
		String nomVAFac = lector.cargaPalabra(i, "VA_FACTOR");
		return new DatosEolicoCP(nomPar, tipo, nomVAFac);
		
	}
	
	public static DatosFallaCP cargaFallaCP(String nomPar, String tipo) {
		ArrayList<Integer> escProgramables = new ArrayList<Integer>();
		boolean hayProg = lector.cargaBooleano(i, "ESC_PROGRAMABLES");		
		if(hayProg) {
			for(int j=2; j<texto.get(i).size(); j++) {
				escProgramables.add(Integer.parseInt(texto.get(i).get(j)));
			}			
		}
		i++;
		int diasDecProg = lector.cargaEntero(i, "DIAS_DEC_PROG");
		i++;
		boolean hayEF = lector.cargaBooleano(i, "ESC_FORZADOS");

		ArrayList<Integer> escForzados = new ArrayList<Integer>();
		if(hayEF) {
			for(int j=2; j<texto.get(i).size(); j++) {
				escForzados.add(Integer.parseInt(texto.get(i).get(j)));
			}
		}		
		for(Integer ie: escForzados) {
			if(!escProgramables.contains(ie)) {
				System.out.println("El escalón de falla " + ie + " está forzado y no es programable");
				System.exit(1);
			}
		}
		return new DatosFallaCP(nomPar, tipo , hayProg, diasDecProg, escProgramables, hayEF, escForzados);
	}
	
	
	public static DatosContIntCP cargaContInt(String nomPar, String tipo) {
		String barra = lector.cargaPalabra(i, "BARRA");
		i++;
		double potencia = lector.cargaReal(i, "POTENCIA");
		i++;
		double valTD = lector.cargaReal(i, "VALOR_TOTAL_DESPACHO");
		return new DatosContIntCP(nomPar, tipo, barra, potencia, valTD);
	}
	
	
	public static DatosConstHipCP cargaConstHipCP(String nomPar, String tipo) {
		boolean usoHip = lector.cargaBooleano(i, "USO");
		ArrayList<DatosHiperplanoCP> hiperplanos = null;
		i++;
		String origen = lector.cargaPalabra(i, "ORIGEN");
		boolean origenCorrida = false;
		if(origen.equalsIgnoreCase(ConCP.CORRIDA)) {
			origenCorrida = true;
		}else if(origen.equalsIgnoreCase(ConCP.EXTERNO)) {
			hiperplanos = cargaHiperplanos();
		}else {
			System.out.println("Error en origen de hiperplanos");
			System.exit(1);
		}
		i++;
		boolean vbnoneg = lector.cargaBooleano(i, "VB_NO_NEG");
		return new DatosConstHipCP(nomPar, tipo, usoHip, origenCorrida, hiperplanos, vbnoneg);
	}
	
	public static ArrayList<DatosHiperplanoCP> cargaHiperplanos(){
		ArrayList<DatosHiperplanoCP> result = new ArrayList<DatosHiperplanoCP>();
		ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(dirEntradas + "/hiperplanos.txt");	
		int i = 0;
		if(! texto.get(i).get(0).equalsIgnoreCase("CANT_VE")) {
			System.out.println("Error en cantidad de variables de estado en hiperplanos.txt");
			System.exit(1);
		}
		int cantVE = Integer.parseInt(texto.get(i).get(1));
		i++;
		
		String[] nomVE = new String[cantVE];
		for(int j=0; j<cantVE; j++) {
			nomVE[j] = texto.get(i).get(j);
		}
		i++;
		do {
			double[] puntoSoporte = new double[cantVE];
			for(int j=0; j<cantVE; j++) {
				puntoSoporte[j] = Double.parseDouble(texto.get(i).get(j));
			}
			double[] coeficientes = new double[cantVE+1];
			for(int j=0; j<= cantVE; j++) {
				coeficientes[j] = Double.parseDouble(texto.get(i).get(j+cantVE));
			}			
			DatosHiperplanoCP hip = new DatosHiperplanoCP(nomVE, puntoSoporte, coeficientes);
			result.add(hip);
			i++;
		}while(i<texto.size()-2);
		return result;
	}
	
	
	

}
