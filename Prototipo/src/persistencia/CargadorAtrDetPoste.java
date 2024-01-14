/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorAtrDetPoste is part of MOP.
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

import java.util.ArrayList;

import datatypesProcEstocasticos.DatosAgregadorLineal;
import datatypesSalida.DatosSalidaAtributosDetallados;
import utilitarios.LeerDatosArchivo;
import utilitarios.UtilArrays;

public class CargadorAtrDetPoste {
	
	/**
	 * Crea un datatype DatosSalidaAtributosDetallados a partir de un archivo de atributos detallados por poste
	 * escrito por la clase EscritorResumenSimulacionParalelo
	 */
	
//	private boolean porPoste;  // si es false hay un solo valor por paso
//	private double[][][] atributos; // primer índice paso, segundo índice escenario, tercer índice poste, eventualmente el único.
//	private double[][][] atributosPerc; // primer índice paso, segundo índice ordenado según el valor creciente, tercer índice poste, eventualmente el único.
//	private double[][] medias; // promedio en los escenarios, primer índice recorre los pasos, segundo índice poste eventualmente el único.  

	/**
	 * Lee una archivo de atributos detallados de una línea por escenario OBSOLETO, AHORA ES UNA LINEA POR POSTE
	 * @param arch
	 * @return
	 */
	public static DatosSalidaAtributosDetallados devuelveDatosAtrDetPoste(String arch) {
		
		DatosSalidaAtributosDetallados dad = new DatosSalidaAtributosDetallados();		
		int cantLC = EscritorResumenSimulacionParalelo.getCantLineasCabezal();		
		ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(arch);
		ArrayList<String> instIni = new ArrayList<String>(); 
		int cantEsc = Integer.parseInt(texto.get(cantLC-2).get(3));
		int cantPasos = texto.size()-cantLC-1;
		int cantPostesCorr = 0;
		double[][][] atributos = new double[cantPasos][cantEsc][];
		double[][] medias = new double[cantPasos][];
		int ifil = cantLC;
		int ipas = 0;
		while(ifil<texto.size()) {
			if(texto.get(ifil).get(0).equalsIgnoreCase("CANT_POSTES")) {
				cantPostesCorr = Integer.parseInt(texto.get(ifil).get(1));
				ifil++;
			}else {
				instIni.add(texto.get(ifil).get(3));			
				int icol = 2;
				double[] medAux = new double[cantPostesCorr];
				for(int j=0; j<cantPostesCorr; j++) {
					medAux[j] = Double.parseDouble(texto.get(ifil).get(icol));
					icol++;
				}
				medias[ipas] = medAux;
				for(int iesc=0; iesc<cantEsc; iesc++) {
					double[] aux1e = new double[cantPostesCorr];
					for(int j=0; j<cantPostesCorr; j++) {
						aux1e[j] = Double.parseDouble(texto.get(ifil).get(icol));
						icol++;
					}
					atributos[ipas][iesc]=aux1e;
				}
				ipas++;
				ifil++;
			}
		
		}
		
		String[] instIA = UtilArrays.dameArrayS(instIni);
		dad.setAtributos(atributos);
		dad.setMedias(medias);
		dad.setInstIni(instIA);
		return dad;
	}
	
	
	/**
	 * Lee un archivo de atributos detallados por paso, poste y escenario CON UNA LINEA POR POSTE
	 * @param arch
	 * @return
	 */
	public static DatosSalidaAtributosDetallados devuelveDatosAtrDetPoste1LineaPorPoste(String arch) {
		
		DatosSalidaAtributosDetallados dad = new DatosSalidaAtributosDetallados();		
		int cantLC = EscritorResumenSimulacionParalelo.getCantLineasCabezal();		
		ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(arch);
		ArrayList<String> instIni = new ArrayList<String>(); 
		int cantEsc = Integer.parseInt(texto.get(cantLC-2).get(1));
		int cantPasos = Integer.parseInt(texto.get(cantLC-1).get(1));
		int cantPostesCorr = 0;
		double[][][] atributos = new double[cantPasos][cantEsc][];
		double[][] medias = new double[cantPasos][];
		int ifil = cantLC+1;
		int ipasant = -1;
		double[] medaux = null;
		double[][] aux1paso = new double[cantEsc][cantPostesCorr];;
		while(ifil<texto.size()) {			
			if(texto.get(ifil).get(0).equalsIgnoreCase("CANT_POSTES")) {
				cantPostesCorr = Integer.parseInt(texto.get(ifil).get(1));
				ifil = ifil + 2;
				medaux = new double[cantPostesCorr];   // almacena las medias de cada poste del paso
				aux1paso = new double[cantEsc][cantPostesCorr];
			}else {
				int ipaso = Integer.parseInt(texto.get(ifil).get(0));
				if(ipaso!=ipasant) {					
					if(ipasant!=-1) {
						medias[ipasant] = medaux;
						atributos[ipasant] = aux1paso;						
					}
					instIni.add(texto.get(ifil).get(1));
					ipasant = ipaso;	
				}				  
				for(int ipos = 0; ipos<cantPostesCorr; ipos++) {
					medaux[ipos] = Double.parseDouble(texto.get(ifil).get(3));
					int icol = 0;								
					for(int iesc=0; iesc<cantEsc; iesc++) {
						aux1paso[iesc][ipos]= Double.parseDouble(texto.get(ifil).get(4+icol));	
						icol++;
					}					
					ifil++;
				}				
			}
		}
		medias[ipasant] = medaux;
		atributos[ipasant] = aux1paso;	
		String[] instIA = UtilArrays.dameArrayS(instIni);
		dad.setAtributos(atributos);
		dad.setMedias(medias);
		dad.setInstIni(instIA);
		return dad;
	}
	
	
	
	
	public static void main(String[] args) {
		
		String nom = "potencias_PTigreB_PRUEBA-SALIDAS-MAYO2022_2022-5-19_12-38-28-chico.xlt";
		String dir = "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\PRUEBAS\\PRUEBAS-SALIDAS-MAYO22";
		String nomArch = dir + "/" + nom;
		
		DatosSalidaAtributosDetallados dad = devuelveDatosAtrDetPoste(nomArch);
		System.out.println("termina lectura de salida de atributos detallados");
		
		
		
	}
	

}
