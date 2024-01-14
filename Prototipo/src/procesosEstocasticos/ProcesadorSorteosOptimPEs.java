/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesadorSorteosOptimPEs is part of MOP.
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
import java.util.Hashtable;

import datatypesSalida.DatosPEsUnEscenario;
import datatypesSalida.DatosPEsUnPaso;
import matrices.Oper;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.DirectoriosYArchivos;
import utilitarios.UtilArrays;

public class ProcesadorSorteosOptimPEs {
	
	

	private int cantPasos;
	
	private LineaTiempo lt;
	
	private ArrayList<ProcesoEstocastico> listaPEs; // lista de PEs de los que se saca la simulación
	
	private Hashtable<String, ProcesoEstocastico> tablaPEs; // tabla de los PEs, clave nombre PE
	
	/**
	 * Lista de combinaciones (nombre del PE + "-" + nombre variable aleatoria del PE)
	 */
	private ArrayList<String> nombresPEyVA;  
	
	private ArrayList<Integer> numerosPasos;  // los números de los pasos que se van generando
	
	private ArrayList<DatosPEsUnPaso> resultados;  // resultados de un escenario de simulación
		
	
		
	
	public ProcesadorSorteosOptimPEs(LineaTiempo lt, ArrayList<ProcesoEstocastico> listaPEs){
		this.lt = lt;
		this.cantPasos = lt.getCantidadPasos();
		numerosPasos = new ArrayList<Integer>();
		resultados = new ArrayList<DatosPEsUnPaso>();
		this.listaPEs = listaPEs;
		tablaPEs = new Hashtable<String, ProcesoEstocastico>();
		nombresPEyVA = new ArrayList<String>(); 
		
		for(ProcesoEstocastico p: listaPEs){
			tablaPEs.put(p.getNombre(), p);
			for(VariableAleatoria va: p.getVariablesAleatorias()){
				nombresPEyVA.add(clavePEVA(p,va));
			}
		}		
	}
	
	
	

	
	/**
	 * Construye la clave en el Hashtable a partir de los nombres
	 * del PE y la VA
	 */
	public static String clavePEVA(ProcesoEstocastico p, VariableAleatoria va){
		return p.getNombre() + "-" + va.getNombre();
	}
		
//	public void inicializarPaso(int numPaso){
//		d1PasoCorr = new DatosPEsUnPaso(numPaso, nombresPEyVA);	
//		listaEstadosCorr = new ArrayList<double[]>();
//	}
	
	
	public void imprimirSorteosOptim(String dirSalidas){
		for(ProcesoEstocastico pe: listaPEs){
			String nomPE = pe.getNombre();
			String dirPE = dirSalidas + "/" + nomPE;
			DirectoriosYArchivos.creaDirectorio(dirSalidas, nomPE);
			for(VariableAleatoria va: pe.getVariablesAleatorias()){
				String archVA = dirPE + "/" + va.getNombre() + ".xlt";
				DirectoriosYArchivos.siExisteElimina(archVA);
				StringBuilder sb = new StringBuilder();
				sb.append("PASO\t");
				sb.append("ESTADO, SORTEO, VALOR DE LA VA\n");
				String clave = clavePEVA(pe,va);
				for(int ip=0; ip<numerosPasos.size();ip++){
					int numpaso = numerosPasos.get(ip);
					DatosPEsUnPaso d1P = resultados.get(ip);
					ArrayList<int[]> estados = d1P.getEstados();
					ArrayList<double[][]> valores = d1P.getValores().get(clave);					
					for(int ie=0; ie<estados.size(); ie++){
						int[] codigoEst = estados.get(ie);
						double[][] aux = valores.get(ie);
						for(int isort=0; isort<aux.length; isort++){
							sb.append(numpaso + "\t");
							for(int ive=0; ive<codigoEst.length; ive++){
								sb.append(codigoEst[ive]+"\t");
							}
							sb.append(isort+"\t");
							for(int im=0; im<aux[isort].length;im++){
								sb.append(aux[isort][im] + "\t");
							}
							sb.append("\n");
						}
						sb.append("\n");
					}				
				}			
				DirectoriosYArchivos.siExisteElimina(archVA);
				DirectoriosYArchivos.agregaTexto(archVA, sb.toString());
			}

		}
	}
	
	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

	public LineaTiempo getLt() {
		return lt;
	}

	public void setLt(LineaTiempo lt) {
		this.lt = lt;
	}

	public ArrayList<ProcesoEstocastico> getListaPEs() {
		return listaPEs;
	}

	public void setListaPEs(ArrayList<ProcesoEstocastico> listaPEs) {
		this.listaPEs = listaPEs;
	}

	public Hashtable<String, ProcesoEstocastico> getTablaPEs() {
		return tablaPEs;
	}

	public void setTablaPEs(Hashtable<String, ProcesoEstocastico> tablaPEs) {
		this.tablaPEs = tablaPEs;
	}

	public ArrayList<String> getNombresPEyVA() {
		return nombresPEyVA;
	}

	public void setNombresPEyVA(ArrayList<String> nombresPEyVA) {
		this.nombresPEyVA = nombresPEyVA;
	}

	public ArrayList<DatosPEsUnPaso> getResultados() {
		return resultados;
	}

	public void setResultados(ArrayList<DatosPEsUnPaso> resultados) {
		this.resultados = resultados;
	}

	public ArrayList<Integer> getNumerosPasos() {
		return numerosPasos;
	}

