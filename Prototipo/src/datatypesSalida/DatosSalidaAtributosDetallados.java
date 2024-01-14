/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosSalidaAtributosDetallados is part of MOP.
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

package datatypesSalida;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Contiene para cada atributo detallado de un participante (escalón de falla o barra) los resultados
 * por paso, escenario (y poste si corresponde) y los mismos resultados pero ordenando 
 * los escenarios en orden creciente del valor en cada paso (sufijo PERC)
 * 
 * Los atributos detallados son los que aparecen en utilitarios.Constantes para cada Participante
 * 
 * @author ut469262
 *
 */
public class DatosSalidaAtributosDetallados { 
	
	private boolean porPoste;  // si es false hay un solo valor por paso
	private double[][][] atributos; // primer índice paso, segundo índice escenario, tercer índice poste, eventualmente el único.
	private double[][][] atributosPerc; // primer índice paso, segundo índice ordenado según el valor creciente, tercer índice poste, eventualmente el único.
	private double[][] medias; // promedio en los escenarios, primer índice recorre los pasos, segundo índice poste eventualmente el único.  
	private String[] instIni;  // instantes iniciales de los pasos; solo se carga al leer de texto escrito
	private String titulo;  // titulo de las salidas
	public boolean isPorPoste() {
		return porPoste;
	}

	public void setPorPoste(boolean porPoste) {
		this.porPoste = porPoste;
	}

	public double[][][] getAtributos() {
		return atributos;
	}


	public void setAtributos(double[][][] atributos) {
		this.atributos = atributos;
	}


	public double[][][] getAtributosPerc() {
		return atributosPerc;
	}



	public void setAtributosPerc(double[][][] atributosPerc) {
		this.atributosPerc = atributosPerc;
	}
	

	public double[][] getMedias() {
		return medias;
	}

	public void setMedias(double[][] medias) {
		this.medias = medias;
	}
	

	public String[] getInstIni() {
		return instIni;
	}

	public void setInstIni(String[] instIni) {
		this.instIni = instIni;
	}
	
	
	
	

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	/**
	 * Ordena el índice de la segunda dimensión según valor creciente, para todos
	 * los valores de las dimensiones 1 y 3.
	 * 
	 * Por ejemplo si la dimensión 2 es filas y la 3 es columnas y se tiene
	 * la matriz original:
	 * 3 5 8 0
	 * 5 1 0 4
	 * 0 9 3 7
	 * se devuelve la matriz
	 * 0 1 0 0
	 * 3 5 3 4
	 * 5 9 8 7
	 * 
	 * @param matriz
	 * @return
	 */
	public static double[][][] ordenaValorCrecienteD2(double[][][] matriz){
		double[][][] result = matriz.clone();
		int dim1 = matriz.length;
		int dim2 = matriz[0].length;
		for(int i1=0; i1< dim1; i1++){
			int dim3 = matriz[i1][0].length;
			for(int i3=0; i3<dim3; i3++){
				ArrayList<Double> ordenador = new ArrayList<Double>();
				for(int i2=0; i2<dim2; i2++){
					ordenador.add(matriz[i1][i2][i3]);
				}
				Collections.sort(ordenador);
				for(int i2=0; i2<dim2; i2++){
					result[i1][i2][i3]=ordenador.get(i2);
				}
			}
		}
		return result;		
	}
	
	/**
	 * Calcula la media de los valores de la segunda dimensión de matriz, para 
	 * todos los valores de la primera y la tercera dimensión
	 * @param matriz
	 * @return
	 */
	public static double[][] calculaMediaD2(double[][][] matriz){
		int dim1 = matriz.length;
		int dim2 = matriz[0].length;
		double[][] result = new double[dim1][];
		for(int i1=0; i1< dim1; i1++){
			int dim3 = matriz[i1][0].length;
			double[] aux = new double[dim3];
			for(int i3=0; i3<dim3; i3++){
				double media = 0.0;
				for(int i2=0; i2<dim2; i2++){
					media+= matriz[i1][i2][i3];
				}
				media = media/dim2;
				aux[i3]= media;
			}
			result[i1] = aux;
		}
		return result;						
	}
	
	
	
	/**
	 * Suma a this los datos de sumando para todo paso, escenario y poste 
	 * en el double[][][] atributosatributos
	 * @param args
	 */
	public void sumaDatos(DatosSalidaAtributosDetallados sumando){
		int dim1 = this.atributos.length;
		int dim2 = this.atributos[0].length;
		int dim3 = this.atributos[0][0].length;
		for(int i1=0; i1<dim1; i1++){
			for(int i2=0; i2<dim1; i2++){
				for(int i3=0; i3<dim1; i3++){
					this.atributos[i1][i2][i3] += sumando.getAtributos()[i1][i2][i3];
				}
			}
		}
	}
	
	
	public static void main(String[] args){
		double[][][] mat = new double[2][3][2];
		StringBuilder sb = new StringBuilder();
		for(int i1=0; i1<2; i1++){
			sb.append("i1 = " + i1 + "\n");
			for(int i2=0; i2<3; i2++){
				for(int i3=0; i3<2; i3++){
					mat[i1][i2][i3] = (i1+1)*(3-i2)+i3;
					sb.append(mat[i1][i2][i3]+ "\t");
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
		System.out.print(sb.toString());
		double[][][] result = ordenaValorCrecienteD2(mat);
		sb = new StringBuilder();
		for(int i1=0; i1<2; i1++){
			sb.append("i1 = " + i1 + "\n");
			for(int i2=0; i2<3; i2++){
				for(int i3=0; i3<2; i3++){
					sb.append(result[i1][i2][i3]+ "\t");
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
		System.out.print(sb.toString());	
		
		System.out.println("MEDIAS");
		double[][] medias = calculaMediaD2(mat);
		sb = new StringBuilder();
		for(int i1=0; i1<2; i1++){
			sb.append("i1 = " + i1 + "\n");			
			for(int i3=0; i3<2; i3++){
				sb.append(medias[i1][i3]+ "\t");
			}
			sb.append("\n");
		}		
		System.out.print(sb.toString());	
	}
	
	
	
	
}
