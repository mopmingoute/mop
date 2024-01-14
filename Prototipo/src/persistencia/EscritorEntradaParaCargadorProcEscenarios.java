/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorEntradaParaCargadorProcEscenarios is part of MOP.
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

import utilitarios.DirectoriosYArchivos;

public class EscritorEntradaParaCargadorProcEscenarios {

	/**
	 * 
	 * @param dirArchivo
	 * @param titulo
	 * @param nombrePaso
	 * @param anioIni
	 * @param cantAnios
	 * @param cantPasosPorAnio cantPasosPorAnio para cada año la cantidad de pasos
	 * @param cantEsc
	 * @param cantVE
	 * @param datos primer índice año, segundo índice escenario, tercer índice paso, cuarto índice variable aleatoria
	 */
	public static void escribeEntradaPEscenarios(String dirArchivo, String titulo, String nombrePaso, int anioIni, int cantAnios, int[] cantPasosPorAnio, int cantEscTot, int cantEscUsados, int cantVA, double[][][][] datos) {
		DirectoriosYArchivos.siExisteElimina(dirArchivo);
		DirectoriosYArchivos.agregaTexto(dirArchivo, titulo);
		for(int ian=0; ian<cantAnios; ian++) {
			for(int iesc=0; iesc<cantEscTot; iesc++) {
				System.out.println("Escribiendo año " + ian + " escenerio " + iesc);
				StringBuilder sb = new StringBuilder();
				sb.append((anioIni+ian) + "\t" + (iesc+1) +"\t" );
				for(int iv=0; iv<cantVA; iv++) {				
					for(int ip=0; ip<cantPasosPorAnio[ian]; ip++) {
						sb.append(datos[ian][iesc][ip][iv]+"\t");						
					}
					DirectoriosYArchivos.agregaTexto(dirArchivo, sb.toString());
				}				
			}			
		}
	}
}