	public void setNumerosPasos(ArrayList<Integer> numerosPasos) {
		this.numerosPasos = numerosPasos;
	}
	


//	/**
//	 * Escribe las salidas de texto y crea las estructuras de datos de salida 
//	 * para estimar estadísticos de un proceso estocástico simulado.
//	 * @param dirSalidas
//	 */
//	public void escribeValores1PE(ProcesoEstocastico p, String dirSalidas){
//		String dirArchChan = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/PRUEBAS/observPorPaso.xlt";
//
//		
//		String nomDir = dirSalidas + "/" + p.getNombre();
//		if(!DirectoriosYArchivos.existeDirectorio(nomDir)){
//			DirectoriosYArchivos.creaDirectorio(dirSalidas, p.getNombre());
//		}
//		
//		if(tipoSimulPE.equals(utilitarios.Constantes.ESC_UNICO)){
//			seriesSimul = new Hashtable<String,Serie>();
//		}else if(tipoSimulPE.equals(utilitarios.Constantes.ESC_MULT)){
//			hazEscSimul = new Hashtable<String, HazEsc>();
//		}
//				
//		for(VariableAleatoria va: p.getVariablesAleatorias()){
//			ArrayList<Integer> anioT = new ArrayList<Integer>();
//			ArrayList<Integer> pasoT = new ArrayList<Integer>();
//			ArrayList<double[]> datosT = new ArrayList<double[]>();
//			if(tipoSimulPE.equals(utilitarios.Constantes.ESC_UNICO)){
//				Serie s1 = new Serie(p.getNombrePaso());
//			}else{
//				HazEsc s1 = new HazEsc(p.getNombrePaso());
//			}			
//			String nva = va.getNombre();
//			String nomArch = nomDir+"/"+nva+".xlt";
//			if(DirectoriosYArchivos.existeArchivo(nomArch)){
//				DirectoriosYArchivos.eliminaArchivo(nomArch);
//			}
//			StringBuilder sb = new StringBuilder();
//			sb.append("SIMULACION DEL PROCESO " + p.getNombre() + "\tVARIABLE "+va.getNombre()+"\tEstimación " + p.getNombreEstimacion());
//			sb.append("\n");
//			sb.append("numpaso \tfecha inicial\tint.muestreo\t");
//			for(int ie=0; ie<cantEsc; ie++){
//				sb.append("ESC-"+ie+"\t");
//			}
//			sb.append("PROMEDIO");
//			sb.append("\n");
//
//			lt.reiniciar();
//			PasoTiempo paso = lt.devuelvePasoActual();
//			int numpaso = 0;
//			int t=0;
//			int ims =0; // índice de instantes de muestreo de la simulación desde su inicio.
//			StringBuilder sb2 = new StringBuilder(nva + "\n"); 
//			while (paso != null){
//				long instante = paso.getInstanteInicial();
//				int cantIM = paso.getDuracionPaso()/paso.getIntervaloMuestreo();
//				int cantEsc = 1;   // cantEsc es la cantidad de valores que se escriben por paso
//				int salto = cantIM;
//				if(p.isMuestreado()){
//					cantEsc = cantIM;
//					salto = 1;
//				}
//				for(int im=0; im<cantEsc; im++){
//					int pasoAnio = p.pasoDelAnio(instantesDeMuestreoSimulacion.get(ims))+1;// en las series el paso del año empieza en 1
//					pasoT.add(pasoAnio); 
//					anioT.add(p.anioDeInstante(instante));
//					sb.append(numpaso+"\t");
//					sb.append(lt.fechaYHoraDeInstante(paso.getInstanteInicial()) +"\t");
//					sb.append(im + "\t");
//					double prom = 0.0;
//					double[] auxT = new double[cantEsc];
//					double val=0.0;
//					for(int ie=0; ie<cantEsc; ie++){	
//						val = resultados.get(ie).getValores().get(clavePEVA(p,va))[numpaso][im];
//						auxT[ie] = val;
//						prom += val;
//						sb.append(val);
//						sb.append("\t");						 
//					}
//					sb2.append(pasoAnio + "\t" + val + "\n");  
//					datosT.add(auxT);
//					sb.append(prom/cantEsc);
//					ims=ims+salto;
//				}						
//				lt.avanzarPaso();
//				paso = lt.devuelvePasoActual();				
//				numpaso++;	
//				sb.append("\n");		
//			}	
//			DirectoriosYArchivos.agregaTexto(nomArch, sb.toString());
//			int[] anioA = utilitarios.UtilArrays.dameArrayI(anioT);
//			int[] pasoA = utilitarios.UtilArrays.dameArrayI(pasoT);
//			double[][] datosA = UtilArrays.dameArrayDobleD(datosT);
//			
//		
//			DirectoriosYArchivos.agregaTexto(dirArchChan, sb2.toString());
//			
//			
//			String clave = clavePEVA(p, va);
//			if(tipoSimulPE.equals(utilitarios.Constantes.ESC_UNICO)){
//				Serie s1 = new Serie(va.getNombre(), p.getNombrePaso(), 
//						Oper.devuelveCol(datosA, 0), anioA, pasoA);
//				seriesSimul.put(clave, s1);				
//			}else if(tipoSimulPE.equals(utilitarios.Constantes.ESC_MULT)){
//				datosA = UtilArrays.dameArrayDobleD(datosT);
//				HazEsc h1 = new HazEsc(va.getNombre(), p.getNombrePaso(), datosA, anioA, pasoA);
//				hazEscSimul.put(clave, h1);
//			}						
//		}	
//	}		

}
