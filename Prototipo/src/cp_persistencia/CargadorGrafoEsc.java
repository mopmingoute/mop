/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorGrafoEsc is part of MOP.
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

import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosGrafoEscCP;
import cp_datatypesEntradas.DatosMatPTrans;
import cp_despacho.DespachoProgEstocastica;
import cp_despacho.GrafoEscenarios;
import procesosEstocasticos.MatPTrans;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.AsistenteLectorTextos;
import utilitarios.LeerDatosArchivo;

public class CargadorGrafoEsc {
	
	private static final int COLINI = 2;
	
	
	public static DatosGrafoEscCP devuelveGrafoEsc(String dirEntrada, int cantEtapas, int[] cantPosEtapas) {
		
		DatosGrafoEscCP dat = new DatosGrafoEscCP();
				
		// Lee valores del grafo por etapa y escenario
		
		String dirArchivo = dirEntrada + "/GrafoEscenarios.txt";		
        ArrayList<ArrayList<String>> texto;       
        texto = LeerDatosArchivo.getDatos(dirArchivo);
        
        AsistenteLectorEscritorTextos lector = new AsistenteLectorEscritorTextos(texto, dirArchivo);   
        int i=0;
        ArrayList<String> nombresVA = new ArrayList<String>();
        for(int j=1; j<texto.get(i).size(); j++) {
        	nombresVA.add(texto.get(i).get(j));  
        }
        
        int cantVA = nombresVA.size();
                
    	/**
    	 * Clave: nombreVA + "_" + etapa + "_" + escenario
    	 *  etapa entero de 0 hasta cantEtapas-1
    	 *  escenario entero de 0 hasta cantEscEtapa[etapa]-1
    	 * Valor: double[] con los valores de la variable aleatoria nombreVA por poste en la etapa y el escenario
    	 */
        Hashtable<String, double[]> valoresVA = new Hashtable<String, double[]>();
        
        i++;
		int[] cantEscEtapa = new int[cantEtapas];
		for(int j=0; j<cantEtapas; j++) {
			cantEscEtapa[j] = Integer.parseInt(texto.get(i).get(j+1));
		}
		
		dat.setCantEscEtapa(cantEscEtapa);
		
		i++;
		for(int iva=0; iva<cantVA; iva++) {
			if(!texto.get(i).get(0).equalsIgnoreCase(nombresVA.get(iva))) {
				System.out.println("Error en entrada de variable aleatoria : " + texto.get(i).get(0));
				System.exit(1);
			}else {
				i++;
			}
			for(int iet=0; iet<cantEtapas; iet++) {
				if(Integer.parseInt(texto.get(i).get(1))!= iet) {
					System.out.println("Error en entrada de variable aleatoria : " + nombresVA.get(iva) + " etapa " +  iet);
					System.exit(1);
				}
				int n = cantPosEtapas[iet];
				for(int iesc=0; iesc<cantEscEtapa[iet]; iesc++) {
					if(Integer.parseInt(texto.get(i).get(3))!= iesc) {
						System.out.println("Error en entrada de variable aleatoria : " + nombresVA.get(iva) + " etapa " +  iet + " escenario " + iesc);
						System.exit(1);
					}					
					double[] aux = new double[n];
					for(int j=0; j<n; j++) {
						aux[j] = Double.parseDouble(texto.get(i).get(5+j));
					}
					String clave = cp_despacho.GrafoEscenarios.claveEnGrafo(nombresVA.get(iva), iet, iesc);
					
 					i++;
					valoresVA.put(clave, aux);
				}		
			}			
		}
				
		dat.setCantEscEtapa(cantEscEtapa);
		dat.setNombresVA(nombresVA);
		dat.setValoresVA(valoresVA);
		
		
		// lee matrices de transición
				
       ArrayList<MatPTrans> matrices = new ArrayList<MatPTrans>(); 
        
        dirArchivo = dirEntrada + "/MatPTrans.txt";		
        texto = LeerDatosArchivo.getDatos(dirArchivo);
        lector = new AsistenteLectorEscritorTextos(texto, dirArchivo);
        
        i = 0;
        for(int iet=0; iet<cantEtapas-1; iet++){
        	int etapaLeida = lector.cargaEntero(i, "ETAPA_INICIAL");
        	i++;
        	if(etapaLeida!=iet) {
        		System.out.println("ERROR EN LECTURA MATRIZ DE TRANSICIÓN ETAPA " + iet);
        	}
        			
        	int nfil = cantEscEtapa[iet];
        	int ncol = cantEscEtapa[iet+1];
        	
        	MatPTrans mpt = new MatPTrans(texto, i, COLINI, nfil, ncol);
        	matrices.add(mpt);
        	i = i + nfil;
        }
        dat.setMatTransicion(matrices);
		
		System.out.println("Termina la lectura del grafo de escenarios");
		return dat;
	}
	
	

}
